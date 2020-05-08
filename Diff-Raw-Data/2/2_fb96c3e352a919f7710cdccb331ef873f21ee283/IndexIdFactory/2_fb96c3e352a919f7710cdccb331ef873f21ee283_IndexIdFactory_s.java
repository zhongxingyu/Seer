 package com.txtr.hibernatedelta;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.txtr.hibernatedelta.model.ExplicitHibernateIndex;
 import com.txtr.hibernatedelta.model.HibernateColumn;
 import com.txtr.hibernatedelta.model.HibernateDatabase;
 import com.txtr.hibernatedelta.model.HibernateIndexName;
 import com.txtr.hibernatedelta.model.HibernateIndexNames;
 import com.txtr.hibernatedelta.model.HibernateTable;
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Ordering;
 
 public class IndexIdFactory {
 
     private IndexIdFactory() {
     }
 
     public static void setIndexNames(HibernateDatabase newDatabase, HibernateIndexNames indexNames) {
         for (HibernateTable table : newDatabase.getTables()) {
             setIndexNames(table, indexNames);
         }
     }
 
     public static void setIndexNames(HibernateTable newTable, HibernateIndexNames indexNames) {
 
         List<ExplicitHibernateIndex> explicitIndexes = newTable.getExplicitIndexes();
         Collections.sort(explicitIndexes, Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(new Function<ExplicitHibernateIndex, String>() {
             @Override
             public String apply(ExplicitHibernateIndex input) {
                 return StringUtils.join(input.getColumns(), ",");
             }
         }));
 
         for (HibernateColumn column : newTable.getColumns()) {
             if (needsIndexName(column)) {
                 column.setIndexName(getIndexName(newTable, ImmutableList.of(column.getName()), indexNames));
             }
         }
 
         for (ExplicitHibernateIndex index : explicitIndexes) {
             if (index.getName() == null) {
                 index.setIndexName(getIndexName(newTable, index.getColumns(), indexNames));
             }
         }
     }
 
     private static boolean needsIndexName(HibernateColumn column) {
        return column.getTargetTable() != null && column.getForeignKeyIndexName() == null && !column.isPrimaryKey();
     }
 
     private static HibernateIndexName getIndexName(HibernateTable table, List<String> columns, HibernateIndexNames indexNames) {
         HibernateIndexName oldName = indexNames.find(table.getName(), columns);
         if (oldName != null) {
             return oldName;
         }
 
         HibernateIndexName name = new HibernateIndexName(createIndexName(table.getIndexPrefix(), indexNames), table.getName(), columns);
         indexNames.getIndexNames().add(name);
         return name;
     }
 
     private static String createIndexName(String tableName, HibernateIndexNames indexNames) {
         int i = 1;
         while (true) {
             String indexName = tableName + i;
 
             if (indexNames.findByName(indexName) == null) {
                 return indexName;
             }
             i++;
         }
     }
 }
