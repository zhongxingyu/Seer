 /*
 *
 *	Copyright (c) 2013 Andreas Reder
 *	Author      : Andreas Reder <andreas.reder@lielas.org>
 *	File		: 
 *
 *	This File is part of lielas, see www.lielas.org for more information.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
 package lielas.ui;
 
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import lielas.core.Config;
 import lielas.core.Device;
 import lielas.core.DeviceContainer;
 import lielas.core.ExceptionHandler;
 import lielas.core.LBus;
 import lielas.core.LBusReceiver;
 import lielas.core.LBusSender;
 import lielas.core.LanguageHelper;
 import lielas.core.SQLHelper;
 import lielas.core.CSVHelper;
 import lielas.core.User;
 
 import lielas.LiewebUI;
 import lielas.ui.OptionsUserDetailsScreen;
 import lielas.ui.YesNoPopupScreen.PopupClosedListener;
 
 //import com.github.wolfie.refresher.Refresher;
 //import com.github.wolfie.refresher.Refresher.RefreshListener;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.server.Sizeable.Unit;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.InlineDateField;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.server.AbstractExtension;
 
 @SuppressWarnings({ "serial", "unused" })
 public class OptionsScreen extends Panel{
 
 	private LiewebUI app;
 	
 	private TabSheet settingsTab = null;
 	
 //	Refresher refresher = null;
 	
 	private VerticalLayout globalSettingsLayout = null;
 	private VerticalLayout userSettingsLayout = null;
 	private VerticalLayout groupSettingsLayout = null;
 	private VerticalLayout updateSettingsLayout = null;
 	private VerticalLayout registerSettingsLayout = null;
 	
 	private VerticalLayout usersDetailLayout = null;
 	private VerticalLayout groupsDetailLayout = null;
 	
 	private Table usersTable = null;
 	private Table groupsTable = null;
 	
 	private Label clockSettingsLbl = null;
 	private Label languageSettingsLbl = null;
 	private Label databaseSettingsLbl = null;
 	private Label networkSettingsLbl = null;
 	private Label sixLowPanSettingsLbl = null;
 	private Label gatewaySettingsLbl = null;
 	
 	private CheckBox useTimeServerCB = null;
 	private TextField timeServerTxt = null;
 	private Label rtcStateTxt = null;
 	private InlineDateField dateField = null;
 	private NativeButton setDatetimeBttn = null;
 	
 	private DateField deleteBeforedateField = null;
 	
 	private Label useDhcpDLbl = null;
 	private CheckBox useDhcpCB = null;
 	private Label ipAddressDLbl = null;
 	private TextField ipAddressCTx = null;
 	private Label netmaskDLbl = null;
 	private TextField netmaskCTx = null;
 	private Label gwAddressDLbl = null;
 	private TextField gwAddressCTx = null;
 
 	private Label slpPanIdDLbl;
 	private TextField slpPanIdCTx;
 	
 	private TextField gwPrefixCTx = null;
 	private TextField gwIPCTx = null;
 	private TextField gwUARTCTx = null;
 	private TextField gwBaudrateCTx = null;
 	private TextField gwTunnelCTx = null;
 	
 	private NativeButton newUserBttn = null;
 	private NativeButton deleteDataBttn = null;
 	private NativeButton deleteDataBeforeBttn = null;
 	private NativeButton deleteDatabaseBttn  = null;
 	private NativeButton createTestDataBttn = null;
 	private NativeButton saveIPBttn = null;
 	
 	Label regCaptionLbl;
 	Label regMIntDLbl;
 	TextField regMIntCTx;
 	Label regPIntDLbl;
 	NativeSelect regPIntCSel;
 	Label regAIntDLbl;
 	TextField regAIntCTx;
 	NativeButton regSaveButton;
 	Label regTimeDLbl;
 	TextField regTimeCTx;
 	
 	final Integer RUNMODE_NORMAL = 1;
 	final Integer RUNMODE_REG = 2;
 	
 	Integer runmode = RUNMODE_NORMAL;
 	
 	private HorizontalLayout userSetDetailsNoSelectionLayout = null;
 	
 	private OptionsUserDetailsScreen optionsUserDetailsScreen = null;
 	
 	public OptionsScreen(final LiewebUI app){
 		this.app = app;
 		setSizeFull();
 		Activate();
 		
 		HorizontalLayout hLayout = new HorizontalLayout();
 		hLayout.setSizeFull();
 		
 		//refresher
 		/*refresher = new Refresher();
 		refresher.setRefreshInterval(2000);
 		refresher.addListener(new RefreshListener(){
 			@Override
 			public void refresh(final Refresher source){
 				CheckRunmode();
 			}
 		});
 		addExtension(refresher);*/
 		
 		/************************************************************************************************************************
 		 * 
 		 * 		settingsTab
 		 * 
 		 ************************************************************************************************************************/	
 		
 		settingsTab = new TabSheet();
 		settingsTab.setHeight(100, Unit.PERCENTAGE);
 		settingsTab.setWidth(1150, Unit.PIXELS);
 		settingsTab.addStyleName("tab-settings");
 		
 		/************************************************************************************************************************
 		 * 		globalSettingsLayout
 		 ************************************************************************************************************************/	
 		globalSettingsLayout = new VerticalLayout();
 		//globalSettingsLayout.setSizeFull();
 
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		// 								Clock Settings
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		//create panel
 		Panel clockSettingsPanel = new Panel();
 		clockSettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(clockSettingsPanel);
 		
 		//create vlayout
 		VerticalLayout clockSettingsLayout = new VerticalLayout();
 		clockSettingsLayout.addStyleName("settings-block");
 		clockSettingsPanel.setContent(clockSettingsLayout);
 		
 		//header
 		//clockSettingsLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_CLOCK_SETTINGS));
 		clockSettingsLbl = new Label("");
 		clockSettingsLbl.addStyleName("bold");
 		clockSettingsLayout.addComponent(clockSettingsLbl);
 
 		//content
 		FormLayout timeServerLo = new FormLayout();
 		clockSettingsLayout.addComponent(timeServerLo);
 		
 		rtcStateTxt = new Label("RTC Status: ");
 		timeServerLo.addComponent(rtcStateTxt);
 		
 		dateField = new InlineDateField();
 		dateField.setValue(new Date());
 		dateField.setResolution(Resolution.SECOND);
 		dateField.setVisible(false);
 		timeServerLo.addComponent(dateField);
 		
 		setDatetimeBttn = new NativeButton("Set Date");
 		setDatetimeBttn.addStyleName("optionsscreen");
 		setDatetimeBttn.setHeight(24, Unit.PIXELS);
 		setDatetimeBttn.setVisible(false);
 		timeServerLo.addComponent(setDatetimeBttn);
 		
 		setDatetimeBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				SetDateTimeBttnClicked();
 			}			
 		});
 		
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		// 						Language Settings 
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		//create panel
 		/*Panel languageSettingsPanel = new Panel();
 		languageSettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(languageSettingsPanel);
 		
 		//create vlayout
 		VerticalLayout languageSettingsLayout = new VerticalLayout();
 		languageSettingsLayout.addStyleName("settings-block");
 		languageSettingsPanel.setContent(languageSettingsLayout);
 
 		//header
 		languageSettingsLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_LANG_SETTINGS));
 		languageSettingsLbl.addStyleName("bold");
 		languageSettingsLayout.addComponent(languageSettingsLbl);*/
 		
 		//content
 		
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		// 							Database Settings
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		
 
 		//create panel
 		Panel databaseSettingsPanel = new Panel();
 		databaseSettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(databaseSettingsPanel);
 
 		//create vlayout
 		VerticalLayout databaseSettingsLayout = new VerticalLayout();
 		databaseSettingsLayout.addStyleName("settings-block");
 		databaseSettingsPanel.setContent(databaseSettingsLayout);
 		
 		//header
 		databaseSettingsLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_SETTINGS));
 		databaseSettingsLbl.addStyleName("bold");
 		databaseSettingsLbl.addStyleName("header");
 		databaseSettingsLayout.addComponent(databaseSettingsLbl);
 
 		//content
 		deleteDataBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE));
 		deleteDataBttn.addStyleName("optionsscreen");
 		deleteDataBttn.setHeight(24, Unit.PIXELS);
 		databaseSettingsLayout.addComponent(deleteDataBttn);
 		
 		deleteDataBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				DeleteDataBttnClicked(event);
 			}			
 		});
 		
 		deleteBeforedateField = new DateField();
 		deleteBeforedateField.addStyleName("optionsscreen");
 		deleteBeforedateField.setValue(new Date());
 		deleteBeforedateField.setResolution(Resolution.SECOND);
 		//deleteBeforedateField.setVisible(false);
 		databaseSettingsLayout.addComponent(deleteBeforedateField);
 		
 		deleteDataBeforeBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_BEFORE));
 		deleteDataBeforeBttn.addStyleName("optionsscreen");
 		deleteDataBeforeBttn.setHeight(24, Unit.PIXELS);
 		databaseSettingsLayout.addComponent(deleteDataBeforeBttn);
 		
 		deleteDataBeforeBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				DeleteDataBeforeBttnClicked(event);
 			}			
 		});
 		
 	/*	deleteDatabaseBttn = new NativeButton("Delete Database");
 		deleteDatabaseBttn.addStyleName("optionsscreen");
 		//deleteDatabaseBttn.setWidth(180, Unit.PIXELS);
 		deleteDatabaseBttn.setHeight(24, Unit.PIXELS);
 		databaseSettingsPanel.addComponent(deleteDatabaseBttn);
 		
 		deleteDatabaseBttn.addListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				DeleteDatabaseBttnClicked(event);
 			}			
 		});*/
 		
 		
 		/*createTestDataBttn = new NativeButton("Create Testdata");
 		createTestDataBttn.addStyleName("optionsscreen");
 		//deleteDatabaseBttn.setWidth(180, Unit.PIXELS);
 		createTestDataBttn.setHeight(24, Unit.PIXELS);
 		databaseSettingsPanel.addComponent(createTestDataBttn);
 		
 		createTestDataBttn.addListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {		
 				if(DeviceContainer.sql != null){
 					DeviceContainer.sql.CreateTestData(app.deviceContainer, 100000);
 				}
 			}			
 		});*/
 		
 
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		// 							Network settings
 		//////////////////////////////////////////////////////////////////////////////////////////////
 
 		//create panel
 		Panel networkSettingsPanel = new Panel();
 		networkSettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(networkSettingsPanel);
 		
 
 		//create vlayout
 		VerticalLayout networkSettingsLayout = new VerticalLayout();
 		networkSettingsLayout.addStyleName("settings-block");
 		networkSettingsPanel.setContent(networkSettingsLayout);
 		
 		//header
 		networkSettingsLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SETTINGS));
 		networkSettingsLbl.addStyleName("bold");
 		networkSettingsLayout.addComponent(networkSettingsLbl);
 		
 		
 		//content
 		
 		GridLayout networkSettingsGridLayout = new GridLayout(2, 10);
 		networkSettingsLayout.addComponent(networkSettingsGridLayout);
 
 		useDhcpDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_TYPE));
 		useDhcpDLbl.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(useDhcpDLbl, 0, 0);
 		useDhcpCB = new CheckBox();
 		useDhcpCB.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(useDhcpCB, 1, 0);
 		
 		useDhcpCB.addValueChangeListener(new ValueChangeListener(){
 			public void valueChange(ValueChangeEvent event){
 				useDhcpCBclicked(event);
 			}
 		});
 		
 		ipAddressDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_IP));
 		ipAddressDLbl.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(ipAddressDLbl, 0, 1);
 		ipAddressCTx = new TextField();
 		ipAddressCTx.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(ipAddressCTx, 1, 1);
 		
 		netmaskDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_NETMASK));
 		netmaskDLbl.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(netmaskDLbl, 0, 2);
 		netmaskCTx = new TextField();
 		netmaskCTx.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(netmaskCTx, 1, 2);
 
 		gwAddressDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_GATEWAY));
 		gwAddressDLbl.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(gwAddressDLbl, 0, 3);
 		gwAddressCTx = new TextField();
 		gwAddressCTx.addStyleName("settings");
 		networkSettingsGridLayout.addComponent(gwAddressCTx, 1, 3);
 
 		saveIPBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SAVE));
 		saveIPBttn.addStyleName("optionsscreen");
 		saveIPBttn.setHeight(24, Unit.PIXELS);
 		networkSettingsGridLayout.addComponent(saveIPBttn, 0, 4);
 		
 		saveIPBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				SaveIPBttnClicked(event);
 			}			
 		});
 		
 		
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		// 							6LowPan Settings
 		//////////////////////////////////////////////////////////////////////////////////////////////
 		
 		//create panel
 		/*Panel sixLowPanSettingsPanel = new Panel();
 		sixLowPanSettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(sixLowPanSettingsPanel);
 		
 
 		//create vlayout
 		VerticalLayout sixLowPanSettingsLayout = new VerticalLayout();
 		sixLowPanSettingsLayout.addStyleName("settings-block");
 		sixLowPanSettingsPanel.setContent(sixLowPanSettingsLayout);
 		
 		//header
 		sixLowPanSettingsLbl = new Label("6LowPan Settings");
 		sixLowPanSettingsLbl.addStyleName("bold");
 		sixLowPanSettingsLayout.addComponent(sixLowPanSettingsLbl);
 		
 		GridLayout sixLowPanSettingsGridLayout = new GridLayout(2, 10);
 		sixLowPanSettingsLayout.addComponent(sixLowPanSettingsGridLayout);	
 		
 		slpPanIdDLbl = new Label("Panid");
 		slpPanIdDLbl.addStyleName("settings");
 		sixLowPanSettingsGridLayout.addComponent(slpPanIdDLbl, 0, 3);
 		slpPanIdCTx = new TextField();
 		slpPanIdCTx.addStyleName("settings");
 		sixLowPanSettingsGridLayout.addComponent(slpPanIdCTx, 1, 3);	*/	
 		
 		
 		
 	
 		// Gateway settings
 		
 		/*Panel gatewaySettingsPanel = new Panel();
 		gatewaySettingsPanel.addStyleName("settings-block");
 		globalSettingsLayout.addComponent(gatewaySettingsPanel);
 		
 		gatewaySettingsLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_6LOWPANGW_SETTINGS));
 		gatewaySettingsLbl.addStyleName("bold");
 		gatewaySettingsPanel.setContent(gatewaySettingsLbl);
 		
 		FormLayout gatewayFormLayout = new FormLayout();
 		gatewaySettingsPanel.setContent(gatewayFormLayout);
 		
 		gwPrefixCTx = new TextField("Prefix");
 		gatewayFormLayout.addComponent(gwPrefixCTx);
 		
 		gwIPCTx = new TextField("IP");
 		gatewayFormLayout.addComponent(gwIPCTx);
 		
 		gwUARTCTx = new TextField("UART");
 		gatewayFormLayout.addComponent(gwUARTCTx);
 		
 		gwBaudrateCTx = new TextField("Baudrate");
 		gatewayFormLayout.addComponent(gwBaudrateCTx);
 		
 		gwTunnelCTx = new TextField("Tunnel");
 		gatewayFormLayout.addComponent(gwTunnelCTx);
 		*/
 		
 		VerticalLayout gSpacerLayout = new VerticalLayout();
 		gSpacerLayout.setSizeFull();
 		globalSettingsLayout.addComponent(gSpacerLayout);
 		
 		
 		/************************************************************************************************************************
 		 * 		userSettingsLayout
 		 ************************************************************************************************************************/
 		userSettingsLayout = new VerticalLayout();
 		//userSettingsLayout.setSizeFull();
 		
 		HorizontalLayout uhLo = new HorizontalLayout();
 		//uhLo.setSizeFull();
 		uhLo.addStyleName("userlist");
 		
 		usersTable = new Table(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_NAME));
 		usersTable.setWidth(620, Unit.PIXELS);
 		usersTable.setSelectable(true);
 		usersTable.setImmediate(true);
 		usersTable.setPageLength(0);
 		usersTable.setStyleName("userlist");
 		
 		usersTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_USER, String.class, null);
 		usersTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_GROUP, String.class, null);
 		usersTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_TIMEZONE, String.class, null);
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_USER, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_USER));
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_GROUP, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_GROUP));
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_TIMEZONE, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_TIMEZONE));
 		
 		FillUsersTable();
 		
 		usersTable.addValueChangeListener(new Table.ValueChangeListener() {
 			public void valueChange(ValueChangeEvent event) {
 				UsersTableClickedHandler(event);
 			}
 		});
 
 		uhLo.addComponent(usersTable);
 		
 		userSettingsLayout.addComponent(uhLo);
 		
 		optionsUserDetailsScreen = new OptionsUserDetailsScreen(this.app);
 		optionsUserDetailsScreen.Update();
 		
 		uhLo.addComponent(optionsUserDetailsScreen);
 		
 		
 		/************************************************************************************************************************
 		 * 		groupSettingsLayout
 		 ************************************************************************************************************************/
 		groupSettingsLayout = new VerticalLayout();
 		groupSettingsLayout.setSizeFull();
 		
 		HorizontalLayout ghLo = new HorizontalLayout();
 		ghLo.setSizeFull();
 		ghLo.addStyleName("userlist");
 		
 		
 		groupsTable = new Table(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_NAME));
 		groupsTable.setWidth(620, Unit.PIXELS);
 		groupsTable.setSelectable(true);
 		groupsTable.setImmediate(true);
 		groupsTable.setPageLength(0);
 		groupsTable.setStyleName("grouplist");
 		
 		groupsTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_GROUP, String.class, null);
 		groupsTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DEL_DEVICES, String.class, null);
 		groupsTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_REG_DEVICES, String.class, null);
 		groupsTable.addContainerProperty(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DOWNLOAD, String.class, null);
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_GROUP, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_GROUP));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DEL_DEVICES, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DEL_DEVICES));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_REG_DEVICES, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_REG_DEVICES));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DOWNLOAD, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DOWNLOAD));
 		
 
 		groupsTable.addItem(new Object[]{ "Admin", "allowed", "allowed",  "allowed"}, new Integer(1));
 		groupsTable.addItem(new Object[]{ "User", "forbidden", "forbidden",  "allowed"}, new Integer(2));
 		
 		// Details
 		
 		groupsDetailLayout = new VerticalLayout();
 		groupsDetailLayout.setWidth(460, Unit.PIXELS);
 		groupsDetailLayout.setStyleName("detaillist");
 		groupsDetailLayout.setVisible(true);
 		
 		// Header 
 		
 		/*groupDetailHeaderLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_DETAILS_CAPTION));
 		groupDetailHeaderLbl.setWidth(460, Unit.PIXELS);
 		groupDetailHeaderLbl.addStyleName("detaillist-header");
 		groupDetailHeaderLbl.setHeight(29, Unit.PIXELS);
 		groupsDetailLayout.addComponent(groupDetailHeaderLbl);*/
 		
 		// Footer
 		
 		HorizontalLayout groupsDetailFooterLayout = new HorizontalLayout();
 		groupsDetailFooterLayout.setWidth(460, Unit.PIXELS);
 		groupsDetailFooterLayout.setHeight(40, Unit.PIXELS);
 		groupsDetailFooterLayout.setStyleName("detaillist-footer");
 		groupsDetailFooterLayout.setMargin(false);
 		groupsDetailLayout.addComponent(groupsDetailFooterLayout);
 		
 		
 		ghLo.addComponent(groupsTable);
 		ghLo.addComponent(groupsDetailLayout);
 		
 		//groupSettingsLayout.addComponent(ghLo);
 
 
 		/************************************************************************************************************************
 		 * 		updateSettingsLayout
 		 ************************************************************************************************************************/
 		updateSettingsLayout = new VerticalLayout();
 		updateSettingsLayout.setSizeFull();
 		
 		
 		/************************************************************************************************************************
 		 * 		updateSettingsLayout
 		 ************************************************************************************************************************/
 		registerSettingsLayout = new VerticalLayout();
 		registerSettingsLayout.setSizeFull();
 		
 		GridLayout regSettingsGridLayout = new GridLayout(2, 10);
 		registerSettingsLayout.addComponent(regSettingsGridLayout);
 		
 
 		regCaptionLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_CAPTION));
 		regCaptionLbl.addStyleName("reg-settings");
 		regCaptionLbl.addStyleName("bold");
 		regSettingsGridLayout.addComponent(regCaptionLbl, 0, 0);
 
 		regMIntDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_MINT_LBL));
 		regMIntDLbl.addStyleName("reg-settings");
 		regSettingsGridLayout.addComponent(regMIntDLbl, 0, 1);
 		regMIntCTx = new TextField();
 		regMIntCTx.addStyleName("reg-settings");
 		regMIntCTx.setWidth(180, Unit.PIXELS);
 		regSettingsGridLayout.addComponent(regMIntCTx, 1, 1);
 		
 		regPIntDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_PINT_LBL));
 		regPIntDLbl.addStyleName("reg-settings");
 		regSettingsGridLayout.addComponent(regPIntDLbl, 0, 2);
 		regPIntCSel = new NativeSelect();
 		regPIntCSel.addStyleName("reg-settings");
 		regPIntCSel.setWidth(180, Unit.PIXELS);
 		regPIntCSel.setNullSelectionAllowed(false);
 		regSettingsGridLayout.addComponent(regPIntCSel, 1, 2);
 		
 		regAIntDLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_AINT_LBL));
 		regAIntDLbl.addStyleName("reg-settings");
 		regSettingsGridLayout.addComponent(regAIntDLbl, 0, 3);
 		regAIntCTx = new TextField();
 		regAIntCTx.addStyleName("reg-settings");
 		regAIntCTx.setWidth(180, Unit.PIXELS);
 		regSettingsGridLayout.addComponent(regAIntCTx, 1, 3);
 		
 		regTimeDLbl = new Label("Registration time");
 		regTimeDLbl.addStyleName("reg-settings");
 		regSettingsGridLayout.addComponent(regTimeDLbl, 0, 4);
 		regTimeCTx = new TextField();
 		regTimeCTx.addStyleName("reg-settings");
 		regTimeCTx.setWidth(180, Unit.PIXELS);
 		regSettingsGridLayout.addComponent(regTimeCTx, 1, 4);
 		
 		regSaveButton = new NativeButton("Start registration mode");
 		regSaveButton.addStyleName("reg-settings");
 		regSaveButton.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				StartRegModeBttnClicked(event);
 			}	
 		});
 		regSettingsGridLayout.addComponent(regSaveButton, 1, 5);
 		regSettingsGridLayout.setComponentAlignment(regSettingsGridLayout.getComponent(1, 5), Alignment.MIDDLE_RIGHT);
 		
 
 		
 		/************************************************************************************************************************/
 		 
 		 
 		settingsTab.addTab(globalSettingsLayout, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL));
 		settingsTab.addTab(userSettingsLayout, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER));
 		//settingsTab.addTab(groupSettingsLayout, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP));
 		//settingsTab.addTab(updateSettingsLayout, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_UPDATE));
 		settingsTab.addTab(registerSettingsLayout, "Registration");		
 		hLayout.addComponent(settingsTab);
 		hLayout.setComponentAlignment(settingsTab, Alignment.TOP_CENTER);
 		setContent(hLayout);
 		
 		this.Update();
 	}
 	
 	public void Activate(){
 		app.headerScreen.setOptionsButtonActive();
 	}
 	
 	public void Update(){
 		String str;
 		
 		Config cfg =  new Config();
 		cfg.LoadSettings();
 		
 		settingsTab.getTab(globalSettingsLayout).setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL));
 		settingsTab.getTab(userSettingsLayout).setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER));
 		//settingsTab.getTab(groupSettingsLayout).setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP));
 		//settingsTab.getTab(updateSettingsLayout).setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_UPDATE));
 		settingsTab.getTab(registerSettingsLayout).setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG));
 		
 		// update Global settings
 		
 		//database settings
 		databaseSettingsLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_SETTINGS));
 		deleteDataBttn.setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE));
 		deleteDataBeforeBttn.setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_BEFORE));
 		
 		
 		//language settings
 		//languageSettingsLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_LANG_SETTINGS));
 		
 		//rtc settings
 		clockSettingsLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_CLOCK_SETTINGS));
 		LBusReceiver lbus = new LBusReceiver(app.config.getLbusServerAddress(), app.config.getLbusServerPort(), "rtc", "state");
 		
 		String rtcState = lbus.get();
 		rtcStateTxt.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_CLOCK_STATE) + ": " + rtcState);
 		if(rtcState.equals("not synced")){
 			dateField.setVisible(true);
 			setDatetimeBttn.setVisible(true);
 		}
 		
 		//network settings
 		str = app.sql.getNetType();
 		if(str.equals("dhcp") || str.equals("DHCP")){
 			useDhcpCB.setValue(true);
 			netmaskCTx.setEnabled(false);
 			ipAddressCTx.setEnabled(false);
 			gwAddressCTx.setEnabled(false);
 		}else{
 			useDhcpCB.setValue(false);
 			ipAddressCTx.setEnabled(true);
 			netmaskCTx.setEnabled(true);
 			gwAddressCTx.setEnabled(true);
 
 			ipAddressCTx.setValue(app.sql.getNetAddress());
 			netmaskCTx.setValue(app.sql.getNetMask());
 			gwAddressCTx.setValue(app.sql.getNetGateway());
 		}
 
 		useDhcpDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_TYPE));
 		ipAddressDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_IP));
 		netmaskDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_NETMASK));
 		gwAddressDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_GATEWAY));
 		saveIPBttn.setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SAVE));
 		
 		/*gwPrefixCTx.setValue(cfg.getSixLowPanPrefix());
 		gwIPCTx.setValue(cfg.getSixLowPanGatewayIP());
 		gwUARTCTx.setValue(cfg.getSixLowPanUART());
 		gwBaudrateCTx.setValue(cfg.getSixLowPanBaudrate());
 		gwTunnelCTx.setValue(cfg.getSixLowPanTunnel());
 		*/
 		
 		// user settings 
 		usersTable.setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_NAME));
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_USER, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_USER));
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_GROUP, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_GROUP));
 		usersTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_TIMEZONE, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_TABLE_COL_TIMEZONE));
 		
 		// 6LowPan settings
 		//slpPanIdCTx.setValue(app.sql.getPanid());
 		
 		FillUsersTable();
 		
 		optionsUserDetailsScreen.Update();
 		
 		// group settings
 		/*groupsTable.setCaption(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_NAME));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_GROUP, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_GROUP));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DEL_DEVICES, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DEL_DEVICES));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_REG_DEVICES, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_REG_DEVICES));
 		groupsTable.setColumnHeader(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DOWNLOAD, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_TABLE_COL_DOWNLOAD));
 		
 		groupDetailHeaderLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GROUP_DETAILS_CAPTION));*/
 		
 		/*regCaptionLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_CAPTION));
 		regMIntDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_MINT_LBL));
 		regPIntDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_PINT_LBL));
 		regAIntDLbl.setValue(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_REG_AINT_LBL));
 		
 		regPIntCSel.removeAllItems();
 		regPIntCSel.addItem(app.langHelper.GetString(LanguageHelper.AT_PINT_OPT_LIFETIME));
 		regPIntCSel.addItem(app.langHelper.GetString(LanguageHelper.AT_PINT_OPT_NORMAL));
 		regPIntCSel.addItem(app.langHelper.GetString(LanguageHelper.AT_PINT_OPT_FAST_GATHER));
 		regPIntCSel.select(app.langHelper.GetString(LanguageHelper.AT_PINT_OPT_NORMAL));
 		
 		regMIntCTx.setValue(app.sql.getRegMInt().toString());
 		regPIntCSel.select(app.langHelper.GetString(Device.getProcessIntervallString(app.sql.getRegPInt())));
 		regAIntCTx.setValue(app.sql.getRegAInt().toString());
 		regTimeCTx.setValue(app.sql.getRegTime().toString());*/
 		
 	}
 	
 
 	private void FillUsersTable(){
 		usersTable.removeAllItems();
 		User user = app.userContainer.firstItemId();
 		for(int i = 0; i < app.userContainer.size(); i++){
 			usersTable.addItem(new Object[]{user.getLogin(), "", user.getTimezone().toString()}, user.getID());
 			user = app.userContainer.nextItemId(user);
 		}
 	}
 	
 	private void UseTimeServerCBClickHandler(ValueChangeEvent event){
 		dateField.setEnabled(!((Boolean) event.getProperty().getValue()));
 		
 	}
 	
 	private void UsersTableClickedHandler(ValueChangeEvent event){
 		User user = null;
 		
 		if( usersTable.getValue() == null){
 			optionsUserDetailsScreen.setUser(null);
 			optionsUserDetailsScreen.Update();
 		}else{
 			int id = Integer.parseInt(event.getProperty().getValue().toString());
 			
 			for( int i = 0; i< app.userContainer.size() && user == null; i++){
 				user = app.userContainer.getIdByIndex(i);
 				if(user.getID() != id){
 					user = null;
 				}
 			}
 			
 			if(user != null){
 				optionsUserDetailsScreen.setUser(user);
 				optionsUserDetailsScreen.Update();
 			}
 		}
 	}
 	
 	public void SelectUsersTableRow(int row){
 		usersTable.select(row);
 	}
 	
 	private void DeleteDataBttnClicked(ClickEvent event){
 		YesNoPopupScreen ackPopup =  new YesNoPopupScreen(app, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_POP_HEADER), 
 				app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_POP_TEXT));
 		
 		ackPopup.addListener(new PopupClosedListener(){
 			@Override
 			public void popupClosedEvent(YesNoPopupScreen e) {
 				if(e.isYesClicked()){
 					if(DeviceContainer.sql.DeleteData()){
 						Notification.show("Database successfully deleted");
 					}else{
 						Notification.show("Error: Couldn't delete database", Notification.Type.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 	}
 
 	private void DeleteDataBeforeBttnClicked(ClickEvent event){
 
 		YesNoPopupScreen ackPopup =  new YesNoPopupScreen(app, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_POP_HEADER), 
 				app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_DB_DELETE_POP_TEXT));
 		
 		ackPopup.addListener(new PopupClosedListener(){
 			@Override
 			public void popupClosedEvent(YesNoPopupScreen e) {
 				if(e.isYesClicked()){
 					
 					Date date = (Date) deleteBeforedateField.getValue();
 					//convert date to UTC
 					Calendar cal = Calendar.getInstance();
 					TimeZone tz = TimeZone.getTimeZone("UTC");
 					cal.setTimeZone(tz);
 					cal.setTime(date);
 					cal.add(Calendar.HOUR_OF_DAY, -app.user.getTimezone());
 					
 					//convert date to SQL date
 					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 					String dt = sdf.format(cal.getTime());
 					app.sql.DeleteDataBefore(app, dt);
 					
 					/*if(DeviceContainer.sql.DeleteData()){
 						Notification.show("Database successfully deleted");
 					}else{
 						Notification.show("Error: Couldn't delete database", Notification.Type.ERROR_MESSAGE);
 					}*/
 				}
 			}
 		});
 
 
 
 	}
 	private void SaveIPBttnClicked(ClickEvent event){
 		YesNoPopupScreen ackPopup = new YesNoPopupScreen(app, app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SAVE_POP_HEADER),
 				app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SAVE_POP_TEXT));
 		ackPopup.addListener(new PopupClosedListener(){
 			@Override
 			public void popupClosedEvent(YesNoPopupScreen e) {
 				if(e.isYesClicked()){
 					LBusSender lbus = new LBusSender(app.config.getLbusServerAddress(), app.config.getLbusServerPort(), "lbus");
 					lbus.setCmd(lbus.LBUS_CMD_CHG);
 					lbus.setUser(app.user.getID());
 					lbus.setAddress("liegw");
 					
 					if(useDhcpCB.getValue()){
 						app.sql.setNetType("dhcp");
 						app.sql.setNetAddress("");
 						app.sql.setNetMask("");
 						app.sql.setNetGateway("");
 						
 						ipAddressCTx.setValue("");
 						netmaskCTx.setValue("");
 						gwAddressCTx.setValue("");
 						
 						lbus.setPayload("\"net_type\":\"dhcp\"");
 					}else{
 						app.sql.setNetType("static");
 						app.sql.setNetAddress(ipAddressCTx.getValue());
 						app.sql.setNetMask(netmaskCTx.getValue());
 						app.sql.setNetGateway(gwAddressCTx.getValue());
 						lbus.setPayload("\"net_type\":\"static\"\n");
 					}
 					
 
 					lbus.send();
					
					getUI().getPage().setLocation("lieweb");
					getUI().getSession().close();
 
 					Notification.show(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_GLOBAL_NETWORK_SAVE_SUCCESS));
 				}
 				app.Update();
 			}
 		});
 	}
 	
 	private void DeleteDatabaseBttnClicked(ClickEvent event){
 		
 		/*if(DeviceContainer.sql != null){
 			DeviceContainer.sql.CreateTestData(app.deviceContainer, 100000);
 		}*/
 
 		if(DeviceContainer.sql.DeleteDatabase()){
 			Notification.show("Database successfully deleted");
 		}else{
 			Notification.show("Error: Couldn't delete database", Notification.Type.ERROR_MESSAGE);
 		}
 	}
 	
 	private void StartRegModeBttnClicked(ClickEvent event) {
 	}
 	
 	private void CheckRunmode(){
 		Integer newRunmode = 1;
 		// get runmode
 		
 		//check if reg mode
 		if(newRunmode == RUNMODE_REG){
 		
 		}else if(newRunmode != runmode){
 			if(newRunmode == RUNMODE_REG){
 				//change to registration mode
 				
 			}else{
 				//change back to normal mode
 				
 			}
 		}		
 	}
 	
 	private void useDhcpCBclicked(ValueChangeEvent event){
 		if(useDhcpCB.getValue()){
 			netmaskCTx.setEnabled(false);
 			ipAddressCTx.setEnabled(false);
 			gwAddressCTx.setEnabled(false);
 		}else{
 			netmaskCTx.setEnabled(true);
 			ipAddressCTx.setEnabled(true);
 			gwAddressCTx.setEnabled(true);
 		}
 	}
 	
 	private void SetDateTimeBttnClicked(){
 		Date date = (Date) dateField.getValue();
 		Calendar cal = Calendar.getInstance();
 		TimeZone tz = TimeZone.getTimeZone("UTC");
 		cal.setTimeZone(tz);
 		cal.setTime(date);
 		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String dt = sdf.format(cal.getTime());
 		
 		LBusSender lbus = new LBusSender(app.config.getLbusServerAddress(), app.config.getLbusServerPort(), "lbus");
 		lbus.setCmd(lbus.LBUS_CMD_CHG);
 		lbus.setUser(app.user.getID());
 		lbus.setAddress("liegw");
 		lbus.setPayload("\"rtc\":\"" + dt + "\"\n");
 		lbus.send();
 		
 	}
 }
 
