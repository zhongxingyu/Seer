 package org.tmcdb.engine.schema;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.tmcdb.utils.DirectoryUtils.checkIsDirectory;
 
 /**
  * @author Pavel Talanov
  */
 @SuppressWarnings("ConstantConditions")
 public final class SchemaManager {
     @NotNull
     private final File schemasDir;
     @Nullable
     private Map<String, TableSchema> tableNameToSchema = null;
 
     public SchemaManager(@NotNull File schemasDir) {
         checkIsDirectory(schemasDir);
         this.schemasDir = schemasDir;
     }
 
     public void initialize() {
         File[] files = schemasDir.listFiles();
         if (files == null) {
             throw new IllegalStateException("Error reading schemas");
         }
         tableNameToSchema = new HashMap<String, TableSchema>();
         for (File file : files) {
             if (file.isFile()) {
                 TableSchema tableSchema = readSchema(file);
                 tableNameToSchema.put(tableSchema.getTableName(), tableSchema);
             } else {
                 throw new IllegalStateException("Schema directory content is corrupted");
             }
         }
     }
 
     @NotNull
     private TableSchema readSchema(@NotNull File file) {
         try {
             FileInputStream fileInputStream = new FileInputStream(file);
             try {
                 ObjectInputStream serializationInputStream = new ObjectInputStream(fileInputStream);
                 try {
                     Object deserializedObject = serializationInputStream.readObject();
                     assert deserializedObject instanceof TableSchema;
                     return (TableSchema) deserializedObject;
                 } finally {
                     serializationInputStream.close();
                 }
             } finally {
                 fileInputStream.close();
             }
         } catch (Exception e) {
             throw new IllegalStateException("Error deserializing data from " + file.getAbsolutePath(), e);
         }
     }
 
    public void deinitialize() {
         writeSchemas();
     }
 
     private void writeSchemas() {
         checkInitialized();
         for (TableSchema table : tableNameToSchema.values()) {
             File schemaFile = new File(schemasDir, table.getTableName());
             try {
                 FileOutputStream fileOutputStream = new FileOutputStream(schemaFile);
                 try {
                     ObjectOutputStream serializationOutputStream = new ObjectOutputStream(fileOutputStream);
                     try {
                         serializationOutputStream.writeObject(table);
                     } finally {
                         serializationOutputStream.close();
                     }
                 } finally {
                     fileOutputStream.close();
                 }
             } catch (Exception e) {
                throw new IllegalStateException("Error serializing data to " + schemaFile.getAbsolutePath(), e);
             }
 
         }
     }
 
     @NotNull
     public List<TableSchema> getAllTables() {
         checkInitialized();
         return new ArrayList<TableSchema>(tableNameToSchema.values());
     }
 
     private void checkInitialized() {
         assert tableNameToSchema != null : "Initialize() was not called or failed.";
     }
 
     @Nullable
     public TableSchema getSchema(@NotNull String tableName) {
         checkInitialized();
         return tableNameToSchema.get(tableName);
     }
 
     public void addNewSchema(@NotNull TableSchema schema) {
         checkInitialized();
         tableNameToSchema.put(schema.getTableName(), schema);
        writeSchemas();
     }
 }
