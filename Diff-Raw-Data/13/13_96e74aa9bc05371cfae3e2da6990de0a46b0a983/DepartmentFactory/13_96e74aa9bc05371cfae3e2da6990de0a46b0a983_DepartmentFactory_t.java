 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.openhr.factories;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
import com.openhr.data.Branch;
 import com.openhr.data.Department;
 import com.openhr.factories.common.OpenHRSessionFactory;
 
 /**
  * 
  * @author Venkat
  */
 public class DepartmentFactory implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static Session session;
 	private static Query query;
 	private static List<Department> comps;
 
 	public DepartmentFactory() {
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Department> findAll() throws Exception{
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Department.findAll");
 		comps = query.list();
 		session.getTransaction().commit();
  		return comps;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Department> findById(Integer branchId) throws Exception{
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Department.findById");
 		query.setInteger(0, branchId);
 		comps = query.list();
 		session.getTransaction().commit();
 
 		return comps;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Department> findByBranchId(Integer branchId) throws Exception{
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Department.findByBranchId");
		Branch branch = new Branch();
		branch.setId(branchId);
		query.setParameter(0, branch);
 		comps = query.list();
 		session.getTransaction().commit();
 
 		return comps;
 	}
 	
 //	@SuppressWarnings("unchecked")
 //	public static List<Department> findByBranchId(String deptName) throws Exception{
 //		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 //		session.beginTransaction();
 //		query = session.getNamedQuery("Department.findName");
 //		query.setString(0, deptName);
 //		comps = query.list();
 //		session.getTransaction().commit();
 //
 //		return comps;
 //	}
 	
 	@SuppressWarnings("unchecked")
 	public static Integer findDepartmentLastId() throws Exception{
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Department.findLastId");
 		List<Department> lastId = query.list();
  		session.getTransaction().commit();
 		if (lastId.size() != 0) {
 			return lastId.get(0).getId();
 		} else {
 			return 0;
 		}
 	}
 
 	
 	 
 		
 
 	public static boolean delete(Department e) throws Exception{
 		boolean done = false;
 		Session session1 = OpenHRSessionFactory.getInstance().openSession();
 		session1.beginTransaction();
 		Department comp = (Department) session1.get(Department.class, e.getId());
 		session1.delete(comp);
 		session1.getTransaction().commit();
 		session1.flush();
 		done = true;
 
 		return done;
 	}
 
 	public static boolean insert(Department e) throws Exception {
 		boolean done = false;
  		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
  		session.save(e);
 		session.getTransaction().commit();
 		done = true;
  		return done;
  	}
 	
 
 	public static boolean update(Department e) throws Exception {
 		boolean done = false;
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 
 		// System.out.println("ALREADY IN SESSION : "+session.contains(session.get(Employee.class,
 		// e.getId())));
 
 		Department comp = (Department) session.get(Department.class, e.getId());
 
 		comp.setDeptname(e.getDeptname());
 		comp.setBranchId(e.getBranchId());
 		
 		session.update(comp);
 		session.getTransaction().commit();
 		
 		done = true;
 
 		return done;
 	}
 
 }
