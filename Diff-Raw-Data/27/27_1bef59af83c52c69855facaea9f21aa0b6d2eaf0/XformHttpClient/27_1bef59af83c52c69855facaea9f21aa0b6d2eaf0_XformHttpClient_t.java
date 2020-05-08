 package org.motechproject.ghana.national.functional.mobile;
 
 import com.jcraft.jzlib.ZInputStream;
 import org.apache.commons.lang.StringUtils;
 import org.fcitmuk.epihandy.FormData;
 import org.fcitmuk.epihandy.FormDef;
 import org.fcitmuk.epihandy.RequestHeader;
 import org.fcitmuk.epihandy.StudyData;
 import org.fcitmuk.epihandy.StudyDef;
 import org.fcitmuk.epihandy.xform.EpihandyXform;
 import org.motechproject.ghana.national.domain.Constants;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
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
         for (String string: xmlStrings) {
             final FormDef formDef = EpihandyXform.getFormDef(EpihandyXform.getDocument(new StringReader(string)));
             studyDef.addForm(formDef);
             final FormData formData = new FormData(formDef);
             hackDate(formData, "/patientRegistration/date");
             hackDate(formData, "/patientRegistration/dateOfBirth");
             formsList.add(formData);
         }
         final StudyData studyData = new StudyData(studyDef);
         studyData.addForms(formsList);
         studyData.write(dataOutputStream);
     }
 
     private static void hackDate(FormData formData, String variableName) throws ParseException {
         final String answer = (String) formData.getQuestion(variableName).getAnswer();
         if (answer != null)
             formData.setDateValue(variableName, new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).parse(answer));
     }
 
     private static XformResponse processResponse(HttpConnection httpConnection) throws IOException {
         DataInputStream dataInputStream = null;
         try {
             if (httpConnection.getResponseCode() == HttpConnection.HTTP_OK) {
                 dataInputStream = new DataInputStream(new ZInputStream(httpConnection.openInputStream()));
                final byte status = dataInputStream.readByte();

                if(status != 1) {
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
 
     static class XformResponse {
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
 
     static class Error {
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
 
         public Map<String, String> getErrors() {
             final String errors = error.split(":")[1];
             final String[] errorPairsAsString = errors.split("\n");
 
             final HashMap<String, String> errorPairs = new HashMap<String, String>();
             for (String errorPair : errorPairsAsString) {
                 if (StringUtils.isNotEmpty(errorPair)) {
                     final String[] pair = errorPair.split("=");
                     errorPairs.put(pair[0], pair[1]);
                 }
             }
             return errorPairs;
         }
     }
 }
 
