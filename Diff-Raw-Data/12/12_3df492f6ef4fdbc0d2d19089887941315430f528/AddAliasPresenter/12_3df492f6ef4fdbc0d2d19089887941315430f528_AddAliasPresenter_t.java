 /**
  * 
  */
 package org.mdkt.maildist.client.presenter;
 
 import org.mdkt.library.client.presenter.Presenter;
 import org.mdkt.library.client.rpc.DefaultAsyncCallBack;
 import org.mdkt.maildist.client.event.SaveAliasCancelledEvent;
 import org.mdkt.maildist.client.event.SaveAliasEvent;
 import org.mdkt.maildist.client.rpc.DistListServiceAsync;
 import org.mdkt.maildist.client.view.AddAliasView;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.HasWidgets;
 
 /**
  * @author trung
  * 
  */
 public class AddAliasPresenter implements Presenter, AddAliasView.Presenter {
 
 	private final DistListServiceAsync aliasService;
 	private final EventBus eventBus;
 	private final AddAliasView view;
 
 	public AddAliasPresenter(DistListServiceAsync aliasService,
 			EventBus eventBus, AddAliasView addAliasView) {
 		this.aliasService = aliasService;
 		this.eventBus = eventBus;
 		this.view = addAliasView;
 		this.view.setPresenter(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.mdkt.library.client.presenter.Presenter#go(com.google.gwt.user.client
 	 * .ui.HasWidgets)
 	 */
 	@Override
 	public void go(HasWidgets container) {
 		container.clear();
 		container.add(view.asWidget());
 	}
 
 	@Override
 	public void onAddButtonClicked() {
 		final String aliasName = view.getAliasValue().getText();
 		aliasService.findAlias(aliasName, new DefaultAsyncCallBack<Boolean>("find alias") {
 			public void _onSuccess(Boolean result) {
 				if (result) {
					view.setErrorAlias("The alias email already exists");
 				} else {
 					eventBus.fireEvent(new SaveAliasEvent(aliasName));
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onCancelButtonClicked() {
 		eventBus.fireEvent(new SaveAliasCancelledEvent());
 	}
 }
