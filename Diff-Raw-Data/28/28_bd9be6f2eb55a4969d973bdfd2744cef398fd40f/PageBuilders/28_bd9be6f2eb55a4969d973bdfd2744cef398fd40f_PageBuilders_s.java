 package com.wideplay.warp.internal.pages;
 
 import com.wideplay.warp.rendering.PageHandler;
 
 import javax.servlet.ServletContext;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * On: Mar 24, 2007 10:38:12 PM
  *
  * @author Dhanji R. Prasanna
  */
 public class PageBuilders {
 
     private PageBuilders() {
     }
 
     public static void buildAndStorePageHandlers(ServletContext context, Class<?> pageClass, String packageName, Map<String, PageHandler> pages) {
         new PageHandlerBuilder(context).build(pageClass, packageName, pages);
     }
 }
