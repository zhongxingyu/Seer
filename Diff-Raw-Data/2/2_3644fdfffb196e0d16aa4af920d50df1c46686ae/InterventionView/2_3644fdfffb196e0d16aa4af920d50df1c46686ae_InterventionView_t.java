 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.Device;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.User;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 import ch.cern.atlas.apvs.client.service.InterventionServiceAsync;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.validation.EmptyValidator;
 import ch.cern.atlas.apvs.client.validation.IntegerValidator;
 import ch.cern.atlas.apvs.client.validation.ListBoxField;
 import ch.cern.atlas.apvs.client.validation.NotNullValidator;
 import ch.cern.atlas.apvs.client.validation.OrValidator;
 import ch.cern.atlas.apvs.client.validation.StringValidator;
 import ch.cern.atlas.apvs.client.validation.TextAreaField;
 import ch.cern.atlas.apvs.client.validation.TextBoxField;
 import ch.cern.atlas.apvs.client.validation.ValidationFieldset;
 import ch.cern.atlas.apvs.client.validation.ValidationForm;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.client.widget.DataStoreName;
 import ch.cern.atlas.apvs.client.widget.EditTextColumn;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GenericColumn;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.HumanTime;
 import ch.cern.atlas.apvs.client.widget.PagerHeader;
 import ch.cern.atlas.apvs.client.widget.PagerHeader.TextLocation;
 import ch.cern.atlas.apvs.client.widget.ScrolledDataGrid;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.Label;
 import com.github.gwtbootstrap.client.ui.Modal;
 import com.github.gwtbootstrap.client.ui.ModalFooter;
 import com.github.gwtbootstrap.client.ui.constants.FormType;
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.ColumnSortList;
 import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class InterventionView extends GlassPanel implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private ScrolledDataGrid<Intervention> table = new ScrolledDataGrid<Intervention>();
 	private ScrollPanel scrollPanel;
 
 	private ClickableTextColumn<Intervention> startTime;
 
 	private boolean selectable = false;
 	private boolean sortable = true;
 
 	private final String END_INTERVENTION = "End Intervention";
 
 	private InterventionServiceAsync interventionService;
 	// private Validator validator;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	private HorizontalPanel footer = new HorizontalPanel();
 	private PagerHeader pager;
 	private Button update;
 	private boolean showUpdate;
 
 	public InterventionView() {
 	}
 
 	@Override
 	public boolean configure(Element element,
 			final ClientFactory clientFactory, Arguments args) {
 
 		interventionService = clientFactory.getInterventionService();
 
 		String height = args.getArg(0);
 
 		EventBus eventBus = clientFactory.getEventBus(args.getArg(1));
 
 		table.setSize("100%", height);
 		table.setEmptyTableWidget(new Label("No Interventions"));
 
 		pager = new PagerHeader(TextLocation.LEFT);
 		pager.setDisplay(table);
 
 		update = new Button("Update");
 		update.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				pager.setPage(0);
 				scrollPanel.setVerticalScrollPosition(scrollPanel
 						.getMinimumHorizontalScrollPosition());
 
 				table.getColumnSortList().push(
 						new ColumnSortInfo(startTime, false));
 				scheduler.update();
 			}
 		});
 		update.setVisible(false);
 		footer.add(update);
 
 		setWidth("100%");
 		add(table, CENTER);
 
 		scrollPanel = table.getScrollPanel();
 		scrollPanel.addScrollHandler(new ScrollHandler() {
 
 			@Override
 			public void onScroll(ScrollEvent event) {
 				if (scrollPanel.getVerticalScrollPosition() == scrollPanel
 						.getMinimumVerticalScrollPosition()) {
 					scheduler.update();
 				}
 			}
 		});
 
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
 					order[i] = new SortOrder(((DataStoreName)info.getColumn())
 							.getDataStoreName(), info.isAscending());
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
 								log.warn("RPC DB FAILED " + caught);
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
 		startTime = new ClickableTextColumn<Intervention>() {
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
 		table.addColumn(startTime, new TextHeader("Start Time"),
 				pager.getHeader());
 		table.getColumnSortList().push(new ColumnSortInfo(startTime, false));
 
 		// endTime
 		EditableCell cell = new EditableCell() {
 			@Override
 			protected Class<? extends Cell<? extends Object>> getCellClass(
 					Context context, Object value) {
 				return value == END_INTERVENTION ? ButtonCell.class
 						: TextCell.class;
 			}
 		};
 		GenericColumn<Intervention> endTime = new GenericColumn<Intervention>(
 				cell) {
 			@Override
 			public String getValue(Intervention object) {
 				return object.getEndTime() != null ? PtuClientConstants.dateFormatNoSeconds
 						.format(object.getEndTime()) : END_INTERVENTION;
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_inspections.endtime";
 			}
 		};
 		endTime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		endTime.setSortable(sortable);
 		endTime.setEnabled(clientFactory.isSupervisor());
 		endTime.setFieldUpdater(new FieldUpdater<Intervention, Object>() {
 
 			@Override
 			public void update(int index, Intervention object, Object value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 
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
 		table.addColumn(endTime, new TextHeader("End Time"), pager.getHeader());
 
 		Header<String> interventionFooter = new Header<String>(new ButtonCell()) {
 			@Override
 			public String getValue() {
 				return "Start a new Intervention";
 			}
 
 			public boolean onPreviewColumnSortEvent(Context context,
 					Element elem, NativeEvent event) {
 				// events are handled, do not sort, fix for #454
 				return false;
 			}
 		};
 		interventionFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 
 				ValidationFieldset fieldset = new ValidationFieldset();
 
 				final ListBoxField userField = new ListBoxField("User",
 						new NotNullValidator());
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
 
 				final ListBoxField ptu = new ListBoxField("PTU",
 						new NotNullValidator());
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
 
 				final TextBoxField impact = new TextBoxField("Impact Number");
 				fieldset.add(impact);
 
 				final TextAreaField description = new TextAreaField(
 						"Description");
 				fieldset.add(description);
 
 				final Modal m = new Modal();
 
 				Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				Button ok = new Button("Ok");
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 
 						Intervention intervention = new Intervention(userField
 								.getId(), ptu.getId(), new Date(), impact
 								.getValue(), 0.0, description.getValue());
 
 						interventionService.addIntervention(intervention,
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
 					// }
 				});
 
 				ValidationForm form = new ValidationForm(ok, cancel);
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 
 				m.setTitle("Start a new Intervention");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 
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
 				clientFactory.isSupervisor() ? interventionFooter : null);
 
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
 			
 			public boolean onPreviewColumnSortEvent(Context context,
 					Element elem, NativeEvent event) {
 				// events are handled, do not sort, fix for #454
 				return false;
 			}
 		};
 		nameFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 
 				ValidationFieldset fieldset = new ValidationFieldset();
 
 				final TextBoxField fname = new TextBoxField("First Name",
 						new StringValidator(1, 50, "*"));
 				fieldset.add(fname);
 
 				final TextBoxField lname = new TextBoxField("Last Name",
						new StringValidator(2, 50, "*"));
 				fieldset.add(lname);
 
 				final TextBoxField cernId = new TextBoxField("CERN ID",
 						new OrValidator(new EmptyValidator(),
 								new IntegerValidator("Enter a number")));
 				fieldset.add(cernId);
 
 				final Modal m = new Modal();
 
 				final Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				final Button ok = new Button("Ok");
 				ok.setEnabled(false);
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 
 						User user = new User(0, fname.getValue(), lname
 								.getValue(), cernId.getValue());
 
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
 					// }
 				});
 
 				ValidationForm form = new ValidationForm(ok, cancel);
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 				m.setTitle("Add a new User");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 		table.addColumn(name, new TextHeader("Name"),
 				clientFactory.isSupervisor() ? nameFooter : null);
 
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
 			
 			public boolean onPreviewColumnSortEvent(Context context,
 					Element elem, NativeEvent event) {
 				// events are handled, do not sort, fix for #454
 				return false;
 			}
 		};
 		deviceFooter.setUpdater(new ValueUpdater<String>() {
 
 			@Override
 			public void update(String value) {
 				ValidationFieldset fieldset = new ValidationFieldset();
 
 				final TextBoxField ptuId = new TextBoxField("PTU ID",
 						new StringValidator(2, 20, "Enter alphanumeric ID"));
 				fieldset.add(ptuId);
 
 				final TextBoxField ip = new TextBoxField("IP");
 				fieldset.add(ip);
 
 				final TextBoxField macAddress = new TextBoxField("MAC Address");
 				fieldset.add(macAddress);
 
 				final TextBoxField hostName = new TextBoxField("Host Name");
 				fieldset.add(hostName);
 
 				final TextAreaField description = new TextAreaField(
 						"Description");
 				fieldset.add(description);
 
 				final Modal m = new Modal();
 
 				Button cancel = new Button("Cancel");
 				cancel.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 					}
 				});
 
 				Button ok = new Button("Ok");
 				ok.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						m.hide();
 
 						Device device = new Device(0, ptuId.getValue(), ip
 								.getValue(), description.getValue(), macAddress
 								.getValue(), hostName.getValue());
 
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
 					// }
 				});
 
 				ValidationForm form = new ValidationForm(ok, cancel);
 				form.setType(FormType.HORIZONTAL);
 				form.add(fieldset);
 
 				m.setTitle("Add a new PTU");
 				m.add(form);
 				m.add(new ModalFooter(cancel, ok));
 				m.show();
 			}
 		});
 		table.addColumn(ptu, new TextHeader("PTU ID"),
 				clientFactory.isSupervisor() ? deviceFooter : null);
 
 		// Impact #
 		EditTextColumn<Intervention> impact = new EditTextColumn<Intervention>() {
 			@Override
 			public String getValue(Intervention object) {
 				return object.getImpactNumber() != null ? object
 						.getImpactNumber() : "";
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_inspections.impact_num";
 			}
 		};
 		impact.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		impact.setSortable(true);
 		impact.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 			@Override
 			public void update(int index, Intervention object, String value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 
 				interventionService.updateInterventionImpactNumber(
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
 		table.addColumn(impact, new TextHeader("Impact #"), new TextHeader(""));
 
 		// Rec Status
 //		TextColumn<Intervention> recStatus = new TextColumn<Intervention>() {
 //			@Override
 //			public String getValue(Intervention object) {
 //				return object.getRecStatus() != null ? Double.toString(object
 //						.getRecStatus()) : "";
 //			}
 //
 //			@Override
 //			public String getDataStoreName() {
 //				return "tbl_inspections.rec_status";
 //			}
 //		};
 //		impact.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 //		impact.setSortable(true);
 //		impact.setEnabled(clientFactory.isSupervisor());
 //		table.addColumn(recStatus, new TextHeader("Rec Status"),
 //				new TextHeader(""));
 
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
 		description.setEnabled(clientFactory.isSupervisor());
 		description.setFieldUpdater(new FieldUpdater<Intervention, String>() {
 			@Override
 			public void update(int index, Intervention object, String value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 
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
 					showUpdate = true;
 					table.redraw();
 				}
 			}
 		});
 
 		return true;
 	}
 
 	private void selectIntervention(Intervention intervention) {
 	}
 
 	private boolean needsUpdate() {
 		if (showUpdate) {
 			ColumnSortList sortList = table.getColumnSortList();
 			ColumnSortInfo sortInfo = sortList.size() > 0 ? sortList.get(0)
 					: null;
 			if (sortInfo == null) {
 				return true;
 			}
 			if (!sortInfo.getColumn().equals(startTime)) {
 				return true;
 			}
 			if (sortInfo.isAscending()) {
 				return true;
 			}
 			showUpdate = (scrollPanel.getVerticalScrollPosition() != scrollPanel
 					.getMinimumVerticalScrollPosition())
 					|| (pager.getPage() != pager.getPageStart());
 			return showUpdate;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean update() {
 		// show or hide update button
 		update.setVisible(needsUpdate());
 
 		RangeChangeEvent.fire(table, table.getVisibleRange());
 		table.redraw();
 
 		return false;
 	}
 }
