 package com.mpower.util;
 
 import java.awt.Color;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.sf.jasperreports.engine.JasperPrint;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.jaspersoft.jasperserver.irplugin.JServer;
 import com.mpower.domain.ReportAdvancedFilter;
 import com.mpower.domain.ReportChartSettings;
 import com.mpower.domain.ReportCrossTabFields;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportFieldType;
 import com.mpower.domain.ReportGroupByField;
 import com.mpower.domain.ReportStandardFilter;
 import com.mpower.domain.ReportWizard;
 import com.mpower.service.ReportFieldService;
 
 import ar.com.fdvs.dj.core.DJConstants;
 import ar.com.fdvs.dj.domain.ColumnsGroupVariableOperation;
 import ar.com.fdvs.dj.domain.DJChart;
 import ar.com.fdvs.dj.domain.DJChartOptions;
 import ar.com.fdvs.dj.domain.DJCrosstab;
 import ar.com.fdvs.dj.domain.DJCrosstabColumn;
 import ar.com.fdvs.dj.domain.DJCrosstabRow;
 import ar.com.fdvs.dj.domain.DynamicReport;
 import ar.com.fdvs.dj.domain.Style;
 import ar.com.fdvs.dj.domain.builders.ChartBuilderException;
 import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
 import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
 import ar.com.fdvs.dj.domain.builders.CrosstabBuilder;
 import ar.com.fdvs.dj.domain.builders.CrosstabColumnBuilder;
 import ar.com.fdvs.dj.domain.builders.CrosstabRowBuilder;
 import ar.com.fdvs.dj.domain.builders.DJChartBuilder;
 import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
 import ar.com.fdvs.dj.domain.builders.GroupBuilder;
 import ar.com.fdvs.dj.domain.constants.Border;
 import ar.com.fdvs.dj.domain.constants.GroupLayout;
 import ar.com.fdvs.dj.domain.constants.Page;
 import ar.com.fdvs.dj.domain.entities.ColumnsGroup;
 import ar.com.fdvs.dj.domain.entities.ColumnsGroupVariable;
 import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
 import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
 import ar.com.fdvs.dj.util.SortUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ReportGenerator {
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	private Style detailStyle;
 	private Style headerStyle;
 	private Style headerVariables;
 	private Style titleStyle;
 	private Style importeStyle;
 	private Style oddRowStyle;
 	private String reportServicesURI;
 	private String reportUserName;
 	private String reportPassword;
 
 	private JServer server = null;
 	private String reportUnitDataSourceURI;
 	private Map params;
 	private Map inputControls;
 
 	public ReportGenerator() {
 		params = new HashMap();
 		inputControls = new HashMap();
 	}
 	
 	public Map getInputControls() {
 		return inputControls;
 	}
 
 	public Map getParams() {
 		return params;
 	}
 
 	private void startServer() {
 		if (server == null) {
 			server = new JServer();
 			server.setUsername(reportUserName);
 			server.setPassword(reportPassword);
 			server.setUrl(reportServicesURI);
 		}
 	}
 
 	private void initStyles() {
 		detailStyle = new Style("detail");
 
 		headerStyle = new Style("header");
 
 		headerVariables = new Style("headerVariables");
 
 		titleStyle = new Style("titleStyle");
 
 		importeStyle = new Style();
 
 		oddRowStyle = new Style();
 	}
 
 	private File getTemplateFile() throws Exception {
 		startServer();
 
 		//
 		// get the report template file from the server
 		File templateFile = File.createTempFile("template", ".jrxml");
 		ResourceDescriptor templateRD = new ResourceDescriptor();
 		// templateRD.setWsType(ResourceDescriptor.TYPE_JRXML);
 		// templateRD.setParentFolder("/templates");
 		templateRD
 				.setUriString("/templates/mpower_template_files/mpower_template_jrxml");
 		ResourceDescriptor rd = server.getWSClient().get(templateRD,
 				templateFile);
 
 		return templateFile;
 	}
 
 	public DynamicReport Generate(ReportWizard wiz,javax.sql.DataSource jdbcDataSource, ReportFieldService reportFieldService) throws Exception {
 		File templateFile = getTemplateFile();
 		initStyles();
 
 		String reportTitle = wiz.getDataSubSource().getDisplayName() + " Custom Report";
 		if (wiz.getReportName() != null && wiz.getReportName().length() > 0)
 			reportTitle = wiz.getReportName();
 		
 		//Build the main report
 		FastReportBuilder drb = new FastReportBuilder();
   		Integer margin = new Integer(20);
   		
  		drb
 			.setTitleStyle(titleStyle)
 			.setTitle(reportTitle)					//defines the title of the report
 			.setDetailHeight(new Integer(15))
 			.setLeftMargin(margin).setRightMargin(margin).setTopMargin(margin).setBottomMargin(margin)
 			.setAllowDetailSplit(false)
   			.setIgnorePagination(true)
   			.setUseFullPageWidth(true)
   			.setWhenNoDataShowNoDataSection();
 //			.setPrintBackgroundOnOddRows(true)
 //			.setGrandTotalLegendStyle(headerVariables)
 //			.setOddRowBackgroundStyle(oddRowStyle)
 //			.setPageSizeAndOrientation(Page.Page_Letter_Landscape());
   		
  		//list used in for select statement of the query
  		List<ReportField> reportFieldsOrderedList = new LinkedList();
  		//
  		//Tabular and Summary Reports
  		if (wiz.getReportType().compareToIgnoreCase("matrix") != 0){
  	 		//Get a list of the selected report fields in order with the groupby fields(if any) at the beginning of the list
  			reportFieldsOrderedList = getSelectedReportFields(wiz, reportFieldService);
  	  		
  	 		//Build the columns for the report 
  	  		List<AbstractColumn> builtColumns = buildColumns(reportFieldsOrderedList);
  	  		
  	  		//Add the Columns if the report is a summary or tabular report 
  	  		if ( wiz.getReportType().compareToIgnoreCase("summary") == 0 || wiz.getReportType().compareToIgnoreCase("tabular") == 0 ){
  	  			Iterator itColumnsBuilt = builtColumns.iterator();
  	  	  		while (itColumnsBuilt.hasNext()){
  	  	  			AbstractColumn column = (AbstractColumn) itColumnsBuilt.next();
  	  	  			drb.addColumn(column);
  	  	  		}	
  	  		}//end Add columns
  	  		
  	  		
  	  		//
  	  		//Summary Report Only
  	  		//
  	  		if ( wiz.getReportType().compareToIgnoreCase("summary") == 0 ){
  	  			//
  	  			//Groups
  	  			//
  	  			List<ColumnsGroup> groupBy = new LinkedList<ColumnsGroup>();
  	  			groupBy = buildGroups(builtColumns, reportFieldsOrderedList, wiz);	//Build groups
  	  			Iterator itGroupsBuilt = groupBy.iterator();  //Add Groups
  	  			while (itGroupsBuilt.hasNext()){
  	  				ColumnsGroup group = (ColumnsGroup) itGroupsBuilt.next();
  	  				drb.addGroup(group);
  	  			}
  	  			
  	  			//
  	  			//Charts
  	  			//
  	  			List<DJChart> chartGroups = buildCharts(groupBy, builtColumns, wiz, reportFieldService);  //Build charts
  	  	  		Iterator itChartsBuilt = chartGroups.iterator();  //Add Charts
  	  			while (itChartsBuilt.hasNext()){
  	  				DJChart chart = (DJChart) itChartsBuilt.next();
  	  				drb.addChart(chart);
  	  			}
  	  	  	}//end if Summary Report
  	  		
  	  		//Add Global footer variables to the Report (Grand Totals at the end of the report)
  	  		drb = AddGlobalFooterVariables(reportFieldsOrderedList, builtColumns,drb);
  		}//end Tabular and Summary Reports
 
   		
   		//
   		//Matrix Reports
   		//
  		//Build and Add the columns for the matrix fields
   		if (wiz.getReportType().compareToIgnoreCase("matrix") == 0){
  			//List<ReportField> matrixFieldsList = new LinkedList();
  			//Add Row Fields
  			List<ReportGroupByField> rowFields = wiz.getReportCrossTabFields().getReportCrossTabRows();
  			Iterator itRow = rowFields.iterator();
  			while (itRow.hasNext()){
  				ReportGroupByField fGroupBy = (ReportGroupByField) itRow.next();
 				if (fGroupBy != null && fGroupBy.getFieldId() != -1 ){
  					ReportField f = reportFieldService.find(fGroupBy.getFieldId());
  					reportFieldsOrderedList.add(f);
 	 			}	
  			}
  			//Add Column Fields
  			List<ReportGroupByField> colFields = wiz.getReportCrossTabFields().getReportCrossTabColumns();
  			Iterator itCol = colFields.iterator();
  			while (itCol.hasNext()){
  				ReportGroupByField fGroupBy = (ReportGroupByField) itCol.next();
 				if (fGroupBy != null && fGroupBy.getFieldId() != -1){
  					ReportField f = reportFieldService.find(fGroupBy.getFieldId());
  					reportFieldsOrderedList.add(f);
  	 			}
  			}
  			//Add the Measure
  			ReportField fMeasure = reportFieldService.find(wiz.getReportCrossTabFields().getReportCrossTabMeasure());
  			reportFieldsOrderedList.add(fMeasure);
  			
  			List<AbstractColumn> builtMatrixColumns = buildColumns(reportFieldsOrderedList);
  			Iterator itMatrixColumnsBuilt = builtMatrixColumns.iterator();
   	  		while (itMatrixColumnsBuilt.hasNext()){
   	  			AbstractColumn column = (AbstractColumn) itMatrixColumnsBuilt.next();
   	  			drb.addColumn(column);
   	  		}	
  	 		//Create the Crosstab
   			DJCrosstab djcross = createCrosstab(wiz, reportFieldService);
   			//Add it to the report footer
   			drb.addFooterCrosstab(djcross);
   		}//end Matrix Report
   		
   		
   		//
 		//Build query
   		//
 		@SuppressWarnings("unused")
 		String query = "SELECT";
 		//List<ReportField> selectedReportFields = getSelectedReportFields(wiz, reportFieldService);
 		Iterator itSelectedReportFields = reportFieldsOrderedList.iterator();
 		boolean addComma = false;
 		while (itSelectedReportFields.hasNext()) {
 			ReportField selectedField = (ReportField) itSelectedReportFields.next();
 			if (addComma)
 				query = query + ",";
 			else
 				addComma = true;
 			
 			query = query + " " + selectedField.getColumnName();
 		}
 		
 		query = query + " FROM " + wiz.getDataSubSource().getViewName();
 		
 		Boolean bWhere = false;
 		
 		//
 		// Add any 'filters'
 		List<ReportStandardFilter> standardFilters = wiz.getStandardFilters();
 		Iterator itStandardFilters = standardFilters.iterator();
 		
 		List<ReportAdvancedFilter> filters = wiz.getAdvancedFilters();
 		Iterator itFilter = filters.iterator();
 		while (itStandardFilters.hasNext()) {
 			ReportStandardFilter filter = (ReportStandardFilter) itStandardFilters.next();
 			if (filter.getFieldId() == -1) break; // this is an empty filter
 			ReportField rf = reportFieldService.find(filter.getFieldId());
 			if (!bWhere) {
 				bWhere = true;
 				query += " WHERE ";
 			} else {
 				query += " AND ";
 			}
 			
 			
 			query += " " + rf.getColumnName();
 
 			
 			switch(filter.getDuration()) {
 			case 1: // Current FY
 				break;
 			case 2: // Previous FY
 				break;
 			case 3: // Current FY
 				break;
 			case 4: // Current FY
 				break;
 			case 5: // Current FY
 				break;
 			case 6: // Current FY
 				break;
 			case 7: // Current FY
 				break;
 			case 8: // Current FY
 				break;
 			case 9: // Current FY
 				break;
 			case 10: // Current FY
 				break;
 			case 11: // Current FY
 				break;
 			case 12: // Current FY
 				break;
 			case 13: // Current FY
 				break;
 			case 14: // Current FY
 				break;
 			case 15: // Current FY
 				break;
 			case 16: // Current FY
 				break;
 			case 17: // Current FY
 				break;
 			case 18: // Current FY
 				break;
 			case 19: // Current FY
 				break;
 			case 20: // Current FY
 				break;
 			case 21: // Today
 				query += " = CURDATE()";
 				break;
 			case 22: // Yesterday
 				query += " = Date_Add(CURDATE(),INTERVAL -1 DAY)";
 				break;
 			case 23: // Last 30
 				query += " > Date_Add(CURDATE(),INTERVAL -30 DAY)";
 				break;
 			case 24: // Last 60
 				query += " > Date_Add(CURDATE(),INTERVAL -60 DAY)";
 				break;
 			case 25: // Last 90
 				query += " > Date_Add(CURDATE(),INTERVAL -90 DAY)";
 				break;
 			case 26: // Last 120
 				query += " > Date_Add(CURDATE(),INTERVAL -120 DAY)";
 				break;
 			case 27: // Last 7
 				query += " > Date_Add(CURDATE(),INTERVAL -7 DAY)";
 				break;
 				
 			}
 		}
 		
 
 		while (itFilter.hasNext()) {
 			ReportAdvancedFilter filter = (ReportAdvancedFilter) itFilter
 					.next();
 			
			if (filter == null || filter.getFieldId() == -1) continue; // this is an empty filter
 			ReportField rf = reportFieldService.find(filter.getFieldId());
 
 
 
 			
 			if (!bWhere) {
 				bWhere = true;
 				query += " WHERE ";
 			} else {
 				query += " AND ";
 			}
 
 			
 			query += " " + rf.getColumnName();
 			switch (filter.getOperator()) {
 			case 1:	query += " = ";		break;
 			case 2:	query += " != ";	break;
 			case 3:	query += " < ";		break;
 			case 4:	query += " >"; break;
 			case 5:	query += " <="; break;
 			case 6:	query += " >="; break;
 			case 7: break; // contains ; break;
 			case 8: break; // startswith ; break;			
 			case 9: break; // includes ; break;			
 			case 10: break; // excludes ; break;
 			case 11: query += " LIKE "; break; // like ; break;						
 		
 			}
 			
 			InputControlParameters ic = new InputControlParameters();
 			ic.setLabel(rf.getDisplayName());
 			ic.setType(rf.getFieldType());
 			ic.setFilter(filter.getOperator());
 			String controlName = rf.getControlName();
 			
 			inputControls.put(controlName, ic);
 			
 			if ( rf.getFieldType() == ReportFieldType.DATE) {
 				query += " $P{" + controlName + "} ";
 				params.put(controlName, "java.util.Date");
 				drb.addParameter(controlName, "java.util.Date");				
 			} else	if(rf.getFieldType() == ReportFieldType.STRING) {
 				query += " $P{" + controlName + "} ";
 				drb.addParameter(controlName, "java.lang.String");
 				params.put(controlName, "java.lang.String");
 			} else if(rf.getFieldType() == ReportFieldType.DOUBLE) {
 				query += " $P{" + controlName + "} ";
 				drb.addParameter(controlName, "java.lang.Double");
 				params.put(controlName, "java.lang.Double");
 			} else if(rf.getFieldType() == ReportFieldType.INTEGER) {
 				query += " $P{" + controlName + "} ";
 				drb.addParameter(controlName, "java.lang.Integer");
 				params.put(controlName, "java.lang.Integer");
 			} else if(rf.getFieldType() == ReportFieldType.MONEY) {
 				query += " $P{" + controlName + "} ";
 				drb.addParameter(controlName, "java.lang.Double");
 				params.put(controlName, "java.lang.Double");
 			} else if(rf.getFieldType() == ReportFieldType.BOOLEAN) {
 				query += " $P{" + controlName + "} ";
 				drb.addParameter(controlName, "java.lang.Boolean");
 				params.put(controlName, "java.lang.Boolean");
 			} 
 		}
 		
 		//
 		//Add the order by clause
 		String orderBy = getOrderByClause(wiz, reportFieldService);
 		if (orderBy != null){
 			query += orderBy;
 		}
 
 		//Limit the number of rows returned 
 		if (wiz.getRowCount() != -1)
 			query += " LIMIT 0," + wiz.getRowCount().toString();
 		query += ";";
 
 		logger.info(query);
 
 		drb.setQuery(query, DJConstants.QUERY_LANGUAGE_SQL);
 		drb.setTemplateFile(templateFile.getAbsolutePath());
 		DynamicReport dr = drb.build();
 
 		return dr;
 	}
 	
 
 	/**
 	 * Builds the order by clause for the summary and matrix reports.
 	 * <P>
 	 * {@code}String orderBy = getOrderByClause(wiz, reportFieldService);
 	 * @param ReportWizard wiz
 	 * @param ReportFieldService reportFieldService
 	 * @return String orderBy
 	 */
 	private String getOrderByClause(ReportWizard wiz, ReportFieldService reportFieldService) {
 		String orderBy = new String();
 		Boolean addComma = false;
 		//
 		//Summary Reports
 		if (wiz.getReportType().compareTo("summary") == 0) {
 			List<ReportGroupByField> rptGroupByFields = wiz.getReportGroupByFields();
 			Iterator itRptGroupByFields = rptGroupByFields.iterator();
 			
 			if (itRptGroupByFields != null){
 				addComma = false;
 				while (itRptGroupByFields.hasNext()){
 					ReportGroupByField groupByField = (ReportGroupByField) itRptGroupByFields.next();
 					if (groupByField != null) {						
 						if (groupByField.getFieldId() != -1){
 							if (!addComma) {
 								orderBy += " ORDER BY";
 								addComma = true;
 							}
 							else
 								orderBy += ","; 
 							ReportField rg = reportFieldService.find(groupByField.getFieldId());
 							orderBy += " " + rg.getColumnName();
 							orderBy += " " + groupByField.getSortOrder();	
 						}
 					}
 				}
 			}
 		return orderBy;
 		}//end if summary
 		//
 		//Matrix Reports
 		if (wiz.getReportType().compareTo("matrix") == 0) {
 			ReportCrossTabFields rptCTList = wiz.getReportCrossTabFields();
 			List<ReportGroupByField> ctRows = rptCTList.getReportCrossTabRows();
 			List<ReportGroupByField> ctCols = rptCTList.getReportCrossTabColumns();
 			addComma = false;
 			//order by rows first
 			Iterator itCtRows = ctRows.iterator();
 			while (itCtRows.hasNext()){
 				ReportGroupByField rowField = (ReportGroupByField) itCtRows.next();
 				if (rowField != null) {						
 					if (rowField.getFieldId() != -1){
 						if (!addComma) {
 							orderBy += " ORDER BY";
 							addComma = true;
 						}
 						else
 							orderBy += ","; 
 						ReportField rg = reportFieldService.find(rowField.getFieldId());
 						orderBy += " " + rg.getColumnName();
 						orderBy += " " + rowField.getSortOrder();	
 					}
 				}
 			}
 			
 			//order by Columns last
 			Iterator itCtCols = ctCols.iterator();
 			while (itCtCols.hasNext()){
 				ReportGroupByField colField = (ReportGroupByField) itCtCols.next();
 				if (colField != null) {						
 					if (colField.getFieldId() != -1){
 						if (!addComma) {
 							orderBy += " ORDER BY";
 							addComma = true;
 						}
 						else
 							orderBy += ","; 
 						ReportField rg = reportFieldService.find(colField.getFieldId());
 						orderBy += " " + rg.getColumnName();
 						orderBy += " " + colField.getSortOrder();	
 					}
 				}
 			}
 		return orderBy;
 		}//end if matrix report
 			
 		return null;
 	}
 
 	/**
 	 * Creates a crosstab Report using DynamicJasper.
 	 * <P>
 	 * The data must be sorted with the following criteria: row(1) , ..., row(n) , column(1) , ..., column(m)
 	 * where row(1) is the outer-most row, row(n)is the inner-most row 
 	 * and column(1) is the outer-most column, column(m) is the inner-most column 
 	 * <P>
 	 * {@code}DJCrosstab djcross = createCrosstab(wiz, reportFieldService);
 	 * @param ReportWizard wiz
 	 * @param ReportFieldService reportFieldService
 	 * @return CrosstabBuilder cb
 	 */
 	private DJCrosstab createCrosstab(ReportWizard wiz, ReportFieldService reportFieldService) {
 		 
 		//get the settings for the crosstab report
 		ReportCrossTabFields reportCrossTabFields = wiz.getReportCrossTabFields();
 		
 		//
 		//Create the crosstab builder
 		CrosstabBuilder cb = new CrosstabBuilder();
  		cb.setHeight(200)
  			.setWidth(500)
  			//The .setDatasource is not working correctly with JasperServer. We remove this generated code 
  			//manually from the jrxml file before saving. (See the ReportFormWizardController removeCrossTabDataSubset.)
  			.setDatasource(null,DJConstants.DATA_SOURCE_ORIGIN_USE_REPORT_CONNECTION, DJConstants.DATA_SOURCE_TYPE_SQL_CONNECTION)
  			.setUseFullWidth(true)
  			.setColorScheme(4)
  			.setAutomaticTitle(true).setRowStyles(detailStyle, detailStyle, detailStyle)
  			.setColumnStyles(detailStyle, detailStyle, detailStyle)
  			.setCellBorder(Border.THIN);
  		
  		//
  		//Add a Measure to the builder(can only have one)
  		ReportField measure = reportFieldService.find(reportCrossTabFields.getReportCrossTabMeasure());
  		String operation = reportCrossTabFields.getReportCrossTabOperation();
  		String valueClassName = getValueClassName(measure);
     	ColumnsGroupVariableOperation cgvo = null;
     	if (operation.compareToIgnoreCase("AVERAGE") == 0) cgvo = ColumnsGroupVariableOperation.SUM;
     	if (operation.compareToIgnoreCase("SUM") == 0) cgvo = ColumnsGroupVariableOperation.AVERAGE;
     	if (operation.compareToIgnoreCase("HIGHEST") == 0) cgvo = ColumnsGroupVariableOperation.HIGHEST;
     	if (operation.compareToIgnoreCase("LOWEST") ==0) cgvo = ColumnsGroupVariableOperation.LOWEST;
     	if (operation.compareToIgnoreCase("COUNT") ==0) cgvo = ColumnsGroupVariableOperation.COUNT;
     	if (cgvo == null) cgvo = ColumnsGroupVariableOperation.COUNT;
         cb.addMeasure(measure.getColumnName(), valueClassName, cgvo, measure.getDisplayName(), detailStyle );
  
  		//
         //Add rows to the builder
         List<ReportGroupByField> reportCrossTabRows =	reportCrossTabFields.getReportCrossTabRows();
 		Iterator itCTRows  = reportCrossTabRows.iterator();
 		while (itCTRows.hasNext()){
 			ReportGroupByField ctRow = (ReportGroupByField) itCTRows.next();
			if (ctRow != null && ctRow.getFieldId() != -1){
 				ReportField fRow = reportFieldService.find(ctRow.getFieldId());
 				valueClassName = getValueClassName(fRow);
 				DJCrosstabRow row = new CrosstabRowBuilder().setProperty(fRow.getColumnName(),valueClassName)
 	 			.setHeaderWidth(100).setHeight(20)
 	 			.setTitle(fRow.getDisplayName())
 	 			.setShowTotals(true)
 	 			.build();
 				cb.addRow(row);	
 			}
 		}
         
 		//
         //Add the columns to the builder
         List<ReportGroupByField> reportCrossTabColumns =	reportCrossTabFields.getReportCrossTabColumns();
 		Iterator itCTCols  = reportCrossTabColumns.iterator();
 		while (itCTCols.hasNext()){
 			ReportGroupByField ctCol = (ReportGroupByField) itCTCols.next();
			if (ctCol != null && ctCol.getFieldId() != -1){
 				ReportField fCol = reportFieldService.find(ctCol.getFieldId());
 				valueClassName = getValueClassName(fCol);
 				DJCrosstabColumn col = new CrosstabColumnBuilder().setProperty(fCol.getColumnName(),valueClassName)
 	 			.setHeaderHeight(60).setWidth(80)
 	 			.setTitle(fCol.getDisplayName())
 	 			.setShowTotals(true)
 	 			.build();
 				cb.addColumn(col);		
 			}
 		}
 		
 		return cb.build();
 	}
 
 	public String getValueClassName(ReportField reportField) {
 		String valueClassName = new String();
 		switch (reportField.getFieldType()) {
         case NONE:   	valueClassName = String.class.getName();	break;
         case STRING:   	valueClassName = String.class.getName();	break;
         case INTEGER:   valueClassName = Long.class.getName(); 		break;
         case DOUBLE:   	valueClassName = String.class.getName();	break;
         case DATE:   	valueClassName = Date.class.getName();  	break;
         case MONEY:   	valueClassName = Float.class.getName(); 	break;
         case BOOLEAN:   valueClassName = Boolean.class.getName();	
     	}
 		return valueClassName;
 	}
 
 
 
 	private List<ReportField> getSelectedReportFields(ReportWizard wiz, ReportFieldService reportFieldService) {
   		List<ReportField> fields = wiz.getSelectedReportFieldsInOrder();
   		Iterator itFields = fields.iterator();
   		List<ReportGroupByField> groupByFields = wiz.getReportGroupByFields();
   		Iterator itGroupByFields = groupByFields.iterator();
 
   		List<ReportField> selectedReportFieldsList = new LinkedList<ReportField>();
   		//get the groupby fields first
   		while (itGroupByFields.hasNext()){
   			ReportGroupByField group = (ReportGroupByField) itGroupByFields.next();
 			if (group == null) continue;
 			ReportField f = reportFieldService.find(group.getFieldId());
 			if (f == null || f.getId() == -1) continue;
 			selectedReportFieldsList.add(f);
   		}
   		
   		//then get a list of all remaining selected fields(excluding groupby columns)
   		while (itFields.hasNext()){
   			ReportField f = (ReportField) itFields.next();
 			if (f == null || f.getId() == -1 || wiz.IsFieldGroupByField(f.getId())) continue;
 			selectedReportFieldsList.add(f);
   		}
   		
   		return selectedReportFieldsList; 
 	}
 	
 	private List<AbstractColumn> buildColumns(List<ReportField> fields)throws ColumnBuilderException{
 		List<AbstractColumn> columnsBuilt = new LinkedList<AbstractColumn>();
 		Iterator itFields = fields.iterator();
 		String valueClassName = null;
 		String pattern = null;
 		while (itFields.hasNext()){
 			ReportField f = (ReportField) itFields.next();
 	    	switch (f.getFieldType()) {
 		        case NONE:   	valueClassName = String.class.getName(); pattern ="";	 		break;
 		        case STRING:   	valueClassName = String.class.getName(); pattern ="";			break;
 		        case INTEGER:   valueClassName = Long.class.getName(); 	 pattern ="";	 		break;
 		        case DOUBLE:   	valueClassName = String.class.getName(); pattern ="";	 		break;
 		        case DATE:   	valueClassName = Date.class.getName();   pattern ="MM/dd/yyyy";	break;
 		        case MONEY:   	valueClassName = Float.class.getName();  pattern ="$ 0.00";		break;
 		        case BOOLEAN:   valueClassName = Boolean.class.getName();pattern ="";	
 			}
 			AbstractColumn column = ColumnBuilder.getInstance()
 			 				.setColumnProperty(f.getColumnName(), valueClassName )
 			 				.setTitle(f.getDisplayName())
 			 				//.setStyle(detailStyle)
 			 				.setPattern(pattern)
 			 				.setHeaderStyle(headerStyle)
 			 				.build();
 			column.setName(f.getColumnName());
 			columnsBuilt.add(column);
 		}
 		return columnsBuilt;
 	}
 	
 	private List<ColumnsGroup> buildGroups(List<AbstractColumn> builtColumns, List<ReportField> reportFieldsOrderedList, ReportWizard wiz){
 		List<ColumnsGroup> groupsBuilt = new LinkedList<ColumnsGroup>();
 		
 		//Iterate thru the list of columns and if isgroupbyfield add group
 		Iterator itbuiltColumns = builtColumns.iterator();
 		while (itbuiltColumns.hasNext()){
 			AbstractColumn column = (AbstractColumn) itbuiltColumns.next();
 			
 			ReportField reportField = wiz.getReportFieldByColumnName(column.getName().toString());
 			
 			if  ( wiz.IsFieldGroupByField(reportField.getId())){
 				ColumnsGroup group = new ColumnsGroup();
 				group.setColumnToGroupBy((PropertyColumn) column);
 				group.setLayout(GroupLayout.DEFAULT);
 				
 				ArrayList cgvList = new ArrayList();
 				group.setFooterVariables(getColumnGroupVariables(cgvList, builtColumns, reportFieldsOrderedList, wiz));
 				
 				//add the group
 				groupsBuilt.add(group);
 			}
 		}
 		return groupsBuilt;
 	}	
 	
 	private ArrayList getColumnGroupVariables(ArrayList cgvList, List<AbstractColumn> builtColumns, List<ReportField> reportFieldsOrderedList, ReportWizard wiz) {
 		//set the summary info for the groups
 		for(int index=0; index < reportFieldsOrderedList.size(); index++) {
 			if (((ReportField)reportFieldsOrderedList.get(index)).getIsSummarized()) {
 				if (((ReportField)reportFieldsOrderedList.get(index)).getAverage()) {
 			    	AbstractColumn col = ((AbstractColumn)builtColumns.get(index));
 			    	ColumnsGroupVariable cgv = new ColumnsGroupVariable(col, ColumnsGroupVariableOperation.AVERAGE);
 			    	cgvList.add(cgv);
 			     }//end if
 			    if (((ReportField)reportFieldsOrderedList.get(index)).getPerformSummary()) {
 			    	AbstractColumn col = ((AbstractColumn)builtColumns.get(index));
 			    	ColumnsGroupVariable cgv = new ColumnsGroupVariable(col, ColumnsGroupVariableOperation.SUM);
 			    	cgvList.add(cgv);
 			     }//end if
 			    if (((ReportField)reportFieldsOrderedList.get(index)).getLargestValue()) {
 			    	AbstractColumn col = ((AbstractColumn)builtColumns.get(index));
 			    	ColumnsGroupVariable cgv = new ColumnsGroupVariable(col, ColumnsGroupVariableOperation.HIGHEST);
 			    	cgvList.add(cgv);
 			     }//end if
 			    if (((ReportField)reportFieldsOrderedList.get(index)).getSmallestValue()) {
 			    	AbstractColumn col = ((AbstractColumn)builtColumns.get(index));
 			    	ColumnsGroupVariable cgv = new ColumnsGroupVariable(col, ColumnsGroupVariableOperation.LOWEST);
 			    	cgvList.add(cgv);
 			     }//end if set cgv
 			}//end if summary field
 		}//end for
 		return cgvList;
 	}
 	
 	private List<DJChart> buildCharts(List<ColumnsGroup> groupByColumnsBuilt, List<AbstractColumn> allColumns, ReportWizard wiz,ReportFieldService reportFieldService) throws ChartBuilderException{
 		List<DJChart> chartsBuilt = new LinkedList<DJChart>();
 		List<ReportChartSettings> chartSettings = wiz.getReportChartSettings();
 		Iterator itChartSettings = chartSettings.iterator();
 		while (itChartSettings.hasNext()){
 			ReportChartSettings rcs = (ReportChartSettings) itChartSettings.next();
 			if (rcs.getChartType().compareTo("-1") == 0) continue; 
 			ReportField rfx = reportFieldService.find(rcs.getFieldIdx());
 			ColumnsGroup cg = getReportColumnsGroup(rfx, groupByColumnsBuilt);
 			ReportField rfy = reportFieldService.find(rcs.getFieldIdy());
 			List<AbstractColumn> columns = getReportAbstractColumn(rfy, allColumns);
 			
 			//DJChartBuilder cb = new DJChartBuilder(); 
 			DJChart chart = new DJChart();
 				DJChartOptions options = new DJChartOptions();
 				options.setPosition(DJChartOptions.POSITION_FOOTER);
 				options.setShowLabels(true);
 				chart.setOptions(options);
 				chart.setColumnsGroup(cg);
 				chart.setColumns(columns);
 				
 				//set chart type
 				if (rcs.getChartType().compareTo("Bar") == 0) chart.setType(DJChart.BAR_CHART);	
 				if (rcs.getChartType().compareTo("Pie") == 0) chart.setType(DJChart.PIE_CHART);
 				
 		        //set chart operation
 		        if (rcs.getOperation().compareTo("RecordCount") == 0) chart.setOperation(DJChart.CALCULATION_COUNT);
 		        if (rcs.getOperation().compareTo("Sum") == 0) chart.setOperation(DJChart.CALCULATION_SUM);
 		        						
 				chartsBuilt.add(chart);	
 		}//end while itChartSettings
 		return chartsBuilt;
 	}		
 
 	private List<AbstractColumn> getReportAbstractColumn(ReportField rfy, List<AbstractColumn> allColumns) {
 		List<AbstractColumn> columns = new LinkedList<AbstractColumn>();
 		Iterator itCol = allColumns.iterator();
 		while (itCol.hasNext()){
 			AbstractColumn column = (AbstractColumn) itCol.next();
 			if (rfy.getColumnName().compareToIgnoreCase(column.getName()) == 0){
 				columns.add(column);	
 			}
 		}
 		return columns;
 	}
 
 	private ColumnsGroup getReportColumnsGroup(ReportField rfx, List<ColumnsGroup> groupByColumnsBuilt) {
 		Iterator itGroupBy = groupByColumnsBuilt.iterator();
 		while (itGroupBy.hasNext()){
 			ColumnsGroup cg = (ColumnsGroup) itGroupBy.next();
 			if (rfx.getColumnName().compareToIgnoreCase(cg.getColumnToGroupBy().getName()) == 0){
 				return cg;	
 			}
 		}
 		return null;
 	}
 	
 	private FastReportBuilder AddGlobalFooterVariables(List<ReportField> fields, List<AbstractColumn> columnsBuilt, FastReportBuilder drb){
 	
 		//Iterate thru each of the fields to see if it is summarized  
 		Iterator itFields = fields.iterator();
 		while (itFields.hasNext()){
 			ReportField f = (ReportField) itFields.next();
 			Iterator itColumnsBuilt = columnsBuilt.iterator();
 			//if the report field issummarized = true get the abstract column
 			if (f.getIsSummarized()){
 				while (itColumnsBuilt.hasNext()){
 					AbstractColumn column = (AbstractColumn) itColumnsBuilt.next();
 					if (f.getColumnName() == column.getName()){
 						if (f.getPerformSummary()){
 							drb.addGlobalFooterVariable(column, ColumnsGroupVariableOperation.SUM).setGrandTotalLegend("Total");
 							break;
 						}
 						if (f.getAverage()){
 							drb.addGlobalFooterVariable(column, ColumnsGroupVariableOperation.AVERAGE).setGrandTotalLegend("Average");
 							break;
 						}
 						if (f.getSmallestValue()){
 							drb.addGlobalFooterVariable(column, ColumnsGroupVariableOperation.LOWEST).setGrandTotalLegend("Min");
 							break;
 						}
 						if (f.getLargestValue()){
 							drb.addGlobalFooterVariable(column, ColumnsGroupVariableOperation.HIGHEST).setGrandTotalLegend("Max");
 							break;
 						}
 					}//end if column name = field name
 				}//end while itColumnsBuilt
 			}//end if isSummarized
 		}//end while itFields
 		return drb;
 	}
 
 	private ResourceDescriptor putReportUnit(ResourceDescriptor rd,String name, String label, String desc, File report, Map params2) throws Exception 
     {
 		File resourceFile = null;
 
 		ResourceDescriptor tmpDataSourceDescriptor = new ResourceDescriptor();
 		tmpDataSourceDescriptor.setWsType(ResourceDescriptor.TYPE_DATASOURCE);
 		tmpDataSourceDescriptor.setReferenceUri(reportUnitDataSourceURI);
 		tmpDataSourceDescriptor.setIsReference(true);
 		rd.getChildren().add(tmpDataSourceDescriptor);
 
 		ResourceDescriptor jrxmlDescriptor = new ResourceDescriptor();
 		jrxmlDescriptor.setWsType(ResourceDescriptor.TYPE_JRXML);
 		jrxmlDescriptor.setName(name);
 		jrxmlDescriptor.setLabel(label);
 		jrxmlDescriptor.setDescription(desc);
 		jrxmlDescriptor.setIsNew(true);
 		jrxmlDescriptor.setHasData(true);
 		jrxmlDescriptor.setMainReport(true);
 
 		rd.getChildren().add(jrxmlDescriptor);
 		rd.setResourceProperty(
 				ResourceDescriptor.PROP_RU_ALWAYS_PROPMT_CONTROLS, true);
 		ResourceDescriptor reportDescriptor = server.getWSClient()
 				.addOrModifyResource(rd, report);
 
 		//
 		// if there are parameters for this report then add the input controls
 		Iterator it = inputControls.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry entrySet = (Entry) it.next();
 			String key = (String) entrySet.getKey();
 			InputControlParameters ic = (InputControlParameters) entrySet
 					.getValue();
 
 			//
 			// add an input control for every key
 			ResourceDescriptor jrxmlControl = new ResourceDescriptor();
 
 			jrxmlControl.setWsType(ResourceDescriptor.TYPE_INPUT_CONTROL);
 			// jrxmlControl.setDataType(ResourceDescriptor.DT_TYPE_TEXT);
 
 			jrxmlControl
 					.setControlType(ResourceDescriptor.IC_TYPE_SINGLE_VALUE);
 			jrxmlControl.setName(key);
 
 			jrxmlControl.setLabel(ic.getLabel());
 			jrxmlControl.setDescription(ic.getLabel());
 
 			jrxmlControl.setVersion(2);
 			jrxmlControl.setHasData(false);
 			jrxmlControl.setMandatory(false);
 			jrxmlControl.setIsNew(true);
 			jrxmlControl.setIsReference(false);
 			jrxmlControl.setStrictMax(false);
 			jrxmlControl.setStrictMin(false);
 			jrxmlControl.setReadOnly(false);
 			jrxmlControl.setMainReport(false);
 			jrxmlControl.setParentFolder(rd.getUriString() + "_files");
 			// jrxmlControl.setReferenceUri(rd.getUriString() + "_files/" +
 			// key);
 			jrxmlControl.setUriString(rd.getUriString() + "_files/" + key);
 			jrxmlControl
 					.setResourceType("com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl");
 			ResourceDescriptor dataTypeDescriptor = new ResourceDescriptor();
 			// dataTypeDescriptor.setWsType(ResourceDescriptor.TYPE_DATA_TYPE);
 			// dataTypeDescriptor.setDataType(ResourceDescriptor.DT_TYPE_TEXT);
 			// dataTypeDescriptor.setName("String");
 			// dataTypeDescriptor.setLabel("String");
 			dataTypeDescriptor.setWsType(ResourceDescriptor.TYPE_REFERENCE);
 			dataTypeDescriptor.setIsReference(true);
 
 			if (ic.getType() == ReportFieldType.STRING)
 				dataTypeDescriptor.setReferenceUri("/datatypes/String");
 			else if (ic.getType() == ReportFieldType.DATE)
 				dataTypeDescriptor.setReferenceUri("/datatypes/Date");
 			else
 				dataTypeDescriptor.setReferenceUri("/datatypes/Number");
 
 			jrxmlControl.getChildren().add(dataTypeDescriptor);
 
 			// server.getWSClient().addOrModifyResource(jrxmlControl, null);
 			server.getWSClient().modifyReportUnitResource(rd.getUriString(),
 					jrxmlControl, null);
 
 		}
 		return reportDescriptor;
 	}
 
 	public ResourceDescriptor put(String type, String name, String label,
 			String desc, String parentFolder, File report, Map params)
 			throws Exception {
 		ResourceDescriptor rd = new ResourceDescriptor();
 		rd.setName(name);
 		rd.setLabel(label);
 		rd.setDescription(desc);
 		rd.setParentFolder(parentFolder);
 		rd.setUriString(rd.getParentFolder() + "/" + rd.getName());
 		rd.setWsType(type);
 		rd.setIsNew(true);
 
 		if (type.equalsIgnoreCase(ResourceDescriptor.TYPE_FOLDER)) {
 			return server.getWSClient().addOrModifyResource(rd, null);
 		} else if (type.equalsIgnoreCase(ResourceDescriptor.TYPE_REPORTUNIT)) {
 			return putReportUnit(rd, name, label, desc, report, params);
 		}
 
 		// shouldn't reach here
 		return null;
 
 	}
 
 	public JasperPrint runReport(String reportUnit, java.util.Map parameters)
 			throws Exception {
 		ResourceDescriptor rd = new ResourceDescriptor();
 		rd.setWsType(ResourceDescriptor.TYPE_REPORTUNIT);
 
 		return server.getWSClient().runReport(rd, parameters);
 	}
 
 	public void addOrModifyResource(ResourceDescriptor rd, File tempFile)
 			throws Exception {
 		server.getWSClient().addOrModifyResource(rd, tempFile);
 
 	}
 
 	public String getReportServicesURI() {
 		return reportServicesURI;
 	}
 
 	public void setReportServicesURI(String reportServicesURI) {
 		this.reportServicesURI = reportServicesURI;
 	}
 
 	public String getReportUserName() {
 		return reportUserName;
 	}
 
 	public void setReportUserName(String reportUserName) {
 		this.reportUserName = reportUserName;
 	}
 
 	public String getReportPassword() {
 		return reportPassword;
 	}
 
 	public void setReportPassword(String reportPassword) {
 		this.reportPassword = reportPassword;
 	}
 
 	public String getReportUnitDataSourceURI() {
 		return reportUnitDataSourceURI;
 	}
 
 	public void setReportUnitDataSourceURI(String reportUnitDataSourceURI) {
 		this.reportUnitDataSourceURI = reportUnitDataSourceURI;
 	}
 
 	public void resetInputControls() {
 		params = new HashMap();
 		inputControls = new HashMap();
 		
 	}
 }
