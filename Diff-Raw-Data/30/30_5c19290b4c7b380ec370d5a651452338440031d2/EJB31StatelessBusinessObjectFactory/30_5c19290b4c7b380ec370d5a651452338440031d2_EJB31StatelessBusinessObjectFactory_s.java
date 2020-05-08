 /*
  * JBoss, Home of Professional Open Source
  * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @authors tag. See the copyright.txt in the
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
 package org.jboss.ejb3.heks.businessobject;
 
 import org.jboss.ejb3.core.businessobject.BusinessObjectFactory;
 import org.jboss.ejb3.session.SessionContainer;
 import org.jboss.ejb3.util.CollectionHelper;
 import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
 import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
 
 import javax.naming.NamingException;
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Create a BusinessObjectFactory that allows no-interfaces to work.
  * 
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class EJB31StatelessBusinessObjectFactory implements BusinessObjectFactory
 {
    @Override
    public <B> B createBusinessObject(SessionContainer container, Serializable sessionId, Class<B> businessInterface)
    {
       // a copy out of LegacyStatelessBusinessObjectFactory with an addition for no-interface
 
       assert sessionId == null : "sessionId must be null for Stateless";
       assert businessInterface != null : "businessInterface is null";
 
       try
       {
          /*
           * Get all business interfaces
           */
          Set<String> businessInterfaceNames = new HashSet<String>();
          JBossSessionBeanMetaData smd= (JBossSessionBeanMetaData) container.getXml();
          CollectionHelper.addAllIfSet(businessInterfaceNames, smd.getBusinessRemotes());
          CollectionHelper.addAllIfSet(businessInterfaceNames, smd.getBusinessLocals());
 
          // do no-interface addition
          if(smd instanceof JBossSessionBean31MetaData)
          {
            if(((JBossSessionBean31MetaData) smd).isNoInterfaceBean())
               businessInterfaceNames.add(container.getBeanClassName());
          }
 
          String interfaceName = businessInterface.getName();
 
          if (!businessInterfaceNames.contains(interfaceName))
             throw new IllegalStateException("Cannot find BusinessObject for interface: " + interfaceName);
 
          String jndiName = container.getXml().determineResolvedJndiName(interfaceName);
          return businessInterface.cast(container.getInitialContext().lookup(jndiName));
       }
       catch (NamingException e)
       {
          throw new RuntimeException("failed to invoke getBusinessObject", e);
       }
 
    }
 }
