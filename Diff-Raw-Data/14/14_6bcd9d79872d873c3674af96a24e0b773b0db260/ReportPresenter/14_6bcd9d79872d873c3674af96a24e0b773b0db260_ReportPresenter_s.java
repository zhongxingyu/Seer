 /**
 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.fountainhead.client.presenter;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.fountainhead.client.place.NameTokens;
 import com.fountainhead.client.view.ReportUiHandlers;
 import com.fountainhead.shared.CurrentUser;
 import com.fountainhead.shared.JSONTreeLoader;
 import com.fountainhead.shared.LoadTreeAction;
 import com.fountainhead.shared.LoadTreeResult;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.annotations.TabInfo;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
 
 /**
  * A sample {@link Presenter} that lets user toggle between being an
  * administrator and a regular user.
  * <p />
  * It demonstrates the option 1 described in {@link TabInfo}.
  * 
  */
 public class ReportPresenter
 		extends
 			Presenter<ReportPresenter.MyView, ReportPresenter.MyProxy>
 		implements
 			ReportUiHandlers {
 
 	private final DispatchAsync dispatcher;
 	private final CurrentUser currentUser;
 	/**
 	 * {@link ReportPresenter}'s proxy.
 	 */
 	@ProxyCodeSplit
 	@NameToken(NameTokens.settingsPage)
 	@TabInfo(container = MainPagePresenter.class, label = "Report", priority = 1)
 	// The third tab in the main page
 	public interface MyProxy extends TabContentProxyPlace<ReportPresenter> {
 	}
 
 	/**
 	 * {@link ReportPresenter}'s view.
 	 */
 	public interface MyView extends View, HasUiHandlers<ReportUiHandlers> {
 		void setAdmin(boolean isAdmin);
 		void setUserName(String username);
 		Tree getTree();
 		ListBox getList();
 
 	}
 
 	@Inject
 	public ReportPresenter(final EventBus eventBus, final MyView view,
 			final MyProxy proxy, final DispatchAsync dispatcher,
 			final CurrentUser currentUser) {
 		super(eventBus, view, proxy);
 		this.currentUser = currentUser;
 		this.dispatcher = dispatcher;
 		view.setUiHandlers(this);
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetTabContent,
 				this);
 
 	}
 
 	@Override
 	protected void onReveal() {
 		loadTree();
 	}
 
 	@Override
 	public void loadTree() {
 		final JSONTreeLoader loader = new JSONTreeLoader(getView().getTree());
 		getDispatcher().execute(new LoadTreeAction("/WEB-INF/tree-data.json"),
 				new AsyncCallback<LoadTreeResult>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// fatal error . log it
 						if (Log.isDebugEnabled())
 							Log.debug("loadTree()-Caught unexpected Error:\n"
 									+ caught.toString());
 
 						loader.populateTreeError(caught.getMessage());
 
 					}
 
 					@Override
 					public void onSuccess(LoadTreeResult result) {
 						if (Log.isDebugEnabled())
 							Log.debug("loadTree() -OnSucces()- "
 									+ result.getResponse());
 						JSONValue jsonValue = JSONParser.parse(result
 								.getResponse());
 						loader.populateTree(jsonValue);
 					}
 				});
 	}
 
 	/**
 	 * @return the dispatcher
 	 */
 	public DispatchAsync getDispatcher() {
 		return dispatcher;
 	}
 
 	/**
 	 * @return the currentUser
 	 */
 	public CurrentUser getCurrentUser() {
 		return currentUser;
 	}
 	private boolean containsItem(String item) {
 		boolean found = false;
 		for (int i = 0; i < getView().getList().getItemCount(); i++) {
 			if (item.equals(getView().getList().getItemText(i))) {
 				found = true;
 				break;
 			}
 		}
 		return found;
 
 	}
 
 	@Override
 	public void onSelection() {
 		String selectedItem = getView().getTree().getSelectedItem().getText();
 		if (!containsItem(selectedItem))
 			getView().getList().addItem(selectedItem);
 	}
 
 }
