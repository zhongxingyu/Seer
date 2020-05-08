 package vinna;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vinna.exception.ConfigException;
 import vinna.exception.VuntimeException;
 import vinna.http.Request;
 import vinna.route.*;
 
 import java.io.*;
 import java.util.*;
 
 public class Vinna {
     private static final Logger logger = LoggerFactory.getLogger(Vinna.class);
 
     public static final String BASE_PACKAGE = "base-package";
     public static final String ROUTES = "routes";
     public static final String CONF = "conf";
     public static final String CONTROLLER_FACTORY = "controller-factory";
 
     private Map<String, Object> config;
     private String basePackage;
     private Router router;
     private ControllerFactory controllerFactory;
     private List<ActionArgument> routeParameters;
 
     // is true when a route is created with the programmatic API without specify a controller and/or a method
     private boolean isDirtyState = false;
 
     protected final RequestBuilder req = new RequestBuilder();
 
     public void init(Map<String, Object> config) {
         this.config = new HashMap<>(config);
         if (config.get(BASE_PACKAGE) == null) {
             basePackage = getClass().getPackage().getName();
             this.config.put(BASE_PACKAGE, basePackage);
         } else {
             basePackage = (String) config.get(BASE_PACKAGE);
         }
 
         conf(this.config);
 
         this.controllerFactory = controllerFactory(this.config);
 
         this.router = new Router();
         routes(this.config);
 
         if (isDirtyState) {
             // TODO enhance the message
             throw new ConfigException("Something is going wrong");
         }
     }
 
     public Map<String, Object> getConfig() {
         return Collections.unmodifiableMap(config);
     }
 
     protected void conf(Map<String, Object> config) {
         List<String> confPaths = new ArrayList<>();
         confPaths.add("vinna/conf.properties");
         if (config.get(CONF) == null) {
             String path = this.basePackage.replace(".", "/") + "/conf.properties";
             if (!confPaths.contains(path)) {
                 final Reader reader = getRoutesReader(path);
                 if (reader != null) {
                     confPaths.add(path);
                     try {
                         reader.close();
                     } catch (IOException e) {
                         logger.warn("Cannot close conf file '" + path + "'", e);
                     }
                 }
             }
         } else {
             String[] userConfPaths = ((String) config.get(CONF)).trim().split("\\s*,\\s*");
             for (String userConfPath : userConfPaths) {
                 if (!confPaths.contains(userConfPath)) {
                     confPaths.add(userConfPath);
                 }
             }
         }
         for (String confPath : confPaths) {
             logger.info("loading conf file {}", confPath);
             Reader reader = getConfReader(confPath);
             if (reader == null) {
                 throw new VuntimeException("Cannot open conf file '" + confPath + "'");
             }
             try {
                 loadConf(reader, config);
             } catch (IOException e) {
                 throw new VuntimeException("Error while reading conf file " + confPath, e);
             }
         }
     }
 
     protected void loadConf(Reader reader, Map<String, Object> config) throws IOException {
         Properties props = new Properties();
         props.load(reader);
         for (Map.Entry<Object, Object> entry : props.entrySet()) {
             config.put(entry.getKey().toString(), injectVariables((String) entry.getValue(), config));
         }
     }
 
     protected String injectVariables(String s, Map<String, Object> values) {
         for (Map.Entry<String, Object> e : values.entrySet()) {
             if (e.getValue() != null) {
                 s = s.replace("{" + e.getKey() + "}", e.getValue().toString());
             }
         }
         return s;
     }
 
     protected Reader getConfReader(String confPath) {
         InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(confPath);
         if (stream == null) {
             return null;
         } else {
             try {
                 return new InputStreamReader(stream, "utf-8");
             } catch (UnsupportedEncodingException e) {
                 throw new VuntimeException("Error opening conf file '" + confPath + "'", e);
             }
         }
     }
 
     /**
      * Override to define the app routes
      *
      * @param config
      */
     protected void routes(Map<String, Object> config) {
         String[] routesPaths;
         if (config.get(ROUTES) == null) {
             String path = this.basePackage.replace(".", "/") + "/routes";
             final Reader reader = getRoutesReader(path);
             if (reader == null) {
                 path = "vinna/routes";
             } else {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     logger.warn("Cannot close routes file '" + path + "'", e);
                 }
             }
             routesPaths = new String[]{path};
         } else {
             routesPaths = ((String) config.get(ROUTES)).trim().split("\\s*,\\s*");
         }
         for (String routesPath : routesPaths) {
             Reader reader = getRoutesReader(routesPath);
             if (reader == null) {
                 throw new VuntimeException("Cannot open routes file '" + routesPath + "'");
             }
             loadRoutes(reader);
         }
 
         /*try {
             final URL resource = getClass().getClassLoader().getResource(routeFile);
             if (resource != null) {
                 if ("file".equals(resource.getProtocol())) {
 
                 }
                 System.out.println(resource);
                 File file = new File(resource.toURI());
                 System.out.println(file.exists());
             } else {
 
             }
 
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
 */
         //TODO: define default catchall routes
     }
 
     protected Reader getRoutesReader(String routesPath) {
         InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(routesPath);
         if (stream == null) {
             return null;
         } else {
             try {
                 return new InputStreamReader(stream, "utf-8");
             } catch (UnsupportedEncodingException e) {
                 throw new VuntimeException("Error opening routes file '" + routesPath + "'", e);
             }
         }
     }
 
     /**
      * override to provide a custom controller factory
      *
      * @param config
      * @return a ... you guessed right, a controller factory
      */
     protected ControllerFactory controllerFactory(Map<String, Object> config) {
         if (config.get(CONTROLLER_FACTORY) == null) {
             return new DefaultControllerFactory(this.basePackage);
         } else {
             try {
                 Class<ControllerFactory> clz = (Class<ControllerFactory>) Thread.currentThread().getContextClassLoader().loadClass((String) config.get(CONTROLLER_FACTORY));
                 return clz.newInstance();
             } catch (ClassNotFoundException e) {
                 throw new ConfigException("Cannot find the controller factory class", e);
             } catch (InstantiationException | IllegalAccessException e) {
                 throw new ConfigException("Cannot instantiate the controller factory", e);
             }
         }
     }
 
     public final Object createController(String id, Class<?> clazz) {
         return controllerFactory.create(id, clazz);
     }
 
     public final RouteResolution match(Request request) {
         return router.match(request);
     }
 
     public final void addRoute(Route route) {
         logger.debug("Route created: {}", route);
         this.isDirtyState = false;
         this.router.addRoute(route);
     }
 
     public String getBasePackage() {
         return basePackage;
     }
 
     /**
      * Default user locale detection strategy. Use the container's {@link javax.servlet.ServletRequest#getLocale()}.
      * Override to use a different strategy, like retrieving the locale from a cookie or from the path.
      *
      * @return a locale that'll be used by various parts of the framework, like the validation subsystem.
      */
     public Locale getUserLocale() {
         return VinnaContext.get().request.getLocale();
     }
 
     protected void loadRoutes(Reader reader) {
         String prefix = (String) config.get("routes-prefix");
         if (prefix == null) {
             prefix = "";
         }
         List<Route> routes = new RoutesParser(reader).load(prefix);
         router.addRoutes(routes);
     }
 
     protected final RouteBuilder get(String path) {
         return createRoute("GET", path);
     }
 
     protected final RouteBuilder post(String path) {
         return createRoute("POST", path);
     }
 
     protected final RouteBuilder createRoute(String verb, String path) {
         if (isDirtyState) {
             // TODO enhance the message
             throw new ConfigException("Something is going wrong.");
         }
         routeParameters = new ArrayList<>();
         isDirtyState = true;
         String prefix = (String) config.get("routes-prefix");
         if (prefix == null) {
             prefix = "";
         }
         return new RouteBuilder(verb.toUpperCase(), prefix + path, this, routeParameters);
     }
 
     protected final ActionArgument.Variable param(String name) {
         ActionArgument.Variable param = new ActionArgument.Variable(name);
         routeParameters.add(param);
         return param;
     }
 
     protected final <T> T constant(T value) {
         ActionArgument param = new ActionArgument.Const<>(value);
         routeParameters.add(param);
         return value;
     }
 
     protected final class RequestBuilder {
 
         private RequestBuilder() {
         }
 
         public final ActionArgument.Header header(String name) {
             ActionArgument.Header param = new ActionArgument.Header(name);
             routeParameters.add(param);
             return param;
         }
 
         public final Map<String, Collection<String>> headers() {
             ActionArgument.Headers headers = new ActionArgument.Headers();
             routeParameters.add(headers);
             return null;
         }
 
         public final ActionArgument.RequestParameter param(String name) {
             ActionArgument.RequestParameter param = new ActionArgument.RequestParameter(name);
             routeParameters.add(param);
             return param;
         }
 
         public final Map<String, Collection<String>> params() {
             ActionArgument.RequestParameters headers = new ActionArgument.RequestParameters();
             routeParameters.add(headers);
             return null;
         }
 
         public final InputStream body() {
             ActionArgument.RequestBody bodyActionArgument = new ActionArgument.RequestBody();
             routeParameters.add(bodyActionArgument);
             return null;
         }
     }
 }
