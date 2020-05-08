 package hudson.plugins.sctmexecutor;
 
 import hudson.FilePath;
 import hudson.plugins.sctmexecutor.exceptions.SCTMException;
 import hudson.plugins.sctmexecutor.service.ISCTMService;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Text;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import com.borland.sctm.ws.execution.entities.ExecutionResult;
 import com.borland.sctm.ws.execution.entities.TestDefinitionResult;
 import com.borland.sctm.ws.performer.SPNamedEntity;
 
 public class SCTMResultWriter implements ITestResultWriter {
   private static final int PASSED = 1;
 
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor"); //$NON-NLS-1$
   
   private ISCTMService service;
   private FilePath rootDir;
   private boolean ignoreSetupCleanup;
 
   public SCTMResultWriter(FilePath rootDir, ISCTMService service, boolean ignoreSetupCleanup) {
     this.rootDir = rootDir;
     this.service = service;
     this.ignoreSetupCleanup = ignoreSetupCleanup;
   }
 
   @Override
   public void write(ExecutionResult result) throws SCTMException {
     try {
       FilePath execDefFolder = new FilePath(this.rootDir, result.getExecDefName());
         execDefFolder.mkdirs();
       
       if (!ignoreSetupCleanup) {
         writeTestDefResult(result.getSetupTestDef(), execDefFolder, result.getExecDefName());
         writeTestDefResult(result.getCleanupTestDef(), execDefFolder, result.getExecDefName());
       }
       
       for (TestDefinitionResult testResult : result.getTestDefResult()) {
         writeTestDefResult(testResult, execDefFolder, result.getExecDefName());
       }
     } catch (Exception e) {
       String msg = MessageFormat.format(Messages.getString("SCTMResultWriter.err.createResultFolderExecDef"), result.getExecDefName(), result.getExecDefId()); //$NON-NLS-1$
       LOGGER.log(Level.FINE, msg, e);
       throw new SCTMException(msg);
     }
   }
 
   private void writeTestDefResult(TestDefinitionResult testDefResult, FilePath execDefResultFolder, String execDefName) throws SCTMException {
     if (testDefResult == null)
       return;
     String name = testDefResult.getName();
     int testRunId = testDefResult.getTestRunId();
     
     name = name.replaceAll("/|\\|:|\\x2A|\\x3F|<|>|\\x7c", "_"); // \/:*?<>| replace it because not allowed for file names
     
     InputStream result = null;
     try {
       FilePath testDefResFolder = new FilePath(execDefResultFolder, name);
       testDefResFolder.mkdirs();
       SPNamedEntity[] resultFiles = this.service.getResultFiles(testRunId);
       boolean resultFileFound = false;
       for (SPNamedEntity resultFile : resultFiles) {
         String resultFileName = resultFile.getMsName();
         FilePath file = new FilePath(testDefResFolder, resultFileName);
         int miId = resultFile.getMiId();
         result = this.service.loadResultFile(miId);
         if (resultFileName.matches("output.xml")) { //$NON-NLS-1$
           completeAndWriteResultFile(result, file, execDefName, testDefResult.getName());
           resultFileFound = true;
         } else
           file.copyFrom(result);
       }
       
      if (!resultFileFound)
         writeDefaultResultFile(result, testDefResFolder, execDefName, testDefResult);
     } catch (Exception e) {
       String msg = MessageFormat.format(Messages.getString("SCTMResultWriter.err.err.createResultFolderTestDef"), testDefResult.getName(), testDefResult.getTestDefId()); //$NON-NLS-1$
       LOGGER.log(Level.FINE, msg, e);
       throw new SCTMException(msg);
     } finally {
       if (result != null) {
         try {
           result.close();
         } catch (IOException e) {
           throw new SCTMException(e.getMessage());
         }
       }
     }
   }
 
   private void writeDefaultResultFile(InputStream result, FilePath testDefResultFolder, String execDefName, TestDefinitionResult testDefRes) throws IOException, InterruptedException {
     Element root = new Element("TestSuite");
     root.setAttribute("TestItem", execDefName);
     addSubElements(root, 1, testDefRes.getDuration(), testDefRes.getStatus() == PASSED);
     
     Element test = new Element("Test");
     test.setAttribute("TestItem", testDefRes.getName());
    addSubElements(test, 1, testDefRes.getDuration(), testDefRes.getStatus() == PASSED);
     root.addContent(test);
     
     FilePath resultFile = new FilePath(testDefResultFolder, "output.xml");
     Document doc = new Document(root);
     
     writeResultFile(resultFile, doc);
   }
 
   private void completeAndWriteResultFile(InputStream result, FilePath file, String execDefName, String testDefName) throws JDOMException, IOException, InterruptedException {
     Document document = new SAXBuilder().build(result);
     Element rootElement = document.getRootElement();
     Element oldRootElement = (Element) rootElement.clone();
     rootElement.removeContent();
     rootElement.setAttribute("TestItem", execDefName); //$NON-NLS-1$
     rootElement.removeAttribute("ExtId"); //$NON-NLS-1$
     addSubElements(oldRootElement, rootElement);
     Element testDefElement = new Element("TestSuite"); //$NON-NLS-1$
     addSubElements(oldRootElement, testDefElement);
     testDefElement.setAttribute("TestItem", testDefName); //$NON-NLS-1$
     testDefElement.addContent(oldRootElement);
     rootElement.addContent(testDefElement);
     
     writeResultFile(file, document);
   }
 
   private void writeResultFile(FilePath file, Document document) throws IOException, InterruptedException {
     XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
     OutputStream out = null;
     try {
       out = file.write();
       xmlWriter.output(document, out);
     } finally {
       if (out != null)
         out.close();
     }
   }
   
   private void addSubElements(Element testDefElement, int runCount, long duration, boolean success) {
     addCDataElement(testDefElement, "RunCount", String.valueOf(runCount)); //$NON-NLS-1$
     if (!success)
       addCDataElement(testDefElement, "ErrorCount", "1"); //$NON-NLS-1$ //$NON-NLS-2$
     addCDataElement(testDefElement, "Timer", String.valueOf(duration)); //$NON-NLS-1$
     addCDataElement(testDefElement, "WasSuccess", String.valueOf(success)); //$NON-NLS-1$
   }
 
   private void addSubElements(Element oldRootElement, Element testDefElement) {
     addCDataElement(testDefElement, "RunCount", oldRootElement.getChildText("RunCount")); //$NON-NLS-1$ //$NON-NLS-2$
     addCDataElement(testDefElement, "Timer", oldRootElement.getChildText("Timer")); //$NON-NLS-1$ //$NON-NLS-2$
     addCDataElement(testDefElement, "WasSuccess", oldRootElement.getChildText("WasSuccess")); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   private void addCDataElement(Element testDefElement, String elementName, String value) {
     Element elem = new Element(elementName);
     elem.addContent(new Text(value));
     testDefElement.addContent(elem);
   }
 }
