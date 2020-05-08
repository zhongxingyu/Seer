 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.dataout;
 
 import org.patientview.model.Patient;
 import org.patientview.patientview.model.Comment;
 import org.patientview.patientview.model.TestResult;
 import org.patientview.model.Unit;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.utils.LegacySpringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.servlet.ServletContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 public class DataOutThread implements Runnable {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DataOutThread.class);
 
     private String prebit;
     private String directory;
     private String archiveDirectory;
     private int minutesBetweenWait;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private ServletContext servletContext;
 
     private static final int SECONDS_IN_MINUTE = 60;
     private static final int MILLISECONDS = 1000;
 
     public DataOutThread() {
     }
 
     public DataOutThread(ServletContext context, String dataoutDir,
                          String dataOutArchiveDirectory, int minutesBetweenWait) {
         this.directory = dataoutDir;
         this.archiveDirectory = dataOutArchiveDirectory;
         this.minutesBetweenWait = minutesBetweenWait;
         this.servletContext = context;
     }
 
     public void run() {
         try {
             while (true) {
                 try {
                     List<Unit> units = returnAllTheUnits();
 
                     for (Unit unit : units) {
                         List<Patient> patients = patientsInUnit(unit.getUnitcode());
 
                         for (Patient patient : patients) {
                             Document patientDataOutXmlDoc = makePatientDataOutXml(patient);
 
                             if (!(null == patientDataOutXmlDoc)) {
                                 writeDataOutFile(patientDataOutXmlDoc, patient, unit);
                             }
                         }
                     }
                 } catch (Exception e) {
                     LOGGER.error(e.getMessage());
                     LOGGER.debug(e.getMessage(), e);
                 }
                 Thread.sleep(MILLISECONDS * SECONDS_IN_MINUTE * minutesBetweenWait);
                 Date now = new Date(System.currentTimeMillis());
                 System.out.println("DataOutThread " + dateFormat.format(now));
             }
         } catch (InterruptedException e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
     }
 
     private Document makePatientDataOutXml(Patient patient) {
         Document doc = null;
 
         List<TestResult> testResults = fetchTestResultsForPatient(patient);
         List<Comment> comments = fetchCommentsForPatient(patient);
 
         if (testResults.isEmpty() && comments.isEmpty()) {
             return doc;
         }
 
         try {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             doc = docBuilder.newDocument();
             Element rootElement = doc.createElement("patientview");
             doc.appendChild(rootElement);
 
             Element dateOfReport = addChildElement(doc, rootElement, "dateofreport", getTimeStampNow());
 
             Element centreDetails = addChildElement(doc, rootElement, "centredetails");
             Element centreCode = addChildElement(doc, centreDetails, "centrecode", patient.getUnitcode());
 
             Element patientTag = addChildElement(doc, rootElement, "patient");
             Element personalDetails = addChildElement(doc, patientTag, "personaldetails");
             Element nhsno = addChildElement(doc, personalDetails, "nhsno", patient.getNhsno());
             Element surname = addChildElement(doc, personalDetails, "surname", patient.getSurname());
             Element forename = addChildElement(doc, personalDetails, "forename", patient.getForename());
            Element dateofbirth = addChildElement(doc, personalDetails, "dateofbirth", patient.getDateofbirth());
 
             Element testDetails = addChildElement(doc, rootElement, "testDetails");
 
             String testCodeTrack = "";
             Element currentTest = null;
             Element currentDateRange = null;
             String currentDateRangeStop = null;
 
             for (TestResult testResult : testResults) {
 
                 if (!testCodeTrack.equals(testResult.getTestcode())) {
                     currentTest = addChildElement(doc, testDetails, "test");
                     if (!(null == currentDateRange)) {
                         currentDateRange.setAttribute("stop", currentDateRangeStop);
                     }
                     Element dateRange = addChildElement(doc, currentTest, "daterange");
                     currentDateRange = dateRange;
                     dateRange.setAttribute("start", testResult.getIsoDayDatestamp());
                     testCodeTrack = testResult.getTestcode();
                     Element testCode = addChildElement(doc, currentTest, "testcode", testResult.getTestcode());
                 }
 
                 Element result = addChildElement(doc, currentTest, "result");
                 Element dateStamp = addChildElement(doc, result, "datestamp", testResult.getIsoDatestamp());
                 Element source = addChildElement(doc, result, "source", testResult.getUnitcode());
                 currentDateRangeStop = testResult.getIsoDayDatestamp();
                 Element prePost = addChildElement(doc, result, "prepost", testResult.getPrepost());
                 Element value = addChildElement(doc, result, "value", testResult.getValue());
             }
 
             if (!(null == currentDateRange) && !(null == currentDateRangeStop)) {
                 currentDateRange.setAttribute("stop", currentDateRangeStop);
             }
 
             Element commentsTag = addChildElement(doc, rootElement, "comments");
 
             for (Comment comment : comments) {
 
                 Element commentTag = addChildElement(doc, commentsTag, "comment");
                 Element commentDate = addChildElement(doc, commentsTag, "commentdate",
                         comment.getIsoFormattedDatestamp());
                 Element commentContent = addChildElement(doc, commentsTag, "commentbody", comment.getBody());
             }
         } catch (ParserConfigurationException e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
         return doc;
     }
 
     private List<Comment> fetchCommentsForPatient(Patient patient) {
         List<Comment> comments = null;
 
         try {
             comments = LegacySpringUtils.getCommentManager().get(patient.getNhsno());
         } catch (Exception e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
 
         return comments;
     }
 
     private String getTimeStampNow() {
         Calendar nowCal = GregorianCalendar.getInstance();
         SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         return dateTimeFormat.format(nowCal.getTime());
     }
 
     private List<TestResult> fetchTestResultsForPatient(Patient patient) {
         return LegacySpringUtils.getTestResultManager().get(patient.getNhsno(), UnitUtils.PATIENT_ENTERS_UNITCODE);
     }
 
     private Element addChildElement(Document doc, Element parentElement, String elementName) {
         Element childElement = doc.createElement(elementName);
         parentElement.appendChild(childElement);
         return childElement;
     }
 
     private Element addChildElement(Document doc, Element parentElement, String elementName, String data) {
         Element childElement = addChildElement(doc, parentElement, elementName);
         childElement.appendChild(doc.createTextNode(data));
         return childElement;
     }
 
     private List<Patient> patientsInUnit(String unitCode) {
         List<Patient> patients = null;
 
         try {
             patients = LegacySpringUtils.getPatientManager().get(unitCode);
         } catch (Exception e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
 
         return patients;
     }
 
     private List<Unit> returnAllTheUnits() {
 
         List<Unit> units = null;
 
         try {
             units = LegacySpringUtils.getUnitManager().getUnitsWithUser();
         } catch (Exception e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
 
         return units;
     }
 
     private void writeDataOutFile(Document doc, Patient patient, Unit unit)
             throws ParserConfigurationException, TransformerException {
         String directory1stPart = directory;
         String directory2ndPart = unit.getUnituser();
         String directory3rdPart = LegacySpringUtils.getContextProperties().getProperty(prebit + ".directory.thirdpart");
 
         String filePath = directory1stPart + "/" + directory2ndPart + "/" + directory3rdPart;
 
         File uktDir = new File(filePath);
         File uktExportFile = new File(uktDir, "data_out_" + unit.getUnitcode() + "_" + patient.getNhsno() + ".xml");
 
         //write the content into xml file
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
         DOMSource source = new DOMSource(doc);
         StreamResult result = new StreamResult(uktExportFile);
         transformer.transform(source, result);
     }
 
     public String getPrebit() {
         return prebit;
     }
 
     public void setPrebit(String prebit) {
         this.prebit = prebit;
     }
 
     public String getDirectory() {
         return directory;
     }
 
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     public String getArchiveDirectory() {
         return archiveDirectory;
     }
 
     public void setArchiveDirectory(String archiveDirectory) {
         this.archiveDirectory = archiveDirectory;
     }
 
     public int getMinutesBetweenWait() {
         return minutesBetweenWait;
     }
 
     public void setMinutesBetweenWait(int minutesBetweenWait) {
         this.minutesBetweenWait = minutesBetweenWait;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public void setServletContext(ServletContext servletContext) {
         this.servletContext = servletContext;
     }
 }
