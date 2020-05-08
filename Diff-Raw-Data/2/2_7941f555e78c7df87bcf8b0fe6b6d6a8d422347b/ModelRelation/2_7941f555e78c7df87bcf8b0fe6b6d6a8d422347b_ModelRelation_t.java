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
 
 import static org.oobium.utils.StringUtils.*;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.build.model.ModelDefinition.getJavaEntries;
 import static org.oobium.build.model.ModelDefinition.getString;
 
 import java.lang.reflect.Field;
 import java.util.Map;
 
 import org.oobium.persist.Relation;
 
 public class ModelRelation {
 
 	public final ModelDefinition model;
 
 	public final boolean hasMany;
 	public final int limit;
 	public final String name;
 	public final String type;
 	public final String opposite;
 	public final String through;
 	public final boolean readOnly;
 	public final boolean required;
 	public final boolean unique;
 	public final boolean virtual;
 	public final int onDelete;
 	public final int onUpdate;
 	private ModelRelation oppositeRelation;
 	
 	public ModelRelation(ModelDefinition model, String annotation, boolean hasMany) {
 		this.model = model;
 		this.hasMany = hasMany;
 
 		char[] ca = annotation.toCharArray();
 		int start = annotation.indexOf('(') + 1;
 		int end = annotation.length() - 1;
 		Map<String, String> entries = getJavaEntries(ca, start, end);
 		
 		this.name = getString(entries.get("name"));
 		this.type = model.getType(entries.get("type"));
 		this.limit = coerce(entries.get("limit"), -1);
 		this.opposite = getString(entries.get("opposite"));
 		this.through = getString(entries.get("through"));
 		this.readOnly = coerce(entries.get("readOnly"), false);
 		this.required = coerce(entries.get("required"), false);
 		this.unique = coerce(entries.get("unique"), false);
 		this.virtual = coerce(entries.get("virtual"), false);
 		this.onDelete = getReferential(entries.get("onDelete"));
 		this.onUpdate = getReferential(entries.get("onUpdate"));
 	}
 	
 	public ModelRelation getOpposite() {
 		return oppositeRelation;
 	}
 	
 	private int getReferential(String referential) {
 		try {
 			return coerce(referential, Relation.UNDEFINED);
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
 				return Relation.UNDEFINED;
 			}
 		}
 	}
 
 	public String getSimpleType() {
 		return simpleName(type);
 	}
 
 	public boolean hasOpposite() {
 		return opposite != null && opposite.length() > 0;
 	}
 	
 	public boolean isThrough() {
 		return !blank(through);
 	}
 
 	public boolean isUnique() {
 		return unique;
 	}
 
 	public boolean isVirtual() {
 		return virtual;
 	}
 
 	void setOpposite(ModelDefinition[] models) {
 		if(hasOpposite() && oppositeRelation == null) {
 			for(ModelDefinition model : models) {
 				if(type.equals(model.getCanonicalName())) {
 					oppositeRelation = model.relations.get(opposite);
 					if(oppositeRelation != null) {
 						oppositeRelation.oppositeRelation = this;
 					}
 				}
 			}
 		}
 	}
 
 }
