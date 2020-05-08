 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.javaloader.test;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ManagedProperty;
 
 import javax.faces.event.ComponentSystemEvent;
 import javax.el.ELContext;
 
 
 @ManagedBean(name = "javatestbean")
 @RequestScoped
 public class TestBean2 {
 
     int cnt = 0;
     
     String sayHello = "<h2>hello world test</h2>";
     String hello2 = "hello from added attribute";
     String hello3 = "hello from  added attribute 2";
 
 
     @ManagedProperty(value = "#{javatestbean4xxx}")
     TestBean3 bean3;
 
     @ManagedProperty(value = "#{javatestbean4}")
     TestBean4 bean4;
 
     public void validate(ComponentSystemEvent e) {
         System.out.println("Validating");
     }
 
     public String getSayHello() {
      
 
         return bean4.getHello();
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
 
     public String doAction() {
         return null;
     }
 
     public String getResource() throws java.io.IOException {
         /*  ResourceHandler handler = FacesContext.getCurrentInstance().getApplication().getResourceHandler();
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
       return strBuf.toString(); */
         return "hello world";
     }
 
     public TestBean3 getBean3() {
         return bean3;
     }
 
     public void setBean3(TestBean3 bean3) {
         this.bean3 = bean3;
     }
 
     public TestBean4 getBean4() {
         return bean4;
     }
 
     public void setBean4(TestBean4 bean4) {
         this.bean4 = bean4;
     }
 }
