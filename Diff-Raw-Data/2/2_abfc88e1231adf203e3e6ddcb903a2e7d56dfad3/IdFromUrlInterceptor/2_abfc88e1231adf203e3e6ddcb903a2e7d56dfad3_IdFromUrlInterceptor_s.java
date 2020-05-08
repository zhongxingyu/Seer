 package org.otherobjects.cms.controllers.interceptors;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.core.CollectionFactory;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 /**
  * Interceptor that tries to read an id value from URLs of the pattern /controller/action/id and tries to then set
  * the read value on the passed in handler (generally a Controller class) by calling a method with the signature setId(String id) if existent.
  * 
  * @author joerg
  *
  */
 public class IdFromUrlInterceptor extends HandlerInterceptorAdapter
 {
     private final Map<String, Method> handlerMethodCache = CollectionFactory.createConcurrentMapIfPossible(16);
     private final static Pattern idPattern = Pattern.compile("^/(\\S*)/(\\S*)/(\\S*)");
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
     {
 
         Matcher matcher = idPattern.matcher(request.getPathInfo());
 
         if (matcher.matches())
         {
             try
             {
                 Method idSetter;
                 if (handlerMethodCache.containsKey(handler.getClass().getName()))
                     idSetter = handlerMethodCache.get(handler.getClass().getName());
                 else
                 {
                     idSetter = handler.getClass().getMethod("setId", new Class[]{String.class});
                     handlerMethodCache.put(handler.getClass().getName(), idSetter);
                 }
 
                 if (idSetter != null)
                     idSetter.invoke(handler, new Object[]{matcher.group(3)});
             }
             catch (Exception e)
             {
                 //noop
             }
         }
         return true;
     }
 }
