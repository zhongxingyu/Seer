 /*
  * $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The DOMS project.
  * Copyright (C) 2007-2010  The State and University Library
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package dk.statsbiblioteket.doms.ingesters.radiotv;
 
 import dk.statsbiblioteket.doms.client.DOMSWSClient;
 import dk.statsbiblioteket.doms.client.FileInfo;
 import dk.statsbiblioteket.doms.client.NoObjectFound;
 import dk.statsbiblioteket.doms.client.ServerOperationFailed;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.validation.Schema;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author &lt;tsh@statsbiblioteket.dk&gt;
  */
 public class RadioTVMetadataProcessor implements HotFolderScannerClient {
 
     private static final String RITZAU_ORIGINALS_ELEMENT = "//program/originals/ritzau_original";
     private static final String GALLUP_ORIGINALS_ELEMENT = "//program/originals/gallup_original";
     private static final String HAS_METAFILE_RELATION_TYPE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasShard";
     private static final String CONSISTS_OF_RELATION_TYPE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#consistsOf";
     private static final String RECORDING_PBCORE_DESCRIPTION_DOCUMENT_ELEMENT = "//program/pbcore/pbc:PBCoreDescriptionDocument";
     private static final String RECORDING_FILES_FILE_ELEMENT = "//program/program_recording_files/file";
     private static final String FILE_URL_ELEMENT = "file_url";
     private static final String FILE_NAME_ELEMENT = "file_name";
     private static final String FORMAT_URI_ELEMENT = "format_uri";
     private static final String MD5_SUM_ELEMENT = "md5_sum";
 
     private static final String PROGRAM_TEMPLATE_PID = "doms:Template_Program";
     private static final String PROGRAM_PBCORE_DS_ID = "PBCORE";
     private static final String RITZAU_ORIGINAL_DS_ID = "RITZAU_ORIGINAL";
     private static final String GALLUP_ORIGINAL_DS_ID = "GALLUP_ORIGINAL";
     private static final String META_FILE_TEMPLATE_PID = "doms:Template_Shard";
     private static final String META_FILE_METADATA_DS_ID = "SHARD_METADATA";
     private static final String RADIO_TV_FILE_TEMPLATE_PID = "doms:Template_RadioTVFile";
     private static final int MAX_FAIL_COUNT = 3;
 
     private final File failedFilesFolder;
     private final File processedFilesFolder;
     private final Schema preIngestFileSchema;
     private final DOMSLoginInfo domsLoginInfo;
     private final XPathFactory xPathFactory;
 
     private int exceptionCount = 0;
 
 
     private DocumentBuilder preingestFilesBuilder;
     private DocumentBuilder unSchemaedBuilder;
     private DOMSWSClient domsClient;
 
 
     public RadioTVMetadataProcessor(DOMSLoginInfo domsLoginInfo,
                                     File failedFilesFolder, File processedFilesFolder,
                                     Schema preIngestFileSchema) {
         this.failedFilesFolder = failedFilesFolder;
         this.processedFilesFolder = processedFilesFolder;
         this.preIngestFileSchema = preIngestFileSchema;
         this.domsLoginInfo = domsLoginInfo;
 
         xPathFactory = XPathFactory.newInstance();
 
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                 .newInstance();
         DocumentBuilderFactory unschemaedFactory = DocumentBuilderFactory
                 .newInstance();
         documentBuilderFactory.setSchema(preIngestFileSchema);
         documentBuilderFactory.setNamespaceAware(true);
         try {
             preingestFilesBuilder = documentBuilderFactory
                     .newDocumentBuilder();
 
             unSchemaedBuilder = unschemaedFactory.newDocumentBuilder();
 
         } catch (ParserConfigurationException pce) {
             pce.printStackTrace();
             // TODO: Log this
             // TODO: we should not allow endless failures....
             // TODO: FATAL
             fatalException();
             return;//will never be reached, but no matter
         }
 
         ErrorHandler documentErrorHandler = new org.xml.sax.ErrorHandler() {
 
             @Override
             public void warning(SAXParseException exception)
                     throws SAXException {
                 throw exception;
             }
 
             @Override
             public void fatalError(SAXParseException exception)
                     throws SAXException {
                 throw exception;
             }
 
             @Override
             public void error(SAXParseException exception)
                     throws SAXException {
                 throw exception;
             }
         };
         preingestFilesBuilder.setErrorHandler(documentErrorHandler);
 
 
 
     }
 
     public DOMSWSClient getDomsClient() {
         if (domsClient == null){
             domsClient = new DOMSWSClient();
             domsClient.login(domsLoginInfo.getDomsWSAPIUrl(), domsLoginInfo
                     .getLogin(), domsLoginInfo.getPassword());
 
         }
         return domsClient;
     }
 
     /* (non-Javadoc)
     * @see dk.statsbiblioteket.doms.ingesters.radiotv.HotFolderScannerClient#fileAdded(java.io.File)
     */
     public void fileAdded(File addedFile) {
         List<String> pidsToPublish = new ArrayList<String>();
         try {
             try {
 
                 final Document radioTVMetadata = preingestFilesBuilder
                         .parse(addedFile);
 
 
                 final List<String> filePIDs = ingestFiles(
                         radioTVMetadata, getDomsClient());
                 pidsToPublish.addAll(filePIDs);
 
                 final String metaFilePID = ingestMetaFile(
                         radioTVMetadata, filePIDs, getDomsClient());
                 pidsToPublish.add(metaFilePID);
 
                 final String programPID = ingestProgram(
                         radioTVMetadata, metaFilePID, getDomsClient());
                 pidsToPublish.add(programPID);
 
                 getDomsClient().publishObjects(pidsToPublish);
                 writeActivePID(programPID, addedFile);
 
                 // Move the processed file to the finished files folder.
                 moveFile(addedFile, processedFilesFolder);
 
 
 
             } catch (SAXException se) {
                 failed(addedFile,pidsToPublish, getDomsClient());
 
                 se.printStackTrace();
                 // TODO: Log this
                 // TODO: we should not allow endless failures....
                 // TODO: Wounded > many = FATAL
                 incrementFailedTries();
 
             } catch (ServerOperationFailed se) {
                 failed(addedFile,pidsToPublish, getDomsClient());
 
                 // Failed calling the DOMS server
                 se.printStackTrace();
                 // TODO: Log this
                 // TODO: FATAL, POSSIBLE RETIRES
                 // TODO: we should not allow endless failures....
 
             } catch (XPathExpressionException xpee) {
                 failed(addedFile,pidsToPublish, getDomsClient());
 
                 // Failed parsing the Radio-TV XML document...
                 xpee.printStackTrace();
                 // TODO: Log this
                 // TODO: we should not allow endless failures....
                 // TODO: should _NEVER_ happend, code is broken
 
             } catch (URISyntaxException use) {
                 // Failed parsing the Radio-TV XML document...
 
                 failed(addedFile,pidsToPublish, getDomsClient());
 
                 use.printStackTrace();
                 // TODO: Log this
                 // TODO: we should not allow endless failures....
                 // failure can be from fileIngest as a pre ibngest error
                 // or from ingestMetaFile as a config/code error
 
             } catch (IOException ioe) {
                 failed(addedFile,pidsToPublish, getDomsClient());
                 ioe.printStackTrace();
                 // TODO: Log this
                 // TODO: we should not allow endless failures....
                 // a code error for ingestMetaFile
             }
         } catch (Exception fnfe) {
             // TODO: Log this.
             fnfe.printStackTrace();
         }
 
 
     }
 
     private void writeActivePID(String programPID, File failedMetadataFile) throws IOException {
         File activePIDsFile = new File(Ingester.LUKEWARM_FOLDER.getAbsolutePath(), failedMetadataFile
                 .getName() + ".InProcessPIDs");
         if (!activePIDsFile.exists()){
             try{
                 activePIDsFile.createNewFile();
             } catch (Exception e){
                 System.err.println(e);
             }
         }
         if (activePIDsFile.canWrite()){
             FileWriter fstream = new FileWriter(activePIDsFile);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(programPID + '\n');
             out.flush();
             out.close();
 
         }   else {
             throw new IOException("Cannot write file " + activePIDsFile.getAbsolutePath());
         }
     }
 
     private void failed(File addedFile, List<String> pidsToPublish, DOMSWSClient domsClient)
             throws FileNotFoundException, ServerOperationFailed {
         moveFile(addedFile, failedFilesFolder);
         try {
             for ( String programPID : pidsToPublish) {
                 writeActivePID(programPID, addedFile);
             }
         } catch(Exception e) {
             System.err.println(e);
         }
         // writeFailedPIDs(addedFile, pidsToPublish, failedFilesFolder);
         // Attempt to delete partial ingest
         domsClient.deleteObjects(pidsToPublish);
     }
 
 
     /* (non-Javadoc)
      * @see dk.statsbiblioteket.doms.ingesters.radiotv.HotFolderScannerClient#fileDeleted(java.io.File)
      */
     public void fileDeleted(File deletedFile) {
         // Not relevant.
     }
 
     /* (non-Javadoc)
      * @see dk.statsbiblioteket.doms.ingesters.radiotv.HotFolderScannerClient#fileModified(java.io.File)
      */
 
     public void fileModified(File modifiedFile) {
         // Not relevant.
     }
 
     /**
      * @param radioTVMetadata
      *            Bibliographical metadata about the program.
      * @param metafilePID
      *            PID to the metafile which represents the program data.
      * @return PID of the newly created program object, created by the DOMS.
      * @throws ServerOperationFailed
      *             if creation or manipulation of the program object fails.
      * @throws XPathExpressionException
      *             if any errors were encountered while processing the
      *             <code>radioTVMetadata</code> XML document.
      * @throws ParserConfigurationException
      *             if creation of a <code>DocumentBuilder</code> instance fails.
      */
     private String ingestProgram(
             Document radioTVMetadata, String metafilePID, DOMSWSClient domsClient)
             throws ServerOperationFailed, XPathExpressionException {
 
         // First, fetch the PBCore metadata document node from the pre-ingest
         // document.
 
         final XPath xPath = this.xPathFactory.newXPath();
         // FIXME! Remove uglyness!
         NamespaceContext pbcoreNamespace = new NamespaceContext() {
 
             private final String namespaceURI = "http://www.pbcore.org/PBCore/PBCoreNamespace.html";
             private final String prefix = "pbc";
 
             @SuppressWarnings("unchecked")
             @Override
             public Iterator getPrefixes(String namespaceURI) {
                 List<String> prefixes = new ArrayList<String>();
                 prefixes.add(prefix);
                 return prefixes.iterator();
             }
 
             @Override
             public String getPrefix(String namespaceURI) {
                 if (this.namespaceURI.equals(namespaceURI)) {
                     return prefix;
                 } else {
                     return null;
                 }
             }
 
             @Override
             public String getNamespaceURI(String prefix) {
                 if (this.prefix.equals(prefix)) {
                     return namespaceURI;
                 } else {
                     return null;
                 }
             }
         };
 
         xPath.setNamespaceContext(pbcoreNamespace);
         final Node radioTVPBCoreElement = (Node) xPath.evaluate(
                 RECORDING_PBCORE_DESCRIPTION_DOCUMENT_ELEMENT, radioTVMetadata,
                 XPathConstants.NODE);
 
         // Create a program object in the DOMS and update the PBCore metadata
         // datastream with the PBCore metadata from the pre-ingest file.
         final String programObjectPID = domsClient
                 .createObjectFromTemplate(PROGRAM_TEMPLATE_PID);
 
 
         final Document pbCoreDataStreamDocument = unSchemaedBuilder.newDocument();
 
         // Import the PBCore metadata from the pre-ingest document and use it as
         // the contents for the PBCore metadata data stream of the program
         // object.
         final Node newPBCoreElement = pbCoreDataStreamDocument.importNode(
                 radioTVPBCoreElement, true);
 
         pbCoreDataStreamDocument.appendChild(newPBCoreElement);
 
         domsClient.updateDataStream(programObjectPID, PROGRAM_PBCORE_DS_ID,
                 pbCoreDataStreamDocument);
 
         // Get the program title from the PBCore metadata and use that as the
         // object label for this program object.
         final Node titleNode = (Node) xPath.evaluate(
                 "pbc:pbcoreTitle[pbc:titleType=\"titel\"]/pbc:title",
                 radioTVPBCoreElement, XPathConstants.NODE);
         final String programTitle = titleNode.getTextContent();
         domsClient.setObjectLabel(programObjectPID, programTitle);
 
         // Get the Ritzau metadata from the pre-ingest document and add it to
         // the Ritzau metadata data stream of the program object.
         final Node ritzauPreingestElement = (Node) xPath.evaluate(
                 RITZAU_ORIGINALS_ELEMENT, radioTVMetadata, XPathConstants.NODE);
 
         // Build a Ritzau data document for the Ritzau data stream in the
         // program object.
         final Document ritzauOriginalDocument = unSchemaedBuilder.newDocument();
         final Element ritzauOriginalRootElement = ritzauOriginalDocument
                 .createElement("ritzau_original");
 
         ritzauOriginalRootElement.setAttribute("xmlns",
                 "http://doms.statsbiblioteket.dk/types/ritzau_original/0/1/#");
         ritzauOriginalDocument.appendChild(ritzauOriginalRootElement);
 
         ritzauOriginalRootElement.setTextContent(ritzauPreingestElement
                 .getTextContent());
 
         domsClient.updateDataStream(programObjectPID, RITZAU_ORIGINAL_DS_ID,
                 ritzauOriginalDocument);
 
         // Add the Gallup metadata
         final Node gallupPreingestElement = (Node) xPath.evaluate(
                 GALLUP_ORIGINALS_ELEMENT, radioTVMetadata, XPathConstants.NODE);
 
         final Document gallupOriginalDocument = unSchemaedBuilder.newDocument();
 
         final Element gallupOriginalRootElement = gallupOriginalDocument
                 .createElement("gallup_original");
 
         gallupOriginalRootElement.setAttribute("xmlns",
                 "http://doms.statsbiblioteket.dk/types/gallup_original/0/1/#");
 
         gallupOriginalRootElement.setTextContent(gallupPreingestElement
                 .getTextContent());
 
         gallupOriginalDocument.appendChild(gallupOriginalRootElement);
 
         domsClient.updateDataStream(programObjectPID, GALLUP_ORIGINAL_DS_ID,
                 gallupOriginalDocument);
 
         // Create relations to the metafile/shard
         domsClient.addObjectRelation(programObjectPID,
                 HAS_METAFILE_RELATION_TYPE, metafilePID);
 
         return programObjectPID;
     }
 
     /**
      * Ingest a metafile (aka. shard) object which represents the program data
      * (i.e. video and/or audio). A metafile may consist of data chunks from
      * multiple physical files identified by the PIDs in the
      * <code>filePIDs</code> list. The metadata provided by
      * <code>radioTVMetadata</code> contains, among other things, information
      * about location and duration of the chunks of data from the physical files
      * which constitutes the contents of the metafile.
      * <p/>
      * TODO: Consider cleaning up/consolidating the exceptions
      *
      * @param radioTVMetadata
      *            Metadata about location and duration of the relevant data
      *            chunks from the physical files.
      * @param filePIDs
      *            List of PIDs for the physical files containing the data
      *            represented by this metafile.
      * @return PID of the newly created metafile object, created by the DOMS.
      * @throws ServerOperationFailed
      *             if creation or manipulation of the metafile object fails.
      * @throws IOException
      * @throws ParserConfigurationException
      *             if construction of the DocumentBuilder, for creation of data
      *             stream documents, fails.
      * @throws XPathExpressionException
      *             if any errors were encountered while processing the
      *             <code>radioTVMetadata</code> XML document.
      * @throws URISyntaxException
      */
     private String ingestMetaFile(
             Document radioTVMetadata, List<String> filePIDs, DOMSWSClient domsClient)
             throws ServerOperationFailed, IOException, XPathExpressionException,
             URISyntaxException {
 
         // Create a file object from the file object template.
         final String metaFilePID = domsClient
                 .createObjectFromTemplate(META_FILE_TEMPLATE_PID);
 
         // TODO: Do something about this.
         final FileInfo fileInfo = new FileInfo("shard/" + metaFilePID, new URL(
                 "http://www.statsbiblioteket.dk/doms/shard/" + metaFilePID),
                 "", new URI("info:pronom/fmt/199"));
 
         domsClient.addFileToFileObject(metaFilePID, fileInfo);
 
 
         final Document metadataDataStreamDocument = unSchemaedBuilder
                 .newDocument();
 
         final Element metadataDataStreamRootElement = metadataDataStreamDocument
                 .createElement("shard_metadata");
 
         metadataDataStreamDocument.appendChild(metadataDataStreamRootElement);
 
         // Add all the "file" elements from the radio-tv metadata document.
         // TODO: Note that this is just a first-shot implementation until a
         // proper metadata format has been defined.
         final XPath xPath = this.xPathFactory.newXPath();
         final NodeList recordingFileElements = (NodeList) xPath.evaluate(
                 RECORDING_FILES_FILE_ELEMENT, radioTVMetadata,
                 XPathConstants.NODESET);
 
         for (int fileElementIdx = 0; fileElementIdx < recordingFileElements
                 .getLength(); fileElementIdx++) {
 
             final Node currentRecordingFile = recordingFileElements
                     .item(fileElementIdx);
 
             // Append to the end of the list of child nodes.
             final Node newMetadataElement = metadataDataStreamDocument
                     .importNode(currentRecordingFile, true);
 
             metadataDataStreamRootElement.appendChild(newMetadataElement);
         }
 
         domsClient.updateDataStream(metaFilePID, META_FILE_METADATA_DS_ID,
                 metadataDataStreamDocument);
 
         // Create relations to the relevant file(s)
         for (String filePID : filePIDs) {
             domsClient.addObjectRelation(metaFilePID,
                     CONSISTS_OF_RELATION_TYPE, filePID);
         }
 
         return metaFilePID;
     }
 
     /**
      * Ingest any missing file objects into the DOMS and return a list of PIDs
      * for all the DOMS file objects corresponding to the files listed in the
      * <code>radioTVMetadata</code> document.
      *
      * @param radioTVMetadata
      *            Metadata XML document containing the file information.
      * @param domsClient
      * @return A <code>List</code> of PIDs of the radio-tv file objects created
      *         by the DOMS.
      * @throws XPathExpressionException
      *             if any errors were encountered while processing the
      *             <code>radioTVMetadata</code> XML document.
      * @throws MalformedURLException
      *             if a file element contains an invalid URL.
      * @throws ServerOperationFailed
      *             if creation and retrieval of a radio-tv file object fails.
      * @throws URISyntaxException
      *             if the format URI for the file is invalid.
      */
     private List<String> ingestFiles(
             Document radioTVMetadata, DOMSWSClient domsClient) throws XPathExpressionException,
             MalformedURLException, ServerOperationFailed, URISyntaxException {
 
         // Get the recording files XML element and process the file information.
         final XPath xPath = this.xPathFactory.newXPath();
         final NodeList recordingFiles = (NodeList) xPath.evaluate(
                 RECORDING_FILES_FILE_ELEMENT, radioTVMetadata,
                 XPathConstants.NODESET);
 
         // Ensure that the DOMS contains a file object for each recording file
         // element in the radio-tv XML document.
         final List<String> fileObjectPIDs = new ArrayList<String>();
         for (int nodeIndex = 0; nodeIndex < recordingFiles.getLength(); nodeIndex++) {
 
             final Node currentFileNode = recordingFiles.item(nodeIndex);
 
             final Node fileURLNode = (Node) xPath.evaluate(FILE_URL_ELEMENT,
                     currentFileNode, XPathConstants.NODE);
             final String fileURLString = fileURLNode.getTextContent();
             final URL fileURL = new URL(fileURLString);
 
             String fileObjectPID;
             try {
                 fileObjectPID = domsClient.getFileObjectPID(fileURL);
             } catch (NoObjectFound nof) {
                 // The DOMS contains no file object for this file URL.
                 // Create a new one now.
                 final Node fileNameNode = (Node) xPath
                         .evaluate(FILE_NAME_ELEMENT, currentFileNode,
                                 XPathConstants.NODE);
 
                 final String fileName = fileNameNode.getTextContent();
 
                 final Node formatURINode = (Node) xPath.evaluate(
                         FORMAT_URI_ELEMENT, currentFileNode,
                         XPathConstants.NODE);
 
                 final URI formatURI = new URI(formatURINode.getTextContent());
 
                 final Node md5SumNode = (Node) xPath.evaluate(MD5_SUM_ELEMENT,
                         currentFileNode, XPathConstants.NODE);
 
                 // The MD5 check sum is optional. Just leave it empty if the
                 // pre-ingest file does not provide it.
                 String md5String = "";
                 if (md5SumNode != null) {
                     md5String = md5SumNode.getTextContent();
                 }
 
                 final FileInfo fileInfo = new FileInfo(fileName, fileURL,
                         md5String, formatURI);
 
                 fileObjectPID = domsClient.createFileObject(
                         RADIO_TV_FILE_TEMPLATE_PID, fileInfo);
             }
             fileObjectPIDs.add(fileObjectPID);
         }
         return fileObjectPIDs;
     }
 
     /**
      * Move <code>fileToMove</code> to the folder specified by
      * <code>destinationFolder</code>.
      *
      * @param fileToMove
      *            Path to the file to move to <code>destinationFolder</code>.
      * @param destinationFolder
      *            Path of the destination folder to move the file to.
      */
     private void moveFile(File fileToMove, File destinationFolder) {
         fileToMove.renameTo(new File(destinationFolder.getAbsolutePath()
                 + File.separator + fileToMove.getName()));
 
     }
 
     private void writeFailedPIDs(File failedMetadataFile,
                                  List<String> failedPIDs, File outputFolder)
             throws FileNotFoundException {



         final File failedPIDsFile = new File(outputFolder, failedMetadataFile
                 .getName() + ".failedPIDs");
        final PrintStream printStream = new PrintStream(failedPIDsFile);
 
        final Iterator<String> pidIterator = failedPIDs.iterator();
        while (pidIterator.hasNext()) {
            printStream.print(pidIterator.next());
            if (pidIterator.hasNext()) {
                printStream.print(", ");
            }
        }
        printStream.println();
        printStream.close();
     }
 
     /**
      * ends all attempts to ingest from the current list of file descriptions in
      * the pre-ingest file Voilent exit needed "system.exit()
      */
     private void fatalException() {
         System.exit(-1);
         // To change body of created methods use File | Settings | File
         // Templates.
     }
 
     /**
      * The number of tires is counted up.
      *
      * @param incrementBy
      *            a number of tries
      */
     private void increaseFailedTries(int incrementBy) {
         exceptionCount += incrementBy;
         if (exceptionCount >= MAX_FAIL_COUNT) {
             fatalException();
         }
     }
 
     /**
      * The number of tries is incremented by one
      */
     private void incrementFailedTries() {
         increaseFailedTries(1);
     }
 
 }
