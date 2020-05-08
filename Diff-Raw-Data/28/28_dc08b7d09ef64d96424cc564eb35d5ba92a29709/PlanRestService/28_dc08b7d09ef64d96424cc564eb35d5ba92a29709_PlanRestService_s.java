 package com.usavich.rest.plan;
 
 import com.usavich.common.lib.CommonUtils;
 import com.usavich.entity.plan.*;
 import com.usavich.service.plan.def.PlanService;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: p
  * Date: 13-11-12
  * Time: 上午9:40
  * To change this template use File | Settings | File Templates.
  */
 public class PlanRestService implements PlanRestDef {
 
     @Autowired
     private PlanService planService;
 
     @Override
     public Plan getPlan(String planId, String lastUpdateTime) {
         CommonUtils.newMethodCall("PlanRestService.getPlan");
         return planService.getPlan(CommonUtils.parseIntegerToNull(planId),
                 CommonUtils.parseDateDefaultToNull(lastUpdateTime));
     }
 
     @Override
     public List<Plan> getPlans(String pageNo) {
         CommonUtils.newMethodCall("PlanRestService.getPlans");
         return planService.getPlansForRest(CommonUtils.parseIntegerToNull(pageNo));
     }
 
     @Override
     public Plan createPlan(String userId, Plan newPlan) {
         CommonUtils.newMethodCall("PlanRestService.createPlan");
         return planService.createPlan(CommonUtils.parseIntegerToNull(userId), newPlan);
     }
 
     @Override
     public List<PlanCollect> getPlanCollection(String userId, String lastUpdateTime) {
         CommonUtils.newMethodCall("PlanRestService.getPlanCollection");
         return planService.getPlanCollection(CommonUtils.parseIntegerToNull(userId),
                 CommonUtils.parseDateDefaultToNull(lastUpdateTime));
     }
 
     @Override
     public void updateUserCollects(String userId, List<PlanCollect> planCollects) {
         CommonUtils.newMethodCall("PlanRestService.updateUserCollects");
         planService.updateUserCollects(CommonUtils.parseIntegerToNull(userId), planCollects);
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunHistory(String userId, String lastUpdateTime) {
         CommonUtils.newMethodCall("PlanRestService.getPlanRunHistory");
         return planService.getPlanRunHistory(CommonUtils.parseIntegerToNull(userId),
                 CommonUtils.parseDateDefaultToNull(lastUpdateTime));
     }
 
     @Override
     public PlanRunHistory getUserLastUpdatePlan(String userId) {
        CommonUtils.newMethodCall("PlanRestService.getUserExecutingPlan");
         return planService.getUserLastUpdatePlan(CommonUtils.parseIntegerToNull(userId));
     }
 
     @Override
     public void updateRunningHistory(String userId, List<PlanRunHistory> planHistory) {
         CommonUtils.newMethodCall("PlanRestService.updateRunningHistory");
         planService.updateRunningHistory(CommonUtils.parseIntegerToNull(userId), planHistory);
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunningByPlanId(String planId, String pageNo) {
         CommonUtils.newMethodCall("PlanRestService.getPlanRunningByPlanId");
         return planService.getPlanRunningByPlanId(CommonUtils.parseIntegerToNull(planId), CommonUtils.parseIntegerToNull(pageNo));
     }
 
     @Override
     public List<PlanRunHistory> getPlanRunningByUserId(String userId, String pageNo) {
         CommonUtils.newMethodCall("PlanRestService.getPlanRunningByUserId");
         return planService.getPlanRunningByUserId(CommonUtils.parseIntegerToNull(userId), CommonUtils.parseIntegerToNull(pageNo));
     }
 
     @Override
     public List<PlanUserFollow> getPlanFollower(String userId, String lastUpdateTime) {
         CommonUtils.newMethodCall("PlanRestService.getPlanFollower");
         return planService.getPlanFollower(CommonUtils.parseIntegerToNull(userId),
                 CommonUtils.parseDateDefaultToNull(lastUpdateTime));
     }
 
     @Override
     public void updatePlanFollower(String userId, List<PlanUserFollow> planFollow) {
         CommonUtils.newMethodCall("PlanRestService.updatePlanFollower");
         planService.updatePlanFollower(CommonUtils.parseIntegerToNull(userId), planFollow);
     }
 }
