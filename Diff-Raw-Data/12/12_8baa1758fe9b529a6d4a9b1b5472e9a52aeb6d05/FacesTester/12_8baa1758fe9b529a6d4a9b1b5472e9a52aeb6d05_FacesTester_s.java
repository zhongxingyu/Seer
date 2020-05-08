 package com.steeplesoft.jsf.facestester;
 
 import com.steeplesoft.jsf.facestester.metadata.FacesConfig;
 import com.steeplesoft.jsf.facestester.servlet.FilterChainImpl;
 import com.steeplesoft.jsf.facestester.servlet.WebDeploymentDescriptor;
 import com.sun.faces.context.FacesContextImpl;
 import static com.steeplesoft.jsf.facestester.servlet.ServletContextFactory.createServletContext;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.springframework.mock.web.MockHttpServletResponse;
 
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 import javax.el.ELContext;
 import javax.el.ELResolver;
 import static java.lang.String.format;
 
 import javax.faces.FactoryFinder;
 import javax.faces.component.UIComponent;
 import static javax.faces.FactoryFinder.LIFECYCLE_FACTORY;
 import static javax.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;
 import javax.faces.context.FacesContext;
 import javax.faces.lifecycle.LifecycleFactory;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 import javax.servlet.ServletContext;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletRequestEvent;
 import javax.servlet.ServletRequestListener;
 import javax.servlet.ServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * @author jasonlee
  */
 public class FacesTester {

     private FacesContextBuilder facesContextBuilder;
     private FacesLifecycle lifecycle;
     private ServletContext servletContext;
     protected WebDeploymentDescriptor descriptor;
 
     public FacesTester() {
         FacesContext context = FacesContext.getCurrentInstance();
         if (context != null) {
             context.release();
         }
         descriptor = WebDeploymentDescriptor.createFromFile(Util.lookupWebAppPath());
         servletContext = createServletContext(descriptor);
         facesContextBuilder = new FacesContextBuilderImpl(servletContext, descriptor);
 
         LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
         lifecycle = new FacesLifecycleImpl(factory.getLifecycle(DEFAULT_LIFECYCLE));
     }
 
     public FacesComponent createComponent(String componentType) {
         FacesContext context = getFacesContext(); //facesContextBuilder.createFacesContext("/dummyPage.xhtml", "GET", lifecycle);
         lifecycle.execute(context);
 
         return new FacesComponent(context.getApplication().createComponent(componentType));
     }
 
     public FacesContext getFacesContext() {
         FacesContext context = FacesContext.getCurrentInstance();
         if (context == null) {
             context = facesContextBuilder.createFacesContext("/dummyPage.xhtml", "GET", lifecycle);
         }
         return context;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public <T> T getManagedBean(Class<T> type, String name) {
         FacesContext context = getFacesContext();
         ELResolver elResolver = context.getApplication().getELResolver();
         ELContext elContext = context.getELContext();
         T bean = (T) elResolver.getValue(elContext, null, name);
 
         return bean;
     }
 
     public FacesPage requestPage(String uri) {
         FacesContext context = facesContextBuilder.createFacesContext(uri, "GET", lifecycle);
 
         FilterChain filterChain = createAppropriateFilterChain(uri);
         try {
             filterChain.doFilter((ServletRequest) context.getExternalContext().getRequest(), (ServletResponse) context.getExternalContext().getResponse());
         } catch (Exception ex) {
             throw new FacesTesterException("An error occurred while executing the filters", ex);
         }
 
         ServletRequestEvent sre = new ServletRequestEvent(servletContext, (ServletRequest) context.getExternalContext().getRequest());
         for (EventListener listener : descriptor.getListeners()) {
             if (listener instanceof ServletRequestListener) {
                 ((ServletRequestListener)listener).requestInitialized(sre);
             }
         }
 
         lifecycle.execute(context);
         lifecycle.render(context);
 
         checkForErrors(context);
 
         try {
             filterChain = createAppropriateFilterChain(uri);
             filterChain.doFilter((ServletRequest) context.getExternalContext().getRequest(), (ServletResponse) context.getExternalContext().getResponse());
         } catch (Exception ex) {
             throw new FacesTesterException("An error occurred while executing the filters", ex);
         }
 
         for (EventListener listener : descriptor.getListeners()) {
             if (listener instanceof ServletRequestListener) {
                 ((ServletRequestListener)listener).requestDestroyed(sre);
             }
         }
 
         return new FacesPage(context, facesContextBuilder, lifecycle, uri);
     }
 
     public void validateFacesConfig(String path) throws IOException, SAXException, ParserConfigurationException {
         validateFacesConfig(new File(path));
     }
 
     public void validateFacesConfig(File file) throws IOException, SAXException, ParserConfigurationException {
         FacesConfig config = new FacesConfig(file);
         config.validateManagedBeans();
         config.validateComponents();
     }
 
     public void testStateSaving(String componentType) {
         testStateSaving(componentType, new String[]{});
     }
 
     /**
      * The idea for this method is to have it interrogate the UIComponent for
      * all "local" getters and setters.  It would then set dummy data on the component
      * and call saveState() on it.  Next, it would create a new instance of the
      * component and call restoreState() on it, using the saved state from above.
      * Finally, it would call each getter on the new component and insure that the
      * values returned match the values set on the old component.  If the component
      * has a property that has not been added to the state saving code, this should
      * detect that oversight.  The problem is that we might detect a getter/setter
      * pair that are not really part of the component's state.  It seems odd to have
      * such a pair of methods, but only convention would prevent a component author
      * from doing that, and conventions are rarely followed completely.  We may have
      * to provide a blacklist parameter for this situations.
      *
      * @param componentType      component type to test
      * @param blackListedMethods methods to skip
      */
     public void testStateSaving(String componentType, String... blackListedMethods) {
         Set<String> methodsToSkip = new TreeSet<String>();
         methodsToSkip.add("Family");
         methodsToSkip.add("Converter");
         methodsToSkip.add("LocalValue");
         methodsToSkip.add("RendersChildren");
         for (String method : blackListedMethods) {
             methodsToSkip.add(method.substring(0, 1).toUpperCase() + method.substring(1));
         }
 
         UIComponent origComp = createComponent(componentType).getWrappedComponent();
         Method[] methods = origComp.getClass().getDeclaredMethods();
         List<String> properties = new ArrayList<String>();
 
         for (Method method : methods) {
             if (isGetter(method)) {
                 String property = method.getName().substring(3);
                 if (methodsToSkip.contains(property)) {
                     continue;
                 }
                 Class type = method.getReturnType();
                 try {
                     Method setter = origComp.getClass().getDeclaredMethod("set" + property, type);
                     if (isSetter(setter)) {
                         // We've found a readable/writable property, so let's generate some
                         // dummy data for testing
                         Object dummyData = generateDefaultValue(type);
                         setter.invoke(origComp, dummyData);
                         properties.add(property);
                     }
                 } catch (Exception ex) {
                     Logger.getLogger(FacesTester.class.getName()).info("Skipping '" + property + "'.  Does not appear to be a property:  " + ex.getLocalizedMessage());
                 }
             }
         }
 
         Object state = origComp.saveState(FacesContext.getCurrentInstance());
         UIComponent newComp = createComponent(componentType).getWrappedComponent();
         newComp.restoreState(FacesContext.getCurrentInstance(), state);
 
         if (properties.size() > 0) { // We found properties, so let's test state saving
             for (String property : properties) {
                 try {
                     Method getter = origComp.getClass().getDeclaredMethod("get" + property, new Class<?>[]{});
                     Object value1 = getter.invoke(origComp);
                     Object value2 = getter.invoke(newComp);
                     if (!value1.equals(value2) && (value1 != value2)) {
                         throw new AssertionError("The restored state for '" + property + "' does not match.");
                     }
                 } catch (Exception ex) {
                     Logger.getLogger(FacesTester.class.getName()).log(Level.SEVERE, null, ex);
                     throw new AssertionError(ex.getLocalizedMessage());
                 }
             }
         }
     }
 
     /**
      * This method will allow a resource to be injected into a managed bean, irrespective of
      * whether or not a setter is available.  For example:
      * <code><pre>
      * public class MyBean {
      *     &#064;PersistenceContext(unitName="em");
      *     protected EntityManager em;
      *
      *     public Foo getSomeData() {
      *         return em.fetch(Foo.class, 1);
      *     }
      * }
      * </pre></code>
      *
      * Given the above managed bean class, the unit test would then do something like this:
      * <code><pre>
      *     MyBean myBean = new MyBean();
      *     EntityManager em = createTestEntityManager();
      *     facesTester.inject(myBean, "em", em);
      *     Foo foo = myBean.getSomeData();</pre></code>
      * Note that this method is experimental and may go away.
      * @param target The Object on which injection will be performed
      * @param property The name of the property to be modified
      * @param value The value to be injected
      */
     public void inject (Object target, String property, Object value) {
         Method setter = getSetter(target, property, value.getClass());
         boolean injectionCompleted = false;
         if (setter != null) {
             try {
                 setter.invoke(target, value);
                 injectionCompleted = true;
             } catch (IllegalAccessException ex) {
                 throw new FacesTesterException("The setter method for '" + property + "' is not accessible.", ex);
             } catch (IllegalArgumentException ex) {
                 throw new FacesTesterException("Injection attempted with an invalid value.  Expected '" +
                         setter.getParameterTypes()[0].toString() + "'. Found '" + value.getClass().toString() + "'.", ex);
             } catch (InvocationTargetException ex) {
                 throw new FacesTesterException(ex);
             }
         }
         if (!injectionCompleted) { // try property injection
             Field field = null;
             try {
                 field = target.getClass().getDeclaredField(property);
                 field.setAccessible(true);
                 field.set(target, value);
                 injectionCompleted = true;
             } catch (NoSuchFieldException ex) {
                 throw new FacesTesterException("The property '" + property + "' was not found.", ex);
             } catch (IllegalArgumentException ex) {
                 String expectedType = "<unknown>";
                 try {
                     expectedType = field.get(target).getClass().toString();
                 } catch (Exception ex1) {
                     //
                 }
                 throw new FacesTesterException("Injection attempted with an invalid value.  Expected '" +
                     expectedType + "'. Found '" + value.getClass().toString() + "'.", ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(FacesTester.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         if (!injectionCompleted) {
             throw new FacesTesterException("Unable to perform injection on " + property);
         }
     }
 
     protected Method getSetter(Object target, String property, Class paramType) {
         Method setter = null;
         try {
             property = property.substring(0,1).toUpperCase() + property.substring(1);
             setter = target.getClass().getDeclaredMethod("set" + property, new Class<?>[]{paramType});
         } catch (NoSuchMethodException ex) {
             //Logger.getLogger(FacesTester.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return setter;
     }
 
     @SuppressWarnings({"RedundantIfStatement"})
     protected static boolean isGetter(Method method) {
         if (!method.getName().startsWith("get")) {
             return false;
         }
         if (method.getParameterTypes().length != 0) {
             return false;
         }
         if (void.class.equals(method.getReturnType())) {
             return false;
         }
         return true;
     }
 
     @SuppressWarnings({"RedundantIfStatement"})
     protected static boolean isSetter(Method method) {
         if (!method.getName().startsWith("set")) {
             return false;
         }
         if (method.getParameterTypes().length != 1) {
             return false;
         }
         return true;
     }
 
     private void checkForErrors(FacesContext context) {
         MockHttpServletResponse response = (MockHttpServletResponse) context.getExternalContext().getResponse();
 
         if (SC_OK == response.getStatus()) {
             return;
         }
 
         switch (response.getStatus()) {
             case SC_NOT_FOUND:
                 throw new FacesTesterException(format("The page %s was not found.", response.getErrorMessage()));
             default:
                 throw new FacesTesterException(response.getErrorMessage());
         }
     }
 
     /*
      * Finding a way to make this more intelligent would be awesome :)
      */
     private Object generateDefaultValue(Class type) throws InstantiationException, IllegalAccessException {
         String name = type.getName();
         if (name.startsWith("java.lang")) {
             name = name.substring(10).toLowerCase();
         }
         if (name.equals("string")) {
             return "FacesTester dummy value";
         } else if (name.equals("boolean")) {
             return Boolean.FALSE;
         } else if (name.equals("integer") || name.equals("int")) {
             return Integer.MAX_VALUE;
         } else if (name.equals("double")) {
             return Double.MAX_VALUE;
         } else if (name.equals("float")) {
             return Float.MAX_VALUE;
         } else if (name.equals("short")) {
             return Short.MAX_VALUE;
         } else {
             return type.newInstance();
         }
     }
 
     /**
      * This method will attempt to create a regex out of the filter mapping to determine
      * if this filter is appropriate for this request.  There's a really good chance that
      * it needs some more work to be as robust as it needs to be.
      * @param uri
      * @return
      */
     private FilterChain createAppropriateFilterChain(String uri) {
         List<Filter> filters = new ArrayList<Filter>();
 
         for (Map.Entry<String, String> entry : descriptor.getFilterMappings().entrySet()) {
             final String mapping = entry.getKey();
             String regEx = mapping.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\.\\*");
             if (Pattern.matches(regEx, uri)) {
                 filters.add(descriptor.getFilters().get(entry.getValue()).getFilter());
             }
         }
 
         return new FilterChainImpl(filters);
     }
 }
