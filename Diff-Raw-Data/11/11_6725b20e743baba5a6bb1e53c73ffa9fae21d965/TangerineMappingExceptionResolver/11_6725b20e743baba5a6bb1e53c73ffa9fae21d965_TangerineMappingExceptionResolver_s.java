 package com.orangeleap.tangerine.web.common;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 
 public class TangerineMappingExceptionResolver extends SimpleMappingExceptionResolver {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Override
     public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object command, Exception exception) {
         logger.error(ExceptionUtils.getStackTrace(exception));
         return super.resolveException(request, response, command, exception);
     }
 }
