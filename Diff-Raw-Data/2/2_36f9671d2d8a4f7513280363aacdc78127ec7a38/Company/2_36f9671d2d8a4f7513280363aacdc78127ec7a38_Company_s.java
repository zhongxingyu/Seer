 /**
  * Ian Dimayuga
  * EECS293 HW1
  */
 package edu.cwru.icd3;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * @author ian A data structure to represent a strictly hierarchical organization of employees.
  */
 public class Company {
 	/**
 	 * Map of employee names to manager names. Each employee has one manager, with the exception of the CEO.
 	 */
 	private Map<String, String> m_managerMap;
 
 	/**
 	 * Map of employee names to the set of direct subordinates.
 	 */
 	private Map<String, Set<String>> m_directReportMap;
 
 	/**
 	 * Creates an empty Company with no employees.
 	 */
 	public Company() {
 		m_managerMap = new HashMap<String, String>();
 		m_directReportMap = new HashMap<String, Set<String>>();
 	}
 
 	/**
 	 * Adds an employee to the company, specifying their manager.
 	 * 
 	 * @param employee
 	 *            Name of the employee to be added.
 	 * @param manager
 	 *            Name of an existing employee to be the manager of the new employee. A manager of null is used if and
 	 *            only if the Company is currently empty, and the new employee will be the CEO.
 	 */
 	public void add(String employee, String manager) {
 		// Null-check employee
 		if (employee == null) {
 			throw new NullPointerException("employee cannot be null");
 		}
 		if (manager == null) {
 			// If manager is null, but the Company isn't empty, throw exception
 			if (m_directReportMap.size() > 0) {
 				throw new IllegalArgumentException("manager is null, but Company is not empty.");
 			}
 		} else if (!m_directReportMap.containsKey(manager)) {
 			// If manager doesn't exist in the Company, throw exception
 			// This includes the case where the Company is empty but a manager was specified
 			throw new NoSuchElementException(String.format("Company has no employee with name %s.", manager));
 		}
 		// If employee already exists in the Company, throw exception
 		if (m_managerMap.containsKey(employee)) {
 			throw new IllegalArgumentException(
 					String.format("Company already contains an employee named %s.", employee));
 		}
 		// Add employee to Company, and to manager's set of direct reports if manager exists
 		if (manager != null) {
 			m_directReportMap.get(manager).add(employee);
 		}
		m_directReportMap.put(employee, new HashSet());
 		m_managerMap.put(employee, manager);
 	}
 }
