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
 
 import static org.oobium.persist.Relation.*;
 import static org.oobium.build.model.ModelDefinition.getJavaEntries;
 import static org.oobium.build.model.ModelDefinition.getString;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.simpleName;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.oobium.persist.Relation;
 
 public class ModelRelation {
 
 	private final ModelDefinition model;
 
 	private final boolean hasMany;
 	private String name;
 	private String type;
 	private String opposite;
 	private String through;
 	private boolean readOnly;
 	private boolean unique;
 	private boolean virtual;
 	private int dependent;
 	private int onDelete;
 	private int onUpdate;
 	private String embed;
 	private boolean embedded;
 	private boolean include;
 	private ModelRelation oppositeRelation;
 	
 	public ModelRelation(ModelDefinition model, String annotation, boolean hasMany) {
 		this.model = model;
 		this.hasMany = hasMany;
 
 		char[] ca = annotation.toCharArray();
 		int start = annotation.indexOf('(') + 1;
 		int end = annotation.length() - 1;
 		Map<String, String> entries = getJavaEntries(ca, start, end);
 		
 		name(getString(entries.get("name")));
 		type(model.getType(entries.get("type")));
 		opposite(getString(entries.get("opposite")));
 		through(getString(entries.get("through")));
 		readOnly(coerce(entries.get("readOnly"), DEFAULT_READONLY));
 		unique(coerce(entries.get("unique"), DEFAULT_UNIQUE));
 		virtual(coerce(entries.get("virtual"), DEFAULT_VIRTUAL));
 		dependent(getReferential(entries.get("dependent"), DEFAULT_DEPENDENT));
 		onDelete(getReferential(entries.get("onDelete"), DEFAULT_ONDELETE));
 		onUpdate(getReferential(entries.get("onUpdate"), DEFAULT_ONUPDATE));
 		embed(getString(entries.get("embed")));
 		embedded(coerce(entries.get("embedded"), DEFAULT_EMBEDDED));
 		include(coerce(entries.get("include"), DEFAULT_INCLUDE));
 	}
 
 	private ModelRelation(ModelRelation original, ModelDefinition model, boolean hasMany) {
 		this.model = model;
 		this.hasMany = hasMany;
 		name(original.name);
 		type(original.type);
 		opposite(original.opposite);
 		through(original.through);
 		readOnly(original.readOnly);
 		unique(original.unique);
 		virtual(original.virtual);
 		dependent(original.dependent);
 		onDelete(original.onDelete);
 		onUpdate(original.onUpdate);
 		embed(original.embed);
 		embedded(original.embedded);
 		include(original.include);
 	}
 	
 	public int dependent() {
 		return dependent;
 	}
 	
 	public ModelRelation dependent(int dependent) {
 		this.dependent = dependent;
 		return this;
 	}
 	
 	public String embed() {
 		return embed;
 	}
 	
 	public ModelRelation embed(String embed) {
 		this.embed = (embed == null) ? DEFAULT_EMBED : embed;
 		return this;
 	}
 	
 	public boolean embedded() {
 		return embedded;
 	}
 	
 	public ModelRelation embedded(boolean embedded) {
 		this.embedded = embedded;
 		return this;
 	}
 
 	public ModelRelation getCopy() {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public ModelRelation getCopy(boolean hasMany) {
 		return new ModelRelation(this, model, hasMany);
 	}
 	
 	public ModelRelation getCopy(ModelDefinition model) {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public ModelRelation getCopy(ModelDefinition model, boolean hasMany) {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public Map<String, Object> getCustomProperties() {
 		// when updating this method, make sure to also update #hasCustomProperties()
 		Map<String, Object> props = new HashMap<String, Object>();
 		if(!opposite.equals(DEFAULT_OPPOSITE)) {
 			props.put("opposite", opposite);
 		}
 		if(!through.equals(DEFAULT_THROUGH)) {
 			props.put("through", through);
 		}
 		if(readOnly != DEFAULT_READONLY) {
 			props.put("readOnly", readOnly);
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			props.put("unique", unique);
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			props.put("virtual", virtual);
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			props.put("dependent", dependent);
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			props.put("onDelete", onDelete);
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			props.put("onUpdate", onUpdate);
 		}
 		if(!embed.equals(DEFAULT_EMBED)) {
 			props.put("embed", embed);
 		}
 		if(embedded != DEFAULT_EMBEDDED) {
 			props.put("embedded", embedded);
 		}
 		if(include != DEFAULT_INCLUDE) {
 			props.put("include", include);
 		}
 		return props;
 	}
 	
 	public Map<String, Object> getProperties() {
 		Map<String, Object> props = getCustomProperties();
 		props.put("name", name);
 		props.put("type", type);
 		props.put("hasMany", hasMany);
 		return props;
 	}
 	
 	public ModelRelation getOpposite() {
 		return oppositeRelation;
 	}
 	
 	private int getReferential(String referential, int defaultValue) {
 		try {
 			return coerce(referential, defaultValue);
 		} catch(Exception e) {
 			String constant;
 			String type;
 			int ix = referential.lastIndexOf('.');
 			if(ix == -1) {
 				constant = referential;
 				type = model.getType(constant);
 			} else {
 				constant = referential.substring(ix+1);
 				type = model.getType(referential.substring(0, ix));
 			}
 			try {
 				Class<?> c = Class.forName(type);
 				Field f = c.getField(constant);
 				return f.getInt(c);
 			} catch(Exception e2) {
 				return defaultValue;
 			}
 		}
 	}
 	
 	public String getSimpleType() {
 		return simpleName(type);
 	}
 	
 	public boolean hasCustomProperties() {
 		// when updating this method, make sure to also update #getCustomProperties()
 		if(!opposite.equals(DEFAULT_OPPOSITE)) {
 			return true;
 		}
 		if(!through.equals(DEFAULT_THROUGH)) {
 			return true;
 		}
 		if(readOnly != DEFAULT_READONLY) {
 			return true;
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			return true;
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			return true;
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			return true;
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			return true;
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			return true;
 		}
 		if(!embed.equals(DEFAULT_EMBED)) {
 			return true;
 		}
 		if(embedded != DEFAULT_EMBEDDED) {
 			return true;
 		}
 		if(include != DEFAULT_INCLUDE) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean hasMany() {
 		return hasMany;
 	}
 	
 	public boolean hasOpposite() {
 		return opposite != null && opposite.length() > 0;
 	}
 	
 	public boolean include() {
 		return include;
 	}
 	
 	public ModelRelation include(boolean include) {
 		this.include = include;
 		return this;
 	}
 
 	public boolean isThrough() {
 		return !blank(through);
 	}
 	
 	public ModelDefinition model() {
 		return model;
 	}
 	
 	public String name() {
 		return name;
 	}
 	
 	public ModelRelation name(String name) {
 		if(name == null) {
 			throw new IllegalArgumentException("name cannot be null");
 		}
 		this.name = name;
 		return this;
 	}
 	
 	public int onDelete() {
 		return onDelete;
 	}
 
 	public ModelRelation onDelete(int onDelete) {
 		this.onDelete = onDelete;
 		return this;
 	}
 
 	public int onUpdate() {
 		return onUpdate;
 	}
 
 	public ModelRelation onUpdate(int onUpdate) {
 		this.onUpdate = onUpdate;
 		return this;
 	}
 
 	public String opposite() {
 		return opposite;
 	}
 	
 	public ModelRelation opposite(String opposite) {
 		this.opposite = (opposite == null) ? DEFAULT_OPPOSITE : opposite;
 		return this;
 	}
 	
 	public boolean readOnly() {
 		return readOnly;
 	}
 	
 	public ModelRelation readOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 		return this;
 	}
 	
 	void setOpposite(ModelDefinition[] models) {
 		if(hasOpposite() && oppositeRelation == null) {
 			for(ModelDefinition model : models) {
 				if(type.equals(model.getCanonicalName())) {
 					oppositeRelation = model.getRelation(opposite);
 					if(oppositeRelation != null) {
 						oppositeRelation.oppositeRelation = this;
 					}
 				}
 			}
 		}
 	}
 	
 	public String through() {
 		return through;
 	}
 	
 	public ModelRelation through(String through) {
 		this.through = (through == null) ? DEFAULT_THROUGH : through;
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append('@').append(Relation.class.getSimpleName()).append('(');
 		sb.append("name=\"").append(name).append("\"");
 		sb.append(", type=").append(getSimpleType()).append(".class");
 		if(!through.equals(DEFAULT_THROUGH)) {
 			sb.append(", through=\"").append(through).append('"');
 		} else {
 			if(!opposite.equals(DEFAULT_OPPOSITE)) {
 				sb.append(", opposite=\"").append(opposite).append('"');
 			}
 			if(embedded != DEFAULT_EMBEDDED) {
 				sb.append(", embedded=true");
 			} else if(!embed.equals(DEFAULT_EMBED)) {
 				sb.append(", embed=\"").append(embed).append('"');
 			}
 			if(readOnly) {
 				sb.append(", readOnly=true");
 			}
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			sb.append(", unique=true");
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			sb.append(", virtual=true");
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			sb.append(", dependent=").append(dependent);
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			sb.append(", onDelete=").append(onDelete);
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			sb.append(", onUpdate=").append(onUpdate);
 		}
 		if(include != DEFAULT_INCLUDE) {
 			sb.append(", include=true");
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 	
 	public String type() {
 		return type;
 	}
 	
 	public ModelRelation type(String type) {
 		if(type == null) {
 			throw new IllegalArgumentException("type cannot be null");
 		}
 		this.type = type;
 		return this;
 	}
 	
 	public boolean unique() {
 		return unique;
 	}
 	
 	public ModelRelation unique(boolean unique) {
 		this.unique = unique;
 		return this;
 	}
 
 	public boolean virtual() {
 		return virtual;
 	}
 
 	public ModelRelation virtual(boolean virtual) {
 		this.virtual = virtual;
 		return this;
 	}
 
 	
 }
