 package com.myonlinebd.catalog.client.view;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.myonlinebd.catalog.client.presenter.AccountCreatorPresenter;
 import com.myonlinebd.catalog.client.requestfactory.BusinessCardsRequestFactory;
 import com.myonlinebd.catalog.shared.entities.AccountProxy;
 import com.myonlinebd.catalog.shared.entities.AddressProxy;
 import com.myonlinebd.catalog.shared.entities.ResponseProxy;
 
 
 /**
  * @author Adelin Ghanayem adelin.ghanaem@clouway.com
  */
 public class AccountCreatorWorkflow extends Composite implements AccountCreatorView {
 
 
   AccountCreatorWorkflowBinder binder = GWT.create(AccountCreatorWorkflowBinder.class);
 
 
   public void show(HasWidgets hasWidgets) {
     hasWidgets.clear();
     hasWidgets.add(this.asWidget());
   }
 
 
   interface AccountCreatorWorkflowBinder extends UiBinder<HTMLPanel, AccountCreatorWorkflow> {
   }
 
 
   //The Editor Driver
   interface Driver extends RequestFactoryEditorDriver<AccountProxy, AccountEditor> {
   }
 
   private Driver driver;
 
 
   private AccountCreatorPresenter presenter;
 
   BusinessCardsRequestFactory.AccountContext context;
 
   @UiField()
   AccountEditor accountEditor;
 
   AccountProxy proxy;
 
   @UiField
   Button submit;
 
   @UiField
   Label errorMessages;
 
   @UiField(provided = true)
   HeaderView header;
 
   @Inject
   public AccountCreatorWorkflow(BusinessCardsRequestFactory requestFactory, PlaceController controller) {
     header = new HeaderView(controller);
     initWidget(binder.createAndBindUi(this));
 
     driver = GWT.create(Driver.class);
     //initialize the drive with factory and account editor
     driver.initialize(requestFactory, accountEditor);
 
     //save the proxy on the server
     context = requestFactory.accountContext();
     //create the proxy
     proxy = context.create(AccountProxy.class);
 
     proxy.setAddress(context.create(AddressProxy.class));
 
     context.create(proxy).to(new Receiver<ResponseProxy>() {
 
 
       @Override
       public void onSuccess(ResponseProxy response) {
        Window.alert("So far so good !");
       }
     });
     //return a mutable proxy
     proxy = context.edit(proxy);
 
     //start drive the editor
     driver.edit(proxy, context);
   }
 
 
   //TODO:it seems like we have  to persist the object before then pass it to the editor  .... !
   @UiHandler("submit")
   public void onSubmit(ClickEvent event) {
     driver.flush();
     if (proxy.getAddress() != null) {
       Window.alert(proxy.getAddress().getStreet());
     }
     presenter.createAccount(context);
   }
 
 
   public Widget asWidget() {
     return this;
   }
 
 
   public void setPresenter(AccountCreatorPresenter accountCreatorPresenter) {
     presenter = accountCreatorPresenter;
   }
 
   @Override
   public void disableSubmitButton() {
     //To change body of implemented methods use File | Settings | File Templates.
   }
 
   @Override
   public void showMessage(String violationMessages) {
     //To change body of implemented methods use File | Settings | File Templates.

   }
 }
 
