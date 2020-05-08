 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.aerogear.prodoctor.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 
 import org.aerogear.prodoctor.model.Lead;
 import org.aerogear.prodoctor.model.SaleAgent;
 import org.aerogear.prodoctor.service.LeadSender;
 import org.aerogear.prodoctor.utility.SaleAgentCriteria;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.model.IdentityType;
 import org.picketlink.idm.model.User;
 import org.picketlink.idm.query.IdentityQuery;
 
 /**
  * 
  */
 @Stateless
 @Path("/saleagents")
 public class SaleAgentEndpoint {
 
 	@Inject
 	private IdentityManager identityManager;
 
 	@PersistenceContext(unitName = "picketlink-default")
 	private EntityManager em;
 
 	@POST
 	@Consumes("application/json")
 	public Response create(SaleAgent entity) {
 		em.persist(entity);
 		return Response.created(
 				UriBuilder.fromResource(SaleAgentEndpoint.class)
 						.path(String.valueOf(entity.getId())).build()).build();
 	}
 
 	@DELETE
 	@Path("/{id:[0-9][0-9]*}")
 	public Response deleteById(@PathParam("id") Long id) {
 		SaleAgent entity = em.find(SaleAgent.class, id);
 		if (entity == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		em.remove(entity);
 		return Response.noContent().build();
 	}
 
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	@Produces("application/json")
 	public Response findById(@PathParam("id") Long id) {
 		TypedQuery<SaleAgent> findByIdQuery = em.createQuery(
 				"SELECT s FROM SaleAgent s WHERE s.id = :entityId",
 				SaleAgent.class);
 		findByIdQuery.setParameter("entityId", id);
 		SaleAgent entity = findByIdQuery.getSingleResult();
 		if (entity == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		return Response.ok(entity).build();
 	}
 
 	@GET
 	@Produces("application/json")
 	public List<SaleAgent> listAll() {
 		final List<SaleAgent> results = em.createQuery(
 				"SELECT s FROM SaleAgent s", SaleAgent.class).getResultList();
 		return results;
 	}
 
 	@PUT
 	@Path("/{id:[0-9][0-9]*}")
 	@Consumes("application/json")
 	public Response update(@PathParam("id") Long id, SaleAgent entity) {
 		entity.setId(id);
 		entity = em.merge(entity);
 		return Response.noContent().build();
 	}
 
 	public List<SaleAgent> listByCriteria(String status, String location) {
 
 		IdentityQuery<User> query = identityManager
 				.createIdentityQuery(User.class);
 
 		if (!status.isEmpty()) {
 			query.setParameter(IdentityType.ATTRIBUTE.byName("status"),
 					new Object[] { status });
 		}
 		if (!location.isEmpty()) {
 			query.setParameter(IdentityType.ATTRIBUTE.byName("location"),
 					new Object[] { location });
 		}
 		List<User> users = query.getResultList();
		//System.out.println("SIZE " + users.get(0).C);
 		List<SaleAgent> saleAgents = new ArrayList<SaleAgent>();
 		for (User user : users) {
 			SaleAgent agent = new SaleAgent();
 			agent.setLoginName(user.getLoginName());
 			agent.setAttribute(user.getAttribute("location"));
 			agent.setAttribute(user.getAttribute("status"));
 			saleAgents.add(agent);
 		}
 		return saleAgents;
 
 	}
 
 	
 }
