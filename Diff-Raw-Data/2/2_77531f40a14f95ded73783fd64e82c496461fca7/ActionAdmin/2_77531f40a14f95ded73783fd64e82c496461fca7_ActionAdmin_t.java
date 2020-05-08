 package org.powertac.tourney.actions;
 
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 import org.hibernate.Session;
 import org.hibernate.exception.ConstraintViolationException;
 import org.powertac.tourney.beans.Location;
 import org.powertac.tourney.beans.Machine;
 import org.powertac.tourney.beans.Pom;
 import org.powertac.tourney.beans.User;
 import org.powertac.tourney.services.Database;
 import org.powertac.tourney.services.HibernateUtil;
 import org.powertac.tourney.services.TournamentProperties;
 import org.powertac.tourney.services.Upload;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @ManagedBean
 @RequestScoped
 public class ActionAdmin
 {
   private String sortColumnPom = null;
   private boolean sortAscendingPom = true;
   private String sortColumnMachine = null;
   private boolean sortAscendingMachine = true;
   private String sortColumnUsers = null;
   private boolean sortAscendingUsers = true;
 
   private String newLocationName = "";
   private Date newLocationStartTime = null;
   private Date newLocationEndTime = null;
 
   private int machineId = -1;
   private String machineName = "";
   private String machineUrl = "";
   private String machineViz = "";
 
   private Upload upload = new Upload();
   private UploadedFile uploadedPom;
   private String pomName;
 
   private TournamentProperties properties = TournamentProperties.getProperties();
 
   public ActionAdmin ()
   {
   }
 
   public List<String> getConfigErrors()
   {
     return properties.getConfigErrors();
   }
 
   //<editor-fold desc="Location stuff">
   public List<Location> getLocationList ()
   {
     List<Location> locations = new ArrayList<Location>();
     Database db = new Database();
 
     try {
       db.startTrans();
       locations = db.getLocations();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return locations;
   }
 
   public void addLocation ()
   {
     if (newLocationName.isEmpty() || (newLocationStartTime == null) || (newLocationEndTime == null)) {
       return;
     }
 
     Database db = new Database();
     try {
       db.startTrans();
       db.addLocation(newLocationName, newLocationStartTime, newLocationEndTime);
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
 
   public void deleteLocation (Location l)
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.deleteLocation(l.getLocationId());
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
   //</editor-fold>
 
   //<editor-fold desc="Pom stuff">
   public List<Database.Pom> getPomList ()
   {
     List<Database.Pom> poms = new ArrayList<Database.Pom>();
 
     Database db = new Database();
 
     try {
       db.startTrans();
       poms = db.getPoms();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return poms;
   }
 
   public void submitPom ()
   {
     if (pomName.isEmpty()) {
       // Show succes message.
       String msg = "Error: You need to fill in the pom name";
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("pomUploadForm", fm);
       return;
     }
 
     if (uploadedPom == null) {
       String msg = "Error: You need to choose a pom file";
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("pomUploadForm", fm);
       return;
     }
 
     User currentUser = User.getCurrentUser();
     Pom p = new Pom();
     p.setName(getPomName());
     p.setUploadingUser(currentUser.getUsername());
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     session.beginTransaction();
     try {
       session.save(p);
     }
     catch (ConstraintViolationException e) {
       session.getTransaction().rollback();
       String msg = "Error: This name is already used";
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("pomUploadForm", fm);
       return;
     }
 
     upload.setUploadedFile(uploadedPom);
     upload.setUploadLocation(properties.getProperty("pomLocation"));
     boolean pomStored = upload.submit("pom." + p.getPomId() + ".xml");
 
     if (pomStored) {
       session.getTransaction().commit();
     }
     else {
       session.getTransaction().rollback();
     }
   }
   //</editor-fold>
 
   //<editor-fold desc="Machine stuff">
   public List<Machine> getMachineList ()
   {
     return Machine.getMachineList();
   }
 
   public void toggleAvailable (Machine m)
   {
     Database db = new Database();
 
     try {
       db.startTrans();
       if (m.isAvailable()) {
         db.setMachineAvailable(m.getMachineId(), false);
       }
       else {
         db.setMachineAvailable(m.getMachineId(), true);
       }
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
   public void toggleStatus(Machine m){
     Database db = new Database();
     
     try {
       db.startTrans();
       if(m.isInProgress()){
         db.setMachineStatus(m.getMachineId(), Machine.STATE.idle);
       }else{
         db.setMachineStatus(m.getMachineId(), Machine.STATE.running);
       }
       db.commitTrans();
     }
     catch(Exception e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
   
   public void editMachine(Machine m)
   {
     machineId = m.getMachineId();
     machineName = m.getName();
     machineUrl = m.getUrl();
    machineViz = m.getVizUrl();
   }
   
   public void saveMachine()
   {
     machineUrl = machineUrl.replace("https://", "").replace("http://", "");
     machineViz = machineViz.replace("https://", "").replace("http://", "");
 
     if (machineName.isEmpty() || machineUrl.isEmpty() || machineViz.isEmpty()) {
       String msg = "Error: machine not saved, some fields were empty!";
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("saveMachine", fm);
   	  return;
   	}
 
     // It's a new machine
     if (machineId == -1) {
       addMachine();
       return;
     }
 	  
     Database db = new Database();
     try {
       db.startTrans();
       db.editMachine(machineName, machineUrl, machineViz, machineId);
       db.commitTrans();
       resetMachineData();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       String msg = "Error : machine not edited " + e.getMessage();
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("saveMachine", fm);
     }
   }
 
   public void deleteMachine (Machine m)
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.deleteMachine(m.getMachineId());
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       String msg = "Error : machine not added " + e.getMessage();
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("saveMachine", fm);
     }
   }
 
   public void addMachine ()
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.addMachine(machineName, machineUrl, machineViz);
       db.commitTrans();
       
       resetMachineData();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       String msg = "Error : machine not added " + e.getMessage();
       FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
       FacesContext.getCurrentInstance().addMessage("saveMachine", fm);
     }
   }
 
   private void resetMachineData() {
     machineId = -1;
     machineViz = "";
     machineName = "";
     machineUrl = "";
   }
   //</editor-fold>
 
   public List<User> getUserList()
   {
     List<User> users = new ArrayList<User>();
 
     Database db = new Database();
     try {
       db.startTrans();
       users = db.getAllUsers();
       db.commitTrans();
     }
     catch (SQLException e){
       db.abortTrans();
       e.printStackTrace();
     }
 
     return users;
   }
 
   public void refresh ()
   {
 
   }
 
   //<editor-fold desc="Setters and Getters">
   public String getNewLocationName ()
   {
     return newLocationName;
   }
   public void setNewLocationName (String newLocationName)
   {
     this.newLocationName = newLocationName;
   }
 
   public Date getNewLocationStartTime ()
   {
     return newLocationStartTime;
   }
   public void setNewLocationStartTime (Date newLocationStartTime)
   {
     this.newLocationStartTime = newLocationStartTime;
   }
 
   public Date getNewLocationEndTime ()
   {
     return newLocationEndTime;
   }
   public void setNewLocationEndTime (Date newLocationEndTime)
   {
     this.newLocationEndTime = newLocationEndTime;
   }
 
   public String getPomName ()
   {
     return pomName;
   }
   public void setPomName (String pomName)
   {
     this.pomName = pomName.trim();
   }
 
   public UploadedFile getUploadedPom ()
   {
     return uploadedPom;
   }
   public void setUploadedPom (UploadedFile uploadedPom)
   {
     this.uploadedPom = uploadedPom;
   }
 
   public int getMachineId ()
   {
     return machineId;
   }
   public void setMachineId(int machineId) {
     this.machineId = machineId;
   }
 
   public String getMachineName ()
   {
     return machineName;
   }
   public void setMachineName (String machineName)
   {
     this.machineName = machineName;
   }
 
   public String getMachineUrl ()
   {
     return machineUrl;
   }
   public void setMachineUrl (String machineUrl)
   {
     this.machineUrl = machineUrl;
   }
 
   public String getMachineViz ()
   {
     return machineViz;
   }
   public void setMachineViz (String machineViz)
   {
     this.machineViz = machineViz;
   }
   //</editor-fold>
 
   //<editor-fold desc="Sorting setters and getters">
   public boolean isSortAscendingPom ()
   {
     return sortAscendingPom;
   }
   public void setSortAscendingPom (boolean sortAscendingPom)
   {
     this.sortAscendingPom = sortAscendingPom;
   }
 
   public String getSortColumnPom()
   {
     return sortColumnPom;
   }
   public void setSortColumnPom(String sortColumnPom)
   {
     this.sortColumnPom = sortColumnPom;
   }
 
   public String getSortColumnMachine ()
   {
     return sortColumnMachine;
   }
   public void setSortColumnMachine (String sortColumnMachine)
   {
     this.sortColumnMachine = sortColumnMachine;
   }
 
   public boolean isSortAscendingMachine ()
   {
     return sortAscendingMachine;
   }
   public void setSortAscendingMachine (boolean sortAscendingMachine)
   {
     this.sortAscendingMachine = sortAscendingMachine;
   }
 
   public String getSortColumnUsers ()
   {
     return sortColumnUsers;
   }
   public void setSortColumnUsers (String sortColumnUsers)
   {
     this.sortColumnUsers = sortColumnUsers;
   }
 
   public boolean isSortAscendingUsers ()
   {
     return sortAscendingUsers;
   }
   public void setSortAscendingUsers (boolean sortAscendingUsers)
   {
     this.sortAscendingUsers = sortAscendingUsers;
   }
   //</editor-fold>
 }
