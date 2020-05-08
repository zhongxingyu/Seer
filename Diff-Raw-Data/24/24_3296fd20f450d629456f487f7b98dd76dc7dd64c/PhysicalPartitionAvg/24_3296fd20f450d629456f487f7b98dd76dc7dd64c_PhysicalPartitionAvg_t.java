/* $Id: PhysicalPartitionAvg.java,v 1.2 2003/09/17 00:00:42 jinli Exp $ */
 package niagara.query_engine;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import niagara.logical.PartitionAvg;
 import niagara.optimizer.colombia.*;
 import niagara.utils.*;
 
 import org.w3c.dom.Node;
 
 public class PhysicalPartitionAvg extends PhysicalPartitionGroup {
 	class GroupStatistics {
 	int count;
 	double sum;
 	}
 		
 	private Attribute avgAttribute;
 	private AtomicEvaluator ae;
 	private ArrayList values;
//	private static Double emptyGroupValue;
 
 	public void opInitFrom(LogicalOp logicalOperator) {
 		super.opInitFrom(logicalOperator);
 		// Get the averaging attribute from the logical operator
 		avgAttribute = ((PartitionAvg) logicalOperator).getAvgAttribute();
 	//	emptyGroupValue = Double.valueOf("0");
 	}
 
 	public void opInitialize() {
 		super.opInitialize();
 		ae = new AtomicEvaluator(avgAttribute.getName());
 		ae.resolveVariables(inputTupleSchemas[0], 0);
 		values = new ArrayList();
 	}
 
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#processTuple(StreamTupleElement, Object)
 	 */
 	public Object processTuple(
 		StreamTupleElement tuple,
 		Object previousGroupInfo) {
 		
 		ae.getAtomicValues(tuple, values);
 	try {
 		if (landmark) {
 			GroupStatistics prevAverage = (GroupStatistics) previousGroupInfo;
 			GroupStatistics newAverage;
 						
 			double newValue = Double.parseDouble((String) values.get(0));
 			values.clear();
 			newAverage = new GroupStatistics();
 
 			// New group
 			if(prevAverage == null) {
 				newAverage.count = 1;
 				newAverage.sum = newValue;
 				return newAverage;
 			}
 			// We already have statistics for the group
 			newAverage.count = prevAverage.count + 1;
 			newAverage.sum = prevAverage.sum + newValue;
 			double newAvg = newAverage.sum / newAverage.count;
 			double prevAvg = prevAverage.sum / prevAverage.count;
 			// We're messing with floating point arithmetic here,
 			// this test may fail...
 			if (newAvg != prevAvg) 
 				return newAverage;
 			else
 				// No change in group
 				return prevAverage;
 					
 		} else {
 			Vector newGroup = (Vector) previousGroupInfo;
 			
 			Double newValue = Double.valueOf((String) values.get(0));
 			values.clear();
			
 			int index = ((Double)newGroup.firstElement()).intValue();
 			Double expiredValue = (Double)newGroup.elementAt(index);
			//if (expiredValue == null)
			//	expiredValue = Double.valueOf("0");
 			newGroup.set(index, newValue);
 			index = index + 1;
			
 			if (index > range)
 				index = 1;
				
 			newGroup.set(0, new Double(index));
 			//Double max = findMax(newGroup);
 			double sum = ((Double)newGroup.elementAt(range+1)).doubleValue();
 			int count = ((Double)newGroup.elementAt(range+2)).intValue();
 			
			if (count < range) //for the initial several windows;
 				count += 1;
 				
 			sum = sum - expiredValue.doubleValue() + newValue.doubleValue();
 			
 			newGroup.set(range+1, new Double(sum));
 			newGroup.set(range+2, new Double(count));
 
 			return newGroup;
 		}
 				
 	} catch (NumberFormatException nfe) {
 		throw new RuntimeException("XXX vpapad what do we do here?!");
 	}
 	}
 	
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#emptyGroupValue()
 	 */
 	public Object emptyGroupValue() {
		//return null;
		return Double.valueOf("0");
 	}
 	
 
 	public Vector EmptyGroup() {
 
 		Vector emptyGroup = new Vector(range  +3);
 		emptyGroup.addElement(Double.valueOf("1"));
 		//the first element is the pointer to the next available element to put the new input item;
 		//the last two elements are used to save the current sum and count;
 		for (int i = 1; i <= range; i++ )
			emptyGroup.addElement(emptyGroupValue());
 		emptyGroup.addElement(Double.valueOf("0"));
 		emptyGroup.addElement(Double.valueOf("0"));
 		return emptyGroup;		
 	}
 	
 
 
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#constructOutput(Object)
 	 */
 	public Node constructOutput(Object groupInfo) {
 		if (landmark) {
 			if (groupInfo == null)
 				return doc.createTextNode(String.valueOf(0.0));
 			GroupStatistics gs = (GroupStatistics) groupInfo;
 			double avg = gs.sum / gs.count;
 				return doc.createTextNode(String.valueOf(avg));
 						
 		} else {
 			double sum = ((Double)((Vector) groupInfo).elementAt(range+1)).doubleValue();
 			double count = ((Double)((Vector) groupInfo).elementAt(range+2)).doubleValue();
 			double avg = sum / count;
 			return doc.createTextNode(String.valueOf(avg));
 		}
 		
 		
 	}
 
 	public Op opCopy() {
 		PhysicalPartitionAvg op = new PhysicalPartitionAvg();
 		if (logicalGroupOperator != null)
 			op.initFrom(logicalGroupOperator);
 		return op;
 	}
 
 	public boolean equals(Object o) {
 		if (o == null || !(o instanceof PhysicalPartitionAvg))
 			return false;
 		if (o.getClass() != PhysicalPartitionAvg.class)
 			return o.equals(this);
 		return logicalGroupOperator.equals(
 			((PhysicalPartitionAvg) o).logicalGroupOperator);
 	}
 
 	public int hashCode() {
 		return logicalGroupOperator.hashCode();
 	}
 }
