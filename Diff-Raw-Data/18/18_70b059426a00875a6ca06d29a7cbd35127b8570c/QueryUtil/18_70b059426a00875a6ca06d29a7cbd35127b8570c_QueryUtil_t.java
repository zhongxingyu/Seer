 package com.fap.framework.db.util;
 
 
 public final class QueryUtil {
 
     static public String buildLongOrClause(final String columnName, final int parametersCount) {
 
        StringBuffer buf = new StringBuffer();

        String columnAssign = " OR " + columnName +"=?";
 
         for(int i = 0; i < parametersCount; ++i) {
            if (i == 0) {
                buf.append("( " + columnName + "=?");
             }
             else {
                buf.append(columnAssign);
             }
         }// for
 
        buf.append(" )");
 
        return buf.toString();
 
     }
 
 }// class
