 /*
  * MongoLink, Object Document Mapper for Java and MongoDB
  *
  * Copyright (c) 2012, Arpinum or third-party contributors as
  * indicated by the @author tags
  *
  * MongoLink is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MongoLink is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  *
  * You should have received a copy of the Lesser GNU General Public License
  * along with MongoLink.  If not, see <http://www.gnu.org/licenses/>. 
  *
  */
 
 package org.mongolink;
 
 import com.google.common.collect.Lists;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import org.bson.types.ObjectId;
 import org.junit.Before;
 import org.junit.Test;
 import org.mongolink.domain.criteria.Criteria;
 import org.mongolink.domain.criteria.Restrictions;
 import org.mongolink.domain.mapper.ContextBuilder;
 import org.mongolink.test.entity.FakeChildAggregate;
 import org.mongolink.test.entity.FakeEntity;
 import org.mongolink.test.entity.FakeEntityWithNaturalId;
 import org.mongolink.test.factory.TestFactory;
 
 import java.util.List;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 
 @SuppressWarnings("unchecked")
 public class TestsIntegration extends TestsWithMongo {
 
     private void initData() {
         BasicDBObject fakeEntity = new BasicDBObject();
         fakeEntity.put("_id", new ObjectId("4d9d9b5e36a9a4265ea9ecbe"));
         fakeEntity.put("value", "fake entity value");
         fakeEntity.put("comments", new BasicDBList());
         fakeEntity.put("index", 42);
         fakeEntity.put("comment", new BasicDBObject("value", "the comment"));
 
         BasicDBObject fakeChild = new BasicDBObject();
         fakeChild.put("_id", new ObjectId("5d9d9b5e36a9a4265ea9ecbe"));
         fakeChild.put("childName", "child value");
         fakeChild.put("value", "parent value");
         fakeChild.put("comments", new BasicDBList());
         fakeChild.put("index", 0);
         fakeChild.put("__discriminator", "FakeChildAggregate");
 
         db.getCollection("fakeentity").insert(fakeEntity, fakeChild);
 
         BasicDBObject naturalIdEntity = new BasicDBObject();
         naturalIdEntity.put("_id", "naturalkey");
         naturalIdEntity.put("value", "naturalvalue");
 
         db.getCollection("fakeentitywithnaturalid").insert(naturalIdEntity);
     }
 
     @Before
     public void before() {
         initData();
     }
 
     @Test
     public void canGetById() {
         FakeEntity entityFound = mongoSession.get("4d9d9b5e36a9a4265ea9ecbe", FakeEntity.class);
 
         assertThat(entityFound, notNullValue());
         assertThat(entityFound.getId(), is("4d9d9b5e36a9a4265ea9ecbe"));
         assertThat(entityFound.getValue(), is("fake entity value"));
     }
 
     @Test
     public void canGetByNaturalId() {
         FakeEntityWithNaturalId fakeAggregateWithNaturalId = mongoSession.get("naturalkey",
                 FakeEntityWithNaturalId.class);
 
         assertThat(fakeAggregateWithNaturalId, notNullValue());
         assertThat(fakeAggregateWithNaturalId.getNaturalKey(), is("naturalkey"));
     }
 
     @Test
     public void canUseSessionManager() {
         ContextBuilder contextBuilder = TestFactory.contextBuilder().withFakeEntity();
         MongoSessionManager manager = MongoSessionManager.create(contextBuilder, new ConfigProperties().addSettings(Settings.defaultInstance()));
         MongoSession session = manager.createSession();
         session.start();
         session.save(new FakeEntity("new fake entity"));
     }
 
     @Test
     public void insertingSetId() {
         DBCollection testid = db.getCollection("testid");
         BasicDBObject dbo = new BasicDBObject();
 
         testid.insert(Lists.<DBObject>newArrayList(dbo));
 
         assertThat(dbo.get("_id"), notNullValue());
     }
 
     @Test
     public void canGetChildEntity() {
         FakeChildAggregate entity = (FakeChildAggregate) mongoSession.get("5d9d9b5e36a9a4265ea9ecbe", FakeEntity.class);
 
         assertThat(entity, notNullValue());
         assertThat(entity.getValue(), is("parent value"));
         assertThat(entity.getChildName(), is("child value"));
     }
 
     @Test
     public void canSaveChildEntity() {
         FakeChildAggregate FakeChildAggregate = new FakeChildAggregate();
         FakeChildAggregate.setChildName("child");
         FakeChildAggregate.setValue("value from parent");
         FakeChildAggregate.addComment("this is a comment!");
 
         mongoSession.save(FakeChildAggregate);
 
         FakeChildAggregate entityFound = mongoSession.get(FakeChildAggregate.getId(), FakeChildAggregate.class);
 
         assertThat(entityFound, notNullValue());
         assertThat(entityFound.getComments().size(), is(1));
     }
 
     @Test
     public void canLimitSearch() {
         for (int i = 0; i < 10; i++) {
             mongoSession.save(new FakeEntity("valeur"));
         }
         final Criteria criteria = mongoSession.createCriteria(FakeEntity.class);
         criteria.add(Restrictions.equals("value", "valeur"));
         criteria.limit(1);
 
         final List result = criteria.list();
 
         assertThat(result.size(), is(1));
     }
 
     @Test
     public void canSkipSearch() {
         for (int i = 0; i < 10; i++) {
             mongoSession.save(new FakeEntity("valeur"));
         }
         final Criteria criteria = mongoSession.createCriteria(FakeEntity.class);
         criteria.add(Restrictions.equals("value", "valeur"));
         criteria.skip(1);
 
         final List result = criteria.list();
 
         assertThat(result.size(), is(9));
     }
 
     @Test
     public void canPopulateComponent() {
         final FakeEntity entity = mongoSession.get("4d9d9b5e36a9a4265ea9ecbe", FakeEntity.class);
 
         assertThat(entity.getComment(), notNullValue());
         assertThat(entity.getComment().getValue(), is("the comment"));
     }
 
     @Test
     public void canRemoveAnElementFromList() {
         FakeEntity fake = new FakeEntity("test");
         fake.addComment("a comment");
 		fake.addComment("another comment");
         mongoSession.save(fake);
 
 		fake.getComments().remove(1);
 		mongoSession.stop();
 
 		mongoSession = sessionManager.createSession();
         mongoSession.start();
         final FakeEntity entityFound = mongoSession.get(fake.getId(), FakeEntity.class);
         assertThat(entityFound.getComments().size(), is(1));
 		assertThat(entityFound.getComments().get(0).getValue(), is("a comment"));
     }
 
 }
