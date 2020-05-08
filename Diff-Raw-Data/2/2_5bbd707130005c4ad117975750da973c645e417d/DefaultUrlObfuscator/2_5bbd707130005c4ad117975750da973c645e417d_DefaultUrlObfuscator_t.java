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
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.ejb.Stateless;
 
 import net.java.dev.cejug.classifieds.login.entity.facade.client.RegistrationConstants;
 import net.java.dev.cejug.classifieds.login.entity.facade.client.URLDeobfuscator;
 
 /**
  * Default URL obfuscator applies a a simple shift plus the MD5 cypher o the
  * plain information.
  * 
  * @author $Author: felipegaucho $
  * @version $Rev$ ($Date: 2009-02-14 12:28:21 +0100 (Sat, 14 Feb 2009) $)
  */
 @Stateless
 public class DefaultUrlObfuscator implements URLObfuscator, URLDeobfuscator {
 	private static final String URL_PARAMS_LOGIN = "&login=";
 	private static final String URL_PARAMS_EMAIL = "email=";
 	private static final String URL_PARAMS_SEPARATOR = "?&=";
 	private transient final DESedeStringEncrypter ENCRYPTER;
	private transient final static String ENCRYPTION_KEY = "todo_to_think_about_this_key_not_hard_code";
 
 	public DefaultUrlObfuscator() throws GeneralSecurityException {
 		ENCRYPTER = new DESedeStringEncrypter(ENCRYPTION_KEY);
 	}
 
 	@Override
 	public URL createObfuscatedUrl(String login, String email, String baseUrl)
 			throws MalformedURLException, GeneralSecurityException,
 			UnsupportedEncodingException {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(URL_PARAMS_EMAIL);
 		buffer.append(email);
 		buffer.append(URL_PARAMS_LOGIN);
 		buffer.append(login);
 		return new URL(baseUrl + ENCRYPTER.encrypt(buffer.toString()));
 	}
 
 	@Override
 	public Map<String, String> extractParameters(String obfuscated)
 			throws GeneralSecurityException, IOException {
 		Map<String, String> parameters = new HashMap<String, String>();
 		String plain = ENCRYPTER.decrypt(obfuscated);
 		StringTokenizer parametersTokenizer = new StringTokenizer(plain,
 				URL_PARAMS_SEPARATOR, false);
 		while (parametersTokenizer.hasMoreTokens()) {
 			String token = parametersTokenizer.nextToken();
 			if (token.equals(RegistrationConstants.EMAIL.value())
 					&& parametersTokenizer.hasMoreTokens()) {
 				parameters.put(RegistrationConstants.EMAIL.value(),
 						parametersTokenizer.nextToken());
 			} else if (token.equals(RegistrationConstants.LOGIN.value())
 					&& parametersTokenizer.hasMoreTokens()) {
 				parameters.put(RegistrationConstants.LOGIN.value(),
 						parametersTokenizer.nextToken());
 			}
 		}
 		return parameters;
 	}
 }
