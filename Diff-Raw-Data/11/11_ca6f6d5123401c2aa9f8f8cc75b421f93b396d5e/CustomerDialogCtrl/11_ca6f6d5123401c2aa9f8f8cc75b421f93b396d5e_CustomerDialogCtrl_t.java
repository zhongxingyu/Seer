 /**
  * Copyright 2010 the original author or authors.
  * 
  * This file is part of Zksample2. http://zksample2.sourceforge.net/
  *
  * Zksample2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Zksample2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Zksample2.  If not, see <http://www.gnu.org/licenses/gpl.html>.
  */
 package de.forsthaus.webui.customer;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.Path;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Checkbox;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Panel;
 import org.zkoss.zul.Panelchildren;
 import org.zkoss.zul.Tab;
 import org.zkoss.zul.Tabpanel;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 import de.forsthaus.UserWorkspace;
 import de.forsthaus.backend.model.Branche;
 import de.forsthaus.backend.model.Customer;
 import de.forsthaus.backend.service.BrancheService;
 import de.forsthaus.backend.service.ChartService;
 import de.forsthaus.backend.service.CustomerService;
 import de.forsthaus.backend.util.HibernateSearchObject;
 import de.forsthaus.webui.util.ButtonStatusCtrl;
 import de.forsthaus.webui.util.GFCBaseCtrl;
 import de.forsthaus.webui.util.MultiLineMessageBox;
 import de.forsthaus.webui.util.ZksampleMessageUtils;
 import de.forsthaus.webui.util.searchdialogs.BranchAdvancedSearchListBox;
 import de.forsthaus.webui.util.searchdialogs.BranchExtendedSearchListBox;
 import de.forsthaus.webui.util.searchdialogs.BranchSimpleSearchListBox;
 
 /**
  * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++<br>
  * This is the controller class for the
  * /WEB-INF/pages/customer/customerDialog.zul file. <br>
  * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++<br>
  * <br>
  * 
  * 1. In this dialog we can do the mainly database oriented methods like <br>
  * new, edit, save, delete a customer. Please attention, we have a relation to <br>
  * the table 'filiale'. <br>
  * <br>
  * 2. Before closing this dialog we check if there are unsaved changes. <br>
  * <br>
  * 3. We show the components corresponding to the logged-in user rights. <br>
  * 4. We have a little validation implemented. <br>
  * <br>
  * 5. By selecting the order tab we load a new zul-file in it for showing <br>
  * the orders and the orderpositions. They have their own controllers. <br>
  * <br>
  * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  * 
  * @changes 05/15/2009: sge Migrating the list models for paging. <br>
  *          07/24/2009: sge changes for clustering.<br>
  *          10/12/2009: sge changings in the saving routine.<br>
  *          11/07/2009: bbr changed to extending from GFCBaseCtrl<br>
  *          (GenericForwardComposer) for spring-managed creation.<br>
  *          +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  * @author bbruhns
  * @author sgerth
  */
 public class CustomerDialogCtrl extends GFCBaseCtrl implements Serializable {
 
 	private static final long serialVersionUID = -546886879998950467L;
 	private final static Logger logger = Logger.getLogger(CustomerDialogCtrl.class);
 
 	// private PagedListWrapper<Branche> plwBranche;
 
 	/*
 	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	 * All the components that are defined here and have a corresponding
 	 * component with the same 'id' in the zul-file are getting autowired by our
 	 * 'extends GFCBaseCtrl' GenericForwardComposer.
 	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	 */
 	protected Window window_customerDialog; // autowired
 
 	// tab Address
 	protected Tab tabCustomerDialogAddress; // autowired
 	protected Tabpanel tabPanelCustomerAddress; // autowired
 	protected Textbox kunNr; // autowired
 	protected Textbox kunMatchcode; // autowired
 	protected Textbox kunName1; // autowired
 	protected Textbox kunName2; // autowired
 	protected Textbox kunOrt; // autowired
 	protected Checkbox kunMahnsperre; // autowired
 
 	protected Textbox kunBranche; // autowired
 	protected Button btnSearchBranchSimple; // autowired
 	protected Button btnSearchBranchAdvanced; // autowired
 	protected Button btnSearchBranchExtended; // autowired
 
 	// tab Chart
 	protected Tab tabCustomerDialogChart; // autowired
 	protected Tabpanel tabPanelCustomerDialogChart; // autowired
 
 	// tab Orders
 	protected Tab tabCustomerDialogOrders; // autowired
 	protected Tabpanel tabPanelCustomerOrders; // autowired
 
 	// tab Memos
 	protected Tab tabCustomerDialogMemos; // autowired
 	protected Tabpanel tabPanelCustomerMemos; // autowired
 
 	// not auto wired vars
 	private Customer customer; // overhanded per param
 	private transient CustomerListCtrl customerListCtrl; // overhanded per param
 
 	// old value vars for edit mode. that we can check if something
 	// on the values are edited since the last init.
 	private transient String oldVar_kunNr;
 	private transient String oldVar_kunMatchcode;
 	private transient String oldVar_kunName1;
 	private transient String oldVar_kunName2;
 	private transient String oldVar_kunOrt;
 	private transient String oldVar_kunBranche;
 	private transient boolean oldVar_kunMahnsperre;
 
 	private transient boolean validationOn;
 
 	// Button controller for the CRUD buttons
 	private transient final String btnCtroller_ClassPrefix = "button_CustomerDialog_";
 	private transient ButtonStatusCtrl btnCtrl;
 	protected Button btnNew; // autowire
 	protected Button btnEdit; // autowire
 	protected Button btnDelete; // autowire
 	protected Button btnSave; // autowire
 	protected Button btnCancel; // autowire
 	protected Button btnClose; // autowire
 
 	protected Button btnHelp; // autowire
 
 	// ServiceDAOs / Domain Classes
 	private transient BrancheService brancheService;
 	private transient CustomerService customerService;
 	private transient ChartService chartService;
 
 	/**
 	 * default constructor.<br>
 	 */
 	public CustomerDialogCtrl() {
 		super();
 	}
 
 	// +++++++++++++++++++++++++++++++++++++++++++++++++ //
 	// +++++++++++++++ Component Events ++++++++++++++++ //
 	// +++++++++++++++++++++++++++++++++++++++++++++++++ //
 
 	/**
 	 * Before binding the data and calling the dialog window we check, if the
 	 * zul-file is called with a parameter for a selected customer object in a
 	 * Map.
 	 * 
 	 * @param event
 	 * @throws Exception
 	 */
 	public void onCreate$window_customerDialog(Event event) throws Exception {
 		// window_customerDialog.setContentStyle("padding-left: 0px; padding-right: 0px; padding-top: 0px; padding-bottom: 0px;");
 
 		/* set components visible dependent of the users rights */
 		doCheckRights();
 		/* create the Button Controller. Disable not used buttons during working */
 		btnCtrl = new ButtonStatusCtrl(getUserWorkspace(), btnCtroller_ClassPrefix, btnNew, btnEdit, btnDelete, btnSave, btnCancel, btnClose);
 
 		// get the params map that are overhanded by creation.
 		Map<String, Object> args = getCreationArgsMap(event);
 
 		// READ OVERHANDED params !
 		if (args.containsKey("customer")) {
 			customer = (Customer) args.get("customer");
 			setCustomer(customer);
 		} else {
 			setCustomer(null);
 		}
 
 		// READ OVERHANDED params !
 		// we get the customerListWindow controller. So we have access
 		// to it and can synchronize the shown data when we do insert, edit or
 		// delete customers here.
 		if (args.containsKey("customerListCtrl")) {
 			setCustomerListCtrl((CustomerListCtrl) args.get("customerListCtrl"));
 		} else {
 			setCustomerListCtrl(null);
 		}
 
 		// // +++++++++ DropDown ListBox +++++++++++++++++++ //
 		// kunBranche.setModel(new
 		// ListModelList(getBrancheService().getAlleBranche()));
 		//
 		// kunBranche.setItemRenderer(new
 		// CustomerBrancheListModelItemRenderer());
 		//
 		// // get the ListModelList back for this Listbox for work with it
 		// ListModelList lml = (ListModelList) kunBranche.getModel();
 		// // get Object that we want to select in the Listbox
 		// Branche branche = customer.getBranche();
 		// // select the ListItem in the Listbox by an integerPosition by
 		// getting
 		// // it's position in the corresponding ListModelList (lml)
 		// kunBranche.setSelectedIndex(lml.indexOf(branche));
 
 		// set Field Properties
 		doSetFieldProperties();
 
 		doShowDialog(getCustomer());
 
 	}
 
 	/**
 	 * Set the properties of the fields, like maxLength.<br>
 	 */
 	private void doSetFieldProperties() {
 		kunNr.setMaxlength(20);
 		kunMatchcode.setMaxlength(20);
 		kunName1.setMaxlength(50);
 		kunName2.setMaxlength(50);
 		kunOrt.setMaxlength(50);
 	}
 
 	/**
 	 * User rights check. <br>
 	 * Only components are set visible=true if the logged-in <br>
 	 * user have the right for it. <br>
 	 * 
 	 * The rights are get from the spring framework users grantedAuthority(). A
 	 * right is only a string. <br>
 	 */
 	private void doCheckRights() {
 
 		final UserWorkspace workspace = getUserWorkspace();
 
 		window_customerDialog.setVisible(workspace.isAllowed("window_customerDialog"));
 
 		tabCustomerDialogAddress.setVisible(workspace.isAllowed("tab_CustomerDialog_Address"));
 		tabPanelCustomerAddress.setVisible(workspace.isAllowed("tab_CustomerDialog_Address"));
 
 		tabCustomerDialogChart.setVisible(workspace.isAllowed("tab_CustomerDialog_Chart"));
 		tabPanelCustomerDialogChart.setVisible(workspace.isAllowed("tab_CustomerDialog_Chart"));
 
 		tabCustomerDialogOrders.setVisible(workspace.isAllowed("tab_CustomerDialog_Orders"));
 		tabPanelCustomerOrders.setVisible(workspace.isAllowed("tab_CustomerDialog_Orders"));
 
 		tabCustomerDialogMemos.setVisible(workspace.isAllowed("tab_CustomerDialog_Memos"));
 		tabPanelCustomerMemos.setVisible(workspace.isAllowed("tab_CustomerDialog_Memos"));
 
 		btnHelp.setVisible(workspace.isAllowed("button_CustomerDialog_btnHelp"));
 		btnNew.setVisible(workspace.isAllowed("button_CustomerDialog_btnNew"));
 		btnEdit.setVisible(workspace.isAllowed("button_CustomerDialog_btnEdit"));
 		btnDelete.setVisible(workspace.isAllowed("button_CustomerDialog_btnDelete"));
 		btnSave.setVisible(workspace.isAllowed("button_CustomerDialog_btnSave"));
 		btnClose.setVisible(workspace.isAllowed("button_CustomerDialog_btnClose"));
 
 	}
 
 	/**
 	 * If we select the tab 'Orders' we load the components from a new zul-file <br>
 	 * with his own controller. <br>
 	 * 
 	 * @param event
 	 */
 	public void onSelect$tabCustomerDialogOrders(Event event) {
 		logger.debug(event.toString());
 
 		final Customer aCustomer = getCustomer();
 
 		/* overhanded params to the zul file */
 		final HashMap<String, Object> map = new HashMap<String, Object>();
 		map.put("customer", aCustomer);
 		map.put("customerDialogCtrl", this);
 
 		// PageSize for the Listboxes
 		map.put("rowSizeOrders", new Integer(10));
 		map.put("rowSizeOrderPositions", new Integer(10));
 
 		final Tabpanel orderTab = (Tabpanel) Path.getComponent("/window_customerDialog/tabPanelCustomerOrders");
 		orderTab.getChildren().clear();
 
 		final Panel panel = new Panel();
 		final Panelchildren pChildren = new Panelchildren();
 
 		panel.appendChild(pChildren);
 		orderTab.appendChild(panel);
 
 		// call the zul-file and put it on the tab.
 		Executions.createComponents("/WEB-INF/pages/order/orderList.zul", pChildren, map);
 	}
 
 	/**
 	 * If we select the tab 'Chart'. <br>
 	 * 
 	 * @param event
 	 * @throws IOException
 	 */
 	public void onSelect$tabCustomerDialogChart(Event event) throws IOException {
 		logger.debug(event.toString());
 
 		final Customer aCustomer = getCustomer();
 
 		/* overhanded params to the zul file */
 		HashMap<String, Object> map = new HashMap<String, Object>();
 		map.put("customer", aCustomer);
 		map.put("customerDialogCtrl", this);
 
 		// PageSize for the Listboxes
 		map.put("rowSizeOrders", new Integer(10));
 		map.put("rowSizeOrderPositions", new Integer(10));
 
 		Tabpanel chartTab = (Tabpanel) Path.getComponent("/window_customerDialog/tabPanelCustomerDialogChart");
 		chartTab.getChildren().clear();
 
 		Panel panel = new Panel();
 		Panelchildren pChildren = new Panelchildren();
 
 		panel.appendChild(pChildren);
 		chartTab.appendChild(panel);
 
 		// call the zul-file and put it on the tab.
 		Executions.createComponents("/WEB-INF/pages/customer/customerChart.zul", pChildren, map);
 
 	}
 
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	// +++++++++++++++++++++++ Components events +++++++++++++++++++++++
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 
 	/**
 	 * If we close the dialog window. <br>
 	 * 
 	 * @param event
 	 * @throws Exception
 	 */
 	public void onClose$window_customerDialog(Event event) throws Exception {
 		logger.debug(event.toString());
 
 		doClose();
 	}
 
 	/**
 	 * when the "save" button is clicked. <br>
 	 * 
 	 * @param event
 	 * @throws InterruptedException
 	 */
 	public void onClick$btnSave(Event event) throws InterruptedException {
 		logger.debug(event.toString());
 
 		doSave();
 	}
 
 	/**
 	 * when the "edit" button is clicked. <br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnEdit(Event event) {
 		logger.debug(event.toString());
 
 		doEdit();
 	}
 
 	/**
 	 * when the "help" button is clicked. <br>
 	 * 
 	 * @param event
 	 * @throws InterruptedException
 	 */
 	public void onClick$btnHelp(Event event) throws InterruptedException {
 		logger.debug(event.toString());
 		ZksampleMessageUtils.doShowNotImplementedMessage();
 	}
 
 	/**
 	 * when the "new" button is clicked. <br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnNew(Event event) {
 		logger.debug(event.toString());
 
 		doNew();
 	}
 
 	/**
 	 * when the "delete" button is clicked. <br>
 	 * 
 	 * @param event
 	 * @throws InterruptedException
 	 */
 	public void onClick$btnDelete(Event event) throws InterruptedException {
 		logger.debug(event.toString());
 
 		doDelete();
 	}
 
 	/**
 	 * when the "cancel" button is clicked. <br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnCancel(Event event) {
 		logger.debug(event.toString());
 
 		doCancel();
 	}
 
 	/**
 	 * If the Button 'Search Branch SimpleSearch' is clicked.<br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnSearchBranchSimple(Event event) {
 		doSearchBranchSimple(event);
 	}
 
 	/**
 	 * If the Button 'Search Branch AdvancedSearch' is clicked.<br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnSearchBranchAdvanced(Event event) {
 		doSearchBranchAdvanced(event);
 	}
 
 	/**
 	 * If the Button 'Search Branch ExtendedSearch' is clicked.<br>
 	 * 
 	 * @param event
 	 */
 	public void onClick$btnSearchBranchExtended(Event event) {
 		doSearchBranchExtended(event);
 	}
 
 	/**
 	 * when the "close" button is clicked. <br>
 	 * 
 	 * @param event
 	 * @throws InterruptedException
 	 */
 	public void onClick$btnClose(Event event) throws InterruptedException {
 		logger.debug(event.toString());
 
 		try {
 			doClose();
 		} catch (final Exception e) {
 			// close anyway
 			this.window_customerDialog.onClose();
 			// Messagebox.show(e.toString());
 		}
 	}
 
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	// ++++++++++++++++++++++++ GUI operations +++++++++++++++++++++++++
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 
 	/**
 	 * Closes the dialog window. <br>
 	 * <br>
 	 * Before closing we check if there are unsaved changes in <br>
 	 * the components and ask the user if saving the modifications. <br>
 	 * 
 	 * @throws InterruptedException
 	 * 
 	 */
 	private void doClose() throws InterruptedException {
 		// logger.debug("DataIsChanged :" + isDataChanged());
 
 		if (isDataChanged()) {
 
 			// Show a confirm box
 			String msg = Labels.getLabel("message_Data_Modified_Save_Data_YesNo");
 			String title = Labels.getLabel("message.Information");
 
 			MultiLineMessageBox.doSetTemplate();
 			if (MultiLineMessageBox.show(msg, title, MultiLineMessageBox.YES | MultiLineMessageBox.NO, MultiLineMessageBox.QUESTION, true, new EventListener() {
 				@Override
 				public void onEvent(Event evt) {
 					switch (((Integer) evt.getData()).intValue()) {
 					case MultiLineMessageBox.YES:
 						try {
 							doSave();
 						} catch (final InterruptedException e) {
 							throw new RuntimeException(e);
 						}
 					case MultiLineMessageBox.NO:
 						break; //
 					}
 				}
 			}
 
 			) == MultiLineMessageBox.YES) {
 			}
 		}
 		this.window_customerDialog.onClose();
 	}
 
 	/**
 	 * Cancel the actual operation. <br>
 	 * <br>
 	 * Resets to the original status.<br>
 	 * 
 	 */
 	private void doCancel() {
 		doResetInitValues();
 		doReadOnly();
 		btnCtrl.setInitEdit();
 	}
 
 	/**
 	 * Writes the bean data to the components.<br>
 	 * 
 	 * @param aCustomer
 	 *            Customer
 	 */
 	public void doWriteBeanToComponents(Customer aCustomer) {
 
 		kunNr.setValue(aCustomer.getKunNr());
 		kunMatchcode.setValue(aCustomer.getKunMatchcode().toUpperCase());
 		kunName1.setValue(aCustomer.getKunName1());
 		kunName2.setValue(aCustomer.getKunName2());
 		kunOrt.setValue(aCustomer.getKunOrt());
 		kunMahnsperre.setChecked(aCustomer.getKunMahnsperre());
 
 		kunBranche.setValue(aCustomer.getBranche().getBraBezeichnung());
 
 	}
 
 	/**
 	 * Writes the components values to the bean.<br>
 	 * 
 	 * @param aCustomer
 	 */
 	public void doWriteComponentsToBean(Customer aCustomer) {
 
 		aCustomer.setKunNr(kunNr.getValue());
 		aCustomer.setKunMatchcode(kunMatchcode.getValue().toUpperCase());
 		aCustomer.setKunName1(kunName1.getValue());
 		aCustomer.setKunName2(kunName2.getValue());
 		aCustomer.setKunOrt(kunOrt.getValue());
 
 	}
 
 	/**
 	 * Opens the Dialog window modal.
 	 * 
 	 * It checks if the dialog opens with a new or existing object and set the
 	 * readOnly mode accordingly.
 	 * 
 	 * @param aCustomer
 	 * @throws InterruptedException
 	 */
 	public void doShowDialog(Customer aCustomer) throws InterruptedException {
 
 		// if aCustomer == null then we opened the Dialog without
 		// args for a given entity, so we get a new Obj().
 		if (aCustomer == null) {
 			/** !!! DO NOT BREAK THE TIERS !!! */
 			// We don't create a new DomainObject() in the frontend.
 			// We GET it from the backend.
 			aCustomer = getCustomerService().getNewCustomer();
 			setCustomer(aCustomer);
 		} else {
 			setCustomer(aCustomer);
 		}
 
 		// set Readonly mode accordingly if the object is new or not.
 		if (aCustomer.isNew()) {
 			btnCtrl.setInitNew();
 			doEdit();
 			// setFocus
 			kunNr.focus();
 
 		} else {
 			btnCtrl.setInitEdit();
 			doReadOnly();
 		}
 
 		try {
 			// fill the components with the data
 			doWriteBeanToComponents(aCustomer);
 
 			// stores the inital data for comparing if they are changed
 			// during user action.
 			doStoreInitValues();
 
 			window_customerDialog.doModal(); // open the dialog in modal
 			// mode
 		} catch (final Exception e) {
 			Messagebox.show(e.toString());
 		}
 	}
 
 	/**
 	 * Opens the Search and Get Dialog for Branches.<br>
 	 * It appends/changes the branch object for the current bean.<br>
 	 * 
 	 * @param event
 	 */
 	private void doSearchBranchSimple(Event event) {
 
 		final Branche branche = BranchSimpleSearchListBox.show(window_customerDialog);
 
 		if (branche != null) {
 			kunBranche.setValue(branche.getBraBezeichnung());
 			Customer aCustomer = getCustomer();
 			aCustomer.setBranche(branche);
 			setCustomer(aCustomer);
 		}
 	}
 
 	/**
 	 * Opens the Search and Get Dialog for Branches.<br>
 	 * It appends/changes the branch object for the current bean.<br>
 	 * 
 	 * @param event
 	 */
 	private void doSearchBranchAdvanced(Event event) {
 
 		Branche branche = BranchAdvancedSearchListBox.show(window_customerDialog);
 
 		if (branche != null) {
 			kunBranche.setValue(branche.getBraBezeichnung());
 			Customer aCustomer = getCustomer();
 			aCustomer.setBranche(branche);
 			setCustomer(aCustomer);
 		}
 	}
 
 	/**
 	 * Opens the Search and Get Dialog for Branches.<br>
 	 * It appends/changes the branch object for the current bean.<br>
 	 * 
 	 * @param event
 	 */
 	private void doSearchBranchExtended(Event event) {
 
 		Branche branche = BranchExtendedSearchListBox.show(window_customerDialog);
 
 		if (branche != null) {
 			kunBranche.setValue(branche.getBraBezeichnung());
 			Customer aCustomer = getCustomer();
 			aCustomer.setBranche(branche);
 			setCustomer(aCustomer);
 		}
 	}
 
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	// ++++++++++++++++++++++++++++++ helpers ++++++++++++++++++++++++++
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 
 	/**
 	 * Stores the init values in mem vars. <br>
 	 */
 	private void doStoreInitValues() {
 		oldVar_kunNr = kunNr.getValue();
 		oldVar_kunMatchcode = kunMatchcode.getValue();
 		oldVar_kunName1 = kunName1.getValue();
 		oldVar_kunName2 = kunName2.getValue();
 		oldVar_kunOrt = kunOrt.getValue();
 
 		oldVar_kunBranche = kunBranche.getValue();
 		oldVar_kunMahnsperre = kunMahnsperre.isChecked();
 	}
 
 	/**
 	 * Resets the init values from mem vars. <br>
 	 */
 	private void doResetInitValues() {
 		kunNr.setValue(oldVar_kunNr);
 		kunMatchcode.setValue(oldVar_kunMatchcode);
 		kunName1.setValue(oldVar_kunName1);
 		kunName2.setValue(oldVar_kunName2);
 		kunOrt.setValue(oldVar_kunOrt);
 
 		kunBranche.setValue(oldVar_kunBranche);
 		kunMahnsperre.setChecked(oldVar_kunMahnsperre);
 	}
 
 	/**
 	 * Checks, if data are changed since the last call of <br>
 	 * doStoreInitData() . <br>
 	 * 
 	 * @return true, if data are changed, otherwise false
 	 */
 	private boolean isDataChanged() {
 		boolean changed = false;
 
 		if (oldVar_kunNr != kunNr.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunMatchcode != kunMatchcode.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunName1 != kunName1.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunName2 != kunName2.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunOrt != kunOrt.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunBranche != kunBranche.getValue()) {
 			changed = true;
 		}
 		if (oldVar_kunMahnsperre != kunMahnsperre.isChecked()) {
 			changed = true;
 		}
 
 		return changed;
 	}
 
 	/**
 	 * Sets the Validation by setting the accordingly constraints to the fields.
 	 */
 	private void doSetValidation() {
 
 		setValidationOn(true);
 
 		kunNr.setConstraint("NO EMPTY");
 		kunMatchcode.setConstraint("NO EMPTY");
 		kunName1.setConstraint("NO EMPTY");
 		kunOrt.setConstraint("NO EMPTY");
 		// TODO helper textbox for selectedItem ?????
 		// kunBranche.setConstraint(new SimpleConstraint("NO EMPTY"));
 	}
 
 	/**
 	 * Disables the Validation by setting empty constraints.
 	 */
 	private void doRemoveValidation() {
 
 		setValidationOn(false);
 
 		kunNr.setConstraint("");
 		kunMatchcode.setConstraint("");
 		kunName1.setConstraint("");
 		kunOrt.setConstraint("");
 	}
 
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 	// +++++++++++++++++++++++++ crud operations +++++++++++++++++++++++
 	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 
 	/**
 	 * Deletes a customer object from database.<br>
 	 * 
 	 * @throws InterruptedException
 	 */
 	private void doDelete() throws InterruptedException {
 
 		final Customer aCustomer = getCustomer();
 
 		// Show a confirm box
 		String msg = Labels.getLabel("message.Question.Are_you_sure_to_delete_this_record") + "\n\n --> " + aCustomer.getKunName1() + " " + aCustomer.getKunName2() + " ," + aCustomer.getKunOrt();
 		String title = Labels.getLabel("message.Deleting.Record");
 
 		MultiLineMessageBox.doSetTemplate();
 		if (MultiLineMessageBox.show(msg, title, MultiLineMessageBox.YES | MultiLineMessageBox.NO, Messagebox.QUESTION, true, new EventListener() {
 			@Override
 			public void onEvent(Event evt) {
 				switch (((Integer) evt.getData()).intValue()) {
 				case MultiLineMessageBox.YES:
 					try {
 						deleteCustomer();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				case MultiLineMessageBox.NO:
 					break; //
 				}
 			}
 
 			// delete from database
 			private void deleteCustomer() throws InterruptedException {
 
 				try {
 					getCustomerService().delete(aCustomer);
 				} catch (DataAccessException e) {
 					ZksampleMessageUtils.showErrorMessage(e.getMostSpecificCause().toString());
 				}
 
 				// ++ create the searchObject and init sorting ++ //
 				final HibernateSearchObject<Customer> soCustomer = new HibernateSearchObject<Customer>(Customer.class, getCustomerListCtrl().getCountRows());
 				soCustomer.addSort("kunName1", false);
 				// Set the ListModel
 				getCustomerListCtrl().getPagedListWrapper().setSearchObject(soCustomer);
 
 				// now synchronize the customers listBox
 				final ListModelList lml = (ListModelList) getCustomerListCtrl().listBoxCustomer.getListModel();
 
 				// Check if the customer object is new or updated
 				// -1 means that the obj is not in the list, so it's
 				// new..
 				if (lml.indexOf(aCustomer) == -1) {
 				} else {
 					lml.remove(lml.indexOf(aCustomer));
 				}
 
 				CustomerDialogCtrl.this.window_customerDialog.onClose(); // close
 				// the
 				// dialog
 			}
 		}
 
 		) == MultiLineMessageBox.YES) {
 		}
 
 	}
 
 	/**
 	 * Create a new customer object. <br>
 	 */
 	private void doNew() {
 
		// remember the old vars
		doStoreInitValues();

 		/** !!! DO NOT BREAK THE TIERS !!! */
 		// we don't create a new Kunde() in the frontend.
 		// we get it from the backend.
 		final Customer aCustomer = getCustomerService().getNewCustomer();
 
 		// our customer have a table-reference on filiale
 		// we take the filiale we have become by logged in
 		aCustomer.setOffice(getUserWorkspace().getOffice());
 		setCustomer(aCustomer);
 
 		doEdit(); // edit mode
		doClear(); // clear all commponents
 
 		btnCtrl.setBtnStatus_New();
 
 		// setFocus
 		kunNr.focus();
 
 	}
 
 	/**
 	 * Set the components for edit mode. <br>
 	 */
 	private void doEdit() {
 
 		kunNr.setReadonly(false);
 		kunMatchcode.setReadonly(false);
 		kunName1.setReadonly(false);
 		kunName2.setReadonly(false);
 		kunOrt.setReadonly(false);
 		kunMahnsperre.setDisabled(false);
 
 		kunBranche.setReadonly(false);
 		btnSearchBranchSimple.setDisabled(false);
 		btnSearchBranchAdvanced.setDisabled(false);
 		btnSearchBranchExtended.setDisabled(false);
 
 		btnCtrl.setBtnStatus_Edit();
 
 		// remember the old vars
 		doStoreInitValues();
 	}
 
 	/**
 	 * Set the components to ReadOnly. <br>
 	 */
 	public void doReadOnly() {
 
 		kunNr.setReadonly(true);
 		kunMatchcode.setReadonly(true);
 		kunName1.setReadonly(true);
 		kunName2.setReadonly(true);
 		kunOrt.setReadonly(true);
 		kunMahnsperre.setDisabled(true);
 
 		kunBranche.setReadonly(true);
 		btnSearchBranchSimple.setDisabled(true);
 		btnSearchBranchAdvanced.setDisabled(true);
 		btnSearchBranchExtended.setDisabled(true);
 	}
 
 	/**
 	 * Clears the components values. <br>
 	 */
 	public void doClear() {
 
 		// remove validation, if there are a save before
 		doRemoveValidation();
 
 		kunNr.setValue("");
 		kunMatchcode.setValue("");
 		kunName1.setValue("");
 		kunName2.setValue("");
 		kunOrt.setValue("");
 		kunMahnsperre.setChecked(false);
 
 		// unselect the last customers branch
 		kunBranche.setValue("");
 	}
 
 	/**
 	 * Saves the components to table. <br>
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void doSave() throws InterruptedException {
 
 		final Customer aCustomer = getCustomer();
 
 		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 		// force validation, if on, than execute by component.getValue()
 		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 		if (!isValidationOn()) {
 			doSetValidation();
 		}
 
 		// fill the customer object with the components data
 		doWriteComponentsToBean(aCustomer);
 
 		// get the selected branch object from the listbox
 		// Listitem item = kunBranche.getSelectedItem();
 
 		if (StringUtils.isEmpty(kunBranche.getValue())) {
 			try {
 				Messagebox.show("Please select a branch !");
 			} catch (final InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 			return;
 		}
 
 		// ListModelList lml1 = (ListModelList) kunBranche.getListModel();
 		// Branche branche = (Branche) lml1.get(item.getIndex());
 		// aCustomer.setBranche(branche);
 
 		if (kunMahnsperre.isChecked() == true) {
 			aCustomer.setKunMahnsperre(true);
 		} else {
 			aCustomer.setKunMahnsperre(false);
 		}
 
 		// save it to database
 		try {
 			getCustomerService().saveOrUpdate(aCustomer);
 			// fill the components with the data
 			// if something is changed during the save process
 			doWriteBeanToComponents(aCustomer);
 
 		} catch (DataAccessException e) {
 			ZksampleMessageUtils.showErrorMessage(e.getMostSpecificCause().toString());
 
 			// Reset to init values
 			doResetInitValues();
 
 			doReadOnly();
 			btnCtrl.setBtnStatus_Save();
 			return;
 		}
 
 		// ++ create the searchObject and init sorting ++ //
 		final HibernateSearchObject<Customer> soCustomer = new HibernateSearchObject<Customer>(Customer.class, getCustomerListCtrl().getCountRows());
 		soCustomer.addSort("kunName1", false);
 		// Set the ListModel
 		getCustomerListCtrl().getPagedListWrapper().setSearchObject(soCustomer);
 
 		// call from cusromerList then synchronize the customers listBox
 		if (getCustomerListCtrl().listBoxCustomer != null) {
 
 			// now synchronize the customer listBox
 			final ListModelList lml = (ListModelList) getCustomerListCtrl().listBoxCustomer.getListModel();
 
 			// Check if the customer object is new or updated
 			// -1 means that the obj is not in the list, so it's new.
 			if (lml.indexOf(aCustomer) == -1) {
 				lml.add(aCustomer);
 			} else {
 				lml.set(lml.indexOf(aCustomer), aCustomer);
 			}
 
 		}
 
 		doReadOnly();
 		btnCtrl.setBtnStatus_Save();
 		// init the old values vars new
 		doStoreInitValues();
 
 	}
 
 	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++//
 	// ++++++++++++++++++ getter / setter +++++++++++++++++++//
 	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++//
 
 	public void setValidationOn(boolean validationOn) {
 		this.validationOn = validationOn;
 	}
 
 	public boolean isValidationOn() {
 		return this.validationOn;
 	}
 
 	public Customer getCustomer() {
 		return this.customer;
 	}
 
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	public void setCustomerService(CustomerService customerService) {
 		this.customerService = customerService;
 	}
 
 	public CustomerService getCustomerService() {
 		return this.customerService;
 	}
 
 	public BrancheService getBrancheService() {
 		return this.brancheService;
 	}
 
 	public void setBrancheService(BrancheService brancheService) {
 		this.brancheService = brancheService;
 	}
 
 	public void setChartService(ChartService chartService) {
 		this.chartService = chartService;
 	}
 
 	public ChartService getChartService() {
 		return this.chartService;
 	}
 
 	public void setCustomerListCtrl(CustomerListCtrl customerListCtrl) {
 		this.customerListCtrl = customerListCtrl;
 	}
 
 	public CustomerListCtrl getCustomerListCtrl() {
 		return this.customerListCtrl;
 	}
 
 }
