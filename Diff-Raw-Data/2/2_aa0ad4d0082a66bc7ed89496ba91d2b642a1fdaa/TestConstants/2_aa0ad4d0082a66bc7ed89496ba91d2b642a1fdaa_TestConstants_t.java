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
 
 /**
  * System tests constants.
  *
  */
 public class TestConstants {
 	/** Target platform name */
 	public static final String TARGET_PLATFORM_NAME = "Swordfish";
 	/** Target platform feature name */
	public static final String TARGET_FEATURE_NAME = "Swordfish Tooling (Incubation)";
 	/** Target platform template name */
 	public static final String TARGET_PLATFORM_TEMPLATE_NAME = "Swordfish";
 	/** Target platform definition name */
 	public static final String TARGET_PLATFORM_DEFINITION_NAME = "Swordfish";
 	/** Label: Button Add */
 	public static final String BUTTON_ADD = "Add...";
 	/** Label: Button Browse */
 	public static final String BUTTON_BROWSE = "Browse...";
 	/** Label: Button Cancel */
 	public static final String BUTTON_CANCEL = "Cancel";
 	/** Label: Button OK */
 	public static final String BUTTON_OK = "OK";
 	/** Label: Button Apply */
 	public static final String BUTTON_APPLY = "Apply";
 	/** Label: Button Yes */
 	public static final String BUTTON_YES = "Yes";
 	/** Label: Button Set Active */
 	public static final String BUTTON_SET_ACTIVE = "Set Active";
 	/** Label: Button Reload */
 	public static final String BUTTON_RELOAD = "Reload...";
 	/** Label: Button Back */
 	public static final String BUTTON_BACK = "< Back";
 	/** Label: Button Next */
 	public static final String BUTTON_NEXT = "Next >";
 	/** Label: Button Finish */
 	public static final String BUTTON_FINISH = "Finish";
 	/** Label: Button Refresh */
 	public static final String BUTTON_REFRESH = "Refresh";
 	/** Label: Button Select All */
 	public static final String BUTTON_SELECT_ALL = "Select All";
 	/** Label: Button Pin Console */
 	public static final String BUTTON_PIN_CONSOLE = "Pin Console";
 	/** Label: Button Show Console When Standard Out Changes */
 	public static final String BUTTON_SHOW_OUT_CONSOLE = "Show Console When Standard Out Changes";
 	/** Label: Button Show Console When Standard Error Changes */
 	public static final String BUTTON_SHOW_ERR_CONSOLE = "Show Console When Standard Error Changes";
 	/** Label: Radio Template */
 	public static final String RADIO_TEMPALTE = "Template:";
 	/** Label: Radio Service Regustry  */
 	public static final String RADIO_SERVICE_REGISTRY = "Service Registry";
 	/** Label: Node Plug-in Development */
 	public static final String NODE_PLUGIN_DEVELOPMENT = "Plug-in Development";
 	/** Label: Node Target Platform */
 	public static final String NODE_TARGET_PLATFORM = "Target Platform";
 	/** Label: Node General */
 	public static final String NODE_GENERAL = "General";
 	/** Part of definition name */
 	public static final String PART_OF_ACTIVE_DEFINITION_NAME = " (Active)";
 	/** Menu item: File */
 	public static final String MENU_ITEM_FILE = "File";
 	/** Menu item: New */
 	public static final String MENU_ITEM_NEW = "New";
 	/** Menu item: Project */
 	public static final String MENU_ITEM_PROJECT = "Project...";
 	/** View: Problems */
 	public static final String VIEW_PROBLEMS = "Problems";
 	/** View: Package Explorer */
 	public static final String VIEW_PACKAGE_EXPLORER = "Package Explorer";
 	/** View: Welcome */
 	public static final String VIEW_WELCOME = "Welcome";
 	/** Error text. */
 	public static final String ERROR_CANT_COMPILE = "There are some problems during compilation.";
 	/** Error text. */
 	public static final String ERROR_PLATFORM_NOT_ACTIVE = "Swordfish Platform is not active.";
 	/** Error text. */
 	public static final String ERROR_PLATFORM_CANT_BE_ACTIVE = "Swordfish Platform can not be active.";
 	/** Error text. */
 	public static final String ERROR_PLATFORM_NOT_FOUND = "Swordfish Platform not found.";
 	/** Error text. */
 	public static final String ERROR_NO_WELCOME = "No Welcome window view.";
 	/** Window: Eclipse SDK */
 	public static final String WINDOW_ECLIPSE_SDK = "Eclipse";
 	/** Window: Preferences */
 	public static final String WINDOW_PREFERENCES = "Preferences";
 	/** Window: New Target Definition */
 	public static final String WINDOW_NEW_TARGET_DEFINITION = "New Target Definition";
 	/** Window: Load Target Platform */
 	public static final String WINDOW_LOAD_TARGET_PLATFORM = "Load Target Platform";
 	/** Window: Install */
 	public static final String WINDOW_INSTALL = "Install";
 	/** Window: Add Site */
 	public static final String WINDOW_ADD_SITE = "Add Site";
 	/** Window: New Plug-in Project */
 	public static final String WINDOW_NEW_PLUGIN_PROJECT = "New Plug-in Project";
 	/** Window: New plug-in project with custom templates */
 	public static final String WINDOW_NEW_PLUGIN_PROJECT_CUSTOM = "New plug-in project with custom templates";
 	/** Window: WSDL files in Swordfish Service Registry*/
 	public static final String WINDOW_WSDL_FILES_SWORFISH_SERVICE_REGISTRY = "WSDL files in Swordfish Service Registry";
 	/** Menu item: Other */	
 	public static final String MENU_ITEM_OTHER = "Other..."; 
 	/** Timeout: switch platform */
 	public static final int TIMEOUT_SWITCH_PLATFORM = 90000;
 	/** Timeout: remote */
 	public static final int TIMEOUT_REMOTE = 90000;
 	/** Timeout: for refreshing */
 	public static final int TIMEOUT_FOR_REFRESHING = 10000;
 	/** Timeout: for creating endpoint */
 	public static final int TIMEOUT_CREATING_ENDPOINT = 120000;
 	/** Timeout: for enable button */
 	public static final int TIMEOUT_FOR_ENABLE = 500;
 	/** Timeout: for refresh console */	
 	public static final int TIME_OUT_REFRESH_CONSOLE = 5000;	
 	/** Timeout: for refreshing */
 	public static final int TIMEOUT_FOR_REFRESHING_PROJECT = 2000;
     /** Name of project generated from library.wsdl **/
 	public static final String LIBRARY_PROJECT_NAME = "jaxws.service.library";
     /** Name of consumer project for project with name LIBRARY_PROJECT_NAME **/
 	public static final String LIBRARY_PROJECT_CONSUMER_NAME = LIBRARY_PROJECT_NAME+".consumer";
     /** Name of project generated from BookingService.wsdl **/	
 	public static final String BOOKING_PROJECT_NAME = "jaxws.service.booking.service";
     /** Name of project generated from FlightBooking.wsdl **/	
 	public static final String FLIGHT_SERVICE_PROJECT_NAME = "jaxws.service.flight.booking";	
 	
     /** Context menu Close Project **/
 	public static final String CONTEXT_MENU_CLOSE_PROJECT = "Close Project";
 	
     /** Name of BPEL Editor for UpdateSite **/	
 	public static final String BPEL_EDITOR_NAME = "Eclipse BPEL Designer (Incubation)";	
 
     /** Name of BPEL Editor for UpdateSite **/	
 	public static final String BPEL_DESIGNER_NAME = "BPEL Visual Designer";	
 	
 	/** Count iterations */
 	public static final int COUNT_ITERATIONS_READING_CONSOLE = 100;
 
 	/** Count iterations */
 	public static final int COUNT_READING_CONSOLE_WAITINGS = 20;
 	
 	/** Reread console timeour */
 	public static final int TIMEOUT_CONSOLE_READ_LINE = 100;
 	
 }
 
