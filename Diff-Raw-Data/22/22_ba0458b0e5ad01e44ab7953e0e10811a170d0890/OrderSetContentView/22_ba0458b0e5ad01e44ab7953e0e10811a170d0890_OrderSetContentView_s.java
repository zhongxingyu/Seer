 package edu.arizona.sirls.client.view.orders;
 
 import java.util.ArrayList;
 
 import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 
 import edu.arizona.sirls.client.presenter.orders.OrderSetContentPresenter;
 import edu.arizona.sirls.shared.beans.orders.Order;
 import edu.arizona.sirls.shared.beans.orders.OrderSet;
 import edu.arizona.sirls.shared.beans.orders.TermInOrder;
 
 public class OrderSetContentView extends Composite implements
 		OrderSetContentPresenter.Display {
 
 	private Button saveBtn;
 	private Button newTermBtn;
 	private Button newOrderBtn;
 
 	private FlexTable layout;
 	private FlexTable baseTermList;
 	private OrderSet orderSet;
 	private int numOfTerms;
 
 	public OrderSetContentView(OrderSet orderSet) {
 		this.orderSet = orderSet;
 		this.numOfTerms = orderSet.getTerms().size();
 
 		layout = new FlexTable();
 		layout.setWidth("100%");
 		layout.addStyleName("ORDERS_orderset_tbl");
 		initWidget(layout);
 		CellFormatter cellFormatter = layout.getCellFormatter();
 		ColumnFormatter columnFormatter = layout.getColumnFormatter();
 
 		// label: available terms
 		Label label_availableTerms = new Label("Available Terms: ");
 		layout.setWidget(0, 0, label_availableTerms);
 		cellFormatter.addStyleName(0, 0, "tbl_field_label");
 		columnFormatter.setWidth(0, "20%");
 		layout.getRowFormatter().addStyleName(0, "ORDERS_base_row");
 
 		// base terms
 		HorizontalPanel basePanel = new HorizontalPanel();
 		basePanel.setSpacing(5);
 		// basePanel.setWidth("100%");
 		layout.setWidget(0, 1, basePanel);
 		columnFormatter.addStyleName(1, "ORDERS_table_order");
 		columnFormatter.setWidth(1, "80%");
 
 		// base terms list
 		baseTermList = new FlexTable();
 		baseTermList.addStyleName("ORDERS_base_tbl");
 		basePanel.add(baseTermList);
 
 		// new term btn
 		newTermBtn = new Button("New Term");
 		basePanel.add(newTermBtn);
 
 		// save btn
 		saveBtn = new Button("Save Orders");
 		basePanel.add(saveBtn);
 
 		// new order btn
 		newOrderBtn = new Button("New Order");
 	}
 
 	@Override
 	public Button getSaveBtn() {
 		return saveBtn;
 	}
 
 	@Override
 	public Button getNewTermBtn() {
 		return newTermBtn;
 	}
 
 	@Override
 	public Button getNewOrderBtn() {
 		return newOrderBtn;
 	}
 
 	@Override
 	public OrderSet getData() {
 		return orderSet;
 	}
 
 	@Override
 	public FlexTable getBaseTermList() {
 		return baseTermList;
 	}
 
 	@Override
 	public FlexTable getLayoutTbl() {
 		return layout;
 	}
 
 	@Override
 	public int getNumOfTerms() {
 		return numOfTerms;
 	}
 
 	@Override
 	public void setNumOfTerms(int numOfTerms) {
 		this.numOfTerms = numOfTerms;
 	}
 
 	@Override
 	public OrderSet getDataToSave() {
 		OrderSet orderSetToSave = new OrderSet(orderSet.getCategoryID(),
 				orderSet.getCategoryName());
 
 		// get new terms
 		ArrayList<String> newTerms = new ArrayList<String>();
 		GQuery newTermLbls = GQuery.$(baseTermList.getElement()).find(
 				"." + Styles.TO_SAVE);
 		for (int i = 0; i < newTermLbls.length(); i++) {
 			newTerms.add(newTermLbls.get(i).getAttribute("term_name"));
 		}
 		orderSetToSave.setTerms(newTerms);
 
 		// for each order row, check if have data to save
 		ArrayList<Order> orders = new ArrayList<Order>();
 		int orderCount = layout.getRowCount() - 2;
 		for (int i = 0; i < orderCount; i++) {
 			int row = i + 1;
 			GQuery orderNameLbl = GQuery.$(layout.getWidget(row, 0)
 					.getElement());
 
 			// check if this row has data to save
 			if (orderNameLbl.parent().parent().find("." + Styles.TO_SAVE)
 					.length() < 1) {
 				continue;
 			}
 
 			// get order id, order name, order description, whether order is
 			// newly created
 			Order order = new Order(orderNameLbl.attr("order_name"),
 					orderNameLbl.attr("order_description"));
 			if (orderNameLbl.hasClass(Styles.TO_SAVE)) {
 				order.setNewlyCreated(true);
 			} else {
 				order.setNewlyCreated(false);
 				order.setOrderID(orderNameLbl.attr("order_id"));
 			}
 			// get terms in order
 			ArrayList<TermInOrder> termsInOrder = new ArrayList<TermInOrder>();
 			OrderTblView orderTblView = (OrderTblView) layout.getWidget(row, 1);
 			ArrayList<DroppableContainerView> boxes = orderTblView.getBoxes();
 			for (DroppableContainerView box : boxes) {
 				int position = box.getBoxIndex();
 				for (String term : box.getTermsInBox()) {
 					TermInOrder termInOrder = new TermInOrder(term, position);
 					termsInOrder.add(termInOrder);
 				}
 			}
 
 			order.setTermsInOrder(termsInOrder);
 			orders.add(order);
 		}
 		orderSetToSave.setOrders(orders);
 
 		return orderSetToSave;
 	}
 
 }
