 package de.tuberlin.dima.presslufthammer.data.columnar.inmemory;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReader;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnWriter;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 
 public class InMemoryTablet implements Tablet {
     private SchemaNode schema;
     private Map<SchemaNode, List<ColumnEntry>> columns;
     private Map<SchemaNode, InMemoryColumnWriter> columnWriters;
 
     public InMemoryTablet(SchemaNode schema) {
         this.schema = schema;
         columns = Maps.newHashMap();
         columnWriters = Maps.newHashMap();
         createColumns(schema);
     }
 
     private void createColumns(SchemaNode schema) {
         List<ColumnEntry> column = Lists.newLinkedList();
         columns.put(schema, column);
         InMemoryColumnWriter columnWriter = new InMemoryColumnWriter(schema,
                 column);
         columnWriters.put(schema, columnWriter);
         if (schema.isRecord()) {
             for (SchemaNode childSchema : schema.getFieldList()) {
                 createColumns(childSchema);
             }
         }
     }
 
     public SchemaNode getSchema() {
         return schema;
     }
     
     public boolean hasColumn(SchemaNode schema) {
         return columns.containsKey(schema);
     }
 
     public ColumnWriter getColumnWriter(SchemaNode schema) {
         if (!columns.containsKey(schema)) {
             throw new RuntimeException(
                     "This should not happen, bug in program.");
         }
         return columnWriters.get(schema);
     }
 
     public ColumnReader getColumnReader(SchemaNode schema) {
         if (!columns.containsKey(schema)) {
             throw new RuntimeException(
                    "This should not happen, bug in program.");
         }
         return new InMemoryColumnReader(schema, columns.get(schema));
     }
 
     public void printColumns() {
         for (SchemaNode schema : columnWriters.keySet()) {
             System.out.println("COLUMN: " + schema.getQualifiedName());
             for (ColumnEntry entry : columns.get(schema)) {
                 System.out.println("r: " + entry.repetitionLevel + ", d: "
                         + entry.definitionLevel + ", field: " + entry.value);
             }
         }
     }
 
     static class ColumnEntry {
         int repetitionLevel;
         int definitionLevel;
         Object value;
 
         public ColumnEntry(Object value, int repetitionLevel,
                 int definitionLevel) {
             this.repetitionLevel = repetitionLevel;
             this.definitionLevel = definitionLevel;
             this.value = value;
         }
     }
 }
