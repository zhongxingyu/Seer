 package org.iucn.sis.client.api.container;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.ChangesFieldWidgetCache;
 import org.iucn.sis.client.api.caches.FieldWidgetCache;
 import org.iucn.sis.client.api.caches.MarkedCache;
 import org.iucn.sis.client.api.caches.ReferenceCache;
 import org.iucn.sis.client.api.caches.RegionCache;
 import org.iucn.sis.client.api.caches.SchemaCache;
 import org.iucn.sis.client.api.caches.StatusCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.UserStore;
 import org.iucn.sis.client.api.caches.ViewCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.debug.HostedModeDebugger;
 import org.iucn.sis.client.api.debug.LiveDebugger;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.utils.ClientReferenceParser;
 import org.iucn.sis.client.api.utils.HasCache;
 import org.iucn.sis.client.api.utils.SIS;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.UserPreferences;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.parsers.ReferenceParserFactory;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.solertium.lwxml.factory.NativeDocumentFactory;
 import com.solertium.lwxml.gwt.debug.DebuggingApplication;
 import com.solertium.lwxml.gwt.debug.SysDebugger;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.api.FixedSizedQueue;
 
 public abstract class SISClientBase implements EntryPoint, DebuggingApplication {
 	public static class SimpleSupport {
 		public static void doLogin(final String username, final String password) {
 			if (username != null && password != null && !username.equals("") && !password.equals("")) {
 				StringBuffer xml = new StringBuffer("<auth>");
 				xml.append("<u>");
 				xml.append(username);
 				xml.append("</u>");
 				xml.append("<p>");
 				xml.append(password);
 				xml.append("</p>");
 				xml.append("</auth>");
 				
 				final NativeDocument auth = NativeDocumentFactory.newNativeDocument();
 				auth.postAsText(UriBase.getInstance().getSISBase() + "/authn/origin", xml.toString(), new GenericCallback<String>() {
 					public void onFailure(Throwable caught) {
 						String ret = auth.getText();
 
 						if (ret != null && !ret.equals(""))
 							instance.buildLogin(ret);
 						else
 							instance.buildLogin("Invalid login credentials.");
 						
 						WindowUtils.hideLoadingAlert();
 					}
 
 					public void onSuccess(String result) {
 						getProfile(password, username, auth.getText());
 					}
 				});
 
 			} else
 				instance.buildLogin("Invalid login credentials.");
 		}
 
 		public static void doLogout() {
 			currentUser = null;
 			instance.buildLogin("Log out successful.");
 
 			// TODO: FLUSH APPROPRIATE CACHES
 			AssessmentCache.impl.doLogout();
 			FieldWidgetCache.impl.doLogout();
 			ChangesFieldWidgetCache.get().doLogout();
 			TaxonomyCache.impl.doLogout();
 			WorkingSetCache.impl.doLogout();
 			MarkedCache.impl.onLogout();
 			StatusCache.impl.clearCache();
 			AuthorizationCache.impl.clear();
 			StateManager.impl.doLogout();
 			ReferenceCache.impl.doLogout();
 			ViewCache.impl.doLogout();
 			UserStore.impl.doLogout();
 			History.newItem("", false);
 			
 			NativeDocumentFactory.setDefaultInstance(new NativeDocumentFactory());
 		}
 
 		private static void getProfile(final String password, final String username, final String authn) {
 			AuthorizationCache.impl.setCredentials(authn);
 			NativeDocumentFactory.setDefaultInstance(new NativeDocumentFactory() {
 				public NativeDocument createNativeDocument() {
 					return AuthorizationCache.impl.getNativeDocument();
 				}
 			});
 			
 			AuthorizationCache.impl.init(new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					final NativeDocument doc = NativeDocumentFactory.newNativeDocument();
 					doc.get(UriBase.getInstance().getSISBase() + "/profile/" + username, new GenericCallback<String>() {
 						public void onFailure(Throwable caught) {
 							if( "403".equals(caught.getMessage()) )
 								instance.buildLogin("Login " + username + " not active.");
 							else
 								instance.buildLogin("Invalid credentials.");
 						}
 
 						public void onSuccess(String result) {
 							ClientUser user = ClientUser.fromXML(doc.getDocumentElement());
 							if (!user.isSISUser()) {
 								WindowUtils.hideLoadingAlert();
 								instance.buildLogin("Login " + username + " not active.");
 							}
 							else {
 								WindowUtils.showLoadingAlert("Login successful, loading...");
 								
 								currentUser = user;
 								currentUser.password = password;
 								
 								if ("admin".equalsIgnoreCase(username))
 									currentUser.setProperty(UserPreferences.AUTO_SAVE, UserPreferences.AutoSave.DO_ACTION);
 								
 								try {
 									if (!iAmOnline)
 										currentUser.setProperty("quickGroup", "offline");
 									
 									AuthorizationCache.impl.addUser(currentUser);
 			
 									instance.initializeCaches(new SimpleListener() {
 										public void handleEvent() {
 											instance.buildPostLogin();		
 										}
 									});
 								} catch (Exception e) {
 									Debug.println(e);
 								}
 							}
 						}
 					});
 				}
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Error Initializing Oracle", caught.getMessage());
 					instance.initializeCaches(new SimpleListener() {
 						public void handleEvent() {
 							instance.buildPostLogin();
 						}
 					});
 				}
 			});
 		}
 	}
 	
 	private static void displayJavascriptError(Throwable e) {
 		final com.extjs.gxt.ui.client.widget.Window window = 
 			WindowUtils.newWindow("Javascript Error", null, false);
 		window.setAutoHide(true);
 		window.setLayout(new FitLayout());
 		window.setSize(450, 300);
 		window.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				window.hide();
 			}
 		}));
 		final StringBuilder builder = new StringBuilder();
 		builder.append("A javascript error has occurred.  Please save your work, if " +
 			"possible, and refresh your browser.  If you would like to submit this " +
			"error as a bug, please file a ticket and copy the contents of " +
 			"this textbox into the body of the ticket.  Thank you.\n\n");
 		builder.append("Error occurred on " + new Date() + "\n");
 		builder.append(SIS.getBuildNumber() + "\n");
		builder.append("Host: " + Window.Location.getHostName() + "\n");
		builder.append("URL: " + Window.Location.getPath() + "\n");
 		builder.append(HostedModeDebugger.serializeThrowable(e));
 		
 		final TextArea area = new TextArea();
 		area.setReadOnly(true);
 		area.setValue(builder.toString());
 		
 		window.add(area);
 		window.show();
 	}
 
 	public static SISClientBase instance;
 	
 	public static ClientUser currentUser;
 	
 	public static boolean iAmOnline = true;
 
 	public static NativeDocument getHttpBasicNativeDocument() {
 		return NativeDocumentFactory.newNativeDocument();
 	}
 
 	public static SISClientBase getInstance() {
 		return instance;
 	}
 	
 	protected FixedSizedQueue logQueue;
 
 	public int getLogLevel() {
 		return SysDebugger.OFF;
 	}
 	
 	public FixedSizedQueue getLog() {
 		return logQueue;
 	}
 
 	public final void onModuleLoad() {
 		if ("true".equals(Window.Location.getParameter("debug"))) {
 			logQueue = new FixedSizedQueue(500);
 			
 			Debug.setInstance(new HostedModeDebugger() {
 				protected void writeOutput(String output) {
 					super.writeOutput(output);
 					logQueue.add(output);
 				}
 			});
 			
 			GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 				public void onUncaughtException(Throwable e) {
 					Debug.println(e);
 					displayJavascriptError(e);
 				}
 			});
 		}
 		else if (UriBase.getInstance().isHostedMode())
 			Debug.setInstance(new HostedModeDebugger());
 		else {
 			GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 				public void onUncaughtException(Throwable e) {
 					displayJavascriptError(e);
 				}
 			});
 			Debug.setInstance(new LiveDebugger());
 		}
 		
 		loadModule();
 	}
 	
 	protected final void initializeCaches(final SimpleListener listener) {
 		ReferenceParserFactory.get().setParser(new ClientReferenceParser());
 		
 		final List<String> inits = new ArrayList<String>();
 		final Map<String, GenericCallback<NativeDocument>> handlers = 
 			new HashMap<String, GenericCallback<NativeDocument>>();
 		
 		//Implementation-specific caches
 		for (HasCache cache : getCachesToInitialize()) {
 			inits.add(cache.getCacheUrl());
 			handlers.put(cache.getCacheUrl(), cache.getCacheInitializer());
 		}
 		
 		//Default, required caches
 		for (HasCache cache : new HasCache[] { MarkedCache.impl, 
 				SchemaCache.impl, RegionCache.impl }) {
 			inits.add(cache.getCacheUrl());
 			handlers.put(cache.getCacheUrl(), cache.getCacheInitializer());
 		}
 		
 		WindowUtils.showLoadingAlert("Loading working sets...");
 		WorkingSetCache.impl.update(new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				SIS.fetchList(inits, handlers, new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						WindowUtils.showLoadingAlert("Done, welcome, " + currentUser.getDisplayableName());
 						DeferredCommand.addPause();
 						DeferredCommand.addCommand(new Command() {
 							public void execute() {
 								listener.handleEvent();
 							}
 						});
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Failed to load cache data.");
 						listener.handleEvent();
 					}
 				}, false);
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not load working sets.");
 				onSuccess(null);
 			}
 		});
 	}
 	
 	protected abstract Collection<HasCache> getCachesToInitialize();
 	
 	public abstract void loadModule();
 	
 	public abstract void buildLogin(String message);
 	
 	public abstract void buildPostLogin();
 	
 	/**
 	 * This method is invoked whenever a user elects to logout of the system.
 	 */
 	public abstract void onLogout();
 	
 	/**
 	 * This method is invoked when the current assessment gets changed in the 
 	 * AssessmentCache, so the client can update panels as necessary.
 	 */
 	public abstract void onAssessmentChanged();
 	
 	/**
 	 * This method is invoked when the current working set gets changed in the 
 	 * WorkingSetCache, so the client can update panels as necessary.
 	 */
 	public abstract void onWorkingSetChanged();
 	
 	/**
 	 * This method is invoked when the current taxon gets changed in the 
 	 * TaxonomyCache, so the client can update panels as necessary.
 	 */
 	public abstract void onTaxonChanged();
 	
 	/**
 	 * This method is invoked when some class in the shared packages needs the
 	 * client to display the reference editor, with the supplied hooks.
 	 */
 	public abstract void onShowReferenceEditor(String title, Referenceable referenceable, 
 			GenericCallback<Object> onAddCallback, GenericCallback<Object> onRemoveCallback);
 }
