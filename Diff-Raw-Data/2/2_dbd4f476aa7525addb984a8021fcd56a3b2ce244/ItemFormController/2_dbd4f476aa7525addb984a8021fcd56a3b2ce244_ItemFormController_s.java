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
 import edu.unsw.triangle.util.Messages;
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
 		Item item = bid.getItem();
 		
 		if (item == null)
 		{
 			logger.severe("item id: " + bid.getItemId() + " is null");
 			Errors errors = new Errors().rejectValue("item.error", "item id: " + bid.getItemId() + " is null");
 			return handleFormError(bid, errors);
 		}
 		
 		// Check item is active and not expired
 		if (item.getStatus() != ItemStatus.ACTIVE || item.getTimeLeft() < 0)
 		{
 			// Item is not active or expired
 			logger.severe("item id: " + bid.getItemId() + " is not active or expired");
 			Errors errors = new Errors().rejectValue("bid", "bid failed, item is no longer active");
 			return handleFormError(bid, errors);
 		}
 		
 		// Check bid is greater than current bid plus increment
 		if (bid.getBidFloat() < (item.getBid() + item.getIncrement()))
 		{
 			logger.severe("item id: bid: $" + bid.getBid() + " is less than current bid: " + item.getBid() + " and increment: " + item.getIncrement());
 			Errors errors = new Errors().rejectValue("bid", "bid is less than current bid plus increment");
 			return handleFormError(bid, errors);
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
 			return handleFormError(bid, errors);
 		}
 		
 		Messages messages = new Messages().add("bid.success", "your bid was successful");
 		ModelView modelView = new ModelView(getSuccessView()).redirect().addParameter("id", String.valueOf(item.getId())).addModel("messages", messages);
 		
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
 		Bid bid = (Bid) command;
 		ModelView modelView = new ModelView(getFormView()).forward().addModel("item", bid.getItem()).
 				addModel("errors", errors).addParameter("id", String.valueOf(bid.getItemId()));
 		return modelView;
 	}
 }
