 package org.openmrs.module.htmlformflowsheet.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openmrs.Patient;
 import org.openmrs.api.APIAuthenticationException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
 import org.openmrs.module.htmlformflowsheet.PatientChartConfiguration;
 import org.openmrs.module.htmlformflowsheet.web.utils.HtmlFormFlowsheetWebUtils;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 /**
  * Controller for Patient Charts. Should be configured by passing it a PatientChartConfiguration bean
  * This is the root controller for the htmlformflowsheet page
  */
 public class PatientChartController implements Controller {
 	
 	private String formView = "/module/htmlformflowsheet/patientChart";
 	private PatientChartConfiguration configuration;
 	
 	
 	@SuppressWarnings("unchecked")
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		ModelMap model = new ModelMap();
 		if (Context.isAuthenticated()){
 			
 		    if (configuration == null) {
 				throw new RuntimeException("You need to provide a configuration");
 			}
 			
 			Integer selectTab = 0;
 			try {
 			    selectTab = Integer.valueOf(request.getParameter("selectTab"));
 			} catch (Exception ex){}
 			
 			
 			
 			if (configuration.getTabs() == null || configuration.getTabs().size() == 0){
 			    try{
 			        HtmlFormFlowsheetWebUtils.configureTabsAndLinks();
 			    } catch (Exception ex){
 			        ex.printStackTrace();
 		            throw new RuntimeException("there was an error configuring the default tabs and links.  Please verify the values in the global property htmlformflowsheet.configuration.");
 		        }
 			}
 	
 			
 			String fullPage = "true";
 			String title = "";
 			String readOnly = "false";
 			String showAllEncsWithEncType = "false";
 			String showHtmlFormInstead = "false";
 			
 			String configParam = request.getParameter("configuration");
 			String linksParam = request.getParameter("links");
 			String fullPageStr = request.getParameter("fullPage");
 			String readOnlyStr = request.getParameter("readOnly");
 			String windowHeight = request.getParameter("windowHeight");
 			String showProvider = request.getParameter("showProvider");
 			String showAllEncsWithEncTypeStr = request.getParameter("showAllEncsWithEncType");
 			String showHtmlFormInsteadStr = request.getParameter("showHtmlForm");
 			String titleStr = request.getParameter("title");
 			if (showHtmlFormInsteadStr != null && showHtmlFormInsteadStr.equals("true"))
 			    showHtmlFormInstead = "true";	
 			if (fullPageStr != null && fullPageStr.equals("false"))
 			    fullPage = "false";
 			if (readOnlyStr != null && readOnlyStr.equals("true"))
 			    readOnly = "true";
 			if (showAllEncsWithEncTypeStr != null && showAllEncsWithEncTypeStr.equals("true"))
 			    showAllEncsWithEncType = "true";
 			if (titleStr != null)
 				title = titleStr;
 			if (configParam != null && !configParam.equals("")){
 			    //here's the url-override of the configuration
 			    PatientChartConfiguration  pcc = HtmlFormFlowsheetWebUtils.buildGenericConfigurationFromStrings(configParam, linksParam);
 			    model.put("tabs", pcc.getTabs());
 	            model.put("links", pcc.getLinks());
 			} else {
 			    //here's the default config that lives in the controller, and is directly configurable with global properties
 			    model.put("tabs", configuration.getTabs());
 		        model.put("links", configuration.getLinks());
 			}
 			model.put("fullPage", fullPage);
 		    model.put("selectTab", selectTab);
 		    model.put("showAllEncsWithEncType", showAllEncsWithEncType);
 		    model.put("showHtmlFormInstead", showHtmlFormInstead);
 		    model.put("title", title);
 		    String addAnotherButtonLabel = (String) request.getParameter("addAnotherButtonLabel");
 		    if (addAnotherButtonLabel != null && !addAnotherButtonLabel.equals("") && !addAnotherButtonLabel.equals("null")){
 		       model.put("addAnotherButtonLabel", addAnotherButtonLabel);
 		    }    
 		    
		    if("true".equals(showProvider))
 		    {
 		    	model.put("showProvider", true);
 		    	model.put("providerHeader", request.getParameter("providerHeader"));
 		    }
 		    else {
 		    	model.put("showProvider", false);
 		    }
 		    //redirect to a dummy html if no patientId for edit htmlform page
 			Integer patientId = null;
 			Patient patient = null;
 	        try {
 	            patientId = Integer.valueOf(request.getParameter("patientId"));
 	            patient = Context.getPatientService().getPatient(patientId);
 	        } catch (Exception ex){ 
 	            patientId = 0;
 	            patient = new Patient();
 	            readOnly = "true";
 	        }
 	
 	        model.put("readOnly", readOnly);
 			model.put("patientId", patientId);
 			model.put("patient", patient);
 			if (windowHeight == null || windowHeight.equals(""))
 			    windowHeight = "400";  //default window size
 			model.put("windowHeight", windowHeight);
 		} else {
 			throw new APIAuthenticationException("You are not logged in.");
 		}
 		return new ModelAndView(formView, "model", model); 
 	}
 	
 	public void setConfiguration(PatientChartConfiguration configuration) {
 		this.configuration = configuration;
 	}
 	
 	public PatientChartConfiguration getConfiguration() {
 		return configuration;
 	}
 	
     public String getFormView() {
     	return formView;
     }
 	
     public void setFormView(String formView) {
     	this.formView = formView;
     }
 
 }
