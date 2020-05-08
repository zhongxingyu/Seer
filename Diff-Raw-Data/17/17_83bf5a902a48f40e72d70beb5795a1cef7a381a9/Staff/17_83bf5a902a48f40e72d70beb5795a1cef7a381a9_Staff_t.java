 package oscar.model;
 
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import oscar.persistance.DbRecordable;
 import oscar.util.Utility;
 
 /**
  * @author Stefano
  *
  * Class Staff for staff members
  */
 public class Staff extends Person implements DbRecordable {
 
     /** Database table name*/
     public static String TABLE = "staff";
     public static String FK = "personId";
     private String username;
     private String password;
     private boolean admin;
     private boolean chauffeur;
 
     public Staff() {
         this.dependencies = new HashMap<String, String>();
         this.dependencies.put("person", "personId");
         this.useTable = TABLE;
         this.initStaff();
     }
     
     
 
     /**
      * Class constructor
      * @param username
      */
     public Staff(String colName, String value) {
         //super(colName,value);
         this.useTable = TABLE;
         this.initStaff();
         HashMap<String, String> attributes = this.findOneBy(colName, value);
         this.username = attributes.get("username");
         this.admin = (attributes.get("admin").contains("1")) ? true : false;
         this.chauffeur = (attributes.get("chauffeur").contains("1")) ? true : false;
         this.personId = Integer.parseInt(attributes.get("personId"));
     }
 
     private void initStaff() {
         this.dependentTable = "person";
         this.dependentTablePK = "personId";
         this.foreignKey = "personId";
     }
 
     /**
      * Class constructor
      * @param name
      * @param surname
      * @param dateOfBirth
      * @param email
      * @param address
      * @param phone
      * @param username
      * @param admin
      * @param chauffeur
      */
     public Staff(String name, String surname, String dateOfBirth,
             String email, String address, String phone, String username,
             String password, boolean admin, boolean chauffeur) {
         super(name, surname, dateOfBirth, email, address, phone);
         initStaff();
         this.username = username;
         this.admin = admin;
         this.chauffeur = chauffeur;
         try {
             this.password = Utility.encodeSHA256(password);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(Staff.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      *
      * @param password inputed password
      * @return whether the password match the one in the database
      * @throws NoSuchAlgorithmException
      */
     public boolean authorize(String password) {
         HashMap<String, String> hm = this.findOneBy("username", this.username);
         String inputPassword = "";
         try {
             inputPassword = Utility.encodeSHA256(password);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(Staff.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (hm.get("password").equals(inputPassword))
             return true;
         return false;
     }
 
     /**
      *
      * @return
      */
     @Override
     public boolean add() {
         try {
             //HashMap<String, HashMap<String, String>> data = Utility.convertToHashMapWithParent(this);
             return this.addDependent();
         } catch (SQLException ex) {
             Logger.getLogger(Staff.class.getName()).log(Level.SEVERE, null, ex);
         }
         return false;
     }
 
     /**
      *
      * @return
      */
     @Override
     public boolean delete() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      *
      * @return
      */
     @Override
     public boolean update() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean isAdmin() {
         return admin;
     }
 
     @Override
     public TableModel getTableModel() {
 
         ArrayList<HashMap<String, String>> dependencies = new ArrayList<HashMap<String, String>>();
 
         HashMap<String, String> personDep = new HashMap<String, String>();
 
         personDep.put("table", "person");
         personDep.put("pk", "personId");
         personDep.put("joinType", "inner join");
 
         //personDep.put("joinTo", "");
         personDep.put("fk", "personId");
         dependencies.add(personDep);
 
         ArrayList<HashMap<String, String>> map = this.queryDependent(dependencies, "*", "*");
         DefaultTableModel model = new DefaultTableModel(
                 new Object[]{"Id", "Name", "Surname", "Admin", "Chauffeur", "Username", "Date of birth", "email"}, 0);
         for (HashMap<String, String> row : map)
             model.addRow(new Object[]{
                         row.get("personId"),
                         row.get("name"),
                         row.get("surname"),
                         (row.get("admin").contains("1")) ? true : false,
                         (row.get("chauffeur").contains("1")) ? true : false,
                         row.get("username"),
                         row.get("dateOfBirth"),
                         row.get("email")
                     });
         return model;
     }
 
     public boolean isIsAdmin() {
         return admin;
     }
    public boolean getAdmin(){
         return isIsAdmin();
     }
 
     public void setIsAdmin(String isAdmin) {
         this.admin = isAdmin.equals("1") ? true : false;
     }
     
     
    public boolean getChauffeur(){
         return isIsChauffeur();
     }
 
     public boolean isIsChauffeur() {
         return chauffeur;
     }
 
     public void setIsChauffeur(String isChauffeur) {
         this.chauffeur = isChauffeur.equals("1") ? true : false;
     }
     
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         try {
             this.password = Utility.encodeSHA256(password);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(Staff.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
