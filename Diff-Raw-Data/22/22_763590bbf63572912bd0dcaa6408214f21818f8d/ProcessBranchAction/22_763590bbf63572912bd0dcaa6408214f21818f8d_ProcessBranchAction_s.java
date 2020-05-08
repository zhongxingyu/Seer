 package com.openhr.company;
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import com.openhr.common.PayhumConstants;
 import com.openhr.data.ConfigData;
 import com.openhr.data.EmpPayrollMap;
 import com.openhr.data.Employee;
 import com.openhr.data.EmployeeBonus;
 import com.openhr.data.EmployeePayroll;
 import com.openhr.data.Payroll;
 import com.openhr.data.PayrollDate;
 import com.openhr.data.Users;
 import com.openhr.factories.CompanyFactory;
 import com.openhr.factories.ConfigDataFactory;
 import com.openhr.factories.EmpPayTaxFactroy;
 import com.openhr.factories.EmployeeFactory;
 import com.openhr.factories.LicenseFactory;
 import com.openhr.factories.PayrollFactory;
 import com.openhr.taxengine.TaxEngine;
 
 public class ProcessBranchAction extends Action{
 
 	@Override
 	public ActionForward execute(ActionMapping map, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		
 		BufferedReader bf = request.getReader();
 		StringBuffer sb = new StringBuffer();
 		String line = null;
 		while ((line = bf.readLine()) != null) {
 			sb.append(line);
 		}
 		JSONObject json = JSONObject.fromObject(sb.toString());
 		int branchId   		= json.getInt("branchId");
 		
		ConfigData config = ConfigDataFactory.findByName(PayhumConstants.PROCESS_BRANCH);
		config.setConfigValue(Integer.toString(branchId));
 		ConfigDataFactory.update(config);
  		
  		int a[] = {0};
  		Calendar currDtCal = Calendar.getInstance();
 
 	    // Zero out the hour, minute, second, and millisecond
 	    currDtCal.set(Calendar.HOUR_OF_DAY, 0);
 	    currDtCal.set(Calendar.MINUTE, 0);
 	    currDtCal.set(Calendar.SECOND, 0);
 	    currDtCal.set(Calendar.MILLISECOND, 0);
 
 	    Date now = currDtCal.getTime();
 	    
 		List<Company> comps = CompanyFactory.findAll();
 		Company comp = null;
 		for(Company cp: comps) {
 			if (!PayhumConstants.MASTER_COMP.equalsIgnoreCase(cp.getName())){
 				comp = cp;
 				break;
 			}
 		}
 		
 		String compName = comp.getName();
 		compName = compName.replace(" ", "_");
 		
 		// Check for licenses
 		List<Licenses> compLicenses = LicenseFactory.findByCompanyId(comp.getId());
 		if(compLicenses.isEmpty()) {
 			//throw new Exception("No License available.");
 			a[0] = 1;
 		}
 		for(Licenses lis : compLicenses) {
 			if(lis.getActive() == 1) {
 				Date endDate = lis.getTodate();
 				if( ! isLicenseActive(now, endDate)) {
 					// License has expired and throw an error
 					//throw new Exception("License has expired");
 					a[0] = 2;
 					break;
 				} else {
 					// License end date is valid, so lets check the key.
 					String licenseKeyStr = LicenseValidator.formStringToEncrypt(compName, endDate);
 					if(LicenseValidator.encryptAndCompare(licenseKeyStr, lis.getLicensekey())) {
 						// License key is valid, so proceed.
 						break;
 					} else {
 						//throw new Exception("License is tampered. Contact Support.");
 						a[0] = 3;
 						break;
 					}
 				}
 			}
 		}
 		
 		if(a[0] == 0) {
 			// If the payroll run dates is empty, it means this is first time, so lets populate.
 			List<PayrollDate> payrollDates = populatePayrollDates();
 
 			int returnRes = getTobeProcessedDate(payrollDates);
 			
 			if(returnRes == 1) {
 				// adhoc
 				// Check if there is any record to proceed.
 				a[0] = checkIfAdhocIsPossible(now);
 			} else if (returnRes == 2) {
 				a[0] = 5;
 			}
 		}
 		
 		JSONArray result = JSONArray.fromObject(a);
     	response.setContentType("application/json; charset=utf-8");
 		PrintWriter out = response.getWriter();
 		if (result == null) {
 			out.print("");
 		} else {
 			out.print(result.toString());
 		}
 		out.flush();
 		
 		return null;
 	}
 	
 	private int checkIfAdhocIsPossible(Date now) throws Exception {
 		
 		Calendar salaryProcessDate = Calendar.getInstance();
 		salaryProcessDate.setTime(now);
 		List<Employee> activeEmpList = new ArrayList<Employee>();
 		List<Employee> inActiveEmpList = new ArrayList<Employee>();
 		
 		// License validation is done and there is a active license, lets proceed and run the tax engine
 		// to compute the payroll for the current pay period
 		//TODO: Get dept ID
 		Integer deptId = 0; // Means All depts
 		
 		ConfigData config = ConfigDataFactory.findByName(PayhumConstants.PROCESS_BRANCH); 
 		Integer branchId = Integer.parseInt(config.getConfigValue());
 		
 		if(branchId == 0) {
 			// Employees of all Branches
 			activeEmpList = EmployeeFactory.findAllActive();
 		} else {
 			if(deptId == 0) {
 				// Employees of all depts of a given branch.
 				activeEmpList = EmployeeFactory.findAllActiveByBranch(branchId);
 			} else {
 				// Particular dept of a particular branch
 				activeEmpList = EmployeeFactory.findActiveByDeptID(deptId);
 			}
 		}
 		
 		int count = 0;
 		for(Employee emp: activeEmpList) {
 			EmployeePayroll empPayroll = EmpPayTaxFactroy.findEmpPayrollbyEmpID(emp);
 			
 			EmployeeBonus latestBonus = TaxEngine.getLatestBonus(empPayroll, salaryProcessDate);
 			
 			if(latestBonus != null) {
 				count++;
 			}
 		}
 		
 		return count > 0 ? 4 : 6;
 	}
 
 	private boolean isLicenseActive(Date currentDate, Date endDate) {
 	    if (currentDate.after(endDate)) {
 	    	// License has expired
 	    	return false;
 	    }
 	    
 	    return true;
 	}
 	
 	private int getTobeProcessedDate(List<PayrollDate> payDates) throws Exception {
 		List<Payroll> payRuns = PayrollFactory.findAllPayrollRuns();
 		
 		for(PayrollDate payDate: payDates) {
 			boolean processed = false;
 			for(Payroll run: payRuns) {
 				if(run.getPayDateId() == payDate) {
 					processed = true;
 					break;
 				}
 			}
 			
 			if(!processed) {
 				// If this date is post the current date, but its not processed, lets process it.
 				Date currDate = new Date();
 				if(currDate.after(payDate.getRunDate())
 				|| currDate.equals(payDate.getRunDate())) {
 					return 0;
 				} else {
 					// Current date is before the next processing date, so its an adhoc
 					//throw new Exception("All payroll dates are processed, should be an adhoc one.");
 					return 1;
 				}
 			}
 		}
 		
 		return 2;
 	}
 	
 	private List<PayrollDate> populatePayrollDates() throws Exception {
 		// Check if the payroll dates are calculated or not, if not default to Monthly and choose last friday of
 		// each month as payroll date.
 		List<PayrollDate> payrollDates = PayrollFactory.findAllPayrollDate();
 		if(payrollDates == null || payrollDates.isEmpty()) {
 			Calendar currCal = Calendar.getInstance();
 			int currMonth = currCal.get(Calendar.MONTH);
 			int currYear = currCal.get(Calendar.YEAR);
 			
 			if(currMonth >= 3) {
 				// Populate from current month to december
 				for(int i = currMonth; i < 12; i++) {
 					Date rDate = getLastFriday(i, currYear);
 					PayrollDate payDate = new PayrollDate();
 					payDate.setRunDate(rDate);
 					PayrollFactory.insertPayrollDate(payDate);
 					
 					payrollDates.add(payDate);
 				}
 				
 				// Populate from current month to march
 				for(int i = 0; i < 3; i++) {
 					Date rDate = getLastFriday(i, currYear+1);
 					PayrollDate payDate = new PayrollDate();
 					payDate.setRunDate(rDate);
 					PayrollFactory.insertPayrollDate(payDate);
 					
 					payrollDates.add(payDate);
 				}
 				
 			} else {
 				for(int i = 0; i <= currMonth; i++) {
 					Date rDate = getLastFriday(i, currYear + 1);
 					PayrollDate payDate = new PayrollDate();
 					payDate.setRunDate(rDate);
 					PayrollFactory.insertPayrollDate(payDate);
 					
 					payrollDates.add(payDate);
 				}
 			}
 		}
 		
 		return payrollDates;
 	}
 	
 	private Date getLastFriday( int month, int year ) {
 		Calendar retCal = Calendar.getInstance();
 		retCal.set(Calendar.YEAR, year);
 		retCal.set(Calendar.MONTH, month);
 		retCal.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
 		retCal.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
 		return retCal.getTime();
 	}
 }
