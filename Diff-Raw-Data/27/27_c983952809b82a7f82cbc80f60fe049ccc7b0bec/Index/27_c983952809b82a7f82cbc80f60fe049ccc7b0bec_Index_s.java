   
   /*
    * Preppa, Inc.
    * 
    * Copyright 2009. All rights
   reserved.
    * 
    * $Id$
    */
 
 package com.preppa.web.pages.contribution.question.awaiting;
 
 import com.preppa.web.data.QuestionDAO;
 import com.preppa.web.data.VoteDAO;
 import com.preppa.web.entities.Question;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.chenillekit.tapestry.core.components.SlidingPanel;
 
 /**
  *
  * @author Jan Jan
  */
 public class Index {
     @Property
     private List<Question> nonApproved;
     @Inject
     private QuestionDAO questionDAO;
     @Property
     private Question question;
     @Inject
     private VoteDAO voteDAO;
    @Component(parameters = {"subject=Critical Reading", "closed=true", "options={duration:0.0}"})
     private SlidingPanel cr;
     @Component(parameters = {"subject=Math", "closed=true", "options={duration:0.0}"})
     private SlidingPanel math;
     @Component(parameters = {"subject=Writing", "closed=true", "options={duration:0.0}"})
    private SlidingPanel writing;
     
     void onActivate() {
         List<Question> qTemp = questionDAO.findAll();
         nonApproved = new ArrayList<Question>();
 
         for (int i = 0; i < qTemp.size(); i++) {
 
             if (voteDAO.findSumByQuestionId(qTemp.get(i).getId()) < 1 && qTemp.get(i).getFlags().isEmpty() && nonApproved.contains(qTemp.get(i)) == false) {
                 nonApproved.add(qTemp.get(i));
             }
         }
     }
 }
