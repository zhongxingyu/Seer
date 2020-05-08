 package org.ryu22e.nico2cal.controller;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 /**
  * @author ryu22e
  *
  */
 public final class IndexController extends Controller {
 
     /*
      * (non-Javadoc) {@inheritDoc}
      */
     @Override
     protected Navigation run() throws Exception {
         return forward("Index.jsp");
     }
 
 }
