 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.services.persistence.hibernate;
 
 import java.util.Map;
 
 import org.hibernate.Session;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import de.cosmocode.palava.bridge.Server;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.command.Job;
 import de.cosmocode.palava.bridge.command.Response;
 import de.cosmocode.palava.bridge.session.HttpSession;
 
 /**
  * Abstract base class for Hibernate related entity management.
  *
  * @author Willi Schoenborn
  */
 public abstract class HibernateJob implements Job {
     
     @Inject
     private Provider<Session> provider;
     
     @Override
     public final void process(Call request, Response response, HttpSession httpSession, Server server,
             Map<String, Object> caddy) throws Exception {
 
         final Session session = provider.get();
         process(request, response, httpSession, server, caddy, session);
         session.flush();
     }
     
     /**
      * Delegates the call to sub classes, including the Hibernate {@link Session}.
      * 
      * @param request the request
      * @param response the {@link Response}
      * @param httpSession the {@link HttpSession}
      * @param server the {@link Server}
      * @param caddy the caddy
      * @param session the {@link Session}
      * @throws Exception if processing failed
      */
     public abstract void process(Call request, Response response, HttpSession httpSession, Server server,
         Map<String, Object> caddy, Session session) throws Exception;
 
 }
