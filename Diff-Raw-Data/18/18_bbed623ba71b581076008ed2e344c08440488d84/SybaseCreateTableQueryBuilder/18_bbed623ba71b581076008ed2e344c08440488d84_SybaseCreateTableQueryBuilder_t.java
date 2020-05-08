 package net.codjo.database.sybase.impl.query.builder;
 import net.codjo.database.common.impl.query.builder.AbstractCreateTableQueryBuilder;
 public class SybaseCreateTableQueryBuilder extends AbstractCreateTableQueryBuilder {
 
     @Override
     public String get() {
         StringBuilder create = new StringBuilder().
               append("create table ");
        if (table.isTemporary() && !table.getName().startsWith("#")) {
             create.append("#");
         }
         return create
               .append(table.getName())
               .append(" ( ").append(content).append(" )").toString();
     }
 }
