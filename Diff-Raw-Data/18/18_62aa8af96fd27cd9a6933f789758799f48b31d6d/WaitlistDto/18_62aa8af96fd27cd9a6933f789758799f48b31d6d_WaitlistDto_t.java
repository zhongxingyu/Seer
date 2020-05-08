 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package backend.DataTransferObjects;
 
 import java.sql.Date;
 import java.util.HashMap;
 
 /**
  *
  * @author kenny
  */
 public class WaitlistDto extends AbstractDto {
     
     public static final String WAITLIST_ID = "WaitlistID";
    public static final String TYPE = "Type";
     public static final String DATE = "DatetimeEntered";
     public static final String COMMENTS = "Comments";
     
     private PatientDto patient;
     
     public WaitlistDto() {
         fieldsMap = new HashMap<String, Object>();
         fieldsMap.put(WAITLIST_ID, null);
        fieldsMap.put(TYPE, null);
         fieldsMap.put(DATE, null);
         fieldsMap.put(COMMENTS, null);
     }
     
     public Integer getWaitlistID(){
         return (Integer) fieldsMap.get(WAITLIST_ID);
     }
     
     public Integer getPatientID(){
         return patient.getPatID();
     }
 
     public PatientDto getPatient() {
     	return patient;
     }
 
     public void setPatient(PatientDto patient) {
     	this.patient = patient;
     }
 
     public Integer getTypeID(){
        return ((TypeDto) fieldsMap.get(TYPE)).getTypeID();
     }
     
    public void setType(TypeDto t){
        fieldsMap.put(TYPE, t);
     }
     
     public String getTypeName(){
        return ((TypeDto) fieldsMap.get(TYPE)).getTypeName();
     }
     
     public Date getDate(){
         return (Date) fieldsMap.get(DATE);
     }
     
     public String getComments() {
     	return (String) fieldsMap.get(COMMENTS);
     }
     
     public void setComments(String comment) {
     	fieldsMap.put(COMMENTS, comment);
     }
 }
