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
 
 import org.accesointeligente.client.ClientSessionUtil;
 import org.accesointeligente.client.events.*;
 import org.accesointeligente.client.services.RequestServiceAsync;
 import org.accesointeligente.client.uihandlers.RequestListUiHandlers;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.User;
 import org.accesointeligente.model.UserFavoriteRequest;
 import org.accesointeligente.shared.*;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.ContentSlot;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.proxy.*;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.*;
 
 import javax.inject.Inject;
 
 public class RequestListPresenter extends Presenter<RequestListPresenter.MyView, RequestListPresenter.MyProxy> implements RequestListUiHandlers, RequestSearchEventHandler, LoginSuccessfulEventHandler, LoginRequiredEventHandler {
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> SLOT_SEARCH_WIDGET = new Type<RevealContentHandler<?>>();
 
 	public interface MyView extends View, HasUiHandlers<RequestListUiHandlers> {
 		void setListTitle(String title);
 		void setListTitleStyle(String style);
 		void initTableColumns();
 		void initTableFavColumn();
 		void initTableReceiptColumn();
 		void removeColumns();
 		void setSearchButtonVisible(Boolean visible);
 		void setSearchHandleVisible(Boolean visible);
 		void setSearchToolTipVisible(Boolean visible);
 		CellTable<Request> getRequestTable();
 	}
 
 	@ProxyCodeSplit
 	@NameToken(AppPlace.LIST)
 	public interface MyProxy extends ProxyPlace<RequestListPresenter> {
 	}
 
 	@Inject
 	private PlaceManager placeManager;
 
 	@Inject
 	private RequestSearchPresenter requestSearchPresenter;
 
 	@Inject
 	private RequestServiceAsync requestService;
 
 	private RequestListType listType;
 	private RequestListType oldListType;
 	private AsyncDataProvider<Request> requestData;
 	private RequestSearchParams searchParams;
 
 	@Inject
 	public RequestListPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onBind() {
 		addHandler(RequestSearchEvent.TYPE, this);
 		addHandler(LoginSuccessfulEvent.TYPE, this);
 		addHandler(LoginRequiredEvent.TYPE, this);
 		prepareDataProvider();
 	}
 
 	@Override
 	public void onReset() {
 		if (listType != null && !listType.equals(oldListType)) {
 			if (checkPrivileges()) {
 				searchParams = null;
 				prepareViewStyle();
 				resetData(0);
 				oldListType = listType;
 			} else {
 				showNotification("Necesita acceder para poder ver esta lista", NotificationEventType.NOTICE);
 				placeManager.revealDefaultPlace();
 			}
 		}
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		String type = request.getParameter("type", "");
 
 		try {
 			listType = Enum.valueOf(RequestListType.class, type.toUpperCase());
 		} catch (IllegalArgumentException iaex) {
 			showNotification("No existe el tipo de lista solicitado: " + type, NotificationEventType.ERROR);
 			placeManager.revealDefaultPlace();
 		}
 	}
 
 	private void prepareDataProvider() {
 		requestData = new AsyncDataProvider<Request>() {
 			@Override
 			protected void onRangeChanged(HasData<Request> display) {
 				if (listType == null) {
 					return;
 				}
 
 				AsyncCallback<Page<Request>> callback = new AsyncCallback<Page<Request>>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No es posible recuperar el listado solicitado", NotificationEventType.ERROR);
 						placeManager.revealDefaultPlace();
 					}
 
 					@Override
 					public void onSuccess(Page<Request> result) {
 						updateRowData(result.getStart().intValue(), result.getData());
 						updateRowCount(result.getDataCount().intValue(), true);
 					}
 				};
 
 				switch (listType) {
 					case MYREQUESTS:
 						if (searchParams != null) {
 							requestService.getUserRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), searchParams, callback);
 						} else {
 							requestService.getUserRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), callback);
 						}
 						break;
 					case FAVORITES:
 						if (searchParams != null) {
 							requestService.getUserFavoriteRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), searchParams, callback);
 						} else {
 							requestService.getUserFavoriteRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), callback);
 						}
 						break;
 					case DRAFTS:
 						requestService.getUserDraftList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), callback);
 						break;
 					case GENERAL:
 						if (searchParams != null) {
 							requestService.getRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), searchParams, callback);
 						} else {
 							requestService.getRequestList(display.getVisibleRange().getStart(), display.getVisibleRange().getLength(), callback);
 						}
 						break;
 				}
 			}
 		};
 
 		requestData.addDataDisplay(getView().getRequestTable());
 	}
 
 	private boolean checkPrivileges() {
 		switch (listType) {
 			case MYREQUESTS:
 			case FAVORITES:
 			case DRAFTS:
 				return ClientSessionUtil.checkSession();
 			default:
 				return true;
 		}
 	}
 
 	private void prepareViewStyle() {
 		clearSlot(SLOT_SEARCH_WIDGET);
 		getView().setSearchButtonVisible(false);
 		getView().setSearchHandleVisible(false);
 		getView().setSearchToolTipVisible(false);
 		getView().removeColumns();
 		getView().initTableColumns();
 
 		switch (listType) {
 			case MYREQUESTS:
 				getView().setListTitle("Mis solicitudes");
 				getView().setListTitleStyle(RequestListType.MYREQUESTS.getType());
 				getView().initTableReceiptColumn();
 				break;
 			case FAVORITES:
 				getView().setListTitle("Mis favoritas");
 				getView().setListTitleStyle(RequestListType.FAVORITES.getType());
 				getView().initTableFavColumn();
 				break;
 			case DRAFTS:
 				getView().setListTitle("Mis borradores");
 				getView().setListTitleStyle(RequestListType.DRAFTS.getType());
 				break;
 			case GENERAL:
 				getView().setListTitle("Listado de solicitudes");
 				getView().setListTitleStyle(RequestListType.GENERAL.getType());
 				setInSlot(SLOT_SEARCH_WIDGET, requestSearchPresenter);
 				getView().setSearchButtonVisible(true);
 				getView().setSearchHandleVisible(true);
 				getView().initTableFavColumn();
 				break;
 		}
 	}
 
 	private void resetData(int start) {
 		CellTable<Request> requestTable = getView().getRequestTable();
 		requestTable.setPageStart(start);
 		RangeChangeEvent.fire(requestTable, new Range(start, requestTable.getPageSize()));
 	}
 
 	@Override
 	public void requestToggleFavorite(final Request request) {
 		final User user = ClientSessionUtil.getUser();
 		requestService.getFavoriteRequest(request, user, new AsyncCallback<UserFavoriteRequest>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar los favoritos", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(UserFavoriteRequest result) {
 				if (result == null) {
 					requestService.createFavoriteRequest(request, user, new AsyncCallback<UserFavoriteRequest>() {
 						@Override
 						public void onFailure(Throwable caught) {
 							showNotification("No es posible almacenar el favorito", NotificationEventType.ERROR);
 						}
 
 						@Override
 						public void onSuccess(UserFavoriteRequest result) {
 							resetData(getView().getRequestTable().getPageStart());
 							showNotification("Se ha agregado el favorito", NotificationEventType.SUCCESS);
 						}
 					});
 				} else {
 					UserFavoriteRequest favorite = new UserFavoriteRequest();
 					favorite.setRequest(request);
 					favorite.setUser(user);
 
 					requestService.deleteFavoriteRequest(favorite, new AsyncCallback<Void>() {
 						@Override
 						public void onFailure(Throwable caught) {
 							showNotification("No es posible eliminar el favorito", NotificationEventType.ERROR);
 						}
 
 						@Override
 						public void onSuccess(Void result) {
 							resetData(getView().getRequestTable().getPageStart());
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
 		fireEvent(new NotificationEvent(params));
 	}
 
 	@Override
 	public void showRequest(Integer requestId) {
		if (listType.equals(RequestListType.DRAFTS)) {
 			placeManager.revealPlace(new PlaceRequest(AppPlace.REQUESTSTATUS).with("requestId", requestId.toString()));
 		} else {
 			placeManager.revealPlace(new PlaceRequest(AppPlace.RESPONSE).with("requestId", requestId.toString()));
 		}
 	}
 
 	@Override
 	public void onSearch(RequestSearchEvent event) {
 		searchParams = event.getParams();
 
 		if (isVisible()) {
 			resetData(0);
 		}
 	}
 
 	// FIXME: use PlaceRequest instead lf URL redirections
 	@Override
	public String getRequestBaseUrlPlace(Integer requestId) {
 		String baseUrl;
 
		if (listType.equals(RequestListType.DRAFTS)) {
			PlaceRequest placeRequest = new PlaceRequest(AppPlace.REQUESTSTATUS).with("requestId", requestId.toString());
			baseUrl = "#" + placeManager.buildHistoryToken(placeRequest);
 		} else {
			PlaceRequest placeRequest = new PlaceRequest(AppPlace.RESPONSE).with("requestId", requestId.toString());
			baseUrl = "#" + placeManager.buildHistoryToken(placeRequest);
 		}
 
 		return baseUrl;
 	}
 
 	@Override
 	public void gotoRequest() {
 		placeManager.revealPlace(new PlaceRequest(AppPlace.REQUEST));
 	}
 
 	@Override
 	public void loginSuccessful(LoginSuccessfulEvent event) {
 		oldListType = null;
 	}
 
 	@Override
 	public void loginRequired(LoginRequiredEvent event) {
 		oldListType = null;
 	}
 
 	@Override
 	public void gotoLogin() {
 		placeManager.revealPlace(new PlaceRequest(AppPlace.LOGIN));
 	}
 }
