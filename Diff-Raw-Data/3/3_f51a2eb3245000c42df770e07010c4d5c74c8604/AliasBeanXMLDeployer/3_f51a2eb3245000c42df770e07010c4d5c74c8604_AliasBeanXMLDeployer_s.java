 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.test.spring.test;
 
 import java.util.Set;
 
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.spi.deployment.KernelDeployment;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
 import org.jboss.beans.metadata.spi.NamedAliasMetaData;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class AliasBeanXMLDeployer extends BasicXMLDeployer
 {
    public AliasBeanXMLDeployer(Kernel kernel)
    {
       super(kernel);
    }
 
    protected void deployBeans(KernelController controller, KernelDeployment deployment) throws Throwable
    {
       super.deployBeans(controller, deployment);
       Set<NamedAliasMetaData> aliases = deployment.getAliases();
       if (aliases != null && aliases.isEmpty() == false)
       {
          for (NamedAliasMetaData alias : aliases)
             controller.addAlias(alias.getAliasValue(), alias.getName());
       }
    }
 
    protected void undeployBeans(KernelController controller, KernelDeployment deployment)
    {
      super.undeployBeans(controller, deployment);
       Set<NamedAliasMetaData> aliases = deployment.getAliases();
       if (aliases != null && aliases.isEmpty() == false)
       {
          for (NamedAliasMetaData alias : aliases)
             controller.removeAlias(alias.getAliasValue());
       }
    }
 }
