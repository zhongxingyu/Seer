 package edu.northwestern.bioinformatics.studycalendar.test;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.CompositeDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.dbunit.operation.DatabaseOperation;
import org.dbunit.DatabaseUnitException;
 import org.springframework.web.servlet.HandlerMapping;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
import java.sql.SQLException;
 
 /**
  * @author Rhett Sutphin
  */
 public class LoaderController implements Controller {
     private static Log log = LogFactory.getLog(LoaderController.class);
     private String resourcePrefix;
     private DataSource dataSource;
 
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         response.setContentType("text/plain");
         Writer w = new OutputStreamWriter(response.getOutputStream());
         Loader loader = createLoader(request, w);
 
         loader.load();
         w.close();
 
         return null;
     }
 
     private Loader createLoader(HttpServletRequest request, Writer writer) throws IOException {
         String commaDelim = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
         List<String> names = Arrays.asList(commaDelim.split(","));
         return new Loader(names, writer);
     }
 
     public void setResourcePrefix(String resourcePrefix) {
         this.resourcePrefix = resourcePrefix;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     private class Loader {
         private List<String> names;
         private Writer out;
 
         private Loader(List<String> names, Writer out) {
             this.names = names;
             this.out = out;
         }
 
         public void load() throws Exception {
            try {
                executeLoad();
            } catch (Exception e) {
                error("Load failed due to exception", e);
            }
        }

        private void executeLoad() throws IOException, SQLException, DatabaseUnitException {
             log("Opening connection");
             IDatabaseConnection conn = new DatabaseConnection(dataSource.getConnection());
 
             log("Creating merged dataset from " + names);
             List<IDataSet> sets = new ArrayList<IDataSet>();
             for (String name : names) {
                 InputStream resource = getResourceFor(name);
                 if (resource != null) {
                     sets.add(new FlatXmlDataSet(resource));
                 }
             }
             if (sets.size() == 0) {
                 error("No datasets found");
             } else {
                 CompositeDataSet dataset = new CompositeDataSet(sets.toArray(new IDataSet[sets.size()]));
 
                 log("Wiping all configured tables");
                 DatabaseOperation.DELETE_ALL.execute(conn, dataset);
 
                 log("Inserting test data");
                 DatabaseOperation.INSERT.execute(conn, dataset);
 
                 log("Load complete");
             }
         }
 
         private InputStream getResourceFor(String name) throws IOException {
             StringBuilder sb = new StringBuilder().append(resourcePrefix);
             if (sb.charAt(sb.length() - 1) != '/') sb.append('/');
             String resName = sb.append(name).append(".xml").toString();
 
             log(" - Loading dataset " + name);
             InputStream resource = getClass().getResourceAsStream(resName);
             if (resource == null) {
                 error("   Cannot find resource " + resName + " implied by " + name);
             }
             return resource;
         }
 
         private void log(String message) throws IOException {
             log.info(message);
             out.write("INFO  " + message + '\n');
             out.flush();
         }
 
         private void error(String message) throws IOException {
            error(message, null);
        }

        private void error(String message, Exception e) throws IOException {
            if (e == null) {
                log.error(message);
            } else {
                log.error(message, e);
            }

             out.write("ERROR " + message + '\n');
            if (e != null) {
                out.write("      " + e.getMessage() + '\n');
                for (StackTraceElement element : e.getStackTrace()) {
                    out.write("        " + element + '\n');
                }
            }

             out.flush();
         }
     }
 }
