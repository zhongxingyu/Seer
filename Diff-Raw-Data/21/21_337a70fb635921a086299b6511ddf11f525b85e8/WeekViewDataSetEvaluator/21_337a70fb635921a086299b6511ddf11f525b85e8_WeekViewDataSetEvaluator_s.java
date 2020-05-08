 package org.openmrs.module.rwandareports.dataset.evaluator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.openmrs.annotation.Handler;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.common.ObjectUtil;
 import org.openmrs.module.reporting.dataset.DataSet;
 import org.openmrs.module.reporting.dataset.DataSetColumn;
 import org.openmrs.module.reporting.dataset.DataSetRow;
 import org.openmrs.module.reporting.dataset.SimpleDataSet;
 import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
 import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.EvaluationException;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
 import org.openmrs.module.rwandareports.dataset.WeekViewDataSetDefinition;
 
 @Handler(supports = { WeekViewDataSetDefinition.class })
 public class WeekViewDataSetEvaluator implements DataSetEvaluator {
 	
 	public WeekViewDataSetEvaluator() {
 	}
 	
 	/**
 	 * @throws EvaluationException
 	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
 	 * @should evaluate a MultiPeriodIndicatorDataSetDefinition
 	 */
 	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
 		
 		context = ObjectUtil.nvl(context, new EvaluationContext());
 		if (context == null) {
 			context = new EvaluationContext();
 		}
 		
 		WeekViewDataSetDefinition dsd = (WeekViewDataSetDefinition) dataSetDefinition;
 		RowPerPatientDataSetDefinition base = dsd.getBaseDefinition();
 		
 		Map<String, Object> mappingsMon = new HashMap<String, Object>();
 		mappingsMon.put("endDate", "${startDate}");
 		
 		Map<String, Object> mappingsTues = new HashMap<String, Object>();
 		mappingsTues.put("endDate", "${startDate+1d}");
 		
 		Map<String, Object> mappingsWed = new HashMap<String, Object>();
 		mappingsWed.put("endDate", "${startDate+2d}");
 		
 		Map<String, Object> mappingsThurs = new HashMap<String, Object>();
 		mappingsThurs.put("endDate", "${startDate+3d}");
 		
 		Map<String, Object> mappingsFri = new HashMap<String, Object>();
 		mappingsFri.put("endDate", "${startDate+4d}");
 		
 		Map<String, Object> mappingsSat = new HashMap<String, Object>();
 		mappingsSat.put("endDate", "${startDate+5d}");
 		
 		Map<String, Object> mappingsSun = new HashMap<String, Object>();
 		mappingsSun.put("endDate", "${startDate+6d}");
 		
 		EvaluationContext ecMon = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsMon));
 		EvaluationContext ecTues = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsTues));
 		EvaluationContext ecWed = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsWed));
 		EvaluationContext ecThurs = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(
 		        base, mappingsThurs));
 		EvaluationContext ecFri = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsFri));
 		EvaluationContext ecSat = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsSat));
 		EvaluationContext ecSun = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappingsSun));
 		
 		SimpleDataSet monday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecMon);
 		SimpleDataSet tuesday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecTues);
 		SimpleDataSet wednesday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecWed);
 		SimpleDataSet thursday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecThurs);
 		SimpleDataSet friday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecFri);
 		SimpleDataSet saturday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecSat);
 		SimpleDataSet sunday = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ecSun);
 		
 		Map<SimpleDataSet, String> week = new HashMap<SimpleDataSet, String>();
 		week.put(monday, "monday");
 		week.put(tuesday, "tuesday");
 		week.put(wednesday, "wednesday");
 		week.put(thursday, "thursday");
 		week.put(friday, "friday");
 		week.put(saturday, "saturday");
 		week.put(sunday, "sunday");
 		
 		SimpleDataSet result = new SimpleDataSet(dsd, context);
		addColumns(result, monday);
 		
 		int maxSize = 0;
 		
 		for (SimpleDataSet day : week.keySet()) {
 			if (day.getRowMap().size() > maxSize) {
 				maxSize = day.getRowMap().size();
 			}
 		}
 		
 		for (int i = 0; i < maxSize; i++) {
 			
 			for (SimpleDataSet day : week.keySet()) {
 				String dayName = week.get(day);
 				Map<Integer, DataSetRow> rows = day.getRowMap();
 					
 				List<DataSetColumn> columns = result.getMetaData().getColumns();
 				for (DataSetColumn column : columns) {
 					if (column.getName().contains(dayName)) {
 						String columnName = column.getName().substring(dayName.length());
 						
 						if (day.getRowMap().size() > i) {
 							DataSetRow row = rows.get(i+1);
 							result.addColumnValue(i, column, row.getColumnValue(columnName));
 						} else {
 							result.addColumnValue(i, column, "");
 						}
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	private void addColumns(SimpleDataSet result, SimpleDataSet example) {
 		List<DataSetColumn> columns = example.getMetaData().getColumns();
 		
 		List<String> days = new ArrayList<String>();
 		days.add("monday");
 		days.add("tuesday");
 		days.add("wednesday");
 		days.add("thursday");
 		days.add("friday");
 		days.add("saturday");
 		days.add("sunday");
 		
 		for (String day : days) {
 			for (DataSetColumn column : columns) {
 				DataSetColumn col = new DataSetColumn(day + column.getName(), day + column.getLabel(), column.getClass());
 				result.getMetaData().addColumn(col);
 			}
 		}
 	}
 	
 }
