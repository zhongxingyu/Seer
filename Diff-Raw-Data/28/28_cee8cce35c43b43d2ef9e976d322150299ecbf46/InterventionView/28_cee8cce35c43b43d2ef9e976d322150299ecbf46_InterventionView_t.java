 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.Device;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.User;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 import ch.cern.atlas.apvs.client.service.InterventionServiceAsync;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.client.widget.DataStoreName;
 import ch.cern.atlas.apvs.client.widget.EditTextColumn;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GenericColumn;
 import ch.cern.atlas.apvs.client.widget.HumanTime;
 import ch.cern.atlas.apvs.client.widget.ListBoxField;
 import ch.cern.atlas.apvs.client.widget.TextAreaField;
 import ch.cern.atlas.apvs.client.widget.TextBoxField;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.Fieldset;
 import com.github.gwtbootstrap.client.ui.Form;
 import com.github.gwtbootstrap.client.ui.Label;
 import com.github.gwtbootstrap.client.ui.Modal;
 import com.github.gwtbootstrap.client.ui.ModalFooter;
 import com.github.gwtbootstrap.client.ui.constants.ButtonType;
 import com.github.gwtbootstrap.client.ui.constants.FormType;
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.ColumnSortList;
 import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.validation.client.Validation;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
public class InterventionView extends DockPanel implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private DataGrid<Intervention> table = new DataGrid<Intervention>();
 
 	private boolean selectable = false;
 	private boolean sortable = true;
 
 	private final String END_INTERVENTION = "End Intervention";
 
 	private InterventionServiceAsync interventionService;
 	private Validator validator;
 	
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	public InterventionView() {
 		interventionService = InterventionServiceAsync.Util.getInstance();
 		validator = Validation.buildDefaultValidatorFactory().getValidator();
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		String height = args.getArg(0);
 
 		EventBus eventBus = clientFactory.getEventBus(args.getArg(1));
 
 		table.setSize("100%", height);
 		table.setEmptyTableWidget(new Label("No Interventions"));
 
		SimplePager pager = new SimplePager(TextLocation.RIGHT);
		add(pager, SOUTH);
		pager.setDisplay(table);
		
		setWidth("100%");
		add(table, CENTER);
 
 		AsyncDataProvider<Intervention> dataProvider = new AsyncDataProvider<Intervention>() {
 
 			@Override
 			protected void onRangeChanged(HasData<Intervention> display) {
 				log.info("ON RANGE CHANGED " + display.getVisibleRange());
 
 				interventionService.getRowCount(new AsyncCallback<Integer>() {
 
 					@Override
 					public void onSuccess(Integer result) {
 						updateRowCount(result, true);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						updateRowCount(0, true);
 					}
 				});
 
 				final Range range = display.getVisibleRange();
 
 				final ColumnSortList sortList = table.getColumnSortList();
 				SortOrder[] order = new SortOrder[sortList.size()];
 				for (int i = 0; i < sortList.size(); i++) {
 					ColumnSortInfo info = sortList.get(i);
 					// FIXME #88 remove cast
 					order[i] = new SortOrder(
 							((DataStoreName) info.getColumn())
 									.getDataStoreName(),
 							info.isAscending());
 				}
 
 				if (order.length == 0) {
 					order = new SortOrder[1];
 					order[0] = new SortOrder("tbl_inspections.starttime", false);
 				}
 
 				interventionService.getTableData(range, order,
 						new AsyncCallback<List<Intervention>>() {
 
 							@Override
 							public void onSuccess(List<Intervention> result) {
 								updateRowData(range.getStart(), result);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("RPC DB FAILED "+caught);
 								updateRowCount(0, true);
 							}
 						});
 			}
 		};
 
 		// Table
 		dataProvider.addDataDisplay(table);
 
 		AsyncHandler columnSortHandler = new AsyncHandler(table);
 		table.addColumnSortHandler(columnSortHandler);
 
 		// startTime
 		ClickableTextColumn<Intervention> startTime = new ClickableTextColumn<Intervention>() {
 			@Override
 			public String getValue(Intervention object) {
 				return PtuClientConstants.dateFormatNoSeconds.format(object
 						.getStartTime());
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_inspections.starttime";
 			}
 		};
 		startTime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		startTime.setSortable(sortable);
 		if (selectable) {
 			startTime.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 
 				@Override
 				public void update(int index, Intervention object, String value) {
 					selectIntervention(object);
 				}
 			});
 		}
 		Header<String> interventionFooter = new Header<String>(new ButtonCell()) {
 			@Override
 			public String getValue() {
 				return "Start a new Intervention";
 			}
 		};
 		interventionFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 
 				Fieldset fieldset = new Fieldset();
 
 				final ListBoxField userField = new ListBoxField("User");
 				fieldset.add(userField);
 
 				interventionService.getUsers(true,
 						new AsyncCallback<List<User>>() {
 
 							@Override
 							public void onSuccess(List<User> result) {
 								for (Iterator<User> i = result.iterator(); i
 										.hasNext();) {
 									User user = i.next();
 									userField.addItem(user.getDisplayName(),
 											user.getId());
 								}
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("Caught : " + caught);
 							}
 						});
 
 				final ListBoxField ptu = new ListBoxField("PTU");
 				fieldset.add(ptu);
 
 				interventionService.getDevices(true,
 						new AsyncCallback<List<Device>>() {
 
 							@Override
 							public void onSuccess(List<Device> result) {
 								for (Iterator<Device> i = result.iterator(); i
 										.hasNext();) {
 									Device device = i.next();
 									ptu.addItem(device.getName(),
 											device.getId());
 								}
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("Caught : " + caught);
 							}
 						});
 
 				final TextAreaField description = new TextAreaField(
 						"Description");
 				fieldset.add(description);
 
 				Form form = new Form();
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 
 				final Modal m = new Modal();
 
 				Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				Button ok = new Button("Ok");
 				ok.setType(ButtonType.PRIMARY);
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 						
 						Intervention intervention = new Intervention(userField.getId(), ptu.getId(), new Date(),
 								description.getValue());
 
 						// FIXME #194
 						Set<ConstraintViolation<Intervention>> violations = validator
 								.validate(intervention);
 
 						if (!violations.isEmpty()) {
 							StringBuffer errorMessage = new StringBuffer();
 							for (ConstraintViolation<Intervention> constraintViolation : violations) {
 								errorMessage.append('\n');
 								errorMessage.append(constraintViolation
 										.getMessage());
 							}
 							log.warn(errorMessage.toString());
 						} else {
 							interventionService.addIntervention(
 									intervention,
 									new AsyncCallback<Void>() {
 
 										@Override
 										public void onSuccess(Void result) {
 											scheduler.update();
 										}
 
 										@Override
 										public void onFailure(Throwable caught) {
 											log.warn("Failed");
 										}
 									});
 						}
 					}
 				});
 
 				m.setTitle("Start a new Intervention");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 		table.addColumn(startTime, new TextHeader("Start Time"),
 				interventionFooter);
 		// twice for descending
 		table.getColumnSortList().push(startTime);
 		table.getColumnSortList().push(startTime);
 
 		// endTime
 		EditableCell cell = new EditableCell() {
 			@Override
 			protected Class<? extends Cell<? extends Object>> getCellClass(
 					Context context, Object value) {
 				return value == END_INTERVENTION ? ButtonCell.class
 						: TextCell.class;
 			}
 		};
 		Column<Intervention, Object> endTime = new GenericColumn<Intervention>(
 				cell) {
 			@Override
 			public String getValue(Intervention object) {
 				return object.getEndTime() != null ? PtuClientConstants.dateFormatNoSeconds
 						.format(object.getEndTime()) : END_INTERVENTION;
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_inspections.starttime";
 			}
 		};
 		endTime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		endTime.setSortable(sortable);
 		endTime.setFieldUpdater(new FieldUpdater<Intervention, Object>() {
 
 			@Override
 			public void update(int index, Intervention object, Object value) {
 
 				if (Window.confirm("Are you sure")) {
 					interventionService.endIntervention(object.getId(),
 							new Date(), new AsyncCallback<Void>() {
 
 								@Override
 								public void onSuccess(Void result) {
 									scheduler.update();
 								}
 
 								@Override
 								public void onFailure(Throwable caught) {
 									log.warn("Failed " + caught);
 								}
 
 							});
 				}
 			}
 		});
 		table.addColumn(endTime, new TextHeader("End Time"), new TextHeader(""));
 
 		ClickableTextColumn<Intervention> duration = new ClickableTextColumn<Intervention>() {
 
 			@Override
 			public String getValue(Intervention object) {
 				long d = object.getEndTime() == null ? new Date().getTime()
 						: object.getEndTime().getTime();
 				d = d - object.getStartTime().getTime();
 
 				return HumanTime.upToMins(d);
 			}
 		};
 		duration.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		duration.setSortable(false);
 		if (selectable) {
 			duration.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 
 				@Override
 				public void update(int index, Intervention object, String value) {
 					selectIntervention(object);
 				}
 			});
 		}
 		table.addColumn(duration, new TextHeader("Duration"),
 				new TextHeader(""));
 
 		// Name
 		ClickableHtmlColumn<Intervention> name = new ClickableHtmlColumn<Intervention>() {
 			@Override
 			public String getValue(Intervention object) {
 				return object.getName();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_users.lname";
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(sortable);
 		if (selectable) {
 			name.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 
 				@Override
 				public void update(int index, Intervention object, String value) {
 					selectIntervention(object);
 				}
 			});
 		}
 		Header<String> nameFooter = new Header<String>(new ButtonCell()) {
 			@Override
 			public String getValue() {
 				return "Add a new User";
 			}
 		};
 		nameFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 
 				Fieldset fieldset = new Fieldset();
 
 				final TextBoxField fname = new TextBoxField("First Name");
 				fieldset.add(fname);
 
 				final TextBoxField lname = new TextBoxField("Last Name");
 				fieldset.add(lname);
 
 				final TextBoxField cernId = new TextBoxField("CERN ID");
 				fieldset.add(cernId);
 
 				Form form = new Form();
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 
 				final Modal m = new Modal();
 
 				Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				Button ok = new Button("Ok");
 				ok.setType(ButtonType.PRIMARY);
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 
 						User user = new User(0, fname.getValue(), lname
 								.getValue(), cernId.getValue());
 
 						// FIXME #194
 						Set<ConstraintViolation<User>> violations = validator
 								.validate(user);
 
 						if (!violations.isEmpty()) {
 							StringBuffer errorMessage = new StringBuffer();
 							for (ConstraintViolation<User> constraintViolation : violations) {
 								errorMessage.append('\n');
 								errorMessage.append(constraintViolation
 										.getMessage());
 							}
 							log.warn(errorMessage.toString());
 						} else {
 							interventionService.addUser(user,
 									new AsyncCallback<Void>() {
 
 										@Override
 										public void onSuccess(Void result) {
 											scheduler.update();
 										}
 
 										@Override
 										public void onFailure(Throwable caught) {
 											log.warn("Failed " + caught);
 										}
 									});
 						}
 					}
 				});
 
 				m.setTitle("Add a new User");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 		table.addColumn(name, new TextHeader("Name"), nameFooter);
 
 		// PtuID
 		ClickableTextColumn<Intervention> ptu = new ClickableTextColumn<Intervention>() {
 			@Override
 			public String getValue(Intervention object) {
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
 			ptu.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 
 				@Override
 				public void update(int index, Intervention object, String value) {
 					selectIntervention(object);
 				}
 			});
 		}
 		Header<String> deviceFooter = new Header<String>(new ButtonCell()) {
 			@Override
 			public String getValue() {
 				return "Add a new PTU";
 			}
 		};
 		deviceFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 				Fieldset fieldset = new Fieldset();
 
 				final TextBoxField ptuId = new TextBoxField("PTU ID");
 				fieldset.add(ptuId);
 
 				final TextBoxField ip = new TextBoxField("IP");
 				fieldset.add(ip);
 
 				final TextAreaField description = new TextAreaField(
 						"Description");
 				fieldset.add(description);
 
 				Form form = new Form();
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 
 				final Modal m = new Modal();
 
 				Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				Button ok = new Button("Ok");
 				ok.setType(ButtonType.PRIMARY);
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 
 						Device device = new Device(0, ptuId.getValue(), ip
 								.getValue(), description.getValue());
 
 						// FIXME #194
 						Set<ConstraintViolation<Device>> violations = validator
 								.validate(device);
 
 						if (!violations.isEmpty()) {
 							StringBuffer errorMessage = new StringBuffer();
 							for (ConstraintViolation<Device> constraintViolation : violations) {
 								errorMessage.append('\n');
 								errorMessage.append(constraintViolation
 										.getMessage());
 							}
 							log.warn(errorMessage.toString());
 						} else {
 
 							interventionService.addDevice(device,
 									new AsyncCallback<Void>() {
 
 										@Override
 										public void onSuccess(Void result) {
 											scheduler.update();
 										}
 
 										@Override
 										public void onFailure(Throwable caught) {
 											log.warn("Failed " + caught);
 										}
 									});
 						}
 					}
 				});
 
 				m.setTitle("Add a new PTU");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 		table.addColumn(ptu, new TextHeader("PTU ID"), deviceFooter);
 
 		// Description
 		EditTextColumn<Intervention> description = new EditTextColumn<Intervention>() {
 			@Override
 			public String getValue(Intervention object) {
 				return object.getDescription() != null ? object
 						.getDescription() : "";
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_inspections.dscr";
 			}
 		};
 		description.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		description.setSortable(true);
 		description.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 			@Override
 			public void update(int index, Intervention object, String value) {
 				interventionService.updateInterventionDescription(
 						object.getId(), value, new AsyncCallback<Void>() {
 
 							@Override
 							public void onSuccess(Void result) {
 								scheduler.update();
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("Failed " + caught);
 							}
 						});
 			}
 		});
 		table.addColumn(description, new TextHeader("Description"),
 				new TextHeader(""));
 
 		// Selection
 		if (selectable) {
 			final SingleSelectionModel<Intervention> selectionModel = new SingleSelectionModel<Intervention>();
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Intervention m = selectionModel.getSelectedObject();
 							log.info(m + " " + event.getSource());
 						}
 					});
 		}
 
 		// FIXME #189 this is the normal way, but does not work in our tabs...
 		// tabs should detach, attach...
 		addAttachHandler(new AttachEvent.Handler() {
 
 			private Timer timer;
 
 			@Override
 			public void onAttachOrDetach(AttachEvent event) {
 
 				if (event.isAttached()) {
 					// refresh for duration
 					timer = new Timer() {
 						@Override
 						public void run() {
 							table.redraw();
 						}
 					};
 					timer.scheduleRepeating(60000);
 				} else {
 					if (timer != null) {
 						timer.cancel();
 						timer = null;
 					}
 				}
 			}
 		});
 
 		// FIXME #189 so we handle it with events
 		SelectTabEvent.subscribe(eventBus, new SelectTabEvent.Handler() {
 
 			@Override
 			public void onTabSelected(SelectTabEvent event) {
 				if (event.getTab().equals("Interventions")) {
 					table.redraw();
 				}
 			}
 		});
 
 		return true;
 	}
 
 	private void selectIntervention(Intervention intervention) {
 	}
 
 	@Override
 	public boolean update() {
 		RangeChangeEvent.fire(table, table.getVisibleRange());
 		table.redraw();
 		
 		return false;
 	}
 }
