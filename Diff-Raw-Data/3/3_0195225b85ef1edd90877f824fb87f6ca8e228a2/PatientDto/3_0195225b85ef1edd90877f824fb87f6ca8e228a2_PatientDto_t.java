 package backend.DataTransferObjects;
 
 import java.util.HashMap;
 
 /**
  * DTO class for patients
  */
 public class PatientDto extends AbstractDto {
 
     public static final String PATIENT_ID = "PatID";
     public static final String FIRST = "FirstName";
     public static final String LAST = "LastName";
     public static final String PHONE = "PhoneNumber";
     public static final String NOTES = "Notes";
     public static final String NO_SHOW = "NumberOfNoShows";
 
     public static void main(String[] args) {
         // main method for testing only, please delete
     }
 
     public PatientDto () {
         fieldsMap = new HashMap<String, Object>();
         fieldsMap.put(PATIENT_ID, null);
         fieldsMap.put(FIRST, null);
         fieldsMap.put(LAST, null);
         fieldsMap.put(PHONE, null);
         fieldsMap.put(NOTES, null);
         fieldsMap.put(NO_SHOW, null);
     }
     
     public Integer getNoShows(){
    	if (fieldsMap.get(NO_SHOW) == null){
    		return 0;
    	}
         return (Integer) fieldsMap.get(NO_SHOW);
     }
 
     public Integer getPatID() {
         return (Integer) getField(PATIENT_ID);
     }
 
     public String getFirst() {
         return (String) getField(FIRST);
     }
 
     public String getLast() {
         return (String) getField(LAST);
     }
 
     public String getPhone() {
         return (String) getField(PHONE);
     }
 
     public String getNotes() {
         if (getField(NOTES) == null){
             return "";
         }
         return (String) getField(NOTES);
     }
 
     public PatientDto setFirst(String first) {
         fieldsMap.put(FIRST, first);
         return this;
     }
 
     public PatientDto setLast(String last) {
         fieldsMap.put(LAST, last);
         return this;
     }
 
     public PatientDto setPhone(String phone) {
         fieldsMap.put(PHONE, phone);
         return this;
     }
 
     public PatientDto setNotes(String notes) {
         fieldsMap.put(NOTES, notes);
         return this;
     }
 
     public String getFullName() {
         return this.getFirst() + " " + this.getLast();
     }
 }
