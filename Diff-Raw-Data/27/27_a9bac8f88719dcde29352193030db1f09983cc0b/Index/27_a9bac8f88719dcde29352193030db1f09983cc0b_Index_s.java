 package org.iskconsv.client;
 
 import org.iskconsv.client.command.FrameCommand;
 import org.iskconsv.client.command.HistoryCommand;
 import org.iskconsv.client.command.PopupCommand;
 import org.iskconsv.client.command.WidgetCommand;
 import org.iskconsv.client.resources.Resources;
 import org.iskconsv.client.view.Donate;
 import org.iskconsv.client.view.Home;
 import org.iskconsv.client.view.LocationView;
 import org.iskconsv.client.view.StaticView;
 import org.iskconsv.client.view.SubscribePopup;
 import org.iskconsv.client.widget.IFrame;
 import org.iskconsv.client.widget.NavigationBar;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 public class Index implements EntryPoint
 {
 	private static IndexUiBinder uiBinder = GWT.create(IndexUiBinder.class);
 
 	interface IndexUiBinder extends UiBinder<DockLayoutPanel, Index>
 	{
 	}
 
 	@UiField
 	DockLayoutPanel layoutPanel;
 
 	@UiField
 	NavigationBar topNavigationBar;
 
 	@UiField
 	MenuBar menuBar;
 
 	@UiField
 	FlowPanel contentPanel;
 
 	@UiField
 	HTMLPanel header;
 
 	@UiField
 	Resources resources;
 
 	private static final String calendarURL = "http://www.google.com/calendar/embed?showTitle=0&showNav=0&showTabs=0&showCalendars=0&mode=AGENDA&height=600&wkst=2&bgcolor=%23FFFFFF&src=iskconsv.org_rbojmj93jcrbo0d4t6m17h54c0%40group.calendar.google.com&color=%23BE6D00&ctz=America%2FLos_Angeles";
 
	private static final String videoURL = "http://www.justin.tv/widgets/live_embed_player.swf?channel=iskconsv";
 
 	private static final String spyURL = "http://spy.appspot.com/find/iskcon?full=1&latest=25";
 
 	private static final String audioLecturesURL = "http://audio.iskcondesiretree.info/06_-_More/08_-_ISKCON_Silicon_Valley-ISV/ISV.php";
 
 	private static final String donorListURL = "http://spreadsheets.google.com/pub?key=0AntwXKY6rGRmdHJGNnU0QmRYcGNEcFU2UGZUQXhfVWc&hl=en&output=html&widget=true";
 
 	private static final String fy09AnnualStatementURL = "https://spreadsheets.google.com/pub?key=0AntwXKY6rGRmdFp1NzcxQ2w3cUFkTHhlUGZLQ0UxZ2c&output=html&widget=true";
 
 	enum Token
 	{
 		calendar, video, spy, donate, contact, iskcon, location, audioLectures, founder, motelSankirtan, donorList, fy09AnnualStatement
 	}
 
 	@Override
 	public void onModuleLoad()
 	{
 		Controller controller = Controller.INSTANCE;
 
 		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));
 		RootLayoutPanel.get().addStyleName(resources.style().rootPanel());
 
 		resources.style().ensureInjected();
 
 		Window.setMargin("0px");
 
 		IFrame.setStylePrimaryName(resources.style().iframe());
 
 		header.getElement().getStyle().setBackgroundImage("url(" + resources.logo().getURL() + ")");
 
 		controller.setTargetPanel(contentPanel);
 		History.addValueChangeHandler(controller);
 
 		controller.addCommandMapItem("", new WidgetCommand(new Home(), ""));
 		menuBar.addItem("Home", new HistoryCommand(""));
 
 		MenuBar aboutMenu = new MenuBar(true);
 		menuBar.addItem("About", aboutMenu);
 
 		controller.addCommandMapItem(Token.iskcon, new WidgetCommand(new StaticView(resources.iskcon()), Token.iskcon));
 		aboutMenu.addItem("ISKCON", new HistoryCommand(Token.iskcon));
 
 		controller.addCommandMapItem(Token.founder, new WidgetCommand(new StaticView(resources.founder()),
 				Token.founder));
 		aboutMenu.addItem("Founder", new HistoryCommand(Token.founder));
 
 		controller.addCommandMapItem(Token.motelSankirtan, new WidgetCommand(
 				new StaticView(resources.motelSankirtan()), Token.motelSankirtan));
 		aboutMenu.addItem("Motel Sankirtan", new HistoryCommand(Token.motelSankirtan));
 
 		MenuBar experienceMenu = new MenuBar(true);
 		menuBar.addItem("Experience", experienceMenu);
 
 		controller.addCommandMapItem(Token.calendar, new FrameCommand(calendarURL));
 		experienceMenu.addItem("Event Calendar", new HistoryCommand(Token.calendar));
 
		controller.addCommandMapItem(Token.video, new FrameCommand(videoURL));
 		experienceMenu.addItem("Live Video", new HistoryCommand(Token.video));
 
 		controller.addCommandMapItem(Token.audioLectures, new FrameCommand(audioLecturesURL));
 		experienceMenu.addItem("Audio Lectures", new HistoryCommand(Token.audioLectures));
 
 		controller.addCommandMapItem(Token.spy, new FrameCommand(spyURL));
 		experienceMenu.addItem("Live Stream", new HistoryCommand(Token.spy));
 
 		controller.addCommandMapItem(Token.donorList, new FrameCommand(donorListURL));
 		controller.addCommandMapItem(Token.fy09AnnualStatement, new FrameCommand(fy09AnnualStatementURL));
 
 		controller.addCommandMapItem(Token.donate, new WidgetCommand(new Donate(resources), Token.donate));
 		menuBar.addItem("Donate", new HistoryCommand(Token.donate));
 
 		menuBar.addItem("Subscribe", new PopupCommand(new SubscribePopup()));
 
 		controller.addCommandMapItem(Token.contact, new WidgetCommand(new StaticView(resources.contact()),
 				Token.contact));
 		menuBar.addItem("Contact", new HistoryCommand(Token.contact));
 
 		controller.addCommandMapItem(Token.location, new WidgetCommand(new LocationView(), Token.location));
 		menuBar.addItem("Location", new HistoryCommand(Token.location));
 
 		// if (Cookies.getCookie("username") == null)
 		// {
 		// Anchor signInAnchor = topNavigationBar.addAnchor("Sign In", "#");
 		// signInAnchor.addClickHandler(new CommandClickHandler(new PopupCommand(new
 		// LoginPopup())));
 		// }
 		// else
 		// {
 		// topNavigationBar.addHTML(Cookies.getCookie("username"));
 		// topNavigationBar.addAnchor("Sign Out", "/auth/logout/");
 		// }
 
 		History.fireCurrentHistoryState();
 	}
 }
