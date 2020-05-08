 package org.minispm.security.utils;
 
 import org.apache.shiro.subject.Subject;
 import org.minispm.security.service.ShiroDbRealm;
 
 /**
  * User: shibaoxu
  * Date: 13-1-29
  * Time: 下午6:42
  */
 public class SecurityUtils {
     public static ShiroDbRealm.ShiroUser getCurrentShiroUser(){
         return  (ShiroDbRealm.ShiroUser ) org.apache.shiro.SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
 
     }
 
     public static Subject getCurrentSubject(){
         return org.apache.shiro.SecurityUtils.getSubject();
     }
 }
