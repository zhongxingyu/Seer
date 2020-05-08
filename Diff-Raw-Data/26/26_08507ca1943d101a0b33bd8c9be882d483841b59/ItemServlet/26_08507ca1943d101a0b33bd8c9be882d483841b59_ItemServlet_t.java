 package com.iappsam.servlet.item;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.iappsam.managers.ItemManager;
 import com.iappsam.search.ItemSearcher;
 import com.iappsam.util.Managers;
 
 @WebServlet("/items")
 public class ItemServlet extends HttpServlet {
 
 	public static final String LIST_ITEMS_JSP = "/items/list.jsp";
 	public static final String NEW_ITEM_JSP = "/items/new.jsp";
 	public static final String VIEW_ITEM_JSP = "/items/view.jsp";
 
 	private static final long serialVersionUID = 1L;
 
 	private AddItemAction addItem;
 	private NewItemAction newItem;
 	private ViewItemAction viewItem;
 	private SearchItemsAction searchItems;
 	private ListItemsAction listItems;
 
 	public ItemServlet() {
 		this(Managers.ITEM_MANAGER, new ItemSearcher());
 	}
 
 	public ItemServlet(ItemManager itemManager, ItemSearcher searcher) {
 		super();
 		addItem = new AddItemAction(itemManager);
 		newItem = new NewItemAction(itemManager);
 		viewItem = new ViewItemAction(itemManager);
		searchItems = new SearchItemsAction(searcher);
 		listItems = new ListItemsAction(itemManager);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		addItem.process(request, response);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		parseAction(request).process(request, response);
 	}
 
 	private Action parseAction(HttpServletRequest request) {
 
 		String newParam = request.getParameter(ItemParameter.NEW_ITEM);
 		String id = request.getParameter(ItemParameter.ITEM_ID);
 		String q = request.getParameter(ItemParameter.QUERY);
 
 		if (newParam != null && newParam.equals("item")) {
 			return newItem;
 		} else if (id != null) {
 			return viewItem;
 		} else if (q != null)
 			return searchItems;
 
 		return listItems;
 	}
 }
