 package com.usavich.db.running.dao.impl;
 
 import com.usavich.db.running.dao.def.RunningDAO;
 import com.usavich.entity.common.Experience;
 import com.usavich.entity.running.OnGoingRunning;
 import com.usavich.entity.running.RunningHistory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: LeonLu
  * Date: 6/21/13
  * Time: 6:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RunningDAOImpl implements RunningDAO {
 
     @Autowired
     private RunningMapper runningMapper;
 
     @Override
     public List<RunningHistory> getRunningHistories(Integer userId, Integer missionId) {
         return runningMapper.getRunningHistories(userId, missionId);
     }
 
     @Override
     public List<RunningHistory> getRunningHistoriesByDate(Integer userId, Date lastUpdateTime, int startSize, int pageSize) {
         return runningMapper.getRunningHistoriesByDate(userId, lastUpdateTime, startSize, pageSize);
     }
 
     @Override
     public void createRunningHistory(RunningHistory runningHistory) {
         RunningHistory existingHistory = runningMapper.getRunningHistoryByUuid(runningHistory.getRunUuid());
        if (existingHistory.getUserId() == null) {
             runningMapper.createRunningHistory(runningHistory);
         }
     }
 
     @Override
     public List<OnGoingRunning> getOnGoingRunning(Integer userId, Date lastUpdateTime) {
         return runningMapper.getOnGoingRunning(userId, lastUpdateTime);
     }
 
     @Override
     public void createOnGoingRunning(OnGoingRunning goingRunning) {
         OnGoingRunning existingRunning = runningMapper.getOnGoingRunningByUuid(goingRunning.getRunUuid());
        if (existingRunning.getUserId() == null) {
             runningMapper.createOnGoingRunning(goingRunning);
         }
     }
 
     @Override
     public Experience getExperienceLevel(double experience) {
         return runningMapper.getExperienceLevel(experience);
     }
 }
