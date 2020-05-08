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
 package ar.com.zauber.leviathan.api;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Map;
 
 import ar.com.zauber.leviathan.api.URIFetcherResponse.URIAndCtx;
 
 /**
  * URI fetcher
  *
  * @author Juan F. Codagnone
  * @since Oct 12, 2009
  */
 public interface URIFetcher {
 
     /**
      * fetch uri
      * 
      * @deprecated Use {@link #get(URI)} instead. This method will be deleted on
      *             next version.
      */
     @Deprecated
     URIFetcherResponse fetch(URI uri);
 
     /**
      * fetch uri
      * 
      * @deprecated Use {@link #get(URIAndCtx)} instead. This method will be
      *             deleted on next version.
      */
     @Deprecated
     URIFetcherResponse fetch(URIAndCtx uri);
     
     /** get from uri */
     URIFetcherResponse get(URI uri);
 
     /** get from uri */
     URIFetcherResponse get(URIAndCtx uri);    
     
     /** post to uri */
     URIFetcherResponse post(URIAndCtx uri, InputStream body);
     
    /** post to uri */
     URIFetcherResponse post(URIAndCtx uri, Map<String, String> body);
     
 }
