 package edu.arizona.sirls.client.presenter.orders;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.arizona.sirls.client.event.orders.ClickOrderNameEvent;
 import edu.arizona.sirls.client.event.orders.ClickOrderNameEventHandler;
 import edu.arizona.sirls.client.event.orders.ClickTermEvent;
 import edu.arizona.sirls.client.event.orders.ClickTermEventHandler;
 import edu.arizona.sirls.client.event.orders.DragTermEndEvent;
 import edu.arizona.sirls.client.event.orders.DragTermEndEventHandler;
 import edu.arizona.sirls.client.event.orders.DragTermStartEvent;
 import edu.arizona.sirls.client.event.orders.DragTermStartEventHandler;
 import edu.arizona.sirls.client.event.orders.DropTermToBoxEvent;
 import edu.arizona.sirls.client.event.orders.DropTermToBoxEventHandler;
 import edu.arizona.sirls.client.presenter.Presenter;
 import edu.arizona.sirls.client.rpc.OrderServiceAsync;
 import edu.arizona.sirls.client.view.orders.DraggableTermView;
 import edu.arizona.sirls.client.view.orders.DroppableContainerView;
 import edu.arizona.sirls.client.view.orders.OrderNameLabelView;
 import edu.arizona.sirls.client.view.orders.OrderTblView;
 import edu.arizona.sirls.client.view.orders.Styles;
 import edu.arizona.sirls.shared.beans.orders.Order;
 import edu.arizona.sirls.shared.beans.orders.OrderSet;
 
 public class OrderSetContentPresenter implements Presenter {
 
 	public static interface Display {
 		Widget asWidget();
 
 		Button getSaveBtn();
 
 		Button getNewTermBtn();
 
 		Button getNewOrderBtn();
 
 		OrderSet getData();
 
 		FlexTable getBaseTermList();
 
 		FlexTable getLayoutTbl();
 
 		int getNumOfTerms();
 
 		void setNumOfTerms(int numOfTerms);
 
 		OrderSet getDataToSave();
 	}
 
 	private final Display display;
 	private OrderServiceAsync rpcService;
 
 	// each order set has its own event bus
 	private HandlerManager eventBus = new HandlerManager(null);
 
 	// fields to support function
 	private DraggableTermView termDragged;
 	private boolean hitValidDroppableBox;
 	private DroppableContainerView boxDropped;
 
 	// Style names
 	private String style_selected_term = "ORDERS_selected_term";
 	private String style_in_order = "ORDERS_in_order";
 
 	private ArrayList<OrderTblView> orderTblList = new ArrayList<OrderTblView>();
 
 	public OrderSetContentPresenter(Display display,
 			OrderServiceAsync rpcService) {
 		this.display = display;
 		this.rpcService = rpcService;
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 		container.clear();
 		bindEvents();
 		container.add(display.asWidget());
 		fillInBaseTerms(display.getData().getTerms());
 		fillInOrders(display.getData().getOrders());
 	}
 
 	@Override
 	public void bindEvents() {
 		eventBus.addHandler(ClickOrderNameEvent.TYPE,
 				new ClickOrderNameEventHandler() {
 
 					@Override
 					public void onClick(ClickOrderNameEvent event) {
 						highlightTermsInBase(event.getTerms());
 					}
 				});
 
 		eventBus.addHandler(ClickTermEvent.TYPE, new ClickTermEventHandler() {
 
 			@Override
 			public void onClick(ClickTermEvent event) {
 				highlightTerm(event.getTermName());
 
 				// TODO: show context
 			}
 		});
 
 		eventBus.addHandler(DragTermStartEvent.TYPE,
 				new DragTermStartEventHandler() {
 
 					@Override
 					public void onDragTermStart(DragTermStartEvent event) {
 						// set dragging widget
 						setTermDragged(event.getWidget());
 
 						// reset hitDroppableBox to be false
 						hitValidDroppableBox = false;
 
 					}
 				});
 
 		eventBus.addHandler(DragTermEndEvent.TYPE,
 				new DragTermEndEventHandler() {
 
 					@Override
 					public void onDragTermEnd(DragTermEndEvent event) {
 						/**
 						 * detect if the user is trying to remove a term out of
 						 * an order; drag end fired after on drop
 						 */
 						if (!hitValidDroppableBox && !termDragged.isFromBase()) {
 							String termName = termDragged.getTermName();
 							if (Window.confirm("Do you want to remove term '"
 									+ termName + "' from this order? ")) {
 								removeDraggedTermFromOrder();
 								highlightTermsInBase(event.getWidget()
 										.getParentOrder().getTermsList());
 							}
 						}
 					}
 				});
 
 		eventBus.addHandler(DropTermToBoxEvent.TYPE,
 				new DropTermToBoxEventHandler() {
 
 					@Override
 					public void onDropTermToBox(DropTermToBoxEvent event) {
 						// detect if the term already exist in this order
 						ArrayList<String> existingTerms = event.getWidget()
 								.getParentOrder().getTermsList();
 						String termName = termDragged.getTermName();
 
 						// if drag from base
 						if (termDragged.isFromBase()) {
 							if (existingTerms.indexOf(termName) >= 0) {
 								Window.alert("Term '"
 										+ termDragged.getTermName()
 										+ "' already exists in this order. ");
 								// highlight terms of this order
 								highlightTermsInBase(existingTerms);
 							} else {
 								setHitValidDroppableBox(true);
 								// drop the term into the droppable box
 								addTermToOrder(termName, event.getWidget());
 							}
 						} else {
 							// check if src and target are in the same order
 							DroppableContainerView srcBox = (DroppableContainerView) termDragged
 									.getParent().getParent();
 							DroppableContainerView targetBox = event
 									.getWidget();
 							if (srcBox.equals(targetBox)) {
 								return;
 							}
 
 							if (srcBox.getParentOrder().equals(
 									targetBox.getParentOrder())) {
 								setHitValidDroppableBox(true);
 								changeTermPosition(termName, event.getWidget());
 							}
 						}
 					}
 				});
 
 		display.getNewOrderBtn().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				String orderName = Window.prompt("Please input order's name",
 						"");
 
 				// validate order name
 				if (isValidOrderName(orderName)) {
 					String orderDescription = Window.prompt(
 							"Please describe order '" + orderName + "'", "");
 
 					// validate order description
 					if (orderDescription.equals("")) {
 						Window.alert("Order description cannot be empty!");
 						return;
 					}
 
 					addNewOrder(orderName, orderDescription);
 				}
 			}
 		});
 
 		display.getNewTermBtn().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// get term name
 				String termName = Window.prompt("Please input term name: ", "");
 
 				// validate term name
 				if (isValidTermName(termName)) {
 					// add term into base table
 					DraggableTermView newTermView = new DraggableTermView(
 							termName, true, null);
 					newTermView.getTermLabel().addStyleName(Styles.TO_SAVE);
 					new DraggableTermPresenter(newTermView, eventBus)
 							.goWithFlexTable(display.getBaseTermList(), 0,
 									display.getNumOfTerms());
 
 					// for each order, add an empty box
 					for (OrderTblView orderTbl : orderTblList) {
 						DroppableContainerView newBox = new DroppableContainerView(
 								display.getNumOfTerms(),
 								new ArrayList<String>(), orderTbl);
 
 						new DroppableContainerPresenter(newBox, eventBus)
 								.goWithFlexTable(orderTbl.getOrderTbl(), 0,
 										display.getNumOfTerms());
 						orderTbl.addBoxToList(newBox);
 					}
 
 					// update numTerms
 					display.setNumOfTerms(display.getNumOfTerms() + 1);
 				}
 			}
 		});
 
 		display.getSaveBtn().addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// check if have to_save
 				if (GQuery.$(display.getLayoutTbl().getElement())
 						.find("." + Styles.TO_SAVE).length() > 0) {
 					saveData();
 				} else {
 					Window.alert("You didn't make any changes in this category. ");
 				}
 			}
 		});
 	}
 
 	private void saveData() {
 		rpcService.saveOrderSet(display.getDataToSave(),
 				new AsyncCallback<Void>() {
 
 					@Override
 					public void onSuccess(Void result) {
 						Window.alert("Changes in this order category has been saved successfully. ");
 						// clear all to_save in this set
 						GQuery.$(display.getLayoutTbl().getElement())
 								.find("." + Styles.TO_SAVE)
 								.removeClass(Styles.TO_SAVE);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Server Error: failed to save data to server. \n\n"
 								+ caught.getMessage());
 					}
 				});
 	}
 
 	private boolean isValidOrderName(String orderName) {
 		if (orderName.equals("")) {
 			Window.alert("Order name cannot be empty!");
 			return false;
 		}
 		if (GQuery
 				.$(display.getLayoutTbl().getElement())
 				.find(OrderNameLabelView.TAG + "[order_name=\"" + orderName
 						+ "\"]").length() > 0) {
 			Window.alert("Order '" + orderName
 					+ "' alredy exists in this category. ");
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean isValidTermName(String termName) {
 		if (termName.equals("")) {
 			return false;
 		}
 
 		if (GQuery
 				.$(display.getBaseTermList().getElement())
 				.find(DraggableTermView.TAG + "[term_name=\"" + termName
 						+ "\"]").length() > 0) {
 			Window.alert("Term '" + termName
 					+ "' alredy exists in this category. ");
 			return false;
 		}
 
 		return true;
 	}
 
 	private void fillInBaseTerms(ArrayList<String> terms) {
 		int collumn = 0;
 		for (String term : terms) {
 			new DraggableTermPresenter(new DraggableTermView(term, true, null),
 					eventBus).goWithFlexTable(display.getBaseTermList(), 0,
 					collumn);
 			collumn++;
 		}
 	}
 
 	private void addOrderRow(Order order, int row, boolean isNewOrder) {
 		OrderTblView orderContent = new OrderTblView(order,
 				display.getNumOfTerms());
 		orderTblList.add(orderContent);
 
 		// order name label
 		OrderNameLabelView orderNameLbl = new OrderNameLabelView(order,
 				orderContent);
 		if (isNewOrder) {
 			orderNameLbl.addStyleName(Styles.TO_SAVE);
 		}
 		new OrderNameLabelPresenter(orderNameLbl, eventBus).goWithFlexTable(
 				display.getLayoutTbl(), row, 0);
 
 		// terms list in this order
 		new OrderTblPresenter(orderContent, eventBus).goWithFlexTable(
 				display.getLayoutTbl(), row, 1);
 	}
 
 	private void fillInOrders(ArrayList<Order> orders) {
 		int row = 1;
 		for (Order order : orders) {
 			addOrderRow(order, row, false);
 			row++;
 		}
 
 		// add new order button
 		addNewOrderBtn(row);
 	}
 
 	private void addNewOrderBtn(int row) {
 		display.getLayoutTbl().setWidget(row, 0, display.getNewOrderBtn());
 		display.getLayoutTbl().getCellFormatter()
 				.addStyleName(row, 0, "ORDERS_order_name_cell");
 	}
 
 	public DraggableTermView getTermDragged() {
 		return termDragged;
 	}
 
 	public void setTermDragged(DraggableTermView termDragged) {
 		this.termDragged = termDragged;
 	}
 
 	public boolean isHitValidDroppableBox() {
 		return hitValidDroppableBox;
 	}
 
 	public void setHitValidDroppableBox(boolean hitValidDroppableBox) {
 		this.hitValidDroppableBox = hitValidDroppableBox;
 	}
 
 	public DroppableContainerView getBoxDropped() {
 		return boxDropped;
 	}
 
 	public void setBoxDropped(DroppableContainerView boxDropped) {
 		this.boxDropped = boxDropped;
 	}
 
 	/**
 	 * highlight terms in base terms table
 	 * 
 	 * @param terms
 	 */
 	private void highlightTermsInBase(ArrayList<String> terms) {
 		// work on base terms table
 		GQuery baseTbl = GQuery.$(display.getBaseTermList().getElement());
 
 		// remove previous highlight
 		baseTbl.find("." + style_in_order).removeClass(style_in_order);
 
 		// highlight one by one
 		for (String term : terms) {
 			baseTbl.find(DraggableTermView.TAG + "[term_name=\"" + term + "\"]")
 					.addClass(style_in_order);
 		}
 	}
 
 	/**
 	 * highlight term clicked in the entire order set
 	 * 
 	 * @param term
 	 */
 	private void highlightTerm(String term) {
 		// work on the entire order set
 		GQuery orderSetTbl = GQuery.$(display.getLayoutTbl().getElement());
 
 		// remove previous highlight from the entire page
 		$("." + style_selected_term).removeClass(style_selected_term);
 
 		// highlight all draggable terms with this name
 		orderSetTbl
 				.find(DraggableTermView.TAG + "[term_name=\"" + term + "\"]")
 				.addClass(style_selected_term);
 	}
 
 	/**
 	 * remove the term being dragged out of its parent order
 	 */
 	private void removeDraggedTermFromOrder() {
 		String termName = termDragged.getTermName();
 		// update base terms table, make this term "not in this order"
 		GQuery baseTermBtl = GQuery.$(display.getBaseTermList().getElement());
 		baseTermBtl.find(
 				DraggableTermView.TAG + "[term_name=\"" + termName + "\"]")
 				.removeClass(style_in_order);
 
 		// add to_save to this order
 		termDragged.getParent().addStyleName(Styles.TO_SAVE);
 
 		// update term list of this order
 		DroppableContainerView parentBox = (DroppableContainerView) termDragged
 				.getParent().getParent();
 		parentBox.removeTermFromBox(termName);
 		termDragged.getParentOrder().removeTerm(termName);
 
 		// remove the widget
 		termDragged.asWidget().removeFromParent();
 
 		// highlight
 		highlightTermsInBase(parentBox.getParentOrder().getTermsList());
 	}
 
 	/**
 	 * add a term to an order
 	 * 
 	 * @param termName
 	 * @param targetBox
 	 */
 	private void addTermToOrder(String termName,
 			DroppableContainerView targetBox) {
 		// add to_save to target
 		targetBox.addStyleName(Styles.TO_SAVE);
 
 		// create term and add it to target
 		new DraggableTermPresenter(new DraggableTermView(termName, false,
 				targetBox.getParentOrder()), eventBus).go(targetBox
 				.getContainer());
 
 		// update termslist
 		targetBox.addTermToBox(termName);
 		targetBox.getParentOrder().addTerm(termName);
 
 		// hightlight
 		highlightTermsInBase(targetBox.getParentOrder().getTermsList());
 	}
 
 	private void changeTermPosition(String termName,
 			DroppableContainerView targetBox) {
 		// add style to_save to both src and target
 		termDragged.getParent().addStyleName(Styles.TO_SAVE);
 		targetBox.addStyleName(Styles.TO_SAVE);
 
 		// create draggable term and add it to target
 		new DraggableTermPresenter(new DraggableTermView(termName, false,
 				targetBox.getParentOrder()), eventBus).go(targetBox
 				.getContainer());
 		// update terms of target
 		targetBox.addTermToBox(termName);
 
 		// remove from src
 		DroppableContainerView srcBox = (DroppableContainerView) termDragged
 				.getParent().getParent();
 		srcBox.removeTermFromBox(termName);
 		termDragged.asWidget().removeFromParent();
 
 		highlightTermsInBase(targetBox.getParentOrder().getTermsList());
 	}
 
 	/**
 	 * add a order when click new order button
 	 * 
 	 * @param orderName
 	 * @param orderDescription
 	 */
 	private void addNewOrder(String orderName, String orderDescription) {
 		// get row value
 		int row = display.getLayoutTbl().getRowCount() - 1;
 
 		// add order
 		Order order = new Order(orderName, orderDescription);
 		addOrderRow(order, row, true);
 
 		// re-add new order btn
 		addNewOrderBtn(row + 1);
 	}
 
 }
