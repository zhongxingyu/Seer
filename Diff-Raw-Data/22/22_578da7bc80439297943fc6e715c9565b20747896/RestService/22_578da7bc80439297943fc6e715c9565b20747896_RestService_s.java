 package org.inigma.shared.webapp;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.inigma.shared.message.NoopMessageSource;
 import org.inigma.shared.tools.ClassUtil;
 import org.inigma.shared.tools.CollectionsUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.HttpStatus.Series;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 /**
  * Base web service providing access to a common set of functionality.
  *
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  * @since 12/20/13 11:54 PM
  */
 public abstract class RestService {
     protected static final ObjectMapper MAPPER = new ObjectMapper();
     static final String REQUEST_AS_STRING = "org.inigma.request.string";
     private static final String REQUEST_FOR_FIELD_CHECK = "org.inigma.request.fieldCheck";
     private static final String HTTP_STATUS = "org.inigma.request.httpStatus";
    private static final String ERRORS = "org.inigma.errors";
     private static final String RESPONSE_OBJECT = "org.inigma.response.object";
     private static final Splitter PATH_SPLITTER = Splitter.on(CharMatcher.anyOf(".[]")).trimResults()
             .omitEmptyStrings();
 
     private static class ResponseException extends RuntimeException {
     }
 
     public static Authentication getAuthentication() {
         return SecurityContextHolder.getContext().getAuthentication();
     }
 
     public static <T> T getRequest(Class<T> type) {
         try {
             return MAPPER.readValue(getRawRequestBody(), type);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public static boolean isErrorCondition() {
         HttpStatus status = getHttpStatus();
         return !getInternalErrors().isEmpty() || status.series() == Series.CLIENT_ERROR
                 || status.series() == Series.SERVER_ERROR;
     }
 
     public static boolean isRejected() {
         return !getInternalErrors().isEmpty();
     }
 
     public static boolean isRequestFieldDefined(String field) {
         List<String> path = Lists.newArrayList(getPathParts(field));
         Object root = getHttpServletRequest().getAttribute(REQUEST_FOR_FIELD_CHECK);
         if (root == null) {
             switch (getRawRequestBody().charAt(0)) {
                 case '{':
                     root = getRequest(Map.class);
                     break;
                 case '[':
                     root = getRequest(List.class);
                     break;
                 default:
                     root = new Object();
             }
             getHttpServletRequest().setAttribute(REQUEST_FOR_FIELD_CHECK, root);
         }
         Object parent = evaluateExpression(root, path.subList(0, path.size() - 1));
         if (parent instanceof Map) {
             return ((Map) parent).containsKey(path.get(path.size() - 1));
         }
         if (parent instanceof List) {
             Integer index = GuavaShim.ints_tryParse(path.get(path.size() - 1));
             return index != null && index < ((List) parent).size();
         }
         return false;
     }
 
     public static void reject(String code) {
         reject(code, null);
     }
 
     public static void reject(String code, String message) {
         getInternalErrors().add(new GlobalError(code, message));
     }
 
     public static void rejectField(String field, String code) {
         rejectField(field, code, null);
     }
 
     public static void rejectField(String field, String code, String message) {
         getInternalErrors().add(new FieldError(field, code, message));
     }
 
     public static void rejectIfEmptyOrWhitespace(String field) {
         rejectIfEmptyOrWhitespace(field, true);
     }
 
     public static void rejectIfEmptyOrWhitespace(String field, boolean defined) {
         Map request = getRequest(Map.class);
         if (!defined && !isRequestFieldDefined(field)) {
             // short circuit, field is undefined so do not validate it
             return;
         }
 
         Object value = evaluateExpression(request, getPathParts(field));
         if (value == null) {
             rejectField(field, "required");
         } else if (!StringUtils.hasText(value.toString())) {
             rejectField(field, "blank");
         }
     }
 
     public static void rejectIfWhitespace(String field) {
         rejectIfWhitespace(field, true);
     }
 
     public static void rejectIfWhitespace(String field, boolean defined) {
         Map request = getRequest(Map.class);
         if (!defined && !isRequestFieldDefined(field)) {
             // short circuit, field is undefined so do not validate it
             return;
         }
 
         Object value = evaluateExpression(request, getPathParts(field));
         if (value != null && !StringUtils.hasText(value.toString())) {
             rejectField(field, "blank");
         }
     }
 
     public static void stopImmediately(String code) {
         stopImmediately(code, (String) null);
     }
 
     public static void stopImmediately(String code, String message) {
         stopImmediately(code, message, HttpStatus.BAD_REQUEST);
     }
 
     public static void stopImmediately(String code, HttpStatus status) {
         stopImmediately(code, null, status);
     }
 
     public static void stopImmediately(String code, String message, HttpStatus status) {
         setHttpStatus(status);
         reject(code, message);
         stopOnRejections();
     }
 
     public static void stopOnErrorCondition() {
         if (isErrorCondition()) {
             throw new ResponseException();
         }
     }
 
     public static void stopOnRejections() {
         if (isRejected()) {
             throw new ResponseException();
         }
     }
 
     private static Object evaluateExpression(Object source, Iterable<String> fieldPath) {
         for (String path : fieldPath) {
             if (source instanceof Map) {
                 source = ((Map) source).get(path);
             } else if (source instanceof List) {
                 Integer index = GuavaShim.ints_tryParse(path);
                 List list = (List) source;
                 if (index == null || index >= list.size()) {
                     return null;
                 }
                 source = list.get(index);
             } else {
                 return null;
             }
         }
         return source;
     }
 
     private static HttpServletRequest getHttpServletRequest() {
         return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
     }
 
     private static HttpStatus getHttpStatus() {
         HttpStatus status = (HttpStatus) getHttpServletRequest().getAttribute(HTTP_STATUS);
         if (status == null) {
             return HttpStatus.OK;
         }
         return status;
     }
 
     private static List<GlobalError> getInternalErrors() {
         HttpServletRequest request = getHttpServletRequest();
        List<GlobalError> errors = (List<GlobalError>) request.getAttribute(ERRORS);
        if (errors == null) {
            errors = CollectionsUtil.appendOnlyList();
            request.setAttribute(ERRORS, errors);
        }
        return errors;
     }
 
     private static Iterable<String> getPathParts(String field) {
         return PATH_SPLITTER.split(field);
     }
 
     private static String getRawRequestBody() {
         return (String) getHttpServletRequest().getAttribute(REQUEST_AS_STRING);
     }
 
     private static void setHttpStatus(HttpStatus httpStatus) {
         getHttpServletRequest().setAttribute(HTTP_STATUS, httpStatus);
     }
 
     protected Logger logger = LoggerFactory.getLogger(getClass());
     @Autowired(required = false)
     private MessageSource messageSource = new NoopMessageSource();
 
     @ExceptionHandler({ AccessDeniedException.class, InsufficientAuthenticationException.class })
     public Response handleAccessDeniedException() {
         reject("unauthorized");
         return response(null, HttpStatus.UNAUTHORIZED);
     }
 
     @ExceptionHandler({ ResponseException.class })
     public Response handleResponseException() {
         return response();
     }
 
     @ExceptionHandler
     public Response handleUnknownExceptions(Exception e) {
         logger.error("Internal Server Exception", e);
         reject("exception", e.getMessage());
         return response(null, HttpStatus.INTERNAL_SERVER_ERROR);
     }
 
     protected <T> T createResponse(Class<T> c) {
         try {
             Constructor<T> constructor = c.getDeclaredConstructor();
             if ((constructor.getModifiers() & Modifier.PRIVATE) != 0) {
                 throw new IllegalArgumentException(c.getCanonicalName() + " empty constructor is private.");
             }
             constructor.setAccessible(true);
             T t = constructor.newInstance();
             setResponse(t);
             return t;
         } catch (InstantiationException e) {
             throw new IllegalStateException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         } catch (NoSuchMethodException e) {
             throw new IllegalStateException(e);
         } catch (InvocationTargetException e) {
             throw new IllegalStateException(e);
         }
     }
 
     protected Response response() {
         setErrorMessages();
         List<GlobalError> errors = getInternalErrors();
         Object responseObject = getHttpServletRequest().getAttribute(RESPONSE_OBJECT);
         if (responseObject == null) {
         } else if (responseObject instanceof Map) {
             responseObject = Maps.newLinkedHashMap((Map) responseObject);
         } else if (responseObject instanceof Number) {
         } else if (responseObject instanceof Boolean) {
         } else if (responseObject instanceof List) {
         } else if (responseObject instanceof Iterable) {
             responseObject = ImmutableList.copyOf((Iterable) responseObject);
         } else if (responseObject instanceof Iterator) {
             responseObject = ImmutableList.copyOf((Iterator) responseObject);
         } else if (responseObject instanceof Object[]) {
         } else if (responseObject instanceof Date) {
         } else if (responseObject instanceof Calendar) {
         } else if (responseObject instanceof String
                 || ClassUtil.instanceOf(responseObject, "org.bson.types.ObjectId")) {
             responseObject = '"' + responseObject.toString() + '"';
         } else {
             try {
                 responseObject = MAPPER.readValue(MAPPER.writeValueAsBytes(responseObject), Map.class);
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
         }
         if (isErrorCondition() && !(responseObject instanceof Map)) {
             responseObject = Collections.singletonMap("errors", errors);
         } else if (responseObject instanceof Map) {
             ((Map) responseObject).put("errors", errors);
         }
 
         HttpStatus status = getHttpStatus();
         // It is ok to have no errors but return an error code. It is not ok to have errors but return a success.
         // No status assumes success.
         if (isRejected() && status.series() != Series.CLIENT_ERROR && status.series() != Series.SERVER_ERROR) {
             status = HttpStatus.BAD_REQUEST;
         }
         return new Response(responseObject, status);
     }
 
     protected Response response(Object responseObject) {
         setResponse(responseObject);
         return response();
     }
 
     protected Response response(Object responseObject, HttpStatus status) {
         setHttpStatus(status);
         setResponse(responseObject);
         return response();
     }
 
     protected void setErrorMessages() {
         for (GlobalError error : getInternalErrors()) {
             if (error.getMessage() == null) {
                 String code = error.getCode();
                 if (error instanceof FieldError) {
                     code += "." + ((FieldError) error).getField();
                 }
                 error.setMessage(messageSource.getMessage(code, null, LocaleContextHolder.getLocale()));
             }
         }
     }
 
     protected void setResponse(Object object) {
         getHttpServletRequest().setAttribute(RESPONSE_OBJECT, object);
     }
 }
