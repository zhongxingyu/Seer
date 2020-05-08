 package eu.play_project.dcep.distributedetalis.configurations;
 
import java.io.Serializable;

 import com.jtalis.core.JtalisContextImpl;
 
 import eu.play_project.dcep.distributedetalis.EcConnectionManagerNet;
 import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
 import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
 import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
 import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
 import eu.play_project.dcep.distributedetalis.api.Configuration;
 import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
 import eu.play_project.dcep.distributedetalis.api.EcConnectionManager;
 import eu.play_project.play_commons.constants.Constants;
 
public class DefaultConfiguration implements Configuration, Serializable{
 
 
 	@Override
 	public void configure(DEtalisConfigApi dEtalisConfigApi) {
 		
 		// Init ETALIS
 		PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
 		JtalisContextImpl etalis = null;
 		
 		try {
 			etalis = new JtalisContextImpl(engine);
 			dEtalisConfigApi.setEtalis(etalis);
 		} catch (Exception e) {
 			dEtalisConfigApi.getLogger().error("Error initializing ETALIS", e);
 		}
 		
 		// Load Semantic Web Library
 		PrologSemWebLib semWebLib = new PrologSemWebLib();
 		dEtalisConfigApi.setSemWebLib(semWebLib);
 		semWebLib.init(etalis);
 		
 		dEtalisConfigApi.setEventInputProvider(new JtalisInputProvider(semWebLib));
 		dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerNet(Constants
 				.getProperties().getProperty("eventcloud.registry"), dEtalisConfigApi.getDistributedEtalis()));
 		dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
 		dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
 				dEtalisConfigApi.getEventSinks(), dEtalisConfigApi.getRegisteredQuerys(),
 				dEtalisConfigApi.getEcConnectionManager()));
 
 		dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
 		dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());
 
//		engine.consult(System.getProperty("user.dir")
//				+ "/src/main/resources/PrologMethods/constructQueryImp.pl");
//		engine.consult(System.getProperty("user.dir")
//				+ "/src/main/resources/PrologMethods/ReferenceCounting.pl");
//		engine.consult(System.getProperty("user.dir")
//				+ "/src/main/resources/PrologMethods/Measurement.pl");
//		engine.consult(System.getProperty("user.dir")
//				+ "/src/main/resources/PrologMethods/Math.pl");
 
 		// engine.consult("/opt/play-platform-src/play-dcep/play-dcep-distributedetalis/src/main/resources/PrologMethods/constructQueryImp.pl");
 		// engine.consult("/opt/play-platform-src/play-dcep/play-dcep-distributedetalis/src/main/resources/PrologMethods/ReferenceCounting.pl");
 		// engine.consult("/opt/play-platform-src/play-dcep/play-dcep-distributedetalis/src/main/resources/PrologMethods/Measurement.pl");
 		// engine.consult("/opt/play-platform-src/play-dcep/play-dcep-distributedetalis/src/main/resources/PrologMethods/Math.pl");
 
 		// Set ETALIS properties.
 		etalis.setEtalisFlags("save_ruleId", "on");
 		etalis.addEventTrigger("complex/_");
 		etalis.addEventTrigger("realtimeResult/2");
 		// etalis.setEtalisFlags("event_consumption_policy",
 		// "chronological");
 		// etalis.setEtalisFlags("logging","off");
 		// etalis.setEtalisFlags("java_notification","on");
 
 
 		// Instatiate measurement unit.
 		// this.measurementUnit = new MeasurementUnit(this,	
 	}
 }
