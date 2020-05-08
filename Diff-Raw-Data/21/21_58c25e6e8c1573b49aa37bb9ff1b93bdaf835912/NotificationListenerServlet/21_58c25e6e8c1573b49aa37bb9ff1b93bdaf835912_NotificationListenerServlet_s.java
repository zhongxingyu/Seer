 package com.mediaplatform.web;
 
 import com.mediaplatform.manager.ConfigBean;
 import com.mediaplatform.model.Event;
 import com.mediaplatform.model.EventType;
 import com.mediaplatform.model.LiveStream;
 import com.mediaplatform.model.User;
 import org.apache.commons.lang.StringUtils;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * User: timur
  * Date: 1/2/13
  * Time: 7:30 PM
  */
 public class NotificationListenerServlet extends HttpServlet {
     public static final String CALL_PARAM_NAME = "call";
     public static final String ADDR_PARAM_NAME = "addr";
     public static final String APP_PARAM_NAME = "app";
     public static final String FLASH_VER_PARAM_NAME = "flashVer";
     public static final String SWF_URL_PARAM_NAME = "swfUrl";
     public static final String TC_URL_PARAM_NAME = "tcUrl";
     public static final String PAGE_URL_PARAM_NAME = "pageUrl";
     public static final String STREAM_NAME_PARAM_NAME = "streamName";
     public static final String UNAME_PARAM_NAME = "uname";
     public static final String CONTENT_NAME_PARAM_NAME = "cname";
     @Inject
     protected ConfigBean configBean;
 //
 //    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
 //    private EntityManager entityManager;
 
     @Inject
     @Named(value = "appEm")
     protected EntityManager entityManager;
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         doGet(req, resp);
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         process(req);
     }
 
 
     //TODO authentication checking
     protected void process(HttpServletRequest req){
         EventType type = EventType.valueOf(req.getParameter(CALL_PARAM_NAME));
         String addr = req.getParameter(ADDR_PARAM_NAME);
         String app = req.getParameter(APP_PARAM_NAME);
         String flashVer = req.getParameter(FLASH_VER_PARAM_NAME);
         String swfUrl = req.getParameter(SWF_URL_PARAM_NAME);
         String tcUrl = req.getParameter(TC_URL_PARAM_NAME);
         String pageUrl = req.getParameter(PAGE_URL_PARAM_NAME);
         String streamName = req.getParameter(STREAM_NAME_PARAM_NAME);
         String userName = req.getParameter(UNAME_PARAM_NAME);
         String contentName = req.getParameter(CONTENT_NAME_PARAM_NAME);
 
         Event event = new Event(type, addr, app, flashVer, swfUrl, tcUrl, pageUrl, streamName, contentName);
         if(StringUtils.isNotBlank(userName)){
             try {
                 User user = (User) entityManager.createQuery("select u from User u where u.username = :userName").setParameter("userName", userName).getSingleResult();
                 event.setUser(user);
             } catch (NoResultException e) {
                 //empty
             }
         }
         entityManager.persist(event);
 
         //todo rise event
         if(EventType.publish_done == type){
             LiveStream liveStream = (LiveStream) entityManager.createQuery("select s from LiveStream s where s.title = :streamName").setParameter("streamName", contentName).getSingleResult();
             liveStream.setPublished(false);
             entityManager.merge(liveStream);
         }
     }
 }
