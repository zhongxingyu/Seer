 package module.webservice;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 import myorg._development.PropertiesManager;
 import myorg.domain.VirtualHost;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 
 @Path("/exportStructureService")
 public class ExportStructureService {
 
     @GET
    @Path("{username}/{password}/listCostCenters.csv")
     @Produces("text/csv")
     public Response listCostCenters(@PathParam("username") final String username,
 	    @PathParam("password") final String password) {
 	check(username, password);
 	final String content = generateCostCenterList();
 	return Response.ok(content, "text/csv").build();
     }
 
     private String generateCostCenterList() {
 	final StringBuilder stringBuilder = new StringBuilder();
 	for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnitsSet()) {
 	    if (unit instanceof CostCenter) {
 		final CostCenter costCenter = (CostCenter) unit;
 		if (isActive(costCenter)) {
 		    stringBuilder.append(costCenter.getCostCenter());
 		    stringBuilder.append("\t");
 		    stringBuilder.append(costCenter.getName());
 		    stringBuilder.append("\n");
 		}
 	    }
 	}
 	return stringBuilder.toString();
     }
 
     private boolean isActive(final CostCenter costCenter) {
 	// TODO : review this...
 	return true;
     }
 
     private void check(final String username, final String password) {
 	final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
 	final String hostname = virtualHost.getHostname();
 
 	final String keyUsername = "exportStructureService.username." + hostname;
 	final String keyPassword = "exportStructureService.password." + hostname;
 
 	final String eUsername = PropertiesManager.getProperty(keyUsername);
 	final String ePassword = PropertiesManager.getProperty(keyPassword);
 
 	if (!match(username, eUsername) || !match(password, ePassword)) {
 	    throw new Error("unauthorized.access");
 	}
     }
 
     private boolean match(final String s1, final String s2) {
 	return s1 != null && s1.equals(s2);
     }
 
 }
