 package org.remus.mapred;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.thrift.TException;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.core.PipelineSubmission;
 import org.remus.core.RemusInstance;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.NotImplemented;
 
 
 public class MapReduceCallback {
 
 	static class MapReduceEmit {
 		String key;
 		Object val;
 		long emitID;
 		public MapReduceEmit(String key, Object val, long emitID) {
 			this.key = key;
 			this.val = val;
 			this.emitID = emitID;
 		}
 	}
 
 	long emitCount;
 	List<MapReduceEmit> outList;
 	private RemusDB db;
 	private RemusAttach attach;
 	private Map jobInfo;
 	private String pipeline;
 	private String applet;
 
 	public MapReduceCallback(String pipeline, String applet, Map jobInfo, RemusDB db, RemusAttach attach) {
 		init(pipeline, applet, jobInfo, db, attach);
 	}
 	
 	public void init(String pipeline, String applet, Map jobInfo, RemusDB db, RemusAttach attach) {
 		emitCount = 0;
 		this.jobInfo = jobInfo;
 		this.pipeline = pipeline;
 		this.applet = applet;
 		outList = new LinkedList<MapReduceEmit>();
 		this.db = db;
 		this.attach = attach;
 	}
 
 	public void emit(String key, Object val) {
 		outList.add(new MapReduceEmit(key, val, emitCount));
 		emitCount++;
 	}
 
 	public void writeEmits(AppletRef ar, long jobID) throws TException, NotImplemented {
 		for (MapReduceEmit mpe : outList) {
 			db.add(ar, jobID, mpe.emitID, mpe.key, mpe.val);
 		}
 	}
 
 	public InputStream openInput(String key, String name) {
 		Map inMap = (Map)jobInfo.get(PipelineSubmission.InputField);
 		try {
 			RemusInstance inst = RemusInstance.getInstance(db, pipeline, (String) inMap.get(PipelineSubmission.InstanceField));
 			AppletRef arAttach =  new AppletRef(pipeline, inst.toString(), (String) inMap.get("_applet"));
 			return attach.readAttachement(arAttach, key, name);
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
 	
 	public void copyTo(String key, String name, File file) {
 		RemusInstance inst;
 		try {
 			inst = RemusInstance.getInstance(db, pipeline, (String)jobInfo.get(PipelineSubmission.InstanceField));
 			AppletRef arAttach =  new AppletRef(pipeline, inst.toString(), applet);
 			attach.copyTo(arAttach, key, name, file);
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}

	public RemusDB getDatabase() {
		return db;
	}
	
	public String getPipeline() {
		return pipeline;
	}
 }
