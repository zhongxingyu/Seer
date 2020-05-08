 package de.unipotsdam.hpi.thorben.ppi;
 
 import de.unipotsdam.hpi.thorben.ppi.measure.process.TargetFunction;
 import de.unipotsdam.hpi.thorben.ppi.typehelper.TypeHelper;
 
 public abstract class AbstractTargetFunction<N extends Number> implements
 		TargetFunction {
 	
 	protected TypeHelper<N> helper;
 	
 	public AbstractTargetFunction(TypeHelper<N> typeHelper) {
 		this.helper = typeHelper;
 	}
 
 	public boolean apply(Number operator1, Number operator2) {
 		return applySpecific(helper.asType(operator1), helper.asType(operator2));
 	}
 
	public abstract boolean applySpecific(N operator1, N operator2);
 }
