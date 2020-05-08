 package me.deal.client.view.main;
 
 import java.util.ArrayList;
 import java.util.TimerTask;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.events.DealsEventHandler;
 import me.deal.client.events.DealsLocationEvent;
 import me.deal.client.events.DealsLocationEventHandler;
 import me.deal.client.model.Deals;
 import me.deal.client.model.DealsLocation;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.client.servlets.DirectionsServiceAsync;
 import me.deal.shared.Category;
 import me.deal.shared.Deal;
 
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
 					}
 				}
 			}
 		});
 		
 		eventBus.addHandler(DealsLocationEvent.TYPE,
 				new DealsLocationEventHandler() {
 			@Override
 			public void onDealsLocation(DealsLocationEvent event) {
 				dealService.getYipitDeals(DealsLocation.getInstance().getDealsLocation().getLatLng(),
 						DealsLocation.getInstance().DEFAULT_RADIUS, DEFAULT_NUM_DEALS, new ArrayList<Category>(),
 						new AsyncCallback<ArrayList<Deal>>() {
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Failed to load deals.");
 							}
 
 							@Override
 							public void onSuccess(ArrayList<Deal> result) {
 								Deals.getInstance().setDeals(result);
 								eventBus.fireEvent(new DealsEvent());
 							}
 				});
 			}
         });
 		
 		eventBus.addHandler(DealsEvent.TYPE,
 				new DealsEventHandler() {
 				@Override
 				public void onDeals(DealsEvent event) {
 					loadingSpinnerImage.setVisible(true);
 					listItemContainer.clear();
 					final ArrayList<Deal> deals = Deals.getInstance().getDeals();
 					final Timer dealTimer = new Timer() {
 						Integer dealIndex = 0;
 						public void run() {
 							if(dealIndex != deals.size()) {
 								System.out.println("Added " + deals.get(dealIndex).getTitle());
 								listItemContainer.add(new ListItemWidget(deals.get(dealIndex)));
 								this.schedule(1);
 								dealIndex++;
 							}
 						}
 					};
 					dealTimer.schedule(1);
 					loadingSpinnerImage.setVisible(false);
 					dealsLoaded = true;
 				}
 		});
 	}
 }
