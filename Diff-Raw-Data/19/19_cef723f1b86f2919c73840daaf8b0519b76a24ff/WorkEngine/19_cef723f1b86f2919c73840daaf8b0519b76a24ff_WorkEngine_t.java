 package org.remus.mapred;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.thrift.TException;
 import org.remus.JSON;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.core.PipelineSubmission;
 import org.remus.core.RemusInstance;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.JobState;
 import org.remus.thrift.JobStatus;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.RemusNet;
 import org.remus.thrift.WorkDesc;
 import org.remus.thrift.WorkMode;
 import org.remus.RemusDBSliceIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.net.idn.StringPrep;
 
 public class WorkEngine implements Runnable {
 	private WorkDesc work;
 	private RemusDB db;
 	private MapReduceFunction mapred;
 	private JobState status;
 	private String message;
 	private RemusAttach attach;
 	private Logger logger;
 	private boolean canceled = false; 
 	public WorkEngine(WorkDesc work, RemusNet.Iface db, RemusNet.Iface attach, MapReduceFunction mapred) {
 		this.work = work;
 		this.db = RemusDB.wrap(db);
 		this.attach = RemusAttach.wrap(attach);
 		this.mapred = mapred;
 		status = JobState.QUEUED;
 		logger = LoggerFactory.getLogger(WorkEngine.class);
 	}
 
 	@Override
 	public void run() {
 		AppletRef arWork = new AppletRef(work.workStack.pipeline, 
 				work.workStack.instance, work.workStack.applet + "/@work");
 		status = JobState.WORKING;
 		message = null;
 		Map stackInfo = (Map) JSON.loads(work.infoJSON);
 		mapred.init(stackInfo);
 		PipelineSubmission sub = new PipelineSubmission(stackInfo);
 		try { 
 			if (work.mode == WorkMode.MAP) {
 				String inputInstStr = sub.getInputInstance();				
 				RemusInstance inst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						inputInstStr);
 				AppletRef ar = new AppletRef(
 						work.workStack.pipeline,
 						inst.toString(),
 						sub.getInputApplet()
 				);
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 				for (long jobID = work.workStart; jobID < work.workEnd; jobID++) {
 					for (Object key : db.get(arWork, Long.toString(jobID))) {
 						MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 						for (Object value : db.get(ar, (String) key)) {
 							mapred.map((String) key, value, cb);
 						}
 						if (!canceled) {
 							cb.writeEmits(outRef, jobID);
 							logger.info("Emiting: " + cb.emitCount);
 						}
 					}
 				}
 				status = JobState.DONE;
 			} else if (work.mode == WorkMode.SPLIT) {
 				MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 				if (!canceled) {
 					mapred.split(stackInfo, cb);
 					cb.writeEmits(outRef, 0);
 					logger.info("Emiting: " + cb.emitCount);
 				}
 				status = JobState.DONE;
 			} else if (work.mode == WorkMode.REDUCE) {
 				String inputInstStr = sub.getInputInstance();				
 				RemusInstance inst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						inputInstStr);
 				AppletRef ar = new AppletRef(
 						work.workStack.pipeline,
 						inst.toString(),
 						sub.getInputApplet()
 				);
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 
 				for (long jobID = work.workStart; jobID < work.workEnd; jobID++) {
 					MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 					for (Object key : db.get(arWork, Long.toString(jobID))) {
 						mapred.reduce((String) key, db.get(ar, (String) key), cb);
 					}
 					if (!canceled) {
 						cb.writeEmits(outRef, jobID);
 						logger.info("Emiting: " + cb.emitCount);
 					}
 				}
 				status = JobState.DONE;
 			} else if (work.mode == WorkMode.MATCH) {
 
 				String leftInputInstStr = sub.getLeftInputInstance();				
 				String rightInputInstStr = sub.getRightInputInstance();
 
 				RemusInstance leftInst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						leftInputInstStr);
 				RemusInstance rightInst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						rightInputInstStr);
 
 				AppletRef leftAr = new AppletRef(
 						work.workStack.pipeline,
 						leftInst.toString(),
 						sub.getLeftInputApplet()
 				);
 				AppletRef rightAr = new AppletRef(
 						work.workStack.pipeline,
 						rightInst.toString(),
 						sub.getRightInputApplet()
 				);
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 
 				for (long jobID = work.workStart; jobID < work.workEnd; jobID++) {
 					MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 					for (Object key : db.get(arWork, Long.toString(jobID))) {
 						mapred.match((String) key, db.get(leftAr, (String) key), db.get(rightAr, (String) key), cb);
 					}
 					if (!canceled) {
 						cb.writeEmits(outRef, jobID);
 						logger.info("Emiting: " + cb.emitCount);
 					}
 				}
 				status = JobState.DONE;
 
 			} else if (work.mode == WorkMode.MERGE) {
 				String leftInputInstStr = sub.getLeftInputInstance();				
 				String rightInputInstStr = sub.getRightInputInstance();
 
 				RemusInstance leftInst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						leftInputInstStr);
 				RemusInstance rightInst = RemusInstance.getInstance(db, 
 						work.workStack.pipeline, 
 						rightInputInstStr);
 
 				AppletRef leftAr = new AppletRef(
 						work.workStack.pipeline,
 						leftInst.toString(),
 						sub.getLeftInputApplet()
 				);
 				AppletRef rightAr = new AppletRef(
 						work.workStack.pipeline,
 						rightInst.toString(),
 						sub.getRightInputApplet()
 				);
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 
 				for (long jobID = work.workStart; jobID < work.workEnd; jobID++) {
 					MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 					if (sub.getAxis() == PipelineSubmission.LEFT_AXIS) {
 						for (Object leftKey : db.get(arWork, Long.toString(jobID))) {
 							for (String rightKey : db.listKeys(rightAr)) {
 								mapred.merge(
 										(String) leftKey, db.get(leftAr, (String) leftKey), 
 										(String) rightKey, db.get(rightAr, (String) rightKey), 
 										cb);
 							}
 						}
 					} else {
 						for (Object rightKey : db.get(arWork, Long.toString(jobID))) {
 							for (String leftKey : db.listKeys(leftAr)) {
 								mapred.merge(
 										(String) leftKey, db.get(leftAr, (String) leftKey), 
 										(String) rightKey, db.get(rightAr, (String) rightKey), 
 										cb);
 							}
 						}
 					}
 					if (!canceled) {
 						cb.writeEmits(outRef, jobID);
 						logger.info("Emiting: " + cb.emitCount);
 					}
 				}
 				status = JobState.DONE;
 			} else if (work.mode == WorkMode.PIPE) {
 
 				List inputList = sub.getInputList();
 				List<Object> siList = new LinkedList<Object>();
 				int i = 0;
 				for (Object inputInfo : inputList) {
 					RemusInstance inst = RemusInstance.getInstance(db, 
 							work.workStack.pipeline, 
 							(String) ((Map) inputInfo).get("_instance"));
 					AppletRef ar = new AppletRef(
 							work.workStack.pipeline,
 							inst.toString(),
 							(String) ((Map) inputInfo).get("_applet")
 					);
 					siList.add(new StackIterator(db, ar));
 					i++;
 				}
 
 				AppletRef outRef = new AppletRef(
 						work.workStack.pipeline,
 						work.workStack.instance,
 						work.workStack.applet
 				);
 				MapReduceCallback cb = new MapReduceCallback(work.workStack.pipeline, work.workStack.applet, stackInfo, db, attach);
 				mapred.pipe(siList, cb);
 				if (!canceled) {
 					cb.writeEmits(outRef, 0);
 					logger.info("Emiting: " + cb.emitCount);
 				}
 				status = JobState.DONE;
 			} else {
 				status = JobState.ERROR;
 			}
 		} catch (TException e) {
 			status = JobState.ERROR;
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			e.printStackTrace(pw);
 			message = sw.toString();
 		} catch (NotImplemented e) {
 			status = JobState.ERROR;
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			e.printStackTrace(pw);
 			message = sw.toString();
 		} catch (NotSupported e) {
 			status = JobState.ERROR;
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			e.printStackTrace(pw);
 			message = sw.toString();
		} catch (Exception e) {
			status = JobState.ERROR;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			message = sw.toString();
 		}
 		cleanup();
 	}
 
 	public JobStatus getStatus() {
 		JobStatus out = new JobStatus();
 		out.status = status;
 		out.errorMsg = message;
 		return out;
 	}
 
 	public void cancel() {
 		canceled = true;
 		if (status != JobState.WORKING) {
 			cleanup();
 		}
 	}
 	
 	public void cleanup() {
 		mapred.cleanup();
 	}
 
 	public class StackIterator extends RemusDBSliceIterator<Object []> {
 		public StackIterator(RemusDB db, AppletRef stack) {
 			super(db, stack, "", "", true);
 		}
 		@Override
 		public void processKeyValue(String key, Object val, long jobID, long emitID) {
 			addElement(new Object []  {key, val});
 		}
 	}
 
 	@Override
 	public String toString() {
 		return work.workStack.instance + ":" + work.workStack.applet;
 	}
 	
 }
