 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.target.platform.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.swordfish.tooling.test.util.Util;
 import org.eclipse.swtbot.eclipse.finder.SWTBotEclipseTestCase;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 
 /**
  * @author etatarintseva
  */
 public class TestRunProvider extends SWTBotEclipseTestCase {
 	private static Logger LOG = Logger.getLogger("TestRunProvider");
 	private static final String REQUEST_BODY_HELLO_WORLD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
 			+ "<soap:Body><ns2:sayHi xmlns:ns2=\"http://sampletest\">\n"
 			+ "<text>Fish</text></ns2:sayHi></soap:Body></soap:Envelope>\n";
 
 	private static final String REQUEST_BODY_BOOKING_SERVICE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
 			+ "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n"
 			+ "<soap:Header></soap:Header><soap:Body><ns2:createReservation \n"
 			+ "xmlns:ns2=\"http://cxf.samples.swordfish.eclipse.org/\">\n"
 			+ "<passengers><age>24</age><firstName>Volodymyr</firstName><id>1</id>\n" + "<lastName>Zhabiuk</lastName>\n"
 			+ "</passengers><flight><flightNumber>LC023</flightNumber><id>1</id>\n"
 			+ "</flight></ns2:createReservation></soap:Body></soap:Envelope>\n";
 
 	private static final String PROJECT_NAME = "jaxws.flight.booking.consumer";
 	private static final String TEMPLATE_NAME = "JAX-WS Service Consumer from WSDL";
 	private static final String FLIGHT_SERVICE_WSDL = "FlightBooking.wsdl";
 	private static final String BOOKING_SERVICE_WSDL = "BookingService.wsdl";
 	private static final String LIBRARY_SERVICE_WSDL = "Library.wsdl";
 	private static final String PROJECT_WITH_WSDL = "project.with.wsdl";
 	private static final String BOOKING_SERVICE_INVOKER_JAVA = "BookingServiceClientInvoker.java";
 	private static final String LIBRARY_SERVICE_INVOKER_JAVA = "LibraryClientInvoker.java";
 	private static final String LIBRARY_WSDL = "Library.wsdl";
 	private static final String JAXWS_CONSUMER_XML = "jaxws-consumer.xml";
 	private static final String PROJECT_FROM_SR = "jaxws.service.consumer.from.sr";
 
 	private StringBuilder failures;
 	
 	@Override
 	public void setUp() throws Exception {
 		failures = new StringBuilder();
 	}
 	
 	/**
 	 * Try to upload all needed WSDL
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 
 	public void testUploadWSDLToSR() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 
 		Util.editTestWSDLFile(bot, PROJECT_WITH_WSDL, LIBRARY_WSDL);
 
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.viewByTitle(TestConstants.VIEW_PACKAGE_EXPLORER).show();
 		bot.viewByTitle(TestConstants.VIEW_PACKAGE_EXPLORER).setFocus();
 
 		uploadWSDLtoSR(FLIGHT_SERVICE_WSDL);
 		uploadWSDLtoSR(BOOKING_SERVICE_WSDL);
 		uploadWSDLtoSR(LIBRARY_WSDL);
 	}
 
 	private void uploadWSDLtoSR(String fileName) throws Exception {
 		bot.tree().select(0);
 		SWTBotTreeItem parentItem = bot.tree().getTreeItem(PROJECT_WITH_WSDL).select().expand();
 		SWTBotTreeItem childItem = parentItem.getNode(fileName).select();
 		// Thread.sleep(50000);
 		childItem.contextMenu("Upload to Swordfish Registry").click();
 		Util.waitForWindowToDisappear(bot, "Service Registry Upload", TestConstants.TIMEOUT_REMOTE);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 	}
 
 	/**
 	 * Try to create consumer for Flight service
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 
 	public void testCreateConsumerFromTemplate() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu(TestConstants.MENU_ITEM_FILE).menu(TestConstants.MENU_ITEM_NEW).menu(TestConstants.MENU_ITEM_PROJECT).click();
 		Util.waitForWindowToAppear(bot, "New", TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().select("Plug-in Project");
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.textWithLabel("Project name:").setText(PROJECT_NAME);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		try {
 			bot.table(0).select(TEMPLATE_NAME);
 		} catch (IllegalArgumentException e) {
 			fail("Cannot find template for the project");
 		}
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(FLIGHT_SERVICE_WSDL).select().click();
 		bot.radio("Use dynamic endpoint lookup").click();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot, "New plug-in project with custom templates", TestConstants.TIMEOUT_FOR_REFRESHING);
 
 	}
 
 	/**
 	 * Try to create consumer for Booking service
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 	public void testCreateBookingServiceConsumerFromWizard() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu(TestConstants.MENU_ITEM_FILE).menu(TestConstants.MENU_ITEM_NEW).menu(TestConstants.MENU_ITEM_OTHER).click();
 		Util.waitForWindowToAppear(bot, TestConstants.MENU_ITEM_NEW, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode("Swordfish").expandNode("JAX-WS Service Consumer from WSDL").select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.textWithLabel("Project name:").setText(TestConstants.PROJECT_BOOKING_CONSUMER_NAME);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(BOOKING_SERVICE_WSDL).select().click();
 		bot.radio("Use dynamic endpoint lookup").click();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot, "New plug-in project with custom templates", TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		bot.tree().expandNode(TestConstants.PROJECT_BOOKING_CONSUMER_NAME).expandNode("src").doubleClick();
 		bot.tree().expandNode(TestConstants.PROJECT_BOOKING_CONSUMER_NAME).expandNode("src").expandNode(
 				"org.eclipse.swordfish.samples.cxf.sample").getNode(BOOKING_SERVICE_INVOKER_JAVA).doubleClick();
 		boolean editorIsActive = false;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Select All").click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Delete").click();
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).toTextEditor().getStyledText().setText(
 				getClientInvoker("BookingServiceClientInvoker.txt"));
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).save();
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).close();
 	}
 
 	public void testCreateBookingServiceConsumerWithStaticEndpointFromWizard() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu(TestConstants.MENU_ITEM_FILE).menu(TestConstants.MENU_ITEM_NEW).menu(TestConstants.MENU_ITEM_OTHER).click();
 		Util.waitForWindowToAppear(bot, TestConstants.MENU_ITEM_NEW, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode("Swordfish").expandNode("JAX-WS Service Consumer from WSDL").select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.textWithLabel("Project name:").setText(TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		bot.tree().expandNode(PROJECT_WITH_WSDL).getNode(BOOKING_SERVICE_WSDL).select().click();
 
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot, "New plug-in project with custom templates", TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		bot.tree().expandNode(TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME).expandNode("src").expandNode(
 				"org.eclipse.swordfish.samples.cxf.sample").getNode(BOOKING_SERVICE_INVOKER_JAVA).doubleClick();
 		boolean editorIsActive = false;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Select All").click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Delete").click();
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).toTextEditor().getStyledText().setText(
 				getClientInvoker("BookingServiceClientInvoker.txt"));
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).save();
 		bot.editorByTitle(BOOKING_SERVICE_INVOKER_JAVA).close();
 	}
 
 	/**
 	 * Try to create service consumer right-click on wsdl file and click import
 	 * 
 	 * @throws Exception
 	 *             when test is failed
 	 */
 
 	public void testCreateBookingServiceConsumerWithImport() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_REMOTE);
 		bot.viewByTitle(TestConstants.VIEW_PACKAGE_EXPLORER).close();
 		Util.IDE.getShowViewMenu(bot).menu(TestConstants.MENU_ITEM_OTHER).click();
 		bot.tree().getTreeItem("Java").select().expand().getNode(TestConstants.VIEW_PACKAGE_EXPLORER).select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_OK), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_OK).click();
 
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		SWTBotTreeItem parentItem = bot.tree().getTreeItem(PROJECT_WITH_WSDL).select().expand();
 		SWTBotTreeItem childItem = parentItem.getNode(BOOKING_SERVICE_WSDL).select();
 
 		childItem.contextMenu("Import...").click();
 
 		bot.tree().expandNode("Swordfish").getNode("JAX-WS Service Consumer from WSDL").select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		bot.textWithLabel("Project name:").setText(TestConstants.BOOKING_PROJECT_NAME + ".from import");
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_ENABLE);
 		if (!bot.button(TestConstants.BUTTON_NEXT).isEnabled()) {
 			throw new Exception("Incorrect data for creating project");
 		}
 
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		try {
 			Util.waitForWindowToAppear(bot, "Open Associated Perspective?", TestConstants.TIMEOUT_FOR_ENABLE);
 			bot.button(TestConstants.BUTTON_YES).click();
 		} catch (Exception e) {
 			Util.waitForWindowToDisappear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT_CUSTOM,
 							TestConstants.TIMEOUT_FOR_REFRESHING);
 		}
 
 		Util.waitForWindowToDisappear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT_CUSTOM, TestConstants.TIMEOUT_REMOTE);
 	}
 
 	/**
 	 * Try to create service consumer from Library.wsdl located in Swordfish
 	 * Service Registry endpoint resolving type - static http endpoint
 	 */
 	public void testCreateLibraryServiceConsumerWithStaticEndpointFromWizard() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu(TestConstants.MENU_ITEM_FILE).menu(TestConstants.MENU_ITEM_NEW).menu(TestConstants.MENU_ITEM_OTHER).click();
 		Util.waitForWindowToAppear(bot, TestConstants.MENU_ITEM_NEW, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode("Swordfish").expandNode("JAX-WS Service Consumer from WSDL").select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.textWithLabel("Project name:").setText(TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		// pick WSDL from Swordfish Service Regestry
 		bot.radio(TestConstants.RADIO_SERVICE_REGISTRY).click();
 
 		// we have two "Browse..." button here, so we need to use index instead
 		// button label
 		Util.waitForButtonToEnable(bot.button(1), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(1).click();
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_WSDL_FILES_SWORFISH_SERVICE_REGISTRY,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		bot.text().setText("Library");
 
 		Thread.sleep(100);
 
 		bot.button(TestConstants.BUTTON_OK).click();
 
 		Util.waitForWindowToDisappear(bot, TestConstants.WINDOW_WSDL_FILES_SWORFISH_SERVICE_REGISTRY,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 		Util.waitForWindowToDisappear(bot, "New plug-in project with custom templates", TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		bot.tree().expandNode(TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME).expandNode("src").expandNode(
 				"org.sopware.services.demos.library._1.sample").getNode(LIBRARY_SERVICE_INVOKER_JAVA).doubleClick();
 		boolean editorIsActive = false;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Select All").click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Delete").click();
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).toTextEditor().getStyledText().setText(
 				getClientInvoker("LibraryClientInvoker.txt"));
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).save();
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).close();
 	}
 
 	/**
 	 * Try to create service consumer from Library.wsdl located in Swordfish
 	 * Service Registry endpoint resolving type - dynamic
 	 */
 	public void testCreateLibraryServiceConsumerWithImport() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		bot.menu(TestConstants.MENU_ITEM_FILE).menu(TestConstants.MENU_ITEM_NEW).menu(TestConstants.MENU_ITEM_OTHER).click();
 		Util.waitForWindowToAppear(bot, TestConstants.MENU_ITEM_NEW, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.tree().expandNode("Swordfish").expandNode("JAX-WS Service Consumer from WSDL").select();
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_NEXT), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.textWithLabel("Project name:").setText(TestConstants.LIBRARY_PROJECT_CONSUMER_NAME);
 		bot.radio("an OSGi framework:").click();
 		bot.button(TestConstants.BUTTON_NEXT).click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_NEW_PLUGIN_PROJECT, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_NEXT).click();
 
 		// pick WSDL from Swordfish Service Regestry
 		bot.radio(TestConstants.RADIO_SERVICE_REGISTRY).click();
 
 		// we have two "Browse..." button here, so we need to use index instead
 		// button label
 		Util.waitForButtonToEnable(bot.button(1), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(1).click();
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_WSDL_FILES_SWORFISH_SERVICE_REGISTRY,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		bot.text().setText("Library");
 
 		Thread.sleep(100);
 
 		bot.button(TestConstants.BUTTON_OK).click();
 
 		Util.waitForWindowToDisappear(bot, TestConstants.WINDOW_WSDL_FILES_SWORFISH_SERVICE_REGISTRY,
 				TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		bot.radio("Use dynamic endpoint lookup").click();
 
 		Util.waitForButtonToEnable(bot.button(TestConstants.BUTTON_FINISH), TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.button(TestConstants.BUTTON_FINISH).click();
 
 		Util.waitForWindowToDisappear(bot, "New plug-in project with custom templates", TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		bot.tree().expandNode(TestConstants.LIBRARY_PROJECT_CONSUMER_NAME).expandNode("src").doubleClick();
 		bot.tree().expandNode(TestConstants.LIBRARY_PROJECT_CONSUMER_NAME).expandNode("src").expandNode(
 				"org.sopware.services.demos.library._1.sample").getNode(LIBRARY_SERVICE_INVOKER_JAVA).doubleClick();
 		boolean editorIsActive = false;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Select All").click();
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Edit").menu("Delete").click();
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).toTextEditor().getStyledText().setText(
 				getClientInvoker("LibraryClientInvoker.txt"));
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).save();
 		bot.editorByTitle(LIBRARY_SERVICE_INVOKER_JAVA).close();
 	}
 
 	/**
 	 * Try to run tp only with consumer for Booking service
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 	public void testRunBookingServiceConsumer() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 		bot.tree(1).expandNode("Workspace").getNode(TestConstants.PROJECT_BOOKING_CONSUMER_NAME + " (1.0.0.qualifier)").check();
 		bot.text(1).setText("BookingServiceConsumer");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "The reservation was created with id";
 		if (!waitResponse(response)) {
 			addFailure("Consumer did not receive response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(true, "ACTIVE", TestConstants.PROJECT_BOOKING_CONSUMER_NAME);
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * run and test consumer created by
 	 * testCreateBookingServiceConsumerWithStaticEndpointFromWizard test
 	 * 
 	 * @throws Exception
 	 */
 	public void testRunBookingServiceConsumerWithStaticEndpoint() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 		// Thread.sleep(5000);
 		bot.tree(1).expandNode("Workspace").getNode(
 				TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME + " (1.0.0.qualifier)").check();
 		// Thread.sleep(2000);
 		bot.text(1).setText("BookingServiceConsumerWithStaticEndpoint");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "The reservation was created with id";
 		if (!waitResponse(response)) {
 			addFailure("Consumer did not receive response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(false, "ACTIVE", TestConstants.PROJECT_BOOKING_CONSUMER_NAME + "_");
 		verifyPluginStates(true, "ACTIVE", TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME + "_");
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * change url address of the static http endpoint definition to some fake
 	 * value run TP and check that consumer not able to receive response from
 	 * provider
 	 * 
 	 * @throws Exception
 	 */
 	public void testRunBookingServiceConsumerWithInvalidStaticEndpoint() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		SWTBotTreeItem item = bot.tree().expandNode(TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME).expandNode(
 				"META-INF").expandNode("spring");
 		item.getNode(JAXWS_CONSUMER_XML).doubleClick();
 		boolean editorIsActive = false;
 		SWTBotEditor editor = null;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				editor = bot.editorByTitle(JAXWS_CONSUMER_XML);
 				editor.setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		String jaxwsConsumerXmlContent = editor.toTextEditor().getText();
 
 		// check static endpoint definition
 		int physicalAddressIndex = jaxwsConsumerXmlContent.indexOf("http://localhost:8192/cxfsample/");
 		assertFalse("Physical address for static http endpoint wasn't generated", physicalAddressIndex == -1);
 
 		// made endpoint definition invalid
 		jaxwsConsumerXmlContent = jaxwsConsumerXmlContent.replace("http://localhost:8192/cxfsample/",
 				"http://localhost:8192/cxfsample_invalid");
 		editor.toTextEditor().setText(jaxwsConsumerXmlContent);
 		editor.save();
 		editor.close();
 
 		// now we can run consumer and check failed request to provider
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 		// Thread.sleep(5000);
 		bot.tree(1).expandNode("Workspace").getNode(
 				TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME + " (1.0.0.qualifier)").check();
 		// Thread.sleep(2000);
 		bot.text(1).setText("BookingServiceConsumerWithInvalidStaticEndpoint");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "The reservation was created with id";
 		if (waitResponse(response)) {
 			addFailure("Http endpoint definition is invalid and consumer should not receive any response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(false, "ACTIVE", TestConstants.PROJECT_BOOKING_CONSUMER_NAME + "_");
 		verifyPluginStates(true, "ACTIVE", TestConstants.PROJECT_BOOKING_CONSUMER_STATIC_ENDPOINT_NAME + "_");
 		verifyConsoleOutput(true, "Caused by: org.apache.cxf.interceptor.Fault: Could not send Message.", "Send message not failed");
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * Try to run TargetPlatform only with consumer for Library service and test
 	 * dynamic endpoint lookup
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 	public void testRunLibraryServiceConsumer() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 		// Thread.sleep(5000);
 		bot.tree(1).expandNode("Workspace").getNode(TestConstants.LIBRARY_PROJECT_CONSUMER_NAME + " (1.0.0.qualifier)").check();
 		// Thread.sleep(2000);
 		bot.text(1).setText("LibraryServiceConsumer");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "Result of book seeking is";
 		if (!waitResponse(response)) {
 			addFailure("Consumer did not receive response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(true, "ACTIVE", TestConstants.LIBRARY_PROJECT_CONSUMER_NAME);
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * run and test consumer created by
 	 * testCreateBookingServiceConsumerWithStaticEndpointFromWizard test testing
 	 * static http endpoint generation and work
 	 * 
 	 * @throws Exception
 	 */
 	public void testRunLibraryServiceConsumerWithStaticEndpoint() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 
 		bot.tree(1).expandNode("Workspace").getNode(
 				TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME + " (1.0.0.qualifier)").check();
 		bot.text(1).setText("LibraryServiceConsumerWithStaticEndpoint");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "Result of book seeking is";
 		if (!waitResponse(response)) {
 			addFailure("Consumer did not receive response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(false, "ACTIVE", TestConstants.LIBRARY_PROJECT_CONSUMER_NAME + "_");
 		verifyPluginStates(true, "ACTIVE", TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME + "_");
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * change url address of the static http endpoint definition to some fake
 	 * value run TP and check that consumer not able to receive response from
 	 * provider
 	 * 
 	 * @throws Exception
 	 */
 
 	public void testRunLibraryServiceConsumerWithInvalidStaticEndpoint() throws Exception {
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		Util.maximizeView(bot, TestConstants.VIEW_PACKAGE_EXPLORER);
 		bot.tree().select(0);
 		SWTBotTreeItem item = bot.tree().expandNode(TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME).expandNode(
 				"META-INF").expandNode("spring");
 		item.getNode(JAXWS_CONSUMER_XML).doubleClick();
 		boolean editorIsActive = false;
 		SWTBotEditor editor = null;
 		while (!editorIsActive) {
 			try {
 				Thread.sleep(TestConstants.TIMEOUT_FOR_ENABLE);
 				editor = bot.editorByTitle(JAXWS_CONSUMER_XML);
 				editor.setFocus();
 				editorIsActive = true;
 			} catch (Exception e) {
 				editorIsActive = false;
 			}
 		}
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 
 		String jaxwsConsumerXmlContent = editor.toTextEditor().getText();
 
 		// check static endpoint definition
 		int physicalAddressIndex = jaxwsConsumerXmlContent.indexOf("http://localhost:8192/LibraryProvider/");
 		assertFalse("Physical address for static http endpoint wasn't generated", physicalAddressIndex == -1);
 
 		// made endpoint definition invalid
 		jaxwsConsumerXmlContent = jaxwsConsumerXmlContent.replace("http://localhost:8192/LibraryProvider/",
 				"http://localhost:8192/LibraryProvider_invalid");
 		editor.toTextEditor().setText(jaxwsConsumerXmlContent);
 		editor.save();
 		editor.close();
 
 		// now we can run consumer and check failed request to provider
 
 		Util.waitForWindowToAppear(bot, TestConstants.WINDOW_ECLIPSE_SDK, TestConstants.TIMEOUT_FOR_REFRESHING);
 		bot.menu("Run").menu("Run Configurations...").click();
 		bot.tree().select(0);
 		bot.tree().expandNode("OSGi Framework").select();
 		bot.tree().contextMenu("New").click();
 		Util.waitForButtonToEnable(bot.button("Run"), TestConstants.TIMEOUT_REMOTE);
 		bot.tree(1).expandNode("Workspace").uncheck();
 
 		bot.tree(1).expandNode("Workspace").getNode(
 				TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME + " (1.0.0.qualifier)").check();
 		bot.text(1).setText("LibraryServiceConsumerWithInvalidStaticEndpoint");
 		bot.button(TestConstants.BUTTON_APPLY).click();
 
 		bot.button("Run").click();
 		int currentLength = -1;
 		int countIterations = 0;
 
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		bot.toolbarToggleButtonWithTooltip(TestConstants.BUTTON_PIN_CONSOLE).toggle();
 		Util.maximizeView(bot, "Console");
 
 		String response = "The reservation was created with id";
 		if (waitResponse(response)) {
 			addFailure("Http endpoint definition is invalid and consumer should not receive any response." + bot.styledText().getText());
 		}
 
 		bot.styledText().setText("ss" + "\n");
 		currentLength = -1;
 		countIterations = 0;
 		while (currentLength != bot.styledText().getText().length() && countIterations < 3) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			Thread.sleep(TestConstants.TIME_OUT_REFRESH_CONSOLE);
 		}
 
 		verifyPluginStates(false, "ACTIVE", TestConstants.LIBRARY_PROJECT_CONSUMER_NAME + "_");
 		verifyPluginStates(true, "ACTIVE", TestConstants.LIBRARY_PROJECT_CONSUMER_STATIC_ENDPOINT_NAME + "_");
 		verifyConsoleOutput(true, "Caused by: org.apache.cxf.interceptor.Fault: Could not send Message.", "Send message not failed");
 		bot.toolbarButtonWithTooltip("Terminate").click();
 		assertFailures();
 	}
 
 	/**
 	 * Try to run service provider for hello word service
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 	public void testTryToRunServiceProviderHelloWorld() throws Exception {
 		checkIsServiceRun("http://localhost:8197/HelloWorld/", REQUEST_BODY_HELLO_WORLD);
 	}
 
 	/**
 	 * Try to run service provider for booking service
 	 * 
 	 * @throws Exception
 	 *             - in case waiting timed out
 	 */
 
 	public void testTryToRunServiceBookingService() throws Exception {
 		checkIsServiceRun("http://localhost:8192/cxfsample/", REQUEST_BODY_BOOKING_SERVICE);
 	}
 
 	public void testCheckBookingServiceServiceWSDL() throws Exception {
 		checkWSDL("http://localhost:8192/cxfsample/?wsdl", "");
 	}
 
 	public void testCheckFlightBookingWSDL() throws Exception {
 		checkWSDL("http://localhost:8197/FlightBooking/?wsdl", "");
 	}
 
 	private void checkIsServiceRun(String uri, String request) {
 		PostMethod post = new PostMethod(uri);
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		Util.maximizeView(bot, "Console");
 		String consoleContent = bot.styledText().getText();
 		try {
 			RequestEntity entity = new StringRequestEntity(request, "text/xml", "UTF8");
 			post.setRequestEntity(entity);
 			post.setRequestHeader("SOAPAction", "");
 			// Get HTTP client
 			HttpClient httpclient = new HttpClient();
 			LOG.info("PROXY:" + httpclient.getHostConfiguration().getProxyHost()
 							+ httpclient.getHostConfiguration().getProxyPort());
 			int result = httpclient.executeMethod(post);
 			if (result != 200) {
 				fail("Response status code: " + result + post.getResponseBodyAsString() + consoleContent);
 			}
 		} catch (Exception ex) {
 			LOG.log(Level.SEVERE, "Could not send request to address: ", ex);
 			fail("Could not send request to address " + uri + ex.getMessage() + consoleContent);
 		} finally {
 			post.releaseConnection();
 		}
 	}
 
 	private void checkWSDL(String uri, String request) {
 		GetMethod get = new GetMethod(uri);
 		bot.viewByTitle("Console").show();
 		bot.viewByTitle("Console").setFocus();
 		Util.maximizeView(bot, "Console");
 		String consoleContent = bot.styledText().getText();
 		try {
 			HttpClient httpclient = new HttpClient();
 			int result = httpclient.executeMethod(get);
 			if (result != 200) {
 				fail("Response status code: " + result + get.getResponseBodyAsString() + consoleContent);
 			}
 		} catch (Exception ex) {
 			LOG.log(Level.SEVERE, "Could not send request to address: ", ex);
 			fail("Could not send request to address " + uri + ex.getMessage() + consoleContent);
 		} finally {
 			get.releaseConnection();
 		}
 	}
 
 	private String getClientInvoker(String templateName) throws IOException {
 		InputStream url = this.getClass().getResourceAsStream(templateName);
 		String wsdl = IOUtils.toString(url);
 		return wsdl;
 	}
 
 	private boolean waitResponse(String response) throws IOException, InterruptedException {
 		int currentLength = -1;
 		int countIterations = 0;
 		// while writing into console
 		while (currentLength != bot.styledText().getText().length()
 				&& countIterations < TestConstants.COUNT_READING_CONSOLE_WAITINGS) {
 			currentLength = bot.styledText().getText().length();
 			countIterations++;
 			int j = 0;
 			for (j = 0; j < TestConstants.COUNT_ITERATIONS_READING_CONSOLE; j++) {
 				if (bot.styledText().getText().contains(response)) {
 					return true;
 				}
 				Thread.sleep(TestConstants.TIMEOUT_CONSOLE_READ_LINE);
 			}
 		}
 		if (!bot.styledText().getText().contains(response)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private void addFailure(String newFailure) {
 		failures.append(newFailure);
 		failures.append("\n");
 	}
 
 	private void assertFailures() throws Exception {
 		assertEquals("Tests failed", "", failures.toString());
 	}
 	
 	private void verifyPluginStates(boolean mustHaveState, String state, String... pluginNames) {
 		for (String plugin : pluginNames) {
			verifyConsoleOutput(mustHaveState, state + "      " + plugin, "Plugin " + (mustHaveState ? "not " : "") + state);
 		}
 	}
 
 	private void verifyConsoleOutput(boolean mustHave, String matchString, String error) {
 		if (bot.styledText().getText().contains(matchString) && !mustHave) {
 			addFailure(error);
 		}
 
 	}
 }
