 package controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.ExchangeDAO;
 import model.ItemDAO;
 import model.MessageDAO;
 import model.Model;
 import model.UserDAO;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.factory.RollbackException;
 import org.mybeans.factory.Transaction;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.Exchange;
 import databean.Item;
 import databean.User;
 import formbeans.BuyItemForm;
 
 public class BuyItemAction extends Action{
 	
 	private FormBeanFactory<BuyItemForm> formBeanFactory = FormBeanFactory.getInstance(BuyItemForm.class, "<>\"");
 	
 	ItemDAO itemDAO;
 	MessageDAO messageDAO;
 	UserDAO userDAO;
 	ExchangeDAO exchangeDAO;
 	
 	public BuyItemAction(Model model){
 		itemDAO = model.getItemDAO();
 		messageDAO = model.getMessageDAO();
 		userDAO = model.getUserDAO();
 		exchangeDAO = model.getExchangeDAO();
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
 		List<String> errors = prepareErrors(request);
 		
 		if (!form.isPresent()) { return "browse.do"; }
 		
 		errors.addAll(form.getValidationErrors());
 		if (errors.size() != 0) return "browse.do";
 		
 		int itemId = form.getItemIdAsInt();
 		int buyType = form.getBuyTypeAsInt();
         User curUser = (User) request.getSession(false).getAttribute("user");
         
         if (curUser == null) {
         	errors.add("You are not logged in");
         	return "browse.do";
         }
         
         Item item = null;
         User admin = null;
         try {
         	item = itemDAO.getItemById(itemId);
         	admin = userDAO.lookup("Admin");
         	curUser = userDAO.lookup(curUser.getUserName());
         } catch (DAOException e) {
         	errors.add(e.getMessage());
         	return "browse.do";
         }
         request.getSession().setAttribute("user", curUser);
         if(item == null || admin == null || item.getStatus() == Item.CLOSED) {
         	errors.add("Item not available");
         	return "browse.do";
         }
      
         if(item.getType() == Item.POST)
 			request.setAttribute("posted", item);
 		else 
 			request.setAttribute("requested", item);
         if (item.getOwner().getUserName().equals(curUser.getUserName())) {
 			request.setAttribute("isOwner", 1);
 
         	errors.add("You cannot buy your own item");
         	return "item_page.jsp";
         }
 		request.setAttribute("isOwner", 0);
 
 		if(buyType == Exchange.ANSWER_POST_WITH_CREDIT && item.getType() == Item.POST && item.getCredit() != -1){
 			try {
 				
 				int credit = item.getCredit();
 				if(curUser.getCredit() - credit < 0){
 					errors.add("Not enough credits.");			
 					return "item_page.jsp";
 				}
 				
 				User owner = item.getOwner();
 				Transaction.begin();
 				itemDAO.closeItem(itemId);
 				curUser.setCredit(curUser.getCredit() - credit);
 				userDAO.transferCredit(credit, curUser, owner);
 				Exchange[] pending = exchangeDAO.findItemPendingTransactions(item);
 				for (Exchange xchg : pending) {
 					messageDAO.send(admin, xchg.getResponder(), "Transaction dismissed", 
 									"The item (" + item.getItemName() + ") you have responded to is now closed");
 					userDAO.updateNewMsgCount(xchg.getResponder().getUserName(), 1);
 				}
 				//exchangeDAO.openPendingTransaction(item, curUser, Exchange.ANSWER_POST_WITH_CREDIT);
 				exchangeDAO.createSuccessTransaction(item, curUser);
 				exchangeDAO.closeItemTransaction(item);
 				messageDAO.send(admin, owner, "Your item has been sold", 
 								"Your item: " + item.getItemName() +" has been bought by "
 								+ curUser.getUserName() + " and transaction now is closed.");
 				userDAO.updateNewMsgCount(owner.getUserName(), 1);
 				messageDAO.send(admin, curUser, "You won the item", 
 								"You have just won the item " + item.getItemName()
 								+ ". Congratulations.");
 				userDAO.updateNewMsgCount(curUser.getUserName(), 1);
 				Transaction.commit();
 				request.setAttribute("success", "Transaction was successfully made. Your " +
 						"remaining credits are " + curUser.getCredit());
 				
 			} catch (RollbackException e) {
 				errors.add(e.getMessage());
 				return "item_page.jsp";
 			} finally {
 				if (Transaction.isActive()) Transaction.rollback();
 			}
 		}
 		else if ((buyType == Exchange.ANSWER_POST_WITH_EXCHANGE && item.getType() == Item.POST && item.getExchangeItemDescription() != null) ||
 				 (buyType == Exchange.ANSWER_REQUEST_FOR_CREDIT && item.getType() == Item.REQUEST && item.getCredit() != -1) ||
 				 (buyType == Exchange.ANSWER_REQUEST_FOR_EXCHANGE && item.getType() == Item.REQUEST && item.getExchangeItemDescription() != null)) {
 			try {
 				if (exchangeDAO.exists(item, buyType, curUser)) {
 					errors.add("You have already reponded to this item");
 					return "item_page.jsp";
 				}
 			} catch (DAOException e) {
 				errors.add(e.getMessage());
 				return "item_page.jsp";
 			}
 			
 			try {
 				
 				User owner = item.getOwner();
 				Transaction.begin();
 				int exchangeId = exchangeDAO.openPendingTransaction(item, curUser, buyType);
				String url = "<a href=&quot;complete.do?exchangeId=" + exchangeId + "&quot;>link</a>";
 				String[] buyTypeName = {"exchange with items", "exchange for credits", "exchange with items"};
 				
 				String content = "Your item (" + item.getItemName() + ") has been responded " +
 						"by " + curUser.getUserName() +  
 						", who agreed to " + buyTypeName[buyType - 2] + ". Click this " + url +
 								" if you want to make a transaction with him.";
				System.out.println(content.length());
 				
 				
 				String content2 = "The item: " + item.getItemName() + " you requested has been sent " +
 						"to the user: " + item.getOwner().getUserName() + ", email: " + item.getOwner().getEmail() + 
 						". You agreed to " + buyTypeName[buyType - 2] + ". You will get automatically message notification" +
 								" if the item owner makes the transaction with you.";
 				
 				messageDAO.send(admin, owner, "Your item has been responded", content);
 				userDAO.updateNewMsgCount(owner.getUserName(), 1);
 				messageDAO.send(admin, curUser, "Your request is accepted", content2);
 				userDAO.updateNewMsgCount(curUser.getUserName(), 1);
 				Transaction.commit();
 				
 				
 				request.setAttribute("success", "Your request has been sent.");
 			} catch (RollbackException e) {
 				errors.add(e.getMessage());
 				return "item_page.jsp";
 			} finally {
 				if (Transaction.isActive()) Transaction.rollback();
 			}
 		}
 		else {
 			errors.add("Illegal state argument");
 			return "item_page.jsp";
 		}
 		
 		try {
 			curUser = userDAO.lookup(curUser.getUserName());
 			request.getSession().setAttribute("user", curUser);
 		} catch(DAOException e) {
 			
 		}
 		
 		return "showItems.do?itemId=" + item.getId();
 	}
 
 }
