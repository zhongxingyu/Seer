 package controllers.cms;
 
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import models.cms.NavigationItem;
 import models.cms.NavigationMappedItem;
 import models.cms.SeoParameter;
 import models.cms.Translation;
 import models.cms.VirtualPage;
 import models.cms.VirtualPageTemplate;
import play.i18n.Lang;
 import play.mvc.Controller;
 import play.mvc.Http.Cookie;
 import play.templates.JavaExtensions;
 import plugins.cms.navigation.NavigationCache;
 import plugins.router.Route;
 
 /**
  * @author benoit
  */
 @Route("/--cms")
 public class CmsController extends Controller {
     
     @Route("/clearcache")
     public static void ClearCache(){
         
         NavigationCache.init();
     }
     
     public static void redirectMappedItem(){
         
        String lang     = Lang.get();
         String resource = request.path;
         
         NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
         
         if (mappedItem != null){
             
             redirect(mappedItem.source);
         }
     }
     
     public static void virtualPage(){
         
         String resource = request.path;
         
         VirtualPage virtualPage = NavigationCache.getVirtualPage(resource);
         if (virtualPage != null){
             
             renderTemplate(virtualPage.view);
         }
     }
 	
 	public static void pageNotFound(){
 				
 		notFound();
 	}
 	
     
     @Route("/navigation")
     public static void manageNavigation(String path){
         
         if (path == null){
             path = "/";
         }
         
         NavigationItem openedNavItem = NavigationItem.findByPath(path);
         
         renderTemplate("cms/navigation.html", openedNavItem);
     }
     
     @Route("/navigation/create")
     public static void create(Long parentid, String name, Integer pos){
         
         Map<String,Object> result = new HashMap<String, Object>();
         boolean status = false;
         
         try {
             NavigationItem parentItem = NavigationItem.findById(parentid);
 
             String path = "/" + JavaExtensions.slugify(name);
             if (parentItem != null){
 
                 path = parentItem.path + path;
             }
 
             NavigationItem navigationItem = new NavigationItem();
             navigationItem.name     = name;
             navigationItem.path     = path;
             navigationItem.parent   = parentItem;
             navigationItem.position = pos;
 
             
             navigationItem.save();
             result.put("id", navigationItem.id);
             status = true;
             
         }
         catch (Exception ex) {}
         
         result.put("status", status);
         renderJSON(result);
     }
     
     @Route("/navigation/rename")
     public static void rename(Long navid, String newname){
         
         Map<String,Object> result = new HashMap<String, Object>();
         boolean status = false;
         
         NavigationItem navItem= NavigationItem.findById(navid);
         
         if (navItem != null){
             
             List<Translation> translations = Translation.findByCode(navItem.name);
             for (Translation translation : translations){
                 
                 translation.code = newname;
                 translation.save();
             }
             
             navItem.name = newname;
             navItem.save();
             status = true;
         }
         
         result.put("status", status);
         renderJSON(result);
     }
     
     @Route("/navigation/remove")
     public static void remove(Long navid){
         
         Map<String,Object> result = new HashMap<String, Object>();
         boolean status = false;
         
         NavigationItem navItem= NavigationItem.findById(navid);
         if (navItem != null){
             
             List<NavigationMappedItem> mappedItems = NavigationMappedItem.findBySource(navItem.path);
             for (NavigationMappedItem mappedItem : mappedItems){
                 
                 mappedItem.delete();
             }
             
             navItem.delete();
             status = true;
         }
         
         result.put("status", status);
         renderJSON(result);
     }
     
     @Route("/navigation/move")
     public static void move(Long parentid, Long navid, Long pos){
         
         Map<String,Object> result = new HashMap<String, Object>();
         boolean status = false;
         
         try {
             NavigationItem parent = NavigationItem.findById(parentid);
         
             List<NavigationItem> items = NavigationItem.findByParentAndPos(parent, pos);
             int n=0;
             for (NavigationItem ni : items){
                 ni.position = pos + n++;
                 ni.save();
             }
 
             NavigationItem navItem= NavigationItem.findById(navid);
 
             navItem.parent   = parent;
             navItem.position = pos;
             navItem.save();
         }
         catch (Exception ex) {}
         
         result.put("status", status);
         renderJSON(result);
     }
     
     @Route("/navigation/edit")
     public static void edit(Long navid, Boolean update){
         
         NavigationItem  navItem = NavigationItem.findById(navid);
         List<String>    langs   = Translation.findDistinctLanguages();
         
         if (update != null){
             
             boolean success = false;
             Map<String,Object> results = new HashMap<String, Object>();
             
             try {
                 
                 String newpath = params.get("fr_url");
                 
                 for (String lang : langs){
                     
                     if (!"fr".equals(lang)){
                     
                         /**
                          * Gestion des urls
                          */
                         NavigationMappedItem mappedItem = NavigationMappedItem.findBySourceAndLang(navItem.path, lang);
                         String lang_url = params.get(lang + "_url").trim();
 
                         if (lang_url.isEmpty()){
 
                             if (mappedItem != null){
                                 mappedItem.delete();
                             }
                         }
                         else {
 
                             if (mappedItem == null){
 
                                 mappedItem = new NavigationMappedItem();
                                 mappedItem.language = lang;
                             }
 
                             mappedItem.source       = newpath;
                             mappedItem.destination  = lang_url;
 
                             mappedItem.save();
                         }
                     }
                     
                     /**
                      * Trad de la nav
                      */
                     String lang_trad = params.get(lang + "_trad");
                     Translation translation = Translation.findByCodeAndLanguage(navItem.name, lang);
                     if (translation == null){
                         translation = new Translation();
                         translation.code     = navItem.name;
                         translation.language = lang;
                     }
                     translation.value = lang_trad;
                     translation.save();
                     
                     /**
                      * Gestion du seo
                      */
                     SeoParameter seo = SeoParameter.findByPathAndLang(navItem.path, lang);
                     
                     if (seo == null){
                         
                         seo = new SeoParameter();
                         seo.language = lang;
                     }
                     seo.path        = newpath;
                     
                     seo.title       = JavaExtensions.addSlashes(params.get(lang + "_meta_title"));
                     seo.keywords    = JavaExtensions.addSlashes(params.get(lang + "_meta_keywords"));
                     seo.description = JavaExtensions.addSlashes(JavaExtensions.escape(params.get(lang + "_meta_desc")));
                     seo.robots      = params.get(lang + "_robots");
                     
                     seo.inSitemap   = (params.get(lang + "_insitemap") != null);
                     seo.frequency   = params.get(lang + "_freq");
                     seo.priority    = params.get(lang + "_prio", BigDecimal.class);
                     
                     seo.save();
                 }
                 
                VirtualPage vp = VirtualPage.findByPath(navItem.path);
                if (vp != null){
                    
                    vp.path = newpath;
                    vp.save();
                }
                
                navItem.active = (params.get("navitem_active") != null);
                 navItem.path   = newpath;
                 navItem.save();
                 
 				NavigationCache.initNavigationItem();
 				NavigationCache.initNavigationMappedItem();
 				NavigationCache.initVirtualPage();
                 success = true;
             }
             catch (Exception ex) {
                 
                 Writer stacktrace = new StringWriter();
                 PrintWriter printWriter = new PrintWriter(stacktrace);
                 ex.printStackTrace(printWriter);
                 
                 results.put("error", ex.getMessage());
                 results.put("stacktrace", stacktrace.toString());
             }
             
             results.put("success", success);
             renderJSON(results);
         }
         else {
             
             List<VirtualPageTemplate> templates = VirtualPageTemplate.all().fetch();
 
             VirtualPageTemplate virtualPageTemplate = null;
             VirtualPage virtualPage = VirtualPage.findByPath(navItem.path);
             if (virtualPage != null){
 
                 for (VirtualPageTemplate template : templates){
 
                     if (template.view.equals(virtualPage.view)){
                         virtualPageTemplate = template;
                         break;
                     }
                 }
             }
             
             Cookie tabCookie    = request.cookies.get("cms-edit-nav-last-tab");
             String selectedTab  = (tabCookie == null) ? "#cms_nav_urls" : tabCookie.value;
             
             Cookie langCookie   = request.cookies.get("cms-edit-nav-last-lang");
             String selectedLang = (langCookie == null) ? "fr" : langCookie.value;
             
 
             Map<String, SeoParameter> seos = new HashMap<String, SeoParameter>();
             Map<String, NavigationMappedItem> mappedItems = new HashMap<String, NavigationMappedItem>();
             for (String lang : langs){
 
                 NavigationMappedItem mappedItem = NavigationMappedItem.findBySourceAndLang(navItem.path, lang);
                 mappedItems.put(lang, mappedItem);
 
                 SeoParameter seoParameter = SeoParameter.findByPathAndLang(navItem.path, lang);
                 seos.put(lang, seoParameter);
             }
 
             renderTemplate("cms/nav-edit.html", navItem, virtualPage, virtualPageTemplate, templates, langs, selectedLang, selectedTab, mappedItems, seos);
         }
     }
         
     /**
      * @deprecated 
      * 
      * @param item
      * @param arbo
      * @return 
      */
     private static String reverseWay(NavigationItem item, String arbo){
         
         NavigationItem parent = item.parent;
         
         String node = "<ul>";
         
         List<NavigationItem> items = NavigationItem.findByParent(parent);
         for (NavigationItem navigationItem : items){
 
             
             node += "<li id=\"nav"+navigationItem.id+"\"><a href=\"#\">" + navigationItem.name + "</a>";
             
             if (navigationItem.equals(item)){
                 
                 node +=  arbo;
             }
             
             node += "</li>";
         }
         
         if (items.isEmpty()){
             
             node += "<li id=\"nav"+item.id+"\"><a href=\"#\">" + item.path + "</a>"+ arbo +"</li>";
         }
         
         node +="</ul>";
         
         
         if (parent != null){
             
             node = reverseWay(parent, node);           
         }
         
         return node; 
     }
 }
