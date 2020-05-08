 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serempre.codegen.metamodel;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import javax.xml.parsers.ParserConfigurationException;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.apache.velocity.exception.TemplateInitException;
 import org.xml.sax.SAXException;
 import serempre.codegen.metamodel.parsers.ModelParser;
 import static resources.ResourceUtility.*;
 import resources.StringUtility;
 
 /**
  *
  * @author Thor
  */
 public abstract class Merger {
     //this context is used when parsing tempaltes
     protected VelocityContext mVelocityContext = null;
     /**
      * default parameterless constructor
      */
     public Merger() {
         
     }
     /**
      * prepares velocity for execution
      * @throws URISyntaxException if a connection to the jar file holding template resources fails
      */
     public void init() throws URISyntaxException{
         //create a string manupulation utility to aid while code is being generated
         StringUtility strUtility = new StringUtility();
         //choose file loader to fetch resources/templates
         Velocity.setProperty("resource.loader", "file, class, jar");
         //set file loader as the class used to load resources/templates
         Velocity.setProperty("resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
         //set current execution path as the root where resources are fetched from
         Velocity.setProperty("file.resource.loader.path", ".");
         //prevent resource loader from using caches
         Velocity.setProperty("file.resource.loader.cache", "false");
         //refresh resources everytime they are requested
         Velocity.setProperty("file.resource.loader.modificationCheckInterval", "0");
         //add a class path resource loader
         Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
         //add a jar resource loader
         Velocity.setProperty("jar.resource.loader.class", "org.apache.velocity.runtime.resource.loader.JarResourceLoader");
         //TODO: change the following setting to point to a jar or a csv list of jars where resources can be fetched from
         //jar url are written as 'jar:file:/myjarplace/myjar1.jar, jar:file:/myjarplace/myjar1.jar'
         //set root lookup for resources within a jar
         Velocity.setProperty("jar.resource.loader.path", "jar:file:"+getPathToFile("dist/MetaModel.jar"));
         //initialize velocity so that configuration changes take place
         Velocity.init();
         //create a velocity context to feed data into the templates
         mVelocityContext = new VelocityContext();
         //create a velocity context and feed in the string utility
         mVelocityContext.put("strUtility", strUtility);
     }
     /**
      * merges a model and it's templates
      * @param modelPath path to model file
      * @param outputPath path to where output fields should be written to
      * @throws ParserConfigurationException if there are no parser implementations available
      * @throws SAXException if there are errors while parsing the model
      * @throws IOException if there are streaming errors
      */
     public void start(String modelPath, String outputPath) throws ParserConfigurationException, SAXException, IOException{
         //create a model parser to parse the business model
         ModelParser domain = new ModelParser();
         //parse the business model
         parseFileUsingParsingContext(domain, modelPath);
         //put the business model within the velocity context, so that it becomes available to all templates
         mVelocityContext.put("domain", domain);
         //perform code generation
         merge(domain, outputPath);
     }
     /**
      * gets the path for the template being used during a model transformation
      * @return jar relative URL like 'resources/attribute.vsl'
      */
     public abstract String getTemplateName();
     /**
      * if this is set to true, then the model will be transformed and written to a single file. If set to false, then each class in the model will be processed on it's own 
      * @return true when model transformation is to be rendered in a single file. False when each class should be processed on it's own
      */
     public abstract boolean bulkTransformation();
     /**
      * returns the name of the model being processed
      * @return the name of the model being processed
      */
     public abstract String getPackageName();
     /**
      * returns the extension used for output files
      * @return something like 'java', 'py', 'c', and so on
      */
     public abstract String getTargetFileExtension();
     /**
      * processes the domain and applies any needed transformations to output application source doe
      * @param domain representation for the application business model
      * @param outputPath a path where all output files are to be written to
      */
     public void merge(ModelParser domain, String outputPath) throws ParserConfigurationException, SAXException, IOException {
         //template location is regarded as the template's name
         String templateName = this.getTemplateName();
         //prepare a buffer to write to an output file
         BufferedWriter writer = null;
         //if the test template is unavalable, then fail this test
         if(!Velocity.resourceExists(templateName)){
             throw new ResourceNotFoundException(String.format("template is not available: %s", templateName));
         }
         //build a template object using targeted template
         Template template = Velocity.getTemplate(templateName);
         //make sure output folder exists
        buildTargetPath(getPathToFile("/output"));
         //check transformation mode and if bulk, then start model transformation
         if(bulkTransformation()){
             try{
                 //create a path to the file to which to output a transformed model
                String outputFilePath = String.format("/output/%s.%s", getPackageName(), getTargetFileExtension());
                 //make the path relative to the jar's execution context
                 outputFilePath = getPathToFile(outputFilePath);
                 //open up a file stream
                 writer = new BufferedWriter(new FileWriter(outputFilePath));
                 //perform model transformation
                 template.merge(mVelocityContext, writer);
             }finally{
                 if(writer!=null){
                     writer.flush();
                     writer.close();
                 }
             }
         }else {
             try{
                 //for each class in the model
                 for(ClassDescriptor classDescriptor : domain.getClasses().values()){
                     //create a path to the file to which to output a transformed class
                    String outputFilePath = String.format("/output/%s.%s", classDescriptor.getClassName(), getTargetFileExtension());
                     //make the path relative to the jar's execution context
                     outputFilePath = getPathToFile(outputFilePath);
                     //open up a file stream
                     writer = new BufferedWriter(new FileWriter(outputFilePath));
                     //create a new context variable called class which holds this particular class descriptor
                     mVelocityContext.put("class", classDescriptor);
                     //render model transformation
                     template.merge(mVelocityContext, writer);
                 }
             }finally{
                 if(writer!=null){
                     writer.flush();
                     writer.close();
                 }
             }
         }
     }
 }
