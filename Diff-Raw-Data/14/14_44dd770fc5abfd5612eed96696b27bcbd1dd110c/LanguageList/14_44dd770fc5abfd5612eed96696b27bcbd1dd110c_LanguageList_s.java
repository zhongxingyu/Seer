 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cyrilliclanguagehelper;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 
 /**
  * @class LanguageList
  * @author dean
  * @decription Holds a list of implemented languages
  */
 public class LanguageList extends ArrayList {
     private String inputDirectory;
     
     /**
      * Default constructor.  Set the input directory to the current working
      * directory and load any files within it.
      */
     public LanguageList()
     {
         System.out.println("New language list created");
         
         //  By default use the current directory
         File workingDirectory = new File(".");  
         try
         {
            this.inputDirectory = workingDirectory.getCanonicalPath() + "\\";
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         
         loadLanguageFiles();
     }
     
     /**
      * Create a new LanguageList and attempt to populate it with XML files
      * within the specified directory.  Will also load any XML files found in 
      * the applications current working directory.
      */
     public LanguageList(String inputDirectory)
     {
         this();
         
         this.inputDirectory = inputDirectory;
         
         loadLanguageFiles();
     }
     
     /**
      * Load all XML files in the input directory.
      */
     private void loadLanguageFiles()
     {
         File[] fileList = XMLFileFinder();
         
         for (int i = 0; i < fileList.length; i++)
         {
             Language newLanguage = new Language(fileList[i]);
             
             this.add(newLanguage);
         }
     }
     
     /**
      * Produce a list of all language files in the input directory.
      * @return File[]; the list of language files
      */
     private File[] XMLFileFinder()
     {
         System.out.println("Loading language files in " + inputDirectory);
         
         File searchDirectory = new File(inputDirectory);
         
         return searchDirectory.listFiles(new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 //  TODO: check for non-language XML files
                 //  HACK: better way to ignore build.xml than this
                 return (name.endsWith(".xml") && (!name.equals("build.xml")));
             }
         } );
     }
 }
