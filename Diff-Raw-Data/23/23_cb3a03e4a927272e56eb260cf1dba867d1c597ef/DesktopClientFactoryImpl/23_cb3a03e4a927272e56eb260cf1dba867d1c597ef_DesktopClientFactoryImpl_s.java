 package com.google.gwt.stockwatcher.client.ui.desktop;
 
 import com.google.gwt.stockwatcher.client.ClientFactory;
 import com.google.gwt.stockwatcher.client.ui.BuyStockView;
 import com.google.gwt.stockwatcher.client.ui.LogoView;
 import com.google.gwt.stockwatcher.client.ui.StatusView;
 import com.google.gwt.stockwatcher.client.ui.StockWatcherView;
 import com.google.gwt.user.client.ui.IsWidget;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 12-11-30
  * Time: 下午4:08
  * To change this template use File | Settings | File Templates.
  */
 public class DesktopClientFactoryImpl extends ClientFactory {
 
     // cache all created views
     private Map<String, IsWidget> viewMap = new HashMap<String, IsWidget>();
 
     public <T extends IsWidget> T getView(Class<T> viewInterface) {
         if(viewMap.containsKey(viewInterface.getName())) {
             return (T)viewMap.get(viewInterface.getName());
         }
 
         IsWidget view = null;
 
         if(viewInterface.equals(StockWatcherView.class)) {
             view = new StockWatcherViewImpl();
         }
         else if(viewInterface.equals(StatusView.class)) {
             view = new StatusViewImpl();
         }
         else if(viewInterface.equals(LogoView.class)) {
             view = new LogoViewImpl();
         }
         else if(viewInterface.equals(BuyStockView.class)) {
             view = new BuyStockViewImpl();
         }
         else {
 
         }
         if(view == null) {
             throw new RuntimeException("No corresponding view for view interface: " + viewInterface.getName());
         }
/*
        if(view instanceof ActivityView) {
            ((ActivityView)view).setActivity(this);
            viewMap.put(viewInterface.getName(), view);
        }
*/
         return (T)view;
     }
 }
