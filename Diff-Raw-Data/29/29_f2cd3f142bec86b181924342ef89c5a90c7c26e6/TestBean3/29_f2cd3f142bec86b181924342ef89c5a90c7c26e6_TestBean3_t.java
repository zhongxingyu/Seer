 package org.apache.myfaces.javaloader.test;
 
 import org.apache.myfaces.scripting.loaders.java.ScriptingClass;
 import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.Resource;
 import javax.faces.context.FacesContext;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.nio.CharBuffer;
 
 
 public class TestBean3 {
     String sayHello = "hello world";
     String hello2 = "hello from added attribute";
     String hello3 = "hello from  added attribute 2";
 
 
     
 
     
     private String myText() {
        return "Hello World from bean3 ";
     }
 
     public String getSayHello() {
         return myText();
     }
 
     public String getSayHello2() {
         return hello2;
     }
 
 
     public void setSayHello(String hello) {
         this.sayHello = hello;
        System.out.println("hello world");
     }
 
     public void setResource(String param) {
 
     }
     
 
     public String getResource() {
      /*   ResourceHandler handler = FacesContext.getCurrentInstance().getApplication().getResourceHandler();
         Resource resource = handler.createResource("testResource");
         InputStream istr = resource.getInputStream();
         BufferedReader rdr = new BufferedReader(new InputStreamReader(istr));
         StringBuilder strBuf = new StringBuilder();
         String line = null;
         try {
             while ((line = rdr.readLine()) != null) {
                 strBuf.append(line);
             }
         } catch (IOException ex) {
 
         };
         return strBuf.toString();
         */
         return "hello world";
     }
 
 }
