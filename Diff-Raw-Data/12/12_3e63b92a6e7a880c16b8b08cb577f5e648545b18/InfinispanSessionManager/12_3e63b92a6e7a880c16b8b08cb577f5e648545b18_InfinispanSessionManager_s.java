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
 
 package org.jboss.weld.shared.jetty6.session;
 
 import javax.servlet.http.HttpServletRequest;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.jboss.weld.shared.plugins.cache.CacheBuilder;
 import org.jboss.weld.shared.plugins.session.InfinispanSessionManagerAdapter;
 
 import org.infinispan.marshall.Externalizer;
 import org.mortbay.jetty.servlet.AbstractSessionManager;
 
 /**
  * Infinispan based session manager.
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class InfinispanSessionManager extends AbstractSessionManager
 {
    private static AtomicBoolean addExternalizer = new AtomicBoolean(true);
    private static Method idHack = InfinispanSessionManagerAdapter.getClusterId(Session.class);
 
    private InfinispanSessionManagerAdapter<Session> adapter;
 
    public InfinispanSessionManager(CacheBuilder cacheBuilder, String applicationId)
    {
       adapter = new Jetty6InfinispanSessionManagerAdapter(cacheBuilder, applicationId);
 
       if (addExternalizer.getAndSet(false))
          cacheBuilder.config().addExternalizer(new SessionExternalizer());
    }
 
    public void doStart() throws Exception
    {
       super.doStart();
       adapter.start();
    }
 
    public void doStop() throws Exception
    {
       adapter.stop();
       super.doStop();
    }
 
    public Map getSessionMap()
    {
       return adapter.getSessionMap();
    }
 
    public int getSessions()
    {
       return adapter.getSessions();
    }
 
    protected void addSession(Session session)
    {
       adapter.addSession(session);
    }
 
    public Session getSession(String idInCluster)
    {
       return adapter.getSession(idInCluster);
    }
 
    protected void invalidateSessions()
    {
       adapter.invalidateSessions();
    }
 
    protected Session newSession(HttpServletRequest request)
    {
       return new InfinispanSession(request);
    }
 
    protected void removeSession(String idInCluster)
    {
       adapter.removeSession(idInCluster);
    }
 
    class InfinispanSession extends Session
    {
       private InfinispanSession(HttpServletRequest request)
       {
          super(request);
       }
 
       private InfinispanSession(long created, String clusterId, long cookieSet, long lastAccessed)
       {
          super(created, clusterId);
          _cookieSet = cookieSet;
          _lastAccessed = lastAccessed;
       }
 
       protected Map newAttributeMap()
       {
          return adapter.newAttributeMap(this);
       }
 
       private void doWriteObject(ObjectOutput out) throws IOException
       {
          out.writeUTF(_clusterId);
          out.writeUTF(_nodeId);
          out.writeBoolean(_idChanged);
          out.writeLong( _created);
          out.writeLong(_cookieSet);
          out.writeLong(_accessed);
          out.writeLong(_lastAccessed);
          out.writeInt(_requests);
       }
    }
 
    private class SessionExternalizer implements Externalizer<InfinispanSession>
    {
       private Set<Class<? extends InfinispanSession>> classes = new HashSet<Class<? extends InfinispanSession>>(1);
 
       private SessionExternalizer()
       {
          classes.add(InfinispanSession.class);
       }
 
       public void writeObject(ObjectOutput out, InfinispanSession object) throws IOException
       {
          object.doWriteObject(out);
       }
 
       @SuppressWarnings({"UnusedDeclaration"})
       public InfinispanSession readObject(ObjectInput in) throws IOException, ClassNotFoundException
       {
          String clusterId = in.readUTF();
          String nodeId = in.readUTF();
          boolean idChanged = in.readBoolean();
          long created = in.readLong();
          long cookieSet = in.readLong();
          long accessed = in.readLong();
          long lastAccessed = in.readLong();
          int requests = in.readInt();
          return new InfinispanSession(created, clusterId, cookieSet, lastAccessed);
       }
 
       public Set<Class<? extends InfinispanSession>> getTypeClasses()
       {
          return classes;
       }
 
       public Integer getId()
       {
         return null;  // null OK?
       }
    }
 
    private class Jetty6InfinispanSessionManagerAdapter extends InfinispanSessionManagerAdapter<Session>
    {
       private Jetty6InfinispanSessionManagerAdapter(CacheBuilder cacheBuilder, String region)
       {
          super(cacheBuilder, region);
       }
 
       protected String getId(Session session)
       {
          try
          {
             if (idHack != null)
                return (String) idHack.invoke(session);
          }
          catch (Throwable ignored)
          {
          }
          return session.getId();
       }
 
       protected void invalidate(Session session)
       {
          session.invalidate();
       }
    }
 }
