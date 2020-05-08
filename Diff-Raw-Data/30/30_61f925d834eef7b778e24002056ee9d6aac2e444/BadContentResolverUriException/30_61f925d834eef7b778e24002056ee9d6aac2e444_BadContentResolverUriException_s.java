 /**
  * Copyright (C) 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  *
  * These coded instructions, statements, and computer programs contain
 * proprietary information of Linccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Android-API".
  * 
  * Linccer Android-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Android-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Android-API. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.hoccer.api.android;
 
 public class BadContentResolverUriException extends Exception {
 
     private static final long serialVersionUID = 1L;
 
     public BadContentResolverUriException() {
         super();
     }
 
     public BadContentResolverUriException(String detailMessage, Throwable throwable) {
         super(detailMessage, throwable);
     }
 
     public BadContentResolverUriException(String detailMessage) {
         super(detailMessage);
     }
 
     public BadContentResolverUriException(Throwable throwable) {
         super(throwable);
     }
 
 }
