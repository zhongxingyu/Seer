 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextCell;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.client.widget.VerticalFlowPanel;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class MeasurementView extends VerticalFlowPanel {
 
 	private static NumberFormat format = NumberFormat.getFormat("0.00");
 
 	private PtuSettings settings;
 	private Measurement last = new Measurement();
 	private ListDataProvider<Measurement> dataProvider = new ListDataProvider<Measurement>();
 	private CellTable<Measurement> table = new CellTable<Measurement>();
 	private ListHandler<Measurement> columnSortHandler;
 	private ClickableHtmlColumn<Measurement> name;
 	private SingleSelectionModel<Measurement> selectionModel;
 
 	private List<String> show = null;
 
 	private String ptuId = "PTU1234";
 
 	private EventBus cmdBus;
 
 	private boolean showHeader = true;
 	private boolean showName = true;
 	private boolean sortable = true;
 	private boolean selectable = true;
 
 	private String options;
 
 	public MeasurementView(final ClientFactory clientFactory, Arguments args) {
 
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		options = args.getArg(1);
 		show = args.getArgs(2);
 
 		showHeader = !options.contains("NoHeader");
 		showName = !options.contains("NoName");
 		sortable = !options.contains("NoSort");
 		selectable = !options.contains("NoSelection");
 
 		if (selectable) {
 			selectionModel = new SingleSelectionModel<Measurement>();
 		}
 
 		add(table);
 
 		PtuSettingsChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new PtuSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedEvent event) {
 						settings = event.getPtuSettings();
 
 						update();
 					}
 				});
 
 		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(final SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 
 				if (ptuId == null) {
 					dataProvider.getList().clear();
 				}
 				update();
 			}
 		});
 
 		MeasurementChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement measurement = event.getMeasurement();
 						if (measurement.getPtuId().equals(ptuId)) {
 							last = replace(measurement, last);
 							update();
 						}
 					}
 				});
 
 		name = new ClickableHtmlColumn<Measurement>() {
 			@Override
 			public String getValue(Measurement object) {
 				return object.getDisplayName();
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(sortable);
 		if (selectable) {
 			name.setFieldUpdater(new FieldUpdater<Measurement, String>() {
 
 				@Override
 				public void update(int index, Measurement object, String value) {
 					selectMeasurement(object.getName());
 				}
 			});
 		}
 
 		table.addColumn(name, showHeader ? new TextHeader("") {
 			@Override
 			public String getValue() {
 				if (!showName)
 					return null;
 
 				if (ptuId == null)
 					return "Name";
 
 				if (settings != null) {
 					String name = settings.getName(ptuId);
 
 					if (name != null)
 						return name;
 				}
 
 				return "PTU Id: " + ptuId;
 			}
 		} : null);
 
 		ClickableTextColumn<Measurement> value = new ClickableTextColumn<Measurement>() {
 			@Override
 			public String getValue(Measurement object) {
 				if ((object == null) || (object.getValue() == null)) {
 					return "";
 				}
 				return format.format(object.getValue());
 			}
 
 			@Override
 			public void render(Context context, Measurement object,
 					SafeHtmlBuilder sb) {
 				String s = getValue(object);
 				((ClickableTextCell) getCell()).render(context,
 						decorate(s, object, last), sb);
 			}
 
 		};
 		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		if (selectable) {
 			value.setFieldUpdater(new FieldUpdater<Measurement, String>() {
 
 				@Override
 				public void update(int index, Measurement object, String value) {
 					selectMeasurement(object.getName());
 				}
 			});
 		}
 		table.addColumn(value, showHeader ? "Value" : null);
 
 		ClickableHtmlColumn<Measurement> unit = new ClickableHtmlColumn<Measurement>() {
 			@Override
 			public String getValue(Measurement object) {
 				return object.getUnit();
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		unit.setSortable(sortable);
 		if (selectable) {
 			unit.setFieldUpdater(new FieldUpdater<Measurement, String>() {
 
 				@Override
 				public void update(int index, Measurement object, String value) {
 					selectMeasurement(object.getName());
 				}
 			});
 		}
 		table.addColumn(unit, showHeader ? "Unit" : null);
 
 		List<Measurement> list = new ArrayList<Measurement>();
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(list);
 
 		columnSortHandler = new ListHandler<Measurement>(dataProvider.getList());
 		columnSortHandler.setComparator(name, new Comparator<Measurement>() {
 			public int compare(Measurement o1, Measurement o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getName().compareTo(o2.getName())
 							: 1;
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(value, new Comparator<Measurement>() {
 			public int compare(Measurement o1, Measurement o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if ((o1 != null) && (o1.getValue() != null)) {
 					if ((o2 != null) && (o2.getValue() != null)) {
 						double d1 = o1.getValue().doubleValue();
 						double d2 = o2.getValue().doubleValue();
 						return d1 < d2 ? -1 : d1 == d2 ? 0 : 1;
 					}
 					return 1;
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(unit, new Comparator<Measurement>() {
 			public int compare(Measurement o1, Measurement o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getUnit().compareTo(o2.getUnit())
 							: 1;
 				}
 				return -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(name);
 
 		if (selectable) {
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Measurement m = selectionModel.getSelectedObject();
 							System.err.println(m + " " + event.getSource());
 						}
 					});
 		}
 
 		// fill initial list
 		for (Iterator<String> i = show.iterator(); i.hasNext();) {
 			String name = i.next();
 			System.err.println("Added " + name);
 			list.add(new NullMeasurement(null, name));
 		}
 	}
 
 	public static SafeHtml decorate(String s, Measurement current,
 			Measurement last) {
 		if ((current != null) && (last != null)
				&& (current.getPtuId() == last.getPtuId())
 				&& current.getName().equals(last.getName())) {
 			double c = current.getValue().doubleValue();
 			double l = last.getValue().doubleValue();
 			String a = (c == l) ? "&larr;" : (c > l) ? "&uarr;" : "&darr;";
 			s = a + "&nbsp;<b>" + s + "</b>";
 		}
 		return SafeHtmlUtils.fromSafeConstant(s);
 	}
 
 	private void update() {
 		// Re-sort the table
 		if (sortable) {
 			ColumnSortEvent.fire(table, table.getColumnSortList());
 		}
 		table.redraw();
 
 		if (selectable) {
 			Measurement selection = selectionModel.getSelectedObject();
 
 			if ((selection == null) && (dataProvider.getList().size() > 0)) {
 				selection = dataProvider.getList().get(0);
 
 				selectMeasurement(selection.getName());
 			}
 
 			// re-set the selection as the async update may have changed the
 			// rendering
 			if (selection != null) {
 				selectionModel.setSelected(selection, true);
 			}
 		}
 	}
 
 	private Measurement replace(Measurement measurement, Measurement lastValue) {
 		List<Measurement> list = dataProvider.getList();
 
 		int i = 0;
 		while (i < list.size()) {
 			if (list.get(i).getName().equals(measurement.getName())) {
 				break;
 			}
 			i++;
 		}
 
 		if (i == list.size()) {
 			if (show.size() == 0) {
 				list.add(measurement);
 				lastValue = measurement;
 			}
 		} else {
 			lastValue = list.set(i, measurement);
 		}
 
 		return lastValue;
 	}
 
 	private void selectMeasurement(String name) {
 		SelectMeasurementEvent.fire(cmdBus, name);
 	}
 
 }
