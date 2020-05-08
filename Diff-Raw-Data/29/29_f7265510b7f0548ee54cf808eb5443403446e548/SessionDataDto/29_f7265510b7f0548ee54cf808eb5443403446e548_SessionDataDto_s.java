 package com.griddynamics.jagger.webclient.client.dto;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * @author "Artem Kirillov" (akirillov@griddynamics.com)
  * @since 5/29/12
  */
 public class SessionDataDto implements Serializable {
     private String name;
     private Date startDate;
     private Date endDate;
     private int activeKernelsCount;
     private int tasksExecuted;
     private int tasksFailed;
 
     public SessionDataDto() {
     }
 
     public SessionDataDto(String name, Date startDate, Date endDate, int activeKernelsCount, int tasksExecuted, int tasksFailed) {
         this.name = name;
         this.startDate = startDate;
         this.endDate = endDate;
         this.activeKernelsCount = activeKernelsCount;
         this.tasksExecuted = tasksExecuted;
         this.tasksFailed = tasksFailed;
     }
 
     public String getName() {
         return name;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public int getActiveKernelsCount() {
         return activeKernelsCount;
     }
 
     public int getTasksExecuted() {
         return tasksExecuted;
     }
 
     public int getTasksFailed() {
         return tasksFailed;
     }
 }
