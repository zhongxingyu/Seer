 package org.remus.manage;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.thrift.TException;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.core.AppletInstance;
 import org.remus.core.PipelineSubmission;
 import org.remus.core.RemusApp;
 import org.remus.core.RemusApplet;
 import org.remus.core.RemusInstance;
 import org.remus.core.RemusPipeline;
 
 import org.remus.plugin.PeerManager;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.RemusNet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WorkSchedule {
 
 	private PeerManager peerManager;
 	//private Map<AppletInstance, Set<RemoteJob>> activeStacks;
 
 	private Map<AppletInstance, Integer> assignRate;
 	private Logger logger;
 	private SchemaEngine schemaEngine;
 	private WorkerPool workerPool;
 	private HashMap<AppletInstance, InstanceWorker> workerMap;
 
 	public static final int MAX_REFRESH_TIME = 30 * 1000;
 
 	public WorkSchedule(PeerManager peerManager, SchemaEngine schemaEngine) {
 		logger = LoggerFactory.getLogger(WorkSchedule.class);
 		this.peerManager = peerManager;
 		//activeStacks = new HashMap<AppletInstance, Set<RemoteJob>>();
 		assignRate = new HashMap<AppletInstance, Integer>();
 		this.schemaEngine = schemaEngine;
 		workerPool = new WorkerPool(peerManager);
 		workerMap = new HashMap<AppletInstance, InstanceWorker>();
 	}
 
 
 	public boolean doSchedule() {
 		boolean workChange = false;
 		//First, scan all of the stacks to find active worksets
 		if (scanJobs()) {
 			workChange = true;
 		}
 		//scan the workers, start assigning untouched work to workers that aren't over limit
 		if (workSchedule()) {
 			workChange = true;
 		}
 		//collect and clean finished jobs
 		try {
 			if (cleanJobs()) {
 				workChange = true;
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return workChange;
 	}
 
 
 
 	private boolean scanJobs() {
 		boolean change = false;
 		RemusNet.Iface db = null;
 		RemusNet.Iface attach = null;
 		try {
 			db = peerManager.getPeer(peerManager.getDataServer());
 			attach = peerManager.getPeer(peerManager.getAttachStore());
 			RemusApp app = new RemusApp(RemusDB.wrap(db), RemusAttach.wrap(attach));
 			int activeCount = 0;
 			Set<AppletInstance> fullSet = new HashSet<AppletInstance>();
 			for (String name : app.getPipelines()) {
 				List<RemusInstance> instList = new LinkedList<RemusInstance>();
 				RemusPipeline pipe = app.getPipeline(name);
 
 				schemaEngine.processSubmissions(pipe);
 
 				for (String subKey : pipe.getSubmits()) {
 					PipelineSubmission subData = pipe.getSubmitData(subKey);
 					instList.add(subData.getInstance());
 				}
 				for (RemusInstance inst : instList) {
 					Set<AppletInstance> curSet = pipe.getActiveApplets(inst);
 					activeCount += curSet.size();
 					fullSet.addAll(curSet);
 				}
 				//check for work that needs to be instanced
 				for ( String appletName : pipe.getMembers() ) {
 					RemusApplet applet = pipe.getApplet(appletName);
 					if (applet.getMode() != RemusApplet.STORE) {
 						for (RemusInstance inst : instList) {
 							if (!pipe.hasAppletInstance(inst, appletName)) {
 								boolean inputFound = false;
 								for (String input : applet.getSources()) {
 									if (pipe.hasAppletInstance(inst, input)) {
 										inputFound = true;
 									}
 									if (inputFound) {
 										AppletInstance src = pipe.getAppletInstance(inst, applet.getSource());
 										if (src != null) {
 											PipelineSubmission info = src.getInstanceInfo();
 											applet.createInstance(info, inst);
 											change = true;
 										} 
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			syncAppletList(fullSet);
 			if (activeCount > 0) {
 				logger.info("MANAGER found " + activeCount + " active stacks");
 			}
 		} catch (RemusDatabaseException e) {
 			e.printStackTrace();
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		} finally {
 			peerManager.returnPeer(db);
 			peerManager.returnPeer(attach);
 		}
 		return change;
 	}
 
 	
 	public boolean workSchedule() {
 		boolean workAdded = false;
 		try {
 			
 			synchronized (workerMap) {
 				for (AppletInstance acur : workerMap.keySet()) {	
 					if (workerMap.get(acur) == null) {
 						InstanceWorker worker = workerPool.getWorker(acur);
 						if (worker != null) {
 							workerMap.put(acur, worker);
 						}
 					} else {
 						if (workerMap.get(acur).checkWork()) {
 							workAdded = true;
 						}
 					}
 				}
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return workAdded;
 	}
 
 	
 
 
 	private boolean cleanJobs() throws TException, NotImplemented {
 		boolean found = false;
 		synchronized (workerMap) {
 			int activeCount = 0;
 			for (AppletInstance ai : workerMap.keySet()) {			
				Map<RemoteJob, Boolean> removeSet = new HashMap<RemoteJob, Boolean>();
 				InstanceWorker worker = workerMap.get(ai);
 				if (worker != null) {
 					activeCount++;
 					if (worker.isDone()) {
 						found = true;
 					} else {
 						
 					}
					
				}
					
 			}
 			if (activeCount > 0) {
 				logger.info("Active RemoteJobs: " + activeCount);			
 			}
 		}
 		return found;
 	}
 
 
 
 
 
 	private void syncAppletList(Set<AppletInstance> aiSet) {
 		Set<AppletInstance> removeSet = new HashSet<AppletInstance>();		
 		synchronized (workerMap) {		
 			for (AppletInstance ai : aiSet) {
 				if (!workerMap.containsKey(ai)) {
 					workerMap.put(ai, null);
 					assignRate.put(ai, 1);
 				}
 			}
 			for (AppletInstance ai : workerMap.keySet()) {
 				if (!aiSet.contains(ai)) {
 					removeSet.add(ai);
 				}
 			}
 		}
 
 		for (AppletInstance ai : removeSet) {
 			workerMap.get(ai).removeJob();			
 			synchronized (workerMap) {
 				workerMap.remove(ai);				
 			}
 		}
 	}
 
 
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Object getScheduleInfo() {
 		Map out = new HashMap();
 		synchronized (workerMap) {
 			int outCount = workerMap.size();
 			out.put("activeCount", Integer.toString(outCount));			
 			Map aiMap = new HashMap();
 			for (AppletInstance ai : workerMap.keySet()) {
 				List o = new LinkedList();
 				//for (RemoteJob rj : workerMap.get(ai)) {
 				//	o.add(rj.getPeerID());
 				//}
 				aiMap.put(ai.toString(), o);
 			}
 			out.put("active", aiMap);
 		}		
 		return out;
 	}
 
 
 
 }
