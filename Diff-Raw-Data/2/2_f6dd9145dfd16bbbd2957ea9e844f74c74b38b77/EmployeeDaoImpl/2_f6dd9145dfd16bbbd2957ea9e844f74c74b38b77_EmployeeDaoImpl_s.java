 /**************************************************************************
  * Copyright (c) 2013 2359 Media Pvt Ltd
  *
  * NOTICE:  All information contained herein is, and remains the 
  * property of 2359 Media Pvt Ltd and its suppliers, if any. 
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden unless prior written permission is obtained
  * from 2359 Media Pvt Ltd
  ***************************************************************************/
 package com.media2359.euphoria.dao.employee;
 /**
  * EmployeeDaoImpl
  *
  * TODO Write something about this class
  * 
  * @author Praveen
  * @version 1.0 2013
  **/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.media2359.euphoria.model.employee.Employee;
 
 
 @Repository
 @Transactional(readOnly = true)
 public class EmployeeDaoImpl extends HibernateDaoSupport implements EmployeeDao {
 	private final Logger log = Logger.getLogger(EmployeeDaoImpl.class);
 	
 	@Autowired
 	public EmployeeDaoImpl(SessionFactory sessionFactory) {
 		setSessionFactory(sessionFactory);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Employee> getAllEmployees() {
 		 List<Employee> employeeList = new ArrayList<Employee>();
 		 
 		 Employee emp1  = new Employee();
 		 emp1.setName("Alfred");
 		 emp1.setCompanyEmail("alfred@companyemail.com");
 		 emp1.setEmploymentType("Permenant");
 		 emp1.setDesignation("System Architect");
 		 emp1.setPlatForms("Rails,iOS,HTML,Android");
 		 emp1.setMobile("99999988");
 		 emp1.setPersonalEmail("alfred@personalemail.com");
 		 employeeList.add(emp1);
 		 
 		 Employee emp2  = new Employee();
 		 emp2.setName("Lung Sen");
 		 emp2.setCompanyEmail("lungsen@companyemail.com");
 		 emp2.setEmploymentType("Hourly");
 		 emp2.setDesignation("Developer");
 		 emp2.setPlatForms("HTML,Android");
 		 emp2.setMobile("99998988");
 		 emp2.setPersonalEmail("lungsen@personalemail.com");
 		 employeeList.add(emp2);
 		 
 		 Employee emp3  = new Employee();
 		 emp3.setName("May");
 		 emp3.setCompanyEmail("may@companyemail.com");
 		 emp3.setEmploymentType("Conntract");
 		 emp3.setDesignation("Developer");
 		 emp3.setPlatForms("Rails,iOS");
 		 emp3.setMobile("99898988");
 		 emp3.setPersonalEmail("may@personalemail.com");
 		 employeeList.add(emp3);
 		 
 		 Employee emp4  = new Employee();
 		 emp4.setName("TY");
 		 emp4.setCompanyEmail("may@companyemail.com");
 		 emp4.setEmploymentType("Permenant");
 		 emp4.setDesignation("Developer");
 		 emp4.setPlatForms("HTML,iOS");
 		 emp4.setMobile("99898988");
 		 emp4.setPersonalEmail("may@personalemail.com");
 		 employeeList.add(emp4);
 		 
 		 Employee emp5  = new Employee();
 		 emp5.setName("Praveen");
 		 emp5.setCompanyEmail("praveen@companyemail.com");
 		 emp5.setEmploymentType("Hourly");
 		 emp5.setDesignation("Developer");
 		 emp5.setPlatForms("HTML,iOS");
 		 emp5.setMobile("98898988");
 		 emp5.setPersonalEmail("praveen@personalemail.com");
 		 employeeList.add(emp5);
 		 
 		 Employee emp6  = new Employee();
 		 emp6.setName("Shiv");
 		 emp6.setCompanyEmail("shiv@companyemail.com");
 		 emp6.setEmploymentType("Permenant");
		 emp6.setDesignation("PM/Post Man");
 		 emp6.setPlatForms("Rails,iOS,HTML,Android");
 		 emp6.setMobile("98888988");
 		 emp6.setPersonalEmail("shiv@personalemail.com");
 		 employeeList.add(emp6);
 		 
 		 return employeeList;
 	}
 }
