 package sterling.web;
 
 import static java.util.Arrays.asList;
 import static sterling.lang.expression.ExpressionConversions.convertString;
 import static sterling.lang.expression.ExpressionConversions.convertSymbol;
 import static sterling.lang.expression.ExpressionFactory.nothing;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import sterling.lang.SterlingException;
 import sterling.lang.expression.AbstractExpression;
 import sterling.lang.expression.Expression;
 import sterling.lang.expression.Symbol;
 
 public class SterlingWebApp extends AbstractExpression {
 
     private static final List<String> METHODS = asList("GET", "POST", "PUT", "DELETE", "ERROR");
     private static final String ERROR = "ERROR";
     private static final String HTTP_GET = "GET";
     private static final String HTTP_HEAD = "HEAD";
     private static final String NOT_FOUND = "404";
     private static final String NOT_ALLOWED = "405";
 
     private final Map<String, Map<String, Expression>> handlers;
     private final Expression errorNotFound;
     private final Expression errorNotAllowed;
 
     public SterlingWebApp(Expression errorNotFound, Expression errorNotAllowed) {
         this.handlers = new HashMap<>();
         this.errorNotFound = errorNotFound;
         this.errorNotAllowed = errorNotAllowed;
     }
 
     @Override
     public Expression access(Expression member) throws SterlingException {
         Symbol symbol = convertSymbol(member);
         if (super.defines(symbol)) {
             return super.access(symbol);
         } else {
             return new PathCollector(this, symbol.getValue());
         }
     }
 
     public void addHandler(String method, String pathInfo, Expression handler) {
         method = normalizeMethod(method);
         pathInfo = normalizePathInfo(pathInfo);
         if (!METHODS.contains(method)) {
             throw new IllegalArgumentException("Invalid method: " + method);
         }
         if (!handlers.containsKey(pathInfo)) {
             handlers.put(pathInfo, new HashMap<String, Expression>());
         }
         handlers.get(pathInfo).put(method.toUpperCase(), handler);
     }
 
     public Expression getHandlerFor(String method, String pathInfo) {
         method = normalizeMethod(method);
         pathInfo = normalizePathInfo(pathInfo);
         if (handlers.containsKey(pathInfo)) {
             if (handlers.get(pathInfo).containsKey(method)) {
                 return handlers.get(pathInfo).get(method);
            } else if (ERROR.equals(method) && NOT_ALLOWED.equals(pathInfo)) {
                return errorNotAllowed;
             } else {
                 return getHandlerFor(ERROR, NOT_ALLOWED);
             }
        } else if (ERROR.equals(method) && NOT_FOUND.equals(pathInfo)) {
            return errorNotFound;
        } else {
            return getHandlerFor(ERROR, NOT_FOUND);
         }
     }
 
     public List<String> getOptions(String path) {
         String pathInfo = normalizePathInfo(path);
         List<String> options = new ArrayList<>();
         for (String method : handlers.keySet()) {
             if (handlers.get(method).containsKey(pathInfo)) {
                 options.add(method);
             }
         }
         if (options.contains(HTTP_GET)) {
             options.add(HTTP_HEAD);
         }
         return options;
     }
 
     public void handle(HttpServletRequest request, HttpServletResponse response) throws SterlingException {
         String method = request.getMethod().toUpperCase();
         if (HTTP_HEAD.equals(method)) {
             method = HTTP_GET;
         }
         Expression handler = getHandlerFor(method, request.getPathInfo());
         handler = handler.apply(new SterlingWebRequest(request));
         handler = handler.apply(new SterlingWebResponse(response));
         handler.reduce();
     }
 
     private String normalizeMethod(String method) {
         return method.toUpperCase();
     }
 
     private String normalizePathInfo(String pathInfo) {
         return pathInfo == null || pathInfo.isEmpty() ? "/" : pathInfo;
     }
 
     private static final class HandlerRegistrar extends AbstractExpression {
 
         private final SterlingWebApp webapp;
         private final String method;
         private final String pathInfo;
 
         public HandlerRegistrar(SterlingWebApp webapp, String method, String pathInfo) {
             this.webapp = webapp;
             this.method = method;
             this.pathInfo = pathInfo;
         }
 
         @Override
         public Expression apply(Expression argument) throws SterlingException {
             webapp.addHandler(method, pathInfo, argument);
             return nothing();
         }
     }
 
     private static final class PathCollector extends AbstractExpression {
 
         private final SterlingWebApp webapp;
         private final String method;
 
         public PathCollector(SterlingWebApp webapp, String method) {
             this.webapp = webapp;
             this.method = method;
         }
 
         @Override
         public Expression apply(Expression argument) throws SterlingException {
             return new HandlerRegistrar(webapp, method, convertString(argument).getValue());
         }
     }
 }
