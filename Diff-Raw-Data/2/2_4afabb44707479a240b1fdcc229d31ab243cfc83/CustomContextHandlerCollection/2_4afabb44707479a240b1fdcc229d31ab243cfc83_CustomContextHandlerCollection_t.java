 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2010, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.weld.shared.jetty6.context;
 
 import org.jboss.weld.shared.jetty6.WeldAppContext;
 import org.jboss.weld.shared.jetty6.session.SessionManagerProvider;
 
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.SessionManager;
 import org.mortbay.jetty.handler.ContextHandlerCollection;
 import org.mortbay.jetty.servlet.SessionHandler;
 
 /**
  * Custom context handler.
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class CustomContextHandlerCollection extends ContextHandlerCollection
 {
    private SessionManagerProvider sessionManagerProvider;
 
    public CustomContextHandlerCollection()
    {
       setContextClass(WeldAppContext.class);
    }
 
    public void addHandler(Handler handler)
    {
       super.addHandler(handler);
       if (handler instanceof WeldAppContext)
       {
          WeldAppContext wac = (WeldAppContext) handler;
          if (sessionManagerProvider != null)
          {
             SessionHandler sessionHandler = wac.getSessionHandler();
            String applicationId = wac.getWar();
             SessionManager manager = sessionManagerProvider.createSessionManager(applicationId);
             sessionHandler.setSessionManager(manager);
          }
       }
    }
 
    public void setSessionManagerProvider(SessionManagerProvider sessionManagerProvider)
    {
       this.sessionManagerProvider = sessionManagerProvider;
    }
 }
