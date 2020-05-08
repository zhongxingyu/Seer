 package com.usavich.service.plan.impl;
 
 import com.usavich.common.exception.ErrorMessageMapper;
 import com.usavich.common.exception.ServerRequestException;
 import com.usavich.common.lib.Callable;
 import com.usavich.db.common.dao.def.CommonDAO;
 import com.usavich.db.plan.dao.def.PlanDAO;
 import com.usavich.entity.common.IDGeneration;
 import com.usavich.entity.enums.*;
 import com.usavich.entity.mission.Mission;
 import com.usavich.entity.plan.*;
 import com.usavich.service.Cache.CacheFacade;
 import com.usavich.service.backend.BackendJobCache;
 import com.usavich.service.mission.def.MissionService;
 import com.usavich.service.plan.def.PlanService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: p
  * Date: 13-11-12
  * Time: 上午9:42
  * To change this template use File | Settings | File Templates.
  */
 public class PlanServiceImpl implements PlanService {
 
     @Autowired
     private PlanDAO planDAO;
 
     @Autowired
     private CommonDAO commonDAO;
 
     @Autowired
     private MissionService missionService;
 
     @Override
     public List<Plan> getPlansForRest(Integer pageNo) {
         if (BackendJobCache.first100Plan.size() == 0) {
             getPlanByPageNo(0, 100);
         }
         List<Plan> returnedPlan = new ArrayList<Plan>();
         Integer from = pageNo == null ? 0 : pageNo * 10;
         if (from < BackendJobCache.first100Plan.size()) {
            for (int i = from; i < 10; i++) {
                 if (BackendJobCache.first100Plan.size() > i) {
                     returnedPlan.add(BackendJobCache.first100Plan.get(i));
                 }
             }
         }
         return returnedPlan;
     }
 
     @Override
     public List<Plan> getPlanByPageNo(Integer pageNo, Integer pageSize) {
         Integer from = pageNo == null ? 0 : pageNo * 10;
         return planDAO.getPlansByPage(from, pageSize);
     }
 
     @Override
     public Plan getPlan(final Integer planId, final Date lastUpdateTime) {
         String key = "plan.id." + planId.toString();
 
         Plan plan = CacheFacade.PLAN.get(key, new Callable<Plan>() {
             @Override
             public Plan execute() {
                 Plan plan = planDAO.getPlan(planId, null);
                 plan.setMissions(missionService.getMissionsByPlanId(planId));
                 return plan;
             }
         });
 
         if (lastUpdateTime != null) {
             if (plan.getLastUpdateTime().before(lastUpdateTime)) {
                 return null;
             }
         }
         return plan;
     }
 
     @Override
     public List<PlanCollect> getPlanCollection(Integer userId, Date lastUpdateTime) {
         return planDAO.getPlanCollection(userId, lastUpdateTime);
     }
 
     @Override
     @Transactional
     public void updateUserCollects(Integer userId, List<PlanCollect> planCollects) {
         for (int i = 0; i < planCollects.size(); i++) {
             PlanCollect planCollect = planCollects.get(i);
             planCollect.setCollectTime(new Date());
             planDAO.createUserCollect(userId, planCollects.get(i));
         }
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunHistory(Integer userId, Date lastUpdateTime) {
         return planDAO.getPlanRunHistory(userId, lastUpdateTime);
     }
 
     @Override
     @Transactional
     public void updateRunningHistory(Integer userId, List<PlanRunHistory> planHistoryList) {
         for (PlanRunHistory planHistory : planHistoryList) {
             if (planHistory.getOperate() == OperateEnum.Insert.ordinal()) {
                 planHistory.setHistoryStatus(HistoryStatusEnum.Execute.ordinal());
                 planDAO.createPlanRunning(userId, planHistory);
             } else if (planHistory.getOperate() == OperateEnum.Update.ordinal()) {
                 if (planHistory.getRemainingMissions() == 0) {
                     planHistory.setHistoryStatus(HistoryStatusEnum.Finished.ordinal());
                     planHistory.setNextMissionId(null);
                 }
                 planDAO.updatePlanRunning(userId, planHistory);
             } else if (planHistory.getOperate() == OperateEnum.Delete.ordinal()) {
                 if (planHistory.getRemainingMissions() == 0) {
                     planHistory.setHistoryStatus(HistoryStatusEnum.Cancled.ordinal());
                 }
                 planDAO.updatePlanRunning(userId, planHistory);
             }
         }
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunningByPlanId(Integer planId, Integer pageNo) {
         Integer from = pageNo == null ? 0 : pageNo * 10;
         Integer pageSize = 10;
         return planDAO.getPlanRunningByPlanId(planId, from, pageSize);
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunningByUserId(Integer userId, Integer pageNo) {
         Integer from = pageNo == null ? 0 : pageNo * 10;
         Integer pageSize = 10;
         return planDAO.getPlanRunningByUserId(userId, from, pageSize);
     }
 
     @Override
     public List<PlanUserFollow> getPlanFollower(Integer userId, Date lastUpdateTime) {
         return planDAO.getPlanFollower(userId, lastUpdateTime);
     }
 
     @Override
     @Transactional
     public void updatePlanFollower(Integer userId, List<PlanUserFollow> planFollow) {
         for (int i = 0; i < planFollow.size(); i++) {
             PlanUserFollow planUserFollow = planFollow.get(i);
             planUserFollow.setAddTime(new Date());
             planDAO.createPlanFollower(userId, planFollow.get(i));
         }
     }
 
     @Override
     @Transactional
     public Plan createPlan(Integer userId, Plan newPlan) {
         IDGeneration idGeneration = commonDAO.getIDGenerationInfo();
         if (newPlan != null && newPlan.getTotalMissions() != null && newPlan.getTotalMissions() > 0) {
             idGeneration.setPlanId(idGeneration.getPlanId() + 1);
             newPlan.setPlanId(idGeneration.getPlanId());
             newPlan.setSharedPlan(SharedPlanEnum.Shared.ordinal());
             newPlan.setPlanFlag(PlanFlagEnum.New.ordinal());
             String missionIds = "";
             for (Mission mission : newPlan.getMissions()) {
                 if (mission.getSequence() != null) {
                     mission.setPlanId(idGeneration.getPlanId());
                     idGeneration.setMissionId(idGeneration.getMissionId() + 1);
                     mission.setMissionId(idGeneration.getMissionId());
                     //todo:: add experience and score calc
                     missionIds = missionIds + idGeneration.getMissionId().toString() + ",";
                 }else{
                     throw new ServerRequestException(ErrorMessageMapper.PARAM_ERROR.toString());
                 }
             }
             if (missionIds.length() > 0) {
                 missionIds.substring(0, missionIds.length() - 2);
                 newPlan.setMissionIds(missionIds);
             }
             for (Mission mission : newPlan.getMissions()) {
                 if (mission.getSequence() != null) {
                     missionService.createMission(mission);
                 }
             }
             planDAO.createPlan(newPlan);
             commonDAO.updateIDGenerationFriend(idGeneration);
             return newPlan;
         }
         throw new ServerRequestException(ErrorMessageMapper.PARAM_ERROR.toString());
     }
 
     @Override
     public PlanRunHistory getUserLastUpdatePlan(Integer userId) {
         return planDAO.getUserLastUpdatePlan(userId);
     }
 }
 
 
