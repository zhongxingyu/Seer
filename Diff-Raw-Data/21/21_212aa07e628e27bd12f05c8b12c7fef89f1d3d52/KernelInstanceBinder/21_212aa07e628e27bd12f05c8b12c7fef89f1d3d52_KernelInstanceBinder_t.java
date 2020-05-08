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
 package org.jboss.guice.plugins;
 
 import java.util.Map;
 
 import com.google.inject.Binder;
 import com.google.inject.spi.SourceProviders;
 import org.jboss.beans.metadata.api.annotations.EntryValue;
 import org.jboss.beans.metadata.api.annotations.Inject;
 import org.jboss.beans.metadata.api.annotations.MapValue;
 import org.jboss.beans.metadata.api.annotations.StringValue;
 import org.jboss.beans.metadata.api.annotations.Value;
 import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
 
 /**
  * Kernel instance binder.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class KernelInstanceBinder extends InstanceBinder
 {
    static
    {
       SourceProviders.skip(KernelInstanceBinder.class);
    }
 
    public KernelInstanceBinder(Binder binder)
    {
       super(binder);
    }
 
    @MapValue(
          value = {
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.dependency.spi.Controller", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME))
                   ),
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.kernel.Kernel", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_NAME))
                   ),
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.kernel.spi.dependency.KernelController", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME))
                   ),
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.kernel.spi.registry.KernelBus", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_BUS_NAME))
                   ),
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.kernel.spi.registry.KernelRegistry", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_REGISTRY_NAME))
                   ),
                   @EntryValue(
                         key = @Value(type = Class.class, string = @StringValue(value="org.jboss.kernel.spi.config.KernelConfigurator", type = Class.class)),
                         value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONFIGURATOR_NAME))
                   )
                }
    )
   public void setBindings(Map<Class<?>, Object> bindings)
    {
       super.setBindings(bindings);
    }
 }
