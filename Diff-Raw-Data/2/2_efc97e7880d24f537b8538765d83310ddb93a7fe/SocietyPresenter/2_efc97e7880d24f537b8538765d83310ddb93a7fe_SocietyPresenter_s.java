 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.SocietyServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.SocietyUiHandlers;
 import cl.votainteligente.inspector.model.Person;
 import cl.votainteligente.inspector.model.Society;
 import cl.votainteligente.inspector.shared.NotificationEvent;
 import cl.votainteligente.inspector.shared.NotificationEventParams;
 import cl.votainteligente.inspector.shared.NotificationEventType;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 
 import java.util.Iterator;
 
 public class SocietyPresenter extends Presenter<SocietyPresenter.MyView, SocietyPresenter.MyProxy> implements SocietyUiHandlers {
 	public static final String PLACE = "society";
 	public static final String PARAM_SOCIETY_ID = "societyId";
 
 	public interface MyView extends View, HasUiHandlers<SocietyUiHandlers> {
 		void clearSocietyData();
 		void setSocietyName(String societyName);
 		void setSocietyFantasyName(String societyFantasyName);
 		void setSocietyUid(String societyUid);
 		void setSocietyCreationDate(String societyCreationDate);
 		void setSocietyCurrentStock(String societyCurrentStock);
 		void setSocietyStatus(String societyStatus);
 		void setSocietySubject(String societySubject);
 		void setSocietyType(String societyType);
 		void setSocietyMembers(String societyMembers);
 		void setSocietyInitialStock(String societyInitialStock);
 		void setParlamentarianStock(String parlamentarianStock);
 		void setSocietyAddress(String societyAddress);
 		void setSocietyPublishDate(String societyPublishDate);
 		void setNotaryName(String notaryName);
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<SocietyPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private SocietyServiceAsync societyService;
 	private Long societyId;
 	private Society society;
 
 	@Inject
 	public SocietyPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 		getView().clearSocietyData();
 
 		if (societyId != null) {
 			getSociety(societyId);
 		}
 	}
 
 	@Override
 	protected void onReveal() {
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_POPUP_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			societyId = Long.parseLong(placeRequest.getParameter(PARAM_SOCIETY_ID, null));
 		} catch (NumberFormatException nfe) {
 			societyId = null;
 		}
 	}
 
 	public Long getSocietyId() {
 		return societyId;
 	}
 
 	public void setSocietyId(Long societyId) {
 		this.societyId = societyId;
 	}
 
 	public void getSociety(Long societyId) {
 		societyService.getSociety(societyId, new AsyncCallback<Society>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				NotificationEventParams params = new NotificationEventParams();
 				params.setMessage(applicationMessages.getErrorSociety());
 				params.setType(NotificationEventType.ERROR);
 				params.setDuration(NotificationEventParams.DURATION_SHORT);
 				fireEvent(new NotificationEvent(params));
 			}
 
 			@Override
 			public void onSuccess(Society result) {
				if (society != null) {
 					society = result;
 
 					if (society.getName() != null) {
 						getView().setSocietyName(society.getName());
 					}
 
 					if (society.getFantasyName() != null) {
 						getView().setSocietyFantasyName(society.getFantasyName());
 					}
 
 					if (society.getUid() != null) {
 						getView().setSocietyUid(society.getUid());
 					}
 
 					if (society.getCreationDate() != null) {
 						getView().setSocietyCreationDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(society.getCreationDate()));
 					}
 
 					if (society.getCurrentStock() != null) {
 						getView().setSocietyCurrentStock(NumberFormat.getCurrencyFormat().format(society.getCurrentStock()));
 					}
 
 					if (society.getSocietyStatus() != null) {
 						getView().setSocietyStatus(society.getSocietyStatus().getName());
 					}
 
 					if (society.getSocietyType() != null) {
 						getView().setSocietyType(society.getSocietyType().getName());
 					}
 
 					if (society.getMembers() != null && !society.getMembers().isEmpty()) {
 						StringBuilder sb = new StringBuilder();
 						Iterator<Person> iterator = society.getMembers().iterator();
 
 						while (iterator.hasNext()) {
 							sb.append(iterator.next().toString());
 
 							if (iterator.hasNext()) {
 								sb.append(", ");
 							} else {
 								sb.append(".");
 							}
 						}
 
 						getView().setSocietyMembers(sb.toString());
 					}
 
 					if (society.getInitialStock() != null) {
 						getView().setSocietyInitialStock(NumberFormat.getCurrencyFormat().format(society.getInitialStock()));
 					}
 
 					if (society.getAddress() != null) {
 						getView().setSocietyAddress(society.getAddress());
 					}
 
 					if (society.getPublishDate() != null) {
 						getView().setSocietyPublishDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(society.getPublishDate()));
 					}
 
 					if (society.getNotary() != null) {
 						getView().setNotaryName(society.getNotary().getName());
 					}
 				} else {
 					NotificationEventParams params = new NotificationEventParams();
 					params.setMessage(applicationMessages.getErrorSociety());
 					params.setType(NotificationEventType.ERROR);
 					params.setDuration(NotificationEventParams.DURATION_SHORT);
 					fireEvent(new NotificationEvent(params));
 					History.back();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void close() {
 		History.back();
 	}
 }
