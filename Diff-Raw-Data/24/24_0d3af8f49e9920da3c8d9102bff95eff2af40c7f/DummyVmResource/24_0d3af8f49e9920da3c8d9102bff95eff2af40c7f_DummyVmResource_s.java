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
 package com.redhat.rhevm.api.dummy.resource;
 
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Actions;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.resource.VmResource;
 import com.redhat.rhevm.api.dummy.model.DummyVmStatus;
 import com.redhat.rhevm.api.dummy.model.DummyVM;
 
 public class DummyVmResource implements VmResource {
     /* FIXME: would like to do:
      * private @Context UriInfo uriInfo;
      */
 
     private DummyVM vm;
 
     /**
      * Package-protected ctor, never needs to be instantiated by JAX-RS framework.
      *
      * @param vm  encapsulated VM
      */
    DummyVmResource(DummyVM vm) {
         this.vm = vm;
     }
 
     /**
      * Package-level accessor for encapsulated VM
      *
      * @return  encapsulated VM
      */
    VM getVM() {
         return vm;
     }
 
    VM addLinks(UriBuilder uriBuilder) {
         vm.setLink(new Link("self", uriBuilder.build()));
         vm.setActions(new Actions(uriBuilder, VmResource.class));
         return new VM(vm);
     }
 
     /* FIXME: kill uriInfo param, make href auto-generated? */
     @Override
     public VM get(UriInfo uriInfo) {
         return addLinks(uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public VM update(UriInfo uriInfo, VM vm) {
         this.vm.update(vm);
         return addLinks(uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public void start() {
         vm.setStatus(DummyVmStatus.UP);
     }
 
     @Override
     public void stop() {
         vm.setStatus(DummyVmStatus.DOWN);
     }
 
     @Override
     public void shutdown() {
         vm.setStatus(DummyVmStatus.DOWN);
     }
 
     @Override
     public void suspend() {
     }
 
     @Override
     public void restore() {
     }
 
     @Override
     public void migrate() {
     }
 
     @Override
     public void move() {
     }
 
     @Override
     public void detach() {
     }
 
     @Override
     public void changeCD() {
     }
 
     @Override
     public void ejectCD() {
     }
 }
