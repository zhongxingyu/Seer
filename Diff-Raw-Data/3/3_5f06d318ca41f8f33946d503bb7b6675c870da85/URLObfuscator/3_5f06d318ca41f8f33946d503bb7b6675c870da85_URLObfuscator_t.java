 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008-2009 CEJUG - Ceará Java Users Group
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.login.security;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 
 import javax.ejb.Local;
 
 /**
  * The confirmation of a registration is done through an URL encrypted to ensure
  * a minimum security level.
  * 
  * @author $Author: felipegaucho $
  * @version $Rev$ ($Date: 2009-02-14 12:28:21 +0100 (Sat, 14 Feb 2009) $)
  */
 @Local
 public interface URLObfuscator {
 	/**
 	 * Convert a response URL in a plain text.
 	 * 
 	 * @param baseUrl
 	 * 
 	 * @return a plain confirmation URL.
 	 * @throws MalformedURLException
 	 * @throws GeneralSecurityException
 	 * @throws UnsupportedEncodingException
 	 */
 	URL createObfuscatedUrl(String login, String email, String baseUrl)
 			throws MalformedURLException, GeneralSecurityException,
 			UnsupportedEncodingException;
 }
