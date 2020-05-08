 package pl.edu.agh.security.deps.warehouse;
 
 import javax.annotation.security.RolesAllowed;
 import javax.jws.WebMethod;
 
 public class WarehouseBean {
 
     @RolesAllowed({ "magister" })
     @WebMethod
     public WarehouseResponse registerTransaction(WarehouseRequest request) {
         return WarehouseResponse.OK;
     }
 
 }
