 package com.seitenbau.testing.dbunit.generator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class DatabaseModel
 {
 
   DataSetGenerator generator;
 
   String databaseName;
 
   String packageName;
 
   String targetPath = DataSetGenerator.DEFAULT_OUTPUT_FOLDER;
 
   boolean isModelClassGeneration;
 
   boolean isTableDSLGeneration;
 
   final List<Table> tables = new ArrayList<Table>();
 
   protected String __forceCaller;
 
   public DatabaseModel()
   {
     isModelClassGeneration = false;
     isTableDSLGeneration = true;
   }
 
   public DataSetGenerator getDataSetGenInstance()
   {
     if (generator == null)
     {
       generator = new DataSetGenerator(packageName, databaseName, targetPath, isTableDSLGeneration,
           isModelClassGeneration);
     }
     return generator;
   }
 
   public void database(String name)
   {
     this.databaseName = name;
   }
 
   public void packageName(String name)
   {
     this.packageName = name;
   }
 
   public void generatedSourceFolder(String folder)
   {
     if (generator == null)
     {
       this.targetPath = folder;
     }
     else
     {
       generator.setTargetPath(folder);
     }
   }
 
   public void catchException(String exception)
   {
     getDataSetGenInstance().catchException(exception);
   }
 
   /**
    * Adds a table to the database model.
    * @param name The database name of the table
    * @return The builder to configure the table.
    */
   public TableBuilder table(String name)
   {
     return new TableBuilder(this, name);
   }
 
   /**
    * Adds an associative table to the database model. Associative tables should be used
    * to model n:m relations, although they can be used for modeling every binary relation.
    * <p>
    * Associative tables consist of two columns with foreign relations and can have further
    * columns for attributes, which describe the relation.
    * <p>
    * The following example shows a table with images and and a table with tags to categorize
    * the images. Each image requires at least one tag, while a tag does not need to have
    * associated images.
    * <code>
    * <pre class="groovyTestCase">
    * import com.seitenbau.testing.dbunit.generator.*;
    *
    * public class DemoDatabaseModel extends DatabaseModel {
    *
    *   public DemoDatabaseModel() {
    *     Table image = table("image")
    *       .column("id", DataType.BIGINT)
    *         .defaultIdentifier()
    *       .column("name", DataType.VARCHAR)
    *       .column("content", DataType.BLOB)
    *     .build();
    *
    *     Table tag = table("tag")
    *       .column("name", DataType.VARCHAR)
    *         .defaultIdentifier()
    *     .build();
    *
    *     associativeTable("image_tag")
    *       .column("image_id", DataType.BIGINT)
    *         .reference
    *           .foreign(image)
    *             .name("hasTags")
    *             .multiplicity("1..*")
    *       .column("tag_name", DataType.VARCHAR)
    *         .reference
    *           .foreign(tag)
    *             .name("containsImages")
    *             .multiplicity("0..*")
    *     .build();
    *   }
    * }
    *
    * DemoDatabaseModel model = new DemoDatabaseModel();
    * DataSetGenerator generator = model.getDataSetGenInstance();
    * DataSet dataSet = generator.getDataSet();
    *
    * assert dataSet.getTables().size() == 3;
    * </pre>
    * </code>
    *
    * @param name The name of the table
    * @return The builder to configure the associative table
    */
   public TableBuilder associativeTable(String name)
   {
     return new AssociativeTableBuilder(this, name);
   }
 
   /**
    * Adds a built table to the data set
    * @param table The Table built by a TableBuilder
    */
   void addTable(Table table)
   {
     getDataSetGenInstance().addTable(table);
   }
 
   /**
    * Starts the generation of the DSL model classes
    * @throws Exception
    */
   public void generate() throws Exception
   {
     DataSetGenerator gen = getAndConfigureDataSetGenInstance();
     gen.generate();
   }
 
   public void generateInto(String folder) throws Exception
   {
     DataSetGenerator gen = getAndConfigureDataSetGenInstance();
     gen.generateInto(folder);
   }
 
   public void enableTableModelClassesGeneration()
   {
     isModelClassGeneration = true;
   }
 
  public void disbaleTableDSLGeneration()
   {
     isTableDSLGeneration = false;
   }
 
   private DataSetGenerator getAndConfigureDataSetGenInstance()
   {
     DataSetGenerator gen = getDataSetGenInstance();
     if (__forceCaller != null)
     {
       gen.setCaller(__forceCaller);
     }
     return gen;
   }
 
 }
