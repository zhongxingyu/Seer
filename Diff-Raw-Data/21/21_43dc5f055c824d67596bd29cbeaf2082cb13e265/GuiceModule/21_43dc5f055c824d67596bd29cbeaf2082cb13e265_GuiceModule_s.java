 /*
  * The MIT License
  *
  * Copyright (c) 2013, Robert Sandell - sandell.robert@gmail.com. All rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.joinedminds.tools.evet;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.name.Names;
 
 import javax.servlet.ServletContext;
 
 /**
  * Description
  *
  * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
  */
 public class GuiceModule extends AbstractModule {
 
     private ServletContext context;
     private String dbHost;
     private Integer dbPort;
     private String dbName;
     private String dbUser;
     private String dbPasswd;
 
     public GuiceModule(ServletContext context, String dbHost, Integer dbPort, String dbName, String dbUser, String dbPasswd) {
         this.context = context;
         this.dbHost = dbHost;
         this.dbPort = dbPort;
         this.dbName = dbName;
         this.dbUser = dbUser;
         this.dbPasswd = dbPasswd;
     }
 
     @Override
     protected void configure() {
         bind(ServletContext.class).toInstance(context);
         bind(String.class).annotatedWith(Names.named("DB_HOST")).toInstance(dbHost);
        bind(Integer.class).annotatedWith(Names.named("DB_PORT")).toInstance(dbPort);
         bind(String.class).annotatedWith(Names.named("DB_NAME")).toInstance(dbName);
         bind(String.class).annotatedWith(Names.named("DB_USER")).toInstance(dbUser);
         bind(String.class).annotatedWith(Names.named("DB_PASSWD")).toInstance(dbPasswd);
 
         bind(Db.class);
         bind(Evet.class);
     }
 }
