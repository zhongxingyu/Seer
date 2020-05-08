 package com.dbpractice.realestate.dao;
 
 import java.util.List;
 
 import com.dbpractice.realestate.domain.Appartment;
 import com.dbpractice.realestate.domain.Application;
 import com.dbpractice.realestate.domain.Client;
 import com.dbpractice.realestate.domain.District;
 
 public interface ApplicationDAO {
	public Integer createApplication(Application application);
 	public void clearClosed();
 	public int getRequestsCount(District district);
 	public List<Application> getRequest(String status);
 	public void updateApplication(Application application);
 	public void deleteApplication(Application application);
 	public Application getApplicationById(int applicationId);
 }
