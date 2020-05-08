 package ddth.dasp.status.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.framework.cache.ICacheManager;
 import ddth.dasp.status.DaspBundleConstants;
 
 public class CacheStatusController extends BaseController {
 
     private final static String VIEW_NAME = DaspBundleConstants.MODULE_NAME + ":cache";
 
     @RequestMapping
     public String handleRequest() {
         return VIEW_NAME;
     }
 
     @SuppressWarnings("unchecked")
     @ModelAttribute("CACHE")
    private Object buildModelCache() {
         List<Object> model = new ArrayList<Object>();
         Object temp = DaspGlobal.getGlobalVar(ICacheManager.GLOBAL_KEY);
         if (!(temp instanceof Map)) {
             temp = new HashMap<String, ICacheManager>();
         }
         Map<String, ICacheManager> allCacheManagers = (Map<String, ICacheManager>) temp;
         Map<String, Object> modelEntry;
         for (Entry<String, ICacheManager> entry : allCacheManagers.entrySet()) {
             modelEntry = new HashMap<String, Object>();
             modelEntry.put("id", entry.getKey());
             modelEntry.put("cacheManager", entry.getValue());
             model.add(modelEntry);
         }
         return model;
     }
 }
