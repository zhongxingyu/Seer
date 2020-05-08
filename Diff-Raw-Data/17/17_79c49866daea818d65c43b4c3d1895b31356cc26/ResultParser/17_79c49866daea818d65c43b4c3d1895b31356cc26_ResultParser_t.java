 package com.worthsoln.patientview.parser;
 
 import com.worthsoln.ibd.Ibd;
 import com.worthsoln.ibd.model.Allergy;
 import com.worthsoln.ibd.model.MyIbd;
 import com.worthsoln.ibd.model.Procedure;
 import com.worthsoln.ibd.model.enums.*;
 import com.worthsoln.patientview.TestResultDateRange;
 import com.worthsoln.patientview.model.*;
 import com.worthsoln.patientview.model.Diagnosis;
 import com.worthsoln.patientview.model.enums.DiagnosticType;
 import com.worthsoln.patientview.utils.TimestampUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.*;
 
 import javax.servlet.ServletContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ResultParser {
 
    public static final SimpleDateFormat IMPORT_DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM");
 
     private ArrayList<TestResult> testResults = new ArrayList<TestResult>();
     private ArrayList<TestResultDateRange> dateRanges = new ArrayList<TestResultDateRange>();
     private ArrayList<Letter> letters = new ArrayList<Letter>();
     private ArrayList<Diagnosis> otherDiagnoses = new ArrayList<Diagnosis>();
     private ArrayList<Medicine> medicines = new ArrayList<Medicine>();
     private ArrayList<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
     private ArrayList<Procedure> procedures = new ArrayList<Procedure>();
     private ArrayList<Allergy> allergies = new ArrayList<Allergy>();
     private Map xmlData = new HashMap();
     private String[] topLevelElements = new String[]{"flag", "centrecode", "centrename", "centreaddress1",
             "centreaddress2", "centreaddress3", "centreaddress4", "centrepostcode", "centretelephone", "centreemail",
             "gpname", "gpaddress1", "gpaddress2", "gpaddress3", "gppostcode", "gptelephone", "gpemail", "diagnosisedta",
             "diagnosisdate", "rrtstatus", "tpstatus", "surname", "forename", "dateofbirth", "sex", "nhsno",
             "hospitalnumber", "address1", "address2", "address3", "address4", "postcode", "telephone1", "telephone2",
             "mobile", "diagnosisyear", "ibddiseaseextent", "ibddiseasecomplications", "ibdeimanifestations",
             "bodypartsaffected", "familyhistory", "smokinghistory", "surgicalhistory", "vaccinationrecord", "bmdexam",
             "namedconsultant", "ibdnurse", "bloodgroup", "diagnosis", "colonoscopysurveillance"};
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ResultParser.class);
 
     public void parseResults(ServletContext context, File resultsFile) throws Exception {
         Document doc = getDocument(context, resultsFile);
         for (int i = 0; i < topLevelElements.length; i++) {
             collectTopLevelData(topLevelElements[i], doc);
         }
         collectTestResults(doc);
         collectDateRanges(doc);
         collectLetters(doc);
         collectOtherDiagnosis(doc);
         collectMedicines(doc);
         collectDiagnostics(doc);
         collectProcedures(doc);
         collectAllergies(doc);
     }
 
     private void collectDateRanges(Document doc) {
         NodeList testNodeList = doc.getElementsByTagName("test");
         for (int i = 0; i < testNodeList.getLength(); i++) {
             Node testNode = testNodeList.item(i);
             NodeList testResultNodes = testNode.getChildNodes();
             String testCode = "";
             String startDate = "";
             String stopDate = "";
             for (int j = 0; j < testResultNodes.getLength(); j++) {
                 Node testResultNode = testResultNodes.item(j);
                 if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("daterange"))) {
                     NamedNodeMap attributes = testResultNode.getAttributes();
                     startDate = attributes.getNamedItem("start").getNodeValue();
                     stopDate = attributes.getNamedItem("stop").getNodeValue();
                 } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("testcode"))) {
                     testCode = testResultNode.getFirstChild().getNodeValue();
                 }
             }
             TestResultDateRange dateRange =
                     new TestResultDateRange(getData("nhsno"), getData("centrecode"), testCode, startDate, stopDate);
             dateRanges.add(dateRange);
         }
     }
 
     private void collectTestResults(Document doc) {
         NodeList testNodeList = doc.getElementsByTagName("test");
         for (int i = 0; i < testNodeList.getLength(); i++) {
             Node testNode = testNodeList.item(i);
             NodeList testResultNodes = testNode.getChildNodes();
             String testCode = "";
             String testName = "";
             String testUnits = "";
             for (int j = 0; j < testResultNodes.getLength(); j++) {
                 Node testResultNode = testResultNodes.item(j);
                 if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("testname"))) {
                     testName = testResultNode.getFirstChild().getNodeValue(); // this is not stored for now
                 } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("testcode"))) {
                     testCode = testResultNode.getFirstChild().getNodeValue();
                 } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("units")) && testResultNode.getFirstChild() != null) {
                     testUnits = testResultNode.getFirstChild().getNodeValue(); // this is not stored for now
                 } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (testResultNode.getNodeName().equals("result"))) {
                     TestResult testResult = new TestResult(getData("nhsno"), getData("centrecode"), null, testCode, "");
                     NodeList resultDataNodes = testResultNode.getChildNodes();
                     for (int k = 0; k < resultDataNodes.getLength(); k++) {
                         Node resultDataNode = resultDataNodes.item(k);
                         if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE) &&
                                 (resultDataNode.getNodeName().equals("datestamp"))) {
                             parseDatestamp(testResult, resultDataNode);
                         } else if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE) &&
                                 (resultDataNode.getNodeName().equals("prepost"))) {
                             testResult.setPrepost(resultDataNode.getFirstChild().getNodeValue());
                         } else if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE) &&
                                 (resultDataNode.getNodeName().equals("value"))) {
                             testResult.setValue(resultDataNode.getFirstChild().getNodeValue().trim());
                         }
                     }
                     testResults.add(testResult);
                 }
             }
         }
     }
 
     private void collectLetters(Document doc) {
         NodeList letterNodes = doc.getElementsByTagName("letter");
         for (int i = 0; i < letterNodes.getLength(); i++) {
             Node letterNode = letterNodes.item(i);
             Letter letter = new Letter();
             letter.setNhsno(getData("nhsno"));
             letter.setUnitcode(getData("centrecode"));
             NodeList letterDetailNodes = letterNode.getChildNodes();
             for (int j = 0; j < letterDetailNodes.getLength(); j++) {
                 Node letterDetailNode = letterDetailNodes.item(j);
                 if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (letterDetailNode.getNodeName().equals("letterdate"))) {
                     letter.setStringDate(letterDetailNode.getFirstChild().getNodeValue());
                 } else if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (letterDetailNode.getNodeName().equals("lettertype"))) {
                     letter.setType(letterDetailNode.getFirstChild().getNodeValue());
                 } else if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                         (letterDetailNode.getNodeName().equals("lettercontent"))) {
                     NodeList nodes = letterDetailNode.getChildNodes();
                     for (int k = 0; k < nodes.getLength(); k++) {
                         Node node = nodes.item(k);
                         if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                             CDATASection cdata = (CDATASection) node;
                             letter.setContent(cdata.getData());
                         }
                     }
                 }
             }
             letters.add(letter);
         }
     }
 
     private void collectOtherDiagnosis(Document doc) {
         NodeList diagnosisNodes = doc.getElementsByTagName("diagnosis");
         for (int i = 0; i < diagnosisNodes.getLength(); i++) {
             Node diagnosisNode = diagnosisNodes.item(i);
             Node diagnosisNodeChild = diagnosisNode.getFirstChild();
             if (diagnosisNodeChild != null) {
                 Diagnosis diagnosis = new Diagnosis();
                 diagnosis.setNhsno(getData("nhsno"));
                 diagnosis.setUnitcode(getData("centrecode"));
                 diagnosis.setDiagnosis(diagnosisNode.getFirstChild().getNodeValue());
                 diagnosis.setDisplayorder(i + "");
                 otherDiagnoses.add(diagnosis);
             }
         }
     }
 
     private void collectProcedures(Document doc) {
         NodeList procedureNodes = doc.getElementsByTagName("procedure");
         for (int i = 0; i < procedureNodes.getLength(); i++) {
             Node procedureNode = procedureNodes.item(i);
             Procedure procedure = new Procedure();
             procedure.setNhsno(getData("nhsno"));
             procedure.setUnitcode(getData("centrecode"));
             NodeList procedureDetailNodes = procedureNode.getChildNodes();
             for (int j = 0; j < procedureDetailNodes.getLength(); j++) {
                 try {
                     Node procedureDetailNode = procedureDetailNodes.item(j);
                     if ((procedureDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (procedureDetailNode.getNodeName().equals("procedurename"))) {
                         procedure.setProcedure(procedureNode.getFirstChild().getNodeValue());
                     } else if ((procedureDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (procedureDetailNode.getNodeName().equals("proceduredate"))) {
                         procedure.setDate(procedureDetailNode.getFirstChild().getNodeValue());
                     }
                 } catch (NullPointerException e) {
                     e.printStackTrace();
                 }
             }
             procedures.add(procedure);
         }
     }
 
     private void collectDiagnostics(Document doc) {
         NodeList diagnosticsNodes = doc.getElementsByTagName("diagnostic");
         for (int i = 0; i < diagnosticsNodes.getLength(); i++) {
             Node diagnosticNode = diagnosticsNodes.item(i);
 
             Diagnostic diagnostic = new Diagnostic();
             diagnostic.setNhsno(getData("nhsno"));
             diagnostic.setUnitcode(getData("centrecode"));
 
             NodeList diagnosticsDetailNodes = diagnosticNode.getChildNodes();
             for (int j = 0; j < diagnosticsDetailNodes.getLength(); j++) {
                 try {
                     Node diagnosticDetailNode = diagnosticsDetailNodes.item(j);
                     if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (diagnosticDetailNode.getNodeName().equals("diagnosticname"))) {
                         diagnostic.setDescription(diagnosticDetailNode.getFirstChild().getNodeValue());
                     } else if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (diagnosticDetailNode.getNodeName().equals("diagnosticdate"))) {
                         diagnostic.setDatestamp(TimestampUtils.createTimestamp(
                                 diagnosticDetailNode.getFirstChild().getNodeValue()));
                     } else if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (diagnosticDetailNode.getNodeName().equals("diagnostictype"))) {
                         diagnostic.setDiagnosticType(DiagnosticType.getDiagnosticType(
                                 diagnosticDetailNode.getFirstChild().getNodeValue()));
                     }
                 } catch (NullPointerException e) {
                     e.printStackTrace();
                 }
             }
 
             if (diagnostic.getDiagnosticType() != null) {
                 diagnostics.add(diagnostic);
             }
         }
     }
 
     private void collectAllergies(Document doc) {
         NodeList allergyNodes = doc.getElementsByTagName("allergy");
         for (int i = 0; i < allergyNodes.getLength(); i++) {
             Node allergyNode = allergyNodes.item(i);
             Allergy allergy = new Allergy();
             allergy.setNhsno(getData("nhsno"));
             allergy.setUnitcode(getData("centrecode"));
             NodeList allergyDetailNodes = allergyNode.getChildNodes();
             for (int j = 0; j < allergyDetailNodes.getLength(); j++) {
                 try {
                     Node allergyDetailNode = allergyDetailNodes.item(j);
                     if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergysubstance"))) {
                         allergy.setSubstance(allergyNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergytypecode"))) {
                         allergy.setTypeCode(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergyreaction"))) {
                         allergy.setReaction(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergyconfidencelevel"))) {
                         allergy.setConfidenceLevel(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergyinfosource"))) {
                         allergy.setInfoSource(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergystatus"))) {
                         allergy.setStatus(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergydescription"))) {
                         allergy.setDescription(allergyDetailNode.getFirstChild().getNodeValue());
                     } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (allergyDetailNode.getNodeName().equals("allergyrecordeddate"))) {
                         allergy.setRecordedDate(allergyDetailNode.getFirstChild().getNodeValue());
                     }
                 } catch (NullPointerException e) {
                     e.printStackTrace();
                 }
             }
             allergies.add(allergy);
         }
     }
 
     private void collectMedicines(Document doc) {
         NodeList medicineNodes = doc.getElementsByTagName("drug");
         for (int i = 0; i < medicineNodes.getLength(); i++) {
             Node medicineNode = medicineNodes.item(i);
             Medicine medicine = new Medicine();
             medicine.setNhsno(getData("nhsno"));
             medicine.setUnitcode(getData("centrecode"));
             NodeList medicineDetailNodes = medicineNode.getChildNodes();
             for (int j = 0; j < medicineDetailNodes.getLength(); j++) {
                 try {
                     Node medicineDetailNode = medicineDetailNodes.item(j);
                     if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (medicineDetailNode.getNodeName().equals("drugstartdate"))) {
                         medicine.setStartdate(medicineDetailNode.getFirstChild().getNodeValue());
                     } else if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (medicineDetailNode.getNodeName().equals("drugname"))) {
                         medicine.setName(medicineDetailNode.getFirstChild().getNodeValue());
                     } else if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE) &&
                             (medicineDetailNode.getNodeName().equals("drugdose"))) {
                         medicine.setDose(medicineDetailNode.getFirstChild().getNodeValue());
                     }
                 } catch (NullPointerException e) {
                     e.printStackTrace();
                 }
             }
             medicines.add(medicine);
         }
     }
 
     private void parseDatestamp(TestResult testResult, Node resultDataNode) {
         String dateStampString = resultDataNode.getFirstChild().getNodeValue();
         testResult.setDatestampString(dateStampString);
     }
 
     private void collectTopLevelData(String dataId, Document doc) {
         NodeList nhsNoNodeList = doc.getElementsByTagName(dataId);
         if (nhsNoNodeList.getLength() != 0) {
             Node nhsNoNode = nhsNoNodeList.item(0);
             if (nhsNoNode.getFirstChild() == null) {
                 topLevelDataSpecialCases(xmlData, dataId);
             } else {
                 xmlData.put(dataId, nhsNoNode.getFirstChild().getNodeValue());
             }
         } else {
             topLevelDataSpecialCases(xmlData, dataId);
         }
     }
 
     private void topLevelDataSpecialCases(Map xmlData, String dataId) {
         if (dataId.equals("rrtstatus")) {
             xmlData.put(dataId, "GEN");
         } else if (dataId.equals("diagnosisedta")) {
             xmlData.put(dataId, "DEF");
         }
     }
 
     private Document getDocument(ServletContext context, File file) {
         Document doc = null;
         try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             doc = db.parse(file);
         } catch (Exception e) {
             e.printStackTrace();
             //EmailUtils.sendEmail(context, e.toString());
         }
         return doc;
     }
 
     public String getData(String dataId) {
         return (String) xmlData.get(dataId);
     }
 
     public Patient getPatient() {
         Patient patient = new Patient((String) xmlData.get("nhsno"), (String) xmlData.get("surname"),
                 (String) xmlData.get("forename"), (String) xmlData.get("dateofbirth"), (String) xmlData.get("sex"),
                 (String) xmlData.get("address1"), (String) xmlData.get("address2"), (String) xmlData.get("address3"),
                 (String) xmlData.get("address4"), (String) xmlData.get("postcode"), (String) xmlData.get("telephone1"),
                 (String) xmlData.get("telephone2"), (String) xmlData.get("mobile"), (String) xmlData.get("centrecode"),
                 (String) xmlData.get("diagnosisedta"), (String) xmlData.get("diagnosisdate"),
                 (String) xmlData.get("rrtstatus"), (String) xmlData.get("tpstatus"),
                 (String) xmlData.get("hospitalnumber"), (String) xmlData.get("gpname"),
                 (String) xmlData.get("gpaddress1"), (String) xmlData.get("gpaddress2"),
                 (String) xmlData.get("gpaddress3"), (String) xmlData.get("gppostcode"),
                 (String) xmlData.get("gptelephone"), (String) xmlData.get("gpemail"),
                 (String) xmlData.get("bmdexam"), (String) xmlData.get("bloodgroup"));
 
         return patient;
     }
 
     public MyIbd getMyIbd() {
         MyIbd myIbd = new MyIbd();
         myIbd.setNhsno((String) xmlData.get("nhsno"));
         myIbd.setUnitcode((String) xmlData.get("centrecode"));
         myIbd.setDiagnosis(com.worthsoln.ibd.model.enums.Diagnosis.getDiagnosisByXmlName(
                 (String) xmlData.get("diagnosis")));
 
         Object diagnosisDate = xmlData.get("diagnosisdate");
         // this is an ibd only field so can be null
         if (diagnosisDate != null) {
             try {
                 myIbd.setYearOfDiagnosis(
                        IMPORT_DATE_FORMAT.parse((String) diagnosisDate));
             } catch (Exception e) {
                 LOGGER.error("Could not parse diagnosisyear for MyIbd with NHS No {}", myIbd.getNhsno(), e);
             }
         }
         myIbd.setDiseaseExtent(DiseaseExtent.getDiseaseExtentByXmlName((String) xmlData.get("ibddiseaseextent")));
         myIbd.setEiManifestations((String) xmlData.get("ibdeimanifestations"));
         ArrayList<Complication> complications = new ArrayList<Complication>();
         complications.add(Complication.getComplication((String) xmlData.get("ibddiseasecomplications")));
         myIbd.setComplications(complications);
         myIbd.setBodyPartAffected(BodyPartAffected.getBodyPartAffected((String) xmlData.get("bodypartsaffected")));
         myIbd.setFamilyHistory(FamilyHistory.getFamilyHistory((String) xmlData.get("familyhistory")));
         myIbd.setSmoking(Smoking.getSmoking((String) xmlData.get("smokinghistory")));
         myIbd.setSurgery(Surgery.getSurgery((String) xmlData.get("surgicalhistory")));
         myIbd.setVaccinationRecord(VaccinationRecord.getVaccinationRecord((String) xmlData.get("vaccinationrecord")));
         myIbd.setNamedConsultant((String) xmlData.get("namedconsultant"));
         myIbd.setNurses((String) xmlData.get("ibdnurse"));
         if (xmlData.get("colonoscopysurveillance") != null) {
             try {
                 myIbd.setYearForSurveillanceColonoscopy(
                        IMPORT_DATE_FORMAT.parse((String) xmlData.get("colonoscopysurveillance")));
             } catch (ParseException e) {
                 LOGGER.error("Could not parse colonoscopysurveillance for MyIbd with NHS No {}", myIbd.getNhsno(), e);
             }
         }
 
         return myIbd;
     }
 
     public Centre getCentre() {
         return new Centre((String) xmlData.get("centrecode"), (String) xmlData.get("centrename"),
                 (String) xmlData.get("centreaddress1"), (String) xmlData.get("centreaddress2"),
                 (String) xmlData.get("centreaddress3"), (String) xmlData.get("centreaddress4"),
                 (String) xmlData.get("centrepostcode"), (String) xmlData.get("centretelephone"),
                 (String) xmlData.get("centreemail"));
     }
 
     public String getFlag() {
         return (String) xmlData.get("flag");
     }
 
     public ArrayList<TestResult> getTestResults() {
         return testResults;
     }
 
     public ArrayList<TestResultDateRange> getDateRanges() {
         return dateRanges;
     }
 
     public ArrayList<Letter> getLetters() {
         return letters;
     }
 
     public ArrayList<Diagnosis> getOtherDiagnoses() {
         return otherDiagnoses;
     }
 
     public ArrayList<Medicine> getMedicines() {
         return medicines;
     }
 
     public ArrayList<Diagnostic> getDiagnostics() {
         return diagnostics;
     }
 
     public ArrayList<Procedure> getProcedures() {
         return procedures;
     }
 
     public ArrayList<Allergy> getAllergies() {
         return allergies;
     }
 }
