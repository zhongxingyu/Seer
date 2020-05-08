 package com.shadowolf.config;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 
 public class Config {
 	private static final boolean DEBUG = true;
 	private static final Logger LOGGER = Logger.getLogger(Config.class);
 
 	public static final String DEFAULT_CONF_PATH = "/WEB-INF/config.xml";
 
 	private static List<PluginConfig> plugins = new ArrayList<PluginConfig>();
 	private static Map<String, String> parameters = new HashMap<String, String>();
 
 	public static String getParameter(final String param) {
		return parameters.get(param);
 	}
 
 	public static void parseConfigElement(final Element rootElement) {
 		if(!rootElement.hasChildren()) {
 			return;
 		}
 
 		for(final Element e : rootElement.getChildren()) {
 			if(e.getName().equals("plugin")) {
 				if(DEBUG) {
 					LOGGER.debug("Adding plugin: " + e.getAttribute("class"));
 				}
 				final PluginConfig pConf = new PluginConfig(e.getAttribute("class"));
 
 				if(e.hasChildren()) {
 					for(final Element option : e.getChildren()) {
 						if((option.getText() == null) || option.getText().trim().equals("")) {
 							continue;
 						}
 
 						if(DEBUG) {
 							LOGGER.debug(option.getAttribute("name") + option.getText().trim());
 						}
 						if(option.getAttribute("name").equals("unit")) {
 							pConf.setTimeUnit(option.getText().trim());
 							if(DEBUG) {
 								LOGGER.debug("Setting unit to: " + option.getText().trim());
 							}
 						} else if(option.getAttribute("name").equals("period")) {
 							if(DEBUG) {
 								LOGGER.debug("Setting period to: " + option.getText().trim());
 							}
 							pConf.setPeriod(option.getText().trim());
 						} else if(option.getAttribute("name").equals("delay")) {
 							if(DEBUG) {
 								LOGGER.debug("Setting delay to: " + option.getText().trim());
 							}
 							pConf.setDelay(option.getText().trim());
 						} else {
 							if(DEBUG) {
 								LOGGER.debug("Adding option: " + option.getAttribute("name") + " with text: " + option.getText().trim());
 							}
 							pConf.setParameter(option.getAttribute("name"), option.getText().trim());
 						}
 					}
 				}
 
 				plugins.add(pConf);
 			} else if (e.getName().equals("parameter")) {
 				parameters.put(e.getAttribute("name"), e.getText());
 				if(DEBUG) {
 					LOGGER.debug("Adding parameter: " + e.getAttribute("name") + " with text: " + e.getText().trim());
 				}
 			}
 		}
 	}
 
 
 	public static List<PluginConfig> getPlugins() {
 		return plugins;
 	}
 
 
 	public static Map<String, String> getParameters() {
 		return parameters;
 	}
 
 }
