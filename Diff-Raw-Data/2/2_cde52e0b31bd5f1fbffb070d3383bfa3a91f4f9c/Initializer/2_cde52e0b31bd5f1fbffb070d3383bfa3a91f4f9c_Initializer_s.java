 /*******************************************************************************
  * Copyright 2012 Patrick O'Leary
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.pjaol.ESB.config;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import com.pjaol.ESB.core.Controller;
 import com.pjaol.ESB.core.Evaluator;
 import com.pjaol.ESB.core.Module;
 import com.pjaol.ESB.core.PipeLine;
 
 public class Initializer {
 
 	private Logger _logger = Logger.getLogger(getClass());
 	private ESBCore core = ESBCore.getInstance();
 	private Map<String, Controller> uris = new HashMap<String, Controller>();
 
 	/**
 	 * Using Components
 	 * @throws ConfigurationException
 	 */
 	public void startup() throws ConfigurationException {
 
 		// add libs to the classpath
 		addLibs();
 		
 		// Initialize and put pipelines in the core
 		try {
 			Map<String, PipeLineComponent> pipeComponents = core.getPipeLineComponent();
 			Map<String, PipeLine> pipelines = startupPipes(pipeComponents);
 			core.setPipelines(pipelines);
 			
 			// initialize monitor after all pipelines have been init
 			// this gives the ESBMonitor pipeline the opportunity to start
 			for(Entry<String, PipeLine> k: pipelines.entrySet()){
 				k.getValue().initializeMonitor();
 			}
 		} catch (InstantiationException e) {
 			throw new ConfigurationException(e);
 		} catch (IllegalAccessException e) {
 			throw new ConfigurationException(e);
 		} catch (ClassNotFoundException e) {
 			throw new ConfigurationException(e);
 		}
 
 		// Initialize and put controllers in the core
 		try {
 			core.setControllers(startupControllers(core
 					.getControllerComponent()));
 		} catch (InstantiationException e) {
 			throw new ConfigurationException(e.getMessage());
 		} catch (IllegalAccessException e) {
 			throw new ConfigurationException(e.getMessage());
 		} catch (ClassNotFoundException e) {
 			throw new ConfigurationException(e.getMessage());
 		}
 
 		// uris are created in startupControllers
 		core.setControllerUris(uris);
 
 	}
 	
 	private void initializeMonitors(){
 		
 	}
 
 	private Map<String, PipeLine> startupPipes(
 			Map<String, PipeLineComponent> items)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		Map<String, PipeLine> result = new HashMap<String, PipeLine>();
 
 		for (PipeLineComponent c : items.values()) {
 			// System.out.println(c);
 			PipeLine pipeline = new PipeLine();
 			pipeline.setName(c.getName());
 
 			Evaluator evaluator = initializeEvaluator(c.getEvaluator());
 			pipeline.setEvaluator(evaluator);
 			pipeline.setTimeout(c.getTimeout());
 
 			List<ConfigurationComponent> modules = c.getModules();
 			List<Module> pipeModules = new ArrayList<Module>();
 			for (ConfigurationComponent mod : modules) {
 
 				Module m = initializeModule(mod);
 				pipeModules.add(m);
 			}
 			
 			pipeline.setModules(pipeModules);
 			pipeline.init(c.getArgs());
 			//pipeline.initializeMonitor();
 			
 			result.put(c.getName(), pipeline);
 		}
 
 		return result;
 	}
 
 	private Map<String, Controller> startupControllers(
 			Map<String, ControllerComponent> items)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		Map<String, Controller> result = new HashMap<String, Controller>();
 
 		for (ControllerComponent controllerComponent : items.values()) {
 			Controller controller = initializeController(controllerComponent);
 			result.put(controllerComponent.getName(), controller);
 			uris.put(controllerComponent.getUri(), controller);
 		}
 
 		return result;
 	}
 
 	private Controller initializeController(
 			ControllerComponent controllerComponent)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		String className = controllerComponent.getClassName();
 		_logger.info("Initializing controller: "
 				+ controllerComponent.getName() + " : " + className);
 		Controller controller = extracted(className);
 		controller.setName(controllerComponent.getName());
 		controller.setPipelines(controllerComponent.getPipelines());
 		controller.setPipes(controllerComponent.getPipes());
 		controller.setUri(controllerComponent.getUri());
 		controller.setLimitorPipeLines(controllerComponent
 				.getLimiterPipeLines());
 		controller.setLimitorName(controllerComponent.getLimiterName());
 		controller.setTimeout(controllerComponent.getTimeout());
 
 		controller.init(controllerComponent.getArgs());
 		controller.initializeMonitor();
 		
 		return controller;
 
 	}
 
 	private Evaluator initializeEvaluator(EvaluatorComponent evaluatorComponent)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		String className = evaluatorComponent.getClassName();
 		_logger.info("Initializing evaluator: " + evaluatorComponent.getName()
 				+ " : " + className);
 		Evaluator e = extracted(className);
 		e.setName(evaluatorComponent.getName());
 		e.init(evaluatorComponent.getArgs());
 		
 		return e;
 	}
 
 	private Module initializeModule(ConfigurationComponent module)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		String className = module.getClassName();
 		_logger.info("Initializing Module: " + module.getName() + " : "
 				+ className);
 		Module m = extracted(className);
 		m.setName(module.getName());
 		m.init(module.getArgs());
 		m.initializeMonitor();
 		
 		return m;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T extracted(String className) throws InstantiationException,
 			IllegalAccessException, ClassNotFoundException {
 		return (T) Thread.currentThread().getContextClassLoader().getClass().forName(className).newInstance();
 		//return (T) Class.forName(className).newInstance();
 	}
 	
 	
 	private void addLibs(){
 		Map<String, String> globals = core.getGlobals();
 		String libs = globals.get("lib");
 		URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader().getParent();
 		
 		URL[] urls = urlClassLoader.getURLs();
 		
 		if (libs != null){
 			String [] libPaths = libs.split(",|\n");
 			for(String path : libPaths){
 				path = path.trim();
 				File f = new File(path);
 				
 				if (! f.exists()){
 					// search for the path from the basicesb home
 					String home = System.getProperty(BasicESBVariables.basicESBHomeProperty);
 					String homePath = home+File.separatorChar+path;
 					f = new File(homePath);
 					if (! f.exists()){
 						String cwd = new File(".").getAbsolutePath();
 						_logger.error("Loading lib : " + path+" failed at "+ cwd+" and "+homePath );
 						continue;
 					}
 				}
 
 				
 				_logger.info("Loading path: "+ path);
 				
 				String[] jars = f.list(new FilenameFilter() {
 					
 					@Override
 					public boolean accept(File dir, String fileName) {
 						// Do not load the servlet api, conflicts with servlet container
 						if (fileName.startsWith("servlet-api")){
 							_logger.info("Skipping : "+ fileName);
 							return false;
 						}
 						
 						return fileName.endsWith(".jar");
 					}
 				});
 				
 				for(String jar: jars){
 					try {
 						String fullPath = path+File.separator+jar;
 						addPath(fullPath);
 						_logger.info("Adding jar: "+ fullPath);
 					} catch (Exception e) {
 						_logger.error("Failed to load jar: "+jar+"\n"+ e.getMessage());
 						e.printStackTrace();
 					}
 				}
 				
 				
 			}
 		}
 	}
 	
 	public static void addPath(String s) throws Exception {
 		  File f = new File(s);
 		  if (! f.exists()){
 			  System.err.println("Cannot file file: "+ s);
 		  }
 		  
 		  URI fURI = f.toURI();
 		  URL u = fURI.toURL();
 		  
 		  
 		  URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader(); 
 		  Class urlClass = URLClassLoader.class;
 		  Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
 		  method.setAccessible(true);
 		  method.invoke(urlClassLoader, new Object[]{u});
 		  
 		  
 		}
 }
