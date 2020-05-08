 package com.fap.framework.db.util;
 
 
 public final class QueryUtil {
 
     static public String buildLongOrClause(final String columnName, final int parametersCount) {
 
        String orClause = null;
 
         for(int i = 0; i < parametersCount; ++i) {
            if (orClause == null) {
                orClause = "( " + columnName + "=?";
             }
             else {
                orClause = orClause + " OR " + columnName +"=?";
             }
         }// for
 
        orClause = orClause + " )";
 
        return orClause;
 
     }
 
 }// class
