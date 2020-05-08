 /*
  * Copyright (c) 2012. Mohan Ambalavanan
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *       http://www.apache.org/licenses/LICENSE-2.0
  *    Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *  limitations under the License
  */
 
 package com.creative.dao.repository;
 
import static com.creative.dao.repository.TestUtil.*;

 import org.hibernate.CacheMode;
 import org.hibernate.FlushMode;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mohan
  * Date: 7/07/12
  * Time: 8:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GenericBatchDaoImplTest {
 
     private GenericBatchDao genericBatchDao;
 
     private HibernateParam hibernateParam;
 
     @Before
     public void init() {
         hibernateParam = new HibernateParam();
         mockHibernateParam(hibernateParam);
         genericBatchDao = new GenericBatchDaoImpl(hibernateParam.sessionFactory, 10);
     }
 
 
     @Test
     public void testExecuteInsertBatch() throws Exception {
 
         mockCurrentSession(hibernateParam.sessionFactory, hibernateParam.session);
         genericBatchDao.executeInsertBatch(TEST_STRING_LIST);
         verifySession();
         for (String s : TEST_STRING_LIST) {
 
             verify(hibernateParam.session).save(s);
         }
     }
 
     @Test
     public void testExecuteSaveOrUpdateBatch() throws Exception {
 
         mockCurrentSession(hibernateParam.sessionFactory, hibernateParam.session);
         genericBatchDao.executesaveOrUpdateBatch(TEST_STRING_LIST);
         verifySession();
         for (String s : TEST_STRING_LIST) {
 
             verify(hibernateParam.session).saveOrUpdate(s);
         }
     }
 
     @Test
     public void testExecuteDeleteBatch() throws Exception {
         mockCurrentSession(hibernateParam.sessionFactory, hibernateParam.session);
         genericBatchDao.executeDeleteBatch(TEST_STRING_LIST);
         verifySession();
         for (String s : TEST_STRING_LIST) {
 
             verify(hibernateParam.session).delete(s);
 
 
         }
     }
 
     @Test
     public void testExecuteUpdate() throws Exception {
         mockCurrentSession(hibernateParam.sessionFactory, hibernateParam.session);
         when(hibernateParam.query.executeUpdate()).thenReturn(TEST_INTEGER);
         int rows = genericBatchDao.executeUpdate(hibernateParam.query);
         assertEquals(rows, TEST_INTEGER);
         verifySession();
 
     }
 
     public void verifySession() {
         verify(hibernateParam.session).setCacheMode(CacheMode.IGNORE);
         verify(hibernateParam.session).setFlushMode(FlushMode.MANUAL);
 
     }
 }
