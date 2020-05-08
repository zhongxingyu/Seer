 package ddth.dasp.status.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 import ddth.dasp.common.utils.OsgiUtils;
 import ddth.dasp.status.DaspBundleConstants;
 
 public class OsgiStatusController extends BaseController {
 
     private final static String VIEW_NAME = DaspBundleConstants.MODULE_NAME + ":osgi";
 
     @RequestMapping
     public String handleRequest() {
         return VIEW_NAME;
     }
 
     @ModelAttribute("OSGI")
    private Object buildModelOsgi() {
         List<Object> model = new ArrayList<Object>();
 
         IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
         BundleContext bc = osgiBootstrap.getBundleContext();
         Bundle[] bundles = bc.getBundles();
 
         for (Bundle bundle : bundles) {
             Map<String, Object> bundleModel = new HashMap<String, Object>();
             model.add(bundleModel);
             bundleModel.put("id", bundle.getBundleId());
             bundleModel.put("name", bundle.getSymbolicName() != null ? bundle.getSymbolicName()
                     : "null");
             bundleModel.put("version", bundle.getVersion());
             bundleModel.put("state", OsgiUtils.getBundleStateAsString(bundle));
         }
 
         return model;
     }
 }
