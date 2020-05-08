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
 
 import static org.oobium.build.util.SourceFile.ensureImports;
 import static org.oobium.persist.ModelDescription.DEFAULT_ALLOW_DELETE;
 import static org.oobium.persist.ModelDescription.DEFAULT_ALLOW_UPDATE;
 import static org.oobium.persist.ModelDescription.DEFAULT_DATESTAMPS;
 import static org.oobium.persist.ModelDescription.DEFAULT_EMBEDDED;
 import static org.oobium.persist.ModelDescription.DEFAULT_TIMESTAMPS;
 import static org.oobium.utils.CharStreamUtils.closer;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.findAll;
 import static org.oobium.utils.FileUtils.readFile;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.controllerSimpleName;
 import static org.oobium.utils.StringUtils.getResourceAsString;
 import static org.oobium.utils.StringUtils.join;
 import static org.oobium.utils.StringUtils.*;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.build.model.ModelUtils.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.build.gen.model.PropertyDescriptor;
 import org.oobium.persist.Attribute;
 import org.oobium.persist.ModelDescription;
 import org.oobium.persist.Relation;
 import org.oobium.persist.Validate;
 import org.oobium.persist.Validations;
 import org.oobium.utils.json.JsonUtils;
 
 public class ModelDefinition {
 
 	private static final String packageRegex = "package\\s+([\\w\\d\\._]+);";
 	private static final Pattern packagePattern = Pattern.compile(packageRegex);
 
 	private static final char[] MODEL_DESCRIPTION = "@ModelDescription".toCharArray();
 	private static final char[] INDEXES = "@Indexes".toCharArray();
 	private static final char[] VALIDATIONS = "@Validations".toCharArray();
 	
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
 
 	public static ModelDefinition[] getModelDefinitions(Collection<File> models) {
 		return getModelDefinitions(models.toArray(new File[models.size()]));
 	}
 
 	public static ModelDefinition[] getModelDefinitions(File[] models) {
 		ModelDefinition[] defs = new ModelDefinition[models.length];
 	
 		for(int i = 0; i < defs.length; i++) {
 			defs[i] = new ModelDefinition(models[i]);
 		}
 		
 		for(ModelDefinition def : defs) {
 			def.setOpposites(defs);
 		}
 		
 		return defs;
 	}
 
 	public static List<ModelDefinition> getSystemDefinitions() {
 		List<ModelDefinition> defs = new ArrayList<ModelDefinition>();
 		defs.add(getSessionDefinition());
 		defs.add(getUserDefinition());
 		return defs;
 	}
 	
 	public static ModelDefinition getSessionDefinition() {
 		String source = getResourceAsString(ModelDefinition.class, "system/Session.src");
 		ModelDefinition def = new ModelDefinition("Session", source);
 		return def;
 	}
 
 	public static ModelDefinition getUserDefinition() {
 		String source = getResourceAsString(ModelDefinition.class, "system/User.src");
 		ModelDefinition def = new ModelDefinition("User", source);
 		return def;
 	}
 
 	
 	private File file; // TODO this file object will not work when we need to deal with jars...
 	private String source;
 	private int mdstart; // ModelDescription start
 	private int mdend;   // ModelDescription end
 	private int ixstart; // Indexes start
 	private int ixend;   // Indexes end
 	private int mvstart; // Validations start
 	private int mvend;   // Validations end
 
 	private String type;
 	private String packageName;
 	private final LinkedHashMap<String, ModelAttribute> attributes;
 	private final LinkedHashMap<String, ModelRelation> hasOne;
 	private final LinkedHashMap<String, ModelRelation> hasMany;
 	private final ArrayList<String> indexes;
 	private final LinkedHashMap<String, ModelValidation> validations;
 	private boolean datestamps = DEFAULT_DATESTAMPS;
 	private boolean timestamps = DEFAULT_TIMESTAMPS;
 	private boolean allowUpdate = DEFAULT_ALLOW_UPDATE;
 	private boolean allowDelete = DEFAULT_ALLOW_DELETE;
 	private boolean embedded = DEFAULT_EMBEDDED;
 
 	private String[] siblings;
 
 	
 	public ModelDefinition(File file) {
 		this(file.getName(), null, file, null);
 	}
 	
 	public ModelDefinition(File file, String source) {
 		this(file.getName(), source, file, null);
 	}
 
 	private ModelDefinition(ModelDefinition original) {
 		this.file = new File(original.file.getAbsolutePath());
 		this.source = original.source;
 		this.mdstart = original.mdstart;
 		this.mdend = original.mdend;
 
 		this.packageName = original.packageName;
 		this.type = original.type;
 
 		this.datestamps = original.datestamps;
 		this.timestamps = original.timestamps;
 		this.allowUpdate = original.allowUpdate;
 		this.allowDelete = original.allowDelete;
 		this.embedded = original.embedded;
 		
 		this.attributes = new LinkedHashMap<String, ModelAttribute>();
 		for(Entry<String, ModelAttribute> entry : original.attributes.entrySet()) {
 			this.attributes.put(entry.getKey(), entry.getValue().getCopy());
 		}
 		
 		this.hasOne = new LinkedHashMap<String, ModelRelation>();
 		for(Entry<String, ModelRelation> entry : original.hasOne.entrySet()) {
 			this.hasOne.put(entry.getKey(), entry.getValue().getCopy());
 		}
 
 		this.hasMany = new LinkedHashMap<String, ModelRelation>();
 		for(Entry<String, ModelRelation> entry : original.hasMany.entrySet()) {
 			this.hasMany.put(entry.getKey(), entry.getValue().getCopy());
 		}
 		
 		this.indexes = new ArrayList<String>(original.indexes);
 
 		this.validations = new LinkedHashMap<String, ModelValidation>();
 		for(Entry<String, ModelValidation> entry : original.validations.entrySet()) {
 			this.validations.put(entry.getKey(), entry.getValue().getCopy());
 		}
 
 		this.siblings = (original.siblings != null) ? Arrays.copyOf(original.siblings, original.siblings.length) : null;
 	}
 
 	public ModelDefinition(String simpleName) {
 		this(simpleName, null, null, new String[0]);
 	}
 
 	private ModelDefinition(String simpleName, String source, File file, String[] siblings) {
 		if(source == null) {
 			if(file != null && file.isFile()) {
 				this.source = readFile(file).toString();
 			} else {
 				this.source =
 					"package unknown;\n" +
 					"\n" +
 					"import org.oobium.persist.ModelDescription;\n" +
 					"\n" +
 					"@ModelDescription()\n" +
 					"public class " + simpleName + " {\n" +
 					"\n" +
 					"}";
 			}
 		} else {
 			this.source = source;
 		}
 
 		this.packageName = parsePackageName();
 		this.type = parseType(simpleName);
 
 		this.file = file;
 		this.siblings = siblings;
 		
 		this.attributes = new LinkedHashMap<String, ModelAttribute>();
 		this.hasOne = new LinkedHashMap<String, ModelRelation>();
 		this.hasMany = new LinkedHashMap<String, ModelRelation>();
 		this.indexes = new ArrayList<String>();
 		this.validations = new LinkedHashMap<String, ModelValidation>();
 
 		parse();
 	}
 	
 	public ModelDefinition(String simpleName, String source, String...siblings) {
 		this(simpleName, source, null, siblings);
 	}
 
 	public ModelAttribute addAttribute(ModelAttribute attribute) {
 		ModelAttribute attr = attribute.getCopy();
 		attributes.put(attr.name(), attr);
 		return attr;
 	}
 	
 	public ModelAttribute addAttribute(String attribute) {
 		ModelAttribute attr = new ModelAttribute(this, attribute);
 		attributes.put(attr.name(), attr);
 		return attr;
 	}
 
 	public ModelAttribute addAttribute(String name, String type, Map<String, Object> options) {
 		return addAttribute(build(name, type, options));
 	}
 	
 	public ModelAttribute addAttribute(String name, String type, String...options) {
 		return addAttribute(build(name, type, options));
 	}
 	
 	public ModelRelation addHasMany(String name, String type, String...options) {
 		return addRelation(build(name, type, options), true);
 	}
 	
 	public ModelRelation addHasOne(String name, String type, String...options) {
 		return addRelation(build(name, type, options), false);
 	}
 
 	public ModelRelation addRelation(ModelRelation relation) {
 		ModelRelation rel = relation.getCopy(this);
 		if(rel.hasMany()) {
 			this.hasMany.put(rel.name(), rel);
 		} else {
 			this.hasOne.put(rel.name(), rel);
 		}
 		return rel;
 	}
 	
 	public ModelRelation addRelation(String annotation, boolean hasMany) {
 		ModelRelation rel = new ModelRelation(this, annotation, hasMany);
 		if(rel.hasMany()) {
 			this.hasMany.put(rel.name(), rel);
 		} else {
 			this.hasOne.put(rel.name(), rel);
 		}
 		return rel;
 	}
 	
 	public ModelRelation addRelation(String name, String type, boolean hasMany) {
 		return addRelation(build(name, type), hasMany);
 	}
 	
 	public ModelRelation addRelation(String name, String type, boolean hasMany, Map<String, Object> options) {
 		return addRelation(build(name, type, options), hasMany);
 	}
 
 	public ModelValidation addValidation(ModelValidation validation) {
 		ModelValidation v = validation.getCopy();
 		validations.put(v.field(), v);
 		return validation;
 	}
 	
 	public ModelValidation addValidation(String field, Map<String, ?> options) {
 		ModelValidation v = validations.get(field);
 		if(v == null) {
 			v = new ModelValidation(this, field, options);
 			validations.put(v.field(), v);
 		} else {
 			v.putAll(options);
 		}
 		if(v.getCustomProperties().isEmpty()) {
 			validations.remove(v.field());
 		}
 		return v;
 	}
 	
 	private void addValidations(String annotation) {
 		char[] ca = annotation.toCharArray();
 		int start = annotation.indexOf('(') + 1;
 		int end = annotation.length() - 1;
 		Map<String, String> entries = getJavaEntries(ca, start, end);
 		
 		if(entries.containsKey("field")) {
 			String[] fields = getString(entries.remove("field")).split("\\s*,\\s*");
 			for(String field : fields) {
 				addValidation(field, entries);
 			}
 		} else {
 			addValidation(null, entries);
 		}
 	}
 
 	public boolean allowDelete() {
 		return allowDelete;
 	}
 
 	public ModelDefinition allowDelete(boolean allowDelete) {
 		this.allowDelete = allowDelete;
 		return this;
 	}
 	
 	public boolean allowUpdate() {
 		return allowUpdate;
 	}
 	
 	public ModelDefinition allowUpdate(boolean allowUpdate) {
 		this.allowUpdate = allowUpdate;
 		return this;
 	}
 
 	private String build(String name, String type, Map<String, Object> options) {
 		if(!type.endsWith(".class")) type = type + ".class";
 		StringBuilder sb = new StringBuilder();
 		sb.append("(name=\"").append(name).append("\",type=").append(type);
 		if(options != null) {
 			for(Entry<String, Object> option : options.entrySet()) {
 				sb.append(',').append(option);
 			}
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 
 	private String build(String name, String type, String...options) {
 		if(!type.endsWith(".class")) type = type + ".class";
 		StringBuilder sb = new StringBuilder();
 		sb.append("(name=\"").append(name).append("\",type=").append(type);
 		for(String option : options) {
 			sb.append(',').append(option);
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 
 	public boolean datestamps() {
 		return datestamps;
 	}
 	
 	public ModelDefinition datestamps(boolean datestamps) {
 		this.datestamps = datestamps;
 		return this;
 	}
 	
 	public boolean embedded() {
 		return embedded;
 	}
 
 	public ModelDefinition embedded(boolean embedded) {
 		this.embedded = embedded;
 		return this;
 	}
 	
 	public boolean equivalent(ModelDefinition other) {
 		if(this.type.equals(other.type)
 				&& this.attributes.size() == other.attributes.size()
 				&& this.hasOne.size() == other.hasOne.size()
 				&& this.hasMany.size() == other.hasMany.size()
 				&& this.timestamps == other.timestamps
 				&& this.datestamps == other.datestamps
 				&& this.allowDelete == other.allowDelete
 				&& this.allowUpdate == other.allowUpdate
 				&& this.embedded == other.embedded
 				&& this.validations.size() == other.validations.size()
 				&& this.indexes.equals(other.indexes)) {
 			if(this.attributes.size() > 0) {
 				ModelAttribute[] aa = this.attributes.values().toArray(new ModelAttribute[this.attributes.size()]);
 				ModelAttribute[] ab = other.attributes.values().toArray(new ModelAttribute[other.attributes.size()]);
 				for(int i = 0; i < aa.length; i++) {
 					if(!aa[i].getProperties().equals(ab[i].getProperties())) {
 						return false;
 					}
 				}
 			}
 			if(this.hasOne.size() > 0) {
 				ModelRelation[] ra = this.hasOne.values().toArray(new ModelRelation[this.hasOne.size()]);
 				ModelRelation[] rb = other.hasOne.values().toArray(new ModelRelation[other.hasOne.size()]);
 				for(int i = 0; i < ra.length; i++) {
 					if(!ra[i].getProperties().equals(rb[i].getProperties())) {
 						return false;
 					}
 				}
 			}
 			if(this.hasMany.size() > 0) {
 				ModelRelation[] ra = this.hasMany.values().toArray(new ModelRelation[this.hasMany.size()]);
 				ModelRelation[] rb = other.hasMany.values().toArray(new ModelRelation[other.hasMany.size()]);
 				for(int i = 0; i < ra.length; i++) {
 					if(!ra[i].getProperties().equals(rb[i].getProperties())) {
 						return false;
 					}
 				}
 			}
 			if(this.validations.size() > 0) {
 				ModelValidation[] va = this.validations.values().toArray(new ModelValidation[this.validations.size()]);
 				ModelValidation[] vb = other.validations.values().toArray(new ModelValidation[other.validations.size()]);
 				for(int i = 0; i < va.length; i++) {
 					if(!va[i].getProperties().equals(vb[i].getProperties())) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public ModelAttribute getAttribute(String name) {
 		return attributes.get(name);
 	}
 	
 	public List<ModelAttribute> getAttributes() {
 		return getAttributes(true);
 	}
 
 	public List<ModelAttribute> getAttributes(boolean includeTemporal) {
 		List<ModelAttribute> attrs = new ArrayList<ModelAttribute>(attributes.values());
 
 		if(includeTemporal) {
 			if(datestamps) {
 				for(ModelAttribute attr : getDatestampFields()) {
 					attrs.add(attr);
 				}
 			}
 			if(timestamps) {
 				for(ModelAttribute attr : getTimestampFields()) {
 					attrs.add(attr);
 				}
 			}
 		}
 		return attrs;
 	}
 	
 	public String getCanonicalName() {
 		return type;
 	}
 
 	public String getControllerName() {
 		return controllerSimpleName(type);
 	}
 	
 	public ModelDefinition getCopy() {
 		return new ModelDefinition(this);
 	}
 	
 	public ModelAttribute[] getDatestampFields() {
 		return new ModelAttribute[] {
 				new ModelAttribute(this, "createdOn"),
 				new ModelAttribute(this, "updatedOn")
 		};
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public File getFile(File srcFolder, String extension) {
 		String name = getSimpleName();
 		if(extension != null && extension.length() > 0) {
 			if(extension.charAt(0) == '.') {
 				if(extension.length() > 1) {
 					name = name + extension;
 				}
 			} else {
 				name = name + "." + extension;
 			}
 		}
 		String folder = getPackageName().replace('.', File.separatorChar);
 		return new File(srcFolder, folder + File.separator + name);
 	}
 	
 	public List<ModelRelation> getHasManys() {
 		return new ArrayList<ModelRelation>(hasMany.values());
 	}
 	
 	public List<ModelRelation> getHasOnes() {
 		return new ArrayList<ModelRelation>(hasOne.values());
 	}
 
 	public List<String> getIndexes() {
 		return new ArrayList<String>(indexes);
 	}
 	
 	public String getIndexesAnnotation() {
 		if(indexes.isEmpty()) {
 			return null;
 		}
 		
 		if(indexes.size() == 1) {
 			return "@Indexes(\"" + indexes.get(0) + "\")";
 		}
 		
 		return join("@Indexes({\"", indexes, "\"\n})", "\"\n, \"");
 	}
 	
 	public String getModelDescriptionAnnotation() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("@ModelDescription(");
 		boolean first = true;
 		if(!attributes.isEmpty()) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			}
 			sb.append("\tattrs = {\n");
 			for(Iterator<ModelAttribute> iter = attributes.values().iterator(); iter.hasNext(); ) {
 				sb.append("\t\t").append(iter.next());
 				if(iter.hasNext()) sb.append(',');
 				sb.append('\n');
 			}
 			sb.append("\t}");
 		}
 		if(!hasOne.isEmpty()) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\thasOne = {\n");
 			for(Iterator<ModelRelation> iter = hasOne.values().iterator(); iter.hasNext(); ) {
 				sb.append("\t\t").append(iter.next());
 				if(iter.hasNext()) sb.append(',');
 				sb.append('\n');
 			}
 			sb.append("\t}");
 		}
 		if(!hasMany.isEmpty()) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\thasMany = {\n");
 			for(Iterator<ModelRelation> iter = hasMany.values().iterator(); iter.hasNext(); ) {
 				sb.append("\t\t").append(iter.next());
 				if(iter.hasNext()) sb.append(',');
 				sb.append('\n');
 			}
 			sb.append("\t}");
 		}
 		if(datestamps) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\tdatestamps = true");
 		}
 		if(timestamps) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\ttimestamps = true");
 		}
 		if(!allowUpdate) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\tallowUpdate = false");
 		}
 		if(!allowDelete) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\tallowDelete = true");
 		}
 		if(embedded) {
 			if(first) {
 				first = false;
 				sb.append("\n");
 			} else {
 				sb.append(",\n");
 			}
 			sb.append("\tembedded = true");
 		}
 		if(!first) {
 			sb.append('\n');
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 
 	public List<String> getModelDescriptionImports() {
 		List<String> imports = new ArrayList<String>();
 		if(hasAttributes(false)) {
 			imports.add(Attribute.class.getCanonicalName());
 			for(ModelAttribute attr : attributes.values()) {
 				if(!attr.type().startsWith("java.lang") && !primitives.contains(attr.type())) {
 					imports.add(attr.type());
 				}
 			}
 		}
 		if(hasRelations()) {
 			imports.add(Relation.class.getCanonicalName());
 			Pattern p = Pattern.compile("import\\s+static\\s+(" + Relation.class.getCanonicalName().replace(".", "\\.") + "\\.[\\*\\w]+)\\s*;");
 			Matcher m = p.matcher(source);
 			while(m.find()) {
 				imports.add("static " + m.group(1));
 			}
 			for(ModelRelation r : hasOne.values()) {
 				p = Pattern.compile("import\\s+" + r.type() + "\\s*;");
 				m = p.matcher(source);
 				if(m.find()) {
 					imports.add(r.type());
 				}
 			}
 			for(ModelRelation r : hasMany.values()) {
 				p = Pattern.compile("import\\s+" + r.type() + "\\s*;");
 				m = p.matcher(source);
 				if(m.find()) {
 					imports.add(r.type());
 				}
 			}
 		}
 		return imports;
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
 		for(Entry<String, ModelRelation> entry : hasOne.entrySet()) {
 			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
 		}
 		for(Entry<String, ModelRelation> entry : hasMany.entrySet()) {
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
 	
 	public ModelRelation getRelation(String field) {
 		ModelRelation r = hasOne.get(field);
 		if(r == null) {
 			r = hasMany.get(field);
 		}
 		return r;
 	}
 	
 	public List<ModelRelation> getRelations() {
 		List<ModelRelation> relations = new ArrayList<ModelRelation>();
 		relations.addAll(hasOne.values());
 		relations.addAll(hasMany.values());
 		return relations;
 	}
 	
 	protected String[] getSiblings() {
 		if(siblings != null && siblings.length > 0) {
 			return siblings;
 		}
 		String[] sa = (file != null) ? file.getParentFile().list() : new String[0];
 		if(sa == null) {
 			return new String[0];
 		}
 		return sa;
 	}
 
 	public String getSimpleName() {
 		return simpleName(type);
 	}
 	
 	public ModelAttribute[] getTimestampFields() {
 		return new ModelAttribute[] {
 				new ModelAttribute(this, "createdAt"),
 				new ModelAttribute(this, "updatedAt")
 		};
 	}
 	
 	String getType(String name) {
 		String type = null;
 		if(name.endsWith(".class")) name = name.substring(0, name.length() - 6);
 		boolean array = name.endsWith("[]");
 		if(array) name = name.substring(0, name.length()-2);
 		
 		int ix = name.indexOf('.');
 		if(ix != -1) {
 			String seg1 = name.substring(0, ix);
 //			if(seg1.equals("com") || seg1.equals("org") || seg1.equals("java") || seg1.equals("javax")) {
 //				// assume it is fully qualified and exit
 //				return name;
 //			}
 			// handle inner classes
 			name = getType(seg1) + name.substring(ix);
 			if(name.startsWith("java.lang.")) {
 				name = name.substring(10);
 			}
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
 			if("String".equals(name)) {
 				type = "java.lang.String";
 			} else {
 				String src = name + ".java"; // TODO will be ".class" if this is in a jar
 				String[] siblings = getSiblings();
 				for(String sibling : siblings) {
 					if(src.equals(sibling)) {
 						type = (packageName != null) ? (packageName + "." + name) : name;
 						break;
 					}
 				}
 				
 				if(type == null) {
 					p = Pattern.compile("import\\s+(static\\s+)?([\\w\\d\\._\\$]+)\\.\\*;");
 					m = p.matcher(source);
 					while(m.find()) {
 						try {
 							Class<?> c = Class.forName(m.group(2));
 							if(c.getField(name) != null) {
 								return m.group(2);
 							}
 						} catch(Exception e) {
 							// try again
 						}
 					}
 				}
 				
 				if(type == null) {
 					type = "java.lang." + name;
 				}
 			}
 		}
 		if(array) {
 			return type + "[]";
 		}
 		return type;
 	}
 
 	public ModelValidation getValidation(String field) {
 		return validations.get(field);
 	}
 	
 	public List<ModelValidation> getValidations() {
 		return new ArrayList<ModelValidation>(validations.values());
 	}
 	
 	public String getValidationsAnnotation() {
 		if(validations.isEmpty()) {
 			return null;
 		}
 		
 		if(validations.size() == 1) {
 			return "@Validations(" + validations.values().iterator().next() + ")";
 		}
 		
 		// TODO improve validations packing routine
 
 		List<String> fields = new ArrayList<String>(validations.keySet());
 		Set<String> found = new HashSet<String>();
 		Map<ModelValidation, Set<String>> m1 = new HashMap<ModelValidation, Set<String>>();
 		for(int i = 0; i < fields.size(); i++) {
 			String field = fields.get(i);
 			if(found.contains(field)) {
 				continue;
 			}
 			ModelValidation current = validations.get(field);
 			Set<String> set = new TreeSet<String>();
 			set.add(current.field());
 			m1.put(current, set);
 			for(int j = 0; j < fields.size(); j++) {
 				if(i != j) {
 					field = fields.get(j);
 					if(found.contains(field)) {
 						continue;
 					}
 					ModelValidation validation = validations.get(field);
 					if(current.getCustomProperties().equals(validation.getCustomProperties())) {
 						m1.get(current).add(field);
 						found.add(field);
 					}
 				}
 			}
 		}
 		
 		Map<String, ModelValidation> m2 = new TreeMap<String, ModelValidation>();
 		for(Entry<ModelValidation, Set<String>> entry : m1.entrySet()) {
 			m2.put(join(entry.getValue(), ','), entry.getKey());
 		}
 
 		if(m2.size() == 1) {
 			Entry<String, ModelValidation> e = m2.entrySet().iterator().next();
 			return "@Validations(" + e.getValue().toString(e.getKey()) + ")";
 		}
 		else {
 			StringBuilder sb = new StringBuilder();
 			sb.append("@Validations({\n");
 			for(Iterator<Entry<String, ModelValidation>> iter = m2.entrySet().iterator(); iter.hasNext(); ) {
 				Entry<String, ModelValidation> e = iter.next();
 				sb.append('\t').append(e.getValue().toString(e.getKey()));
 				if(iter.hasNext()) {
 					sb.append(",\n");
 				} else {
 					sb.append('\n');
 				}
 			}
 			sb.append("})");
 			return sb.toString();
 		}
 	}
 	
 	private List<String> getValidationsImports() {
 		if(validations.isEmpty()) {
 			return new ArrayList<String>(0);
 		} else {
 			List<String> imports = new ArrayList<String>();
 			imports.add(Validations.class.getCanonicalName());
 			imports.add(Validate.class.getCanonicalName());
 			return imports;
 		}
 	}
 
 	public boolean hasAttribute(String field) {
 		if(attributes.containsKey(field)) {
 			return true;
 		}
 		if(timestamps && (ModelDescription.CREATED_AT.equals(field) || ModelDescription.UPDATED_AT.equals(field))) {
 			return true;
 		}
 		if(datestamps && (ModelDescription.CREATED_ON.equals(field) || ModelDescription.UPDATED_ON.equals(field))) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean hasAttributes() {
 		return hasAttributes(true);
 	}
 	
 	public boolean hasAttributes(boolean includeTemporal) {
 		if(includeTemporal) {
 			return !attributes.isEmpty() || timestamps || datestamps;
 		}
 		return !attributes.isEmpty();
 	}
 	
 	public boolean hasField(String name) {
 		return attributes.containsKey(name) || hasOne.containsKey(name) || hasMany.containsKey(name);
 	}
 	
 	public boolean hasMany() {
 		return !hasMany.isEmpty();
 	}
 	
 	public boolean hasMany(String field) {
 		return hasMany.containsKey(field);
 	}
 	
 	public boolean hasOne() {
 		return !hasOne.isEmpty();
 	}
 	
 	public boolean hasOne(String field) {
 		return hasOne.containsKey(field);
 	}
 
 	public boolean hasRelation(String name) {
 		return hasOne.containsKey(name) || hasMany.containsKey(name);
 	}
 	
 	public boolean hasRelations() {
 		return !hasOne.isEmpty() || !hasMany.isEmpty();
 	}
 
 	public boolean hasValidation(String field) {
 		return validations.containsKey(field);
 	}
 	
 	public boolean hasValidations() {
 		return !validations.isEmpty();
 	}
 	
 	public boolean isNew() {
 		return file == null || !file.isFile();
 	}
 
 	public boolean isThrough(String field) {
 		ModelRelation relation = hasOne.get(field);
 		if(relation != null) {
 			return relation.isThrough();
 		}
 		relation = hasMany.get(field);
 		if(relation != null) {
 			return relation.isThrough();
 		}
 		return false;
 	}
 	
 	public void load() {
 		attributes.clear();
 		hasOne.clear();
 		hasMany.clear();
 		indexes.clear();
 
 		if(file != null && file.isFile()) {
 			source = readFile(file).toString();
 		}
 		
 		parse();
 	}
 
 	public String packageName() {
 		return packageName;
 	}
 	
 	private void parse() {
 		parse(false);
 	}
 	
 	private void parse(boolean positionsOnly) {
 		char[] ca = source.toCharArray();
 
 		mdstart = ixstart = mvstart = findStart(getSimpleName(), ca);
 		mdend   = ixend   = mvend   = -1;
 		
 		int s0 = findAll(ca, 0, MODEL_DESCRIPTION);
 		if(s0 != -1) {
 			int s1 = find(ca, '(', s0);
 			if(s1 != -1) {
 				int s2 = closer(ca, s1);
 				if(s2 != -1) {
 					mdstart = s0;
 					mdend = s2+1;
 					if(!positionsOnly) {
 						parseDescription(ca, s1+1, s2);
 					}
 				}
 			}
 		}
 
 		s0 = findAll(ca, 0, INDEXES);
 		if(s0 != -1) {
 			int s1 = find(ca, '(', s0);
 			if(s1 != -1) {
 				int s2 = closer(ca, s1);
 				if(s2 != -1) {
 					ixstart = s0;
 					ixend = s2+1;
 					if(!positionsOnly) {
 						parseIndexes(ca, s1+1, s2);
 					}
 				}
 			}
 		}
 
 		s0 = findAll(ca, 0, VALIDATIONS);
 		if(s0 != -1) {
 			int s1 = find(ca, '(', s0);
 			if(s1 != -1) {
 				int s2 = closer(ca, s1);
 				if(s2 != -1) {
 					mvstart = s0;
 					mvend = s2+1;
 					if(!positionsOnly) {
 						parseValidations(ca, s1+1, s2);
 					}
 				}
 			}
 		}
 	}
 
 	private void parseAttributes(String attribute) {
 		char[] ca = attribute.toCharArray();
 		for(String attr : getJavaArguments(ca, 1, ca.length-1)) {
 			addAttribute(attr);
 		}
 	}
 	
 	private void parseDescription(char[] ca, int start, int end) {
 		Map<String, String> parameters = getJavaEntries(ca, start, end);
 		
 		String v;
 		
 		v = parameters.get("attrs");
 		if(v != null) {
 			parseAttributes(v);
 		}
 		
 		v = parameters.get("hasOne");
 		if(v != null) {
 			parseRelations(v, false);
 		}
 
 		v = parameters.get("hasMany");
 		if(v != null) {
 			parseRelations(v, true);
 		}
 		
 		datestamps = coerce(parameters.get("datestamps")).from(DEFAULT_DATESTAMPS);
 		timestamps = coerce(parameters.get("timestamps")).from(DEFAULT_TIMESTAMPS);
 		allowUpdate = coerce(parameters.get("allowUpdate")).from(DEFAULT_ALLOW_UPDATE);
 		allowDelete = coerce(parameters.get("allowDelete")).from(DEFAULT_ALLOW_DELETE);
 		embedded = coerce(parameters.get("embedded")).from(DEFAULT_EMBEDDED);
 	}
 	
 	private void parseIndexes(char[] ca, int start, int end) {
 		String s = new String(ca, start, end-start+1).trim();
 		indexes.addAll(JsonUtils.toStringList(s));
 	}
 
 	private String parsePackageName() {
 		Matcher m = packagePattern.matcher(source);
 		if(m.find()) {
 			return m.group(1);
 		}
 		return null;
 	}
 
 	private void parseRelations(String relations, boolean hasMany) {
 		char[] ca = relations.toCharArray();
 		for(String relation : getJavaArguments(ca, 1, ca.length-1)) {
 			addRelation(relation, hasMany);
 		}
 	}
 
 	private String parseType(String name) {
 		int ix = name.indexOf('.');
 		if(ix != -1) {
 			name = name.substring(0, ix);
 		}
 		if(packageName != null) {
 			return packageName + "." + name;
 		}
 		return name;
 	}
 
 	private void parseValidations(char[] ca, int start, int end) {
 		int s1 = find(ca, '{', start, end);
 		int s2 = (s1 == -1) ? end : closer(ca, s1, end);
 		if(s1 == -1) {
 			s1 = start;
 		} else {
 			s1++;
 			s2--;
 		}
 		for(String v : getJavaArguments(ca, s1, s2)) {
 			addValidations(v);
 		}
 	}
 
 	public boolean remove(String field) {
 		validations.remove(field);
 		if(attributes.remove(field) != null) {
 			return true;
 		}
 		if(hasOne.remove(field) != null) {
 			return true;
 		}
 		if(hasMany.remove(field) != null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeValidation(String field) {
 		return (validations.remove(field) != null);
 	}
 
 	public void save() {
 		if(file == null) {
 			throw new IllegalArgumentException("cannot save: file is null");
 		}
 		if(file.isDirectory()) {
 			throw new IllegalArgumentException("cannot save: file is a directory");
 		}
 
 		if(file.isFile()) {
 			// pick up any class changes that may have occurred
 			// any changes to ModelDescription, Validations, or Indexes will be overwritten
 			source = readFile(file).toString();
 		}
 
 		// reset the positions, in case any changes occurred
 		parse(true);
 		
 		Edits edits = new Edits();
 		edits.add(mdstart, mdend, getModelDescriptionAnnotation());
 		edits.add(mvstart, mvend, getValidationsAnnotation());
 		edits.add(ixstart, ixend, getIndexesAnnotation());
 
 		List<String> imports = new ArrayList<String>();
 		imports.addAll(getModelDescriptionImports());
 		imports.addAll(getValidationsImports());
 		
 		StringBuilder sb = new StringBuilder(source);
 		edits.apply(sb);
 		ensureImports(sb, imports);
 		writeFile(file, sb.toString());
 	}
 
 	public void save(File file) {
 		this.file = file;
 		save();
 	}
 	
 	public void setAttributeOrder(String[] names) {
 		Map<String, ModelAttribute> tmp = new HashMap<String, ModelAttribute>(attributes);
 		attributes.clear();
 		for(String name : names) {
 			attributes.put(name, tmp.get(name));
 		}
 	}
 
 	public void setFile(File file) {
 		this.file = file;
 	}
 
 	public void setOpposites(ModelDefinition[] models) {
 		for(ModelRelation relation : hasOne.values()) {
 			relation.setOpposite(models);
 		}
 		for(ModelRelation relation : hasMany.values()) {
 			relation.setOpposite(models);
 		}
 	}
 	
 	public void setPackageName(String packageName) {
 		this.type = packageName + "." + getSimpleName();
 		this.packageName = packageName;
 		source = source.replaceFirst(packageRegex, "package " + packageName + ";");
 	}
 	
 	public void setSiblings(String...siblings) {
 		this.siblings = siblings;
 	}
 
 	public String[] siblings() {
 		return siblings;
 	}
 
 	public ModelDefinition siblings(String[] siblings) {
 		this.siblings = siblings;
 		return this;
 	}
 
 	public boolean timestamps() {
 		return timestamps;
 	}
 
 	public ModelDefinition timestamps(boolean timestamps) {
 		this.timestamps = timestamps;
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return type + " => {" + "}";
 	}
 
 	public String type() {
 		return type;
 	}
 	
 }
