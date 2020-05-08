package com.cs252.lab06;
 
 public class User
 {
     private String _username;
     private String _ipAddress;
     private String _status;
     
     public User(String username, String ipAddress)
     {
         _username = username;
         _ipAddress = ipAddress;
         _status = "Available";
     }
     
     public String getUsername()
     {
         return _username;
     }
     
     public String getIPAddress()
     {
         return _ipAddress;
     }
     
     public String getStatus()
     {
         return _status;
     }
     
     public void setStatus(String value)
     {
         _status = value;
     }
 }
 
