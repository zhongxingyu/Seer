 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mwr.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import com.mwr.database.*;
 import com.mwr.businesslogic.TokenGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 @ManagedBean(name = "bean")
 @SessionScoped
 public class DatabaseJSFManagedBean {
 
     private Session session;
     private HibernateUtil helper;
     private Employee employee;
     private Device device;
     private Devicenotregistered device_not;
     private List<Devicenotregistered> waitingList;
     private List<Device> deviceList;
     private List<Employee> employeeList;
     private List<Technician> technicianList, techAdmin, techNonAdmin;
     private Settings latestSetting;
     private List<Blacklistedapplications> apps;
     private Scanresults results;
     private List<Scanresults> device_results;
     private String appName;
     private String appCategory;
     private int allowed;
     private int denied;
     private List<Employee> employeeDevices;
     private List<Settings> settings;
     private List<Scanresults> allowedScans;
     private List<Scanresults> deniedScans;
     private Settings specificSetting;
     private int rootedWeight;
     private int debugWeight;
     private int unknownSourcesWeight;
     private int osWeight;
     private int lowRiskApp;
     private int mediumRiskApp;
     private int highRiskApp;
     private int blockedApp;
     private int accessScore;
 
     public DatabaseJSFManagedBean() {
 
         session = HibernateUtil.getSessionFactory().openSession();
 
     }
 
     /*===============================================
      *||                  To do                    ||
      *===============================================
      */
     //logout link to log out user
     public void logout() {
     }
 
     //get a specific Device for an Employee
     //get a specific Scanresults for a Device
     public Scanresults getScanResult(String scanid) {
         return null;
     }
 
     //get a token for a Device for an Employee
     public String getDeviceToken(String devid) {
         return null;
     }
 
     public void setAppName(String name) {
         appName = name;
     }
 
     public void setAppCategory(String cat) {
         appCategory = cat;
     }
 
     public String getAppName() {
         return appName;
     }
 
     public String getAppCategory() {
         return appCategory;
     }
 
     //check if device registered
     public boolean deviceRegistered(String mac, String serial, String androidID) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Device where MACAddress = :mac and UID = :uid and SerialNumber = :serial");
         query.setParameter("mac", mac);
         query.setParameter("serial", serial);
         query.setParameter("uid", androidID);
         List<Device> devices = query.list();
         if (devices.isEmpty()) {
             return false;
         } else {
             return true;
         }
     }
 
     //add a blacklisted Application
     public String addBlacklistedApp() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Blacklistedapplications app = new Blacklistedapplications(appName, appCategory);
         session.save(app);
         session.getTransaction().commit();
         session.close();
         return "settings.xhtml";
     }
     //change a blacklisted Application's catagory
 
     public void changeAppCatagory(int id, String Catagory) {
     }
 
     //Save WeightSystem Settings
     public void saveWeightSettings(int api, int usb, int unknownS, int rooted, int access) {
         osWeight = api;
         debugWeight = usb;
         unknownSourcesWeight = unknownS;
         rootedWeight = rooted;
         accessScore = access;
         addSetting();
     }
 
     //Save Blacklisted Applications Settings
     public void saveBlacklistedSettings(int low, int med, int high, int block) {
         lowRiskApp = low;
         mediumRiskApp = med;
         highRiskApp = high;
         blockedApp = block;
         addSetting();
     }
 
 
     //Save Scan Settings
     public void saveScanSettings() {
         
         addSetting();
     }
 
     /*===============================================
      *||               To do END                   ||
      *===============================================
      */
     /*===============================================
      *||            Newly added                    ||
      *===============================================
      */
     //get path to relevent link. (Dit lyk beter)
     public String getLink(String link) {
 
         return "/BYOD/faces/view/" + link + ".xhtml";
     }
 
     //get the Employee with the empId = id
     public Employee getEmployee(String id) {
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Employee where empId = :id");
             query.setParameter("id", id);
             List<Employee> list = query.list();
             Employee e = list.get(0);
             if (e == null) {
                 return null;
             } else {
                 return e;
             }
         } catch (Exception e) {
             return null;
         }
     }
 
     public Device getDevice(String id) {
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Device where id = :id");
             query.setParameter("id", id);
             List<Device> list = query.list();
             Device d = list.get(0);
             if (d == null) {
                 return null;
             } else {
                 return d;
             }
         } catch (Exception e) {
             return null;
         }
     }
     //get the Devices owned by the Employee with the empId = id
 
     /*===============================================
      *||         Newly added END                   ||
      *===============================================
      */
     /*===============================================
      *||           Technician stuff                ||
      *===============================================
      */
     //validate password for a Technician
     public Boolean correctPassword(String empid) {
         return null;
     }
 
     //validate password1 == password2
     public Boolean matchPasswords(String p1, String p2) {
         return null;
     }
 
     /*===============================================
      *||         Technician stuff END              ||
      *===============================================
      */
     public void addToWaitingList(String mac, String android, String serial, String make, String model, String username, String password, String idnumber, String name, String surname) throws NoSuchAlgorithmException {
         try {
             TokenGenerator gen = new TokenGenerator();
             String token = gen.generateToken(mac, android, serial);
             DevicenotregisteredId devicePK = new DevicenotregisteredId(mac, android, serial);
             device_not = new Devicenotregistered(devicePK, make, model, username, password, idnumber, name, surname, token);
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             session.save(device_not);
             session.getTransaction().commit();
         } finally {
             session.close();
         }
 
     }
 
     public Employee addEmployee(String username, String password, String name, String surname, String idnumber) {
         employee = new Employee(username, password, new Date(), name, surname, idnumber);
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(employee);
         session.getTransaction().commit();
         session.close();
         return employee;
     }
 
     public void addDevice(Devicenotregistered d) {
         try {
 
             Employee emp = addEmployee(d.getUsername(), d.getPassword(), d.getName(), d.getSurname(), d.getIdnumber());
             DeviceId id = new DeviceId(d.getId());
             Device dev = new Device(id, emp, d.getMake(), d.getModel(), new Date());
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             session.save(dev);
             session.getTransaction().commit();
             session.close();
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             session.delete(d);
             session.getTransaction().commit();
 
         } finally {
             session.close();
         }
 
     }
 
     public void addTechnician(String employeeCode, String userName, String password, Date dateRegistered, boolean admin) {
         try {
             Technician tech = new Technician(employeeCode, userName, password, dateRegistered, admin);
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             session.save(tech);
             session.getTransaction().commit();
         } finally {
             session.close();
         }
     }
 
     public Employee getEmployee() {
         try {
             return employee;
         } catch (Exception e) {
             System.err.println("Employee does not exist");
         }
         return employee;
     }
 
     public List getWaitingList() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Devicenotregistered");
         waitingList = query.list();
         return waitingList;
     }
 
     public List getWaitingList(String id) {
         List<Devicenotregistered> waitingListUnique = null;
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Devicenotregistered where idnumber = :id");
             query.setParameter("id", id);
             waitingListUnique = query.list();
         } catch (Exception e) {
             System.err.println("Problem...");
         }
         return waitingListUnique;
     }
 
     public List getDeviceList() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Device");
         deviceList = query.list();
         return deviceList;
     }
 
     public List getTechnicianList() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Technician");
         technicianList = query.list();
         return technicianList;
 
 
     }
 
     public List getTechnicianAdminList() {
 
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Technician");
         technicianList = query.list();
         for (int i = 0; i < technicianList.size(); i++) {
             if (technicianList.get(i).isAdmin()) {
                 techAdmin.add(technicianList.get(i));
             } else {
                 techNonAdmin.add(technicianList.get(i));
             }
         }
         return technicianList;
 
 
     }
 
     public List getEmployeeList() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Employee order by Surname asc");
         employeeList = query.list();
         return employeeList;
 
     }
 
     public List<Blacklistedapplications> getApps() {
 
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Blacklistedapplications");
         apps = query.list();
         return apps;
 
     }
 
     public Settings getLatestSetting() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Settings order by SettingDate desc");
         List setting = query.list();
         latestSetting = (Settings) setting.get(0);
         session.close();
         return latestSetting;
 
     }
 
     public boolean addScanResults(String mac, String serial, String androidID, boolean rooted, boolean debug, boolean unknown, String installedApps, int api) {
         getLatestSetting();
         getApps();
        int totalScore;
         int rootScore = 0;
         int debugScore = 0;
         int unknownScore = 0;
         int appScore = 0;
        int apiScore;
 
         if (rooted) {
             rootScore = latestSetting.getRootedWeight();
         }
         if (debug) {
             debugScore = latestSetting.getDebugWeight();
         }
         if (unknown) {
             unknownScore = latestSetting.getUnknownSourcesWeight();
         }
 
         String blacklistedApps = "";
         String[] appArray = installedApps.split(",");
         for (int i = 0; i < appArray.length; i++) {
             int k = 0;
             while (k < apps.size()) {
                 if (appArray[i].contains(apps.get(k).getAppName())) {
                     String cat = apps.get(k).getAppCategory();
                     blacklistedApps += appArray[i];
                     if (cat.equals("Low")) {
                         appScore += latestSetting.getLowRiskApp();
                     } else if (cat.equals("Medium")) {
                         appScore += latestSetting.getMediumRiskApp();
                     } else if (cat.equals("High")) {
                         appScore += latestSetting.getHighRiskApp();
                     } else if (cat.equals("Blocked")) {
                         appScore += latestSetting.getBlockedApp();
                     }
                 }
                 k++;
             }
         }
 
         apiScore = (17 - api) * latestSetting.getOsweight();
         totalScore = rootScore + debugScore + unknownScore + appScore + apiScore;
         boolean allow = false;
         if (totalScore < latestSetting.getAccessScore()) {
             allow = true;
         }
 
         results = new Scanresults(latestSetting, new Date(), rooted, rootScore, debug, debugScore, unknown, unknownScore, blacklistedApps, appScore, Integer.toString(api), apiScore, totalScore, allow, mac, androidID, serial);
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(results);
         session.getTransaction().commit();
 
         return allow;
 
     }
 
     public List getEmployeeDevices(int id) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Device where employee_empID = :id");
         query.setParameter("id", id);
         employeeDevices = query.list();
         return employeeDevices;
     }
 
     public int getWeightSystemTotal() {
         int score = 0;
         List<Integer> weights = new ArrayList<Integer>();
 
         weights.add(latestSetting.getAccessScore());
         weights.add(latestSetting.getDebugWeight());
         weights.add(latestSetting.getOsweight());
         weights.add(latestSetting.getRootedWeight());
         weights.add(latestSetting.getUnknownSourcesWeight());
 
         for (int i = 0; i < weights.size(); i++) {
             score += weights.get(i);
         }
 
         return score;
     }
 
     public int getWeightAppsTotal() {
         int score = 0;
         List<Integer> weights = new ArrayList<Integer>();
 
         weights.add(latestSetting.getBlockedApp());
         weights.add(latestSetting.getHighRiskApp());
         weights.add(latestSetting.getLowRiskApp());
         weights.add(latestSetting.getMediumRiskApp());
 
         for (int i = 0; i < weights.size(); i++) {
             score += weights.get(i);
         }
 
         return score;
     }
 
     public int getScanDays() {
         return 5;
     }
 
     public int getScanHours() {
         return 0;
     }
 
     public int getScanMinutes() {
         return 0;
     }
 
     public int getScanSeconds() {
         return 0;
     }
 
     public String setDevice(DeviceId id) {
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Scanresults where device_MACAddress = :mac and device_UID = :uid and device_SerialNumber = :serial order by Date desc");
             query.setParameter("mac", id.getMacaddress());
             query.setParameter("uid", id.getUid());
             query.setParameter("serial", id.getSerialNumber());
             device_results = query.list();
             query = session.createQuery("from Device where MACAddress = :mac and UID = :uid and SerialNumber = :serial");
             query.setParameter("mac", id.getMacaddress());
             query.setParameter("uid", id.getUid());
             query.setParameter("serial", id.getSerialNumber());
             device = (Device) query.list().get(0);
         } finally {
             return "device.xhtml";
         }
 
     }
 
     public List getDevice_Results() {
         return device_results;
     }
 
     public int getDeviceSize() {
         return getDeviceList().size();
     }
 
     public Device getDevice() {
         return device;
     }
 
     public int getQueueSize() {
         return getWaitingList().size();
 
     }
 
     public int getAllowed() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where accessAllowed = true");
         allowed = query.list().size();
         session.close();
         return allowed;
     }
 
     public int getDenied() {
 
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where accessAllowed = false");
         denied = query.list().size();
         session.close();
         return denied;
     }
 
     public String removeDevice(Device device) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("delete from Scanresults where device_MACAddress = :mac and device_UID = :uid and device_SerialNumber = :serial");
         query.setParameter("mac", device.getId().getMacaddress());
         query.setParameter("uid", device.getId().getUid());
         query.setParameter("serial", device.getId().getSerialNumber());
         query.executeUpdate();
         session.delete(device);
         session.getTransaction().commit();
         session.close();
         return "devices.xhtml";
     }
 
     public String removeApp(Blacklistedapplications app) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("delete from Blacklistedapplications where appID = :id");
         query.setParameter("id", app.getAppId());
         query.executeUpdate();
         session.getTransaction().commit();
         session.close();
         return "settings.xhtml";
     }
 
     public List<Settings> getSettings() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Settings");
         settings = query.list();
         return settings;
     }
 
     public List<Scanresults> getAllowedScans() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where accessAllowed = 1");
         allowedScans = query.list();
         return allowedScans;
     }
 
     public List<Scanresults> getDeniedScans() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where accessAllowed = 0");
         deniedScans = query.list();
         return deniedScans;
     }
 
     public String setSpecificSetting(int id) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         specificSetting = (Settings) session.get(Settings.class, id);
         return "specificSetting.xhtml";
     }
 
     public String viewEmployee(int id) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         employee = (Employee) session.get(Employee.class, id);
         return "user.xhtml";
     }
 
     public Settings getSpecificSetting() {
 
         return specificSetting;
     }
 
     public Scanresults getLatestScan(String mac, String serial, String androidID) {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where device_MACAddress = :mac and device_UID = :uid and device_SerialNumber = :serial order by Date desc");
         query.setParameter("mac", mac);
         query.setParameter("uid", androidID);
         query.setParameter("serial", serial);
         Scanresults scan = (Scanresults) query.list().get(0);
         return scan;
 
     }
 
     public int getRootedWeight() {
         getLatestSetting();
         return latestSetting.getRootedWeight();
     }
 
     public int getDebugWeight() {
         getLatestSetting();
         return latestSetting.getDebugWeight();
     }
 
     public int getUnknownSourcesWeight() {
         getLatestSetting();
         return latestSetting.getUnknownSourcesWeight();
     }
 
     public int getOsWeight() {
         getLatestSetting();
         return latestSetting.getOsweight();
     }
 
     public int getLowRiskApp() {
         getLatestSetting();
         return latestSetting.getLowRiskApp();
     }
 
     public int getMediumRiskApp() {
         getLatestSetting();
         return latestSetting.getMediumRiskApp();
     }
 
     public int getHighRiskApp() {
         getLatestSetting();
         return latestSetting.getHighRiskApp();
     }
 
     public int getBlockedApp() {
         getLatestSetting();
         return latestSetting.getBlockedApp();
     }
 
     public int getAccessScore() {
         getLatestSetting();
         return latestSetting.getAccessScore();
     }
 
     public String addSetting() {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Settings setting = new Settings(accessScore, osWeight, rootedWeight, debugWeight, unknownSourcesWeight, lowRiskApp, mediumRiskApp, highRiskApp, blockedApp);
         session.save(setting);
         session.getTransaction().commit();
         session.close();
         return "settings.xhtml";
     }
 }
