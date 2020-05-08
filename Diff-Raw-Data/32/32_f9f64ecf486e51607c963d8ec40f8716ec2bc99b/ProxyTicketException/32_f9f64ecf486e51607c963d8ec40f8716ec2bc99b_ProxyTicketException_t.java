 /*
  * ProxyTicketException.java
  *
  * Created on Feb 12, 2007
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
 package org.soulwing.cas.support;
 
import org.soulwing.cas.client.ProxyResponse;
 
 
 /**
  * An exception thrown when a request for a CAS proxy ticket fails.
  *
  * @author Carl Harris
  */
 public class ProxyTicketException extends RuntimeException {
 
   private static final long serialVersionUID = 6323178515814921510L;
 
  public ProxyTicketException(ProxyResponse response) {
     super(response.getResultMessage() + "(" + response.getResultCode() + ")");
   }
 
 }
