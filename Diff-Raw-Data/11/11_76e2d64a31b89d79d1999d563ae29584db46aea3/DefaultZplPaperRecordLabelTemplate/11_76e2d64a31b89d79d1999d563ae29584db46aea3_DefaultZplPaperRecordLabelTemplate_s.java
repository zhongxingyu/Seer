 package org.openmrs.module.paperrecord;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.api.context.Context;
 import org.openmrs.layout.web.address.AddressSupport;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.utils.GeneralUtils;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 public class DefaultZplPaperRecordLabelTemplate implements PaperRecordLabelTemplate {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private MessageSourceService messageSourceService;
 
     private EmrApiProperties emrApiProperties;
 
     public void setMessageSourceService(MessageSourceService messageSourceService) {
         this.messageSourceService = messageSourceService;
     }
 
     public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
         this.emrApiProperties = emrApiProperties;
     }
 
     @Override
     public String generateLabel(Patient patient, String paperRecordIdentifier) {
 
         if (patient.getPersonName() == null) {
             throw new IllegalArgumentException("Patient needs to have at least one name");
         }
 
         PatientIdentifier primaryIdentifier = patient.getPatientIdentifier(emrApiProperties.getPrimaryIdentifierType());
 
         if (primaryIdentifier == null) {
             throw new IllegalArgumentException("No primary identifier for this patient");
         }
 
         DateFormat df = new SimpleDateFormat("dd/MMM/yyyy", Context.getLocale());
 
         // TODO: potentially pull this formatting code into a configurable template?
         // build the command to send to the printer -- written in ZPL
         StringBuilder data = new StringBuilder();
         data.append("^XA");
         data.append("^CI28");   // specify Unicode encoding
 
         /* LEFT COLUMN */
 
         String patientName = null;
         if(patient.getPersonName() != null ){
             patientName = (patient.getPersonName().getFamilyName() != null ? patient.getPersonName().getFamilyName() : "") + ", "
                     + (patient.getPersonName().getGivenName() != null ? patient.getPersonName().getGivenName() : "");
         }
         /* Name (Only print first and last name) */
         if (patientName != null) {
             if (patientName.length() > PaperRecordLabelTemplate.CHARTLABEL_PRINTER_LINE_MAX_SIZE){
                 patientName = StringUtils.substring(patientName,  0, PaperRecordLabelTemplate.CHARTLABEL_PRINTER_LINE_MAX_SIZE -1);
             }
             data.append("^FO140,40^AVN^FD" + patientName + "^FS");
         }
 
         /* Primary identifier */
         data.append("^FO140,120^AUN^FD" + primaryIdentifier.getIdentifier() + "^FS");
 
 
         /* Birthdate & Gender */
         data.append("^FO140,190^ATN^FD");
         if (patient.getBirthdate() != null) {
             data.append(df.format(patient.getBirthdate()) + (patient.getBirthdateEstimated() ? " (*)" : "") + ", ");
         }
         if (patient.getGender() != null) {
            data.append(messageSourceService.getMessage("paperrecord.gender." + patient.getGender()));
         }
         data.append("^FS");
 
         /* Address (using address template) */
         if (patient.getPersonAddress() != null) {
 
             int verticalPosition = 250;
 
             // print out the address using the layout format
             // first iterate through all the lines in the format
             if (AddressSupport.getInstance().getDefaultLayoutTemplate() != null && AddressSupport.getInstance().getDefaultLayoutTemplate().getLines() != null) {
 
                 List<List<Map<String, String>>> lines = AddressSupport.getInstance().getDefaultLayoutTemplate().getLines();
                 ListIterator<List<Map<String, String>>> iter = lines.listIterator();
 
                 while (iter.hasNext()) {
                     List<Map<String, String>> line = iter.next();
                     // now iterate through all the tokens in the line and build the string to print
                     StringBuffer output = new StringBuffer();
                     for (Map<String, String> token : line) {
                         // find all the tokens on this line, and then add them to that output line
                         if (token.get("isToken").equals(AddressSupport.getInstance().getDefaultLayoutTemplate().getLayoutToken())) {
 
                             String property = GeneralUtils.getPersonAddressProperty(patient.getPersonAddress(), token.get("codeName"));
 
                             if (!StringUtils.isBlank(property)) {
                                 output.append(property + ", ");
                             }
                         }
                     }
 
                     if (output.length() > 2) {
                         // drop the trailing comma and space from the last token on the line
                         output.replace(output.length() - 2, output.length(), "");
                     }
 
                     if (!StringUtils.isBlank(output.toString())) {
                         data.append("^FO140," + verticalPosition + "^ATN^FD" + output.toString() + "^FS");
                         verticalPosition = verticalPosition + 50;
                     }
                 }
             } else {
                 log.error("Address template not properly configured");
             }
         }
 
         /* RIGHT COLUMN */
 
         /* Print the patient's paper record identifier, if it exists */
         if (StringUtils.isNotBlank(paperRecordIdentifier)) {
             data.append("^FO680,40^FB520,1,0,R,0^AUN^FD" + messageSourceService.getMessage("paperrecord.archivesRoom.recordNumber.label")
                     + " " + paperRecordIdentifier + "^FS");
         }
 
         /* Print the bar code, based on the primary identifier */
         data.append("^FO780,100^ATN^BY4^BCN,150,N^FD" + primaryIdentifier.getIdentifier() + "^FS");    // print barcode & identifier
 
         /* Print command */
         data.append("^XZ");
 
         return data.toString();
     }
 
     @Override
     public String getEncoding() {
         return "UTF-8";
     }
 }
