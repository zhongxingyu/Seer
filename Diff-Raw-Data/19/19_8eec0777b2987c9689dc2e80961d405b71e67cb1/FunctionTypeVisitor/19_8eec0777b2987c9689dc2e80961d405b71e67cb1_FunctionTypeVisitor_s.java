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
  *     David McCann - July 2011, visit tests
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.test.visit;
 
 //javase imports
 import java.util.ArrayList;
 import java.util.List;
 
 //DDL imports
 import org.eclipse.persistence.tools.oracleddl.metadata.ArgumentType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FunctionType;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.BaseDatabaseTypeVisitor;
 
 class FunctionTypeVisitor extends BaseDatabaseTypeVisitor {
 
     public String funcName;
     public String schema;
     public List<String> argData = new ArrayList<String>();
    public String returnArg; 
    
     public void beginVisit(FunctionType funcType) {
         funcName = funcType.getProcedureName();
         schema = funcType.getSchema();
         returnArg = funcType.getReturnArgument().getTypeName();
     }
    
     public void beginVisit(ArgumentType argType) {
         if (argType.optional()) {
            argData.add(argType.getArgumentName() + "(opt) " + argType.getDirection() + " " + argType.getDataType());
        } else {
            argData.add(argType.getArgumentName() + " " + argType.getDirection() + " " + argType.getDataType());
         }
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder("FUNCTION ");
         if (schema != null) {
             sb.append(schema);
             sb.append(".");
         }
         sb.append(funcName);
         sb.append(" (");
         for(int i=0; i<argData.size();) {
             String arg = argData.get(i);
             sb.append(arg);
             if (++i < argData.size()) {
                 sb.append(", ");
             }
         }
         sb.append(") RETURN ");
         sb.append(returnArg);
         return sb.toString();
     }
 }
