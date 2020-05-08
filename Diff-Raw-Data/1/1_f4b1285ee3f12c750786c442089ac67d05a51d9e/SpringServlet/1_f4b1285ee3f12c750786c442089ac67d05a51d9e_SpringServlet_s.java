 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package net.lilylnx.springnet;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 import org.springframework.web.servlet.DispatcherServlet;
 
 import net.lilylnx.springnet.core.SessionManager;
 import net.lilylnx.springnet.core.support.spring.ViewResolver;
 import net.lilylnx.springnet.extension.RequestOperationChain;
 import net.lilylnx.springnet.util.ConfigKeys;
 import net.lilylnx.springnet.util.SpringConfig;
 
 import mantech.domain.UserSession;
 
 /**
  * Đây là Servlet chính, kế thừa từ {@link DispatcherServlet}
  * của SpringMVC dùng để điều khiển các modules của hệ thống,
  * dễ dàng thiết lập và tùy biến chức năng.
  * 
  * @author Tien Nguyen
  * @version $Id: SpringServlet.java,v 1.0 2011/06/22 15:30:33 lilylnx Exp $
  */
 public class SpringServlet extends DispatcherServlet {
 
   private static final long serialVersionUID = 6672351209776527508L;
   
   private static final Logger LOG = Logger.getLogger(SpringServlet.class);
   private SpringConfig config;
   private SessionManager sessionManager;
   private RequestOperationChain operationChain;
 
   public SpringServlet() {}
 
   /**
    * Phương thức init() được thực hiện ngay sau khi servlet này được khởi tạo xong.
    * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
     LOG.info(String.format("<< INITIALIZING [%s] >>", SpringServlet.class.getName()));
 
     super.init(config);
 
     // Đặt tên attribute cho ApplicationContext (được nạp vào ServletContext attribute)
     this.setContextAttribute(ConfigKeys.SPRING_CONTEXT);
 
     // Gán ApplicationContext vào ServletContext với SPRING_CONTEXT key
     ApplicationContext beanFactory = (ApplicationContext)this.getServletContext()
         .getAttribute(getServletContextAttributeName());
     this.getServletContext().setAttribute(ConfigKeys.SPRING_CONTEXT, beanFactory);
 
     this.config = beanFactory.getBean(SpringConfig.class);
     this.config.setProperty(ConfigKeys.APPLICATION_PATH, getServletContext().getRealPath(""));
     
     this.sessionManager = beanFactory.getBean(SessionManager.class);
     this.operationChain = beanFactory.getBean(RequestOperationChain.class);
 
     showStuff(beanFactory);
 
     LOG.info("<< COMPLETED! >>");
   }
 
   /**
    * Được thực thi với mỗi request từ client.
    * @see javax.servlet.Servlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     request.setAttribute(ConfigKeys.ANONYMOUS_USER_ID, config.getInt(ConfigKeys.ANONYMOUS_USER_ID, 1));
     request.setAttribute(ConfigKeys.HTTP_SERVLET_RESPONSE, response);
 
     ServletRequestAttributes attributes = new ServletRequestAttributes(request);
     
     try {
       RequestContextHolder.setRequestAttributes(attributes);
       
       UserSession userSession = this.sessionManager.refreshSession(request, response);
       
       request.setAttribute(ConfigKeys.USER_SESSION, userSession);
       request.setAttribute(UserSession.class.getName(), userSession);
       
       this.operationChain.callAllOperations();
       this.putDefaultProps(request);
 
       super.service(request, response);
     }
     finally {
       RequestContextHolder.resetRequestAttributes();
       attributes.requestCompleted();
     }
   }
   
   /**
    * Đưa một số key/value cho view.
    */
   private void putDefaultProps(HttpServletRequest request) {
     ViewResolver viewResolver = (ViewResolver)SpringNet.getComponent("viewResolver");
 
     Map<String, Object> defaultAttributes = viewResolver.getAttributesMap();
     Date now = Calendar.getInstance().getTime();
 
     // Cached attributes
     defaultAttributes.put("name", config.getString("name"));
     defaultAttributes.put("version", config.getString("version"));
     defaultAttributes.put("webpage", config.getString("link.webpage"));
     defaultAttributes.put("contextPath", config.getString("context.path"));
     defaultAttributes.put("ext", config.getString("servlet.extension"));
     defaultAttributes.put("encoding", config.getString("encoding"));
     defaultAttributes.put("dateTimeFormat", config.getString("dateTime.format"));
     defaultAttributes.put("now", now);
     defaultAttributes.put("timestamp", new Long(System.currentTimeMillis()));
     defaultAttributes.put("config", config);
     
     defaultAttributes.put("pageTitle", config.getString("web.page.title"));
     defaultAttributes.put("metaKeywords", config.getString("web.page.metatag.keywords"));
     defaultAttributes.put("metaDescription", config.getString("web.page.metatag.description"));
 
     // Non-cached attributes
     request.setAttribute("p", request.getMethod().equalsIgnoreCase("GET") ? request.getParameter("p") : null);
   }
 
   private void showStuff(ApplicationContext beanFactory) {
     LOG.info("=== Loaded beans ===");
     for (String bean : beanFactory.getBeanDefinitionNames()) {
       LOG.info(bean);
     }
     LOG.info(String.format("Deployed in: %s", this.config.getString(ConfigKeys.APPLICATION_PATH)));
   }
 
 }
