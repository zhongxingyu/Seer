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
 package com.redhat.rhevm.api.resource;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.PUT;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 /* FIXME: doesn't seem to do anything ? Also, we could do without
  *        the explicit dependency on RESTeasy
  */
 import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.Host;
 import com.redhat.rhevm.api.model.Hosts;
 
 @Path("/hosts")
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
 @Formatted
 public interface HostResource
 {
 	/* FIXME: can we make uriInfo a field instead of a parameter to
 	 *        each method? Adding @Context to the implementation
 	 *        class doesn't seem to work.
 	 */
 
 	@GET
 	@Path("{id}")
 	public Host get(@Context UriInfo uriInfo, @PathParam("id") String id);
 
 	@GET
 	public Hosts list(@Context UriInfo uriInfo);
 
 	/* FIXME: need to move this to e.g. a top-level /search
 	 * @GET
 	 * public Hosts search(String criteria);
 	 */
 
 	/**
 	 * Creates a new host and adds it to the database. The host is
 	 * created based on the properties of @host.
 	 * <p>
 	 * The Host#name, Host#address and Host#rootPassword properties
 	 * are required.
 	 *
 	 * @param host  the host definition from which to create the new
 	 *              host
 	 * @return      the new newly created Host
 	 */
 	@POST
 	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
 	public Response add(@Context UriInfo uriInfo, Host host);
 
 	/**
 	 * Modifies an existing host's properties in the database.
 	 * <p>
 	 * Only the Host#name property can be modified.
 	 *
 	 * @param host  the host definition with the modified properties
 	 * @return      the updated Host
 	 */
 	@PUT
 	@Path("{id}")
 	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_X_YAML, MediaType.APPLICATION_JSON})
 	public Host update(@Context UriInfo uriInfo, @PathParam("id") String id, Host host);
 
 	@DELETE
 	@Path("{id}")
 	public void remove(@PathParam("id") String id);
 
 	@POST
 	@Action
 	@Path("{id}")
 	public void approve(@PathParam("id") String id);
 
 	@POST
 	@Action
 	@Path("{id}")
 	public void fence(@PathParam("id") String id);
 
 	@POST
 	@Action
 	@Path("{id}")
 	public void resume(@PathParam("id") String id);
 
 	//@WebMethod public void connectStorage(String id, String storageDevice);
 }
