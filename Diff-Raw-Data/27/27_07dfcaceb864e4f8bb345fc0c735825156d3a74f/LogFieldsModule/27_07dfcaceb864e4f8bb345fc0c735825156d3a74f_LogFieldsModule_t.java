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
 
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Scopes;
 import com.nesscomputing.httpserver.log.LogFields.AttributeField;
 import com.nesscomputing.httpserver.log.LogFields.CookieField;
 import com.nesscomputing.httpserver.log.LogFields.ElapsedTimeField;
 import com.nesscomputing.httpserver.log.LogFields.MDCField;
 import com.nesscomputing.httpserver.log.LogFields.MethodField;
 import com.nesscomputing.httpserver.log.LogFields.QueryStringField;
 import com.nesscomputing.httpserver.log.LogFields.RemoteAddrField;
 import com.nesscomputing.httpserver.log.LogFields.RequestContentLengthField;
 import com.nesscomputing.httpserver.log.LogFields.RequestContentTypeField;
 import com.nesscomputing.httpserver.log.LogFields.RequestDateHeaderField;
 import com.nesscomputing.httpserver.log.LogFields.RequestHeaderField;
 import com.nesscomputing.httpserver.log.LogFields.RequestUriField;
 import com.nesscomputing.httpserver.log.LogFields.ResponseCodeField;
import com.nesscomputing.httpserver.log.LogFields.ResponseContentLengthField;
 import com.nesscomputing.httpserver.log.LogFields.ResponseContentTypeField;
 import com.nesscomputing.httpserver.log.LogFields.ResponseHeaderField;
 import com.nesscomputing.httpserver.log.LogFields.ThreadNameField;
 import com.nesscomputing.httpserver.log.LogFields.TimestampField;
 
 public class LogFieldsModule extends AbstractModule
 {
     @Override
     public void configure()
     {
         LogFieldBinder.bindField(binder(), "attribute").to(AttributeField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "requestHeader").to(RequestHeaderField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "requestDateHeader").to(RequestDateHeaderField.class).in(Scopes.SINGLETON); // Normalizes HTTP dates to ISO dates
         LogFieldBinder.bindField(binder(), "responseHeader").to(ResponseHeaderField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "requestContentLength").to(RequestContentLengthField.class).in(Scopes.SINGLETON);
        LogFieldBinder.bindField(binder(), "responseContentLength").to(ResponseContentLengthField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "requestContentType").to(RequestContentTypeField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "responseContentType").to(ResponseContentTypeField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "method").to(MethodField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "requestUri").to(RequestUriField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "cookie").to(CookieField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "query").to(QueryStringField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "remoteAddr").to(RemoteAddrField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "timestamp").to(TimestampField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "responseCode").to(ResponseCodeField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "threadName").to(ThreadNameField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "elapsedTime").to(ElapsedTimeField.class).in(Scopes.SINGLETON);
         LogFieldBinder.bindField(binder(), "mdcField").to(MDCField.class).in(Scopes.SINGLETON);
     }
 }
