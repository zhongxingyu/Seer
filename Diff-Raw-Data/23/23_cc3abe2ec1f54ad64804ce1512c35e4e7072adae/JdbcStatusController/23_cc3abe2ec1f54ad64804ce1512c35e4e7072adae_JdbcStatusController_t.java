 package ddth.dasp.status.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.framework.dbc.IJdbcFactory;
 import ddth.dasp.status.DaspBundleConstants;
 
 public class JdbcStatusController extends BaseController {
 
     private final static String VIEW_NAME = DaspBundleConstants.MODULE_NAME + ":jdbc";
 
     @RequestMapping
     public String handleRequest() {
         return VIEW_NAME;
     }
 
     @SuppressWarnings("unchecked")
     @ModelAttribute("JDBC")
    public Object buildModelJdbc() {
         List<Object> model = new ArrayList<Object>();
         Object temp = DaspGlobal.getGlobalVar(IJdbcFactory.GLOBAL_KEY);
         if (!(temp instanceof Map)) {
             temp = new HashMap<String, IJdbcFactory>();
         }
         Map<String, IJdbcFactory> allJdbcFactories = (Map<String, IJdbcFactory>) temp;
         Map<String, Object> modelEntry;
         for (Entry<String, IJdbcFactory> entry : allJdbcFactories.entrySet()) {
             modelEntry = new HashMap<String, Object>();
             modelEntry.put("id", entry.getKey());
             modelEntry.put("jdbcFactory", entry.getValue());
             model.add(modelEntry);
         }
         return model;
     }
 }
