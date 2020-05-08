 package com.ffe.estimate.service;
 
 import java.util.List;
import java.util.Map;

import org.jbpm.task.query.TaskSummary;
 
 import com.ffe.common.exception.GTSException;
 import com.ffe.estimate.model.Estimate;
 import com.ffe.process.task.model.EstimateTaskSummary;
 
 /*
  * Adapter class 
  */
 public interface EstimateService {
 
 	public Estimate getEstimate(long estimateID) throws GTSException;
 
 	public List<Estimate> findEstimate(String searchString) throws GTSException;
 
 	public Estimate saveEstimate(Estimate estimate) throws GTSException;
 
 	public void deleteEstimate(Estimate estimate) throws GTSException;
 	
 	
 
 	boolean isEstimatePresent(long physicalEstimateId, String wpn, String estimateName)
 			throws GTSException;
 	public Estimate submitEstimate(Estimate estimate) throws GTSException;
 	public Estimate approveEstimate(Long taskId,String userName,Estimate estimate) throws GTSException;
 	public Estimate rejectEstimate(Long taskId,String userName,Estimate estimate) throws GTSException;
 	public void triggerRegionalApprovalProcess(Estimate estimate)throws GTSException;
 	public List<EstimateTaskSummary> lstEstimateTaskAssginedtoUser(String userName,String language) throws GTSException;
 	public List<EstimateTaskSummary> lstEstimateTaskAssginedtoGroup(String groupNam,String languagee) throws GTSException;
 	public void claimEstimateTask(Long taskId,String userName)throws GTSException;
 	
 }
