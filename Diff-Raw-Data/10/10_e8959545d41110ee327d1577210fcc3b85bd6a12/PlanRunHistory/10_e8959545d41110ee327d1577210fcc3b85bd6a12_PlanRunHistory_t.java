 package com.usavich.entity.plan;
 
 import com.usavich.common.lib.CustomDateDeserializer;
 import com.usavich.common.lib.CustomDateSerializer;
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: p
  * Date: 13-11-8
  * Time: 下午2:37
  * To change this template use File | Settings | File Templates.
  */
 public class PlanRunHistory {
 
     private Integer planId;
     private String planName;
     private String planRunUuid;
     private Integer nextMissionId;
     private Integer userId;
     private String nickName;
    private String sex;
     private Date startTime;
     private Date endTime;
     private Integer rate;
     private String rateComment;
     private Integer remainingMissions;
     private Integer totalMissions;
     private Integer historyStatus; //0 execute 1 finished  -1 cancled
     private Date  lastUpdateTime;
     private Integer operate;
 
     public Integer getPlanId() {
         return planId;
     }
 
     public void setPlanId(Integer planId) {
         this.planId = planId;
     }
 
     public String getPlanName() {
         return planName;
     }
 
     public void setPlanName(String planName) {
         this.planName = planName;
     }
 
     public String getPlanRunUuid() {
         return planRunUuid;
     }
 
     public void setPlanRunUuid(String planRunUuid) {
         this.planRunUuid = planRunUuid;
     }
 
     public Integer getNextMissionId() {
         return nextMissionId;
     }
 
     public void setNextMissionId(Integer nextMissionId) {
         this.nextMissionId = nextMissionId;
     }
 
     public Integer getUserId() {
         return userId;
     }
 
     public void setUserId(Integer userId) {
         this.userId = userId;
     }
 
     public String getNickName() {
         return nickName;
     }
 
     public void setNickName(String nickName) {
         this.nickName = nickName;
     }
 
     @JsonSerialize(using = CustomDateSerializer.class)
     public Date getStartTime() {
         return startTime;
     }
 
     @JsonDeserialize(using = CustomDateDeserializer.class)
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     @JsonSerialize(using = CustomDateSerializer.class)
     public Date getEndTime() {
         return endTime;
     }
 
     @JsonDeserialize(using = CustomDateDeserializer.class)
     public void setEndTime(Date endTime) {
         this.endTime = endTime;
     }
 
     public Integer getRate() {
         return rate;
     }
 
     public void setRate(Integer rate) {
         this.rate = rate;
     }
 
     public String getRateComment() {
         return rateComment;
     }
 
     public void setRateComment(String rateComment) {
         this.rateComment = rateComment;
     }
 
     public Integer getRemainingMissions() {
         return remainingMissions;
     }
 
     public void setRemainingMissions(Integer remainingMissions) {
         this.remainingMissions = remainingMissions;
     }
 
     public Integer getTotalMissions() {
         return totalMissions;
     }
 
     public void setTotalMissions(Integer totalMissions) {
         this.totalMissions = totalMissions;
     }
 
     public Integer getHistoryStatus() {
         return historyStatus;
     }
 
     public void setHistoryStatus(Integer historyStatus) {
         this.historyStatus = historyStatus;
     }
 
     @JsonSerialize(using = CustomDateSerializer.class)
     public Date getLastUpdateTime() {
         return lastUpdateTime;
     }
 
     @JsonDeserialize(using = CustomDateDeserializer.class)
     public void setLastUpdateTime(Date lastUpdateTime) {
         this.lastUpdateTime = lastUpdateTime;
     }
 
     public Integer getOperate() {
         return operate;
     }
 
     public void setOperate(Integer operate) {
         this.operate = operate;
     }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
 }
