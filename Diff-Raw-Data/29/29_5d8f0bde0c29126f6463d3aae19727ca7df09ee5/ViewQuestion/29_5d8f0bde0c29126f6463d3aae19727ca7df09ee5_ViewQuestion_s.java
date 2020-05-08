 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.pages.contribution.question;
 
 import com.preppa.web.data.QuestionDAO;
 import com.preppa.web.entities.Question;
 import java.util.List;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.annotations.InjectPage;
 
 /**
  *
  * @author newtonik
  */
 public class ViewQuestion {
     @Inject
     private QuestionDAO questionDAO;
 
 	@Persist(PersistenceConstants.FLASH)
 	private String _errorMessage;
 
     @Property
     @Persist
     private Question question;
 
 	void onAction(String pass) {
 		/*try {
 			list.set(pass);
 			return list;
 		}
 		catch (Exception e) {
 			_errorMessage = e.getMessage();
 			return null;
 		}*/
 	}
 
 
     Object onActivate() {
 
         return null;
     }
 
     /**
      * @return the the articles queried
      */
     public List<Question> getAllQuestions() {
         List<Question> returnVal;
 
        returnVal = questionDAO.findAll();
 
         return returnVal;
     }
 
 }
