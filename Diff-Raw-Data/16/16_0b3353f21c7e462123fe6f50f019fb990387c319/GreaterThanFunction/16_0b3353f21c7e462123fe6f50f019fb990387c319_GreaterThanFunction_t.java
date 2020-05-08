 package de.unipotsdam.hpi.thorben.ppi;
 
 import de.unipotsdam.hpi.thorben.ppi.typehelper.TypeHelper;
 
 public class GreaterThanFunction<N extends Number> extends AbstractTargetFunction<N> {
 
 	public GreaterThanFunction(TypeHelper<N> typeHelper) {
 		super(typeHelper);
 	}
 
	protected boolean applySpecific(N operator1, N operator2) {
 		return helper.greaterThan(operator1, operator2);
 	}
 }
