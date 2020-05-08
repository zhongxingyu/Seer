 package com.usavich.entity.mission;
 
 import java.math.BigInteger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: LeonLu
  * Date: 8/5/13
  * Time: 5:52 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MissionChallenge {
 
     private Integer challengeId;
     private String grade;
     private BigInteger time;
     private Double distance;
     private String note;
 
     public Integer getChallengeId() {
         return challengeId;
     }
 
     public void setChallengeId(Integer challengeId) {
         this.challengeId = challengeId;
     }
 
     public String getGrade() {
         return grade;
     }
 
     public void setGrade(String grade) {
         this.grade = grade;
     }
 
     public BigInteger getTime() {
         return time;
     }
 
     public void setTime(BigInteger time) {
         this.time = time;
     }
 
     public Double getDistance() {
         return distance;
     }
 
     public void setDistance(Double distance) {
         this.distance = distance;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 }
