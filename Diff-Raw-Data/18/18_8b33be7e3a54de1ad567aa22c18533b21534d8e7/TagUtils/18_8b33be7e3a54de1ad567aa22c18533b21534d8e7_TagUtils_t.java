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
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.common.FieldData;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.Scope;
 import com.paxxis.chime.client.common.Tag;
 import com.paxxis.chime.client.common.TagContext;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.StringData;
 import com.paxxis.chime.service.ApplyTagsResponse;
 import com.paxxis.chime.service.Tools;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * 
  * @author Robert Englander 
  */
 public class TagUtils 
 {
     static List<FieldData> _noFields = new ArrayList<FieldData>();
     static List<Scope> _noScopes = new ArrayList<Scope>();
 
     
     private TagUtils()
     {}
     
 
     public static List<String> getAppliedUserIds(Tag tag, DataInstance instance, DatabaseConnection database) throws Exception {
         List<String> userIds = new ArrayList<String>();
 
         String sql = "select user_id from Chime.User_tag_applied where tag_id = '" + tag.getId() +
                 "' and instance_id = '" + instance.getId() + "'";
         DataSet dataSet = database.getDataSet(sql, true);
         while (dataSet.next()) {
             String id = dataSet.getFieldValue("user_id").asString();
             userIds.add(id);
         }
 
         dataSet.close();
 
         return userIds;
     }
     
     public static ApplyTagsResponse removeTags(DataInstance instance, User user, List<Tag> tags, DatabaseConnection database) throws Exception
     {
         ApplyTagsResponse response = new ApplyTagsResponse();
         
         database.startTransaction();
         
         try
         {
             // unapply each tag from the data instance
             for (Tag tag : tags)
             {
                 if (removeTag(instance, user, tag, database))
                 {
                     response.setInstanceModified(true);
                 }
             }
 
             database.commitTransaction();
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
         
         return response;
     }
     
     private static boolean removeTag(DataInstance instance, User user, Tag tag, DatabaseConnection database) throws Exception
     {
         boolean noLongerApplied = false;
         
         String typeName = Tools.getTableSet();
 
         InstanceId instanceId = instance.getId();
 
         if (isApplied(instance, user, tag, database))
         {
             // remove the record from the instance tag table
             String sql = "delete from " + typeName + "_tag where instance_id = '" + instanceId +
                             "' and user_id = '" + user.getId() + "' and tag_id = '" + tag.getId() + "'";
             
             database.executeStatement(sql);
 
             // update the user tag applied table
             sql = "delete from Chime.User_tag_applied where user_id = '" + user.getId() +
                     "' and tag_id = '" + tag.getId() + "' and instance_id = '" + instanceId + "'";
             database.executeStatement(sql);
             
             // update the tag metrics table
             sql = "update " + typeName + "_tag_metrics set count = (count - 1) where " +
                         "instance_id = '" + instanceId + "' and tag_id = '" + tag.getId() + "'";
 
             database.executeStatement(sql);
 
             // this could be smarter, but...
             // delete the entry in the metrics table if the count is 0
             sql = "delete from " + typeName + "_tag_metrics where count = 0 and " +
                         "instance_id = '" + instanceId + "' and tag_id = '" + tag.getId() + "'";
 
             database.executeStatement(sql);
             
             // update the usage count in the tag table
             sql = "update DataInstance set intVal = (intVal - 1) where id = '" + tag.getId() + "'";
             database.executeStatement(sql);
 
             for (Shape shape : instance.getShapes()) {
                 // update the type specific metrics
                 sql = "update Chime.Type_tag_usage set usageCount = (usageCount - 1) where " +
                             "type_id = '" + shape.getId() + "' and tag_id = '" + tag.getId() + "'";
 
                 database.executeStatement(sql);
 
                 // update the user specific metrics
                 sql = "update Chime.User_tag_usage set usageCount = (usageCount - 1) where " +
                             "type_id = '" + shape.getId() + "' and tag_id = '" + tag.getId() + "' and user_id = '" + user.getId() + "'";
 
                 database.executeStatement(sql);
             }
             
             noLongerApplied = !isApplied(instance, null, tag, database);
             
             if (noLongerApplied)
             {
                 sql = "update " + typeName + " set tagCount = (tagCount - 1), tagged = CURRENT_TIMESTAMP, taggedAction = 'R', taggedByName = '" + user.getName() + "', taggedBy = '" + user.getId() + "' where id = '" + instanceId + "'";
                 database.executeStatement(sql);
             }
             else
             {
                 sql = "update " + typeName + " set tagged = CURRENT_TIMESTAMP, taggedAction = 'R', taggedByname = '" + user.getName() + "', taggedBy = '" + user.getId() + "' where id = '" + instanceId + "'";
                 database.executeStatement(sql);
                 
             }
         }
         
         return true;
     }
     
     public static ApplyTagsResponse applyTags(DataInstance instance, User user, List<Tag> tags, DatabaseConnection database) throws Exception
     {
         ApplyTagsResponse response = new ApplyTagsResponse();
         InstanceId instanceId = instance.getId();
 
         database.startTransaction();
         
         try
         {
             // apply each tag to the data instance
             for (Tag tag : tags)
             {
                 boolean ok = true;
                 
                 // do not tag a tag with itself
                 if (instance.getShapes().get(0).getName().equals("Tag"))
                 {
                     if (instanceId.equals(tag.getId()))
                     {
                         ok = false;
                     }
                 }
                 
                 if (ok)
                 {
                     if (applyTag(instance, user, tag, database))
                     {
                         response.setInstanceModified(true);
                     }
                 }
             }
 
             database.commitTransaction();
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return response;
     }
     
     /**
      * Apply a tag to a specified data instance.
      * 
      */
     private static boolean applyTag(DataInstance instance, User user, Tag tag, DatabaseConnection database) throws Exception
     {
         boolean firstTime = false;
         boolean applied = false;
         InstanceId instanceId = instance.getId();
 
         String typeTable = Tools.getTableSet();
         
         if (!isApplied(instance, user, tag, database))
         {
             // add the record to the instance tag table
             InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
             String sql = "insert into " + typeTable + "_tag (id, instance_id, user_id, tag_id, timestamp, private) values ('" +
                     id + "', '" + instanceId +
                     "', '" + user.getId() + "', '" + tag.getId() +
                     "', CURRENT_TIMESTAMP, 'N')";
             
             database.executeStatement(sql);
             
             // update the tag metrics table
             sql = "select 1 from " + typeTable + "_tag_metrics where instance_id = '" + instanceId +
                         "' and tag_id = '" + tag.getId() + "'";
             
             DataSet dataSet = database.getDataSet(sql, true);
             boolean doUpdate = dataSet.next();
             dataSet.close();
             
             if (doUpdate)
             {
                 sql = "update " + typeTable + "_tag_metrics set count = (count + 1) where " +
                         "instance_id = '" + instanceId + "' and tag_id = '" + tag.getId() + "'";
             }
             else
             {
                 id = Tools.getNewId(Tools.DEFAULT_EXTID);
                 sql = "insert into " + typeTable + "_tag_metrics (count, id, instance_id, tag_id) values (1, '" +
                         id + "', '" + instanceId + "', '" + tag.getId() + "')";
         
                 firstTime = true;
             }
             
             database.executeStatement(sql);
 
             // update the user tag applied table
             id = Tools.getNewId(Tools.DEFAULT_EXTID);
             sql = "insert into Chime.User_tag_applied (id, user_id, tag_id, instance_id) values ('" +
                     id + "', '" + user.getId() + "','" + tag.getId() + "','" + instanceId + "')";
             database.executeStatement(sql);
             
             // update the usage count in the tag table
             sql = "update Chime.DataInstance set intVal = (intVal + 1) where id = '" + tag.getId() + "'";
             database.executeStatement(sql);
 
             // update the shape specific metrics
             for (Shape shape : instance.getShapes()) {
                 sql = "select 1 from Chime.Type_tag_usage where type_id = '" + shape.getId() +
                             "' and tag_id = '" + tag.getId() + "'";
 
                 dataSet = database.getDataSet(sql, true);
                 doUpdate = dataSet.next();
                 dataSet.close();
 
                 if (doUpdate)
                 {
                     sql = "update Chime.Type_tag_usage set usageCount = (usageCount + 1) where " +
                             "type_id = '" + shape.getId() + "' and tag_id = '" + tag.getId() + "'";
                 }
                 else
                 {
                     id = Tools.getNewId(Tools.DEFAULT_EXTID);
                     sql = "insert into Chime.Type_tag_usage (usageCount, id, type_id, tag_id) values (1, '" +
                             id + "', '" + shape.getId() + "', '" + tag.getId() + "')";
                 }
 
                 database.executeStatement(sql);
 
                 // update the user specific tag metrics
                 sql = "select 1 from Chime.User_tag_usage where type_id = '" + shape.getId() +
                             "' and tag_id = '" + tag.getId() + "' and user_id = '" + user.getId() + "'";
 
                 dataSet = database.getDataSet(sql, true);
                 doUpdate = dataSet.next();
                 dataSet.close();
 
                 if (doUpdate)
                 {
                     sql = "update Chime.User_tag_usage set usageCount = (usageCount + 1) where " +
                             "type_id = '" + shape.getId() + "' and tag_id = '" + tag.getId() + "' and user_id = '" + user.getId() + "'";
                 }
                 else
                 {
                     id = Tools.getNewId(Tools.DEFAULT_EXTID);
                     sql = "insert into Chime.User_tag_usage (usageCount,id,type_id,tag_id,user_id) values (1, '" +
                             id + "', '" + shape.getId() + "', '" + tag.getId() + "', '" + user.getId() + "')";
                 }
 
                 database.executeStatement(sql);
 
             }
 
             if (firstTime)
             {
                 sql = "update " + typeTable + " set expiration = null, tagCount = (tagCount + 1), tagged = CURRENT_TIMESTAMP, taggedAction = 'A', taggedByName = '" + user.getName() + "', taggedBy = '" + user.getId() + "' where id = '" + instanceId + "'";
                 database.executeStatement(sql);
             }
             else
             {
                 sql = "update " + typeTable + " set expiration = null, tagged = CURRENT_TIMESTAMP, taggedAction = 'A', taggedByName = '" + user.getName() + "', taggedBy = '" + user.getId() + "' where id = '" + instanceId + "'";
                 database.executeStatement(sql);
             }
 
             sql = "update Chime.RegisteredInterest set instance_name = '" +
                     instance.getName() + "', last_update = CURRENT_TIMESTAMP where instance_id ='"
                     + instanceId + "' and user_id <> '" + user.getId() + "'";
             database.executeStatement(sql);
             
             applied = true;
         }
         
         return applied;
     }
     
     private static boolean isApplied(DataInstance instance, User user, Tag tag, DatabaseConnection database) throws Exception
     {
         String sql = "select 1 from " + Tools.getTableSet() + "_tag where instance_id = '" + instance.getId() +
                         "' and tag_id = '" + tag.getId() + "'";
 
         if (user != null)
         {
             sql += " and user_id = '" + user.getId() + "'";
         }
         
         DataSet dataSet = database.getDataSet(sql, true);
         boolean isApplied = dataSet.next();
         dataSet.close();
         
         return isApplied;
     }
     
     private static List<Tag> createTags(List<String> names, User user, DatabaseConnection database) throws Exception
     {
         database.startTransaction();
         
         List<Tag> results = new ArrayList<Tag>();
         
         try
         {
             Shape tagShape = ShapeUtils.getInstance("Tag", database, true);
             List<Shape> shapes = new ArrayList<Shape>();
             shapes.add(tagShape);
 
             for (String name : names)
             {
                 Tag tag = (Tag)DataInstanceUtils.createInstance(shapes, name, null, null, new String[] {null, null}, _noFields, _noScopes, user, database);
                 results.add(tag);
             }
             
             database.commitTransaction();
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
         
         return results;
     }
     
     public static List<Tag> findTags(List<String> strings, DatabaseConnection database) throws Exception
     {
         List<Tag> tags = new ArrayList<Tag>();
 
         String sql = "select A.id, A.name, A.intVal usageCount, B.value description from DataInstance A, DataInstance_Text B where " +
                 " A.id = B.instance_id and B.datatype_column = 1";
 
         if (strings.size() > 0) {
             sql += " and ";
         }
         
         String op = "";
         for (String string : strings)
         {
             sql += op + "A.name like " + new StringData(string.trim() + "%").asSQLValue();
             op = " or  ";
         }
         sql += " order by A.name";
         
         DataSet dataSet = database.getDataSet(sql, true);
         while (dataSet.next())
         {
             String id = dataSet.getFieldValue("id").asString();
             long usageCount = dataSet.getFieldValue("usageCount").asLong();
             String name = dataSet.getFieldValue("name").asString();
             String desc = dataSet.getFieldValue("description").asString();
             
             Tag tag = new Tag();
             tag.setId(InstanceId.create(id));
             tag.setName(name);
             tag.setDescription(desc);
             tag.setUsageCount(usageCount);
             
             tags.add(tag);
         }
 
         dataSet.close();
         
         return tags;
     }
     
     public static List<Tag> findAppliedTags(List<String> strings, Shape type, DatabaseConnection database) throws Exception
     {
         List<Tag> tags = new ArrayList<Tag>();
 
         String sql = "select distinct(A.tag_id) 'id', B.name name, B.intVal usageCount, C.value description from Chime.User_tag_usage A, Chime.DataInstance B, Chime.DataInstance_Text C "
                 + "where type_id = '" + type.getId() + "' and B.id = A.tag_id and C.instance_id and C.datatype_column = 1 and = B.id and (";
         
         String op = "";
         for (String string : strings)
         {
             sql += op + "B.name like " + new StringData(string.trim() + "%").asSQLValue();
             op = " or  ";
         }
         sql += ") order by B.name";
         
         DataSet dataSet = database.getDataSet(sql, true);
         while (dataSet.next())
         {
             String id = dataSet.getFieldValue("id").asString();
             long usageCount = dataSet.getFieldValue("usageCount").asLong();
             String name = dataSet.getFieldValue("name").asString();
             String desc = dataSet.getFieldValue("description").asString();
             
             Tag tag = new Tag();
             tag.setId(InstanceId.create(id));
             tag.setName(name);
             tag.setDescription(desc);
             tag.setUsageCount(usageCount);
             
             tags.add(tag);
         }
 
         dataSet.close();
         
         return tags;
     }
     
     public static List<Tag> getTags(List<String> names, boolean modifyList, DatabaseConnection database) throws Exception
     {
         List<Tag> tags = new ArrayList<Tag>();
 
         if (names.size() > 0)
         {
             String sql = "select A.id, A.intVal usageCount from DataInstance A, DataInstance_Type B "
                     + " where A.id = B.instance_id and B.datatype_id = '700' and A.name in (";
             String comma = "";
             for (String name : names)
             {
                 sql += (comma + "'" + name + "'");
                 comma = ",";
             }
             sql += ")";
 
             Shape shape = ShapeUtils.getInstance("Tag", database, true);
             DataSet dataSet = database.getDataSet(sql, true);
             while (dataSet.next())
             {
                 String id = dataSet.getFieldValue("id").asString();
                 long usageCount = dataSet.getFieldValue("usageCount").asLong();
 
                 Tag tag = (Tag)DataInstanceUtils.getInstance(InstanceId.create(id), null, database, true, true);
                 tag.setUsageCount(usageCount);
             
                 tags.add(tag);
 
                 if (modifyList)
                 {
                     String tagName = tag.getName().trim();
                     int cnt = names.size();
                     for (int i = (cnt - 1); i >= 0; i--)
                     {
                         if (tagName.equalsIgnoreCase(names.get(i).trim()))
                         {
                             names.remove(i);
                         }
                     }
                 }
             }
 
             dataSet.close();
         }
         
         return tags;
     }
     
     
     public static List<TagContext> getTagContexts(User user, DatabaseConnection database) throws Exception
     {
         String sql = "SELECT A.usageCount usageCount, A.type_id, B.id, B.intVal totalCount from "
                 + " Chime.User_tag_usage A, Chime.DataInstance B where A.user_id = '" + user.getId()
                 + "' and A.tag_id = B.id order by B.name";
 
         List<TagContext> contexts = new ArrayList<TagContext>();
         HashMap<String, TagContext> tagMap = new HashMap<String, TagContext>();
         
         DataSet dataSet = database.getDataSet(sql, true);
         while (dataSet.next())
         {
             String id = dataSet.getFieldValue("id").asString();
             long totalCount = dataSet.getFieldValue("totalCount").asLong();
             long contextCount = dataSet.getFieldValue("usageCount").asLong();
 
             if (contextCount > 0)
             {
                 // if we already have a context, all we need to do is modify
                 // the context usage count.  otherwise we need to create a
                 // new context
                 if (tagMap.containsKey(id))
                 {
                     TagContext context = tagMap.get(id);
                     context.setUsageCount(context.getUsageCount() + contextCount);
                 }
                 else
                 {
                     Tag tag = (Tag)DataInstanceUtils.getInstance(InstanceId.create(id), user, database, true, true);
                     tag.setUsageCount(totalCount);
 
                     TagContext context = new TagContext();
                     context.setTag(tag);
                     context.setUsageCount(contextCount);
 
                     contexts.add(context);
                     tagMap.put(id, context);
                 }
             }
         }
         
         dataSet.close();
 
         return contexts;
         
     }
     
     /**
      * This is pretty much hacked up for performance reasons.  Maybe there's a cleaner way...
      * @return a list of tag contexts for the specified instance
      * @throws java.lang.Exception
      */
     public static List<TagContext> getTagContexts(InstanceId instanceId, User user, DatabaseConnection database) throws Exception {
 
         String tableSet = Tools.getTableSet();
 
         String sql;
 
         if (user == null) {
             sql = "select distinct(B.id) Bid, A.count Contextcount, B.name, B.intVal Bcount, 0 isPrivate "
                     + " from " + tableSet + "_tag_metrics A, Chime.DataInstance B, Chime.DataInstance_community C"
                     + " where B.id = A.tag_id and A.instance_id = '" + instanceId +
                    "' and C.instance_id = B.id and C.community_id = '100' order by name";
         } else if (user.isAdmin() && user.isIndexing()) {
             sql = "select distinct(B.id) Bid, A.count Contextcount, B.name, B.intVal Bcount, 0 isPrivate "
                     + " from " + tableSet + "_tag_metrics A, Chime.DataInstance B"
                     + " where B.id = A.tag_id and A.instance_id = '" + instanceId +
                     "' order by name";
         } else {
             String notprivate = " and B.id not in (select distinct(B.id) Bid"
                     + " from " + tableSet + "_tag_metrics A, Chime.DataInstance B, Chime.DataInstance_community C"
                     + " where B.id = A.tag_id and A.instance_id = '" + instanceId +
                     "' and C.instance_id = B.id and C.community_id = '100') order by name";
 
             sql = "select distinct(B.id) Bid, A.count Contextcount, B.name," +
                     "B.intVal Bcount, 0 isPrivate" +
                     " from Chime.DataInstance_tag_metrics A, Chime.DataInstance B, Chime.DataInstance_community C" +
                     " where B.id = A.tag_id and A.instance_id = '" + instanceId + "' and" +
                     " C.instance_id = B.id and C.community_id = '100'" +
                     " union" +
                     " select distinct(B.id) Bid, A.count Contextcount, B.name," +
                     " B.intVal Bcount, 1 isPrivate" +
                     " from Chime.DataInstance_tag_metrics A, Chime.DataInstance B, Chime.DataInstance_community C" +
                     " where B.id = A.tag_id and A.instance_id = '" + instanceId + "' and C.instance_id = B.id and" +
                     " C.community_id = '" + user.getId() + "'" + notprivate;
         }
 
         List<TagContext> contexts = new ArrayList<TagContext>();
         
         DataSet dataSet = database.getDataSet(sql, true);
         Shape shape = ShapeUtils.getInstance("Tag", database, true);
         while (dataSet.next())
         {
             String id = dataSet.getFieldValue("Bid").asString();
             long usageCount = dataSet.getFieldValue("Bcount").asLong();
             long contextCount = dataSet.getFieldValue("Contextcount").asLong();
             boolean isPrivate = dataSet.getFieldValue("isPrivate").asInteger() == 1;
             
             String name = dataSet.getFieldValue("name").asString();
 
             if (usageCount > 0)
             {
                 Tag tag = new Tag();
                 tag.setId(InstanceId.create(id));
                 tag.setName(name);
                 tag.addShape(shape);
                 tag.setPrivate(isPrivate);
                 
                 tag.setUsageCount(usageCount);
                 
                 TagContext context = new TagContext();
                 context.setTag(tag);
                 context.setUsageCount(contextCount);
                 
                 contexts.add(context);
             }
         }
 
         dataSet.close();
 
         return contexts;
     }
     
     public static List<Tag> getUserTags(InstanceId instanceId, User user, DatabaseConnection database, boolean useCache) throws Exception
     {
         String tableSet = Tools.getTableSet();
         
         List<Tag> tags = null;
 
         if (useCache) {
             tags = CacheManager.instance().getTags(instanceId, user.getId());
             if (tags != null)
             {
                 return tags;
             }
         }
         
         String sql = "select distinct(B.id) Bid, B.name Bname, A.count, B.intVal Bcount from "
                 + tableSet + "_tag_metrics A, Chime.DataInstance B, "
                 + tableSet + "_tag C where B.id = A.tag_id and A.instance_id = '"
                 + instanceId + "' and C.instance_id = '" + instanceId + "' and B.id = C.tag_id and C.user_id = '"
                 + user.getId() + "' order by B.name";
 
         tags = new ArrayList<Tag>();
         
         DataSet dataSet = database.getDataSet(sql, true);
         Shape shape = ShapeUtils.getInstance("Tag", database, true);
         while (dataSet.next())
         {
             String id = dataSet.getFieldValue("Bid").asString();
             long usageCount = dataSet.getFieldValue("Bcount").asLong();
             
             String name = dataSet.getFieldValue("Bname").asString();
 
             if (usageCount > 0)
             {
                 Tag tag = new Tag();
                 tag.setId(InstanceId.create(id));
                 tag.setName(name);
                 tag.addShape(shape);
 
                 tag.setUsageCount(usageCount);
 
                 tags.add(tag);
             }
         }
 
         CacheManager.instance().putTags(instanceId, user.getId(), tags);
         dataSet.close();
         return tags;
     }
 }
