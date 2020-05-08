 package net.cscott.sdr.webapp.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class SDRweb implements EntryPoint {
     final TextBox callEntry = new TextBox();
     final FlexTable callList = new FlexTable();
     final Label currentCall = new Label();
     final VerticalPanel topPanel = new VerticalPanel();
     final VerticalPanel canvasPanel = new VerticalPanel();
     DockPanel playBar = new DockPanel();
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
         // Make a command that we will execute from all leaves.
         // XXX: WRITE ME
         Command cmd = new Command() {
           public void execute() {
             Window.alert("You selected a menu item!");
           }
         };
 
         topPanel.setWidth("100%");
         // Menu bar
         MenuBar fileMenu = new MenuBar(true);
         fileMenu.addItem("New", cmd);
         fileMenu.addItem("Open", cmd);
         fileMenu.addItem("Save", cmd);
         fileMenu.addItem("Close", cmd);
 
         MenuBar programMenu = new MenuBar(true);
         programMenu.addItem("Basic", cmd);
         programMenu.addItem("Mainstream", cmd);
         programMenu.addItem("Plus", cmd);
 
         // Make a new menu bar, adding a few cascading menus to it.
         MenuBar menu = new MenuBar();
         menu.addItem("File", fileMenu);
         menu.addItem("Program", programMenu);
         topPanel.add(menu);
 
         DockPanel callBar = new DockPanel();
         callBar.setWidth("100%");
         Label callLabel = new Label("Call: ");
         callLabel.setHorizontalAlignment(Label.ALIGN_RIGHT);
         Button callGo = new Button("Go");
         callEntry.setWidth("100%");
         callEntry.setStyleName("callEntry");
         callBar.add(callLabel, DockPanel.LINE_START);
         callBar.add(callGo, DockPanel.LINE_END);
         callBar.add(callEntry, DockPanel.CENTER);
         callBar.setCellVerticalAlignment(callLabel, DockPanel.ALIGN_MIDDLE);
         callBar.setCellVerticalAlignment(callEntry, DockPanel.ALIGN_MIDDLE);
         callBar.setCellVerticalAlignment(callGo, DockPanel.ALIGN_MIDDLE);
         callBar.setCellWidth(callEntry, "100%");
         callBar.setSpacing(5);
         topPanel.add(callBar);
         RootPanel.get("div-top").add(topPanel);
 
         // call list panel
         callList.setText(0, 0, "Sequence");
         callList.getFlexCellFormatter().setColSpan(0, 0, 2);
         callList.getRowFormatter().setStyleName(0, "callListHeader");
         callList.setStyleName("callList");
         for (int i=1; i<50; i++) {
             callList.setText(i,0,"Call #"+i);
             Button removeButton = new Button("X");
             removeButton.setStyleName("removeButton");
             callList.setWidget(i, 1, removeButton);
             if (i==25) {
                 i++;
                 callList.setHTML(i, 0, "<hr/>");
                 callList.getFlexCellFormatter().setColSpan(i, 0, 2);
             }
         }
         RootPanel.get("div-calllist").add(callList);
 
         canvasPanel.add(currentCall);
 
        //playBar.setWidth("100%");
         Button playButton = new Button("Play"); // xxx replace with image
         Label playSlider = new Label("Slider"); // xxx replace with slider
         playBar.add(playButton, DockPanel.LINE_START);
         playBar.add(playSlider, DockPanel.CENTER);
         playBar.setCellWidth(playSlider, "100%");
         playBar.setCellHorizontalAlignment(playSlider, playBar.ALIGN_RIGHT);
         RootPanel.get("div-playbar").add(playBar);
 
         // canvas takes up all the rest of the space
         Label canvas = new Label("canvas");
         canvasPanel.add(canvas);
         canvasPanel.setCellHeight(canvas, "100%");
         RootPanel.get("div-canvas").add(canvasPanel);
 
         // we want to take advantage of the entire client area
         Window.setMargin("0px");
         // set a resize handler to keep all the dimensions in line.
         doResize();
         Window.addResizeHandler(new ResizeHandler() {
             public void onResize(ResizeEvent event) {
                 doResize(event.getWidth(), event.getHeight());
           }
         });
         // trigger this shortly after load
         Timer resizeTimer = new Timer() {
             @Override
             public void run() { doResize(); }
         };
         resizeTimer.schedule(1);
 
         // set up default text and handlers for callEntry
         callEntry.setText("Type a square dance call");
         callEntry.setSelectionRange(0, callEntry.getText().length());
     }
     void doResize() {
         doResize(Window.getClientWidth(), Window.getClientHeight());
     }
     void doResize(int width, int height) {
         int panelBottom = topPanel.getAbsoluteTop()+topPanel.getOffsetHeight();
         String style = "padding-top: "+panelBottom+"px;";
         RootPanel.get("div-calllist").getElement().setAttribute("style", style);
         RootPanel.get("div-canvas").getElement().setAttribute("style", style);
         // compensate for border of canvasPanel
         //width-=2; height-=2;
         int right = callList.getAbsoluteLeft()+callList.getOffsetWidth();
         right += 5; // padding on right of call list
         style = "padding-left: "+right+"px;";
         canvasPanel.getElement().setAttribute("style", style);
         playBar.getElement().setAttribute("style", style);
         canvasPanel.setHeight((height-panelBottom)+"px");
     }
 }
