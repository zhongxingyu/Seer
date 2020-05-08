 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.util.EventListener;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletContextAttributeListener;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextAttributeEvent;
 
 import javax.servlet.http.HttpSessionListener;
 import javax.servlet.http.HttpSessionAttributeListener;
 import javax.servlet.http.HttpSessionActivationListener;
 import javax.servlet.http.HttpSessionBindingListener;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionBindingEvent;
 
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** An event listener that delegates the event handling to a bean obtained from
  * the spring application context.
  *
  * It forwards all events to a spring configured listener defined under the
 * bean named, by default, 'katari.moduleListenerProxy' in the spring
 * application context.
  *
  * The specific bean name is configured using the 'listenerBeanName' context
  * parameter.
  */
 public final class SpringBootstrapListener implements ServletContextListener,
        ServletContextAttributeListener, HttpSessionActivationListener,
        HttpSessionAttributeListener, HttpSessionListener,
        HttpSessionBindingListener {
 
   /** The serialization version number.
    *
    * This number must change every time a new serialization incompatible change
    * is introduced in the class.
    */
   private static final long serialVersionUID = 1;
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(SpringBootstrapListener.class);
 
   /** The target filter that this filter delegates all requests.
    *
    * This is null until initialization (call to init), not null afterwards.
    */
   private EventListener delegate = null;
 
   /*
    * ServletContextListener methods.
    */
 
   /** Notification that the web application is ready to process requests.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextListener.
    *
    * @param event The servlet context event. It cannot be null.
    */
   public void contextInitialized(final ServletContextEvent event) {
     log.trace("Entering contextInitialized");
     String beanName;
     beanName = event.getServletContext().getInitParameter("listenerBeanName");
     if (beanName == null) {
       beanName = "katari.moduleListenerProxy";
     }
 
     BeanFactory context = getBeanFactory(event.getServletContext());
     delegate = (EventListener) context.getBean(beanName);
     if (delegate instanceof ServletContextListener) {
       ((ServletContextListener) delegate).contextInitialized(event);
     }
     log.trace("Leaving contextInitialized");
   }
 
   /** Notification that the servlet context is about to be shut down.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextListener.
    *
    * @param event The servlet context event. It cannot be null.
    */
   public void contextDestroyed(final ServletContextEvent event) {
     log.trace("Entering contextDestroyed");
     if (delegate instanceof ServletContextListener) {
       ((ServletContextListener) delegate).contextDestroyed(event);
     }
     log.trace("Leaving contextDestroyed");
   }
 
   /*
    * ServletContextAttributeListener methods.
    */
 
   /** Notification that a new attribute was added to the servlet context.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextAttributeListener..
    *
    * @param event The servlet context attribute event. It cannot be null.
    */
   public void attributeAdded(final ServletContextAttributeEvent event) {
     log.trace("Entering attributeAdded");
     if (delegate instanceof ServletContextAttributeListener) {
       ((ServletContextAttributeListener) delegate).attributeAdded(event);
     }
     log.trace("Leaving attributeAdded");
   }
 
   /** Notification that an existing attribute has been remved from the servlet
    * context.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextAttributeListener..
    *
    * @param event The servlet context attribute event. It cannot be null.
    */
   public void attributeRemoved(final ServletContextAttributeEvent event) {
     log.trace("Entering attributeRemoved");
     if (delegate instanceof ServletContextAttributeListener) {
       ((ServletContextAttributeListener) delegate).attributeRemoved(event);
     }
     log.trace("Leaving attributeRemoved");
   }
 
   /** Notification that an attribute on the servlet context has been replaced.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextAttributeListener..
    *
    * @param event The servlet context attribute event. It cannot be null.
    */
   public void attributeReplaced(final ServletContextAttributeEvent event) {
     log.trace("Entering attributeReplaced");
     if (delegate instanceof ServletContextAttributeListener) {
       ((ServletContextAttributeListener) delegate).attributeReplaced(event);
     }
     log.trace("Leaving attributeReplaced");
   }
 
   /*
    * HttpSessionActivationListener methods.
    */
 
   /** Notification that the session has just been activated.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionActivationListener.
    *
    * @param event The session event. It cannot be null.
    */
   public void sessionDidActivate(final HttpSessionEvent event) {
     log.trace("Entering sessionDidActivate");
     if (delegate instanceof HttpSessionActivationListener) {
       ((HttpSessionActivationListener) delegate).sessionDidActivate(event);
     }
     log.trace("Leaving sessionDidActivate");
   }
 
   /** Notification that the session is about to be passivated.
    *
    * It forwards the operation to the delegate if the delegate implements
    * ServletContextAttributeListener.
    *
    * @param event The session event. It cannot be null.
    */
   public void sessionWillPassivate(final HttpSessionEvent event) {
     log.trace("Entering sessionWillPassivate");
     if (delegate instanceof HttpSessionActivationListener) {
       ((HttpSessionActivationListener) delegate).sessionWillPassivate(event);
     }
     log.trace("Leaving sessionWillPassivate");
   }
 
   /*
    * HttpSessionAttributeListener methods.
    */
 
   /** Notification that a new attribute was added to a session.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionAttributeListener..
    *
    * @param event The session binding event. It cannot be null.
    */
   public void attributeAdded(final HttpSessionBindingEvent event) {
     log.trace("Entering attributeAdded");
     if (delegate instanceof HttpSessionAttributeListener) {
       ((HttpSessionAttributeListener) delegate).attributeAdded(event);
     }
     log.trace("Leaving attributeAdded");
   }
 
   /** Notification that an existing attribute has been remved from the servlet
    * context.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionAttributeListener..
    *
    * @param event The servlet context attribute event. It cannot be null.
    */
   public void attributeRemoved(final HttpSessionBindingEvent event) {
     log.trace("Entering attributeRemoved");
     if (delegate instanceof HttpSessionAttributeListener) {
       ((HttpSessionAttributeListener) delegate).attributeRemoved(event);
     }
     log.trace("Leaving attributeRemoved");
   }
 
   /** Notification that an attribute on the servlet context has been replaced.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionAttributeListener..
    *
    * @param event The servlet context attribute event. It cannot be null.
    */
   public void attributeReplaced(final HttpSessionBindingEvent event) {
     log.trace("Entering attributeReplaced");
     if (delegate instanceof HttpSessionAttributeListener) {
       ((HttpSessionAttributeListener) delegate).attributeReplaced(event);
     }
     log.trace("Leaving attributeReplaced");
   }
 
   /*
    * HttpSessionListener methods.
    */
 
   /** Notification that a session was created.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionListener..
    *
    * @param event The session event. It cannot be null.
    */
   public void sessionCreated(final HttpSessionEvent event) {
     log.trace("Entering sessionCreated");
     if (delegate instanceof HttpSessionListener) {
       ((HttpSessionListener) delegate).sessionCreated(event);
     }
     log.trace("Leaving sessionCreated");
   }
 
   /** Notification that a session was invalidated.
    *
    * It forwards the operation to the delegate if the delegate implements
    * HttpSessionListener..
    *
    * @param event The session event. It cannot be null.
    */
   public void sessionDestroyed(final HttpSessionEvent event) {
     log.trace("Entering sessionDestroyed");
     if (delegate instanceof HttpSessionListener) {
       ((HttpSessionListener) delegate).sessionDestroyed(event);
     }
     log.trace("Leaving sessionDestroyed");
   }
 
   /*
    * HttpSessionBindingListener methods.
    */
 
   /** Notifies the object that it is being bound to a session and identifies
    * the session.
    *
    * @param event the session binding event. It cannot be null.
    */
   public void valueBound(final HttpSessionBindingEvent event) {
     log.trace("Entering valueBound");
     if (delegate instanceof HttpSessionBindingListener) {
       ((HttpSessionBindingListener) delegate).valueBound(event);
     }
     log.trace("Leaving valueBound");
   }
 
   /** Notifies the object that it is being unbound from a session and
    * identifies the session.
    *
    * @param event the session binding event. It cannot be null.
    */
   public void valueUnbound(final HttpSessionBindingEvent event) {
     log.trace("Entering valueUnbound");
     if (delegate instanceof HttpSessionBindingListener) {
       ((HttpSessionBindingListener) delegate).valueUnbound(event);
     }
     log.trace("Leaving valueUnbound");
   }
 
   /** Returns the spring web application context.
    *
    * @param context The servlet context. It cannot be null.
    *
    * @return Returns the spring web application context. It never returns null.
    */
   private BeanFactory getBeanFactory(final ServletContext context) {
     BeanFactory appContext;
     appContext = WebApplicationContextUtils.getWebApplicationContext(context);
     if (appContext == null) {
       throw new RuntimeException("The spring application context has not been"
           + " initialized");
     }
     return appContext;
   }
 }
 
