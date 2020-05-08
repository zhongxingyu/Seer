 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client.presenters;
 
 import org.accesointeligente.client.AppController;
 import org.accesointeligente.client.ClientSessionUtil;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.User;
 import org.accesointeligente.model.UserFavoriteRequest;
 import org.accesointeligente.shared.*;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.inject.Inject;
 
 import java.util.List;
 import java.util.Map;
 
 public class RequestListPresenter extends CustomWidgetPresenter<RequestListPresenter.Display> implements RequestListPresenterIface, RequestSearchEventHandler {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestListPresenterIface presenter);
 		void displayMessage(String message);
 		void setListTitle(String title);
 		void setListTitleStyle(String style);
 		void setSearchWidget(Widget widget);
 		void removeSearchWidget();
 		void initTable();
 		void initTableColumns();
 		void initTableFavColumn();
 		void removeTableFavColumn();
 		void setRequests(ListDataProvider<Request> data);
 		void searchPanelToggleVisible();
 		void searchToolTipToggleVisible();
 	}
 
 	private String listType;
 
 	@Inject
 	public RequestListPresenter(Display display, EventBus eventBus) {
 		super(display, eventBus);
 		bind();
 	}
 
 	@Override
 	public void setup() {
 		RequestSearchPresenter presenter = presenterInjector.getRequestSearchPresenter();
 		presenter.setup();
 		display.setSearchWidget(presenter.getDisplay().asWidget());
 
 		if (ClientSessionUtil.checkSession()) {
 			display.initTableFavColumn();
 		} else {
 			display.removeTableFavColumn();
 		}
 	}
 
 	@Override
 	protected void onBind() {
 		display.setPresenter(this);
 		eventBus.addHandler(RequestSearchEvent.TYPE, this);
 		display.initTable();
 	}
 
 	@Override
 	protected void onUnbind() {
 	}
 
 	@Override
 	protected void onRevealDisplay() {
 	}
 
 	@Override
 	public void loadRequests(Integer offset, Integer limit, String type) {
 		listType = type;
 
 		if (type.equals(RequestListType.MYREQUESTS.getType())) {
 			display.setListTitle("Mis solicitudes");
 			display.setListTitleStyle(RequestListType.MYREQUESTS.getType());
 			display.removeTableFavColumn();
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserRequestList(offset, limit, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.FAVORITES.getType())) {
 			display.setListTitle("Mis favoritas");
 			display.setListTitleStyle(RequestListType.FAVORITES.getType());
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserFavoriteRequestList(offset, limit, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.DRAFTS.getType())) {
 			display.setListTitle("Mis borradores");
 			display.setListTitleStyle(RequestListType.DRAFTS.getType());
 			display.removeTableFavColumn();
 			display.removeSearchWidget();
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserDraftList(offset, limit, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.GENERAL.getType())) {
 			display.setListTitle("Listado de solicitudes");
 			display.setListTitleStyle(RequestListType.GENERAL.getType());
 			display.searchPanelToggleVisible();
 
 			serviceInjector.getRequestService().getRequestList(offset, limit, new AsyncCallback<List<Request>>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 					History.newItem(AppPlace.HOME.getToken());
 				}
 
 				@Override
 				public void onSuccess(List<Request> results) {
 					ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 					display.setRequests(data);
 				}
 			});
 		} else {
 			showNotification("No existe el tipo de lista solicitado: " + type, NotificationEventType.ERROR);
 			History.newItem(AppPlace.HOME.getToken());
 		}
 	}
 
 	@Override
 	public void loadRequests(Integer offset, Integer limit, String type, RequestSearchParams params) {
 
 		if (type.equals(RequestListType.MYREQUESTS.getType())) {
 			display.setListTitle("Mis solicitudes");
 			display.setListTitleStyle(RequestListType.MYREQUESTS.getType());
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserRequestList(offset, limit, params, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.FAVORITES.getType())) {
 			display.setListTitle("Mis favoritas");
 			display.setListTitleStyle(RequestListType.FAVORITES.getType());
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserFavoriteRequestList(offset, limit, params, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.DRAFTS.getType())) {
 			display.setListTitle("Mis borradores");
 			display.setListTitleStyle(RequestListType.DRAFTS.getType());
 
 			if (ClientSessionUtil.checkSession()) {
 				serviceInjector.getRequestService().getUserDraftList(offset, limit, new AsyncCallback<List<Request>>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						History.newItem(AppPlace.HOME.getToken());
 					}
 
 					@Override
 					public void onSuccess(List<Request> results) {
 						ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 						display.setRequests(data);
 					}
 				});
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				History.newItem(AppPlace.HOME.getToken());
 			}
 
 		} else if (type.equals(RequestListType.GENERAL.getType())) {
 			display.setListTitle("Listado de solicitudes");
 			display.setListTitleStyle(RequestListType.GENERAL.getType());
 
 			serviceInjector.getRequestService().getRequestList(offset, limit, params, new AsyncCallback<List<Request>>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 					History.newItem(AppPlace.HOME.getToken());
 				}
 
 				@Override
 				public void onSuccess(List<Request> results) {
 					ListDataProvider<Request> data = new ListDataProvider<Request>(results);
 					display.setRequests(data);
 				}
 			});
 		} else {
 			showNotification("No existe el tipo de lista solicitado: " + type, NotificationEventType.ERROR);
 			History.newItem(AppPlace.HOME.getToken());
 		}
 	}
 
 	@Override
 	public void requestToggleFavorite(final Request request) {
 		final User user = ClientSessionUtil.getUser();
 		serviceInjector.getRequestService().getFavoriteRequest(request, user, new AsyncCallback<UserFavoriteRequest>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar los favoritos", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(UserFavoriteRequest result) {
 				if (result == null) {
 					serviceInjector.getRequestService().createFavoriteRequest(request, user, new AsyncCallback<UserFavoriteRequest>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							showNotification("No es posible almacenar el favorito", NotificationEventType.ERROR);
 						}
 
 						@Override
 						public void onSuccess(UserFavoriteRequest result) {
 							Map<String, String> parameters = AppController.getHistoryTokenParameters(AppController.getCurrentHistoryToken());
 							loadRequests(0, 100, parameters.get("type"));
 							showNotification("Se ha agregado el favorito", NotificationEventType.SUCCESS);
 						}
 					});
 				} else {
 					UserFavoriteRequest favorite = new UserFavoriteRequest();
 					favorite.setRequest(request);
 					favorite.setUser(user);
 
 					serviceInjector.getRequestService().deleteFavoriteRequest(favorite, new AsyncCallback<Void>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							showNotification("No es posible eliminar el favorito", NotificationEventType.ERROR);
 						}
 
 						@Override
 						public void onSuccess(Void result) {
 							Map<String, String> parameters = AppController.getHistoryTokenParameters(AppController.getCurrentHistoryToken());
 							loadRequests(0, 100, parameters.get("type"));
 							showNotification("Se ha eliminado el favorito", NotificationEventType.SUCCESS);
 						}
 					});
 				}
 			}
 		});
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		params.setDuration(NotificationEventParams.DURATION_NORMAL);
 		eventBus.fireEvent(new NotificationEvent(params));
 	}
 
 	@Override
 	public void showRequest(Integer requestId) {
 		if (listType.equals(RequestListType.DRAFTS.getType())) {
 			History.newItem(AppPlace.REQUESTSTATUS.getToken() + "?requestId=" + requestId.toString());
 		} else {
 			History.newItem(AppPlace.RESPONSE.getToken() + "?requestId=" + requestId.toString());
 		}
 	}
 
 	@Override
 	public void onSearch(RequestSearchEvent event) {
 		Map<String, String> parameters = AppController.getHistoryTokenParameters(AppController.getCurrentHistoryToken());
 		String type = parameters.get("type");
		if (type.equals(RequestListType.GENERAL.getType())) {
			display.searchToolTipToggleVisible();
		}
 		loadRequests(0, 100, type, event.getParams());
 	}
 
 	@Override
 	public String getRequestBaseUrlPlace() {
 		String baseUrl;
 
 		if (listType.equals(RequestListType.DRAFTS.getType())) {
 			baseUrl = "#" + AppPlace.REQUESTSTATUS.getToken() + "?requestId=";
 		} else {
 			baseUrl = "#" + AppPlace.RESPONSE.getToken() + "?requestId=";
 		}
 
 		return baseUrl;
 	}
 }
