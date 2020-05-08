 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.jpa.resolvers;
 
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 
 /**
  * Based on the available meta data resolve persistence unit references.
  * 
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  * @version $Revision: $
  */
 public interface PersistenceUnitDependencyResolver
 {
    /**
     * Create a bean name for a persistence unit (deployment).
     * 
     * @param deploymentUnit the deployment unit in which the persistence unit is defined
     * @param persistenceUnitName the name of the persistence unit
     * @return the bean name
     */
    String createBeanName(DeploymentUnit deploymentUnit, String persistenceUnitName);
    
    /**
     * @param deploymentUnit the deployment unit that has a persistence unit reference
     * @param persistenceUnitName the (relative) name of a persistence unit
     * @return the bean name of the persistence unit
     */
   String resolvePersistenceUnitSupplier(DeploymentUnit deploymentUnit, String persistenceUnitName);
 }
