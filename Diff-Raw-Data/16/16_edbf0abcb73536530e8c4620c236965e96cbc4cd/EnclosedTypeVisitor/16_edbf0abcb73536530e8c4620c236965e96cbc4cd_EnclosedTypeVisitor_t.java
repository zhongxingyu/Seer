 /*******************************************************************************
  * Copyright (c) 2012 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     David McCann - April 13 2012 - Initial Implementation
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.metadata.visit;
 
 //javase imports
 import java.util.ArrayList;
 import java.util.List;
 
 //DDL parser imports
 import org.eclipse.persistence.tools.oracleddl.metadata.ArgumentType;
 import org.eclipse.persistence.tools.oracleddl.metadata.CompositeDatabaseType;
import org.eclipse.persistence.tools.oracleddl.metadata.DatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ObjectTableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ObjectType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLCollectionType;
import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLCursorType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLRecordType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.VArrayType;
 
 /**
  * Custom visitor that will iterate through enclosed types, building up
  * a list of type instances.  Only one instance of a given type will
  * exist in the list.
  *
  */
 public class EnclosedTypeVisitor extends BaseDatabaseTypeVisitor {
 
     protected List<CompositeDatabaseType> cTypes = new ArrayList<CompositeDatabaseType>();
 
     /**
      * Returns the list of types that were processed during visits
      */
     public List<CompositeDatabaseType> getCompositeDatabaseTypes() {
         return cTypes;
     }
 
     @Override
     public void endVisit(TableType databaseType) {
     	addType(databaseType);
     }
     @Override
     public void endVisit(ObjectTableType databaseType) {
     	addType(databaseType);
     }
     @Override
     public void endVisit(ObjectType databaseType) {
     	addType(databaseType);
     }
     @Override
     public void endVisit(VArrayType databaseType) {
     	addType(databaseType);
     }
     @Override
     public void endVisit(PLSQLCollectionType databaseType) {
     	addType(databaseType);
     }
     @Override
     public void endVisit(PLSQLRecordType databaseType) {
     	addType(databaseType);
     }
     @Override
    public void visit(PLSQLCursorType databaseType) {
    	addType(databaseType);
    }
    @Override
     public void endVisit(ArgumentType databaseType) {
    	// sometimes we get here with an ArgumentType instance - use the enclosed type if non-null
    	if (databaseType.getEnclosedType() != null 
    			&& databaseType.getEnclosedType().isComposite() 
    			&& !databaseType.getEnclosedType().isROWTYPEType()
    			&& !databaseType.getEnclosedType().isPLSQLCursorType()) {
     		addType((CompositeDatabaseType) databaseType.getEnclosedType());
     	}
     }
     
     /**
      * Adds the given CompositeDatabaseType to the list of types if it doesn't 
      * already exist in the list.  Comparison is done based on type name.
      * 
      */
     protected void addType(CompositeDatabaseType cType) {
     	boolean exists = false;
     	for (CompositeDatabaseType type : cTypes) {
     		if (type.getTypeName().equals(cType.getTypeName())) {
     			exists = true;
     			break;
     		}
     	}
     	if (!exists) {
     		cTypes.add(cType);
     	}
     }
 }
