 package fr.imag.adele.apam.apamMavenPlugin;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.core.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.core.ComponentDeclaration;
 import fr.imag.adele.apam.core.ComponentReference;
 import fr.imag.adele.apam.core.CompositeDeclaration;
 import fr.imag.adele.apam.core.DependencyDeclaration;
 import fr.imag.adele.apam.core.DependencyInjection;
 import fr.imag.adele.apam.core.GrantDeclaration;
 import fr.imag.adele.apam.core.ImplementationDeclaration;
 import fr.imag.adele.apam.core.ImplementationReference;
 import fr.imag.adele.apam.core.InstanceDeclaration;
 import fr.imag.adele.apam.core.InterfaceReference;
 import fr.imag.adele.apam.core.MessageReference;
 import fr.imag.adele.apam.core.OwnedComponentDeclaration;
 import fr.imag.adele.apam.core.PropertyDefinition;
 import fr.imag.adele.apam.core.Reference;
 import fr.imag.adele.apam.core.ResourceReference;
 import fr.imag.adele.apam.core.SpecificationReference;
 import fr.imag.adele.apam.core.VisibilityDeclaration;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Util;
 
 public class CheckObr {
 
 	private static Logger logger = LoggerFactory.getLogger(CheckObr.class);
 
 	private static final Set<String>             allFields         = new HashSet<String>();
 
 	private static boolean                       failedChecking = false;
 
 	/**
 	 * An string value that will be used to represent mandatory attributes not specified. From CoreParser.
 	 */
 	public final static String                   UNDEFINED      = new String("<undefined value>");
 
 
 	public static boolean getFailedChecking() {
 		return CheckObr.failedChecking;
 	}
 
 	public static void error(String msg) {
 		CheckObr.failedChecking = true;
 		logger.error("ERROR: " + msg);
 	}
 
 	public static void warning(String msg) {
 		logger.error("Warning: " + msg);
 	}
 
 	public static void setFailedParsing(boolean failed) {
 		CheckObr.failedChecking = failed;
 	}
 
 	/**
 	 * Checks if the constraints set on this dependency are syntacticaly valid.
 	 * Only for specification dependencies.
 	 * Checks if the attributes mentioned in the constraints can be set on an implementation of that specification.
 	 * @param dep a dependency
 	 */
 	private static void checkConstraint(DependencyDeclaration dep) {
 		if ((dep == null) || !(dep.getTarget() instanceof ComponentReference))
 			return;
 
 		//get the spec or impl definition
 		ApamCapability cap = ApamCapability.get(dep.getTarget().as(ComponentReference.class));
 		if (cap == null)
 			return;
 		
 		//computes the attributes that can be associated with this spec or implementations members
 		Map<String, String> validAttrs = cap.getValidAttrNames();
 
 		CheckObr.checkFilters(dep.getImplementationConstraints(), dep.getImplementationPreferences(), validAttrs, dep.getTarget().getName());
 		CheckObr.checkFilters(dep.getInstanceConstraints(), dep.getInstancePreferences(), validAttrs, dep.getTarget().getName());
 	}
 
 
 	private static void checkFilters(Set<String> filters, List<String> listFilters, Map<String, String> validAttr, String comp) {
 		if (filters != null) {
 			for (String f : filters) {
 				ApamFilter parsedFilter = ApamFilter.newInstance(f);
 				parsedFilter.validateAttr(validAttr, f, comp);
 			}
 		}
 		if (listFilters != null) {
 			for (String f : listFilters) {
 				ApamFilter parsedFilter = ApamFilter.newInstance(f);
 				parsedFilter.validateAttr(validAttr, f, comp);
 			}
 		}
 	}
 
 
 	/**
 	 * Checks the attributes defined in the component; 
 	 * if valid, they are returned.
 	 * Then the attributes pertaining to the entity above are added.
 	 * @param component the component to check
 	 */
 	public static Map<String, String> getValidProperties(ComponentDeclaration component) {
 		//the attributes to return
 		Map<String, String> ret = new HashMap <String, String> ();
 		//Properties of this component
 		Map<String, String> properties = component.getProperties();
 
 		ApamCapability entCap = ApamCapability.get(component.getReference()) ;
 		if (entCap == null) return ret ; //should never happen.
 		
 		//return the valid attributes
 		for (String attr : properties.keySet()) {
 			if (validDefObr (entCap, attr, properties.get(attr))) {
 				ret.put(attr, properties.get(attr)) ;
 			}
 		}
 		
 		//add the attribute coming from "above" if not already instantiated and heritable
 		ApamCapability group = entCap.getGroup() ;
 		if (group != null && group.getProperties()!= null) {
 			for (String prop : group.getProperties().keySet()) {
 				if (ret.get(prop) == null && Util.isInheritedAttribute(prop)) {
 					ret.put(prop, group.getProperties().get(prop)) ;
 				}			
 			}
 		}	
 		
 		/*
 		 * Add the default values specified in the group for properties not
 		 * explicitly initialized
 		 */
 		if (group != null) {
 			for (String prop : group.getValidAttrNames().keySet()) {
 				if (! Util.isInheritedAttribute(prop)) 
 					continue;
 				if ( ret.get(prop) != null )
 					continue;
 				if (group.getAttrDefault(prop) == null)
 					continue;
 				
 				ret.put(prop, group.getAttrDefault(prop)) ;
 			}
 		} 
 		
 		return ret ;
 	}
 
 	/**
 	 * Checks if the attribute / values pair is valid for the component ent.
 	 * 
 	 * @param entName
 	 * @param attr
 	 * @param value
 	 * @param groupProps
 	 * @param superGroupProps
 	 * @return
 	 */
 	private static boolean validDefObr (ApamCapability ent, String attr, String value) {
 		if (Util.isPredefinedAttribute(attr))return true ; ;
 		if (!Util.validAttr(ent.getName(), attr)) return false  ;
 		
 //		//Top group all is Ok
 //		ApamCapability group = ent.getGroup() ;
 //		if (group == null) return true ;
 		
 		if (ent.getGroup()!= null && ent.getGroup().getProperties().get(attr) != null)  {
 			warning("Cannot redefine attribute \"" + attr + "\"");
 			return false ;
 		}
 
 		String defAttr = null ;
 		//if we are at top level, the attribute definition is at the same level; otherwise it must be defined "above"
 		ApamCapability group = (ent.getGroup() == null) ? ent : ent.getGroup() ;
 		while (group != null) {
 			defAttr = group.getAttrDefinition(attr)  ;
 			if (defAttr != null) break ;
 			group = group.getGroup() ;
 		}
 		 
 		if (defAttr == null) {
 			warning("In " + ent.getName() + ", attribute \"" + attr + "\" used but not defined.");
 			return false ;
 		}
 
 		return Util.checkAttrType(attr, value, defAttr) ;		
 	}
 
 
 	/**
 	 * An implementation has the following provide; check if consistent with the list of provides found in "cap".
 	 * 
 	 * @param cap. Can be null.
 	 * @param interfaces = "{I1, I2, I3}" or I1 or null
 	 * @param messages= "{M1, M2, M3}" or M1 or null
 	 * @return
 	 */
 	public static boolean checkImplProvide(ComponentDeclaration impl, String spec, Set<InterfaceReference> interfaces,
 			Set<MessageReference> messages) {
 		if (!(impl instanceof AtomicImplementationDeclaration)) return true ;
 		
 		if (spec == null)
 			return true;
 		ApamCapability cap = ApamCapability.get(new SpecificationReference(spec));
 		if (cap == null) {
 			return true;
 		}
 
 		Set<MessageReference> specMessages = cap.getProvideMessages();
 		Set<InterfaceReference> specInterfaces = cap.getProvideInterfaces();
 
 		if (!(messages.containsAll(specMessages)))
 			CheckObr.error("Implementation " + impl.getName() + " must produce messages "
 					+ Util.toStringSetReference(specMessages)) ;
 
 		if (!(interfaces.containsAll(specInterfaces)))
 			CheckObr.error("Implementation " + impl.getName() + " must implement interfaces "
 					+ Util.toStringSetReference(specInterfaces)) ;
 
 		return true;
 	}
 
 
 	public static void checkCompoMain(CompositeDeclaration composite) {
 		String name = composite.getName();
 		String implName = composite.getMainComponent().getName();
 		ApamCapability cap = ApamCapability.get(composite.getMainComponent());
 		if (cap == null) {
 			return;
 		}
 		if (composite.getSpecification() != null) {
 			String spec = composite.getSpecification().getName();
 			String capSpec = cap.getProperty(CST.PROVIDE_SPECIFICATION);
 			if ((capSpec != null) && !spec.equals(capSpec)) {
 				CheckObr.error("In " + name + " Invalid main implementation. " + implName
 						+ " must implement specification " + spec);
 			}
 		}
 
 		Set<MessageReference> mainMessages = cap.getProvideMessages();
 		Set<MessageReference> compositeMessages = composite.getProvidedResources(MessageReference.class);
 		if (!mainMessages.containsAll(compositeMessages))
 			CheckObr.error("In " + name + " Invalid main implementation. " + implName
 					+ " produces messages " + mainMessages
 					+ " instead of " + compositeMessages);
 
 		Set<InterfaceReference> mainInterfaces = cap.getProvideInterfaces() ;
 		Set<InterfaceReference> compositeInterfaces = composite.getProvidedResources(InterfaceReference.class);
 		if (!mainInterfaces.containsAll(compositeInterfaces))
 			CheckObr.error("In " + name + " Invalid main implementation. " + implName
 					+ " implements " + mainInterfaces
 					+ " instead of " + compositeInterfaces);
 	}
 
 	/**
 	 * For all kinds of components checks the dependencies : fields (for implems), and constraints.
 	 * 
 	 * @param component
 	 */
 	public static void checkRequire(ComponentDeclaration component) {
 		Set<DependencyDeclaration> deps = component.getDependencies();
 		if (deps == null)
 			return;
 		CheckObr.allFields.clear();
 		Set<String> depIds = new HashSet<String>();
 		for (DependencyDeclaration dep : deps) {
 			if (depIds.contains(dep.getIdentifier())) {
 				CheckObr.error("Dependency " + dep.getIdentifier() + " allready defined.");
 			} else
 				depIds.add(dep.getIdentifier());
 			// validating dependency constraints and preferences..
 			CheckObr.checkConstraint(dep);
 			// Checking fields and complex dependencies
 			CheckObr.checkFieldTypeDep(dep, component);
 		}
 	}
 
 
 
 
 	/**
 	 * Provided a dependency "dep" (simple or complex) checks if the field type and attribute multiple are compatible.
 	 * For complex dependency, for each field, checks if the target specification implements the field resource.
 	 * 
 	 * @param dep : a dependency
 	 * @param component : the component currently analyzed
 	 */
 	private static void checkFieldTypeDep(DependencyDeclaration dep, ComponentDeclaration component) {
 		if (!(component instanceof AtomicImplementationDeclaration)) return ;
 
 		// All field must have same multiplicity, and must refer to interfaces and messages provided by the specification.
 
 		Set<ResourceReference> specResources = new HashSet<ResourceReference>();
 		
 		if (dep.getTarget() instanceof ComponentReference<?>) {
 			ApamCapability cap = ApamCapability.get((ComponentReference)dep.getTarget()) ;
 			if (cap == null) return ;
 			specResources = cap.getProvideResources() ;
 		}
 		else {
 			specResources.add(dep.getTarget().as(ResourceReference.class));
 		}
 	
 		for (DependencyInjection innerDep : dep.getInjections()) {
 			
 			String type = innerDep.getResource().getJavaType();
 
 			if ((innerDep.getResource() != ResourceReference.UNDEFINED) && !(specResources.contains(innerDep.getResource()))) {
 				CheckObr.error("In " + component.getName() + dep + "\n      Field "
 						+ innerDep.getName()
 						+ " is of type " + type
 						+ " which is not implemented by specification or implementation " + dep.getIdentifier());
 			}
 		}
 	}
 
 	/**
 	 * Provided an atomic dependency, returns if it is multiple or not.
 	 * Checks if the same field is declared twice.
 	 * 
 	 * @param dep
 	 * @param component
 	 * @return
 	 */
 	public static boolean isFieldMultiple(DependencyInjection dep, ComponentDeclaration component) {
 		if (CheckObr.allFields.contains(dep.getName()) && !dep.getName().equals(CheckObr.UNDEFINED)) {
 			CheckObr.error("In " + component.getName() + " field/method " + dep.getName()
 					+ " allready declared");
 		}
 		else {
 			CheckObr.allFields.add(dep.getName());
 		}
 
 		return dep.isCollection();
 	}
 	
 	/**
 	 * check all the characteristics that can be found in the <contentMngt> of a composite
 	 * @param component
 	 */
 	public static void checkCompositeContent (CompositeDeclaration component) {
 		
 		checkStart ((CompositeDeclaration) component) ;
 		checkState ((CompositeDeclaration) component) ;
 		checkOwn ((CompositeDeclaration) component) ;
 		checkGrant ((CompositeDeclaration) component) ;
 		checkVisibility ((CompositeDeclaration) component) ;
 		checkGeneric ((CompositeDeclaration) component) ;
 		checkOverride ((CompositeDeclaration) component) ;
 	}
 
 	/**
 	 * Check the start characteristic.
 	 * It is very similar to an instance declaration, plus a trigger.
 	 * @param component
 	 */
 	private static void checkStart (CompositeDeclaration component) {
 		for (InstanceDeclaration start : component.getInstanceDeclarations()) {
 			//String main = start.
 		}
 	}
 
 	/**
 	 * Check the start characteristic.
 	 * It is very similar to an instance declaration, plus a trigger.
 	 * @param component
 	 */
 	private static void checkState (CompositeDeclaration component) {
 		PropertyDefinition.Reference ref = component.getStateProperty() ;
 		if (ref == null) {
			error ("A state must be associated with an implementation.") ;
 			return ;
 		}
 		
 		ComponentReference compo = ref.getDeclaringComponent() ;
 		if (! (compo instanceof ImplementationReference)) {
 			error ("A state must be associated with an implementation.") ;
 			return ;
 		}
 			ApamCapability implCap = ApamCapability.get(compo) ;
 			if (implCap == null) {
 				error ("Implementation for state unavailable: " + compo.getName()) ;
 				return ;
 			}
 		String propertyDef = implCap.getAttrDefinition(ref.getIdentifier()) ;
 		if (propertyDef == null) {
 			error ("The state attribute " + ref.getIdentifier() + " on implementation " + compo.getName() + " is undefined.") ;
 			return ;
 		}
 	}
 
 	private static boolean visibilityExpression (String expr) {
 		if (expr == null) {
 			//error("Missing expression in visibility. ") ;
 			return true ;
 		}
 		if (expr.equals(CST.V_FALSE) || expr.equals(CST.V_TRUE)) 
 			return true ;
 		try {
 			ApamFilter f = ApamFilter.newInstance(expr) ;
 		} catch (Exception e) {
 			error ("Bad filter in visibility expression " + expr) ;
 			return false ;
 		}
 		return true ;
 	}
 	
 	private static void checkVisibility (CompositeDeclaration component) {
 		VisibilityDeclaration visiDcl = component.getVisibility() ;
 		if (! visibilityExpression(visiDcl.getApplicationInstances())) 
 			error ("bad expression in Application visibility: " + visiDcl.getApplicationInstances()) ;
 		if (! visibilityExpression(visiDcl.getFriendImplementations())) 
 			error ("bad expression in Friend implementation visibility: " + visiDcl.getFriendImplementations()) ;
 		if (! visibilityExpression(visiDcl.getFriendInstances())) 
 			error ("bad expression in Friend instance visibility: " + visiDcl.getFriendInstances()) ;
 		if (! visibilityExpression(visiDcl.getLocalImplementations())) 
 			error ("bad expression in Local implementation visibility: " + visiDcl.getLocalImplementations()) ;
 		if (! visibilityExpression(visiDcl.getLocalInstances())) 
 			error ("bad expression in Local instance visibility: " + visiDcl.getLocalInstances()) ;
 		if (! visibilityExpression(visiDcl.getBorrowImplementations())) 
 			error ("bad expression in Borrow implementation visibility: " + visiDcl.getBorrowImplementations()) ;
 		if (! visibilityExpression(visiDcl.getBorrowInstances())) 
 			error ("bad expression in Borrow instance visibility: " + visiDcl.getBorrowInstances()) ;
 	}
 	
 	/**
 	 * 
 	 * @param component
 	 */
 	private static void checkOwn (CompositeDeclaration component) {
 		Set<OwnedComponentDeclaration> owned = component.getOwnedComponents() ;
 		for (OwnedComponentDeclaration own : owned) {
 			ApamCapability ownCap = ApamCapability.get(own.getComponent()) ;
 			if (ownCap == null) {
 				error ("Unknown component in own expression : " + own.getComponent().getName()) ;
 				break ;
 			}
 			//computes the attributes that can be associated with this spec or implementations members
 			Map<String, String> validAttrs = ownCap.getValidAttrNames();
 
 			CheckObr.checkFilters(own.getImplementationConstraints(), null, validAttrs, ownCap.getName());			
 			CheckObr.checkFilters(own.getInstanceConstraints(), null, validAttrs, ownCap.getName());			
 		}
 	}
 
 	private static void checkGrant (CompositeDeclaration component) {
 		List<GrantDeclaration> grants = component.getGrants();
 		for (GrantDeclaration grant : grants) {
 			DependencyDeclaration.Reference dep = grant.getDependency() ;
 			ComponentReference compo = dep.getDeclaringComponent() ;
 			String depName = dep.getIdentifier() ;
 			Set<String> states = grant.getStates() ;
 			ApamCapability cap = ApamCapability.get(compo) ;
 			if (cap == null) {
 				error ("Unknown component in own expression : " + compo.getName()) ;
 				break ;
 			}
 			System.out.println("Need to check dependency " + depName + " of component "
 					+ compo.getName() + " state attribute " + states ) ;
 			//look for the dependency
 			//TODO We do not have the dependencies in OBR
 			//TODO cannot compile that !! Need read the component, to get the dependency target, 
 			// and check if the states are valid.
 			//cap.getAttrDefinition(name) ;
 		}
 	}
 
 	
 	private static void checkGeneric (CompositeDeclaration component) {
 	
 	}
 
 	
 	private static void checkOverride (CompositeDeclaration component) {
 		
 	}
 
 }
