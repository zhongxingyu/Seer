 package com.shivanshusingh.pluginanalyser.utils.parsing;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * 
  * has the utility and helper functions that help in parsing extract and other files in the system.
  * @author Shivanshu Singh
  *
  */
 public class ParsingUtil {
 
 	/**
 	 * @param entry
 	 * @param property
 	 * @return {@link Set}
 	 * @throws IOException
 	 */
 	public static Set<String> restorePropertyFromExtract(File entry, String property) throws IOException {
 	
 		property = property.trim();
 		Set<String> result = new HashSet<String>();
 		BufferedReader br = new BufferedReader(new FileReader(entry));
 		String line = "";
 		while (null != (line = br.readLine())) {
 	
 			line = line.replace("\n", "").trim();
 			if (property.equalsIgnoreCase(line)) {
 				// found the property.
 				// now push all the entries into the set.
 				while (null != (line = br.readLine())) {
 					line = line.replace("\n", "").trim();
 					if (Constants.MARKER_TERMINATOR.equalsIgnoreCase(line)) {
 						break;
 					}
 					result.add(line);
 				}
 				break;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * separates the function class from function name. e.g. if the input is:
 	 * org.s.G com.x.A.foo () returned: classAndFuncName[0]=org.s.G
 	 * classAndFuncName[1]=com.x.A classAndFuncName[3]=foo ()
 	 * 
 	 * @param funcSignature
 	 * @return
 	 */
 	public static String[] separateFuncNameElements(String funcSignature) {
 		String[] classAndFuncName = new String[3];
 		String[] spaceSplits = funcSignature.split(" ");
 	
 		// returnType
 		classAndFuncName[0] = spaceSplits[0].trim();
 	
 		String[] dotSplits = spaceSplits[1].trim().split("\\.");
 	
 		// function name and parameters
 		classAndFuncName[2] = dotSplits[dotSplits.length - 1].trim() + " " + spaceSplits[2].trim();
 	
 		// class name
 		if(2<=dotSplits.length)
 			classAndFuncName[1] = dotSplits[0].trim();
 		else
 			classAndFuncName[1]=    "";
 		
 		for (int x = 1; x < dotSplits.length - 1; x++)
 			classAndFuncName[1] += "." + dotSplits[x].trim();
 	
 		return classAndFuncName;
 	}
 
 	/**
 	 * constructs a function signature from the  funcElements[] array where funcElements[0] is the return type funcElements[1] is the class and funcElements[2] is the function name only without class or return type but will the parameters part.
 	 * @param funcElements
 	 * @return
 	 */
 	public static String reconstructFuncSignature(String[] funcElements) {
 		String signature = "";
 		if (null != funcElements && 3 == funcElements.length) {
 			signature += funcElements[0].trim() + " " + funcElements[1].trim() + "." + funcElements[2].trim();
 		}
 		return signature;
 	}
 
 	/**
 	 * get the bundle property name from a bundle property entry by stripping any version or other information
 	 * 
 	 * @param bundleEntry
 	 * @return
 	 */
 	public static String getBundlePropertyNameFromBundleEntry(String bundleEntry) {
 		// getting the class / package name from the
 		// bundle export entry.
 		// getting split on ";" first.
 		bundleEntry = bundleEntry.split(";")[0].trim();
 		bundleEntry = bundleEntry.split(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL)[0].trim();
 		return bundleEntry;
 	}
 
 	/**
 	 * return 1 is the input is a function entry , 2 in case of a type entry and
 	 * 0 otherwise.
 	 * 
 	 * @param imp
 	 * @return
 	 */
 	public static int getEntryType(String str) {
 		if (null == str || "".equalsIgnoreCase(str.trim()))
 			return 0;
 		str = str.trim();
 		String[] splits = str.split(" ");
 		if (splits.length >= 3)
 			return 1;
 		else
 	
 			return 2;
 	}
 
 }
