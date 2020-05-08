 package com.compomics.natter_remake.controllers;
 
 import com.compomics.natter_remake.model.Project;
 import com.compomics.natter_remake.model.RovFile;
 import com.compomics.natter_remake.model.RovFileData;
 import java.io.ByteArrayInputStream;
 import java.util.List;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import org.apache.commons.io.FileUtils;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Davy
  */
 public class DataExtractor {
 
     public static List<RovFile> extractDataInMem(Project project) throws SQLException, ParserConfigurationException, IOException, XMLStreamException {
         List<RovFile> rovFiles = DbDAO.downloadRovFilesInMemoryForProject(project);
         for (RovFile file : rovFiles) {
            parseRovFile(new InputStreamReader(new ByteArrayInputStream(file.getFileContent())));
         }
         return rovFiles;
     }
 
     public static List<RovFile> extractDataLowMem(Project project) throws SQLException, ParserConfigurationException, IOException, XMLStreamException {
         List<Integer> quantitationFileIds = DbDAO.getQuantitationFileIdsForProject(project);
         List<RovFile> rovFiles = new ArrayList<RovFile>(quantitationFileIds.size());
         for (Integer quantitation_fileid : quantitationFileIds) {
             RovFile rovFile = DbDAO.getQuantitationFileForQuantitationFileId(quantitation_fileid);
            parseRovFile(new InputStreamReader(new ByteArrayInputStream(rovFile.getFileContent())));
         }
         return rovFiles;
     }
 
     /**
      * extracts the rov files to the OS temp dir and removes them afterwards
      *
      * @throws SQLException
      * @throws FileNotFoundException
      */
     public static void extractDataToLocal(Project project) throws SQLException, FileNotFoundException, NullPointerException, IOException, ParserConfigurationException, SAXException, XMLStreamException {
         File natterSaveLocation = new File(System.getProperty("java.io.tmpdir") + "/natter_output_files");
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 try {
                     File natterSaveLocation = new File(System.getProperty("java.io.tmpdir") + "/natter_output_files");
                     if (natterSaveLocation.exists()) {
                         FileUtils.deleteDirectory(natterSaveLocation);
                     }
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
         });
        extractDataToLocal(project,natterSaveLocation);
     }
 
    public static void extractDataToLocal(Project project,File rovFileOutputLocationFolder) throws SQLException, FileNotFoundException, NullPointerException, IOException, ParserConfigurationException, SAXException, XMLStreamException {
         List<RovFile> filesToRun = DbDAO.downloadRovFilesLocallyForProject(project, rovFileOutputLocationFolder);
         for (RovFile file : filesToRun) {
             file.addParsedData(parseRovFile(file));
         }
         FileDAO.writeExtractedDataToDisk(filesToRun);
     }
 
     public static void extractDataToLocalLowMem(File rovFileOutputLocationFolder, Project project) throws SQLException, FileNotFoundException, NullPointerException, IOException, ParserConfigurationException, SAXException, XMLStreamException {
         List<RovFile> filesToRun = DbDAO.downloadRovFilesLocallyForProject(project, rovFileOutputLocationFolder);
         for (RovFile file : filesToRun) {
             file.addParsedData(parseRovFile(file));
             FileDAO.writeExtractedDataToDisk(file);
         }
     }
 
     //TODO write per read to disk or per section or per buffer filled?
     private static RovFileData parseRovFile(File rovFile) throws ParserConfigurationException, IOException, XMLStreamException {
         InvalidXMLCharacterFilterReader rovFileStreamReader = new InvalidXMLCharacterFilterReader(new InputStreamReader(new FileInputStream(rovFile.getAbsolutePath()), "UTF-8"));
         RovFileData data = DataExtractor.parseRovFile(rovFileStreamReader);
         rovFileStreamReader.close();
         return data;
     }
 
     private static RovFileData parseRovFile(Reader reader) throws ParserConfigurationException, IOException, XMLStreamException {
         XMLInputFactory xmlParseFactory = XMLInputFactory.newInstance();
         XMLEventReader xmlReader = xmlParseFactory.createXMLEventReader(reader);
         RovFileXMLParser xmlParser = new RovFileXMLParser(xmlReader);
         return xmlParser.getRovFileData();

     }
 }
