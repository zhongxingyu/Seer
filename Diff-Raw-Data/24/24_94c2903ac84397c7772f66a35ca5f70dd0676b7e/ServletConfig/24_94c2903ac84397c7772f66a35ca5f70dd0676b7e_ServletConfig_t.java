 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 
 public class ServletConfig extends GuiceServletContextListener {
 
     @Override
     protected Injector getInjector() {
         return Guice.createInjector(new MyApplicationModule());
     }
 }
