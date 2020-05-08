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
 
 import org.accesointeligente.client.services.RequestServiceAsync;
 import org.accesointeligente.client.uihandlers.ResponseUserSatisfactionUiHandlers;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.Response;
 import org.accesointeligente.shared.*;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.proxy.*;
 
 import javax.inject.Inject;
 
 public class ResponseUserSatisfactionPresenter extends Presenter<ResponseUserSatisfactionPresenter.MyView, ResponseUserSatisfactionPresenter.MyProxy> implements ResponseUserSatisfactionUiHandlers {
 	public interface MyView extends View, HasUiHandlers<ResponseUserSatisfactionUiHandlers> {
 		void showUserSatisfactionPanel(Boolean visible);
 		void showRequestStatusPanel(Boolean visible);
 	}
 
 	@ProxyCodeSplit
 	@NameToken(AppPlace.RESPONSEUSERSATISFACTION)
 	public interface MyProxy extends ProxyPlace<ResponseUserSatisfactionPresenter> {
 	}
 
 	@Inject
 	private PlaceManager placeManager;
 
 	@Inject
 	private RequestServiceAsync requestService;
 
 	private String responseKey;
 	private Integer responseId;
 	private Response response;
 	private Request request;
 
 	@Inject
 	public ResponseUserSatisfactionPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 		Window.setTitle("Danos tu opinion");
 		if (responseId != null) {
 			loadResponse();
 		}
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		super.prepareFromRequest(request);
 
 		try {
 			responseId = Integer.parseInt(request.getParameter("responseId", null));
 			responseKey = request.getParameter("responseKey", null);
 		} catch (Exception ex) {
 			responseId = null;
 			responseKey = null;
 		}
 	}
 
 	private void loadResponse() {
 		if (responseId == null || responseKey ==  null) {
 			showNotification("Falta más información para cargar esta ventana", NotificationEventType.ERROR);
 			placeManager.revealDefaultPlace();
 		}
 		requestService.getResponse(responseId, responseKey, new AsyncCallback<Response>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No fue posible cargar la información de la respuesta, por favor intente nuevamente", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(Response result) {
 				if (result != null) {
 					response = result;
 					requestService.getRequestByResponseId(responseId, new AsyncCallback<Request>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							showNotification("No fue posible cargar la información de la respuesta, por favor intente nuevamente", NotificationEventType.ERROR);
 						}
 
 						@Override
 						public void onSuccess(Request result) {
 							if (result != null) {
 								request = result;
								if (response.getUserSatisfaction() != null && response.getUserSatisfaction() != UserSatisfaction.NOANSWER) {
 									getView().showUserSatisfactionPanel(false);
 									getView().showRequestStatusPanel(false);
 								}
 							} else {
 								showNotification("No fue posible cargar la información de la respuesta, por favor intente nuevamente", NotificationEventType.ERROR);
 								placeManager.revealDefaultPlace();
 							}
 						}
 					});
 				} else {
 					showNotification("La información entregada es incorrecta", NotificationEventType.ERROR);
 					placeManager.revealDefaultPlace();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void updateResponse(ResponseType responseType, UserSatisfaction userSatisfaction) {
 		response.setUserSatisfaction(userSatisfaction);
 		response.setType(responseType);
 		requestService.saveResponse(response, new AsyncCallback<Response>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No fue posible realizar esta acción, por favor intente nuevamente", NotificationEventType.ERROR);
 				getView().showUserSatisfactionPanel(true);
 				getView().showRequestStatusPanel(false);
 			}
 
 			@Override
 			public void onSuccess(Response result) {
 				requestService.setRequestUserSatisfaction(request, new AsyncCallback<Request>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						showNotification("No fue posible realizar esta acción, por favor intente nuevamente", NotificationEventType.ERROR);
 						getView().showUserSatisfactionPanel(true);
 						getView().showRequestStatusPanel(false);
 					}
 
 					@Override
 					public void onSuccess(Request result) {
 						showNotification("Hemos guardado su respuesta", NotificationEventType.SUCCESS);
 						getView().showUserSatisfactionPanel(false);
 						getView().showRequestStatusPanel(false);
 					}
 				});
 			}
 		});
 	}
 
 	@Override
 	public RequestStatus getRequestStatus() {
 		if (response != null) {
 			return response.getRequest().getStatus();
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		params.setDuration(NotificationEventParams.DURATION_NORMAL);
 		fireEvent(new NotificationEvent(params));
 	}
 }
