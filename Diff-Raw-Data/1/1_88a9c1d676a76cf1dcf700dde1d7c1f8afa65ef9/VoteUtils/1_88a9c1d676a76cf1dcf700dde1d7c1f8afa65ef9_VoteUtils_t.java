 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.data;
 
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.service.Tools;
 
 /**
  *
  * @author Robert Englander
  */
 public class VoteUtils {
     public static class UserVote {
         public boolean vote;
         public boolean hasVote = false;
     }
 
     public enum Vote {
         Y,
         N
     }
 
     public enum VoteType {
         ReviewVotesWritten,
         CommentVotesWritten,
         ReviewVotesReceived,
         CommentVotesReceived,
     }
 
     private VoteUtils()
     {}
 
     public static long getCount(User user, Vote vote, VoteType voteType, DatabaseConnection database) throws Exception {
         long count = 0;
 
         String sql = null;
         switch (voteType) {
             case ReviewVotesWritten:
                 sql = "select count(id) count from " + Tools.getTableSet() + "_positive where value = '" +
                         vote.toString() + "' and user_id = '" + user.getId() + "' and instance_typeId = '500'";
                 break;
             case CommentVotesWritten:
                 sql = "select count(id) count from " + Tools.getTableSet() + "_positive where value = '" +
                         vote.toString() + "' and user_id = '" + user.getId() + "' and instance_typeId = '600'";
                 break;
             case ReviewVotesReceived:
                 sql = "select count(id) count from " + Tools.getTableSet() + "_positive where value = '" +
                         vote.toString() + "' and instance_userId = '" + user.getId() + "' and instance_typeId = '500'";
                 break;
             case CommentVotesReceived:
                 sql = "select count(id) count from " + Tools.getTableSet() + "_positive where value = '" +
                         vote.toString() + "' and instance_userId = '" + user.getId() + "' and instance_typeId = '600'";
                 break;
         }
 
         DataSet dataSet = database.getDataSet(sql, true);
         boolean found = dataSet.next();
         if (found) {
             count = dataSet.getFieldValue("count").asLong();
         }
 
        dataSet.close();
         return count;
     }
 
     public static UserVote getVote(InstanceId id, User user, DatabaseConnection database) throws Exception {
 
         String sql = "select id, value from " + Tools.getTableSet() +
                         "_positive where instance_id = '" + id +
                         "' and user_id = '" + user.getId() + "'";
 
         DataSet dataSet = database.getDataSet(sql, true);
         boolean found = dataSet.next();
 
         UserVote result = new UserVote();
         if (found) {
             result.vote = dataSet.getFieldValue("value").asString().equals("Y");
             result.hasVote = true;
         }
 
         dataSet.close();
 
         return result;
     }
 
     public static DataInstance applyVote(DataInstance instance, boolean isPositive, User user, DatabaseConnection database) throws Exception
     {
         database.startTransaction();
         InstanceId id = instance.getId();
         String tableSet = Tools.getTableSet();
 
         try
         {
             // is this a new vote?  or is this an adjusted vote?
             UserVote userVote = getVote(instance.getId(), user, database);
 
             String value = "N";
             if (isPositive) {
                 value = "Y";
             }
 
             String sql;
 
             if (userVote.hasVote) {
                 boolean oldIsPositive = userVote.vote;
                 if (isPositive == oldIsPositive) {
                     // it's the same vote
                     database.commitTransaction();
                     return instance;
                 }
 
                 sql = "update " + tableSet + "_positive set value = '" + value + "' " +
                         "where instance_id = '" + instance.getId() +
                             "' and user_id = '" + user.getId() + "'";
             }
             else {
                 InstanceId newid = Tools.getNewId(Tools.DEFAULT_EXTID);
                 sql = "insert into " + tableSet + "_positive (id,value,instance_id, user_id, instance_typeId, instance_userId) values ('"+
                         newid + "', '" + value + "', '" +
                         instance.getId() + "', '"
                         + user.getId() + "', '"
                         + instance.getShapes().get(0).getId() + "', '"
                         + instance.getUpdatedBy().getId() + "')";
             }
 
             database.executeStatement(sql);
 
             // compute the new counts and ranking
             int posCount = instance.getPositiveCount();
             int negCount = instance.getNegativeCount();
             if (isPositive) {
                 posCount++;
                 if (userVote.hasVote) {
                     negCount--;
                 }
             } else {
                 negCount++;
                 if (userVote.hasVote) {
                     posCount--;
                 }
             }
 
             int total = posCount + negCount;
             int delta = posCount - negCount;
             if (total < 5) {
                 // no weighting
             } else if (total < 10) {
                 delta = (int)((float)delta * 1.4f);
             } else if (total < 25) {
                 delta = (int)((float)delta * 1.6f);
             } else if (total < 100) {
                 delta = (int)((float)delta * 1.75f);
             } else {
                 delta = (int)((float)delta * 1.9f);
             }
 
             int newRanking = delta + 50;
             if (newRanking > 100) {
                 newRanking = 100;
             }
 
             if (newRanking < 0) {
                 newRanking = 0;
             }
 
             sql = "update " + tableSet + " set " +
                     "positiveCount = " + posCount + ", negativeCount = " + negCount + ", ranking = " + newRanking +
                     " where id = '" + instance.getId() + "'";
 
             database.executeStatement(sql);
 
             database.commitTransaction();
 
             UserUtils.updateUserProfile(user, database);
             CacheManager.instance().putUserSession(user);
             CacheManager.instance().put(user);
 
             // the owner of the instance that was voted on needs its profile updated too
             User instanceOwner = UserUtils.getUserByName(instance.getUpdatedBy().getName(), user, database);
             UserUtils.updateUserProfile(instanceOwner, database);
             CacheManager.instance().put(instanceOwner);
 
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         DataInstance data = DataInstanceUtils.getInstance(id, user, database, true, false);
         return data;
     }
 }
