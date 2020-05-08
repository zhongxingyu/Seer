 package com.wordpress.salaboy.smartknowledgediscovery.services;
 
 import com.worpdress.salaboy.smartprocessdiscovery.model.questionaire.Question;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.jpa.JpaTransactionManager;
 
 import javax.transaction.*;
 
 /**
  * creation date: 2/23/11
  */
 public class PersistenceServicesTest {
 
     @Test
     public void loadSpringPersistenceContext() throws SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
         ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:persistence-context.xml");
         final QuestionService questionService = (QuestionService) applicationContext.getBean("questionService");
         final Question question = new Question("test");
         questionService.add(question);
         Assert.assertEquals(1, questionService.listAll().size());
     }
 }
