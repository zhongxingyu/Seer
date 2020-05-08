 package com.ssm.llp.biz.validation;
 
 import com.google.common.base.Strings;
import com.ssm.llp.biz.validation.script.ScriptManager;
import com.ssm.llp.core.dao.SsmFilterDao;
 import com.ssm.llp.core.model.SsmFilter;
 import com.ssm.llp.core.model.SsmFilterType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.Ordered;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author rafizan.baharum
  * @since 9/22/13
  */
 @Component("searchValidator")
 public class SearchValidator extends ValidatorSupport implements Validator, Ordered {
 
     private Logger log = LoggerFactory.getLogger(SearchValidator.class);
 
     @Override
     public boolean validate(String name, ValidatorContext context) {
        List<SsmFilter> filters = filterDao.find(SsmFilterType.POISON);
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("name", name);
         map.put("context", context);
         boolean result = false;
         for (SsmFilter filter : filters) {
             result = scriptManager.executeSearchFilter(filter.getScript(), map);
             log.debug(filter.getName() + ":" + Strings.padStart(String.valueOf(result), 10, '*'));
             if (result) {
                 context.setError(filter.getError());
                 break;
             }
         }
         context.setPoisoned(result);
         return result;
     }
 
     @Override
     public int getOrder() {
         return Ordered.LOWEST_PRECEDENCE;
 
     }
 }
