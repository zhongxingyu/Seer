 package org.remus.core;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.thrift.TException;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.Constants;
 import org.remus.thrift.NotImplemented;
 import org.remus.work.WorkGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yaml.snakeyaml.scanner.Constant;
 
 /**
  * The AppletInstance refers to a specific Remus Applet instance. This class
  * can be used to determine is an applet instance has complete, is ready,
  * has old timestamps, and determine which jobs are ready to be worked on.
  * @author kellrott
  *
  */
 public class AppletInstance {
 
 	private RemusDB datastore;
 	private RemusAttach attachstore;
 	private Logger logger;
 
 	private AppletInstanceRecord appletInstance;
 
 	public AppletInstance(String pipeline, RemusInstance instance, String applet, RemusDB datastore, RemusAttach attachstore) throws RemusDatabaseException {
 		logger = LoggerFactory.getLogger(AppletInstance.class);
 
 		AppletRef instTable = new AppletRef(pipeline, Constants.STATIC_INSTANCE, Constants.INSTANCE_APPLET );
 
 		try {
 			String [] appletArray = applet.split(":");
 			for (Object obj : datastore.get(instTable, instance.toString() + ":" + appletArray[0])) {
 				this.appletInstance = new AppletInstanceRecord(obj);
 			}
 		} catch (TException e) {
 			throw new RemusDatabaseException(e.toString());
 		} catch (NotImplemented e) {
 			throw new RemusDatabaseException(e.toString());
 		}
 
 		if (this.appletInstance == null) {
 			throw new RemusDatabaseException("Applet Instance not found");
 		}
 
 		this.datastore = datastore;
 		this.attachstore = attachstore;
 	}
 
 
 	public AppletInstance(AppletInstanceRecord rec, RemusDB datastore, RemusAttach attachstore ) {
 		logger = LoggerFactory.getLogger(AppletInstance.class);
 
 		this.appletInstance = rec;
 		this.datastore = datastore;
 		this.attachstore = attachstore;
 	}
 
 	@Override
 	public String toString() {
 		return appletInstance.getInstance() + ":" + appletInstance.getApplet();
 	}
 
 	@Override
 	public int hashCode() {
 		return appletInstance.getInstance().hashCode() + appletInstance.getApplet().hashCode();	
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		AppletInstance w = (AppletInstance) obj;
 		if (w.getRecord().getInstance().equals(getRecord().getInstance()) 
 				&& w.getRecord().getApplet().equals(getRecord().getApplet())) {
 			return true;
 		}
 		return false;
 	}	
 
 	public boolean isReady( ) {
 		try {
 			if (appletInstance.getMode() == AppletInstanceRecord.STORE) {
 				return true;
 			}
 			if (isInError()) {
 				return false;
 			}
 			if (appletInstance.hasSources()) {
 				boolean allReady = true;
 				for (String src : appletInstance.getSources()) {
 					try {
 						AppletInstanceRecord input = getInput(src);
 						if (input != null) {
 							AppletInstance ai = new AppletInstance(input.getPipeline(), 
 									new RemusInstance(input.getInstance()),
 									input.getApplet(), datastore, attachstore);
 							if (!ai.isComplete()) {
 								allReady = false;
 							}							
 						} else {
 							allReady = false;
 						}
 					} catch (RemusDatabaseException e) {
 						e.printStackTrace();
 						allReady = false;
 					}
 				}
 				return allReady;
 			}		
 		} catch (RemusDatabaseException e) {
 			return false;
 		}
 		return true;
 	}
 
 
 
 	private AppletInstanceRecord getInput(String src) {
 		try {
 			AppletInput input = appletInstance.getInput(src, datastore);
 			if (input == null ) {
 				return null;
 			}
 			if (input.pipeline == null) {
 				input.pipeline = appletInstance.getPipeline();
 			}
 			if (input.instance == null) {
 				input.instance = RemusInstance.getInstance(datastore, input.pipeline, appletInstance.getInstance());
 			}
 			AppletRef instTable = new AppletRef(input.pipeline, Constants.STATIC_INSTANCE, Constants.INSTANCE_APPLET);
 			String [] appletNames = input.applet.split(":");
 			for (Object obj : datastore.get(instTable, input.instance + ":" + appletNames[0]) ) {
 				return new AppletInstanceRecord(obj);
 			}
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public long inputTimeStamp( ) {
 		long out = 0;
 		for (String src : appletInstance.getSources()) {
 			AppletInstanceRecord input = getInput(src);
 			if (input != null) {
 				try {
 					AppletInstance ai = new AppletInstance(input, datastore, attachstore);
 					//long val = ai.getDataTimeStamp();
 					long val = ai.getStatusTimeStamp();
 					if (out < val) {
 						out = val;
 					}
 				} catch (TException e) {
 					e.printStackTrace();
 				} catch (NotImplemented e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}			
 		return out;
 	}
 
 
 	public long getDataTimeStamp( ) throws TException, NotImplemented {
 		return datastore.getTimeStamp(new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet()));
 	}
 
 	public long getStatusTimeStamp() throws NotImplemented, TException {
 		return datastore.getTimeStamp(new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.WORK_APPLET));
 	}
 
 
 	public boolean isInError() {
 		boolean found = false;
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.ERROR_APPLET);
 		for (@SuppressWarnings("unused") String key : datastore.listKeys(ar)) {
 			found = true;
 		}
 		return found;
 	}
 
 
 
 
 	public void finishWork(long jobID, String workerName, long emitCount) throws TException, NotImplemented {
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.DONE_APPLET);
 		datastore.add(ar, 0L, 0L, Long.toString(jobID), workerName);
 		if (appletInstance != null) {
 			WorkGenerator gen = appletInstance.getWorkGenerator();
 			gen.finalizeWork(appletInstance, datastore);
 		}
 	}
 
 	public void errorWork(long jobID, String errorMsg) {
 		AppletRef arWork = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.ERROR_APPLET);
 		try {
 			datastore.add(arWork, 0, 0,
 					String.valueOf(jobID), errorMsg);
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 	}
 
 	public long [] getReadyJobs(int count) {
 		long [] out = new long[count];
 		int curPos = 0;
 		WorkStatus status = getStatus();
 		if (status.done()) {
 			return null;
 		}
 
 		if (!isReady()) {
 			return null;
 		}
 
 		//See if work needs to be generated
 
 		if (!isComplete()) {
 			try {
 				WorkGenerator gen = appletInstance.getWorkGenerator();
 				if (gen != null) {
 					long infoTime = getStatusTimeStamp();
 					long dataTime = inputTimeStamp();
 					if (infoTime < dataTime 
 							|| !status.workCountSet()) {
 						logger.info("GENERATE WORK: " + appletInstance.getPipeline() + "/" + toString());
 						gen.writeWorkTable(appletInstance, datastore, attachstore);
 					} else {
 						//logger.info("Active Work Stack: " + inst.toString() + ":" + this.getID());
 					}
 				}
 			} catch (TException e) {
 				e.printStackTrace();
 			} catch (NotImplemented e) {
 				e.printStackTrace();
 			}
 		} else {
 			if (getRecord().hasSources()) {
 				try {
 					long thisTime = getStatusTimeStamp();
 					long inTime = inputTimeStamp();
 					//System.err.println( this.getPath() + ":" + thisTime + "  " + "IN:" + inTime );			
 					if (inTime > thisTime) {
 						logger.info("YOUNG INPUT (applet reset):" + toString());
 						WorkStatus.unsetComplete(appletInstance.getPipeline(), 
 								new RemusInstance(appletInstance.getInstance()),
 								appletInstance.getApplet(),
 								datastore);
 					}
 				} catch (TException e){
 					e.printStackTrace();
 				} catch (NotImplemented e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 
 		String jobStart = status.getJobStart();
 		if (jobStart == null) {
 			jobStart = "";
 		}
 		String newJobStart = jobStart;
 
 		AppletRef arStatus = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.WORK_APPLET);
 		AppletRef arDone = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet() + Constants.DONE_APPLET);
 
 		try {
 			boolean found = true;
 			boolean allDone = true;
 			boolean firstSlice = true;
 			String sliceStart = jobStart;
 			logger.debug("Starting JobScan: " + appletInstance.getInstance() + ":" + appletInstance.getApplet() + " " + jobStart );
 			while (found && curPos < count) {
 				found = false;
 				for (String key : datastore.keySlice(arStatus, sliceStart, count - curPos)) {
 					if (firstSlice || key.compareTo(sliceStart) != 0) {
 						found = true;
 						if (!datastore.containsKey(arDone, key)) {
 							out[curPos] = Long.valueOf(key);
 							curPos++;
 						} else {
 							if (allDone) {
 								newJobStart = key;
 							}
 							allDone = false;
 						}
 						sliceStart = key;
 						firstSlice = false;
 					}
 				}
 			}
 			logger.debug("Ending JobScan: " + appletInstance.getInstance() + ":" + appletInstance.getApplet() + " " + newJobStart + " JobCount: " + curPos);
 			if (count > 0 && curPos == 0) {
 				logger.info("Work DONE: " + appletInstance.getInstance() + ":" + appletInstance.getApplet());
 				setComplete();
 			} else if (newJobStart.equals(jobStart)) {
 				logger.info("JobStart : " + appletInstance.getInstance() + ":" + appletInstance.getApplet() + " = " + jobStart);
 				status.setJobStart(newJobStart);
 				setWorkStat(status);
 			}
 
 			//logger.debug("Found : " + curPos + " jobs");
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 
 		long [] a = Arrays.copyOf(out, curPos);
 		Arrays.sort(a);
 		return a;
 	}
 
 
 	public void setComplete() {
 		WorkStatus stat = getStatus();
 		stat.setWorkDone(true);
 		try {
 			setWorkStat(stat);
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void setWorkStat(WorkStatus status) throws TException, NotImplemented {
 		AppletRef arWork = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.WORK_APPLET);
 		datastore.add(arWork, 0, 0, appletInstance.getInstance() + ":" + appletInstance.getApplet(), status);
 	}
 
 	public void setWorkStat( int jobStart, int doneCount, int errorCount, int totalCount, long timestamp) throws TException, NotImplemented {
 		WorkStatus ws = new WorkStatus(jobStart, doneCount, errorCount, totalCount, timestamp);
 		setWorkStat(ws);
 	}
 
 	@SuppressWarnings("rawtypes")
 	public WorkStatus getStatus() {
 		Object statObj = null;
 		AppletRef arWork = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.WORK_APPLET);
 		try {
 			for (Object curObj : datastore.get(arWork, appletInstance.getInstance() + ":" + appletInstance.getApplet())) {
 				statObj = curObj;
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 		if (statObj == null) {
 			statObj = new HashMap();
 		}
 		return new WorkStatus((Map) statObj);
 	}
 
 
 	@SuppressWarnings("rawtypes")
 	public boolean isComplete() {
 		boolean found = false;
 		AppletRef arStatus = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.WORK_APPLET);
 		//AppletRef arError = new AppletRef(pipeline.getID(), RemusInstance.STATIC_INSTANCE_STR, applet.getID() + Constants.ERROR_APPLET);
 		try {
 			for (Object statObj : datastore.get(arStatus, appletInstance.getInstance() + ":" + appletInstance.getApplet())) {
 				if (statObj != null) {
 					WorkStatus ws = new WorkStatus((Map) statObj);
 					if (ws.done()) {
 						found = true;
 					}
 				}
 			}
 			if (found) {
 				//for (@SuppressWarnings("unused") String key : applet.getDataStore().listKeys(arError)) {
 				//	found = false;
 				//}
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 		return found;
 	}
 
 	public PipelineSubmission getInstanceInfo() throws TException, NotImplemented {
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.INSTANCE_APPLET);
 		for (Object obj : datastore.get(ar, appletInstance.getInstance() + ":" + appletInstance.getApplet())) {
 			return new PipelineSubmission(obj);
 		}
 		return null;
 	}
 
 	public void updateInstanceInfo(PipelineSubmission instInfo) throws TException, NotImplemented {
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.INSTANCE_APPLET);
 		datastore.add(ar, 0, 0, appletInstance.getInstance() + ":" + appletInstance.getApplet(), instInfo);		
 	}
 
 	public AppletRef getAppletRef() {
 		AppletRef arWork = new AppletRef(appletInstance.getPipeline(), appletInstance.getInstance(), appletInstance.getApplet());
 		return arWork;
 	}
 
 
 	public List<String> listAttachments() throws NotImplemented, TException {
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.INSTANCE_APPLET);
 		return attachstore.listAttachments(ar, appletInstance.getInstance() + ":" + appletInstance.getApplet());
 	}
 
 
 	public InputStream readAttachment(String file) throws NotImplemented {
 		AppletRef ar = new AppletRef(appletInstance.getPipeline(), RemusInstance.STATIC_INSTANCE_STR, Constants.INSTANCE_APPLET);
 		return attachstore.readAttachment(ar, appletInstance.getInstance() + ":" + appletInstance.getApplet(), file);
 	}
 
 	public AppletInstanceRecord getRecord() {
 		return appletInstance;
 	}
 
 }
