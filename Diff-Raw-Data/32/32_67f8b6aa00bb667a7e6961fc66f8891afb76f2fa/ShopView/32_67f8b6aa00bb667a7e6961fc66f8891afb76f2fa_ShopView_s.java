 package ikm.views;
 
 import ikm.Main;
 import ikm.ViewManager;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemCommandListener;
 import javax.microedition.lcdui.StringItem;
 
 public class ShopView extends Form implements CommandListener, ItemCommandListener {
 	private ViewManager viewManager;
 	private String[] line;
 	private String[] fieldNames;
 	private Command exitCommand = new Command("Back", Command.BACK, 1);
 	private Command mapCommand = new Command("  ", Command.ITEM, 1);
 	private StringItem showMapItem;
 	private Main main;
 	private int idx;
 	
 	public ShopView(String caption, ViewManager viewManager, String[] line,
 			String[] fieldNames, Main main, int idx) {
 		super(caption);
 		this.viewManager = viewManager;
 		this.line = line;
 		this.fieldNames = fieldNames;
 		this.main = main;
 		this.idx = idx;
 		
 		showMapItem = new StringItem(null, "  ", StringItem.BUTTON);
 		showMapItem.setDefaultCommand(mapCommand);
 		showMapItem.setItemCommandListener(this);
 		
 		append(new StringItem(fieldNames[1], line[2]));
 		append(showMapItem);
 		
 		addCommand(exitCommand);
 		//addCommand(mapCommand);
 		setCommandListener(this);
 	}
 	
 	public void commandAction(Command c, Displayable d) {
 		if (c == exitCommand) {
 			viewManager.goBack();
 		} else if (c == mapCommand) {
 			MapView mapView = main.getMapView();
 			mapView.centerOn(idx);
 			viewManager.showView(mapView);
 		}
 	}
 	
 	public void commandAction(Command c, Item item) {
 		if (showMapItem == item) {
 			MapView mapView = main.getMapView();
 			mapView.centerOn(idx);
 			viewManager.showView(mapView);
 		}
 	}
 }
