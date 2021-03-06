 package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;
 
 import org.ovirt.engine.core.common.businessentities.DbUser;
 import org.ovirt.engine.ui.uicommonweb.models.users.UserGeneralModel;
 import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
 import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
 import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
 import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
 import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserSelectionChangeEvent;
 import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGeneralView;
 import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
 import org.ovirt.engine.ui.webadmin.widget.tab.ModelBoundTabData;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.TabData;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.annotations.ProxyEvent;
 import com.gwtplatform.mvp.client.annotations.TabInfo;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
 
 public class SubTabUserGeneralPresenter extends AbstractSubTabPresenter<DbUser, UserListModel, UserGeneralModel, SubTabUserGeneralView, SubTabUserGeneralPresenter.ProxyDef> {
 
     @ProxyCodeSplit
     @NameToken(ApplicationPlaces.userGeneralSubTabPlace)
     public interface ProxyDef extends TabContentProxyPlace<SubTabUserGeneralPresenter> {
     }
 
     public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
     }
 
     @TabInfo(container = UserSubTabPanelPresenter.class)
     static TabData getTabData(ClientGinjector ginjector) {
         return new ModelBoundTabData(ginjector.getApplicationConstants().userGeneralSubTabLabel(), 0,
                 ginjector.getSubTabUserGeneralModelProvider());
     }
 
     @Inject
     public SubTabUserGeneralPresenter(EventBus eventBus, SubTabUserGeneralView view, ProxyDef proxy,
            PlaceManager placeManager, DetailModelProvider<UserListModel, UserGeneralModel> modelProvider) {
         super(eventBus, view, proxy, placeManager, modelProvider);
     }
 
     @Override
     protected void revealInParent() {
         RevealContentEvent.fire(this, UserSubTabPanelPresenter.TYPE_SetTabContent, this);
     }
 
     @Override
     protected PlaceRequest getMainTabRequest() {
         return new PlaceRequest(ApplicationPlaces.userMainTabPlace);
     }
 
     @ProxyEvent
     public void onUserSelectionChange(UserSelectionChangeEvent event) {
         updateMainTabSelection(event.getSelectedItems());
     }
 
 }
