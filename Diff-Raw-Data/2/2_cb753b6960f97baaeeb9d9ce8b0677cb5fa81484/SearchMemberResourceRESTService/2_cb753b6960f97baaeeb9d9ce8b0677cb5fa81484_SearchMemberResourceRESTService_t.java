 package com.laurinka.skga.server.rest;
 
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import com.laurinka.skga.server.rest.model.NameNumberXml;
 import com.laurinka.skga.server.services.SkgaWebsiteService;
 
 /**
  * JAX-RS Example
  * <p/>
  * This class produces a RESTful service to read the contents of the members
  * table.
  */
 @Path("/members")
 @RequestScoped
 public class SearchMemberResourceRESTService {
 	@Inject
 	private EntityManager em;
 
 	@Inject
 	private SkgaWebsiteService service;
 
 	@GET
 	@Path("/search")
 	@Produces("text/xml")
 	public List<NameNumberXml> lookupMemberById(@QueryParam("q") String q) {
 		List<NameNumberXml> results = em
 				.createQuery(
 						"select new com.laurinka.skga.server.rest.model.NameNumberXml(m.name2, m.nr) from SkgaNumber m "
 								+ "where m.name2 is not null "
 								+ "and m.name2 like :name" + " order by m.nr desc", //
 						NameNumberXml.class)
				.setParameter("name", "%" + q + "%").setMaxResults(10)
 				.getResultList();
 		return results;
 	}
 }
