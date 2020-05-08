 package org.openmrs.module.reportingui.fragment.controller;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.openmrs.Cohort;
 import org.openmrs.module.reporting.cohort.EvaluatedCohort;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
 import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
 import org.openmrs.module.reporting.dataset.DataSet;
 import org.openmrs.module.reporting.dataset.DataSetColumn;
 import org.openmrs.module.reporting.dataset.DataSetRow;
 import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
 import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  */
 public class AdHocAnalysisFragmentController {
 
     public Result preview(@RequestParam("rowQueries") String rowQueriesJson,
                             @RequestParam("columns") String columnsJson,
                             UiUtils ui,
                             @SpringBean AllDefinitionLibraries allDefinitionLibraries,
                             @SpringBean CohortDefinitionService cohortDefinitionService,
                             @SpringBean DataSetDefinitionService dataSetDefinitionService) throws Exception {
 
         ObjectMapper jackson = new ObjectMapper();
         ArrayNode rowQueries = jackson.readValue(rowQueriesJson, ArrayNode.class);
         ArrayNode columns = jackson.readValue(columnsJson, ArrayNode.class);
 
         Result result = new Result();
 
         CompositionCohortDefinition composition = new CompositionCohortDefinition();
         int i = 0;
         for (JsonNode rowQuery : rowQueries) {
             // {
             //   "type":"org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition",
             //   "key":"reporting.library.cohortDefinition.builtIn.males",
             //   "name":"Male patients",
             //   "description":"Patients whose gender is M",
             //   "parameters":[]
             // }
             i += 1;
             CohortDefinition cohortDefinition = allDefinitionLibraries.getDefinition(CohortDefinition.class, rowQuery.get("key").getTextValue());
            composition.addSearch("" + i, cohortDefinition, "");
         }
 
         composition.setCompositionString(OpenmrsUtil.join(composition.getSearches().keySet(), " AND "));
 
         EvaluatedCohort cohort = cohortDefinitionService.evaluate(composition, new EvaluationContext());
         result.setAllRows(cohort.getMemberIds());
 
         // for preview purposes, just get 25 rows
         Cohort previewCohort = new Cohort();
         int j = 0;
         for (Integer member : cohort.getMemberIds()) {
             j += 1;
             previewCohort.addMember(member);
             if (j > 25) {
                 break;
             }
         }
 
         PatientDataSetDefinition dsd = new PatientDataSetDefinition();
         for (JsonNode column : columns) {
             // {
             //   "type":"org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition",
             //   "key":"reporting.patientDataCalculation.patientId",
             //   "name":"reporting.patientDataCalculation.patientId.name",
             //   "description":"reporting.patientDataCalculation.patientId.description",
             //   "parameters":[]
             // }
 
             PatientDataDefinition definition = allDefinitionLibraries.getDefinition(PatientDataDefinition.class, column.get("key").getTextValue());
             dsd.addColumn(column.get("name").getTextValue(), definition, "");
         }
 
         EvaluationContext previewEvaluationContext = new EvaluationContext();
         previewEvaluationContext.setBaseCohort(previewCohort);
 
         DataSet data = dataSetDefinitionService.evaluate(dsd, previewEvaluationContext);
         result.setColumnNames(getColumnNames(data));
         result.setData(transform(data, ui));
 
         return result;
     }
 
     private List<String> getColumnNames(DataSet data) {
         List<String> list = new ArrayList<String>();
         for (DataSetColumn dataSetColumn : data.getMetaData().getColumns()) {
             list.add(dataSetColumn.getLabel());
         }
         return list;
     }
 
     private List transform(DataSet data, UiUtils ui) {
         List<DataSetColumn> columns = data.getMetaData().getColumns();
 
         List<List<String>> list = new ArrayList<List<String>>();
         for (DataSetRow row : data) {
             List<String> simpleRow = new ArrayList<String>();
             Map<DataSetColumn, Object> columnValues = row.getColumnValues();
             for (DataSetColumn column : columns) {
                 simpleRow.add(ui.format(columnValues.get(column)));
             }
             list.add(simpleRow);
         }
 
         return list;
     }
 
     public class Result {
 
         private Set<Integer> allRows;
 
         private List<String> columnNames;
 
         private List<List<String>> data;
 
         public Result() { }
 
         public Set<Integer> getAllRows() {
             return allRows;
         }
 
         public void setAllRows(Set<Integer> allRows) {
             this.allRows = allRows;
         }
 
         public List<String> getColumnNames() {
             return columnNames;
         }
 
         public void setColumnNames(List<String> columnNames) {
             this.columnNames = columnNames;
         }
 
         public List<List<String>> getData() {
             return data;
         }
 
         public void setData(List<List<String>> data) {
             this.data = data;
         }
 
     }
 
 }
