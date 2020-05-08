 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.security;
 
 import org.springframework.security.web.util.IpAddressMatcher;
 import org.web4thejob.context.ContextUtil;
 
 /**
  * @author Veniamin Isaias
  * @since 1.1.1
  */
 
 public class SecurityUtil {
     private static boolean firstUse = true;
 
     public static boolean hasIpAddress(String ipAddress, String remoteAddress) {
         return (new IpAddressMatcher(ipAddress).matches(remoteAddress));
     }
 
     public static boolean isFromIntranet(String remoteAddress) {
         return hasIpAddress("127.0.0.1", remoteAddress) || hasIpAddress("10.0.0.1/8",
                remoteAddress) || hasIpAddress("192.168.1.1/24", remoteAddress);
     }
 
     public static boolean isFirstUse() {
         if (firstUse) {
 
             try {
                 boolean installed = ContextUtil.getSystemJoblet().isInstalled();
                 boolean pchanged = ContextUtil.getSecurityService().authenticate(UserIdentity.USER_ADMIN,
                         UserIdentity.USER_ADMIN) == null;
                 firstUse = !(installed && pchanged);
             } catch (Exception e) {
                 //do nothing
             }
         }
         return firstUse;
     }
 
 }
