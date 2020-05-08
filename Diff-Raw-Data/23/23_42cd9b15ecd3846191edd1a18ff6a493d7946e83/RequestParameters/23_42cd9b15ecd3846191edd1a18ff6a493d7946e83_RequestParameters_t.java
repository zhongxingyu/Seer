 package ixcode.platform.http.protocol.request;
 
 import ixcode.platform.collection.*;
 
 import javax.servlet.http.*;
 import java.util.*;
 
 public class RequestParameters  {
 
     private final FList<RequestParameter> parameters = new FArrayList<RequestParameter>();
 
    public static RequestParameters requestParameters() {
        return new RequestParameters();
    }

     public static RequestParameters requestParametersFrom(HttpServletRequest httpServletRequest) {
         Set<String> parameterNames = httpServletRequest.getParameterMap().keySet();
         RequestParameters requestParameters = new RequestParameters();
         String queryString = httpServletRequest.getQueryString();
         for (String parameterName : parameterNames) {
             requestParameters.withParameter(queryString.contains(parameterName),
                                             parameterName,
                                             httpServletRequest.getParameterValues(parameterName));
         }
         return requestParameters;
     }
 
 
     public void apply(Action<RequestParameter> action) {
         parameters.apply(action);
     }
 
     RequestParameters withQueryParameter(String name, String... values) {
         return withParameter(true, name, values);
     }
 
     private RequestParameters withParameter(boolean isQueryParameter, String parameterName, String... parameterValues) {
         parameters.add(new RequestParameter(parameterName, parameterValues, isQueryParameter));
         return this;
     }
 
 
 }
