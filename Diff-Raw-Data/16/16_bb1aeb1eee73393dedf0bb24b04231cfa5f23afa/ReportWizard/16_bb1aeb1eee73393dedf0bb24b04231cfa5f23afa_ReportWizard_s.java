 package com.mpower.domain;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.FactoryUtils;
 import org.apache.commons.collections.list.LazyList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.annotations.IndexColumn;
 
 @Entity
 @Table(name = "REPORTWIZARD")
 public class ReportWizard implements java.io.Serializable {
 
 	@Transient
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "REPORTWIZARD_ID")
 	private Long id;
 
 	@Column(name = "REPORT_NAME")
 	private String reportName;
 	@Column(name = "REPORT_COMMENT")
 	private String reportComment;
 
 	@Column(name="ROW_COUNT")
 	private Integer rowCount;
 
 	@Column(name="REPORTDATASOURCE_ID")
 	private long srcId;
 
 	@Column(name = "REPORTSUBSOURCE_ID")
 	private long subSourceId;
 
 	@Transient
 	private ReportDataSource src;
 
 	@Transient
 	private ReportDataSubSource subsource;
 
 	@Transient
 	private List<ReportDataSource> sources;
 
 	@Transient
 	private List<ReportDataSubSource> subsources;
 
 	@ManyToMany()
 	@IndexColumn(name="REPORTFIELD_ID")
 	private List<ReportField> fields;
 
 	@ManyToMany()
 	@IndexColumn(name="REPORTFIELDGROUP_ID")
 	@Column(name = "FIELDGROUPS")
 	private List<ReportFieldGroup> fieldGroups;
 
 	@OneToMany(cascade = CascadeType.ALL)
 	@IndexColumn(name="REPORTFILTER_ID")
 	private List<ReportFilter> reportFilters;
 
 	@Transient
 	private List<ReportChartSettings> reportChartSettings;
 
 	@Transient
 	private ReportCrossTabFields reportCrossTabFields;
 
 	@Transient
 	private List<ReportSelectedField> reportSelectedFields;
 	
     @Transient
     private String currentWizardStep;
     
     @Transient
     private List  reportWizardSteps;
 	
 	@Column(name = "REPORT_TYPE")
 	private String reportType;
 	private ReportLayout reportLayout;
 	private Boolean recordCount;
 	private String reportPath;
 	private String reportTemplatePath;
 	private String reportTemplateJRXML;	
 
 	@Transient
 	private List   reportTemplateList;
 
 	@Transient
 	private String username;
 
 	@Transient
 	private String password;
 
 	@Transient
 	private String company;
 
 	public ReportWizard() {
 		reportType = "tabular";
 		reportLayout = ReportLayout.PORTRAIT;
 		srcId = 0;
 		subSourceId = 0;
 		rowCount = -1;
 		recordCount = false;
 		company = "Default";

 		//
 		// create a filter list decorated as a LazyList
 		reportFilters = LazyList.decorate(new ArrayList<ReportFilter>(),FactoryUtils.instantiateFactory(ReportFilter.class));
 		reportChartSettings = LazyList.decorate(new ArrayList<ReportChartSettings>(),FactoryUtils.instantiateFactory(ReportChartSettings.class));
 		reportCrossTabFields = new ReportCrossTabFields();
 		reportSelectedFields = LazyList.decorate(new ArrayList<ReportSelectedField>(),FactoryUtils.instantiateFactory(ReportSelectedField.class));
 	}
 
 	public List<ReportChartSettings> getReportChartSettings() {
 		return reportChartSettings;
 	}
 
 	public ReportCrossTabFields getReportCrossTabFields() {
 		return reportCrossTabFields;
 	}
 
 	public ReportDataSource getDataSource() {
 		logger.info("**** in getSrc()");
 		return src;
 	}
 
 	public List<ReportDataSource> getDataSources() {
 		return sources;
 	}
 
 	public ReportDataSubSource getDataSubSource() {
 		logger.info("**** in getDatasubSource");
 		return subsource;
 	}
 
 	public List<ReportDataSubSource> getDataSubSources() {
 		return subsources;
 	}
 
 	public List<ReportFieldGroup> getFieldGroups() {
 		return fieldGroups;
 	}
 
 	public List<ReportField> getFields() {
 		return fields;
 	}
 
 	public Boolean getRecordCount() {
 		return recordCount;
 	}
 
 	public String getReportComment() {
 		return reportComment;
 	}
 
 	public ReportDataSource getReportDataSource() {
 		return src;
 	}
 
 	public ReportLayout getReportLayout() {
 		return reportLayout;
 	}
 
 	public String getReportName() {
 		return reportName;
 	}
 
 	public String getReportType() {
 		logger.info("**** getReportType");
 		if (reportType.compareTo("tabular") == 0 || reportType.compareTo("summary") == 0) {
 		    if (hasGroupByFields()) {
 		    	reportType = "summary";
 		    } else {
 		    	reportType = "tabular";
 		    }
 		}
 		return reportType;
 	}
 
 	public Integer getRowCount() {
 		return rowCount;
 	}
 
 	public long getSrcId() {
 		return srcId;
 	}
 
 	public long getSubSourceId() {
 		return subSourceId;
 	}
 
 	public void setReportChartSettings(List<ReportChartSettings> reportChartSettings) {
 		this.reportChartSettings = reportChartSettings;
 	}	
 
 	public void setReportCrossTabFields(ReportCrossTabFields reportCrossTabFields) {
 		this.reportCrossTabFields = reportCrossTabFields;
 	}
 
 	public void setDataSource(ReportDataSource src) {
 		logger.info("**** in setSrc()");
 		this.src = src;
 	}
 
 	public void setDataSources(List<ReportDataSource> sources) {
 		this.sources = sources;
 	}
 
 	public void setDataSubSource(ReportDataSubSource subsource) {
 		logger.info("**** in setDatasubSource");
 		this.subsource = subsource;
 	}
 
 	public void setDataSubSources(List<ReportDataSubSource> subsource) {
 		this.subsources = subsource;
 	}
 
 	public void setFieldGroups(List<ReportFieldGroup> fieldGroups) {
 		this.fieldGroups = fieldGroups;
 	}
 
 	public void setFields(List<ReportField> fields) {
 		this.fields = fields;
 	}
 
 	public void setRecordCount(Boolean count) {
 		recordCount = count;
 	}
 
 	public void setReportComment(String reportComment) {
 		this.reportComment = reportComment;
 	}
 
 	public void setReportDataSource(ReportDataSource src) {
 		this.src = src;
 	}
 
 	public void setReportLayout(ReportLayout value) {
 		reportLayout = value;
 	}
 
 	public void setReportName(String name) {
 		reportName = name;
 	}
 
 	public void setReportType(String type) {
 		logger.info("**** setReportType");
 		if (type.compareTo("tabular") == 0 || type.compareTo("summary") == 0) {
 		    if (hasGroupByFields()) {
 		    	reportType = "summary";
 		    } else {
 		    	reportType = "tabular";
 		    }
 		} else {
 			reportType = type;
 		}
 	}
 
 	public boolean hasGroupByFields() {
 		boolean result = false;
 		List<ReportSelectedField> reportSelectedFields = getReportSelectedFields();
 		Iterator<ReportSelectedField> itReportSelectedFields = reportSelectedFields.iterator();
 		
 		if (itReportSelectedFields != null){
 			while (itReportSelectedFields.hasNext()) {
 				ReportSelectedField reportSelectedField = (ReportSelectedField) itReportSelectedFields.next();
 				if (reportSelectedField != null 
 					&& reportSelectedField.getFieldId() != -1
 					&& reportSelectedField.getGroupBy()) {
 					result = true;
 					break;
 				}
 			}
 		}
 		return result;		
 	}
 	
 	public void setRowCount(Integer rowCount) {
 		this.rowCount = rowCount;
 	}
 
 	public void setSrcId(long srcId) {
 		this.srcId = srcId;
 	}
 
 	public void setSubSourceId(long srcId) {
 		this.subSourceId = srcId;
 	}
 
 
 	public void populateDefaultReportFields() {
 		reportSelectedFields.clear();
 		Iterator<ReportField> iteratorFields = fields.iterator();
 		while(iteratorFields.hasNext()) {
 			ReportField reportField = iteratorFields.next();				
 			if (reportField.getIsDefault()) {
 				ReportSelectedField reportSelectedField = new ReportSelectedField();
 				reportSelectedField.setAverage(reportField.getAverage());
 				reportSelectedField.setFieldId(reportField.getId());
 				reportSelectedField.setMax(reportField.getLargestValue());
 				reportSelectedField.setMin(reportField.getSmallestValue());
 				reportSelectedField.setSum(reportField.getPerformSummary());
 				reportSelectedField.setSortOrder("ASC");
 				reportSelectedFields.add(reportSelectedField);
 			}
 		}
 	}
 	
 	public Boolean IsFieldGroupByField(long fieldId) {
 		Boolean result = false;
 		if (reportSelectedFields != null)
 		{
 			Iterator<ReportSelectedField> iteratorReportSelectedFields = reportSelectedFields.iterator();
 			while(iteratorReportSelectedFields.hasNext()) {
 				ReportSelectedField reportSelectedField = (ReportSelectedField) iteratorReportSelectedFields.next();
 				if (reportSelectedField != null && reportSelectedField.getFieldId() == fieldId && reportSelectedField.getGroupBy()) {
 					result = true;
 					break;
 				}
 			}
 		}
 		return result;
 	}
 
     public Boolean HasSummaryFields() {
     	Boolean result = false;
     	if (reportSelectedFields != null)
     	{
     	    Iterator<ReportSelectedField> itFields = reportSelectedFields.iterator();
     	    while(itFields.hasNext()) {
     	    	ReportSelectedField rptField = (ReportSelectedField) itFields.next();
 	    		if (rptField.getIsSummarized() == true) {
 	    		    result = true;
 	    		    break;
 	    		}
     	    }
     	}
     	return result;
     }
 	
 	public String getReportPath() {
 		return reportPath;
 	}
 
 	public void setReportPath(String reportPath) {
 		this.reportPath = reportPath;
 	}
 
 	public ReportField getReportFieldByColumnName(String columnName) {
 		ReportField result = new ReportField();
 		if (fields != null)
 		{
 			Iterator<ReportField> iteratorReportFields = fields.iterator();
 			while(iteratorReportFields.hasNext()) {
 				ReportField  reportField = iteratorReportFields.next();
 				if (reportField != null && reportField.getColumnName().compareToIgnoreCase(columnName) == 0) {
 					result = reportField;
 					break;
 				}
 			}
 		}
 		return result;
 	}
 
 	public void setReportFilters(List<ReportFilter> reportFilters) {
 		this.reportFilters = reportFilters;
 	}
 
 	public List<ReportFilter> getReportFilters() {
 		return reportFilters;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String u) {
 		int index = u.indexOf("@");
 
 		if (index != -1) {
 			company = u.substring(index + 1);
 		} else {
 			company = "Default";
 		}
 		username = u;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String p) {
 		password = p;
 	}
 
 	public String getTempFolderPath() {
 		return "/Reports/" + company + "/Temp";
 	}
 
 	public void setReportTemplateJRXML(String reportTemplateJRXML) {
 		this.reportTemplateJRXML = reportTemplateJRXML;
 	}
 
 	public String getReportTemplateJRXML() {
 		return reportTemplateJRXML;
 	}
 	
 	public String getReportTemplatePath() {
 		return reportTemplatePath;
 	}
 
 	public void setReportTemplatePath(String path) {
 		reportTemplatePath = path;
 		int index = path.lastIndexOf('/');
 		setReportTemplateJRXML(path + "_files/" + path.substring(index + 1) + "_jrxml");
 		//		reportTemplatePath = path;
 	}
 
 	public List getReportTemplateList() {
 		return reportTemplateList;
 	}
 
 	public void setReportTemplateList(List l) {
 		reportTemplateList = l;
 	}
 
 	public String getCompany() {
 		return company;
 	}
 
 	public void setReportSelectedFields(List<ReportSelectedField> reportSelectedFields) {
 		this.reportSelectedFields = reportSelectedFields;
 	}
 
 	public List<ReportSelectedField> getReportSelectedFields() {
 		return reportSelectedFields;
 	}
 	public String getCurrentWizardStep() {
 		return currentWizardStep;
 	}
 
 	public void setCurrentWizardStep(String wizardStep) {
 		this.currentWizardStep = wizardStep;
 	}
 
 	public List getReportWizardSteps() {
 		return reportWizardSteps;
 	}
 
 	public void setReportWizardSteps(List reportWizardSteps) {
 		this.reportWizardSteps = reportWizardSteps;
 	}	
 }
