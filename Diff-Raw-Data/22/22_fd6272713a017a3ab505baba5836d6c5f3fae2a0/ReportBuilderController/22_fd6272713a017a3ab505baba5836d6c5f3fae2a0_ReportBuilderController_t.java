 package edu.northwestern.bioinformatics.studycalendar.web.reporting;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ReportRowDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import org.apache.log4j.Logger;
 
 /**
  * @author Yufang Wang
  * @author Jaron Sampson
  */
 
 @AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
 public class ReportBuilderController extends PscSimpleFormController {
 	private SiteDao siteDao;
 	private StudyDao studyDao;
 	private ParticipantDao participantDao;
 	private static final Logger log = Logger.getLogger(ReportBuilderController.class.getName());
 	private ReportRowDao reportRowDao;
 
     public ReportBuilderController() {
         setCommandClass(ReportBuilderCommand.class);
         setFormView("reporting/reportBuilder");
         setSuccessView("report");
     }
     
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         ControllerTools.registerDomainObjectEditor(binder, "sitesFilter", siteDao);
         ControllerTools.registerDomainObjectEditor(binder, "studiesFilter", studyDao);
         ControllerTools.registerDomainObjectEditor(binder, "participantsFilter", participantDao);
     }
 
     
     
     protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
         log.debug("referenceData"); 
         Map<String, Object> refdata = new HashMap<String, Object>();
         List<Site> sites = new ArrayList<Site>();
         sites = siteDao.getAll();
         
     	refdata.put("sites", sites);        
         
         return refdata;
     }
 
 
 	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
     	
     	ReportBuilderCommand reportCommand = (ReportBuilderCommand) oCommand;
     	
         Map<String, Object> model = new HashMap<String, Object>();
         String startDate = reportCommand.getStartDate();
         String endDate = reportCommand.getEndDate();
         List<Study> studies = reportCommand.getStudiesFilter();
         List<Site> sites = reportCommand.getSitesFilter();
         List<Participant> participants = reportCommand.getParticipantsFilter();
         
         if(startDate == null) {startDate = "";}
         if(endDate == null) {endDate = "";}
         if(sites == null) {sites = new ArrayList<Site>();}
         if(studies == null) {studies = new ArrayList<Study>();}
         if(participants == null) {participants = new ArrayList<Participant>();}
         
 		Collection reportRows = initializeBeanCollection(sites, studies, participants, startDate, endDate);
 		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportRows);
 		model.put("datasource", dataSource);
		String format = new String();
		if(reportCommand.getExcelFormat()) {
			format = "xls";
		} else {
			format = "pdf";
		}
        model.put("format", format);
     	  	
         return new ModelAndView(getSuccessView(), model);
     }
 
 	private Collection initializeBeanCollection(List<Site> sites, List<Study> studies, List<Participant> participants, String startDate, String endDate) {	
 		List<ReportRow> reportRows = reportRowDao.getFilteredReport(sites, studies, participants, startDate, endDate);
 		
 		return reportRows;
 	}
 	
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         //log.debug("formBackingObject");
     	ReportBuilderCommand command = new ReportBuilderCommand();
         return command;
     }
 
 
     ////// CONFIGURATION    
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     @Required
 	public void setReportRowDao(ReportRowDao reportRowDao) {
 		this.reportRowDao = reportRowDao;
 	}
 
     @Required
 	public void setParticipantDao(ParticipantDao participantDao) {
 		this.participantDao = participantDao;
 	}
 
     @Required
 	public void setStudyDao(StudyDao studyDao) {
 		this.studyDao = studyDao;
 	}
 
 }
