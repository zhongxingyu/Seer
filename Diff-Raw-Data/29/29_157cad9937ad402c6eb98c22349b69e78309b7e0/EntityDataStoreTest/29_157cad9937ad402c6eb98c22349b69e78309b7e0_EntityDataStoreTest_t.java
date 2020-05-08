 /*
  * Copyright 2013 BrewTour.
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
 package za.co.brewtour.test.datastore.entities;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import java.util.Date;
 import java.util.List;
 import javax.jdo.JDOException;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
import javax.jdo.Transaction;

 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import za.co.brewtour.server.entity.Beer;
 import za.co.brewtour.server.persistence.PMF;
 
 /**
  *
  * @author bestermichael
  */
 public class EntityDataStoreTest {
 
     private final LocalServiceTestHelper helper = new LocalServiceTestHelper((new LocalDatastoreServiceTestConfig()));
     PersistenceManager pm;
 
     @Before
     public void setUp() {
         helper.setUp();
         pm = PMF.get().getPersistenceManager();
 
     }
 
     @After
     public void tearDown() {
         helper.tearDown();
         pm.close();
     }
 
     //@Test
     //public void searchNonExistingBeerEntity() {
     //pm = PMF.get().getPersistenceManager();
     //Beer beer = new Beer("Jack Black Lager", "Something nice", "Jack Black", new Date(), "5", "Pale Lager", "url:url");
     //check that we don't currently have this entity in our DataStore:
     //try {
     ///pm.getObjectById(beer);
     //fail();
     //} catch (JDOException e) {
     //assertTrue(true);
     //} finally {
     //pm.close();
     //}
     //}
     @Test
     public void addNewBeerEntity() {
         //pm = PMF.get().getPersistenceManager();
         Beer beer = new Beer("Jack Black Lager", "Something nice", "Jack Black", new Date(), "5", "Pale Lager", "url:url");
         Beer beer2 = new Beer("Pale Ale", "Something nice", "Jack Black", new Date(), "5", "Pale Ale", "url:url");
 
 
         //Persist the Beer to the DataStore, and check that it has been persisted:
         pm.makePersistent(beer);
         pm.makePersistent(beer2);
 
         //Check that the entity exists.
         try {
             Query query = pm.newQuery(Beer.class);
             List<Beer> beerEntities = (List<Beer>) query.execute();
             assertNotNull(beerEntities);
             assertTrue("Entities List's size must be 2", beerEntities.size() == 2);
         } catch (JDOException e) {
             fail();
         } //finally {
         //pm.close();
         //}
 
     }
 
     @Test
     public void deleteBeerEntity() {
         Beer beer = new Beer("Jack Black Lager", "Something nice", "Jack Black", new Date(), "5", "Pale Lager", "url:url");
 
         //First persist an object:
         pm.makePersistent(beer);
 
         //Now delete it:
         pm.deletePersistent(beer);
 
         try {
             Query query = pm.newQuery(Beer.class);
             List<Beer> beerEntities = (List<Beer>) query.execute();
             assertTrue("Entity has not been deleted", beerEntities.isEmpty());
 
         } catch (JDOException e) {
             fail();
         }
     }
 
     @Test
     public void updateBeerEntity() {
    	
    	// We need two transactions, one to create the beer and the second to update it
    	Transaction tx = pm.currentTransaction();
         Beer beer = new Beer("Jack Black Lager", "Something nice", "Jack Black", new Date(), "5", "Pale Lager", "url:url");
 
        // begin insert transaction
        tx.begin();
        
         //First persist an object:
         pm.makePersistent(beer);
        // Commit insert transaction, flushing the object to the datastore
        tx.commit();
        
        // begin update transaction
        tx.begin();
        //Beer detachedBeer = pm.detachCopy(beer);
        Beer jackBlackBeer = pm.getObjectById(Beer.class, beer.getKey());
        Beer detachedBeer = pm.detachCopy(jackBlackBeer);
         //Update the object and persist it:
         detachedBeer.setBeerName("Pale Ale");
        
         pm.makePersistent(detachedBeer);
        // Commit update transaction 
        tx.commit();
 
         try {
             Query query = pm.newQuery(Beer.class);
             List<Beer> beerEntities = (List<Beer>) query.execute();
 
             assertTrue("Should only have one entity persisted", beerEntities.size() == 1);
            assertTrue("Beer name should have been updated", beerEntities.get(0).getBeerName().equals("Pale Ale"));
         } catch (JDOException e) {
             fail();
         }
     }
 }
