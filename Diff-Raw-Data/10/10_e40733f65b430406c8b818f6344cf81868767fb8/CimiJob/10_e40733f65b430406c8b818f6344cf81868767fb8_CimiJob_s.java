 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.domain;
 
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 import org.ow2.sirocco.apis.rest.cimi.utils.CimiDateAdapter;
 
 /**
  * Class Job.
  */
 @XmlRootElement(name = "Job")
 @JsonSerialize(include = Inclusion.NON_NULL)
 public class CimiJob extends CimiCommonId {
 
     /** Serial number */
     private static final long serialVersionUID = 1L;
 
     /**
      * Field "targetEntity".
      */
     private String targetEntity;
 
     /**
      * Field "action". URI
      */
     private String action;
 
     /**
      * Field "status".
      */
     private String status;
 
     /**
      * Field "returnCode".
      */
     private Integer returnCode;
 
     /**
      * Field "progress".
      */
     private Integer progress;
 
     /**
      * Field "statusMessage".
      */
     private String statusMessage;
 
     /**
      * Field "timeOfStatusChange". DateTimeUTC Format ISO 8601 ?
      */
     private Date timeOfStatusChange;
 
     /**
      * Field "isCancellable".
      */
     private Boolean isCancellable;
 
     /**
      * Field "parentJob".
      */
     private ParentJob parentJob;
 
     /**
      * Field "nestedJobs".
      */
     private NestedJob[] nestedJobs;
 
     /**
      * Return the value of field "targetEntity".
      * 
      * @return The value
      */
     public String getTargetEntity() {
         return this.targetEntity;
     }
 
     /**
      * Set the value of field "targetEntity".
      * 
      * @param targetEntity The value
      */
     public void setTargetEntity(final String targetEntity) {
         this.targetEntity = targetEntity;
     }
 
     /**
      * Return the value of field "action".
      * 
      * @return The value
      */
     public String getAction() {
         return this.action;
     }
 
     /**
      * Set the value of field "action".
      * 
      * @param action The value
      */
     public void setAction(final String action) {
         this.action = action;
     }
 
     /**
      * Return the value of field "status".
      * 
      * @return The value
      */
     public String getStatus() {
         return this.status;
     }
 
     /**
      * Set the value of field "status".
      * 
      * @param status The value
      */
     public void setStatus(final String status) {
         this.status = status;
     }
 
     /**
      * Return the value of field "returnCode".
      * 
      * @return The value
      */
     public Integer getReturnCode() {
         return this.returnCode;
     }
 
     /**
      * Set the value of field "returnCode".
      * 
      * @param returnCode The value
      */
     public void setReturnCode(final Integer returnCode) {
         this.returnCode = returnCode;
     }
 
     /**
      * Return the value of field "progress".
      * 
      * @return The value
      */
     public Integer getProgress() {
         return this.progress;
     }
 
     /**
      * Set the value of field "progress".
      * 
      * @param progress The value
      */
     public void setProgress(final Integer progress) {
         this.progress = progress;
     }
 
     /**
      * Return the value of field "statusMessage".
      * 
      * @return The value
      */
     public String getStatusMessage() {
         return this.statusMessage;
     }
 
     /**
      * Set the value of field "statusMessage".
      * 
      * @param statusMessage The value
      */
     public void setStatusMessage(final String statusMessage) {
         this.statusMessage = statusMessage;
     }
 
     /**
      * Return the value of field "timeOfStatusChange".
      * 
      * @return The value
      */
     @XmlJavaTypeAdapter(CimiDateAdapter.class)
     public Date getTimeOfStatusChange() {
         return this.timeOfStatusChange;
     }
 
     /**
      * Set the value of field "timeOfStatusChange".
      * 
      * @param timeOfStatusChange The value
      */
     public void setTimeOfStatusChange(final Date timeOfStatusChange) {
         this.timeOfStatusChange = timeOfStatusChange;
     }
 
     /**
      * Return the value of field "isCancellable".
      * 
      * @return The value
      */
     public Boolean getIsCancellable() {
         return this.isCancellable;
     }
 
     /**
      * Set the value of field "isCancellable".
      * 
      * @param isCancellable The value
      */
     public void setIsCancellable(final Boolean isCancellable) {
         this.isCancellable = isCancellable;
     }
 
     /**
      * Return the value of field "parentJob".
      * 
      * @return The value
      */
     public ParentJob getParentJob() {
         return this.parentJob;
     }
 
     /**
      * Set the value of field "parentJob".
      * 
      * @param parentJob The value
      */
     public void setParentJob(final ParentJob parentJob) {
         this.parentJob = parentJob;
     }
 
     /**
      * Return the value of field "nestedJobs".
      * 
      * @return The value
      */
     @XmlElement(name = "nestedJob")
     public NestedJob[] getNestedJobs() {
         return this.nestedJobs;
     }
 
     /**
      * Set the value of field "nestedJobs".
      * 
      * @param nestedJobs The value
      */
     public void setNestedJobs(final NestedJob[] nestedJobs) {
         this.nestedJobs = nestedJobs;
     }
 
 }
