 package org.kevoree.library.android.nodeType;
 
 import org.kevoree.ContainerRoot;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.logging.KevoreeLogLevel;
 import org.kevoree.framework.AbstractNodeType;
 import org.kevoree.library.defaultNodeTypes.CommandMapper;
 import org.kevoree.library.defaultNodeTypes.planning.KevoreeKompareBean;
 import org.kevoree.log.Log;
 import org.kevoreeadaptation.AdaptationModel;
 import org.kevoreeadaptation.AdaptationPrimitive;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * User: ffouquet
  * Date: 15/09/11
  * Time: 17:10
  */
 
 @Library(name = "Android")
 @NodeType
 @DictionaryType({
 		@DictionaryAttribute(name = "logLevel", defaultValue = "DEBUG", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"}),
 		@DictionaryAttribute(name = "coreLogLevel", defaultValue = "DEBUG", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"})
 })
 @PrimitiveCommands(
 		values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty", "RemoveThirdParty"},
 		value = {})
 public class AndroidNode extends AbstractNodeType {
 
 	private KevoreeKompareBean kompareBean = null;
 	private CommandMapper mapper = null;
 	private boolean isRunning;
 
     private Map<String, Object> registry;
 
 	@Start
 	@Override
 	public void startNode () {
 		isRunning = true;
         registry = new HashMap<String, Object>();
 		kompareBean = new KevoreeKompareBean(registry);
 		mapper = new CommandMapper(registry);
 		mapper.setNodeType(this);
 		setLogLevel();
 	}
 
 
 	@Stop
 	@Override
 	public void stopNode () {
 		kompareBean = null;
 		mapper = null;
 		isRunning = false;
 		//Cleanup the local runtime
 //		KevoreeDeployManager.instance$.clearAll(this);
         registry.clear();
 	}
 
 	@Update
 	public void updateNode () {
 		Log.info("Updating node and maybe log levels...");
 		setLogLevel();
 	}
 
 	private void setLogLevel () {
 		if (getBootStrapperService().getKevoreeLogService() != null) {
             Log.info("setting log levels...");
 			KevoreeLogLevel logLevel = KevoreeLogLevel.WARN;
 			KevoreeLogLevel corelogLevel = KevoreeLogLevel.WARN;
 			if ("DEBUG".equals(getDictionary().get("logLevel"))) {
 				logLevel = KevoreeLogLevel.DEBUG;
 			}
 			if ("WARN".equals(getDictionary().get("logLevel"))) {
 				logLevel = KevoreeLogLevel.WARN;
 			}
 			if ("INFO".equals(getDictionary().get("logLevel"))) {
 				logLevel = KevoreeLogLevel.INFO;
 			}
 			if ("ERROR".equals(getDictionary().get("logLevel"))) {
 				logLevel = KevoreeLogLevel.ERROR;
 			}
 			if ("FINE".equals(getDictionary().get("logLevel"))) {
 				logLevel = KevoreeLogLevel.FINE;
 			}
 
 			if ("DEBUG".equals(getDictionary().get("coreLogLevel"))) {
 				corelogLevel = KevoreeLogLevel.DEBUG;
 			}
 			if ("WARN".equals(getDictionary().get("coreLogLevel"))) {
 				corelogLevel = KevoreeLogLevel.WARN;
 			}
 			if ("INFO".equals(getDictionary().get("coreLogLevel"))) {
 				corelogLevel = KevoreeLogLevel.INFO;
 			}
 			if ("ERROR".equals(getDictionary().get("coreLogLevel"))) {
 				corelogLevel = KevoreeLogLevel.ERROR;
 			}
 			if ("FINE".equals(getDictionary().get("coreLogLevel"))) {
 				corelogLevel = KevoreeLogLevel.FINE;
 			}
 			getBootStrapperService().getKevoreeLogService().setUserLogLevel(logLevel);
 			getBootStrapperService().getKevoreeLogService().setCoreLogLevel(corelogLevel);
 		}
 	}
 
 	@Override
	public AdaptationModel plan (ContainerRoot current, ContainerRoot target) {
		return kompareBean.plan(current, target, this.getNodeName());
 	}
 
 	@Override
 	public org.kevoree.api.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
         //if(adaptationPrimitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveDeployUnit())){
         //      return new NoopPrimitiveCommand();
         //} else {
             return mapper.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
         //}
 	}
 }
