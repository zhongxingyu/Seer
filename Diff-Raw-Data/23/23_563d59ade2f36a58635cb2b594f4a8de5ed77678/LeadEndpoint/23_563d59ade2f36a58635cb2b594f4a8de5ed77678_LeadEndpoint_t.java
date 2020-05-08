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
import java.util.LinkedHashMap;
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
 import org.aerogear.prodoctor.service.LeadSender;
import org.picketlink.idm.model.User;
 
 /**
  * 
  */
 @Stateless
 @Path("/leads")
 public class LeadEndpoint {
 	@Inject
 	private LeadSender leadSender;
 
 	@PersistenceContext(unitName = "forge-default")
 	private EntityManager em;
 
 	@POST
 	@Consumes("application/json")
 	public Response create(Lead entity) {
 		em.persist(entity);
 		return Response.created(
 				UriBuilder.fromResource(LeadEndpoint.class)
 						.path(String.valueOf(entity.getId())).build()).build();
 	}
 
 	@DELETE
 	@Path("/{id:[0-9][0-9]*}")
 	public Response deleteById(@PathParam("id") Long id) {
 		Lead entity = em.find(Lead.class, id);
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
 		TypedQuery<Lead> findByIdQuery = em
 				.createQuery(
 						"SELECT l FROM Lead l LEFT JOIN FETCH l.saleAgent WHERE l.id = :entityId",
 						Lead.class);
 		findByIdQuery.setParameter("entityId", id);
 		Lead entity = findByIdQuery.getSingleResult();
 		if (entity == null) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		return Response.ok(entity).build();
 	}
 
 	@GET
 	@Produces("application/json")
 	public List<Lead> listAll() {
 		final List<Lead> results = em.createQuery(
 				"SELECT l FROM Lead l LEFT JOIN FETCH l.saleAgent", Lead.class)
 				.getResultList();
 		return results;
 	}
 
 	@PUT
 	@Path("/{id:[0-9][0-9]*}")
 	@Consumes("application/json")
 	public Response update(@PathParam("id") Long id, Lead entity) {
 		entity.setId(id);
 		entity = em.merge(entity);
 		return Response.noContent().build();
 	}
 
	public void sendLead(Long id, List<LinkedHashMap> agents) {
 		TypedQuery<Lead> findByIdQuery = em
 				.createQuery(
 						"SELECT l FROM Lead l LEFT JOIN FETCH l.saleAgent WHERE l.id = :entityId",
 						Lead.class);
 		findByIdQuery.setParameter("entityId", id);
 		Lead entity = findByIdQuery.getSingleResult();
		System.out.println("LIST" +agents);
		List<String> aliases = new ArrayList<String>();
		for(LinkedHashMap hashMap : agents){
			aliases.add(hashMap.get("loginName").toString());
			
		}
		leadSender.sendLeads(aliases, entity);
 	}
 }
