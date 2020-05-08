 package org.snowfk.test.sample1;
 
 import java.util.Map;
 
 import org.junit.Test;
 import org.snowfk.test.sample1.modB.Employee;
 import org.snowfk.util.MapUtil;
 import org.snowfk.web.RequestContext;
 import org.snowfk.web.WebModule;
 import org.snowfk.web.db.hibernate.HibernateDaoHelper;
 
 import static org.junit.Assert.*;
 
 public class TestModBEntityBeans extends Sample1TestSupport {
 
     //temporary, until we fix the test for maven.
     @Test
     public void alwaysPass(){}
     /**
      * Test WebMethod param for non primitive types
      */
     @SuppressWarnings("unchecked")
     //@Test
     public void testWebActionObjectWebParam() {
         try {
             Map params = MapUtil.mapIt("employee.firstName", "Mike", "employee.lastName", "Donavan", "employee2.firstName",
                                     "John", "employee2.lastName", "Smith");
             WebModule modB = webApplication.getWebModule("sample1.modB");
             RequestContext rc = new RequestContext(null, (Map<String, Object>) params);
             Employee[] employees = (Employee[]) webApplication.processWebAction(modB.getName(), "autoEmployeesFromParamMap", rc)
                                     .getResult();
             assertNotNull("Employee not created", employees);
             assertEquals("Mike", employees[0].getFirstName());
             assertEquals("Donavan", employees[0].getLastName());
             assertEquals("John", employees[1].getFirstName());
             assertEquals("Smith", employees[1].getLastName());
 
        } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }
 
     @SuppressWarnings({ "unchecked", "deprecation" })
     //@Test
     public void testEmployeeCreateAndDelete() {
         try {
 
             /*--------- Check ModB ---------*/
             WebModule modB = webApplication.getWebModule("sample1.modB");
             assertNotNull(modB);
             assertEquals("entityClasses size", 1, modB.getEntityClasses().length);
             /*--------- /Check ModB ---------*/
 
             HibernateDaoHelper hibernateDaoHelper = appLoader.getHibernateDaoHelper();
 
             //build and process webAction
             Map params = MapUtil.mapIt("firstName", "Jeremy", "username", "jeremychone");
             RequestContext rc = new RequestContext(webApplication.getPart("t:sample1.modB:/home"), (Map<String, Object>) params);
             Employee employee = (Employee) webApplication.processWebAction(modB.getName(), "saveEmployee", rc).getResult();
 
             hibernateDaoHelper.evict(employee);
 
             //check if the Employee has been created
             employee = (Employee) hibernateDaoHelper.get(Employee.class, employee.getId());
             assertNotNull("employee has not bee created", employee);
 
             hibernateDaoHelper.delete(employee);
 
             //closeSessionInView
             hibernateHandler.closeSessionInView();
 
        } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }
 }
