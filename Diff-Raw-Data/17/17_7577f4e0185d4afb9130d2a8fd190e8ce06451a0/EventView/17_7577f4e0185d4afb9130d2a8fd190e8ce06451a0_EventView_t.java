 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 import ch.cern.atlas.apvs.client.service.EventServiceAsync;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.ColumnSortList;
 import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
 import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HeaderPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class EventView extends DockPanel implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private EventBus cmdBus;
 	private String ptuId;
 	private String measurementName;
 
 	private MyDataGrid<Event> table = new MyDataGrid<Event>();
 
 	private ClickableTextColumn<Event> date;
 	private String ptuHeader;
 	private ClickableTextColumn<Event> ptu;
 	private String nameHeader;
 	private ClickableHtmlColumn<Event> name;
 
 	private boolean selectable = false;
 	private boolean sortable = true;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	private class MyDataGrid<T> extends DataGrid<T> {
 		ScrollPanel getScrollPanel() {
 			return (ScrollPanel) ((HeaderPanel) table.getWidget())
 					.getContentWidget();
 		}
 	}
 
 	public EventView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		String height = args.getArg(0);
 
 		if (args.size() > 1) {
 			cmdBus = clientFactory.getEventBus(args.getArg(1));
 		}
 
 		table.setSize("100%", height);
 		table.setEmptyTableWidget(new Label("No Events"));
 
 		final SimplePager pager = new SimplePager(TextLocation.RIGHT);
 		add(pager, SOUTH);
 		pager.setDisplay(table);
 
 		final TextArea msg = new TextArea();
 		// FIXME, not sure how to handle scroll bar and paging
 		// add(msg, NORTH);
 
 		setWidth("100%");
 		add(table, CENTER);
 
 		final ScrollPanel scrollPanel = table.getScrollPanel();
 		scrollPanel.addScrollHandler(new ScrollHandler() {
 
 			int line = 0;
 
 			@Override
 			public void onScroll(ScrollEvent event) {
 				msg.setText(msg.getText() + "\n" + line + " "
 						+ event.toDebugString() + " "
 						+ scrollPanel.getVerticalScrollPosition() + " "
 						+ scrollPanel.getMinimumVerticalScrollPosition() + " "
 						+ scrollPanel.getMaximumVerticalScrollPosition());
 				msg.getElement().setScrollTop(
 						msg.getElement().getScrollHeight());
 				line++;
 			}
 		});
 
 		AsyncDataProvider<Event> dataProvider = new AsyncDataProvider<Event>() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			protected void onRangeChanged(HasData<Event> display) {
				EventServiceAsync.Util.getInstance().getRowCount(ptuId,
						measurementName, new AsyncCallback<Integer>() {
 
 							@Override
 							public void onSuccess(Integer result) {
 								table.setRowCount(result);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								table.setRowCount(0);
 							}
 						});
 
 				final Range range = display.getVisibleRange();
 
 				final ColumnSortList sortList = table.getColumnSortList();
 				SortOrder[] order = new SortOrder[sortList.size()];
 				for (int i = 0; i < sortList.size(); i++) {
 					ColumnSortInfo info = sortList.get(i);
 					// FIXME #88 remove cast
 					order[i] = new SortOrder(
 							((ClickableTextColumn<Event>) info.getColumn())
 									.getDataStoreName(),
 							info.isAscending());
 				}
 
 				if (order.length == 0) {
 					order = new SortOrder[1];
 					order[0] = new SortOrder("tbl_events.datetime", false);
 				}
 
 				EventServiceAsync.Util.getInstance().getTableData(range, order,
						ptuId, measurementName,
						new AsyncCallback<List<Event>>() {
 
 							@Override
 							public void onSuccess(List<Event> result) {
 								table.setRowData(range.getStart(), result);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("RPC DB FAILED " + caught);
 								table.setRowCount(0);
 							}
 						});
 			}
 		};
 
 		// Table
 		dataProvider.addDataDisplay(table);
 
 		AsyncHandler columnSortHandler = new AsyncHandler(table);
 		table.addColumnSortHandler(columnSortHandler);
 
 		// Subscriptions
 		EventChangedEvent.register(clientFactory.getRemoteEventBus(),
 				new EventChangedEvent.Handler() {
 
 					@Override
 					public void onEventChanged(EventChangedEvent e) {
 						Event event = e.getEvent();
 						if (event == null)
 							return;
 
 						if (((ptuId == null) || event.getPtuId().equals(ptuId))
 								&& ((measurementName == null) || event
 										.getName().equals(measurementName))) {
 							scheduler.update();
 
 							ColumnSortList sortList = table.getColumnSortList();
 							ColumnSortInfo sortInfo = sortList.size() > 0 ? sortList
 									.get(0) : null;
 							boolean sortedOnDate = sortInfo != null ? sortInfo
 									.getColumn().equals(date) : false;
 							boolean sortedDescending = sortInfo != null ? !sortInfo
 									.isAscending() : false;
 
 							if (!sortedOnDate
 									|| !sortedDescending
 									|| (scrollPanel.getVerticalScrollPosition() != scrollPanel
 											.getMinimumHorizontalScrollPosition())
 									|| (pager.getPage() != pager.getPageStart())) {
 								System.err.println("************* Event "
 										+ e.getEvent()
 										+ " "
 										+ scrollPanel
 												.getVerticalScrollPosition()
 										+ " v"
 										+ scrollPanel
 												.getMinimumHorizontalScrollPosition()
 										+ " "
 										+ pager.getPage()
 										+ " s"
 										+ pager.getPageStart()
 										+ " "
 										+ event.getPtuId()
 										+ " "
 										+ ptuId
 										+ " "
 										+ measurementName
 										+ " "
 										+ (sortInfo != null ? sortInfo
 												.isAscending() : "null") + " "
 										+ sortedOnDate);
 							}
 						}
 					}
 				});
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 					scheduler.update();
 				}
 			});
 
 			SelectMeasurementEvent.subscribe(cmdBus,
 					new SelectMeasurementEvent.Handler() {
 
 						@Override
 						public void onSelection(SelectMeasurementEvent event) {
 							measurementName = event.getName();
 							scheduler.update();
 						}
 					});
 
 			// FIXME #189
 			SelectTabEvent.subscribe(cmdBus, new SelectTabEvent.Handler() {
 
 				@Override
 				public void onTabSelected(SelectTabEvent event) {
 					if (event.getTab().equals("Summary")) {
 						scheduler.update();
 					}
 				}
 			});
 		}
 
 		// DATE and TIME (1)
 		date = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return PtuClientConstants.dateFormat.format(object.getDate());
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.datetime";
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
		table.addColumn(date, new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("Date / Time")));
 
 		// desc sort, push twice
 		table.getColumnSortList().push(date);
 		table.getColumnSortList().push(date);
 
 		// PtuID (2)
 		ptu = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getPtuId();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_devices.name";
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
 
 		// Name (3)
 		name = new ClickableHtmlColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getName();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.sensor";
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
 
 		// EventType
 		ClickableTextColumn<Event> eventType = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getEventType();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.event_type";
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
 
 		// Value
 		ClickableTextColumn<Event> value = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getValue().toString();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.value";
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
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.threshold";
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
 				return object.getUnit();
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
 			final SingleSelectionModel<Event> selectionModel = new SingleSelectionModel<Event>();
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Event m = selectionModel.getSelectedObject();
 							log.info(m + " " + event.getSource());
 						}
 					});
 		}
 
 		return true;
 	}
 
 	private void selectEvent(Event event) {
 	}
 
 	@Override
 	public boolean update() {
 		// enable / disable columns
 		if (table.getColumnIndex(ptu) >= 0) {
 			if (ptuId != null) {
 				table.removeColumn(ptu);
 			}
 		} else {
 			if (ptuId == null) {
 				// add Ptu Column
 				table.insertColumn(1, ptu, ptuHeader);
 			}
 		}
 
 		if (table.getColumnIndex(name) >= 0) {
 			if (measurementName != null) {
 				table.removeColumn(name);
 			}
 		} else {
 			if (measurementName == null) {
 				// add Name column
 				table.insertColumn(2, name, nameHeader);
 			}
 		}
 
 		// Re-sort the table
 		RangeChangeEvent.fire(table, table.getVisibleRange());
 		table.redraw();
 
 		return false;
 	}
 }
