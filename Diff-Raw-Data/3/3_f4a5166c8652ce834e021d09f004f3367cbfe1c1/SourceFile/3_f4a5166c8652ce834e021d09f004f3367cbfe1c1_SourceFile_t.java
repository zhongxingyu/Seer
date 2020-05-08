 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.util;
 
 import static org.oobium.utils.StringUtils.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import org.oobium.build.gen.model.PropertyDescriptor;
 
 
 public class SourceFile {
 	
 	private static final char[] CLASS = {'c','l','a','s','s'};
 	private static final char[] IMPORT = {'i','m','p','o','r','t'};
 	private static final char[] PACKAGE = {'p','a','c','k','a','g','e'};
 
 	public enum ClassModifier {
 		DEFAULT, PUBLIC, PROTECTED, PRIVATE;
 		public String getModifier() {
 			if(this == DEFAULT) {
 				return "";
 			}
 			return name().toLowerCase();
 		}
 		public boolean isDefault() {
 			return this == DEFAULT;
 		}
 	}
 
 	public static int closer(StringBuilder sb, int start) {
 		if(start >= 0 && start < sb.length()) {
 			char opener = sb.charAt(start);
 			char closer = closerChar(opener);
 			int count = 1;
 			for(int i = start+1; i >= 0 && i < sb.length(); i++) {
 				char c0 = sb.charAt(i-1);
 				char c1 = sb.charAt(i);
 				if(c1 == opener && c1 != closer) {
 					count++;
 				} else if(c1 == closer) {
 					if(closer != '"' || c0 != '\\') { // check for escape char
 						count--;
 						if(count == 0) {
 							return i;
 						}
 					}
 				} else if(c1 == '"') { // string
 					for(i++; i < sb.length(); i++) {
 						if(sb.charAt(i-1) != '\\' && sb.charAt(i) == '"') {
 							break;
 						}
 					}
 				} else if(sb.charAt(i-1) == '/' && sb.charAt(i) == '/') { // java line comment
 					for(i++; i < sb.length(); i++) {
 						if(sb.charAt(i) == '\n') {
 							break;
 						}
 					}
 				} else if(sb.charAt(i-1) == '/' && sb.charAt(i) == '*') { // java multiline comment
 					for(i++; i < sb.length(); i++) {
 						if(sb.charAt(i-1) == '*' && sb.charAt(i) == '/') {
 							break;
 						}
 					}
 				}
 			}
 		}
 		return -1;
 	}
 	
 	public static char closerChar(char c) {
 		switch(c) {
 			case '<': return '>';
 			case '(': return ')';
 			case '{': return '}';
 			case '[': return ']';
 			case '"': return '"';
 			case '\'': return '\'';
 		}
 		return 0;
 	}
 
 	public static int find(StringBuilder sb, int from, int to, char...cs) {
 		for(int i = from; i >= 0 && i < to && i < sb.length(); i++) {
 			if(sb.charAt(i) == cs[0]) {
 				boolean found = true;
 				for(int j = 1; j < cs.length; j++) {
 					if((i+j) == sb.length() || sb.charAt(i+j) != cs[j]) {
 						found = false;
 						break;
 					}
 				}
 				if(found) {
 					return i;
 				}
 			} else if(sb.charAt(i) == '(') {
 				i = closer(sb, i);
 				if(i == -1) {
 					break;
 				}
 			} else if(sb.charAt(i) == '"') { // string
 				for(i++; i < sb.length(); i++) {
 					if(sb.charAt(i-1) != '\\' && sb.charAt(i) == '"') {
 						break;
 					}
 				}
 			} else if(sb.charAt(i) == '/' && sb.charAt(i+1) == '/') { // java line comment
 				for(i+=2; i < sb.length(); i++) {
 					if(sb.charAt(i) == '\n') {
 						break;
 					}
 				}
 			} else if(sb.charAt(i) == '<' && sb.charAt(i+1) == '*') { // java multiline comment
 				for(i+=2; i < sb.length(); i++) {
 					if(sb.charAt(i) == '*' && sb.charAt(i+1) == '>') {
 						break;
 					}
 				}
 			}
 		}
 		return -1;
 	}
 	
 	public static String ensureImport(String src, String imp) {
 		return ensureImports(src, Collections.singleton(imp));
 	}
 	
 	public static String ensureImports(String src, Collection<String> imports) {
 		return ensureImports(new StringBuilder(src), imports);
 	}
 	
 	public static String removeUnusedImport(String src, String imp) {
 		StringBuilder sb = new StringBuilder(src);
 		TreeSet<String> imports = loadImports(sb);
 		if(imports.remove(imp)) {
 			replaceImports(sb, imports);
 			String s = sb.toString();
 			if(unused(s, imp)) {
 				return s;
 			}
 		}
 		return src;
 	}
 	
 	public static String removeUnusedImports(StringBuilder sb, Collection<String> imports) {
 		String src = sb.toString();
 		TreeSet<String> imps = loadImports(sb);
 		boolean doit = false;
 		for(String imp : imports) {
 			if(imps.contains(imp) && unused(src, imp)) {
 				imps.remove(imp);
 				doit = true;
 			}
 		}
 		if(doit) {
 			replaceImports(sb, imps);
 			return sb.toString();
 		} else {
 			return src;
 		}
 	}
 	
 	private static boolean unused(String src, String imp) {
 		String s = imp.startsWith("static ") ? imp.substring(7) : imp;
 		int ix = s.lastIndexOf('.');
 		if(ix != -1) {
 			s = s.substring(ix+1);
 		}
 		Pattern p = Pattern.compile("\\W+" + s + "\\W+");
 		return !p.matcher(src).find();
 	}
 	
 	public static String ensureImports(StringBuilder sb, Collection<String> imports) {
 		TreeSet<String> imps = loadImports(sb);
 		imps.addAll(imports);
 		replaceImports(sb, imps);
 		return sb.toString();
 	}
 	
 	public static TreeSet<String> loadImports(String src) {
 		return loadImports(new StringBuilder(src));
 	}
 	
 	public static TreeSet<String> loadImports(StringBuilder sb) {
 		TreeSet<String> imports = new TreeSet<String>();
 		int stop = find(sb, 0, sb.length(), CLASS);
 		int start = find(sb, 0, stop, IMPORT);
 		while(start != -1) {
 			start += IMPORT.length;
 			while(Character.isWhitespace(sb.charAt(start)) && start < stop) {
 				start++;
 			}
 			int end = find(sb, start, stop, ';');
			if(end == -1) {
				break;
			}
 			imports.add(sb.substring(start, end));
 			start = find(sb, end, stop, IMPORT);
 		}
 		return imports;
 	}
 
 	private static void replaceImports(StringBuilder sb, TreeSet<String> imports) {
 		if(imports.isEmpty()) {
 			return;
 		}
 		
 		int stop = find(sb, 0, sb.length(), CLASS);
 		if(stop == -1) {
 			return;
 		}
 		
 		int start = find(sb, 0, stop, PACKAGE);
 		if(start == -1) {
 			start = 0;
 		} else {
 			start = find(sb, start+1, stop, '\n') + 1;
 		}
 
 		int end = find(sb, start, stop, IMPORT);
 		if(end != -1) {
 			while(true) {
 				int i = find(sb, end + IMPORT.length, stop, IMPORT);
 				if(i != -1) {
 					end = i;
 				} else {
 					end = find(sb, end, stop, '\n') + 1;
 					break;
 				}
 			}
 		}
 		
 		TreeSet<String> statics = new TreeSet<String>();
 		for(Iterator<String> iter = imports.iterator(); iter.hasNext(); ) {
 			String imp = iter.next();
 			if(imp.startsWith("static ")) {
 				statics.add(imp);
 				iter.remove();
 			}
 		}
 		for(String imp1 : new HashSet<String>(statics)) {
 			if(imp1.endsWith(".*")) {
 				String s1 = imp1.substring(0, imp1.lastIndexOf('.'));
 				for(Iterator<String> iter = statics.iterator(); iter.hasNext(); ) {
 					String imp2 = iter.next();
 					if(imp1 != imp2) {
 						String s2 = imp2.substring(0, imp2.lastIndexOf('.'));
 						if(s1.equals(s2) || (s1 + "Model").equals(s2)) {
 							iter.remove();
 						}
 					}
 				}
 			}
 		}
 		
 		StringBuilder sb2 = new StringBuilder();
 		sb2.append('\n');
 		for(String imp : statics) {
 			sb2.append("import ").append(imp).append(";\n");
 		}
 		if(!statics.isEmpty() && !imports.isEmpty()) {
 			sb2.append('\n');
 		}
 		for(String imp : imports) {
 			sb2.append("import ").append(imp).append(";\n");
 		}
 		
 		if(end == -1) {
 			sb.insert(start, sb2.toString());
 		} else {
 			sb.replace(start, end, sb2.toString());
 		}
 	}
 
 	public String simpleName;
 	public String packageName;
 	public String superName;
 	public boolean isAbstract;
 	public TreeSet<String> interfaces = new TreeSet<String>();
 	public TreeSet<String> imports = new TreeSet<String>();
 	public TreeMap<Integer, String> classAnnotations = new TreeMap<Integer, String>();
 	public TreeSet<String> staticImports = new TreeSet<String>();
 	public List<String> staticInitializers = new ArrayList<String>();
 	public List<String> initializers = new ArrayList<String>();
 	public TreeMap<String, String> staticVariables = new TreeMap<String, String>();
 	public TreeMap<String, String> staticMethods = new TreeMap<String, String>();
 	public TreeMap<String, String> variables = new TreeMap<String, String>();
 	public TreeMap<Integer, String> constructors = new TreeMap<Integer, String>();
 	public TreeMap<String, String> methods = new TreeMap<String, String>();
 	public TreeMap<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>();
 	public String propertiesEnum;
 	public String propertiesArray;
 
 	public String rawSource;
 
 	public ClassModifier classModifier = ClassModifier.PUBLIC;
 	
 	public void addMethod(MethodCreator mc){
 		methods.put(mc.name, mc.toString());
 	}
 	
 	public String getCanonicalName() {
 		return packageName + "." + simpleName;
 	}
 	
 	public String getFileName() {
 		return simpleName + ".java";
 	}
 
 	public String getFilePath() {
 		return getCanonicalName().replace('.', File.separatorChar) + ".java";
 	}
 	
 	public String getPackageName() {
 		return packageName;
 	}
 	
 	public String getSimpleName() {
 		return simpleName;
 	}
 	
 	public String toSource() {
 		for(Iterator<String> iter = imports.iterator(); iter.hasNext(); ) {
 			String imp = iter.next();
 			if(imp.startsWith("static ")) {
 				staticImports.add(imp.substring(7));
 				iter.remove();
 			}
 		}
 		
 		StringBuilder sb = new StringBuilder();
 
 		if(packageName != null) {
 			sb.append("package ").append(packageName).append(";\n");
 			sb.append('\n');
 		}
 		if(!staticImports.isEmpty()) {
 			for(String imp : staticImports) {
 				sb.append("import static ").append(imp).append(";\n");
 			}
 			sb.append('\n');
 		}
 		if(!imports.isEmpty()) {
 			for(String imp : imports) {
 				sb.append("import ").append(imp).append(";\n");
 			}
 			sb.append('\n');
 		}
 		for(String ann : classAnnotations.values()) {
 			sb.append(ann).append('\n');
 		}
 		if(!classModifier.isDefault()) {
 			sb.append(classModifier.getModifier()).append(' ');
 		}
 		if(isAbstract) {
 			sb.append("abstract ");
 		}
 		sb.append("class ").append(simpleName);
 		if(superName != null) {
 			sb.append(" extends ").append(superName);
 		}
 		if(!interfaces.isEmpty()) {
 			sb.append(" implements");
 			for(String iface : interfaces) {
 				sb.append(' ').append(iface);
 			}
 		}
 		sb.append(" {\n");
 		if(!properties.isEmpty()) {
 			sb.append('\n');
 			if(propertiesEnum != null && propertiesEnum.length() > 0) {
 				sb.append("\tpublic enum ").append(propertiesEnum).append(" {");
 				for(Iterator<PropertyDescriptor> i = properties.values().iterator(); i.hasNext();) {
 					sb.append("\n\t\t");
 					sb.append(i.next().enumProp());
 					if(i.hasNext()) {
 						sb.append(',');
 					} else {
 						sb.append("\n\t");
 					}
 				}
 				sb.append("}\n");
 			} else {
 				for(PropertyDescriptor prop : properties.values()) {
 					sb.append("\tpublic static final String ").append(prop.enumProp()).append(" = \"").append(prop.variable()).append("\";\n");
 				}
 				if(propertiesArray != null && propertiesArray.length() > 0) {
 					sb.append("\n");
 					sb.append("\tpublic static final String[] ").append(propertiesArray).append(" = {");
 					for(Iterator<PropertyDescriptor> i = properties.values().iterator(); i.hasNext();) {
 						sb.append("\n\t\t");
 						sb.append(i.next().enumProp());
 						if(i.hasNext()) {
 							sb.append(',');
 						} else {
 							sb.append("\n\t");
 						}
 					}
 					sb.append("};\n");
 				}
 			}
 		}
 		if(!staticVariables.isEmpty()) {
 			sb.append('\n');
 			for(String var : staticVariables.values()) {
 				sb.append("\t");
 				if(var.startsWith("public ") || var.startsWith("protected ") || var.startsWith("private ")) {
 					sb.append(var);
 				} else {
 					sb.append("private ").append(var);
 				}
 				if(!var.endsWith(";")) {
 					sb.append(';');
 				}
 				sb.append('\n');
 			}
 		}
 		for(String method : staticMethods.values()) {
 			sb.append('\n');
 			sb.append(source('\t', '\t', method)).append('\n');
 		}
 		if(!staticInitializers.isEmpty()) {
 			sb.append('\n');
 			sb.append("\tstatic {\n");
 			for(String init : staticInitializers) {
 				sb.append("\t\t").append(init);
 				if(!init.endsWith(";")) {
 					sb.append(';');
 				}
 				sb.append('\n');
 			}
 			sb.append("\t}\n");
 		}
 		if(!variables.isEmpty()) {
 			sb.append('\n');
 			for(String var : variables.values()) {
 				sb.append("\t");
 				if(var.startsWith("public ") || var.startsWith("protected ") || var.startsWith("private ")) {
 					sb.append(var);
 				} else {
 					sb.append("private ").append(var);
 				}
 				if(!var.endsWith(";")) {
 					sb.append(';');
 				}
 				sb.append('\n');
 			}
 		}
 		if(!initializers.isEmpty()) {
 			sb.append("\n\t{\n");
 			for(String init : initializers) {
 				sb.append("\t\t").append(init);
 				if(!init.endsWith(";")) {
 					sb.append(';');
 				}
 				sb.append('\n');
 			}
 			sb.append("\t}\n");
 		}
 		if(rawSource != null && rawSource.length() > 0) {
 			sb.append('\n');
 			sb.append(source('\t', '\t', rawSource)).append('\n');
 		}
 		for(String constructor : constructors.values()) {
 			sb.append('\n');
 			sb.append(source('\t', '\t', constructor)).append('\n');
 		}
 		for(String method : methods.values()) {
 			sb.append('\n');
 			sb.append(source('\t', '\t', method)).append('\n');
 		}
 		sb.append("\n}");
 
 		return sb.toString();
 	}
 	
 	@Override
 	public String toString() {
 		return super.toString() + " {" + getCanonicalName() + "}";
 	}
 	
 }
