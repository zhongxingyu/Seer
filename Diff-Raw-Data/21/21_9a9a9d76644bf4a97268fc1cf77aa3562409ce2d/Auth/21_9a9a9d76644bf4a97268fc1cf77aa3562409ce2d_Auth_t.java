 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 import redstone.xmlrpc.XmlRpcFault;
 
 /**
  *
  * @author asaleh
  */
 public class Auth {
 
     public static class login extends TcmsHashCommand {
 
         @RequiredField
         public String username;
         @RequiredField
         public String password;
 
         public login() {
         }
 
         public login(String username, String password) {
             this.username = username;
             this.password = password;
         }
 
         public String invoke(TcmsConnection c) throws XmlRpcFault {
             return (String) c.invoke(this);
         }
     }
 
     public static class login_krbv extends TcmsArrayCommand {
 
         public String invoke(TcmsConnection c) throws XmlRpcFault {
             return (String) c.invoke(this);
         }
     }
 
     public static class logout extends TcmsArrayCommand {
     }
 
     public static class get extends TcmsArrayCommand {
 
         @RequiredField
         public Integer id;
     };
 }
