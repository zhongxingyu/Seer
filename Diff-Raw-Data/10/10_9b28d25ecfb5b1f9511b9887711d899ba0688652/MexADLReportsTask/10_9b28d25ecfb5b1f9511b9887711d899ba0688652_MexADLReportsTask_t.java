 package mx.itesm.mexadl.metrics.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.LogManager;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.objectweb.asm.ClassReader;
 
 /**
  * The MexADLReportsTask class is responsible for generating HTML reports for
  * user-friendly access.
  * 
  * @author jccastrejon
  * 
  */
 public class MexADLReportsTask extends Task {
     /**
      * Base directory
      */
     private String basedir;
 
     /**
      * Directory where the Java classes to be analyzed are stored.
      */
     private String classes;
 
     /**
      * Task execution.
      */
     public void execute() throws BuildException {
         Map<String, String> componentTypes;
 
         try {
             // Logging configuration
             LogManager.getLogManager().readConfiguration(
                     MexAdlTask.class.getClassLoader().getResourceAsStream(
                             "mx/itesm/mexadl/metrics/logging-reports.properties"));
 
             // Get the components types information
             componentTypes = this.getComponentsTypes(new File(this.classes));
 
             // Generate HTML reports
             try {
                 Util.generateHtmlReport("interactions-verification", this.basedir, componentTypes);
             } catch (Exception e) {
                e.printStackTrace();
             }
             try {
                 Util.generateHtmlReport("metrics-verification", this.basedir, componentTypes);
             } catch (Exception e) {
                e.printStackTrace();
             }
         } catch (Exception e) {
            e.printStackTrace();
         }
     }
 
     /**
      * Get the component types associated to the classes in the
      * <em>classesDir</em> directory.
      * 
      * @param classesDir
      * @return Map containing pairs of [className, componentType]
      * @throws IOException
      */
     protected Map<String, String> getComponentsTypes(final File classesDir) throws IOException {
         InputStream inputStream;
         List<String> classesList;
         Map<String, String> returnValue;
         ComponentsAnnotationsVisitor componentsVisitor;
 
         returnValue = new HashMap<String, String>();
         classesList = Util.getClassesInDirectory(classesDir);
         if (classesList != null) {
             for (String clazz : classesList) {
                 componentsVisitor = new ComponentsAnnotationsVisitor();
                 inputStream = new FileInputStream(clazz);
                 new ClassReader(inputStream).accept(componentsVisitor, ClassReader.SKIP_DEBUG);
                inputStream.close();
 
                 // Store only if the class has both name and type
                 if ((componentsVisitor.getComponentName() != null) && (componentsVisitor.getComponentType() != null)) {
                     returnValue.put(componentsVisitor.getComponentName(), componentsVisitor.getComponentType());
                 }
             }
         }
 
         return returnValue;
     }
 
     public void setBasedir(final String basedir) {
         this.basedir = basedir;
     }
 
     public void setClasses(String classes) {
         this.classes = classes;
     }
 }
