 package com.servicemesh.devops.demo.quickquote;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import javax.ws.rs.core.Response;
 
 @Path("/quote")
 public class QuoteResource {
 	
 	@GET
 	@Produces("text/html")
 	public Response getInsuranceTypes() {
 		List<String> insuranceTypes = getInsuranceTypeData();
 		String listData = getInsuranceTypesData(insuranceTypes);
 		return Response.ok(listData).build();
 	}
 	
 	private String getInsuranceTypesData(List<String>types) {
 		StringBuilder buffer = new StringBuilder();		
 		String radio = "<input type=\"radio\" name=\"group1\" value=\"%s\">%s</input><br>";
 		for (String insurance : types) {
 			buffer.append(String.format(radio, insurance, insurance));
 		}
 		return buffer.toString();
 	}
 	
 	private List<String> getInsuranceTypeData() {
 		List<String>insuranceTypes = new ArrayList<String>();
 		insuranceTypes.add("Auto");
 		insuranceTypes.add("Home");
 		insuranceTypes.add("Marine");
 		insuranceTypes.add("Life");
 		insuranceTypes.add("Aviation");
		insuranceTypes.add("Festival")
 		return insuranceTypes;
 	}
 
 }
