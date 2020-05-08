 package org.motechproject.ghana.national.functional.framework;
 
 import com.jcraft.jzlib.ZInputStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.fcitmuk.epihandy.*;
 import org.fcitmuk.epihandy.xform.EpihandyXform;
 import org.motechproject.ghana.national.domain.Constants;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class XformHttpClient {
     public static XformResponse execute(String url, String studyName, String... xmlStrings) throws IOException, ParseException {
         HttpConnection httpConnection = (HttpConnection) Connector.open(url);
         setUpConnection(httpConnection);
         DataOutputStream dataOutputStream = httpConnection.openDataOutputStream();
         serializeUserData(dataOutputStream);
         serializeXforms(studyName, dataOutputStream, xmlStrings);
         return processResponse(httpConnection);
     }
 
     private static void setUpConnection(HttpConnection httpConnection) throws IOException {
         httpConnection.setRequestMethod(HttpConnection.POST);
         httpConnection.setRequestProperty("Content-Type", "application/octet-stream");
         httpConnection.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.0");
         httpConnection.setRequestProperty("Content-Language", "en-US");
     }
 
     private static DataOutputStream serializeUserData(DataOutputStream dataOutputStream) throws IOException {
         dataOutputStream.writeUTF("motech");
         dataOutputStream.writeUTF("ghs");
         dataOutputStream.writeUTF("");
         dataOutputStream.writeUTF("");
         dataOutputStream.writeByte(RequestHeader.ACTION_UPLOAD_DATA); //action
         return dataOutputStream;
     }
 
     private static void serializeXforms(String studyName, DataOutputStream dataOutputStream, String... xmlStrings) throws IOException, ParseException {
         dataOutputStream.writeByte(xmlStrings.length);
         StudyDef studyDef = new StudyDef();
         studyDef.setForms(new Vector());
         studyDef.setName(studyName);
         studyDef.setId(1);
         studyDef.setVariableName(" ");
         Vector<FormData> formsList = new Vector<FormData>();
         for (String string : xmlStrings) {
             final FormDef formDef = EpihandyXform.getFormDef(EpihandyXform.getDocument(new StringReader(string)));
             hackDynamicLists(formDef);
 
             FormData formData = new FormData(formDef);
             studyDef.addForm(formData.getDef());
             hackDate(formData, "/editPatient/date");
             hackDate(formData, "/editPatient/nhisExpires");
             hackDate(formData, "/ANCRegistration/date");
             hackDate(formData, "/ANCRegistration/estDeliveryDate");
             hackDate(formData, "/ANCRegistration/lastTTDate");
             hackDate(formData, "/ANCRegistration/lastIPTDate");
             hackDate(formData, "/patientRegistration/date");
             hackDate(formData, "/patientRegistration/dateOfBirth");
             hackDate(formData, "/patientRegistration/lastIPTDate");
             hackDate(formData, "/patientRegistration/lastTTDate");
             hackDate(formData, "/patientRegistration/bcgDate");
             hackDate(formData, "/patientRegistration/lastOPVDate");
             hackDate(formData, "/patientRegistration/lastPentaDate");
             hackDate(formData, "/patientRegistration/measlesDate");
             hackDate(formData, "/patientRegistration/yellowFeverDate");
             hackDate(formData, "/patientRegistration/lastIPTiDate");
             hackDate(formData, "/patientRegistration/lastVitaminADate");
             hackDate(formData, "/patientRegistration/expDeliveryDate");
             hackDate(formData, "/CWCRegistration/registrationDate");
             hackDate(formData, "/CWCRegistration/bcgDate");
             hackDate(formData, "/CWCRegistration/lastOPVDate");
             hackDate(formData, "/CWCRegistration/lastPentaDate");
             hackDate(formData, "/CWCRegistration/measlesDate");
             hackDate(formData, "/CWCRegistration/yellowFeverDate");
             hackDate(formData, "/CWCRegistration/lastIPTiDate");
             hackDate(formData, "/CWCRegistration/lastVitaminADate");
             hackDate(formData, "/opvVisit/visitDate");
             hackDate(formData, "/opvVisit/dateOfBirth");
             hackDate(formData, "/opvVisit/nhisExpires");
             hackDate(formData, "/ancVisit/date");
             hackDate(formData, "/ancVisit/estDeliveryDate");
             hackDate(formData, "/ancVisit/nextANCDate");
             hackDate(formData, "/abortion/date");
            hackDate(formData, "/delivery/date");
             hackDate(formData, "/clientQuery/dateOfBirth");
             formsList.add(formData);
         }
         final StudyData studyData = new StudyData(studyDef);
         studyData.addForms(formsList);
         studyData.write(dataOutputStream);
     }
 
     private static void assignOptionsFromDynamicDefs(FormDef formDef, List<DynamicOptionDependency> dynamicOptionDependencies) {
         if (dynamicOptionDependencies.size() > 0) {
             DynamicOptionDependency dependency = dynamicOptionDependencies.remove(0);
             boolean independent = true;
             for (DynamicOptionDependency dynamicOptionDependency : dynamicOptionDependencies) {
                 if (dependency.dependsOn == dynamicOptionDependency.getId()) {
                     independent = false;
                     break;
                 }
             }
             if (independent) {
                 DynamicOptionDef dynamicOptions = formDef.getDynamicOptions(dependency.getDependsOn());
                 QuestionDef dependsOnQuestion = formDef.getQuestion(dependency.getDependsOn());
                 Vector dependsOnQuestionOptions = dependsOnQuestion.getOptions();
                 String dependsOnQuestionDefaultValue = dependsOnQuestion.getDefaultValue();
                 byte parentOptionId = -1;
                 for (Object dependsOnQuestionOption : dependsOnQuestionOptions) {
                     OptionDef dependsOnQuestionOption1 = (OptionDef) dependsOnQuestionOption;
                     if (dependsOnQuestionOption1.getVariableName().equals(dependsOnQuestionDefaultValue)) {
                         parentOptionId = dependsOnQuestionOption1.getId();
                         break;
                     }
                 }
                 formDef.getQuestion(dependency.getId()).setOptions((parentOptionId != -1) ? dynamicOptions.getParentToChildOptions().get(parentOptionId) : new Vector());
             } else {
                 dynamicOptionDependencies.add(dependency);
             }
             assignOptionsFromDynamicDefs(formDef, dynamicOptionDependencies);
         }
     }
 
     private static void hackDynamicLists(FormDef formDef) throws ParseException {
         Hashtable dynamicOptions = formDef.getDynamicOptions();
         if (dynamicOptions != null) {
 
             List<DynamicOptionDependency> dependencies = new ArrayList<DynamicOptionDependency>();
 
             for (Object entry : dynamicOptions.entrySet()) {
                 Map.Entry<Byte, DynamicOptionDef> dynamicOptionsDefEntry = (Map.Entry<Byte, DynamicOptionDef>) entry;
                 dependencies.add(new DynamicOptionDependency(dynamicOptionsDefEntry.getValue().getQuestionId(), dynamicOptionsDefEntry.getKey()));
             }
             assignOptionsFromDynamicDefs(formDef, dependencies);
 
         }
     }
 
     private static void hackDate(FormData formData, String variableName) throws ParseException {
         final QuestionData question = formData.getQuestion(variableName);
         if (question == null) return;
         final String answer = (String) question.getAnswer();
         if (answer == null) return;
         formData.setDateValue(variableName, new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).parse(answer));
     }
 
     private static XformResponse processResponse(HttpConnection httpConnection) throws IOException {
         DataInputStream dataInputStream = null;
         try {
             if (httpConnection.getResponseCode() == HttpConnection.HTTP_OK) {
                 dataInputStream = new DataInputStream(new ZInputStream(httpConnection.openInputStream()));
                 final byte status = dataInputStream.readByte();
 
                 if (status != 1) {
                     throw new RuntimeException("xml processing failed.");
                 }
 
                 final XformResponse response = new XformResponse(status, dataInputStream.readInt(), dataInputStream.readInt());
                 for (int i = 0; i < response.getFailureCount(); i++) {
                     response.addError(new Error(dataInputStream.readByte(), dataInputStream.readShort(), dataInputStream.readUTF()));
                 }
                 return response;
             }
             return null;
         } finally {
             if (dataInputStream != null) {
                 dataInputStream.close();
             }
         }
     }
 
     public static class XformResponse {
         private byte status;
         private int successCount;
         private int failureCount;
 
         private XformResponse(byte status, int successCount, int failureCount) {
             this.status = status;
             this.successCount = successCount;
             this.failureCount = failureCount;
         }
 
         private List<Error> errors = new ArrayList<Error>();
 
         public void addError(Error error) {
             errors.add(error);
         }
 
         public byte getStatus() {
             return status;
         }
 
         public int getSuccessCount() {
             return successCount;
         }
 
         public int getFailureCount() {
             return failureCount;
         }
 
         public List<Error> getErrors() {
             return errors;
         }
     }
 
     public static class Error {
         private byte studyIndex;
         private short formIndex;
         private String error;
 
         private Error(byte studyIndex, short formIndex, String error) {
             this.studyIndex = studyIndex;
             this.formIndex = formIndex;
             this.error = error;
         }
 
         public byte getStudyIndex() {
             return studyIndex;
         }
 
         public short getFormIndex() {
             return formIndex;
         }
 
         public Map<String, List<String>> getErrors() {
             final String errors = error.split(":")[1];
             final String[] errorPairsAsString = errors.split("\n");
 
             final HashMap<String, List<String>> errorPairs = new HashMap<String, List<String>>();
             for (String errorPair : errorPairsAsString) {
                 if (StringUtils.isNotEmpty(errorPair)) {
                     final String[] pair = errorPair.split("=");
                     if (errorPairs.get(pair[0]) == null) {
                         errorPairs.put(pair[0], new ArrayList<String>());
                     }
                     errorPairs.get(pair[0]).add(pair[1]);
                 }
             }
             return errorPairs;
         }
     }
 
     public static class XFormParser {
         public static String parse(String templateName, Map<String, String> data) throws Exception {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document document = db.parse(new File(XFormParser.class.getClassLoader().getResource(templateName).toURI()));
 
             for (Map.Entry<String, String> entry : data.entrySet()) {
                 final NodeList nodeList = document.getElementsByTagName(entry.getKey());
                 if (nodeList == null || nodeList.getLength() == 0)
                     continue;
                 final Node node = nodeList.item(0);
                 node.setTextContent(entry.getValue());
             }
 
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(document);
             StringWriter writer = null;
             try {
                 writer = new StringWriter();
                 StreamResult result = new StreamResult(writer);
                 transformer.transform(source, result);
                 return writer.getBuffer().toString();
             } finally {
                 IOUtils.closeQuietly(writer);
             }
         }
     }
 
     public static class DynamicOptionDependency {
         private byte id;
         private byte dependsOn;
 
         public DynamicOptionDependency(byte id, byte dependsOn) {
             this.id = id;
             this.dependsOn = dependsOn;
         }
 
         public byte getId() {
             return id;
         }
 
         public byte getDependsOn() {
             return dependsOn;
         }
 
         @Override
         public String toString() {
             return "DynamicOptionDependency{" +
                     "id=" + id +
                     ", dependsOn=" + dependsOn +
                     '}';
         }
     }
 }
 
