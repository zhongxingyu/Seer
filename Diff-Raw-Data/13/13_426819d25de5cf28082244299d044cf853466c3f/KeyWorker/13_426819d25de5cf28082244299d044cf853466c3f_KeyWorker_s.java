 package org.remus.manage;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.thrift.TException;
 import org.remus.JSON;
 import org.remus.core.AppletInstance;
 import org.remus.core.PipelineSubmission;
 import org.remus.plugin.PeerManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.JobState;
 import org.remus.thrift.JobStatus;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.RemusNet;
 import org.remus.thrift.WorkDesc;
 import org.remus.thrift.WorkMode;
 
 
 
 public class KeyWorker extends InstanceWorker {
 
 	public KeyWorker(PeerManager peerManager, AppletInstance ai) {
 		super(peerManager, ai);
 		logger.debug("KEYWORKER:" + ai + " started");
 		state = WORKING;
 	}
 
 
 	public static final int MAX_REFRESH_TIME = 30 * 1000;
 	int state;
 	
 	Set<RemoteJob> remoteJobs = new HashSet<RemoteJob>();
 	Map<String,Set<Integer>> activeWork = new HashMap<String, Set<Integer>>();
 
 	public boolean checkWork() throws NotImplemented, TException {
 		long [] workIDs;
 		boolean workChange = false;
 		int activeCount = 0;
 		//Check existing jobs
 		Map<RemoteJob,Boolean> removeSet = new HashMap<RemoteJob,Boolean>();
 		for (RemoteJob rj : remoteJobs) {
 			logger.debug("Checking " + rj.getPeerID() + " for " + rj.getJobID() + " " + ai);
 			try {
 				RemusNet.Iface worker = peerManager.getPeer(rj.getPeerID());
 				if (worker != null) {
 					JobStatus s = worker.jobStatus(rj.getJobID());
 					if (s.status == JobState.DONE) {
 						workChange = true;
 						for (long i = rj.getWorkStart(); i < rj.getWorkEnd(); i++) {
 							ai.finishWork(i, rj.getPeerID(), s.emitCount);
 						}
 						removeSet.put(rj, true);
 						logger.info("Worker Finished: " + rj.getJobID());
 					} else if (s.status == JobState.ERROR) {
 						workChange = true;
 						for (long i = rj.getWorkStart(); i < rj.getWorkEnd(); i++) {
 							ai.errorWork(i, s.errorMsg);
 						}
 						removeSet.put(rj, false);
 						logger.warn("JOB ERROR: " + ai + " job:" + rj.getJobID() + " " + s.errorMsg);
 					} else if (s.status == JobState.UNKNOWN) {
 						logger.warn("Worker lost job: " + rj.getJobID());
 					} else {
 						activeCount += 1;
 					}
 					peerManager.returnPeer(worker);
 				}
 			} catch (TException e) {
 				logger.warn("Worker Connection Failed: " + rj.getPeerID());
 				removeSet.put(rj, false);
 				peerManager.peerFailure(rj.getPeerID());
 			} catch (NotImplemented e) {
 				logger.warn("Worker Call Error: " + rj.getPeerID());
 				removeSet.put(rj, false);
 			}
 		}
 		for (RemoteJob rj : removeSet.keySet()) {
 			returnPeer(rj.getPeerID());
 			removeRemoteJob(ai, rj, removeSet.get(rj));
 		}
 
 		workIDs = getAppletWorkCache(ai);
 
 		if (workIDs != null) {
 			int curPos = 0;
 			while (curPos < workIDs.length) {
 				if (workIDs[curPos] == -1) {
 					curPos++;
 				} else {
 					int last = curPos;
 					do {
 						curPos++;
					} while (curPos < workIDs.length && workIDs[curPos] - workIDs[last] == curPos - last);						
					workAssign(ai, borrowPeer(), workIDs[last], workIDs[curPos - 1] + 1);
					for (int i = last; i < curPos; i++) {
						workIDs[i] = -1;
 					}
 					workChange = true;
 				}
 			}
 		}
 
 		return workChange;
 	}
 
 
 	private void workAssign(AppletInstance ai, String peerID, long workStart, long workEnd) {
 		try {
 			PipelineSubmission instanceInfo = ai.getInstanceInfo();
 			if (instanceInfo == null) {
 				return;
 			}
 			WorkDesc wdesc = new WorkDesc();
 			wdesc.setWorkStack(new AppletRef());		
 			wdesc.workStack.setPipeline(ai.getPipeline().getID());
 			wdesc.workStack.setInstance(ai.getInstance().toString());
 			wdesc.workStack.setApplet(ai.getApplet().getID());
 			wdesc.setWorkStart(workStart);
 			wdesc.setWorkEnd(workEnd);
 			wdesc.setLang(ai.getApplet().getType());
 			logger.info("Assigning " + ai + ":" + workStart + "-" + workEnd + " to " + peerID + " " + wdesc.workStack);
 			wdesc.setInfoJSON(JSON.dumps(instanceInfo));
 
 			RemusNet.Iface worker = peerManager.getPeer(peerID);
 			if (worker == null) {
 				return;
 			}
 			int mode = ai.getApplet().getMode();
 			if (mode == WorkMode.AGENT.getValue()) {
 				logger.info("Agent Operation");
 				wdesc.setMode(WorkMode.MAP);				
 
 				String jobID = worker.jobRequest(peerManager.getManager(), peerManager.getAttachStore(), wdesc);
 
 				addRemoteJob(ai, new RemoteJob(peerID, jobID, workStart, workEnd));
 
 			} else {
 				wdesc.setMode(WorkMode.findByValue(mode));
 				String jobID = worker.jobRequest(peerManager.getDataServer(), peerManager.getAttachStore(), wdesc);
 
 				addRemoteJob(ai, new RemoteJob(peerID, jobID, workStart, workEnd));
 
 			}			
 			peerManager.returnPeer(worker);
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	int assignRate = 1;
 
 	private long [] getAppletWorkCache(AppletInstance ai) throws NotImplemented, TException {
 		Set<String> peers = peerManager.getWorkers();
 		long [] workIDs = null;
 		synchronized (workIDCache) {			
 			if (workIDCache.containsKey(ai) && workIDCache.get(ai) != null && workIDCache.get(ai).length != 0) {
 				workIDs = workIDCache.get(ai);
 				Arrays.sort(workIDs);
 			} else {
 				workIDs = ai.getReadyJobs(assignRate * peers.size());				
 				workIDCache.put(ai, workIDs);
 			}
 		}
 		for (RemoteJob rj :remoteJobs) {
 			for (int i = 0; i < workIDs.length; i++) {
 				if (workIDs[i] >= rj.getWorkStart() && workIDs[i] < rj.getWorkEnd()) {
 					workIDs[i] = -1;
 				}
 			}
 		}
 
 
 		boolean valid = false;
 		for (int i = 0; i < workIDs.length; i++) {
 			if (workIDs[i] != -1) {
 				valid = true;
 			}
 		}
 		if (valid) {
 			Arrays.sort(workIDs);
 			return workIDs;
 		} 
 		if (ai.isComplete()) {
 			state = DONE;
 		} 
 		if (ai.isInError()) {
 			state = ERROR;
 		}
 		workIDCache.put(ai, new long [0]);
 		return null;
 	}
 
 
 
 	private Map<AppletInstance,long []> workIDCache = new HashMap<AppletInstance, long[]>();
 
 	private void flushAppletWorkCache() {
 		synchronized (workIDCache) {			
 			List<AppletInstance> removeList = new LinkedList<AppletInstance>();
 			for (AppletInstance ai : workIDCache.keySet()) {
 				if (workIDCache.get(ai) == null || workIDCache.get(ai).length == 0) {
 					removeList.add(ai);
 				}
 			}
 			for (AppletInstance ai : removeList) {
 				workIDCache.remove(ai);
 			}
 		}
 	}
 
 
 
 	private void addRemoteJob(AppletInstance ai, RemoteJob rj) {
 		remoteJobs.add(rj);
 	}
 
 	private void removeRemoteJob(AppletInstance ai, RemoteJob rj, boolean adjustAssignRate) {
 		if (adjustAssignRate) {
 			int newAssignRate = assignRate;
 			long runTime = (new Date()).getTime() - rj.getStartTime();
 			if (runTime > MAX_REFRESH_TIME) {
 				newAssignRate /= 2;
 			}
 			newAssignRate++;
 			logger.info("ASSIGN RATE: " + ai + " " + newAssignRate);
 			assignRate = newAssignRate;
 		}
 		try {
 			RemusNet.Iface worker = peerManager.getPeer(rj.getPeerID());					
 			worker.jobCancel(rj.getJobID());	
 			peerManager.returnPeer(worker);
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		} catch (TException e) {
 			peerManager.peerFailure(rj.getPeerID());
 			e.printStackTrace();
 		}
 		remoteJobs.remove(rj);
 	}
 
 
 	@Override
 	public boolean isDone() {
 		if ( state == DONE ) {
 			return true;
 		}
 		return false;
 	}
 
 
 	@Override
 	public void removeJob() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
