 package com.tap.ui;
 
 import hirondelle.date4j.DateTime.DayOverflow;
 
 import java.util.List;
 import java.util.TimeZone;
 import java.util.UUID;
 
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.DateTime;
 
 import com.hexapixel.widgets.generic.Utils;
 import com.tap.bizlogic.OrderLogic;
 import com.tap.tableordersys.BookOrder;
 import com.tap.tableordersys.Guests;
 import com.tap.tableordersys.Order;
 import com.tap.tableordersys.Table;
 import com.tap.usersys.Operator;
 
 public class NewBookingBox {
 
 	private MainUI mainUI;
 	private OrderLogic orderLogic;
 	protected Shell shell;
 	private Text textGuestID;
 	private Text textGuestAmount;
 	private Text textAddtionalInfo;
 	DateTime dateTime;
 	DateTime dateTime_1;
 	private Text textBookOrderID;
 	Button buttonAllowSeatTogether;
 
 	public NewBookingBox(){}
 	public NewBookingBox(MainUI mainUI, OrderLogic orderLogic) {
 		super();
 		this.orderLogic = orderLogic;
 		this.mainUI = mainUI;
 	}
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			NewBookingBox window = new NewBookingBox();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shell = new Shell();
 		shell.setSize(502, 316);
 		shell.setText("New book order");
 		Utils.centerDialogOnScreen(shell);
 		
 		Label lblBookorderId = new Label(shell, SWT.NONE);
 		lblBookorderId.setBounds(41, 20, 92, 17);
 		lblBookorderId.setText("BookOrder ID");
 		
 		textBookOrderID = new Text(shell, SWT.BORDER);
 		textBookOrderID.setBounds(175, 19, 73, 23);
 		textBookOrderID.setText(UUID.randomUUID().toString().substring(0, 3));
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setText("Guest ID");
 		label.setBounds(41, 51, 61, 17);
 		
 		textGuestID = new Text(shell, SWT.BORDER);
 		textGuestID.setText(UUID.randomUUID().toString().substring(0, 3));
 		textGuestID.setBounds(175, 48, 73, 23);
 		
 		Label label_1 = new Label(shell, SWT.NONE);
 		label_1.setText("Guest amount");
 		label_1.setBounds(39, 80, 94, 17);
 		
 		textGuestAmount = new Text(shell, SWT.BORDER);
 		textGuestAmount.setBounds(175, 77, 73, 23);
 		
 		buttonAllowSeatTogether = new Button(shell, SWT.CHECK);
 		buttonAllowSeatTogether.setText("Allow seat together");
 		buttonAllowSeatTogether.setBounds(41, 103, 146, 17);
 		
 		Label label_2 = new Label(shell, SWT.NONE);
 		label_2.setText("Additional infomation");
 		label_2.setBounds(41, 121, 61, 17);
 		
 		textAddtionalInfo = new Text(shell, SWT.BORDER);
 		textAddtionalInfo.setBounds(41, 144, 207, 23);
 		
 		Label lblBookTime = new Label(shell, SWT.NONE);
 		lblBookTime.setBounds(41, 186, 61, 17);
 		lblBookTime.setText("Book time");
 		
 		dateTime = new DateTime(shell, SWT.BORDER | SWT.TIME | SWT.SHORT);
 		dateTime.setBounds(155, 183, 93, 24);
 		
 		Label lblBookDate = new Label(shell, SWT.NONE);
 		lblBookDate.setBounds(267, 20, 61, 17);
 		lblBookDate.setText("Book date");
 		
 		dateTime_1 = new DateTime(shell, SWT.BORDER | SWT.CALENDAR);
 		dateTime_1.setBounds(257, 44, 219, 160);
 		
 		Button button_1 = new Button(shell, SWT.NONE);
 		button_1.setText("Commit");
 		button_1.setBounds(197, 241, 80, 27);
 		button_1.addSelectionListener(new btCommitListener());
 
 	}
 	class btCommitListener implements SelectionListener{
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Integer amount = new Integer(0);;
 			try{
 				amount = new Integer(textGuestAmount.getText());
 				if(amount<=0){
 					MessageBox msgBox = new MessageBox(shell, 1);
 					msgBox.setMessage("How can you have so \"MANY\" people?");
 					msgBox.open();
 					return;
 				}
 			}catch (NumberFormatException ex) {
 				System.err.println(ex.getMessage());
 				MessageBox msgBox = new MessageBox(shell, 1);
 				msgBox.setMessage("Wrong amount input!");
 				msgBox.open();
 				return;
 			}
 			
 			hirondelle.date4j.DateTime bookTime = new hirondelle.date4j.DateTime(dateTime_1.getYear()
 					, dateTime_1.getMonth()+1, dateTime_1.getDay(), dateTime.getHours()
 					, dateTime.getMinutes(), 0, 0);
 			hirondelle.date4j.DateTime laterThenThisTime = hirondelle.date4j.DateTime.now(TimeZone.getDefault());
 			//laterThenThisTime = laterThenThisTime.plus(0, 0, 0, +2, 0, 0, DayOverflow.Spillover);
 			if( bookTime.compareTo(laterThenThisTime)<=0 ){
 				System.err.println("newBooking: book time should be later!");
 				MessageBox msgBox = new MessageBox(shell, 1);
 				msgBox.setMessage("Only can book time after now!");
 				msgBox.open();
 				return;
 			}
 			
 			//Save Booking
 			Guests g = new Guests(textGuestID.getText(), textAddtionalInfo.getText(), textGuestAmount.getText()
 					, !buttonAllowSeatTogether.getSelection());
 			List<BookOrder> o = orderLogic.newBooking(textBookOrderID.getText(), g, bookTime);
 			if(null!=o){
 				MessageBox msgBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK );
 				StringBuffer msgContent = new StringBuffer();
 				for(Order or:o){
 					msgContent.append(" "+or.getTable().getId());
 				}
 				msgBox.setMessage("Customer occupied table of "+msgContent);
 				msgBox.open();
 			}else{
 				MessageBox msgBox = new MessageBox(shell, 2);
 				msgBox.setMessage("Not enough table avalable at time:"+bookTime+".");
 				msgBox.open();
 			}
 			mainUI.doRefreshCurrentTableView();
 			shell.close();
 		}
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {}
 	}
 }
