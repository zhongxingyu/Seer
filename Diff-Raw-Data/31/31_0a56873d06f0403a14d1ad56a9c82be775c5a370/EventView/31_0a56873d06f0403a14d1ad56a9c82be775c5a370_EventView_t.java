 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class EventView extends SimplePanel {
 
 	private EventBus cmdBus;
 	private String ptuId;
 	private String measurementName;
 
 	private ListDataProvider<Event> dataProvider = new ListDataProvider<Event>();
 	private DataGrid<Event> table = new DataGrid<Event>();
 	private ListHandler<Event> columnSortHandler;
 	private SingleSelectionModel<Event> selectionModel;
 
	private String ptuHeader;
 	private ClickableTextColumn<Event> ptu;
	private String nameHeader;
 	private ClickableHtmlColumn<Event> name;
 
 	private Map<String, String> units = new HashMap<String, String>();
 
 	private boolean selectable = false;
 	private boolean sortable = true;
 
 	public EventView(ClientFactory clientFactory, Arguments args) {
 
 		String height = args.getArg(0);
 
 		if (args.size() > 1) {
 			cmdBus = clientFactory.getEventBus(args.getArg(1));
 		}
 
 		table.setSize("100%", height);
 		table.setEmptyTableWidget(new Label("No Events"));
 
 		add(table);
 
 		// Table
 		dataProvider.addDataDisplay(table);
 
 		columnSortHandler = new ListHandler<Event>(dataProvider.getList());
 		table.addColumnSortHandler(columnSortHandler);
 
 		// Subscriptions
 		EventChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new EventChangedEvent.Handler() {
 
 					@Override
 					public void onEventChanged(EventChangedEvent e) {
 						Event event = e.getEvent();
 						if (event == null)
 							return;
 
 						if (((ptuId == null) || event.getPtuId().equals(ptuId))
 								&& ((measurementName == null) || event
 										.getName().equals(measurementName))) {
 							dataProvider.getList().add(event);
 						}
 
 						update();
 					}
 				});
 
 		MeasurementChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement m = event.getMeasurement();
 						units.put(unitKey(m.getPtuId(), m.getName()),
 								m.getUnit());
 
 						update();
 					}
 				});
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 
 					dataProvider.getList().clear();
 
 					update();
 				}
 			});
 
 			SelectMeasurementEvent.subscribe(cmdBus,
 					new SelectMeasurementEvent.Handler() {
 
 						@Override
 						public void onSelection(SelectMeasurementEvent event) {
 							measurementName = event.getName();
 
 							dataProvider.getList().clear();
 
 							update();
 						}
 					});
 		}
 
 		// DATE and TIME (1)
 		ClickableTextColumn<Event> date = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return PtuClientConstants.dateFormat.format(object.getDate());
 			}
 		};
 		date.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		date.setSortable(sortable);
 		if (selectable) {
 			date.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(date, "Date / Time");
 		columnSortHandler.setComparator(date, new Comparator<Event>() {
 			public int compare(Event o1, Event o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getDate().compareTo(o2.getDate())
 							: 1;
 				}
 				return -1;
 			}
 		});
 		table.getColumnSortList().push(date);
 
 		// PtuID (2)
 		ptu = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getPtuId();
 			}
 
 		};
 		ptu.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		ptu.setSortable(sortable);
 		if (selectable) {
 			ptu.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
		ptuHeader = "PTU ID";
		table.addColumn(ptu, ptuHeader);
 		columnSortHandler.setComparator(ptu, new Comparator<Event>() {
 			public int compare(Event o1, Event o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getPtuId()
 							.compareTo(o2.getPtuId()) : 1;
 				}
 				return -1;
 			}
 		});
 
 		// Name (3)
 		name = new ClickableHtmlColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getName();
 			}
 
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(sortable);
 		if (selectable) {
 			name.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
		nameHeader = "Name";
		table.addColumn(name, nameHeader);
 		columnSortHandler.setComparator(name, new Comparator<Event>() {
 			public int compare(Event o1, Event o2) {
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
 
 		// EventType
 		ClickableTextColumn<Event> eventType = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getEventType();
 			}
 
 		};
 		eventType.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		eventType.setSortable(sortable);
 		if (selectable) {
 			eventType.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(eventType, "EventType");
 		columnSortHandler.setComparator(eventType, new Comparator<Event>() {
 			public int compare(Event o1, Event o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getEventType().compareTo(
 							o2.getEventType()) : 1;
 				}
 				return -1;
 			}
 		});
 
 		// Value
 		ClickableTextColumn<Event> value = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getValue().toString();
 			}
 
 		};
 		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			value.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(value, "Value");
 
 		// Threshold
 		ClickableTextColumn<Event> threshold = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getTheshold().toString();
 			}
 
 		};
 		threshold.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			threshold.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(threshold, "Threshold");
 
 		// Unit
 		ClickableHtmlColumn<Event> unit = new ClickableHtmlColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return units.get(unitKey(object.getPtuId(), object.getName()));
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			unit.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(unit, "Unit");
 
 		// Selection
 		if (selectable) {
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Event m = selectionModel.getSelectedObject();
 							System.err.println(m + " " + event.getSource());
 						}
 					});
 		}
 	}
 
 	private void selectEvent(Event event) {
 	}
 
 	private void update() {
		// enable / disable columns
		if (table.getColumnIndex(ptu) >= 0) {
			table.removeColumn(ptu);
		}
 		if (ptuId == null) {
 			// add Ptu Column
			table.insertColumn(1, ptu, ptuHeader);
 		}
 
		if (table.getColumnIndex(name) >= 0) {
			table.removeColumn(name);
		}
 		if (measurementName == null) {
 			// add Name column
			table.insertColumn(2, name, nameHeader);
		} 
 
 		// Re-sort the table
 		if (sortable) {
 			ColumnSortEvent.fire(table, table.getColumnSortList());
 		}
 		table.redraw();
 
 		if (selectable) {
 			Event selection = selectionModel.getSelectedObject();
 
 			if ((selection == null) && (dataProvider.getList().size() > 0)) {
 				selection = dataProvider.getList().get(0);
 
 				selectEvent(selection);
 			}
 
 			// re-set the selection as the async update may have changed the
 			// rendering
 			if (selection != null) {
 				selectionModel.setSelected(selection, true);
 			}
 		}
 	}
 
 	private String unitKey(String ptuId, String name) {
 		return ptuId + ":" + name;
 	}
 }
