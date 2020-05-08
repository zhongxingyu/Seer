 package com.google.gwt.stockwatcher.client.activity;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.stockwatcher.client.ClientFactory;
 import com.google.gwt.stockwatcher.client.event.BuyStockEvent;
 import com.google.gwt.stockwatcher.client.event.BuyStockEventHandler;
 import com.google.gwt.stockwatcher.client.place.BuyStockPlace;
 import com.google.gwt.stockwatcher.client.place.StockWatcherPlace;
 import com.google.gwt.stockwatcher.client.ui.StockWatcherView;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 
 /**
  *
  */
 public class StockWatcherActivity extends PlaceActivity<StockWatcherPlace> {
 
     public StockWatcherActivity(ClientFactory clientFactory, StockWatcherPlace place) {
         super(clientFactory, place);
     }
 
     @Override
     public void start(AcceptsOneWidget panel, EventBus eventBus) {
         final StockWatcherView stockWatcherView = getClientFactory().getView(StockWatcherView.class);
 
         //设置所有的事件，只有需要View和Activity交互的时候才设置 Event, 页面能自处理的页面事件，再View上自行完成
         getClientFactory().getEventBus().addHandler(BuyStockEvent.TYPE, new BuyStockEventHandler() {
             @Override
             public void onBuyStock(BuyStockEvent event) {
                 BuyStockPlace buyStockPlace = new BuyStockPlace(event.getStock().getCode());
                 buyStockPlace.setStock(event.getStock());
                 toPlace(buyStockPlace);
             }
         });
         // init views
         //stockWatcherView.addStock(new Stock());
         panel.setWidget(stockWatcherView.asWidget());
 
         //TODO: Multi Displays, Create other ActivityManager for other Display Area
         getClientFactory().getStatusActivityManager().setDisplay(stockWatcherView.getStatusPanel());
     }
 
     @Override
     public String mayStop() {
        // setDisplay null, will remove handlers from eventBus
         getClientFactory().getStatusActivityManager().setDisplay(null);
         return super.mayStop();
     }
 }
