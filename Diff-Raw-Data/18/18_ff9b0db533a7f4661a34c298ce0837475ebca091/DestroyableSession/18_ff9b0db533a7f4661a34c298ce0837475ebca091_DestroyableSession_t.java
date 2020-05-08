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
 
 package de.cosmocode.palava.jpa.hibernate;
 
 import org.hibernate.Session;
 
 import com.google.common.base.Preconditions;
 
import de.cosmocode.palava.core.scope.Destroyable;
 
 /**
  * {@link Destroyable} version of a Hibernate {@link Session}.
  *
  * @author Willi Schoenborn
  */
 final class DestroyableSession extends ForwardingSession implements Destroyable {
 
     private static final long serialVersionUID = -3229611777799970807L;
     
     private final Session session;
     
     public DestroyableSession(Session session) {
         this.session = Preconditions.checkNotNull(session, "Session");
     }
     
     @Override
     protected Session delegate() {
         return session;
     }
 
     @Override
     public void destroy() {
         if (session.isOpen()) session.close();
     }
 
 }
