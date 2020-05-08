 package fr.imag.adele.apam;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.declarations.CallbackMethod;
 import fr.imag.adele.apam.declarations.DependencyDeclaration;
 import fr.imag.adele.apam.declarations.DependencyInjection;
 import fr.imag.adele.apam.declarations.CallbackMethod.CallbackTrigger;
 
 public class Dependency extends DependencyDeclaration {
 
 	private final Set<ApamFilter> implementationConstraintFilters ;
 
 	/**
 	 * The set of constraints that must be satisfied by the target component instance
 	 */
 	private final Set<ApamFilter> instanceConstraintFilters;
 
 	/**
 	 * The list of preferences to choose among candidate service provider implementation
 	 */
 	private final List<ApamFilter> implementationPreferenceFilters;
 
 	/**
 	 * The list of preferences to choose among candidate service provider instances
 	 */
 	private final List<ApamFilter> instancePreferenceFilters;
 
 
 	public Dependency (DependencyDeclaration dep, Component component) {
 		super (dep.getComponent(), dep.getIdentifier(), dep.isMultiple(), dep.getTarget()) ;
		
//        this.callbacks.putAll(dep.callbacks);
//        this.injections.addAll(dep.injections);
        
        this.getImplementationConstraints().addAll(dep.getImplementationConstraints());
        this.getInstanceConstraints().addAll(dep.getInstanceConstraints());
        this.getImplementationPreferences().addAll(dep.getImplementationPreferences());
        this.getInstancePreferences().addAll(dep.getInstancePreferences());

        this.setMissingException(dep.getMissingException());
        this.setMissingPolicy(dep.getMissingPolicy());
 		
 		implementationConstraintFilters = new HashSet<ApamFilter> () ;
 		ApamFilter f ;
 		for (String c : dep.getImplementationConstraints()) {
 			f = ApamFilter.newInstanceApam(c, false, component) ;
 			if (f != null) 
 				implementationConstraintFilters.add(f);
 		}
 		
 		instanceConstraintFilters = new HashSet<ApamFilter> () ;
 		for (String c : dep.getInstanceConstraints()) {
 			f = ApamFilter.newInstanceApam(c, false, component) ;
 			if (f != null) 
 				instanceConstraintFilters.add(f);
 		}
 		
 		implementationPreferenceFilters = new ArrayList<ApamFilter> () ;
 		for (String c : dep.getImplementationPreferences()) {
 			f = ApamFilter.newInstanceApam(c, false, component) ;
 			if (f != null) 
 				implementationPreferenceFilters.add(f);
 		}
 		
 		instancePreferenceFilters = new ArrayList<ApamFilter> () ;
 		for (String c : dep.getInstancePreferences()) {
 			f = ApamFilter.newInstanceApam(c, false, component) ;
 			if (f != null) 
 				instancePreferenceFilters.add(f);
 		}
 	}
 
 	/**
 	 * Get the constraints that need to be satisfied by the implementation that resolves the reference
 	 */
 	public Set<ApamFilter> getImplementationConstraintFilters() {
 		return implementationConstraintFilters;
 	}
 
 	/**
 	 * Get the constraints that need to be satisfied by the instance that resolves the reference
 	 */
 	public Set<ApamFilter> getInstanceConstraintFilters() {
 		return instanceConstraintFilters;
 	}
 
 	/**
 	 * Get the resource provider preferences
 	 */
 	public List<ApamFilter> getImplementationPreferenceFilters() {
 		return implementationPreferenceFilters;
 	}
 
 	/**
 	 * Get the instance provider preferences
 	 */
 	public List<ApamFilter> getInstancePreferenceFilters() {
 		return instancePreferenceFilters;
 	}
 	
 	
 	public String toString () {
 	        StringBuffer ret = new StringBuffer ();
 	        ret.append (" effective dependency id: " + getIdentifier() + ". toward " + getTarget()) ;
 	        
 	        if (!getImplementationConstraintFilters().isEmpty()) {
 	        	ret.append ("\n         Implementation Constraints");
 	            for (ApamFilter inj : getImplementationConstraintFilters()) {
 	            	ret.append ("\n            " + inj);
 	            }
 	        }
 	        if (!getInstanceConstraintFilters().isEmpty()) {
 	        	ret.append ("\n         Instance Constraints");
 	            for (ApamFilter inj : getInstanceConstraintFilters()) {
 	            	ret.append ("\n            " + inj);
 	            }
 	        }
 	        if (!getImplementationPreferenceFilters().isEmpty()) {
 	        	ret.append ("\n         Implementation Preferences");
 	            for (ApamFilter inj : getImplementationPreferenceFilters()) {
 	            	ret.append ("\n            " + inj);
 	            }
 	        }
 	        if (!getInstancePreferenceFilters().isEmpty()) {
 	        	ret.append ("\n         Instance Preferences");
 	            for (ApamFilter inj : getInstancePreferenceFilters()) {
 	            	ret.append ("\n            " + inj);
 	            }
 	        }
 	        return ret.toString();
 
 	    }
 
 	}
 
 	/**
 	 * Set the filters, as a transformation of the string into filters, and substitutions
 	 */
 	//	public void setImplementationConstraintFilters(Set<ApamFilter> filters) {
 	//		implementationConstraintFilters = filters;
 	//	}
 	//	public void setInstanceConstraintFilters(Set<ApamFilter> filters) {
 	//		instanceConstraintFilters  = filters;;
 	//	}
 	//	public void setImplementationPreferenceFilters(List<ApamFilter> filters) {
 	//		implementationPreferenceFilters  = filters;;
 	//	}
 	//	public void setInstancePreferenceFilters(List<ApamFilter> filters) {
 	//		instancePreferenceFilters  = filters;;
 	//	}
 //
 //
 //}
