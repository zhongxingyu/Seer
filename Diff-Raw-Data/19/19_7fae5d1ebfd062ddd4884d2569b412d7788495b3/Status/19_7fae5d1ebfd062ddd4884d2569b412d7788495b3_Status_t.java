 package com.rcviana.rest.status;
 
import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.rcviana.dao.StatusDao;
 import com.rcviana.qualifier.StatusQualifierDb;
 
 /**
  * @author Robson Viana
  * @version 0.1
  */
 @Path("/v1/status/*")
@ApplicationScoped
 public class Status {
 
 	private static final String VERSION = "0.1";
 
 	@Inject @StatusQualifierDb
 	private StatusDao statusDao;
 	
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public String getStatus() {
 		return "<h1>" + statusDao.getStatusDB() + "</h1>";
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	@Path("/version")
 	public String getVersion() {
 		return "<h1>Version : " + VERSION + "</h1>";
 	}
 }
