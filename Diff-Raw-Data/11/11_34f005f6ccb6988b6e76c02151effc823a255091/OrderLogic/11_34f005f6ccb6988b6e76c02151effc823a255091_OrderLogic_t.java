 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Orders;
 import overwatch.db.Personnel;
 import overwatch.gui.tabs.OrderTab;
 import overwatch.security.LoginManager;
 
 
 
 
 
 /**
  * Implements the program logic for the Orders tab.
  * Controls saving, loading, security checking etc.
  * 
  * @author  Lee Coakley
  * @version 1
  */
 
 
 
 
 
 public class OrderLogic extends TabController
 {
 	private final OrderTab tab;
 	
 	
 	
 	
 	
 	/**
 	 * Plug the GUI tab into the controller.
 	 * @param tab
 	 */
 	public OrderLogic( OrderTab tab )
 	{
 		this.tab = tab;
 		
 		attachEvents();
 	}
 	
 	
 	
 	
 	
 	public JPanel getTab() {
 		return tab;
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect() {	
 		populatePanels();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void doCreateOrder()
 	{
 		// TODO
 		System.out.println( "create" );
 	}
 	
 	
 	
 	
 	
 	private void doMarkAsDone()
 	{
 		// TODO Personnel save
 		System.out.println( "mark as done" );
 	}
 	
 	
 	
 	
 	
 	private void populatePanels()
 	{
 		populateMessagePanel( null );
 		
 		tab.ordersIn.setSearchableItems(
 			Orders.getOrdersAndSubjectsSentTo( LoginManager.getUser() )
 		);
 		
		tab.ordersOut.setSearchableItems(
 			Orders.getOrdersAndSubjectsSentBy( LoginManager.getUser() )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateMessagePanel( Integer orderNo )
 	{
 		if (orderNo == null) {
 			tab.messagePanel.clearAll();
 			return;
 		}
 		
 		EnhancedResultSet ers = Database.query(
 			"select sentDate,                " +
 			"       subject,                 " +
 			"       body,                    " +
 			"       sentBy,                  " +
 			"       sentTo                   " +
 			"                                " +
 			"from Orders   o,                " +
 			"     Messages m                 " +
 			"                                " +
 			"where o.messageNo = m.messageNo " +
 			"  and o.orderNo   = " + orderNo  + ";"
 		);
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "message" );
 			return;
 		}
 		
 		
 		String sentBy  = Personnel.getLoginName(  ers.getElemAs( "sentBy", Integer.class )  );
 		String sentTo  = Personnel.getLoginName(  ers.getElemAs( "sentTo", Integer.class )  );
 		
 		tab.messagePanel.sentBy .field.setText( sentBy );
 		tab.messagePanel.sentTo .field.setText( sentTo );
 		tab.messagePanel.subject.field.setText( ers.getElemAs( "subject",String.class ) );
 		tab.messagePanel.body         .setText( ers.getElemAs( "body",   String.class ) );
 	}
 	
 	
 	
 	
 	
 	private void attachEvents()
 	{
 		setupTabChangeActions();
 		setupSelectActions   ();
 		setupButtonActions   ();
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify( this );	
 	}
 	
 	
 	
 	
 	
 	private void setupSelectActions()
 	{
 		tab.addOrdersInSelectListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				populateMessagePanel( tab.ordersIn.getSelectedItem() );
 			}
 		});
 		
 		
 		tab.addOrdersOutSelectListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				populateMessagePanel( tab.ordersOut.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		tab.addCreateOrderListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				doMarkAsDone();
 			}
 		});
 		
 		
 		tab.addMarkAsDoneListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				doMarkAsDone();
 			}
 		});
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
