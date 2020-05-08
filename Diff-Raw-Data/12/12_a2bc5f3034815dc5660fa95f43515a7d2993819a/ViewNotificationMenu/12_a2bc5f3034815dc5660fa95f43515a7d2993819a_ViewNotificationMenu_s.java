 /**
  * 
  */
 package org.iplantc.de.client.views.panels;
 
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AnalysisPayloadEvent;
 import org.iplantc.de.client.events.AnalysisPayloadEventHandler;
 import org.iplantc.de.client.events.AnalysisUpdateEvent;
 import org.iplantc.de.client.events.DataPayloadEvent;
 import org.iplantc.de.client.events.DataPayloadEventHandler;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent;
 import org.iplantc.de.client.models.Notification;
 import org.iplantc.de.client.services.MessageServiceFacade;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotificationHelper.Category;
 import org.iplantc.de.client.utils.NotifyInfo;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * @author sriram
  * 
  */
 public class ViewNotificationMenu extends Menu {
 
     private ListStore<Notification> store;
 
     public static final String TOTAL_NOTIFI_COUNT = "totalNotificationCount";
 
     private int totalNotificationCount;
 
     private CustomListView<Notification> view;
 
     public ViewNotificationMenu() {
         setLayout(new FitLayout());
         registerEventHandlers();
         view = initList();
         LayoutContainer lc = buildPanel();
         lc.add(view);
         add(lc);
     }
 
     private LayoutContainer buildPanel() {
         LayoutContainer lc = new LayoutContainer();
         lc.setLayout(new FitLayout());
         lc.setSize(250, 270);
         lc.setBorders(false);
         return lc;
     }
 
     private CustomListView<Notification> initList() {
         store = new ListStore<Notification>();
         CustomListView<Notification> view = new CustomListView<Notification>();
 
         view.setTemplate(getTemplate());
         view.getSelectionModel().addSelectionChangedListener(
                 new SelectionChangedListener<Notification>() {
 
                     @Override
                     public void selectionChanged(SelectionChangedEvent<Notification> se) {
                         final Notification notification = se.getSelectedItem();
                         if (notification == null) {
                             return;
                         }
                         NotificationHelper.getInstance().view(notification);
                     }
                 });
         view.setItemSelector("div.search-item"); //$NON-NLS-1$
         view.setStore(store);
         view.setEmptyText(I18N.DISPLAY.noNewNotifications());
         return view;
     }
 
     private void registerEventHandlers() {
         final EventBus eventbus = EventBus.getInstance();
 
         // handle data events
         eventbus.addHandler(DataPayloadEvent.TYPE, new DataPayloadEventHandler() {
             @Override
             public void onFire(DataPayloadEvent event) {
                 addFromEventHandler(Category.DATA, I18N.DISPLAY.fileUpload(), event.getMessage(),
                         NotificationHelper.getInstance().buildDataContext(event.getPayload()),
                         event.getPayload());
                 highlightNewNotifications();
             }
         });
 
         // handle analysis events
         eventbus.addHandler(AnalysisPayloadEvent.TYPE, new AnalysisPayloadEventHandler() {
             @Override
             public void onFire(AnalysisPayloadEvent event) {
                 addFromEventHandler(Category.ANALYSIS, I18N.CONSTANT.analysis(), event.getMessage(),
                         NotificationHelper.getInstance().buildAnalysisContext(event.getPayload()),
                         event.getPayload());
                 highlightNewNotifications();
             }
         });
     }
 
     @Override
     public void showAt(int x, int y) {
         super.showAt(x, y);
         highlightNewNotifications();
         markAsSeen();
     }
 
     private void markAsSeen() {
         List<Notification> new_notifications = store.getModels();
         JSONArray arr = new JSONArray();
         int i = 0;
         if (new_notifications.size() > 0) {
             for (Notification n : new_notifications) {
                 arr.set(i++, new JSONString(n.get("id").toString()));
                 n.set(Notification.SEEN, true);
             }
 
             JSONObject obj = new JSONObject();
             obj.put("uuids", arr);
 
             MessageServiceFacade facade = new MessageServiceFacade();
             facade.markAsSeen(obj, new AsyncCallback<String>() {
 
                 @Override
                 public void onSuccess(String result) {
                     // Do nothing intentionally
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(caught);
                 }
             });
         }
 
     }
 
     /**
      * 
      * persist total notification count
      * 
      * @param total
      */
     public void setTotalNotificationCount(int total) {
         totalNotificationCount = total;
     }
 
     /**
      * get total notification count
      * 
      * @return
      */
     public int getTotalNotificationCount() {
         return totalNotificationCount;
     }
 
     private Notification addItemToStore(final Category category, final JSONObject objMessage,
             final String context, JSONObject payload) {
         Notification ret = null; // assume failure
 
         if (objMessage != null) {
             ret = new Notification(objMessage, context);
 
             Notification model = store.findModel("id", ret.get("id"));
 
             if (model == null) {
                 add(category, ret);
                 totalNotificationCount = totalNotificationCount + 1;
                 fireEvents(payload);
 
             } else {
                 ret = null;
             }
         }
 
         return ret;
     }
 
     private void fireEvents(JSONObject payload) {
         NotificationCountUpdateEvent ncue = new NotificationCountUpdateEvent(getTotalNotificationCount());
         EventBus instance = EventBus.getInstance();
         instance.fireEvent(ncue);
 
         AnalysisUpdateEvent aue = new AnalysisUpdateEvent(payload);
         instance.fireEvent(aue);
     }
 
     /**
      * reset all notification count
      * 
      */
     public void resetCount() {
         totalNotificationCount = 0;
     }
 
     private void addFromEventHandler(final Category category, final String header,
             final JSONObject objMessage, final String context, final JSONObject payload) {
         Notification notification = addItemToStore(category, objMessage, context, payload);
 
         if (notification != null) {
             NotifyInfo.display(header, notification.getMessage());
         }
     }
 
     public void add(final Category category, final Notification notification) {
         // did we get a valid notification?
         if (category != Category.ALL && notification != null) {
             notification.setCategory(category);
             store.add(notification);
         }
 
         store.sort(Notification.PROP_TIMESTAMP, SortDir.DESC);
     }
 
     private String getTemplate() {
         StringBuilder template = new StringBuilder();
         template.append("<tpl for=\".\"><div class=\"search-item\">"); //$NON-NLS-1$
         template.append("<tpl if=\"context\"> <div class='notification_context'> </tpl>");
         template.append("{message} <tpl if=\"context\"> </div> </tpl></div></tpl>");
         return template.toString();
     }
 
     private void highlightNewNotifications() {
         List<Notification> new_notifications = store.getModels();
         for (Notification n : new_notifications) {
            System.out.println("seen--->" + n.get(Notification.SEEN));
             if (n.get(Notification.SEEN) == null
                     || Boolean.parseBoolean(n.get(Notification.SEEN).toString()) == false) {
                 view.highlight(view.getStore().indexOf(n), true);
             } else {
                 view.highlight(view.getStore().indexOf(n), false);
             }
 
         }
     }
 
     private class CustomListView<M extends ModelData> extends ListView<M> {
         private String emptyText;
 
         @Override
         protected void afterRender() {
             super.afterRender();
 
             applyEmptyText();
         }
 
         @Override
         public void refresh() {
             super.refresh();
 
             applyEmptyText();
         }
 
         protected void applyEmptyText() {
             if (emptyText == null) {
                 emptyText = "&nbsp;";
             }
             if (store.getModels().size() == 0) {
                 el().setInnerHtml("<div class='x-grid-empty'>" + emptyText + "</div>");
             }
         }
 
         public void setEmptyText(String emptyText) {
             this.emptyText = emptyText;
         }
 
         @SuppressWarnings("unused")
         public String getEmptyText() {
             return emptyText;
         }
 
         public void highlight(int index, boolean highLight) {
             Element e = getElement(index);
             if (e != null) {
                fly(e).setStyleName("new_notification", highLight);
                if (highLight && GXT.isAriaEnabled()) {
                    setAriaState("aria-activedescendant", e.getId());
                 }
             }
         }
     }
 
 }
