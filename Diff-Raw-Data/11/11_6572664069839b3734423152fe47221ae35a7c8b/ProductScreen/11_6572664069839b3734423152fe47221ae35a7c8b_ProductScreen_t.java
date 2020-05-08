 package main.java.com.owfg.facade.bb.StoreManagement.gui;
 
 import main.java.com.owfg.facade.bb.StoreManagement.app.MyApp;
 import net.rim.device.api.ui.*;
 import net.rim.device.api.ui.component.*;
 import net.rim.device.api.ui.container.*;
 
 public class ProductScreen extends MainScreen {
 	private final int resultRows = 4;
 	private final int resultCols = 2;
 	private String upc = MyApp.resultText;
 	private final int maxUPCLength = 20;
 	private final int resultBoxLength = 7;
 	private String[] stores = {"918 Fleetwood", "930 Maple Ridge"};
 	private LabelField productName;
 	private EditField boh, onOrder, min, inTransit, fcst, pack, reg, source;
 	
 	public ProductScreen() {
 		super();
		MyApp.resultText = "";
 		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);
 		
 		//Fields for a form
 		VerticalFieldManager form = new VerticalFieldManager(FIELD_HCENTER);
 		form.setMargin(10, 50, 10, 50);
 		EditField upcField = new EditField("UPC: ", upc, maxUPCLength, FIELD_LEFT);
 		form.add(upcField);
 		form.add(new ObjectChoiceField("Store: ", stores, 0));
 		vfm.add(form);
 		
 		productName = new LabelField("", LabelField.ELLIPSIS | FIELD_HCENTER);
 		vfm.add(productName);
 		
 		//Result grid
 		GridFieldManager grid = new GridFieldManager(resultRows, resultCols,
 				GridFieldManager.FIXED_SIZE | FIELD_HCENTER);
 		
 		boh = new EditField("BOH: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(boh);
 		onOrder = new EditField("On Order: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(onOrder);
 		min = new EditField("Min: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(min);
 		inTransit = new EditField("In Transit: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(inTransit);
 		fcst = new EditField("Fcst: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(fcst);
 		pack = new EditField("Pack: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(pack);
 		reg = new EditField("Reg: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(reg);
 		source = new EditField("Source: ", "", resultBoxLength, FIELD_RIGHT | READONLY);
 		grid.add(source);
 		
 		grid.setColumnPadding(100);
 		grid.setRowPadding(10);
 		vfm.add(grid);
 		add(vfm);
 	}
 	
     public MenuItem scan = new MenuItem("Scan again", 100, 1) {
 		public void run() {
 			synchronized (MyApp.getEventLock()) {
 				MyApp.app.popScreen(getScreen());
 				MyApp.app.pushScreen(MyApp.cScreen);
 			}
 			MyApp.cScreen.startThread();
 		}
 	};
 
     public MenuItem submit = new MenuItem("Submit", 100, 1) {
 		public void run() {
 			//get results from webservice
 			productName.setText("HALLS CHERRY COUGH TABS");
 			boh.setText("6");
 			onOrder.setText("1");
 			min.setText("2");
 			inTransit.setText("1");
 			fcst.setText("4.3");
 			pack.setText("6");
 			reg.setText("$4.99");
 			source.setText("DSD");
 		}
 	};
 	
 
     public MenuItem quit = new MenuItem("Exit", 100, 1) {
 		public void run() {
 			if (Dialog.ask(Dialog.D_YES_NO, "Exit?") == 4) {
 				System.exit(0);
 			}
 		}
 	};
 	
 	public void makeMenu(Menu menu, int instance) {
 		menu.add(scan);
 		menu.add(submit);
 		menu.add(quit);
 	}
 
 	public boolean onSavePrompt() {
 	    return true;
 	}
 }
