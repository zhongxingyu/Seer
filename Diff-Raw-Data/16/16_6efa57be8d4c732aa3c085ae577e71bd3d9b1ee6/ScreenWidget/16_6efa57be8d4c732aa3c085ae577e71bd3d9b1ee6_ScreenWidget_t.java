 package project.client.screen;
 
 import java.util.List;
 
 import project.client.AceProject;
 import project.client.editor.AceEditorWidget;
 import project.client.login.LoginInfo;
 import project.client.points.PointUpdateService;
 import project.client.points.PointUpdateServiceAsync;
 import project.client.profile.ProfileWidget;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /*All the widgets extend this class.
  * This is where a lot of the magic happens.
  * All the microtask widgets are put inside the contentPanel.
  * I've been using labels as spacers.
  * The current layout is this:
  * 
  * RootPanel|->VerticalPanel|-> CrowdCoding title
  * 						    |-> hPanel|->(spacer) |->userPointsPanel|->Spacer, label, spacer, label, user points
  *                                                |->contentPanel   |->mainPanel |-> currentWidget
  *                                    |->(spacer) |->pointRankPanel |->label, leaderboard
  */
 public abstract class ScreenWidget extends VerticalPanel {
 
 	protected VerticalPanel mainPanel = new VerticalPanel();
 	private final Label userPoints = new Label("0");
 	private final VerticalPanel pointRank = new VerticalPanel();
 	private final Anchor signOutLink = new Anchor("Sign Out");
 	private PointUpdateServiceAsync pointUpdater;
 	protected SubmitServiceAsync submitService;
 	private Label lblYouAreSigned;
 	private Label lab;
 	private final Long points = 1L;
 	private VerticalPanel userPointsPanel;
 	private final HorizontalPanel hPanel = new HorizontalPanel();
 	private final Label spacer = new Label();
 	private final Label spacer1 = new Label();
 	private final Label spacer2 = new Label();
 	private final Label userPointSpacer = new Label();
 	public AceEditorWidget a;
 	protected SimplePanel instructions = new SimplePanel();
 	protected HorizontalPanel horizontalPanel;
 	protected Anchor prefs;
 	protected Button button;
 	protected HandlerRegistration remover;
 	private Label lab1;
 	protected static LoginInfo loginInfo;
 	boolean temporaryForTestingPurposes = true;
 
 	public ScreenWidget() {
 		buildButtonUI();
 		buildUI();
 		buildPointDisplays();
 		if (temporaryForTestingPurposes) { // when using GWT WindowBuilder this
 											// should be false, else true
 			startService();
 			RootPanel.get("ace").getElement().setAttribute("align", "center");
 			DOM.setStyleAttribute(RootPanel.get("ace").getElement(),
 					"marginLeft", "auto");
 			DOM.setStyleAttribute(RootPanel.get("ace").getElement(),
 					"marginRight", "auto"); // centers page
 			DOM.setStyleAttribute(instructions.getElement(), "height", "200px");
 			DOM.setStyleAttribute(instructions.getElement(), "width", "750px"); // setSize()
 																				// doesn't
 																				// work
 																				// on
 																				// firefox
 		}
 	}
 
 	// This function is messy, just read the comments at the very top for a
 	// better picture
 	private void buildUI() {
		RootPanel.get("ace").setSize(Window.getClientWidth() + "px",
				Window.getClientHeight() + "px"); // so horizontal scrollbar
													// floats at the bottom of
													// the page
 		userPointsPanel = new VerticalPanel();
 		hPanel.add(userPointsPanel);
 		userPointsPanel.setSize("250px", "40px");
 		spacer.setSize("1px", Window.getClientHeight() / 4 + "px");
 		spacer1.setSize("1px", Window.getClientHeight() / 4 + "px");
 
 		if (loginInfo != null)
 			lab = new Label(loginInfo.getNickname());
 		else
 			lab = new Label();
 
 		lab1 = new Label("Your Points:");
 		lab1.setHorizontalAlignment(ALIGN_CENTER);
 		lab1.setHeight("40px");
 
 		userPointsPanel.add(spacer);
 		userPointsPanel.add(lab);
 		userPointsPanel.add(userPointSpacer);
 		userPointsPanel.add(lab1);
 
 		lab.setHorizontalAlignment(ALIGN_CENTER);
 		lab.setStyleName("gwt-DialogBox");
 		lab.setSize("150px", "80px");
 
 		final VerticalPanel contentPanel = new VerticalPanel();
 		hPanel.add(contentPanel);
 		hPanel.setCellHorizontalAlignment(userPointsPanel, ALIGN_RIGHT);
 
 		spacer2.setSize("75px", "1px");
 		hPanel.add(spacer2);
 
 		Label lblCrowdcoding = new Label("Crowd Coding");
 		lblCrowdcoding.setHeight("30px");
 		add(lblCrowdcoding);
 		lblCrowdcoding.setStyleName("gwt-Title");
 		lblCrowdcoding
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		contentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		contentPanel.add(instructions);
 		contentPanel.add(mainPanel);
 		contentPanel.setSize("750px", "750px");
 
 		mainPanel.setSize("750px", "650px"); // panel inside content panel that
 												// holds the widgets
 
 		VerticalPanel footerPanel = new VerticalPanel();
 		contentPanel.add(footerPanel);
 		footerPanel.setSize("750px", "160px");
 
 		add(hPanel);
 
 		horizontalPanel = new HorizontalPanel();
 		footerPanel.add(horizontalPanel);
 		horizontalPanel
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		horizontalPanel.setSize("750px", "40px");
 		button = new Button("Submit");
 		remover = button.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				try {
 					callSubmitService();
 					AceProject.submit();
 				} catch (Exception e) {
 					System.out.println(e.getMessage());
 				}
 			}
 		});
 
 		horizontalPanel.add(button);
 		button.setWidth("100px");
 
 		HorizontalPanel bottomFooterPanel = new HorizontalPanel();
 		footerPanel.add(bottomFooterPanel);
 		bottomFooterPanel
 				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		bottomFooterPanel.setSpacing(5);
 		bottomFooterPanel.setSize("750px", "40px");
 
 		Label label = new Label("Editor by daveho@Github");
 		bottomFooterPanel.add(label);
 		label.setSize("180px", "23px");
 
 		bottomFooterPanel.add(signOutLink);
 		prefs = new Anchor("Set Preferences");
 		bottomFooterPanel.add(prefs);
 		final ScreenWidget t = this;
 		prefs.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent e) {
 				RootPanel.get("ace").clear();
 				RootPanel.get("ace").add(new ProfileWidget(t));
 
 			}
 		});
 
 		// Current user
 		String text;
 		if (loginInfo != null)
 			text = loginInfo.getNickname();
 		else
 			text = "null";
 		lblYouAreSigned = new Label("You are signed in as " + text);
 		bottomFooterPanel.add(lblYouAreSigned);
 	}
 
 	private void startService() {
 
 		if (submitService == null)
 			submitService = (SubmitServiceAsync) GWT
 					.create(SubmitService.class);
 
 		submitService.instantiate(new AsyncCallback() {
 			@Override
 			public void onFailure(Throwable t) {
 				t.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(Object o) {
 
 			}
 		});
 
 		if (pointUpdater == null)
 			pointUpdater = (PointUpdateServiceAsync) GWT
 					.create(PointUpdateService.class);
 		callRankUpdateService();
 		callPointUpdateService();
 		// Set up sign out hyperlink.
 		signOutLink.setHref(loginInfo.getLogoutUrl());
 	}
 
 	private void buildPointDisplays() { // displays player points
 		pointRank.setStyleName("gwt-DialogBox");
 		if (pointRank != null) {
 			Label rankLabel = new Label("High Scores");
 			rankLabel.setStyleName("Bold");
 			VerticalPanel pointRankPanel = new VerticalPanel();
 			pointRankPanel.add(spacer1);
 			if (pointRankPanel != null)
 				pointRankPanel.add(rankLabel);
 			pointRankPanel.add(pointRank);
 			hPanel.add(pointRankPanel);
 		}
 		pointRank.setSize("250px", "40px");
 		userPoints.setStyleName("gwt-DialogBox");
 
 		userPointsPanel.add(userPoints);
 		userPoints.setHorizontalAlignment(ALIGN_CENTER);
 		userPointsPanel.setCellHorizontalAlignment(userPoints, ALIGN_CENTER); // centers
 																				// text
 		userPointsPanel.setCellHorizontalAlignment(lab, ALIGN_CENTER);
 		userPointsPanel.setCellHorizontalAlignment(lab1, ALIGN_CENTER);
 		userPoints.setSize("250px", "20px");
 		userPoints.setStyleName("userPoints");
 	}
 
 	private void callRankUpdateService() { // leaderboard update
 		pointUpdater.updatedList(new AsyncCallback<List<String>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 
 			@Override
 			public void onSuccess(List<String> result) {
 				pointRank.clear();
 				for (int x = 0; x < result.size(); x++) {
 					pointRank.add(new Label(result.get(x)));
 				}
 			}
 		});
 	}
 
 	private void callPointUpdateService() {
 
 		pointUpdater.updatedPoints(new AsyncCallback<Long>() {
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 
 			@Override
 			public void onSuccess(Long result) {
 				userPoints.setText(result.toString());
 			}
 		});
 	}
 
 	// Method used to call service
 	public void callSubmitService() {
 		submitService.sendCode("Success", points, new AsyncCallback<String>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("Point update failure");
 			}
 
 			@Override
 			public void onSuccess(String result) {
 				updatePoints();
 			}
 		});
 	}
 
 	protected void updatePoints() {
 		callRankUpdateService();
 		callPointUpdateService();
 		lab.setText(loginInfo.getNickname());
 		lblYouAreSigned.setText("You are signed in as "
 				+ loginInfo.getNickname());
 	}
 
 	private void buildButtonUI() {
 	}
 
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		if (temporaryForTestingPurposes) // true if not using GWT WindowBuilder
 			updatePoints();
 	}
 
 	public abstract void UI();
 
 	public abstract void submit();
 
 	public static void setLoginInfo(LoginInfo l) {
 		loginInfo = l;
 	}
 
 	public static LoginInfo getLoginInfo() {
 		return loginInfo;
 	}
 
 }
