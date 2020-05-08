 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.deployers.spi.deployer.managed;
 
 import java.util.Set;
 
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 import org.jboss.managed.api.ManagedDeployment;
 
 /**
  * A plugin interface for building up the ManagedComponents of
  * a ManagedDeployment. 
  * 
  * @author Scott.Stark@jboss.org
  * @version $Revision$
  */
 public interface ManagedComponentCreator<T>
 {
    /**
     * Callout to create the ManagedComponents that correspond to
     * the given metdata.
     * 
     * @param unit - the deployment unit to process
     * @param md - the unit ManagedDeployment
     * @param metaData - the unit metadata of type T for which the
     * ManagedComponents should be created.
     */
    public void build(DeploymentUnit unit, ManagedDeployment md,
          Set<? extends T> metaData);
 }
