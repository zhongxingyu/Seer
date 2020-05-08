 package com.ezhang.pop.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import com.ezhang.pop.core.StateMachine;
 import com.ezhang.pop.core.StateMachine.EventAction;
 import com.ezhang.pop.model.DestinationList;
 import com.ezhang.pop.model.DistanceMatrix;
 import com.ezhang.pop.model.DistanceMatrixItem;
 import com.ezhang.pop.model.FuelDistanceItem;
 import com.ezhang.pop.model.FuelInfo;
 import com.ezhang.pop.rest.PopRequestFactory;
 import com.ezhang.pop.rest.PopRequestManager;
 import com.ezhang.pop.settings.AppSettings;
 import com.foxykeep.datadroid.requestmanager.Request;
 import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
 
 public class FuelStateMachine extends Observable implements RequestListener {
 
 	private static class TimerHandler extends Handler {
 		FuelStateMachine m_fuelStateMachine = null;
 
 		public TimerHandler(FuelStateMachine fuelStateMachine) {
 			m_fuelStateMachine = fuelStateMachine;
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			m_fuelStateMachine.m_stateMachine
 					.HandleEvent(EmEvent.Timeout, null);
 		}
 	}
 
 	enum EmState {
 		Start, GeoLocationRecieved, SuburbRecieved, FuelInfoRecieved, DistanceRecieved, Timeout
 	}
 
 	enum EmEvent {
 		Invalid, GeoLocationEvent, SuburbEvent, FuelInfoEvent, DistanceEvent, Refresh, Timeout, RecalculatePrice
 	}
 
 	StateMachine<EmState, EmEvent> m_stateMachine = new StateMachine<EmState, EmEvent>(
 			EmState.Start);
 	public ArrayList<FuelDistanceItem> m_fuelDistanceItems = new ArrayList<FuelDistanceItem>();
 	private List<FuelInfo> m_fuelInfoList = null;
 	private PopRequestManager m_restReqManager;
 	private LocationManager m_locationManager;
 	public Location m_location = null;
 	public String m_suburb = null;
 	public String m_address = null;
 	private String m_provider = null;
 	public EmEvent m_timeoutEvent = EmEvent.Invalid;
 	Timer m_timer = null;
 	TimerTask m_timerTask = null;
 	Handler m_timeoutHandler = new TimerHandler(this);
 	AppSettings m_settings = null;
 
 	public FuelStateMachine(PopRequestManager reqManager,
 			LocationManager locationManager, AppSettings settings) {
 
 		m_restReqManager = reqManager;
 		m_locationManager = locationManager;
 		m_settings = settings;
 
 		InitStateMachineTransitions();
 
 		m_provider = GetBestProvider();
 		if (m_provider != null) {
 			m_locationManager.requestLocationUpdates(m_provider, 60 * 1000L,
 					20.0f, this.m_locationListener);
 		}
 
 		this.m_location = this.m_locationManager
 				.getLastKnownLocation(m_provider);
 
 		Refresh();
 	}
 
 	public void Refresh() {
 		this.m_stateMachine.HandleEvent(EmEvent.Refresh, null);
 	}
 
 	private void StartTimer(int millSeconds) {
 		StopTimer();
 
 		if (m_timer == null) {
 			m_timer = new Timer();
 		}
 
 		if (m_timerTask == null) {
 			m_timerTask = new TimerTask() {
 				public void run() {
 					m_timeoutHandler.sendMessage(new Message());
 				}
 			};
 		}
 
 		if (m_timer != null && m_timerTask != null) {
 			m_timer.schedule(m_timerTask, millSeconds);
 		}
 	}
 
 	private void StopTimer() {
 		if (m_timer != null) {
 			m_timer.cancel();
 			m_timer = null;
 		}
 
 		if (m_timerTask != null) {
 			m_timerTask.cancel();
 			m_timerTask = null;
 		}
 	}
 
 	private void InitStateMachineTransitions() {
 		m_stateMachine.AddTransition(EmState.Start,
 				EmState.GeoLocationRecieved, EmEvent.GeoLocationEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestSuburb();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.Start, EmState.Start,
 				EmEvent.Refresh, new EventAction() {
 					public void PerformAction(Bundle param) {
 						Start();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.Start, EmState.Timeout,
 				EmEvent.Timeout, new EventAction() {
 					public void PerformAction(Bundle param) {
 						m_timeoutEvent = EmEvent.GeoLocationEvent;
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.Timeout, EmState.Start,
 				EmEvent.Refresh, new EventAction() {
 					public void PerformAction(Bundle param) {
 						Start();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.GeoLocationRecieved,
 				EmState.SuburbRecieved, EmEvent.SuburbEvent, new EventAction() {
 					public void PerformAction(Bundle param) {
 						m_suburb = param
 								.getString(PopRequestFactory.BUNDLE_CUR_SUBURB_DATA);
 						m_address = param
 								.getString(PopRequestFactory.BUNDLE_CUR_ADDRESS_DATA);
 						if (m_suburb == "") {
 							m_stateMachine
 									.SetState(EmState.GeoLocationRecieved);
 							RequestSuburb();
 							return;
 						}
 						RequestFuelInfo();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.GeoLocationRecieved,
 				EmState.Timeout, EmEvent.Timeout, new EventAction() {
 					public void PerformAction(Bundle param) {
						m_timeoutEvent = EmEvent.SuburbEvent;
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.GeoLocationRecieved,
 				EmState.GeoLocationRecieved, EmEvent.Refresh,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						if (m_suburb == null) {
 							RequestSuburb();
 						} else {
 							m_stateMachine.SetState(EmState.SuburbRecieved);
 							RequestFuelInfo();
 						}
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.SuburbRecieved,
 				EmState.FuelInfoRecieved, EmEvent.FuelInfoEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						m_fuelInfoList = param
 								.getParcelableArrayList(PopRequestFactory.BUNDLE_FUEL_DATA);
 						RequestDistanceMatrix(m_fuelInfoList);
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.SuburbRecieved,
 				EmState.SuburbRecieved, EmEvent.Refresh, new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestFuelInfo();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.SuburbRecieved,
 				EmState.GeoLocationRecieved, EmEvent.GeoLocationEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestSuburb();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.SuburbRecieved, EmState.Timeout,
 				EmEvent.Timeout, new EventAction() {
 					public void PerformAction(Bundle param) {
 						m_timeoutEvent = EmEvent.FuelInfoEvent;
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.FuelInfoRecieved,
 				EmState.DistanceRecieved, EmEvent.DistanceEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						StopTimer();
 						DistanceMatrix distanceMatrix = param
 								.getParcelable(PopRequestFactory.BUNDLE_DISTANCE_MATRIX_DATA);
 						OnDistanceMatrixRecieved(distanceMatrix);
 						Notify();
 					}
 				});
 		m_stateMachine.AddTransition(EmState.FuelInfoRecieved,
 				EmState.SuburbRecieved, EmEvent.Refresh, new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestFuelInfo();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.FuelInfoRecieved,
 				EmState.GeoLocationRecieved, EmEvent.GeoLocationEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestSuburb();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.FuelInfoRecieved, EmState.Timeout,
 				EmEvent.Timeout, new EventAction() {
 					public void PerformAction(Bundle param) {
 						m_timeoutEvent = EmEvent.DistanceEvent;
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.DistanceRecieved,
 				EmState.GeoLocationRecieved, EmEvent.GeoLocationEvent,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestSuburb();
 						Notify();
 					}
 				});
 
 		m_stateMachine.AddTransition(EmState.DistanceRecieved,
 				EmState.SuburbRecieved, EmEvent.Refresh, new EventAction() {
 					public void PerformAction(Bundle param) {
 						RequestFuelInfo();
 						Notify();
 					}
 				});
 		m_stateMachine.AddTransition(EmState.DistanceRecieved,
 				EmState.DistanceRecieved, EmEvent.RecalculatePrice,
 				new EventAction() {
 					public void PerformAction(Bundle param) {
 						OnRecalculatePrice();
 						Notify();
 					}
 				});
 	}
 
 	private void OnRecalculatePrice() {
 		for (FuelDistanceItem item : this.m_fuelDistanceItems) {
 			if (item.voucherType != null && item.voucherType != "") {
 				if (item.voucherType == "wws") {
 					if (m_settings.m_wwsDiscount != item.voucher) {
 						item.price += item.voucher
 								- m_settings.m_wwsDiscount;
 						item.voucher = m_settings.m_wwsDiscount;
 					}
 				}
 				if (item.voucherType == "coles") {
 					if (m_settings.m_colesDiscount != item.voucher) {
 						item.price += item.voucher
 								- m_settings.m_colesDiscount;
 						item.voucher = m_settings.m_colesDiscount;
 					}
 				}
 			}
 		}
 	}
 
 	private void OnDistanceMatrixRecieved(DistanceMatrix distanceMatrix) {
 		m_fuelDistanceItems.clear();
 		int i = 0;
 		for (DistanceMatrixItem distanceItem : distanceMatrix
 				.GetDistanceItems()) {
 			FuelDistanceItem item = new FuelDistanceItem();
 			item.distance = distanceItem.distance;
 			item.distanceValue = distanceItem.distanceValue;
 			item.duration = distanceItem.duration;
 			FuelInfo fuelInfo = this.m_fuelInfoList.get(i);
 			item.tradingName = fuelInfo.tradingName;
 			item.price = fuelInfo.price;
 			String lowTradingName = item.tradingName
 					.toLowerCase(Locale.ENGLISH);
 			if (lowTradingName.contains("woolworths")
 					&& m_settings.m_wwsDiscount > 0) {
 				item.price -= m_settings.m_wwsDiscount;
 				item.voucher = m_settings.m_wwsDiscount;
 				item.voucherType = "wws";
 			} else if (lowTradingName.contains("coles")
 					&& m_settings.m_colesDiscount > 0) {
 				item.price -= m_settings.m_colesDiscount;
 				item.voucher = m_settings.m_colesDiscount;
 				item.voucherType = "coles";
 			} else {
 				item.voucherType = "";
 			}
 			item.latitude = fuelInfo.latitude;
 			item.longitude = fuelInfo.longitude;
 			item.destinationAddr = fuelInfo.address;
 			m_fuelDistanceItems.add(item);
 			i++;
 		}
 		Notify();
 	}
 
 	@Override
 	public void onRequestFinished(Request request, Bundle resultData) {
 		if (request.getRequestType() == PopRequestFactory.REQ_TYPE_DISTANCE_MATRIX) {
 			this.m_stateMachine.HandleEvent(EmEvent.DistanceEvent, resultData);
 		}
 
 		if (request.getRequestType() == PopRequestFactory.REQ_TYPE_GET_CUR_SUBURB) {
 			this.m_stateMachine.HandleEvent(EmEvent.SuburbEvent, resultData);
 		}
 
 		if (request.getRequestType() == PopRequestFactory.REQ_TYPE_FUEL) {
 			this.m_stateMachine.HandleEvent(EmEvent.FuelInfoEvent, resultData);
 		}
 	}
 
 	@Override
 	public void onRequestConnectionError(Request request, int statusCode) {
 	}
 
 	@Override
 	public void onRequestDataError(Request request) {
 	}
 
 	@Override
 	public void onRequestCustomError(Request request, Bundle resultData) {
 	}
 
 	private void RequestSuburb() {
 		StartTimer(5000);
 		Request req = PopRequestFactory.GetCurrentSuburbRequest(m_location);
 		m_restReqManager.execute(req, this);
 	}
 
 	private void RequestFuelInfo() {
 		StartTimer(5000);
 		Request req = PopRequestFactory.GetFuelInfoRequest(m_suburb,
 				m_settings.IncludeSurroundings(), m_settings.GetFuelType());
 		m_restReqManager.execute(req, this);
 	}
 
 	private void RequestDistanceMatrix(List<FuelInfo> fuelInfoList) {
 		if (m_location == null) {
 			m_location = this.m_locationManager
 					.getLastKnownLocation(m_provider);
 		}
 		if (m_location != null) {
 			DestinationList dests = new DestinationList();
 			for (FuelInfo item : fuelInfoList) {
 				dests.AddDestination(item.latitude, item.longitude);
 			}
 			String src = String.format("%s,%s", this.m_location.getLatitude(),
 					this.m_location.getLongitude());
 			StartTimer(5000);
 			Request req = PopRequestFactory
 					.GetDistanceMatrixRequest(src, dests);
 			m_restReqManager.execute(req, this);
 		} else {
 			// TODO: Still waiting location data.
 		}
 	}
 
 	private String GetBestProvider() {
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 
 		String provider = m_locationManager.getBestProvider(criteria, true);
 		return provider;
 	}
 
 	private final LocationListener m_locationListener = new LocationListener() {
 		public void onLocationChanged(Location location) { // ıʱ˺Providerͬ꣬Ͳᱻ
 			// log it when the location changes
 			if (location != null) {
 				if (m_location == null || !location.equals(m_location)) {
 					m_location = location;
 					m_suburb = null;
 					m_fuelInfoList = null;
 					m_fuelDistanceItems.clear();
 					m_stateMachine.HandleEvent(EmEvent.GeoLocationEvent, null);
 				}
 			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			// Providerdisableʱ˺GPSر
 		}
 
 		public void onProviderEnabled(String provider) {
 			// Providerenableʱ˺GPS
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// Providerת̬ڿáʱú޷״ֱ̬лʱ˺
 		}
 	};
 
 	public EmState GetCurState() {
 		// TODO Auto-generated method stub
 		return this.m_stateMachine.GetState();
 	}
 
 	private void Notify() {
 		setChanged();
 		notifyObservers();
 	}
 
 	private void Start() {
 		if (m_location == null) {
 			StartTimer(3 * 60 * 1000);
 			Notify();
 			return;
 		}
 		if (m_suburb == null) {
 			m_stateMachine.SetState(EmState.GeoLocationRecieved);
 			RequestSuburb();
 		} else {
 			m_stateMachine.SetState(EmState.SuburbRecieved);
 			RequestFuelInfo();
 		}
 		Notify();
 	}
 
 	public void ReCalculatePrice() {
 		this.m_stateMachine.HandleEvent(EmEvent.RecalculatePrice, null);
 	}
 }
