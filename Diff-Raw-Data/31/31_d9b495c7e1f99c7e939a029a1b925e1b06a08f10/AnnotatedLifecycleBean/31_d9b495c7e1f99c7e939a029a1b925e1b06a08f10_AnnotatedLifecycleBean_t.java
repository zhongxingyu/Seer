 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.test.kernel.deployment.support;
 
import org.jboss.beans.metadata.spi.annotations.CreateLifecycle;
import org.jboss.beans.metadata.spi.annotations.DestroyLifecycle;
import org.jboss.beans.metadata.spi.annotations.StopLifecycle;
import org.jboss.beans.metadata.spi.annotations.StartLifecycle;

 /**
  * @author <a href="mailto:ales.justin@gmail.com">Ales Justin</a>
  */
 public class AnnotatedLifecycleBean
 {
 
    private boolean m_create;
    private boolean m_start;
    private boolean m_stop;
    private boolean m_destroy;
 
    public boolean isCreate()
    {
       return m_create;
    }
 
    public boolean isStart()
    {
       return m_start;
    }
 
    public boolean isStop()
    {
       return m_stop;
    }
 
    public boolean isDestroy()
    {
       return m_destroy;
    }
 
   @CreateLifecycle
    public void annotatedCreate()
    {
       m_create = true;
    }
 
   @StartLifecycle
    public void annotatedStart()
    {
       m_start = true;
    }
 
   @StopLifecycle
    public void annotatedStop()
    {
       m_start = false;
       m_stop = true;
    }
 
   @DestroyLifecycle
    public void annotatedDestroy()
    {
       m_create = false;
       m_destroy = true;
    }
 
 }
