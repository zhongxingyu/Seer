 package com.openhr.factories;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import com.openhr.data.EmpPayrollMap;
 import com.openhr.data.EmployeePayroll;
 import com.openhr.data.Payroll;
 import com.openhr.data.PayrollDate;
 import com.openhr.factories.common.OpenHRSessionFactory;
 
 public class PayrollFactory implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static Session session;
 	private static Query query;
 	private static List<PayrollDate> payrollDateList;
 	private static List<EmpPayrollMap> empPayrollMapList;
 	
 	public static List<EmpPayrollMap> findAllEmpPayrollMaps() throws Exception {
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("EmpPayrollMap.findAll");
 		empPayrollMapList = query.list();
 
 		return empPayrollMapList;
 	}
 
 	public static List<EmpPayrollMap> findEmpPayrollMapByEmpPayroll(EmployeePayroll empPayroll) throws Exception {
 		List<EmpPayrollMap> retList = new ArrayList<EmpPayrollMap>();
 		
 		if(empPayrollMapList != null || !empPayrollMapList.isEmpty()) {
 			for(EmpPayrollMap ep: empPayrollMapList) {
 				if(ep.getEmppayId().getId().compareTo(empPayroll.getId()) == 0) {
 					retList.add(ep);
 				}
 			}
 			
 			if(! retList.isEmpty()) {
 				return retList;
 			}
 		}
 		
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("EmpPayrollMap.findByEmpPayrollId");
 		query.setParameter(0, empPayroll);
 		
 		empPayrollMapList = query.list();
 
 		return empPayrollMapList;
 	}
 
 	public static EmpPayrollMap findEmpPayrollMapByEmpPayrollDate(EmployeePayroll empPayroll,
 			Payroll payroll) throws Exception {
 		if(empPayrollMapList != null && !empPayrollMapList.isEmpty()) {
 			for(EmpPayrollMap ep: empPayrollMapList) {
 				if(ep.getEmppayId().getId().compareTo(empPayroll.getId()) == 0
 				&& ep.getPayrollId().getId().compareTo(payroll.getId()) == 0) {
 					return ep;
 				}
 			}
 		}
 		
 		Session lsession = OpenHRSessionFactory.getInstance().openSession();
 		lsession.beginTransaction();
 		query = lsession.getNamedQuery("EmpPayrollMap.findByEmpPayrollIdAndPayrollId");
 		query.setParameter(0, empPayroll);
 		query.setParameter(1, payroll);
 		
 		List<EmpPayrollMap> empPayrollMapList = query.list();
 		lsession.close();
 		
		if(empPayrollMapList.size() > 0) {
			return empPayrollMapList.get(0);
		}
		
		return null;
 	}
 
 	public static List<Payroll> findAllPayrollRuns() throws Exception {
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Payroll.findAll");
 		
 		List<Payroll> payrollList = query.list();
 
 		return payrollList;
 	}
 
 	public static List<Payroll> findPayrollRunByDate(Date runDate) throws Exception {
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("Payroll.findByRunOnDate");
 		query.setParameter(0, runDate);
 		List<Payroll> payrollList = query.list();
 
 		return payrollList;
 	}
 	
 	public static List<PayrollDate> findAllPayrollDate() throws Exception {
 		
 		session = OpenHRSessionFactory.getInstance().getCurrentSession();
 		session.beginTransaction();
 		query = session.getNamedQuery("PayrollDate.findAll");
 
 		payrollDateList = query.list();
 		return payrollDateList;
 	}
 
 	public static void insertPayrollDate(PayrollDate rDate) {
 
 		Session lsession = OpenHRSessionFactory.getInstance().openSession();
 		lsession.beginTransaction();
 		try {
 			lsession.save(rDate);
 			lsession.getTransaction().commit();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		} finally {
 			lsession.close();
 		}
 	}
 
 	public static void insertEmpPayrollMap(EmpPayrollMap empPayMap) {
 		Session lsession = OpenHRSessionFactory.getInstance().openSession();
 		lsession.beginTransaction();
 		try {
 			lsession.save(empPayMap);
 			lsession.getTransaction().commit();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		} finally {
 			lsession.close();
 		}
 	}
 
 	public static void insertPayroll(Payroll payroll) {
 		Session lsession = OpenHRSessionFactory.getInstance().openSession();
 		lsession.beginTransaction();
 		try {
 			lsession.save(payroll);
 			lsession.getTransaction().commit();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		} finally {
 			lsession.close();
 		}
 	}
 }
