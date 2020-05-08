 package com.iappsam.servlet.item;
 
 import static com.iappsam.servlet.item.ItemAttribute.ITEMS;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.iappsam.entities.Item;
import com.iappsam.managers.ItemManager;
import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.search.ItemSearcher;
 
 public class SearchItemsAction implements Action {
 
	private ItemManager itemManager;
 	private ItemSearcher searcher;
 
	public SearchItemsAction(ItemManager itemManager, ItemSearcher searcher) {
 		super();
		this.itemManager = itemManager;
 		this.searcher = searcher;
 	}
 
 	@Override
 	public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		List<Item> items = searcher.search(request.getParameter(ItemParameter.QUERY));
		try {
			items = itemManager.getAllItems();
			request.setAttribute(ITEMS, items);
			request.getRequestDispatcher(ItemServlet.LIST_ITEMS_JSP).forward(request, response);
		} catch (TransactionException e) {
			e.printStackTrace();
		}
 	}
 }
