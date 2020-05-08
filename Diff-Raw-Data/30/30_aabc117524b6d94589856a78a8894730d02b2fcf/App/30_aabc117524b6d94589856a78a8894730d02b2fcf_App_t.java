 /**
  * 
  */
 package org.cichonski.ontviewer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Properties;
import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.cichonski.ontviewer.servlet.DynamicPathBuilder;
 import org.cichonski.ontviewer.servlet.PathBuilder;
import org.cichonski.ontviewer.servlet.View;
 import org.cichonski.ontviewer.servlet.ViewBuilder;
 import org.cichonski.ontviewer.servlet.ViewBuilder.ViewContainer;
 
 /**
  * <p>
  * The alternative way of using this application. The main method of this class
  * will allow the use to generate the static pages with the same directory
  * structure and page naming as the dynamic web application running behind the
  * servlet code.
  * </p>
  * 
  * @author Paul Cichonski
  */
 public class App {
     private static final Logger log = Logger.getLogger(App.class.getName());
     private static final String ONT_DIR_LOC = "ontologies/";
     private static final String PROPERTY_FILE_LOC ="ont-viewer.properties";
     
     // don't really do it this way, get this input as a args param.
     private static final String PATH_SEPERATOR = System.getProperty("file.separator");
     private static final String DEFAULT_STORAGE_LOCATION = System.getProperty("user.home") + PATH_SEPERATOR + "ont-viewer-files" + PATH_SEPERATOR;
     
     /**
      * @param args
      */
     public static void main(String[] args) {
         // TODO Auto-generated method stub
         try {
             String contextPath = "/vocabs";
             final Properties props = new Properties();
             props.load(new FileInputStream(getFile(PROPERTY_FILE_LOC)));
             String fileExtension = props.getProperty("file-extension");
             final PathBuilder pathBuilder = new DynamicPathBuilder(contextPath, fileExtension);
             ViewContainer viewContainer = ViewBuilder.buildViews(getFile(ONT_DIR_LOC), pathBuilder, props); 
             writeFile(new File(DEFAULT_STORAGE_LOCATION + "index.html"), viewContainer.getIndex());
            for (View view : viewContainer.getViews().values()){
                writeFile(new File(DEFAULT_STORAGE_LOCATION
                    + makePathOsAgnostic(view.getPath())), view.getView());
            }
         } catch (FileNotFoundException e){
             throw new RuntimeException(e);
         } catch (IOException e){
             throw new RuntimeException(e);
         }
     }
     
     private static void writeFile(File file, String html){
         FileWriter writer = null;
         try {
             file.getParentFile().mkdirs();
             writer = new FileWriter(file);
             writer.write(html);
         } catch (IOException e){
            throw new RuntimeException(file.getName(), e);
         } finally {
             try {
                if (writer != null){writer.close();}
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     private static File getFile(String fileLoc){
         File file = null;
         try {
             file = new File(App.class.getClassLoader().getResource(fileLoc).toURI());
         } catch (URISyntaxException e){
             log.log(Level.SEVERE, "could not find ontologies for view generation", e);
         }
         return file;
     }
    
    private static String makePathOsAgnostic(String path){
        final StringBuilder pathBuilder = new StringBuilder();
        final StringTokenizer tokenizer = new StringTokenizer(path, "/");
        boolean first = true;
        while (tokenizer.hasMoreElements()){
            if (!first){
                pathBuilder.append(PATH_SEPERATOR);
            } else {
                first = false;
            }
            pathBuilder.append(tokenizer.nextElement());
        }
        return pathBuilder.toString();
    }
 
 }
