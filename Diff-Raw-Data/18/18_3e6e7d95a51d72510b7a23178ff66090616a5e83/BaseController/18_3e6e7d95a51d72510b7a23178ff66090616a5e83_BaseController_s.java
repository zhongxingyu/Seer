 package org.motechproject.ananya.referencedata.web.controller;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.motechproject.ananya.referencedata.flw.response.BaseResponse;
 import org.motechproject.ananya.referencedata.flw.validators.CSVRequestValidationException;
 import org.motechproject.ananya.referencedata.flw.validators.ValidationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public abstract class BaseController {
     private Logger logger = LoggerFactory.getLogger(BaseController.class);
 
 
     @ExceptionHandler(Exception.class)
     @ResponseBody
     public BaseResponse handleException(final Exception exception, HttpServletResponse response) {
         response.setContentType("application/json; application/xml");
         if (exception instanceof ValidationException || exception instanceof HttpMessageNotReadableException) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             logger.warn(getExceptionString(exception));
         } else {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             logger.error(getExceptionString(exception));
         }
         return BaseResponse.failure(exception.getMessage());
     }
 
     @ExceptionHandler(CSVRequestValidationException.class)
     @ResponseBody
     public void handleCsvException(final Exception exception, HttpServletResponse response) throws IOException {
         response.setContentType("text/plain");
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.getOutputStream().write(getExceptionString(exception).getBytes());
         logger.error(getExceptionString(exception));
     }
 
     private String getExceptionString(Exception ex) {
         StringBuilder sb = new StringBuilder();
         sb.append(ExceptionUtils.getMessage(ex));
        sb.append(ExceptionUtils.getStackTrace(ex));
        sb.append(ExceptionUtils.getRootCauseMessage(ex));
        sb.append(ExceptionUtils.getRootCauseStackTrace(ex));
         return sb.toString();
     }
 }
 
