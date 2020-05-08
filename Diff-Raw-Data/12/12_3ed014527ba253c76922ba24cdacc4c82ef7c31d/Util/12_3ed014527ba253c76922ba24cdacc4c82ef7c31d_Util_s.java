 package fr.imag.adele.apam.util;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.felix.ipojo.metadata.Element;
 import org.osgi.framework.Filter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.core.ComponentDeclaration;
 import fr.imag.adele.apam.core.CompositeDeclaration;
 import fr.imag.adele.apam.core.DependencyDeclaration;
 import fr.imag.adele.apam.core.ImplementationReference;
 import fr.imag.adele.apam.core.ResourceReference;
 import fr.imag.adele.apam.core.SpecificationReference;
 import fr.imag.adele.apam.core.UndefinedReference;
 import fr.imag.adele.apam.util.CoreParser.ErrorHandler;
 
 /**
  * The static Class Util provides a set of static method for the iPOJO service concrete machine.
  * 
  * @author SAM team
  */
 public class Util {
 	private static Logger logger = LoggerFactory.getLogger(Util.class);
 	static boolean        failed;
 
 	private Util() {
 	};
 
 	public static List<ComponentDeclaration> getComponents(Element root) {
 		Util.failed = false;
 		CoreParser parser = new CoreMetadataParser(root);
 		return parser.getDeclarations(new ErrorHandler() {
 
 			@Override
 			public void error(Severity severity, String message) {
 				logger.error("error parsing component declaration : " + message);
 				Util.failed = true;
 			}
 		});
 	}
 
 	public static boolean getFailedParsing() {
 		return Util.failed;
 	}
 
 	/**
 	 * takes a list of string "A" "B" "C" ... and produces "{A, B, C, ...}"
 	 * 
 	 * @param names
 	 * @return
 	 */
 	public static String toStringResources(Set<String> names) {
 		if ((names == null) || (names.size() == 0))
 			return null;
 		String ret = "{";
 		for (String name : names) {
 			ret += name + ", ";
 		}
 		return ret.substring(0, ret.length() - 2) + "}";
 	}
 
 	public static String toStringArrayString(String[] names) {
 		return toStringResources(new HashSet<String>(Arrays.asList(names)));
 	}
 
 	public static Set<Filter> toFilter(Set<String> filterString) {
 		Set<Filter> filters = new HashSet<Filter>();
 		if (filterString == null)
 			return filters;
 
 		for (String f : filterString) {
 			filters.add(ApamFilter.newInstance(f));
 		}
 		return filters;
 	}
 
 	public static List<Filter> toFilterList(List<String> filterString) {
 		List<Filter> filters = new ArrayList<Filter>();
 		if (filterString == null)
 			return filters;
 
 		for (String f : filterString) {
 			filters.add(ApamFilter.newInstance(f));
 		}
 		return filters;
 	}
 
 	/**
 	 * Warning: returns an unmodifiable List !
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static List<String> splitList(String str) {
 		return Arrays.asList(Util.split(str));
 	}
 
 	public static Set<String> splitSet(String str) {
 		return new HashSet<String>(Arrays.asList(Util.split(str)));
 	}
 
 	/**
 	 * Provided a string contain a list of values, return an array of string containing the different values.
 	 * A list is of the form "{A, B, .... G}" or "[A, B, .... G]"
 	 * 
 	 * If the string is empty or is not a list, return the empty array.
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static String[] split(String str) {
 		if ((str == null) || (str.length() == 0))
 			return new String[0];
 
 		str = str.trim();
 		if ((str.charAt(0) != '{') && (str.charAt(0) != '[')) {
 			return stringArrayTrim(str.split(","));
 		}
 
 		str = str.replaceAll("\\ ", "");
 		str = str.replaceAll(";", ",");
 		str = str.replaceAll("\\[,", "[");
 		str = str.replaceAll(",]", "]");
 
 		String internal;
 		if (((str.charAt(0) == '{') && (str.charAt(str.length() - 1) == '}'))
 				|| ((str.charAt(0) == '[') && (str.charAt(str.length() - 1) == ']'))) {
 
 			internal = (str.substring(1, str.length() - 1)).trim();
 			// Check empty array
 			if (internal.length() == 0) {
 				return new String[0];
 			}
 			return internal.split(",");
 		} else {
 			return new String[] { str };
 		}
 	}
 
 	//    /**
 	//     * Orders the array in lexicographical order.
 	//     * 
 	//     * @param interfaces
 	//     */
 	//    public static String[] orderInterfaces(String[] interfaces) {
 	//        if (interfaces == null)
 	//            return null;
 	//        boolean ok = false;
 	//        String tmp;
 	//        while (!ok) {
 	//            ok = true;
 	//            for (int i = 0; i < interfaces.length - 1; i++) {
 	//                if (interfaces[i].compareTo(interfaces[i + 1]) > 0) {
 	//                    tmp = interfaces[i];
 	//                    interfaces[i] = interfaces[i + 1];
 	//                    interfaces[i + 1] = tmp;
 	//                    ok = false;
 	//                }
 	//            }
 	//        }
 	//        return interfaces;
 	//    }
 
 	//    /**
 	//     * compares the two arrays of interfaces. They may be in a different order. Returns true if they contain exactly the
 	//     * same interfaces (strings).
 	//     * 
 	//     * @param i1 : an array of strings
 	//     * @param i2
 	//     * @return
 	//     */
 	//    public static boolean sameInterfaces(String[] i1, String[] i2) {
 	//        if ((i1 == null) || (i2 == null) || (i1.length == 0) || (i2.length == 0))
 	//            return false;
 	//        if (i1.length != i2.length)
 	//            return false;
 	//        if ((i1.length == 1) && (i1[0].equals(i2[0])))
 	//            return true;
 	//        
 	//        for (int i = 0; i < i1.length; i++) {
 	//            if (!i1[i].equals(i2[i]))
 	//                return false;
 	//        }
 	//        return true;
 	//    }
 
 	//    public static String ANDLDAP(String... params) {
 	//        StringBuilder sb = new StringBuilder("(&");
 	//        for (String p : params) {
 	//            sb.append(p);
 	//        }
 	//        sb.append(")");
 	//        return sb.toString();
 	//    }
 	//
 	//    public static Filter buildFilter(Set<Filter> filters) {
 	//        if ((filters == null) || (filters.size() == 0))
 	//            return null;
 	//        String ldap = null;
 	//        for (Filter f : filters) {
 	//            if (ldap == null) {
 	//                ldap = f.toString();
 	//            } else {
 	//                ldap = Util.ANDLDAP(ldap, f.toString());
 	//            }
 	//        }
 	//        Filter ret = null;
 	//        try {
 	//            ret = org.osgi.framework.FrameworkUtil.createFilter(ldap);
 	//        } catch (InvalidSyntaxException e) {
 	//            logger.debug("Invalid filters : ");
 	//            for (Filter f : filters) {
 	//                logger.debug("   " + f.toString());
 	//                ;
 	//            }
 	//            e.printStackTrace();
 	//        }
 	//        return ret;
 	//    }
 
 	public static boolean checkImplVisibilityExpression(String expre, Implementation impl) {
 		if ((expre == null) || expre.equals(CST.V_TRUE))
 			return true;
 		if (expre.equals(CST.V_FALSE))
 			return false;
 		Filter f = ApamFilter.newInstance(expre);
 		if (f == null)
 			return false;
 		return impl.match(f);
 	}
 
 	/**
 	 * Implementation toImpl can be borrowed by composite type compoFrom if :
 	 * compoFrom accepts to borrow the service
 	 * toImpl is inside compoFrom.
 	 * attribute friendImplementation is set, and toImpl is inside a friend and matches the attribute.
 	 * toImpl does not matches the attribute localImplementation.
 	 * 
 	 * @param compoFrom
 	 * @param toImpl
 	 * @return
 	 */
 	public static boolean checkImplVisible(CompositeType compoFrom, Implementation toImpl) {
 		if (toImpl.getInCompositeType().isEmpty() || toImpl.getInCompositeType().contains(compoFrom))
 			return true;
 
 		// First check inst can be borrowed
 		String borrow = ((CompositeDeclaration) compoFrom.getDeclaration()).getVisibility().getBorrowImplementations(); // getProperty(CST.A_BORROWIMPLEM));
 		if ((borrow != null) && (Util.checkImplVisibilityExpression(borrow, toImpl) == false))
 			return false;
 
 		// true if at least one composite type that own toImpl accepts to lend it to compoFrom.
 		for (CompositeType compoTo : toImpl.getInCompositeType()) {
 			if (Util.checkImplVisibleInCompo(compoFrom, toImpl, compoTo))
 				return true;
 		}
 		return false;
 	}
 
 	public static boolean
 	checkImplVisibleInCompo(CompositeType compoFrom, Implementation toImpl, CompositeType compoTo) {
 		if (compoFrom == compoTo)
 			return true;
 		if (compoFrom.isFriend(compoTo)) {
 			String friend = ((CompositeDeclaration) compoTo.getDeclaration()).getVisibility()
 			.getFriendImplementations(); // .getProperty(CST.A_FRIENDIMPLEM));
 			//            String friend = ((String) compoTo.getProperty(CST.A_FRIENDIMPLEM));
 			if ((friend != null) && Util.checkImplVisibilityExpression(friend, toImpl))
 				return true;
 		}
 		String local = ((CompositeDeclaration) compoTo.getDeclaration()).getVisibility().getLocalImplementations();
 		//        String local = ((String) compoTo.getProperty(CST.A_LOCALIMPLEM));
 		if ((local != null) && Util.checkImplVisibilityExpression(local, toImpl))
 			return false;
 		return true;
 	}
 
 	public static boolean checkInstVisibilityExpression(String expre, Instance inst) {
 		if ((expre == null) || expre.equals(CST.V_TRUE))
 			return true;
 		if (expre.equals(CST.V_FALSE))
 			return false;
 		Filter f = ApamFilter.newInstance(expre);
 		if (f == null)
 			return false;
 		return inst.match(f);
 	}
 
 	/**
 	 * Instance toInst can be borrowed by composite compoFrom if :
 	 * compoFrom accepts to borrow the attribute
 	 * toInst is inside compoFrom.
 	 * attribute friendInstance is set, and toInst is inside a friend and matches the attribute.
 	 * attribute appliInstance is set, and toInst is in same appli and matches the attribute.
 	 * toInst does not matches the attribute localInstance.
 	 * 
 	 * @param compoFrom
 	 * @param toInst
 	 * @return
 	 */
 	public static boolean checkInstVisible(Composite compoFrom, Instance toInst) {
 		Composite toCompo = toInst.getComposite();
 		//        CompositeType toCompoType = toInst.getComposite().getCompType();
 		CompositeType fromCompoType = compoFrom.getCompType();

 		if (compoFrom == toCompo)
 			return true;
 
 		// First check inst can be borrowed
 		String borrow = ((CompositeDeclaration) fromCompoType.getDeclaration()).getVisibility().getBorrowInstances();
 		if ((borrow != null) && (Util.checkInstVisibilityExpression(borrow, toInst) == false))
 			return false;
 
 		if (compoFrom.dependsOn(toCompo)) {
			String friend = ((CompositeDeclaration) fromCompoType.getDeclaration()).getVisibility()
 			.getFriendInstances();
 			if ((friend != null) && Util.checkInstVisibilityExpression(friend, toInst))
 				return true;
 		}
 		if (compoFrom.getAppliComposite() == toCompo.getAppliComposite()) {
			String appli = ((CompositeDeclaration) fromCompoType.getDeclaration()).getVisibility()
 			.getApplicationInstances();
 			if ((appli != null) && Util.checkInstVisibilityExpression(appli, toInst))
 				return true;
 		}
		String local = ((CompositeDeclaration) fromCompoType.getDeclaration()).getVisibility().getLocalInstances();
 		if ((local != null) && Util.checkInstVisibilityExpression(local, toInst))
 			return false;
 		return true;
 	}
 
 	public static boolean isInheritedAttribute(String attr) {
 		if (isReservedAttributePrefix(attr))
 			return false;
 		for (String pred : CST.notInheritedAttribute) {
 			if (pred.equals(attr))
 				return false;
 		}
 		return true;
 	}
 
 	public static boolean isFinalAttribute(String attr) {
 		for (String pred : CST.finalAttributes) {
 			if (pred.equals(attr))
 				return true;
 		}
 		return false;
 	}
 
 	public static boolean isReservedAttributePrefix(String attr) {
 		for (String prefix : CST.reservedPrefix) {
 			if (attr.startsWith(prefix))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Check if attribute "attr=value" is valid when set on object "inst".
 	 * inst can be an instance, an implementation or a specification.
 	 * Check if the value is consistent with the type.
 	 * All predefined attributes are Ok (scope ...)
 	 * Cannot be a reserved attribute
 	 */
 	public static boolean validAttr(String component, String attr) {
 
 		if (Util.isFinalAttribute(attr)) {
 			logger.error("ERROR: in " + component + ", attribute\"" + attr + "\" is final");
 			return false;
 		}
 
 		if (Util.isReservedAttributePrefix(attr)) {
 			logger.error("ERROR: in " + component + ", attribute\"" + attr + "\" is reserved");
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * only string, int, boolean and enumerations attributes are accepted.
 	 * Return the value if it is correct.
 	 * For "int" returns an Integer object, otherwise it is the string "value"
 	 * 
 	 * @param value
 	 * @param type
 	 */
 	public static Object checkAttrType(String attr, String value, String type) {
 		if ((type == null) || (value == null))
 			return null;
 
 		if (type.equals("boolean")) {
 			if (value.equalsIgnoreCase(CST.V_TRUE) || value.equalsIgnoreCase(CST.V_FALSE)) 
 				return value ;
 			logger.error("Invalid attribute value \"" + value + "\" for attribute \"" + attr
 					+ "\".  Boolean value expected");
 			return null;						
 		}
 		if (type.equals("int") || type.equals("integer")) {
 			Set<String> values = Util.splitSet(value);
 			Integer valInt = null ;
 			try {
 				for (String val : values) {
 					valInt = Integer.parseInt(val);
 				}
 				return valInt;
 			} catch (Exception e) {
 				logger.error("Invalid attribute value \"" + value + "\" for attribute \"" + attr
 						+ "\".  Integer value(s) expected");
 				return null;
 			}
 		}
 
 		//        if ((type.charAt(0) == '{') || (type.charAt(0) == '[')) { // enumerated value
 		Set<String> enumVals = Util.splitSet(type); 
 
 		//A single value : it must be only "string"
 		if (enumVals.size() == 1) {
 			if (type.equals("string")) 
 				return value ;
 			logger.error("Invalid attribute type \"" + type + "\" for attribute \"" + attr);
 			return null ;
 		}
 
 		//It is an enumeration
 		Set<String> values = Util.splitSet(value);
 		if (enumVals.containsAll(values))
 			return value;
 
 		String errorMes = "Invalid attribute value(s) \"" + value + "\" for attribute \"" + attr
 		+ "\".  Expected subset of: " + type;
 		logger.error(errorMes);
 		return null;
 	}
 
 	public static String[] stringArrayTrim(String[] strings) {
 		String[] ret = new String[strings.length];
 		for (int i = 0; i < strings.length; i++) {
 			ret[i] = strings[i].trim();
 		}
 		return ret;
 	}
 
 	public static String toStringSetReference(Set<? extends ResourceReference> setRef) {
 		String ret = "{";
 		for (ResourceReference ref : setRef) {
 			ret = ret + ref.getJavaType() + ", ";
 		}
 		int i = ret.lastIndexOf(',');
 		ret = ret.substring(0, i);
 		return ret + "}";
 	}
 
 	public static boolean checkFilters(Set<String> filters, List<String> listFilters, Map<String, String> validAttr,
 			String comp) {
 		boolean ok = true;
 		if (filters != null) {
 			for (String f : filters) {
 				ApamFilter parsedFilter = ApamFilter.newInstance(f);
 				if (!parsedFilter.validateAttr(validAttr, f, comp))
 					ok = false;
 			}
 		}
 		if (listFilters != null) {
 			for (String f : listFilters) {
 				ApamFilter parsedFilter = ApamFilter.newInstance(f);
 				if (!parsedFilter.validateAttr(validAttr, f, comp))
 					ok = false;
 			}
 		}
 		return ok;
 	}
 
 	public static void printFileToConsole(URL path) throws IOException {
 		try {
 			DataInputStream in = new DataInputStream(path.openStream());
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				// Print the content on the console
 				System.out.println(strLine);
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 		}
 	}
 
 	///About dependencies
 	/**
 	 * Provided a client instance, checks if its dependency "clientDep", matches another dependency: "compoDep".
 	 * 
 	 * matches only based on same name (same resource or same component).
 	 * If client cardinality is multiple, compo cardinallity must be multiple too.
 	 * No provision for the client constraints or characteristics (missing, eager)
 	 * 
 	 * @param compoInst the composite instance containing the client
 	 * @param compoDep the dependency that matches or not
 	 * @param clientDep the client dependency we are trying to resolve
 	 * @return
 	 */
 	public static boolean matchDependency(Instance compoInst, DependencyDeclaration compoDep, DependencyDeclaration clientDep) {
 		boolean multiple = clientDep.isMultiple();
 		//Look for same dependency: the same specification, the same implementation or same resource name
 		//Constraints are not taken into account
 		//		for (DependencyDeclaration compoDep : compoDeps) {
 		if (compoDep.getTarget().getClass().equals(clientDep.getTarget().getClass())) { // same nature
 			if (compoDep.getTarget().equals(clientDep.getTarget())) {
 				if (!multiple || compoDep.isMultiple())
 					return true;
 			}
 		}
 
 		//Look for a compatible dependency.
 		//Stop at the first dependency matching only based on same name (same resource or same component)
 		//No provision for : cardinality, constraints or characteristics (missing, eager)
 		//		for (DependencyDeclaration compoDep : compoDeps) {
 		//Look if the client requires one of the resources provided by the specification
 		if (compoDep.getTarget() instanceof SpecificationReference) {
 			Specification spec = CST.apamResolver.findSpecByName(compoInst.getComposite().getCompType(),
 					((SpecificationReference) compoDep.getTarget()).getName());
 			if ((spec != null) && spec.getDeclaration().getProvidedResources().contains(clientDep.getTarget()))
 				if (!multiple || compoDep.isMultiple())
 					return true;
 		} else {
 			//If the composite has a dependency toward an implementation
 			//and the client requires a resource provided by that implementation
 			if (compoDep.getTarget() instanceof ImplementationReference) {
 				String implName = ((ImplementationReference<?>) compoDep.getTarget()).getName();
 				Implementation impl = CST.apamResolver.findImplByName(compoInst.getComposite().getCompType(), implName);
 				if (impl != null) {
 					//The client requires the specification implemented by that implementation
 					if (clientDep.getTarget() instanceof SpecificationReference) {
 						String clientReqSpec = ((SpecificationReference) clientDep.getTarget()).getName();
 						if (impl.getImplDeclaration().getSpecification().getName().equals(clientReqSpec))
 							if (!multiple || compoDep.isMultiple())
 								return true;
 					} else {
 						//The client requires a resource provided by that implementation
 						if (impl.getImplDeclaration().getProvidedResources().contains(clientDep.getTarget()))
 							if (!multiple || compoDep.isMultiple())
 								return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * Provided an instance, computes all the dependency declaration that applies to that instance.
 	 * If can be defined on the instance, the implementation, the specification, or on both.
 	 * For each dependency, we clone it, and we aggregate the constraints as found at all level, 
 	 * including the generic ones found in the composite type.
 	 * The dependencies returned are clones of the original ones.
 	 * 
 	 */
 	public static Set<DependencyDeclaration> computeAllEffectiveDependency (Instance client) {
 		if (client == null) return null ;
 		Set<DependencyDeclaration> allDeps = new HashSet <DependencyDeclaration> ();
 		for (DependencyDeclaration dep : computeAllDependencies (client)) {
 			allDeps.add(computeEffectiveDependency(client, dep.getIdentifier())) ;
 		}
 		return allDeps ;
 	}
 
 	/**
 	 * Provided an instance, computes all the dependency declaration that applies to that instance/
 	 * If can be defined on the instance, the implementation, the specification, or on both.
 	 * In case the same dependency is defined multiple time, it is the most concrete one that must be taken into account.
 	 * There is no attempt to compute all the constraints that apply on a given dependency; 
 	 * We are only interested in the target.
 	 * @param client
 	 * @return
 	 */
 	public static Set<DependencyDeclaration> computeAllDependencies (Instance client) {
 		Set<DependencyDeclaration> allDeps = new HashSet<DependencyDeclaration> () ;
 		allDeps.addAll(client.getDeclaration().getDependencies());
 
 		boolean found ;
 		for (DependencyDeclaration dep : client.getImpl().getDeclaration().getDependencies()) {
 			found= false ;
 			for (DependencyDeclaration allDep : allDeps) {
 				if (allDep.getIdentifier().equals(dep.getIdentifier())) {
 					found= true;
 					break ;
 				}
 			}
 			if (!found) allDeps.add(dep) ;
 		}
 		for (DependencyDeclaration dep : client.getSpec().getDeclaration().getDependencies()) {
 			found= false ;
 			for (DependencyDeclaration allDep : allDeps) {
 				if (allDep.getIdentifier().equals(dep.getIdentifier())) {
 					found= true;
 					break ;
 				}
 			}
 			if (!found) allDeps.add(dep) ;
 		}
 		return allDeps ;
 	}
 
 	/**
 	 * A dependency may have properties fail= null, wait, exception; exception = null, exception
 	 * A contextual dependency can also have hide: null, true, false and eager: null, true, false 
 	 * The most local definition overrides the others. 
 	 * Exception null can be overriden by an exception; only generic exception overrides another non null one.
 	 * 
 	 * @param dependency : the low level one that will be changed if needed
 	 * @param dep: the group dependency
 	 * @param generic: the dep comes from the composite type. It can override the exception, and has hidden and eager.
 	 * @return
 	 */
 	private static void overrideDepFlags (DependencyDeclaration dependency, DependencyDeclaration dep, boolean generic) {
 		//If set, cannot be changed by the group definition.
 		//NOTE: This strategy is because it cannot be compiled, and we do not want to make an error during resolution
 		if (dependency.getMissingPolicy() == null) {
 			dependency.setMissingPolicy(dep.getMissingPolicy()) ;
 		} 
 
 		if (dependency.getMissingException() == null || (generic && dep.getMissingException() != null)) {
 			dependency.setMissingException(dep.getMissingException()) ;
 		}
 
 		if (generic) {
 			dependency.setHide(dep.isHide()) ;
 			dependency.setEager(dep.isEager()) ;
 		} 
 	}
 
 	/**
 	 * The dependencies "depName" that apply on a client are those of the instance, plus those of the implem, 
 	 * plus those of the spec, and finally those in the composite. 
 	 * We aggregate the constraints as found at all level, including the generic one found in the composite type.
 	 * We compute also the dependency flags.
 	 * 
 	 * @param client
 	 * @param dependency
 	 * @return
 	 */
 	public static DependencyDeclaration computeEffectiveDependency (Instance client, String depName) {
 
 		//Find the first dependency declaration.
 		Component depComponent = client ;
 		//take the declaration declared at the most concrete level
 		DependencyDeclaration dependency = client.getApformInst().getDeclaration().getDependency(depName);
 		if (dependency == null) {
 			dependency = client.getImpl().getApformImpl().getDeclaration().getDependency(depName);
 			depComponent = client.getImpl() ;
 		}
 		//the dependency can be defined at spec level if implem is a composite
 		if (dependency == null) {
 			dependency = client.getSpec().getApformSpec().getDeclaration().getDependency(depName);
 			depComponent =client.getSpec();
 		}
 
 		if (dependency == null) return null ;
 
 		//Now compute the inheritance instance, Implem, Spec.
 		dependency = dependency.clone();
 		Component group = depComponent.getGroup() ;
 
 		while (group != null) {
 			for (DependencyDeclaration dep : group.getDeclaration().getDependencies()) {
 				if (dep.getIdentifier().equals(dependency.getIdentifier())) {
 					overrideDepFlags (dependency, dep, false);
 					dependency.getImplementationConstraints().addAll(dep.getImplementationConstraints()) ;
 					dependency.getInstanceConstraints().addAll(dep.getInstanceConstraints()) ;
 					dependency.getImplementationPreferences().addAll(dep.getImplementationPreferences()) ;
 					dependency.getInstancePreferences().addAll(dep.getInstancePreferences()) ;
 					break ;
 				}
 			}
 			group = group.getGroup() ;
 		}
 
 		//Add the composite generic constraints
 		CompositeType compoType = client.getComposite().getCompType() ;
 		Map<String, String> validAttrs = client.getValidAttributes() ;
 		for ( DependencyDeclaration  genDep  : compoType.getCompoDeclaration().getContextualDependencies()) {
 
 			if (matchGenericDependency(client, genDep, dependency)) {
 				overrideDepFlags (dependency, genDep, true) ;
 
 				if (Util.checkFilters(genDep.getImplementationConstraints(), null, validAttrs, client.getName())) {
 					dependency.getImplementationConstraints().addAll(genDep.getImplementationConstraints()) ;
 				}
 				if (Util.checkFilters(genDep.getInstanceConstraints(), null, validAttrs, client.getName())) {
 					dependency.getInstanceConstraints().addAll(genDep.getInstanceConstraints()) ;
 				}
 				if (Util.checkFilters(null, genDep.getImplementationPreferences(), validAttrs, client.getName())) {
 					dependency.getImplementationPreferences().addAll(genDep.getImplementationPreferences()) ;
 				}
 				if (Util.checkFilters(null, genDep.getInstancePreferences(), validAttrs, client.getName())) {
 					dependency.getInstancePreferences().addAll(genDep.getInstancePreferences()) ;
 				}
 			}
 		}
 		return dependency ;
 	}
 
 	/**
 	 * Provided a composite (compoInst), checks if the provided generic dependency constraint declaration
 	 * matches the compoClient dependency declaration.
 	 * 
 	 * @param compoInst the composite instance containing the client
 	 * @param genericDeps the dependencies of the composite: a regExpression
 	 * @param clientDep the client dependency we are trying to resolve.
 	 * @return
 	 */
 	public static boolean matchGenericDependency(Instance compoInst, DependencyDeclaration compoDep, DependencyDeclaration clientDep) {
 
 		String pattern = compoDep.getTarget().getName() ;
 		//Look for same dependency: the same specification, the same implementation or same resource name
 		//Constraints are not taken into account
 		if (compoDep.getTarget().getClass().equals(clientDep.getTarget().getClass())) { // same nature
 			if (clientDep.getTarget().getName().matches(pattern)) {
 				return true;
 			}
 		}
 
 		//If the client dep is an implementation dependency, check if the specification matches the pattern
 		if (compoDep.getTarget() instanceof SpecificationReference) {
 			if (clientDep.getTarget() instanceof ImplementationReference) {
 				String implName = ((ImplementationReference<?>) clientDep.getTarget()).getName();
 				Implementation impl = CST.apamResolver.findImplByName(compoInst.getComposite().getCompType(), implName);
 				if (impl != null && impl.getSpec().getName().matches(pattern)) {
 					return true ;
 				}
 			}
 		}
 
 		return false;
 	}
 
     public static String toStringUndefinedResource(Set<UndefinedReference> undefinedReferences) {
         if ((undefinedReferences == null) || (undefinedReferences.size() == 0))
             return null;
         String ret = "{";
         for (UndefinedReference undfinedReference : undefinedReferences) {
             ret += undfinedReference.getSubject() + ", ";
         }
         return ret.substring(0, ret.length() - 2) + "}";
     }
 
 }
