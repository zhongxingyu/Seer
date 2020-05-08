 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.client.tablet.LocalStorage;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestEvent;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.Event;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class PtuTabSelector extends HorizontalPanel implements Module {
 
 	@SuppressWarnings("unused")
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private RemoteEventBus remoteEventBus;
 	private List<EventBus> eventBusses = new ArrayList<EventBus>();
 
 	private List<String> ptuIds;
 	private String selectedPtuId;
 	private String selectedTab;
 	private PtuSettings settings;
 	private InterventionMap interventions;
 	private List<String> extraTabs;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	public PtuTabSelector() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
		// add(new Brand("ATWSS"));
 
 		remoteEventBus = clientFactory.getRemoteEventBus();
 
 		String[] busNames = args.getArg(0).split(",");
 		for (int i = 0; i < busNames.length; i++) {
 			eventBusses.add(clientFactory.getEventBus(busNames[i].trim()));
 		}
 		extraTabs = args.getArgs(1);
 
 		selectedTab = LocalStorage.getInstance().get(LocalStorage.SELECTED_TAB);
 		selectedPtuId = LocalStorage.getInstance().get(LocalStorage.PTU_ID);
 
 		// listen to all event busses
 		for (final EventBus eventBus : eventBusses) {
 			RequestEvent.register(eventBus, new RequestEvent.Handler() {
 
 				@Override
 				public void onRequestEvent(RequestEvent event) {
 					String eventType = event.getRequestedClassName();
 					if (eventType.equals(SelectPtuEvent.class.getName())) {
 						eventBus.fireEvent(new SelectPtuEvent(selectedPtuId));
 					} else if (eventType.equals(SelectTabEvent.class.getName())) {
 						eventBus.fireEvent(new SelectTabEvent(selectedTab));
 					}
 				}
 			});
 		}
 
 		PtuSettingsChangedRemoteEvent.subscribe(remoteEventBus,
 				new PtuSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedRemoteEvent event) {
 						settings = event.getPtuSettings();
 
 						scheduler.update();
 					}
 				});
 
 		InterventionMapChangedRemoteEvent.subscribe(remoteEventBus,
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 
 						interventions = event.getInterventionMap();
 
 						ptuIds = interventions.getPtuIds();
 
 						scheduler.update();
 
 						if (selectedTab != null) {
 							fireEvent(new SelectTabEvent(
 									selectedPtuId == null ? selectedTab : "Ptu"));
 							fireEvent(new SelectPtuEvent(selectedPtuId));
 						}
 					}
 				});
 
 		return true;
 	}
 
 	private String getName(String id) {
 		return (interventions != null) && (interventions.get(id) != null)
 				&& !interventions.get(id).getName().equals("") ? interventions
 				.get(id).getName() + " (" + id.toString() + ")" : id.toString();
 	}
 
 	@Override
 	public boolean update() {
 		clear();
 		if (ptuIds != null) {
 
 			Collections.sort(ptuIds);
 
 			for (Iterator<String> i = ptuIds.iterator(); i.hasNext();) {
 				final String id = i.next();
 				if ((settings != null) && !settings.isEnabled(id))
 					continue;
 
 				final ToggleButton b = new ToggleButton(getName(id));
 				b.setDown(id.equals(selectedTab));
 
 				b.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 
 						selectedTab = id;
 						selectedPtuId = id;
 
 						radio(b);
 
 						fireEvent(new SelectTabEvent("Ptu"));
 
 						LocalStorage.getInstance().put(
 								LocalStorage.SELECTED_TAB, selectedTab);
 						fireEvent(new SelectPtuEvent(selectedPtuId));
 					}
 				});
 				add(b);
 			}
 		}
 
 		for (Iterator<String> i = extraTabs.iterator(); i.hasNext();) {
 			final String name = i.next();
 			final ToggleButton b = new ToggleButton(name);
 			b.setDown(name.equals(selectedTab));
 
 			b.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					selectedTab = name;
 					selectedPtuId = null;
 
 					radio(b);
 
 					fireEvent(new SelectTabEvent(selectedTab));
 
 					LocalStorage.getInstance().put(LocalStorage.SELECTED_TAB,
 							selectedTab);
 					fireEvent(new SelectPtuEvent(selectedPtuId));
 				}
 			});
 			add(b);
 		}
 
 		return false;
 	}
 
 	private void radio(ToggleButton b) {
 		for (int i = 0; i < getWidgetCount(); i++) {
 			Widget w = getWidget(i);
 			if (w instanceof ToggleButton) {
 				ToggleButton t = (ToggleButton) w;
 				t.setDown(w == b);
 			}
 		}
 	}
 
 	private void fireEvent(Event<?> event) {
 		for (Iterator<EventBus> i = eventBusses.iterator(); i.hasNext();) {
 			i.next().fireEvent(event);
 		}
 	}
 }
