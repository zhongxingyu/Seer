 package gov.nih.nci.eagle.web.reports;
 
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelResultEntry;
 import gov.nih.nci.caintegrator.application.configuration.SpringContext;
 import gov.nih.nci.caintegrator.domain.annotation.gene.bean.GeneBiomarker;
 import gov.nih.nci.caintegrator.service.findings.GeneralizedLinearModelFinding;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 import gov.nih.nci.eagle.util.FieldBasedComparator;
 import gov.nih.nci.eagle.util.PatientGroupManager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletResponse;
 
 public class GLMReport extends BaseClassComparisonReport {
 
 
 
     @PostConstruct
     protected void init() {
 
         TaskResult result = (TaskResult)findingsManager.getTaskResult(task);
         this.taskResult = (GeneralizedLinearModelFinding)result;
         sortAscending = true;
         sortComparator = new FieldBasedComparator("pvalues[0]", sortAscending);
         sortedBy = "groupName_0";
         reportBeans = new ArrayList<GLMReportBean>();
 
         for (GeneralizedLinearModelResultEntry entry : ((GeneralizedLinearModelFinding)result).getResultEntries()) {
             GLMReportBean bean = new GLMReportBean(entry, ((GeneBiomarker)((GeneralizedLinearModelFinding)result)
                     .getReporterAnnotationsMap().get(entry.getReporterId()))
                     .getHugoGeneSymbol());
             reportBeans.add(bean);
         }
         
         patientInfoMap = new HashMap<String, List>();
         for(String groupName : (List<String>)getBaselineGroups()) {
             List patients = getQueryDTO().getBaselineGroupMap().get(groupName);
             List patientInfo = patientManager.getPatientInfo(patients);
             patientInfoMap.put(groupName, patientInfo);
         }
         for(String groupName : (List<String>)getComparisonGroups()) {
             List patients = getQueryDTO().getComparisonGroupsMap().get(groupName);
             List patientInfo = patientManager.getPatientInfo(patients);
             patientInfoMap.put(groupName, patientInfo);
         }
     }
 
 
 
     public int getNumberGroups() {
     	return (((GeneralizedLinearModelFinding)taskResult).getGroupNames() != null ? ((GeneralizedLinearModelFinding)taskResult).getGroupNames().size() : 0);
     }
 
     public Collection getGroupNames() {
     	List<String> gnames = ((GeneralizedLinearModelFinding)taskResult).getGroupNames();
     	List<String> newgnames = new ArrayList<String>();
     	for(String s : gnames)	{
     		newgnames.add(s.replace("_afterAdjustment", " (after adjustment)").replaceAll("_beforeAdjustment", " (before adjustment)").replace("afterAdjustmentPvalue", "After Adjustment Pvalue").replace("beforeAdjustmentPvalue", "Before Adjustment Pvalue"));
     	}
     	return newgnames;
     }
 
     public void sortDataList(ActionEvent event) {
         String sortFieldAttribute = getAttribute(event, "sortField");
 
         //js doesnt like the [] notation, so had to use another Att with an underscore
         sortedBy = getAttribute(event, "sortedBy") != null ? getAttribute(event, "sortedBy") : sortFieldAttribute.replace("[", "_").replace("]", "");
      
         // Get and set sort field and sort order.
         // Get and set sort field and sort order.
         if (sortFieldAttribute != null
                 && sortFieldAttribute.equals(sortComparator.getField())) {
             sortAscending = !sortAscending;
         } else {
             sortAscending = true;
         }
         if (sortFieldAttribute != null) {
             sortComparator = new FieldBasedComparator(sortFieldAttribute,
                     sortAscending);
         }
 
     }
     
     public void generateCSV(ActionEvent event)	{
     	Collection reportBeans = this.getReportBeans();
     	List<List> csv = new ArrayList<List>();
     	
    		for(GLMReportBean rb : (List<GLMReportBean>)reportBeans){
    			if(csv.size()==0){
    				List headers = rb.getRowLabels();
    				//since the columns are dynamic based on the # of groups, we need to overwrite some of the header with
    				//values from the Report, as the ReportBean doesnt have this info
    				for(int i=0; i<this.getGroupNames().size(); i++){
    					
    					try {
 						headers.set(i+1, this.getGroupNames().toArray()[i].toString() + ""); 
 						//overwrite, with an offset of 2
 					} catch (RuntimeException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
    				
    				}
    				csv.add(headers);
    			}
    			csv.add(rb.getRow());
 		}
 		
         FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
 		try {
 			CSVUtil.renderCSV(response, csv);
 		} catch (Exception e) {
 			// TODO: handle exception
 		} finally	{
 			FacesContext.getCurrentInstance().responseComplete();
 		}
 
     }
 }
