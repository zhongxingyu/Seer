 /**
  * 
  */
 package org.iplantc.de.client.views.panels;
 
 import java.util.List;
 
 import org.iplantc.core.client.widgets.MenuHyperlink;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.dispatchers.WindowDispatcher;
 import org.iplantc.de.client.events.DeleteNotificationsUpdateEvent;
 import org.iplantc.de.client.events.DeleteNotificationsUpdateEventHandler;
 import org.iplantc.de.client.factories.WindowConfigFactory;
 import org.iplantc.de.client.models.Notification;
 import org.iplantc.de.client.models.NotificationWindowConfig;
 import org.iplantc.de.client.services.MessageServiceFacade;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotificationHelper.Category;
 import org.iplantc.de.client.utils.NotifyInfo;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ListViewEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * @author sriram
  * 
  */
 public class ViewNotificationMenu extends Menu {
 
     private ListStore<Notification> store;
 
     private CustomListView<Notification> view;
 
     public static final int NEW_NOTIFICATIONS_LIMIT = 10;
 
     private int total_unseen;
 
     private NotificationHelper helper = NotificationHelper.getInstance();
 
     private final String linkStyle = "de_header_menu_hyperlink"; //$NON-NLS-1$
 
     private HorizontalPanel hyperlinkPanel;
 
     public ViewNotificationMenu() {
         setLayout(new FitLayout());
         initListeners();
         view = initList();
         LayoutContainer lc = buildPanel();
         hyperlinkPanel = new HorizontalPanel();
         lc.add(view);
         add(lc);
         add(hyperlinkPanel);
     }
 
     private LayoutContainer buildPanel() {
         LayoutContainer lc = new LayoutContainer();
         lc.setLayout(new FitLayout());
         lc.setSize(250, 270);
         lc.setBorders(false);
         return lc;
     }
 
     private void initListeners() {
         EventBus.getInstance().addHandler(DeleteNotificationsUpdateEvent.TYPE,
                 new DeleteNotificationsUpdateEventHandler() {
 
                     @Override
                     public void onDelete(DeleteNotificationsUpdateEvent event) {
                         for (Notification n : store.getModels()) {
                             for (Notification deleted : event.getIds()) {
                                 if (n.getId().equals(deleted.getId())) {
                                     store.remove(n);
                                 }
                             }
                         }
 
                     }
                 });
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
                         hide();
                     }
                 });
         view.setItemSelector("div.search-item"); //$NON-NLS-1$
         view.setStore(store);
         view.setEmptyText(I18N.DISPLAY.noNewNotifications());
         return view;
     }
 
     @Override
     public void showAt(int x, int y) {
         highlightNewNotifications();
        int remaining_count = total_unseen - NEW_NOTIFICATIONS_LIMIT;
         helper.markAsSeen(store.getModels());
        addNotifictionsLink(remaining_count);
         super.showAt(x, y);
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
             if (n.get(Notification.SEEN) == null
                     || Boolean.parseBoolean(n.get(Notification.SEEN).toString()) == false) {
                 view.highlight(view.getStore().indexOf(n), true);
             } else {
                 view.highlight(view.getStore().indexOf(n), false);
             }
 
         }
     }
 
     public void setUnseenCount(int count) {
         this.total_unseen = count;
     }
 
     public void fetchUnseenNotifications() {
         MessageServiceFacade facadeMessageService = new MessageServiceFacade();
         facadeMessageService.getRecentMessages(new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
 
             @Override
             public void onSuccess(String result) {
                 processMessages(result);
             }
         });
     }
 
    private void addNotifictionsLink(int remaining_count) {
         hyperlinkPanel.removeAll();
        hyperlinkPanel.add(buildNotificationHyerlink(remaining_count));
         hyperlinkPanel.layout(true);
     }
 
    private MenuHyperlink buildNotificationHyerlink(final int remaining_count) {
         String displayText;
        if (remaining_count > 0) {
            displayText = I18N.DISPLAY.newNotifications() + " (" + remaining_count + " )";
         } else {
             displayText = I18N.DISPLAY.allNotifications();
         }
         return new MenuHyperlink(displayText, linkStyle, "", new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                if (remaining_count > 0) {
                     showNotificationWindow(NotificationHelper.Category.NEW);
                 } else {
                     showNotificationWindow(NotificationHelper.Category.ALL);
                 }
                 hide();
             }
         });
     }
 
     /** Makes the notification window visible and filters by a category */
     private void showNotificationWindow(final Category category) {
         NotificationWindowConfig config = new NotificationWindowConfig();
         config.setCategory(category);
 
         // Build window config
         WindowConfigFactory configFactory = new WindowConfigFactory();
         JSONObject windowConfig = configFactory
                 .buildWindowConfig(Constants.CLIENT.myNotifyTag(), config);
         WindowDispatcher dispatcher = new WindowDispatcher(windowConfig);
         dispatcher.dispatchAction(Constants.CLIENT.myNotifyTag());
     }
 
     /**
      * Process method takes in a JSON String, breaks out the individual messages, transforms them into
      * events, finally the event is fired.
      * 
      * @param json string to be processed.
      */
     public void processMessages(final String json) {
 
         JSONObject objMessages = JSONParser.parseStrict(json).isObject();
         int size = 0;
         // cache before removing
         List<Notification> temp = store.getModels();
         store.removeAll();
         boolean displayInfo = false;
 
         if (objMessages != null) {
             JSONArray arr = objMessages.get("messages").isArray(); //$NON-NLS-1$
             if (arr != null) {
                 JSONObject objItem;
                 size = arr.size();
                 for (int i = 0; i < size; i++) {
                     if (isVisible() && !isMasked()) {
                         mask(I18N.DISPLAY.loadingMask());
                     }
                     objItem = arr.get(i).isObject();
                     if (objItem != null) {
                         Notification n = buildNotification(objItem);
                         if (n != null && !isExists(n)) {
                             store.add(n);
                             if (!isExist(temp, n)) {
                                 displayNotificationPopup(n);
                                 displayInfo = true;
                             }
 
                         }
                     }
                 }
                 if (total_unseen > NEW_NOTIFICATIONS_LIMIT && displayInfo) {
                     NotifyInfo.display(I18N.DISPLAY.newNotifications(),
                             I18N.DISPLAY.newNotificationsAlert());
                 }
                 store.sort(Notification.PROP_TIMESTAMP, SortDir.DESC);
                 highlightNewNotifications();
                 unmask();
             }
         }
 
     }
 
     private boolean isExist(List<Notification> list, Notification n) {
         for (Notification noti : list) {
             if (noti.getId().equals(n.getId())) {
                 return true;
             }
         }
 
         return false;
 
     }
 
     private Notification buildNotification(JSONObject objItem) {
         String type;
         boolean seen;
         type = JsonUtil.getString(objItem, "type"); //$NON-NLS-1$
         seen = JsonUtil.getBoolean(objItem, "seen", false);
         Notification n = helper.buildNotification(type, seen, objItem);
         return n;
     }
 
     private boolean isExists(Notification n) {
         Notification temp = store.findModel("id", n.getId());
         if (temp == null) {
             return false;
         } else {
             return true;
         }
 
     }
 
     private void displayNotificationPopup(Notification n) {
         if (!n.isSeen()) {
             if (n.getCategory().equals(Category.DATA)) {
                 NotifyInfo.display(Category.DATA.toString(), n.getMessage());
             } else if (n.getCategory().equals(Category.ANALYSIS)) {
                 NotifyInfo.display(Category.ANALYSIS.toString(), n.getMessage());
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
             if (store.getModels().size() == 0 && isRendered()) {
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
 
         @SuppressWarnings("unchecked")
         @Override
         protected void onClick(ListViewEvent<M> e) {
             super.onClick(e);
             ListViewSelectionModel<M> selectionModel = getSelectionModel();
             Notification md = (Notification)selectionModel.getSelectedItem();
             selectionModel.deselectAll();
             selectionModel.select(false, (M)md);
         }
 
         public void highlight(int index, boolean highLight) {
             Element e = getElement(index);
             if (e != null) {
                 if (highLight) {
                     fly(e).setStyleName("new_notification", highLight);
                     if (highLight && GXT.isAriaEnabled()) {
                         setAriaState("aria-activedescendant", e.getId());
                     }
                 } else {
                     fly(e).removeStyleName("new_notification");
                 }
             }
         }
     }
 
 }
