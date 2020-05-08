 package project.client.screen;
 
 import java.util.List;
 
 import project.client.AceProject;
 import project.client.editor.AceEditorWidget;
 import project.client.login.LoginInfo;
 import project.client.points.PointUpdateService;
 import project.client.points.PointUpdateServiceAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 
 public abstract class ScreenWidget extends HorizontalPanel{
 	protected LayoutPanel mainPanel = new LayoutPanel();
 	private Label userPoints=new Label("0");
 	private VerticalPanel pointRank=new VerticalPanel();
 	private Anchor signOutLink = new Anchor("Sign Out");
 	private PointUpdateServiceAsync pointUpdater;
 	protected SubmitServiceAsync submitService;	//temporary
 	private LoginInfo loginInfo;
 	private Long points=1L;
 	private VerticalPanel verticalPanel_1;
 	public AceEditorWidget a;
 	
 	public ScreenWidget(LoginInfo loginInfo){
 		//setSize(Window.getClientHeight() + "px",Window.getClientWidth()+"px");
 		setHeight(Window.getClientHeight()+"px");
 		setWidth(Window.getClientWidth()+"px");
 		this.loginInfo=loginInfo;
 		//setSize("100%", "100%");		
 		buildButtonUI();
 		buildUI();
 		buildPointDisplays();
 		startService();
 		
 		Window.addResizeHandler(new ResizeHandler() {
 			 public void onResize(ResizeEvent event) {
 			   int height = event.getHeight();  
 			   setHeight(height + "px");
 			   int width = event.getWidth();
 			   setWidth(width + "px");
 			   System.out.println(height + ":" + width);
 			 }
 			});
 		//RootPanel.get().getElement().setAttribute("align", "center");
 		//DOM.setStyleAttribute(RootPanel.get().getElement(), "marginLeft", "auto");  //removes border
 		//DOM.setStyleAttribute(RootPanel.get().getElement(), "marginRight", "auto");  //removes border
 		//RootPanel.get().getElement().setAttribute("alig")
 		/*HorizontalPanel.ALIGN_MIDDLE;
 		HorizontalPanel.ALIGN_CENTER;*/
 		//UI();
 		System.out.println("resized to "+ (Window.getClientHeight()-1));
 	}
 	
 
 
 	private void buildUI() {
 
 								
 								verticalPanel_1 = new VerticalPanel();
 								add(verticalPanel_1);
 								verticalPanel_1.setSize("250px", "40px");
 								
 								
 								Label lab=new Label("Your current total points:");
 								verticalPanel_1.add(lab);
 								lab.setStyleName("gwt-DialogBox");
 								lab.setSize("150px", "40px");
 								
 								final VerticalPanel verticalPanel_2 = new VerticalPanel();
 								add(verticalPanel_2);
 								verticalPanel_2.setSize("750", "750");
 								
 								Label lblCrowdcoding = new Label("Crowd Coding");
 								verticalPanel_2.add(lblCrowdcoding);
 								lblCrowdcoding.setStyleName("gwt-Title");
 								lblCrowdcoding.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 								verticalPanel_2.add(mainPanel);
 								mainPanel.setSize("750px", "650px");
 								
 								VerticalPanel verticalPanel = new VerticalPanel();
 								verticalPanel_2.add(verticalPanel);
 								verticalPanel.setSize("750px", "80px");
 								
 								HorizontalPanel horizontalPanel = new HorizontalPanel();
 								verticalPanel.add(horizontalPanel);
 								horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 								horizontalPanel.setSize("750px", "40px");
 								Button button = new Button("Submit");
 								button.addClickHandler(new ClickHandler() {
 									@Override
 									public void onClick(ClickEvent event) {
 										try {
 											callSubmitService();
 											callRankUpdateService();
 											callPointUpdateService();
 											AceProject.submit();
 										}
 										catch (Exception e) {
 											System.out.println(e.getMessage());
 										}
 									}
 								});
 								
 											horizontalPanel.add(button);
 											button.setWidth("100px");
 											
 											HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
 											verticalPanel.add(horizontalPanel_1);
 											horizontalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 											horizontalPanel_1.setSpacing(5);
 											horizontalPanel_1.setSize("750px", "40px");
 											
 													
 													
 													Label label = new Label("Editor by daveho@Github");
 													horizontalPanel_1.add(label);
 													label.setSize("180px", "23px");
 													
 													Label lblPreferencesWillGo = new Label("Preferences will go here");
 													horizontalPanel_1.add(lblPreferencesWillGo);
 													lblPreferencesWillGo.setSize("170px", "20px");
 													horizontalPanel_1.add(signOutLink);
 													
 													//Current user
 													Label lblYouAreSigned = new Label("You are signed in as "+loginInfo.getNickname());
 													horizontalPanel_1.add(lblYouAreSigned);
 													
 													Window.addResizeHandler(new ResizeHandler() {
 														 public void onResize(ResizeEvent event) {  
 														  int width = event.getWidth();
														  int height = event.getHeight();
 														   verticalPanel_1.setWidth(width/5 + "px");  //makes vertpan1 20% of total screen real estate
														   verticalPanel_1.setHeight(height/5 + "px");
 														   System.out.println("buildUI width is: "+ width);
 														 }
 														});
 													setInitialSize(); //causes window to center without resizing
 													  
 	}
 
 	private void setInitialSize() {
 		setHeight(Window.getClientHeight()+"px");
 		setWidth(Window.getClientWidth()+"px");
 		verticalPanel_1.setWidth(Window.getClientWidth()/5 + "px");
 		System.out.println("Client width is: "+ Window.getClientWidth());
 		Window.resizeTo(Window.getClientWidth()-1,Window.getClientHeight()-1);
 		System.out.println("initial resize");
 		
 		
 	}
 
 
 
 	private void startService(){
 
 		
 		
 		if (submitService == null)
 			submitService = (SubmitServiceAsync) GWT
 					.create(SubmitService.class);
 		
 		submitService.instantiate(new AsyncCallback(){
 			public void onFailure(Throwable t){
 				t.printStackTrace();
 			}
 			public void onSuccess(Object o){
 				
 			}
 		});
 		
 		if(pointUpdater==null)
 			pointUpdater=(PointUpdateServiceAsync) GWT
 					.create(PointUpdateService.class);
 		callRankUpdateService();
 		callPointUpdateService();
 		// Set up sign out hyperlink.
 		signOutLink.setHref(loginInfo.getLogoutUrl());
 	}
 	
 	private void buildPointDisplays(){  //displays player points
 		pointRank.setStyleName("gwt-DialogBox");
 		if(pointRank!=null)
 			add(pointRank);
 		pointRank.setSize("200px", "28px");
 		userPoints.setStyleName("gwt-DialogBox");
 		
 		verticalPanel_1.add(userPoints);
 		userPoints.setSize("150px", "20px");
 		
 	}
 	
 	private void callRankUpdateService(){  //leaderboard update
 		pointUpdater.updatedList( new AsyncCallback<List<String>>(){
 			public void onFailure(Throwable caught){
 			}
 			
 			public void onSuccess(List<String> result){
 				pointRank.clear();
 				for(int x=0; x<result.size(); x++)
 					pointRank.add(new Label(result.get(x)));
 			}
 		});
 	}
 	
 	private void callPointUpdateService(){
 		pointUpdater.updatedPoints(new AsyncCallback<Long>(){
 			public void onFailure(Throwable caught){
 			}
 			
 			public void onSuccess(Long result){
 				userPoints.setText(result.toString());
 			}
 		});
 	}
 	// Method used to call service
 	public void callSubmitService() {
 		submitService.sendCode("Success", points, new AsyncCallback<String>() {
 			public void onFailure(Throwable caught) {
 				Window.alert("Failure");
 			}
 
 			public void onSuccess(String result) {
 				//Window.alert("Success");
 			}
 		});
 	}
 	
 	private void buildButtonUI(){
 	}
 	
 
 	public abstract void UI();
 	
 	public abstract void submit();
 }
