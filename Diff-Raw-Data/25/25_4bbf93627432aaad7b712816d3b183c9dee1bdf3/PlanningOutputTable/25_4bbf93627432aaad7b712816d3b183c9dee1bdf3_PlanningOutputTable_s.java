 package org.chai.kevin.planning;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.chai.kevin.data.Type;
 import org.chai.kevin.value.StoredValue;
 import org.chai.kevin.value.Value;
 
 public class PlanningOutputTable {
 
 	private PlanningOutput planningOutput;
 	private StoredValue value;
 	
 	public PlanningOutputTable(PlanningOutput planningOutput, StoredValue value) {
 		this.planningOutput = planningOutput;
 		this.value = value;
 	}
 
 	public Type getHeaderType() {
		return planningOutput.getDataElement().getType().getType(planningOutput.getFixedHeader());
 	}
 	
 	public List<Value> getRows() {
 		List<Value> values = new ArrayList<Value>();
 		if (value != null && !value.getValue().isNull()) {
 			for (int i = 0; i < value.getValue().getListValue().size(); i++) {
 				values.add(planningOutput.getDataElement().getType().getValue(value.getValue(), PlanningUtils.getPrefix(planningOutput.getFixedHeader(), i)));
 			}
 		}
 		return values;
 	}
 	
 	public Type getValueType(PlanningOutputColumn column) {
 		return planningOutput.getDataElement().getType().getType(column.getPrefix());
 	}
 	
 	public Value getValue(int row, PlanningOutputColumn column) {
 		if (value == null || value.getValue().isNull()) return null; 
 		return planningOutput.getDataElement().getType().getValue(value.getValue(), PlanningUtils.getPrefix(column.getPrefix(), row));
 	}
 	
 	public PlanningOutput getPlanningOutput() {
 		return planningOutput;
 	}
 	
 }
