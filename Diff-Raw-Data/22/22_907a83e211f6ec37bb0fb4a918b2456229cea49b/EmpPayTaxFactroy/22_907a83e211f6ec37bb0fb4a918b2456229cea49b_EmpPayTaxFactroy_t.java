 package com.openhr.factories;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import com.openhr.data.DeductionsType;
 import com.openhr.data.EmpPayTax;
 import com.openhr.data.Employee;
 import com.openhr.data.EmployeePayroll;
 import com.openhr.data.OverTimePayRateData;
 
 import com.openhr.data.TaxRatesData;
 import com.openhr.data.Users;
 import com.openhr.data.Leave;
 import com.openhr.factories.common.OpenHRSessionFactory;
 import com.openhr.taxengine.DeductionsDone;
 import com.openhr.taxengine.ExemptionsDone;
 
 public class EmpPayTaxFactroy implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static Session session;
 	private static Query query;
 	private static List<EmpPayTax> employees;
 	private static List<EmployeePayroll> empsum;
 
 	public EmpPayTaxFactroy() {
 	}
 
 	// TODO: Need to check this and remove it.
 	public static List<EmpPayTax> findAll() throws Exception {
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		session.evict(employees);
 		query = session.getNamedQuery("EmpPayTax.findAll");
 		employees = query.list();
 
 
 		return employees;
 	}
 	
 	public static List<OverTimePayRateData> getOverTimeRate(OverTimePayRateData ovu)
 
 	{
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("OverTimePayRateData.findById");
 		
 		query.setInteger(0, ovu.getId());
 	List<OverTimePayRateData> lov=query.list();
 	session.getTransaction().commit();
 	return lov;
 
 	}
 	
 	
 	public static boolean updateOverTimeRate(OverTimePayRateData ovu)
 
 	{
 		 boolean done = false;
 	       // List<DeductionsType> matchingName =DeductionFactory.findByNameOverRate(dt.getName());
 	        
 	        session = OpenHRSessionFactory.getInstance().getCurrentSession();
 	        session.beginTransaction();
 
 	        try {
 	        	OverTimePayRateData ovus = null;
 	        	
 	        	if(ovu.getId() != null) {
 	        		ovus = (OverTimePayRateData) session.get(OverTimePayRateData.class, ovu.getId()); ;
 	        	}
 	        	
 	        	
 	        	ovus.setGrossPercent(ovu.getGrossPercent());
 	            
 	          
 	            
 	            
 	           session.update(ovus);
 	            session.getTransaction().commit();
 	            done = true;
 	        } catch (Exception ex) {
 	            ex.printStackTrace();
 	        }finally{
 	        	
 	        }
 	        return done;
 	    }
 
 	
 public static List<OverTimePayRateData> findOverTimeRateAll()
 
 {
 	session = OpenHRSessionFactory.getInstance().getCurrentSession();
 	session.beginTransaction();
 	query = session.getNamedQuery("OverTimePayRateData.findAll");
 List<OverTimePayRateData> lov=query.list();
 session.getTransaction().commit();
 return lov;
 
 }
 	public static List<EmployeePayroll> findAllEmpPayroll() 
 	{
 		Session session1 = OpenHRSessionFactory.getInstance().openSession();
 		session1.beginTransaction();
 		session1.evict(empsum);
 		query = session1.getNamedQuery("EmployeePayroll.findAll");
 		empsum = query.list();
 		session1.getTransaction().commit();
 		session1.close();
 		return empsum;
 	}
 
 	public static EmployeePayroll findEmpPayrollbyEmpID(Employee empID) 
 	{
 		if(System.getProperty("DRYRUN") != null 
 		&& System.getProperty("DRYRUN").equalsIgnoreCase("true")) {
 			EmployeePayroll empP = EmployeePayroll.loadEmpPayroll(empID);
 			return empP;
 		}
 		
 		EmployeePayroll ePayroll = null; 
 		boolean found = false;
 		if(empsum != null && empsum.size() > 0) {
 			for(EmployeePayroll ePay : empsum) {
 				if(ePay.getEmployeeId().getId().equals(empID.getId())) {
 					ePayroll = ePay;
 					found = true;
 					break;
 				}
 			}
 		} 
 		
 		if(!found) {
 			session = OpenHRSessionFactory.getInstance().getCurrentSession();
 			session.beginTransaction();
 			query = session.getNamedQuery("EmployeePayroll.findByEmployeeId");
 			query.setParameter(0, empID);
 			empsum = query.list();
 			ePayroll = empsum.get(0);
 			session.getTransaction().commit();
 		}
 		
 		return ePayroll;
 	}
 
 	public synchronized static List<ExemptionsDone> exemptionsDone(Integer payrollId)
 			throws Exception {
 		EmployeePayroll ePay = new EmployeePayroll();
 		ePay.setId(payrollId);
 		Session session2 = OpenHRSessionFactory.getInstance().openSession();
 		session2.beginTransaction();
 		query = session2.getNamedQuery("ExemptionsDone.findByEmpPayrollId");
 		query.setParameter(0, ePay);
 		List<ExemptionsDone> empsum1 = query.list();
 
 		session2.getTransaction().commit();
         session2.close();
 		return empsum1;
 	}
 	
 	public synchronized static List<DeductionsDone> deductionsDone(Integer payrollId)
 			throws Exception {
 		EmployeePayroll ePay = new EmployeePayroll();
 		ePay.setId(payrollId);
 		Session session1 = OpenHRSessionFactory.getInstance().openSession();
 		session1.beginTransaction();
 		query = session1.getNamedQuery("DeductionsDone.findByEmpPayrollId");
 		query.setParameter(0, ePay);
 		List<DeductionsDone> empsum2 = query.list();
 
 		session1.getTransaction().commit();
         session1.close();
 		return empsum2;
 	}
 
 	public static boolean insertTaxRates(TaxRatesData txr) {
         boolean done = false;
 
         session = OpenHRSessionFactory.getInstance().getCurrentSession();
         session.beginTransaction();
         try {
         	session.save(txr);            
             session.getTransaction().commit();
             done = true;
         } catch (Exception ex) {
             ex.printStackTrace();
         }finally{
         	
         }
         return done;
         
     }
 
 	public static boolean update(EmployeePayroll empPayroll) {
 		boolean done = false;
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		
 		try {
 			EmployeePayroll ePayroll = (EmployeePayroll) session.get(EmployeePayroll.class, empPayroll.getId());
 			ePayroll.setTotalIncome(empPayroll.getTotalIncome());
 			ePayroll.setAccomodationAmount(empPayroll.getAccomodationAmount());
 			ePayroll.setAllowances(empPayroll.getAllowances());
 			ePayroll.setEmployerSS(empPayroll.getEmployerSS());
 			ePayroll.setGrossSalary(empPayroll.getGrossSalary());
 			ePayroll.setNetPay(empPayroll.getNetPay());
 			ePayroll.setTaxableIncome(empPayroll.getTaxableIncome());
 			ePayroll.setTaxAmount(empPayroll.getTaxAmount());
 			ePayroll.setTotalDeductions(empPayroll.getTotalDeductions());
 			ePayroll.setDeductionsDone(empPayroll.getDeductionsDone());
 			ePayroll.setExemptionsDone(empPayroll.getExemptionsDone());
			ePayroll.setOvertimeamt(empPayroll.getOvertimeamt());
			ePayroll.setTaxableOverseasIncome(empPayroll.getTaxableOverseasIncome());
 			
 			session.update(ePayroll);
 			//session.getTransaction().commit();
 			
 			done = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 
 		}
 		return done;
 		
 	}
 	
 }
