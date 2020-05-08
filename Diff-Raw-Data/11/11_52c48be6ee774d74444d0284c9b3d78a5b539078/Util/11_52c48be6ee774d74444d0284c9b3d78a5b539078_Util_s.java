 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.util;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
import org.apache.felix.ipojo.metadata.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.declarations.ComponentDeclaration;
 import fr.imag.adele.apam.declarations.RelationDeclaration;
 import fr.imag.adele.apam.declarations.ResourceReference;
 import fr.imag.adele.apam.declarations.UndefinedReference;
import fr.imag.adele.apam.util.CoreParser.ErrorHandler;
 
 /**
  * The static Class Util provides a set of static method for the iPOJO service concrete machine.
  *
  * @author SAM team
  */
 public final class Util {
 	private static Logger logger = LoggerFactory.getLogger(Util.class);
 
 	//cannot be instantiated
 	private Util() {
 	};
 
 
 	
 	/*
 	 * =============== String manipulation =============//
 	 */
 
 	/**
 	 * takes a list of string "A" "B" "C" ... and produces "{A, B, C, ...}"
 	 *
 	 * @param names
 	 * @return
 	 */
 	public static String toStringResources(Set<String> names) {
 		if ((names == null) || (names.size() == 0)) return null;
 
 		StringBuffer ret = new StringBuffer () ;
 		ret.append("{");
 		for (String name : names) {
 			ret.append(name + ", ");
 		}
 		return ret.toString().substring(0, ret.length() - 2) + "}";
 	}
 
 	public static String toStringArrayString(String[] names) {
 		return toStringResources(new HashSet<String>(Arrays.asList(names)));
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
 	 * A list is of the form "{A, B, .... G}" or simply "A, B, .... G"
 	 * Ignores spaces around commas, and around braces.
 	 *
 	 * If the string is empty or empty list ("{}"), return the empty array.
 	 *
 	 * @param str
 	 * @return a string array. Never null, but can be length 0
 	 */
 	public static String[] split(String str) {
 		if ((str == null) || (str.length() == 0)) {
 			return new String[0];
 		}
 
 		str = str.trim();
 		if  (str.length() == 0){
 			return new String[0];
 		}
 
 		//It is an explicit set. Remove braces.
 		if (str.charAt(0) == '{')  {
 			if (str.charAt(str.length() - 1) != '}') {
 				logger.error("Invalid string. \"}\" missing: " + str) ;
 			}
 			str = (str.substring(1, str.length() - 1)).trim();
 			//It was an empty set ("{}"
 			if (str.length() == 0) {
 				return new String[0];
 			}
 		}
 
 		//It is a simple set of values or a singleton
 		return stringArrayTrim(str.split(","));
 	}
 
 
 	/**
 	 * Transforms an array of string in a string list in the Ldap format 
 	 * @param value
 	 * @return
 	 */
 	public static String stringArray2String (String[] value) {
 		StringBuffer sVal = new StringBuffer () ;
 		int lv = ((String[])value).length;
 		for (int i=0; i < lv ; i++) {
 			sVal.append (((String[])value)[i]) ; 
 			if (i < lv -1) {
 				sVal.append(", ") ;
 			}
 		}
 		return sVal.toString() ;
 	}
 
 	/**
 	 * Transforms an array of string in a string list in the Ldap format 
 	 * @param value
 	 * @return
 	 */
 	public static String stringSet2String (Set<String> values) {
 		StringBuffer sVal = new StringBuffer () ;
 		int lv = values.size();
 		int i = 0 ;
 		for (String val : values) {
 			sVal.append (val) ; 
 			if (i < lv -1) {
 				sVal.append(", ") ;
 			}
 			i++ ;
 		}
 		return sVal.toString() ;
 	}
 
 	/**
 	 * Transforms an atytribute value into a string.
 	 * If a set of String, put it into the LDAP form "a, b, c, d"
 	 * If Integer, transform in String
 	 * @param value
 	 * @return
 	 */
 	@SuppressWarnings ("unchecked")
 	public static String toStringAttrValue (Object value) {
 		if (value == null)            return null ;
 		if (value instanceof Integer) return value.toString() ;
 		if (value instanceof Boolean) return value.toString() ;
 		if (value instanceof String)  return (String)value ;
 		return Util.stringSet2String((Set<String>)value);
 
 	}
 	/**
 	 * Transforms an array of int in a string list in the Ldap format 
 	 * @param value
 	 * @return
 	 */
 	public static String intArray2String (int[] value) {
 		StringBuffer sVal = new StringBuffer () ;
 		int lv = ((int[])value).length;
 		for (int i=0; i < lv ; i++) {
 			sVal.append (Integer.toString(((int[])value)[i]) ); 
 			if (i < lv -1) {
 				sVal.append(", ") ;
 			}
 		}
 		return sVal.toString() ;
 	}
 
 
 
 
 	public static String[] stringArrayTrim(String[] strings) {
 		String[] ret = new String[strings.length];
 		for (int i = 0; i < strings.length; i++) {
 			ret[i] = strings[i].trim();
 		}
 		return ret;
 	}
 
 	public static String toStringSetReference(Set<? extends ResourceReference> setRef) {
 		StringBuffer ret = new StringBuffer () ;
 		ret.append("{");
 		for (ResourceReference ref : setRef) {
 			ret.append(ref.getJavaType() + ", ");
 		}
 		String rets = ret.toString() ;
 		int i = rets.lastIndexOf(',');
 		return rets.substring(0, i) + "}";
 	}
 
 	public static void printFileToConsole(URL path) throws IOException {
 		DataInputStream in = null ;
 		BufferedReader br = null ;
 		try {
 			in = new DataInputStream(path.openStream());
 			br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				// Print the content on the console
 				System.out.println(strLine);
 			}
 		} catch (Exception e) {// Catch exception if any
 		} finally {
 			// Close the input stream in all cases
 			if (in != null) in.close();
 			if (br != null) br.close();
 		}
 	}
 
 
 	public static Set<Instance> getSharableInsts(Instance client, Set<Instance> validInsts) {
 		if (validInsts == null) return null ;
 		Set<Instance> ret = new HashSet<Instance> () ;
 
 		for (Instance inst : validInsts) {
 			if (inst.isSharable()) {
 				ret.add(inst) ;
 			}
 		}
 		return ret ;
 	}
 
 	public static String toStringUndefinedResource(Set<UndefinedReference> undefinedReferences) {
 		if ((undefinedReferences == null) || (undefinedReferences.size() == 0)) {
 			return null;
 		}
 		StringBuffer ret = new StringBuffer() ; 
 		ret.append("{");
 		for (UndefinedReference undfinedReference : undefinedReferences) {
 			ret.append(undfinedReference.getSubject() + ", ");
 		}
 		return ret.substring(0, ret.length() - 2) + "}";
 	}
 
 	/**
 	 * Provided a source component, checks if the provided override relation 
 	 * matches the compoClient relation declaration.
 	 * Tags source and sourceType are mandatory. 
 	 * To be applied on a component C, the override must be such that :
 	 * 		id matches the override id
 	 *      source must be the name of C or of an ancestor of C.
 	 * 		target must be the same type (resource of component, and its name must match). 
 	 *
 	 * @param source the source of the relation
 	 * @param overDep the dependencies of the composite: a regExpression
 	 * @param sourceDep the client relation we are trying to resolve.
 	 * @return
 	 */
	public static boolean matchOverrideRelation(Component source, RelationDeclaration overDep, RelationDeclaration sourceDep) {
 
 		//Check if Ids are compatible
 		if (! sourceDep.getIdentifier().matches(overDep.getIdentifier()))
 			return false ;
 		
 		//Check if overDep source is source or one of its ancestors
 		Component group = source ;
 		boolean found = false ;
 		while (group != null) {
			if (overDep.getSourceName() == null || sourceDep.getSourceName().matches(overDep.getSourceName())) {
 				found = true;
 				break ;
 			}
 			group = group.getGroup();
 		}
 		
 		if (!found) 
 			return false ;
 		
 		/*
 		 * Check if targets are compatible
 		 * Same target: the same specification, the same implementation or same resource name with a matching
 		 */
 		String pattern = overDep.getTarget().getName() ;
 		// same nature: direct comparison
 		if (overDep.getTarget().getClass().equals(sourceDep.getTarget().getClass())
 				&& (sourceDep.getTarget().getName().matches(pattern))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * A relation may have properties fail= null, wait, exception; exception = null, exception
 	 * A contextual relation can also have hide: null, true, false and eager: null, true, false
 	 * The most local definition overrides the others.
 	 * Exception null can be overriden by an exception; only generic exception overrides another non null one.
 	 *
 	 * @param relation : the low level one that will be changed if needed
 	 * @param dep: the group relation
 	 * @param generic: the dep comes from the composite type. It can override the exception, and has hidden and eager.
 	 * @return
 	 */
 	public static void overrideDepFlags (RelationDeclaration relation, RelationDeclaration dep, boolean generic) {
 		//If set, cannot be changed by the group definition.
 		//NOTE: This strategy is because it cannot be compiled, and we do not want to make an error during resolution
 		if (relation.getMissingPolicy() == null || (generic && dep.getMissingPolicy() != null)) {
 			relation.setMissingPolicy(dep.getMissingPolicy()) ;
 		}
 
 		if (relation.getMissingException() == null || (generic && dep.getMissingException() != null)) {
 			relation.setMissingException(dep.getMissingException()) ;
 		}
 
 		if (generic) {
 			relation.setHide(dep.isHide()) ;
 			relation.setEager(dep.isEager()) ;
 		}
 	}
 
 	
 }
