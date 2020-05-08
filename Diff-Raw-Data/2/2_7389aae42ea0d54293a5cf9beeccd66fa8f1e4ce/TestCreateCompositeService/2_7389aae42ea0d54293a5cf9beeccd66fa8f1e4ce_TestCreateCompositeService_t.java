 package org.eclipse.swordfish.tooling.target.platform.test;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.swordfish.tooling.test.util.SWTBotCommonActionsSupport;
 import org.eclipse.swordfish.tooling.test.util.Util;
 import org.eclipse.swordfish.tooling.test.util.project.ProjectCompare;
 import org.eclipse.swtbot.eclipse.finder.SWTBotEclipseTestCase;
 
 public class TestCreateCompositeService extends SWTBotEclipseTestCase {
 
 	private static final String FLIGHT_RESERVATION_WSDL = "FlightReservation.wsdl";
 	private static final String PAYMENT_PROCESSING_WSDL = "PaymentProcessing.wsdl";
 	private static final String FLIGHT_BOOKING_WSDL = "FlightBooking.wsdl";
	private static final String PROJECT_WITH_WSDL = "composite.project.with.wsdl";
 
 	private static final String FLIGHT_RESERVATION_CONSUMER_PROJECT_NAME = "jaxws.flight.reservation.consumer";
 	private static final String PAYMENT_PROCESSING_CONSUMER_PROJECT_NAME = "jaxws.payment.processing.consumer";
 	private static final String FLIGHT_BOOKING_SERVICE_PROJECT_NAME = "jaxws.flight.booking.service";
 	private static final String COMPOSITE_FLIGHT_BOOKING_SERVICE_PROJECT_NAME = "composite.flight.booking.service";
 
 	public void testEmptyConsumerProjects() throws Exception {
 		createWSDLProject();
 		openJAXWSServiceWizard();
 		
 		assertFalse(bot.button(TestConstants.BUTTON_NEXT).isEnabled());
 		assertFalse(bot.button(TestConstants.BUTTON_FINISH).isEnabled());
 		bot.textWithLabel("Project name:").setText(FLIGHT_BOOKING_SERVICE_PROJECT_NAME);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT),
 				TestConstants.TIMEOUT_FOR_ENABLE);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertTrue(bot.checkBoxInGroup("Options", 0).isChecked());
 		bot.checkBoxInGroup("Options", 0).deselect();
 		assertFalse(bot.checkBoxInGroup("Options", 0).isChecked());
 		assertFalse(bot.checkBoxInGroup("Options", 1).isEnabled());
 		assertFalse(bot.checkBoxInGroup("Options", 2).isChecked());
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(FLIGHT_BOOKING_WSDL).select()
 				.click();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertNotNull(bot.table());
 		assertEquals(0, bot.table().rowCount());
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot,
 				"New plug-in project with custom templates",
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK,
 				TestConstants.TIMEOUT_FOR_REFRESHING);		
 	}
 
 	public void testCreateCompositeServiceFromWSDL() throws Exception {
 		openJAXWSConsumerWizard();
 		createJAXWSConsumerWithWizard(FLIGHT_RESERVATION_CONSUMER_PROJECT_NAME,
 				FLIGHT_RESERVATION_WSDL);
 
 		openJAXWSConsumerWizard();
 		createJAXWSConsumerWithWizard(PAYMENT_PROCESSING_CONSUMER_PROJECT_NAME,
 				PAYMENT_PROCESSING_WSDL);
 
 		openJAXWSServiceWizard();
 		
 		assertFalse(bot.button(TestConstants.BUTTON_NEXT).isEnabled());
 		assertFalse(bot.button(TestConstants.BUTTON_FINISH).isEnabled());
 		bot.textWithLabel("Project name:").setText(COMPOSITE_FLIGHT_BOOKING_SERVICE_PROJECT_NAME);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT),
 				TestConstants.TIMEOUT_FOR_ENABLE);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertTrue(bot.checkBoxInGroup("Options", 0).isChecked());
 		bot.checkBoxInGroup("Options", 0).deselect();
 		assertFalse(bot.checkBoxInGroup("Options", 0).isChecked());
 		assertFalse(bot.checkBoxInGroup("Options", 1).isEnabled());
 		assertFalse(bot.checkBoxInGroup("Options", 2).isChecked());
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(FLIGHT_BOOKING_WSDL).select()
 				.click();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertNotNull(bot.table().getTableItem(0));
 		assertEquals(bot.table().getTableItem(0).getText(),
 				FLIGHT_RESERVATION_CONSUMER_PROJECT_NAME);
 		assertFalse(bot.table().getTableItem(FLIGHT_RESERVATION_CONSUMER_PROJECT_NAME).isChecked());
 		bot.table().getTableItem(FLIGHT_RESERVATION_CONSUMER_PROJECT_NAME).check();
 		assertNotNull(bot.table().getTableItem(1));
 		assertEquals(bot.table().getTableItem(1).getText(),
 				PAYMENT_PROCESSING_CONSUMER_PROJECT_NAME);
 		assertFalse(bot.table().getTableItem(PAYMENT_PROCESSING_CONSUMER_PROJECT_NAME).isChecked());
 		bot.table().getTableItem(PAYMENT_PROCESSING_CONSUMER_PROJECT_NAME).check();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT),
 				TestConstants.TIMEOUT_FOR_ENABLE);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot,
 				"New plug-in project with custom templates",
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK,
 				TestConstants.TIMEOUT_FOR_REFRESHING);		
 		
 		ProjectCompare comparator = new ProjectCompare();
 		String compareResult = comparator.compare(COMPOSITE_FLIGHT_BOOKING_SERVICE_PROJECT_NAME);
 		assertEquals(ProjectCompare.OK_RESULT, compareResult);
 	}
 	
 	private void createWSDLProject() throws Exception {
 		SWTBotCommonActionsSupport.closeWelcomeViewIfOpened(bot);
 
 		Util.IDE.getShowViewMenu(bot).menu(TestConstants.MENU_ITEM_OTHER)
 				.click();
 		bot.tree().getTreeItem("Java").select();
 		bot.tree().getTreeItem("Java").doubleClick();
 		bot.tree().getTreeItem("Java").select().expand().getNode(
 				TestConstants.VIEW_PACKAGE_EXPLORER).select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_OK),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_OK).click();
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 
 		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(
 				PROJECT_WITH_WSDL).exists()) {
 			SWTBotCommonActionsSupport.setTextEditorForWSDL(bot);
 			SWTBotCommonActionsSupport
 					.createTestProject(bot, PROJECT_WITH_WSDL);
 			SWTBotCommonActionsSupport.createTestWSDLFile(bot,
 					PROJECT_WITH_WSDL, FLIGHT_RESERVATION_WSDL);
 			SWTBotCommonActionsSupport.createTestWSDLFile(bot,
 					PROJECT_WITH_WSDL, PAYMENT_PROCESSING_WSDL);
 			SWTBotCommonActionsSupport.createTestWSDLFile(bot,
 					PROJECT_WITH_WSDL, FLIGHT_BOOKING_WSDL);
 		}
 	}
 	
 	private void openJAXWSConsumerWizard() throws InterruptedException {
 		openWizard("JAX-WS Service Consumer from WSDL");
 	}
 
 	private void openJAXWSServiceWizard() throws InterruptedException {
 		openWizard("JAX-WS Service Provider from WSDL");
 	}
 
 	private void openWizard(String text) throws InterruptedException {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu(TestConstants.MENU_ITEM_FILE)
 				.menu(TestConstants.MENU_ITEM_NEW).menu(
 						TestConstants.MENU_ITEM_OTHER).click();
 		Util.waitForWindowToAppear(bot, TestConstants.MENU_ITEM_NEW,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode("Swordfish").expandNode(text).select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 	}
 	
 	private void createJAXWSConsumerWithWizard(String projectName,
 			String wsdlFileName) throws Exception {
 		assertFalse(bot.button(TestConstants.BUTTON_NEXT).isEnabled());
 		assertFalse(bot.button(TestConstants.BUTTON_FINISH).isEnabled());
 		bot.textWithLabel("Project name:").setText(projectName);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT),
 				TestConstants.TIMEOUT_FOR_ENABLE);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertTrue(bot.checkBoxInGroup("Options", 0).isChecked());
 		bot.checkBoxInGroup("Options", 0).deselect();
 		assertFalse(bot.checkBoxInGroup("Options", 0).isChecked());
 		assertFalse(bot.checkBoxInGroup("Options", 1).isEnabled());
 		assertFalse(bot.checkBoxInGroup("Options", 2).isChecked());
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		
 		Util.waitForWindowToAppear(bot,
 				TestConstants.WINDOW_NEW_PLUGIN_PROJECT,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(wsdlFileName).select()
 				.click();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH),
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		assertTrue(bot.radioInGroup("Provider endpoint", 0).isSelected());
 		assertTrue(bot.checkBox("Generate example consumer code").isChecked());
 		bot.checkBox("Generate example consumer code").deselect();
 		assertFalse(bot.checkBox("Generate example consumer code").isChecked());
 		
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot,
 				"New plug-in project with custom templates",
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 	}
 	
 }
