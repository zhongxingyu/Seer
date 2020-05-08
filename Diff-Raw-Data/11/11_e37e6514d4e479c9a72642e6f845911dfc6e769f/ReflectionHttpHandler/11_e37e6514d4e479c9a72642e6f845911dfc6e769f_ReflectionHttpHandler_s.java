 /*
  * Copyright 2013 Simon Taddiken
  *
  * This file is part of Polly HTTP API.
  *
  * Polly HTTP API is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, either version 3 of the License, or (at 
  * your option) any later version.
  *
  * Polly HTTP API is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
  * more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with Polly HTTP API. If not, see http://www.gnu.org/licenses/.
  */
 package de.skuzzle.polly.http.internal;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import de.skuzzle.polly.http.api.Controller;
 import de.skuzzle.polly.http.api.HttpEvent;
 import de.skuzzle.polly.http.api.HttpEvent.RequestMode;
 import de.skuzzle.polly.http.api.handler.HttpEventHandler;
 import de.skuzzle.polly.http.api.HttpException;
 import de.skuzzle.polly.http.annotations.Param;
 import de.skuzzle.polly.http.api.ParameterHandler;
 import de.skuzzle.polly.http.api.answers.HttpAnswer;
 import de.skuzzle.polly.http.api.answers.HttpAnswers;
 
 
 class ReflectionHttpHandler implements HttpEventHandler {
     
     private final String uri;
     private final Method handler;
     private final RequestMode mode;
     private final Controller carrier;
     private final HttpServletServerImpl parent;
     private final boolean matchExactly;
     
     
     public ReflectionHttpHandler(RequestMode mode, String uri, Controller carrier, 
             Method handler, HttpServletServerImpl parent, boolean matchExactly) {
         this.matchExactly = matchExactly;
         this.mode = mode;
         this.carrier = carrier;
         this.uri = uri;
         this.handler = handler;
         this.parent = parent;
     }
     
     
     
     @Override
     public HttpAnswer handleHttpEvent(String registered, HttpEvent e, 
             HttpEventHandler next) throws HttpException {
         
         if (e.getMode() != this.mode || 
                 this.matchExactly && !e.getPlainUri().equals(this.uri)) {
             return next.handleHttpEvent(registered, e, next);
         }
         
         
         // extract actual parameters from the request
         final Object[] params = new Object[this.handler.getParameterTypes().length];
         for (int i = 0; i < this.handler.getParameterTypes().length; ++i) {
             final Annotation[] an = this.handler.getParameterAnnotations()[i];
             
             // extract parameter name from annotated method parameter
             // INVARIANT: every parameter is annotated!
             Param key = null;
             for (Annotation a : an) {
                 if (a instanceof Param) {
                     key = (Param) a;
                     break;
                 }
             }
             
             // value associated with that key in the current request 
             String sValue = e.parameterMap(this.mode).get(key.value());
             if (sValue == null) {
                 if (key.treatEmpty()) {
                     sValue = key.ifEmptyValue();
                 } else {
                    return HttpAnswers.newStringAnswer("missing parameter: " + 
                        key.value());
                 }
             }
             
             final Class<?> type = this.handler.getParameterTypes()[i];
             final ParameterHandler ph = this.parent.findHandler(type, key.typeHint());
             // INVARIANT: ph can not be null
             assert ph != null;
             
             params[i] = ph.parse(sValue);
         }
         
         // execute the function
         try {
             final Controller copy = this.carrier.bind(registered, e);
             return (HttpAnswer) this.handler.invoke(copy, params);
         } catch (InvocationTargetException e1) {
             if (e1.getTargetException() instanceof HttpException) {
                 throw (HttpException) e1.getTargetException();
             }
             throw new HttpException(e1.getTargetException());
         } catch (Exception e1) {
             throw new HttpException(e1);
         }
     }
 }
