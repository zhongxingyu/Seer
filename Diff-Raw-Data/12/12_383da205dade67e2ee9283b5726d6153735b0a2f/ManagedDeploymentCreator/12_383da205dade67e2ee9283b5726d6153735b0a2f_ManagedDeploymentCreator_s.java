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
 
 import java.util.Map;
 
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 import org.jboss.managed.api.ManagedDeployment;
 import org.jboss.managed.api.ManagedObject;
 
 /**
  * A plugin interface for building up a ManagedDeployment.
  * 
  * TODO: Generally an implementation would make use of registered
  * ManagedComponentCreator instances. Should the ManagedComponentCreator
  * registry be part of this interface?
  *  
  * @author Scott.Stark@jboss.org
  * @version $Revision$
  */
 public interface ManagedDeploymentCreator
 {
    /**
     * Build up the deployment management view.
     * @param unit - the deployment unit to process
    * @param md - the unit ManagedDeployment
     */
    public ManagedDeployment build(DeploymentUnit unit, Map<String, ManagedObject> unitMOs,
          ManagedDeployment parent);
 }
