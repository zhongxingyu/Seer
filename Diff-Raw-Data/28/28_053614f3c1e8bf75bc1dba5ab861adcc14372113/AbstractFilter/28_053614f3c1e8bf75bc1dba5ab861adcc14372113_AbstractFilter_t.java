 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Skeletal implementation of {@link Filter}.
  *
  * <p>Typically, a subclass only needs to override {@link #doRequest}
  * to process a normal page request. Unlike the servlet filter interface,
  * however, this class has separate hooks for different types of
  * dispatches:</p>
  *
  * <ul>
  * <li>{@link #doError}</li>
  * <li>{@link #doForward}</li>
  * <li>{@link #doInclude}</li>
  * </ul>
  *
  * <p>Note that these methods will only trigger if the corresponding
  * {@code <dispatcher/>} tags are defined in {@code <filter-mapping/>}
  * like so:</p>
  *
  * <blockquote><pre>{@literal
 <filter-mapping>
     <dispatcher>ERROR</dispatcher>
     <dispatcher>FORWARD</dispatcher>
     <dispatcher>INCLUDE</dispatcher>
     <dispatcher>REQUEST</dispatcher>
 </filter-mapping>
  * }</pre></blockquote>
  *
  * <p>One time initialization and destruction logic should be defined in:</p>
  *
  * <ul>
  * <li>{@link #doInit}</li>
  * <li>{@link #doDestroy}</li>
  * </ul>
  *
  * <p>Additionally, a subclass can override {@link #dependencies} to
  * declare other filter classes that it depends on. These dependencies
  * will be executed automatically before each of the phases in processing
  * the filter.</p>
  */
 public abstract class AbstractFilter implements Filter {
 
     /**
      * {@link Settings} key prefix for disabling a filter.
      *
      * <p>For example, if you set {@code dari/disableFilter/my.Filter}
      * to {@code true}, instances of {@code my.Filter} class won't do
      * anything.</p>
      */
     public static final String DISABLE_FILTER_SETTING_PREFIX = "dari/disableFilter/";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFilter.class);
 
     private static final String ATTRIBUTE_PREFIX = AbstractFilter.class.getName() + ".";
     private static final String EXECUTED_MAP_ATTRIBUTE = ATTRIBUTE_PREFIX + "executedMap";
     private static final String FILTERS_ATTRIBUTE = ATTRIBUTE_PREFIX + "filters";
 
     private static final PullThroughValue<Set<Class<? extends Auto>>> AUTO_CLASSES = new PullThroughValue<Set<Class<? extends Auto>>>() {
         @Override
         protected Set<Class<? extends Auto>> produce() {
             return ObjectUtils.findClasses(Auto.class);
         }
     };
 
     private FilterConfig filterConfig;
     private ServletContext servletContext;
     private final List<Filter> initialized = new ArrayList<Filter>();
 
     /**
      * Returns the config associated with this filter.
      *
      * @return Never {@code null} if {@linkplain #init initialized}.
      */
     public FilterConfig getFilterConfig() {
         return filterConfig;
     }
 
     /**
      * Returns the servlet context associated with this filter.
      *
      * @return Never {@code null} if {@linkplain #init initialized}.
      */
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     // --- Filter support ---
 
     /**
      * Returns other filters that should be initialized and destroyed
      * before this one.
      *
      * @return {@code null} is equivalent to an empty iterable.
      */
     protected Iterable<Class<? extends Filter>> dependencies() {
         return null;
     }
 
     /**
      * Initializes this filter along with all other dependencies.
      * A subclass can override {@link #doInit} to provide additional
      * functionality.
      *
      * @param config Can't be {@code null}.
      */
     @Override
     public final void init(FilterConfig config) throws ServletException {
         ErrorUtils.errorIfNull(config, "config");
 
         filterConfig = config;
         servletContext = config.getServletContext();
 
         try {
             doInit();
         } catch (Exception error) {
             ErrorUtils.rethrowIf(error, ServletException.class);
             ErrorUtils.rethrow(error);
         }
 
         LOGGER.info("Initialized [{}]", getClass().getName());
     }
 
     /**
      * Triggers when this filter needs to be initialized. A subclass
      * must override this method instead of {@link #init}.
      * The {@link FilterConfig} object normally available as an
      * argument can be accessed using {@link #getFilterConfig}.
      */
     protected void doInit() throws Exception {
     }
 
     /**
      * Destroys this filter along with all other dependencies that
      * were automatically initialized. A subclass can override
      * {@link #doDestroy} to provide additional functionality.
      */
     @Override
     public final void destroy() {
         for (Filter filter : initialized) {
             filter.destroy();
         }
 
         try {
             doDestroy();
         } catch (Exception error) {
             ErrorUtils.rethrow(error);
         }
 
         filterConfig = null;
         servletContext = null;
         initialized.clear();
 
         LOGGER.info("Destroyed [{}]", getClass().getName());
     }
 
     /**
      * Triggers when this filter needs to be destroyed. A subclass
      * must override this method instead of {@link #destroy}.
      */
     protected void doDestroy() throws Exception {
     }
 
     /**
      * Filters a page after processing it with all other dependencies.
      * A subclass can override {@link #doError}, {@link #doInclude},
      * {@link #doForward}, and/or {@link #doRequest} to provide
      * additional functionality.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     @Override
     public final void doFilter(
             ServletRequest request,
             ServletResponse response,
             FilterChain chain)
             throws IOException, ServletException {
 
         if (Settings.get(boolean.class, DISABLE_FILTER_SETTING_PREFIX + getClass().getName())) {
             chain.doFilter(request, response);
             return;
         }
 
         // Make sure that this filter doesn't run more than once per request.
         @SuppressWarnings("unchecked")
         Map<ServletRequest, Set<Class<?>>> executedMap = (Map<ServletRequest, Set<Class<?>>>) request.getAttribute(EXECUTED_MAP_ATTRIBUTE);
 
         if (executedMap == null) {
             executedMap = new HashMap<ServletRequest, Set<Class<?>>>();
             request.setAttribute(EXECUTED_MAP_ATTRIBUTE, executedMap);
         }
 
         Set<Class<?>> executed = executedMap.get(request);
 
         if (executed == null) {
             executed = new HashSet<Class<?>>();
             executedMap.put(request, executed);
         }
 
         if (executed.contains(getClass())) {
             chain.doFilter(request, response);
             return;
         }
 
         executed.add(getClass());
 
         // Find all dependencies.
         List<Filter> dependencies = new ArrayList<Filter>();
         Iterable<Class<? extends Filter>> dependenciesIterable = dependencies();
         List<Class<? extends Filter>> dependencyClasses = new ArrayList<Class<? extends Filter>>();
 
         if (dependenciesIterable != null) {
             for (Class<? extends Filter> d : dependenciesIterable) {
                 dependencyClasses.add(d);
             }
         }
 
         for (Class<? extends Auto> autoClass : AUTO_CLASSES.get()) {
             getFilter(autoClass).updateDependencies(getClass(), dependencyClasses);
         }
 
         if (!dependencyClasses.isEmpty()) {
             for (Class<? extends Filter> dependencyClass : dependencyClasses) {
                 if (executed.contains(dependencyClass)) {
                     continue;
                 }
 
                 Filter dependency = getFilter(dependencyClass);
 
                 if (dependency instanceof AbstractFilter &&
                         ObjectUtils.isBlank(((AbstractFilter) dependency).dependencies()) &&
                         !hasDispatchOverride(dependencyClass)) {
 
                     if (JspUtils.isIncluded(request)) {
                         if (!hasIncludeOverride(dependencyClass)) {
                             continue;
                         }
 
                    } else if (JspUtils.isError(request)) {
                         if (!hasErrorOverride(dependencyClass)) {
                             continue;
                         }
 
                     } else if (JspUtils.isForwarded(request)) {
                         if (!hasForwardOverride(dependencyClass)) {
                             continue;
                         }
 
                     } else if (!hasRequestOverride(dependencyClass)) {
                         continue;
                     }
                 }
 
                 dependencies.add(dependency);
             }
         }
 
         if (!dependencies.isEmpty()) {
             new DependencyFilterChain(dependencies, chain).doFilter(request, response);
             return;
         }
 
         // No dependencies so try to skip the chain.
         if (request instanceof HttpServletRequest &&
                 response instanceof HttpServletResponse) {
 
             Class<? extends Filter> thisClass = getClass();
             HttpServletRequest httpRequest = (HttpServletRequest) request;
             HttpServletResponse httpResponse = (HttpServletResponse) response;
 
             try {
                 if (hasDispatchOverride(thisClass)) {
                     doDispatch(httpRequest, httpResponse, chain);
                     return;
                 }
 
                 if (JspUtils.isIncluded(httpRequest)) {
                     if (hasIncludeOverride(thisClass)) {
                         doInclude(httpRequest, httpResponse, chain);
                         return;
                     }
 
                 } else if (JspUtils.isError(httpRequest)) {
                     if (hasErrorOverride(thisClass)) {
                         doError(httpRequest, httpResponse, chain);
                         return;
                     }
 
                 } else if (JspUtils.isForwarded(httpRequest)) {
                     if (hasForwardOverride(thisClass)) {
                         doForward(httpRequest, httpResponse, chain);
                         return;
                     }
 
                 } else if (hasRequestOverride(thisClass)) {
                     doRequest(httpRequest, httpResponse, chain);
                     return;
                 }
 
             } catch (Exception error) {
                 ErrorUtils.rethrowIf(error, IOException.class);
                 ErrorUtils.rethrowIf(error, ServletException.class);
                 ErrorUtils.rethrow(error);
             }
         }
 
         chain.doFilter(request, response);
     }
 
     @SuppressWarnings("unchecked")
     private <F extends Filter> F getFilter(Class<F> filterClass) throws ServletException {
         ServletContext context = getServletContext();
         Map<Class<? extends Filter>, Filter> filters = (Map<Class<? extends Filter>, Filter>) context.getAttribute(FILTERS_ATTRIBUTE);
 
         if (filters == null) {
             synchronized (context) {
                 filters = (Map<Class<? extends Filter>, Filter>) context.getAttribute(FILTERS_ATTRIBUTE);
                 if (filters == null) {
                     filters = new ConcurrentHashMap<Class<? extends Filter>, Filter>();
                     context.setAttribute(FILTERS_ATTRIBUTE, filters);
                 }
             }
         }
 
         Filter filter = filters.get(filterClass);
 
         if (filter == null) {
             synchronized (filters) {
                 filter = filters.get(filterClass);
                 if (filter == null) {
                     LOGGER.info("Creating [{}] for [{}]", filterClass, getClass().getName());
                     filter = TypeDefinition.getInstance(filterClass).newInstance();
                     filter.init(new DependencyFilterConfig(filter));
                     initialized.add(filter);
                     filters.put(filterClass, filter);
                 }
             }
         }
 
         return (F) filter;
     }
 
     private static boolean hasOverride(ConcurrentMap<Class<?>, Boolean> overrides, Class<?> filterClass, String methodName) {
         Boolean override = overrides.putIfAbsent(filterClass, Boolean.TRUE);
 
         if (override != null) {
             return override;
         }
 
         if (!AbstractFilter.class.isAssignableFrom(filterClass)) {
             override = Boolean.TRUE;
 
         } else {
             override = Boolean.FALSE;
             for (Class<?> c = filterClass; !AbstractFilter.class.equals(c); c = c.getSuperclass()) {
                 try {
                     c.getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);
                     override = Boolean.TRUE;
                     break;
                 } catch (NoSuchMethodException error) {
                     // Try to find the method in the super class.
                 }
             }
         }
 
         overrides.put(filterClass, override);
         return override;
     }
 
     private static final ConcurrentMap<Class<?>, Boolean> DISPATCH_OVERRIDES = new ConcurrentHashMap<Class<?>, Boolean>();
 
     private static boolean hasDispatchOverride(Class<? extends Filter> filterClass) {
         return hasOverride(DISPATCH_OVERRIDES, filterClass, "doDispatch");
     }
 
     private static final ConcurrentMap<Class<?>, Boolean> ERROR_OVERRIDES = new ConcurrentHashMap<Class<?>, Boolean>();
 
     private static boolean hasErrorOverride(Class<? extends Filter> filterClass) {
         return hasOverride(ERROR_OVERRIDES, filterClass, "doError");
     }
 
     private static final ConcurrentMap<Class<?>, Boolean> FORWARD_OVERRIDES = new ConcurrentHashMap<Class<?>, Boolean>();
 
     private static boolean hasForwardOverride(Class<? extends Filter> filterClass) {
         return hasOverride(FORWARD_OVERRIDES, filterClass, "doForward");
     }
 
     private static final ConcurrentMap<Class<?>, Boolean> INCLUDE_OVERRIDES = new ConcurrentHashMap<Class<?>, Boolean>();
 
     private static boolean hasIncludeOverride(Class<? extends Filter> filterClass) {
         return hasOverride(INCLUDE_OVERRIDES, filterClass, "doInclude");
     }
 
     private static final ConcurrentMap<Class<?>, Boolean> REQUEST_OVERRIDES = new ConcurrentHashMap<Class<?>, Boolean>();
 
     private static boolean hasRequestOverride(Class<? extends Filter> filterClass) {
         return hasOverride(REQUEST_OVERRIDES, filterClass, "doRequest");
     }
 
     /**
      * Triggers when this filter needs to process a page. This method
      * will delegate to one of: {@link #doError}, {@link #doInclude},
      * {@link #doForward}, or {@link #doRequest}.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     protected void doDispatch(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         if (JspUtils.isIncluded(request)) {
             if (hasIncludeOverride(getClass())) {
                 doInclude(request, response, chain);
                 return;
             }
 
         } else if (JspUtils.isError(request)) {
             if (hasErrorOverride(getClass())) {
                 doError(request, response, chain);
                 return;
             }
 
         } else if (JspUtils.isForwarded(request)) {
             if (hasForwardOverride(getClass())) {
                 doForward(request, response, chain);
                 return;
             }
 
         } else if (hasRequestOverride(getClass())) {
             doRequest(request, response, chain);
             return;
         }
 
         chain.doFilter(request, response);
     }
 
     /**
      * Triggers when this filter needs to process an {@linkplain
      * JspUtils#isIncluded included} page. Note that the servlet
      * specification requires {@code <dispatcher>INCLUDE</dispatcher>}
      * to be declared in {@code <filter-mapping/>} for this to work.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     protected void doInclude(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         chain.doFilter(request, response);
     }
 
     /**
      * Triggers when this filter needs to process an {@linkplain
      * JspUtils#isError error} page. Default implementation calls
      * {@link #doRequest}. Note that the servlet specification
      * requires {@code <dispatcher>ERROR</dispatcher>} to be declared in
      * {@code <filter-mapping/>} for this to work.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     protected void doError(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         chain.doFilter(request, response);
     }
 
     /**
      * Triggers when this filter needs to process a {@linkplain
      * JspUtils#isForwarded forwarded} page. Note that the servlet
      * specification requires {@code <dispatcher>FORWARD</dispatcher>}
      * to be declared in {@code <filter-mapping/>} for this to work.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     protected void doForward(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         chain.doFilter(request, response);
     }
 
     /**
      * Triggers when this filter needs to process a normal request.
      *
      * @param request Can't be {@code null}.
      * @param response Can't be {@code null}.
      * @param chain Can't be {@code null}.
      */
     protected void doRequest(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws Exception {
 
         chain.doFilter(request, response);
     }
 
     /**
      * Filter that can automatically configure itself based on other
      * {@link AbstractFilter}s.
      */
     public interface Auto extends Filter {
 
         /**
          * Updates the given {@code dependencies} for the given
          * {@code filterClass}.
          */
         public void updateDependencies(
                 Class<? extends AbstractFilter> filterClass,
                 List<Class<? extends Filter>> dependencies);
     }
 
     // Passes an alternate and unique view of the config to the filter
     // dependencies.
     private class DependencyFilterConfig implements FilterConfig {
 
         private final Filter filter;
 
         public DependencyFilterConfig(Filter filter) {
             this.filter = filter;
         }
 
         // --- FilterConfig support ---
 
         @Override
         public String getFilterName() {
             return filterConfig.getFilterName() +
                     "$" + filter.getClass().getName();
         }
 
         @Override
         public String getInitParameter(String name) {
             return filterConfig.getInitParameter(name);
         }
 
         @Override
         @SuppressWarnings("unchecked")
         public Enumeration<String> getInitParameterNames() {
             return filterConfig.getInitParameterNames();
         }
 
         @Override
         public ServletContext getServletContext() {
             return filterConfig.getServletContext();
         }
     }
 
     // Runs all dependencies first.
     private class DependencyFilterChain implements FilterChain {
 
         private final List<Filter> dependencies;
         private int index;
         private final FilterChain finalChain;
 
         public DependencyFilterChain(List<Filter> dependencies, FilterChain finalChain) {
             this.dependencies = dependencies;
             this.index = -1;
             this.finalChain = finalChain;
         }
 
         // --- FilterChain support ---
 
         @Override
         public void doFilter(
                 ServletRequest request,
                 ServletResponse response)
                 throws IOException, ServletException {
 
             ++ index;
 
             if (index < dependencies.size()) {
                 dependencies.get(index).doFilter(request, response, this);
                 return;
             }
 
             if (request instanceof HttpServletRequest &&
                     response instanceof HttpServletResponse) {
                 try {
                     doDispatch(
                             (HttpServletRequest) request,
                             (HttpServletResponse) response,
                             finalChain);
 
                 } catch (Exception error) {
                     ErrorUtils.rethrowIf(error, IOException.class);
                     ErrorUtils.rethrowIf(error, ServletException.class);
                     ErrorUtils.rethrow(error);
                 }
 
             } else {
                 finalChain.doFilter(request, response);
             }
         }
     }
 }
