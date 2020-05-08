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
 import org.accesointeligente.model.Institution;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.RequestCategory;
 import org.accesointeligente.shared.*;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 
 import java.util.*;
 
 public class RequestPresenter extends CustomWidgetPresenter<RequestPresenter.Display> implements RequestPresenterIface {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestPresenterIface presenter);
 		void setInstitutions(Map<String, Institution> institutions);
 		void cleanRequestCategories();
 		void addRequestCategories(RequestCategory category);
 		Institution getInstitution();
 		String getRequestInfo();
 		String getRequestContext();
 		String getRequestTitle();
 		Set<RequestCategory> getRequestCategories();
 		Boolean getAnotherInstitutionYes();
 		Boolean getAnotherInstitutionNo();
 	}
 
 	private Request request;
 
 	@Inject
 	public RequestPresenter(Display display, EventBus eventBus) {
 		super(display, eventBus);
 		bind();
 	}
 
 	@Override
 	public void setup() {
 		getRequestCategories();
 		getInstitutions();
 	}
 
 	@Override
 	protected void onBind() {
 		display.setPresenter(this);
 	}
 
 	@Override
 	protected void onUnbind() {
 	}
 
 	@Override
 	protected void onRevealDisplay() {
 	}
 
 	@Override
 	public void getRequestCategories() {
 		display.cleanRequestCategories();
 
 		serviceInjector.getRequestService().getCategories(new AsyncCallback<List<RequestCategory>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("Error obteniendo actividades", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(List<RequestCategory> result) {
 				for (RequestCategory category : result) {
 					display.addRequestCategories(category);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void getInstitutions() {
 		serviceInjector.getInstitutionService().getInstitutions(new AsyncCallback<List<Institution>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				showNotification("No es posible recuperar las instituciones", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(List<Institution> result) {
 				Map<String, Institution> institutions = new HashMap<String, Institution>();
 
 				for (Institution institution: result) {
 					if (institution.getEnabled()) {
 						institutions.put(institution.getName(), institution);
 					}
 				}
 
 				display.setInstitutions(institutions);
 			}
 		});
 	}
 
 	@Override
 	public void submitRequest() {
 		Institution institution = display.getInstitution();
 
 		if (institution == null) {
 			showNotification("Por favor complete el campo de Institución", NotificationEventType.NOTICE);
 			return;
 		}
 
 		String requestInfo = display.getRequestInfo();
 		String requestContext = display.getRequestContext();
 
 		if (requestInfo == null || requestInfo.trim().length() == 0) {
 			showNotification("Por favor complete el campo de Información", NotificationEventType.NOTICE);
 			return;
 		}
 
 		if (requestContext == null || requestContext.trim().length() == 0) {
 			showNotification("Por favor complete el campo de Contexto", NotificationEventType.NOTICE);
 			return;
 		}
 
 		String requestTitle = display.getRequestTitle();
 		Set<RequestCategory> categories = display.getRequestCategories();
 		Boolean anotherInstitutionYes = display.getAnotherInstitutionYes();
 		Boolean anotherInstitutionNo = display.getAnotherInstitutionNo();
 
 		if (requestTitle == null || requestTitle.trim().length() == 0) {
 			showNotification("Por favor complete el campo de Titulo de la solicitud", NotificationEventType.NOTICE);
 			return;
 		}
 
 		if(anotherInstitutionYes == false && anotherInstitutionNo == false) {
 			showNotification("Por favor seleccione si desea solicitar esta información a otro organismo", NotificationEventType.NOTICE);
 			return;
 		}
 
 		request = new Request();
 		request.setInstitution(institution);
 		request.setInformation(requestInfo);
 		request.setContext(requestContext);
 		request.setTitle(requestTitle);
 		request.setCategories(categories);
 		request.setAnotherInstitution(anotherInstitutionYes);
 		request.setUser(ClientSessionUtil.getUser());
 		request.setStatus(RequestStatus.DRAFT);
 
 		serviceInjector.getRequestService().saveRequest(request, new AsyncCallback<Request>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				caught.printStackTrace(System.err);
 				showNotification("No se ha podido almacenar su solicitud, intente nuevamente", NotificationEventType.ERROR);
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				request = result;
 				showRequest();
 			}
 		});
 	}
 
 	@Override
 	public void showRequest() {
 		if (request != null) {
 			History.newItem(AppPlace.REQUESTSTATUS.getToken() + "?requestId=" + request.getId());
 		} else {
 			showNotification("No se ha podido cargar la solicitud", NotificationEventType.NOTICE);
 		}
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		params.setDuration(NotificationEventParams.DURATION_NORMAL);
 		eventBus.fireEvent(new NotificationEvent(params));
 	}
 }
