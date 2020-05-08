 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.client.widget.ActiveCheckboxCell;
 import ch.cern.atlas.apvs.client.widget.DynamicSelectionCell;
 import ch.cern.atlas.apvs.client.widget.StringList;
 import ch.cern.atlas.apvs.client.widget.VerticalFlowPanel;
 import ch.cern.atlas.apvs.dosimeter.shared.DosimeterPtuChangedEvent;
 import ch.cern.atlas.apvs.dosimeter.shared.DosimeterSerialNumbersChangedEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.ptu.shared.PtuIdsChangedEvent;
 
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.cell.client.TextInputCell;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 
 /**
  * Shows a list of PTU settings which are alive. A list of ever alive PTU
  * settings is persisted.
  * 
  * @author duns
  * 
  */
 public class PtuSettingsView extends VerticalFlowPanel {
 
 	private ListDataProvider<Integer> dataProvider = new ListDataProvider<Integer>();
 	private CellTable<Integer> table = new CellTable<Integer>();
 	private ListHandler<Integer> columnSortHandler;
 
 	protected PtuSettings settings = new PtuSettings();
 	protected List<Integer> dosimeterSerialNumbers = new ArrayList<Integer>();
 
 	public PtuSettingsView(final RemoteEventBus eventBus) {
 		add(table);
 
 		// ACTIVE
 		Column<Integer, Boolean> active = new Column<Integer, Boolean>(
 				new ActiveCheckboxCell()) {
 			@Override
 			public Boolean getValue(Integer object) {
				return settings.isEnabled(object);
 			}			
 		};
 		active.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		active.setSortable(true);
 		table.addColumn(active, "Active");
 
 		// ENABLED
 		Column<Integer, Boolean> enabled = new Column<Integer, Boolean>(
 				new CheckboxCell()) {
 			@Override
 			public Boolean getValue(Integer object) {
 				return settings.isEnabled(object);
 			}
 		};
 		enabled.setFieldUpdater(new FieldUpdater<Integer, Boolean>() {
 
 			@Override
 			public void update(int index, Integer object, Boolean value) {
 				settings.setEnabled(object, value);
 				fireSettingsChangedEvent(eventBus, settings);
 			}
 		});
 		enabled.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		enabled.setSortable(true);
 		table.addColumn(enabled, "Enabled");
 
 		// PTU ID
 		Column<Integer, Number> ptuId = new Column<Integer, Number>(
 				new NumberCell(NumberFormat.getFormat("0"))) {
 			@Override
 			public Number getValue(Integer object) {
 				return object;
 			}
 		};
 		ptuId.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		ptuId.setSortable(true);
 		table.addColumn(ptuId, "PTU ID");
 
 		// NAME
 		Column<Integer, String> name = new Column<Integer, String>(
 				new TextInputCell()) {
 			@Override
 			public String getValue(Integer object) {
 				return settings.getName(object);
 			}
 		};
 		name.setFieldUpdater(new FieldUpdater<Integer, String>() {
 
 			@Override
 			public void update(int index, Integer object, String value) {
 				settings.setName(object, value);
 				fireSettingsChangedEvent(eventBus, settings);
 			}
 		});
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(true);
 		table.addColumn(name, "Name");
 
 		// DOSIMETER
 		Column<Integer, String> dosimeter = new Column<Integer, String>(
 				new DynamicSelectionCell(new StringList<Integer>(
 						dosimeterSerialNumbers))) {
 
 			@Override
 			public String getValue(Integer object) {
 				return settings.getDosimeterSerialNumber(object).toString();
 			}
 		};
 		dosimeter.setFieldUpdater(new FieldUpdater<Integer, String>() {
 
 			@Override
 			public void update(int index, Integer object, String value) {
 				settings.setDosimeterSerialNumber(object,
 						Integer.parseInt(value));
 				fireSettingsChangedEvent(eventBus, settings);
 			}
 		});
 		dosimeter.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		dosimeter.setSortable(true);
 		table.addColumn(dosimeter, "Dosimeter #");
 
 		// HELMET URL
 		Column<Integer, String> helmetUrl = new Column<Integer, String>(
 				new TextInputCell()) {
 			@Override
 			public String getValue(Integer object) {
 				return settings.getHelmetUrl(object);
 			}
 		};
 		helmetUrl.setFieldUpdater(new FieldUpdater<Integer, String>() {
 
 			@Override
 			public void update(int index, Integer object, String value) {
 				settings.setHelmetUrl(object, value);
 				fireSettingsChangedEvent(eventBus, settings);
 			}
 		});
 		helmetUrl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		helmetUrl.setSortable(true);
 		table.addColumn(helmetUrl, "Helmet Camera URL");
 
 		// HAND URL
 		Column<Integer, String> handUrl = new Column<Integer, String>(
 				new TextInputCell()) {
 			@Override
 			public String getValue(Integer object) {
 				return settings.getHandUrl(object);
 			}
 		};
 		handUrl.setFieldUpdater(new FieldUpdater<Integer, String>() {
 
 			@Override
 			public void update(int index, Integer object, String value) {
 				settings.setHandUrl(object, value);
 				fireSettingsChangedEvent(eventBus, settings);
 			}
 		});
 		handUrl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		handUrl.setSortable(true);
 		table.addColumn(handUrl, "Hand Camera URL");
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<Integer>());
 
 		// SORTING
 		columnSortHandler = new ListHandler<Integer>(dataProvider.getList());
 		columnSortHandler.setComparator(ptuId, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return o1 != null ? o1.compareTo(o2) : -1;
 			}
 		});
 		columnSortHandler.setComparator(enabled, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return settings.isEnabled(o1).compareTo(settings.isEnabled(o2));
 			}
 		});
 		columnSortHandler.setComparator(name, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return settings.getName(o1).compareTo(settings.getName(o2));
 			}
 		});
 		columnSortHandler.setComparator(dosimeter, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return settings.getDosimeterSerialNumber(o1).compareTo(
 						settings.getDosimeterSerialNumber(o2));
 			}
 		});
 		columnSortHandler.setComparator(helmetUrl, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return settings.getHelmetUrl(o1).compareTo(
 						settings.getHelmetUrl(o2));
 			}
 		});
 		columnSortHandler.setComparator(handUrl, new Comparator<Integer>() {
 			public int compare(Integer o1, Integer o2) {
 				return settings.getHandUrl(o1).compareTo(
 						settings.getHandUrl(o2));
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(ptuId);
 
 		PtuSettingsChangedEvent.subscribe(eventBus,
 				new PtuSettingsChangedEvent.Handler() {
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedEvent event) {
 						System.err.println("PTU Settings changed");
 						settings = event.getPtuSettings();
 
 						mergeList(settings.getPtuIds());
 
 						update();
 					}
 				});
 
 		PtuIdsChangedEvent.subscribe(eventBus,
 				new PtuIdsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuIdsChanged(PtuIdsChangedEvent event) {
 						System.err.println("PTU IDS changed");
						List<Integer> newPtuIds = mergeList(event.getPtuIds());
 						for (Iterator<Integer> i = newPtuIds.iterator(); i
 								.hasNext();) {
 							settings.add(i.next());
 						}

 						if (!newPtuIds.isEmpty()) {
 							fireSettingsChangedEvent(eventBus, settings);
 						}

 						update();
 					}
 				});
 
 		DosimeterSerialNumbersChangedEvent.subscribe(eventBus,
 				new DosimeterSerialNumbersChangedEvent.Handler() {
 
 					@Override
 					public void onDosimeterSerialNumbersChanged(
 							DosimeterSerialNumbersChangedEvent event) {
 						dosimeterSerialNumbers.clear();
 						dosimeterSerialNumbers.addAll(event
 								.getDosimeterSerialNumbers());
 						System.err.println("DOSI changed "
 								+ dosimeterSerialNumbers.size());
 
 						// FIXME, allow for setting not available as DOSI #
 						update();
 					}
 				});
 	}
 
 	private List<Integer> mergeList(List<Integer> newList) {
 		List<Integer> currentPtuIds = dataProvider.getList();
 		List<Integer> newPtuIds = new ArrayList<Integer>();
 		for (Iterator<Integer> i = newList.iterator(); i.hasNext();) {
 			Integer ptuId = i.next();
 			if (!currentPtuIds.contains(ptuId)) {
 				newPtuIds.add(ptuId);
 			}
 		}
 		currentPtuIds.addAll(newPtuIds);
 		return newPtuIds;
 	}
 
 	private void update() {
 		table.redraw();
 	}
 
 	private void fireSettingsChangedEvent(RemoteEventBus eventBus,
 			PtuSettings settings) {
 
 		eventBus.fireEvent(new PtuSettingsChangedEvent(settings));
 		eventBus.fireEvent(new DosimeterPtuChangedEvent(settings
 				.getDosimeterToPtuMap()));
 	}
 }
