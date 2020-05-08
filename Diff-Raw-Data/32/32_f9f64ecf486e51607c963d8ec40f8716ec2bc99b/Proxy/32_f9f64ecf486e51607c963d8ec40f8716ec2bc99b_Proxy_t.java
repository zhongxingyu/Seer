 /*
  * ProxyTicketRequestor.java
  *
  * Created on Feb 9, 2007
  *
  * Copyright (C) 2006, 2007 Carl E Harris, Jr.
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  */
 package org.soulwing.cas.client;
 
 
 
 /**
  * A service interface that requests proxy tickets using a CAS proxy granting 
  * ticket.
  *
  * @author Carl Harris
  */
 public interface Proxy {
 
   /**
    * Invoke the CAS <code>/proxy</code> function to acquire a proxy ticket.
    * @param request proxy request
    * @return proxy response from the CAS server.
    */
  ProxyResponse proxy(ProxyRequest request);
 
 }
