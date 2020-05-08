 package com.ssm.llp.biz.validation.script;
 
 import com.ssm.llp.core.dao.SsmFilterDao;
 import com.ssm.llp.core.dao.SsmNameDao;
 import com.ssm.llp.core.dao.impl.SsmFilterDaoImpl;
 import com.ssm.llp.core.dao.impl.SsmNameDaoImpl;
 import org.apache.bsf.BSFEngine;
 import org.apache.bsf.BSFException;
 import org.apache.bsf.BSFManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.util.*;
 
 /**
  * @author rafizan.baharum
  * @since 9/6/13
  */
 @Component("scriptManager")
 public class ScriptManager implements InitializingBean {
 
     private Logger log = LoggerFactory.getLogger(ScriptManager.class);
 
     private BSFEngine engine;
 
     private BSFManager manager;
 
     @Autowired
     private ApplicationContext context;
 
     @Autowired
     private ScriptLog scriptLog;
 
     @Autowired
     private ScriptUtil scriptUtil;
 
     @Autowired
     private SsmNameDao nameDao;
 
     @Autowired
     private SsmFilterDao filterDao;
 
     @PostConstruct
     public void init() {
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         manager = new BSFManager();
         BSFManager.registerScriptingEngine("beanshell", "bsh.util.BeanShellBSFEngine", new String[]{"bsh"});
         engine = manager.loadScriptingEngine("beanshell");
     }
 
     public void execute(String script, Map<String, Object> params) {
         try {
             registerParameters(params);
             engine.eval(null, 0, 0, script);
         } catch (BSFException e) {
             e.printStackTrace();
         }
     }
 
     public boolean executePoisonFilter(String script, Map<String, Object> params) {
         try {
             registerParameters(params);
             return (Boolean) engine.eval(null, 0, 0, script);
         } catch (BSFException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public boolean executeSearchFilter(String script, Map<String, Object> params) {
         try {
             registerParameters(params);
             return (Boolean) engine.eval(null, 0, 0, script);
         } catch (BSFException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     private void registerParameters(Map<String, Object> params) {
         try {
             manager.declareBean("nameDao", nameDao, SsmNameDaoImpl.class);
             manager.declareBean("filterDao", filterDao, SsmFilterDaoImpl.class);
             manager.declareBean("name", params.get("name"), String.class);
             manager.declareBean("log", scriptLog, ScriptLog.class);
            manager.declareBean("util", scriptUtil, ScriptUtil.class);
         } catch (BSFException e) {
             e.printStackTrace();
         }
 //        Set<String> strings = params.keySet();
 //        for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
 //            String next = iterator.next();
 //        }
     }
 
 }
