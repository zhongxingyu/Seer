 package fr.imag.adele.apam.util;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.AttrType;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Relation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.impl.InstanceImpl;
 
 
 /*
  * 
  * Syntax of substitution is : subst == [prefix "+"][sourceName]["." depId] "$" attr ["+" Suffix]
  * 
  */
 
 public class Substitute {
 	//	static Substitute s = new Substitute () ;
 	private final static Logger	logger = LoggerFactory.getLogger(Substitute.class);
 
 
 	public static class SplitSub {
 		public String attr ;
 		public String sourceName ;
 		public List<String> depIds ;
 		public String prefix ;
 		public String suffix ;
 
 		public String toString() {
 			StringBuffer ret = new StringBuffer ("");
 			if (prefix != null) 
 				ret.append(prefix + "+") ;
 			if (sourceName != null) 
 				ret.append(sourceName) ;
 			if (depIds != null) {
 				for (String depId : depIds)
 					ret.append ("." + depId) ;
 			}
 			ret.append("$" + attr) ;
 			if (suffix != null) 
 				ret.append("+" + suffix) ;
 			return ret.toString() ;
 		}
 		/**
 		 * 	Stores the differents elements, and separate the prefix and suffix from source and attr
 		 * 
 		 *  Syntax is : subst == [prefix "+"][sourceName]["." depId] "$" attr ["+" Suffix]
 		 *
 		 * @param attrSub : cannot be null
 		 * @param sourceName can be null
 		 * @param depId can be null
 		 */
 		public SplitSub (String attrSub, String sourceName, String depId) {
 			//separate the prefix and suffix from source and attr
 
 			int s = attrSub.indexOf('+') ;
 			if (s == 0) { // invalid : attrsub = "+azer"
 				attr = "" ;
 				suffix = attrSub.substring(1) ;
 			}
 			if (s >0) {
 				attr  = attrSub.substring(0, s) ;
 				suffix = attrSub.substring(s+1) ;
 			}
 			else {
 				this.attr = attrSub ;
 			}
 
 			//Compute the navigation
 			if (depId == null || depId.isEmpty())
 				this.depIds = null ;
 			else {
 				depIds = new ArrayList<String> ();
 				while (depId != null) {
 					s= depId.indexOf('.') ;
 					if (s < 0) { //It is the last one
 						depIds.add(depId) ;
 						depId = null ; 
 					}
 					else {
 						depIds.add(depId.substring(0, s)) ;
 						depId = depId.substring(s+1) ; 						
 					}
 				}
 			}
 
 			s = sourceName.indexOf('+') ;			
 			if (s < 1) { // invalid : source = "+azer" or "azert"
 				prefix = null ;
 			} else {
 				prefix = sourceName.substring(0, s) ;
 			}
 			this.sourceName = sourceName.substring(s+1) ;
 			if (this.sourceName.isEmpty())
 				this.sourceName = "this" ;
 		}
 	}
 
 	/**
 	 * Value is a substitution, with syntax $[source[.depIds]]"$"Attr
 	 * Only split in three parts "source", "depIds" and "attr"
 	 *  
 	 * @param value a string to substitute
 	 * @return a SplitSub object the three elements. Null is not a substitution
 	 */
 	public static SplitSub split (String value) {
 		if (value.charAt(0) != '$')
 			return null ;
 
 		//eliminate first "$"
 		value = value.substring(1);
 
 		//If no "$Attr" it is invalid
 		int i = value.indexOf('$') ;
 		if (i==-1) 
 			return null ;
 
 		String attr = value.substring(i+1) ;
 		if (i==0) { //no source, no navigation "$$Attr"
 			return new SplitSub (attr,"this", null) ;
 		}
 
 		String	prefix = value.substring(0, i) ;
 		int j = prefix.indexOf('.') ;
 		if (j==-1) 
 			return new SplitSub (attr, prefix, null) ;
 		if (j == 0)
 			return new SplitSub(attr, "this", prefix.substring(1)) ;
 		return new SplitSub(attr, prefix.substring(0, j), prefix.substring(j+1)) ;
 	}
 
 	public static boolean isSubstitution (Object value) {
 		return ((value instanceof String) && (((String)value).charAt(0)=='$' || ((String)value).charAt(0)=='@')) ;
 	}
 
 	/**
 	 * 
 	 * @param sourceType
 	 * @param targetType
 	 * @param attr
 	 * @param sub
 	 * @return
 	 */
 	public static boolean checkSubType (AttrType sourceType, AttrType targetType, String attr, SplitSub sub) {
 
 		/*
 		 * We are in a filter, source is the target, and the real source is ignored.
 		 * Validity has been checked at compile time  
 		 */		
 		if (sourceType == null)
 			return true ;
 		if (targetType==null) 
 			return false ;
 
 
 		if (sub.prefix!= null || sub.suffix != null) { //The result is a string, not an enumeration
 			if (sourceType.type==AttrType.STRING) {
 				return true ;
 			}
 			logger.error("Attribute " + attr +  " of type " + sourceType.typeString + " : invalid substitution with string \"" + sub + "\". Attribute " 
 					+  sub.attr + " of type : " + targetType.typeString ) ;
 			return false ;
 		}
 
 
 		if (sourceType.type!=targetType.type) {
 			logger.error("Attribute " + attr +  " of type " + sourceType.typeString + " : invalid substitution with \"" + sub + "\". Attribute " 
 					+  sub.attr + " of type : " + targetType.typeString ) ;
 			return false ;
 		}
 
		if (sourceType.type==AttrType.ENUM && !sourceType.enumValues.containsAll(targetType.enumValues)) {
 			logger.error("Attribute " + attr +  " of type " + sourceType.typeString + " : Not the same enumeration set as attribute " 
 					+  sub.attr + " of type : " + targetType.typeString ) ;
 			return false ;
 		}
 
 		return (sourceType.isSet || !targetType.isSet) ;
 	}
 
 
 	private static String concatSub (SplitSub sub, String val) {
 		String ret = val ;
 		if (sub.prefix != null) {
 			ret = sub.prefix + ret ;
 		}
 		if (sub.suffix != null) {
 			ret =  ret + sub.suffix;				
 		}
 		return ret ;
 	}
 
 	/**
 	 * Return the value of attribute "attr" of component source, if the types are compatibles.
 	 * Types are compatible if the type of "attr" and "typeAttr" are equal.
 	 * @param source
 	 * @param attr
 	 * @param sourceTypeAttr
 //	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private static Object checkReturnSub (Component source, SplitSub sub, String sourceAttr, AttrType sourceTypeAttr) {
 
 		AttrType t = source.getAttrType (sub.attr) ;
 		if (t == null) return null ;
 
 		if (!checkSubType (sourceTypeAttr, t, sourceAttr, sub)) {
 			return null ;
 		}
 
 		Object ret = source.getPropertyObject(sub.attr) ;
 		if (ret == null) 
 			return null ;
 
 		if (t.type == AttrType.INTEGER) {
 			return ret ;				
 		} 
 		if (t.isSet) {
 			Set<String> retCol = new HashSet<String> () ;
 			for (String s : (Set<String>)ret) {
 				retCol.add(concatSub(sub, s)) ; 
 			}
 			return retCol ;
 		}
 		if (ret instanceof String) {
 			return concatSub(sub, (String)ret) ;
 		}
 		return ret ;
 	}
 
 	public static Object functionSubstitute (String attr, String value, Component source) {
 		if (!(source instanceof Instance)) {
 			logger.error("Invalid function substitution. " + source.getName() + " is not an instance.") ;
 		}
 		String func = value.substring(1);
 		Class<?> c = ((InstanceImpl)source).getServiceObject().getClass();			    
 
 		try {
 			Method[] allMethods = c.getDeclaredMethods();
 			for (Method m : allMethods) {
 				if ( m.getName().equals(func)) {
 					Object ret = m.invoke(((InstanceImpl)source).getServiceObject(), (Instance)source) ;
 					return ret ;
 				}
 			}
 			logger.error("Not found method " + func + " in class " + c.getCanonicalName() + " of instance " + source) ;
 			return null ;
 		} catch (Exception e) {
 			logger.error("Invalid invoke on " + func + " in class " + c.getCanonicalName() + " of instance " + source) ;
 			e.printStackTrace() ;
 		}
 		return null ;
 	}
 
 
 	/**
 	 * Provided that component sources has an attribute "attr=value", with value a meta-substitution, 
 	 * returns the value after the substitution.
 	 * 
 	 * If not a substitution returns the value as is.
 	 * If the substitution fails, returns null.
 	 * 
 	 * Syntax is : subst == [prefix "+"][sourceName][{"." depId}] "$" attr ["+" Suffix]
 	 * 
 	 * The depId element is valid only if source is an instance.
 	 * @param attr 
 	 * @param value
 	 * @param source the component that has the "attr=value" property
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static Object substitute (String attr, Object valueObject, Component source) {
 
 		/*
 		 * No substitution cases
 		 */
 		if (source == null || (valueObject == null) || (!(valueObject instanceof String))) 
 			return valueObject ;
 
 		String value = (String)valueObject ;
 		if (value.startsWith("\\$") || value.startsWith("\\@")) 
 			return value.substring (1) ;
 
 		if (value.isEmpty() || (value.charAt(0) != '$' && value.charAt(0) != '@' )) 
 			return valueObject ;
 
 		/*
 		 * Substitution needed
 		 */
 		//A function to call
 		if (value.charAt(0) == '@')
 			return functionSubstitute(attr, value, source) ;
 
 		/*
 		 * It is a meta substitution
 		 */
 		AttrType st = null ;
 		// If attr is null, it is because it is a substitution in a filter. Source is currently the target ! Do no check the attr
 		if (attr != null) {
 			st = source.getAttrType(attr) ;
 		}
 
 		SplitSub sub = split (value) ;
 		if (sub == null) return value ;
 
 		if (!sub.sourceName.equals("this")) {
 			//Look for the source component
 			source = CST.apamResolver.findComponentByName(source, sub.sourceName) ;
 			if (source == null) {
 				logger.error("Component " + sub.sourceName + " not found in substitution : " + value + " of attribute " + attr) ;
 				return null ;
 			}
 		}  
 
 		if (sub.depIds == null) {
 			return checkReturnSub (source, sub, attr, st) ;
 		}
 
 		/*
 		 *  look for the navigation from the source component
 		 */
 		Set<Component> dest = navigate (source, sub.depIds) ;
 
 		if (dest.size() == 1) {
 			return checkReturnSub (dest.iterator().next(), sub, attr, st) ;
 		}
 
 		/*
 		 * We have more than one source; TypeAttr must be a set, and each destination must be either a singleton of the right type, 
 		 * or the same type of Set.
 		 * We are building a collection with all the values.
 		 * 
 		 */
 		if (!st.isSet) {
 			logger.error("Invalid type for attribute " + attr 
 					+ " It must be a set for a multiple relation or navigation" + sub.depIds);
 			return null ;
 		}
 
 		if (st.type== AttrType.INTEGER) {
 			Set<Integer> retSetInt = new HashSet <Integer> () ;
 			for (Component d : dest) {
 				Object oneVal =  checkReturnSub (d, sub, attr, st) ;
 				if (oneVal != null) {
 					if (oneVal instanceof Set) {
 						retSetInt.addAll((Set<Integer>)oneVal) ;
 					} else {
 						retSetInt.add((Integer)oneVal) ;
 					}
 				}
 			}
 			return retSetInt ;
 		}
 
 		Set<String> retSetString = new HashSet <String> () ;
 		for (Component d : dest) {
 			Object oneVal =  checkReturnSub (d, sub, attr, st) ;
 			if (oneVal != null) {
 				if (oneVal instanceof Set) {
 					retSetString.addAll((Set<String>)oneVal) ;
 				} else {
 					retSetString.add((String)oneVal) ;
 				}
 			}
 		}
 		return retSetString ;
 	}
 
 	/**
 	 * From an unique component source, and a list of relation names, compute the set of components reached by that navigation.
 	 * 
 	 * @param firstSource
 	 * @param navigation
 	 * @return
 	 */
 	private static Set<Component> navigate (Component firstSource, List<String> navigation) {
 		//Compute the navigation. It returns a set of components
 		Relation depDcl ;
 		Set<Component> dests   = new HashSet <Component> ();
 
 		Set<Component> sources = new HashSet <Component> ()  ;
 		sources.add(firstSource) ;
 		if (navigation == null || navigation.isEmpty())
 			return sources ;
 		
 		Set<Component> tempDests ;
 
 		for (String depId : navigation ) {
 			for (Component source : sources) {
 				depDcl =  source.getRelation (depId) ;
 				if (depDcl == null && !CST.isFinalRelation(depId)) {
 					logger.error("relation " + depId + " undefined for component " + source.getName());
 				} else {
 					tempDests = source.getLinkDests(depId) ;
 					if (tempDests.isEmpty()) {
 						logger.debug("relation id " + depId + " not resolved for component " + source.getName());
 					} else
 						dests.addAll (tempDests) ; //never null
 				}
 			}
 			if (dests.isEmpty()) return dests ; //no solution
 			
 			//Go one step further
 			sources.clear();
 			sources.addAll(dests) ;
 			dests.clear () ;
 		}
 		
 		return sources ;
 	}
 	/**
 	 * Transforms a list of constraint in string, into a list of filters, and substitute the values if needed
 	 * @param filterString
 	 * @param component
 	 * @return
 	 */
 	public static List<ApamFilter> toFiltersSubst (List<String> filterString, Component component) {
 		if (component == null)
 			return null ;
 
 		List<ApamFilter> ret = new ArrayList<ApamFilter> () ;
 		if (filterString == null || filterString.isEmpty())
 			return ret ;
 		for (String sf : filterString) {
 			try {
 				ret.add(ApamFilter.newInstanceApam(sf, component)) ;
 			}
 			catch (Exception e) {
 				logger.error("Invalif filter " + sf + " for component " + component.getName()) ;
 			}
 		}
 		return ret ;
 	}
 
 
 	/**
 	 * Transforms a list of constraints in string, into a list of filters, and substitute the values if needed
 	 * @param filterString
 	 * @param component
 	 * @return
 	 */
 	public static Set<ApamFilter> toFiltersSubst (Set<String> filterString, Component component) {
 		if (component == null)
 			return null ;
 
 		Set<ApamFilter> ret = new HashSet<ApamFilter> () ;
 		for (String sf : filterString) {
 			try {
 				ret.add(ApamFilter.newInstanceApam(sf, component)) ;
 			}
 			catch (Exception e) {
 				logger.error("Invalif filter " + sf + " for component " + component.getName()) ;
 			}
 		}
 		return ret ;
 	}
 }
 
