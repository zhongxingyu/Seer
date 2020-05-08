 package org.ocha.hdx.rest;
 
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.ocha.hdx.model.api2.ApiIndicatorValue;
 import org.springframework.stereotype.Component;
 
 @RolesAllowed({ "admin", "api" })
 @Path("/public/api2")
 @Component
 public class ApiV2Resource {
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/values")
	public List<ApiIndicatorValue> getIndicatorValues(@QueryParam("e") final List<String> entityCodes, @QueryParam("it") final List<String> indicatorTypeCodes, @QueryParam("s") final List<String> sourceCodes,
			@QueryParam("minTime") final String minTime, @QueryParam("maxTime") final String maxTime) {
		return null;
 	}
 
 }
