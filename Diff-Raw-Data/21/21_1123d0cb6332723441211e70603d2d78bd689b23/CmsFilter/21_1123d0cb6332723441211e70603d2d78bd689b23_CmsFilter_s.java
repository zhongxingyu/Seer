 package plugins.cms;
 
 
 import plugins.cms.navigation.NavigationCache;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.URLDecoder;
 import models.cms.NavigationItem;
 import models.cms.NavigationMappedItem;
 import models.cms.User;
 import models.cms.VirtualPage;
 import org.hibernate.Filter;
 import play.Logger;
 import play.PlayPlugin;
 import play.db.jpa.JPA;
 import play.i18n.Lang;
 import play.mvc.Http.Request;
 import play.mvc.Router;
 import play.mvc.Router.Route;
 import play.mvc.Scope;
 import play.mvc.Scope.RenderArgs;
 
 
 /**
  * @author benoit
  */
 public class CmsFilter extends PlayPlugin {
 
     
     @Override
     public void routeRequest(Request request) {
         
         CmsContext.current.set(new CmsContext());
         
        String lang     = "fr";//Lang.get();
         String resource = request.path;
         
        
         VirtualPage virtualPage = NavigationCache.getVirtualPage(resource);
         if (virtualPage != null){
             
             // handle virtual page
             request.path = Router.reverse("cms.CmsController.virtualPage").url;
             return;
         }
         
         NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
         if (mappedItem != null){
             
             if (mappedItem.redirect){
                 
                 request.path = Router.reverse("cms.CmsController.redirectMappedItem").url;
                 return;
             }
             else {
                 
                 request.path = mappedItem.source;
             }
         }
     }
     
     @Override
     public void onRequestRouting(Route route) {
         
         Request request = Request.current();
         
         try {
             
             String uri      = request.url;
             String encoding = request.encoding;
             
             final int i = uri.indexOf("?");
             
             String path = URLDecoder.decode(uri, encoding);
             if (i != -1) {
                 path = URLDecoder.decode(uri.substring(0, i), encoding);   
             }
 
             request.path = path;
             //Logger.info("reset path to :" + path);
             
         } catch (UnsupportedEncodingException ex) {
             
             Logger.error("[CustomRoutingPlugin] Unable to reset request.path");
         }
     }
     
     @Override
     public void beforeActionInvocation(Method method) {
         
         CmsContext cmsContext = CmsContext.current();
         
         String lang     = Lang.get();
         String resource = Request.current().path;
         
         
         /**
          * handle navigation
          */
         NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
         if (mappedItem != null && !mappedItem.redirect){
             
             resource = mappedItem.source;
         }
         RenderArgs.current().put("__REQUESTED_RESOURCE", resource);
         cmsContext.requestedResource = resource;
         
         NavigationItem item = NavigationCache.get(resource);
         if (item != null){
             RenderArgs.current().put("__CURRENT_NAVIGATION_ITEM", item);
             cmsContext.currentNavigationItem = item;
         }
         
         RenderArgs.current().put("cms", cmsContext);
         
         /**
          * handle user
          */
         Scope.Session session    = Scope.Session.current();
         Scope.Params  params     = Request.current().params;
         
         
         String email    = params.get("cms_email");
         String password = params.get("cms_password");
         String logout   = params.get("cms_logout");
         
         if (logout != null){
             
             session.remove(CmsContext.Constant.CMS_USER);
         }
         
         if (email != null && password != null){
             
             User user = User.find("byMailAndPassword", email, password).first();
             
             if (user != null){
                 
                 session.put(CmsContext.Constant.CMS_USER, user.id);
             }
             else {
                 session.remove(CmsContext.Constant.CMS_USER);
             }
         }
         
         /**
          * handle hibernate filter
          */
        org.hibernate.Session hibernateSession = ((org.hibernate.Session)JPA.em().getDelegate());
         
        Filter filter = hibernateSession.enableFilter("langFilter");
        if (filter != null){
            filter.setParameter("langFilterParam", lang);
        }
         
     }
 }
