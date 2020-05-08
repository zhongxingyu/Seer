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
 
 import com.paxxis.chime.client.common.Community;
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.common.FieldData;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.Scope;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.client.common.UserProfile;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.IDataValue;
 import com.paxxis.chime.service.Tools;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Robert Englander
  */
 public class UserUtils {
 
     private UserUtils() {
     }
 
     public static User createUser(String name, String description, String password, User user, DatabaseConnection database) throws Exception {
 
         database.startTransaction();
         User newUser = null;
 
         try {
             Shape userShape = ShapeUtils.getInstance("User", database, true);
             List<Shape> shapes = new ArrayList<Shape>();
             shapes.add(userShape);
 
             // everyone can see the user
             // after creation we need to modify the scope so that the user can edit his/herself
             List<Scope> scopes = new ArrayList<Scope>();
             scopes.add(new Scope(Community.Global, Scope.Permission.R));
 
             String sqlInserts[] = {"charVal", "'" + password + "'"};
 
             newUser = (User)DataInstanceUtils.createInstance(shapes, name,
                     description, null, sqlInserts,
                     new ArrayList<FieldData>(), scopes, user, database);
 
             // ok, now modify the scopes
             ScopeUtils.applyScope(newUser.getId(), new Scope(new Community(newUser.getId()), Scope.Permission.RU), database);
 
             newUser = getUserByName(name, user, database);
             database.commitTransaction();
         } catch (Exception e) {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return newUser;
     }
 
     public static User changePassword(User user, InstanceId userId, String oldPassword, String newPassword, DatabaseConnection database) throws Exception {
         database.startTransaction();
         User result = null;
 
         try {
             if (!user.isAdmin()) {
                 result = (User)DataInstanceUtils.getInstance(userId, user, database, false, true);
                 if (!result.getPassword().equals(oldPassword)) {
                     throw new Exception("Old Password is not valid.");
                 }
             }
 
             String sql = "update " + Tools.getTableSet() + " set charVal = '" + newPassword +
                    "' where id = '" + userId.getValue() + "'";
 
             database.executeStatement(sql);
             result = getUserById(userId, user, database);
             HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Modify, result, user, database);
             database.commitTransaction();
         } catch (Exception e) {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return result;
     }
 
     public static User getUserById(InstanceId userId, User user, DatabaseConnection database) throws Exception
     {
         User result = null;
 
         result = (User)DataInstanceUtils.getInstance(userId, user, database, true, true);
 
         updateUserProfile(result, database);
 
         return result;
     }
 
     public static User getUserByName(String name, User user, DatabaseConnection database) throws Exception
     {
         String sql = "select * from Chime.DataInstance where name = '" + name + "'";
 
         InstancesBundle bundle = DataInstanceUtils.getInstances(sql, null, user, database, true);
         User result = null;
 
         // TODO this is a horrible hack, and MUST be fixed
 
         // go through the bundle looking for 1 that is a User
         for (DataInstance inst : bundle.getInstances()) {
             if (inst.getShapes().get(0).getName().equals("User")) {
                 result = (User)inst;
                 break;
             }
         }
 
         if (result != null) {
             updateUserProfile(result, database);
         }
         
         return result;
     }
 
     public static void updateUserProfile(User user, DatabaseConnection database) throws Exception {
         UserProfile profile = user.getProfile();
 
         String sql = "select * from Chime.UserProfile where user_id = '" + user.getId() + "'";
         DataSet dataSet = database.getDataSet(sql, true);
         if (dataSet.next()) {
             IDataValue emailAddr = dataSet.getFieldValue("emailAddress");
             IDataValue emailNotif = dataSet.getFieldValue("emailNotification");
 
             profile.setEmailNotification(emailNotif.asString().equals("Y"));
             profile.setEmailAddress(emailAddr.asString());
         }
 
         long count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.ReviewVotesWritten, database);
         profile.setPositiveReviewVotesWritten(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.ReviewVotesWritten, database);
         profile.setNegativeReviewVotesWritten(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.CommentVotesWritten, database);
         profile.setPositiveCommentVotesWritten(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.CommentVotesWritten, database);
         profile.setNegativeCommentVotesWritten(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.ReviewVotesReceived, database);
         profile.setPositiveReviewVotesReceived(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.ReviewVotesReceived, database);
         profile.setNegativeReviewVotesReceived(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.Y, VoteUtils.VoteType.CommentVotesReceived, database);
         profile.setPositiveCommentVotesReceived(count);
 
         count = VoteUtils.getCount(user, VoteUtils.Vote.N, VoteUtils.VoteType.CommentVotesReceived, database);
         profile.setNegativeCommentVotesReceived(count);
 
         dataSet.close();
     }
 
 }
