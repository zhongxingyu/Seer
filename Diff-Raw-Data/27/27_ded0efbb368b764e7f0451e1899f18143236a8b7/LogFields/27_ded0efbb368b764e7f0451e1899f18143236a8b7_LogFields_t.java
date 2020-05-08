 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpserver.log;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.MDC;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Response;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 
 public final class LogFields
 {
     private LogFields()
     {
     }
 
 
     public static void validateLogFields(final Map<String, LogField> knownFields, final List<String> fields)
     {
         for (String fieldSpecifier : fields) {
             String[] chunks = StringUtils.split(fieldSpecifier, ":");
             String fieldKey = chunks[0];
             if (chunks.length > 2) {
                 throw new IllegalArgumentException(String.format("Bad specifier \"%s\" has too many colons", fieldSpecifier));
             }
 
             if (!knownFields.containsKey(fieldKey)) {
                 throw new IllegalArgumentException(String.format("Invalid log pattern: unknown field <%s>", fieldSpecifier));
             }
         }
     }
 
     public interface LogField
     {
         Object log(Request request, Response response, String dummy);
 
         String getShortName();
     }
 
     public static class RemoteAddrField implements LogField
     {
         RemoteAddrField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getRemoteAddr();
         }
 
         @Override
         public String getShortName()
         {
             return "ip";
         }
     }
 
     public static class MethodField implements LogField
     {
         MethodField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getMethod();
         }
 
         @Override
         public String getShortName()
         {
             return "m";
         }
     }
 
     public static class RequestUriField implements LogField
     {
         RequestUriField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getRequestURL();
         }
 
         @Override
         public String getShortName()
         {
             return "u";
         }
     }
 
     public static class ResponseCodeField implements LogField
     {
         ResponseCodeField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return response.getStatus();
         }
 
         @Override
         public String getShortName()
         {
             return "rc";
         }
     }
 
     public static class ElapsedTimeField implements LogField
     {
         ElapsedTimeField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             final DateTime start = new DateTime(request.getTimeStamp(), DateTimeZone.UTC);
             final DateTime end = new DateTime(DateTimeZone.UTC);
             return new Duration(start, end).getMillis();
         }
 
         @Override
         public String getShortName()
         {
             return "t";
         }
     }
 
     public static class QueryStringField implements LogField
     {
         QueryStringField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getQueryString();
         }
 
         @Override
         public String getShortName()
         {
             return "q";
         }
     }
 
     public static class CookieField implements LogField
     {
         CookieField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String cookieName)
         {
             final Cookie[] cookies = request.getCookies();
             if (StringUtils.trimToNull(cookieName) !=  null && cookies != null) {
                 for (Cookie cookie : cookies) {
                     if (cookieName.equalsIgnoreCase(cookie.getName())) {
                         return cookie.getValue();
                     }
                 }
             }
             return null;
         }
 
         @Override
         public String getShortName()
         {
             return "co";
         }
     }
 
     public static class RequestContentLengthField implements LogField
     {
         RequestContentLengthField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getContentLength();
         }
 
         @Override
         public String getShortName()
         {
             return "cl";
         }
     }
 
    public static class ResponseContentLengthField implements LogField
    {
        ResponseContentLengthField()
        {
        }

        @Override
        public Object log(Request request, Response response, String dummy)
        {
            return response.getContentCount();
        }

        @Override
        public String getShortName()
        {
            return "cc";
        }
    }

     public static class RequestHeaderField implements LogField
     {
         RequestHeaderField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String header)
         {
             if (StringUtils.trimToNull(header) == null) {
                 return null;
             }
             return request.getHeader(header);
         }
 
         @Override
         public String getShortName()
         {
             return "rh";
         }
     }
 
     public static class ResponseHeaderField implements LogField
     {
         ResponseHeaderField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String header)
         {
             if (StringUtils.trimToNull(header) == null) {
                 return null;
             }
             return response.getHeader(header);
         }
 
         @Override
         public String getShortName()
         {
             return "rs";
         }
     }
 
     public static class AttributeField implements LogField
     {
         AttributeField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String attribute)
         {
             return request.getAttribute(attribute);
         }
 
         @Override
         public String getShortName()
         {
             return "ra"; // Request attribute
         }
     }
 
     public static class RequestContentTypeField implements LogField
     {
         RequestContentTypeField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return request.getContentType();
         }
 
         @Override
         public String getShortName()
         {
             return "rt";
         }
     }
 
     public static class ResponseContentTypeField implements LogField
     {
         ResponseContentTypeField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return response.getContentType();
         }
 
         @Override
         public String getShortName()
         {
             return "ru";
         }
     }
 
     public static class RequestDateHeaderField implements LogField
     {
         RequestDateHeaderField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             final long dateValue = request.getDateHeader("Date");
             if (dateValue > 0) {
                 return dateValue;
             }
             return null;
         }
 
         @Override
         public String getShortName()
         {
             return "rd";
         }
     }
 
     public static class ThreadNameField implements LogField
     {
         ThreadNameField()
         {
         }
 
         @Override
         public String log(Request request, Response response, String dummy)
         {
             return Thread.currentThread().getName();
         }
 
         @Override
         public String getShortName()
         {
             return "tn";
         }
     }
 
     public static class TimestampField implements LogField
     {
         TimestampField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String dummy)
         {
             return new DateTime(request.getTimeStamp(), DateTimeZone.UTC);
         }
 
         @Override
         public String getShortName()
         {
             return "ts";
         }
     }
 
     public static class MDCField implements LogField
     {
         MDCField()
         {
         }
 
         @Override
         public Object log(Request request, Response response, String name) {
             return MDC.get(name);
         }
 
         @Override
         public String getShortName()
         {
             return "md";
         }
     }
 }
