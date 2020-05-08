 package org.cloudsicle.master;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.cloudsicle.communication.SocketSender;
 import org.cloudsicle.main.jobs.CombineJob;
 import org.cloudsicle.main.jobs.CompressJob;
 import org.cloudsicle.main.jobs.DownloadJob;
 import org.cloudsicle.main.jobs.ForwardJob;
 import org.cloudsicle.main.jobs.IJob;
 import org.cloudsicle.master.slaves.ResourcePool;
 import org.cloudsicle.master.slaves.SlaveVM;
 import org.cloudsicle.messages.Activity;
 import org.cloudsicle.messages.Allocation;
 import org.cloudsicle.messages.JobMetaData;
 import org.opennebula.client.ClientConfigurationException;
 
 import com.jcraft.jsch.JSchException;
 
 public class Scheduler implements Runnable {
 
 	private ResourcePool pool;
 	private ArrayDeque<JobMetaData> metaJobQueue;
 	private Monitor monitor;
 
 	/**
 	 * Instantiate a new Scheduler.
 	 * @param monitor 
 	 * 
 	 * @throws ClientConfigurationException
 	 */
 	public Scheduler(Monitor monitor) throws ClientConfigurationException {
 		this.pool = new ResourcePool();
 		this.metaJobQueue = new ArrayDeque<JobMetaData>();
 		this.monitor = monitor;
 		new Thread(this).start();
 	}
 
 	/**
 	 * Add JobMetaData that has to be scheduled.
 	 * 
 	 * @param metajob
 	 */
 	public void schedule(JobMetaData metajob) {
 		this.metaJobQueue.push(metajob);
 	}
 	
 	public void vmFailed(int vmId){
 		SlaveVM vm = pool.getVMById(vmId);
 		JobMetaData job = this.monitor.jobFailed(vmId);
 		vm.initialize(); //redeploy jar
 		schedule(job); //reschedule job
 	}
 	
 	public void vmIsDone(int vmId){
 		SlaveVM vm = pool.getVMById(vmId);
 		this.monitor.moveJobToFinished(vm.getAssignedJob());
 		pool.releaseVM(vm);
 
 	}
 	
 	public void hardExit(){
 		pool.exit();
 	}
 
 	@Override
 	public void run() {
 		/**
 		 * Scheduling (first veeery rough version): - find VM that is not in use
 		 * |-> if none available, add new VM to pool and use that one |-> if
 		 * pool full, wait (FCFS) - set the VM status to unavailable - send the
 		 * VM ID/IP to the client
 		 */
 		while (true) {
 			synchronized (this.metaJobQueue) {
 				if (!this.metaJobQueue.isEmpty()) {
 					JobMetaData metajob = this.metaJobQueue.pop();
 					System.out.println("DEBUG: Sheduling job of "
 							+ metajob.getSender());
 
 					SlaveVM vm = this.pool.requestVM();
 					vm.assignJob(metajob);
 					Allocation alloc = new Allocation();
 					alloc.allocate(vm, metajob.getFiles()); // for now just
 																// give
 																// everything to
 																// one vm
 					createActivity(metajob, alloc);					
 				}
 			}
 		}
 	}
 	
 	private void createActivity(JobMetaData meta, Allocation alloc) {
 
 		ArrayList<IJob> list = new ArrayList<IJob>();
 		HashMap<Integer, HashMap<Integer, String>> allocs = alloc.getAllocations();
 
 		for (Integer vmId : allocs.keySet()) {
 			SlaveVM vm = this.pool.getVMById(vmId);
 			
 			SocketSender sender = new SocketSender(true, vm.getIp());
 
 			ArrayList<Integer> filelist = new ArrayList<Integer>();
 			HashMap<Integer, String> files =  allocs.get(vm.getId());
 			filelist.addAll(files.keySet());
 			DownloadJob d = new DownloadJob(filelist, meta.getSender());
 			CombineJob c = new CombineJob(filelist);
 			CompressJob comp = new CompressJob();
			ForwardJob f = new ForwardJob(false);
 			c.setIP(meta.getSender());
 			comp.setIP(meta.getSender());
 			f.setIP(meta.getSender());
 			f.setRemoteFileName(vm.getIp().getHostAddress()); //Have the output file be $VM_IP.tar.gz
 			list.add(d);
 			list.add(c);
 			list.add(comp);
 			list.add(f);
 			
 			Activity activity = new Activity(list);
 			activity.setClient(meta.getSender());
 			activity.setVM(vm);
 
 			try {
 				System.out.println("Sending Activity to "
 						+ vm.getId() + "@" + vm.getIp().getHostAddress());
 				sender.send(activity, true);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSchException e) {
 				e.printStackTrace();
 			}
 		}
 		monitor.moveJobToRunning(meta.getId());
 	}
 }
