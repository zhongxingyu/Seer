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
 package com.redhat.rhevm.api.common.resource;
 
 import java.util.UUID;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.resource.ActionResource;
 
 public class BaseActionResource implements ActionResource {
 
     private Action action;
 
     BaseActionResource(UriInfo uriInfo, Action action) {
         this.action = action;
         action.setId(UUID.randomUUID().toString());
         addLinks(uriInfo);
     }
 
     @Override
     public Response get(UriInfo uriInfo) {
         return Response.ok(action).build();
     }
 
     @Override
     public Action getAction() {
         return action;
     }
 
     private void addLinks(UriInfo uriInfo) {
         Link replay = new Link();
        replay.setRel("replay");
        replay.setHref(uriInfo.getRequestUri().toString());
         action.getLink().add(replay);

        Link self = new Link();
        self.setRel("self");
        self.setHref(uriInfo.getRequestUriBuilder().path(action.getId()).build().toString());
        action.getLink().add(self);
     }
 }
