 package team13.client;
 
 import java.util.List;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class MainPage implements EntryPoint {
 
 	/*our own instances*/
 	public static MainPage currentPage;
 	
 	private Button authenticButton = new Button("Login");
 	private Button addButton = new Button("<");
 	private Button removeButton = new Button(">");
 	private Button trainButton = new Button("Start");
 	private Label errorMsgLabel = new Label();
 	private HTML hintHTML = new HTML();
 	private Image runningBar = new Image("images/running.gif");
 	private CellList<FBUser> circleCellList = new CellList<FBUser>(new FBUserCell());
 	private CellList<FBUser> friendsCellList = new CellList<FBUser>(new FBUserCell());
 	private FlexTable topTable = new FlexTable();
 	private FlexTable statusTable = new FlexTable();
 	private FlexTable twoListTable = new FlexTable();
 	private FlexTable buttonTable = new FlexTable();
 	private ScrollPanel circlePanel = new ScrollPanel();
 	private ScrollPanel friendsPanel = new ScrollPanel();
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private ListDataProvider<FBUser> circleProvider = new ListDataProvider<FBUser>();
 	private ListDataProvider<FBUser> friendsProvider = new ListDataProvider<FBUser>();
 	private SingleSelectionModel<FBUser> circleSelectionModel = new SingleSelectionModel<FBUser>();
 	private SingleSelectionModel<FBUser> friendsSelectionModel = new SingleSelectionModel<FBUser>();
 	
 	private int lastScrollPos;
 	private static final int DEFAULT_INCREMENT = 20; 
 	
 	private FBFetcher fbFetcher = new FBFetcher();
 	private BPRTester bprTester = new BPRTester();
 	private boolean loggedIn;
 	private boolean isTraining = false;
 	
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		MainPage.currentPage = this;
 		
 		if(Window.Location.getHash().length() == 0){
 			loggedIn = false;
 		}else{
 			loggedIn = true;
 		}
 		
 		initUI();
 		
 		if(loggedIn){
 			setStatus("fetching JSON...", false);
 			fbFetcher.fetchNeededJSON();
 		}
 		
 	}
 	
 	public void checkJSON(){
 		if(fbFetcher.isFetchFinished()){
 			setStatus("initializing BPR...", false);
 			bprTester.init();
 		}
 	}
 	
 	private void initUI(){
 		if(!loggedIn){
 			hintHTML.setHTML("Please login as your Facebook account &rarr; ");
 			trainButton.setVisible(false);
 			statusTable.addStyleName("hiddenWidget");
 		}else{
 			hintHTML.setHTML("Run BPR training (suggested iterations: 250000) &rarr; ");
 			hintHTML.setVisible(false);
 			trainButton.setVisible(false);
 			authenticButton.setText("Logout");
 		}
 		
 		errorMsgLabel.setVisible(false);
 		errorMsgLabel.addStyleName("errorMessage");
 		
 		addButton.setEnabled(false);
 		removeButton.setEnabled(false);
 		
 		authenticButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				if(!loggedIn){
 					fbFetcher.loginFB();
 				}else{
 					fbFetcher.logoutFB();
 				}
 			}
 		});
 		
 		trainButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				if(isTraining == false){
 					isTraining = true;
 					trainButton.setText("Stop");
 					bprTester.startTraining();
 				}else{
 					isTraining = false;
 					trainButton.setText("Start");
 					bprTester.stopTraining();
 				}
 			}
 		});
 		
 		addButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
				bprTester.addSelectedAndPredict(friendsSelectionModel.getSelectedObject());
 			}
 		});
 		
 		removeButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
				bprTester.removeSelectedAndPredict(circleSelectionModel.getSelectedObject());
 			}
 		});
 		
 		circleCellList.setSelectionModel(circleSelectionModel);
 		friendsCellList.setSelectionModel(friendsSelectionModel);
 		
 		circleProvider.addDataDisplay(circleCellList);
 		friendsProvider.addDataDisplay(friendsCellList);
 
 		topTable.setWidget(0, 0, hintHTML);
 		topTable.setWidget(0, 1, trainButton);
 		topTable.setWidget(0, 2, authenticButton);
 		topTable.addStyleName("topTable");
 		
 		circlePanel.add(circleCellList);
 		circlePanel.addStyleName("scrollPanel");
 		
 		friendsPanel.add(friendsCellList);
 		//let cellList enlarge when scrolling to bottom
 		friendsPanel.addScrollHandler(new ScrollHandler(){
 			@Override
 			public void onScroll(ScrollEvent event) {
 				// If scrolling up, ignore the event.
 		        int oldScrollPos = lastScrollPos;
 		        lastScrollPos = friendsPanel.getVerticalScrollPosition();
 		        if (oldScrollPos >= lastScrollPos) {
 		          return;
 		        }
 
 		        int maxScrollTop = friendsPanel.getWidget().getOffsetHeight()
 		            - friendsPanel.getOffsetHeight();
 		        if (lastScrollPos >= maxScrollTop) {
 		          // We are near the end, so increase the page size.
 		          int newPageSize = Math.min(
 		        		  friendsCellList.getVisibleRange().getLength() + DEFAULT_INCREMENT,
 		        		  friendsCellList.getRowCount());
 		          friendsCellList.setVisibleRange(0, newPageSize);
 		        }
 			}
 		});
 		friendsPanel.addStyleName("scrollPanel");
 		
 		buttonTable.setWidget(0, 0, addButton);
 		buttonTable.setWidget(1, 0, removeButton);
 		
 		twoListTable.setText(0, 0, "Circle");
 		twoListTable.setText(0, 2, "Suggested Friends");
 		twoListTable.setWidget(1, 0, circlePanel);
 		twoListTable.setWidget(1, 1, buttonTable);
 		twoListTable.setWidget(1, 2, friendsPanel);
 		
 		statusTable.setWidget(1, 0, runningBar);
 		statusTable.addStyleName("statusTable");
 		statusTable.getCellFormatter().addStyleName(0, 0, "centerAlign");
 		statusTable.getCellFormatter().addStyleName(1, 0, "centerAlign");
 		//this function is not working, may be a bug.
 		//statusTable.getColumnFormatter().setStyleName(0, "centerAlign");
 		
 		mainPanel.add(errorMsgLabel);
 		mainPanel.add(topTable);
 		mainPanel.add(statusTable);
 		mainPanel.add(twoListTable);
 		mainPanel.addStyleName("mainPanel");
 		
 		RootPanel.get("main-content").add(mainPanel);
 	}
 	
 	public void setStatus(String text, boolean hideRunningBar){
 		statusTable.setText(0, 0, text);
 		if(hideRunningBar){
 			statusTable.getCellFormatter().addStyleName(1, 0, "hiddenWidget");
 		}else{
 			statusTable.getCellFormatter().removeStyleName(1, 0, "hiddenWidget");
 		}
 	}
 	
 	public void setAddRemoveButtonEnabled(boolean enabled){
 		addButton.setEnabled(enabled);
 		removeButton.setEnabled(enabled);
 	}
 	
 	public void enableTraining(){
 		hintHTML.setVisible(true);
 		trainButton.setVisible(true);
 	}
 	
 	public void setTrainButtonEnabled(boolean enabled){
 		trainButton.setEnabled(enabled);
 	}
 	
 	public void resetFriendsCellListStatus(){
 		friendsCellList.setPageSize(25);
 		friendsCellList.setPageStart(0);
 		friendsPanel.setVerticalScrollPosition(0);
 	}
 	
 	public void displayError(String error){
 		errorMsgLabel.setText("Error: " + error);
 		errorMsgLabel.setVisible(true);
 	}
 	
 	public List<FBUser> getCircleList(){
 		return circleProvider.getList();
 	}
 	
 	public List<FBUser> getFriendsList(){
 		return friendsProvider.getList();
 	}
 
 }
