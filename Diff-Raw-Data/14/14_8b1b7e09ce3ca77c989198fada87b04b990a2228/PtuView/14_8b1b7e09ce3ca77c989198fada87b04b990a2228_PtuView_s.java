 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.HistoryMap;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextCell;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class PtuView extends GlassPanel implements Module {
 
 	@SuppressWarnings("unused")
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private static NumberFormat format = NumberFormat.getFormat("0.00");
 
 	private RemoteEventBus eventBus;
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private CellTable<String> table = new CellTable<String>();
 
 	private List<String> ptuIds;
 	private Measurement last;
 	private Map<String, ClickableTextColumn<String>> columns;
 	private SingleSelectionModel<String> selectionModel;
 	private Map<String, String> colorMap = new HashMap<String, String>();
 
 	private PtuSettings settings;
 	private InterventionMap interventions;
 	private EventBus cmdBus;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	private Ternary daqOk = Ternary.Unknown;
 	private Ternary databaseConnect = Ternary.Unknown;
 
 	private HistoryMap historyMap;
 
 	private void init() {
 		last = new Measurement();
 		columns = new HashMap<String, ClickableTextColumn<String>>();
 	}
 
 	public PtuView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		eventBus = clientFactory.getRemoteEventBus();
 
 		init();
 
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		
 		table.setWidth("100%");
 
 		add(table, CENTER);
 
 		// name column
 		ClickableHtmlColumn<String> name = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				return historyMap != null ? historyMap.getDisplayName(name) : name;
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(true);
 		name.setFieldUpdater(new FieldUpdater<String, String>() {
 
 			@Override
 			public void update(int index, String name, String value) {
 				selectMeasurementAndPtu(name, null);
 			}
 		});
 		table.addColumn(name, "Name");
 
 		// unit column
 		ClickableHtmlColumn<String> unit = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				return historyMap != null ? historyMap.getUnit(name) : "";
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		unit.setFieldUpdater(new FieldUpdater<String, String>() {
 
 			@Override
 			public void update(int index, String name, String value) {
 				selectMeasurementAndPtu(name, null);
 			}
 		});
 		table.addColumn(unit, "Unit");
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<String>());
 
 		ListHandler<String> columnSortHandler = new ListHandler<String>(
 				dataProvider.getList());
 		columnSortHandler.setComparator(name, new Comparator<String>() {
 			public int compare(String o1, String o2) {
 				return o1 != null ? o1.compareTo(o2) : -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(name);
 
 		selectionModel = new SingleSelectionModel<String>();
 		table.setSelectionModel(selectionModel);
 
 		ConnectionStatusChangedRemoteEvent.subscribe(
 				clientFactory.getRemoteEventBus(),
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
 
 		MeasurementChangedEvent.register(eventBus,
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement measurement = event.getMeasurement();
 						last = addOrReplaceMeasurement(measurement);
 						scheduler.update();
 					}
 				});
 
 		PtuSettingsChangedRemoteEvent.subscribe(eventBus,
 				new PtuSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedRemoteEvent event) {
 						settings = event.getPtuSettings();
 						configChanged();
 						scheduler.update();
 					}
 				});
 
 		InterventionMapChangedRemoteEvent.subscribe(eventBus,
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 						interventions = event.getInterventionMap();
 
 						ptuIds = interventions.getPtuIds();
 
 						configChanged();
 						scheduler.update();
 					}
 				});
 
 		HistoryMapChangedEvent.subscribe(clientFactory,
 				new HistoryMapChangedEvent.Handler() {
 
 					@Override
 					public void onHistoryMapChanged(HistoryMapChangedEvent event) {
 						historyMap = event.getHistoryMap();
 						configChanged();
 						scheduler.update();
 					}
 				});		
 
 		if (cmdBus != null) {
 			ColorMapChangedEvent.subscribe(cmdBus,
 					new ColorMapChangedEvent.Handler() {
 						@Override
 						public void onColorMapChanged(ColorMapChangedEvent event) {
 							colorMap = event.getColorMap();
 							scheduler.update();
 						}
 
 					});
 		}
 
 		return true;
 	}
 
 	private Measurement addOrReplaceMeasurement(Measurement measurement) {
 		
 		String ptuId = measurement.getPtuId();
 		String name = measurement.getName();
 		if ((ptuIds == null) || !ptuIds.contains(ptuId))
 			return null;
 
 		Measurement lastValue = historyMap != null ? historyMap.getMeasurement(ptuId, name) : measurement;
 
 		addRow(name);
 
 		return lastValue;
 	}
 
 	private void addRow(String name) {
 		if (!dataProvider.getList().contains(name)) {
 			dataProvider.getList().add(name);
 		}
 	}
 
 	private void addColumn(final String ptuId) {
 		ClickableTextColumn<String> column = new ClickableTextColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				Measurement m = historyMap != null ? historyMap.getMeasurement(ptuId, name) : null;
 				if (m == null) {
 					return "";
 				}
 				return format.format(m.getValue());
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String color = colorMap.get(ptuId);
 				boolean isSelected = selectionModel.isSelected(name);
 				if ((color != null) && isSelected) {
 					sb.append(SafeHtmlUtils
 							.fromSafeConstant("<div style=\"background:"
 									+ color + "\">"));
 				}
 
 				Measurement m = historyMap != null ? historyMap.getMeasurement(ptuId, name) : null;
 				if (m != null) {
 					((ClickableTextCell) getCell()).render(context,
 							MeasurementView.decorate(getValue(name), m, last),
 							sb);
 				}
 				if ((color != null) && isSelected) {
 					sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));
 				}
 			}
 		};
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		column.setFieldUpdater(new FieldUpdater<String, String>() {
 
 			@Override
 			public void update(int index, String name, String value) {
 				selectMeasurementAndPtu(name, ptuId);
 			}
 		});
 
 		table.addColumn(column, new TextHeader("") {
 			@Override
 			public String getValue() {
 				String name = interventions != null ? interventions.get(ptuId) != null ? interventions
 						.get(ptuId).getName() : null
 						: null;
 				return name != null ? name + "<br/>(" + ptuId.toString() + ")"
 						: ptuId.toString();
 			}
 
 			@Override
 			public void render(Context context, SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue()), sb);
 			}
 		});
 
 		columns.put(ptuId, column);
 	}
 
 	private void configChanged() {
 
 		if (ptuIds == null) {
 			init();
 			return;
 		}
 		
 		// Remove any dead columns
 		for (Iterator<Map.Entry<String, ClickableTextColumn<String>>> i = columns
 				.entrySet().iterator(); i.hasNext();) {
 			Map.Entry<String, ClickableTextColumn<String>> entry = i.next();
 			if (ptuIds.contains(entry.getKey()))
 				continue;
 
 			ClickableTextColumn<String> column = entry.getValue();
 			table.removeColumn(column);
 			i.remove();
 		}
 
 		// add any new columns...
 		for (Iterator<String> i = ptuIds.iterator(); i.hasNext();) {
 			String ptuId = i.next();
 			ClickableTextColumn<String> column = columns.get(ptuId);
 
 			if ((settings == null) || settings.isEnabled(ptuId)) {
 				if (column == null) {
 					addColumn(ptuId);
 				}
 				
 				if (historyMap != null) {
 					for (Measurement measurement: historyMap.getMeasurements(ptuId)) {
						addRow(measurement.getName());
 					}
 				}
 				
 			} else {
 				// remove a column that was disabled
 				if (column != null) {
 					table.removeColumn(column);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean update() {
 		ColumnSortEvent.fire(table, table.getColumnSortList());
 		table.redraw();
 
 		String selection = selectionModel.getSelectedObject();
 		if ((selection == null) && (dataProvider.getList().size() > 0)) {
 			selection = dataProvider.getList().get(0);
 
 			selectMeasurementAndPtu(selection, null);
 		}
 
 		// re-set the selection as the async update may have changed the
 		// rendering
 		if (selection != null) {
 			selectionModel.setSelected(selection, true);
 		}
 		return false;
 	}
 
 	private void selectMeasurementAndPtu(String name, String ptuId) {
 		SelectMeasurementEvent.fire(cmdBus, name);
 		SelectPtuEvent.fire(cmdBus, ptuId);
 	}
 }
