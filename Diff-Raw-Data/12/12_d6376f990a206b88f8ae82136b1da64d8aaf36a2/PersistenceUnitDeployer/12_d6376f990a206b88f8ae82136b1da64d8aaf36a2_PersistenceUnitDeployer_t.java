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
 package org.jboss.jpa.deployers;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.naming.InitialContext;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
 import org.jboss.jpa.deployment.PersistenceDeployment;
 import org.jboss.jpa.deployment.PersistenceUnitDeployment;
 import org.jboss.logging.Logger;
 import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;
 
 /**
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  * @version $Revision: $
  */
 public class PersistenceUnitDeployer extends AbstractSimpleRealDeployer<PersistenceUnitMetaData>
 {
    private static final Logger log = Logger.getLogger(PersistenceUnitDeployer.class);
    
    public PersistenceUnitDeployer()
    {
       super(PersistenceUnitMetaData.class);
       setComponentsOnly(true);
       addOutput(BeanMetaData.class);
    }
 
    @Override
    public void deploy(DeploymentUnit unit, PersistenceUnitMetaData metaData) throws DeploymentException
    {
       log.debug("deploy " + metaData);
       
       InitialContext initialContext = null;
       PersistenceDeployment persistenceDeployment = null;
       List<String> explicitEntityClasses = new ArrayList<String>();
       String ear = null;
       String jar = null;
       boolean isScoped = false;
       VFSDeploymentUnit deploymentUnit = (VFSDeploymentUnit) unit.getParent();
       PersistenceUnitDeployment pu = new PersistenceUnitDeployment(initialContext, persistenceDeployment, explicitEntityClasses, metaData, ear, jar, isScoped, deploymentUnit);
       
       String name = "ToDo";
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(name, PersistenceUnitDeployment.class.getName());
       builder.setConstructorValue(pu);
       
       unit.addAttachment(BeanMetaData.class, builder.getBeanMetaData());
    }
 }
