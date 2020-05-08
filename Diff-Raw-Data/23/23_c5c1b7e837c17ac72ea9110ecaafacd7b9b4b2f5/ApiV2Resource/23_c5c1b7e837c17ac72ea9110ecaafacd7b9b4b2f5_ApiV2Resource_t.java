 package org.ocha.hdx.rest;
 
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.ocha.hdx.model.api2.ApiIndicatorValue;
import org.ocha.hdx.model.api2.ApiResultWrapper;
import org.ocha.hdx.service.ApiV2BackendService;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @RolesAllowed({ "admin", "api" })
 @Path("/public/api2")
 @Component
 public class ApiV2Resource {
 
	@Autowired
	private ApiV2BackendService apiV2BackendService;

 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/values")
	public ApiResultWrapper<ApiIndicatorValue> getIndicatorValues(@QueryParam("e") final List<String> entityCodes, @QueryParam("it") final List<String> indicatorTypeCodes,
			@QueryParam("s") final List<String> sourceCodes, @QueryParam("minTime") final String minTime, @QueryParam("maxTime") final String maxTime, @QueryParam("pageNum") final Integer pageNum,
			@QueryParam("pageSize") final Integer pageSize, @QueryParam("lang") final String lang) {

		// We could let Jersey do this for us, but we might want to do something smarter in the future
		final Integer startYear = minTime == null ? null : new Integer(minTime);
		final Integer endYear = maxTime == null ? null : new Integer(maxTime);

		final ApiResultWrapper<ApiIndicatorValue> listIndicatorsByCriteriaWithPagination = apiV2BackendService.listIndicatorsByCriteriaWithPagination(indicatorTypeCodes, sourceCodes, entityCodes,
				startYear, endYear, pageNum, pageSize, lang);

		return listIndicatorsByCriteriaWithPagination;
 	}
 
 }
