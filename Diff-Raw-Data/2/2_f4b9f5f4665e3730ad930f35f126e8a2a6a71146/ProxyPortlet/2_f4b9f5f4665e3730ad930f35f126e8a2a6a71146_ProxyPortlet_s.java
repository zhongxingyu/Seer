 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.MultiField;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.RowExpander;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.ProxyGetService;
 import com.scholastic.sbam.client.services.ProxyGetServiceAsync;
 import com.scholastic.sbam.client.services.UpdateProxyIpAddressNoteService;
 import com.scholastic.sbam.client.services.UpdateProxyIpAddressNoteServiceAsync;
 import com.scholastic.sbam.client.services.UpdateProxyIpAddressService;
 import com.scholastic.sbam.client.services.UpdateProxyIpAddressServiceAsync;
 import com.scholastic.sbam.client.services.UpdateProxyNoteService;
 import com.scholastic.sbam.client.services.UpdateProxyNoteServiceAsync;
 import com.scholastic.sbam.client.services.UpdateProxyService;
 import com.scholastic.sbam.client.services.UpdateProxyServiceAsync;
 import com.scholastic.sbam.client.stores.KeyModelComparer;
 import com.scholastic.sbam.client.uiobjects.events.AppEvent;
 import com.scholastic.sbam.client.uiobjects.events.AppEventBus;
 import com.scholastic.sbam.client.uiobjects.events.AppEvents;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedComboBox;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedMultiField;
 import com.scholastic.sbam.client.uiobjects.fields.InstitutionSearchField;
 import com.scholastic.sbam.client.uiobjects.fields.IpAddressRangeField;
 import com.scholastic.sbam.client.uiobjects.fields.LockableFieldSet;
 import com.scholastic.sbam.client.uiobjects.fields.NotesIconButtonField;
 import com.scholastic.sbam.client.uiobjects.foundation.AppSleeper;
 import com.scholastic.sbam.client.uiobjects.foundation.FieldFactory;
 import com.scholastic.sbam.client.uiobjects.foundation.GridSupportPortlet;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.shared.objects.ProxyIpInstance;
 import com.scholastic.sbam.shared.objects.ProxyTuple;
 import com.scholastic.sbam.shared.objects.ProxyInstance;
 import com.scholastic.sbam.shared.objects.SimpleKeyProvider;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.objects.UserCacheTarget;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class ProxyPortlet extends GridSupportPortlet<ProxyInstance> implements AppSleeper, AppPortletRequester {
 	
 	protected final int DIRTY_FORM_LISTEN_TIME	=	250;
 	
 	protected final ProxyGetServiceAsync					proxyGetService				= GWT.create(ProxyGetService.class);
 	protected final UpdateProxyServiceAsync					updateProxyService			= GWT.create(UpdateProxyService.class);
 	protected final UpdateProxyNoteServiceAsync				updateProxyNoteService		= GWT.create(UpdateProxyNoteService.class);
 	protected final UpdateProxyIpAddressServiceAsync		updateProxyIpService		= GWT.create(UpdateProxyIpAddressService.class);
 	protected final UpdateProxyIpAddressNoteServiceAsync	updateProxyIpNoteService	= GWT.create(UpdateProxyIpAddressNoteService.class);
 	
 	protected int							proxyId;
 	protected ProxyInstance					proxy;
 	protected String						identificationTip	=	"";
 	
 	protected FormPanel						outerContainer;
 
 	protected ListStore<BeanModel>			ipAddressStore;
 	protected Grid<BeanModel>				ipAddressGrid;
 	
 	protected Timer							dirtyFormListener;
 	
 	protected ToolBar						editSaveToolBar;
 	protected Button						editButton;
 	protected Button						cancelButton;
 	protected Button						saveButton;
 	protected Button						newIpButton;
 	
 	protected boolean						editIpAddress		= false;
 	protected int							proxyIpId			= 0;
 	
 	protected AppPortletProvider			portletProvider;
 	
 	protected ToolTipConfig					notesToolTip		= new ToolTipConfig();
 
 	protected FieldSet						ipAddressGridFieldSet;
 	
 	protected MultiField<String>			proxyIdNotesCombo	= new EnhancedMultiField<String>("Proxy Id:");
 	protected TextField<String>				proxyIdField		= getTextField("");
 	protected TextField<String>				descriptionField	= getTextField("Description");
 	protected TextField<String>				searchKeysField		= getTextField("Search Keys");
 	protected NotesIconButtonField<String>	notesField			= getNotesButtonField();
 //	protected LabelField					statusDisplay		= new LabelField();
 	protected CheckBox						statusField			= FieldFactory.getCheckBoxField("Proxy is Active");
 	
 	protected LockableFieldSet				ipFieldSet			= new LockableFieldSet();
 	
 	protected IpAddressRangeField			ipAddressField		= new IpAddressRangeField("Ip Address");
 //	protected LabelField					ipStatusDisplay		= new LabelField();
 	protected CheckBoxGroup					ipCheckGroup		= new CheckBoxGroup();
 	protected CheckBox						ipApprovedField		= FieldFactory.getCheckBoxField("IP Address is Approved");
 	protected CheckBox						ipStatusField		= FieldFactory.getCheckBoxField("IP Address is Active");
 	protected NotesIconButtonField<String>	ipNotesField		= getIpNotesButtonField();
 	
 	public ProxyPortlet() {
 		super(AppPortletIds.AGREEMENT_DISPLAY.getHelpTextId());
 //		forceHeight = DEFAULT_HEIGHT;
 	}
 
 	public void setProxy(int proxyId) {
 		this.proxyId		= proxyId;
 	}
 	
 	public String getIdentificationTip() {
 		return identificationTip;
 	}
 
 	public void setIdentificationTip(String identificationTip) {
 		if (identificationTip == null)
 			identificationTip = "";
 		this.identificationTip = identificationTip;
 	}
 
 	protected void setPortletHeading() {
 		String heading = "";
 		if (proxyId <= 0) {
 			heading = "Create New Proxy";
 		} else {
 			heading = "Proxy " + AppConstants.appendCheckDigit(proxyId);
 		}
 		setHeading(heading);
 	}
 	
 	@Override
 	public String getPresenterToolTip() {
 		String tooltip = "";
 		if (proxyId <= 0) {
 			tooltip = "Create new proxy";
 		} else {
 			tooltip = "Proxy " + AppConstants.appendCheckDigit(proxyId);
 		}
 		if (identificationTip != null && identificationTip.length() > 0) {
 			tooltip += "<br/><i>" + identificationTip + "</i>";
 		}
 		return tooltip;
 	}
 	
 	@Override  
 	protected void onRender(Element parent, int index) {
 		super.onRender(parent, index);
 		
 		if (proxyId <= 0) {
 			setToolTip(UiConstants.getQuickTip("Use this panel to create a new proxy."));
 		}
 
 		setPortletHeading();
 		
 		setThis();
 		
 		outerContainer = getNewOuterFormPanel();
 		
 		createDisplay();
 		
 		add(outerContainer);
 		
 		if (proxyId > 0)
 			loadProxy(proxyId);
 	}
 	
 	@Override
 	protected void afterRender() {
 		super.afterRender();
 		layout(true);
 
 		//	Handle "new proxy" automatically
 		if (proxyId <= 0) {
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 			beginEdit();
 		}
 	}
 	
 	private void createDisplay() {
 		FormData formData90 = new FormData("-24"); 	//	new FormData("90%");
 
 		//	Required fields
 		descriptionField.setAllowBlank(false);
 
 		proxyIdField.setReadOnly(true);
 		
 		proxyIdNotesCombo.setSpacing(20);
 		
 		proxyIdNotesCombo.add(proxyIdField);
 		proxyIdNotesCombo.add(notesField);
 		outerContainer.add(proxyIdNotesCombo,    formData90);
 		
 		outerContainer.add(descriptionField, formData90); 
 		outerContainer.add(searchKeysField, formData90);
 			
 //		statusDisplay.setFieldLabel("Status:");
 		outerContainer.add(statusField, formData90);
 		
 		ipFieldSet.setId("IPfs");
 		ipFieldSet.setBorders(true);
 		ipFieldSet.setHeading("IP Address");
 		ipFieldSet.setCollapsible(true);
 		FormLayout fLayout = new FormLayout();
 		fLayout.setLabelWidth(0);
 		ipFieldSet.setLayout(fLayout);
 		ipFieldSet.setToolTip(UiConstants.getQuickTip("Define proxy IP address."));
 		
 		ipFieldSet.add(ipAddressField, formData90);
 		ipAddressField.disable();
 
 		ipCheckGroup.setLabelSeparator("");
 		
 		ipCheckGroup.add(ipApprovedField);
 		ipApprovedField.disable();
 		
 		ipCheckGroup.add(ipStatusField);
 		ipStatusField.disable();
 		
 		ipCheckGroup.add(ipNotesField);
 		
 		ipFieldSet.add(ipCheckGroup, formData90);
 		
 		ipFieldSet.collapse();
 		
 		outerContainer.add(ipFieldSet);
 		
 		addIpAddressesGrid(formData90);
 		
 		addEditSaveButtons(outerContainer);
 	}
 	
 	protected void addIpAddressesGrid(FormData formData90) {
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		
 		columns.add(getDisplayColumn("ipRangeDisplay",						"IP Address",				200,
 										"This is the agreement Id."));
 		columns.add(getDisplayColumn("ipLoDisplay",							"Low IP Address",			100));
 		columns.add(getDisplayColumn("ipHiDisplay",							"High IP Address",			100));
 		columns.add(getDisplayColumn("approvedDescription",					"Approved",					80));
 		columns.add(getDisplayColumn("statusDescription",					"Status",					80));
 
 		RowExpander expander = getNoteExpander();
 		columns.add(expander);
 		
 		ColumnModel cm = new ColumnModel(columns);  
 
 		ipAddressStore = new ListStore<BeanModel>();
 		ipAddressStore.setKeyProvider(new SimpleKeyProvider("uniqueKey"));
 		ipAddressStore.setModelComparer(new KeyModelComparer<BeanModel>(ipAddressStore));
 		
 		ipAddressGrid = new Grid<BeanModel>(ipAddressStore, cm); 
 		ipAddressGrid.addPlugin(expander);
 		ipAddressGrid.setBorders(true);  
 		ipAddressGrid.setAutoExpandColumn("ipRangeDisplay"); 
 		ipAddressGrid.setStripeRows(true);
 		ipAddressGrid.setColumnLines(false);
 		ipAddressGrid.setHideHeaders(false);
 		
 		addRowListener(ipAddressGrid);
 		
 //		//	Open a new portlet to display an agreement when a row is selected
 //		agreementsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 
 //		final AppPortlet thisPortlet = this; 
 //		agreementsGrid.getSelectionModel().addListener(Events.SelectionChange,  
 //				new Listener<SelectionChangedEvent<ModelData>>() {  
 //					public void handleEvent(SelectionChangedEvent<ModelData> be) {  
 //						if (be.getSelection().size() > 0) {
 //						//	System.out.println("Agreement " + ((BeanModel) be.getSelectedItem()).get("idCheckDigit"));
 //							AgreementInstance agreement = (AgreementInstance) ((BeanModel) be.getSelectedItem()).getBean();
 //							AgreementPortlet portlet = (AgreementPortlet) portletProvider.getPortlet(AppPortletIds.AGREEMENT_DISPLAY);
 //							portlet.setAgreementId(agreement.getId());
 //							if (proxy != null) {
 //								String foundFor = "Proxy #" + proxy.getIdCheckDigit();
 //								portlet.setIdentificationTip("Opened for " + foundFor + "");
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
 	
 		ipAddressGridFieldSet = new FieldSet();
 		ipAddressGridFieldSet.setBorders(true);
 		ipAddressGridFieldSet.setHeading("IP Addresses");
 		ipAddressGridFieldSet.setCollapsible(true);
 		ipAddressGridFieldSet.setDeferHeight(true);
 		ipAddressGridFieldSet.setToolTip(UiConstants.getQuickTip("These are the IP addresses associated with this proxy.  Click the grid to inspect or edit an address."));
 		ipAddressGridFieldSet.setLayout(new FitLayout());
 		ipAddressGridFieldSet.setHeight(300);
 		ipAddressGridFieldSet.add(ipAddressGrid, new FormData("95%")); // new FormData(cm.getTotalWidth() + 25, 200));
 		
 		outerContainer.add(ipAddressGridFieldSet, new FormData("100%")); // new FormData(cm.getTotalWidth() + 20, 200));
 	}
 	
 	/**
 	 * What to do when a row is selected.
 	 */
 	@Override
 	protected void onRowSelected(BeanModel data) {
 		ProxyIpInstance ip = (ProxyIpInstance) data.getBean();
 		
 		proxyIpId = ip.getIpId();
 		
 		ipAddressField.setValue(ip.getIpLo(), ip.getIpHi());
 		ipApprovedField.setValue(ip.isApproved());
 		ipStatusField.setValue(ip.isActive());
 		if (ip.getNote() != null && ip.getNote().length() > 0) {
 			ipNotesField.setEditMode();
 			ipNotesField.setNote(ip.getNote());
 		} else {
 			ipNotesField.setAddMode();
 			ipNotesField.setNote("");			
 		}
 		
 		beginIpEdit();
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
 		
 		newIpButton = new Button("New IP Address");
 		IconSupplier.forceIcon(newIpButton, IconSupplier.getNewIconName());
 		newIpButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				proxyIpId = -1;
 				beginIpEdit();
 			}  
 		});
 		editSaveToolBar.add(newIpButton);
 		
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
 		return proxy == null || outerContainer.isDirty();	//	formColumn1.isDirty() || formColumn2.isDirty();
 	}
 	
 	protected void handleDirtyForm() {
 		boolean ready = outerContainer.isValid();
 		
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
 		nibf.setEmptyNoteText("Click the note icon to add notes for this proxy.");
 		return nibf;
 	}
 	
 	protected NotesIconButtonField<String> getIpNotesButtonField() {
 		NotesIconButtonField<String> nibf = new NotesIconButtonField<String>(this) {
 			@Override
 			public void updateNote(String note) {
 				asyncUpdateIpAddressNote(note);
 			}
 		};
 		nibf.setEmptyNoteText("Click the note icon to add notes for this proxy IP.");
 		return nibf;
 	}
 	
 	protected void enableEditButton(boolean enabled) {
 		if (editButton != null) editButton.setEnabled(enabled);
 	}
 	
 	/**
 	 * Set an agreement on the form, and load its institution
 	 * @param agreement
 	 */
 	protected void set(ProxyInstance proxy) {
 		this.proxy = proxy;
 		if (proxy == null) {
 			this.proxyId = -1;
 			enableEditButton(false);
 		} else {
 			this.proxyId = proxy.getProxyId();
 			enableEditButton(true);
 		}
 		
 		//	For existing records, set fields that cannot be changed to read only
 //		boolean isNew = proxy == null || proxy.isNewRecord() || proxy.isAddNew() || proxy.getProxyId() == 0;
 //		institutionField.setReadOnly(!isNew);
 		
 		if (proxy != null)
 			registerUserCache(proxy, identificationTip);
 		setPortletHeading();
 
 		if (proxy == null) {
 			MessageBox.alert("Proxy not found.", "The requested proxy was not found.", null);
 			clearFormValues();
 			statusField.setOriginalValue(true);
 			statusField.setValue(true);
 		} else {
 			
 			proxyIdField.setValue(proxy.getProxyIdCheckDigit() + "");
 			descriptionField.setValue(proxy.getDescription());
 			searchKeysField.setValue(proxy.getSearchKeys());
 
 			if (proxy.getNote() != null && proxy.getNote().length() > 0) {
 				notesField.setEditMode();
 				notesField.setNote(proxy.getNote());
 			} else {
 				notesField.setAddMode();
 				notesField.setNote("");			
 			}
 			
 //			statusDisplay.setValue(AppConstants.getStatusDescription(proxy.getStatus()));
 			statusField.setValue(AppConstants.STATUS_ACTIVE == proxy.getStatus());
 			
 		}
 		
 		//	Resize things to account for the data
 		layout(true);
 
 		updatePresenterLabel();
 		updateUserPortlet();	// This is mostly for a "create" so the portlet knows the agreement ID has been set
 		setOriginalValues();
 //		endEdit(false);
 	}
 	
 	public void beginEdit() {
 		editButton.disable();
 		cancelButton.enable();
 		enableFields();
 	}
 	
 	public void beginIpEdit() {
 		editIpAddress = true;
 		beginEdit();
 	}
 	
 	public void endEdit(boolean save) {
 		cancelButton.disable();
 		saveButton.disable();
 		disableFields();
 		if (save) {
 			editButton.disable();	//	Disable this ...let the update enable it when the response arrives
 			if (editIpAddress)
 				asyncUpdateIpAddress();
 			else
 				asyncUpdateProxy();
 		} else {
 			endEditTasks();
 		}
 	}
 	
 	public void endEditTasks() {
 		ipFieldSet.collapse();
 		ipFieldSet.disable();
 		resetFormValues();
 		editButton.enable();
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
 		if (editIpAddress) {
 			ipAddressField.enable();
 			ipApprovedField.enable();
 			ipStatusField.enable();
 			ipFieldSet.setExpanded(true);
 			ipFieldSet.enableFields(false);
 			ipFieldSet.setEnabled(true);
 		} else {
 			descriptionField.enable();
 			searchKeysField.enable();
 			statusField.enable();
 			ipFieldSet.setExpanded(false);
 			ipFieldSet.enableFields(false);
 			ipFieldSet.setEnabled(false);
 		}
 		
 //		for (Field<?> field : outerContainer.getFields()) {
 //			if (field.getParent() != null && field.getParent() instanceof LockableFieldSet) {
 //				LockableFieldSet lfs = (LockableFieldSet) field.getParent();
 //				lfs.enableFields(true);
 //			} else 
 //				field.enable();
 //		}
 	}
 	
 	public void disableFields() {
 		for (Field<?> field : outerContainer.getFields()) {
 			if (field == proxyIdNotesCombo)
 				proxyIdField.disable();
 			else
 				field.disable();
 		}
 	}
 
 	/**
 	 * Load the agreement for an ID
 	 * @param id
 	 */
 	protected void loadProxy(final int proxyId) {
 		proxyGetService.getProxy(proxyId, true,	// Include IP addresses
 				new AsyncCallback<ProxyTuple>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(ProxyTuple proxyTuple) {
 						set(proxyTuple.getProxy());
 						
 						ipAddressStore.removeAll();
 						if (proxyTuple.getProxyIps() != null) {
 							for (ProxyIpInstance agreement : proxyTuple.getProxyIps()) {
 								ipAddressStore.add(ProxyIpInstance.obtainModel(agreement));
 							}
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
 		AppEvent appEvent = new AppEvent(AppEvents.ProxyAccess);
 		if (target instanceof ProxyInstance) {
 			appEvent.set( (ProxyInstance) target);
 		} else if (target instanceof ProxyIpInstance) {
 			appEvent.set( (ProxyIpInstance) target);
 		}
  		AppEventBus.getSingleton().fireEvent(AppEvents.ProxyAccess, appEvent);
 	}
 
 	protected void asyncUpdateIpAddress() {
 		
 		if (proxy == null || proxy.getProxyId() <= 0) {
 			MessageBox.alert("No Proxy!", "Cannot create a proxy IP with no proxy!", null);
 			return;
 		}
 		
 		ProxyIpInstance proxyIp = new ProxyIpInstance();
 		
 		proxyIp.setNewRecord(proxyIpId <= 0);
 		
 		proxyIp.setProxyId(proxy.getProxyId());
 		proxyIp.setIpId(proxyIpId);
 		proxyIp.setIpLo(ipAddressField.getLowValue());
 		proxyIp.setIpHi(ipAddressField.getHighValue());
 		proxyIp.setApproved(ipApprovedField.getValue() ? AppConstants.ANSWER_YES : AppConstants.ANSWER_NO);
 		proxyIp.setStatus(ipStatusField.getValue() ? AppConstants.STATUS_ACTIVE : AppConstants.STATUS_INACTIVE);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateProxyIpService.updateProxyIpAddress(proxyIp,
 				new AsyncCallback<UpdateResponse<ProxyIpInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						editButton.enable();
 					}
 
 					public void onSuccess(UpdateResponse<ProxyIpInstance> updateResponse) {
 						ProxyIpInstance updatedProxyIp = (ProxyIpInstance) updateResponse.getInstance();
 
 						//	This puts the grid in synch
 						BeanModel gridModel = ProxyIpInstance.obtainModel(updatedProxyIp); // ipAddressGrid.getStore().findModel(updatedProxyIp.getUniqueKey());
 						if (gridModel != null) {
 							ProxyIpInstance matchInstance = gridModel.getBean();
 							matchInstance.setNote(updatedProxyIp.getNote());
 							if (ipAddressStore.findModel(gridModel) != null)
 								ipAddressGrid.getStore().update(gridModel);
 							else
 								ipAddressStore.add(gridModel);
 						}
 						
 						endEditTasks();
 				}
 			});
 	}
 
 	protected void asyncUpdateIpAddressNote(String note) {
 	
 		// Set field values from form fields
 		
 		//	Not editing the IP... can't be here
 		if (!editIpAddress)
 			return;
 		
 		//	No / New proxy ... that can't be
 		if (proxy == null || proxy.isNewRecord()) {
 			return;
 		}
 		
 		//	New Proxy IP... update later
 		if (proxyIpId <= 0)
 			return;
 		
 		final ProxyIpInstance proxyIp = new ProxyIpInstance();
 		
 		proxyIp.setProxyId(proxyId);
 		proxyIp.setIpId(proxyIpId);
 		proxyIp.setNote(note);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateProxyIpNoteService.updateProxyIpAddressNote(proxyIp,
 				new AsyncCallback<UpdateResponse<ProxyIpInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy IP note update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						ipNotesField.unlockNote();
 					}
 
 					public void onSuccess(UpdateResponse<ProxyIpInstance> updateResponse) {
 						ProxyIpInstance updatedProxyIp = (ProxyIpInstance) updateResponse.getInstance();
 						if (!ipNotesField.getNote().equals(updatedProxyIp.getNote())) {
 							ipNotesField.setNote(updatedProxyIp.getNote());
 						}
 						BeanModel model = ipAddressStore.findModel(updatedProxyIp.getUniqueKey());
 						if (model != null) {
 							ProxyIpInstance proxyIp = (ProxyIpInstance) model.getBean();
 							proxyIp.setNote(updatedProxyIp.getNote());
 							ipAddressStore.update(model);
 						}
 						ipNotesField.unlockNote();
 				}
 			});
 	}
 
 	protected void asyncUpdateProxy() {
 	
 		// Set field values from form fields
 		
 		if (proxy == null) {
 			proxy = new ProxyInstance();
 			proxy.setNewRecord(true);
 			proxy.setStatus(AppConstants.STATUS_ACTIVE);	//	This is later overridden by statusField, but we set it here just in case that's removed at some point
 		}
 		
 		proxy.setDescription( descriptionField.getValue() );
 		proxy.setSearchKeys( searchKeysField.getValue() );
 		proxy.setStatus(statusField.getValue() ? AppConstants.STATUS_ACTIVE : AppConstants.STATUS_INACTIVE);
 		
 		if (proxy.isNewRecord())
 			proxy.setNote(notesField.getNote());
 		else
 			proxy.setNote(null);	//	This will keep the note from being updated by this call
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateProxyService.updateProxy(proxy,
 				new AsyncCallback<UpdateResponse<ProxyInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						editButton.enable();
 					}
 
 					public void onSuccess(UpdateResponse<ProxyInstance> updateResponse) {
 						ProxyInstance updatedProxy = (ProxyInstance) updateResponse.getInstance();
 						if (updatedProxy.isNewRecord()) {
 							updatedProxy.setNewRecord(false);
 							identificationTip = "Proxy created " + new Date();
 						}
 						proxy.setNewRecord(false);
 						set(updatedProxy);
 				//		enableAgreementButtons(true);
 				}
 			});
 	}
 
 	protected void asyncUpdateNote(String note) {
 	
 		// Set field values from form fields
 		
 		if (proxy == null || proxy.isNewRecord()) {
 			return;
 		}
 		
 		proxy.setNote(note);
 	
 		//	Issue the asynchronous update request and plan on handling the response
 		updateProxyNoteService.updateProxyNote(proxy,
 				new AsyncCallback<UpdateResponse<ProxyInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Proxy note update failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						notesField.unlockNote();
 					}
 
 					public void onSuccess(UpdateResponse<ProxyInstance> updateResponse) {
 						ProxyInstance updatedAgreement = (ProxyInstance) updateResponse.getInstance();
 						if (!notesField.getNote().equals(updatedAgreement.getNote())) {
 							notesField.setNote(updatedAgreement.getNote());
 							proxy.setNote(updatedAgreement.getNote());
 						}
 						notesField.unlockNote();
 				}
 			});
 	}
 	
 	public int getProxyId() {
 		return proxyId;
 	}
 
 	public void setProxyId(int proxyId) {
 		this.proxyId = proxyId;
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
 		if (proxyId > 0)
			return "Proxy " + proxyId;
 		return "Create Proxy";
 	}
 	
 	@Override
 	public boolean allowDuplicatePortlets() {
 		//	Not allowed for a particular proxy
 		if (proxyId > 0)
 			return false;
 		//	Allowed for "create new"
 		return true;
 	}
 	
 	@Override
 	public String getPortletIdentity() {
 		return getPortletIdentity(proxyId);
 	}
 	
 	public static String getPortletIdentity(int proxyId) {
 		if (proxyId <= 0)
 			return ProxyPortlet.class.getName();
 		return ProxyPortlet.class.getName() + ":" + proxyId;
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
 		if (parts.length > 0) proxyId = Integer.parseInt(parts [0]);
 		if (parts.length > 1) identificationTip = parts [1];
 	}
 
 	/**
 	 * For the user cache, return the "set" data as a string for offline storage
 	 */
 	@Override
 	public String getKeyData() {
 		if (identificationTip == null)
 			return proxyId + "";
 		else
 			return proxyId + ":" + identificationTip;
 	}
 
 }
