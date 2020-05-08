 /*
  * jGuard is a security framework based on top of jaas (java authentication and authorization security).
  * it is written for web applications, to resolve simply, access control problems.
  * version $Name$
  * http://sourceforge.net/projects/jguard/
  *
  * Copyright (C) 2004-2010  Charles GAY
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  * jGuard project home page:
  * http://sourceforge.net/projects/jguard/
  */
 
 package net.sf.jguard.jee.jsf;
 
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.servlet.ServletModule;
 import net.sf.jguard.jee.DummyServlet;
 import net.sf.jguard.jee.listeners.ContextListener;
 import net.sf.jguard.jsf.JSFModule;
 
 /**
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Gay</a>
  */
 public class DummyJSFContextListener extends ContextListener {
 
 
     @Override
     public ServletModule getServletModule() {
 
         return new ServletModule() {
             @Override
             protected void configureServlets() {
                serve("/*").by(DummyServlet.class);
             }
         };
     }
 
     @Override
     public Module getTechnologySpecificModule() {
         return new JSFModule();
     }
 
     public Injector getBuiltInjector() {
         return injector;
     }
 }
