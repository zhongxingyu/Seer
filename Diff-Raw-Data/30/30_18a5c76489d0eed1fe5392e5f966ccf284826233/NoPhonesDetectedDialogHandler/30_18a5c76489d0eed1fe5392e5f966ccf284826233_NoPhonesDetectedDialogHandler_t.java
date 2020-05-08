 /**
  * 
  */
 package net.frontlinesms.ui.handler.phones;
 
 import java.awt.Image;
 
 import net.frontlinesms.AppProperties;
 import net.frontlinesms.Utils;
 import net.frontlinesms.smsdevice.events.NoSmsDevicesConnectedNotification;
 import net.frontlinesms.ui.DatabaseSettings;
 import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.i18n.TextResourceKeyOwner;
 
 import org.apache.log4j.Logger;
 
 /**
  * Dialog handler for helping a user when no SMS devices could be detected
  * @author Morgan Belkadi <morgan@frontlinesms.com>
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 @TextResourceKeyOwner(prefix="I18N_")
 public class NoPhonesDetectedDialogHandler implements ThinletUiEventHandler {
 //> STATIC CONSTANTS
 	private static final String COMPONENT_NO_DEVICES_PARENT_PANEL = "pnNoDevices";
 	private static final String COMPONENT_NO_DEVICES_CAUSE_PANEL = "pnNoDevices_cause";
 	private static final String COMPONENT_NO_DEVICES_TRY_PANEL = "pnNoDevices_try";
 	private static final String I18N_NO_DEVICES_CAUSE = "device.connection.nothing.cause";
 	private static final String I18N_NO_DEVICES_TRY = "device.connection.nothing.try";
 
 	private static final String COMPONENT_INCOMPATIBLE_PARENT_PANEL = "pnFailed";
 	private static final String COMPONENT_INCOMPATIBLE_CAUSE_PANEL = "pnFailed_cause";
 	private static final String COMPONENT_INCOMPATIBLE_TRY_PANEL = "pnFailed_try";
 	private static final String I18N_INCOMPATIBLE_CAUSE = "device.connection.failed.cause";
 	private static final String I18N_INCOMPATIBLE_TRY = "device.connection.failed.try";
 
 	private static final String COMPONENT_OWNED_PARENT_PANEL = "pnOwned";
 	private static final String COMPONENT_OWNED_CAUSE_PANEL = "pnOwned_cause";
 	private static final String COMPONENT_OWNED_TRY_PANEL = "pnOwned_try";
 	private static final String I18N_OWNED_CAUSE = "device.connection.owned.cause";
 	private static final String I18N_OWNED_TRY = "device.connection.owned.try";
 	
 //> THINLET LAYOUT DEFINITION FILES
 	/** UI XML File Path: This is the outline for the dialog */
 	private static final String UI_FILE_DEVICE_CONNECTION_DIALOG = "/ui/core/phones/dgNoneDetected.xml";
 //> THINLET COMPONENT NAMES
 
 
 	
 //> INSTANCE PROPERTIES
 	/** Logging object */
 	private final Logger log = Utils.getLogger(this.getClass());
 	/** The {@link UiGeneratorController} that shows the tab. */
 	private final UiGeneratorController uiController;
 	
 	private Object dialogComponent;
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new instance of this controller.
 	 * @param uiController 
 	 * @param notification the notification which triggered this dialog; used to determine what to display in the dialog
 	 */
 	public NoPhonesDetectedDialogHandler(UiGeneratorController uiController) {
 		this.uiController = uiController;
 	}
 	
 //> ACCESSORS
 
 //> UI SHOW METHODS
 	public Object getDialog() {
 		return this.dialogComponent;
 	}
 	
 	/**
 	 * Setup the details of the dialog.
 	 */
 	public void initDialog(NoSmsDevicesConnectedNotification notification) {
 		this.dialogComponent = uiController.loadComponentFromFile(UI_FILE_DEVICE_CONNECTION_DIALOG, this);
 		this.populatePanel(notification);
 	}
 
 	/** Populate the panel containing settings specific to the currently-selected {@link DatabaseSettings}. */
 	private void populatePanel(NoSmsDevicesConnectedNotification notification) {
 		// Populate the dialog depending on the content of the notification
 		// if there were NO DEVICES detected, show the NO DEVICES panel
 		setVisible(COMPONENT_NO_DEVICES_PARENT_PANEL, notification.isNoDevices(),
 				COMPONENT_NO_DEVICES_CAUSE_PANEL, I18N_NO_DEVICES_CAUSE, "/icons/phone_send.png",
 				COMPONENT_NO_DEVICES_TRY_PANEL, I18N_NO_DEVICES_TRY, "");
 
 		// if there were INCOMPATIBLE devices detected, show the INCOMPATIBLE devices panel
 		setVisible(COMPONENT_INCOMPATIBLE_PARENT_PANEL, notification.isIncompatibleDevicesDetected(),
 				COMPONENT_INCOMPATIBLE_CAUSE_PANEL, I18N_INCOMPATIBLE_CAUSE, "/icons/phone_send.png",
 				COMPONENT_INCOMPATIBLE_TRY_PANEL, I18N_INCOMPATIBLE_TRY, "");
 
 		// if there were OWNED devices detected, show the OWNED devices panel
 		setVisible(COMPONENT_OWNED_PARENT_PANEL, notification.isOwnedPortsDetected(),
 				COMPONENT_OWNED_CAUSE_PANEL, I18N_OWNED_CAUSE, "/icons/phone_send.png",
 				COMPONENT_OWNED_TRY_PANEL, I18N_OWNED_TRY, "");
 	}
 	
 	/**
 	 * Sets a panel to visible and populates it with a variable amount of text, or hides the panel.
 	 * @param parentPanelName Name of the parent panel that will display the text
 	 * @param panelNamesAndI18nKeysAndIcons Triplets of panel names and i18n base keys of the strings that will populate them and the icon to be applied to the first label
 	 */
 	private void setVisible(String parentPanelName, boolean visible, String... panelNamesAndI18nKeysAndIcons) {
 		uiController.setVisible(find(parentPanelName), visible);
 		if(visible) {
 			for (int i = 0; i < panelNamesAndI18nKeysAndIcons.length; i+=3) {
 				String panelName = panelNamesAndI18nKeysAndIcons[i];
 				Object panel = find(panelName);
 				
 				String i18nKey = panelNamesAndI18nKeysAndIcons[i+1];
 				
 				String iconPath = panelNamesAndI18nKeysAndIcons[i+2];
 				Image icon = uiController.getIcon(iconPath);
 				
 				for(String i18nText : InternationalisationUtils.getI18nStrings(i18nKey)) {
 					Object label = uiController.createLabel(i18nText);
 					if(icon != null) {
 						// If an icon is available, add it to the first label 
 						uiController.setIcon(label, icon);
 						icon = null;
 					}
 					uiController.add(panel, label);
 				}
 			}
 		}
 	}
 	
 	private Object find(String componentName) {
 		return uiController.find(this.dialogComponent, componentName);
 	}
 
 //> UI EVENT METHODS
 	
 	/**
 	 * This method is called when the checkbox is ticked or unticked
 	 */
 	public void manageAlwaysShow(boolean shouldAlwaysShow) {
 		AppProperties appProperties = AppProperties.getInstance();
 		appProperties.setDeviceConnectionDialogEnabled(shouldAlwaysShow);
 		appProperties.saveToDisk();
 	}
 	
 	/** @param dialog the dialog to remove
 	 * @see UiGeneratorController#remove(Object) */
 	public void removeDialog(Object dialog) {
		this.uiController.closeDeviceConnectionDialog(dialog);
 	}
 	
 	/**
 	 * This method is called when the retry button is pressed in the device connection dialog
 	 */
 	public void retryDeviceConnection() {
 		this.uiController.autodetectModems();
 		this.removeDialog(dialogComponent);
 	}
 	
 //> UI PASS-THRU METHODS
 	/**
 	 * Opens a page of the help manual
 	 * @see FrontlineUI#showHelpPage(String)
 	 */
 	public void showHelpPage(String page) {
 		uiController.showHelpPage(page);
 	}
 	
 
 }
