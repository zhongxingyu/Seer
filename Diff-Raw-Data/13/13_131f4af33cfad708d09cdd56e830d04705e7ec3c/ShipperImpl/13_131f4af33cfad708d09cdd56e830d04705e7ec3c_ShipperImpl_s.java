 package br.usp.ime.futuremarket;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 
 @WebService(targetNamespace = "http://futuremarket.ime.usp.br",
         endpointInterface = "br.usp.ime.futuremarket.Shipper")
 public class ShipperImpl implements Shipper {
 
     
     private final ConcurrentMap<String, DeliveryInfo> deliveryInfoList;
     private long deliveryId;
     private ClassLoader loader = ShipperImpl.class.getClassLoader();
 	private String serviceName;
     
     public ShipperImpl() {
         this(true);
     }
 
     public ShipperImpl(final boolean useRegistry) {
    	Properties properties = initProperties();
        serviceName = properties.getProperty("this.name");
         deliveryInfoList = new ConcurrentHashMap<String, DeliveryInfo>();
         deliveryId = 0l;
 
         if (useRegistry) {
             register();
         }
        
     }
 
     private String getRelativePath() {
         String path = serviceName + "/endpoint";
 
         return path;
     }
 
     
 	private Properties initProperties() {
 		Properties properties = new Properties();
         try {
 			properties.load(loader.getResourceAsStream("shipper.properties"));
 		} catch (IOException e) {
 			System.out.println("Could not read resources/shipper.properties");
 		}
         return properties;
 	}
 
     private void register() {
     	final String REL_PATH = getRelativePath();
         final FutureMarket futureMarket = new FutureMarket();
         futureMarket.register(FutureMarket.SHIPPER_ROLE, serviceName, REL_PATH);
     }
 
     @WebMethod
     public String setDelivery(final PurchaseInfo purchaseInfo) {
         final DeliveryInfo deliveryInfo = new DeliveryInfo();
         deliveryInfo.setPurchase(purchaseInfo);
         deliveryInfo.setId(Long.toString(getDeliveryId()));
         deliveryInfo.setDate(new Date().toString());
         deliveryInfo.setStatus("done");
 
         deliveryInfoList.put(purchaseToIdentifier(purchaseInfo), deliveryInfo);
 
         return "OK";
     }
 
     private long getDeliveryId() {
         synchronized (this) {
             deliveryId++;
         }
 
         return deliveryId;
     }
 
     @WebMethod
     public DeliveryInfo getDeliveryStatus(final PurchaseInfo purchaseInfo) {
         final String purchaseId = purchaseToIdentifier(purchaseInfo);
         final DeliveryInfo deliveryInfo = deliveryInfoList.get(purchaseId);
 
         if (deliveryInfo != null) {
             deliveryInfoList.remove(purchaseId);
         }
 
         return deliveryInfo;
     }
 
     private String purchaseToIdentifier(PurchaseInfo p) {
         return p.getId() + p.getSellerEndpoint();
     }
 }
