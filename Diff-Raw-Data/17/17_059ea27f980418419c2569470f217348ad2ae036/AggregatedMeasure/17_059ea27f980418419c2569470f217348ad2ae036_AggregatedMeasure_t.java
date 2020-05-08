 package de.unipotsdam.hpi.thorben.ppi.measure.process;
 
import java.util.List;

 import de.unipotsdam.hpi.thorben.ppi.measure.instance.BaseMeasure;
 import de.unipotsdam.hpi.thorben.ppi.measure.instance.entity.BaseMeasureInstance;
 
 public class AggregatedMeasure<M extends BaseMeasure<V>, V extends BaseMeasureInstance, N extends Number> implements ProcessMeasure<N> {
 
 	private String id;
 	private M baseMeasure;
 	private AggregationFunction<N, V> aggregationFunction;
 	
 	public N calculate() {
		List<V> values = baseMeasure.getAllValues();
		return aggregationFunction.calculate(values);
 	}
 	
 
 	public String getId() {
 		return id;
 	}
 	
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public M getBaseMeasure() {
 		return baseMeasure;
 	}
 
 	public void setBaseMeasure(M baseMeasure) {
 		this.baseMeasure = baseMeasure;
 	}
 
 	public void setAggregationFunction(
 			AggregationFunction<N, V> function) {
 		this.aggregationFunction = function;
 		
 	}
 	
 	
 
 }
