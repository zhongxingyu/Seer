 package model;
 
 public class Generator
 {

  private static final String DEFAULT_FOLDER = "src/dataset-dsl/java";
 
   public static void main(String[] args) throws Exception
   {
     PersonDatabaseModel model = new PersonDatabaseModel();
     model.generatedSourceFolder(getGeneratedSourceFolder(args));
     model.generate();
   }
 
   private static String getGeneratedSourceFolder(String[] args)
   {
     if (args.length != 1)
     {
       return DEFAULT_FOLDER;
     }
     return args[0];
   }

 }
