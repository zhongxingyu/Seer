 package org.iplantc.de.client.notifications.presenter;
 
 import org.iplantc.de.client.events.EventBus;
 import org.iplantc.de.client.gin.ServicesInjector;
 import org.iplantc.de.client.models.notifications.Notification;
 import org.iplantc.de.client.models.notifications.NotificationCategory;
 import org.iplantc.de.client.models.notifications.NotificationMessage;
 import org.iplantc.de.client.notifications.events.DeleteNotificationsUpdateEvent;
 import org.iplantc.de.client.notifications.util.NotificationHelper;
 import org.iplantc.de.client.notifications.views.NotificationToolbarView;
 import org.iplantc.de.client.notifications.views.NotificationToolbarViewImpl;
 import org.iplantc.de.client.notifications.views.NotificationView;
 import org.iplantc.de.client.notifications.views.NotificationView.Presenter;
 import org.iplantc.de.client.services.callbacks.NotificationCallback;
 import org.iplantc.de.commons.client.ErrorHandler;
 import org.iplantc.de.resources.client.messages.I18N;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 import com.sencha.gxt.data.client.loader.RpcProxy;
 import com.sencha.gxt.data.shared.SortDir;
 import com.sencha.gxt.data.shared.SortInfo;
 import com.sencha.gxt.data.shared.SortInfoBean;
 import com.sencha.gxt.data.shared.loader.FilterConfig;
 import com.sencha.gxt.data.shared.loader.FilterConfigBean;
 import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
 import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
 import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
 import com.sencha.gxt.data.shared.loader.PagingLoader;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 
  * A presenter for notification window
  * 
  * @author sriram
  * 
  */
 public class NotificationPresenter implements Presenter, NotificationToolbarView.Presenter {
 
     private final NotificationView view;
     private final NotificationToolbarView toolbar;
 
     private PagingLoadResult<NotificationMessage> callbackResult;
     private NotificationCategory currentCategory;
 
     public NotificationPresenter(NotificationView view) {
         this.view = view;
         toolbar = new NotificationToolbarViewImpl();
         toolbar.setPresenter(this);
         view.setNorthWidget(toolbar);
         this.view.setPresenter(this);
         setRefreshButton(view.getRefreshButton());
         // set default cat
         currentCategory = NotificationCategory.ALL;
     }
 
     @Override
     public void go(HasOneWidget container) {
         container.setWidget(view.asWidget());
         view.setLoader(initProxyLoader());
     }
 
     private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<NotificationMessage>> initProxyLoader() {
 
         RpcProxy<FilterPagingLoadConfig, PagingLoadResult<NotificationMessage>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<NotificationMessage>>() {
             @Override
             public void load(final FilterPagingLoadConfig loadConfig,
                     final AsyncCallback<PagingLoadResult<NotificationMessage>> callback) {
                 // for 'NEW' filter always set offset to 0
                 List<FilterConfig> fc_list = loadConfig.getFilters();
                 if (fc_list != null) {
                     String cat = (fc_list.get(0).getField() != null ? fc_list.get(0).getField()
                             .toLowerCase() : null);
                     if ((!Strings.isNullOrEmpty(cat))) {
                         if (cat.equalsIgnoreCase(NotificationCategory.NEW.toString())) {
                             loadConfig.setOffset(0);
                         }
                     }
                 }
                 ServicesInjector.INSTANCE.getMessageServiceFacade().getNotifications(loadConfig.getLimit(), loadConfig.getOffset(),
                         (loadConfig.getFilters().get(0).getField()) == null ? "" : loadConfig
                                 .getFilters().get(0).getField().toLowerCase(), loadConfig.getSortInfo()
                                 .get(0).getSortDir().toString(), new NotificationServiceCallback(
                                 loadConfig, callback));
 
             }
 
         };
 
         final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<NotificationMessage>> loader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<NotificationMessage>>(
                 proxy);
         loader.setRemoteSort(true);
         loader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, NotificationMessage, PagingLoadResult<NotificationMessage>>(
                 view.getListStore()));
 
         loader.useLoadConfig(buildDefaultLoadConfig());
         return loader;
     }
 
     @Override
     public void filterBy(NotificationCategory category) {
         currentCategory = category;
         toolbar.setCurrentCategory(category);
         FilterPagingLoadConfig config = view.getCurrentLoadConfig();
         FilterConfig filterBean = new FilterConfigBean();
         if (!currentCategory.toString().equalsIgnoreCase("ALL")) {
             filterBean.setField(currentCategory.toString());
         }
 
         List<FilterConfig> filters = new ArrayList<FilterConfig>();
         filters.add(filterBean);
         config.setFilters(filters);
 
         view.loadNotifications(config);
 
     }
 
     @Override
     public void onNotificationSelection(List<NotificationMessage> items) {
         if (items == null || items.size() == 0) {
             toolbar.setDeleteButtonEnabled(false);
         } else {
             toolbar.setDeleteButtonEnabled(true);
         }
     }
 
     @Override
     public FilterPagingLoadConfig buildDefaultLoadConfig() {
         FilterPagingLoadConfig config = new FilterPagingLoadConfigBean();
         config.setLimit(10);
 
         SortInfo info = new SortInfoBean("timestamp", SortDir.DESC);
         List<SortInfo> sortInfo = new ArrayList<SortInfo>();
         sortInfo.add(info);
         config.setSortInfo(sortInfo);
 
         FilterConfig filterBean = new FilterConfigBean();
         if (!currentCategory.toString().equalsIgnoreCase("ALL")) {
             filterBean.setField(currentCategory.toString());
         }
 
         List<FilterConfig> filters = new ArrayList<FilterConfig>();
         filters.add(filterBean);
         config.setFilters(filters);
 
         return config;
     }
 
     private final class NotificationServiceCallback extends NotificationCallback {
         private final PagingLoadConfig loadConfig;
         private final AsyncCallback<PagingLoadResult<NotificationMessage>> callback;
 
         private NotificationServiceCallback(PagingLoadConfig loadConfig,
                 AsyncCallback<PagingLoadResult<NotificationMessage>> callback) {
             this.loadConfig = loadConfig;
             this.callback = callback;
         }
 
         @Override
         public void onSuccess(String result) {
             super.onSuccess(result);
             Splittable splitResult = StringQuoter.split(result);
             int total = 0;
 
             if (splitResult.get("total") != null) {
                 total = Integer.parseInt(splitResult.get("total").asString());
             }
 
             List<NotificationMessage> messages = Lists.newArrayList();
             for (Notification n : this.getNotifications()) {
                 messages.add(n.getMessage());
             }
 
             callbackResult = new PagingLoadResultBean<NotificationMessage>(messages, total,
                     loadConfig.getOffset());
             callback.onSuccess(callbackResult);
             NotificationHelper.getInstance().markAsSeen(messages);
        }
 
        @Override
        public void onFailure(Throwable caught) {
            ErrorHandler.post(caught);
         }

     }
 
     @Override
     public void onFilterSelection(NotificationCategory cat) {
         filterBy(cat);
     }
 
     @Override
     public void onDeleteClicked() {
         NotificationHelper.getInstance().delete(view.getSelectedItems(), new Command() {
             @Override
             public void execute() {
                 view.loadNotifications(view.getCurrentLoadConfig());
             }
         });
 
     }
 
     @Override
     public void setRefreshButton(TextButton refreshBtn) {
         if (refreshBtn != null) {
             refreshBtn.setText(I18N.DISPLAY.refresh());
             toolbar.setRefreshButton(refreshBtn);
         }
     }
 
     @Override
     public void onDeleteAllClicked() {
         view.mask();
         ServicesInjector.INSTANCE.getMessageServiceFacade().deleteAll(new AsyncCallback<String>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
                 view.unmask();
             }
 
             @Override
             public void onSuccess(String result) {
                 view.unmask();
                 view.loadNotifications(view.getCurrentLoadConfig());
                 DeleteNotificationsUpdateEvent event = new DeleteNotificationsUpdateEvent(null);
                 EventBus.getInstance().fireEvent(event);
             }
         });
 
     }
 
     @Override
     public NotificationCategory getCurrentCategory() {
         return currentCategory;
     }
 }
