 // NetCoder - a web-based pedagogical programming environment
 // Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
 // Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU Affero General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Affero General Public License for more details.
 //
 // You should have received a copy of the GNU Affero General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package edu.ycp.cs.netcoder.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;
 import edu.ycp.cs.netcoder.client.logchange.ChangeFromAceOnChangeEvent;
 import edu.ycp.cs.netcoder.client.logchange.ChangeList;
 import edu.ycp.cs.netcoder.client.status.ProblemDescriptionWidget;
 import edu.ycp.cs.netcoder.client.status.ResultWidget;
 import edu.ycp.cs.netcoder.client.status.StatusAndButtonBarWidget;
 import edu.ycp.cs.netcoder.shared.affect.AffectEvent;
 import edu.ycp.cs.netcoder.shared.logchange.Change;
 import edu.ycp.cs.netcoder.shared.logchange.ChangeType;
 import edu.ycp.cs.netcoder.shared.problems.Problem;
 import edu.ycp.cs.netcoder.shared.problems.User;
 import edu.ycp.cs.netcoder.shared.testing.TestResult;
 import edu.ycp.cs.netcoder.shared.util.Publisher;
 import edu.ycp.cs.netcoder.shared.util.Subscriber;
 
 /**
  * View for working on a problem: code editor, submit button, feedback, etc.
  */
 public class DevelopmentView extends NetCoderView implements Subscriber, ResizeHandler {
 	private static final int PROBLEM_ID = 0; // FIXME
 	
 	private enum Mode {
 		/** Loading problem and current text - editing not allowed. */
 		LOADING,
 		
 		/** Normal state - user is allowed to edit the program text. */
 		EDITING,
 		
 		/**
 		 * Submit in progress.
 		 * Editing disallowed until server response is received.
 		 */
 		SUBMIT_IN_PROGRESS,
 		
 		/**
 		 * Logging out.
 		 */
 		LOGOUT,
 	}
 	
 	// UI mode
 	private Mode mode;
 	private boolean textLoaded;
 	
 	/*
 	// Model objects added to the session.
 	private Object[] sessionObjects;
 	*/
 	
 	// Widgets
 	private ProblemDescriptionWidget problemDescription;
 	private AceEditor editor;
 	private TabLayoutPanel resultsTabPanel;
 	private ResultWidget resultWidget;
 	private Timer flushPendingChangeEventsTimer;
 	
 	// RPC services.
 	private LogCodeChangeServiceAsync logCodeChangeService = GWT.create(LogCodeChangeService.class);
 	private SubmitServiceAsync submitService = GWT.create(SubmitService.class);
 	private LoadExerciseServiceAsync loadService = GWT.create(LoadExerciseService.class);
 	private AffectEventServiceAsync affectEventService = GWT.create(AffectEventService.class);
 	
 	public DevelopmentView(Session session) {
 		super(session);
 		
 		/*
 		// Add ChangeList and AffectEvent to session
 		sessionObjects = new Object[]{ new ChangeList(), new AffectEvent() };
 		for (Object obj : sessionObjects) {
 			getSession().add(obj);
 		}
 		*/
 		addSessionObject(new ChangeList());
 		addSessionObject(new AffectEvent());
 
 		// Observe ChangeList state.
 		// We do this so that we know when the local editor contents are
 		// up to date with the text on the server.
 		session.get(ChangeList.class).subscribe(ChangeList.State.CLEAN, this, getSubscriptionRegistrar());
 		
 		// User won't be allowed to edit until the problem (and previous editor contents, if any)
 		// are loaded.
 		mode = Mode.LOADING;
 		textLoaded = false;
 		
 		// The overall UI is build in a LayoutPanel (which the parent class creates)
 		LayoutPanel layoutPanel = getLayoutPanel();
 		
 		// Add problem description widget
 		problemDescription = new ProblemDescriptionWidget(session, getSubscriptionRegistrar());
 		layoutPanel.add(problemDescription);
 		layoutPanel.setWidgetTopHeight(
 				problemDescription,
 				LayoutConstants.TOP_BAR_HEIGHT_PX, Unit.PX,
 				LayoutConstants.PROBLEM_DESC_HEIGHT_PX, Unit.PX);
 		
 		// Add AceEditor widget
 		editor = new AceEditor();
 		editor.setStyleName("NetCoderEditor");
 		layoutPanel.add(editor);
 		layoutPanel.setWidgetTopHeight(editor,
 				LayoutConstants.TOP_BAR_HEIGHT_PX + LayoutConstants.PROBLEM_DESC_HEIGHT_PX, Unit.PX,
 				200, Unit.PX);
 
 		// Add the status and button bar widget
 		StatusAndButtonBarWidget statusAndButtonBarWidget = new StatusAndButtonBarWidget(getSession(), getSubscriptionRegistrar());
 		layoutPanel.add(statusAndButtonBarWidget);
 		layoutPanel.setWidgetBottomHeight(
 				statusAndButtonBarWidget,
 				LayoutConstants.RESULTS_PANEL_HEIGHT_PX, Unit.PX,
 				LayoutConstants.STATUS_AND_BUTTON_BAR_HEIGHT_PX, Unit.PX);
 		statusAndButtonBarWidget.setOnSubmit(new Runnable() {
 			@Override
 			public void run() {
 				submitCode();
 			}
 		});
 		
 		// Add the ResultWidget
 		/*
 		resultWidget = new ResultWidget();
 		layoutPanel.add(resultWidget);
 		layoutPanel.setWidgetBottomHeight(
 				resultWidget,
 				0, Unit.PX,
 				LayoutConstants.RESULTS_PANEL_HEIGHT_PX, Unit.PX);
 		*/
 		resultsTabPanel = new TabLayoutPanel(LayoutConstants.RESULTS_TAB_BAR_HEIGHT_PX, Unit.PX);
 		
 		resultWidget = new ResultWidget(getSession(), getSubscriptionRegistrar());
 		resultWidget.setWidth("100%");
 		resultWidget.setHeight("100%");
 		resultsTabPanel.add(resultWidget, "Test results");
 		
 		layoutPanel.add(resultsTabPanel);
 		layoutPanel.setWidgetBottomHeight(
 				resultsTabPanel,
 				0, Unit.PX,
 				LayoutConstants.RESULTS_PANEL_HEIGHT_PX, Unit.PX);
 		
 		// UI is now complete
 		initWidget(layoutPanel);
 		
 		// Register the view as a window resize handler
 		Window.addResizeHandler(this);
 		
 		// Initiate loading of the problem and current editor text.
 		loadProblemAndCurrentText();
 		
 		// Create timer to flush unsent change events periodically.
 		this.flushPendingChangeEventsTimer = new Timer() {
 			@Override
 			public void run() {
 				final ChangeList changeList = getSession().get(ChangeList.class);
 				
 				if (changeList == null) {
 					// paranoia
 					return;
 				}
 				
 				if (changeList.getState() == ChangeList.State.UNSENT) {
 					Change[] changeBatch = changeList.beginTransmit();
 
 					AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
 						@Override
 						public void onFailure(Throwable caught) {
 							changeList.endTransmit(false);
 							GWT.log("Failed to send change batch to server");
 						}
 
 						@Override
 						public void onSuccess(Boolean result) {
 							changeList.endTransmit(true);
 						}
 					};
 
 					logCodeChangeService.logChange(changeBatch, callback);
 				}
 			}
 		};
 		flushPendingChangeEventsTimer.scheduleRepeating(1000);
 	}
 
 	/**
 	 * Load the problem and current editor text.
 	 * The current editor text is (hopefully) whatever the user
 	 * had in his/her editor the last time they were logged in.
 	 */
 	protected void loadProblemAndCurrentText() {
 		// Load the problem.
 		loadService.load(PROBLEM_ID, new AsyncCallback<Problem>() {
 			@Override
 			public void onSuccess(Problem result) {
 				if (result != null) {
 					getSession().add(result);
 					onProblemLoaded();
 				} else {
 					loadProblemFailed();
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Could not load problem", caught);
 				loadProblemFailed();
 			}
 		});
 		
 		// Load current text.
 		loadService.loadCurrentText(PROBLEM_ID, new AsyncCallback<String>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Could not load current text", caught);
 				loadCurrentTextFailed();
 			}
 			
 			public void onSuccess(String result) {
 				onCurrentTextLoaded(result);
 			}
 		});
 	}
 
 	/**
 	 * Called when the problem has been loaded.
 	 */
 	protected void onProblemLoaded() {
 		// If the current editor text has been loaded,
 		// then it is ok to start editing.
 		if (textLoaded == true) {
 			startEditing();
 		}
 	}
 	
 	/**
 	 * Called when the current text has been retrieved from the server.
 	 * 
 	 * @param text the current text to load into the editor
 	 */
 	protected void onCurrentTextLoaded(String text) {
 		editor.setText(text);
 		textLoaded = true;
 		
 		// If the problem has been loaded, then it is ok to start editing.
 		if (getSession().get(Problem.class) != null) {
 			startEditing();
 		}
 	}
 
 	protected void startEditing() {
 		editor.setReadOnly(false);
 		mode = Mode.EDITING;
 	}
 
 	protected void loadProblemFailed() {
 		// TODO - improve
 		problemDescription.setErrorText("Could not load problem description");
 	}
 	
 	protected void loadCurrentTextFailed() {
 		// TODO - improve
 		problemDescription.setErrorText("Could not load text for problem");
 	}
 
 	@Override
 	public void activate() {
 		editor.startEditor();
 		editor.setReadOnly(true); // until a Problem is loaded
 		editor.setTheme(AceEditorTheme.ECLIPSE);
 		editor.setFontSize("14px");
 		editor.setMode(AceEditorMode.JAVA);
 		editor.addOnChangeHandler(new AceEditorCallback() {
 			@Override
 			public void invokeAceCallback(JavaScriptObject obj) {
 				// Important: don't send the change to the server unless the
 				// initial editor contents has been loaded.  Otherwise,
 				// the setting of the initial editor contents will get sent
 				// to the server as a change, which is obviously not what
 				// we want.
 				if (!textLoaded) {
 					return;
 				}
 				
 				// Convert ACE onChange event object to a Change object,
 				// and add it to the session's ChangeList
 				User user = getSession().get(User.class);
 				Problem problem = getSession().get(Problem.class);
 				Change change = ChangeFromAceOnChangeEvent.convert(obj, user.getId(), problem.getProblemId());
 				getSession().get(ChangeList.class).addChange(change);
 			}
 		});
 		
 		// make the editor the correct height
 		doResize();
 	}
 	
 	@Override
 	public void deactivate() {
 		// Turn off the flush pending events timer
 		flushPendingChangeEventsTimer.cancel();
 
 		// Unsubscribe all event subscribers
 		getSubscriptionRegistrar().unsubscribeAllEventSubscribers();
 
 		// Clear all local session data
 		removeAllSessionObjects();
 	}
 
 	protected void submitCode() {
 		// If the problem has not been loaded yet,
 		// then there is nothing to do.
 		if (getSession().get(Problem.class) == null) {
 			return;
 		}
 		
 		// Set the editor to read-only!
 		// We don't want any edits until the results have
 		// come back from the server.
 		editor.setReadOnly(true);
 		
 		// Create a Change representing the full text of the document,
 		// and schedule it for transmission to the server.
 		Change fullText = new Change(
 				ChangeType.FULL_TEXT,
 				0, 0, 0, 0, // ignored
 				System.currentTimeMillis(),
 				getSession().get(User.class).getId(),
 				getSession().get(Problem.class).getProblemId(),
 				editor.getText());
 		getSession().get(ChangeList.class).addChange(fullText);
 		
 		// Set the mode to SUBMIT_IN_PROGRESS, indicating that we are
 		// waiting for the full text to be uploaded to the server.
 		mode = Mode.SUBMIT_IN_PROGRESS;
 	}
 	
 	@Override
 	public void eventOccurred(Object key, Publisher publisher, Object hint) {
 		if (key == ChangeList.State.CLEAN && mode == Mode.SUBMIT_IN_PROGRESS) {
 			// Full text of submission has arrived at server,
 			// and because the editor is read-only, we know that the
 			// local text is in-sync.  So, submit the code!
 			
 			AsyncCallback<TestResult[]> callback = new AsyncCallback<TestResult[]>() {
 				@Override
 				public void onFailure(Throwable caught) {
 					final String msg = "Error sending submission to server for compilation"; 
 					
 					getSession().add(new StatusMessage(StatusMessage.Category.ERROR, msg));
 					
 					GWT.log(msg, caught);
 					// TODO: should set editor back to read/write?
 				}
 
 				@Override
 				public void onSuccess(TestResult[] results) {
 					// Great, got results back from server!
 					getSession().add(results);
 					
 					// Add a status message about the results
 					getSession().add(new StatusMessage(
 							StatusMessage.Category.INFORMATION, "Received " + results.length + " test result(s)"));
 					
 					// Can resume editing now
 					startEditing();
 				}
 			};
 			
 			// Send editor text to server. 
 			int problemId = getSession().get(Problem.class).getProblemId();
 			submitService.submit(problemId, editor.getText(), callback);
 		}
 	}
 	
 	@Override
 	public void unsubscribeFromAll() {
 		getSession().get(ChangeList.class).unsubscribeFromAll(this);
 	}
 	
 	@Override
 	public void onResize(ResizeEvent event) {
 		doResize();
 	}
 
 	protected void doResize() {
 		int height = Window.getClientHeight();
 		
 		int availableForEditor = height -
 				(LayoutConstants.TOP_BAR_HEIGHT_PX +
 				 LayoutConstants.PROBLEM_DESC_HEIGHT_PX +
 				 LayoutConstants.STATUS_AND_BUTTON_BAR_HEIGHT_PX +
 				 LayoutConstants.RESULTS_PANEL_HEIGHT_PX);
 		
 		if (availableForEditor < 0) {
 			availableForEditor = 0;
 		}
 		
 		getLayoutPanel().setWidgetTopHeight(
 				editor,
 				LayoutConstants.TOP_BAR_HEIGHT_PX + LayoutConstants.PROBLEM_DESC_HEIGHT_PX, Unit.PX,
 				availableForEditor, Unit.PX);
 		
 		getLayoutPanel().setWidgetBottomHeight(resultsTabPanel, 0, Unit.PX, LayoutConstants.RESULTS_PANEL_HEIGHT_PX, Unit.PX);
 		
 		// FIXME: I don't know how to get the stupid Grid to expand its vertical size automatically to show the $!@$!! rows.
 		resultWidget.setGridSize(
 				Window.getClientWidth() + "px",
 				(LayoutConstants.RESULTS_PANEL_HEIGHT_PX - (LayoutConstants.RESULTS_TAB_BAR_HEIGHT_PX + 4)) + "px");
 	}
 }
