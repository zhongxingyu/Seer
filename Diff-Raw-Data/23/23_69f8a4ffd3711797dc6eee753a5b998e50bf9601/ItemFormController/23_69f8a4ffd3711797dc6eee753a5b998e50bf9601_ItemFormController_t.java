 package edu.unsw.triangle.web;
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.unsw.triangle.controller.AbstractFormController;
 import edu.unsw.triangle.controller.ModelView;
 import edu.unsw.triangle.model.Bid;
 import edu.unsw.triangle.model.Item;
 import edu.unsw.triangle.model.Item.ItemStatus;
 import edu.unsw.triangle.service.BidService;
 import edu.unsw.triangle.service.ItemService;
 import edu.unsw.triangle.util.BidValidator;
 import edu.unsw.triangle.util.Errors;
 import edu.unsw.triangle.util.Validator;
 import edu.unsw.triangle.view.BidBinder;
 import edu.unsw.triangle.view.RequestBinder;
 
 public class ItemFormController extends AbstractFormController {
 
 	private final Logger logger = Logger.getLogger(ItemFormController.class.getName());
 	
 	@Override
 	public String getFormView() 
 	{
 		return "item.view";
 	}
 
 	@Override
 	protected Object createBackingObject(HttpServletRequest request) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// TODO poor logic here
 	@Override
 	protected ModelView handleFormSubmit(Object command) 
 	{
 		Bid bid = (Bid) command;
 		
 		// Check item is active
 		Item item = null;
 		//ModelView modelView = null;
 		try
 		{
 			item = ItemService.findItemById(bid.getItemId());
 		}
 		catch (Exception e)
 		{
 			// Error retrieving item
 			logger.severe("failure to retrieve item id: " + bid.getItemId() + " reason: " + e.getMessage());
			e.printStackTrace();
 			Errors errors = new Errors().rejectValue("item.error", "repository failure reason:" + e.getMessage());
 			return handleFormError(bid, errors).addModel("item", item).addParameter("id", String.valueOf(item.getId()));
 		}
 		
 		if (item.getStatus() != ItemStatus.ACTIVE)
 		{
 			// Item is not active
 			// Update view
 			logger.severe("item id: " + bid.getItemId() + " is not active");
 			Errors errors = new Errors().rejectValue("item.error", "repository failure reason:");
 			return handleFormError(bid, errors).addModel("item", item).addParameter("id", String.valueOf(item.getId()));
 		}
 		
 		// Check bid is greater than current bid plus increment
 		if (bid.getBidFloat() < (item.getBid() + item.getIncrement()))
 		{
			logger.severe("item id: bid: $" + bid.getBid() + " is less than current bid: " + item.getBid() + " and increment: " + item.getIncrement());
 			Errors errors = new Errors().rejectValue("bid", "bid is less than current bid plus increment");
 			return handleFormError(bid, errors).addModel("item", item).addParameter("id", String.valueOf(item.getId()));
 		}
 		
 		// Update new bid
 		try
 		{
 			// Notify bidder of success
 			BidService.updateItemBidAndNotify(bid, item);
 		}
 		catch (Exception e)
 		{
 			logger.severe("updating item with new bid failed reason: " + e.getMessage());
			e.printStackTrace();
 			Errors errors = new Errors().rejectValue("bid", "bid faild to update in repository");
 			return handleFormError(bid, errors).addModel("item", item).addParameter("id", String.valueOf(item.getId()));
 		}
 		// Message session here...
 		ModelView modelView = new ModelView(getSuccessView()).redirect().addParameter("id", String.valueOf(item.getId()));
 		
 		return modelView;
 	}
 
 	@Override
 	protected Validator getValidator() 
 	{
 		return new BidValidator();
 	}
 
 	@Override
 	protected RequestBinder getBinder() 
 	{
 		return new BidBinder();
 	}
 
 	@Override
 	protected String getSuccessView() 
 	{
 		return "item";
 	}
 
 	@Override
 	protected ModelView handleFormError(Object command, Errors errors) 
 	{
 		ModelView modelView = new ModelView(getFormView()).forward().
 				addModel("errors", errors);
 		return modelView;
 	}
 }
