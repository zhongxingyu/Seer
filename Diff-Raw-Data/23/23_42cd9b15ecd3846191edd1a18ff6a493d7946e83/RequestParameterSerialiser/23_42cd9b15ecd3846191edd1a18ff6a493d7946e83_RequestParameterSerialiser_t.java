 package ixcode.platform.http.protocol.request;
 
 import ixcode.platform.collection.*;
 
 import java.util.*;
 
 import static java.util.Arrays.asList;
 
 public class RequestParameterSerialiser {
     private RequestParameters requestParameters;
 
     public static RequestParameterSerialiser serialise(RequestParameters requestParameters) {
         return new RequestParameterSerialiser(requestParameters);
     }
 
     public RequestParameterSerialiser(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
     }
 
     public String toJson() {
         final JsonBuilder json = new JsonBuilder();
 
         requestParameters.apply(new Action<RequestParameter>() {
             @Override public void to(RequestParameter parameter, Collection<RequestParameter> tail) {
                json.appendParameter(parameter);
                if (!tail.isEmpty()) {
                 json.append(", ");
                }
             }
         });
 
         return json.toString();
     }
 
     private static class JsonBuilder {
         private final StringBuilder json = new StringBuilder();
 
         public JsonBuilder() {
             json.append("{ \n\"httpRequest\" : {\n");
         }
 
 
         public void appendParameter(RequestParameter parameter) {
             json.append("    \"").append(parameter.name).append("\" : ");
             appendValues(parameter.parameterValues);
         }
 
         private void appendValues(String[] parameterValues) {
             if (parameterValues.length == 1) {
                 appendSingleParameterValue(parameterValues[0]);
             } else {
                 appendArrayOfParameterValues(parameterValues);
             }
         }
 
         private void appendArrayOfParameterValues(String... parameterValues) {
             json.append("[\n");
             for (Iterator<String> itr = asList(parameterValues).iterator(); itr.hasNext();) {
                 String value = itr.next();
                 json.append("        ");
                 appendParameterValue(value);
                 if (itr.hasNext()) { json.append(","); }
                 json.append("\n");
             }
             json.append("    ]\n");
         }
 
         private void appendSingleParameterValue(String parameterValue) {
             appendParameterValue(parameterValue);
             json.append("\n");
         }
         private void appendParameterValue(String parameterValue) {
             json.append("\"").append(parameterValue).append("\"");
         }
 
 
         public String toString() {
             json.append(" }\n}");
             return json.toString();
         }
 
         public void append(String text) {
             json.append(text);
         }
     }
 
 }
