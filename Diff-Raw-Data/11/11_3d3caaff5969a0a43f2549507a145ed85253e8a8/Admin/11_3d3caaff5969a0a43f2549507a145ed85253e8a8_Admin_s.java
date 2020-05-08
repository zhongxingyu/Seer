 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.societies.collabtools.webapp.controller;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.cis.management.ICis;
 import org.societies.api.cis.management.ICisManager;
 import org.societies.api.context.model.CtxModelType;
 import org.societies.collabtools.api.AbstractCollabAppConnector;
 import org.societies.collabtools.api.ICollabApps;
 import org.societies.collabtools.api.IContextSubscriber;
 import org.societies.collabtools.api.IEngine;
 import org.societies.collabtools.runtime.Operators;
 import org.societies.collabtools.runtime.Rule;
 import org.societies.collabtools.webapp.model.ContextForm;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class Admin {
 
 	private static final Logger logger = LoggerFactory.getLogger(Admin.class);
 
 	//	AsyncCollabAppConnector app = new AsyncCollabAppConnector();
 
 	@Autowired
 	private ICisManager cisManager;
 
 	private String cisName = "";
 
 	@RequestMapping(value = "/index.html", method = RequestMethod.GET)
 	public String index() {
 		return "index";
 	}
 	
 	@RequestMapping(value = "/download.html", method = RequestMethod.GET)
 	public String download() {
 		return "download";
 	}
 
 	@RequestMapping(value="/default.html",method = RequestMethod.GET)
 	public ModelAndView DefaultPage() {
 		//model is nothing but a standard Map object
 		Map<String, Object> model = new HashMap<String, Object>();
 
 		List<ICis> cisList = new ArrayList<ICis>();
 		cisList = cisManager.getCisList();
 		List<String> cisNames = new ArrayList<String>();
 		List<String> cisIdList = new ArrayList<String>();
 		int size = cisList.size();
 		String result = "<br>"+"There are "+size+" CIS: "+"<br>";
 		result += "<form name=\"radioform\"\" method=\"get\">";
 		for (ICis list : cisList) {
 			if (cisName.equalsIgnoreCase(list.getCisId())) {
 				result +="<input type=\"radio\" id=\""+list.getCisId()+"\" name=\"cisIDListRadio\" onClick=\"setText('"+list.getCisId()+"')\" value=\""+list.getCisId()+"\">"+list.getName()+"   - Owner: "+list.getOwnerId()+" [Running] "+"<br>";
 			}
 			else {
 				result +="<input type=\"radio\" id=\""+list.getCisId()+"\" name=\"cisIDListRadio\" onClick=\"setText('"+list.getCisId()+"')\" value=\""+list.getCisId()+"\">"+list.getName()+"   - Owner: "+list.getOwnerId()+"<br>";
 			}
 			cisNames.add(list.getName());
 			cisIdList.add(list.getCisId());
 		}
 		result += "</form>";
 		model.put("message", result);
 		model.put("cisNames", cisNames);
 		model.put("cisIdList", cisIdList);
 
 		return new ModelAndView("default", model) ;
 	}
 
 
 	@RequestMapping(value = "/applications.html", method = RequestMethod.GET)
 	public ModelAndView collabApps() {
 		AbstractCollabAppConnector[] collabAppsConnectors = getCollabAppsConnectors().getCollabAppConnectors();
 		List<String> appnames = new ArrayList<String>();
 		List<String> appserver = new ArrayList<String>();
 		for (AbstractCollabAppConnector apps : collabAppsConnectors){
 			appnames.add(apps.getAppName());
 			appserver.add(apps.getAppServerName());
 		}
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("appnames", appnames);
 		model.put("appserver", appserver);
 		return new ModelAndView("applications", model) ;
 	}
 
 	@RequestMapping(value = "/checkcis.html", method = RequestMethod.GET)
 	public @ResponseBody String checkCisID(@RequestParam String name, String check) {
 		String result;
 		if (getCtxSubscriber() == null){
 			return result="Collabtools is not running";
 		}
 		if (!name.isEmpty()) {
 			if (check.equalsIgnoreCase("start")){
 				result = "Started with cisID: "+name;
 				getCtxSubscriber().initialCtx(name);
 				cisName  = name;
 			}
 			else {
 				result = "Stop cisID: "+name;
 				getCtxSubscriber().stopCtx(name);
 				cisName  = "";
 			}
 
 		}
 		else {
 			result = "CisID is empty, please check again";
 		}
 		return result;
 	}
 
 	@RequestMapping(value="/iphone.html",method = RequestMethod.GET)
 	public ModelAndView iphoneTest() {
 		//model is nothing but a standard Map object
 		Map<String, Object> model = new HashMap<String, Object>();
 		List<ICis> cisList = new ArrayList<ICis>();
 		cisList = cisManager.getCisList();
 		int size = cisList.size();
 		List<String[]> cisresults = new ArrayList<String[]>();
 		for (ICis list : cisList) {
 			String[] elements = {list.getName(), list.getOwnerId(), list.getCisId()}; 
 			cisresults.add(elements);
 		}
 
 		model.put("cisresults", cisresults);
 		model.put("size", size);
 		
 		//Rules
 		List<String> attributeTypes = getTypesList(org.societies.api.context.model.CtxAttributeTypes.class);
 		attributeTypes.addAll(getTypesList(org.societies.api.context.model.CtxAttributeTypes.class));
 		model.put("attributeTypes", attributeTypes);
 		
 		List<String[]> rulesresults = new ArrayList<String[]>();
 		for (Rule rule : getEngine().getRules()) {
 			String[] elements = {rule.getName(), rule.getCtxAttribute(), rule.getOperator().toString(), rule.getCtxType(), Integer.toString(rule.getPriority()), rule.getValue()}; 
 			rulesresults.add(elements);
 		}
 		model.put("rulesresults", rulesresults);
 		
 		//Applications
 		AbstractCollabAppConnector[] collabAppsConnectors = getCollabAppsConnectors().getCollabAppConnectors();
 		List<String> appnames = new ArrayList<String>();
 		List<String> appserver = new ArrayList<String>();
 		for (AbstractCollabAppConnector apps : collabAppsConnectors){
 			appnames.add(apps.getAppName());
 			appserver.add(apps.getAppServerName());
 		}
 		model.put("appnames", appnames);
 		model.put("appserver", appserver);
 
 		return new ModelAndView("iphone", model) ;
 	}
 
 	@RequestMapping(value="/android.html",method = RequestMethod.GET)
 	public ModelAndView androidTest() {
 		//model is nothing but a standard Map object
 		Map<String, Object> model = new HashMap<String, Object>();
 
 		return new ModelAndView("android", model) ;
 	}
 
 
 	@RequestMapping(value="/rules.html",method = RequestMethod.GET)
 	public ModelAndView Rules() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("ctxForm", new ContextForm());
 
 		List<String> values1 = getTypesList(org.societies.api.context.model.CtxAttributeTypes.class);
 		values1.addAll(getTypesList(org.societies.api.context.model.CtxAttributeTypes.class));
 		model.put("attributeTypes", values1);
 
 		List<String[]> rulesresults = new ArrayList<String[]>();
 		for (Rule rule : getEngine().getRules()) {
 			String[] elements = {rule.getName(), rule.getCtxAttribute(), rule.getOperator().toString(), rule.getCtxType(), Integer.toString(rule.getPriority()), rule.getValue()}; 
 			rulesresults.add(elements);
 		}
 
 		model.put("rulesresults", rulesresults);
 		model.put("attribute_label", CtxModelType.ATTRIBUTE.name().toString());
 
 		return new ModelAndView("rules", model) ;
 	}
 
 	@RequestMapping(value = "/rulesmanager.html", method = RequestMethod.GET)
 	public @ResponseBody String RulesManager(
 			@RequestParam(value = "value0", required = true) String ruleName, 
 			@RequestParam(value = "value1", required = true) String ctxAttr, 
 			@RequestParam(value = "value2", required = true) Operators operator, 
 			@RequestParam(value = "value3", required = true) String ctxType,
 			@RequestParam(value = "value4", required = true) String priority,
 			@RequestParam(value = "value5", required = true) String value,
 			@RequestParam(value = "value6", required = true) String edit) {
 		logger.debug("*** Edit: {}", edit);
 		if (edit.equals("delete")) {
 			logger.debug("*** Delete");
 			Rule ruleTodelete = null;
 			for (Rule rulesTemp : getEngine().getRules()) {
 				if(rulesTemp.getName().equalsIgnoreCase(ruleName)) {
 					ruleTodelete = rulesTemp;
 				}
 			}
 			getEngine().deleteRule(ruleTodelete);
 		}
 		else {
 			logger.debug("*** Insert");
 			Rule newRule = new Rule(ruleName, operator, ctxAttr, value, Integer.parseInt(priority), 0, ctxType);
 			getEngine().insertRule(newRule);
 
 			return (ruleName +" included");
 		}
 		return (ruleName +" deleted");
 	}
 
 
 	/**
 	 * @param class1
 	 * @return
 	 */
 	private List<String> getTypesList(Class<?> name)
 	{
 		logger.debug("Extracting parmas from: {}", name.getCanonicalName());
 		Field[] fields = name.getDeclaredFields();
 		List<String> results = new ArrayList<String>();
 		for (Field field : fields)
 		{
 			try
 			{
 				logger.info("add " + field.get(null));
 				String field_string = "" + field.get(null);
 				results.add(field_string);
 			} catch (IllegalArgumentException e) {
 				logger.error("Error casting to String: {}", e.getLocalizedMessage());
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				logger.error("Error casting to String: {}", e.getLocalizedMessage());
 				e.printStackTrace();
 			}
 
 			logger.info("add fields " + field.getName());
 		}
 
 		logger.debug(" Return {} elements", results.size());
 		return results;
 	}
 
 	public IContextSubscriber getCtxSubscriber(){
 		BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
 		ServiceReference<?> ref= bc.getServiceReference(IContextSubscriber.class.getName());
 		return (IContextSubscriber)bc.getService(ref);
 	}
 
 	public ICollabApps getCollabAppsConnectors(){
 		BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
 		ServiceReference<?> ref= bc.getServiceReference(ICollabApps.class.getName());
 		return (ICollabApps)bc.getService(ref);
 	}
 
 	public IEngine getEngine(){
 		BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
 		ServiceReference<?> ref= bc.getServiceReference(IEngine.class.getName());
 		return (IEngine)bc.getService(ref);
 	}
 
 	@RequestMapping(value = "/setcollabapps.html", method = RequestMethod.GET)
 	public @ResponseBody String setCollabAppsConnectors(@RequestParam String app, String server) {
 		AbstractCollabAppConnector[] collabAppsConnectors = getCollabAppsConnectors().getCollabAppConnectors();
 		for (AbstractCollabAppConnector apps : collabAppsConnectors){
 			if (apps.getAppName().equalsIgnoreCase(app)){
 				apps.setAppServerName(server);
 			}
 			//Restarting server
 			apps.setup();
 		}
 		return app+"change to server: "+server;
 	}
 
 	@RequestMapping(value="/notification.html",method = RequestMethod.GET)
 	public ModelAndView getNotification() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		Hashtable<String,List<String>> sessionHashtable = getCtxSubscriber().getSessions();
 		List<String[]> results = new ArrayList<String[]>();
 		Enumeration<String> enumKey = sessionHashtable.keys();
 
 		while(enumKey.hasMoreElements()) {
 			String sessionName = enumKey.nextElement();
 			String[] elements = {sessionName, sessionHashtable.get(sessionName).toString(), getCtxSubscriber().getSessionLanguage(sessionName)}; 
 			results.add(elements);
 		}
 
 		model.put("results", results);
 		model.put("log", readLogFile());
 
 		//TODO: Example with async application here!!***************************
 		//		this.app.setAppName("APP_NAME");
 		//		
 		//		List<String[]> resultsAsync = new ArrayList<String[]>();
 		//		for (Entry<String, String[]> entry : this.app.getHashTableResults().entrySet()) {
 		//			String[] elements = {"Location: "+entry.getKey()+ " people: "+Arrays.toString(entry.getValue())};
 		//			resultsAsync.add(elements);
 		//		}		
 		//
 		//		model.put("app_name", this.app.getAppName());
 		//		model.put("resultsAsync", resultsAsync);
 
 		//Until here*******************************
 
 		return new ModelAndView("notification", model) ;
 	}
 
 	private String readLogFile()  {
 		Reader fileReader = null;
 		try {
 			fileReader = new FileReader("databases/collabToolsLogFile.log");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		BufferedReader input = new BufferedReader(fileReader);
 		String line = null;
 		while (true) {
 			try {
 				if ((line = input.readLine()) != null) {
 					return line;
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				Thread.sleep(1000L);
 			} catch (InterruptedException x) {
 				Thread.currentThread().interrupt();
 			}
 
 			try {
 				input.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 }
