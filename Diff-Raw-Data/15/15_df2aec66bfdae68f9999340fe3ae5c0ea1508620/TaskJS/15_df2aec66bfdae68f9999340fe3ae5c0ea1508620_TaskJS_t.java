 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVACAO, SA (PTIN), IBM Corp.,
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package si.setcce.societies.crowdtasking.api.RESTful.json;
 
 import si.setcce.societies.crowdtasking.Util;
 import si.setcce.societies.crowdtasking.api.RESTful.impl.UsersAPI;
 import si.setcce.societies.crowdtasking.model.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Describe your class here...
  *
  * @author Simon Jureša
  */
 @SuppressWarnings("unused")
 public class TaskJS {
     private Long id;
     private String title;
     private String description;
     private Date created;
     private Long ownerId;
     private String postedBy;    // userNickName
     private List<String> tagList;
     private boolean myTask;
     private String tags;
     private TaskStatus status;
     private Set<Long> involvedUsers;
     private List<String> informChannels;
     private String executeMessage;
     private Long score;
     private Long interestScore;
     private List<MeetingJS> meetings;
     private List<Community> communities;
     private List<String> communityJids;
     private Set<CollaborativeSpace> spaces;
     private String trustLevel;
 
     public TaskJS(Task task, CTUser loggedinUser) {
         this.id = task.getId();
         this.title = task.getTitle();
         this.description = task.getDescription();
         this.created = task.getCreated();
         this.ownerId = task.getOwnerId();
         this.postedBy = task.getPostedBy();
         this.tagList = task.getTagList();
         this.myTask = task.isMyTask();
         this.tags = task.getTags();
         this.status = task.getTaskStatus();
         this.involvedUsers = task.getInvolvedUsers();
         this.informChannels = task.getInformChannels();
         this.executeMessage = task.getExecuteMessage();
         this.score = task.getScore();
         this.interestScore = task.getInterestScore();
         this.meetings = new ArrayList<MeetingJS>();
         if (task.getMeetings() != null) {
 //			Date now = new Date();
             for (Meeting meeting : task.getMeetings()) {
 //				if (meeting.getEndTime() == null || meeting.getEndTime().after(now)) {	// TODO: check this
                if (loggedinUser != null) {
                    meetings.add(new MeetingJS(meeting, loggedinUser.getId()));
                } else {
                    meetings.add(new MeetingJS(meeting));
                }
 //				}
             }
         }
         this.communities = task.getCommunities();
         this.communityJids = task.getCommunityJids();
         this.spaces = task.getSpaces();
         if (loggedinUser == null) {
             this.trustLevel = "unknown";
         } else {
             this.trustLevel = Util.getTrustLevelDescription(
                     loggedinUser.getTrustValueForIdentity(UsersAPI.getUserById(task.getOwnerId()).getSocietiesEntityId()));
         }
     }
 }
