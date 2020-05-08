 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.data;
 
 import com.preppa.web.entities.Question;
 import com.preppa.web.entities.Questiontype;
 import java.util.List;
 import org.chenillekit.hibernate.daos.GenericDAO;
 
 /**
  *
  * @author nwt
  */
 public interface QuestionDAO extends GenericDAO <Question, Integer> {
     Question findById(Integer id);
     List<Question> findByQuestiontype(Questiontype q);
     List<Question> findByQuestiontype(Integer q);
     List<Question> findByTag(String name);
     
     
 }
