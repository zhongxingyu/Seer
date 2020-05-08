 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.archive;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.persistence.PersistenceException;
 
 import org.apache.commons.lang.Validate;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlOptions;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jubula.client.archive.i18n.Messages;
 import org.eclipse.jubula.client.archive.output.NullImportOutput;
 import org.eclipse.jubula.client.archive.schema.ContentDocument;
 import org.eclipse.jubula.client.archive.schema.ContentDocument.Content;
 import org.eclipse.jubula.client.archive.schema.Project;
 import org.eclipse.jubula.client.core.businessprocess.IParamNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.core.persistence.PMSaveException;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.exception.JBVersionException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author BREDEX GmbH
  * @created 11.01.2006
  */
 @SuppressWarnings("synthetic-access")
 public class XmlStorage {
     /**
      * Helper for IO-related tasks that can be cancelled.
      *
      * @author BREDEX GmbH
      * @created Dec 3, 2007
      */
     private static class IOCanceller extends TimerTask {
         
         /** The monitor for which the IO is taking place. */
         private IProgressMonitor m_monitor;
         
         /** The output stream in which the IO is taking place. */
         private OutputStream m_outputStream;
         
         /** The Timer used to schedule regular interruption checks. */
         private Timer m_timer;
         
         /**
          * Constructor
          * 
          * @param monitor The monitor for which the IO is taking place.
          * @param outputStream The output stream in which the IO is taking place.
          */
         public IOCanceller(IProgressMonitor monitor, 
             OutputStream outputStream) {
             
             m_monitor = monitor;
             m_outputStream = outputStream;
             m_timer = new Timer();
         }
         
         /**
          * Signal that the IO task is about to start.
          */
         public void startTask() {
             m_timer.schedule(this, 1000, 1000);
         }
 
         /**
          * Signal that the IO task has finished.
          */
         public void taskFinished() {
             m_timer.cancel();
         }
 
         /**
          * Check whether the operation has been cancelled. If so, the output
          * stream will be closed.
          */
         private void checkTask() {
             if (m_monitor.isCanceled()) {
                 try {
                     m_outputStream.close();
                 } catch (IOException e) {
                     log.error(Messages.ErrorWhileCloseOS, e);
                 }
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             checkTask();
         }
     }
     
     /** XML header encoding definition */
     public static final String RECOMMENDED_CHAR_ENCODING = "UTF-8"; //$NON-NLS-1$
     
     /**
      * The supported character encodings.
      */
     private static final String[] SUPPORTED_CHAR_ENCODINGS = 
         new String[]{RECOMMENDED_CHAR_ENCODING, "UTF-16"};  //$NON-NLS-1$
     
     /** XML header intro */
     private static final String XML_HEADER_START = "<?xml"; //$NON-NLS-1$
     /** XML header end */
     private static final String XML_HEADER_END = "?>"; //$NON-NLS-1$
     
     /** centralized definition of characters for XML header */
     private static final char QUOTE = '"';
     /** centralized definition of characters for XML header */
     private static final char SPACE = ' ';
     /** centralized definition of characters for XML header */
 
     /**
      * the current xml schema namespace
      */
     private static final String SCHEMA_NAMESPACE = "http://www.eclipse.org/jubula/client/archive/schema"; //$NON-NLS-1$
 
     /** name of GUIdancer import/export XML element representing Exec Test Cases */
     private static final String EXEC_TC_XML_ELEMENT_NAME = "usedTestcase"; //$NON-NLS-1$
 
     /** XPATH statement for selecting all Exec Test Cases */
     private static final String XPATH_FOR_EXEC_TCS = "declare namespace s='" + SCHEMA_NAMESPACE + "' " + //$NON-NLS-1$//$NON-NLS-2$
             ".//s:" + EXEC_TC_XML_ELEMENT_NAME; //$NON-NLS-1$
 
     /** standard logging */
     private static Logger log = LoggerFactory.getLogger(XmlStorage.class);
 
     /**
      * the old xml schema namespace (< 5.0)
      */
     private static final String OLD_SCHEMA_NAMESPACE = "http://www.bredexsw.com/guidancer/client/importer/gdschema"; //$NON-NLS-1$
         
     /**
      * Generate an XML document representing the content of the project.
      * 
      * @param project
      *            the root of the data
      * @param includeTestResultSummaries
      *            Whether to save the Test Result Summaries as well.
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @return a String which contains the XML representation, or
      *         <code>null</code> if the operation was cancelled.
      * @throws PMException
      *             of io or encoding errors
      * @throws ProjectDeletedException
      *             in case of current project is already deleted
      */
     private static String save(IProjectPO project, 
             boolean includeTestResultSummaries, IProgressMonitor monitor) 
         throws ProjectDeletedException, PMException {
         XmlOptions genOpts = new XmlOptions();
         genOpts.setCharacterEncoding(RECOMMENDED_CHAR_ENCODING);
         genOpts.setSaveInner();
         genOpts.setSaveAggressiveNamespaces();
         genOpts.setUseDefaultNamespace();
        // Don't make use of pretty print due to 395788
        // genOpts.setSavePrettyPrint();
 
         ContentDocument contentDoc = ContentDocument.Factory
             .newInstance(genOpts);
         Content content = contentDoc.addNewContent();
 
         Project prj = content.addNewProject();
 
         try {
             new XmlExporter(monitor).fillProject(
                     prj, project, includeTestResultSummaries);
         } catch (OperationCanceledException oce) {
             // Operation was cancelled.
             log.info(Messages.ExportOperationCanceled);
             return null;
         }
 
         if (monitor.isCanceled()) {
             // Operation was cancelled.
             return null;
         }
         
         XmlOptions options = new XmlOptions(genOpts);
 
         Collection errors = new ArrayList();
         options.setErrorListener(errors);
         if (!contentDoc.validate(options)) {
             StringBuilder msgs = new StringBuilder(StringConstants.NEWLINE);
             for (Object msg : errors) {
                 msgs.append(msg);
             }
             if (log.isDebugEnabled()) {
                 log.debug(Messages.ValidateFailed 
                         + StringConstants.COLON, msgs);
                 log.debug(Messages.ValidateFailed 
                         + StringConstants.COLON, contentDoc);
             }
             throw new PMSaveException(
                 "XML" + Messages.ValidateFailed + msgs.toString(), //$NON-NLS-1$
                 MessageIDs.E_FILE_IO);
         }
         return contentDoc.xmlText(genOpts);
     }
     
  
     /**
      * Takes the supplied xmlString and parses it. According to the content an
      * instance of IProjetPO along with its associated components is created.
      * 
      * @param xmlString
      *            XML representation of a project
      * @param assignNewGuid
      *            <code>true</code> if the project and all subnodes should be
      *            assigned new GUIDs. Otherwise <code>false</code>.
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @param io
      *            the device to write the import output
      * @return an transient IProjectPO and its components
      * @throws PMReadException
      *             in case of a malformed XML string
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public static IProjectPO load(String xmlString, boolean assignNewGuid, 
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, IProgressMonitor monitor,
         IProgressConsole io) 
         throws PMReadException, JBVersionException, InterruptedException {
         
         return load(xmlString, assignNewGuid, null, null, paramNameMapper, 
                 compNameCache, monitor, io);
     }
     
     /**
      * Takes the supplied xmlString and parses it. According to the content an
      * instance of IProjetPO along with its associated components is created.
      * 
      * @param xmlString
      *            XML representation of a project
      * @param assignNewGuid
      *            <code>true</code> if the project and all subnodes should be
      *            assigned new GUIDs. Otherwise <code>false</code>.
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @return an transient IProjectPO and its components
      * @throws PMReadException
      *             in case of a malformed XML string
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public static IProjectPO load(String xmlString, boolean assignNewGuid, 
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, IProgressMonitor monitor) 
         throws PMReadException, JBVersionException, InterruptedException {
         return load(xmlString, assignNewGuid, null, null, paramNameMapper, 
                 compNameCache, monitor, new NullImportOutput());
     }
 
     /**
      * Takes the supplied xmlString and parses it. According to the content an
      * instance of IProjetPO along with its associated components is created.
      * 
      * @param xmlString
      *            XML representation of a project
      * @param assignNewGuid
      *            Flag for assigning the project a new GUID and version
      * @param majorVersion
      *            Major version number for the created object, or
      *            <code>null</code> if the version from the imported XML should
      *            be used.
      * @param minorVersion
      *            Minor version number for the created object, or
      *            <code>null</code> if the version from the imported XML should
      *            be used.
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @return an transient IProjectPO and its components
      * @throws PMReadException
      *             in case of a malformed XML string
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public static IProjectPO load(String xmlString, boolean assignNewGuid, 
         Integer majorVersion, Integer minorVersion,
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, IProgressMonitor monitor) 
         throws PMReadException, JBVersionException, InterruptedException {
         return load(xmlString, assignNewGuid, majorVersion, minorVersion, 
                 paramNameMapper, compNameCache, 
                 monitor, new NullImportOutput());
     }
     
     /**
      * Takes the supplied xmlString and parses it. According to the content an
      * instance of IProjetPO along with its associated components is created.
      * 
      * @param xmlString
      *            XML representation of a project
      * @param assignNewGuid
      *            Flag for assigning the project a new GUID and version
      * @param majorVersion
      *            Major version number for the created object, or
      *            <code>null</code> if the version from the imported XML should
      *            be used.
      * @param minorVersion
      *            Minor version number for the created object, or
      *            <code>null</code> if the version from the imported XML should
      *            be used.
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @param io
      *            the device to write the import output
      * @return an transient IProjectPO and its components
      * @throws PMReadException
      *             in case of a malformed XML string
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public static IProjectPO load(String xmlString, boolean assignNewGuid, 
         Integer majorVersion, Integer minorVersion,
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, IProgressMonitor monitor,
         IProgressConsole io) 
         throws PMReadException, JBVersionException, InterruptedException {
         
         ContentDocument contentDoc;
         try {
             contentDoc = getContent(xmlString);
             Project projectXml = contentDoc.getContent().getProject();
             int numExecTestCases = 
                 projectXml.selectPath(XPATH_FOR_EXEC_TCS).length;
             
             monitor.beginTask(StringConstants.EMPTY, numExecTestCases + 1);
             monitor.worked(1);
             
             if (assignNewGuid) {
                 return new XmlImporter(monitor, io).createProject(
                     projectXml, assignNewGuid, paramNameMapper, compNameCache);
             } else if (majorVersion != null && minorVersion != null) {
                 return new XmlImporter(monitor, io).createProject(
                     projectXml, majorVersion, minorVersion, paramNameMapper, 
                     compNameCache);
             }
             return new XmlImporter(monitor, io).createProject(projectXml, 
                     paramNameMapper, compNameCache);
         } catch (XmlException e) {
             throw new PMReadException(Messages.InvalidImportFile,
                 MessageIDs.E_LOAD_PROJECT);
         } catch (InvalidDataException e) {
             throw new PMReadException(Messages.InvalidImportFile,
                 e.getErrorId());
         } 
     }
 
     /**
      * Reads the content from a string containing XML data
      * @param xmlString XML data
      * @return a ContentDocument which represents the XML data
      * @throws XmlException  if the parsing fails
      * @throws PMReadException if the validation fails
      */
     private static ContentDocument getContent(String xmlString)
         throws XmlException, PMReadException {
         Map<String, String> substitutes = new HashMap<String, String>();
         substitutes.put(OLD_SCHEMA_NAMESPACE, SCHEMA_NAMESPACE);
         XmlOptions options = new XmlOptions();
         options.setLoadSubstituteNamespaces(substitutes);
 
         ContentDocument contentDoc = ContentDocument.Factory.parse(xmlString,
                 options);
         Collection errors = new ArrayList();
         options.setErrorListener(errors);
         if (!contentDoc.validate(options)) {
             StringBuilder msgs = new StringBuilder(StringConstants.NEWLINE);
             for (Object msg : errors) {
                 msgs.append(msg);
             }
             if (log.isDebugEnabled()) {
                 log.debug(Messages.ValidateFailed 
                         + StringConstants.COLON, msgs);
                 log.debug(Messages.ValidateFailed 
                         + StringConstants.COLON, contentDoc);
             }
             throw new PMReadException(Messages.InvalidImportFile
                     + msgs.toString(), MessageIDs.E_LOAD_PROJECT);
         }
         return contentDoc;
     }
 
     /**
      * Save a project as XML to a file or return the serialized project as
      * string, if fileName == null!
      * 
      * @param proj
      *            proj to be saved
      * @param fileName
      *            name for file to save or null, if wanting to get the project
      *            as serialized string
      * @param includeTestResultSummaries
      *            Whether to save the Test Result Summaries as well.
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @return the serialized project as string, if fileName == null<br>
      *         or<br>
      *         <b>Returns:</b><br>
      *         null otherwise. Always returns <code>null</code> if the save
      *         operation was canceled.
      * @throws PMException
      *             if save failed for any reason
      * @throws ProjectDeletedException
      *             in case of current project is already deleted
      */
     public static String save(IProjectPO proj, String fileName, 
             boolean includeTestResultSummaries, IProgressMonitor monitor)
         throws ProjectDeletedException, PMException {
         
         return save(proj, fileName, includeTestResultSummaries, 
                 monitor, false, null);
     }
 
     /**
      * Save a project as XML to a file or return the serialized project as
      * string, if fileName == null!
      * 
      * @param proj
      *            proj to be saved
      * @param fileName
      *            name for file to save or null, if wanting to get the project
      *            as serialized string
      * @param includeTestResultSummaries
      *            Whether to save the Test Result Summaries as well.
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @param writeToSystemTempDir
      *            Indicates whether the project has to be written to the system
      *            temp directory
      * @param listOfProjectFiles
      *            If a project is written into the temp dir then the written
      *            file is added to the list, if the list is not null.
      * @return the serialized project as string, if fileName == null<br>
      *         or<br>
      *         <b>Returns:</b><br>
      *         null otherwise. Always returns <code>null</code> if the save
      *         operation was canceled.
      * @throws PMException
      *             if save failed for any reason
      * @throws ProjectDeletedException
      *             in case of current project is already deleted
      */
     public static String save(IProjectPO proj, String fileName,
             boolean includeTestResultSummaries,
             IProgressMonitor monitor, boolean writeToSystemTempDir, 
             List<File> listOfProjectFiles)
         throws ProjectDeletedException, PMException {
 
         monitor.beginTask(Messages.XmlStorageSavingProject, 
             getWorkToSave(proj));
                 
         Validate.notNull(proj);
         FileOutputStream fOut = null;
         try {
             String xml = buildXmlHeader() 
                 + XmlStorage.save(proj, includeTestResultSummaries, monitor);
 
             if (fileName == null) {
                 return xml;
             }
             
             if (writeToSystemTempDir) {
                 File fileInTempDir = createTempFile(fileName);
                 if (listOfProjectFiles != null) {
                     listOfProjectFiles.add(fileInTempDir);
                 }
                 fOut = new FileOutputStream(fileInTempDir);
             } else  {
                 fOut = new FileOutputStream(fileName);
             }
             
             IOCanceller canceller = 
                 new IOCanceller(monitor, fOut);
             canceller.startTask();
             fOut.write(xml.getBytes(RECOMMENDED_CHAR_ENCODING));
             canceller.taskFinished();
         } catch (FileNotFoundException e) {
             log.debug(Messages.File + StringConstants.SPACE 
                     + Messages.NotFound, e);
             throw new PMSaveException(Messages.File + StringConstants.SPACE 
                     + fileName + Messages.NotFound + StringConstants.COLON 
                     + StringConstants.SPACE 
                     + e.toString(), MessageIDs.E_FILE_IO);
         } catch (IOException e) {
             // If the operation has been canceled, then this is just
             // a result of cancelling the IO.
             if (!monitor.isCanceled()) {
                 log.debug(Messages.GeneralIoExeption, e);
                 throw new PMSaveException(Messages.GeneralIoExeption 
                         + e.toString(), MessageIDs.E_FILE_IO);
             }
         } catch (PersistenceException e) {
             log.debug(Messages.CouldNotInitializeProxy 
                     + StringConstants.DOT, e);
             throw new PMSaveException(e.getMessage(),
                 MessageIDs.E_DATABASE_GENERAL);
         } finally {
             if (fOut != null) {
                 try {
                     fOut.close();
                 } catch (IOException e) {
                     // just log, we are already done
                     log.error(Messages.CantCloseOOS + fOut.toString(), e);
                 }
             }
         }
         return null;
     }
 
     /**
      * Creates a file with the given name in the system temp directory.
      * @param fileName The name of the file to be created in temp dir
      * @return the created file
      */
     private static File createTempFile(String fileName) throws IOException {
         final String fileNamePrefix;
         final String fileNameSuffix;
         int dotIndex = fileName.lastIndexOf(StringConstants.DOT);
         
         if (dotIndex < 0) {
             fileNamePrefix = fileName;
             fileNameSuffix = StringConstants.EMPTY;
         } else {
             fileNamePrefix = fileName.substring(0, dotIndex) 
                 + StringConstants.UNDERSCORE;
             fileNameSuffix = fileName.substring(dotIndex);
         }
         File fileInTempDir = 
             File.createTempFile(fileNamePrefix, fileNameSuffix);
         
         return fileInTempDir;
     }
 
     /**
      * @return the custom XML header containing the XML export version info
      */
     private static String buildXmlHeader() {
         StringBuilder res = new StringBuilder(XML_HEADER_START);
         res.append(StringConstants.SPACE);
         res.append("version"); //$NON-NLS-1$
         res.append(StringConstants.EQUALS_SIGN);
         res.append(QUOTE);
         res.append("1"); //$NON-NLS-1$
         res.append(StringConstants.DOT);
         res.append("0"); //$NON-NLS-1$
         res.append(QUOTE);
         res.append(StringConstants.SPACE);
         res.append("encoding"); //$NON-NLS-1$
         res.append(StringConstants.EQUALS_SIGN);
         res.append(QUOTE);
         res.append(RECOMMENDED_CHAR_ENCODING);
         res.append(QUOTE);               
         res.append(SPACE);
         res.append(XML_HEADER_END);
         res.append(StringConstants.NEWLINE);
         
         return res.toString();
     }
     
     /**
      * read data from a file using a specified character encoding
      * @param projectURL the project xml URL
      * @param encoding character encoding. Must be a supported encoding or an
      * UnsupportedEncodingException will be thrown. Null is allowed and means
      * system default encoding.
      * @return a Stringbuilder holding the characters from the file
      * @throws IOException  in  case of io problems
      */
     public static StringBuilder readFileContent(
         URL projectURL, String encoding) throws IOException {
         StringBuilder result = new StringBuilder();            
         
         BufferedReader in = null;
         try {
             if (encoding != null) {
                 in = new BufferedReader(new InputStreamReader(
                         projectURL.openStream(), encoding));
             } else {
                 in = new BufferedReader(new InputStreamReader(
                         projectURL.openStream()));
             }
 
             char buff[] = new char[10240];
             int transfer;
             while ((transfer = in.read(buff)) != -1) {
                 result.append(buff, 0, transfer);
             }
         } finally {
             if (in != null) {
                 in.close();
             }
         }
         return result;
     }
     /**
      * Reads the content of the file and returns it as a string. 
      * @param fileURL The URL of the project to import
      * @return The file content
      * @throws PMReadException If the file couldn't be read (wrong file name, IOException)
      */
     private static String readProjectFile(URL fileURL)
         throws PMReadException {
         try {
             final String encoding = getCharacterEncoding(fileURL);
             StringBuilder result = readFileContent(fileURL, encoding);
             String content = checkAndReduceXmlHeader(result);
             return content;            
         } catch (FileNotFoundException e) {
             log.debug(Messages.ClassSerializedCouldntFound, e);
             throw new PMReadException(e.toString(), 
                 MessageIDs.E_FILE_NOT_FOUND);
         } catch (IOException e) {
             log.debug(Messages.FailedReadingFile + StringConstants.COLON 
                     + StringConstants.SPACE + fileURL.getFile());
             throw new PMReadException(e.toString(), MessageIDs.E_FILE_IO);
         } catch (XmlException e) {
             log.debug(Messages.MalformedXMLData);
             throw new PMReadException(e.toString(), MessageIDs.E_LOAD_PROJECT);
         }
     }
 
     /**
      * Gets the character encoding of the given XML-URL.
      * 
      * @param xmlProjectURL
      *            a URL-object which must point a valid XML-Structure.
      * @return The encoding (e.g. UTF-8, UTF-16, ...).
      * @see SUPPORTED_CHAR_ENCODINGS
      * @throws IOException
      *             in case of reading error.
      */
     public static String getCharacterEncoding(URL xmlProjectURL) throws 
         IOException {
         for (String encoding : SUPPORTED_CHAR_ENCODINGS) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     xmlProjectURL.openStream(), encoding));
             final String firstLine = reader.readLine();
             if (firstLine != null && firstLine.contains(encoding)) {
                 return encoding;
             }
         }
         throw new IOException(Messages.NoSupportedFileEncoding 
                 + StringConstants.EXCLAMATION_MARK);
     }
 
     /**
      * @param xmlData the contents of the import file
      * @return the xmlData without the header
      * @throws XmlException if no header available or the version doesn't match
      */
     private static String checkAndReduceXmlHeader(StringBuilder xmlData)
         throws XmlException {
         int startPos = xmlData.indexOf(XML_HEADER_START); 
         int endPos = xmlData.indexOf(XML_HEADER_END);
         if (startPos != 0 || endPos == -1) {
             // wrong header, probably invalid
             throw new XmlException(Messages.NoHeaderFound);
         }
         
         endPos += XML_HEADER_END.length();
         
         return xmlData.substring(endPos);
     }
     
     /**
      * @param xmlData the contents for the "Save As..." action
      * @return the xmlData without the header
      * @throws PMReadException if no header available or the version doesn't match
      */
     public String checkAndReduceXmlHeaderForSaveAs(StringBuilder xmlData)
         throws PMReadException {
         
         String result = null;
         try {
             result = checkAndReduceXmlHeader(xmlData);
         } catch (XmlException e) {
             throw new PMReadException(e.toString(), MessageIDs.
                 E_SAVE_AS_PROJECT_FAILED);
         }
         return result;
     }
 
     /**
      * read a <code> GeneralStorage </code> object from filename <b> call
      * getProjectAutToolKit(String filename) at first </b>
      * 
      * @param fileURL
      *            URL of the project file to read
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param assignNewGuids
      *            <code>true</code> if new GUIDs should be created for each PO.
      *            <code>false</code> if old GUIDs should be used.
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @param io
      *            the device to write the import output
      * @return the persisted object
      * @throws PMReadException
      *             in case of error
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public IProjectPO readProject(URL fileURL, 
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, boolean assignNewGuids, 
         IProgressMonitor monitor, IProgressConsole io) throws PMReadException, 
         JBVersionException, InterruptedException {
 
         return load(readProjectFile(fileURL), assignNewGuids, paramNameMapper, 
                 compNameCache, monitor, io);
     }
     
     /**
      * read a <code> GeneralStorage </code> object from filename <b> call
      * getProjectAutToolKit(String filename) at first </b>
      * 
      * @param fileURL
      *            the URL of project file to read
      * @param paramNameMapper
      *            mapper to resolve param names
      * @param compNameCache
      *            cache to resolve component names
      * @param assignNewGuids
      *            <code>true</code> if new GUIDs should be created for each PO.
      *            <code>false</code> if old GUIDs should be used.
      * @param monitor
      *            The progress monitor for this potentially long-running
      *            operation.
      * @return the persisted object
      * @throws PMReadException
      *             in case of error
      * @throws JBVersionException
      *             in case of version conflict between used toolkits of imported
      *             project and the installed Toolkit Plugins
      * @throws InterruptedException
      *             if the operation was canceled.
      */
     public IProjectPO readProject(URL fileURL, 
         IParamNameMapper paramNameMapper, 
         IWritableComponentNameCache compNameCache, boolean assignNewGuids, 
         IProgressMonitor monitor) throws PMReadException, 
         JBVersionException, InterruptedException {
 
         return load(readProjectFile(fileURL), assignNewGuids, paramNameMapper, 
                 compNameCache, monitor, new NullImportOutput());
     }
 
 
     /**
      * 
      * @param project The project for which the work is predicted.
      * @return The predicted amount of work required to save a project.
      */
     public static int getWorkToSave(IProjectPO project) {
         return new XmlExporter(new NullProgressMonitor())
             .getPredictedWork(project);
     }
 
     /**
      * 
      * @param projectsToSave The projects for which the work is predicted.
      * @return The predicted amount of work required to save the
      *         given projects.
      */
     public static int getWorkToSave(List<IProjectPO> projectsToSave) {
         int totalWork = 0;
         
         for (IProjectPO project : projectsToSave) {
             totalWork += getWorkToSave(project);
         }
 
         return totalWork;
     }
 }
