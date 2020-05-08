 package puf.m2.hms.model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import puf.m2.hms.exception.HmsException;
 import puf.m2.hms.exception.PatientException;
 
 public class Patient extends HmsEntity {
 
     private static final Map<Integer, Patient> PATIENT_MAP = new CacheAwareMap<Integer, Patient>();
 
     @DbProp
     private String name;
     @DbProp
     private String dateOfBirth;
     @DbProp
     private String address;
     @DbProp
     private int sex;
     @DbProp
     private String phone;
     @DbProp
     private String biographicHealth;
 
     public Patient(String name, String dateOfBirth, String address, int sex,
             String phone, String biographicHealth) {
         this.name = name;
         this.dateOfBirth = dateOfBirth;
         this.address = address;
         this.sex = sex;
         this.phone = phone;
         this.biographicHealth = biographicHealth;
     }
 
     public Patient(int id) {
         super(id);
     }
 
   public void save() throws PatientException {
 
         try {
             super.save();
         } catch (HmsException e) {
             throw new PatientException(e);
         }
 
         PATIENT_MAP.put(id, this);
 
     }
 
 /*    public static boolean checkExistPatient(int id) throws HmsException {
 
         boolean existed = false;
         try {
             DB.createConnection();
             final String queryTemplate = "select id from Patient where id = {0}";
 
             ResultSet rs = DB.executeQuery(MessageFormat.format(queryTemplate,
                     id));
 
             if (rs.next()) {
                 existed = true;
             }
 
             DB.closeConnection();
         } catch (SQLException e) {
             throw new HmsException(e);
         }
         return existed;
 
     }*/
 
     public static List<Patient> getPatients() throws PatientException {
         List<Patient> patientList = new ArrayList<Patient>();
 
         final String query = "select * from Patient";
 
         DB.createConnection();
 
         ResultSet rs = DB.executeQuery(query);
 
         try {
             while (rs.next()) {
                 int id = rs.getInt("id");
                 Patient patient = PATIENT_MAP.get(id);
     
                 if (patient == null) {
                     patient = new Patient(rs.getString("name"),
                             rs.getString("dateOfBirth"), rs.getString("address"),
                             rs.getInt("sex"), rs.getString("phone"),
                             rs.getString("biographicHealth"));
                     patient.id = id;
     
                     PATIENT_MAP.put(id, patient);
                 }
     
                 patientList.add(patient);
             }
         } catch (Exception e) {
             throw new PatientException(e);
         }
 
         DB.closeConnection();
 
         return patientList;
     }
 
     public static Patient getPatientById(int id) throws PatientException {
 
         Patient patient = PATIENT_MAP.get(id);
         if (patient != null) {
             return patient;
         }
 
         DB.createConnection();
 
         final String queryTempl = "SELECT * FROM Patient WHERE id = {0}";
         ResultSet rs = DB.executeQuery(MessageFormat.format(queryTempl, id));
 
         try {
             if (rs.next()) {
                 patient = new Patient(rs.getString("name"),
                         rs.getString("dateOfBirth"), rs.getString("address"),
                         rs.getInt("sex"), rs.getString("phone"),
                         rs.getString("biographicHealth"));
                 patient.id = id;
 
                 PATIENT_MAP.put(patient.getId(), patient);
             }
         } catch (SQLException e) {
             throw new PatientException(e);
         }
 
         DB.closeConnection();
 
         return patient;
     }
 
     public static List<Patient> getPatientByName(String patientName)
             throws PatientException {
 
         List<Patient> patientList = new ArrayList<Patient>();
 
        final String queryTemplate = "SELECT * FROM Patient WHERE PatientName = ''{0}''";
 
         DB.createConnection();
 
         ResultSet rs = DB.executeQuery(MessageFormat.format(queryTemplate,
                 patientName));
 
         try {
             while (rs.next()) {
                 int id = rs.getInt("id");
                 Patient patient = PATIENT_MAP.get(id);
     
                 if (patient == null) {
                     patient = new Patient(rs.getString("name"),
                             rs.getString("dateOfBirth"), rs.getString("address"),
                             rs.getInt("sex"), rs.getString("phone"),
                             rs.getString("biographicHealth"));
                     patient.id = id;
     
                     PATIENT_MAP.put(id, patient);
                 }
                 patientList.add(patient);
             }
         } catch (Exception e) {
             throw new PatientException(e);
         }
 
         DB.closeConnection();
 
         return patientList;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDateOfBirth() {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(String dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public int getSex() {
         return sex;
     }
 
     public void setSex(int sex) {
         this.sex = sex;
     }
 
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getBiographicHealth() {
         return biographicHealth;
     }
 
     public void setBiographicHealth(String biographicHealth) {
         this.biographicHealth = biographicHealth;
     }
     
     public static void main (String[] args) throws Exception {
         Patient p = getPatientById(1);
         p.save();
         
     }
 
 }
