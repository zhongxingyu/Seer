 package com.smarty4j.utils;
 
 import java.io.File;
 
 import javax.servlet.ServletContext;
 
 import org.lilystudio.smarty4j.Engine;
 
 public class SmartyManager {
 	
 	public static String INITPARAM_TEMPLATE_PATH = "TemplatePath";	//ʼ
 	public static final String CONFIG_SERVLET_CONTEXT_KEY = "smarty.engine";
 	
 	protected String templatePath; //ģ·
 	protected Engine engine;
 	public Engine getEngine() {
 		return engine;
 	}
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 	}
 	
 	public synchronized Engine getEngine(ServletContext servletContext){
 		if (engine == null) {
 			initEngine(servletContext);
 			servletContext.setAttribute(CONFIG_SERVLET_CONTEXT_KEY, engine);
 		}
 		return engine;
 	}
 	
 	/**
 	 * ʼģ
 	 * @param servletContext
 	 */
 	public void initEngine(ServletContext servletContext){
 		//ʼģ
 		engine = new Engine();
 		//ģ·
 		templatePath = servletContext.getInitParameter(INITPARAM_TEMPLATE_PATH);
 		if (templatePath == null) {
 			templatePath = servletContext.getInitParameter("templatePath");
 		}
 		
 		if (templatePath == null){
 			templatePath = servletContext.getRealPath("/");
 		}
 
 		if (!templatePath.endsWith(File.separator)) {
 			templatePath = templatePath + File.separator;
 		}
 		engine.setTemplatePath(templatePath);	//ģ·
 	}
 	
 }
