 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.MultiField;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.RowExpander;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.InstitutionGetService;
 import com.scholastic.sbam.client.services.InstitutionGetServiceAsync;
 import com.scholastic.sbam.client.services.AgreementLinkGetService;
 import com.scholastic.sbam.client.services.AgreementLinkGetServiceAsync;
 import com.scholastic.sbam.client.services.UpdateAgreementLinkNoteService;
 import com.scholastic.sbam.client.services.UpdateAgreementLinkNoteServiceAsync;
 import com.scholastic.sbam.client.services.UpdateAgreementLinkService;
 import com.scholastic.sbam.client.services.UpdateAgreementLinkServiceAsync;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedComboBox;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedMultiField;
 import com.scholastic.sbam.client.uiobjects.fields.InstitutionSearchField;
 import com.scholastic.sbam.client.uiobjects.fields.LockableFieldSet;
 import com.scholastic.sbam.client.uiobjects.fields.NotesIconButtonField;
 import com.scholastic.sbam.client.uiobjects.foundation.AppSleeper;
 import com.scholastic.sbam.client.uiobjects.foundation.FieldFactory;
 import com.scholastic.sbam.client.uiobjects.foundation.GridSupportPortlet;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.shared.objects.AgreementInstance;
 import com.scholastic.sbam.shared.objects.AgreementLinkTuple;
 import com.scholastic.sbam.shared.objects.InstitutionInstance;
 import com.scholastic.sbam.shared.objects.AgreementLinkInstance;
 import com.scholastic.sbam.shared.objects.LinkTypeInstance;
 import com.scholastic.sbam.shared.objects.SimpleKeyProvider;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class AgreementLinkPortlet extends GridSupportPortlet<AgreementLinkInstance> implements AppSleeper, AppPortletRequester {
 	
 	protected final int DIRTY_FORM_LISTEN_TIME	=	250;
 	protected final int DEFAULT_HEIGHT			=	270;
 	
 	protected final AgreementLinkGetServiceAsync		agreementLinkGetService			= GWT.create(AgreementLinkGetService.class);
 	protected final UpdateAgreementLinkServiceAsync		updateAgreementLinkService		= GWT.create(UpdateAgreementLinkService.class);
 	protected final UpdateAgreementLinkNoteServiceAsync	updateAgreementLinkNoteService	= GWT.create(UpdateAgreementLinkNoteService.class);
 	protected final InstitutionGetServiceAsync			institutionGetService			= GWT.create(InstitutionGetService.class);
 	
 	protected int							agreementLinkId;
 	protected AgreementLinkInstance			agreementLink;
 	protected InstitutionInstance			agreementLinkInstitution;
 	protected InstitutionInstance			createForInstitution;
 	protected String						identificationTip	=	"";
 	
 	protected FormPanel						outerContainer;
 
 	protected ListStore<BeanModel>			agreementsStore;
 	protected Grid<BeanModel>				agreementsGrid;
 	
 	protected Timer							dirtyFormListener;
 	
 	protected ToolBar						editSaveToolBar;
 	protected Button						editButton;
 	protected Button						cancelButton;
 	protected Button						saveButton;
 	
 	protected AppPortletProvider			portletProvider;
 	
 	protected ToolTipConfig					notesToolTip		= new ToolTipConfig();
 
 	protected FieldSet						agreementsFieldSet;
 	
 	protected MultiField<String>			linkIdNotesCombo	= new EnhancedMultiField<String>("Link Id:");
 	protected NumberField					linkIdField			= getIntegerField("");
 	protected NotesIconButtonField<String>	notesField			= getNotesButtonField();
 	protected LabelField					idTipField			= new LabelField();
 	protected InstitutionSearchField		institutionField	= getInstitutionField("ucn", "Institition", 0, "The institution for the agreement link.");
 	protected LabelField					addressDisplay		= new LabelField();
 	protected NumberField					ucnDisplay			= getIntegerField("UCN");
 	protected LabelField					customerTypeDisplay	= new LabelField();
 	protected LabelField					statusDisplay		= new LabelField();
 	protected CheckBox						statusField			= FieldFactory.getCheckBoxField("Link is Active");
 	protected EnhancedComboBox<BeanModel>	linkTypeField		= getComboField("linkType", 	"Link Type",	150,		
 																	"The link type assigned to this agreement link.",	
 																	UiConstants.getLinkTypes(), "linkTypeCode", "descriptionAndCode");
 	
 	public AgreementLinkPortlet() {
 		super(AppPortletIds.AGREEMENT_DISPLAY.getHelpTextId());
 //		forceHeight = DEFAULT_HEIGHT;
 	}
 
 	public void setAgreementLink(int agreementLinkId) {
 		this.agreementLinkId		= agreementLinkId;
 	}
 	
 	public String getIdentificationTip() {
 		return identificationTip;
 	}
 
 	public void setIdentificationTip(String identificationTip) {
 		if (identificationTip == null)
 			identificationTip = "";
 		this.identificationTip = identificationTip;
 	}
 
 	public InstitutionInstance getCreateForInstitution() {
 		return createForInstitution;
 	}
 
 	public void setCreateForInstitution(InstitutionInstance createForInstitution) {
 		this.createForInstitution = createForInstitution;
 	}
 
 	protected void setPortletHeading() {
 		String heading = "";
 		if (agreementLinkId <= 0) {
 			heading = "Create New Agreement Link";
 		} else {
 			heading = "Agreement Link " + AppConstants.appendCheckDigit(agreementLinkId);
 		}
 		if (agreementLinkInstitution != null) {
 			heading += " &nbsp;&nbsp;&nbsp; &mdash; <i>" + agreementLinkInstitution.getInstitutionName() + "</i>";
 		}
 		setHeading(heading);
 	}
 	
 	@Override
 	public String getPresenterToolTip() {
 		String tooltip = "";
 		if (agreementLinkId <= 0) {
 			tooltip = "Create new agreement link";
 		} else {
			tooltip = "Agreement Link " + agreementLinkId;
 		}
 		if (agreementLinkInstitution != null) {
 			tooltip += " &ndash; <i>" + agreementLinkInstitution.getInstitutionName() + "</i>";
 		}
 		if (identificationTip != null && identificationTip.length() > 0) {
 			tooltip += "<br/><i>" + identificationTip + "</i>";
 		}
 		return tooltip;
 	}
 	
 	@Override  
 	protected void onRender(Element parent, int index) {
 		super.onRender(parent, index);
 		
 		if (agreementLinkId <= 0) {
 			setToolTip(UiConstants.getQuickTip("Use this panel to create a new agreement link."));
 		}
 
 		setPortletHeading();
 		
 		setThis();
 		
 		outerContainer = getNewOuterFormPanel();
 		
 		createDisplay();
 		
 		add(outerContainer);
 		
 		if (agreementLinkId > 0)
 			loadAgreementLink(agreementLinkId);
 	}
 	
 	@Override
 	protected void afterRender() {
 		super.afterRender();
 		layout(true);
 
 		//	Handle "new link" automatically
 		if (agreementLinkId <= 0) {
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 			if (createForInstitution != null)
 				set(createForInstitution);
 			beginEdit();
 		}
 	}
 	
 	private void createDisplay() {
 		FormData formData90 = new FormData("-24"); 	//	new FormData("90%");
 
 		//	Required fields
 		institutionField.setAllowBlank(false);
 		linkTypeField.setAllowBlank(false);
 
 		linkIdField.setReadOnly(true);
 		ucnDisplay.setReadOnly(true);
 		
 		linkIdNotesCombo.setSpacing(20);
 		
 		linkIdNotesCombo.add(linkIdField);
 		linkIdNotesCombo.add(notesField);
 		outerContainer.add(linkIdNotesCombo,    formData90);
 
 		outerContainer.add(idTipField, formData90);
 		outerContainer.add(institutionField, formData90);
 		
 		outerContainer.add(addressDisplay, formData90); 
 		outerContainer.add(ucnDisplay, formData90);
 		outerContainer.add(customerTypeDisplay, formData90);
 		outerContainer.add(linkTypeField, formData90);
 			
 		statusDisplay.setFieldLabel("Status:");
 		outerContainer.add(statusField, formData90);
 		
 		addAgreementTermsGrid(formData90);
 		
 		addEditSaveButtons(outerContainer);
 	}
 	
 	protected void addAgreementTermsGrid(FormData formData90) {
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		
 		columns.add(getDisplayColumn("idCheckDigit",						"Agreement #",				100,
 					"This is the agreement Id."));
 		columns.add(getDisplayColumn("billUcn",								"UCN",						100,
 					"This is the Bill To UCN."));
 		columns.add(getDisplayColumn("institution.institutionName",			"Institution",				150,
 					"This is the Bill To Institution."));
 
 		RowExpander expander = getNoteExpander();
 		columns.add(expander);
 		
 		ColumnModel cm = new ColumnModel(columns);  
 
 		agreementsStore = new ListStore<BeanModel>();
 		agreementsStore.setKeyProvider(new SimpleKeyProvider("id"));
 		
 		agreementsGrid = new Grid<BeanModel>(agreementsStore, cm); 
 		agreementsGrid.addPlugin(expander);
 		agreementsGrid.setBorders(true);  
 		agreementsGrid.setAutoExpandColumn("institution.institutionName"); 
 		agreementsGrid.setStripeRows(true);
 		agreementsGrid.setColumnLines(false);
 		agreementsGrid.setHideHeaders(false);
 		
 		addRowListener(agreementsGrid);
 		
 		//	Open a new portlet to display an agreement when a row is selected
 		agreementsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 
 		
 //		final AppPortlet thisPortlet = this; 
 //		agreementsGrid.getSelectionModel().addListener(Events.SelectionChange,  
 //				new Listener<SelectionChangedEvent<ModelData>>() {  
 //					public void handleEvent(SelectionChangedEvent<ModelData> be) {  
 //						if (be.getSelection().size() > 0) {
 //						//	System.out.println("Agreement " + ((BeanModel) be.getSelectedItem()).get("idCheckDigit"));
 //							AgreementInstance agreement = (AgreementInstance) ((BeanModel) be.getSelectedItem()).getBean();
 //							AgreementPortlet portlet = (AgreementPortlet) portletProvider.getPortlet(AppPortletIds.AGREEMENT_DISPLAY);
 //							portlet.setAgreementId(agreement.getId());
 //							if (agreementLink != null) {
 //								String foundFor = "Link #" + agreementLink.getLinkIdCheckDigit();
 //								portlet.setIdentificationTip(foundFor);
 //							}
 ////							Old, simple way
 ////							int insertCol = (portalColumn == 0) ? 1 : 0;
 ////							portletProvider.insertPortlet(portlet, portalRow, insertCol);
 ////							New, more thorough way
 //							portletProvider.insertPortlet(portlet, portalRow, thisPortlet.getInsertColumn());
 //							agreementsGrid.getSelectionModel().deselectAll();
 //						} 
 //					}
 //			});
 	
 		agreementsFieldSet = new FieldSet();
 		agreementsFieldSet.setBorders(true);
 		agreementsFieldSet.setHeading("Agreements");
 		agreementsFieldSet.setCollapsible(true);
 		agreementsFieldSet.setDeferHeight(true);
 		agreementsFieldSet.setToolTip(UiConstants.getQuickTip("These are the agreements associated with this link.  Click the grid to inspect or edit an eagreement."));
 		agreementsFieldSet.setLayout(new FitLayout());
 		agreementsFieldSet.setHeight(300);
 		agreementsFieldSet.add(agreementsGrid, new FormData("95%")); // new FormData(cm.getTotalWidth() + 25, 200));
 		
 		outerContainer.add(agreementsFieldSet, new FormData("100%")); // new FormData(cm.getTotalWidth() + 20, 200));
 	}
 	
 //	This is not used, because a specific listener is added instead
 //	/**
 //	 * What to do when a row is selected.
 //	 */
 	@Override
 	protected void onRowSelected(BeanModel data) {
 		if (data != null)
 			openAgreement((AgreementInstance) data.getBean());
 	}
 	
 	protected void openAgreement(AgreementInstance agreement) {
 		AgreementPortlet portlet = (AgreementPortlet) portletProvider.getPortlet(AppPortletIds.AGREEMENT_DISPLAY);
 		portlet.setAgreementId(agreement.getId());
 		if (agreementLink != null) {
 			String foundFor = "Link #" + agreementLink.getLinkIdCheckDigit();
 			portlet.setIdentificationTip(foundFor);
 		}
 		portletProvider.insertPortlet(portlet, portalRow, this.getInsertColumn());
 		agreementsGrid.getSelectionModel().deselectAll();
 	}
 	
 	protected InstitutionSearchField getInstitutionField(String name, String label, int width, String toolTip) {
         InstitutionSearchField instCombo = new InstitutionSearchField();
 		FieldFactory.setStandard(instCombo, label);
 		
 		if (toolTip != null)
 			instCombo.setToolTip(toolTip);
 		if (width > 0)
 			instCombo.setWidth(width);
 		instCombo.setDisplayField("institutionName");
 		
 		instCombo.addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
 				selectInstitution(se.getSelectedItem());
 			}
 			
 		});
 		
 		return instCombo;
 	}
 	
 	protected void selectInstitution(BeanModel model) {
 		if (model == null) {	// No value selected means leave it as is
 			if (institutionField.getSelectedValue() != null)
 				matchToInstitution( (InstitutionInstance) institutionField.getSelectedValue().getBean() );
 			else
 				if (institutionField.getOriginalValue() != null)
 					matchToInstitution( (InstitutionInstance) institutionField.getOriginalValue().getBean());
 				else
 					matchToInstitution( agreementLinkInstitution );
 		} else
 			matchToInstitution( (InstitutionInstance) model.getBean() );
 	}
 	
 
 	protected void addEditSaveButtons(FormPanel targetPanel) {
 		
 		editSaveToolBar = new ToolBar();
 		editSaveToolBar.setAlignment(HorizontalAlignment.CENTER);
 		editSaveToolBar.setBorders(false);
 		editSaveToolBar.setSpacing(20);
 		editSaveToolBar.setMinButtonWidth(60);
 //		toolBar.addStyleName("clear-toolbar");
 		
 		editButton = new Button("Edit");
 		IconSupplier.forceIcon(editButton, IconSupplier.getEditIconName());
 		editButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				beginEdit();
 			}  
 		});
 		editSaveToolBar.add(editButton);
 		
 		cancelButton = new Button("Cancel");
 		IconSupplier.forceIcon(cancelButton, IconSupplier.getCancelIconName());
 		cancelButton.disable();
 		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				endEdit(false);
 			}  
 		});
 		editSaveToolBar.add(cancelButton);
 		
 		saveButton = new Button("Save");
 		IconSupplier.forceIcon(saveButton, IconSupplier.getSaveIconName());
 		saveButton.disable();
 		saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				handleSave();
 			}  
 		});
 		editSaveToolBar.add(saveButton);
 		
 		targetPanel.setBottomComponent(editSaveToolBar);
 		
 		addDirtyFormListener();
 	}
 	
 	protected void handleSave() {
 		endEdit(true);
 	}
 	
 	protected void addDirtyFormListener() {
 		if (dirtyFormListener == null) {
 			dirtyFormListener = new Timer() {
 
 				@Override
 				public void run() {
 					if (isDirtyForm())
 						handleDirtyForm();
 					else
 						handleCleanForm();
 				}
 				
 			};
 		}
 		
 		dirtyFormListener.scheduleRepeating(DIRTY_FORM_LISTEN_TIME);
 	}
 
 	protected boolean isDirtyForm() {
 		return agreementLink == null || outerContainer.isDirty();	//	formColumn1.isDirty() || formColumn2.isDirty();
 	}
 	
 	protected void handleDirtyForm() {
 		boolean ready = outerContainer.isValid();
 		
 		if (institutionField.getSelectedValue() == null) { 
 			institutionField.markInvalid("Select an instituion.");
 			ready = false;
 		} else
 			institutionField.clearInvalid();
 //		if (commissionTypeField.getSelectedValue() == null) {
 //			commissionTypeField.markInvalid("Select a commission code.");
 //			ready = false;
 //		} else
 //			commissionTypeField.clearInvalid();
 		
 		if (ready)
 			saveButton.enable();
 		else
 			saveButton.disable();
 	}
 	
 	protected void handleCleanForm() {
 		saveButton.disable();
 	}
 	
 	/**
 	 * Set attributes for the main container
 	 */
 	protected void setThis() {
 //		this.setFrame(true);  
 //		this.setCollapsible(true);  
 //		this.setAnimCollapse(false);
 		this.setLayout(new FitLayout());
 		this.setHeight(forceHeight);
 		IconSupplier.setIcon(this, IconSupplier.getAgreementIconName());
 //		this.setSize(grid.getWidth() + 50, 400);  
 	}
 	
 	protected NotesIconButtonField<String> getNotesButtonField() {
 		NotesIconButtonField<String> nibf = new NotesIconButtonField<String>(this) {
 			@Override
 			public void updateNote(String note) {
 				asyncUpdateNote(note);
 			}
 		};
 		nibf.setEmptyNoteText("Click the note icon to add notes for this agreement link.");
 		return nibf;
 	}
 	
 	protected void enableEditButton(boolean enabled) {
 		if (editButton != null) editButton.setEnabled(enabled);
 	}
 	
 	/**
 	 * Set an agreement on the form, and load its institution
 	 * @param site
 	 */
 	protected void set(AgreementLinkInstance agreementLink) {
 		this.agreementLink = agreementLink;
 		if (agreementLink == null) {
 			this.agreementLinkId = -1;
 			enableEditButton(false);
 		} else {
 			this.agreementLinkId = agreementLink.getLinkId();
 			enableEditButton(true);
 		}
 		
 		//	For existing records, set fields that cannot be changed to read only
 //		boolean isNew = agreementLink == null || agreementLink.isNewRecord() || agreementLink.isAddNew() || agreementLink.getLinkId() == 0;
 //		institutionField.setReadOnly(!isNew);
 		
 //		if (agreementLink != null)
 //			registerUserCache(agreementLink, identificationTip);
 		setPortletHeading();
 
 		if (agreementLink == null) {
 			MessageBox.alert("Link not found.", "The requested agreement link was not found.", null);
 			clearFormValues();
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 		} else {
 			
 			linkIdField.setValue(agreementLink.getLinkIdCheckDigit());
 			idTipField.setValue(identificationTip);	
 
 			if (agreementLink.getNote() != null && agreementLink.getNote().length() > 0) {
 				notesField.setEditMode();
 				notesField.setNote(agreementLink.getNote());
 			} else {
 				notesField.setAddMode();
 				notesField.setNote("");			
 			}
 			
 			addressDisplay.setValue("<i>Loading...</i>");
 			customerTypeDisplay.setValue("");
 			
 			linkTypeField.selectByKey(agreementLink.getLinkTypeCode());
 			
 			statusDisplay.setValue(AppConstants.getStatusDescription(agreementLink.getStatus()));
 			statusField.setValue(AppConstants.STATUS_ACTIVE == agreementLink.getStatus());
 			
 			if (agreementLink.getInstitution() != null) {
 				set(agreementLink.getInstitution());
 			} else {
 				if (agreementLink.getUcn() <= 0) {
 					set(InstitutionInstance.getEmptyInstance());
 				} else {
 					loadInstitution(agreementLink.getUcn());
 				}
 			}
 		}
 		
 		//	Resize things to account for the data
 		layout(true);
 
 		updatePresenterLabel();
 		updateUserPortlet();	// This is mostly for a "create" so the portlet knows the agreement ID has been set
 		setOriginalValues();
 //		endEdit(false);
 	}
 	
 	/**
 	 * Set an institution on the form
 	 * @param instance
 	 */
 	protected void set(InstitutionInstance instance) {
 		if (agreementLinkInstitution == instance)
 			return;
 		
 		agreementLinkInstitution = instance;
 		
 		if (agreementLinkInstitution == null) {
 			MessageBox.alert("Institution Not Found", "The Institution for the agreement link was not found.", null);
 			agreementLinkInstitution = InstitutionInstance.getEmptyInstance(); 
 		}
 
 		if (institutionField.getSelectedValue() == null || !agreementLinkInstitution.equals(institutionField.getSelectedValue().getBean())) {
 			institutionField.setValue(InstitutionInstance.obtainModel(agreementLinkInstitution));
 		}
 
 		if (agreementLinkInstitution != null)
 			registerUserCache(agreementLinkInstitution, identificationTip);
 		
 		setPortletHeading();
 		matchToInstitution(agreementLinkInstitution);
 	}
 	
 	protected void matchToInstitution(InstitutionInstance instance) {
 //		institutionBinding.bind(InstitutionInstance.obtainModel(billToInstitution));
 		
 		if (instance == null) {
 			ucnDisplay.setValue(0);
 			addressDisplay.setValue("");
 			customerTypeDisplay.setValue("");
 			return;
 		}
 		
 		ucnDisplay.setValue(instance.getUcn());
 		addressDisplay.setValue(instance.getHtmlAddress());
 		customerTypeDisplay.setValue(instance.getPublicPrivateDescription() + " / " + instance.getGroupDescription() + " &rArr; " + instance.getTypeDescription());
 	}
 	
 	public void beginEdit() {
 		editButton.disable();
 		cancelButton.enable();
 		enableFields();
 	}
 	
 	public void endEdit(boolean save) {
 		cancelButton.disable();
 		saveButton.disable();
 		disableFields();
 		if (save) {
 			editButton.disable();	//	Disable this ...let the update enable it when the response arrives
 			asyncUpdate();
 		} else {
 			resetFormValues();
 			editButton.enable();
 		}
 	}
 	
 	public void clearFormValues() {
 		outerContainer.clear();
 	}
 	
 	public void resetFormValues() {
 		outerContainer.reset();
 	}
 	
 	public void setOriginalValues() {
 		setOriginalValues(outerContainer);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setOriginalValues(FormPanel formPanel) {
 		for (Field<?> field : formPanel.getFields()) {
 			if (field instanceof EnhancedComboBox) {
 				EnhancedComboBox<BeanModel>  ecb = (EnhancedComboBox<BeanModel>) field;
 				ecb.setOriginalValue(ecb.getSelectedValue());
 			} else if (field instanceof InstitutionSearchField) {
 				InstitutionSearchField  isf = (InstitutionSearchField) field;
 				isf.setOriginalValue(isf.getSelectedValue());
 			} else {
 				((Field<Object>) field).setOriginalValue(field.getValue());
 			}
 		}
 	}
 	
 	public void enableFields() {
 		for (Field<?> field : outerContainer.getFields()) {
 			if (field.getParent() != null && field.getParent() instanceof LockableFieldSet) {
 				LockableFieldSet lfs = (LockableFieldSet) field.getParent();
 				lfs.enableFields(true);
 			} else 
 				field.enable();
 		}
 	}
 	
 	public void disableFields() {
 		for (Field<?> field : outerContainer.getFields()) {
 			if (field == linkIdNotesCombo)
 				linkIdField.disable();
 			else
 				field.disable();
 		}
 	}
 
 	/**
 	 * Load the agreement for an ID
 	 * @param id
 	 */
 	protected void loadAgreementLink(final int agreementLinkId) {
 		agreementLinkGetService.getAgreementLink(agreementLinkId, 
 				true, true,	// Include linked agreements
 				new AsyncCallback<AgreementLinkTuple>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Agreement Link access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(AgreementLinkTuple agreementLinkTuple) {
 						set(agreementLinkTuple.getAgreementLink());
 						
 						agreementsStore.removeAll();
 						if (agreementLinkTuple.getAgreements() != null) {
 							for (AgreementInstance agreement : agreementLinkTuple.getAgreements()) {
 								agreementsStore.add(AgreementInstance.obtainModel(agreement));
 							}
 						}
 					}
 			});
 	}
 
 	/**
 	 * Load the institution for the agreement
 	 * @param ucn
 	 */
 	protected void loadInstitution(final int ucn) {
 		institutionGetService.getInstitution(ucn, false,
 				new AsyncCallback<InstitutionInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Institution access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(InstitutionInstance institution) {
 						set(institution);
 					}
 			});
 	}
 	
 	protected int getFieldIntValue(NumberField field) {
 		if (field.getValue() == null)
 			return 0;
 		else
 			return field.getValue().intValue();
 	}
 	
 //	@Override
 //	public void fireUserCacheUpdateEvents(UserCacheTarget target) {
 //		//	Fire an event so any listening portlets can update themselves
 //		AppEvent appEvent = new AppEvent(AppEvents.SiteAccess);
 //		if (target instanceof AgreementLinkInstance)
 //			appEvent.set( (AgreementLinkInstance) target);
 //		AppEventBus.getSingleton().fireEvent(AppEvents.SiteAccess, appEvent);
 //	}
 
 	protected void asyncUpdate() {
 	
 		// Set field values from form fields
 		
 		if (agreementLink == null) {
 			agreementLink = new AgreementLinkInstance();
 			agreementLink.setNewRecord(true);
 			agreementLink.setStatus(AppConstants.STATUS_ACTIVE);	//	This is later overridden by statusField, but we set it here just in case that's removed at some point
 		}
 		
 		agreementLink.setInstitution( ((InstitutionInstance) institutionField.getSelectedValue().getBean()) );
 		agreementLink.setUcn(agreementLink.getInstitution().getUcn());
 		agreementLink.setLinkType( (LinkTypeInstance) linkTypeField.getValue().getBean()) ;
 //		agreementLink.setLinkTypeCode( ((LinkTypeInstance) linkTypeField.getValue().getBean()).getLinkTypeCode()) ;
 		agreementLink.setStatus(statusField.getValue() ? AppConstants.STATUS_ACTIVE : AppConstants.STATUS_INACTIVE);
 		
 		if (agreementLink.isNewRecord())
 			agreementLink.setNote(notesField.getNote());
 		else
 			agreementLink.setNote(null);	//	This will keep the note from being updated by this call
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateAgreementLinkService.updateAgreementLink(agreementLink,
 				new AsyncCallback<UpdateResponse<AgreementLinkInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Agreement Link update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						editButton.enable();
 					}
 
 					public void onSuccess(UpdateResponse<AgreementLinkInstance> updateResponse) {
 						AgreementLinkInstance updatedLink = (AgreementLinkInstance) updateResponse.getInstance();
 						if (updatedLink.isNewRecord()) {
 							updatedLink.setNewRecord(false);
 							if (updatedLink.getInstitution() == null)
 								identificationTip = "Agreement Link created " + new Date();
 							else
 								identificationTip = "Agreement Link created for " + updatedLink.getInstitution().getInstitutionName();
 						}
 						agreementLink.setNewRecord(false);
 						set(updatedLink);
 				//		enableAgreementButtons(true);
 				}
 			});
 	}
 
 	protected void asyncUpdateNote(String note) {
 	
 		// Set field values from form fields
 		
 		if (agreementLink == null || agreementLink.isNewRecord()) {
 			return;
 		}
 		
 		agreementLink.setNote(note);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateAgreementLinkNoteService.updateAgreementLinkNote(agreementLink,
 				new AsyncCallback<UpdateResponse<AgreementLinkInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Agreement Link note update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						notesField.unlockNote();
 					}
 
 					public void onSuccess(UpdateResponse<AgreementLinkInstance> updateResponse) {
 						AgreementLinkInstance updatedAgreement = (AgreementLinkInstance) updateResponse.getInstance();
 						if (!notesField.getNote().equals(updatedAgreement.getNote())) {
 							notesField.setNote(updatedAgreement.getNote());
 							agreementLink.setNote(updatedAgreement.getNote());
 						}
 						notesField.unlockNote();
 				}
 			});
 	}
 	
 	public int getAgreementLinkId() {
 		return agreementLinkId;
 	}
 
 	public void setAgreementLinkId(int agreementLinkId) {
 		this.agreementLinkId = agreementLinkId;
 	}
 
 	@Override
 	public void onExpand() {
 		super.onExpand();
 		awaken();
 	}
 	
 	@Override
 	public void onCollapse() {
 		super.onCollapse();
 		sleep();
 	}
 
 	@Override
 	public void setAppPortletProvider(AppPortletProvider portletProvider) {
 		this.portletProvider = portletProvider;
 	}
 	
 	@Override
 	public String getShortPortletName() {
 		if (agreementLinkId > 0)
 			return "Link " + agreementLinkId;
 		return "Create Agreement Link";
 	}
 	
 	@Override
 	public boolean allowDuplicatePortlets() {
 		//	Not allowed for a particular agreement link
 		if (agreementLinkId > 0)
 			return false;
 		//	Allowed for "create new"
 		return true;
 	}
 	
 	@Override
 	public String getPortletIdentity() {
 		return getPortletIdentity(agreementLinkId);
 	}
 	
 	public static String getPortletIdentity(int agreementLinkId) {
 		if (agreementLinkId <= 0)
 			return AgreementLinkPortlet.class.getName();
 		return AgreementLinkPortlet.class.getName() + ":" + agreementLinkId;
 	}
 	
 	/**
 	 * Turn on the listener timer when waking up.
 	 */
 	@Override
 	public void awaken() {
 		if (this.isExpanded()) {
 			if (dirtyFormListener != null)
 				dirtyFormListener.scheduleRepeating(DIRTY_FORM_LISTEN_TIME);
 		}
 	}
 
 	/**
 	 * Turn off the listener timer when going to sleep.
 	 */
 	@Override
 	public void sleep() {
 		if (dirtyFormListener != null)
 			dirtyFormListener.cancel();
 	}
 
 	/**
 	 * For the user cache, set this instance from a string of data stored offline
 	 */
 	@Override
 	public void setFromKeyData(String keyData) {
 		if (keyData == null)
 			return;
 
 		String [] parts = keyData.split(":");
 		if (parts.length > 0) agreementLinkId = Integer.parseInt(parts [0]);
 		if (parts.length > 1) identificationTip = parts [1];
 	}
 
 	/**
 	 * For the user cache, return the "set" data as a string for offline storage
 	 */
 	@Override
 	public String getKeyData() {
 		if (identificationTip == null)
 			return agreementLinkId + "";
 		else
 			return agreementLinkId + ":" + identificationTip;
 	}
 
 }
