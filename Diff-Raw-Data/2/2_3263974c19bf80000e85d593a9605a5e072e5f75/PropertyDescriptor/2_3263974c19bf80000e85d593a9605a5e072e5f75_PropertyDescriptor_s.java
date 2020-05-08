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
 package org.oobium.build.gen.model;
 
 import static org.oobium.utils.StringUtils.varName;
 
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.build.model.ModelAttribute;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.model.ModelRelation;
 import org.oobium.persist.ModelList;
 import org.oobium.utils.StringUtils;
 
 public class PropertyDescriptor {
 
 	private String modelType;
 	private String rawType;
 	private String type;
 	private String fullType;
 	private String castType;
 	private String typePackage;
 	private String variable;
 	private String init;
 	private String getterName;
 	private String hasserName;
 	private String setterName;
 	private String enumProp;
 
 	private String check;
 	
 	private PropertyBuilder builder;
 	
 	private boolean array;
 	private boolean primitive;
 	
 	private boolean readOnly;
 	private boolean unique;
 	private boolean virtual;
 
 	private ModelDefinition model;
 	private ModelRelation opposite;
 	private boolean isAttr;
 	private boolean hasOne;
 	private boolean hasMany;
 	private String relatedType;
 	private String through;
 
 	public PropertyDescriptor(ModelAttribute attribute) {
 		this(attribute.model().getSimpleName(), attribute.getJavaType(), attribute.name());
 
 		rawType = attribute.type();
 		
 		init = attribute.init();
 		
 		getterName = StringUtils.getterName(variable);
 		
 		check = attribute.check();
 		readOnly = attribute.readOnly();
 		unique = attribute.unique();
 		virtual = attribute.virtual();
 		
 		isAttr = true;
 		hasOne = false;
 		hasMany = false;
 		relatedType = null;
 
 		builder = new AttributeBuilder(this);
 	}
 
 	public PropertyDescriptor(ModelRelation relation) {
 		this(relation.model().getSimpleName(), relation.type(), relation.name());
 
 		init = null;
 		
 		model = relation.model();
 		opposite = relation.getOpposite();
 		
 		readOnly = relation.readOnly();
 		unique = relation.unique();
 		virtual = relation.virtual();
 
 		isAttr = false;
 		hasOne = !relation.hasMany();
 		hasMany = relation.hasMany();
 		relatedType = relation.type();
 		through = relation.through();
 
 		if(relation.hasMany()) {
 			castType = ModelList.class.getSimpleName();
 			getterName = variable;
 		} else {
 			getterName = StringUtils.getterName(variable);
 		}
 
 		builder = relation.hasMany() ? new HasManyBuilder(this) : new HasOneBuilder(this);
 	}
 
 	PropertyDescriptor(String modelType, String type, String name) {
 		this.modelType = modelType;
 
 		this.type = type.substring(type.lastIndexOf('.')+1);
 		variable = name;
 		fullType = type;
 		castType = this.type;
 		enumProp = StringUtils.constant(variable);
 		hasserName = StringUtils.hasserName(variable);
 		setterName = StringUtils.setterName(variable);
 
 		int ix = type.lastIndexOf('.');
 		array = type.endsWith("[]");
 		if(ix != -1 || array) {
 			typePackage = (ix == -1) ? null : type.substring(0, ix);
 		} else {
 			primitive = true;
 		}
 	}
 	
 	public String relatedType() {
 		return relatedType;
 	}
 
 	/**
 	 * @return new String[] { field, subfield }
 	 */
 	public String[] through() {
 		if(isThrough()) {
 			int ix = through.indexOf(':');
 			if(ix == -1) {
				return new String[] { through, varName(type(), hasMany) };
 			} else {
 				return new String[] { through.substring(0, ix), through.substring(ix + 1) };
 			}
 		}
 		return null;
 	}
 	
 	public String castType() {
 		return castType;
 	}
 
 	public String enumProp() {
 		return enumProp;
 	}
 
 	public String rawType() {
 		return rawType;
 	}
 	
 	public String fullType() {
 		return fullType;
 	}
 
 	public String getCheck() {
 		return check;
 	}
 	
 	public String getterName() {
 		return getterName;
 	}
 	
 	public boolean hasCheck() {
 		return check != null && check.length() > 0;
 	}
 	
 	public String hasserName() {
 		return hasserName;
 	}
 
 	public String init() {
 		return init;
 	}
 	
 	public boolean isAttr() {
 		return isAttr;
 	}
 	
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 	
 	public boolean isThrough() {
 		return through != null && through.length() > 0;
 	}
 	
 	public boolean isUnique() {
 		return unique;
 	}
 
 	public boolean isVirtual() {
 		return virtual;
 	}
 
 	public boolean hasDeclaration() {
 		return !virtual;
 	}
 
 	public boolean hasEnum() {
 		return true;
 	}
 
 	public boolean hasGetter() {
 		return !"void".equals(type);
 	}
 
 	public boolean hasImport() {
 		return typePackage != null;
 	}
 
 	public boolean hasInit() {
 		return init != null && !init.isEmpty();
 	}
 	
 	public boolean hasOpposite() {
 		return opposite != null;
 	}
 	
 	public boolean hasOne() {
 		return hasOne;
 	}
 	
 	public boolean hasMany() {
 		return hasMany;
 	}
 	
 	public boolean hasSetter() {
 		return !readOnly;
 	}
 	
 	public List<String> imports() {
 		return builder.getImports();
 	}
 
 	public boolean isPrimitive() {
 		return primitive;
 	}
 
 	public boolean isType(Class<?> clazz) {
 		return this.type.equals(clazz.getSimpleName());
 	}
 
 	public Map<String, String> methods() {
 		return builder.getMethods();
 	}
 	
 	public ModelDefinition model() {
 		return model;
 	}
 	
 	public String modelType() {
 		return modelType;
 	}
 	
 	public ModelRelation opposite() {
 		return opposite;
 	}
 	
 	public String setterName() {
 		return setterName;
 	}
 
 	public String type() {
 		return type;
 	}
 	
 	public String typePackage() {
 		return typePackage;
 	}
 
 	public String variable() {
 		return variable;
 	}
 
 	public Map<String, String> variables() {
 		return builder.getDeclarations();
 	}
 
 }
