 package gov.nih.nci.eagle.web.reports;
 
 import gov.nih.nci.caintegrator.application.lists.ListType;
 import gov.nih.nci.caintegrator.application.lists.ajax.CommonListFunctions;
 import gov.nih.nci.caintegrator.domain.study.bean.StudyParticipant;
 import gov.nih.nci.caintegrator.dto.query.QueryDTO;
 import gov.nih.nci.caintegrator.service.task.TaskResult;
 import gov.nih.nci.eagle.enumeration.SpecimenType;
 import gov.nih.nci.eagle.service.validation.ListValidationService;
 import gov.nih.nci.eagle.util.EAGLEListValidator;
 import gov.nih.nci.eagle.util.FieldBasedComparator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.component.UIData;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 public class EpiReport extends SortableReport{
     
     private QueryDTO queryDTO;
     private String listName;
     private static Logger logger = Logger.getLogger(EpiReport.class);
     
     private UIData reportUIData;
     private List allReportBeans; //hold the original, unfiltered list
     private String currentFilter;
     private ListValidationService listValidationService;
     
     public EpiReport() {
         
     }
     
     @PostConstruct
     protected void init() {
        sortedBy = "patientId";
        sortAscending = true;
        currentFilter = "all";
        
        if(task != null && reportBeans == null) {
            taskResult = (TaskResult)findingsManager.getTaskResult(task);
            this.queryDTO = task.getQueryDTO();
             reportBeans = new ArrayList();
             for(StudyParticipant p : (Collection<StudyParticipant>)taskResult.getTaskResults()) {
                 EpiReportBean erb = new EpiReportBean(p);
                 //set the available specimens
                 List availableSpecimens = new ArrayList<SpecimenType>();
                 //TODO: pull this out into a utlilty, as we do a similar thing in dynamiclisthelper
                 for(SpecimenType s: SpecimenType.values() )	{
                 	List justone = new ArrayList();
         			justone.add(p.getStudySubjectIdentifier());
         			List shouldbeone = listValidationService.validateList(justone, s);
         			if(shouldbeone.size()==1){
         				//hit
         				availableSpecimens.add(s);
         			}
                 }
                 erb.setAvailableSpecimens(availableSpecimens);
                 reportBeans.add(erb);
             }
             if(allReportBeans == null || allReportBeans.size()==0){
             	allReportBeans = new ArrayList();
             	allReportBeans.addAll(reportBeans);
             }
         }
         sortComparator = new FieldBasedComparator(sortedBy, sortAscending);
     }
     
     public void setPatientData(Collection patientData) {
         reportBeans = new ArrayList();
        allReportBeans = new ArrayList();
         if(patientData != null) {
             for(StudyParticipant p : (Collection<StudyParticipant>)patientData) {
                EpiReportBean erb = new EpiReportBean(p);
                List availableSpecimens = new ArrayList<SpecimenType>();
                //TODO: pull this out into a utlilty, as we do a similar thing in dynamiclisthelper
                for(SpecimenType s: SpecimenType.values() ) {
                    List justone = new ArrayList();
                    justone.add(p.getStudySubjectIdentifier());
                    List shouldbeone = listValidationService.validateList(justone, s);
                    if(shouldbeone.size()==1){
                        //hit
                        availableSpecimens.add(s);
                    }

                }
                erb.setAvailableSpecimens(availableSpecimens);
                reportBeans.add(erb);
            }
            if(allReportBeans == null || allReportBeans.size()==0){
                allReportBeans = new ArrayList();
                allReportBeans.addAll(reportBeans);
             }
         }
     }
     
     public Integer saveList(String name) {
         listName = name;
         logger.debug("Saving list: " + listName);
         HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         HttpServletResponse rp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
         List<String> patients = new ArrayList<String>();
         Integer patientsSaved = 0;
         for(EpiReportBean bean : (Collection<EpiReportBean>)reportBeans) {
             if(bean.getSelected()) {
                 logger.debug("Patient is selected: " + bean.getPatientId());
                 patients.add(bean.getPatientId());
                 patientsSaved++;
             }
         }
         EAGLEListValidator validator = null;
         try {
             validator = new EAGLEListValidator(ListType.PatientDID,patients);
         } catch (OperationNotSupportedException e) {
             logger.error("problem creating list", e);
         }
 
         String status = CommonListFunctions.createGenericListWithSession(ListType.PatientDID,null,patients,listName,validator,session);
         logger.debug("Returned status: " + status);
         return patientsSaved;
     }
 
     public QueryDTO getQueryDTO() {
         return queryDTO;
     }
 
     public void setQueryDTO(QueryDTO queryDTO) {
         this.queryDTO = queryDTO;
     }
     public void selectPatient(String patientId, boolean selected) {
         logger.debug("Selecting patient: " + patientId );
         for(EpiReportBean bean : (Collection<EpiReportBean>)reportBeans) {
             if(bean.getPatientId().equals(patientId))
                 bean.setSelected(selected);
         }
     }
     
     public void applyFilter(ActionEvent e){
     	 String f = getAttribute(e, "specimen");
     	 this.setCurrentFilter(f);
     	//back to page 1
     	reportUIData.setFirst(0);
     	//reset
     	if(f.equals("all"))	{
     		//clear them and start again
     		reportBeans.clear();
     		reportBeans.addAll(allReportBeans);
     		return;
     	}
     	SpecimenType sp = SpecimenType.valueOf(f);
     	//filter off all the ones that dont have this in availableSpecimens
     	List filteredBeans = new ArrayList();
     	for(EpiReportBean bean : (Collection<EpiReportBean>) allReportBeans){
     		if(bean.getAvailableSpecimens().contains(sp))	{
     			//show this one
     			filteredBeans.add(bean);
     		}
     		else	{
     			 
     		}
     	}
    		reportBeans.clear();
 		reportBeans.addAll(filteredBeans);
 
     }
     
     public void generateCSV(ActionEvent event)  {
         List reportBeans = this.getReportBeans();
         List<List> csv = new ArrayList<List>();
         
         for(ReportBean ccrb : (List<ReportBean>)reportBeans){
             if(csv.size()==0){
                 csv.add(ccrb.getRowLabels());
             }
             csv.add(ccrb.getRow());
         }
         
         FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
         try {
             CSVUtil.renderCSV(response, csv);
         } catch (Exception e) {
             // TODO: handle exception
         } finally   {
             FacesContext.getCurrentInstance().responseComplete();
         }
     }    
     
     public void toggleAllPatients(String toggle)	{
     	boolean selected = toggle.equals("false") ? false : true; //in case something else gets passed
     	for(EpiReportBean bean : (Collection<EpiReportBean>) reportBeans){
     		selectPatient(bean.getPatientId(), selected);
     	}
     }
     
     public String getListName() {
         return listName;
     }
 
     public void setListName(String listName) {
         this.listName = listName;
     }
 
 	public ListValidationService getListValidationService() {
 		return listValidationService;
 	}
 
 	public void setListValidationService(ListValidationService listValidationService) {
 		this.listValidationService = listValidationService;
 	}
 
 	public List getAllReportBeans() {
 		return allReportBeans;
 	}
 
 	public void setAllReportBeans(List allReportBeans) {
 		this.allReportBeans = allReportBeans;
 	}
 
 	public UIData getReportUIData() {
 		return reportUIData;
 	}
 
 	public void setReportUIData(UIData reportUIData) {
 		this.reportUIData = reportUIData;
 	}
 
 	public String getCurrentFilter() {
 		return currentFilter;
 	}
 
 	public void setCurrentFilter(String currentFilter) {
 		this.currentFilter = currentFilter;
 	}
     
     
 
 }
