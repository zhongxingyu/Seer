 /**
  * Copyright (c) 2009-2010 Zauber S.A. <http://www.zaubersoftware.com/>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ar.com.zauber.leviathan.common;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import ar.com.zauber.leviathan.api.URIFetcherHttpResponse;
 import ar.com.zauber.leviathan.api.URIFetcherResponse;
 
 /**
  * Inmutable {@link URIFetcherResponse} response
  *
  * @author Juan F. Codagnone
  * @since Oct 12, 2009
  */
 public class InmutableURIFetcherResponse implements URIFetcherResponse,
              Serializable {
     private static final long serialVersionUID = -1320437506948659742L;
     private final URIAndCtx ctx;
     private final Throwable throwable;
     private final URIFetcherHttpResponse httpResponse;
 
     /** success constructor */
     public InmutableURIFetcherResponse(final URI uri,
             final URIFetcherHttpResponse httpResponse) {
         this(new InmutableURIAndCtx(uri), httpResponse);
     }
 
     /** success constructor */
     public InmutableURIFetcherResponse(final URIAndCtx ctx,
             final URIFetcherHttpResponse httpResponse) {
         Validate.notNull(ctx, "uri is null");
         Validate.notNull(httpResponse, "http response is null");
 
         this.ctx = ctx;
         this.throwable = null;
         this.httpResponse = httpResponse;
     }
 
     /** error constructor */
     public InmutableURIFetcherResponse(final URI uri,
             final Throwable throwable) {
         this(new InmutableURIAndCtx(uri), throwable);
     }
 
     /** error constructor */
     public InmutableURIFetcherResponse(final URIAndCtx ctx,
             final Throwable throwable) {
         Validate.notNull(ctx, "ctx is null");
         Validate.notNull(throwable, "Throwable is null");
 
         this.ctx = ctx;
         this.throwable = throwable;
         this.httpResponse = null;
     }
 
     /** @see URIFetcherResponse#getURI() */
     public final URI getURI() {
         return ctx.getURI();
     }
 
     /** @see URIFetcherResponse#getError() */
     public final Throwable getError() throws IllegalStateException {
         if(throwable == null) {
             throw new IllegalStateException(
                     "Only call getError() if isSucceeded returned false");
         }
         return throwable;
     }
 
     /** @see URIFetcherResponse#getHttpResponse() */
     public final URIFetcherHttpResponse getHttpResponse()
             throws IllegalStateException {
         if(httpResponse == null) {
             throw new IllegalStateException(
                    "Only call getHttpResponse() if isSucceeded returned true");
         }
 
         return httpResponse;
 
     }
 
     /** @see URIFetcherResponse#isSucceeded() */
     public final boolean isSucceeded() {
         return throwable == null;
     }
 
 
     /** @see URIFetcherResponse#getURIAndCtx() */
     public final URIAndCtx getURIAndCtx() {
         return ctx;
     }
 
     /** @see java.lang.Object#toString() */
     @Override
     public final String toString() {
         return new ToStringBuilder(this, ModelUtils.STYLE)
         .append("uri", ctx.getURI())
         .append("error", throwable)
         .append("response", httpResponse)
         .toString();
     }
 
     /** @see Object#equals(Object) */
     @Override
     public final boolean equals(final Object obj) {
         boolean ret = false;
 
         if(obj == this) {
             ret = true;
         } else if(obj instanceof InmutableURIFetcherResponse) {
             final InmutableURIFetcherResponse r =
                 (InmutableURIFetcherResponse) obj;
             ret = ctx.getURI().equals(r.ctx.getURI());
 
             if(throwable == null && r.throwable == null) {
                 // nada que hacer
             } else if(throwable != null && r.throwable != null) {
                 // en general las excepciones no implementan equals
                 // tampoco lo tiene getClass
                 ret &= throwable.getClass().getName().equals(
                         r.throwable.getClass().getName());
             } else {
                 ret = false;
             }
 
             if(httpResponse == null && r.httpResponse == null) {
                 // nada que hacer
             } else if(httpResponse != null && r.httpResponse != null) {
                 ret &= httpResponse.equals(r.httpResponse);
             } else {
                 ret = false;
             }
         }
 
         return ret;
     }
 
     /** @see Object#hashCode() */
     @Override
     public final int hashCode() {
         int ret = 17;
 
         ret = ret * 39 + ctx.getURI().hashCode();
         ret = ret * 39 + (throwable == null ? 0 : throwable.hashCode());
         ret = ret * 39 + (httpResponse == null ? 0 : httpResponse.hashCode());
 
         return ret;
     }
 }
