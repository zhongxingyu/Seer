 /*
  * The PID webservice offers SOAP methods to manage the Handle System(r) resolution technology.
  *
  * Copyright (C) 2010-2011, International Institute of Social History
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.socialhistoryservices.pid.controllers;
 
 import org.socialhistoryservices.pid.util.NamingAuthority;
 import org.socialhistoryservices.security.MongoTokenStore;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.oauth2.common.OAuth2AccessToken;
 import org.springframework.security.oauth2.provider.ClientDetails;
 import org.springframework.security.oauth2.provider.ClientDetailsService;
 import org.springframework.security.oauth2.provider.ClientToken;
 import org.springframework.security.oauth2.provider.OAuth2Authentication;
 import org.springframework.security.oauth2.provider.token.RandomValueTokenServices;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.HashSet;
 import java.util.List;
 
 @Controller
 public class KeysController {
 
     private MongoTokenStore mongoTokenStore;
     private RandomValueTokenServices tokenServices;
     private ClientDetailsService clientDetailsService;
     private static String clientId = "pid-webservice-client"; // Should be the same as in the Spring ClientProvider
 
     @RequestMapping("/admin/keys")
     public ModelAndView list(
             @RequestParam(value = "token", required = false) String refresh_token) {
 
         ModelAndView mav = new ModelAndView("keys");
         final SecurityContext context = SecurityContextHolder.getContext();
         Authentication authentication = context.getAuthentication();
         List<String> nas = NamingAuthority.getNaRole(authentication);
         if (refresh_token != null) {
             mongoTokenStore.removeAccessTokenUsingRefreshToken(refresh_token);
             mongoTokenStore.removeRefreshToken(refresh_token);
         }
         OAuth2AccessToken token = mongoTokenStore.selectKeys(authentication.getName());
         if (token == null) {
             final ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
             final ClientToken clientToken = new ClientToken(clientId, new HashSet<String>(clientDetails.getResourceIds()),
                     clientDetails.getClientSecret(), new HashSet<String>(clientDetails.getScope()),
                     clientDetails.getAuthorities());
             final OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientToken, authentication);
             token = tokenServices.createAccessToken(oAuth2Authentication);
            mongoTokenStore.storeAccessToken(token, oAuth2Authentication);
         }
         mav.addObject("token", token);
         mav.addObject("nas", nas);
         return mav;
     }
 
     public void setMongoTokenStore(MongoTokenStore mongoTokenStore) {
         this.mongoTokenStore = mongoTokenStore;
     }
 
 
     public void setTokenServices(RandomValueTokenServices tokenServices) {
         this.tokenServices = tokenServices;
     }
 
     public void setClientDetails(org.springframework.security.oauth2.provider.ClientDetailsService clientDetailsService) {
         this.clientDetailsService = clientDetailsService;
     }
 }
