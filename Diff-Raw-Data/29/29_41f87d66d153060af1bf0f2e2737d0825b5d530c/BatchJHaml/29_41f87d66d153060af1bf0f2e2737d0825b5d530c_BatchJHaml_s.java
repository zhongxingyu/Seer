 package com.progriff.jhaml;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.cadrlife.jhaml.JHaml;
 
 /**
  * 
  * @author Benson Lim
  * Creates JSP or HTML in batch ala Staticmatic style.
  */
 public class BatchJHaml {
     /**
      * Path where the haml files are
      */
     private String hamlPath;
     /**
      * Path where the haml layout files are
      */
     private String hamlLayoutPath;
     /**
      * Path where the output files will be generated
      */
     private String outputPath;
     /**
      * The output's extension.
      */
     private String outputExtension;
     /**
      * The current path where the application will run
      */
     private final static String CURRENT_PATH = System.getProperty("user.dir");
     /**
      * File seperator.
      */
     private final static String SEPERATOR = 
                                     System.getProperty("file.separator");
     /**
      * Haml can have multiple layouts. This map is to store them. BatchJHaml
      * only supports application.haml at the moment
      */
     private HashMap<String, String> layouts = new HashMap<String, String>();
     
     /**
      * Default constructor.
      */
     public BatchJHaml() {
         this(CURRENT_PATH + SEPERATOR + "haml", 
                 CURRENT_PATH + SEPERATOR + "haml" + SEPERATOR + "layouts", 
                 CURRENT_PATH + SEPERATOR + "output", 
                 "jsp");
     }
     
     /**
      * Hate default values? Call this one
      * @param hamlPath Path to your Haml folder
      * @param outputPath Path to output
      * @param outputExtension The name of the extension to append
      */
     public BatchJHaml(String hamlPath,
                         String hamlLayoutPath,
                         String outputPath,
                         String outputExtension) {
         this.hamlPath = hamlPath;
         this.outputPath = outputPath;
         this.outputExtension = outputExtension;
         this.hamlLayoutPath = hamlLayoutPath;
     }
     
     /**
      * If you are not generating via ant run , you can call this function
      * from your app.
      */
     public void generateOutput() {
         File file = new File(this.hamlPath);
         populateLayouts();
         
         for(File hamlFile: file.listFiles(getFilenameFilter())) {
             try {
                 String hamlOutput = getHamlOutput(hamlFile);
                 File outputFile = new File(
                         this.outputPath + 
                         SEPERATOR + 
                         StringUtils.replace(hamlFile.getName(), ".haml", "." + 
                                 this.outputExtension));
                 
                 writeToFile(outputFile, hamlOutput);
             }
             catch(IOException ioe) {
                 ioe.printStackTrace();
             }
         }
     }
     
     /**
      * Returns a filter that accepts filenames that: <br/>
      * <ul>
      *   <li/> doesn't start with "."
      *   <li/> ends with ".haml"
      * </ul>
      * @return the FilenameFilter object.
      */
     public FilenameFilter getFilenameFilter() {
         return new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return !name.startsWith(".") && name.endsWith(".haml");
             }
         };
     }
     
     /**
      * Writes the output to the output folder. Replaces <%= yield %> to use
      * the content markup
      * @param outputFile File object to output
      * @param hamlOutput The hamlOutput from JHaml
      * @throws IOException if unable to write to the folder for some reason
      */
     public void writeToFile(File outputFile, String hamlOutput) 
                                                     throws IOException {
         
         String layout = this.layouts.get("application.haml");
         
        FileUtils.writeStringToFile(outputFile, 
                StringUtils.replace(layout, "<%= yield %>", hamlOutput) );
     }
     
     /**
      * Iterates the layouts in the haml/layouts folder and stores the
      * processed MarkUp into a HashMap. The key is the filename.
      */
     public void populateLayouts() {
         JHaml jhaml = new JHaml();
         this.layouts.clear();
         File file = new File(this.hamlLayoutPath);
         for(File layoutFile: file.listFiles(getFilenameFilter())) {
             try {
                 this.layouts.put(layoutFile.getName(), 
                         jhaml.parse(FileUtils.readFileToString(layoutFile)));
             }
             catch(IOException ioe) {
                 System.err.println("Unable to convert layout : " + 
                                         layoutFile.getName());
                 ioe.printStackTrace();
             }
         }
         
     }
     
     /**
      * Gets the XHTML Output based on the hamlFile given
      * @param hamlFile the haml file object
      * @return JSP String
      * @throws IOException if unable to read the file.
      */
     public String getHamlOutput(File hamlFile) throws IOException{
         JHaml jhaml = new JHaml();
         return jhaml.parse(FileUtils.readFileToString(hamlFile));
     }
     
     
     public String getHamlPath() {
         return hamlPath;
     }
 
     public void setHamlPath(String hamlPath) {
         this.hamlPath = hamlPath;
     }
 
     public String getOutputPath() {
         return outputPath;
     }
 
     public void setOutputPath(String outputPath) {
         this.outputPath = outputPath;
     }
 
     public String getOutputExtension() {
         return outputExtension;
     }
 
     public void setOutputExtension(String outputExtension) {
         this.outputExtension = outputExtension;
     }
 
     public HashMap<String, String> getLayouts() {
         return layouts;
     }
 
     public void setLayouts(HashMap<String, String> layouts) {
         this.layouts = layouts;
     }
 
     
     public String getHamlLayoutPath() {
         return hamlLayoutPath;
     }
 
     public void setHamlLayoutPath(String hamlLayoutPath) {
         this.hamlLayoutPath = hamlLayoutPath;
     }
 
     public static void main(String[] args) {
         BatchJHaml batch = new BatchJHaml();
         batch.generateOutput();
         System.out.println("File Generation Completed!");
         
     }
 }
