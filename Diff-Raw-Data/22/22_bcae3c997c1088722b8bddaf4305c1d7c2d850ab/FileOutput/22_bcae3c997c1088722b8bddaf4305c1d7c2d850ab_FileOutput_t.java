 package net.digitalprimates.translators.actionscript;
 
 import java.io.*;
 
 /**
  * User: David "Vandermore" Moore
  * Date: 7/11/13
  * Time: 12:04 PM
  */
 public class FileOutput {
     //TODO:: This class and the base directory could be made more generic for more languages, i.e. made into a utility class.
     public static final String BASE_DIRECTORY = "outputHeaders/actionScript";
 
     //Set up for writing output to a file.
     protected Writer writer = null;
     protected String fileForWritingName;
     public String packageStructure;
     private String directoryPath = "";
     private String noWriteFileName = "NoWriterERROR";
 
     public void openFileForWriting( String fileName ) {
         fileForWritingName = fileName;
 
         try {
             writer = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream( BASE_DIRECTORY + directoryPath + "\\" + fileName ), "utf-8"));
         } catch (IOException ex){
             // report
             System.out.println( "Unable to open file for writing: " + ex.toString() );
         }
     }
 
     public void writeToFile( String stuffToWrite ) {
         //TODO: I don't like this here. We should have a writer already. Creates a dummy file to write to using the package name.
         if ( writer == null ) {
            System.out.println( "writeToFile - no file open for writing" );
             openFileForWriting( noWriteFileName + ".as" );
         }
 
         try {
             writer.write( stuffToWrite );
         } catch (IOException ex){
             // report
             System.out.println( "Unable to write to file: " + ex.toString() );
         }
     }
 
     public void insertLineBreak() {
         //TODO: I don't like this here. We should have a writer already. Creates a dummy file to write to using the package name.
         if ( writer == null ) {
             openFileForWriting( noWriteFileName + ".as" );
         }
 
         try {
             writer.write( "\n" );
         } catch (IOException ex){
             // report
             System.out.println( "Unable to write linebreak to file: " + ex.toString() );
         }
     }
 
     public void closeFileForWriting() {
         if ( writer == null ) {
             return;
         }
 
         try {
             writer.close();
         } catch (Exception ex) {
             // report
             System.out.println( "Unable to close file for writing: " + ex.toString() );
         }
     }
 
     /**
      * Creates a directory structure from a . delimited string.
      * @param value
      */
     public void createDirectories( String value ) {
         packageStructure = value;
 
         String[] packagePathArr = getPackagePath( value );
         String dirSeparator = "\\";
 
         //Need to loop through the package path array to create the directory and subdirectories.
         for ( int i = 0; i < packagePathArr.length; i++ ) {
             directoryPath += dirSeparator;
             directoryPath += packagePathArr[ i ];
         }
 
         File directoryStructure = new File( BASE_DIRECTORY + directoryPath );
         try {
             if ( directoryStructure.mkdirs() ) {
                 //System.out.println("Directory Created");
             } else {
                 //System.out.println("Directory is not created");
             }
         } catch(Exception e){
             e.printStackTrace();
         }
     }
 
     /**
      * Gets an array of strings that is the package package path from a dot delimited string.
      * @param value
      * @return
      */
     protected String[] getPackagePath ( String value ) {
         //The split value is a regex. So I need to escape the . character.
         return value.split("\\.");
     }
 }
