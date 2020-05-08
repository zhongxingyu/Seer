 package home.research.gwthelloworld.client;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.WindowEvent;
 import com.extjs.gxt.ui.client.event.WindowListener;
 import com.extjs.gxt.ui.client.widget.*;
 import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.FlowData;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.resources.client.ResourcePrototype;
 import com.google.gwt.resources.client.impl.ImageResourcePrototype;
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 import com.google.gwt.user.client.ui.RootPanel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Entry point classes define
  * <code>onModuleLoad()</code>.
  */
 public class GWTHelloWorld implements EntryPoint {
 
     private static LayoutContainer centerPanel = new LayoutContainer();
     private static LayoutContainer rightPanel = new VerticalPanel();
     /**
      * The message displayed to the user when the server cannot be reached or
      * returns an error.
      */
     private static final String SERVER_ERROR = "An error occurred while "
         + "attempting to contact the server. Please check your network "
         + "connection and try again.";
 
     /**
      * Create a remote service proxy to talk to the server-side Greeting
      * service.
      */
 //    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
 //    private final Messages messages = GWT.create(Messages.class);
     private void addWindow() {
         GWT.log("### addWindow()");
 
         // basic window setup
         Window xWindow = new Window();
         xWindow.setHeadingHtml("GXT CookBook | Recipe One");
         xWindow.setClosable(true);
         xWindow.setSize(350, 170);
         xWindow.setModal(true);
         xWindow.setBlinkModal(true);
         xWindow.setResizable(true);
         xWindow.setMaximizable(true);
 
 
         // prepare content to show
         LayoutContainer textPanel = new VerticalPanel();
         textPanel.setStyleAttribute("padding", "15px");
         textPanel.addText("This is our first recipe from GXT Cookbok, how are we doing so far ...");
         xWindow.add(textPanel);
 
 //        // some content
 //        StringBuilder msg = new StringBuilder();
 //        msg.append("This window can do lots of stuff.");
 
         // constrain maximize surface to container, not browser's complete viewable area
 //        xWindow.setContainer(textPanel.getElement());
         // constrain draggable to the same as above
 //        xWindow.getDraggable().setContainer(textPanel);
 
 
         // 
 //        AccordionContainer accordionContainer = new AccordionContainer();
 //        RootPanel.get().add(accordionContainer.getContainer());        
 
         xWindow.show();
     }
 
     private void addDialog() {
         GWT.log("### addDialog()");
         final Dialog dialog = new Dialog();
         dialog.setBodyBorder(false);
         dialog.setClosable(false);
         dialog.setHideOnButtonClick(false);
         dialog.setButtons(Dialog.OKCANCEL);
         dialog.setScrollMode(Scroll.NONE);
         dialog.setHeadingHtml("GXT Cookbook :: Recipe Three");
         dialog.addText("Dialogs are descendants of the Window class, so they are windows that can do even more.");
 
         dialog.show();
 
         SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent evt) {
                 String text = evt.getButton().getHtml();
                 String format = "You clicked the {0} button";
 
                 Info.display("Recipe Three", format, text);
             }
         };
 
         Button okBtn = dialog.getButtonById(Dialog.OK);
         okBtn.addSelectionListener(listener);
         Button cancelBtn = dialog.getButtonById(Dialog.CANCEL);
         cancelBtn.addSelectionListener(listener);
     }
 
     private void addMsgBox() {
         GWT.log("### addMsgBox()");
         // btn click handlers
         Listener<MessageBoxEvent> listener = new Listener<MessageBoxEvent>() {
 
             public void handleEvent(MessageBoxEvent be) {
                 Button btn = be.getButtonClicked();
                 Info.display("Recipe Four", "The '{0}' button was presseed", btn.getHtml());
                 MessageBoxType msgBoxType = be.getMessageBox().getType();
 
                 if (msgBoxType != null && (msgBoxType.equals(MessageBoxType.PROMPT)
                     || msgBoxType.equals(MessageBoxType.MULTIPROMPT))) {
                     Info.display("Recipe Four : Prompt", be.getValue());
                 }
 
             }
         };
         // show alert
         MessageBox.alert("Alert", "Invalid Login Credentials", listener);
         // show confirm
         MessageBox.confirm("Confirm", "Do you intend to logout", listener);
         // show prompt msg
         MessageBox.prompt("Prompt", "Please tell us your name promptly", listener);
         // show progress msg
         final MessageBox pBar = MessageBox.progress("-Progress", "Calculating your comprehension so far", "wait ...");
         pBar.getProgressBar().auto();
         Timer pBarTimer = new Timer() {
 
             @Override
             public void run() {
                 pBar.close();
             }
         };
         pBarTimer.schedule(5000);
     }
 
     private void launchWindowManager() {
         GWT.log("### launchWindowManager()");
         // set up some "global" variables
         final Menu toolMenu = new Menu();
         ButtonBar buttonBar = new ButtonBar();
         final WindowManager mgr = WindowManager.get();
         final List<Window> windowList = new ArrayList<Window>();
 
         final WindowListener windowListener = new WindowListener() {
 
             @Override
             public void windowMinimize(WindowEvent we) {
                 final Window window = we.getWindow();
 
                 // make a menu-item for this window,
                 // but only once, so we'll search first
                 boolean found = false;
                 Iterator<Component> it = toolMenu.getItems().iterator();
                 while (it.hasNext()) {
                     Component cmp = (Component) it.next();
                     if (cmp instanceof MenuItem) {
                         MenuItem item = (MenuItem) cmp;
                         if (item.getHtml().equals(we.getWindow().getHeadingHtml())) {
                             found = true;
                             break;
                         }
                     }
                 }
 
                 if (!found) {
                     toolMenu.insert(new MenuItem(we.getWindow().getHeadingHtml(), new SelectionListener<MenuEvent>() {
 
                         @Override
                         public void componentSelected(MenuEvent ce) {
                             if (!window.isVisible()) {
                                 window.show();
                             }
                             mgr.bringToFront(window);
                         }
                     }), 0);
                 }
                 window.hide();
             }
         };
 
         // we'll use this to generate the windows
         Button addWindowBtn = new Button("Add Window", new SelectionListener<ButtonEvent>() { // anonymous SelectionListener<ButtonEvent>
 
             @Override
             public void componentSelected(ButtonEvent evt) {
                 int randInt = Random.nextInt(20);
                 Window dummy = new Window();
                 dummy.setClosable(false);
                 dummy.setSize(200, 120);
                 dummy.setMinimizable(true);
                 dummy.setMaximizable(true);
                 dummy.setId("win_" + randInt);
                 dummy.setHeadingHtml("Window " + randInt);
                 dummy.setContainer(centerPanel.getElement());
                 dummy.addWindowListener(windowListener);
 
                 dummy.show();
                 windowList.add(dummy);
             }
         });
         buttonBar.add(addWindowBtn);
         toolMenu.add(new SeparatorMenuItem());
 
         // add the menu-items to handle hide/show/cascade all				
         // hide-all is easy anyways
         toolMenu.add(new MenuItem("Hide All", new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent evt) {
                 mgr.hideAll();
             }
         }));
 
         // show-all only works because we kept
         // a local list of the windows we've made
         toolMenu.add(new MenuItem("Show All", new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent evt) {
                 // mgr.getWindows() || mgr.getStack() returns only visible windows
                 // so we always have an empty list after calling mgr.hideAll()
                 for (Window window : windowList) {
                     if (window != null && !window.isVisible()) {
                         window.show();
                     }
                 }
             }
         }));
         // cascade is tricky, yeah.
         // cascade is implemented by positioning
         // the windows atop each other, but 25x29 pixels
         // "more" from the last one
         toolMenu.add(new MenuItem("Cascade All", new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent evt) {
                 List<Window> windows = mgr.getWindows();
                 Window reference = null;
                 for (Window window : windows) {
                     window.show();
                     mgr.bringToFront(window);
                     window.center();
                     if (reference != null) {
                         window.setPosition(reference.getPosition(true).x + 25, reference.getPosition(true).y + 29);
                     }
                     reference = window;
                 }
             }
         }));
 
         // create a menu button and attach the menu to it
         Button toolBtn = new Button("Window Tools");	// correct book from SplitButton to this
         toolBtn.setMenu(toolMenu);
         buttonBar.add(toolBtn);
 
         // ### Add widgets to center layout container
         centerPanel.add(buttonBar, new FlowData(10));
 
     }
 
     private void addTabbedContent() {
 
         TabPanel tabPanel = new TabPanel();
         tabPanel.setHeight(250);
         tabPanel.setWidth(450);
         tabPanel.setCloseContextMenu(true);
        
        // set the bottom line
        tabPanel.setTabPosition(TabPanel.TabPosition.BOTTOM);
        
        for(int i = 1; i <=5; ++i) {
            String textFormat = "TabItem " + i;
            TabItem aTabItem = new TabItem(textFormat);
            aTabItem.setClosable(true);
            aTabItem.add(new HtmlContainer("<h1>Tab " + i + "</h1>"));
            tabPanel.add(aTabItem);
        }
 
 
         /*
          * Our Icons interface extends ImageBundle and declares three methods,
          * each named with the exact name of an image placed in the same package
          * as the Icons interface. Having created the interface, preferably in
          * its own java file, we proceed to used it with tabs and everywhere
          * else AbstractImagePrototype icons are used in GXT.
          *
          */
         Icons ICONS = GWT.create(Icons.class);
 
         String title = "DashBoard";
         TabItem homeTab = new TabItem(title);
        homeTab.setIcon(AbstractImagePrototype.create(ICONS.home()));
         homeTab.getHeader().setToolTip("Our " + title);
         homeTab.add(new HtmlContainer("<h1>Dashboard Tab</h1>"));
         tabPanel.add(homeTab);
 
         title = "Valued Customers";
         TabItem customersTab = new TabItem(title);
        customersTab.setIcon(AbstractImagePrototype.create(ICONS.people()));
         customersTab.getHeader().setToolTip("Our Really " + title);
         customersTab.setClosable(true);
         customersTab.add(new HtmlContainer("<h1>Customers Tab</h1>"));
         tabPanel.add(customersTab);
 
         title = "Data Reports";
         TabItem reportsTab = new TabItem(title);
        reportsTab.setIcon(AbstractImagePrototype.create(ICONS.orgchart()));
         reportsTab.getHeader().setToolTip("The customer " + title);
         reportsTab.setClosable(true);
         reportsTab.add(new HtmlContainer("<h1>Reports Tab</h1>"));
         tabPanel.add(reportsTab);
         /*
          * GxtCookbk is the application's entry point class. We access its main
          * content panel using the static GxtCookBk.getAppCenterPanel() call. We
          * add the tabPanel to the main content panel.
          */
 
         centerPanel.add(tabPanel);
 
 
     }
 
     /**
      * This is the entry point method.
      */
     @Override
     public void onModuleLoad() {
 
 //        this.addWindow();
 //        this.addDialog();
 //        this.addMsgBox();
 //        this.launchWindowManager();
         this.addTabbedContent();
 
 
 
         // Attach center layout contaners to the html host page.
         RootPanel.get().add(centerPanel);
         RootPanel.get().add(rightPanel);
 
 
 // *****************************************************************************
 // BEGIN: BoilerPlate        
 // *****************************************************************************        
 //    final Button sendButton = new Button( messages.sendButton() );
 //    final TextBox nameField = new TextBox();
 //    nameField.setText( messages.nameField() );
 //    final Label errorLabel = new Label();
 //
 //    // We can add style names to widgets
 //    sendButton.addStyleName("sendButton");
 //
 //    // Add the nameField and sendButton to the RootPanel
 //    // Use RootPanel.get() to get the entire body element
 //    RootPanel.get("nameFieldContainer").add(nameField);
 //    RootPanel.get("sendButtonContainer").add(sendButton);
 //    RootPanel.get("errorLabelContainer").add(errorLabel);
 //
 //    // Focus the cursor on the name field when the app loads
 //    nameField.setFocus(true);
 //    nameField.selectAll();
 //
 //    // Create the popup dialog box
 //    final DialogBox dialogBox = new DialogBox();
 //    dialogBox.setText("Remote Procedure Call");
 //    dialogBox.setAnimationEnabled(true);
 //    final Button closeButton = new Button("Close");
 //    // We can set the id of a widget by accessing its Element
 //    closeButton.getElement().setId("closeButton");
 //    final Label textToServerLabel = new Label();
 //    final HTML serverResponseLabel = new HTML();
 //    VerticalPanel dialogVPanel = new VerticalPanel();
 //    dialogVPanel.addStyleName("dialogVPanel");
 //    dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
 //    dialogVPanel.add(textToServerLabel);
 //    dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
 //    dialogVPanel.add(serverResponseLabel);
 //    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
 //    dialogVPanel.add(closeButton);
 //    dialogBox.setWidget(dialogVPanel);
 //
 //    // Add a handler to close the DialogBox
 //    closeButton.addClickHandler(new ClickHandler() {
 //      public void onClick(ClickEvent event) {
 //        dialogBox.hide();
 //        sendButton.setEnabled(true);
 //        sendButton.setFocus(true);
 //      }
 //    });
 //
 //    // Create a handler for the sendButton and nameField
 //    class MyHandler implements ClickHandler, KeyUpHandler {
 //      /**
 //       * Fired when the user clicks on the sendButton.
 //       */
 //      public void onClick(ClickEvent event) {
 //        sendNameToServer();
 //      }
 //
 //      /**
 //       * Fired when the user types in the nameField.
 //       */
 //      public void onKeyUp(KeyUpEvent event) {
 //        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 //          sendNameToServer();
 //        }
 //      }
 //
 //      /**
 //       * Send the name from the nameField to the server and wait for a response.
 //       */
 //      private void sendNameToServer() {
 //        // First, we validate the input.
 //        errorLabel.setText("");
 //        String textToServer = nameField.getText();
 //        if (!FieldVerifier.isValidName(textToServer)) {
 //          errorLabel.setText("Please enter at least four characters");
 //          return;
 //        }
 //
 //        // Then, we send the input to the server.
 //        sendButton.setEnabled(false);
 //        textToServerLabel.setText(textToServer);
 //        serverResponseLabel.setText("");
 //        greetingService.greetServer(textToServer, new AsyncCallback<String>() {
 //          public void onFailure(Throwable caught) {
 //            // Show the RPC error message to the user
 //            dialogBox.setText("Remote Procedure Call - Failure");
 //            serverResponseLabel.addStyleName("serverResponseLabelError");
 //            serverResponseLabel.setHTML(SERVER_ERROR);
 //            dialogBox.center();
 //            closeButton.setFocus(true);
 //          }
 //
 //          public void onSuccess(String result) {
 //            dialogBox.setText("Remote Procedure Call");
 //            serverResponseLabel.removeStyleName("serverResponseLabelError");
 //            serverResponseLabel.setHTML(result);
 //            dialogBox.center();
 //            closeButton.setFocus(true);
 //          }
 //        });
 //      }
 //    }
 //
 //    // Add a handler to send the name to the server
 //    MyHandler handler = new MyHandler();
 //    sendButton.addClickHandler(handler);
 //    nameField.addKeyUpHandler(handler);
 // *****************************************************************************
 // END: BoilerPlate        
 // *****************************************************************************                
     }
     // eo onModuleLoad
 }
