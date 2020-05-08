 package com.belerweb.pentaho.ui.service;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.belerweb.pentaho.ui.bean.Option;
 import com.belerweb.pentaho.ui.dao.OptionDao;
 
 @Service
 public class OptionService {
 
   @Autowired
   private OptionDao optionDao;
 
   private Map<String, String> cachedOptions;
   private long lastCachedTime;
 
   public Map<String, String> getOptions() {
     if (cachedOptions == null || System.currentTimeMillis() - lastCachedTime > 36000) {
       cachedOptions = new HashMap<String, String>();
       for (Option option : optionDao.listOption()) {
         cachedOptions.put(option.getName(), option.getValue());
       }
     }
 
     return cachedOptions;
   }
 
   @Transactional
   public void setOption(String name, String value) {
    Option option = optionDao.get(name);
    if (option == null) {
      option = new Option(name, value);
    }
    optionDao.save(option);
     cachedOptions = null;
   }
 
 }
