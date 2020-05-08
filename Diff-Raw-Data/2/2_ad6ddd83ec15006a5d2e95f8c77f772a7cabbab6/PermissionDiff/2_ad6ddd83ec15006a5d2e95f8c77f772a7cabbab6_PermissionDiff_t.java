 package com.github.ryhmrt.mssqldiff.data;
 
 import com.github.ryhmrt.mssqldiff.util.SqlUtil;
 
 
 public class PermissionDiff extends Diff<Permission> {
 
     @Override
     public String getName() {
         return getType() == Type.CREATED ? getTo().getUserName() : getFrom().getUserName();
     }
 
     @Override
     public String toCreateSql() {
         StringBuilder sb = new StringBuilder();
         if (getTo().isCanSelect()) sb.append(grant("SELECT"));
         if (getTo().isCanInsert()) sb.append(grant("INSERT"));
         if (getTo().isCanUpdate()) sb.append(grant("UPDATE"));
         if (getTo().isCanDelete()) sb.append(grant("DELETE"));
         return sb.toString();
     }
 
     @Override
     public String toDropSql() {
         StringBuilder sb = new StringBuilder();
         if (getFrom().isCanSelect()) sb.append(revoke("SELECT"));
         if (getFrom().isCanInsert()) sb.append(revoke("INSERT"));
         if (getFrom().isCanUpdate()) sb.append(revoke("UPDATE"));
         if (getFrom().isCanDelete()) sb.append(revoke("DELETE"));
         return sb.toString();
     }
 
     @Override
     public String toModifySql() {
         StringBuilder sb = new StringBuilder();
         if (!getFrom().isCanSelect() && getTo().isCanSelect()) sb.append(grant("SELECT"));
         if (!getFrom().isCanInsert() && getTo().isCanInsert()) sb.append(grant("INSERT"));
         if (!getFrom().isCanUpdate() && getTo().isCanUpdate()) sb.append(grant("UPDATE"));
         if (!getFrom().isCanDelete() && getTo().isCanDelete()) sb.append(grant("DELETE"));
         if (getFrom().isCanSelect() && !getTo().isCanSelect()) sb.append(revoke("SELECT"));
         if (getFrom().isCanInsert() && !getTo().isCanInsert()) sb.append(revoke("INSERT"));
         if (getFrom().isCanUpdate() && !getTo().isCanUpdate()) sb.append(revoke("UPDATE"));
         if (getFrom().isCanDelete() && !getTo().isCanDelete()) sb.append(revoke("DELETE"));
         return sb.toString();
     }
 
     private String revoke(String option) {
         return SqlUtil.revoke(getFrom().getTableName(), getFrom().getUserName(), option);
     }
 
     private String grant(String option) {
        return SqlUtil.grant(getTo().getTableName(), getTo().getUserName(), option);
     }
 }
