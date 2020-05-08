 package com.mwr.controller;
 
 import com.mwr.database.Device;
 import com.mwr.database.DeviceId;
 import com.mwr.database.Devicenotregistered;
 import com.mwr.database.Employee;
 import com.mwr.database.HibernateUtil;
 import java.io.Serializable;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 @ManagedBean(name = "fix")
 @SessionScoped
 public class FixWidgetBean implements Serializable {
 
     private Session session;
     private Boolean devExist = false;
     private Boolean empExist = false;
     private String devExists = "";
     private String empExists = "";
     private String message = " ";
     private String empID = null;
     private String devID = null;
     private Employee emp = null;
     private DeviceId devices = null;
     private Device empDev = null;
     private List<Device> empDevList = null;
 
     public FixWidgetBean() {
     }
 
     public void getEmployee() {
         try {
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Employee where empId = :id");
             query.setParameter("id", Integer.parseInt(empID));
             List<Employee> list = query.list();
             emp = list.get(0);
 
         } catch (Exception e) {
             message = empID + " does not exist";
         }
     }
 
     public void RegDevice() {
         try {
 
             session = HibernateUtil.getSessionFactory().openSession();
             session.beginTransaction();
             Query query = session.createQuery("from Device where id = :id");
             query.setParameter("id", devID);
             List<Device> list = query.list();
             Device d = list.get(0);
             if (d == null) {
                 message = empID + " does not exist";
                 return;
             }
             empDev = d;
         } catch (Exception e) {
             message = empID + " does not exist";
         }
     }
 
     public void fixDevice() {
         empExist();
         devExist();
 
         if (devExist && empExist) {
             try {
             } catch (Exception e) {
                 message = "Not possible";
             }
         } else {
             message = "Not possible";
         }
     }
 
    public void getEmployeeRegisterdDevices() {
         emp = null;
         getEmployee();
 
         if (emp == null) {
             System.out.println("No employee");
         } else {
             try {
                 session = HibernateUtil.getSessionFactory().openSession();
                 session.beginTransaction();
                 Query query = session.createQuery("from Scanresult where device_MACAddress = :mac and device_UID = :androidid and device_SerialNumber = :serial order by Date desc");
                 query.setParameter("mac", devices.getMacaddress());
                 query.setParameter("androidid", devices.getAndroidId());
                 query.setParameter("serial", devices.getSerialNumber());
                 empDevList = query.list();
                 query = session.createQuery("from Device where MACAddress = :mac and AndroidID = :androidid and SerialNumber = :serial");
                 query.setParameter("mac", devices.getMacaddress());
                 query.setParameter("androidid", devices.getAndroidId());
                 query.setParameter("serial", devices.getSerialNumber());
                if (!query.list().isEmpty()) {
                    empDev = (Device) query.list().get(0);
                } else {
                    message = empID + " does not have any registered devices!";
                }
             } finally {
                session.close();
             }
         }
     }
 
     public void empExist() {
         getEmployee();
         if (emp == null) {
             empExist = false;
             empExists = empID + " does not exist!";
 
         } else {
             empExist = true;
             empExists = "";
         }
     }
 
     public void showDev(Devicenotregistered v) {
         System.out.println("Device: " + v);
     }
 
     public void devExist() {
 
 
         if (empDev == null) {
             devExist = false;
             devExists = devID + " does not exist!";
 
         } else {
             devExist = true;
             devExists = "";
         }
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public Boolean getDevExist() {
         return devExist;
     }
 
     public void setDevExist(Boolean devExist) {
         this.devExist = devExist;
     }
 
     public Boolean getEmpExist() {
         return empExist;
     }
 
     public void setEmpExist(Boolean empExist) {
         this.empExist = empExist;
     }
 
     public String getDevExists() {
         return devExists;
     }
 
     public void setDevExists(String devExists) {
         this.devExists = devExists;
     }
 
     public String getEmpExists() {
         return empExists;
     }
 
     public void setEmpExists(String empExists) {
         this.empExists = empExists;
     }
 
     public String getEmpID() {
         return empID;
     }
 
     public void setEmpID(String empID) {
         this.empID = empID;
     }
 
     public String getDevID() {
         return devID;
     }
 
     public void setDevID(String devID) {
         this.devID = devID;
     }
 
     public Employee getEmp() {
         return emp;
     }
 
     public void setEmp(Employee emp) {
         this.emp = emp;
     }
 
     public Device getEmpDev() {
         return empDev;
     }
 
     public void setEmpDev(Device empDev) {
         this.empDev = empDev;
     }
 
     public List<Device> getEmpDevList() {
         return empDevList;
     }
 
     public void setEmpDevList(List<Device> empDevList) {
         this.empDevList = empDevList;
     }
 }
