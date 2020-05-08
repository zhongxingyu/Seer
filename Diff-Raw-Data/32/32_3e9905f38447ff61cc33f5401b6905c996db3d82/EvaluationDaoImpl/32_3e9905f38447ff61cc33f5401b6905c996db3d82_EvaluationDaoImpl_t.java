 package web.dao.impl;
 
import java.util.List;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 import web.dao.EvaluationDao;
 import web.model.Evaluation;
 import web.model.Property;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 
 @Repository
 public class EvaluationDaoImpl extends AbstractDaoImpl<Evaluation, Integer> implements EvaluationDao {
 
     protected EvaluationDaoImpl() {
         super(Evaluation.class);
     }
     
     @Override
     public Evaluation findByProperty(Property property) {
        List<Evaluation> res = findByCriteria(Restrictions.eq("property", property));
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return null;
     }
     
     @Override
     public Boolean saveEvaluation(Evaluation eval) {
         if (eval == null) {
             return false;
         }
         saveOrUpdate(eval);
         return true;
     }
 }
