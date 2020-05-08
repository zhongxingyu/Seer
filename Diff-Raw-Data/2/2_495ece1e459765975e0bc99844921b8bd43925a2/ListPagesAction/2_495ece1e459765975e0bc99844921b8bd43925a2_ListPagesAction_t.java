 package com.jute.google.perf.action;
 
 import com.jute.google.framework.AbstractAction;
 import com.jute.google.framework.Path;
 import com.jute.google.perf.dao.PageDao;
 import com.jute.google.perf.dao.impl.PageDaoImpl;
 import com.jute.google.perf.model.Page;
 import com.jute.google.perf.model.DataPoint;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.users.UserService;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.cache.Cache;
 import java.util.Map;
 import java.util.List;
 
 /**
  * User: hugozhu
  * Date: Apr 25, 2009
  * Time: 11:57:57 PM
  */
 
 @Singleton
 @Path(id="/clear_data_points")
 public class ListPagesAction extends AbstractAction {
     @Inject
     PageDao pageDao;
 
     @Inject
     Cache cache;
     
     public String execute(Map context, HttpServletRequest req, HttpServletResponse resp) throws Exception {
         List<Page> pages = pageDao.getAllPages();
 
         if (cache!=null) {
             for(Page page: pages) {
                 DataPoint p = (DataPoint) cache.get(page.getUrl());
                 if (p!=null) {
                     page.getProperties().setProperty("code",p.getCode()+"");
                     page.getProperties().setProperty("last_total",p.getTotalTime()+"");
                     page.getProperties().setProperty("last_modified",p.getDate().getTime()/1000l+"");
                    page.getProperties().setProperty("length",p.getLength()+"");
                 }
             }
         }
         context.put("pages",pages);
         return "list_pages";
     }
 }
