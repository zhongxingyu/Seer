 package at.fakeroot.sepm.client;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.client.serialize.ClientGeoObject;
 import at.fakeroot.sepm.shared.client.serialize.ObjectDetails;
 import at.fakeroot.sepm.client.serialize.ObjectSearchService;
 import at.fakeroot.sepm.client.serialize.ObjectSearchServiceAsync;
 import at.fakeroot.sepm.shared.client.serialize.SearchResult;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Manage all requests between the Objects and the server.
  * @author JB
  *
  */
 public class GeoManager implements IGeoManager{
 	
 	private final ObjectSearchServiceAsync objectSearch = GWT.create(ObjectSearchService.class);
 	private int XOFFSET=5;
 	private int YOFFSET=5;
 	private Image logo = new Image("images/design/logo_no_shaddow.png");
 	private SearchBox searchBox;
 	private ResultInfoBox resultBox;
 	private TagCloud tagCloud;
 	private GeoMap geoMap;
 	
 	private String myWhat="";
 	private BoundingBox myBound;
 	 
 	public GeoManager() {
 		this.geoMap=new GeoMap(this);
 		this.searchBox=new SearchBox(this);
 		this.tagCloud = new TagCloud(this);
 		this.resultBox = new ResultInfoBox();		
 		
 	}
 	
 	/**
 	 * Draws the GUI after creating the object. (Has to be run after inserted to container)
 	 */
 	public void drawGUI(){
 		//Logo
 		PopupPanel logoPop = new PopupPanel(false);
 		logo.setPixelSize(190, 40);
 		logoPop.setWidget(this.logo);
 		logoPop.show();
 		logoPop.setPopupPosition(XOFFSET, YOFFSET);
 		
 		//SearchBox
 		PopupPanel searchPop = new PopupPanel(false);
 		searchPop.setWidget(this.searchBox);
 		searchPop.setPopupPosition(XOFFSET, YOFFSET+60);
 		searchPop.show();		
 		
 		//ResultBox
 		PopupPanel infoPop = new PopupPanel(false);
 		infoPop.setWidget(resultBox);
 		infoPop.setPopupPosition(XOFFSET, YOFFSET+130);
 		infoPop.show();
 		
 		//TagCloude
 		PopupPanel tagPop = new PopupPanel(false);
 		tagPop.setWidget(tagCloud);
 		tagPop.setPopupPosition(5, 230);
 		tagPop.show();
 		
 		
 		
 		//HistroyListener
 		History.addValueChangeHandler(new ValueChangeHandler<String>(){
 			String oldToken="";
 			public void onValueChange(ValueChangeEvent<String> event) {
 				System.out.println("histroy: "+event.getValue());
 				if(!oldToken.equals(event.getValue()))
 				{
 					String[] tokens=event.getValue().split("&");
 					for(int i=0;i<tokens.length;i++){
 						if(tokens[i].split("=")[0].equals("pos")){
 							System.out.println("pos");
 							//String[] pos=tokens[i].split("=")[1].split(";");
 							//geoMap.setCenter(Double.parseDouble(pos[1]), Double.parseDouble(pos[0]));
 						}else if(tokens[i].split("=")[0].equals("q")){
 							String[] var = tokens[i].split("=");
 							if (var.length > 1) searchBox.setWhat(var[1]);
 							System.out.println("q");
 						}
 					}
 					
 					
 				}
 				oldToken=event.getValue();
 			}
 		});
 		
 		History.fireCurrentHistoryState();
 	}
 	
 	/**
 	 * Adds a SearchTag to the current search, and searchbox
 	 * @param tag 
 	 */
 	public void addSearchTag(String tag) {
 		if(searchBox.getWhat().equals(""))
 			searchBox.setWhat(tag);
 		else searchBox.setWhat(searchBox.getWhat()+" "+tag);
 		
 	}
 
 	/**
 	 * Starts a standard search
 	 * @param where eg.: Linz, Wien
 	 * @param what eg.: kirche
 	 */
 	public void search(String where, String what) {
 		myWhat=what;
 		geoMap.search(where);
 		search(myWhat);
 	}
 
 	/**
 	 * Starts a search with available BoundingBox 
 	 * @param what eg.: kirche
 	 */
 	public void search(String what) {
 		History.newItem("q="+what.trim());
 		objectSearch.search(myBound, what.trim(), new AsyncCallback<SearchResult>()
 				{
 					public void onFailure(Throwable e) {
 						showErrorMessage(e.getMessage());
 					}
 
 					public void onSuccess(SearchResult result) {
 						geoMap.setPins(result.getResults());
 						tagCloud.refresh(result.getResults().iterator());
 						resultBox.refresh(result.getResults().size(), result.getResultCount());
 						if (result.hasError()) {
 							showErrorMessage(result.getErrorMessage());
 						}
 					}
 					
 				});
 		
 	}
 
 	/**
 	 * Sets the rectangle where the search engine will search.
 	 * @param box  
 	 */
 	public void setBoundingBox(BoundingBox box) {
 		myBound=box;
 		search(myWhat);
 	}
 
 	/**
 	 * Open the DetailView of an ClientGeoObject
 	 * @param geoObject 
 	 */
 	public void showDetailView(ClientGeoObject geoObject) {
 		System.out.println("showDetailView");
 		
 		final DetailView waitingDeVi = geoMap.createDetailView(geoObject);
 		
 		objectSearch.getDetailHTML(10, new AsyncCallback<ObjectDetails>(){
 			public void onFailure(Throwable arg0) {
 				System.err.println( "" + arg0.getMessage() +"  error");	
 			}
 
 			public void onSuccess(ObjectDetails result) {
 				waitingDeVi.setDetail(result.getHTMLString());				
 			}
 			
 		});
 		
 		
 		///TODO benotigt ObjectDetail		
 		//detailView.setDetail(....);
 		/*
 		final String wasSollISagen="testWaitsuper";
 		
 		
 		Timer t = new Timer(){
 			public void run() {
 				waitingDeVi.setDetail(wasSollISagen+": "+Math.random());					
 			}				
 		};
 		t.schedule(1000);
 		*/
 	}
 	
 	/**
 	 * Returns a MapWidget
 	 * @return
 	 */
 	public GeoMap getGeoMap(){
 		return geoMap;
 	}
 
 	public void showErrorMessage(String msg) {
 		final DialogBox errBox = new DialogBox(false, true);
		VerticalPanel vPanel = new VerticalPanel();
		errBox.setWidget(vPanel);
 		errBox.setText("Server Error!");
		
		vPanel.add(new Label(msg));
 		Button ok = new Button("Ok");
 		ok.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				errBox.hide();
 			}
 		});
		vPanel.add(ok);
 		errBox.center();
 	}
 	
 }
