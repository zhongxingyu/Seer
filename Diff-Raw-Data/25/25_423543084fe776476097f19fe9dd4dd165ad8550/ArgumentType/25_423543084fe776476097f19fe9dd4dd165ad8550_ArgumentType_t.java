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
 
 //DDL parser imports
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitable;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitor;
 import static org.eclipse.persistence.tools.oracleddl.metadata.ArgumentTypeDirection.RETURN;
 
 public class ArgumentType implements CompositeDatabaseType, DatabaseTypeVisitable {
 
     protected String argumentName;
     protected DatabaseType dataType;
     protected ArgumentTypeDirection direction;
     protected boolean optional = false;
 
     public ArgumentType(String argumentName) {
         this.argumentName = argumentName;
     }
 
     public String getArgumentName() {
         return argumentName;
     }
 
     public DatabaseType getDataType() {
         return dataType;
     }
     public void setDataType(DatabaseType dataType) {
         this.dataType = dataType;
     }
 
 	public ArgumentTypeDirection getDirection() {
 		return direction;
 	}
 	public void setDirection(ArgumentTypeDirection direction) {
 		this.direction = direction;
 	}
 
 	public boolean isResolved() {
         // if dataType is unresolved, then this argument is unresolved
 		if (dataType == null) {
 			return false;
 		}
 		return dataType.isResolved();
 	}
 
 	public boolean isComposite() {
 		if (dataType == null) {
 			// by default, an argument is 'simple' until otherwise configured 'composite'
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
 
     public boolean optional() {
     	return optional;
     }
     public void setOptional() {
     	optional = true;
     }
     public void unsetOptional() {
     	optional = false;
     }
 
 	public void addCompositeType(DatabaseType enclosedType) {
 		setDataType(enclosedType);
 	}
 
     public String shortName() {
         StringBuilder sb = new StringBuilder();
         if (argumentName != null) {
             sb.append(argumentName);
         }
         else {
             if (direction == RETURN) {
                 sb.append(RETURN);
             }
         }
         sb.append(" ");
         if (direction != RETURN) {
             sb.append(direction.toString());
             sb.append(" ");
         }
         if (dataType == null) {
             sb.append("<null/>");
         }
         else {
             sb.append(dataType.shortName());
         }
         return sb.toString();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         if (argumentName != null) {
             sb.append(argumentName);
         }
         else {
             if (direction == RETURN) {
                 sb.append(RETURN);
             }
         }
         sb.append(" ");
         if (direction != RETURN) {
             sb.append(direction.toString());
             sb.append(" ");
         }
         if (dataType == null) {
             sb.append("<null/>");
         }
         else {
             sb.append(dataType.toString());
         }
         if (optional) {
             sb.append(" (opt)");
         }
         return sb.toString();
     }
 
 	public void accept(DatabaseTypeVisitor visitor) {
 		visitor.visit(this);
 	}
 }
