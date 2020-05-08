 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ToggleButton;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.MultiField;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.CardLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.InstitutionGetService;
 import com.scholastic.sbam.client.services.InstitutionGetServiceAsync;
 import com.scholastic.sbam.client.services.PreferenceCategoryListService;
 import com.scholastic.sbam.client.services.PreferenceCategoryListServiceAsync;
 import com.scholastic.sbam.client.services.SiteLocationGetService;
 import com.scholastic.sbam.client.services.SiteLocationGetServiceAsync;
 import com.scholastic.sbam.client.services.UpdateSiteLocationNoteService;
 import com.scholastic.sbam.client.services.UpdateSiteLocationNoteServiceAsync;
 import com.scholastic.sbam.client.services.UpdateSiteLocationService;
 import com.scholastic.sbam.client.services.UpdateSiteLocationServiceAsync;
 import com.scholastic.sbam.client.uiobjects.events.AppEvent;
 import com.scholastic.sbam.client.uiobjects.events.AppEventBus;
 import com.scholastic.sbam.client.uiobjects.events.AppEvents;
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
 import com.scholastic.sbam.shared.objects.AuthMethodInstance;
 import com.scholastic.sbam.shared.objects.CommissionTypeInstance;
 import com.scholastic.sbam.shared.objects.InstitutionInstance;
 import com.scholastic.sbam.shared.objects.PreferenceCategoryInstance;
 import com.scholastic.sbam.shared.objects.PreferenceCodeInstance;
 import com.scholastic.sbam.shared.objects.SimpleKeyProvider;
 import com.scholastic.sbam.shared.objects.SiteInstance;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.objects.UserCacheTarget;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class SiteLocationPortlet extends GridSupportPortlet<SiteInstance> implements AppSleeper {
 	
 	protected final int DIRTY_FORM_LISTEN_TIME	=	250;
 	protected final int PRESUMED_FORM_HEIGHT	=	270;
 	
 	protected final SiteLocationGetServiceAsync			siteGetService					= GWT.create(SiteLocationGetService.class);
 	protected final UpdateSiteLocationServiceAsync		updateSiteLocationService		= GWT.create(UpdateSiteLocationService.class);
 	protected final UpdateSiteLocationNoteServiceAsync	updateSiteNoteService			= GWT.create(UpdateSiteLocationNoteService.class);
 	protected final InstitutionGetServiceAsync			institutionGetService			= GWT.create(InstitutionGetService.class);
 	protected final PreferenceCategoryListServiceAsync	preferenceCategoryListService	= GWT.create(PreferenceCategoryListService.class);
 	
 	protected int					siteUcn;
 	protected int					siteUcnSuffix;
 	protected String				siteLocCode;
 	protected SiteInstance			site;
 	protected InstitutionInstance	siteInstitution;
 	protected InstitutionInstance	createForInstitution;
 	protected String				identificationTip	=	"";
 	
 	protected AuthMethodInstance	jumpToMethod;
 	
 	protected ContentPanel				outerContainer;
 	protected CardLayout				cards;
 	protected FormPanel					siteCard;
 	protected SiteLocationMethodsCard	methodsCard;
 	
 	protected Timer					dirtyFormListener;
 	
 	protected ToolBar				editSaveToolBar;
 	protected Button				editButton;
 	protected Button				cancelButton;
 	protected Button				saveButton;
 
 	protected ToggleButton			siteButton;
 	protected ToggleButton			methodsButton;
 	protected ToggleButton			contactsButton;
 	
 	protected ToolTipConfig					notesToolTip		= new ToolTipConfig();
 	
 	protected MultiField<String>			codeNotesCombo		= new EnhancedMultiField<String>("Code");
 //	protected TextField<String>				siteLocationIdField	= getTextField("UCN + Code");
 	protected TextField<String>				descriptionField	= getTextField("Description");
 	protected TextField<String>				siteLocCodeField	= getTextField("Code");
 	protected NotesIconButtonField<String>	notesField			= getNotesButtonField();
 	protected LabelField					idTipField			= new LabelField();
 	protected InstitutionSearchField		institutionField	= getInstitutionField("ucn", "Institition", 0, "The institution for the site location.");
 	protected LabelField					addressDisplay		= new LabelField();
 	protected NumberField					ucnDisplay			= getIntegerField("UCN");
 	protected NumberField					ucnSuffixDisplay	= getIntegerField("Suffix");
 	protected LabelField					customerTypeDisplay	= new LabelField();
 	protected LabelField					statusDisplay		= new LabelField();
 	protected EnhancedComboBox<BeanModel>	commissionTypeField	= getComboField("commissionType", 	"Commission",	150,		
 								"The commission code assigned to this agreement for reporting purposes.",	
 								UiConstants.getCommissionTypes(UiConstants.CommissionTypeTargets.AGREEMENT), "commissionCode", "descriptionAndCode");
 	protected CheckBox						statusField			= FieldFactory.getCheckBoxField("Site is Active");
 	
 	
 	protected LockableFieldSet				preferencesFieldSet	= new LockableFieldSet();
 	
 	public SiteLocationPortlet() {
		super(AppPortletIds.AGREEMENT_DISPLAY.getHelpTextId());
 	}
 	
 //	public void setSiteLocation(SiteInstance site) {
 //		this.site = site;
 //		if (site != null)
 //			setSiteLocation(site.getUcn(), site.getUcnSuffix(), site.getSiteLocCode());
 //		if (site == null) {
 //			enableSiteButtons(false);
 //		} else {
 //			enableSiteButtons(true);
 //		}
 //	}
 
 	public void setSiteLocation(int siteUcn, int siteUcnSuffix, String siteLocCode) {
 		this.siteUcn		= siteUcn;
 		this.siteUcnSuffix	= siteUcnSuffix;
 		this.siteLocCode	= siteLocCode;
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
 		if (siteLocCode == null) {
 			heading = "Create New Site Location";
 		} else {
 			heading = "Site Location " + siteUcn + "-" + siteUcnSuffix + " : " + siteLocCode;
 		}
 		if (siteInstitution != null) {
 			heading += " &nbsp;&nbsp;&nbsp; &mdash; <i>" + siteInstitution.getInstitutionName() + "</i>";
 		}
 		setHeading(heading);
 	}
 	
 	@Override
 	public String getPresenterToolTip() {
 		String tooltip = "";
 		if (siteLocCode == null) {
 			tooltip = "Create new site location";
 		} else {
 			tooltip = "Site Location " + siteUcn + "-" + siteUcnSuffix + " : " + siteLocCode;
 		}
 		if (siteInstitution != null) {
 			tooltip += " &ndash; <i>" + siteInstitution.getInstitutionName() + "</i>";
 		}
 		if (identificationTip != null && identificationTip.length() > 0) {
 			tooltip += "<br/><i>" + identificationTip + "</i>";
 		}
 		return tooltip;
 	}
 	
 	@Override  
 	protected void onRender(Element parent, int index) {
 		super.onRender(parent, index);
 		
 		if (siteLocCode == null) {
 			setToolTip(UiConstants.getQuickTip("Use this panel to create a new site location."));
 		}
 
 		setPortletHeading();
 		
 		setThis();
 		
 		outerContainer = new ContentPanel();
 		outerContainer.setBorders(false);
 		outerContainer.setHeaderVisible(false);
 		addPanelSwitchTools();
 		
 		cards = new CardLayout();
 		outerContainer.setLayout(cards);
 
 		siteCard = getNewOuterFormPanel();
 		createDisplayCard();
 		outerContainer.add(siteCard);
 		
 		methodsCard = new SiteLocationMethodsCard();
 		outerContainer.add(methodsCard);
 		
 		add(outerContainer);
 		
 		if (siteUcn > 0 && siteLocCode != null)
 			loadSiteLocation(siteUcn, siteUcnSuffix, siteLocCode);
 	}
 	
 	@Override
 	protected void afterRender() {
 		super.afterRender();
 		resizePreferencesFieldSet();
 		layout(true);
 
 		//	Handle "new site" automatically
 		if (siteLocCode == null || siteLocCode.length() == 0) {
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 			if (createForInstitution != null)
 				set(createForInstitution);
 			loadPreferenceCategories();
 			beginEdit();
 		}
 	}
 	
 	@Override
 	protected void onResize(int width, int height) {
 		super.onResize(width, height);
 		resizePreferencesFieldSet();
 	}
 	
 	protected void resizePreferencesFieldSet() {
 		if (preferencesFieldSet != null && preferencesFieldSet.isRendered() && isRendered() && getOffsetHeight() > 0) {
 			int desiredHeight = (getHeight(true) - preferencesFieldSet.getAbsoluteTop()) - 10;
 			if (editSaveToolBar != null && editSaveToolBar.isRendered()) {
 				desiredHeight = editSaveToolBar.getAbsoluteTop() - preferencesFieldSet.getAbsoluteTop() - 10;	// 10 = padding
 			}
 
 			preferencesFieldSet.setHeight(desiredHeight);
 			preferencesFieldSet.layout(true);
 		}
 	}
 	
 	private void createDisplayCard() {
 		FormData formData90 = new FormData("-24"); 	//	new FormData("90%");
 
 		//	Required fields
 		siteLocCodeField.setAllowBlank(false);
 		institutionField.setAllowBlank(false);
 		descriptionField.setAllowBlank(false);
 		commissionTypeField.setAllowBlank(true);
 		
 		ucnDisplay.setReadOnly(true);
 		
 		codeNotesCombo.setSpacing(20);
 		
 //		siteCard.add(siteLocationIdField, formData90);
 		
 		codeNotesCombo.add(siteLocCodeField);
 		codeNotesCombo.add(notesField);
 		siteCard.add(codeNotesCombo,    formData90);
 		siteCard.add(descriptionField, formData90);
 
 		siteCard.add(idTipField, formData90);
 		siteCard.add(institutionField, formData90);
 		
 		siteCard.add(addressDisplay, formData90); 
 		siteCard.add(ucnDisplay, formData90);
 		siteCard.add(customerTypeDisplay, formData90); 
 			
 		siteCard.add(commissionTypeField, formData90); 
 			
 		statusDisplay.setFieldLabel("Status:");
 		siteCard.add(statusField, formData90);
 		
 		addPreferencesFieldSet(siteCard, formData90);
 		
 		addEditSaveButtons(siteCard);
 	}
 	
 	protected void addPreferencesFieldSet(FormPanel formPanel, FormData formData) {
 		preferencesFieldSet.setCollapsible(true);
 		preferencesFieldSet.setHeading("Site Preferences");
 		preferencesFieldSet.setScrollMode(Scroll.AUTO);
 		preferencesFieldSet.setHeight(200);
 		
 		FormLayout fLayout = new FormLayout();
 		if (formPanel.getWidth() == 0)
 			fLayout.setLabelWidth(200);
 		else
 			fLayout.setLabelWidth(formPanel.getWidth() / 3);
 		preferencesFieldSet.setLayout(fLayout);
 		preferencesFieldSet.setToolTip(UiConstants.getQuickTip("Use these fields to set the product delivery preferences associated with this site."));
 
 		//	Fields will be added dynamically, from the database, when a site is loaded
 		
 		formPanel.add(preferencesFieldSet);
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
 					matchToInstitution( siteInstitution );
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
 
 		if (institutionField.isDirty() && institutionField.getOriginalValue() != null) {
 			
 			// The user changed the institution, so we better warn them about what this means
 
 			final Listener<MessageBoxEvent> confirmListener = new Listener<MessageBoxEvent>() {  
 				public void handleEvent(MessageBoxEvent ce) {  
 					Button btn = ce.getButtonClicked();
 					if ("Yes".equals(btn.getText()))
 						endEdit(true);
 				}
 			};
 			
 			MessageBox.confirm("Institution Change", 
 								"A change to the billing institution will affect the gathering of usage statistics.  Are you sure you want to do this?", 
 								confirmListener);
 		} else
 			endEdit(true);	// No change to institution so just go ahead
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
 		return site == null || siteCard.isDirty();	//	formColumn1.isDirty() || formColumn2.isDirty();
 	}
 	
 	protected void handleDirtyForm() {
 		boolean ready = siteCard.isValid();
 		
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
 	
 	/**
 	 * Add the toolbar buttons
 	 */
 	protected void addPanelSwitchTools() {
 		
 		final int MIN_BUTTON_WIDTH = 80;
 		String toggleGroup = "site" + System.currentTimeMillis();
 		
 		ToolBar toolBar = new ToolBar();
 		toolBar.setAlignment(HorizontalAlignment.CENTER);
 		toolBar.setBorders(false);
 		toolBar.setSpacing(20);
 		toolBar.setToolTip(UiConstants.getQuickTip("Use these buttons to access detailed information for this site."));
 		
 		siteButton = new ToggleButton("Site and Preferences");
 		siteButton.setMinWidth(MIN_BUTTON_WIDTH);
 		siteButton.setToolTip(UiConstants.getQuickTip("Define and edit the site and its preferences."));
 		IconSupplier.forceIcon(siteButton, IconSupplier.getAgreementIconName());
 		siteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					cards.setActiveItem(siteCard);
 					siteButton.toggle(true);
 				}  
 			});
 		siteButton.setToggleGroup(toggleGroup);
 		toolBar.add(siteButton);
 		
 		methodsButton = new ToggleButton("Methods");
 		methodsButton.setMinWidth(MIN_BUTTON_WIDTH);
 		methodsButton.setToolTip(UiConstants.getQuickTip("Define and edit access methods for this agreement."));
 		IconSupplier.forceIcon(methodsButton, IconSupplier.getAccessMethodIconName());
 		methodsButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 				//	methodsCard.setAgreementId(agreementId);
 					methodsCard.setSiteLocation(site);
 					cards.setActiveItem(methodsCard);
 					methodsButton.toggle(true);
 				}
 			});
 		methodsButton.setToggleGroup(toggleGroup);
 		toolBar.add(methodsButton);
 		
 //		remoteButton = new ToggleButton("Remote Setup");
 //		remoteButton.setMinWidth(MIN_BUTTON_WIDTH);
 //		remoteButton.setToolTip(UiConstants.getQuickTip("Define and edit any remote setup for this agreement."));
 //		IconSupplier.forceIcon(remoteButton, IconSupplier.getRemoteIconName());
 //		remoteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 //				@Override
 //				public void componentSelected(ButtonEvent ce) {
 //					remoteButton.toggle(true);
 //				}
 //			});
 //		remoteButton.setToggleGroup(toggleGroup);
 //		toolBar.add(remoteButton);
 		
 		contactsButton = new ToggleButton("Contacts");
 		contactsButton.setMinWidth(MIN_BUTTON_WIDTH);
 		contactsButton.setToolTip(UiConstants.getQuickTip("View, define and edit the contacts for this agreement."));
 		IconSupplier.forceIcon(contactsButton, IconSupplier.getContactsIconName());
 		contactsButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 //					contactsCard.setAgreementId(agreementId);
 //					contactsCard.setAgreement(agreement);
 //					contactsCard.setBillToInstitution(billToInstitution);
 //					cards.setActiveItem(contactsCard);
 					contactsButton.toggle(true);
 				}
 			});
 		contactsButton.setToggleGroup(toggleGroup);
 		toolBar.add(contactsButton);
 
 		siteButton.toggle(true);
 		enableSiteButtons(site != null);
 		
 		outerContainer.setBottomComponent(toolBar);
 	}
 	
 	protected void enableSiteButtons(boolean enabled) {
 		if (editButton != null) editButton.setEnabled(enabled);
 		if (methodsButton != null) methodsButton.setEnabled(enabled);
 		if (contactsButton != null) contactsButton.setEnabled(enabled);
 	}
 	
 	protected NotesIconButtonField<String> getNotesButtonField() {
 		NotesIconButtonField<String> nibf = new NotesIconButtonField<String>(this) {
 			@Override
 			public void updateNote(String note) {
 				asyncUpdateNote(note);
 			}
 		};
 		nibf.setEmptyNoteText("Click the note icon to add notes for this agreement.");
 		return nibf;
 	}
 	
 	protected void enableEditButton(boolean enabled) {
 		if (editButton != null) editButton.setEnabled(enabled);
 	}
 	
 	/**
 	 * Set an agreement on the form, and load its institution
 	 * @param agreement
 	 */
 	protected void set(SiteInstance site) {
 		
 		this.site = site;
 		if (site == null) {
 //			this.siteUcn = -1;
 			enableEditButton(false);
 			enableSiteButtons(false);
 		} else {
 			this.siteUcn = site.getUcn();
 			this.siteUcnSuffix = site.getUcnSuffix();
 			this.siteLocCode = site.getSiteLocCode();
 			enableEditButton(true);
 			enableSiteButtons(true);
 		}
 		
 		//	Clear all fields from the preferencesFieldSet
 		preferencesFieldSet.removeAll();
 		
 		//	For existing records, set fields that cannot be changed to read only
 		boolean isNew = site == null || site.isNewRecord() || site.isAddNew() || site.getUcn() == 0 || site.getSiteLocCode() == null;
 		siteLocCodeField.setReadOnly(!isNew);
 		institutionField.setReadOnly(!isNew);
 		
 		if (site != null)
 			registerUserCache(site, identificationTip);
 		setPortletHeading();
 
 		if (site == null) {
 			MessageBox.alert("Site not found.", "The requested site was not found.", null);
 			clearFormValues();
 			notesField.setAddMode();
 			notesField.setNote("");
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 			if (siteUcn > 0 && siteLocCode != null) {
 				siteLocCodeField.setValue(siteLocCode);
 				if (siteLocCode != null && siteLocCode.equals("main"))
 					descriptionField.setValue("Main location");
 				loadInstitution(siteUcn);
 				loadPreferenceCategories();
 				beginEdit();
 			}
 		} else {
 			
 //			siteLocationIdField.setValue(siteUcn + "-" + siteUcnSuffix + " : " + siteLocCode);
 			siteLocCodeField.setValue(site.getSiteLocCode());
 			descriptionField.setValue(site.getDescription());
 			idTipField.setValue(identificationTip);	
 
 			if (site.getNote() != null && site.getNote().length() > 0) {
 				notesField.setEditMode();
 				notesField.setNote(site.getNote());
 			} else {
 				notesField.setAddMode();
 				notesField.setNote("");			
 			}
 			
 			addressDisplay.setValue("<i>Loading...</i>");
 			customerTypeDisplay.setValue("");
 			
 			statusDisplay.setValue(AppConstants.getStatusDescription(site.getStatus()));
 			statusField.setValue(AppConstants.STATUS_ACTIVE == site.getStatus());
 			if (site.getCommissionType() == null || site.getCommissionType().getCommissionCode().length() == 0)
 				commissionTypeField.setValue(CommissionTypeInstance.obtainModel(CommissionTypeInstance.getEmptyInstance()));
 			else
 				commissionTypeField.setValue(CommissionTypeInstance.obtainModel(site.getCommissionType()));
 			
 			setPreferenceFields(site);
 			
 			if (site.getInstitution() != null) {
 				set(site.getInstitution());
 			} else {
 				if (site.getUcn() <= 0) {
 					set(InstitutionInstance.getEmptyInstance());
 				} else {
 					loadInstitution(site.getUcn());
 				}
 			}
 		}
 		
 		//	Resize things to account for the data
 		layout(true);
 		resizePreferencesFieldSet();
 
 		updatePresenterLabel();
 		updateUserPortlet();	// This is mostly for a "create" so the portlet knows the agreement ID has been set
 		setOriginalValues();
 //		endEdit(false);
 		
 		jumpTo();
 	}
 	
 	protected void setPreferenceFields(SiteInstance site) {
 		if (site != null)
 			setPreferenceFields(site.getAllPreferenceCategories(), site.getSelectedPreferences());
 	}
 		
 	protected void setPreferenceFields(List<PreferenceCategoryInstance> preferenceCategories, HashMap<String, String> selections) {
 		//	Error trap, in case there are no preference categories
 		if (preferenceCategories == null || preferenceCategories.size() == 0) {
 			preferencesFieldSet.add(getLabelField("There are no preferences available."));
 			preferencesFieldSet.layout(true);
 			return;
 		}
 		
 		FormData formData24 = new FormData("-24");
 		
 		//	Handle each category code
 		for (PreferenceCategoryInstance prefCat : preferenceCategories) {
 			
 			if (prefCat.getPreferenceCodes() == null || prefCat.getPreferenceCodes().size() == 0) {
 				//	There are not selection values, so create a free format text field
 				TextField<String> textField = getTextField(prefCat.getDescription());
 				textField.setData("prefCatCode", prefCat.getPrefCatCode());
 				if (selections != null && selections.containsKey(prefCat.getPrefCatCode()))
 					textField.setValue(selections.get(prefCat.getPrefCatCode()));
 				preferencesFieldSet.add(textField, formData24);
 				
 			} else {
 				
 				//	There is one or more selection choice, so create a combo box
 				PreferenceCodeInstance defaultPc = null;
 				PreferenceCodeInstance selected  = null;
 				ListStore<BeanModel> prefCodeStore = new ListStore<BeanModel>();
 				prefCodeStore.setKeyProvider(new SimpleKeyProvider("uniqueKey"));
 				//	Create a combo field for the category
 				EnhancedComboBox<BeanModel> comboBox = this.getComboField(prefCat.getPrefCatCode(), prefCat.getDescription(), 0, prefCodeStore, "descriptionAndCode");
 				comboBox.setSimpleTemplate("<div class=\"{listStyle}\">{descriptionAndCode}</div>");
 				//	Go through and add the preference codes to the store for the combo field
 				for (PreferenceCodeInstance prefCode : prefCat.getPreferenceCodes()) {
 					//	Make the first pref code encountered the default (no code) value
 					if (defaultPc == null) {
 						defaultPc = new PreferenceCodeInstance();
 						defaultPc.setPrefCatCode(prefCat.getPrefCatCode());
 						defaultPc.setPrefSelCode("");
 						defaultPc.setDescription(prefCode.getDescription());
 						prefCodeStore.add(PreferenceCodeInstance.obtainModel(defaultPc));
 					}
 					//	Add the preference code to the store
 					prefCodeStore.add(PreferenceCodeInstance.obtainModel(prefCode));
 					//	If this code is selected for this category for this site location, save it
 					if (selected == null 
 					&&  selections != null
 					&&	selections.containsKey(prefCat.getPrefCatCode())
 					&&  selections.get(prefCat.getPrefCatCode()).equals(prefCode.getPrefSelCode()))
 						selected = prefCode;
 				}
 				
 				//	If nothing is selected and there is a default, select that
 				if (selected == null)
 					selected = defaultPc;
 
 				//	If there is a selection, apply it to the combo box
 				if (selected != null) {
 					List<BeanModel> selection = new ArrayList<BeanModel>();
 					selection.add(PreferenceCodeInstance.obtainModel(selected));
 					comboBox.setSelection(selection);
 				}
 				
 				//	Add the combo box to the preferences field set
 				preferencesFieldSet.add(comboBox, formData24);
 			}
 			
 			//	Force it to redraw
 //			resizePreferencesFieldSet();
 			preferencesFieldSet.layout(true);
 		}
 	}
 	
 	/**
 	 * Set an institution on the form
 	 * @param instance
 	 */
 	protected void set(InstitutionInstance instance) {
 		if (siteInstitution == instance)
 			return;
 		
 		siteInstitution = instance;
 		
 		if (siteInstitution == null) {
 			MessageBox.alert("Institution Not Found", "The Institution for the site was not found.", null);
 			siteInstitution = InstitutionInstance.getEmptyInstance(); 
 		}
 
 		if (institutionField.getSelectedValue() == null || !siteInstitution.equals(institutionField.getSelectedValue().getBean())) {
 			institutionField.setValue(InstitutionInstance.obtainModel(siteInstitution));
 		}
 
 		if (siteInstitution != null)
 			registerUserCache(siteInstitution, identificationTip);
 		
 		setPortletHeading();
 		matchToInstitution(siteInstitution);
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
 		siteCard.clear();
 	}
 	
 	public void resetFormValues() {
 		siteCard.reset();
 	}
 	
 	public void setOriginalValues() {
 		if (siteCard != null)
 			setOriginalValues(siteCard);
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
 		for (Field<?> field : siteCard.getFields()) {
 			if (field.getParent() != null && field.getParent() instanceof LockableFieldSet) {
 				LockableFieldSet lfs = (LockableFieldSet) field.getParent();
 				lfs.enableFields(true);
 			} else 
 				field.enable();
 		}
 	}
 	
 	public void disableFields() {
 		for (Field<?> field : siteCard.getFields()) {
 //			if (field == codeNotesCombo)
 //				siteLocCodeField.disable();
 //			else
 				field.disable();
 		}
 	}
 
 	/**
 	 * Load the agreement for an ID
 	 * @param id
 	 */
 	protected void loadSiteLocation(final int siteUcn, final int siteUcnSuffix, final String siteLocCode) {
 		siteGetService.getSiteLocation(siteUcn, siteUcnSuffix, siteLocCode, 
 				true, true,	// Include site preferences, and the full list of preference categories and codes to work with
 				new AsyncCallback<SiteInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Site access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(SiteInstance site) {
 						set(site);
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
 
 	/**
 	 * Load the institution for the agreement
 	 * @param ucnField
 	 */
 	protected void loadPreferenceCategories() {
 		preferenceCategoryListService.getPreferenceCategories(null, true,
 				new AsyncCallback<List<PreferenceCategoryInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Preference category access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(List<PreferenceCategoryInstance> list) {
 						setPreferenceFields(list, null);
 						//	After they load, open the fields if the user is in edit mode (they should be)
 						if (!editButton.isEnabled() && cancelButton.isEnabled()) {
 							preferencesFieldSet.enableFields(true);
 						}
 					}
 			});
 	}
 	
 	protected int getFieldIntValue(NumberField field) {
 		if (field.getValue() == null)
 			return 0;
 		else
 			return field.getValue().intValue();
 	}
 	
 	@Override
 	public void fireUserCacheUpdateEvents(UserCacheTarget target) {
 		//	Fire an event so any listening portlets can update themselves
 		AppEvent appEvent = new AppEvent(AppEvents.SiteAccess);
 		if (target instanceof SiteInstance)
 			appEvent.set( (SiteInstance) target);
 		AppEventBus.getSingleton().fireEvent(AppEvents.SiteAccess, appEvent);
 	}
 
 	protected void asyncUpdate() {
 	
 		// Set field values from form fields
 		
 		if (site == null) {
 			site = new SiteInstance();
 			site.setNewRecord(true);
 			site.setUcnSuffix(1);
 			site.setSiteLocCode(siteLocCodeField.getValue());
 			site.setStatus(AppConstants.STATUS_ACTIVE);	//	This is later overridden by statusField, but we set it here just in case that's removed at some point
 		}
 		
 		site.setInstitution( ((InstitutionInstance) institutionField.getSelectedValue().getBean()) );
 		site.setUcn(site.getInstitution().getUcn());
 		site.setDescription(descriptionField.getValue());
 		if (commissionTypeField.getSelectedValue() != null)
 			site.setCommissionType( (CommissionTypeInstance) commissionTypeField.getSelectedValue().getBean() );
 		else
 			site.setCommissionType( CommissionTypeInstance.getEmptyInstance() );
 		site.setStatus(statusField.getValue() ? AppConstants.STATUS_ACTIVE : AppConstants.STATUS_INACTIVE);
 		
 		//	Set preferences
 		site.setSelectedPreferences(new HashMap<String, String>());
 		for (Component component : preferencesFieldSet.getItems()) {
 			if (component instanceof ComboBox) {
 				//	Get the selected value from a combo box 
 				@SuppressWarnings("unchecked")
 				ComboBox<BeanModel> comboBox = (ComboBox<BeanModel>) component;
 				PreferenceCodeInstance selected = comboBox.getValue().getBean();
 				site.getSelectedPreferences().put(selected.getPrefCatCode(), selected.getPrefSelCode());
 			} else if (component instanceof TextField) {
 				//	Get the value from a text field... the category code has been saved in the field data
 				@SuppressWarnings("unchecked")
 				TextField<String> textField = (TextField<String>) component;
 				site.getSelectedPreferences().put((String) textField.getData("prefCatCode"), textField.getValue());
 			}
 		}
 		
 		if (site.isNewRecord())
 			site.setNote(notesField.getNote());
 		else
 			site.setNote(null);	//	This will keep the note from being updated by this call
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateSiteLocationService.updateSiteLocation(site,
 				new AsyncCallback<UpdateResponse<SiteInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Agreement update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						editButton.enable();
 					}
 
 					public void onSuccess(UpdateResponse<SiteInstance> updateResponse) {
 						SiteInstance updatedSite = (SiteInstance) updateResponse.getInstance();
 						if (updatedSite.isNewRecord()) {
 							updatedSite.setNewRecord(false);
 							if (updatedSite.getInstitution() == null)
 								identificationTip = "Site created " + new Date();
 							else
 								identificationTip = "Site created for " + updatedSite.getInstitution().getInstitutionName();
 						}
 						site.setNewRecord(false);
 						set(updatedSite);
 				//		enableAgreementButtons(true);
 				}
 			});
 	}
 
 	protected void asyncUpdateNote(String note) {
 	
 		// Set field values from form fields
 		
 		if (site == null || site.isNewRecord()) {
 			return;
 		}
 		
 		site.setNote(note);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateSiteNoteService.updateSiteLocationNote(site,
 				new AsyncCallback<UpdateResponse<SiteInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Site Location note update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						notesField.unlockNote();
 					}
 
 					public void onSuccess(UpdateResponse<SiteInstance> updateResponse) {
 						SiteInstance updatedAgreement = (SiteInstance) updateResponse.getInstance();
 						if (!notesField.getNote().equals(updatedAgreement.getNote())) {
 							notesField.setNote(updatedAgreement.getNote());
 							site.setNote(updatedAgreement.getNote());
 						}
 						notesField.unlockNote();
 				}
 			});
 	}
 	
 	public int getSiteUcn() {
 		return siteUcn;
 	}
 
 	public void setSiteUcn(int siteUcn) {
 		this.siteUcn = siteUcn;
 	}
 
 	public int getSiteUcnSuffix() {
 		return siteUcnSuffix;
 	}
 
 	public void setSiteUcnSuffix(int siteUcnSuffix) {
 		this.siteUcnSuffix = siteUcnSuffix;
 	}
 
 	public String getSiteLocCode() {
 		return siteLocCode;
 	}
 
 	public void setSiteLocCode(String siteLocCode) {
 		this.siteLocCode = siteLocCode;
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
 	public String getShortPortletName() {
 		if (siteLocCode != null)
 			return siteUcn + "-" + siteUcnSuffix + " : " + siteLocCode;
 		return "Create Site Location";
 	}
 	
 	@Override
 	public boolean allowDuplicatePortlets() {
 		//	Not allowed for a particular site location
 		if (siteLocCode != null)
 			return false;
 		//	Allowed for "create new"
 		return true;
 	}
 	
 	@Override
 	public String getPortletIdentity() {
 		return getPortletIdentity(siteUcn, siteUcnSuffix, siteLocCode);
 	}
 	
 	public static String getPortletIdentity(int ucn, int ucnSuffix, String siteLocCode) {
 		if (ucn <= 0 || siteLocCode == null)
 			return SiteLocationPortlet.class.getName();
 		return SiteLocationPortlet.class.getName() + ":" + ucn + ":" + ucnSuffix + ":" + siteLocCode;
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
 		if (parts.length > 0) siteUcn = Integer.parseInt(parts [0]);
 		if (parts.length > 1) siteUcnSuffix = Integer.parseInt(parts [1]);
 		if (parts.length > 2) siteLocCode = parts [2];
 		if (parts.length > 3) identificationTip = parts [3];
 	}
 
 	/**
 	 * For the user cache, return the "set" data as a string for offline storage
 	 */
 	@Override
 	public String getKeyData() {
 		//	Note that if siteLocCode is null, we don't want to write "null" to the key data, so instead we write an empty string
 		if (identificationTip == null)
 			return siteUcn + ":" + siteUcnSuffix + ":" + siteLocCode == null ? "" : siteLocCode;
 		else
 			return siteUcn + ":" + siteUcnSuffix + ":" + (siteLocCode == null ? "" : siteLocCode) + ":" + identificationTip;
 	}
 
 	public AuthMethodInstance getJumpToMethod() {
 		return jumpToMethod;
 	}
 
 	public void setJumpToMethod(AuthMethodInstance jumpToMethod) {
 		this.jumpToMethod = jumpToMethod;
 		jumpTo();
 	}
 
 	public void jumpTo() {
 		if (jumpToMethod != null && site != null && site.getUcn() == jumpToMethod.getUcn()) {
 			methodsCard.setSiteLocation(site);
 			methodsCard.setFocusInstance(jumpToMethod);
 			cards.setActiveItem(methodsCard);
 			methodsButton.toggle(true);
 			
 			jumpToMethod = null;	/// Once we've done this, don't do it any more
 		}
 	}
 
 }
