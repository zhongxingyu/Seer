 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INFSO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.registration.guards;
 
 import javax.naming.directory.Attributes;
 
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.security.User;
 import org.restlet.security.Verifier;
 
 import eu.stratuslab.registration.cfg.AppConfiguration;
 import eu.stratuslab.registration.cfg.Parameter;
 import eu.stratuslab.registration.data.UserEntry;
 import eu.stratuslab.registration.utils.HashUtils;
 import eu.stratuslab.registration.utils.LdapConfig;
 import eu.stratuslab.registration.utils.RequestUtils;
 
 public class LdapVerifier implements Verifier {
 
     public int verify(Request request, Response response) {
 
        // Make sure the necesssary information is available. If not, this will
         // cause it to be requested.
         if (request.getChallengeResponse() == null) {
             return RESULT_MISSING;
         }
 
         // Get the identifier and secret from the request.
         String identifier = getIdentifier(request);
         char[] secret = getSecret(request);
 
         // Blank passwords are not allowed.
         if (secret.length == 0) {
             return RESULT_INVALID;
         }
 
         if (isLdapPasswordCorrect(identifier, secret, request)) {
             setUser(identifier, request);
             return RESULT_VALID;
         } else {
             return RESULT_INVALID;
         }
 
     }
 
     private static String getIdentifier(Request request) {
         return request.getChallengeResponse().getIdentifier();
     }
 
     private static char[] getSecret(Request request) {
         return request.getChallengeResponse().getSecret();
     }
 
     private static void setUser(String identifier, Request request) {
         User user = new User(identifier);
         request.getClientInfo().setUser(user);
     }
 
     private static boolean isLdapPasswordCorrect(String identifier,
             char[] secret, Request request) {
 
         String sshaHashedPassword = extractHashedPassword(identifier, request);
         String plainTextPassword = new String(secret);
         return HashUtils.comparePassword(plainTextPassword, sshaHashedPassword);
     }
 
     private static String extractHashedPassword(String identifier,
             Request request) {
 
         AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);
         LdapConfig ldapEnv = cfg.getLdapConfig(Parameter.LDAP_USER_BASE_DN);
         Attributes attrs = UserEntry.getUserAttributes(identifier, ldapEnv);
         return UserEntry.extractPassword(attrs);
     }
 
 }
