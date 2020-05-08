 package org.accesointeligente.client;
 
 import org.accesointeligente.client.events.*;
 import org.accesointeligente.client.presenters.*;
 import org.accesointeligente.client.views.*;
 import org.accesointeligente.shared.AppPlace;
 
 import net.customware.gwt.presenter.client.EventBus;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import java.util.*;
 
 public class AppController implements ValueChangeHandler<String> {
 	private MainPresenter mainPresenter;
 	private EventBus eventBus;
 	private PopupPanel popup;
 	private static List<String> tokenHistory;
 
 	public AppController(EventBus eventBus) {
 		this.eventBus = eventBus;
 		mainPresenter = new MainPresenter(new MainView(), eventBus);
 		this.eventBus.addHandler(LoginRequiredEvent.TYPE, mainPresenter);
 		this.eventBus.addHandler(LoginSuccessfulEvent.TYPE, mainPresenter);
 		popup = new PopupPanel();
 		tokenHistory = new ArrayList<String>();
 	}
 
 	protected void setup() {
 		RootPanel.get().add(mainPresenter.getDisplay().asWidget());
 
 		mainPresenter.bind();
 		History.addValueChangeHandler(this);
 
 		eventBus.addHandler(LoginSuccessfulEvent.TYPE, new LoginSuccessfulEventHandler() {
 			@Override
 			public void loginSuccessful(LoginSuccessfulEvent event) {
 				History.newItem(getPreviousHistoryToken());
 			}
 		});
 
 		eventBus.addHandler(LoginRequiredEvent.TYPE, new LoginRequiredEventHandler() {
 			@Override
 			public void loginRequired(LoginRequiredEvent event) {
 				if (!AppPlace.HOME.equals(getPlace(History.getToken()))) {
 					History.newItem(AppPlace.HOME.getToken());
 				}
 			}
 		});
 
 		switchSection(History.getToken());
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 		switchSection(token);
 	}
 
 	public void switchSection(String token) {
 		popup.hide();
 		tokenHistory.add(token);
 		Map<String, String> parameters = getHistoryTokenParameters(token);
 		AppPlace place = getPlace(token);
 
 		if (ClientSessionUtil.checkSession()) {
 			switch (place) {
 				case HOME:
 					HomePresenter homePresenter = new HomePresenter(new HomeView(), eventBus);
 					homePresenter.bind();
 					getLayout().clear();
 					getLayout().add(homePresenter.getDisplay().asWidget());
 					break;
 				case REQUEST:
 					RequestPresenter requestPresenter = new RequestPresenter(new RequestView(), eventBus);
 					requestPresenter.bind();
 					getLayout().clear();
 					getLayout().add(requestPresenter.getDisplay().asWidget());
 					break;
 				case LOGOUT:
 					ClientSessionUtil.destroySession();
 					eventBus.fireEvent(new LoginRequiredEvent());
 					break;
 				case REQUESTSTATUS:
 					try {
 						Integer requestId = Integer.parseInt(parameters.get("requestId"));
 						RequestStatusPresenter requestStatusPresenter = new RequestStatusPresenter(new RequestStatusView(), eventBus);
 						requestStatusPresenter.bind();
 						requestStatusPresenter.showRequest(requestId);
 						getLayout().clear();
 						getLayout().add(requestStatusPresenter.getDisplay().asWidget());
 					} catch (Exception e) {
 						Window.alert("Id incorrecta: No se puede cargar la solicitud");
 					}
 					break;
 				case RESPONSE:
 					try {
 						Integer requestId = Integer.parseInt(parameters.get("requestId"));
 						RequestResponsePresenter requestResponsePresenter = new RequestResponsePresenter(new RequestResponseView(), eventBus);
 						requestResponsePresenter.bind();
 						requestResponsePresenter.showRequest(requestId);
 						getLayout().clear();
 						getLayout().add(requestResponsePresenter.getDisplay().asWidget());
 					} catch (Exception e) {
 						Window.alert("Id incorrecta: No se puede cargar la solicitud");
 					}
 					break;
 				case LIST:
 					try {
 						String listType = parameters.get("type");
 						RequestListPresenter requestListPresenter = new RequestListPresenter(new RequestListView(), eventBus);
 						requestListPresenter.bind();
 						requestListPresenter.loadRequests(0, 100, listType);
 						getLayout().clear();
 						getLayout().add(requestListPresenter.getDisplay().asWidget());
 					} catch (Exception e) {
 						Window.alert("Tipo de lista incorrecto: No se puede cargar la lista");
 					}
 					break;
 				case STATISTICS:
 					StatisticsPresenter statistics = new StatisticsPresenter(new StatisticsView(), eventBus);
 					statistics.bind();
 					getLayout().clear();
 					getLayout().add(statistics.getDisplay().asWidget());
 					break;
 				default:
 					History.newItem(AppPlace.HOME.toString());
 			}
 		} else {
 			switch (place) {
 				case HOME:
 					HomePresenter homePresenter = new HomePresenter(new HomeView(), eventBus);
 					homePresenter.bind();
 					getLayout().clear();
 					getLayout().add(homePresenter.getDisplay().asWidget());
 					break;
 				case REQUEST:
 				case LOGIN:
 					LoginPresenter loginPresenter = new LoginPresenter(new LoginView(), eventBus);
 					loginPresenter.bind();
 					popup.setModal(true);
 					popup.setGlassEnabled(true);
 					popup.clear();
 					popup.add(loginPresenter.getDisplay().asWidget());
 					popup.center();
 					break;
 				case REGISTER:
 					RegisterPresenter registerPresenter = new RegisterPresenter(new RegisterView(), eventBus);
 					registerPresenter.bind();
 					getLayout().clear();
 					getLayout().add(registerPresenter.getDisplay().asWidget());
 					break;
 				case RESPONSE:
 					try {
 						Integer requestId = Integer.parseInt(parameters.get("requestId"));
 						RequestResponsePresenter requestResponsePresenter = new RequestResponsePresenter(new RequestResponseView(), eventBus);
 						requestResponsePresenter.bind();
 						requestResponsePresenter.showRequest(requestId);
 						getLayout().clear();
 						getLayout().add(requestResponsePresenter.getDisplay().asWidget());
 					} catch (Exception e) {
 						Window.alert("Id incorrecta: No se puede cargar la solicitud");
 					}
 					break;
 				case LIST:
 					try {
 						String listType = parameters.get("type");
 						RequestListPresenter requtesListPresenter = new RequestListPresenter(new RequestListView(), eventBus);
 						requtesListPresenter.bind();
 						requtesListPresenter.loadRequests(0, 100, listType);
 						getLayout().clear();
 						getLayout().add(requtesListPresenter.getDisplay().asWidget());
 					} catch (Exception e) {
 						Window.alert("Tipo de lista incorrecto: No se puede cargar la lista");
 					}
 					break;
 				case STATISTICS:
 					StatisticsPresenter statistics = new StatisticsPresenter(new StatisticsView(), eventBus);
 					statistics.bind();
 					getLayout().clear();
 					getLayout().add(statistics.getDisplay().asWidget());
 					break;
 				default:
 					History.newItem(AppPlace.HOME.toString());
 			}
 		}
 	}
 
 	public FlowPanel getLayout() {
 		return mainPresenter.getDisplay().getLayout();
 	}
 
 	/**
 	 * get historyToken parameters
 	 *
 	 * like domaint.tld#anchor?[var=1&var3=2&var3=3]
 	 *
 	 * @param historyToken anchor tag
 	 * @return hashmap of the parameters
 	 */
 	public static Map<String, String> getHistoryTokenParameters(String historyToken) {
 		// skip if there is no question mark
 		if (!historyToken.contains("?")) {
 			return null;
 		}
 		// ? position
 		int questionMarkIndex = historyToken.indexOf("?") + 1;
 
 		// get the sub string of parameters var=1&var2=2&var3=3...
 		String[] arStr = historyToken.substring(questionMarkIndex, historyToken.length()).split("&");
 		Map<String, String> params = new HashMap<String, String>();
 
 		for (int i = 0; i < arStr.length; i++) {
 			String[] substr = arStr[i].split("=");
 			params.put(substr[0], substr[1]);
 		}
 
 		return params;
 	}
 
 	public static List<String> getHistoryTokenList() {
 		return tokenHistory;
 	}
 
 	public static String getCurrentHistoryToken() {
 		return getHistoryTokenList().get(getHistoryTokenList().size() - 1);
 	}
 
 	public static String getPreviousHistoryToken() {
 		if(getHistoryTokenList().size() > 2) {
 			return getHistoryTokenList().get(getHistoryTokenList().size() - 2);
 		}
 		return AppPlace.HOME.getToken();
 	}
 
 	public AppPlace getPlace(String token) {
 		AppPlace place = AppPlace.HOME;
 
 		try {
 			place = AppPlace.valueOf(token.replaceFirst("^(.*)\\?.*$", "$1").toUpperCase());
 		} catch (Exception ex) {
 		}
 
 		return place;
 	}
 }
