 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.sopeco.persistence.metadata.entities.DatabaseInstance;
 import org.sopeco.webui.client.event.EventControl;
 import org.sopeco.webui.client.extensions.Extensions;
 import org.sopeco.webui.client.helper.SystemDetails;
 import org.sopeco.webui.client.helper.callback.CallbackBatch;
 import org.sopeco.webui.client.helper.callback.ParallelCallback;
 import org.sopeco.webui.client.helper.push.ServerPush;
 import org.sopeco.webui.client.layout.MainLayoutPanel;
 import org.sopeco.webui.client.layout.login.LoginPanel;
 import org.sopeco.webui.client.log.LogHandler;
 import org.sopeco.webui.client.manager.Manager;
 import org.sopeco.webui.client.manager.ScenarioManager;
 import org.sopeco.webui.client.resources.R;
 import org.sopeco.webui.client.widget.ExceptionDialog;
 import org.sopeco.webui.shared.helper.ExtensionContainer;
 import org.sopeco.webui.shared.rpc.RPC;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 /**
  * Entry class of this webapplication.
  * 
  * @author Marius Oehler
  * 
  */
 public class SoPeCoUI implements EntryPoint, UncaughtExceptionHandler {
 
 	private static SoPeCoUI frontend;
 
 	private static final Logger LOGGER = Logger.getLogger(SoPeCoUI.class.getName());
 
 	private static final String BRANDING_IMAGE_URL = "";
 
 	/**
 	 * Returns the FrontendEntryPoint object of this application.
 	 * 
 	 * @return FrontendEntryPoint-Object
 	 */
 	public static SoPeCoUI get() {
 		return frontend;
 	}
 
 	public static native String getBuildInfo()
 	/*-{
 		return $wnd.buildInfo;
 	}-*/;
 
 	private DatabaseInstance connectedDatabase;
 	private String connectedAccountName;
 
 	private CallbackBatch loadingBatch;
 
 	/**
 	 * This method will be executed at the start of the application. The
 	 * {@link EntryPoint} method {@link #onModuleLoad()}, called automatically
 	 * by loading the module.
 	 */
 	@Override
 	public void onModuleLoad() {
 		frontend = this;
 
 		R.css.cssCommon().ensureInjected();
 
 		RootLayoutPanel.get().addStyleName("rootPanel");
 
 		configLogger();
 		rpcLoad();
 	}
 
 	/**
 	 * Sets the level and the handler of the root-logger.
 	 */
 	private void configLogger() {
 		Logger rootLogger = Logger.getLogger("");
 		rootLogger.setLevel(Level.FINE);
 		rootLogger.addHandler(LogHandler.get());
 
 		GWT.setUncaughtExceptionHandler(this);
 	}
 
 	@Override
 	public void onUncaughtException(Throwable e) {
 		GWT.log("Uncaught Exception", e);
 		e = e.getCause();
 
 		String st = e.getClass().getName() + ": " + e.getMessage();
 		for (StackTraceElement ste : e.getStackTrace()) {
 			st += "\n" + ste.toString();
 		}
 
 		LOGGER.severe(st);
 		GWT.log(e.getClass().getName() + ": " + e.getMessage(), e);
 
 		ExceptionDialog.show(e);
 	}
 
 	/**
 	 * Loading details from SoPeCo and the from the Backend (supported
 	 * exploration extensions, database-host,...).
 	 */
 	private void rpcLoad() {
 		ParallelCallback<ExtensionContainer> loadExtensions = Extensions.getLoadingCallback();
 		ParallelCallback<HashMap<String, String>> loadSystemDetails = SystemDetails.getLoadingCallback();
 
 		loadingBatch = new CallbackBatch(loadExtensions, loadSystemDetails) {
 			@Override
 			protected void onFailure(List<Throwable> exceptionList) {
 				for (Throwable t : exceptionList) {
 					onUncaughtException(t);
 				}
 			}
 
 			@Override
 			protected void onSuccess() {
 				// All stuff was loaded
 				changeDatabase();
 				ServerPush.start();
 			}
 		};
 
 		RPC.getExtensionRPC().getExtensions(loadExtensions);
 		RPC.getSystemDetailsRPC().getMetaDatabaseDetails(loadSystemDetails);
 	}
 
 	/**
 	 * Causing a change of the database.
 	 */
 	public void changeDatabase() {
 		clearRootLayout();
 		Manager.get().reset();
 		EventControl.removeAllHandler();
 
 		// LoginBox box = new LoginBox();
 		// box.center();
 		RootLayoutPanel.get().add(new LoginPanel());
 	}
 
 	public void logout() {
 		Cookies.removeCookie(LoginPanel.COOKIE_RM_ACCOUNT);
 		Cookies.removeCookie(LoginPanel.COOKIE_RM_TOKEN);
		
		ServerPush.stop();
		ServerPush.clear();
		
 		changeDatabase();
 	}
 
 	/**
 	 * Initialize/reset the main view of the application.
 	 * 
 	 * @param newConnectedDatabase
 	 *            the database of the current connection.
 	 */
 	public void initializeMainView() {
 		ScenarioManager.clear();
 		MainLayoutPanel.destroy();
 
 		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
 		rootLayoutPanel.clear();
 		rootLayoutPanel.add(MainLayoutPanel.get());
 	}
 
 	/**
 	 * Clears the root-layout-panel. The browser-window is empty after this
 	 * method.
 	 */
 	private void clearRootLayout() {
 		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
 		rootLayoutPanel.clear();
 	}
 }
