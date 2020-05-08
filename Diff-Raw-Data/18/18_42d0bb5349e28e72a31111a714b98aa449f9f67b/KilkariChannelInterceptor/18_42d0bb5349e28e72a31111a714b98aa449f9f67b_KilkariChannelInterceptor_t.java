 package org.motechproject.ananya.kilkari.web.interceptors;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class KilkariChannelInterceptor extends HandlerInterceptorAdapter {
 
     public static final String IVR_CHANNEL = "ivr";
     public static final String IVR_RESPONSE_FORMAT = "var response = ";
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
             throws java.lang.Exception {
         if (isIvrChannelRequest(request)) response.getOutputStream().print(IVR_RESPONSE_FORMAT);
         return true;
     }
 
     public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
             throws Exception {
        if (isIvrChannelRequest(request)) response.setContentType("application/javascript;charset=UTF-8");
     }
 
     private boolean isIvrChannelRequest(HttpServletRequest request) {
         return request.getParameterMap().containsKey("channel") &&
                 request.getParameter("channel").equalsIgnoreCase(IVR_CHANNEL);
     }
 
 }
