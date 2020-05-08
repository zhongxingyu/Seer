 package com.wideplay.example.servlets;
 
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
 import static com.google.inject.name.Names.named;
 import static com.wideplay.example.servlets.ScopeDemoCountingServlet.*;
 import com.wideplay.warp.servlet.FlashScoped;
 import com.wideplay.warp.servlet.Servlets;
 import com.wideplay.warp.servlet.SessionScoped;
 import com.wideplay.warp.servlet.WarpServletContextListener;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhanji
  * Date: Dec 20, 2007
  * Time: 1:38:13 PM
  *
  * @author Dhanji R. Prasanna (dhanji gmail com)
  */
 public class MyGuiceCreator extends WarpServletContextListener {
 
     @Override
     protected Injector getInjector() {
         return Guice.createInjector(Servlets.configure()
                 .filters()
                     .filter("/*").through(RequestPrintingFilter.class)
 
                 .servlets()
                    .serve("/index.html").with(HelloWorldServlet.class)
                     .serve("/counter.html").with(ScopeDemoCountingServlet.class)
                     .serve("/scoped.html").with(ScopedServletWrappingServlet.class)
                     .serve("/params.html").with(RequestParameterServlet.class)
 
                 .buildModule(),
 
 
                 //bind other modules that our webapp needs
                 new AbstractModule() {
                     protected void configure() {
                         //example of binding scopes to scope annotations
                         bindScope(SessionScoped.class, Servlets.SESSION_SCOPE);
                         bindScope(FlashScoped.class, Servlets.FLASH_SCOPE);
                         //etc...
 
 
                        //Bug in guice forces me to explicitly bind Counter to itself if I want to use annotatedWith + custom scopes =(
                         bind(Counter.class).annotatedWith(named(REQUEST)).to(Counter.class).in(Servlets.REQUEST_SCOPE);
                         bind(Counter.class).annotatedWith(named(SESSION)).to(Counter.class).in(SessionScoped.class);
                         bind(Counter.class).annotatedWith(named(FLASH)).to(Counter.class).in(FlashScoped.class);
 
                     }
                 }
         );
     }
 }
