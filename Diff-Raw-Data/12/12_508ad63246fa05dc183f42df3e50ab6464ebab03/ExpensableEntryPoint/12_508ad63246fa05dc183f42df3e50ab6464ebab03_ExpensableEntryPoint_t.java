 package expensable.client;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceChangeEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 import expensable.client.mvp.AppActivityMapper;
 import expensable.client.mvp.AppPlaceHistoryMapper;
 import expensable.client.place.DashboardPlace;
 import expensable.client.place.ExpenseReportsPlace;
 import expensable.client.view.MainLayout.Tab;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class ExpensableEntryPoint implements EntryPoint {
 
   private static final Place defaultPlace = new DashboardPlace("id:123");
 
    /**
    * This is the entry point method.
    */
   @Override
   public void onModuleLoad() {
     // Create ClientFactory using deferred binding so we can replace with different
     // impls in gwt.xml
     ClientFactory clientFactory = GWT.create(ClientFactory.class);
     EventBus eventBus = clientFactory.getEventBus();
     PlaceController placeController = clientFactory.getPlaceController();
 
     // Start ActivityManager for the main widget with our ActivityMapper
     ActivityMapper activityMapper = new AppActivityMapper(clientFactory);
     ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
     activityManager.setDisplay(clientFactory.getMainLayout());
 
     // Start PlaceHistoryHandler with our PlaceHistoryMapper
     AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
     PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
     historyHandler.register(placeController, eventBus, defaultPlace);
 
     RootLayoutPanel.get().add(clientFactory.getMainLayout());
     // Goes to place represented on URL or default place
     historyHandler.handleCurrentHistory();
 
    ChangeSelectedTabHandler tabHandler = new ChangeSelectedTabHandler(clientFactory);
    eventBus.addHandler(PlaceChangeEvent.TYPE, tabHandler);
    tabHandler.selectTab(placeController.getWhere());
   }
 
   static class ChangeSelectedTabHandler implements PlaceChangeEvent.Handler {
 
     private ClientFactory clientFactory;
 
     public ChangeSelectedTabHandler(ClientFactory clientFactory) {
       this.clientFactory = clientFactory;
     }
 
     @Override
     public void onPlaceChange(PlaceChangeEvent event) {
      selectTab(event.getNewPlace());
    }

    public void selectTab(Place place) {
       if (place instanceof DashboardPlace) {
         clientFactory.getMainLayout().selectTab(Tab.DASHBOARD);
       } else if (place instanceof ExpenseReportsPlace) {
         clientFactory.getMainLayout().selectTab(Tab.EXPENSE_REPORTS);
       }
     }
   }
 }
