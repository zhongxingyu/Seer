 package com.openhr.factories;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import com.openhr.data.EmpPayTax;
 import com.openhr.data.Employee;
 import com.openhr.data.EmployeePayroll;
 
 import com.openhr.factories.common.OpenHRSessionFactory;
 import com.openhr.taxengine.DeductionType;
 import com.openhr.taxengine.DeductionsDeclared;
 import com.openhr.taxengine.DeductionsDone;
 import com.openhr.taxengine.ExemptionType;
 import com.openhr.taxengine.ExemptionsDone;
 
 public class EmpPayTaxFactroy implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static Session session;
 	private static Query query;
 	private static List<EmpPayTax> employees;
 	private static List<EmployeePayroll> empsum;
 
 	public EmpPayTaxFactroy() {
 	}
 
 	public static List<EmpPayTax> findAll() throws Exception {
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("EmpPayTax.findAll");
 		employees = query.list();
 
 		session.getTransaction().commit();
 
 		return employees;
 	}
 
 	public static List<EmployeePayroll> findPaySummery() 
 
 	{
 
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		try
 		{
 		query = session.getNamedQuery("EmployeePayroll.findAll");
 		empsum = query.list();
 		}
 		
 catch(Exception e)
 {
 	e.printStackTrace();
 }
 		session.getTransaction().commit();
 
 		return empsum;
 	}
 
 	public synchronized static List<EmployeePayroll> findById(Integer employeeId)
 			throws Exception {
 		//Employee e=new Employee();
 		//e.setId(employeeId);
 		session = OpenHRSessionFactory.getInstance().openSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("finde");
 		
 		
 		//query.setParameter(0, e);
 		empsum = query.list();
 
 		session.getTransaction().commit();
         session.close();
 		return empsum;
 	}
 
 	
 	public synchronized static List<ExemptionsDone> exemptionsDone(Integer employeeId)
 			throws Exception {
 		Employee e=new Employee();
 		e.setId(employeeId);
 		session = OpenHRSessionFactory.getInstance().openSession();
 		session.beginTransaction();
		query = session.getNamedQuery("ExemptionsDone.findByEmployeeId");
 		
 		
 		query.setParameter(0, e);
 		 List<ExemptionsDone> empsum1 = query.list();
 
 		session.getTransaction().commit();
         session.close();
 		return empsum1;
 	}
 	
 	public synchronized static List<DeductionsDone> deductionsDone(Integer employeeId)
 			throws Exception {
 		Employee e=new Employee();
 		e.setId(employeeId);
 		session = OpenHRSessionFactory.getInstance().openSession();
 		session.beginTransaction();
		query = session.getNamedQuery("DeductionsDone.findByEmployeeId");
 		
 		
 		query.setParameter(0, e);
 		List<DeductionsDone> empsum2 = query.list();
 
 		session.getTransaction().commit();
         session.close();
 		return empsum2;
 	}
 	
 }
