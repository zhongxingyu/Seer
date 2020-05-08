 /*
  * Copyright © 2010 Red Hat, Inc.
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
package com.redhat.rhevm.api.powershell.deploy;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.core.Application;
 
 import com.redhat.rhevm.api.powershell.resource.PowerShellApiResource;
 import com.redhat.rhevm.api.powershell.resource.PowerShellHostsResource;
 import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource;
 
 public class PowerShellApplication extends Application {
 
     private Set<Object> singletons = new HashSet<Object>();
     private Set<Class<?>> classes = new HashSet<Class<?>>();
 
     public PowerShellApplication() {
         singletons.add(new PowerShellHostsResource());
         singletons.add(new PowerShellVmsResource());
         classes.add(PowerShellApiResource.class);
     }
 
     @Override
     public Set<Class<?>> getClasses() {
         return classes;
     }
 
     @Override
     public Set<Object> getSingletons() {
         return singletons;
     }
 }
