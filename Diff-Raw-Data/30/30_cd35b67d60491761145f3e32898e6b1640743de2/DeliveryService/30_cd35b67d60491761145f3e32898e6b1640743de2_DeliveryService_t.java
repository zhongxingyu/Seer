 package pl.edu.agh.security.delivery;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Random;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import pl.edu.agh.security.common.services.DeliveryState;
 import pl.edu.agh.security.common.services.IDeliveryService;
 
 @Path("/delivery")
 @Consumes({ "application/json" })
 @Produces({ "application/json" })
 public class DeliveryService implements IDeliveryService {
 
     Random random = new Random();
 
 	@Override
 	@PUT
    public DeliveryState putDelivery(@QueryParam("source") String source,
            @QueryParam("destination") String destination, @QueryParam("weight") double weight) {
 		DeliveryState deliveryState = new DeliveryState();
         deliveryState.setId(Long.toHexString(random.nextLong()));
 		deliveryState.setEta(Calendar.getInstance().getTime());
        deliveryState.setPrice(new BigDecimal(weight * 5.0));
 		return deliveryState;
 	}
 }
