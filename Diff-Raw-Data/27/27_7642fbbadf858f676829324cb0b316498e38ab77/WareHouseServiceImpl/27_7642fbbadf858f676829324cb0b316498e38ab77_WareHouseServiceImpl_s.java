 package external.warehouse.service;
 
 
 import javax.jws.*;
 
 import external.warehouse.data.*;
 
 
 
 
 @WebService(endpointInterface = "service.WareHouseService")
 public class WareHouseServiceImpl implements WareHouseService {
 	
 	private Warehouse wh;
 	
 	
 	public WareHouseServiceImpl() {
 		super();
 		this.wh = new Warehouse();
 		Product CDs = new Product(1, 200, 500, "CDs");
 		Product DVDs = new Product(3, 200, 300, "DVDs");
 		wh.addProduct(CDs, "A");
 		wh.addProduct(DVDs, "B");
 		
 	}
 
 
 	public LocationResponse getLocation(LocationRequest request){
 		LocationResponse response = new LocationResponse();
 		response.location = wh.getLocation(request.idProduct);
 		return response;
 	}
/*
 	public PlanningResponse getPlanning() {
 		// TODO Auto-generated method stub
 		return null;
 	}
*/
 	public ListNearOutOfStockResponse listNearOutOfStock() {
 		ListNearOutOfStockResponse response = new ListNearOutOfStockResponse();
 		response.list = wh.getListNearOutOfStock();
 		return null;
 	}
 
 	public QuantityResponse getQuantity(QuantityRequest request) {
 		QuantityResponse response = new QuantityResponse();
 		response.quantity = wh.getProduct(request.idProduct).getQuantity();
 		return response;
 	}
 
 	
 	public OrderResponse makeOrder(OrderRequest request) {
 		OrderResponse response = new OrderResponse();
 		Order order = new Order(request.supplier, request.dateOfDelivery, request.listProduct);
 		response.result = wh.addOrder(order);
 		return response;
 	}
/*
 	public DeliveryResponse haveDelivery(DeliveryRequest request) {
 		// TODO Auto-generated method stub
 		return null;
 	}
*/
 }
