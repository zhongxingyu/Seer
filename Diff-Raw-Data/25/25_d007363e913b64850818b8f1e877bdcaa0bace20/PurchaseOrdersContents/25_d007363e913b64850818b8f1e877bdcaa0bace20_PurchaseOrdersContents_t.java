 package com.purchaseOrders;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import org.springframework.web.portlet.bind.annotation.ResourceMapping;
 
 import com.liferay.counter.service.CounterLocalServiceUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.json.JSONArray;
 import com.liferay.portal.kernel.json.JSONException;
 import com.liferay.portal.kernel.json.JSONFactoryUtil;
 import com.liferay.portal.kernel.json.JSONObject;
 import com.sb.model.Item;
 import com.sb.model.PurchaseItem;
 import com.sb.service.ItemLocalServiceUtil;
 import com.sb.service.PurchaseItemLocalServiceUtil;
 
 
 /**
  * Portlet implementation class PurchaseOrdersContents
  */
 @Controller(value = "purchaseorderscontents")
 @RequestMapping("VIEW")
 public class PurchaseOrdersContents {
 	
 	// DEFAULT PROCESS ACTION, EVERY CONTROLLER SHOULD HAVE ONE. IS RESPOSIBLE FOR INTIAL VIEW LOGIC
 	  @ActionMapping
 	  public void showCustomers(ActionRequest request, ActionResponse response)
 	      throws IOException, PortletException 
 	  {
 	    
 	    response.setRenderParameter("javax.portlet.render", "listCustomersrender");
 	    
 	  }
 
 	
 	@RenderMapping
 	public String defaultRenderrer(RenderRequest request,
 			RenderResponse response, Model model){
 		
 	    System.out.println(":::::"+ PurchaseOrdersContents.class.toString()+":::::::");
 
 		return "view";
 	}
 
 
 	@ResourceMapping(value = "updateItem")
 	  public void updateOrder(ResourceRequest request, ResourceResponse response) 
 	      throws PortletException, IOException, JSONException 
 	  {
 	      String updateInfo = request.getParameter("updateInfo");
 	    System.out.println(":::::::Information Received is ::::::: "+ updateInfo);
 	    JSONObject json = JSONFactoryUtil.createJSONObject(updateInfo);
 	    
 	    int purchaseItemID = json.getInt("PurchaseItemID");
 	    int quantity = json.getInt("Quantity");
 	    
 	    String loggedInUserID = request.getRemoteUser();
 	    Boolean succRep = Boolean.TRUE;
 	    
 	   
 	    // BUSINESS LOGIC GOES HERE 
 	   /*
 	    * Call to the update DB util method should go in here
 	    */
 	    
 	    try {
 	    	System.out.println("::::::Populating the model and calling the query:::::::");
 	    	PurchaseItem purchaseItem = PurchaseItemLocalServiceUtil.getPurchaseItem(purchaseItemID);
 	    	purchaseItem.setQuantity(quantity);
 	    	purchaseItem.setUnitPrice(ItemLocalServiceUtil.getItem(purchaseItem.getItemId()).getPrice()*quantity);
 	    	System.out.println(":::::Calling the Utility method with ::::::" + purchaseItem.getUnitPrice());
 	    	PurchaseItemLocalServiceUtil.updatePurchaseItem(purchaseItem, true);
 		} catch (PortalException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			succRep = Boolean.FALSE;
 		} catch (SystemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			succRep = Boolean.FALSE;
 		}
 	    
 	    JSONObject jsonObj = JSONFactoryUtil.createJSONObject();
 	    
 	    if(succRep.booleanValue()){
 	    	
 	    	System.out.println("::::::Success:::::");
 	    	jsonObj.put("ActivityStatus", "success");
 	    
 	    }
 	    else{
 	    	System.out.println("::::::Failure:::::");
 
 	    	jsonObj.put("ActivityStatus", "failure");
 	    }
 	    
 	    System.out.println(":::::Returning:::::" + jsonObj.toString());
 	    response.getWriter().println(jsonObj.toString());
 	  }
 	
 	@ResourceMapping(value = "deleteItem")
 	public void deleteItem(ResourceRequest request, ResourceResponse response) throws JSONException, IOException{
 		
 		String deleteInfo = request.getParameter("deleteInfo");
 		
 		System.out.println(":::::::Deleting. Delete Info is:::::"+ deleteInfo);
 		JSONObject json = JSONFactoryUtil.createJSONObject(deleteInfo);
 		
 		int purchaseItemID = json.getInt("PurchahseItemID");
 		Boolean status = Boolean.TRUE;
 		
 		/*
 		 * Call the Delete DB util method
 		 */
 		try {
 			
 			PurchaseItemLocalServiceUtil.deletePurchaseItem(purchaseItemID);
 			System.out.println(":::::Deleted::::");
 		} catch (PortalException e) {
 			// TODO Auto-generated catch block
 			status = Boolean.FALSE;
 			e.printStackTrace();
 		} catch (SystemException e) {
 			// TODO Auto-generated catch block
 			status = Boolean.FALSE;
 			e.printStackTrace();
 		}
 		
 		JSONObject jsonObj = JSONFactoryUtil.createJSONObject();
 		
 		
 		 if(status.booleanValue())
 			 jsonObj.put("ActivityStatus", "success");
 		 else
 		 	jsonObj.put("ActivityStatus", "failure");
 		 
 		System.out.println(":::::Returning Data:::::: "+ jsonObj.toString());
 		response.getWriter().println(jsonObj.toString());
 	}
 	
 	@ResourceMapping(value = "addItem")
 	public void addItem(ResourceRequest request, ResourceResponse response) throws IOException, JSONException{
 		
 		String addInfo = request.getParameter("addInfo");
 		
 		System.out.println("::::Adding Info:::: "+ addInfo);
 		JSONObject json =  JSONFactoryUtil.createJSONObject(addInfo);
 		
 		int purchaseId = json.getInt("PoId");
 		int itemId = json.getInt("ItemID");
 		int quantity = json.getInt("Quantity");
 		Boolean status = Boolean.TRUE;
 		/*
 		 * Call to the Insert DB Method of the utility class
 		 * 
 		 */
 		JSONObject jsonObj = JSONFactoryUtil.createJSONObject();
 		PurchaseItem purchaseItem;
 		try {
 			System.out.println(":::::Calling add Utility method::: ");
 			purchaseItem = PurchaseItemLocalServiceUtil.createPurchaseItem( new Long(CounterLocalServiceUtil.increment(PurchaseItem.class.getName())).intValue() );
 			purchaseItem.setPoId(purchaseId);
 			purchaseItem.setQuantity(quantity);
 			purchaseItem.setItemId(itemId);
 	    	purchaseItem.setUnitPrice(ItemLocalServiceUtil.getItem(purchaseItem.getItemId()).getPrice()*quantity);
 	    	PurchaseItemLocalServiceUtil.addPurchaseItem(purchaseItem);
 	    	System.out.println("::::::Added in DB::::: ");
 	    	jsonObj.put("PurchaseItemId", purchaseItem.getPurchaseItemId());
 		} catch (SystemException e) {
 			// TODO Auto-generated catch block
 			status= Boolean.FALSE;
 			e.printStackTrace();
 		} catch (PortalException e) {
 			// TODO Auto-generated catch block
 			status= Boolean.FALSE;
 			e.printStackTrace();
 		}
 		
 	jsonObj.put("ActivityStatus", "success");
 
 	System.out.println(":::::Inserted. Returning:::::::::: "+ jsonObj.toString());
 	response.getWriter().print(jsonObj.toString());
 	
 	}
 
 @ResourceMapping(value="autoComplete")
public void autoComplete (ResourceRequest request, ResourceResponse response) throws IOException{
 	
	System.out.println("::::::Receiving:::: "+ request.getParameter("term"));
	String search = "%"+request.getParameter("term")+"%";
 	JSONObject result = JSONFactoryUtil.createJSONObject();
 	JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
 	List<Item> itemsByTitle = null;
 	List<Item> itemsByArtist = null;
 	try {
 		itemsByTitle = ItemLocalServiceUtil.findByTitle(search);
 		itemsByArtist = ItemLocalServiceUtil.findByArtist(search);
 		
		//itemsByTitle.removeAll(itemsByArtist);
		itemsByTitle = new ArrayList<Item>(itemsByTitle);
 		itemsByTitle.removeAll(itemsByArtist);
 		
 		/*for(int i =0; i< itemsByArtist.size(); i++){
 		jsonArray.add(itemsByArtist.get(i));
 		}*/
 		
 		for(Item item : itemsByArtist) {
 			
 			JSONObject obj = JSONFactoryUtil.createJSONObject();
 			obj.put("item_title", item.getTitle());
 			obj.put("item_artist", item.getArtist());
 			obj.put("item_itemId", item.getItemId());
 			obj.put("item_price", item.getPrice());
 			obj.put("item_listPrice", item.getListPrice());
 			obj.put("item_releaseDate", item.getReleaseDate());
 			obj.put("item_version", item.getVersion());
 			obj.put("item_searchCriteria", 1);
			obj.put("value", item.getArtist() + " (Artist)");
 			jsonArray.put(obj);
 		}
 		
 		for(Item item : itemsByTitle) {
 			
 			JSONObject obj = JSONFactoryUtil.createJSONObject();
 			obj.put("item_title", item.getTitle());
 			obj.put("item_artist", item.getArtist());
 			obj.put("item_itemId", item.getItemId());
 			obj.put("item_price", item.getPrice());
 			obj.put("item_listPrice", item.getListPrice());
 			obj.put("item_releaseDate", item.getReleaseDate());
 			obj.put("item_version", item.getVersion());
 			obj.put("item_searchCriteria", 0);
			obj.put("value", item.getTitle() + " (Title)");
 			jsonArray.put(obj);
 		}
 		
 	} catch (SystemException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	
	//result.put( search, jsonArray);
	System.out.println(jsonArray.toString());
	response.getWriter().print(jsonArray.toString());
 }
 
 }
