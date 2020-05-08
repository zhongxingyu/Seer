 package com.mpower.controller;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.security.context.SecurityContext;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.userdetails.UserDetailsService;
 import org.springframework.util.Assert;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ObjectError;
 import org.springframework.validation.Validator;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractWizardFormController;
 
 import ar.com.fdvs.dj.core.DynamicJasperHelper;
 import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
 import ar.com.fdvs.dj.domain.DynamicReport;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.mpower.controller.validator.ReportSaveValidator;
 import com.mpower.controller.validator.ReportWizardValidator;
 import com.mpower.domain.GuruSessionData;
 import com.mpower.domain.ReportChartSettings;
 import com.mpower.domain.ReportCrossTabColumn;
 import com.mpower.domain.ReportCrossTabMeasure;
 import com.mpower.domain.ReportCrossTabRow;
 import com.mpower.domain.ReportCustomFilterCriteria;
 import com.mpower.domain.ReportCustomFilterDefinition;
 import com.mpower.domain.ReportDataSource;
 import com.mpower.domain.ReportDataSubSourceGroup;
 import com.mpower.domain.ReportDataSubSource;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportFieldGroup;
 import com.mpower.domain.ReportFilter;
 import com.mpower.domain.ReportSegmentationType;
 import com.mpower.domain.ReportSelectedField;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.JasperServerService;
 import com.mpower.service.ReportCustomFilterDefinitionService;
 import com.mpower.service.ReportSegmentationResultsService;
 import com.mpower.service.ReportSegmentationTypeService;
 import com.mpower.service.ReportFieldGroupService;
 import com.mpower.service.ReportFieldService;
 import com.mpower.service.ReportSourceService;
 import com.mpower.service.ReportSubSourceGroupService;
 import com.mpower.service.ReportSubSourceService;
 import com.mpower.service.ReportWizardService;
 import com.mpower.service.SessionService;
 import com.mpower.util.ModifyReportJRXML;
 import com.mpower.util.ReportCustomFilterHelper;
 import com.mpower.util.ReportGenerator;
 import com.mpower.util.ReportQueryGenerator;
 import com.mpower.util.ReportWizardHelper;
 import com.mpower.util.SessionHelper;
 import com.mpower.view.DynamicReportView;
 
 public class ReportWizardFormController extends AbstractWizardFormController {
 	private ReportSubSourceGroupService  reportSubSourceGroupService;
 	private ReportSubSourceService  reportSubSourceService;
 
 	/** Logger for this class and subclasses */
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private DynamicReportView       dynamicView;
 	private ReportSourceService     reportSourceService;
 	private ReportWizard            wiz;
 	private ReportWizardService     reportWizardService;
 	private JasperServerService     jasperServerService;
 	private ReportFieldGroupService reportFieldGroupService;
 
 	private ReportFieldService      reportFieldService;
 	private ReportCustomFilterDefinitionService      reportCustomFilterDefinitionService;
 	private ReportCustomFilterHelper reportCustomFilterHelper;
 
 	private ReportSegmentationTypeService      reportSegmentationTypeService;
 	private ReportSegmentationResultsService reportSegmentationResultsService;
 	private SessionService          sessionService;
 	private DataSource jdbcDataSource;
 	private String reportUnitDataSourceURI;
 	private UserDetailsService userDetailsService;
 
 	public ReportWizardFormController() {
 
 	}
 
 	@Override
 	protected Object formBackingObject(HttpServletRequest request)
 	throws ServletException {
 		logger.info("**** in formBackingObject");
 		WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
 		wiz = (ReportWizard)applicationContext.getBean("ReportWizard");
 		wiz.setDataSources(reportSourceService.readSources());
 		SessionHelper.tl_data.get().removeAttribute("GURUSESSIONDATA");
 
 		logger.info("Count " + wiz.getDataSources().size());
 		return wiz;
 	}
 
 	public DynamicReportView getDynamicView() {
 		return dynamicView;
 	}
 
 	public DataSource getJdbcDataSource() {
 		return jdbcDataSource;
 	}
 
 	public ReportFieldGroupService getReportFieldGroupService() {
 		return reportFieldGroupService;
 	}
 
 	public ReportFieldService getReportFieldService() {
 		return reportFieldService;
 	}
 
 	public ReportSourceService getReportSourceService() {
 		return reportSourceService;
 	}
 
 	public ReportSubSourceGroupService getReportSubSourceGroupService() {
 		return reportSubSourceGroupService;
 	}
 
 	public ReportSubSourceService getReportSubSourceService() {
 		return reportSubSourceService;
 	}
 	public String getReportUnitDataSourceURI() {
 		return reportUnitDataSourceURI;
 	}
 	public ReportWizardService getReportWizardService() {
 		return reportWizardService;
 	}
 
 	public SessionService getSessionService() {
 		return sessionService;
 	}
 
 	// returns the last page as the success view
 	private String getSuccessView() {
 		return getPages()[getPages().length - 1];
 	}
 
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		Assert.notNull(request, "Request must not be null");
 
 
 		return super.handleRequest(request, response);
 	}
 
 	@Override
 	 protected void validatePage(Object command, Errors errors, int page) {
 	        Validator[] validators = getValidators();
 	        for (int i=0; i<validators.length; i++) {
 	            Validator validator = validators[i];
 	            if (validator instanceof ReportWizardValidator) {
 	            	if (((ReportWizardValidator)validator).getPage() == page) {
 	                    validator.validate(command, errors);
 	                }
 	            }
 	        }
 	    }
 
 	@Override
 	protected void postProcessPage(HttpServletRequest request, Object command,
 			Errors errors, int page) throws Exception {
 		logger.info("**** in onSubmit()");
 		Map<String, Object> params = new HashMap<String, Object>();
 		ReportWizard wiz = (ReportWizard) command;
 
 		Assert.notNull(request, "Request must not be null");
 		if ((wiz.getSubSourceId() != wiz.getPreviousDataSubSourceId() || wiz.getDataSource() == null) && wiz.getSubSourceId() != 0)
 		{
 			wiz.setPreviousDataSubSourceId(wiz.getSubSourceId());
 			wiz.setPreviousDataSubSourceGroupId(wiz.getDataSubSourceGroupId());
 
 			LoadWizardLookupTables(wiz);
 
 			// clear out any selected filters, chart settings, etc.
 			wiz.setReportSegmentationTypeId(0);
 			wiz.setUseReportAsSegmentation(false);
 			wiz.setSegmentationQuery("");
 			wiz.getReportFilters().clear();
 			wiz.getReportChartSettings().clear();
 			wiz.getReportCrossTabFields().getReportCrossTabColumns().clear();
 			wiz.getReportCrossTabFields().getReportCrossTabRows().clear();
 			wiz.getReportCrossTabFields().getReportCrossTabMeasure().clear();
 
 			// once the data source and sub-source have been selected, select the default fields
 			wiz.populateDefaultReportFields();
 		}
 
 		if (request.getParameter("_target5") != null || request.getParameter("_target5.x") != null) {
 			//
 			// We are saving this report to jasperserver
 			try {
 				ReportSaveValidator rsv = new ReportSaveValidator();
 				rsv.validate(wiz, errors);
 				Boolean saveValidationSuccess = true;
 				Iterator itErrors = errors.getAllErrors().iterator();
 				while (itErrors.hasNext()) {
 					ObjectError error = (ObjectError)itErrors.next();
 					if (error.getCode().contains("error.code")) {
 						saveValidationSuccess = false;
 						break;
 					}
 				}
 				if (saveValidationSuccess)
 					saveReport(wiz);
 			} catch (Exception e) {
 				logger.error(e.getLocalizedMessage());
 				errors.reject(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	@Override
 	protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage) {
 		//
 		// if we are saving a report then redirect the user back to where they hit saveas from
 		if (request.getParameter("_target5") != null || request.getParameter("_target5.x") != null ) {
 			Boolean returnToSavePage = false;
 			Iterator itErrors = errors.getAllErrors().iterator();
 			while (itErrors.hasNext()) {
 				ObjectError error = (ObjectError)itErrors.next();
 				if (error.getCode().contains("error.code")) {
 					returnToSavePage = true;
 					break;
 				}
 			}
 
 			ReportWizard wiz = (ReportWizard) command;
 			if (returnToSavePage)
 				return currentPage;
 			else if (wiz.getExecuteSegmentation())
 			{
 				int pageIndex = getPageIndexByName("ReportExecuteSegmentation");
 				if (pageIndex != -1)
 					return pageIndex;
 				else
 					return wiz.getPreviousPage();
 			}
 			else
 			    return wiz.getPreviousPage();
 		}
 
 		return super.getTargetPage(request, command, errors, currentPage);
 	}
 
 	private int getPageIndexByName(String pageName) {
 		int result = -1;
 		String[] pages = getPages();
 		for (int index = 0; index < pages.length; index++)
 		{
 			if (pages[index].equalsIgnoreCase(pageName))
 			{
 				result = index;
 				break;
 			}
 		}
 		return result;
 	}
 
 	@Override
 	protected ModelAndView processFinish(HttpServletRequest request,
 			HttpServletResponse arg1, Object arg2, BindException arg3)
 	throws Exception {
 
 
 		return new ModelAndView(getSuccessView(),"reportsouce",wiz);
 
 	}
 
 	private void LoadWizardLookupTables(ReportWizard wiz) {
 		wiz.setDataSource(reportSourceService.find(wiz.getSrcId()));
 		wiz.setDataSubSourceGroup(reportSubSourceGroupService.find(wiz.getDataSubSourceGroupId()));
 
 		ReportDataSubSource       rdss = reportSubSourceService.find( wiz.getSubSourceId());
 
 		List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 		wiz.setFieldGroups(lrfg);
 
 		wiz.setDataSubSource(rdss);
 		WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
 		wiz.getDataSubSource().setReportCustomFilterDefinitions(reportCustomFilterHelper.getReportCustomFilterDefinitions(applicationContext, wiz));
 		wiz.getDataSubSource().setReportSegmentationTypes(reportSegmentationTypeService.readReportSegmentationTypeBySubSourceId(rdss.getId()));
 
 		List<ReportField> fields = new LinkedList<ReportField>();
 		// Iterate across the field groups in the
 		Iterator itGroup = lrfg.iterator();
 		while (itGroup.hasNext()) {
 			ReportFieldGroup rfg = (ReportFieldGroup) itGroup.next();
 			fields.addAll(reportFieldService.readFieldByGroupId(rfg.getId()));
 		}
 		wiz.setFields(fields);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected Map referenceData(HttpServletRequest request, Object command,
 			Errors errors, int page) throws Exception {
 		ReportWizard wiz = (ReportWizard) command;
 		Map refData = new HashMap();
 
 		refData.put("page",page);
 		refData.put("maxpages", getPages().length-6); // 6 pages that are not actual steps
 
 		// see if we went backwards
 		if (wiz.getPreviousPage() > page)
 			wiz.setPreviousPage(page -1);
 
 		refData.put("previouspage", wiz.getPreviousPage());
 
 		// Load info passed to the wizard
 		// if no user or password is in the request, see if the user has already been
 		// populated on the wiz.  Going to the second page and then back to the first could cause that.
 		String userName = request.getParameter("username");
 		String password = request.getParameter("password");
 		if (userName == null || password == null) {
 			userName = wiz.getUsername();
 			password = wiz.getPassword();
 		} else {
 			GuruSessionData sessiondata = SessionHelper.getGuruSessionData();
 			sessiondata.setUsername(userName);
 			sessiondata.setPassword(password);
 			if (password.equals("/") || password.length() == 0) {
 				UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
 				password = userDetails.getPassword();
 			}
 			wiz.setUsername(userName);
 			wiz.setPassword(password);
 		}
 
 		String reportUri = request.getParameter("reporturi");
 		if (reportUri != null && reportUri.length() > 0) {
 			long reportId = -1;
 			try {
 				if (reportUri.contains("/THEGURU_"))
 					reportId = Long.parseLong(reportUri.substring(reportUri.lastIndexOf("/THEGURU_") + 9));
 				if (reportId != -1) {
 					ReportWizard tempWiz = reportWizardService.Find(reportId);
 					if (tempWiz != null) {
 						ReportWizardHelper.PopulateWizardFromSavedReport(wiz, tempWiz, page != 9);
 						wiz.setPreviousDataSubSourceGroupId(wiz.getDataSubSourceGroupId());
 						wiz.setPreviousDataSubSourceId(wiz.getSubSourceId());
 						wiz.setReportPath(reportUri.substring(0, reportUri.indexOf("/THEGURU_")));
 						LoadWizardLookupTables(wiz);
 					} else {
 						errors.reject("1", "Unable to load report " + reportUri + ".  You may continue to create a new report, or cancel to return to the report list.");
 					}
 				}
 			} catch (Exception exception) {
 				errors.reject("1", "Unable to load report " + reportUri + ".  You may continue to create a new report, or cancel to return to the report list.  " + exception.getMessage());
 			}
 		}
 
 		// If there is no user from the request or the wiz, send back to jasper server
 		if (userName == null || password == null) {
 			refData.put("userFound", false);
 		} else {
 			refData.put("userFound", true);
 			refData.put("subSourceGroupId", wiz.getDataSubSourceGroupId());
 			refData.put("subSourceId", wiz.getSubSourceId());
 			wiz.getReportGenerator().setReportUserName(userName);
 			wiz.getReportGenerator().setReportPassword(password);
 
 			jasperServerService.setUserName(userName);
 			jasperServerService.setPassword(password);
 			//we want to allow the users logged in as a company to also see the default templates as well
 			if (wiz.getReportTemplateList() != null)
 				wiz.getReportTemplateList().clear();
 
 			if (!(wiz.getCompany().compareToIgnoreCase("default") == 0))
 				wiz.setReportTemplateList(jasperServerService.list("/Reports/Default/templates"));
 			if (wiz.getReportTemplateList() != null)
 				wiz.getReportTemplateList().addAll(jasperServerService.list("/Reports/" + wiz.getCompany() + "/templates"));
 			else
 				wiz.setReportTemplateList(jasperServerService.list("/Reports/" + wiz.getCompany() + "/templates"));
 
 			if (wiz.getReportTemplateJRXML() == null || wiz.getReportTemplateJRXML().length() == 0) {
 				if (wiz.getReportTemplateList().size() > 0)
 					wiz.setReportTemplatePath(((ResourceDescriptor)wiz.getReportTemplateList().get(0)).getUriString());
 				else
 					errors.reject("Invalid Repository","Invalid Repository.  It appears your repository is not setup properly.  Please contact your system administrator.");
 			}
 		}
 
 		//
 		// Report Source
 		if (page == 0) {
 			wiz.getReportGenerator().resetInputControls();
 
 			refData.put("previousSubSourceGroupId", wiz.getPreviousDataSubSourceGroupId());
 			refData.put("previousSubSourceId", wiz.getPreviousDataSubSourceId());
 		}
 
 		//
 		// Report Format
 		if (page == 1) {
 			String reportType = wiz.getReportType();
 			if (reportType.compareToIgnoreCase("summary") == 0)
 				reportType = "tabular";
 			refData.put("reportType", reportType);
 		}
 
 		//
 		// Report content selection
 		if (page == 2) {
 			String reportType = wiz.getReportType();
 			refData.put("reportType", reportType);
 
 			Boolean reportUniqueRecords = wiz.getUniqueRecords();
 			refData.put("reportUniqueRecords", reportUniqueRecords);
 			wiz.setUniqueRecords(false);
 
 			ReportDataSubSource rdss = reportSubSourceService.find(wiz.getSubSourceId());
 			List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 			wiz.setFieldGroups(lrfg);
 
 			refData.put("fieldGroups", wiz.getFieldGroups());
 
 			if (reportType.compareTo("tabular") == 0 || reportType.compareTo("summary") == 0) {
 				List<ReportSelectedField> tempFields = new LinkedList<ReportSelectedField>();
 				tempFields.addAll(wiz.getReportSelectedFields());
 				refData.put("selectedFields", tempFields);
 				// Clear out the selected fields because some items do not post back correctly
 				wiz.getReportSelectedFields().clear();
 
 				refData.put("reportChartSettings", wiz.getReportChartSettings());
 			} else {
 				List<ReportCrossTabColumn> tempColumns = new LinkedList<ReportCrossTabColumn>();
 				tempColumns.addAll(wiz.getReportCrossTabFields().getReportCrossTabColumns());
 				refData.put("matrixColumns", tempColumns);
 				wiz.getReportCrossTabFields().getReportCrossTabColumns().clear();
 
 				List<ReportCrossTabRow> tempRows = new LinkedList<ReportCrossTabRow>();
 				tempRows.addAll(wiz.getReportCrossTabFields().getReportCrossTabRows());
 				refData.put("matrixRows", tempRows);
 				wiz.getReportCrossTabFields().getReportCrossTabRows().clear();
 
 				List<ReportCrossTabMeasure> tempMeasures = new LinkedList<ReportCrossTabMeasure>();
 				tempMeasures.addAll(wiz.getReportCrossTabFields().getReportCrossTabMeasure());
 				refData.put("matrixMeasures", tempMeasures);
 				wiz.getReportCrossTabFields().getReportCrossTabMeasure().clear();
 			}
 		}
 
 		// filter screen
 		if(page==3) {
 			ReportDataSubSource rdss = reportSubSourceService.find(wiz.getSubSourceId());
 			List<ReportFieldGroup>    lrfg = reportFieldGroupService.readFieldGroupBySubSourceId(rdss.getId());
 			wiz.setFieldGroups(lrfg);
 
 		    if (wiz.getShowSqlQuery()) {
 		    	ReportQueryGenerator reportQueryGenerator = new ReportQueryGenerator(wiz, reportFieldService, reportCustomFilterDefinitionService);
 		    	refData.put("showSqlQuery", wiz.getShowSqlQuery());
 				refData.put("sqlQuery", reportQueryGenerator.getQueryString());
 				wiz.setShowSqlQuery(false);
 		    }
 
 			refData.put("fieldGroups", lrfg);
 			WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
 		    refData.put("customFilters", reportCustomFilterHelper.getReportCustomFilterDefinitions(applicationContext, wiz));
 		    List<ReportSegmentationType> reportSegmentationTypes = rdss.getReportSegmentationTypes();
 		    refData.put("useReportAsSegmentation", wiz.getUseReportAsSegmentation());
 		    wiz.setUseReportAsSegmentation(false);
 		    refData.put("segmentationTypeId", wiz.getReportSegmentationTypeId());
 		    refData.put("segmentationTypeCount", reportSegmentationTypes.size());
 		    refData.put("segmentationTypes", reportSegmentationTypes);
 
 			List<ReportFilter> tempFilters = reportCustomFilterHelper.refreshReportCustomFilters(applicationContext, wiz);
 			refData.put("selectedFilters", tempFilters);
 			// Clear out the selected fields because some items do not post back correctly
 			wiz.getReportFilters().clear();
 
 		    refData.put("rowCount", wiz.getRowCount());
 		}
 
 		if (page==4) {
 			refData.put("executeSegmentation", wiz.getExecuteSegmentation());
 			wiz.setExecuteSegmentation(false);
 		    refData.put("useReportAsSegmentation", wiz.getUseReportAsSegmentation());
 		}
 
 		// run a saved report
 		if (page==7) {
 			//			wiz.setReportPath("/Reports/Clementine/" + wiz.getReportName().replace(" ", "_"));
 			refData.put("reportPath", wiz.getReportPath());
 		}
 
 		// running the report
 		if (page==8) {
 			// if there are no fields selected, select the defaults
 			if (wiz.getReportSelectedFields().size() == 0)
 				wiz.populateDefaultReportFields();
 
 //		 	 SuppressWarnings("unused")
 
 			DynamicReport dr = wiz.getReportGenerator().Generate(wiz, jdbcDataSource, reportFieldService, reportCustomFilterDefinitionService);
 			//String query = dr.getQuery().getText();
 
 			//
 			// execute the query and pass it to generateJasperPrint
 			//Connection connection = jdbcDataSource.getConnection();
 			//Statement statement = connection.createStatement();
 
 			File tempFile = TempFileUtil.createTempFile("wiz", ".jrxml");
 			logger.info("Temp File: " + tempFile);
 			DynamicJasperHelper.generateJRXML(dr,new ClassicLayoutManager(), wiz.getReportGenerator().getParams(), null, tempFile.getPath());
 
 		    //
 		    // modify the jrxml
 		    ModifyReportJRXML reportXMLModifier = new ModifyReportJRXML(wiz, reportFieldService);
 		    //in the upgrade to DJ 3.0.4 they add a scriptletclass that jasperserver is not aware of so we remove as we
 		    //are not using it right now
 		    reportXMLModifier.removeDJDefaultScriptlet(tempFile.getPath());
 
 		    // DJ adds a dataset that causes an error on the matrix reports so we need to remove it
 		    if (wiz.getReportType().compareToIgnoreCase("matrix") == 0)
 		    	reportXMLModifier.removeCrossTabDataSubset(tempFile.getPath());
 
 		    // add the summary info/totals to the report - DJ only allows one per column and we need to allow multiple so
 			// 		we are altering the XML directly to add the summary calculations to the jasper report,
 		    //		this also handles adding the calculations to the groups created by DJ.
 			if (wiz.getReportType().compareToIgnoreCase("matrix") != 0 && wiz.HasSummaryFields() == true){
 				reportXMLModifier.AddGroupSummaryInfo(tempFile.getPath());
 			    reportXMLModifier.AddReportSummaryInfo(tempFile.getPath());
 			}
 
 			//move the chart to the header or footer of the report
 			List<ReportChartSettings> rptChartSettings = wiz.getReportChartSettings();
 		    Iterator itRptChartSettings = rptChartSettings.iterator();
 		    while (itRptChartSettings.hasNext()){
 		    	ReportChartSettings rptChartSetting = (ReportChartSettings) itRptChartSettings.next();
 		    	String chartType = rptChartSetting.getChartType();
 		    	String chartLocation = rptChartSetting.getLocation();
 		    	reportXMLModifier.moveChartFromGroup(tempFile.getPath(), chartType, chartLocation);
 		    }
 
 
 			//
 			// save the report to the server
 		    wiz.getReportGenerator().put(ResourceDescriptor.TYPE_REPORTUNIT, tempFile.getName(), tempFile.getName(), tempFile.getName(), wiz.getTempFolderPath(), tempFile, wiz.getReportGenerator().getParams(), wiz.getDataSubSource().getJasperDatasourceName());
 
 			String tempReportPath = wiz.getTempFolderPath() + "/" + tempFile.getName();
 			refData.put("reportPath", tempReportPath);
 
 			tempFile.delete();
 		}
 
 		if (page == getPageIndexByName("ReportExecuteSegmentation") ||
 				page == getPageIndexByName("ReportSegmentationExecutionResults")) {
 			boolean hasErrors = false;
 
 			refData.put("wiz", wiz);
 
 			if (!wiz.getUseReportAsSegmentation()) {
 				hasErrors = true;
 				errors.reject("error.segmentationnotselected", "This report was not selected to be used as a segmentation.  To change this, go to the Report Criteria page and select the 'Use report as segmentation' check box.  However, if the selected " +
 						"secondary data source is not able to be used as a segmentation, that check box will not be available.");
 			}
 			if (wiz.getReportSegmentationTypeId() == 0) {
 				hasErrors = true;
 				errors.reject("error.segmentationtypenotselected", "No segmentation type was selected.");
 			}
 
 			if (!hasErrors) {
 				try {
 					if (page == getPageIndexByName("ReportExecuteSegmentation")) {
 						int rowsAffected = reportSegmentationResultsService.executeSegmentation(wiz.getId());
 						ReportWizard tempWiz = reportWizardService.Find(wiz.getId());
 						wiz.setLastRunDateTime(tempWiz.getLastRunDateTime());
 						wiz.setLastRunByUserName(tempWiz.getLastRunByUserName());
 						wiz.setResultCount(tempWiz.getResultCount());
 						wiz.setExecutionTime(tempWiz.getExecutionTime());
 					}
					refData.put("segmentationType", reportSegmentationTypeService.find(wiz.getReportSegmentationTypeId()).getSegmentationType());
 					refData.put("rowsAffected", wiz.getResultCount());
 					refData.put("executionTime", wiz.getExecutionTime());
 					refData.put("lastRunDate", wiz.getLastRunDateTime());
 					refData.put("lastRunBy", wiz.getLastRunByUserName());
 					refData.put("hasErrors", false);
 				} catch (Exception e) {
 					e.printStackTrace();
 					refData.put("hasErrors", true);
 					String message = "Error executing segmentation query for report ID " + wiz.getId().toString() + " (\"" + wiz.getReportName() + "\")" + System.getProperty("line.separator") + e.getMessage() + System.getProperty("line.separator") + wiz.getSegmentationQuery();
 					errors.reject("error.segmentationexecutionerror", message);
 				}
 			} else {
 				refData.put("hasErrors", true);
 			}
 		}
 		return refData;
 	}
 
 	protected void saveReport(ReportWizard wiz) throws Exception {
 		reportWizardService.save(wiz);
 
 		// If the report is to be used as a segmentation, generate the segmentation SQL, set it on the wiz and save again since the segmentation query will require the report wizard ID
 		if (wiz.getUseReportAsSegmentation()) {
 			ReportQueryGenerator reportQueryGenerator = new ReportQueryGenerator(wiz, reportFieldService, reportCustomFilterDefinitionService);
 			wiz.setSegmentationQuery(reportQueryGenerator.getSegmentationQueryString(reportSegmentationTypeService.find(wiz.getReportSegmentationTypeId()).getColumnName()));
 			reportWizardService.save(wiz);
 		}
 		//
 		// First we must generate a jrxml file
 		//
 
 		DynamicReport dr = wiz.getReportGenerator().Generate(wiz, jdbcDataSource, reportFieldService, reportCustomFilterDefinitionService);
 
 		File tempFile = File.createTempFile("wiz", ".jrxml");
 		logger.info("Temp File: " + tempFile);
 		DynamicJasperHelper.generateJRXML(dr,new ClassicLayoutManager(), wiz.getReportGenerator().getParams(), null, tempFile.getPath());
 
 		//
 	    // modify the jrxml
 	    ModifyReportJRXML reportXMLModifier = new ModifyReportJRXML(wiz, reportFieldService);
 	    //in the upgrade to DJ 3.0.4 they add a scriptletclass that jasperserver is not aware of so we remove as we
 	    //are not using it right now
 	    reportXMLModifier.removeDJDefaultScriptlet(tempFile.getPath());
 
 	    // DJ adds a dataset that causes an error on the matrix reports so we need to remove it
 	    if (wiz.getReportType().compareToIgnoreCase("matrix") == 0)
 	    	reportXMLModifier.removeCrossTabDataSubset(tempFile.getPath());
 
 	    // add the summary info/totals to the report - DJ only allows one per column and we need to allow multiple so
 		// 		we are altering the XML directly to add the summary calculations to the jasper report,
 	    //		this also handles adding the calculations to the groups created by DJ.
 		if (wiz.getReportType().compareToIgnoreCase("matrix") != 0 && wiz.HasSummaryFields() == true){
 			reportXMLModifier.AddGroupSummaryInfo(tempFile.getPath());
 		    reportXMLModifier.AddReportSummaryInfo(tempFile.getPath());
 		   }
 
 		//move the chart to the header or footer of the report
 		List<ReportChartSettings> rptChartSettings = wiz.getReportChartSettings();
 	    Iterator itRptChartSettings = rptChartSettings.iterator();
 	    while (itRptChartSettings.hasNext()){
 	    	ReportChartSettings rptChartSetting = (ReportChartSettings) itRptChartSettings.next();
 	    	String chartType = rptChartSetting.getChartType();
 	    	String chartLocation = rptChartSetting.getLocation();
 	    	reportXMLModifier.moveChartFromGroup(tempFile.getPath(), chartType, chartLocation);
 	    }
 
 		String reportComment = wiz.getDataSubSource().getDisplayName() + " Custom Report";
 		if (wiz.getReportComment() != null && wiz.getReportComment().length() > 0)
 			reportComment = wiz.getReportComment();
 
 		String reportTitle = wiz.getDataSubSource().getDisplayName() + " Custom Report";
 		if (wiz.getReportName() != null && wiz.getReportName().length() > 0)
 			reportTitle = wiz.getReportName();
 		wiz.getReportGenerator().put(ResourceDescriptor.TYPE_REPORTUNIT, wiz.getReportSaveAsName(), reportTitle, reportComment,wiz.getReportPath(),tempFile, wiz.getReportGenerator().getParams(), wiz.getDataSubSource().getJasperDatasourceName());
 
 		//    		wiz.getReportGenerator().put(ResourceDescriptor.TYPE_REPORTUNIT, reportTitle.replace(" ", "_"), reportTitle, reportComment, wiz.getReportPath(),tempFile, wiz.getReportGenerator().getParams());
 
 
 		// delete the temporary file
 		tempFile.delete();
 	}
 
 	public void setDynamicView(DynamicReportView dynamicView) {
 		this.dynamicView = dynamicView;
 	}
 
 	public void setJdbcDataSource(DataSource jdbcDataSource) {
 		this.jdbcDataSource = jdbcDataSource;
 	}
 
 	public void setReportFieldGroupService(
 			ReportFieldGroupService reportFieldGroupService) {
 		this.reportFieldGroupService = reportFieldGroupService;
 	}
 
 	public void setReportFieldService(ReportFieldService reportFieldService) {
 		this.reportFieldService = reportFieldService;
 	}
 
 	public void setReportSourceService(ReportSourceService reportSourceService) {
 		this.reportSourceService = reportSourceService;
 	}
 
 	public void setReportSubSourceGroupService(
 			ReportSubSourceGroupService reportSubSourceGroupService) {
 		this.reportSubSourceGroupService = reportSubSourceGroupService;
 	}
 
 	public void setReportSubSourceService(
 			ReportSubSourceService reportSubSourceService) {
 		this.reportSubSourceService = reportSubSourceService;
 	}
 
 	public void setReportUnitDataSourceURI(String reportUnitDataSourceURI) {
 		this.reportUnitDataSourceURI = reportUnitDataSourceURI;
 	}
 
 	public void setReportWizardService(ReportWizardService reportWizardService) {
 		this.reportWizardService = reportWizardService;
 	}
 
 	public void setSessionService(SessionService sessionService) {
 		this.sessionService = sessionService;
 	}
 
 	@Override
 	protected ModelAndView showForm(HttpServletRequest request,
 			HttpServletResponse response, BindException errors)
 	throws Exception {
 
 		if (request.getParameter("_target9") != null || request.getParameter("_target9.x") != null) {
 			return showPage(request, errors, 9);
 		}
 		else if (request.getParameter("_target10") != null || request.getParameter("_target10.x") != null) {
 			return showPage(request, errors, 10);
 		}
 		else if (request.getParameter("_target11") != null || request.getParameter("_target11.x") != null) {
 			return showPage(request, errors, 11);
 		}
 		else
 		{
 			return showPage(request, errors, getInitialPage(request, errors
 					.getTarget()));
 		}
 	}
 
 	public void setReportCustomFilterDefinitionService(
 			ReportCustomFilterDefinitionService reportCustomFilterDefinitionService) {
 		this.reportCustomFilterDefinitionService = reportCustomFilterDefinitionService;
 	}
 
 	public ReportCustomFilterDefinitionService getReportCustomFilterDefinitionService() {
 		return reportCustomFilterDefinitionService;
 	}
 
 	public void setReportSegmentationTypeService(
 			ReportSegmentationTypeService reportSegmentationTypeService) {
 		this.reportSegmentationTypeService = reportSegmentationTypeService;
 	}
 
 	public ReportSegmentationTypeService getReportSegmentationTypeService() {
 		return reportSegmentationTypeService;
 	}
 
 	public void setJasperServerService(JasperServerService jss) {
 		jasperServerService = jss;
 	}
 
 	public UserDetailsService getUserDetailsService() {
 		return userDetailsService;
 	}
 
 	public void setUserDetailsService(UserDetailsService userDetailsService) {
 		this.userDetailsService = userDetailsService;
 	}
 
 	@Override
 	protected ModelAndView processCancel(HttpServletRequest request,
 			HttpServletResponse response, Object command, BindException errors)
 			throws Exception {
 		command = null;
 		return new ModelAndView(getSuccessView());
 	}
 
 	public void setReportSegmentationResultsService(
 			ReportSegmentationResultsService reportSegmentationResultsService) {
 		this.reportSegmentationResultsService = reportSegmentationResultsService;
 	}
 
 	public ReportSegmentationResultsService getReportSegmentationResultsService() {
 		return reportSegmentationResultsService;
 	}
 
 	public void setReportCustomFilterHelper(ReportCustomFilterHelper reportCustomFilterHelper) {
 		this.reportCustomFilterHelper = reportCustomFilterHelper;
 	}
 
 	public ReportCustomFilterHelper getReportCustomFilterHelper() {
 		return reportCustomFilterHelper;
 	}
 }
