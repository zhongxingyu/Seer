 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jdt.internal.debug.core.logicalstructures;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.ILogicalStructureProvider;
 import org.eclipse.debug.core.ILogicalStructureType;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.jdt.debug.core.IJavaClassType;
 import org.eclipse.jdt.debug.core.IJavaInterfaceType;
 import org.eclipse.jdt.debug.core.IJavaObject;
 import org.eclipse.jdt.debug.core.IJavaType;
 import org.eclipse.jdt.debug.core.JDIDebugModel;
 import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
 
 public class JavaLogicalStructures implements ILogicalStructureProvider {
 	
 	// preference values
 	static final char IS_SUBTYPE_TRUE= 'T';
 	static final char IS_SUBTYPE_FALSE= 'F';
 	
 	/**
 	 * The list of java logical structures.
 	 */
 	private static Map fJavaLogicalStructureMap;
 
 	/**
 	 * The list of java logical structures in this Eclipse install.
 	 */
 	private static List fPluginContributedJavaLogicalStructures;
 	
 	/**
 	 * The list of java logical structures defined by the user.
 	 */
 	private static List fUserDefinedJavaLogicalStructures;
 	
 	/**
 	 * Get the logical structure from the extension point and the preference store,
 	 * and initialize the map.
 	 */
    static {
 		initPluginContributedJavaLogicalStructure();
 		initUserDefinedJavaLogicalStructures();
 		initJavaLogicalStructureMap();
 	}
     
     private static void initJavaLogicalStructureMap() {
     	fJavaLogicalStructureMap= new HashMap();
     	addAllLogicalStructures(fPluginContributedJavaLogicalStructures);
 		addAllLogicalStructures(fUserDefinedJavaLogicalStructures);
     }
 	
 	/**
 	 * @param pluginContributedJavaLogicalStructures
 	 */
 	private static void addAllLogicalStructures(List pluginContributedJavaLogicalStructures) {
 		for (Iterator iter= pluginContributedJavaLogicalStructures.iterator(); iter.hasNext();) {
 			addLogicalStructure((JavaLogicalStructure) iter.next());
 		}
 	}
 
 	/**
 	 * @param structure
 	 */
 	private static void addLogicalStructure(JavaLogicalStructure structure) {
 		String typeName= structure.getQualifiedTypeName();
 		List logicalStructure= (List)fJavaLogicalStructureMap.get(typeName);
 		if (logicalStructure == null) {
 			logicalStructure= new ArrayList();
 			fJavaLogicalStructureMap.put(typeName, logicalStructure);
 		}
 		logicalStructure.add(structure);
 	}
 
 	/**
 	 * Get the configuration elements for the extension point.
 	 */
 	private static void initPluginContributedJavaLogicalStructure() {
 		fPluginContributedJavaLogicalStructures= new ArrayList();
 		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.EXTENSION_POINT_JAVA_LOGICAL_STRUCTURES);
 		IConfigurationElement[] javaLogicalStructureElements= extensionPoint.getConfigurationElements();
 		for (int i= 0; i < javaLogicalStructureElements.length; i++) {
 			try {
 				fPluginContributedJavaLogicalStructures.add(new JavaLogicalStructure(javaLogicalStructureElements[i]));
 			} catch (CoreException e) {
 				JDIDebugPlugin.log(e);
 			}
 		}
 	}
 	
 	/**
 	 * Get the user defined logical structures (from the preference store).
 	 */
 	static private void initUserDefinedJavaLogicalStructures() {
 		fUserDefinedJavaLogicalStructures= new ArrayList();
 		String logicalStructuresString= JDIDebugModel.getPreferences().getString(JDIDebugModel.PREF_JAVA_LOGICAL_STRUCTURES);
 		StringTokenizer tokenizer= new StringTokenizer(logicalStructuresString, "\0", true); //$NON-NLS-1$
 		while (tokenizer.hasMoreTokens()) {
 			String type= tokenizer.nextToken();
 			tokenizer.nextToken();
 			String description= tokenizer.nextToken();
 			tokenizer.nextToken();
 			String isSubtypeValue= tokenizer.nextToken();
 			boolean isSubtype= isSubtypeValue.charAt(0) == IS_SUBTYPE_TRUE;
 			tokenizer.nextToken();
 			String value= tokenizer.nextToken();
 			if (value.charAt(0) == '\0') {
 				value= null;
 			} else {
 				tokenizer.nextToken();
 			}
 			String variablesCounterValue= tokenizer.nextToken();
 			int variablesCounter= Integer.parseInt(variablesCounterValue);
 			tokenizer.nextToken();
 			String[][] variables= new String[variablesCounter][2];
 			for (int i= 0; i < variablesCounter; i++) {
 				variables[i][0]= tokenizer.nextToken();
 				tokenizer.nextToken();
 				variables[i][1]= tokenizer.nextToken();
 				tokenizer.nextToken();
 			}
 			fUserDefinedJavaLogicalStructures.add(new JavaLogicalStructure(type, isSubtype, value, description, variables, false));
 		}
 	}
 	
 	/**
 	 * Save the user defined logical structures in the preference store.
 	 */
 	static private void saveUserDefinedJavaLogicalStructures() {
 		StringBuffer logicalStructuresString= new StringBuffer();
 		for (Iterator iter= fUserDefinedJavaLogicalStructures.iterator(); iter.hasNext();) {
 			JavaLogicalStructure logicalStructure= (JavaLogicalStructure) iter.next();
 			logicalStructuresString.append(logicalStructure.getQualifiedTypeName()).append('\0');
 			logicalStructuresString.append(logicalStructure.getDescription()).append('\0');
 			logicalStructuresString.append(logicalStructure.isSubtypes() ? IS_SUBTYPE_TRUE : IS_SUBTYPE_FALSE).append('\0');
 			String value= logicalStructure.getValue();
 			if (value != null) {
 				logicalStructuresString.append(value);
 			}
 			logicalStructuresString.append('\0');
 			String[][] variables= logicalStructure.getVariables();
 			logicalStructuresString.append(variables.length).append('\0');
 			for (int i= 0; i < variables.length; i++) {
 				String[] strings= variables[i];
 				logicalStructuresString.append(strings[0]).append('\0');
 				logicalStructuresString.append(strings[1]).append('\0');
 			}
 		}
 		JDIDebugModel.getPreferences().setValue(JDIDebugModel.PREF_JAVA_LOGICAL_STRUCTURES, logicalStructuresString.toString());
 	}
 
 	/**
 	 * Return all the defined logical structures.
 	 */
 	static public JavaLogicalStructure[] getJavaLogicalStructures() {
 		JavaLogicalStructure[] logicalStructures= new JavaLogicalStructure[fPluginContributedJavaLogicalStructures.size() + fUserDefinedJavaLogicalStructures.size()];
 		int i= 0;
 		for (Iterator iter= fPluginContributedJavaLogicalStructures.iterator(); iter.hasNext();) {
 			logicalStructures[i++]= (JavaLogicalStructure) iter.next();
 		}
 		for (Iterator iter= fUserDefinedJavaLogicalStructures.iterator(); iter.hasNext();) {
 			logicalStructures[i++]= (JavaLogicalStructure) iter.next();
 		}
 	    return logicalStructures;
 	}
 
 	/**
 	 * Set the user defined logical structures.
 	 */
 	static public void setUserDefinedJavaLogicalStructures(JavaLogicalStructure[] logicalStructures) {
 		fUserDefinedJavaLogicalStructures= Arrays.asList(logicalStructures);
 		saveUserDefinedJavaLogicalStructures();
 		initJavaLogicalStructureMap();
 	}
 
 	public ILogicalStructureType[] getLogicalStructureTypes(IValue value) {
 		if (!(value instanceof IJavaObject)) {
 			return new ILogicalStructureType[0];
 		}
 		IJavaObject javaValue= (IJavaObject) value;
 		List logicalStructures= new ArrayList();
 		try {
 			IJavaType type= javaValue.getJavaType();
 			if (!(type instanceof IJavaClassType)) {
 				return new ILogicalStructureType[0];
 			}
 			IJavaClassType classType= (IJavaClassType) type;
 			List list= (List)fJavaLogicalStructureMap.get(classType.getName());
 			if (list != null) {
 				logicalStructures.addAll(list);
 			}
 			IJavaClassType superClass= classType.getSuperclass();
 			while (superClass != null) {
 				addIfIsSubtype(logicalStructures, (List)fJavaLogicalStructureMap.get(superClass.getName()));
 				superClass= superClass.getSuperclass();
 			}
 			IJavaInterfaceType[] superInterfaces= classType.getAllInterfaces();
 			for (int i= 0; i < superInterfaces.length; i++) {
 				addIfIsSubtype(logicalStructures, (List)fJavaLogicalStructureMap.get(superInterfaces[i].getName()));
 			}
 		} catch (DebugException e) {
 			JDIDebugPlugin.log(e);
 			return new ILogicalStructureType[0];
 		}
 		return (ILogicalStructureType[]) logicalStructures.toArray(new ILogicalStructureType[logicalStructures.size()]);
 	}
 
 	private void addIfIsSubtype(List logicalStructures, List list) {
 		if (list == null) {
 			return;
 		}
 		for (Iterator iter= list.iterator(); iter.hasNext();) {
 			JavaLogicalStructure logicalStructure= (JavaLogicalStructure) iter.next();
 			if (logicalStructure.isSubtypes()) {
 				logicalStructures.add(logicalStructure);
 			}
 		}
 	}
 
 }
