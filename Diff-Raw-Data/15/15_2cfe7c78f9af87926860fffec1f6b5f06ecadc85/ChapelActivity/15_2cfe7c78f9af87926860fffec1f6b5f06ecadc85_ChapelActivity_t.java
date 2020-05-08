 package com.ritchey.portal.client.activity;
 
 import java.util.Date;
 import java.util.Map;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.ritchey.portal.client.Chapel;
 import com.ritchey.portal.client.ClientFactory;
 import com.ritchey.portal.client.PortalServiceAsync;
 import com.ritchey.portal.client.place.ChapelPlace;
 import com.ritchey.portal.client.ui.ChapelView;
 import com.ritchey.portal.shared.ListCount;
 import com.ritchey.portal.shared.Log;
 import com.ritchey.portal.shared.SimpleLoginType;
 
 public class ChapelActivity extends BasePresenter implements
 		ChapelView.Presenter {
 
 	private ClientFactory clientFactory;
 	private SimpleLoginType instructor;
 	private PortalServiceAsync rpcService;
 	private final EventBus eventBus;
 	private ChapelView chapelView;
 	private ChapelView display;
 	private final int limit = Chapel.getAppConstants().queryPerPage();
 	private Date term;
 
 	public void getTerm() {
 		if (clientFactory.getTerm() != null) {
 			term = clientFactory.getTerm();
 			return;
 		}
 		
 		AsyncCallback<Date> callback = new AsyncCallback<Date>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Initialize " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(Date result) {
 				term = result;
 				clientFactory.setTerm(result);
 			}
 			
 		};
 		clientFactory.getRpcService().getStartTerm(callback);
 }
 	
 	public void getName() {
 		this.instructor = ((ChapelPlace) clientFactory.getPlaceController().getWhere()).getStudent();
 		if (instructor.getName() != null) {
			this.display.setPerson(instructor.getName());
 			return;
 		}
 		AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(String result) {
 				instructor.setName(result);
 				Log.debug("NAME IS " + result);
 				display.setPerson(result);
 			}
 			
 		};
 		clientFactory.getRpcService().getName(instructor.getPeopleId(), callback);
 	}
 	
 	public ChapelActivity(ChapelPlace place, ClientFactory clientFactory) {
 		this.instructor = place.getStudent();
 
 		this.clientFactory = clientFactory;
 		this.rpcService = clientFactory.getRpcService();
 		this.eventBus = clientFactory.getEventBus();
 		this.display = clientFactory.getChapelView();
 		
 		bind();
 	}
 	
 	@Override
 	public void bind() {
 		AsyncDataProvider<Map> provider = new AsyncDataProvider<Map>() {
 			@Override
 			protected void onRangeChanged(HasData<Map> display) {
 				getClassList(display, this);
 			}
 		};
 		provider.addDataDisplay(display.getTblClassList());
 		
 		AsyncDataProvider<Map> providerTardy = new AsyncDataProvider<Map>() {
 			@Override
 			protected void onRangeChanged(HasData<Map> display) {
 				getTardyList(display, this);
 			}
 		};
 		providerTardy.addDataDisplay(display.getTblTardyList());
 	}
 
 	private void getClassList(HasData<Map> display,
 			final AsyncDataProvider<Map> dp) {
 		
 		this.instructor = ((ChapelPlace) clientFactory.getPlaceController().getWhere()).getStudent();
 		Log.debug("instructor = " + instructor);
 		
 		final int start = display.getVisibleRange().getStart();
 		int length = display.getVisibleRange().getLength();
 		AsyncCallback<ListCount<Map>> callback = new AsyncCallback<ListCount<Map>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(ListCount<Map> result) {
 				Log.debug("result count = " + result.getCount());
 				Log.debug("result data");
 				for (Map s: result.getData()) {
 					Log.debug("course = " + s);
 				}
 				dp.updateRowCount(result.getCount(), /* exact(not estimate) = */
 						true);
 				dp.updateRowData(start, result.getData());
 				setTotalPresent( result.getEnv());
 				
 				Log.debug("SUCCESS DONE");
 			}
 		};
 		Log.debug("rpcSerice = " + rpcService);
 		
 		rpcService.getPresent(instructor.getPeopleCodeId(), term, start, limit, callback);
 	}
 	
 	Double totalPresent;
 	Double totalTardy;
 	private Object goal;
 	private void setTotalPresent(Map present) {
 		totalPresent = (Double) present.get("total");
 		goal = present.get("goal");
 		display.setLblPresent("Present: " + totalPresent);
 		display.setLblGoal("Goal: " + ((goal!=null)?goal.toString():""));
 		if (totalTardy != null) {
 			Double total = totalTardy+ totalPresent;
 			display.setLblTotal("Total: " + total);
 		}
 	}
 	
 
 	private void setTotalTardy(Double tardy) {
 		totalTardy = tardy;
 		display.setLblTardy("Tardy: " + tardy);
 		if (totalPresent != null) {
 			Double total = totalTardy+ totalPresent;
 			display.setLblTotal("Total: " + total);
 		}
 	}
 	
 	private void getTardyList(HasData<Map> display,
 			final AsyncDataProvider<Map> dp) {
 		
 		this.instructor = ((ChapelPlace) clientFactory.getPlaceController().getWhere()).getStudent();
 		Log.debug("instructor = " + instructor);
 		
 		final int start = display.getVisibleRange().getStart();
 		int length = display.getVisibleRange().getLength();
 		AsyncCallback<ListCount<Map>> callback = new AsyncCallback<ListCount<Map>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(ListCount<Map> result) {
 				Log.debug("result count = " + result.getCount());
 				Log.debug("result data");
 				for (Map s: result.getData()) {
 					Log.debug("course = " + s);
 				}
 				dp.updateRowCount(result.getCount(), /* exact(not estimate) = */
 						true);
 				dp.updateRowData(start, result.getData());
 				
 				setTotalTardy((Double) result.getEnv().get("total"));
 				
 				Log.debug("SUCCESS DONE");
 			}
 
 		};
 		Log.debug("rpcSerice = " + rpcService);
 		
 		rpcService.getTardies(instructor.getPeopleCodeId(), term, start, limit, callback);
 	}
 
 	/**
 	 * Invoked by the ActivityManager to start a new Activity
 	 */
 	@Override
 	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
 		Log.debug("Start Activity");
 		
 		getTerm();
 		getName();
 
 		chapelView = clientFactory.getChapelView();
 		
 		
 		ChapelPlace place = (ChapelPlace) clientFactory.getPlaceController()
 				.getWhere();
 		instructor = place.getStudent();
 		Log.debug("start of term = " + term);
 		
 		chapelView.setPresenter(this);
 		containerWidget.setWidget(chapelView.asWidget());
 	}
 
 	/**
 	 * Ask user before stopping this activity
 	 */
 	@Override
 	public String mayStop() {
 		//return "Please hold on. This activity is stopping.";
 		return null;
 	}
 
 	/**
 	 * Navigate to a new Place in the browser
 	 */
 	public void goTo(Place place) {
 		clientFactory.getPlaceController().goTo(place);
 	}
 }
