 package org.openmrs.module.conceptmanagementapps.page.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptSource;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.page.FileDownload;
 import org.openmrs.ui.framework.page.PageModel;
 
 public class DownloadSpreadsheetPageController {
 	
 	private Log log = LogFactory.getLog(this.getClass());
 	
 	public FileDownload post(UiSessionContext sessionContext, HttpServletRequest request, UiUtils ui, PageModel model,
 	                         HttpServletResponse response) {
 		String classes = (String) request.getParameter("classes");
 		String sourceId = (String) request.getParameter("sourceId");
 		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
 		        .getService(ConceptManagementAppsService.class);
 		List<Concept> conceptList = conceptManagementAppsService.getUnmappedConcepts(sourceId, classes);
 		List<ConceptSource> sourceList = Context.getConceptService().getAllConceptSources();
 		List<ConceptClass> classList = Context.getConceptService().getAllConceptClasses();
 		model.addAttribute("sourceList", sourceList);
 		model.addAttribute("classList", classList);
 		return writeToFile(conceptList);
 		
 	}
 	
 	public void get(UiSessionContext sessionContext, PageModel model, HttpServletResponse response) throws Exception {
 		List<ConceptSource> sourceList = Context.getConceptService().getAllConceptSources();
 		List<ConceptClass> classList = Context.getConceptService().getAllConceptClasses();
 		model.addAttribute("sourceList", sourceList);
 		model.addAttribute("classList", classList);
 		
 	}
 	
 	private FileDownload writeToFile(List<Concept> conceptList) {
 		
 		Locale locale = Context.getLocale();
 		String delimiter = ",";
 		String description, name;
 		String line = "" + "map type" + delimiter + "source name" + delimiter + "source code" + delimiter + "concept Id"
		        + delimiter + "concept uuid" + delimiter + "preferred name" + delimiter + "description" + delimiter + "class"
 		        + delimiter + "datatype" + delimiter + "all existing mappings" + "\n";
 		
 		for (Concept concept : conceptList) {
 			line += " " + delimiter + " " + delimiter + " " + delimiter
 			
 			+ concept.getConceptId() + delimiter + concept.getUuid() + delimiter;
 			ConceptName cn = concept.getName(locale);
 			if (cn == null)
 				name = "";
 			else
 				name = cn.getName();
 			
 			ConceptDescription cd = concept.getDescription(locale);
 			if (cd == null)
 				description = "";
 			else
 				description = cd.getDescription();
 			
 			line += '"' + name.replace("\"", "\"\"") + "\",";
 			
 			if (description == null)
 				description = "";
 			line = line + '"' + description.replace("\"", "\"\"") + "\",";
 			line += '"';
 			if (concept.getConceptClass() != null)
 				line += concept.getConceptClass().getName();
 			line += "\",";
 			
 			line += '"';
 			if (concept.getDatatype() != null)
 				line += concept.getDatatype().getName();
 			line += "\",";
 			
 			String tmp = "";
 			for (ConceptMap cm : concept.getConceptMappings()) {
 				if (cm.getConceptMapType() != null) {
 					name = cm.getConceptMapType().getName();
 					tmp += name.trim().replace("\"", "\"\"");
 					tmp += " ";
 					
 				}
 				if (cm.getConceptReferenceTerm() != null) {
 					name = cm.getConceptReferenceTerm().getConceptSource().getName().toString();
 					tmp += name.trim().replace("\"", "\"\"") + "\n";
 					
 				}
 			}
 			if (tmp.length() > 0 && tmp.charAt(tmp.length() - 1) == '\n') {
 				tmp = tmp.substring(0, tmp.length() - 1);
 			}
 			
 			line += '"' + tmp + "\"\n";
 			
 			tmp = "";
 			
 		}
 		String s = new SimpleDateFormat("dMy_Hm").format(new Date());
 		
 		String contentType = "text/csv;charset=UTF-8";
 		String filename = "conceptsMissingMappings" + s + ".csv";
 		FileDownload missingMappingsFile = new FileDownload(filename, contentType, line.getBytes());
 		return missingMappingsFile;
 	}
 	
 }
