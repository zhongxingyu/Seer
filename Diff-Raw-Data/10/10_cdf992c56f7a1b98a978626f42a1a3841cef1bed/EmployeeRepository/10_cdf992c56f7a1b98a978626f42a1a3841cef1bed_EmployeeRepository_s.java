 package com.trifork.hr.persistence;
 
 import com.trifork.hr.model.Employee;
 
 import javax.persistence.EntityNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Simple repository for Employees.
  */
 public class EmployeeRepository {
 	private static List<Employee> db = new ArrayList<Employee>();
 
 	static {
 		EmployeeRepository.save(new Employee("Test 1"));
 		EmployeeRepository.save(new Employee("Test 2"));
 	}
 
 	public static Employee save(Employee empl) {
 		if (empl.getId() == 0) {
 			empl.setId(findMaxId() + 1);
 		} else {
 			Employee existingEmpl = get(empl.getId());
 			db.remove(existingEmpl);
 		}
 
 		db.add(empl);
 
 
 
 		return empl;
 	}
 
 	private static long findMaxId() {
 		long maxFound = 0;
 		for(Employee emp: db) {
 			if (emp.getId() > maxFound) {
 				maxFound = emp.getId();
 			}
 		}
 
 		return maxFound;
 	}
 
 	/**
	 * @throw EntitytNotFoundException if the Employee indicated by parameter <code>id</code> does not exist
 	 */
 	public static Employee get(long id) {
 		Employee employee = null;
 
 		for(Employee emp: db) {
 			if (emp.getId() == id) {
 				employee = emp;
 			}
 		}
 
 		if (employee == null) throw new EntityNotFoundException("" + id);
 		return employee;
 	}
 
	@SuppressWarnings({"unchecked", "UnusedDeclaration"})
 	public static void remove(long id) {
 		Employee employee = null;
 
 		for(Employee emp: db) {
 			if (emp.getId() == id) {
 				employee = emp;
 			}
 		}
 
 		if (employee != null) {
 			db.remove(employee);
 		}
 	}
 
	@SuppressWarnings({"unchecked", "UnusedDeclaration"})
 	public static List<Employee> listAll() {
 		return db;
 	}
 }
 
