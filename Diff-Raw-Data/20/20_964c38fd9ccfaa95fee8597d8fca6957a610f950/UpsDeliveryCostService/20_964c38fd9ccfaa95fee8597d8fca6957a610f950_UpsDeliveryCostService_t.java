 package si.kcclass.deliverycost.services;
 
 import java.util.List;
 
 import org.springframework.stereotype.Service;
 
 import si.kcclass.deliverycost.domain.DeliveryCostRequest;
 
 import com.ecocoma.service.shipping.ups.Postage;
 import com.ecocoma.service.shipping.ups.Shipping;
 import com.ecocoma.service.shipping.ups.UPSService;
 import com.ecocoma.service.shipping.ups.UPSServiceSoap;
 
 @Service
 public class UpsDeliveryCostService implements DeliveryCostService {
 	
 	/**
 	 * The key can be retrieved from the page below:
 	 * http://www.ecocoma.com/shipping_webservice.aspx
 	 */
	private String KEY_ID = "SHP-T36468206J";
 	private String DOMAIN_ID = "";
 
 	private UPSServiceSoap service;
 	
 	public UpsDeliveryCostService() {
 		UPSService serviceImpl = new UPSService();
 		service = serviceImpl.getUPSServiceSoap();
 	}
 
 	@Override
 	public Double getDeliveryCost(DeliveryCostRequest request) {
 		Double minimalDeliveryCost = null; 
 		Shipping rates = service.getUPSRate(KEY_ID, DOMAIN_ID,
 				request.getShipper().getPostalCode(), 
 				request.getShipper().getCountry().getCode(),
 				request.getRecipient().getPostalCode(), 
 				request.getRecipient().getCountry().getCode(), 
 				request.getWeight().toString(), null);
 		List<Postage> postages = rates.getPackage().getPostage();
 		for (Postage postage: postages) {
 			Double rate = Double.parseDouble(postage.getRate());
			if (rate == 0.0) {
				continue;
			}
 			if (minimalDeliveryCost == null || rate < minimalDeliveryCost) {
 				minimalDeliveryCost = rate;
 			}
 		}
 		// TODO: convert dollars to euro.
 		return minimalDeliveryCost;
 	}
 
 }
