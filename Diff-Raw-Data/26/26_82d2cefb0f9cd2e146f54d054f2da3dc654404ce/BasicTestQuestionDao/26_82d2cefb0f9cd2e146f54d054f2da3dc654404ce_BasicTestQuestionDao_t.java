 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tictac.test_question;
 
 import java.util.List;
 import org.hibernate.classic.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
 import tictac.question.Question;
 import tictac.test.Test;
 import tictac.user.TransactionError;
 
 /**
  *
  * @author bkt
  */
 public class BasicTestQuestionDao implements TestQuestionDao {
 
     private SessionFactory _sessionFactory;
     private Session _session;
 
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
     public SessionFactory getSessionFactory() {
         return _sessionFactory;
     }
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
 
     public void setSessionFactory(SessionFactory _sessionFactory) {
         this._sessionFactory = _sessionFactory;
 
         this._session = this._sessionFactory.openSession();
     }
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
 
     @Override
    @Transactional
     public void saveTestQuestion(TestQuestion test_question) throws TransactionError {
         if (this._session == null || !this._session.isOpen()) {
             throw new TransactionError("Session not opened");
         }
 
         this._session.beginTransaction();
         this._session.saveOrUpdate(test_question);
         this._session.getTransaction().commit();
     }
 
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
     @Override
     @SuppressWarnings("unchecked")
     public List<TestQuestion> listQuestionsWithTestId(long testId) throws TransactionError {
         if (this._session == null || !this._session.isOpen()) {
             throw new TransactionError("Session not opened");
         }
 
         return (List<TestQuestion>) this._session.createCriteria(TestQuestion.class)
                 .add(Restrictions.eq("_testId", testId)).list();
            
     }
         //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
     
     @Override
     @SuppressWarnings("unchecked")
     public List<TestQuestion> listQuestionsWithQuestionId(long questionId) throws TransactionError {
         if (this._session == null || !this._session.isOpen()) {
             throw new TransactionError("Session not opened");
         }
 
         return (List<TestQuestion>) this._session.createCriteria(TestQuestion.class)
                 .add(Restrictions.eq("_questionId", questionId)).list();
            
     }
 
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
     @Override
     public List<TestQuestion> listQuestionsWithLink(long testId, long questionId) throws TransactionError {
         if (this._session == null || !this._session.isOpen()) {
             throw new TransactionError("Session not opened");
         }
 
         return (List<TestQuestion>) this._session.createCriteria(TestQuestion.class)
                 .add(Restrictions.eq("_testId", testId))
                 .add(Restrictions.eq("_questionId", questionId)).list();
     }
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
     @Override
     public void deleteTestQuestion(TestQuestion question) throws TransactionError {
         if (this._session == null || !this._session.isOpen()) {
             throw new TransactionError("Session not opened");
         }
         
         this._session.beginTransaction();
         this._session.delete(question);
         this._session.getTransaction().commit();
     }
 
 }
