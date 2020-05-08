 package vcluster.control;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.SortedMap;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import org.opennebula.client.Client;
 import org.opennebula.client.OneResponse;
 import org.opennebula.client.vm.VirtualMachine;
 
 import vcluster.control.VMMessage.VMMsgType;
 import vcluster.control.cloudman.Cloud;
 import vcluster.global.Config;
 import vcluster.global.Config.CloudType;
 import vcluster.global.Config.VMState;
 import vcluster.ui.Command;
 import vcluster.util.PrintMsg;
 import vcluster.util.PrintMsg.DMsgType;
 
 public class VMManager extends Thread {
 
 	public VMManager()
 	{
 		vmList = new TreeMap<Integer, VMelement>();
 		
 		msgQueue =  new ArrayBlockingQueue <VMMessage>(100);
 		vecTempID = new Vector<Integer>();
 	}
 
 	public BlockingQueue <VMMessage> getMsgQueue() {
 		return msgQueue;
 	}
 
 	public void run() {
 		VMMessage aMsg = null;
 		while(!done) {
 			try {
 				aMsg = msgQueue.take();
 				PrintMsg.print(DMsgType.MSG, "message type = "+aMsg.toString());
 				processMessage(aMsg);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private boolean processMessage(VMMessage aMsg) {
 		
 		VMMsgType aType = aMsg.getMessageType();
 		int numVMs = aMsg.getPrivateData();
 		
 		switch(aType) {
 		case LAUNCH: return createVM(numVMs+"");
 		}
 		return false;
 	}
 	
 	public void dump()
 	{
 		System.out.println("----------------------------------------");
 		System.out.println("\tVM Manager");
 		System.out.println("----------------------------------------");
 
 		Vector <Integer>v = new Vector <Integer>(vmList.keySet());
 		Collections.sort(v);
 		Iterator <Integer> it = v.iterator();
 		while(it.hasNext()) {
 			Integer key = it.next();
 	    	VMelement vm = (VMelement)vmList.get(key);
 			System.out.println("\tID: "+vm.getId()+"\t"+vm.getState());
 		}
 		System.out.println("----------------------------------------");
 	}
 	
 	public void shutDwon() 
 	{
 		/* notify to the other services, e.g. monitoring */
 		msgQueue.clear();
 		msgQueue = null;
 		
 		done = true;
 	}
 	
 	public boolean addVMElement(int id,VMelement vm) 
 	{
 		Object ret = vmList.put(new Integer(id), vm);
 		if (ret != null) return false;
 		return true;
 	}
 	
 	private boolean RESTLaunch(Cloud cloud, int numVMs)
 	{
 		PrintMsg.print(DMsgType.MSG, "launching "+numVMs+" vms using REST API.");
 	    
 	    for(int i = 0 ; i < numVMs; i++) {
 	    	//PluginManager.current_cloudExecutor.rest_launch(cloud);
 	    }
 	    
 	    return true;
 	}
 
 	
 	private boolean isAllVMRunning(Client oneClient) {
 
 		int vmID;
 		boolean allRunning = true;
 		for(int i = 0; i < vecTempID.size(); i++) {
 			vmID = vecTempID.elementAt(i).intValue();
 			
 			VirtualMachine vm = new VirtualMachine(vmID, oneClient);
 			
 			/* to load the data first using the info method */
 			OneResponse rc = vm.info();
 			String status = vm.status();
 			
 			PrintMsg.print(DMsgType.MSG, "vm "+vm.getName()+", id " + vm.getId()
 					+", status = "+status);
 
 			VMelement element = findVMElement(vmID);
 			VMState newVMState = getVMState(status);
 			if (newVMState != element.getState()) {
 				
 				PrintMsg.print(DMsgType.MSG, "state update: "+element.getState()
 						+" --> "+newVMState);
 
 				/* update vm state */
 				updateVMState(vmList.get(vmID), newVMState);
 				
 				PrintMsg.print(DMsgType.MSG, "new state, " + vmList.get(vmID).getState());
 			}
 			
 			if(status == null || !status.equalsIgnoreCase("runn")) 
 				allRunning = false;
 		}
 		return allRunning;
 	}
 	
 	private void updateVMState(VMelement e, VMState state) {
 		e.setState(state);
 	}
 	
 	public VMState getVMState(String vmState) {
 		
 		if(vmState == null) return VMState.NOT_DEFINED;
 
 		if(vmState.equalsIgnoreCase("runn")) return VMState.RUNNING;
 		if(vmState.equalsIgnoreCase("penn")) return VMState.PENDING;
 		if(vmState.equalsIgnoreCase("prol")) return VMState.PROLOG;
 		
 		return VMState.NOT_DEFINED;
 	}
 
 	
 	private VMelement findVMElement(int id) {
 		return vmList.get(id);
 	}
 	
 	
 	public boolean createVM(String cmdLine) {
 		// TODO Auto-generated method stub
 		Cloud cloud = Config.cloudMan.getCurrentCloud();
 		int vms = 1;
 		String [] str = cmdLine.split(" ");
 		ArrayList<String> cmdList = new ArrayList<String>();
 		for(String s : str){
 			cmdList.add(s);
 			//System.out.println(s);
 		}
 		if(cmdList.size()>1&&cmdList.get(1).equalsIgnoreCase("--help")){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    create");
 			System.out.println("        [ --help] | ");
 			System.out.println("        [ -n NUM_VMS | --num=NUM_VMS ] | ");
 			System.out.println("        [ -c CLOUDNAME | --name=CLOUDNAME]");	
 			
 			return false;
 		}
 		if(cmdList.size()==1){
 			cmdList.add("-c");
 			cmdList.add(cloud.getCloudName());
 			cmdList.add("-n");
 			cmdList.add("1");
 		}
 		if(!cmdList.get(1).equals("-c")){
 			cmdList.add(1,"-c");
 			cmdList.add(2,cloud.getCloudName());
 		}
 		if(!cmdList.get(3).equalsIgnoreCase("-n")){
 			cmdList.add(3, "-n");
 			cmdList.add(4, "1");
 		}
 		System.out.println("");
 		cloud = Config.cloudMan.getCloudList().get(cmdList.get(2));
 		if(cloud==null){
 			System.out.println("[ERROR : ] Cloud doesn't exist! please check the cloud name...");
 			System.out.println("");
 			return false;
 		}
 		vms = Integer.parseInt(cmdList.get(4));
 			return cloud.createVM(vms);
 
 	}
 
 	public boolean listVM(String cmdLine) {
 		// TODO Auto-generated method stu
 		String filter ="";
 		String cloudFilter="";
 		StringTokenizer st = new StringTokenizer(cmdLine);		
 		st.nextToken();
 //		if(!st.hasMoreTokens()){System.out.println("[ERROR : ] Expected a VM ID");			return false;}
 		if( st.hasMoreTokens()){
 			String tmp = st.nextToken();
 			HashMap<String,Cloud> cc = new HashMap<String,Cloud>();			
 			if(tmp.equalsIgnoreCase("-refresh")){
 				String name="";
 				if(st.hasMoreTokens()){
 					name = st.nextToken();
 					if(!Config.cloudMan.getCloudList().keySet().contains(name)){
 						System.out.println("[ERROR : ] "+ name+ " has not been loaded!");
 						return false;						
 					}
 					cc.put(name, Config.cloudMan.getCloudList().get(name));
 					cloudFilter = name;
 				}else{
 					cc = Config.cloudMan.getCloudList();
 				}
 				TreeMap<Integer,VMelement> temp = new TreeMap<Integer,VMelement>();
 				for(Cloud cloud : cc.values()){
 					cloud.listVMs();
 					if(getVmList()==null)continue;
 					for(VMelement vm : cloud.getVmList().values()){	
 						boolean flag = true;
 						for(Integer id:vmList.keySet()){
 							if(vm.getId().equals(vmList.get(id).getId())){
 								temp.put(id, vm);
 								flag = false;
 								break;
 							}
 						}	
 						if(flag)temp.put(new Integer(Config.vmMan.getcurrId()), vm);
 					}
 				}

 				for(Integer id:temp.keySet()){
 					
 					vmList.put(id, temp.get(id));
					
 				}
				ArrayList<Integer> ids = new ArrayList<Integer>();
				
				for(Integer id:vmList.keySet()){
					boolean flag = true;
					boolean flagx = false;
					for(Cloud cloud:Config.cloudMan.getCloudList().values()){
						for(VMelement vm : cloud.getVmList().values()){
							if(vm.getId().equals(vmList.get(id).getId())){
								flag = false;
								flagx = true;
								
								break;
							}
						}
						if(flagx){flagx = false;break;}
					}
					if(flag)ids.add(id);					
				}
				for(Integer id:ids){
					vmList.remove(id);
				}
				
 			}else if(tmp.equalsIgnoreCase("-a")||tmp.equalsIgnoreCase("--type=available")){
 				filter="RUNNING";
 			}else if(tmp.equalsIgnoreCase("-r")||tmp.equalsIgnoreCase("--type=running")){
 				filter="RUNNING";
 			}else if(tmp.equalsIgnoreCase("-s")||tmp.equalsIgnoreCase("--type=suspended")){
 				filter="SUSPEND";
 			}else if(tmp.equalsIgnoreCase("-n")){
 				if(st.hasMoreTokens()){
 					cloudFilter = st.nextToken();
 				}else{
 					System.out.println("[ERROR : ] Expected a cloud name!");
 					return false;
 				}
 			}
 		}	
 		
 		String tName = String.format("%-16s", "CLOUD");
 		String tID =String.format("%-12s", "ID");
 		String tStat = String.format("%-16s", "STATUS");
 		System.out.println("----------------------------------------");
 		System.out.println(tID+tStat+tName);
 		System.out.println("----------------------------------------");
 			int runNum = 0;
 			int totalNum = 0;
 			for(Integer id : vmList.keySet()){
 				if(!cloudFilter.equals("")&&vmList.get(id).getCloudName().equals(cloudFilter)){
 					String fName = String.format("%-16s", vmList.get(id).getCloudName());
 					String fID =String.format("%-12s", id);
 					String fStat = String.format("%-16s", vmList.get(id).getState());
 					if(vmList.get(id).getState().equals(Config.VMState.RUNNING))runNum++;
 					System.out.println(fID+fStat+fName);
 					totalNum++;
 				}
 				if(!filter.equals("")&&vmList.get(id).getState().toString().equals(filter)){
 					String fName = String.format("%-16s", vmList.get(id).getCloudName());
 					String fID =String.format("%-12s", id);
 					String fStat = String.format("%-16s", vmList.get(id).getState());
 					if(vmList.get(id).getState().equals(Config.VMState.RUNNING))runNum++;
 					System.out.println(fID+fStat+fName);
 					totalNum++;
 				}
 				if(filter.equals("")&cloudFilter.equals("")){
 					String fName = String.format("%-16s", vmList.get(id).getCloudName());
 					String fID =String.format("%-12s", id);
 					String fStat = String.format("%-16s", vmList.get(id).getState());
 					if(vmList.get(id).getState().equals(Config.VMState.RUNNING))runNum++;
 					System.out.println(fID+fStat+fName);
 					totalNum++;
 				}
 			}
 			System.out.println("----------------------------------------");
 			System.out.println("      Running VMs :  "+runNum+"/"+totalNum);
 			System.out.println("----------------------------------------");
 		return true;
 	}
 
 	public boolean showVM(String cmdLine) {
 		// TODO Auto-generated method stub
 		StringTokenizer st = new StringTokenizer(cmdLine);		
 		String vmID = "";
 		Integer vID;
 		st.nextToken();
 		if(!st.hasMoreTokens()){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    show VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}
 
 		if(!st.hasMoreTokens()){System.out.println("[ERROR : ] Expected a VM ID");			return false;}
 		String tmp = st.nextToken();
 		
 		if(tmp.equalsIgnoreCase("-h")||tmp.equalsIgnoreCase("--help")){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    show VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}else{
 			vmID = tmp;
 			try{
 				vID =new Integer(vmID);
 			}catch (NumberFormatException e){
 				System.out.println("[ERROR : ] VM ID has to be a number!");
 				return false;
 			}
 			if(!vmList.keySet().contains(vID)){
 				System.out.println("[ERROR : ] VM ID doesn't exist!");
 				return false;
 			}
 
 		}
 		String tName = String.format("%-11s", "CLOUD");
 		String tID =String.format("%-11s", "ID ");
 		String tStat = String.format("%-11s", "STATUS");
 		String tpriIP = String.format("%-11s", "privateIP");
 		String tpubIP = String.format("%-11s", "publicIP");
 		String tinID = String.format("%-11s", "internalID");
 		String tlauTime = String.format("%-11s", "launchTime");
 		Integer id = vID;
 		
 		String fName = String.format("%-16s", vmList.get(id).getCloudName());
 		String fID =String.format("%-16s", id);
 		String fStat = String.format("%-16s", vmList.get(id).getState());
 		String fpriIP = String.format("%-16s", vmList.get(id).getPrivateIP());
 		String fpubIP =String.format("%-16s", vmList.get(id).getPubicIP());
 		String finID = String.format("%-16s", vmList.get(id).getId());
 		String flauTime = String.format("%-16s", vmList.get(id).getTime());
 		
 		System.out.println("---------------------------------------");
 		System.out.println(tID+" : "+fID);
 		System.out.println(tName+" : "+fName);
 		System.out.println(tinID+" : "+finID);
 		System.out.println(tStat+" : "+fStat);
 		System.out.println(tpriIP+" : "+fpriIP);
 		System.out.println(tpubIP+" : "+fpubIP);	
 		System.out.println(tlauTime+" : "+flauTime);
 		System.out.println("---------------------------------------");
 		
 		return false;
 	}
 
 	public boolean destroyVM(String cmdLine) {
 		// TODO Auto-generated method stub
 		StringTokenizer st = new StringTokenizer(cmdLine);		
 		String cloudName = "";
 		String vmID = "";
 		Integer vID;
 		st.nextToken();
 		if(!st.hasMoreTokens()){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    destroy VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}
 		if(!st.hasMoreTokens()){System.out.println("[ERROR : ] Expected a VM ID");			return false;}
 		String tmp = st.nextToken();
 		
 		if(tmp.equalsIgnoreCase("-h")||tmp.equalsIgnoreCase("--help")){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    destroy VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}else{
 			vmID = tmp;
 			try{
 				vID =new Integer(vmID);
 			}catch (NumberFormatException e){
 				System.out.println("[ERROR : ] VM ID has to be a number!");
 				return false;
 			}
 			if(!vmList.keySet().contains(vID)){
 				System.out.println("[ERROR : ] VM ID doesn't exist!");
 				return false;
 			}
 
 		}
 		cloudName = vmList.get(vID).getCloudName();
 		String inId = vmList.get(vID).getId();
 		Cloud cloud = Config.cloudMan.getCloudList().get(cloudName);
 		cloud.destroyVM(inId);
 		vmList.remove(vID);
 		return true;
 	}
 
 	public boolean suspendVM(String cmdLine) {
 		// TODO Auto-generated method stub
 		StringTokenizer st = new StringTokenizer(cmdLine);		
 		String cloudName = "";
 		String vmID = "";
 		st.nextToken();
 		if(!st.hasMoreTokens()){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    suspend VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}
 		Integer vID;
 		if(!st.hasMoreTokens()){System.out.println("[ERROR : ] Expected a VM ID");			return false;}
 		String tmp = st.nextToken();
 		
 		if(tmp.equalsIgnoreCase("-h")||tmp.equalsIgnoreCase("--help")){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    suspend VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}else{
 			vmID = tmp;
 			try{
 				vID =new Integer(vmID);
 			}catch (NumberFormatException e){
 				System.out.println("[ERROR : ] VM ID has to be a number!");
 				return false;
 			}
 			if(!vmList.keySet().contains(vID)){
 				System.out.println("[ERROR : ] VM ID doesn't exist!");
 				return false;
 			}
 
 		}
 		cloudName = vmList.get(vID).getCloudName();
 		String inId = vmList.get(vID).getId();
 		Cloud cloud = Config.cloudMan.getCloudList().get(cloudName);
 		cloud.suspendVM(inId);
 		
 		return true;
 	}
 
 	public boolean startVM(String cmdLine) {
 		// TODO Auto-generated method stub
 		StringTokenizer st = new StringTokenizer(cmdLine);		
 		String cloudName = "";
 		String vmID = "";
 		st.nextToken();
 		if(!st.hasMoreTokens()){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    start VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}
 		Integer vID;
 		if(!st.hasMoreTokens()){System.out.println("[ERROR : ] Expected a VM ID");			return false;}
 		String tmp = st.nextToken();
 		
 		if(tmp.equalsIgnoreCase("-h")||tmp.equalsIgnoreCase("--help")){
 			System.out.println("[USAGE : ]");
 			System.out.println("vmman");
 			System.out.println("    start VM_ID");
 			System.out.println("        [ --help] |-h ");
 			return false;
 		}else{
 			vmID = tmp;
 			try{
 				vID =new Integer(vmID);
 			}catch (NumberFormatException e){
 				System.out.println("[ERROR : ] VM ID has to be a number!");
 				return false;
 			}
 			if(!vmList.keySet().contains(vID)){
 				System.out.println("[ERROR : ] VM ID doesn't exist!");
 				return false;
 			}
 
 		}
 		cloudName = vmList.get(vID).getCloudName();
 		String inId = vmList.get(vID).getId();
 		Cloud cloud = Config.cloudMan.getCloudList().get(cloudName);
 		cloud.startVM(inId);
 		
 		return true;
 	}
 
 	
 	
 	public int getcurrId() {
 		return id++;
 	}
 
 	public void setcurrId(int id) {
 		this.id = id;
 	}
 
 
 
 	public TreeMap<Integer,VMelement> getVmList() {
 		return vmList;
 	}
 
 	public void setVmList(TreeMap<Integer, VMelement> vmList) {
 		this.vmList = vmList;
 	}
 
 
 
 	private int id;
 	private boolean done = false;
 	private BlockingQueue <VMMessage> msgQueue;
 	private Vector<Integer> vecTempID = null;
    protected TreeMap <Integer, VMelement> vmList = null;  
    }
 
 
