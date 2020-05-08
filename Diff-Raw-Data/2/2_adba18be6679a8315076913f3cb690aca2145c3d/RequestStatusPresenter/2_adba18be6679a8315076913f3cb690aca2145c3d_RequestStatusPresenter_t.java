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
 
 import org.accesointeligente.client.UserGatekeeper;
 import org.accesointeligente.client.services.RequestServiceAsync;
 import org.accesointeligente.client.uihandlers.RequestStatusUiHandlers;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.RequestCategory;
 import org.accesointeligente.shared.*;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
 import com.gwtplatform.mvp.client.proxy.*;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.Date;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 public class RequestStatusPresenter extends Presenter<RequestStatusPresenter.MyView, RequestStatusPresenter.MyProxy> implements RequestStatusUiHandlers {
 	public interface MyView extends View, HasUiHandlers<RequestStatusUiHandlers> {
 		void setDate(Date date);
 		void setStatus(RequestStatus status);
 		void setInstitutionName(String name);
 		void setRequestInfo(String info);
 		void setRequestContext(String context);
 		void setRequestTitle(String title);
 		void addRequestCategories(RequestCategory category);
 		void setRequestCategories(Set<RequestCategory> categories);
 		void editOptions(Boolean allowEdit);
 	}
 
 	@ProxyCodeSplit
 	@UseGatekeeper(UserGatekeeper.class)
 	@NameToken(AppPlace.REQUESTSTATUS)
 	public interface MyProxy extends ProxyPlace<RequestStatusPresenter> {
 	}
 
 	@Inject
 	private PlaceManager placeManager;
 
 	@Inject
 	private RequestServiceAsync requestService;
 
 	@Inject
 	public RequestStatusPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	private Integer requestId;
 	private Request request;
 
 	@Override
 	protected void onReset() {
 		if (requestId != null) {
 			showRequest(requestId);
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
 			requestId = Integer.parseInt(request.getParameter("requestId", null));
 		} catch (Exception ex) {
 			requestId = null;
 		}
 	}
 
 	@Override
 	public void showRequest(Integer requestId) {
 		requestService.getRequest(requestId, new AsyncCallback<Request>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar la solicitud", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				if (result != null) {
 					setRequest(result);
 					getView().setStatus(result.getStatus());
 					getView().setInstitutionName(result.getInstitution().getName());
 					getView().setRequestInfo(result.getInformation());
 					getView().setRequestContext(result.getContext());
 					getView().setRequestTitle(result.getTitle());
 					getView().setRequestCategories(result.getCategories());
					getView().setDate(result.getCreationDate());
 					getView().editOptions(requestIsEditable());
 				} else {
 					showNotification("No se puede cargar la solicitud", NotificationEventType.ERROR);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void deleteRequest() {
 		requestService.deleteRequest(getRequest(), new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No se ha podido eliminar la solicitud", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				showNotification("Se ha eliminado la solicitud", NotificationEventType.SUCCESS);
 				placeManager.revealPlace(new PlaceRequest(AppPlace.LIST).with("type", RequestListType.MYREQUESTS.getType()));
 			}
 		});
 	}
 
 	@Override
 	public Request getRequest() {
 		return request;
 	}
 
 	@Override
 	public void setRequest(Request request) {
 		this.request = request;
 	}
 
 	@Override
 	public Boolean requestIsEditable() {
 		if (request.getStatus() == RequestStatus.DRAFT) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void confirmRequest() {
 		request.setStatus(RequestStatus.NEW);
 		request.setConfirmationDate(new Date());
 		requestService.saveRequest(request, new AsyncCallback<Request>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible confirmar su borrador de solicitud, por favor intentelo nuevamente", NotificationEventType.NOTICE);
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				showNotification("Ha confirmado su borrador de solicitud. Su solicitud será procesada a la brevedad", NotificationEventType.SUCCESS);
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
 	public void editRequest() {
 		placeManager.revealPlace(new PlaceRequest(AppPlace.EDITREQUEST).with("requestId", request.getId().toString()));
 	}
 
 	@Override
 	public void gotoMyRequests() {
 		placeManager.revealPlace(new PlaceRequest(AppPlace.LIST).with("type", RequestListType.MYREQUESTS.getType()));
 	}
 }
