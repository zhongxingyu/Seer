 package org.primefaces.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Author: Piotr Turek
  */
 public class DefaultEnrollScheduleEvent implements EnrollScheduleEvent, Serializable {
 
     private String id;
 
     private String title;
 
     private Date startDate;
 
     private Date endDate;
 
     private boolean allDay = false;
 
     private String styleClass;
 
     private Object data;
 
     private boolean editable = true;
 
     private int importance = 100;
 
     private int points = 0;
 
     private boolean isPossible = true;
 
     private String teacher = "TBA";
 
     private String place = "";
 
 
     public DefaultEnrollScheduleEvent() {}
 
     public DefaultEnrollScheduleEvent(String title, Date start, Date end) {
         this.title = title;
         this.startDate = start;
         this.endDate = end;
     }
 
     public DefaultEnrollScheduleEvent(String title, Date start, Date end, boolean allDay) {
         this.title = title;
         this.startDate = start;
         this.endDate = end;
         this.allDay = allDay;
     }
 
     public DefaultEnrollScheduleEvent(String title, Date start, Date end, String styleClass) {
         this.title = title;
         this.startDate = start;
         this.endDate = end;
         this.styleClass = styleClass;
     }
 
     public DefaultEnrollScheduleEvent(String title, Date start, Date end, Object data) {
         this.title = title;
         this.startDate = start;
         this.endDate = end;
         this.data = data;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public boolean isAllDay() {
         return allDay;
     }
 
     public void setAllDay(boolean allDay) {
         this.allDay = allDay;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public Object getData() {
         return data;
     }
 
     public void setData(Object data) {
         this.data = data;
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
 
     public int getImportance() {
         return importance;
     }
 
     public int getPoints() {
         return points;
     }
 
     public boolean isPossible() {
         return isPossible;
     }
 
     public String getTeacher() {
         return teacher;
     }
 
     public String getPlace() {
         return place;
     }
 
     public void setImportance(int importance) {
         this.importance = importance;
     }
 
     public void setPoints(int points) {
         this.points = points;
     }
 
     public void setPossible(boolean possible) {
         isPossible = possible;
         if(!isPossible) {
             importance = 0;
             points = 0;
         }
     }
 
     public void setTeacher(String teacher) {
         this.teacher = teacher;
     }
 
     public void setPlace(String place) {
         this.place = place;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final DefaultEnrollScheduleEvent other = (DefaultEnrollScheduleEvent) obj;
         if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
             return false;
         }
         if (this.startDate != other.startDate && (this.startDate == null || !this.startDate.equals(other.startDate))) {
             return false;
         }
         if (this.endDate != other.endDate && (this.endDate == null || !this.endDate.equals(other.endDate))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 61 * hash + (this.title != null ? this.title.hashCode() : 0);
         hash = 61 * hash + (this.startDate != null ? this.startDate.hashCode() : 0);
         hash = 61 * hash + (this.endDate != null ? this.endDate.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
        return "DefaultScheduleEvent{title=" + title + ",startDate=" + startDate + ",endDate=" + endDate + "}";
     }
 
 }
