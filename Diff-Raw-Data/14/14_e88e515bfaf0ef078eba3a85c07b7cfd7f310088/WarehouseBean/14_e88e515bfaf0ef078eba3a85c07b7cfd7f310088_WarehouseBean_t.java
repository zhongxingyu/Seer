 package pl.edu.agh.security.deps.warehouse;
 
 import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
 import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
 
import org.jboss.ejb3.annotation.SecurityDomain;

@Stateless
@WebService(name = "Warehouse", serviceName = "WarehouseService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@SecurityDomain("ws-ldap-saml")
@HandlerChain(file = "/META-INF/handlers.xml")
 public class WarehouseBean {
 
     @RolesAllowed({ "magister" })
     @WebMethod
     public WarehouseResponse registerTransaction(WarehouseRequest request) {
         return WarehouseResponse.OK;
     }
 
 }
