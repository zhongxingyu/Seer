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
 
 package com.paxxis.chime.extension;
 
 import com.paxxis.chime.client.common.CommentsBundle;
 import com.paxxis.chime.client.common.Community;
 import com.paxxis.chime.client.common.DataField;
 import com.paxxis.chime.client.common.DataFieldValue;
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.DataInstanceEvent;
 import com.paxxis.chime.client.common.DataInstanceEvent.EventType;
 import com.paxxis.chime.client.common.DataSocialContext;
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.common.DiscussionsBundle;
 import com.paxxis.chime.client.common.FieldData;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.ReviewsBundle;
 import com.paxxis.chime.client.common.Scope;
 import com.paxxis.chime.client.common.Scope.Permission;
 import com.paxxis.chime.client.common.TagContext;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.client.common.extension.ChimeExtension;
 import com.paxxis.chime.client.common.extension.ExtensionContext;
 import com.paxxis.chime.client.common.extension.MemoryIndexer;
 import com.paxxis.chime.client.common.portal.PortalTemplate;
 import com.paxxis.chime.common.JavaObjectPayload;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.DatabaseConnectionPool;
 import com.paxxis.chime.data.DataInstanceUtils;
 import com.paxxis.chime.data.ShapeUtils;
 import com.paxxis.chime.data.PortalTemplateUtils;
 import com.paxxis.chime.data.UserUtils;
 import com.paxxis.chime.service.NotificationTopicSender;
 import com.paxxis.chime.service.RequestQueueSender;
 import com.paxxis.chime.service.ServiceBusMessageProducer;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Robert Englander
  */
 public class ChimeExtensionManager implements ExtensionContext {
     private static ChimeExtensionManager instance = null;
 
     public static ChimeExtensionManager instance() {
         return instance;
     }
 
     private NotificationTopicSender updateSender;
     private NotificationTopicSender editSender;
     private RequestQueueSender eventSender;
     private List<ExtensionDefinition> definitions = new ArrayList<ExtensionDefinition>();
     private List<ChimeExtension> extensions = new ArrayList<ChimeExtension>();
     private DatabaseConnectionPool dbPool = null;
 
     public ChimeExtensionManager() {
         super();
     }
 
     public void setUpdateNotifier(NotificationTopicSender sender) {
         updateSender = sender;
     }
     
     public void setEditNotifier(NotificationTopicSender sender) {
         editSender = sender;
     }
 
     public void setEventNotifier(RequestQueueSender sender) {
         eventSender = sender;
     }
 
     public List<ChimeExtension> getExtensions() {
         return extensions;
     }
 
     public DataInstance getDataInstance(InstanceId instId) {
         String id = instId.getValue();
         DataInstance inst = null;
         int idx = id.length() - 2;
         String extId = id.substring(idx);
         for (ChimeExtension ext : extensions) {
             if (ext.getId().equals(extId)) {
                 inst = ext.getDataInstance(instId);
                 break;
             }
         }
 
         return inst;
     }
 
     public List<DataInstance> getDataInstances(List<InstanceId> ids) {
         List<DataInstance> results = new ArrayList<DataInstance>();
         for (InstanceId id : ids) {
             DataInstance inst = getDataInstance(id);
             if (inst != null) {
                 results.add(inst);
             }
         }
 
         return results;
     }
     
     public void publishUpdate(DataInstance instance) {
         try {
             if (instance.getPortalTemplate().isAutoUpdate()) {
                 DataInstanceEvent event = new DataInstanceEvent();
                 event.setDataInstance(instance);
                 event.setEventType(EventType.Modify);
                 updateSender.send(new ServiceBusMessageProducer(event), new JavaObjectPayload());
             }
         } catch (Exception e) {
             int x = 1;
         }
     }
 
     public void setConnectionPool(DatabaseConnectionPool pool) {
         dbPool = pool;
     }
 
     public Shape getShapeById(InstanceId id) {
         DatabaseConnection dbconn = dbPool.borrowInstance(this);
         Shape type = null;
 
         try {
             type = ShapeUtils.getInstanceById(id, dbconn, true);
         } catch (Exception e) {
         }
 
         dbPool.returnInstance(dbconn, this);
         return type;
     }
 
     public DataInstance createDataInstance(String extId, InstanceId shapeId, String name, String desc, User user, Community community) {
         Shape type = getShapeById(shapeId);
 
         DataInstance inst = new DataInstance();
         inst.setTransient(true);
         inst.setSocialContext(new DataSocialContext());
         inst.addShape(type);
         inst.setName(name);
         inst.setDescription(desc);
         InstanceId newId = com.paxxis.chime.service.Tools.getNewId(extId);
         inst.setId(newId);
 
         long n = System.currentTimeMillis();
         Date now = new Date();
         now.setTime(n);
         inst.setCreated(now);
         inst.setCreatedBy(user);
         inst.setUpdated(now);
         inst.setUpdatedBy(user);
         inst.setLockType(DataInstance.LockType.NONE);
 
         Scope readScope = new Scope(community, Scope.Permission.R);
         Scope editScope = new Scope(new Community(user.getId()), Scope.Permission.RU);
         inst.getSocialContext().addScope(readScope);
         inst.getSocialContext().addScope(editScope);
         inst.getSocialContext().setReviewsBundle(new ReviewsBundle());
         inst.getSocialContext().setCommentsBundle(new CommentsBundle());
         inst.getSocialContext().setDiscussionsBundle(new DiscussionsBundle());
         inst.getSocialContext().setTagContexts(new ArrayList<TagContext>());
 
         inst.setLockType(DataInstance.LockType.EDIT);
         inst.setLocked(now);
         inst.setLockedBy(user);
         
         try {
             DatabaseConnection db = dbPool.borrowInstance(this);
             PortalTemplate template = PortalTemplateUtils.getTemplate(inst, db);
             dbPool.returnInstance(db, this);
             inst.setPortalTemplate(template);
         } catch (Exception e) {
 
         }
 
         return inst;
     }
     
     public MemoryIndexer createMemoryIndexer() {
         ChimeMemoryIndexer indexer = new ChimeMemoryIndexer();
         indexer.setConnectionPool(dbPool);
         return indexer;
     }
 
     public void setExtensions(List<ExtensionDefinition> defs) {
         definitions.addAll(defs);
     }
 
     public void initialize() {
         for (ExtensionDefinition def : definitions) {
             try {
                 Class clazz = Class.forName(def.getClassName());
                 ChimeExtension ext = (ChimeExtension) clazz.newInstance();
                 ext.setExtensionContext(this);
                 ext.setId(def.getId());
                 ext.setName(def.getName());
                 ext.setCalClassName(def.getCalClassName());
                 ext.setPropertyMap(def.getPropertyMap());
 
                 User user = new User();
                 user.setId(User.SYSTEM);
                 user.setName("admin");
 
                 DatabaseConnection dbconn = dbPool.borrowInstance(this);
                 User extuser = UserUtils.getUserByName(def.getUserName(), user, dbconn);
                 Community extcomm = null;
                 for (Community c : extuser.getCommunities()) {
                     if (c.getId().equals(InstanceId.create(def.getCommunityId()))) {
                         extcomm = c;
                         break;
                     }
                 }
                 dbPool.returnInstance(dbconn, this);
                 ext.setUser(extuser);
                 ext.setCommunity(extcomm);
 
                 for (ExtensionShapeMapping mapping : def.getShapeMapList()) {
                    String id = mapping.getShapeId();
                    ext.setShapeId(InstanceId.create(id));
 
                     String objectName = mapping.getObjectName();
                    ext.setObjectName(objectName);

                    for (String fieldName : mapping.getFieldMap().keySet()) {
                        String shapeFieldName = mapping.getFieldMap().get(fieldName);
                        ext.addFieldMapping(fieldName, shapeFieldName);
                    }
                 }
 
                 ext.initialize();
                 extensions.add(ext);
             } catch (ClassNotFoundException ex) {
                 Logger.getLogger(ChimeExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstantiationException ex) {
                 Logger.getLogger(ChimeExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(ChimeExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Exception ex) {
                 Logger.getLogger(ChimeExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         instance = this;
     }
 
     public DataInstance getInstanceById(InstanceId id, User user) {
         DatabaseConnection db = dbPool.borrowInstance(this);
         DataInstance result = null;
         try {
             result = DataInstanceUtils.getInstance(id, user, db, true, true);
         } catch (Exception ex) {
             Logger.getLogger(ChimeExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         dbPool.returnInstance(db, this);
         return result;
     }
 
     public DataInstance publishEvent(String name, String desc, DataInstance eventType, List<DataInstance> related,
             String summary, User user, Community community) {
         
         DataInstance event = null;
         DatabaseConnection db = dbPool.borrowInstance(this);
         try {
             List<Shape> shapes = new ArrayList<Shape>();
             Shape eventShape = ShapeUtils.getInstanceById(Shape.EVENT_ID, db, true);
             shapes.add(eventShape);
 
             List<Scope> scopes = new ArrayList<Scope>();
             Scope scope = new Scope(community, Permission.R);
             scopes.add(scope);
             scope = new Scope(new Community(user.getId()), Permission.RU);
             scopes.add(scope);
 
             List<FieldData> data = new ArrayList<FieldData>();
             FieldData fd = new FieldData();
             fd.field = eventShape.getField("Type");
             fd.shape = eventShape;
             fd.value = eventType;
             data.add(fd);
 
             Shape refShape = ShapeUtils.getInstanceById(Shape.REFERENCE_ID, db, true);
             DataField relatedField = eventShape.getField("Related Content");
             for (DataInstance rel : related) {
                 fd = new FieldData();
                 fd.field = relatedField;
                 fd.shape = eventShape;
                 fd.value = rel;
                 data.add(fd);
             }
 
             // TODO why does the FieldData have both a field and a shape?  doesn't
             // the field itself define the shape?  the code below assumes that...
             fd = new FieldData();
             fd.field = eventShape.getField("Summary");
             fd.shape = eventShape;
             fd.value = summary;
             data.add(fd);
 
             // if there is no retention specified then we don't want to save this
             // event.  we can do this by tricking the system by creating an outer
             // transaction, and then just rolling it back.  sneaky!
             Shape eventTypeShape = eventType.getShapes().get(0);
             DataField retentionField = eventTypeShape.getField("Retention Days");
             List<DataFieldValue> retValues = eventType.getFieldValues(eventTypeShape, retentionField);
             boolean noSave = retValues.isEmpty();
             Date expiration = null;
 
             if (!noSave) {
                 double retention = Double.parseDouble(retValues.get(0).getName());
                 noSave = retention < 0.0;
 
                 if (retention > 0.0) {
                     // compute the expiration time
                     long exp = Calendar.getInstance().getTimeInMillis() +
                             (long)(retention * 24 * 60 * 60 * 1000);
                     expiration = new Date(exp);
                 }
             }
 
             db.startTransaction();
             event = DataInstanceUtils.createInstance(shapes, name,
                 desc, expiration, new String[2],
                 data, scopes, user, db);
 
             if (noSave) {
                 db.rollbackTransaction();
             } else {
                 db.commitTransaction();
 
                 DataInstanceEvent evt = new DataInstanceEvent();
                 evt.setEventType(EventType.Create);
                 evt.setDataInstance(event);
                 evt.setUser(user);
                 editSender.send(new ServiceBusMessageProducer(evt), new JavaObjectPayload());
             }
 
 
             DataInstanceEvent evt = new DataInstanceEvent();
             evt.setEventType(EventType.Event);
             evt.setDataInstance(event);
             evt.setUser(user);
             eventSender.send(new ServiceBusMessageProducer(evt), new JavaObjectPayload());
 
         } catch (Exception e) {
             int x = 1;
         }
 
         dbPool.returnInstance(db, this);
         return event;
 
     }
 }
 
 
 
