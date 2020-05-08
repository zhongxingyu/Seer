 package de.unipotsdam.hpi.thorben.ppi;
 
 import de.unipotsdam.hpi.thorben.ppi.typehelper.TypeHelper;
 
 public class LowerThanFunction<N extends Number> extends AbstractTargetFunction<N> {
 
 	public LowerThanFunction(TypeHelper<N> typeHelper) {
 		super(typeHelper);
 	}
 
	public boolean applySpecific(N operator1, N operator2) {
 		return helper.lowerThan(operator1, operator2);
 	}
 }
