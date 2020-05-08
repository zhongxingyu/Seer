 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.spektrumprojekt.datamodel.user;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.lang3.StringUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import de.spektrumprojekt.datamodel.identifiable.Identifiable;
 
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "userGlobalIdFrom", "userGlobalIdTo",
         "topicGlobalId" }))
 public class UserSimilarity extends Identifiable {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     public static String getKey(String fromUserGlobalId, String toUserGlobalId,
             String messageGroupId) {
         return StringUtils.join(new Object[] { fromUserGlobalId, toUserGlobalId,
                 messageGroupId }, "#");
 
     }
 
     private final String userGlobalIdFrom;
 
     private final String userGlobalIdTo;
     private final String messageGroupGlobalId;
 
     private double similarity;
 
     private int numberOfMentions;
 
     /**
      * for jpa
      */
     protected UserSimilarity() {
         // for jpa
         userGlobalIdFrom = null;
         userGlobalIdTo = null;
         messageGroupGlobalId = null;
     }
 
     public UserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
             String messageGroupGlobalId) {
         this(userGlobalIdFrom, userGlobalIdTo, messageGroupGlobalId, 0);
     }
 
     public UserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
             String messageGroupGlobalId,
             double similarity) {
         if (userGlobalIdFrom == null) {
             throw new IllegalArgumentException("userGlobalIdFrom cannot be null");
         }
         if (userGlobalIdTo == null) {
             throw new IllegalArgumentException("userGlobalIdTo cannot be null");
         }
         if (messageGroupGlobalId == null) {
             throw new IllegalArgumentException("messageGroupGlobalId cannot be null");
         }
 
         this.userGlobalIdFrom = userGlobalIdFrom;
         this.userGlobalIdTo = userGlobalIdTo;
         this.messageGroupGlobalId = messageGroupGlobalId;
         this.setSimilarity(similarity);
     }
 
     public void consolidate(UserSimilarity reverse, Integer fromCount, Integer toCount) {
         double ratio1 = reverse.getNumberOfMentions() == 0 ? 0 : numberOfMentions
                 / reverse.getNumberOfMentions();
         double ratio2 = numberOfMentions == 0 ? 0 : reverse.getNumberOfMentions()
                 / numberOfMentions;
         if (ratio1 > 0 && ratio2 > 0) {
             this.setSimilarity(Math.min(ratio1, ratio2));
         }
         if (fromCount > 0) {
            this.setSimilarity(Math.max(getSimilarity(),
                    Math.min(1, numberOfMentions / fromCount.doubleValue())));
         }
     }
 
     @JsonIgnore
     public String getKey() {
         return getKey(getUserGlobalIdFrom(), getUserGlobalIdTo(), getMessageGroupGlobalId());
 
     }
 
     public String getMessageGroupGlobalId() {
         return messageGroupGlobalId;
     }
 
     public int getNumberOfMentions() {
         return numberOfMentions;
     }
 
     public double getSimilarity() {
         return similarity;
     }
 
     public String getUserGlobalIdFrom() {
         return userGlobalIdFrom;
     }
 
     public String getUserGlobalIdTo() {
         return userGlobalIdTo;
     }
 
     public void incrementNumberOfMentions() {
         numberOfMentions++;
     }
 
     public void setNumberOfMentions(int numberOfMentions) {
         this.numberOfMentions = numberOfMentions;
     }
 
     public void setSimilarity(double similarity) {
         if (similarity < 0) {
             throw new IllegalArgumentException("similarity cannot be negative. similarity="
                     + similarity);
         }
         if (similarity > 1) {
             throw new IllegalArgumentException("similarity cannot be greater than 1. similarity="
                     + similarity);
         }
         this.similarity = similarity;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("UserSimilarity [userGlobalIdFrom=");
         builder.append(userGlobalIdFrom);
         builder.append(", userGlobalIdTo=");
         builder.append(userGlobalIdTo);
         builder.append(", messageGroupGlobalId=");
         builder.append(messageGroupGlobalId);
         builder.append(", similarity=");
         builder.append(similarity);
         builder.append(", getGlobalId()=");
         builder.append(getGlobalId());
         builder.append(", getId()=");
         builder.append(getId());
         builder.append("]");
         return builder.toString();
     }
 }
