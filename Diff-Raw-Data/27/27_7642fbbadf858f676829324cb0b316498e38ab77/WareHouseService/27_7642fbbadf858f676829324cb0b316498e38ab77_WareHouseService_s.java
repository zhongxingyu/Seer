 package external.warehouse.service;
 
 import javax.jws.*;
 
 
 @WebService(serviceName="WareHouse")
 public interface WareHouseService {
 
 	@WebMethod
 	@WebResult(name="response")
 	public LocationResponse getLocation(@WebParam(name="message") LocationRequest request);
 	
	/*
 	@WebMethod
 	@WebResult(name="planningResponse")
 	public PlanningResponse getPlanning();
	*/
 	
 	@WebMethod
 	@WebResult(name="listNearOutOfStockResponse")
 	public ListNearOutOfStockResponse listNearOutOfStock();
 	
 	
 	@WebMethod
 	@WebResult(name="response")
 	public QuantityResponse getQuantity(@WebParam(name="message") QuantityRequest request);
 	
 	
 	@WebMethod
 	@WebResult(name="orderResponse")
 	public OrderResponse makeOrder(@WebParam(name="request") OrderRequest request);
 	
	/*
 	@WebMethod
 	@WebResult(name="deliveryResponse")
 	public DeliveryResponse haveDelivery(@WebParam(name="request") DeliveryRequest request);
	*/
 	
 }
