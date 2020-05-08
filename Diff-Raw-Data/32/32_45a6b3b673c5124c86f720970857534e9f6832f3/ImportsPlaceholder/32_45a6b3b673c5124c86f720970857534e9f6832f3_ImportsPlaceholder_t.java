 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.codecomposers.java;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.devboost.codecomposers.StringComponent;
 
 public class ImportsPlaceholder extends StringComponent {
 	
 	private Map<String, String> qualifiedImports = new LinkedHashMap<String, String>();
 	private Map<String, String> implicitImports = new LinkedHashMap<String, String>();
 	private String lineBreak;
 
 	public ImportsPlaceholder(String lineBreak) {
 		super(null, null);
 		this.lineBreak = lineBreak;
 	}
 
 	public String getClassName(String qualifiedClassName) {
 		if (isPrimitiveType(qualifiedClassName)) {
 			return qualifiedClassName;
 		}
 		
 		if (isGeneric(qualifiedClassName)) {
 			List<String> types = getTypeArguments(qualifiedClassName);
 			return getClassName(types.get(0)) + "<" + getClassName(types.get(1)) + ">";
 		}
 		
 		if (qualifiedImports.values().contains(qualifiedClassName) ||
 			implicitImports.values().contains(qualifiedClassName)) {
 			String simpleName = getSimpleName(qualifiedClassName);
 			return simpleName;
 		} else {
 			if (isNameImported(qualifiedClassName)) {
 				return qualifiedClassName;
 			} else {
 				String simpleName = getSimpleName(qualifiedClassName);
 				qualifiedImports.put(simpleName, qualifiedClassName);
 				return simpleName;
 			}
 		}
 	}
 
 	private List<String> getTypeArguments(String qualifiedClassName) {
 		// TODO This is a very simplistic way to get the type arguments which
 		// will neither work for multiple nor nested arguments.
 		int begin = qualifiedClassName.indexOf("<");
 		int end = qualifiedClassName.indexOf(">");
 		String type = qualifiedClassName.substring(0, begin);
 		String typeArgument = qualifiedClassName.substring(begin + 1, end);
 		
 		List<String> types = new ArrayList<String>(2);
 		types.add(type);
 		types.add(typeArgument);
 		return types;
 	}
 
 	private boolean isGeneric(String qualifiedClassName) {
 		return qualifiedClassName.contains("<");
 	}
 
 	private boolean isPrimitiveType(String qualifiedClassName) {
 		
 		// Remove array qualifiers
 		qualifiedClassName = qualifiedClassName.replace("[", "");
 		qualifiedClassName = qualifiedClassName.replace("]", "");
 		
 		Class<?>[] primitiveTypes = new Class<?>[] {boolean.class, int.class, char.class, byte.class, long.class, double.class, float.class};
 		for (Class<?> primitiveType : primitiveTypes) {
 			if (primitiveType.getName().equals(qualifiedClassName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isNameImported(String qualifiedClassName) {
 		String simpleName = getSimpleName(qualifiedClassName);
 		return qualifiedImports.keySet().contains(simpleName) ||
 				implicitImports.keySet().contains(simpleName);
 	}
 
 	private String getSimpleName(String qualifiedClassName) {
 		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
 	}
 	
 	@Override
 	public String getText() {
 		List<String> imports = new ArrayList<String>();
 		for (String qualifiedImport : qualifiedImports.values()) {
 			if (qualifiedImport == null) {
 				continue;
 			}
 			// Do not import classes that are imported by default
			String prefix = "java.lang.";
			if (qualifiedImport.startsWith(prefix) && qualifiedImport.substring(prefix.length() + 1).indexOf(".") < 0) {
 				continue;
 			}
 			imports.add(qualifiedImport);
 		}
 		
 		Collections.sort(imports);
 		
 		StringBuilder text = new StringBuilder();
 		for (String importToAdd : imports) {
 			text.append("import ");
 			text.append(importToAdd);
 			text.append(";");
 			text.append(lineBreak);
 		}
 		return text.toString();
 	}
 
 	public void addImplicitImport(String qualifiedClassName) {
 		String simpleName = getSimpleName(qualifiedClassName);
 		implicitImports.put(simpleName, qualifiedClassName);
 	}
 }
