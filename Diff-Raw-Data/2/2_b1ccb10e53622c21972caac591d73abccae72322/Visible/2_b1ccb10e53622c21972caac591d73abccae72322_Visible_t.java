 package fr.imag.adele.apam.util;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Specification;
 
 public class Visible {
 
 	/**
 	 * returns true if an instance inside a source composite can establish a wire or link towards component target. 
 	 */
 	public static boolean isVisibleIn(Composite source, Component target) {
 		
 		assert source != null  && target != null;
 		
 		if (target instanceof Specification)
 			return true;
 		
 		if (target instanceof Implementation)
 			return isVisibleIn(source.getCompType(),(Implementation)target);
 
 		if (target instanceof Instance)
 			return isVisibleIn(source, (Instance)target);
 
 		return true;
 	}
 	
 	/**
 	 * returns true if an instance is visible from inside any composite of the source type. 
 	 */
 	private static boolean isVisibleIn(CompositeType source, Instance target) {
 		
 		assert source != null  && target != null && target.getComposite() != null;
 		
 
 		// Check if target can be imported from source
 		String imports = source.getCompoDeclaration().getVisibility().getImportInstances();
 		if (!matchVisibilityExpression(imports,target,source)) {
 			return false;
 		}
 
 		// Check if the target is exported by its owning composite
 		String exports = target.getComposite().getCompType().getCompoDeclaration().getVisibility().getExportInstances();
 		if ( matchVisibilityExpression(exports,target,target.getComposite().getCompType())) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * returns true if an instance is visible from inside a composite. 
 	 */
 	private static boolean isVisibleIn(Composite source, Instance target) {
 		
 		assert source != null  && target != null && target.getComposite() != null;
 		
 		/*
 		 * Check if the target is in the source composite
 		 */
 		if (target.getComposite().equals(source)) {
 			return true;
 		}
 
 		// Check if target is visible from the instances of source type
 		if ( isVisibleIn(source.getCompType(), target)) {
			return true;
 		}
 
 		// Check if the target is visible to other components in the same application
 		String applicationExports = target.getComposite().getCompType().getCompoDeclaration().getVisibility().getApplicationInstances();
 		if ( matchVisibilityExpression(applicationExports,target,target.getComposite().getCompType())) {
 			return target.getAppliComposite().equals(source.getAppliComposite());
 		}
 		
 		return false;
 	}
 
 	/**
 	 * returns true if an implementation is visible from inside a composite type. 
 	 */
 	private static boolean isVisibleIn(CompositeType source, Implementation target) {
 		
 		assert source != null && target != null && !target.getInCompositeType().isEmpty();
 		
 		if (target.getInCompositeType().contains(source))
 			return true;
 
 		// First check if target can be imported (borrowed) from source
 		String imports = source.getCompoDeclaration().getVisibility().getImportImplementations(); 
 		if (! matchVisibilityExpression(imports,target,source)) {
 			return false;
 		}
 
 		// Check if at least one composite type that owns target exports it.
 		for (CompositeType deployingTarget : target.getInCompositeType()) {
 			String exports = deployingTarget.getCompoDeclaration().getVisibility().getExportImplementations();
 			if (matchVisibilityExpression(exports,target,deployingTarget)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 
 	/**
 	 * returns true if Component source can establish a wire or link towards component target. 
 	 */
 	public static boolean isVisible(Component source, Component target) {
 		
 		assert source != null && target != null;
 		
 		if (source instanceof Specification)
 			return true;
 		
 		if (source instanceof Implementation) {
 			if (target instanceof Specification) 
 				return isVisible( (Implementation)source, (Specification)target) ;
 
 			if (target instanceof Implementation) 
 				return isVisible( (Implementation)source, (Implementation)target) ;
 
 			if (target instanceof Instance) 
 				return isVisible( (Implementation)source, (Instance)target) ;
 		}
 		
 		if (source instanceof Instance) {
 			return isVisibleIn(((Instance)source).getComposite(), target);
 		} 
 		
 		return false;
 	}
 
 
 	private static boolean isVisible (Implementation source, Specification target) {
 		return true;
 	}
 
 	private static boolean isVisible (Implementation source, Implementation target) {
 
 		//They have a composite type in common
 		Set<CompositeType> intersection = new HashSet<CompositeType>(source.getInCompositeType());
 		intersection.retainAll(target.getInCompositeType());
 		
 		if (! intersection.isEmpty())
 			return true;
 
 		// If the target is visible from at least one of the source composite types
 		for (CompositeType sourceCompositeType : source.getInCompositeType()) {
 			if (isVisibleIn(sourceCompositeType, target))
 				return true;
 		}
 		
 		return false ;
 	}
 
 	private static boolean isVisible (Implementation source, Instance target) {
 
 		//If target in same CT than source
 		if (((Implementation)source).getInCompositeType().contains(target.getComposite().getCompType()))
 			return true ;
 
 		// If the target is visible from at least one of the source composite types
 		for (CompositeType sourceCompositeType : source.getInCompositeType()) {
 			if (isVisibleIn(sourceCompositeType, target))
 				return true;
 		}
 		
 		return false ;
 	}
 
 
 	/**
 	 * return true if expression is null, "true" or if the component matches the expression.
 	 * Substitution, if any, is with evaluated in the specified context.
 	 * 
 	 */
 	private static boolean matchVisibilityExpression(String expre, Component comp, Component context) {
 		if ((expre == null) || expre.isEmpty() || expre.equals(CST.V_TRUE)) {
 			return true;
 		}
 		if (expre.equals(CST.V_FALSE)) {
 			return false;
 		}
 
 		//context is used for the substitution, if any.
 		ApamFilter f = ApamFilter.newInstanceApam(expre, context);
 		if (f == null) {
 			//Bad filter
 			return false;
 		}
 		return comp.match(f);
 	}
 
 
 }
