 package ddth.dasp.hetty.mvc;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.osgi.context.BundleContextAware;
 
 import ddth.dasp.common.utils.OsgiUtils;
 import ddth.dasp.framework.osgi.IServiceAutoRegister;
 import ddth.dasp.hetty.IRequestActionHandler;
 import ddth.dasp.hetty.IUrlCreator;
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.message.IRequestParser;
 import ddth.dasp.hetty.mvc.view.IView;
 import ddth.dasp.hetty.mvc.view.IViewResolver;
 import ddth.dasp.hetty.mvc.view.RedirectView;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 /**
  * This action handler implements the following workflow:
  * <ul>
  * <li>
  * {@link #internalHandleRequest(IRequest, ITopicPublisher)} is called to handle
  * the request. If a view object is returned:
  * <ul>
  * <li>
  * {@link #resolveVew(IRequest, String)} is called to resolve the view.</li>
  * <li>If the view is resolved, {@link #buildViewModel()} is called to build the
  * view model</li>
  * <li>If the view is resolved, method
  * {@link IView#render(IRequest, Object, ITopicPublisher)} is call to render
  * view.</li></li>
  * </ul>
  * 
  * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
  */
 public abstract class AbstractActionHandler implements IRequestActionHandler, IServiceAutoRegister,
         ApplicationContextAware, BundleContextAware {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(AbstractActionHandler.class);
 
     private ApplicationContext appContext;
     private BundleContext bundleContext;
 
     private Properties properties;
     private IViewResolver viewResolver;
     private IRequestParser requestParser;
     private IUrlCreator urlCreator;
     private String viewName;
 
     public String getViewName() {
         return viewName;
     }
 
     public void setViewName(String viewName) {
         this.viewName = viewName;
     }
 
     public void setViewResolver(IViewResolver viewResolver) {
         this.viewResolver = viewResolver;
     }
 
     protected IViewResolver getViewResolver() {
         if (viewResolver == null) {
             try {
                 viewResolver = appContext.getBean(IViewResolver.class);
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
             }
         }
         return viewResolver;
     }
 
     public void setRequestParser(IRequestParser requestParser) {
         this.requestParser = requestParser;
     }
 
     protected IRequestParser getRequestParser() {
         if (requestParser == null) {
             try {
                 requestParser = appContext.getBean(IRequestParser.class);
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
             }
         }
         return requestParser;
     }
 
     public void setUrlCreator(IUrlCreator urlCreator) {
         this.urlCreator = urlCreator;
     }
 
     protected IUrlCreator getUrlCreator() {
         if (urlCreator == null) {
             try {
                 urlCreator = appContext.getBean(IUrlCreator.class);
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
             }
         }
         return urlCreator;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getClassName() {
         return IRequestActionHandler.class.getName();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Properties getProperties() {
         return properties;
     }
 
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void handleRequest(IRequest request, ITopicPublisher topicPublisher) throws Exception {
         if (!preHandleRequest(request, topicPublisher)) {
             return;
         }
 
         Object view = internalHandleRequest(request, topicPublisher);
 
         if (view == null) {
             postHandleRequest(request, null, null);
             return;
         }
         Object oldView = view;
         if (!(view instanceof IView)) {
             String viewName = view.toString();
             view = resolveVew(request, viewName);
         }
         if (view instanceof IView) {
             Map<String, Object> model = view instanceof RedirectView ? null : buildViewModel();
             postHandleRequest(request, model, (IView) view);
             ((IView) view).render(request, model, topicPublisher);
         } else {
             postHandleRequest(request, null, null);
             String msg = "Can not resolve view for [" + oldView + "]!";
             throw new Exception(msg);
         }
     }
 
     /**
      * This method simply returns an empty {@link Map}. Sub-class overrides this
      * method to build its own model.
      * 
      * @return
      */
     protected Map<String, Object> buildViewModel() {
         Map<String, Object> model = new HashMap<String, Object>();
         return model;
     }
 
     /**
      * {@link #handleRequest(IRequest, ITopicPublisher)} calls this method
      * before handling the request.
      * 
      * @param request
      * @param topicPublisher
      * @return <code>true</code> to indicates that request should be handled
      *         normally, <code>false</code> to indicate that request has already
      *         been completely handled by this method and should not be handled
      *         any further
      */
     protected boolean preHandleRequest(IRequest request, ITopicPublisher topicPublisher) {
         return true;
     }
 
     /**
      * Sub-class to implement this method to implement its own business. This
      * method is called by {@link #handleRequest(IRequest, ITopicPublisher)}.
      * 
      * @param request
      * @param topicPublisher
      * @return
      */
    protected abstract Object internalHandleRequest(IRequest request, ITopicPublisher topicPublisher);
 
     /**
      * {@link #handleRequest(IRequest, ITopicPublisher)} calls this method after
      * request has been handled, and before rendering view.
      * 
      * @param request
      * @param model
      * @param view
      */
     protected void postHandleRequest(IRequest request, Map<String, Object> model, IView view) {
         // EMPTY
     }
 
     /**
      * Resolves a view name to {@link IView} object.
      * 
      * @param request
      * @param viewName
      * @return
      */
     protected IView resolveVew(IRequest request, String viewName) {
         Map<String, String> replacements = new HashMap<String, String>();
         IViewResolver viewResolver = getViewResolver();
         return viewResolver.resolveView(viewName, replacements);
     }
 
     /**
      * Gets a Spring bean by class.
      * 
      * @param clazz
      * @return
      */
     protected <T> T getSpringBean(Class<T> clazz) {
         try {
             return appContext.getBean(clazz);
         } catch (NoSuchBeanDefinitionException e) {
             return null;
         }
     }
 
     /**
      * Gets a Spring bean by name.
      * 
      * @param name
      * @return
      */
     protected Object getSpringBean(String name) {
         try {
             return appContext.getBean(name);
         } catch (NoSuchBeanDefinitionException e) {
             return null;
         }
     }
 
     /**
      * Gets a Spring bean by name and class.
      * 
      * @param name
      * @param clazz
      * @return
      */
     protected <T> T getSpringBean(String name, Class<T> clazz) {
         try {
             return appContext.getBean(name, clazz);
         } catch (NoSuchBeanDefinitionException e) {
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setApplicationContext(ApplicationContext appContext) {
         this.appContext = appContext;
     }
 
     protected ApplicationContext getApplicationContext() {
         return appContext;
     }
 
     /**
      * Gets an OSGi service by class.
      * 
      * @param clazz
      * @return
      */
     protected <T> T getService(Class<T> clazz) {
         return OsgiUtils.getService(bundleContext, clazz);
     }
 
     /**
      * Gets an OSGi service by class and filter.
      * 
      * @param clazz
      * @param filter
      * @return
      */
     protected <T> T getService(Class<T> clazz, Map<String, String> filter) {
         return OsgiUtils.getService(bundleContext, clazz, filter);
     }
 
     /**
      * Gets an OSGi service by class and filter.
      * 
      * @param clazz
      * @param filterQuery
      * @return
      */
     protected <T> T getService(Class<T> clazz, String filterQuery) {
         return OsgiUtils.getService(bundleContext, clazz, filterQuery);
     }
 
     @Override
     public void setBundleContext(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     protected BundleContext getBundleContext() {
         return bundleContext;
     }
 }
