 package nl.sense_os.testing.client.widgets;
 
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
 import com.google.gwt.user.client.ui.Image;
 
 public class UserProfileWin extends Window {
 //public class UserProfileWin extends ContentPanel {
 
 	public UserProfileWin(int width, int height, HashMap<String, String> param) {
 		setWidth(width);
 		setAutoHeight(true);
 		
 		final LayoutContainer container = new LayoutContainer();
 		HBoxLayout layout = new HBoxLayout();
 		//layout.setPadding(new Padding(2));
 		container.setLayout(layout);
		//container.set
 				
 		final Image logo = new Image("/img/avatar.jpg");
         logo.setPixelSize(96, 96);
         container.add(logo);
         
         Margins margins = new Margins(10, 0, 0, 10);
         
         final ContentPanel center = new ContentPanel();
         center.setHeaderVisible(false);
         center.setWidth(50);
         center.addText("user:<br />mobile: <br />e-mail:");
         center.setBorders(false);
         container.add(center, new HBoxLayoutData(margins));
 
 		final ContentPanel east = new ContentPanel();		
 		east.setHeaderVisible(false);
 		//east.addText("user: " + param.get("name") + "<br />mobile: 2323423<br />mail: " + param.get("name") + "@m.com");
 		east.addText(param.get("name") + "<br />2323423<br />" + param.get("name") + "@m.com");
 
 		container.add(east, new HBoxLayoutData(margins));
 		container.add(east);
 
 		setBodyStyle("background: #fff");
 		setBodyBorder(false);
 
 		//getHeader().setStyleName("x-sense-win-hdr");
 		
 		//bwrap.applyStyles("backgroundColor: white");
 		//bwrapStyle = "backgroundColor: white";
 		
 		add(container);
 	}
 	
 	public void addNotification() {
 		addListener(Events.Activate, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				Info.display("Info", "Example!!!");
 			}
 		});
 	}
 }
