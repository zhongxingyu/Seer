 package org.accesointeligente.client.presenters;
 
 import org.accesointeligente.client.services.RPC;
 import org.accesointeligente.model.Request;
 import org.accesointeligente.model.RequestCategory;
 import org.accesointeligente.shared.RequestStatus;
 
 import net.customware.gwt.presenter.client.EventBus;
 import net.customware.gwt.presenter.client.widget.WidgetDisplay;
 import net.customware.gwt.presenter.client.widget.WidgetPresenter;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
import java.util.Date;
 import java.util.Set;
 
 public class RequestStatusPresenter extends WidgetPresenter<RequestStatusPresenter.Display> implements RequestStatusPresenterIface {
 	public interface Display extends WidgetDisplay {
 		void setPresenter(RequestStatusPresenterIface presenter);
		void setDate(Date date);
 		void setStatus(RequestStatus status);
 		void setInstitutionName(String name);
 		void setRequestInfo(String info);
 		void setRequestContext(String context);
 		void setRequestTitle(String title);
 		void addRequestCategories(RequestCategory category);
 		void setRequestCategories(Set<RequestCategory> categories);
 		void setAnotherInstitution(Boolean anotherInstitution);
 		void displayMessage(String string);
 	}
 
 	public RequestStatusPresenter(Display display, EventBus eventBus) {
 		super(display, eventBus);
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
 	public void showRequest(Integer requestId) {
 		RPC.getRequestService().getRequest(requestId, new AsyncCallback<Request>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				display.displayMessage("No es posible recuperar la solicitud");
 			}
 
 			@Override
 			public void onSuccess(Request result) {
 				if (result != null) {
 					display.setStatus(result.getStatus());
 					display.setInstitutionName(result.getInstitution().getName());
 					display.setRequestInfo(result.getInformation());
 					display.setRequestContext(result.getContext());
 					display.setRequestTitle(result.getTitle());
 					display.setRequestCategories(result.getCategories());
 					display.setAnotherInstitution(result.getAnotherInstitution());
 				} else {
 					display.displayMessage("No se puede cargar la solicitud");
 				}
 			}
 
 		});
 	}
 }
