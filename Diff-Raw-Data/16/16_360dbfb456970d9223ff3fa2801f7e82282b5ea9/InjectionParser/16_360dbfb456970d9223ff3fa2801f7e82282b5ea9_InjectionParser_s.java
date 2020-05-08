 package evaluation.simulator.gui.pluginRegistry;
 
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import evaluation.simulator.conf.service.SimulationConfigService;
 
 class InjectionParser {
 	
 	private static Logger logger = Logger.getLogger(InjectionParser.class);
 	public String getLayer() {
 		return layer;
 	}
 
 	public String getPlugin() {
 		return plugin;
 	}
 
 	public String getLayerConfigName() {
 		return layerConfigName;
 	}
 
 	public String getLayerDisplayName() {
 		return layerDisplayName;
 	}
 
 	public String getPluginConfigName() {
 		return pluginConfigName;
 	}
 
 	public String getPluginDisplayName() {
 		return pluginDisplayName;
 	}
 
 	public int getLayerPosition() {
 		return layerPosition;
 	}
 
 	public int getPluginPosition() {
 		return pluginPosition;
 	}
 
 	public boolean isGlobalProperty() {
 		return globalProperty;
 	}
 
 	private String layer = "";
 	private String plugin = "";
 	private String layerConfigName = "";
 	private String layerDisplayName = "";
 	private String pluginConfigName = "";
 	private String pluginDisplayName = "";
 	private int layerPosition = 0;
 	private int pluginPosition = 0;
 	private boolean globalProperty = true;
 	
 	public InjectionParser( String arguments, String propertyKey ){
 		String[] injectionArguments = arguments.split("@");
 		
 		// Process layer information
 		if ( injectionArguments.length >= 1) {
 			layer = injectionArguments[0];
 			String[] layerSplit = layer.split(",");
 			if ( layerSplit.length >= 1 ){
 				layerConfigName = layerSplit[0];
 				String[] tmp = layerConfigName.split(":");
 				if ( tmp.length == 2){
 					layerPosition = Integer.valueOf(tmp[0]);
 					layerConfigName = tmp[1];
 				}
 				if ( tmp.length > 2){
 					logger.log(Level.DEBUG, "Inject annotation for " + 
 							   propertyKey + " is not well formed \n Injection string is " + arguments);
 					// This might not be critical, but it is better to quit
 					System.exit(-1);
 				}
 			}
 			if ( layerSplit.length >= 2 ){
 				layerDisplayName = layerSplit[1];
 			}
 			if ( layerSplit.length >= 3 ) {
 				logger.log(Level.DEBUG, "Inject annotation for " + 
 						   propertyKey+ " is not well formed \n Injection string is " + arguments);
 				// This might not be critical, but it is better to quit
 				System.exit(-1);
 			}
 		}
 		
 		// Process the plugin information
 		if ( injectionArguments.length >= 2 ) {
 			plugin = injectionArguments[1];
 			String[] pluginSplit = plugin.split(",");
 			if ( pluginSplit.length >= 1 ){
				pluginConfigName = pluginSplit[0];
				String[] tmp = pluginConfigName.split(":");
				if ( tmp.length == 2){
					pluginPosition = Integer.valueOf(tmp[0]);
					pluginConfigName = tmp[1];
				}
				if ( tmp.length > 2){
					logger.log(Level.DEBUG, "Inject annotatioin for " + 
							   propertyKey + " is not well formed \n Injection string is " + arguments);
					// This might not be critical, but it is better to quit
					System.exit(-1);
				}
 			}
 			if ( pluginSplit.length >= 2 ){
 				pluginDisplayName = pluginSplit[1];
 			}
			if ( pluginSplit.length >= 3 ) {
 				logger.log(Level.DEBUG, "Inject annotatioin for " + 
 						   propertyKey + " is not well formed \n Injection string is " + arguments);
 				// This might not be critical, but it is better to quit
 				System.exit(-1);
 			}
 			
 			// overwrite visibility since we have got a plugin
 			// generally plugins pluginproperties are not global
 			globalProperty = false;
 		} 
 		
 		if ( injectionArguments.length >= 3 ) {
 			logger.log(Level.ERROR, "Can not inject " + propertyKey + 
 					   " due to bad injection arguments! \n Injection string is " + arguments);
 			System.exit(-1);
 		}else{
 			logger.log(Level.DEBUG, "Injected property " + propertyKey + " into " + layer + "@" + plugin);
 		}
 	}
 	
 }
