 package com.myservices.test.dao;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import com.myservices.test.dao.exceptions.DAOException;
 import com.myservices.test.entities.Employee;
 
 public class EmployeeDAO {
   
  public static final String SOURCE_PATH = "/sources/";
   
   public Employee getEmployee(String name) throws DAOException {
     try {
      InputStream is = new FileInputStream(new File(SOURCE_PATH + name.toLowerCase() + ".properties"));
       Properties props = new Properties();
       props.load(is);
       Employee result = new Employee();
       result.setId(Long.parseLong(props.getProperty("id")));
       result.setName(props.getProperty("name"));
       result.setSurname(props.getProperty("surname"));
       result.setAge(Integer.parseInt(props.getProperty("age")));
       result.setPhoneNumber(props.getProperty("phoneNumber"));
       result.setDescription(props.getProperty("description"));
       return result;
     } catch (NumberFormatException e1) {
       throw new DAOException("Information is broken.");
     } catch (Throwable e) {
       throw new DAOException("No employee with such name found.");
     }
   }
   
   public void addEmployee(Employee empl) throws DAOException {
     if(empl.getName() == null || "".equals(empl.getName()) || empl.getId() == null) {
       throw new DAOException("Information is broken.");
     }
     Properties props = new Properties();
     props.setProperty("id", empl.getId().toString());
     props.setProperty("name", empl.getName());
     props.setProperty("surname", empl.getSurname());
     props.setProperty("age", empl.getAge() != null ? empl.getAge().toString() : "");
     props.setProperty("phoneNumber", empl.getPhoneNumber());
     props.setProperty("description", empl.getDescription());
     
    File file = new File(SOURCE_PATH + empl.getName().toLowerCase() + ".properties");
    file.mkdirs();
     if(file.exists()) {
       file.delete();
     }
     try {
      props.store(new FileOutputStream(SOURCE_PATH + empl.getName().toLowerCase() + ".properties"), null);
     } catch(IOException e) {
       throw new DAOException("Couldn't save: " + e.getMessage());
     }
   }
   
   private static EmployeeDAO instance;
   
   public static EmployeeDAO getInstance() {
     if(instance == null) {
       instance = new EmployeeDAO();
     }
     return instance;
   }
 }
