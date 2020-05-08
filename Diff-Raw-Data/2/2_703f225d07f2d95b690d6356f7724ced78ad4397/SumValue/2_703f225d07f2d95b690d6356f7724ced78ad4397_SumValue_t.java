 package org.chai.kevin.value;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Summ;
 import org.chai.location.CalculationLocation;
 
 public class SumValue extends CalculationValue<SumPartialValue> {
 	
 	public SumValue(Set<SumPartialValue> calculationPartialValues, Summ calculation, Period period, CalculationLocation location) {
 		super(new ArrayList<SumPartialValue>(calculationPartialValues), calculation, period, location);
 	}
 	
 	public SumValue(List<SumPartialValue> calculationPartialValues, Summ calculation, Period period, CalculationLocation location) {
 		super(calculationPartialValues, calculation, period, location);
 	}
 
 	@Override
 	public boolean isNull(){
 		return getValue().isNull();
 	}
 	
 	@Override
 	public Value getValue() {
 		//data Location
 		if (getLocation().collectsData()) {
 			return getDataLocationValue();
 		}
 		//location
 		Double sum = null;
 		for (SumPartialValue partialValue : getCalculationPartialValues()) {
 			if (!partialValue.getValue().isNull()) {
 				if (sum == null) sum = 0d;
 				sum += partialValue.getValue().getNumberValue().doubleValue();
 			}
 		}
 		
 		return getData().getType().getValue(sum);
 	}
 	
 	@Override
 	public Value getAverage(){		
 		Double average = 0d;
 		//data location
 		if (getLocation().collectsData()) {
 			return getDataLocationValue();			
 		}
 		//location
 		Double sum = 0d;
 		Double num = 0d;
 		for (SumPartialValue sumPartialValue : getCalculationPartialValues()) {
 			if (!sumPartialValue.getValue().isNull()) {
 				// exclude null values from average
 				sum += sumPartialValue.getValue().getNumberValue().doubleValue();
 				num += sumPartialValue.getNumberOfDataLocations();
 			}
 		}
 		
		average = sum / num;
 		
 		if (average.isInfinite()) average = null;
 		else if (average.isNaN()) average = null;		
 		
 		return getData().getType().getValue(average);
 	}
 	
 	//@Override TODO move to CalculationValue for use with AggregationValue
 	public Integer getNumberOfDataLocations(){
 		//data Location
 		if (getLocation().collectsData()) {
 			return 1;
 		}
 		//location
 		Integer numberOfDataLocations = 0;
 		for (SumPartialValue sumPartialValue : getCalculationPartialValues()) {
 			if (!sumPartialValue.getValue().isNull()) {
 				numberOfDataLocations += sumPartialValue.getNumberOfDataLocations();
 			}
 		}
 		return numberOfDataLocations;
 	}
 	
 	private Value getDataLocationValue(){
 		if (getCalculationPartialValues().size() > 1) throw new IllegalStateException("Calculation for DataLocation does not contain only 1 partial value");
 		if (getCalculationPartialValues().size() == 0) return Value.NULL_INSTANCE();
 		return getCalculationPartialValues().get(0).getValue();
 	}
 	
 	@Override
 	public String toString() {
 		return "SumValue [getValue()=" + getValue() + "]";
 	}
 
 	@Override
 	public Date getTimestamp() {
 		return null;
 	}
 	
 }
