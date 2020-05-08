 package org.remus.test.js;
 
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.apache.thrift.TException;
 import org.json.simple.JSONValue;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.remus.JSON;
 import org.remus.KeyValPair;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.core.MemoryDB;
 import org.remus.core.PipelineDesc;
 import org.remus.core.RemusApp;
 import org.remus.core.RemusInstance;
 import org.remus.core.RemusPipeline;
 import org.remus.plugin.PeerManager;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.Constants;
 import org.remus.thrift.KeyValJSONPair;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.RemusNet;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.error.YAMLException;
 
 public class ReMapTest {
 	PluginManager plugManager;
 	PeerManager pm;
 	@SuppressWarnings("unchecked")
 	@Before public void setup() throws Exception {
 		Map initMap = new HashMap();
 		initMap.put("org.remus.js.JSWorker", null);
 		initMap.put("org.remus.manage.WorkManager", null);
 		initMap.put("org.remus.core.MemoryDB", null);
 		
 		plugManager = new PluginManager((Map) initMap);
 		plugManager.start();
 		pm = plugManager.getPeerManager();
 	}
 
 	@Test public void mapTest() throws RemusDatabaseException, TException, NotImplemented, InterruptedException {
 
 		RemusApp app = new RemusApp((RemusDB) pm.getPeer(pm.getDataServer()), (RemusAttach) pm.getPeer(pm.getAttachStore()));
 		
 		RemusPipeline pipe = app.getPipeline("testPipeline");
 		if (pipe != null) {
 			app.deletePipeline(pipe);
 		}
 		InputStream is = ReMapTest.class.getResourceAsStream("remap_pipeline.yaml");
 		Yaml y = new Yaml();
 		Object pipelineDesc = y.load(new InputStreamReader(is));
 		PipelineDesc pipeDesc = new PipelineDesc(pipelineDesc);
 		app.putPipeline("testPipeline", pipeDesc);
 		pipe = app.getPipeline("testPipeline");
 		System.err.println(pipe.getMembers());
 		
 		Map subMap = (Map) JSONValue.parse("{ \"_applets\" : [\"testReMap\"]  }");
 		RemusNet.Iface manager = pm.getPeer(pm.getManager());
 		AppletRef subAR = new AppletRef("testPipeline", Constants.STATIC_INSTANCE, Constants.SUBMIT_APPLET);
 		manager.addDataJSON(subAR, 0, 0, "testSubmit", JSON.dumps(subMap));
 		
 		RemusInstance inst = RemusInstance.getInstance(manager, "testPipeline", "testSubmit");
 
 		AppletRef ar = new AppletRef("testPipeline", inst.toString(), "inputStack");
 
 		RemusDB dataServer = (RemusDB) pm.getPeer(pm.getDataServer());
 				
 		
 		for (String appletName : pipeDesc.getApplets()) {
 			Map appletDesc = pipeDesc.getApplet(appletName);
 			if (appletDesc.containsKey("_init")) {
 				Map initMap = (Map) appletDesc.get("_init");
 				Map data = (Map) initMap.get("_data");
 				AppletRef dataAR = new AppletRef("testPipeline", inst.toString(), appletName);
 				for (Object a : data.keySet()) {
 					dataServer.addDataJSON(dataAR, 0, 0, (String)a, JSON.dumps(data.get(a)));
 				}
 			}
 		}
 		
 		RemusNet.Iface manage = pm.getPeer(pm.getManager());
 
 		boolean done = false;
 		do {
			manage.scheduleRequest();		
 			Thread.sleep(10000);
			Map info = (Map) JSON.loads(manage.scheduleInfoJSON());
			if (((String) info.get("activeCount")).compareTo("0") == 0) {
				done = true;
 			}
			System.out.println(info);
 		} while (!done);
 		
 		//System.out.println(dataServer);
 		
 		AppletRef outAR = new AppletRef("testPipeline", inst.toString(), "testReMap");
 		for (KeyValPair kv : dataServer.listKeyPairs(outAR)) {
 			System.out.println(kv.getKey() + " " + kv.getValue());
 		}
 		
 
 	}
 
 	@After public void tearDown() throws RemusDatabaseException, TException {
 		
 		RemusApp app = new RemusApp((RemusDB) pm.getPeer(pm.getDataServer()), (RemusAttach) pm.getPeer(pm.getAttachStore()));
 		RemusPipeline pipe = app.getPipeline("testPipeline");
 		app.deletePipeline(pipe);
 		
 		plugManager.close();
 	}
 
 }
