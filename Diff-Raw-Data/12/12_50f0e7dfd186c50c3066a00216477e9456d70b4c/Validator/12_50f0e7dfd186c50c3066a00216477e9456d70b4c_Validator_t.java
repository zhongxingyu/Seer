 package com.ssm.llp.biz.validation;
 
 import com.ssm.llp.biz.validation.script.ScriptManager;
 import com.ssm.llp.core.dao.SsmFilterDao;
 import com.ssm.llp.core.model.SsmFilter;
 import com.ssm.llp.core.model.SsmFilterType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author rafizan.baharum
  * @since 9/19/13
  */
 @Component("validator")
 public class Validator {
 
     private Logger log = LoggerFactory.getLogger(Validator.class);
 
     @Autowired
     private SsmFilterDao filterDao;
 
     @Autowired
     private ScriptManager scriptManager;
 
     public ValidatorContext validate(String name) {
         ValidatorContext context = new ValidatorContext();
        if (checkPoisoned(name, context)) return context;
         checkExisted(name, context);
         return context;
     }
 
    private boolean checkPoisoned(String name, ValidatorContext context) {
         List<SsmFilter> filters = filterDao.find(SsmFilterType.POISON);
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("name", name);
         boolean result = false;
         for (SsmFilter filter : filters) {
             result = scriptManager.executePoisonFilter(filter.getScript(), map);
             log.debug(filter.getName() + ":" + result);
             if (result) {
                 context.setWaived(filter.isWaived());
                 context.setError(filter.getError());
                 break;
             }
         }
         context.setPoisoned(result);
        return result;
     }
 
    private boolean checkExisted(String name, ValidatorContext context) {
         List<SsmFilter> filters = filterDao.find(SsmFilterType.SEARCH);
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("name", name);
         boolean result = false;
         for (SsmFilter filter : filters) {
             result = scriptManager.executeSearchFilter(filter.getScript(), map);
             log.debug(filter.getName() + ":" + result);
             if (result) {
                 context.setWaived(filter.isWaived());
                 context.setError(filter.getError());
                 break;
             }
         }
         context.setExisted(result);
        return result;
     }
 }
