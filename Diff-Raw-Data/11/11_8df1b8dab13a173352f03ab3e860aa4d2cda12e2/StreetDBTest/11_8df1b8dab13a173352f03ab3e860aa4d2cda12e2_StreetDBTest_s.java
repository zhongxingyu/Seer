 /*
  * Copyright 2011 the original author or authors.
  * Licensed under the GPL License, version 3;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.gnu.org/licenses/gpl.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ru.jcorp.smartstreets.test;
 
 import junit.framework.TestCase;
 import ru.jcorp.smartstreets.entity.Street;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 public class StreetDBTest extends TestCase {
 
     private EntityManager entityManager;
     private EntityManagerFactory factory;
 
     @Override
     public void setUp() throws Exception {
         factory = Persistence.createEntityManagerFactory("smart-streets",
                 System.getProperties());
 
         entityManager = factory.createEntityManager();
     }
 
     public void testStreets() {
         Street street = new Street();
        street.setCaption("New Street!");
 
         EntityTransaction transaction = entityManager.getTransaction();
         try {
             transaction.begin();
 
             entityManager.persist(street);
 
             transaction.commit();
         } catch (Exception ex) {
             transaction.rollback();
         }
     }
 
     @Override
     public void tearDown() throws Exception {
         if (entityManager != null)
             entityManager.close();
 
         if (factory != null)
             factory.close();
     }
 }
