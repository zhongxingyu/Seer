 package edu.unsw.triangle.web;
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import edu.unsw.triangle.controller.AbstractFormController;
 import edu.unsw.triangle.controller.ModelView;
 import edu.unsw.triangle.model.Item;
 import edu.unsw.triangle.service.ItemService;
 import edu.unsw.triangle.util.Errors;
 import edu.unsw.triangle.util.ItemValidator;
 import edu.unsw.triangle.util.Messages;
 import edu.unsw.triangle.util.Validator;
 import edu.unsw.triangle.view.ItemBinder;
 import edu.unsw.triangle.view.RequestBinder;
 
 public class SellFormController extends AbstractFormController 
 {
 	private final Logger logger = Logger.getLogger(SellFormController.class.getName());
 	@Override
 	public String getFormView() 
 	{
 		return "sell.view";
 	}
 
 	@Override
 	protected Object createBackingObject(HttpServletRequest request) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected ModelView handleFormSubmit(Object command) 
 	{
 		Item item = (Item) command;
 		
 		ModelView modelView = null;
 		// Service operations here
 		try
 		{
 			ItemService.addNewItem(item);
 			// Success message
 			Messages message = new Messages();
			message.add("sell.success", "item \"" + item.getTitle() + "\" is now listed");
 			
 			modelView = new ModelView(getSuccessView()).addModel("messages", message);		
 		}
 		catch (Exception e)
 		{
 			logger.warning("cannot add new item to repository reason: " + e.getMessage());
 			e.printStackTrace();
 			Errors errors = new Errors().rejectValue("sell.error", "cannot add new item to repository");
 			modelView = handleFormError(command, errors);
 		}
 
 		return modelView;
 	}
 
 	@Override
 	protected Validator getValidator() 
 	{
 		return new ItemValidator();
 	}
 
 	@Override
 	protected RequestBinder getBinder() 
 	{
 		return new ItemBinder();
 	}
 
 	@Override
 	protected String getSuccessView() 
 	{
 		return "sell.confirm.view";
 	}
 
 	@Override
 	protected ModelView handleFormError(Object command, Errors errors) 
 	{
 		ModelView modelView = new ModelView(getFormView()).forward().
 				addModel("item", command).addModel("errors", errors);
 		return modelView;
 	}
 
 }
