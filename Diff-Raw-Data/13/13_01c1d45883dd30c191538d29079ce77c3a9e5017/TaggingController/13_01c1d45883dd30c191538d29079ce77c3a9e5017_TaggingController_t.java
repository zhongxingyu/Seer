 package edu.unlv.kilo.web;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edu.unlv.kilo.domain.ItemEntity;
 import edu.unlv.kilo.domain.MoneyValue;
 import edu.unlv.kilo.domain.TransactionDescription;
 import edu.unlv.kilo.domain.TransactionEntity;
 import edu.unlv.kilo.domain.UserData;
 
 @RequestMapping("/tagging/**")
 @Controller
 /**
  * Sorts transactions into items
  * Not safe with concurrent multi-actor operations on the same user's data
  * @author Shane
  *
  */
 public class TaggingController {
 	
 	List<MonetaryTransactionFilter> filters = new ArrayList<MonetaryTransactionFilter>();
 
 	@RequestMapping(method = RequestMethod.POST, value = "{id}")
 	public void post(@PathVariable Long id, ModelMap modelMap,
 			HttpServletRequest request, HttpServletResponse response) {
 	}
 	
 	public void actionFilter(String filterText, boolean exclusive) {
 		try {
 			MonetaryTransactionFilterDescription filter = new MonetaryTransactionFilterDescription(filterText, exclusive);
 			filters.add(filter);
 		} catch (Exception e) {
 			// Non-critical invalid input detected; do nothing
 		}
 	}
 	
 	public void actionRemoveFilter(int index) {
 		filters.remove(index);
 	}
 	
 	public void actionRemoveAllFilters() {
 		filters.clear();
 	}
 
 	@RequestMapping
 	public String index(ModelMap modelMap, HttpServletRequest request, HttpSession session) {
 		UserData.debugEnsureUserData(session);
 		UserData userData = UserData.getSessionUserData(session);
 		
 		// Execute any posted commands (e.g. delete transaction, remove transaction)
 		try {
 			String command = request.getParameter("method");
 			TaggingAction action = TaggingAction.get(command);
 			
 			String id = request.getParameter("id");
 			int index = Integer.parseInt(id);
 			
 			switch (action) {
 			case DELETE:
 				userData.transactionDelete(index);
 				break;
 			case REMOVE:
 				userData.transactionRemove(index);
 				break;
 			case FILTER:
 				String filterText = request.getParameter("filterText");
 				String filterExclusive = request.getParameter("filterExclusive");
 				
 				actionFilter(filterText, filterExclusive != null);
 				break;
 			case REMOVEFILTER:
 				actionRemoveFilter(index);
 				break;
 			case REMOVEALLFILTERS:
 				actionRemoveAllFilters();
 				break;
 			case ITEMCREATE:
 				String itemDescription = request.getParameter("newitemDescription");
				String itemInflationString = request.getParameter("newitemInflation");
				boolean itemInflation = (itemInflationString != null);
 				String itemRecurrenceAutomaticString = request.getParameter("newitemRecurrence");
 				boolean itemRecurrenceAutomatic = itemRecurrenceAutomaticString.compareTo("automatic") == 0;
 				String itemRecurrenceIntervalString = request.getParameter("newitemRecurrenceInterval");
 				int itemRecurrenceInterval = 0;
 				if (!itemRecurrenceAutomatic) {
 					itemRecurrenceInterval = Integer.parseInt(itemRecurrenceIntervalString);
 				}
 				if (itemDescription.length() == 0)
 				{
 					break;
 				}
 				if (!itemRecurrenceAutomatic && itemRecurrenceInterval <= 0)
 				{
 					break;
 				}
 				userData.transactionItemCreate(filters, itemDescription, itemInflation, itemRecurrenceAutomatic, itemRecurrenceInterval);
 				break;
 			case ITEMUSE:
 				String itemIndexString = request.getParameter("useitemDescription");
 				int itemIndex = Integer.parseInt(itemIndexString);
 				
 				userData.transactionItemUse(filters, itemIndex);
 				
 				break;
 			}
 			
 //			action.execute(this);
 		} catch (NumberFormatException e) {
 			// Do nothing
 		} catch (IndexOutOfBoundsException e) {
 			// Do nothing
 		}
 
 		// Build model
 		userData.buildTaggingModel(modelMap, filters);
 		
 		return "tagging/index";
 	}
 	
 	/**
 	 * INTERNAL: Process requests to remove a transaction from selection, delete a transaction from the system, etc.
 	 * @param command textual representation of action
 	 * @param id index of affected transaction from current session's transactions list
 	 */
 //	private void processCommand(TaggingAction action, TransactionEntity transaction) {
 //		action.execute(transaction);
 //	}
 }
 
 
 
 
 
 
 
 
 /*
 Removed from index.jspx:
 					<td>
 						<form id="command" action="/offbudget/tagging/index" method="post">
 							<input type="hidden" name="method" value="DELETE" /> <input
 								type="hidden" name="id" value="${transactionStatus.index}" /> <input
 								onclick="return confirm('Are you sure want to delete this transaction from offBudget?');"
 								value="Delete Transaction" type="image"
 								title="Delete transaction"
 								src="/offbudget/resources/images/delete.png" class="image"
 								alt="Delete Transaction" />
 						</form>
 					</td>
  */
 
