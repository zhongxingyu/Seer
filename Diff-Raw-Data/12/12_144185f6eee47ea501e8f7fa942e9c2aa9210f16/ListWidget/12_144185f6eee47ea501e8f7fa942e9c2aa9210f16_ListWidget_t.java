 package me.deal.client.view.main;
 
 import java.util.ArrayList;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.events.DealsEventHandler;
 import me.deal.client.model.Deals;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.client.servlets.DirectionsServiceAsync;
 import me.deal.shared.Deal;
 import me.deal.shared.LatLngCoor;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 
 public class ListWidget extends Composite {
 
     private static ListWidgetUiBinder uiBinder = GWT
             .create(ListWidgetUiBinder.class);
 
     interface ListWidgetUiBinder extends UiBinder<Widget, ListWidget> {
     }
 
     @UiField
     Image loadingSpinnerImage;
     @UiField
     VerticalPanel listItemContainer;
     
     private final DealServiceAsync dealService;
     private final DirectionsServiceAsync directionsService;
     private final HandlerManager eventBus;
     
     private final ScrollPanel mainScrollPanel;
     private Boolean dealsLoaded = false;
     private final Integer DEFAULT_NUM_DEALS = 10;
     private Boolean initialLoad=true;
     private boolean mapView = false;
     private ArrayList<ListItemWidget> listItems;
     private Integer usedListItemIndex;
     private Integer idIndex;
     
     public void setMapSize(boolean mapView)
     {
         this.mapView = mapView;
     }
     
     public @UiConstructor ListWidget(final ScrollPanel mainScrollPanel,
             final DealServiceAsync dealService,
             final DirectionsServiceAsync directionsService,
             final HandlerManager eventBus) {
         initWidget(uiBinder.createAndBindUi(this));
         this.mainScrollPanel = mainScrollPanel;
         this.dealService = dealService;
         this.directionsService = directionsService;
         this.eventBus = eventBus;
         initialize();
     }
     
     private String getDuplicateURL(ArrayList<Deal> deals, Deal current, Integer dealIndex)
     {
         LatLngCoor test = current.getBusinessAddress().getLatLng();
         for(int i = 0; i < dealIndex; i++)
         {
             double lat1 = test.getLatitude();
             double lat2 = deals.get(i).getBusinessAddress().getLatLng().getLatitude();
             double lon1 = test.getLongitude();
             double lon2 = deals.get(i).getBusinessAddress().getLatLng().getLongitude();
             
             if(lat1 == lat2 && lon1 == lon2)
             {    
                 return deals.get(i).getIDUrl();
             }
         }
         return "";
 
     }
     
     private void initialize() {
         /*
          * TODO: Add code to observe the DealsData model and automatically
          * update the items in the listItemContainer dynamically to reflect
          * changes in the model.
          * 
          */
         
         
         mainScrollPanel.addScrollHandler(new ScrollHandler() {
             @Override
             public void onScroll(ScrollEvent event) {
                 if(dealsLoaded) {
                     int maxPos = mainScrollPanel.getMaximumVerticalScrollPosition();
                     int currPos = mainScrollPanel.getVerticalScrollPosition();
                     
                     if(currPos > maxPos - 1000) {
                         dealsLoaded = false;
                         Deals deals = Deals.getInstance();
                         dealService.getYipitDeals(deals.getUserLocation().getLatLng(),
                         		deals.getRadius(),
                         		deals.DEFAULT_NUM_DEALS,
                         		deals.getOffset(),
                         		deals.getTags(), new AsyncCallback<ArrayList<Deal>> () {
                         	
                         	@Override
                         	public void onFailure(Throwable caught) {
                         		Window.alert("Failed to load deals.");
                         		}
                         	
                         	@Override
                         	public void onSuccess(ArrayList<Deal> result) {
                         		Deals deals = Deals.getInstance();
                         		deals.setOffset(deals.getOffset() + result.size());
                         		deals.addDeals(result);
                         		deals.setRange(deals.getInstance().getDeals().size() - 8, deals.getInstance().getDeals().size() - 1);
                         		eventBus.fireEvent(new DealsEvent());
                         		}
                         	});
                     }
                  
                 }
             }
         });
         
         
         eventBus.addHandler(DealsEvent.TYPE,
                 new DealsEventHandler() {
                 @Override
                 public void onDeals(DealsEvent event) {
                     loadingSpinnerImage.setVisible(true);                    
                     listItemContainer.setVisible(false);
                     listItemContainer.clear();
                     final ArrayList<Deal> deals = Deals.getInstance().getDeals();
                 
                     Integer loadsSinceLastReset = Deals.getInstance().getLoadsSinceLastReset();
                     Deals.getInstance().setDuplicates(0);
                     
                     //If there was a reset, remove all the current deals from the view first
                     if(loadsSinceLastReset != 0 && loadsSinceLastReset == deals.size()) {
                     	System.out.println("Widget count = " + listItemContainer.getWidgetCount());	
                     	System.out.println("After usedListItemIndex = " + usedListItemIndex);
                     	Integer numWidgets = listItemContainer.getWidgetCount();
                     	for(int i = numWidgets-1; i >= 0; i--) {
                     		listItemContainer.remove(i);
                     		usedListItemIndex--;
                     		}
                     	System.out.println("After usedListItemIndex = " + usedListItemIndex);
                     }
                    
                    System.out.println("size before "+listItems.size());
                    // handle the code for exepction throw after index 50. Its a temporary fix
                    if(listItems.size()<usedListItemIndex+deals.size())
                    {
                    for(int i = listItems.size(); i <= usedListItemIndex+deals.size(); i++) {
                		listItems.add(new ListItemWidget());
                		}
                    }
                    System.out.println("size after "+listItems.size());
                     for(int i = 0; i < loadsSinceLastReset; i++) {
                     	Integer dealIndex = deals.size() + i - loadsSinceLastReset;
                     	System.out.println("DealIndex = " + dealIndex + ", usedListItemIndex = " + usedListItemIndex);
                     	Deal currDeal = deals.get(dealIndex);
                     	ListItemWidget currListItemWidget = listItems.get(usedListItemIndex);
                     	currListItemWidget.setDeal(currDeal);
                     	listItemContainer.add(currListItemWidget);
                     	usedListItemIndex++;
                     	
                     	String url = getDuplicateURL(deals, currDeal, dealIndex);
                     	if(url != "") {
                     		System.out.println(url);
             
                             Deals.getInstance().setDuplicates(Deals.getInstance().getDuplicates() + 1);
                             currDeal.setIDUrl(url);
                             } 
                     	else if(mapView) {
                             	try{
                             		currDeal.setIDUrl("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + currDeal.getColor());
                             		System.out.println("here1");
                             		} catch(NullPointerException n) {
                             			currDeal.setIDUrl("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|FFFFFF");
                             			System.out.println("here2");
                             			}
                             }
                        else if(!mapView) {
                             	int temp = (dealIndex - Deals.getInstance().getDuplicates())%35;
                     	   		currDeal.setIDUrl("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".substring(                  
                             	temp,
                             	temp+1) + "|FE7569" );
                         }
 
                     currListItemWidget.setIcon(currDeal.getIDUrl());
                     }
                     listItemContainer.setVisible(true);
                     loadingSpinnerImage.setVisible(false);
                     dealsLoaded = true;
                    System.out.println("Current size of list items is "+listItems.size());
                     
                     if(listItems.size() < listItemContainer.getWidgetCount() + Deals.MAP_VIEW_NUM_DEALS) {
                     	for(int i = 0; i < Deals.MAP_VIEW_NUM_DEALS; i++) {
                     		listItems.add(new ListItemWidget());
                     		}
                     	}
                 }
         });
         
         Integer listItemSize = 10;
         usedListItemIndex = 0;
         idIndex = 0;
         listItems = new ArrayList<ListItemWidget>();
         for(int i = 0; i < listItemSize; i++) {
         	listItems.add(new ListItemWidget());
         	}
     }
 }
