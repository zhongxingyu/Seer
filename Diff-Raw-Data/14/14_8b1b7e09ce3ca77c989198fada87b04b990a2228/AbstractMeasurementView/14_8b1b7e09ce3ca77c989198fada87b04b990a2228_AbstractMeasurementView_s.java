 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.HistoryMap;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public abstract class AbstractMeasurementView extends GlassPanel implements
 		Module {
 
 	protected static NumberFormat format = NumberFormat.getFormat("0.00");
 
 	protected HistoryMap historyMap;
 	protected InterventionMap interventions;
 	protected Measurement last = new Measurement();
 	protected ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	protected SingleSelectionModel<String> selectionModel;
 
 	protected String ptuId = null;
 
 	protected boolean showHeader = true;
 	protected boolean showName = true;
 	protected boolean selectable = true;
 	protected boolean showDate = false;
 
 	protected String options;
 
 	private List<String> show = null;
 
 	private RemoteEventBus remoteEventBus;
 	private EventBus cmdBus;
 
 	private HandlerRegistration measurementHandler;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	private Ternary daqOk = Ternary.Unknown;
 	private Ternary databaseConnect = Ternary.Unknown;
 
 	public AbstractMeasurementView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		remoteEventBus = clientFactory.getRemoteEventBus();
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		options = args.getArg(1);
 		show = args.getArgs(2);
 
 		showHeader = !options.contains("NoHeader");
 		showName = !options.contains("NoName");
 
 		selectable = !options.contains("NoSelection");
 		showDate = options.contains("Date");
 
 		if (selectable) {
 			selectionModel = new SingleSelectionModel<String>();
 		}
 
 		ConnectionStatusChangedRemoteEvent.subscribe(remoteEventBus,
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						switch (event.getConnection()) {
 						case daq:
 							daqOk = event.getStatus();
 							break;
 						case databaseConnect:
 							databaseConnect = event.getStatus();
 							break;
 						default:
 							break;
 						}
 
 						showGlass(daqOk.not().or(databaseConnect.not()).isTrue());
 					}
 				});
 
 		InterventionMapChangedRemoteEvent.subscribe(remoteEventBus,
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 						interventions = event.getInterventionMap();
 						changePtuId();
 						scheduler.update();
 					}
 				});
 
 		HistoryMapChangedEvent.subscribe(clientFactory,
 				new HistoryMapChangedEvent.Handler() {
 
 					@Override
 					public void onHistoryMapChanged(HistoryMapChangedEvent event) {
 						historyMap = event.getHistoryMap();
 						changePtuId();
 						scheduler.update();
 					}
 				});
 
 		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(final SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 
 				changePtuId();
 				scheduler.update();
 			}
 		});
 
 		return true;
 	}
 
 	/**
 	 * Decorate with arrow up, down, left if a value went up, down or stayed the
 	 * same. Only applies to last value. Also calls standard decorate method.
 	 * 
 	 * @param s
 	 * @param current
 	 * @param last
 	 * @return
 	 */
 	public static SafeHtml decorate(String s, Measurement current,
 			Measurement last) {
 		if ((current != null) && (last != null) && (current.getPtuId() != null)
 				&& (current.getPtuId().equals(last.getPtuId()))
 				&& (current.getName() != null)
 				&& current.getName().equals(last.getName())) {
 			double c = current.getValue().doubleValue();
 			double l = last.getValue().doubleValue();
 			String a = (c == l) ? "&larr;" : (c > l) ? "&uarr;" : "&darr;";
 			s = a + "&nbsp;<b>" + s + "</b>";
 		}
 		return decorate(s, current);
 	}
 
 	/**
 	 * Adds date/time as tooltip. Shows future values (beyond 1 minute) in bold,
 	 * values older than 5 minutes in italics and makes values older than a day
 	 * more transparent.
 	 * 
 	 * @param s
 	 * @param current
 	 * @return
 	 */
 	public static SafeHtml decorate(String s, Measurement current) {
 		long now = new Date().getTime();
 		long future1min = now + (60 * 1000);
 		long past5mins = now - (5 * 60 * 1000);
 		long pastDay = now - (24 * 3600 * 1000);
 		long time = current.getDate().getTime();
 		if (time > future1min) {
 			s = "<b>" + s + "</b>";
 		} else if (time < past5mins) {
 			s = "<i>" + s + "</i>";
 		}
 
 		// make text more transparent
 		if (time < pastDay) {
 			s = "<span style=\"opacity: 0.5;\">" + s + "</span>";
 		}
 		// Add date in tooltip
 		s = "<div title=\""
 				+ PtuClientConstants.dateFormat.format(current.getDate())
 				+ "\">" + s + "</div>";
 		return SafeHtmlUtils.fromSafeConstant(s);
 	}
 
 	private void changePtuId() {
 		dataProvider.getList().clear();
 		last = null;
 		if (measurementHandler != null) {
 			measurementHandler.removeHandler();
 			measurementHandler = null;
 		}
 
 		if (interventions == null) {
 			return;
 		}
 
 		if (historyMap == null) {
 			return;
 		}
 
 		for (String ptuId : interventions.getPtuIds()) {
 			for (Measurement measurement : historyMap.getMeasurements(ptuId)) {
 				replace(measurement);
 			}
 		}
 		scheduler.update();
 
 		measurementHandler = MeasurementChangedEvent.register(remoteEventBus,
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement measurement = event.getMeasurement();
 						if (measurement.getPtuId().equals(ptuId)) {
 							last = replace(measurement);
 							scheduler.update();
 						}
 					}
 				});
 
 	}
 
 	@Override
 	public boolean update() {
 		if (selectable) {
 			String selection = selectionModel.getSelectedObject();
 
 			if ((selection == null) && (dataProvider.getList().size() > 0)) {
 				selection = dataProvider.getList().get(0);
 
 				selectMeasurement(selection);
 			}
 		}
 
 		return false;
 	}
 
 	private Measurement replace(Measurement measurement) {
 
 		List<String> list = dataProvider.getList();
 		Measurement lastValue = historyMap.getMeasurement(
 				measurement.getPtuId(), measurement.getName());
 
 		if (!list.contains(measurement.getName())) {
 			if ((show == null) || (show.size() == 0)
 					|| (show.contains(measurement.getName()))) {
 				list.add(measurement.getName());
 				lastValue = measurement;
 			} else {
 				lastValue = null;
 			}
 		}
 
 		return lastValue;
 	}
 
 	protected void selectMeasurement(String name) {
 		SelectMeasurementEvent.fire(cmdBus, name);
 	}
 }
