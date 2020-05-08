 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
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
 
 package org.picketbox.cdi;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.spi.AfterBeanDiscovery;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.Extension;
 import javax.enterprise.inject.spi.ProcessAnnotatedType;
 import javax.enterprise.inject.spi.ProcessInjectionTarget;
 
 import org.picketlink.Identity;
 
 /**
  * <p>PicketBox CDI Extension for configuring the PicketBox security environment.</p>
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  *
  */
 public class PicketBoxExtension implements Extension {
 
     /**
      * <p>Adds the {@link PicketBoxManagerBeanDefinition}.</p>
      *
      * @param event
      * @param beanManager
      */
     public void addPicketBoxManagerBeanDefinition(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
         event.addBean(new PicketBoxManagerBeanDefinition(beanManager));
     }
 
     /**
      * <p>Vetos all {@link Identity} beans. Except the {@link PicketBoxIdentity}.<p>
      *
      * @param event
      * @param beanManager
      */
     public void installPicketBoxIdentity(@Observes ProcessAnnotatedType<Identity> event, BeanManager beanManager) {
         if (!event.getAnnotatedType().getJavaClass().equals(PicketBoxIdentity.class)) {
             event.veto();
         }
     }
 
     /**
      * <p>Vetos all {@link Identity} beans. Except the {@link PicketBoxIdentity}.<p>
      *
      * @param event
      * @param beanManager
      */
     public <X> void configureJPAIdentityStore(@Observes ProcessInjectionTarget<X> event) {
     }
 }
