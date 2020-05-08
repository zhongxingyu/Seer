 /*******************************************************************************
  * Copyright (c) 2010, 2011 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.model;
 
 import static org.oobium.utils.CharStreamUtils.closer;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.findAll;
 import static org.oobium.utils.CharStreamUtils.findEOL;
 import static org.oobium.utils.FileUtils.readFile;
 import static org.oobium.utils.StringUtils.controllerSimpleName;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.build.gen.model.PropertyDescriptor;
 import org.oobium.utils.json.JsonUtils;
 
 public class ModelDefinition {
 
 	private static final char[] MODEL_DESCRIPTION = "@ModelDescription".toCharArray();
 	private static final char[] INDEXES = "@Indexes".toCharArray();
 	
 	private static final Set<String> primitives;
 	static {
 		primitives = new HashSet<String>();
 		primitives.add("byte");
 		primitives.add("short");
 		primitives.add("int");
 		primitives.add("long");
 		primitives.add("float");
 		primitives.add("double");
 		primitives.add("boolean");
 		primitives.add("char");
 	}
 	
 	public static List<String> getJavaArguments(char[] ca, int start, int end) {
 		List<String> args = new ArrayList<String>();
 		
 		int s1 = start;
 		int s = start;
 		while(s < end) {
 			switch(ca[s]) {
 			case ',':
 				String value = new String(ca, s1, s-s1).trim();
 				args.add(value);
 				s++;
 				s1 = s;
 				break;
 			case '<':
 			case '(':
 			case '{':
 			case '[':
 			case '"':
 				s = closer(ca, s, end, true) + 1;
 				break;
 			case '/':
 				if(ca[s-1] == '/') { // line comment
 					s = findEOL(ca, s+1);
 				}
 				break;
 			case '*':
 				if(ca[s-1] == '/') { // multiline comment
 					s++;
 					while(s < end) {
 						if(ca[s] == '/' && ca[s-1] == '*') {
 							s++;
 							break;
 						}
 						s++;
 					}
 				}
 				break;
 			default:
 				s++;
 			}
 		}
 
 		if(end > s1) {
 			String value = new String(ca, s1, end-s1).trim();
 			if(value.length() > 0) {
 				args.add(value);
 			}
 		}
 		
 		return args;
 	}
 
 	public static Map<String, String> getJavaEntries(char[] ca, int start, int end) {
 		Map<String, String> entries = new LinkedHashMap<String, String>();
 		
 		String name = null;
 		int s1 = start;
 		int s = start;
 		while(s < end) {
 			switch(ca[s]) {
 			case '=':
 				name = new String(ca, s1, s-s1).trim();
 				s++;
 				s1 = s;
 				break;
 			case ',':
 				String value = new String(ca, s1, s-s1).trim();
 				entries.put(name, value);
 				name = null;
 				s++;
 				s1 = s;
 				break;
 			case '<':
 			case '(':
 			case '{':
 			case '[':
 			case '"':
 				s = closer(ca, s, end, true) + 1;
 				break;
 			case '/':
 				if(ca[s-1] == '/') { // line comment
 					s = findEOL(ca, s+1);
 				}
 				break;
 			case '*':
 				if(ca[s-1] == '/') { // multiline comment
 					s++;
 					while(s < end) {
 						if(ca[s] == '/' && ca[s-1] == '*') {
 							s++;
 							break;
 						}
 						s++;
 					}
 				}
 				break;
 			default:
 				s++;
 			}
 		}
 		
 		if(name != null) {
 			String value = new String(ca, s1, end-s1).trim();
 			if(value.length() > 0) {
 				entries.put(name, value);
 			}
 		}
 		
 		return entries;
 	}
 
 	public static Map<String, String> getJavaEntries(String s) {
 		return getJavaEntries(s.toCharArray(), 0, s.length());
 	}
 	
 	public static String getString(String in) {
 		if(in == null) {
 			return "";
 		}
 		if(in.length() > 1 && in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') {
 			return in.substring(1, in.length()-1);
 		}
 		return in;
 	}
 
 	public final File file;
 	private final String source;
 
 	public final String packageName;
 	public final String type;
 	public final Map<String, ModelAttribute> attributes;
 	public final Map<String, ModelRelation> relations;
 	public final List<String> indexes;
 	public final boolean datestamps;
 	public final boolean timestamps;
 	
 	public ModelDefinition(File file) {
 		this.file = file;
 		this.source = readFile(file).toString();
 
 		this.packageName = parsePackageName();
 		this.type = parseType();
 		
 		this.attributes = new LinkedHashMap<String, ModelAttribute>();
 		this.relations = new LinkedHashMap<String, ModelRelation>();
 		this.indexes = new ArrayList<String>();
 
 		parse();
 		
 		this.datestamps = attributes.containsKey("datestamps");
 		this.timestamps = attributes.containsKey("timestamps");
 	}
 
 	public String getCanonicalName() {
 		return type;
 	}
 	
 	public String getControllerName() {
 		return controllerSimpleName(type);
 	}
 	
 	public List<String> getIndexes() {
 		return indexes;
 	}
 
 	public String getPackageName() {
 		int ix = type.lastIndexOf('.');
 		if(ix == -1) {
 			return type;
 		}
 		return type.substring(0, ix);
 	}
 	
 	public LinkedHashMap<String, PropertyDescriptor> getProperties() {
 		LinkedHashMap<String, PropertyDescriptor> properties = new LinkedHashMap<String, PropertyDescriptor>();
 		for(Entry<String, ModelAttribute> entry : attributes.entrySet()) {
 			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
 		}
 		for(Entry<String, ModelRelation> entry : relations.entrySet()) {
 			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
 		}
 		if(properties.containsKey("createdAt")) {
 			properties.put("createdAt", properties.remove("createdAt"));
 		}
 		if(properties.containsKey("createdOn")) {
 			properties.put("createdOn", properties.remove("createdOn"));
 		}
 		if(properties.containsKey("updatedAt")) {
 			properties.put("updatedAt", properties.remove("updatedAt"));
 		}
 		if(properties.containsKey("updatedOn")) {
 			properties.put("updatedOn", properties.remove("updatedOn"));
 		}
 		return properties;
 	}
 
 	public String getSimpleType() {
 		int ix = type.lastIndexOf('.');
 		if(ix == -1) {
 			return type;
 		}
 		return type.substring(ix+1);
 	}
 	
 	String getType(String name) {
 		String type = null;
 		boolean array = name.endsWith("[]");
 		if(array) name = name.substring(0, name.length()-2);
		if(name.endsWith(".class")) name = name.substring(0, name.length() - 6);
 		
 		if(name.indexOf('.') != -1) {
 			return array ? (name + "[]") : name;
 		}
 		
 		if(primitives.contains(name)) {
 			return array ? (name + "[]") : name;
 		}
 		
 		Pattern p = Pattern.compile("import\\s+(static\\s+)?([\\w\\d\\._\\$]+\\." + name + ");");
 		Matcher m = p.matcher(source);
 		if(m.find()) {
 			type = m.group(2);
 			if(m.group(1) != null) {
 				type = type.substring(0, type.lastIndexOf('.'));
 			}
 		} else {
 			String src = name + ".java";
 			String[] siblings = file.getParentFile().list();
 			for(String sibling : siblings) {
 				if(src.equals(sibling)) {
 					type = packageName + "." + name;
 					break;
 				}
 			}
 			if(type == null) {
 				type = "java.lang." + name;
 			}
 		}
 		if(array) {
 			return type + "[]";
 		}
 		return type;
 	}
 	
 	public boolean hasAttributes() {
 		return attributes != null && !attributes.isEmpty();
 	}
 
 	public boolean hasRelations() {
 		return relations != null && !relations.isEmpty();
 	}
 	
 	private void parse() {
 		char[] ca = source.toCharArray();
 		
 		int s1 = findAll(ca, 0, MODEL_DESCRIPTION);
 		if(s1 != -1) {
 			s1 = find(ca, '(', s1);
 			if(s1 != -1) {
 				int s2 = closer(ca, s1);
 				if(s2 != -1) {
 					parseDescription(ca, s1+1, s2);
 					s1 = findAll(ca, 0, INDEXES);
 					if(s1 != -1) {
 						s1 = find(ca, '(', s1);
 						if(s1 != -1) {
 							s2 = closer(ca, s1);
 							if(s2 != -1) {
 								parseIndexes(ca, s1+2, s2);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void parseAttrs(String attrs) {
 		char[] ca = attrs.toCharArray();
 		for(String arg : getJavaArguments(ca, 1, ca.length-1)) {
 			ModelAttribute attr = new ModelAttribute(this, arg);
 			attributes.put(attr.name, attr);
 		}
 	}
 	
 	private void parseDescription(char[] ca, int start, int end) {
 		Map<String, String> parameters = getJavaEntries(ca, start, end);
 		
 		String v = parameters.get("attrs");
 		if(v != null) {
 			parseAttrs(v);
 		}
 		
 		v = parameters.get("datestamps");
 		if("true".equals(v)) {
 			attributes.put("createdOn", new ModelAttribute(this, "createdOn"));
 			attributes.put("updatedOn", new ModelAttribute(this, "updatedOn"));
 		}
 
 		v = parameters.get("timestamps");
 		if("true".equals(v)) {
 			attributes.put("createdAt", new ModelAttribute(this, "createdAt"));
 			attributes.put("updatedAt", new ModelAttribute(this, "updatedAt"));
 		}
 		
 		v = parameters.get("hasOne");
 		if(v != null) {
 			parseRelations(v, false);
 		}
 
 		v = parameters.get("hasMany");
 		if(v != null) {
 			parseRelations(v, true);
 		}
 	}
 
 	private void parseIndexes(char[] ca, int start, int end) {
 		String s = new String(ca, start, end-start+1).trim();
 		indexes.addAll(JsonUtils.toStringList(s));
 	}
 	
 	private String parsePackageName() {
 		Pattern p = Pattern.compile("package\\s+([\\w\\d\\._]+);");
 		Matcher m = p.matcher(source);
 		if(m.find()) {
 			return m.group(1);
 		}
 		return null;
 	}
 	
 	private void parseRelations(String relation, boolean hasMany) {
 		char[] ca = relation.toCharArray();
 		for(String arg : getJavaArguments(ca, 1, ca.length-1)) {
 			ModelRelation rel = new ModelRelation(this, arg, hasMany);
 			relations.put(rel.name, rel);
 		}
 	}
 
 	private String parseType() {
 		String name = file.getName();
 		name = name.substring(0, name.length() - 5);
 		return packageName + "." + name;
 	}
 
 	public Map<String, ModelRelation> relations() {
 		return relations;
 	}
 
 	public void setOpposites(ModelDefinition[] models) {
 		for(ModelRelation relation : relations.values()) {
 			relation.setOpposite(models);
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return super.toString()+" {"+type+" => "+"}";
 	}
 
 }
