 package com.google.refine.commands.project;
 
import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.refine.ProjectManager;
 import com.google.refine.ProjectMetadata;
 import com.google.refine.browsing.Engine;
 import com.google.refine.browsing.FilteredRows;
 import com.google.refine.browsing.RowVisitor;
 import com.google.refine.commands.Command;
 import com.google.refine.model.Column;
 import com.google.refine.model.Project;
 import com.google.refine.model.Row;
 import com.google.refine.operations.row.ImportPresetsRowRemovalOperation;
 import com.google.refine.process.Process;
 import com.google.refine.util.ParsingUtilities;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.DatasetBuilderException;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidDatasetNameException;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidDimensionTypeException;
 import edu.dfci.cccb.mev.dataset.domain.contract.RawInput;
 import edu.dfci.cccb.mev.dataset.domain.contract.Selection;
 import edu.dfci.cccb.mev.dataset.domain.simple.SimpleSelection;
 import edu.dfci.cccb.mev.dataset.rest.assembly.tsv.UrlTsvInput;
 import edu.dfci.cccb.mev.presets.contract.Preset;
 import edu.dfci.cccb.mev.presets.contract.PresetDescriptor;
 import freemarker.template.utility.NullArgumentException;
 
 public class ViewPresetAnnotationsCommand extends Command {
 
   final static protected Logger logger = LoggerFactory.getLogger("ViewPresetAnnotationsCommand");
   
   @Override
   public void doGet (final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     // TODO Auto-generated method stub
     ProjectManager.getSingleton().setBusy(true);
     try {
         Properties options = ParsingUtilities.parseUrlParameters(request);
         if(!options.containsKey ("import-preset"))
           throw new NullArgumentException ("import-preset");
         
         String datasetName = options.getProperty ("import-preset");
         long projectID = Project.generateID();
         logger.info("Importing existing project using new ID {}", projectID);
 
         PresetDescriptor descriptor = (PresetDescriptor)request.getAttribute ("descriptor");
 //        File file = new File( descriptor.columnUrl ().getFile() );        
 //        String fileName = file.getName().toLowerCase();
 //        InputStream stream = new FileInputStream (file);
         InputStream stream = descriptor.columnUrl ().openStream ();
         String fileName = descriptor.columnUrl ().getFile ();
         
         ProjectManager.getSingleton().importProject(projectID, stream, !fileName.endsWith(".tar"));
         ProjectManager.getSingleton().loadProjectMetadata(projectID);
 
         ProjectMetadata pm = ProjectManager.getSingleton().getProjectMetadata(projectID);
         if (pm != null) {
             if (options.containsKey("project-name")) {
                 String projectName = options.getProperty("project-name");
                 if (projectName != null && projectName.length() > 0) {
                     pm.setName(projectName);
                 }
             }
             
           //ap
             //(try (BufferedReader reader = new BufferedReader(new FileReader (file))){)
             Path pathDataFile = Paths.get(descriptor.dataUrl ().toURI ());
             try(BufferedReader reader = Files.newBufferedReader (pathDataFile, Charset.defaultCharset ())){
               String sHeader = reader.readLine ();
               if(sHeader!=null){
                 String[] arHeader = sHeader.split ("\t");
                 final ArrayList<String> listHeader = new ArrayList<String>(Arrays.asList(arHeader));
                final Map<String, String> mapHeader = new HashMap<String, String>(listHeader.size ());
                 for(String columnName : listHeader)
                   if(!columnName.trim ().equals ("")) mapHeader.put(columnName, null);
                 
                 Project project = ProjectManager.getSingleton().getProject (projectID);
                 
                 final Engine engine = getEngine (request, project);
                final List<Integer> unmatchedRowIndices = new ArrayList <Integer>();
                 
                 RowVisitor visitor = new RowVisitor () {
                   int rowCount = 0;
                   Column theIdColumn;
 
                   @Override
                   public void start (Project project) {
 
                     // if no id column found, assume first column is the id
                     List<Column> columns = project.columnModel.columns;
                     theIdColumn = columns.get (0);
 
                   }
 
                   @Override
                   public boolean visit (Project project, int rowIndex, Row row) {
                     String cellData = row.getCell (theIdColumn.getCellIndex ()).value.toString ();
                     if (mapHeader.containsKey (cellData)) {
 //                      if(logger.isDebugEnabled ())
 //                        logger.debug ("++ will import"+cellData);
                       rowCount++;
                     }else{
                       unmatchedRowIndices.add(rowIndex);
 //                      if(logger.isDebugEnabled ())
 //                        logger.debug ("-- skip import"+rowIndex);
                     }
                     return false;
                   }
 
                   @Override
                   public void end (Project project) {                        
                     try {
                                
                       ImportPresetsRowRemovalOperation op = new ImportPresetsRowRemovalOperation(getEngineConfig(request), unmatchedRowIndices);
                       Process process = op.createProcess(project, new Properties());            
                       project.processManager.queueProcess(process);         
                       
                     } catch (Exception e) {
                       e.printStackTrace();
                     }
                   }
 
                   @Override
                   public boolean pass (Project project, int rowIndex, Row row) {                    
                     return false;
                   }
                 };
                 
                 FilteredRows filteredRows = engine.getAllFilteredRows ();
                 filteredRows.accept (project, visitor);
               }
             }
             
             redirect(response, "/annotations/import-dataset/project?"
                      +"import-preset="+datasetName+"&project=" + projectID);
         } else {
             respondWithErrorPage(request, response, "Failed to import project. Reason unknown.", null);
         }
     } catch (Exception e) {
         respondWithErrorPage(request, response, "Failed to import project", e);
     } finally {
         ProjectManager.getSingleton().setBusy(false);
     }
   }
   
 }
