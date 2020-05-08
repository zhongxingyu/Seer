 package org.openmrs.module.rwandareports.dataset.evaluator;
 
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
 import org.openmrs.module.rwandareports.dataset.ConsecutiveCombinedDataSetDefinition;
 
 @Handler(supports = { ConsecutiveCombinedDataSetDefinition.class })
 public class ConsecutiveCombinedDataSetEvaluator implements DataSetEvaluator {
 	
 	public ConsecutiveCombinedDataSetEvaluator() {
 	}
 	
 	/**
 	 * @throws EvaluationException
 	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
 	 * @should evaluate a MultiPeriodIndicatorDataSetDefinition
 	 */
 	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
 		
 		ConsecutiveCombinedDataSetDefinition dsd = (ConsecutiveCombinedDataSetDefinition) dataSetDefinition;
 		RowPerPatientDataSetDefinition base = dsd.getBaseDefinition();
 		
 		SimpleDataSet result = new SimpleDataSet(dsd, context);
 		
 		int j = 0;
 		
		boolean sample = true;
		
 		for(int i = 0; i < dsd.getNumberOfIterations(); i++)
 		{
 			context = ObjectUtil.nvl(context, new EvaluationContext());
 			if (context == null) {
 				context = new EvaluationContext();
 			}
 		
 			String parameter = "${startDate}";
 			
 			if(i > 0)
 			{
 				parameter = "${startDate+" + i + "d}";
 			}
 			Map<String, Object> mappings = new HashMap<String, Object>();
 			mappings.put("endDate", parameter);
 			
 			EvaluationContext ec = EvaluationContext.cloneForChild(context, new Mapped<RowPerPatientDataSetDefinition>(base,
 		        mappings));
 		
 			SimpleDataSet day = (SimpleDataSet) Context.getService(DataSetDefinitionService.class).evaluate(base, ec);
 		
			if(day.getRowMap().size() > 0 && sample)
 			{
 				addColumns(result, day);
				sample = false;
 			}
 		
 			Map<Integer, DataSetRow> rows = day.getRowMap();
 			for(Integer rowNum: rows.keySet())
 			{
 				DataSetRow row = rows.get(rowNum);
 				
 				List<DataSetColumn> columns = result.getMetaData().getColumns();
 				for (DataSetColumn column : columns) {
 					result.addColumnValue(j, column, row.getColumnValue(column.getName()));
 					
 				}
 				j++;
 			}
 		}
 		
 		return result;
 	}
 	
 	private void addColumns(SimpleDataSet result, SimpleDataSet example) {
 		List<DataSetColumn> columns = example.getMetaData().getColumns();
 		
 		for (DataSetColumn column : columns) {
 			DataSetColumn col = new DataSetColumn(column.getName(), column.getLabel(), column.getClass());
 			result.getMetaData().addColumn(col);
 		}
 	}
 	
 }
