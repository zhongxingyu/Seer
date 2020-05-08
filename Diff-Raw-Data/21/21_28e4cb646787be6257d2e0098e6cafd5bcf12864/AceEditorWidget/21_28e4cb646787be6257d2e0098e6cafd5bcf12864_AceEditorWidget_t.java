 package project.client.editor;
 
 import project.client.login.LoginInfo;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import edu.ycp.cs.dh.acegwt.client.ace.AceAnnotationType;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
 import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;
 
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.dom.client.Style.Unit;
 
 public class AceEditorWidget extends VerticalPanel {
 	static AceEditor editor1;
 	private SubmitServiceAsync submitService;
 	private VerticalPanel mainPanel = this;
 	private LoginInfo loginInfo;
 	private Anchor signOutLink = new Anchor("Sign Out");
 
 	public AceEditorWidget(LoginInfo loginInfo) {
 		this.loginInfo = loginInfo;
 		createEditor();
 	}
 
 	private void createEditor() {
 		if (submitService == null)
 			submitService = (SubmitServiceAsync) GWT
 					.create(SubmitService.class);
 		// create first AceEditor widget
 		editor1 = new AceEditor(true);
 		editor1.setWidth("798px");
 		editor1.setHeight("300px");
 		
 		// build the UI
 		buildUI();
 		
 	}
 
 	/**
 	 * This method builds the UI. It creates UI widgets that exercise most/all
 	 * of the AceEditor methods, so it's a bit of a kitchen sink.
 	 */
 	private void buildUI() {
 		setWidth("100%");
 		
 		mainPanel.add(editor1);
 
 		LayoutPanel layoutPanel = new LayoutPanel();
 		add(layoutPanel);
 		layoutPanel.setHeight("214px");
 
 		// checkbox to show/hide gutter
 		final CheckBox showGutterBox = new CheckBox("Show gutter: ");
 		layoutPanel.add(showGutterBox);
 		showGutterBox.setSize("100px", "30px");
 		layoutPanel.setWidgetLeftWidth(showGutterBox, 4.0, Unit.PX, 100.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(showGutterBox, 22.0, Unit.PX, 48.0,
 				Unit.PX);
 		showGutterBox.setValue(true);
 		Button setTabSizeButton = new Button("Set tab size");
 		layoutPanel.add(setTabSizeButton);
 		layoutPanel.setWidgetLeftWidth(setTabSizeButton, 700.0, Unit.PX, 100.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(setTabSizeButton, 4.0, Unit.PX, 28.0,
 				Unit.PX);
 		setTabSizeButton.setSize("100px", "");
 
 		// add text box and button to set tab size
 		final TextBox tabSizeTextBox = new TextBox();
 		layoutPanel.add(tabSizeTextBox);
 		layoutPanel.setWidgetLeftWidth(tabSizeTextBox, 632.0, Unit.PX, 62.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(tabSizeTextBox, 0.0, Unit.PX, 32.0,
 				Unit.PX);
 		tabSizeTextBox.setWidth("4em");
 
 		// add text box and button to go to a given line
 		final TextBox gotoLineTextBox = new TextBox();
 		layoutPanel.add(gotoLineTextBox);
 		layoutPanel.setWidgetLeftWidth(gotoLineTextBox, 632.0, Unit.PX, 62.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(gotoLineTextBox, 38.0, Unit.PX, 32.0,
 				Unit.PX);
 		gotoLineTextBox.setWidth("4em");
 		Button gotoLineButton = new Button("Go to line");
 		layoutPanel.add(gotoLineButton);
 		layoutPanel.setWidgetLeftWidth(gotoLineButton, 700.0, Unit.PX, 100.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(gotoLineButton, 42.0, Unit.PX, 28.0,
 				Unit.PX);
 		gotoLineButton.setWidth("100px");
 
 		// checkbox to set/unset readonly mode
 		final CheckBox readOnlyBox = new CheckBox("Read only: ");
 		layoutPanel.add(readOnlyBox);
 		readOnlyBox.setSize("100px", "30px");
 		layoutPanel.setWidgetLeftWidth(readOnlyBox, 632.0, Unit.PX, 82.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(readOnlyBox, 92.0, Unit.PX, 32.0,
 				Unit.PX);
 		readOnlyBox.setValue(false);
 
 		// checkbox to show/hide print margin
 		final CheckBox showPrintMarginBox = new CheckBox("Show print margin: ");
 		layoutPanel.add(showPrintMarginBox);
 		showPrintMarginBox.setSize("150", "30");
 		layoutPanel.setWidgetLeftWidth(showPrintMarginBox, 4.0, Unit.PX, 128.0,
 				Unit.PX);
 		layoutPanel.setWidgetTopHeight(showPrintMarginBox, 92.0, Unit.PX, 30.0,
 				Unit.PX);
 		showPrintMarginBox.setValue(true);
 
 		// Add check box to enable/disable soft tabs
 		final CheckBox softTabsBox = new CheckBox("Soft tabs");
 		layoutPanel.add(softTabsBox);
 		layoutPanel
 				.setWidgetLeftWidth(softTabsBox, 4.0, Unit.PX, 72.0, Unit.PX);
 		layoutPanel.setWidgetTopHeight(softTabsBox, 128.0, Unit.PX, 16.0,
 				Unit.PX);
 		softTabsBox.setSize("100px", "30px");
 		softTabsBox.setValue(true); // I think soft tabs is the default
 
 		// checkbox to set whether or not horizontal scrollbar is always visible
 		final CheckBox hScrollBarAlwaysVisibleBox = new CheckBox(
 				"H scrollbar: ");
 		layoutPanel.add(hScrollBarAlwaysVisibleBox);
 		hScrollBarAlwaysVisibleBox.setSize("100px", "30px");
 		layoutPanel.setWidgetLeftWidth(hScrollBarAlwaysVisibleBox, 4.0,
 				Unit.PX, 100.0, Unit.PX);
 		layoutPanel.setWidgetTopHeight(hScrollBarAlwaysVisibleBox, 56.0,
 				Unit.PX, 28.0, Unit.PX);
 		hScrollBarAlwaysVisibleBox.setValue(true);
 		Button button = new Button("Submit Code");
 		layoutPanel.add(button);
 		button.setWidth("100px");
 		layoutPanel.setWidgetLeftWidth(button, 700.0, Unit.PX, 91.0, Unit.PX);
 		layoutPanel.setWidgetTopHeight(button, 153.0, Unit.PX, 28.0, Unit.PX);
 		layoutPanel.add(signOutLink);
 		layoutPanel
 				.setWidgetLeftWidth(signOutLink, 4.0, Unit.PX, 100.0, Unit.PX);
 		layoutPanel.setWidgetTopHeight(signOutLink, 182.0, Unit.PX, 32.0,
 				Unit.PX);
 
 		// Set up sign out hyperlink.
 		signOutLink.setHref(loginInfo.getLogoutUrl());
 		button.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				try {
 					callSubmitService();
 				}
 
 				catch (Exception e) {
 					System.out.println(e.getMessage());
 				}
 
 			}
 		});
 		hScrollBarAlwaysVisibleBox.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setHScrollBarAlwaysVisible(hScrollBarAlwaysVisibleBox
 						.getValue());
 			}
 		});
 		softTabsBox.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setUseSoftTabs(softTabsBox.getValue());
 			}
 		});
 		showPrintMarginBox.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setShowPrintMargin(showPrintMarginBox.getValue());
 			}
 		});
 		readOnlyBox.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setReadOnly(readOnlyBox.getValue());
 			}
 		});
 		gotoLineButton.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.gotoLine(Integer.parseInt(gotoLineTextBox.getText()));
 			}
 		});
 		setTabSizeButton.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setTabSize(Integer.parseInt(tabSizeTextBox.getText()));
 			}
 		});
 		showGutterBox.addClickHandler(new ClickHandler() {
 			@Override
 			/**
 			 * Code to be overridden
 			 */
 			public void onClick(ClickEvent event) {
 				editor1.setShowGutter(showGutterBox.getValue());
 			}
 		});
 
 		Label label = new Label("Editor by daveho@Github");
 		add(label);
 
 		// RootPanel.get("ace").add(mainPanel);
 	}
 
 	private void updateEditor1CursorPosition() {
 	}
 
 	public String sendText() {
 		return editor1.getText();
 	}
 
 
 	public void buildEditor() {
 		// start the first editor and set its theme and mode
 		editor1.startEditor(); // must be called before calling
 								// setTheme/setMode/etc.
 		editor1.setTheme(AceEditorTheme.ECLIPSE);
 		editor1.setMode(AceEditorMode.JAVA);
 		// use cursor position change events to keep a label updated
 		// with the current row/col
 		editor1.addOnCursorPositionChangeHandler(new AceEditorCallback() {
 			@Override
 			public void invokeAceCallback(JavaScriptObject obj) {
 				updateEditor1CursorPosition();
 			}
 		});
 		updateEditor1CursorPosition(); // initial update
 		// set some initial text in editor 1
 		editor1.setText("//Write code here");
 		// add some annotations
 		editor1.addAnnotation(0, 1, "What's up?", AceAnnotationType.WARNING);
 		editor1.addAnnotation(2, 1, "This code is lame",
 				AceAnnotationType.ERROR);
 		editor1.setAnnotations();
 	}
 
 	// Method used to call service
 	public void callSubmitService() {
 		submitService.sendCode(sendText(), new AsyncCallback<String>() {
 			public void onFailure(Throwable caught) {
 				// System.out.println(caught.toString());
 				Window.alert("Failure");
 			}
 
 			public void onSuccess(String result) {
 				Window.alert("Success");
 			}
 		});
 	}
 }
