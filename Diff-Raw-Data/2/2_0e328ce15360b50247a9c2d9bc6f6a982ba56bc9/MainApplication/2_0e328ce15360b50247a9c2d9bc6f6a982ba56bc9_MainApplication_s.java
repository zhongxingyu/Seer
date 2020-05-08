 package net.openrally;
 
import java.util.Map;

 import net.openrally.composite.FooterBar;
 import net.openrally.composite.HeaderBar;
 
 import com.vaadin.Application;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
 import com.vaadin.terminal.gwt.server.WebBrowser;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class MainApplication extends Application {
 	private static final long serialVersionUID = 701923553044752097L;
 
 	private Window window;
 	private VerticalLayout layout;
 	private HeaderBar headerBar;
 	private HorizontalLayout mainArea;
 	private FooterBar footerBar;
 	
 	private String userIpAddress;
 	
 	private SessionStorage sessionStorage;
 
 	@Override
 	public void init() {
 		
 		sessionStorage = new SessionStorage();
 		
 		window = new Window("Open Restaurant WebUI");
 		setMainWindow(window);
 
 		layout = new VerticalLayout();
 		layout.setSizeFull();
 
 		mainArea = new HorizontalLayout();
 		mainArea.setSizeFull();
 
 		headerBar = new HeaderBar(mainArea);
 
 		footerBar = new FooterBar();
 
 		layout.addComponent(headerBar);
 		layout.setExpandRatio(headerBar, 0);
 
 		layout.addComponent(mainArea);
 		layout.setExpandRatio(mainArea, 1);
 
 		layout.addComponent(footerBar);
 		layout.setExpandRatio(footerBar, 0);
 
 		Label mainAreaLabel = new Label("This is the main area");
 		mainArea.addComponent(mainAreaLabel);
 
 		getMainWindow().setContent(layout);
 		setTheme("restaurantwebuitheme");
 
 		WebBrowser webBrowser = ((WebApplicationContext) getContext())
 				.getBrowser();
 		String userIpAddress = webBrowser.getAddress();
 		
 		sessionStorage.setSessionValue(SessionStorage.USER_IP_ADDRESS, userIpAddress);
 		
 	}
 	
 	public SessionStorage getSessionStorage(){
 		return sessionStorage;
 	}
 	
 	public void adjustAfterLogin(){
 		headerBar.adjustAfterLogin();
 	}
 
 }
