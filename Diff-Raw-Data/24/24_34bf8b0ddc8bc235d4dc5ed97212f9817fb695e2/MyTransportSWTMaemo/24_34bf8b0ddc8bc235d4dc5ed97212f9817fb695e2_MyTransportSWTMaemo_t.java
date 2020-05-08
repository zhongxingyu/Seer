 package example.ui.gui.maemo;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import example.mytansport.MyTransport;
 import example.ui.console.InfoConsole;
 
 public class MyTransportSWTMaemo {
 
 	private final Shell shell;
 	private Display display;
 	private Color error = new Color(null, 255, 0, 0);
 	private MyTransport myTransport;
 	private Label services;
 
 	public MyTransportSWTMaemo(final MyTransport myTransport) {
 		this.myTransport = myTransport;
 		display = Display.getDefault();
 		shell = new Shell(display);
 
 		/* Init the shell */
 		shell.setText("MyTransport");
 		FormLayout layout = new FormLayout();
 		layout.marginHeight = 5;
 		layout.marginWidth = 5;
 		shell.setLayout(layout);
		shell.setSize(710, 450);
 
 		// MENU
 		Menu menuBar = new Menu(shell, SWT.BAR);
 		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		fileMenuHeader.setText("Add service");
 		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
 		fileMenuHeader.setMenu(fileMenu);
 		final MenuItem concertItem = new MenuItem(fileMenu, SWT.PUSH);
 		concertItem.setText("Concert");
 		final MenuItem footItem = new MenuItem(fileMenu, SWT.PUSH);
 		footItem.setText("Foot");
 		shell.setMenuBar(menuBar);
 		concertItem.addListener(SWT.Selection, new MenuListener(concertItem));
 		footItem.addListener(SWT.Selection, new MenuListener(footItem));
 
 		// CHECKBOXS
 		final Button checkPublish = new Button(shell, SWT.CHECK);
 		checkPublish.setText("Publish a transport");
 		FormData checkPublishFormData = new FormData();
 		checkPublishFormData.top = new FormAttachment(0, 0);
 		checkPublishFormData.left = new FormAttachment(0, 0);
 		checkPublish.setLayoutData(checkPublishFormData);
 
 		final Button checkSearch = new Button(shell, SWT.CHECK);
 		checkSearch.setText("Search a transport");
 		FormData checkSearchFormData = new FormData();
 		checkSearchFormData.top = new FormAttachment(checkPublish, 10);
 		checkSearchFormData.left = new FormAttachment(0, 0);
 		checkSearch.setLayoutData(checkSearchFormData);
 
 		// ERROR
 		final Label error = new Label(shell, SWT.NONE);
 		error.setForeground(MyTransportSWTMaemo.this.error);
 		error.setText("Bad format number!");
 		error.setVisible(false);
 		FormData errorFormData = new FormData();
 		errorFormData.top = new FormAttachment(checkSearch, 10);
 		errorFormData.left = new FormAttachment(0, 140);
 		error.setLayoutData(errorFormData);
 
 		// DAY
 		Label day = new Label(shell, SWT.NONE);
 		day.setText("Day (jj/mm/yyyy): ");
 		FormData dayFormData = new FormData();
 		dayFormData.top = new FormAttachment(checkSearch, 50);
 		dayFormData.left = new FormAttachment(0, 0);
 		day.setLayoutData(dayFormData);
 
 		final Text dayText = new Text(shell, SWT.BORDER);
 		dayText.setTextLimit(2);
 		FormData dayTextFormData = new FormData();
 		dayTextFormData.width = 30;
 		dayTextFormData.top = new FormAttachment(checkSearch, 46);
 		dayTextFormData.left = new FormAttachment(day, 15);
 		dayText.setLayoutData(dayTextFormData);
 
 		Label slash1 = new Label(shell, SWT.NONE);
 		slash1.setText("/");
 		FormData slash1FormData = new FormData();
 		slash1FormData.top = new FormAttachment(checkSearch, 50);
 		slash1FormData.left = new FormAttachment(dayText, 5);
 		slash1.setLayoutData(slash1FormData);
 
 		final Text mounthText = new Text(shell, SWT.BORDER);
 		mounthText.setTextLimit(2);
 		FormData mounthTextFormData = new FormData();
 		mounthTextFormData.width = 30;
 		mounthTextFormData.top = new FormAttachment(checkSearch, 46);
 		mounthTextFormData.left = new FormAttachment(slash1, 5);
 		mounthText.setLayoutData(mounthTextFormData);
 
 		Label slash2 = new Label(shell, SWT.NONE);
 		slash2.setText("/");
 		FormData slash2FormData = new FormData();
 		slash2FormData.top = new FormAttachment(checkSearch, 50);
 		slash2FormData.left = new FormAttachment(mounthText, 5);
 		slash2.setLayoutData(slash2FormData);
 
 		final Text yearText = new Text(shell, SWT.BORDER);
 		yearText.setTextLimit(4);
 		FormData yearTextFormData = new FormData();
 		yearTextFormData.width = 60;
 		yearTextFormData.top = new FormAttachment(checkSearch, 46);
 		yearTextFormData.left = new FormAttachment(slash2, 5);
 		yearText.setLayoutData(yearTextFormData);
 
 		// RED STAR1
 		Label star1 = new Label(shell, SWT.NONE);
 		star1.setForeground(new Color(null, 255, 0, 0));
 		star1.setText("*");
 		FormData star1FormData = new FormData();
 		star1FormData.top = new FormAttachment(checkSearch, 46);
 		star1FormData.left = new FormAttachment(yearText, 5);
 		star1.setLayoutData(star1FormData);
 
 		// DESTINATION
 		Label destination = new Label(shell, SWT.NONE);
 		destination.setText("Destination: ");
 		FormData destinationFormData = new FormData();
 		destinationFormData.top = new FormAttachment(yearText, 10);
 		destinationFormData.left = new FormAttachment(0, 0);
 		destination.setLayoutData(destinationFormData);
 
 		final Text destinationText = new Text(shell, SWT.BORDER);
 		FormData destinationTextFormData = new FormData();
 		destinationTextFormData.width = 216;
 		destinationTextFormData.top = new FormAttachment(yearText, 5);
 		destinationTextFormData.left = new FormAttachment(0, 142);
 		destinationText.setLayoutData(destinationTextFormData);
 
 		// RED STAR2
 		Label star2 = new Label(shell, SWT.NONE);
 		star2.setForeground(new Color(null, 255, 0, 0));
 		star2.setText("*");
 		FormData star2FormData = new FormData();
 		star2FormData.top = new FormAttachment(yearText, 5);
 		star2FormData.left = new FormAttachment(destinationText, 5);
 		star2.setLayoutData(star2FormData);
 
 		// MyTransport
 		services = new Label(shell, SWT.NONE);
 		services.setForeground(MyTransportSWTMaemo.this.error);
 		services.setText("No service enable...");
 		FormData myTransportFormData = new FormData();
 		myTransportFormData.width = 300;
 		myTransportFormData.top = new FormAttachment(destinationText, 10);
 		myTransportFormData.left = new FormAttachment(0, 0);
 		services.setLayoutData(myTransportFormData);
 
 		// CONTACT
 		Label contact = new Label(shell, SWT.NONE);
 		contact.setText("Contact: ");
 		FormData contactFormData = new FormData();
 		contactFormData.top = new FormAttachment(services, 10);
 		contactFormData.left = new FormAttachment(0, 0);
 		contact.setLayoutData(contactFormData);
 
 		final Text contactText = new Text(shell, SWT.BORDER);
 		FormData contactTextFormData = new FormData();
 		contactTextFormData.width = 216;
 		contactTextFormData.top = new FormAttachment(services, 5);
 		contactTextFormData.left = new FormAttachment(0, 142);
 		contactText.setLayoutData(contactTextFormData);
 
 		// TRANSPORT
 		Label transport = new Label(shell, SWT.NONE);
 		transport.setText("Transport: ");
 		FormData transportFormData = new FormData();
 		transportFormData.top = new FormAttachment(contactText, 10);
 		transportFormData.left = new FormAttachment(0, 0);
 		transport.setLayoutData(transportFormData);
 
 		final Text transportText = new Text(shell, SWT.BORDER);
 		FormData transportTextFormData = new FormData();
 		transportTextFormData.width = 216;
 		transportTextFormData.top = new FormAttachment(contactText, 5);
 		transportTextFormData.left = new FormAttachment(0, 142);
 		transportText.setLayoutData(transportTextFormData);
 
 		// SEPARATOR
 		Label separator1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_SOLID);
 		FormData separator1FormData = new FormData();
 		separator1FormData.width = 365;
 		separator1FormData.top = new FormAttachment(transportText,20);
 		separator1.setLayoutData(separator1FormData);
 
 		// RESULT
 		final StyledText result = new StyledText(shell, SWT.BORDER);
 		result.setEditable(false);
 		Image font = new Image(display,
 				MyTransportSWTMaemo.class.getResourceAsStream(
 				"myTransport.png"));
 		result.setBackgroundImage(font);
 		FormData resultTextFormData = new FormData();
 		resultTextFormData.width = 280;
 		resultTextFormData.height = 380;
 		resultTextFormData.top = new FormAttachment(0, 0);
 		resultTextFormData.left = new FormAttachment(0, 400);
 		result.setLayoutData(resultTextFormData);
 
 		// button "SEND"
 		final Button okButton = new Button(shell, SWT.PUSH);
 		okButton.setEnabled(false);
 		okButton.setText("Send");
 		okButton.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				try{
 					Integer.parseInt(dayText.getText() + mounthText.getText() + yearText.getText());
 					error.setVisible(false);
 					result.setText("");
 					String key1 = dayText.getText() + "/" + mounthText.getText() + "/" + yearText.getText();
 					String key2 = destinationText.getText();
 					String message1 = "MyTransport";
 					String message2 = contactText.getText();
 					String message3 = transportText.getText();
 					if(checkPublish.getSelection()){
 						if(!yearText.getText().equals(destinationText.getText())) { // DEBUG MODE! 
 							myTransport.put(key1 + "+" + key2, message1 + "+" + message2 + "+" + message3);
 						} else {
 							myTransport.put(key2, message1 + "+" + message2 + "+" + message3); // Debug
 						}
 						result.setText("Summary:\n\t- event: " + message1 + "\n\t- contact: " + message2 + "\n\t- transport: " + message3 + "\n\n===> MyTransport published!");
 					} else {
 						String found;
 						if(!yearText.getText().equals(destinationText.getText())) { // DEBUG MODE! 
 							found = myTransport.get(key1 + "+" + key2);
 						} else {
 							found = myTransport.get(key2);
 						}
 						if(found == null || found.equals("null") || found.equals("")){
 							result.setText("No transport found...");
 						} else {
 							String[] nbResult = found.split("\\*\\*\\*\\*");
 							for(int i=0 ; i<nbResult.length ; i++){
 								if(!nbResult[i].equals("") && !nbResult[i].equals("null")){
 									String[] args = nbResult[i].split("\\+");
 									result.setText(result.getText() + "MyTransport found:\n\t- event: " + args[0] + "\n\t- contact: " + args[1] + "\n\t- transport: " + args[2] + "\n\n");
 								}
 							}
 						}
 					}
 				} catch(NumberFormatException excep){
 					error.setVisible(true);
 				} catch(Exception excep) {
 					excep.printStackTrace();
 				}
 			}
 		});
 		FormData okFormData = new FormData();
 		okFormData.width = 80;
 		okFormData.top = new FormAttachment(separator1, 20);
 		okFormData.left = new FormAttachment(0, 150);
 		okButton.setLayoutData(okFormData);
 		shell.setDefaultButton(okButton);
 
 		// SELECTION LISTENER
 		Listener sendListener = new Listener() {
 			public void handleEvent(Event event) {
 				okButton.setEnabled(
 						(checkPublish.getSelection() || checkSearch.getSelection()) &&
 						!dayText.getText().equals("") && !mounthText.getText().equals("") &&
 						!yearText.getText().equals("") && !destinationText.getText().equals(""));
 				checkPublish.setEnabled(!checkSearch.getSelection());
 				checkSearch.setEnabled(!checkPublish.getSelection());
 			}
 		};
 		checkPublish.addListener(SWT.Selection, sendListener);
 		checkSearch.addListener(SWT.Selection, sendListener);
 		dayText.addListener(SWT.KeyUp, sendListener);
 		mounthText.addListener(SWT.KeyUp, sendListener);
 		yearText.addListener(SWT.KeyUp, sendListener);
 		destinationText.addListener(SWT.KeyUp, sendListener);	
 	}
 
 	private class MenuListener implements Listener{
 		private MenuItem item;
 		public MenuListener(MenuItem item){
 			this.item = item;
 		}
 
 		public void handleEvent(Event arg0) {
 			new AddServiceDialog(shell, item, myTransport, services);
 		}
 	}
 
 	public void start(){
 		shell.open();		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		display.dispose();
 		myTransport.kill();
 	}
 
 	public static void main(String[] args) {
 		try{
 			// LAUNCHING CHORD
 			System.out.print("MyTransport's Launching, please wait... ");
 			String ip = InfoConsole.getIp();
 			MyTransport myTransport = new MyTransport(ip, Integer.parseInt(args[0]));
 			new Thread(myTransport).start();
 			do{
 				Thread.sleep(1000);
 			} while(myTransport.getTransport() == null);
 
 			// IF ARGS
 			if(args.length > 1 && args[1].equals("-j")){
 				String hostToJoin = args[2];
 				int portToJoin = Integer.parseInt(args[3]);
 				myTransport.join(hostToJoin, portToJoin);
 			}
 
 			System.out.println("ok!");
 			Thread.sleep(300);
 
 			MyTransportSWTMaemo myTransportGUI = new MyTransportSWTMaemo(myTransport);
 			myTransportGUI.start();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
