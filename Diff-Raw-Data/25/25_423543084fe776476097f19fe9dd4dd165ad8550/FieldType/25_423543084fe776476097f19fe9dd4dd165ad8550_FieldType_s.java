 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.metadata;
 
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitable;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitor;
 
 public class FieldType implements CompositeDatabaseType, DatabaseTypeVisitable {
 
     protected String fieldName;
     protected DatabaseType dataType;
     protected boolean notNull = false;
     protected boolean pk = false;
 
     public FieldType(String fieldName) {
         this.fieldName = fieldName;
     }
 
     public String getFieldName() {
         return fieldName;
     }
 
     public DatabaseType getDataType() {
         return dataType;
     }
     public void setDataType(DatabaseType dataType) {
         this.dataType = dataType;
     }
 
 	public boolean isResolved() {
         // if dataType is unresolved, then this field is unresolved
 		if (dataType == null) {
 			return false;
 		}
 		return dataType.isResolved();
 	}
 
 	public boolean isComposite() {
 		if (dataType == null) {
 			// by default, a Field is 'simple' until otherwise configured 'composite'
 			return false;
 		}
 		return dataType.isComposite();
 	}
 
     public String getTypeName() {
 		if (dataType == null) {
 			return null;
 		}
 		return dataType.getTypeName();
     }
 
     public boolean notNull() {
     	return notNull;
     }
     public void setNotNull() {
     	this.notNull = true;
     }
     public void unSetNotNull() {
     	this.notNull = false;
     }
 
     public boolean pk() {
     	return pk;
     }
     public void setPk() {
     	this.pk = true;
     }
     public void unSetPk() {
     	this.pk = false;
     }
 
 	public void addCompositeType(DatabaseType enclosedType) {
 		setDataType(enclosedType);
 	}
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(fieldName);
         sb.append(" ");
         if (dataType == null) {
             sb.append("<null/>");
         }
         else {
             sb.append(dataType.shortName());
         }
         if (notNull) {
             sb.append(" (NOT NULL)");
         }
         return sb.toString();
     }
 
    @Override
     public String shortName() {
         return toString();
     }
 
 	public void accept(DatabaseTypeVisitor visitor) {
 		visitor.visit(this);
 	}
 }
