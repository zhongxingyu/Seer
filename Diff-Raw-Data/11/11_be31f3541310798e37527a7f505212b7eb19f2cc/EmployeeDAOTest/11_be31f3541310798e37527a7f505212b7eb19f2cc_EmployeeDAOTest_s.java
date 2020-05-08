 package com.fs.humanResources.model.employee.dao;
 
 import com.fs.common.BaseUnitTest;
 import com.fs.humanResources.model.employee.entities.Employee;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 
 public class EmployeeDAOTest extends BaseUnitTest {
 
     public static final String COUNT_QUERY = "SELECT count(o) from Employee o ";
     public static final String EMPLOYEE_ID_QUERY = "select employee from Employee employee where employee.employeeId=:thisEmployeeId";
     @InjectMocks
     EmployeeDAO employeeDAO;
 
     @Mock
     EntityManager entityManager;
 
     @Mock
     Query countQuery;
 
     @Mock
     Query employeeIdQuery;
 
     Long expectedCount;
 
     Employee employee;
 
     @Before
     public void setUp() {
         expectedCount = new Long(10l);
         employee = new Employee();
         employee.setId(1234l);
 
         when(entityManager.createQuery(eq(COUNT_QUERY))).thenReturn(countQuery);
         when(countQuery.getSingleResult()).thenReturn(expectedCount);
 
         when(entityManager.createQuery(eq(EMPLOYEE_ID_QUERY))).thenReturn(employeeIdQuery);
         when(employeeIdQuery.getSingleResult()).thenReturn(employee);
     }
 
     @Test
     public void countAll_uses_expected_query() {
         Long actualCount = employeeDAO.countAll();
 
         Assert.assertEquals(expectedCount, actualCount);
 
         verify(entityManager, times(1)).createQuery(eq(COUNT_QUERY));
         verify(countQuery,times(1)).getSingleResult();
     }
 
     @Test
     public void create_persits_expected_employee() {
         Employee employee = new Employee();
         employeeDAO.create(employee);
 
         verify(entityManager, times(1)).persist(eq(employee));
     }
 
     @Test
     public void delete_removes_expected_employee() {
         Employee employee = new Employee();
 
         employeeDAO.delete(employee);
 
         verify(entityManager, times(1)).merge(eq(employee));
         verify(entityManager, times(1)).remove(Matchers.<Employee>anyObject());
     }
 
     @Test
     public void delete_merges_expected_employee() {
         Employee employee = new Employee();
 
         employeeDAO.update(employee);
 
         verify(entityManager, times(1)).merge(eq(employee));
     }
 
     @Test
     public void findById_looksfor_expected_employee() {
         Long employeeId = 1234l;
 
         employeeDAO.findById(employeeId);
 
         verify(entityManager, times(1)).find(any(Class.class),eq(employeeId));
     }
 
     @Test
     public void getEmployeeDetails_returns_expected_employee() {
         Long employeeId = 1234l;
 
         Employee actualEmployee = employeeDAO.getEmployeeDetails(employeeId);
         Assert.assertEquals(employee.getId(), actualEmployee.getId());
 
         verify(entityManager, times(1)).createQuery(EMPLOYEE_ID_QUERY);
         verify(employeeIdQuery,times(1)).getSingleResult();
     }
 
 }
