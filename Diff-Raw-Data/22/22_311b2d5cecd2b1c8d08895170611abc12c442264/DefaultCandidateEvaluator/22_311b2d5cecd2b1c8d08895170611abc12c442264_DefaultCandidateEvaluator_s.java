 package org.rosenvold.spring.convention.candidateevaluators;
 
 import org.rosenvold.spring.convention.CandidateEvaluator;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Kristian Rosenvold
  */
 public class DefaultCandidateEvaluator implements CandidateEvaluator {
     @Override
     public boolean isBean(Class prospect) {
         //noinspection unchecked
        return prospect.getAnnotation(Repository.class) != null ||
                prospect.getAnnotation(Component.class) != null ||
                prospect.getAnnotation(Service.class) != null ||
                prospect.getAnnotation(Controller.class) != null;
     }
 }
