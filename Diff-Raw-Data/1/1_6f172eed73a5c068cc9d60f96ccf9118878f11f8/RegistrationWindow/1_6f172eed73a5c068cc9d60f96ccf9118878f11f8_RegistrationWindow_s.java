 package kg.cloud.uims.ui;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 
 import kg.cloud.uims.MyVaadinApplication;
 import kg.cloud.uims.dao.DbStudLess;
 import kg.cloud.uims.dao.DbStudReg;
 import kg.cloud.uims.i18n.UimsMessages;
 import kg.cloud.uims.resources.DataContainers;
 import kg.cloud.uims.resources.RegistrationPDF;
 
 import com.vaadin.data.Container.Filter;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.themes.ChameleonTheme;
 
 public class RegistrationWindow extends Window implements Button.ClickListener,
 		Property.ValueChangeListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	Table notTakenSubjects = new Table();
 	Table currentSubjects = new Table();
 	Button toPDF = new Button();
 	Button save = new Button();
 	Button moveDown = new Button();
 	Button moveUp = new Button();
 	IndexedContainer registeredDatasource;
 	IndexedContainer notTakenDatasource;
 	int hourSum = 0;
 	private Item selectedTableItem1 = null;
 	private Item selectedTableItem2 = null;
 	private String subjectID;
 	private String subjectIDselected1;
 	private Boolean saveStatus = false;
 	private String studentId;
 	private Label studInfo = new Label();
 	private TextField byYear = new TextField();
 	private TextField byCode = new TextField();
 
 	MyVaadinApplication app;
 
 	public RegistrationWindow(final MyVaadinApplication app, String studentId,
 			String studentFullName) {
 		/*
 		 * first check of registration status
 		 */
 		if(app.getCurrentSemester().getRegStatus()==0){
 			notTakenSubjects.setReadOnly(true);
 			currentSubjects.setReadOnly(true);
 			save.setReadOnly(true);
 			moveDown.setReadOnly(true);
 			moveUp.setReadOnly(true);
 		}
 		// super(app.getMessage(UimsMessages.RegistrationHeader+" : "+studentFullName));
 		this.setModal(true);
 		this.setCaption(app.getMessage(UimsMessages.RegistrationHeader) + " "
 				+ studentFullName);
 		this.setWidth("80%");
 		this.setHeight("90%");
 		this.app = app;
 		this.studentId = studentId;
 
 		currentSubjects.setCaption(app
 				.getMessage(UimsMessages.TableCurrentSubjects) + " :");
 		currentSubjects.setWidth("100%");
 		currentSubjects.setHeight(7, UNITS_CM);
 		currentSubjects.setSelectable(true);
 		currentSubjects.setImmediate(true);
 		currentSubjects.setFooterVisible(true);
 		currentSubjects.setStyleName(ChameleonTheme.TABLE_STRIPED);
 
 		notTakenSubjects.setCaption(app
 				.getMessage(UimsMessages.TableNotTakenSubjects) + " :");
 		notTakenSubjects.setWidth("100%");
 		notTakenSubjects.setHeight(6, UNITS_CM);
 		notTakenSubjects.setSelectable(true);
 		notTakenSubjects.setImmediate(true);
 		notTakenSubjects.setStyleName(ChameleonTheme.TABLE_STRIPED);
		notTakenSubjects.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
 
 		VerticalLayout mainLayout = new VerticalLayout();
 		VerticalLayout tablesLayout = new VerticalLayout();
 		HorizontalLayout controlButtons = new HorizontalLayout();
 		HorizontalLayout controlLayout = new HorizontalLayout();
 		HorizontalLayout filtersLayout = new HorizontalLayout();
 
 		mainLayout.setSpacing(true);
 		tablesLayout.addComponent(notTakenSubjects);
 		tablesLayout.addComponent(controlButtons);
 		tablesLayout.addComponent(currentSubjects);
 
 		currentSubjects.addListener((Property.ValueChangeListener) this);
 		notTakenSubjects.addListener((Property.ValueChangeListener) this);
 		controlButtons.setWidth("100%");
 		controlButtons.setHeight("10%");
 		controlLayout.setSpacing(true);
 		
 		ThemeResource iconDown = new ThemeResource("../runo/icons/16/arrow-down.png");
 		ThemeResource iconUp = new ThemeResource("../runo/icons/16/arrow-up.png");
 		ThemeResource iconPDF = new ThemeResource("../runo/icons/16/document-pdf.png");
 		ThemeResource iconOK = new ThemeResource("../runo/icons/16/ok.png");
 
 		moveDown.setCaption(app.getMessage(UimsMessages.MoveDownButton));
 		moveDown.setIcon(iconDown);
 		moveDown.addListener((Button.ClickListener) this);
 		moveUp.setCaption(app.getMessage(UimsMessages.MoveUpButton));
 		moveUp.setIcon(iconUp);
 		moveUp.addListener((Button.ClickListener) this);
 		save.setCaption(app.getMessage(UimsMessages.SaveButton));
 		save.setIcon(iconOK);
 		save.addListener((Button.ClickListener) this);
 		toPDF.setCaption(app.getMessage(UimsMessages.ButtonMakePDF));
 		toPDF.setIcon(iconPDF);
 		toPDF.addListener((Button.ClickListener) this);
 
 		Label filter1Label = new Label(app.getMessage(UimsMessages.FilterByYearLabel));
 		filter1Label.setStyleName(ChameleonTheme.LABEL_BIG);
 		Label filter2Label = new Label(app.getMessage(UimsMessages.FilterByCodeLabel));
 		filter2Label.setStyleName(ChameleonTheme.LABEL_BIG);
 		byYear.setStyleName(ChameleonTheme.TEXTFIELD_SMALL);
 		byCode.setStyleName(ChameleonTheme.TEXTFIELD_SMALL);
 		byYear.addListener(new TextChangeListener() {
 
 			public void textChange(TextChangeEvent event) {
 				if (event.getText().length() > 0) {
 					Filter filter = new SimpleStringFilter(app
 							.getMessage(UimsMessages.StudyYear), event
 							.getText(), true, false);
 
 					notTakenDatasource.removeAllContainerFilters();
 					notTakenDatasource.addContainerFilter(filter);
 				} else {
 					notTakenDatasource.removeAllContainerFilters();
 				}
 				// TODO Auto-generated method stub
 
 			}
 		});
 		
 		byCode.addListener(new TextChangeListener() {
 
 			public void textChange(TextChangeEvent event) {
 				if (event.getText().length() > 0) {
 					Filter filter = new SimpleStringFilter(app
 							.getMessage(UimsMessages.SubjectCode), event
 							.getText(), true, false);
 
 					notTakenDatasource.removeAllContainerFilters();
 					notTakenDatasource.addContainerFilter(filter);
 				} else {
 					notTakenDatasource.removeAllContainerFilters();
 				}
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		studInfo.setCaption(app.getMessage(UimsMessages.LabelStudent) + " : "
 				+ studentFullName);
 		studInfo.setImmediate(true);
 
 		controlButtons.addComponent(moveDown);
 		controlButtons.addComponent(moveUp);
 
 		controlLayout.addComponent(toPDF);
 
 		try {
 
 			DbStudReg dbStudReg = new DbStudReg();
 			dbStudReg.connect();
 			String status = dbStudReg
 					.execSQL(studentId, app.getCurrentSemester().getId(), app
 							.getCurrentYear().getId());
 			dbStudReg.close();
 			if (status.equals("0")) {
 				controlLayout.addComponent(save);
 			} else {
 				studInfo.setCaption(app.getMessage(UimsMessages.LabelStudent)
 						+ " : " + studentFullName + "(Registered)");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		fillTables();
 
 		//filtersLayout.setSizeFull();
 		filtersLayout.setSpacing(true);
 		filtersLayout.addComponent(studInfo);
 		filtersLayout.addComponent(filter1Label);
 		filtersLayout.addComponent(byYear);
 		filtersLayout.addComponent(filter2Label);
 		filtersLayout.addComponent(byCode);
 		mainLayout.addComponent(filtersLayout);
 		mainLayout.addComponent(tablesLayout);
 		mainLayout.addComponent(controlLayout);
 		addComponent(mainLayout);
 
 	}
 
 	public void buttonClick(ClickEvent event) {
 		final Button source = event.getButton();
 		if (source == moveDown) {
 			if (selectedTableItem1 != null) {
 				hourSum += Integer.parseInt(selectedTableItem1.getItemProperty(
 						app.getMessage(UimsMessages.SubjectHour)).toString());
 
 				if (hourSum <= 39) {
 					Item item = registeredDatasource.addItem(subjectID);
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectCode)).setValue(
 							selectedTableItem1.getItemProperty(app
 									.getMessage(UimsMessages.SubjectCode)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectName)).setValue(
 							selectedTableItem1.getItemProperty(app
 									.getMessage(UimsMessages.SubjectName)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectCredit))
 							.setValue(
 									selectedTableItem1.getItemProperty(app
 											.getMessage(UimsMessages.SubjectCredit)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectHour)).setValue(
 							selectedTableItem1.getItemProperty(app
 									.getMessage(UimsMessages.SubjectHour)));
 					item.getItemProperty(app.getMessage(UimsMessages.StudyYear))
 							.setValue(
 									selectedTableItem1.getItemProperty(app
 											.getMessage(UimsMessages.StudyYear)));
 					item.getItemProperty(app.getMessage(UimsMessages.Semester))
 							.setValue(
 									selectedTableItem1.getItemProperty(app
 											.getMessage(UimsMessages.Semester)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectRegistrationStatus))
 							.setValue(new String("0"));
 
 					
 					 currentSubjects
 					 .setContainerDataSource(registeredDatasource);
 					 
 
 					notTakenDatasource.removeItem(subjectID);
 					// notTakenSubjects.setContainerDataSource(notTakenDatasource);
 					currentSubjects.setColumnFooter(
 							app.getMessage(UimsMessages.SubjectHour),
 							Integer.toString(hourSum));
 
 					saveStatus = true;
 
 					selectedTableItem1 = null;
 				} else {
 					hourSum -= Integer.parseInt(selectedTableItem1
 							.getItemProperty(
 									app.getMessage(UimsMessages.SubjectHour))
 							.toString());
 					getWindow().showNotification(
 							app.getMessage(UimsMessages.NotifSumOFSubjExceed));
 				}
 			} else {
 				getWindow().showNotification(
 						app.getMessage(UimsMessages.NotifNothingSelected));
 			}
 		}
 
 		if (source == moveUp) {
 			if (selectedTableItem2 != null) {
 				if (selectedTableItem2
 						.getItemProperty(
 								app.getMessage(UimsMessages.SubjectRegistrationStatus))
 						.toString().equals("0")) {
 					Item item = notTakenDatasource.addItem(subjectIDselected1);
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectCode)).setValue(
 							selectedTableItem2.getItemProperty(app
 									.getMessage(UimsMessages.SubjectCode)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectName)).setValue(
 							selectedTableItem2.getItemProperty(app
 									.getMessage(UimsMessages.SubjectName)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectCredit))
 							.setValue(
 									selectedTableItem2.getItemProperty(app
 											.getMessage(UimsMessages.SubjectCredit)));
 					item.getItemProperty(
 							app.getMessage(UimsMessages.SubjectHour)).setValue(
 							selectedTableItem2.getItemProperty(app
 									.getMessage(UimsMessages.SubjectHour)));
 					item.getItemProperty(app.getMessage(UimsMessages.StudyYear))
 							.setValue(
 									selectedTableItem2.getItemProperty(app
 											.getMessage(UimsMessages.StudyYear)));
 					item.getItemProperty(app.getMessage(UimsMessages.Semester))
 							.setValue(
 									selectedTableItem2.getItemProperty(app
 											.getMessage(UimsMessages.Semester)));
 
 					hourSum -= Integer.parseInt(selectedTableItem2
 							.getItemProperty(
 									app.getMessage(UimsMessages.SubjectHour))
 							.toString());
 					currentSubjects.setColumnFooter(
 							app.getMessage(UimsMessages.SubjectHour),
 							Integer.toString(hourSum));
 
 					registeredDatasource.removeItem(subjectIDselected1);
 					notTakenSubjects.setContainerDataSource(notTakenDatasource);
 					/*
 					 * currentSubjects
 					 * .setContainerDataSource(registeredDatasource);
 					 */
 					selectedTableItem2 = null;
 					// getWindow().showNotification("The item "+selectedTableItem2.getItemProperty(app.getMessage(UimsMessages.SubjectCode)).toString()+"will be removed");
 				} else {
 					getWindow().showNotification(
 							app.getMessage(UimsMessages.NotifCantRemoveSubj));
 				}
 			}
 
 		}
 		if (source == save) {
 			if (registeredDatasource.size() != 0) {
 				if (saveStatus) {
 					try {
 						DbStudLess dbsl = new DbStudLess();
 						dbsl.connect();
 						final String status = "1";
 						final String semester = Integer.toString(app
 								.getCurrentSemester().getId());
 						final String year = Integer.toString(app
 								.getCurrentYear().getId());
 
 						for (int i = 0; i < registeredDatasource.size(); i++) {
 							Item item = registeredDatasource
 									.getItem(registeredDatasource
 											.getIdByIndex(i));
 
 							Object value = item
 									.getItemProperty(
 											app.getMessage(UimsMessages.SubjectRegistrationStatus))
 									.getValue();
 							if (value.toString().equals("0")) {
 								final String subject = registeredDatasource
 										.getIdByIndex(i).toString();
 								dbsl.execRegistration(studentId, subject,
 										status, year, semester);
 								item.getItemProperty(
 										app.getMessage(UimsMessages.SubjectRegistrationStatus))
 										.setValue("1");
 
 							}
 						}
 						saveStatus = false;
 						getWindow()
 								.showNotification(
 										app.getMessage(UimsMessages.NotifThankYouRegistr));
 						dbsl.close();
 
 						DbStudReg dbStudReg = new DbStudReg();
 						dbStudReg.connect();
 						dbStudReg.editStatus(studentId, app
 								.getCurrentSemester().getId(), app
 								.getCurrentYear().getId());
 						dbStudReg.close();
 
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					/*
 					 * currentSubjects
 					 * .setContainerDataSource(registeredDatasource);
 					 */
 					save.setEnabled(false);
 
 				}
 				// getWindow().showNotification(
 				// registeredDatasource.getIdByIndex(0).toString());
 			}
 
 		}
 		if (source == toPDF) {
 			Resource pdf = createPdf();
 			// pdfContents.setSource(pdf);
 			app.getMainWindow().open(pdf);
 		}
 
 	}
 
 	public void fillTables() {
 		DataContainers dc = new DataContainers(app);
 		notTakenDatasource = dc.getStudentNotTookYetSubjects(studentId,
 				Integer.toString(app.getCurrentSemester().getId()));
 		notTakenSubjects.setContainerDataSource(notTakenDatasource);
 
 		registeredDatasource = dc.getStudentRegisteredSubjects(studentId,
 				Integer.toString(app.getCurrentSemester().getId()),
 				Integer.toString(app.getCurrentYear().getId()));
 		currentSubjects.setContainerDataSource(registeredDatasource);
 
 		for (int i = 0; i < registeredDatasource.size(); i++) {
 			Item item = registeredDatasource.getItem(registeredDatasource
 					.getIdByIndex(i));
 			Object value = item.getItemProperty(
 					app.getMessage(UimsMessages.SubjectHour)).getValue();
 
 			Number amount;
 			try {
 				amount = NumberFormat.getNumberInstance().parse(
 						value.toString());
 				hourSum += amount.intValue();
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		currentSubjects.setColumnFooter(
 				app.getMessage(UimsMessages.SubjectName),
 				app.getMessage(UimsMessages.SubjectHoursSum));
 		currentSubjects.setColumnFooter(
 				app.getMessage(UimsMessages.SubjectHour),
 				Integer.toString(hourSum));
 
 	}
 	
 
 	public void valueChange(ValueChangeEvent event) {
 		Property property = event.getProperty();
 		if (property == notTakenSubjects) {
 			selectedTableItem1 = notTakenDatasource.getItem(notTakenSubjects
 					.getValue());
 			if (selectedTableItem1 != null)
 				subjectID = notTakenSubjects.getValue().toString();
 		}
 		if (property == currentSubjects) {
 			selectedTableItem2 = registeredDatasource.getItem(currentSubjects
 					.getValue());
 			if (selectedTableItem2 != null)
 				subjectIDselected1 = currentSubjects.getValue().toString();
 		}
 
 	}
 
 	private Resource createPdf() {
 		// Here we create a new StreamResource which downloads our StreamSource,
 		// which is our pdf.
 		StreamResource resource = new StreamResource(new RegistrationPDF(
 				studentId, subjectID, Integer.toString(app.getCurrentSemester()
 						.getId()), Integer.toString(app.getCurrentYear()
 						.getId())), "test.pdf?" + System.currentTimeMillis(),
 				app);
 		// Set the right mime type
 		resource.setMIMEType("application/pdf");
 		return resource;
 	}
 
 }
