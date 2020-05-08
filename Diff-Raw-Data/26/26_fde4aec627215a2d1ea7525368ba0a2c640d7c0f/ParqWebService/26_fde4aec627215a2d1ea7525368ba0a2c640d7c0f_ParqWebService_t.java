 package com.parq.web.service;
 
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
 
 public interface ParqWebService {
 
 	WebUser validateUser(WebUser userToValidate);
 	
 	List<WebPaymentAccount> getPaymentAccountForUser(WebUser user);
 	
 	List<ParkingHistory> getParkingHistoryForUser(WebUser user, ReportDateRangeFilter dateFilter);
 	
 	boolean changePasswordForUser(WebUser user, PasswordChangeRequest passwordChange);
 	
 	List<ParkingSpaceStatus> getParkingStatusByClient(long clientId, String locationIdentifier);
 	
 	List<ParkingReport> getParkingReportByClientId(long clientId, ReportDateRangeFilter dateFilter);
 	
 	boolean registerNewUser(UserRegistration registration);
 	
 	List<WebParkingLocation> findParkingLocations(MapLocation centerOfMap);
 	
 	List<String> getParkingLocationIdentifierListForClient(long clientId);
 	
	WebClient getClientByAdminId(long adminId);
 }
