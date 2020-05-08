 /**
 
  *
  * Licensed under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation. You may obtain a copy of the
  * License at: http://www.gnu.org/copyleft/gpl.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.fountainhead.client.presenter;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.fountainhead.client.core.LoginAuthenticatedEvent;
 import com.fountainhead.client.place.NameTokens;
 import com.fountainhead.client.view.SignInPageUiHandlers;
 import com.fountainhead.shared.CurrentUser;
 import com.fountainhead.shared.LoginAction;
 import com.fountainhead.shared.LoginException;
 import com.fountainhead.shared.LoginResult;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class SignInPagePresenter
 		extends
 			Presenter<SignInPagePresenter.MyView, SignInPagePresenter.MyProxy>
 		implements
 			SignInPageUiHandlers {
 
 	private final EventBus eventBus;
 	private final DispatchAsync dispatcher;
 	private final PlaceManager placeManager;
 
 	@ProxyStandard
 	@NameToken(NameTokens.signInPage)
 	@NoGatekeeper
 	public interface MyProxy extends ProxyPlace<SignInPagePresenter> {
 	}
 
 	public interface MyView extends View, HasUiHandlers<SignInPageUiHandlers> {
 		String getUserName();
 		String getPassword();
 		public void setError(String errorText);
 		void resetAndFocus();
 	}
 	private final CurrentUser user;
 	@Inject
 	public SignInPagePresenter(final EventBus eventBus, MyView view,
 			MyProxy proxy, final DispatchAsync dispatcher,
 			final PlaceManager placeManager, CurrentUser user) {
 		super(eventBus, view, proxy);
 
 		getView().setUiHandlers(this);
 
 		this.eventBus = eventBus;
 		this.dispatcher = dispatcher;
 		this.placeManager = placeManager;
 		this.user = user;
 	}
 
 	@Override
 	protected void onReset() {
 		super.onReset();
 
 		getView().resetAndFocus();
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealRootContentEvent.fire(this, this);
 	}
 
 	@Override
 	public void onOkButtonClicked() {
 		sendCredentialsToServer();
 	}
 
 	private void sendCredentialsToServer() {
 		String login = getView().getUserName();
 		String password = getView().getPassword();
 		getView().setError("");
 		System.out.println(login + "-" + password);
 
 		getDispatcher().execute(new LoginAction(login, password),
 				new AsyncCallback<LoginResult>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						if (caught instanceof LoginException) {
 							getView().setError(caught.getMessage());
 							getView().resetAndFocus();
 						} else {
 							// Fatal Unexpected error will be logged with
 							// gwt-log
 							if (Log.isDebugEnabled())
 								Log.debug("Sign In-Caught unexpected Error!\n"
 										+ caught.toString());
							Window.alert("Caught unexpected Error!\n Refer to Log for more info about this error");
 						}
 					}
 
 					@Override
 					public void onSuccess(LoginResult result) {
 						// CurrentUser currentUser = new CurrentUser();
 						user.setLogin(result.getResponse().getLogin());
 						user.setLoggedIn(result.getResponse().isLoggedIn());
 						user.setAdministrator(result.getResponse()
 								.isAdministrator());
 						LoginAuthenticatedEvent.fire(eventBus, user);
 
 						PlaceRequest placeRequest = new PlaceRequest(
 								NameTokens.settingsPage);
 
 						getPlaceManager().revealPlace(placeRequest);
 
 						Log.debug("sign in onSuccess() - username: "
 								+ result.getResponse().getLogin());
 					}
 				});
 	}
 
 	private DispatchAsync getDispatcher() {
 		return dispatcher;
 	}
 
 	private PlaceManager getPlaceManager() {
 		return placeManager;
 	}
 }
