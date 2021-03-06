  /************************************************************************************
  * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2009
  * encuestame Development Team.
  * Licensed under the Apache Software License version 2.0
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to  in writing,  software  distributed
  * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
  * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
  * specific language governing permissions and limitations under the License.
  ************************************************************************************
  */
 package org.encuestame.test.business.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.encuestame.persistence.domain.EmailList;
 import org.encuestame.persistence.domain.Email;
 import org.encuestame.persistence.domain.question.Question;
 import org.encuestame.persistence.domain.question.QuestionPattern;
 import org.encuestame.persistence.domain.security.Account;
 import org.encuestame.persistence.domain.security.UserAccount;
 import org.encuestame.persistence.domain.survey.Poll;
 import org.encuestame.persistence.domain.survey.PollFolder;
 import org.encuestame.persistence.exception.EnMeNoResultsFoundException;
 import org.encuestame.persistence.exception.EnMeExpcetion;
 import org.encuestame.core.service.imp.IPollService;
 import org.encuestame.core.util.ConvertDomainBean;
 import org.encuestame.test.business.service.config.AbstractServiceBase;
 import org.encuestame.utils.json.FolderBean;
 import org.encuestame.utils.json.QuestionBean;
 import org.encuestame.utils.json.QuestionPatternBean;
 import org.encuestame.utils.web.QuestionAnswerBean;
 import org.encuestame.utils.web.UnitLists;
 import org.encuestame.utils.web.PollBean;
 import org.hibernate.HibernateException;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
  /**
  * Test for {@link PollService}.
  * @author Morales, Diana Paola paolaATencuestame.org
  * @since 17/05/2010 19:35:36
  * @version $Id:$
  */
 public class TestPollService extends AbstractServiceBase{
 
      /** {@link Account} **/
     private Account user;
 
     /** {@link Question} **/
     private Question question;
 
     /** {@link QuestionPattern} **/
     private QuestionPattern questionPattern;
 
     /** {@link IPollService} **/
     @Autowired
     private IPollService pollService;
 
     /** {@link Poll} **/
     private Poll poll;
 
     private UserAccount userAccount;
 
     /** {@link EmailList} **/
     private EmailList emailList;
 
     /** {@link Email} **/
     private Email emails;
 
     /** {@link PollFolder}. **/
     private PollFolder folder;
 
     /**
      * Init.
      */
     @Before
     public void serviceInit(){
         this.user = createUser("testEncuesta", "testEncuesta123");
         this.userAccount = createUserAccount("diana", this.user);
         this.question = createQuestion("Why the roses are red?","html");
         this.questionPattern = createQuestionPattern("html");
         this.poll = createPoll(new Date(), this.question, "FDK125", this.user, Boolean.TRUE, Boolean.TRUE);
         this.emailList = createDefaultListEmail(this.userAccount.getAccount());
         createDefaultListEmail(this.user, "default");
         this.emails = createDefaultEmails("paola@jotadeveloper.com", this.emailList);
         createDefaultEmails("dianmorales@gmail.com", this.emailList);
         this.folder = createPollFolder("folder 1", this.userAccount);
         this.poll.setPollFolder(folder);
      }
 
     @Test
     public void test(){
        System.out.println("------");
     }
 
     /**
      * Test createPoll.
      * @throws Exception exception
      */
     //@Test
     public void testcreatePoll() throws Exception{
         final QuestionBean question = ConvertDomainBean.convertQuestionsToBean(this.question);
         final PollBean unitPoll = ConvertDomainBean.convertPollDomainToBean(this.poll);
         unitPoll.setQuestionBean(question);
         this.pollService.createPoll(unitPoll, this.userAccount.getUsername(), this.question);
     }
 
     /**
      * Test getPollsByFolder.
      * @throws EnMeNoResultsFoundException
      */
     //@Test
     //TODO: ignore for now.
     public void testgetPollsByFolder() throws EnMeNoResultsFoundException{
         getiPoll().saveOrUpdate(this.poll);
         List<PollBean> polls = this.pollService.getPollsByFolder(ConvertDomainBean
                               .convertFolderToBeanFolder(folder), this.userAccount.getUsername());
         assertEquals(polls.size(), 1);
     }
 
     /**
      * Test retrieveFolderPoll.
      * @throws EnMeNoResultsFoundException exception
      */
     //@Test
     public void testretrieveFolderPoll() throws EnMeNoResultsFoundException{
        List<FolderBean> folders = this.pollService.retrieveFolderPoll();
         assertEquals(folders.size(), 1);
     }
 
     /**
      * Test createPollFolder.
      * @throws EnMeNoResultsFoundException exception
      */
     //@Test
     public void testcreatePollFolder() throws EnMeNoResultsFoundException{
          this.pollService.createPollFolder("folder 2", this.userAccount.getUsername());
         List<FolderBean> folders = this.pollService.retrieveFolderPoll();
          assertEquals(folders.size(), 2);
     }
 
     /**
      * Test updateFolderName.
      * @throws EnMeNoResultsFoundException exception
      */
     //@Test
     public void testupdateFolderName() throws EnMeNoResultsFoundException{
         this.pollService.updateFolderName(this.folder.getId(), "newFolderName", this.userAccount.getUsername());
         final PollFolder folder = this.getiPoll().getPollFolderById(this.folder.getId());
          assertEquals(folder.getFolderName(), "newFolderName");
     }
 
     /**
      * test removePollFolder.
      * @throws EnMeNoResultsFoundException exception
      */
     //@Test(expected = HibernateException.class)
     public void testremovePollFolderBatchUpdateException() throws EnMeNoResultsFoundException{
         final long id = this.folder.getId();
         this.pollService.removePollFolder(id);
     }
 
     /**
      * Remove Poll Folder.
      * @throws EnMeNoResultsFoundException exception
      */
     //@Test()
     public void testremovePollFolder() throws EnMeNoResultsFoundException{
         this.poll.setPollFolder(null);
         getiPoll().saveOrUpdate(this.poll);
         final long id = this.folder.getId();
         this.pollService.removePollFolder(id);
         Assert.assertNull(getiPoll().getPollFolderById(id));
     }
 
     /**
      * Test Find Polls By User.
      * @throws EnMeNoResultsFoundException
      **/
     //@Test
     public void testFindAllPollByUserId() throws EnMeNoResultsFoundException{
         List<PollBean> unitPoll =  new ArrayList<PollBean>();
         unitPoll = pollService.listPollByUser(this.userAccount.getUsername(), 5, 0);
          assertEquals("should be equals",1, unitPoll.size());
     }
 
     /**
      * Test List Polls by Question Keyword.
      * @throws EnMeNoResultsFoundException
      **/
     //FIXME:
     //@Test
     public void testListPollbyQuestionKeyword() throws EnMeNoResultsFoundException{
         List<PollBean> unitPollList = new ArrayList<PollBean>();
         final String keyword = "Why";
         unitPollList = pollService.listPollbyQuestionKeyword(this.userAccount.getUsername(), keyword, 5, 0);
         assertEquals("should be equals",1, unitPollList.size());
 
     }
 
     /**
      * Test Update Question Poll.
      * @throws EnMeExpcetion
      */
     //@Test
     public void testUpdateQuestionPoll() throws EnMeExpcetion{
          final String expectedResponse = "Why the tooth are white";
          final List<QuestionAnswerBean> answers;
          final QuestionPatternBean patternBean;
          answers = new ArrayList<QuestionAnswerBean>();
          answers.add(createAnswersBean("ZXCVB", "Yes", this.question.getQid()));
          answers.add(createAnswersBean("ASDFG", "No", this.question.getQid()));
          patternBean = createPatternBean("radio.class", "radio buttons", "2", "Yes/No", "template.html");
          final QuestionBean unitQuestion = createUnitQuestion(this.question.getQid(), expectedResponse, 1L, this.user.getUid(), answers, patternBean);
          pollService.updateQuestionPoll(unitQuestion);
          assertEquals(this.question.getQuestion(), expectedResponse);
      }
 
     /**
      * Test Update Question Poll.
      * @throws EnMeExpcetion
      */
 
     //@Test
     //@ExpectedException(EnMeExpcetion.class)
     public void testUpdateNullQuestionPoll() throws EnMeExpcetion{
          final String expectedResponse = "Why the sea is blue";
          final List<QuestionAnswerBean> answers;
          final QuestionPatternBean patternBean;
          answers = new ArrayList<QuestionAnswerBean>();
          answers.add(createAnswersBean("ZXCVB", "Yes", this.question.getQid()));
          answers.add(createAnswersBean("ASDFG", "No", this.question.getQid()));
          patternBean = createPatternBean("radio.class", "radio buttons", "2", "Yes/No", "template.html");
          final QuestionBean unitQuestion = createUnitQuestion(1L,"Why the sea is blue", 1L, this.user.getUid(), answers, patternBean);
          pollService.updateQuestionPoll(unitQuestion);
      }
 
     //@Test
     public void testCreateUrlPoll(){
            final String hashUrl="3456DS";
            final String testUrl= pollService.createUrlPoll(URLPOLL, hashUrl, this.userAccount.getCompleteName());
            assertNotNull(testUrl);
     }
 
     //@Test(timeout=80000)
     public void testPublicPollByEmailList(){
         final UnitLists emailUnitList = createUnitEmailList(this.emailList.getIdList(),
                         new Date(), this.emailList.getListName(), this.userAccount.getUid());
          final String urlPoll = pollService.createUrlPoll(URLPOLL, "DS56727", this.userAccount.getCompleteName());
          pollService.publicPollByList(urlPoll, emailUnitList);
          pollService.publicPollByList(urlPoll, new UnitLists());
          assertEquals(1, 1);
     }
 }
