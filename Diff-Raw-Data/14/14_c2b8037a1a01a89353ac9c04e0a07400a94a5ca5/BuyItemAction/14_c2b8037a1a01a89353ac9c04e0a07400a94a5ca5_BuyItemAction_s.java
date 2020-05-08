 package controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.Item;
 import databean.Message;
 import databean.User;
 
 import formbeans.BuyItemForm;
 
 import model.ItemDAO;
 import model.MessageDAO;
 import model.Model;
 import model.UserDAO;
 
 public class BuyItemAction extends Action{
 	
 	private FormBeanFactory<BuyItemForm> formBeanFactory = FormBeanFactory.getInstance(BuyItemForm.class, "<>\"");
 	
 	ItemDAO itemDAO;
 	MessageDAO messageDAO;
 	UserDAO userDAO;
 	
 	public BuyItemAction(Model model){
 		itemDAO = model.getItemDAO();
 		messageDAO = model.getMessageDAO();
 		userDAO = model.getUserDAO();
 	}
 	
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "buyItem.do";
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		// TODO Auto-generated method stub
 		BuyItemForm form = formBeanFactory.create(request);
 		List<String> errors = new ArrayList<String>();
 		request.setAttribute("errors", errors);
 		
 		errors.addAll(form.getValidationErrors());
 		if (errors.size() != 0) return "index.jsp";
 		
 		int itemId = form.getItemIdAsInt();
 		try {
 			if(itemId > itemDAO.getAllItems().length)
 				return "index.jsp";
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int buyType = form.getBuyTypeAsInt();
         User curUser = (User) request.getSession(false).getAttribute("user");
 
 		if(buyType == 1){
 			try {
 				Item item = itemDAO.getItemById(itemId);
 				int credit = item.getCredit();
 				if(curUser.getCredit() - credit < 0){
 					System.out.println(curUser.getCredit());
 					errors.add("Not enough credits.");
 					request.setAttribute("posted", item);
 					return "item_page.jsp";
 				}
 				userDAO.setCredit(curUser.getCredit() - credit, curUser.getUserName());
 				User owner = item.getOwner();
 				userDAO.setCredit(owner.getCredit() + credit, owner.getUserName());
 				itemDAO.closeItem(itemId);
 				curUser.setCredit(curUser.getCredit() - credit);
 				request.setAttribute("success", "Transaction was successfully made. Your " +
 						"remaining credits are " + curUser.getCredit());
 
 			} catch (DAOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}			
 		}
 		else{
 			try {
 				Item item = itemDAO.getItemById(itemId);
				String url = "http://localhost:8080/Breeze/complete.do?buyType=" + buyType + 
						"&buyerName=" + curUser.getUserName() + "&itemId=" + item.getId();
 				String[] buyTypeName = {"exchange with items", "exchange for credits", "exchange with items"};
 				Message msg = new Message();
				String content = "Your item: " + item.getItemName() + " has been responded " +
						"by the user: " + curUser.getUserName() + ", email: " + curUser.getEmail() + 
						", who agreed to " + buyTypeName[buyType - 2] + ". Click the link below " +
								"if you want to make a transaction with him. \n" + url;
 				msg.setContent(content);
 				msg.setSender(curUser);
 				msg.setReceiver(item.getOwner());
 				msg.setTitle("Item Requested!");
 				msg.setSentDate(new Date());
 				Message msg2 = new Message();
 				String content2 = "The item: " + item.getItemName() + " you requested has been sent " +
 						"to the user: " + item.getOwner().getUserName() + ", email: " + item.getOwner().getEmail() + 
 						". You agreed to " + buyTypeName[buyType - 2] + ". You will get automatically message notification" +
 								" if the item owner makes the transaction with you.";
 				msg2.setContent(content2);
 				msg2.setSender(item.getOwner());
 				msg2.setReceiver(curUser);
 				msg2.setTitle("Request Item Confirmation!");
 				msg2.setSentDate(new Date());
 				try {
 					messageDAO.create(msg);
 					messageDAO.create(msg2);
 
 				} catch (DAOException e) {
 					errors.add(e.getMessage());
 					if(item.getType() == 1)
 						request.setAttribute("posted", item);
 					else 
 						request.setAttribute("requested", item);
 					return "item_page.jsp";
 				}
 				
 				
 				
 				request.setAttribute("success", "Your request has been sent.");
 			} catch (DAOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}			
 		}
 	
 		return "showMessage.do";
 	}
 
 }
