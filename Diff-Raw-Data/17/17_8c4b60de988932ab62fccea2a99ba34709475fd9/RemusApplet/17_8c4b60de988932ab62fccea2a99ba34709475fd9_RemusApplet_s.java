 package org.remus.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.remus.thrift.Constants;
 import org.apache.thrift.TException;
 import org.json.simple.JSONAware;
 import org.remus.JSON;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.NotImplemented;
 import org.remus.work.WorkGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class RemusApplet implements JSONAware, Comparable<RemusApplet> {
 
 	Logger logger;
 
 	@SuppressWarnings("unchecked")
 	private String id;
 	private RemusPipeline pipeline;
 	private RemusDB datastore;
 	private RemusAttach attachstore;
 
 
 	private AppletInstanceRecord appletDesc;
 
 	public RemusApplet(RemusPipeline pipeline, String name, RemusDB datastore, RemusAttach attachstore) throws TException, NotImplemented, RemusDatabaseException {
 		logger = LoggerFactory.getLogger(RemusApplet.class);
 		id = name;
 		this.pipeline = pipeline;
 		this.datastore = datastore;
 		this.attachstore = attachstore;
 
 		AppletRef arApplet = new AppletRef(pipeline.getID(), 
 				RemusInstance.STATIC_INSTANCE_STR, Constants.PIPELINE_APPLET);
 
 		for (Object obj : datastore.get(arApplet, name)) {
 			appletDesc = new AppletInstanceRecord(obj);
 		}
 
 
 		if (appletDesc == null) {
 			throw new RemusDatabaseException("Applet Description not found");
 		}
 
 	}
 
 	public int getMode() {
 		try {
 			return appletDesc.getMode();
 		} catch (RemusDatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return AppletInstanceRecord.STORE;
 	}
 
 	public boolean isAuto() {
 		return appletDesc.isAuto();
 	}
 
 
 	public WorkGenerator getWorkGenerator() {
 		return appletDesc.getWorkGenerator();
 	}
 
 	
 
 	public Collection<RemusInstance> getInstanceList() {
 		Collection<RemusInstance> out = new HashSet<RemusInstance>();
 		AppletRef applet = new AppletRef(pipeline.getID(), 
 				RemusInstance.STATIC_INSTANCE_STR, 
 				Constants.INSTANCE_APPLET);
 		for (String key : datastore.listKeys(applet)) {
 			//BUG possible name match clashes
 			if (key.endsWith(":" + getID())) {
 				out.add(new RemusInstance(key.split(":")[0]));
 			}
 		}
 		return out;
 	}
 
 
 	public void deleteInstance(RemusInstance instance) throws TException, NotImplemented {
 		AppletRef ar = new AppletRef(pipeline.getID(), instance.toString(), getID());
 
 		logger.debug("DELETE:" + ar);
 		datastore.deleteStack(ar);
 		ar.applet = getID() + Constants.DONE_APPLET;
 		datastore.deleteStack(ar);
 		ar.applet = getID() + Constants.WORK_APPLET;
 		datastore.deleteStack(ar);
 		ar.applet = getID() + Constants.ERROR_APPLET;
 		datastore.deleteStack(ar);
 
 		ar.instance = RemusInstance.STATIC_INSTANCE_STR;
 		ar.applet = Constants.INSTANCE_APPLET;
 		datastore.deleteValue(ar, instance.toString() + ":" + getID());
 		ar.applet = Constants.WORK_APPLET;
 		datastore.deleteValue(ar, instance.toString() + ":" + getID());
 
 		if (attachstore != null) {
 			ar.applet = getID();
 			ar.instance = instance.toString();
 			attachstore.deleteStack(ar);
 		}
 	}
 
 	private static final List<String> suppressFields = Arrays.asList("_instance", "_code", "_script", "_src", "_pipeline", "_auto", "_input", "_output");
 
 
 	@SuppressWarnings("unchecked")
 	//public boolean createInstance(String submitKey, PipelineSubmission params, RemusInstance inst) throws TException, NotImplemented {
 	public boolean createInstance(PipelineSubmission params, RemusInstance inst) throws TException, NotImplemented, IOException, RemusDatabaseException {
 
 		logger.info("Creating instance of " + getID() + " for " + inst.toString());
 		AppletRef instApplet = new AppletRef(pipeline.getID(), RemusInstance.STATIC_INSTANCE_STR, Constants.INSTANCE_APPLET);
 
 		if (datastore.containsKey(instApplet, inst.toString() + ":" + getID())) {
 			return false;
 		}
 
 		Map baseMap = new HashMap();
 
 		if (params != null) {
 			for (Object key : params.base.keySet()) {
 				if ( !suppressFields.contains(key)) {
 					baseMap.put(key, params.base.get(key));
 				}
 			}
 		}
 
 		baseMap.put("_instance", inst.toString());
 		baseMap.put("_applet", getID());
 		baseMap.put("_pipeline",pipeline.getID());
 
 
 		for (Object key : ((Map)appletDesc.getBase()).keySet()) {
 			baseMap.put(key, ((Map)appletDesc.getBase()).get(key));
 		}
 
 		AppletInstanceRecord ai = new AppletInstanceRecord(baseMap);
 
 
 		if (ai.getMode() == AppletInstanceRecord.MERGER || ai.getMode() == AppletInstanceRecord.MATCHER) {
 			Map inMap = new HashMap();
 			Map lMap = new HashMap();
 			Map rMap = new HashMap();
 			lMap.put("_instance", inst.toString());
 			lMap.put("_applet", ai.getLeftSource());
 			rMap.put("_instance", inst.toString());
 			rMap.put("_applet", ai.getRightSource());
 			inMap.put("_left", lMap);
 			inMap.put("_right", rMap);				
 			inMap.put("_axis", "_left");
 			baseMap.put("_input", inMap);
 		} else if (ai.getMode() == AppletInstanceRecord.PIPE || ai.getMode() == AppletInstanceRecord.REMAPPER || ai.getMode() == AppletInstanceRecord.REREDUCER) {
 			if (ai.isAuto()) {
 				Map outList = new HashMap();
 				for (String input : ai.getSources()) {
 					Map inMap = new HashMap();
 					inMap.put("_instance", inst.toString());
 					inMap.put("_applet", input);
 					outList.put(input, inMap);
 				}
 				baseMap.put("_input", outList);
 			}
 		} else if (ai.hasSources() && ai.isAuto()) {
 			Map inMap = new HashMap();
 			inMap.put("_instance", inst.toString());
 			inMap.put("_applet", ai.getSource());
 			Map srcMap = new HashMap();
 			srcMap.put(ai.getSource(), inMap);
 			baseMap.put("_input", srcMap);			
 		}
 
 		if (params.hasSubmitInput()) {
 			if (params.hasSubmitInputApplet(getID())) {
 				Map inBaseMap = params.getSubmitInputAppletMap(getID());
 				Map<String,Map> inMap = new HashMap<String,Map>();
 				for ( String src : ai.getSources() ) {
 					if (inBaseMap.containsKey(src)) {
 						inMap.put(src, (Map)inBaseMap.get(src));
 					} else {
 						Map appMap = new HashMap();
 						appMap.put("_applet", src);
 						inMap.put(src, appMap);
 					}
 					if (!inMap.get(src).containsKey("_instance")) {
 						inMap.get(src).put("_instance", inst);
 					}
 				}
 				
 				baseMap.put("_input", inMap);
 			}
 		}
 
 		PipelineSubmission instInfo = new PipelineSubmission(baseMap);
 
 		/*
 		if (outputs != null) {
 			for (String output : outputs) {
 				try {
 					PipelineSubmission outputInfo = new PipelineSubmission(new HashMap(baseMap));
 					outputInfo.setInstance(inst);
 					outputInfo.setMode("output");
 					RemusApplet outApplet = new RemusApplet(pipeline, getID() + ":" + output, datastore, attachstore);
 					AppletInstance appInstance = new AppletInstance(pipeline.getID(), inst, outApplet.getID(), datastore, attachstore);
 					appInstance.updateInstanceInfo(outputInfo);
 				} catch (RemusDatabaseException e) {
 				}
 			}
 		}
 		 */
 		
 		datastore.add(instApplet, 0, 0, inst.toString() + ":" + getID(), instInfo);
 
 		AppletRef appletAR = new AppletRef(pipeline.getID(), Constants.STATIC_INSTANCE, Constants.PIPELINE_APPLET);
 		for (String file : attachstore.listAttachments(appletAR, getID())) {
 			InputStream is = readAttachment(file);
 			OutputStream os = attachstore.writeAttachment(instApplet, inst.toString() + ":" + getID(), file);
 			byte [] buffer = new byte[10240];
 			int len;
 			while ((len=is.read(buffer)) > 0) {
 				os.write(buffer,0,len);
 			}
 			os.close();
 			is.close();			
 		}
 
 		return true;
 	};
 
 
 	public void errorWork(RemusInstance inst, long jobID, String workerID, String error) throws TException, NotImplemented {
 		AppletRef applet = new AppletRef(pipeline.getID(), inst.toString(), getID() + Constants.ERROR_APPLET);
 		datastore.add(applet, 0L, 0L, Long.toString(jobID), error);
 	}
 
 	public void deleteErrors(RemusInstance inst) throws TException, NotImplemented {
 		AppletRef applet = new AppletRef(pipeline.getID(), inst.toString(), getID() + Constants.ERROR_APPLET);
 		datastore.deleteStack(applet);
 	};
 
 
 
 	@Override
 	public int hashCode() { 
 		return getID().hashCode();
 	};
 
 
 	@Override
 	public boolean equals(Object obj) {
 		RemusApplet a = (RemusApplet) obj;
 		return a.getID().equals(getID());
 	}
 
 	@Override
 	public int compareTo(RemusApplet o) {
 		return getID().compareTo(o.getID());
 	}
 
 	public RemusDB getDataStore() {
 		return datastore;
 	}
 
 	public String getID() {
 		return id;
 	}
 
 	public RemusAttach getAttachStore() {
 		return attachstore;
 	}
 
 	public AppletInstance getAppletInstance(String inst) throws TException, NotImplemented, RemusDatabaseException {
 		AppletInstance ai = new AppletInstance(pipeline.getID(), RemusInstance.getInstance(datastore, pipeline.getID(), inst), this.getID(), datastore, attachstore);
 		return ai;
 	}
 
 	@Override
 	public String toJSONString() {
 		return JSON.dumps(appletDesc);
 	}
 
 
 	public OutputStream writeAttachment(String name) throws IOException {
 		AppletRef arApplet = new AppletRef(pipeline.getID(), 
 				RemusInstance.STATIC_INSTANCE_STR, Constants.PIPELINE_APPLET);
 		return attachstore.writeAttachment(arApplet, getID(), name);
 	}
 
 	public InputStream readAttachment(String name) throws NotImplemented {
 		AppletRef arApplet = new AppletRef(pipeline.getID(), 
 				RemusInstance.STATIC_INSTANCE_STR, Constants.PIPELINE_APPLET);
 		return attachstore.readAttachment(arApplet, getID(), name);		
 
 	}
 
 	public boolean hasInstance(RemusInstance instance) throws NotImplemented, TException {
 		AppletRef inst = new AppletRef(pipeline.getID(), Constants.STATIC_INSTANCE, Constants.INSTANCE_APPLET);
 		if ( datastore.containsKey(inst, instance.toString() + ":" + getID())) {
 			return true;
 		}
 		return false;
 	}
 
 	public List<String> getSources() {
 		return appletDesc.getSources();
 	}
 
 }
