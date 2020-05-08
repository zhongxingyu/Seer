 /*
  * Copyright (C) 2003-2013 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.estudy.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jcr.ItemExistsException;
 
 import org.estudy.learning.Util;
 import org.estudy.learning.model.ECategory;
 import org.estudy.learning.model.EQuestion;
 import org.estudy.learning.model.ESession;
 import org.estudy.learning.model.ETesting;
 import org.estudy.learning.storage.DataStorage;
 import org.estudy.learning.storage.impl.JcrDataStorage;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.organization.User;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.services.security.Identity;
 import org.exoplatform.services.security.MembershipEntry;
 import org.jgroups.demos.TotalTokenDemo.TotalPayload;
 
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * May 2, 2013  
  */
 public class EstudyServiceTest extends BaseServiceTestCase {
 
   private RepositoryService repositoryService_ ;
   private DataStorage  storage_;
   private static String   username = "root";
   public Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
   private OrganizationService organizationService_;
 
   public void setUp() throws Exception {
     super.setUp();
     repositoryService_ = getService(RepositoryService.class);
     organizationService_ = (OrganizationService) getService(OrganizationService.class);
     storage_ = getService(JcrDataStorage.class);
   }
 
   private void loginUser(String userId) {
     Identity identity = new Identity(userId, membershipEntries);
     ConversationState state = new ConversationState(identity);
     ConversationState.setCurrent(state);
   }
   //mvn test -Dtest=EstudyServiceTest#testInitServices
   public void testInitServices() throws Exception{
 
     assertNotNull(repositoryService_) ;
     assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getName(), "repository");
     assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName(), "portal-test");
     assertNotNull(organizationService_) ;
 
     //assertEquals(organizationService_.getUserHandler().findAllUsers().getSize(), 8);
 
     assertNotNull(storage_);
 
   }
   //mvn test -Dtest=EstudyServiceTest#testEStoreHome
   public void testEStoreHome() throws Exception {
 
     assertNotNull(storage_.getEStorageHome());
 
     assertEquals(Util.E_STUDY_APP,storage_.getEStorageHome().getName());
   }
   //mvn test -Dtest=EstudyServiceTest#testECategory
   public void testECategory() throws Exception {
     ECategory e = new ECategory("Test category");
     storage_.saveCategory(e, true);
     assertNotNull(storage_.getCategories());
     assertEquals(1, storage_.getCategories().size());
 
     e.setName("new name");
     storage_.saveCategory(e, false);
     assertEquals("new name", storage_.getCategory(e.getId()).getName());
 
     try {
       storage_.saveCategory(new ECategory("new name"), true);
     } catch (ItemExistsException e2) {
       log.info("category already exits!");
       assertTrue(true);
     }
     assertEquals(1, storage_.getCategories().size());
 
     storage_.removeCategory(e.getId());
     assertEquals(0, storage_.getCategories().size());
 
 
   }
 
   //mvn test -Dtest=EstudyServiceTest#testEQuestion
   public void testEQuestion() throws Exception {
 
     String title = "what is <a> tag?";
     String[] answers = new String[]{"is html tag", "none of above", "a link tag", "a special char"};
     String[] correct = new String[]{"is html tag","a link tag"};
 
     long point = 2 ;
     EQuestion eq = new EQuestion();
     eq.setTitle(title);
     eq.setAnswers(Arrays.asList(answers));
     eq.setCorrect(Arrays.asList(correct));
     eq.setPoint(point);
     storage_.saveQuestion(eq, true);
 
     assertNotNull(storage_.getQuestions());
     assertEquals(1, storage_.getQuestions().size());
     assertEquals(title,  storage_.getQuestion(eq.getId()).getTitle());
     assertEquals(Arrays.asList(answers),  storage_.getQuestion(eq.getId()).getAnswers());
 
     storage_.saveQuestion(eq, false);
     assertEquals(2, storage_.getQuestion(eq.getId()).getPoint());
 
 
 
     try {
       storage_.saveQuestion(new EQuestion(title), true);
     } catch (ItemExistsException e2) {
       log.info("question or answer already exits!");
       assertTrue(true);
     }
     assertEquals(1, storage_.getQuestions().size());
 
     storage_.removeQuestion(eq.getId());
     assertEquals(0, storage_.getQuestions().size());
   }
 
   //mvn test -Dtest=EstudyServiceTest#testETesting
   public void testETesting() throws Exception {
 
 
     String title = "what is <c> tag?";
     String[] answers = new String[]{"is html tag", "none of above", "a link tag", "a special char"};
     String[] correct = new String[]{"is html tag","a link tag"};
     long point = 2 ;
     EQuestion eq = new EQuestion();
     eq.setTitle(title);
     eq.setAnswers(Arrays.asList(answers));
     eq.setCorrect(Arrays.asList(correct));
     eq.setPoint(point);
     storage_.saveQuestion(eq, true);
 
     String title2 = "what is <b> tag?";
     String[] answers2 = new String[]{"is html tag", "none of above", "a link tag", "a special char"};
     String[] correct2 = new String[]{"is html tag","a link tag"};
 
     String[] wrong = new String[]{"is html tag","a link tag","a special char"};
 
     long point2 = 4 ;
 
     long total = point + point2;
 
     EQuestion eq2 = new EQuestion();
     eq2.setTitle(title2);
     eq2.setAnswers(Arrays.asList(answers2));
     eq2.setCorrect(Arrays.asList(correct2));
     eq2.setPoint(point2);
 
     storage_.saveQuestion(eq2, true);
 
     Map<String, Collection<String>> quest = new HashMap<String, Collection<String>>();
 
     quest.put(eq.getId(), Arrays.asList(correct));
     quest.put(eq2.getId(), Arrays.asList(wrong));
 
     ETesting test = new ETesting();
     test.setTime(0);
     test.setNote("this is final test");
     test.setTotalPoint(point+point2);
     test.setQuest(quest);
 
     loginUser(username);
     Identity current = ConversationState.getCurrent().getIdentity();
     User user = organizationService_.getUserHandler().findUserByName(current.getUserId()) ;
     storage_.saveTesting(user, test, true);
     assertNotNull(storage_.getTestingScore(user.getUserName()));
     Collection<ETesting> list = storage_.getTestingScore(user.getUserName());
     assertEquals(1, list.size());
     list = storage_.getTestingScore(user.getUserName(), Arrays.asList(new String[]{test.getId()}));
     assertEquals(1, list.size());
     ETesting testing = list.iterator().next();
     assertEquals(total, testing.getTotalPoint());
 
     assertEquals(point, testing.getPoint());
 
     quest.put(eq2.getId(), Arrays.asList(correct2));
     test.setQuest(quest);
     storage_.saveTesting(user, test, false);
     list = storage_.getTestingScore(user.getUserName(), Arrays.asList(new String[]{test.getId()}));
     testing = list.iterator().next();
     assertEquals(point + point2, testing.getPoint());
     
     storage_.removeTesting(user.getUserName(), test.getId());
     
     assertEquals(0,storage_.getTestingScore(user.getUserName()).size());
     storage_.removeQuestion(eq.getId());
     storage_.removeQuestion(eq2.getId());
     
   }
 
   //mvn test -Dtest=EstudyServiceTest#testESession
   public void testESession() throws Exception {
     ECategory e = new ECategory("Test category");
     storage_.saveCategory(e, true);
     ESession es = new ESession();
     String title = "study about HTML";
     es.setTitle(title);
     es.setCat(e.getId());
     es.setDec("some description");
     
     
     
     String test = "test1"+Util.SEMI_COLON+"test2";
     es.setQuest(test);
     es.setVlink("htt://youtube.com");
     es.setRflink("htt://w3chool.com");
 
     storage_.saveSession(es, true);
 
     assertNotNull(storage_.getSessions());
     assertEquals(1, storage_.getSessions().size());
     assertEquals(title,  storage_.getSession(es.getId()).getTitle());
     assertEquals(Arrays.asList(test.split(Util.SEMI_COLON)),  storage_.getSession(es.getId()).getQuest());
 
     try {
       storage_.saveSession(new ESession(title), true);
     } catch (ItemExistsException e2) {
       log.info("session already exits!");
       assertTrue(true);
     }
     assertEquals(1, storage_.getSessions().size());
 
     storage_.removeSession(es.getId());
     assertEquals(0, storage_.getSessions().size());
    storage_.removeCategory(e.getId());
    assertEquals(0, storage_.getCategories().size());
   }
 
 }
