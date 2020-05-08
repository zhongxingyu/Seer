 package com.example.plaid.pages;
 
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 public class Index
 {
 
     @Inject
     Messages messages;
 
     public String getText()
     {
         return messages.get("key-used-in-page-klass");
     }
 
 }
