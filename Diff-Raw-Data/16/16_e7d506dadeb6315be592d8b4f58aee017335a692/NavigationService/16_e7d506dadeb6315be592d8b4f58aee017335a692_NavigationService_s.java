 package evaluation.simulator.gui.customElements;
 
 import java.util.Map;
 
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 public class NavigationService {
 
 	private static String path = "etc/html/pluginHelp/";
 	private static Map<String, String> layerMapDisplayNameToConfigName;
 	private static Map<String, SimProp> propertyMap;
 	private static Map<String, String> registeredPlugins;
 
 	private static String content() {
 
 		layerMapDisplayNameToConfigName = SimPropRegistry.getInstance().getLayerMapDisplayNameToConfigName();
 		propertyMap = SimPropRegistry.getInstance().getProperties();
 		registeredPlugins = SimPropRegistry.getInstance().getRegisteredPlugins();
 		String menu = "";
 
 		for ( String layer : layerMapDisplayNameToConfigName.keySet() ){
			menu+="<h1>"+layer+"</h1>";
 			for ( String prop : propertyMap.keySet() ){
 				if ( propertyMap.get(prop).getPluginID().equals("") &&
 						( propertyMap.get(prop).isSuperclass() || propertyMap.get(prop).isGlobal() ) &&
 						propertyMap.get(prop).getPluginLayerID().equals(layerMapDisplayNameToConfigName.get(layer)) ){
 
					menu += "<h2><a href=\"etc/html/plugins/"
							+ prop+".html\">"
							+ prop + "</a></h2>\n";
 				}
 			}
 			for ( String plugin : registeredPlugins.keySet() ){
 				if ( registeredPlugins.get(plugin).equals(layerMapDisplayNameToConfigName.get(layer))){
 
 					// plugins
					menu += "<h2><a href=\"etc/html/plugins/"
							+ plugin+".html\">"
							+ plugin + "</a></h2>\n";
 					//					for ( String prop : propertyMap.keySet() ){
 					//						if ( propertyMap.get(prop).getPluginID().equals(plugin) ){
 					//							// plugin dependent properties
 					//							menu += "<a href=\"etc/html/plugins/"
 					//									+ prop+".html\">"
 					//									+ prop + "</a><br/>\n";
 					//						}
 					//					}
 				}
 			}
 		}
 		return menu;
 	}
 
 	public static String getMenu() {
 		String head = ""
 				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"
 				+ "<head>\n" + "<title> Navigation </title>\n" + "</head>\n"
 				+ "<body>\n" + "<h1>Navigation</h1>\n"
 				+ "<a href=\"VIDEO1\">VIDEOTUTORIAL_TEST</a><br/>";
 		String tail = "</p>\n" + "</body>\n" + "</html>";
 
 		return head + content() + tail;
 	}
 }
