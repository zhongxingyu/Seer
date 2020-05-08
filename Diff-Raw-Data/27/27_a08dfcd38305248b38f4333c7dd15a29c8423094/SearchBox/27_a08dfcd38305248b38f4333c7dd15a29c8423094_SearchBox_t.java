 package at.fakeroot.sepm.client;
 
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 
 /**
  * Class representing the Search Input Box.
  * @author AC
  */
 public class SearchBox extends Composite{
 
 	private HorizontalPanel hPanel= new HorizontalPanel();
 	private FlowPanel fPanel = new FlowPanel();
 	private IGeoManager geoManager=null;
 	private TextBox where=new TextBox();
 	private TextBox what=new TextBox();
 	private Button searchButton = new Button("Search");
	private boolean whatActive = false, whereActive = false;
 
 	private final String whereStyle = "searchWhere";
 	private final String whatStyle = "searchWhat";
 	private final String emptyWhereStyle = "searchWhere emptyWhere";
 	private final String emptyWhatStyle = "searchWhat emptyWhat";
 
 	/**
 	 * Constructor
 	 * @param gm a IGeoManager instance
 	 * */
 	public SearchBox(IGeoManager gm){
 		hPanel.getElement().setId("searchBox");
 		initWidget(hPanel);
 		geoManager=gm;		
 
 		where.setTitle("Where to point the map at");
 		where.setStyleName(emptyWhereStyle);
 		where.addFocusHandler(new FocusHandler(){
 			public void onFocus(FocusEvent fe) {
				where.setStyleName(whereStyle);
				whereActive = true;
 			}
 		});
 		where.addBlurHandler(new BlurHandler(){
 			public void onBlur(BlurEvent be) {
 				if(where.getText().trim().length() == 0){
 					where.setStyleName(emptyWhereStyle);
 				}
				whereActive = false;
 			}
 		});
 		where.addKeyPressHandler(new KeyPressHandler(){
 			public void onKeyPress(KeyPressEvent kpe) {
 				if(kpe.getCharCode() == KeyCodes.KEY_ENTER){
 					performSearch();
 				}
 			}
 		});		
 
 
 		what.setTitle("What to search for");
 		what.setStyleName(emptyWhatStyle);
 		what.addFocusHandler(new FocusHandler(){
 			public void onFocus(FocusEvent fe) {
 				what.setStyleName(whatStyle);
				whatActive = true;
 			}
 		});
 		what.addBlurHandler(new BlurHandler(){
 			public void onBlur(BlurEvent be) {
 				if(what.getText().trim().length() == 0){
 					what.setStyleName(emptyWhatStyle);
 				}
				whatActive = false;
 			}
 		});		
 		what.addKeyPressHandler(new KeyPressHandler(){
 			public void onKeyPress(KeyPressEvent kpe) {
 				if(kpe.getCharCode() == KeyCodes.KEY_ENTER)
 					performSearch();
 			}
 		});
 
 		searchButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent ce) {
 				performSearch();
 			}
 		});
 
 
 		where.setWidth("160px");
 		what.setWidth("160px");
 		searchButton.setSize("80px","54px");
 		fPanel.setHeight("55px");
 		fPanel.add(where);
 		fPanel.add(what);
 		hPanel.add(fPanel);
 		hPanel.setCellHeight(fPanel, "55px");
 		hPanel.add(searchButton);
 	}
 
 	/**
 	 * Performs the actual search.
 	 */
 	private void performSearch()
 	{
		if (getWhere().length() > 0) {
			// if there's something in the where-box, search for the location first
			geoManager.searchByLocationAndTags(getWhere());
		}
		else {
			// else just search for tags
			geoManager.searchByTags(getWhat());
		}
 	}
 
 	/**
 	 * Returns String currently in the where-TextBox
 	 * @return retString where-search String
 	 * */	
 	public String getWhere(){
 		return where.getText().trim();
 	}
 
 	/**
 	 * Returns String currently in the what-TextBox
 	 * @return retString String what-search String 
 	 * */
 	public String getWhat(){
 		return what.getText().trim();
 	}
 
 
 	/**
 	 * Sets String in the where-TextBox for the area search
 	 * @param location 
 	 * */
 	public void setWhere(String location) {
		if (location.trim().isEmpty() && !whereActive)
 			where.setStyleName(emptyWhereStyle);
 		else
 			where.setStyleName(whereStyle);
 		where.setText(location);
 	}
 
 
 	/**
 	 * Adds String to the what-TextBox for the tag search
 	 * @param tagString 
 	 * */	
 	public void setWhat(String tagString){
		if (tagString.trim().isEmpty() && !whatActive)
 			what.setStyleName(emptyWhatStyle);
 		else
 			what.setStyleName(whatStyle);
 		what.setText(tagString);
 	}
 }
