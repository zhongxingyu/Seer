 /**
  * Copyright (C) 2010 Ian C. Smith <m4r35n357@gmail.com>
  *
  * This file is part of JavaEE6Webapp.
  *
  *     JavaEE6Webapp is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     JavaEE6Webapp is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with JavaEE6Webapp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.me.doitto.webapp.ws;
 
 import java.net.URI;
 
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import uk.me.doitto.webapp.beans.AppUserService;
 import uk.me.doitto.webapp.dao.Crud;
 import uk.me.doitto.webapp.entity.AppUser;
 
 /**
  *
  * @author ian
  */
 @Path(AppUserRest.PATH)
 @Stateless
 @LocalBean
 @TransactionAttribute(TransactionAttributeType.NEVER)
 public class AppUserRest extends RestCrudBase<AppUser> {
 
 	private static final long serialVersionUID = 1L;
 
     public static final String PATH = "/appuser";
     
     @EJB
     private AppUserService appUserService;
     
     @Context
     private UriInfo uriInfo;
 
 	@Override
 	protected AppUser overlay (final AppUser incoming, final AppUser existing) {
 		assert incoming != null;
 		assert existing != null;
     	if (incoming.getName() != null) {
     		existing.setName(incoming.getName());
     	}
     	if (incoming.getRealName() != null) {
     		existing.setRealName(incoming.getRealName());
     	}
     	if (incoming.getComments() != null) {
     		existing.setComments(incoming.getComments());
     	}
 		return existing;
 	}
 
     @POST
 	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Override
     public Response create (final AppUser appUser) {
 		assert appUser != null;
     	AppUser combined = overlay(appUser, new AppUser());
     	appUserService.create(combined);
     	assert !combined.isNew();
         URI uri = uriInfo.getAbsolutePathBuilder().path(combined.getId().toString()).build();
         return Response.created(uri).entity(combined).build();
     }
 
 	@Override
 	protected Crud<AppUser> getService() {
 		return appUserService;
 	}
 }
