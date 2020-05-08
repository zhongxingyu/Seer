 package com.parq.web.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.parq.web.PasswordChangeRequest;
 import com.parq.web.ReportDateRangeFilter;
 import com.parq.web.model.MapLocation;
 import com.parq.web.model.ParkingHistory;
 import com.parq.web.model.ParkingReport;
 import com.parq.web.model.ParkingSpaceStatus;
 import com.parq.web.model.UserRegistration;
 import com.parq.web.model.WebClient;
 import com.parq.web.model.WebParkingLocation;
 import com.parq.web.model.WebPaymentAccount;
 import com.parq.web.model.WebUser;
 
 public class MockParqWebService implements ParqWebService{
 
 	@Override
 	public WebUser validateUser(WebUser userToValidate) {
 		
 		if (userToValidate.getPassword().equalsIgnoreCase("password")) {
 			userToValidate.setAuthenticated(true);
 			userToValidate.setAdminUser(false);
 		}
 		else if (userToValidate.getPassword().equalsIgnoreCase("admin")) {
 			userToValidate.setAuthenticated(true);
 			userToValidate.setAdminUser(true);
 		}
 		else {
 			userToValidate.setAuthenticated(false);
 			userToValidate.setAdminUser(false);
 			userToValidate.setLoginFailed(true);
 		}
 		return userToValidate;
 	}
 	
 	@Override
 	public List<WebPaymentAccount> getPaymentAccountForUser(WebUser user) {
 		List<WebPaymentAccount> accs = new ArrayList<WebPaymentAccount>();
 		
 		WebPaymentAccount account1 = new WebPaymentAccount();
 		account1.setCreditCardNumber("1234567890121234");
 		account1.setType("VISA");
 		account1.setDefaultPayment(true);
 		
 		WebPaymentAccount account2 = new WebPaymentAccount();
 		account2.setCreditCardNumber("9876543210128976");
 		account2.setType("MASTER CARD");
 		
 		accs.add(account1);
 		accs.add(account2);
 		
 		return accs;
 	}
 	
 	@Override
 	public List<ParkingHistory> getParkingHistoryForUser(WebUser user, ReportDateRangeFilter dateRangeFilter) {
 		List<ParkingHistory> pHis = new ArrayList<ParkingHistory>();
 		
 		if (dateRangeFilter.getDateRangeFilter() == null 
 				|| ReportDateRangeFilter.DateRange.NONE.equals(dateRangeFilter.getDateRangeFilter())) {
 			return pHis;
 		}
 		
 		ParkingHistory his1 = new ParkingHistory();
 		his1.setCost(1.25);
 		his1.setDate("Nov-13-2011");
 		his1.setEndTime("5:00 pm");
 		his1.setStartTime("4:15 pm");
 		his1.setLocationName("MIT Student Lot");
 		his1.setParkingRefNumber("MIT1001-01");
 		
 		ParkingHistory his2 = new ParkingHistory();
 		his2.setCost(4.25);
 		his2.setDate("Nov-15-2011");
 		his2.setEndTime("5:00 pm");
 		his2.setStartTime("2:15 pm");
 		his2.setLocationName("MIT Student Lot");
 		his2.setParkingRefNumber("MIT1001-02");
 		
 		ParkingHistory his3 = new ParkingHistory();
 		his3.setCost(8.35);
 		his3.setDate("Nov-16-2011");
 		his3.setEndTime("5:00 pm");
 		his3.setStartTime("1:00 pm");
 		his3.setLocationName("MIT Student Lot");
 		his3.setParkingRefNumber("MIT1001-03");
 		
 		pHis.add(his1);
 		pHis.add(his2);
 		pHis.add(his3);
 		
 		return pHis;
 	}
 	
 	@Override
 	public boolean changePasswordForUser(WebUser user, PasswordChangeRequest passwordChange) {
 		boolean changeSuccessful = Math.random() > 0.5;
 		
 		passwordChange.setInvalidPassword(!changeSuccessful);
 		passwordChange.setWrongOldPassword(!changeSuccessful);
 		passwordChange.setNewPasswordNotMatch(!changeSuccessful);
 		
 		return changeSuccessful;
 	}
 	
 	@Override
 	public List<ParkingSpaceStatus> getParkingStatusByClient(long clientId, String locationIdentifier) {
 		List<ParkingSpaceStatus> pStatus = new ArrayList<ParkingSpaceStatus>();
 		
 		ParkingSpaceStatus status1 = new ParkingSpaceStatus();
 		status1.setOccupied(true);
 		status1.setParkingLevel("1");
 		status1.setSpaceId(2345);
 		status1.setSpaceLocation("MIT");
 		status1.setSpaceName("MIT-1001");
 		
 		ParkingSpaceStatus status2 = new ParkingSpaceStatus();
 		status2.setOccupied(true);
 		status2.setParkingLevel("1");
 		status2.setSpaceId(2484);
 		status2.setSpaceLocation("MIT");
 		status2.setSpaceName("MIT-1002");
 		
 		ParkingSpaceStatus status3 = new ParkingSpaceStatus();
 		status3.setOccupied(false);
 		status3.setParkingLevel("1");
 		status3.setSpaceId(2115);
 		status3.setSpaceLocation("MIT");
 		status3.setSpaceName("MIT-1003");
 		
 		pStatus.add(status1);
 		pStatus.add(status2);
 		pStatus.add(status3);
 		
 		return pStatus;
 	}
 	
 	@Override
 	public List<ParkingReport> getParkingReportByClientId(long clientId, ReportDateRangeFilter dateFilter) {
 		List<ParkingReport> reports = new ArrayList<ParkingReport>();
 		
 		if (dateFilter.getDateRangeFilter() == null 
 				|| ReportDateRangeFilter.DateRange.NONE.equals(dateFilter.getDateRangeFilter())) {
 			return reports;
 		}
 		
 		ParkingReport report1 = new ParkingReport();
 		report1.setAmountPaid(1.25);
 		report1.setParkingRefNum("MIT1001-01");
 		report1.setPaymentDatetime("Nov-13-2011");
 		report1.setUserEmail("TestUser@Parq.com");
 		
 		ParkingReport report2 = new ParkingReport();
 		report2.setAmountPaid(4.25);
 		report2.setParkingRefNum("MIT1001-02");
 		report2.setPaymentDatetime("Nov-15-2011");
 		report2.setUserEmail("TestUser@Parq.com");
 		
 		ParkingReport report3 = new ParkingReport();
 		report3.setAmountPaid(8.35);
 		report3.setParkingRefNum("MIT1001-03");
 		report3.setPaymentDatetime("Nov-16-2011");
 		report3.setUserEmail("TestUser@Parq.com");
 		
 		reports.add(report1);
 		reports.add(report2);
 		reports.add(report3);
 		
 		return reports;
 	}
 	
 	@Override
 	public boolean registerNewUser(UserRegistration registration) {
 		boolean registrationSuccessful = Math.random() > 0.5;
 		
 		registration.setInvalidPassword(!registrationSuccessful);
 		registration.setPasswordDoesNotMatch(!registrationSuccessful);
 		registration.setEmailAlreadyExist(!registrationSuccessful);
 		
 		return registrationSuccessful;
 	}
 
 	@Override
 	public List<WebParkingLocation> findParkingLocations(MapLocation centerOfMap) {
 		return Collections.emptyList();
 	}
 
 	@Override
 	public List<String> getParkingLocationIdentifierListForClient(long clientId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public WebClient getClientByAdminId(long userId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
