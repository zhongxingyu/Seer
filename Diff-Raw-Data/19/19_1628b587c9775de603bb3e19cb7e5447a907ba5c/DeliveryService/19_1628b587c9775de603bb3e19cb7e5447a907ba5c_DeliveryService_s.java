 package pl.edu.agh.security.delivery;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Random;
 
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 
 import pl.edu.agh.security.common.services.Delivery;
 import pl.edu.agh.security.common.services.DeliveryState;
 import pl.edu.agh.security.common.services.IDeliveryService;
 
 @Path("/delivery")
 public class DeliveryService implements IDeliveryService {
 
     Random random = new Random();
 
 	@Override
 	@PUT
	public DeliveryState putDelivery(Delivery body) {
 		DeliveryState deliveryState = new DeliveryState();
         deliveryState.setId(Long.toHexString(random.nextLong()));
 		deliveryState.setEta(Calendar.getInstance().getTime());
 		deliveryState.setPrice(new BigDecimal(body.getWeight() * 5.0));
 		return deliveryState;
 	}
 }
