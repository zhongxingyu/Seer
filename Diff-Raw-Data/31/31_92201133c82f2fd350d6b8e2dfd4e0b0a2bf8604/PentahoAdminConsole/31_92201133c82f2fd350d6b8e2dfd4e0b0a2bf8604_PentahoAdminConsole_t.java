 package org.pentaho.pac.client;
 
 import java.util.ArrayList;
 
 import org.pentaho.pac.client.home.HomePanel;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.utils.PacImageBundle;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PentahoAdminConsole extends DockPanel implements ClickListener {
   
   public static PacLocalizedMessages pacLocalizedMessages = (PacLocalizedMessages)GWT.create(PacLocalizedMessages.class);
   public static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   
   ArrayList componentActivationToggleButtons = new ArrayList();
   
   VerticalPanel leftVerticalPanel = new VerticalPanel();
   DockPanel centerPanel = new DockPanel();
   HorizontalPanel toolbar = new ConsoleToolbar();
   HorizontalPanel topPanel = new HorizontalPanel();
   
   TabPanel rightTabPanel = new TabPanel();
   DockPanel dockPanel = new DockPanel();
   DeckPanel deckPanel = new DeckPanel();
   protected AdministrationTabPanel adminTabPanel = new AdministrationTabPanel();
   
   
   protected HomePanel homePanel;
   CommonTasks commonTasks;
   
   public PentahoAdminConsole() {
     initTopPanel();
     
     homePanel = new HomePanel("http://www.pentaho.com/console_home"); //$NON-NLS-1$
     commonTasks = new CommonTasks();
     
     VerticalPanel leftPanel = new VerticalPanel();
     SimplePanel tempPanel = new SimplePanel();
    tempPanel.setStyleName("leftTabPanel_top"); //$NON-NLS-1$
     leftPanel.add(tempPanel);
     
     Label spacer = new Label();
     leftVerticalPanel.add(spacer);
     leftVerticalPanel.setCellHeight(spacer, "20px"); //$NON-NLS-1$
     
     initializeAdminDeck();
     
     adminTabPanel.setWidth("100%"); //$NON-NLS-1$
     adminTabPanel.setHeight("100%"); //$NON-NLS-1$
     adminTabPanel.getDeckPanel().setHeight("100%"); //$NON-NLS-1$
     
     spacer = new Label();
     leftVerticalPanel.add(spacer);
     leftVerticalPanel.setCellHeight(spacer, "50"); //$NON-NLS-1$
     leftVerticalPanel.add(commonTasks);
     spacer = new Label();
     leftVerticalPanel.add(spacer);
     leftVerticalPanel.setCellHeight(spacer, "100%"); //$NON-NLS-1$
     leftVerticalPanel.setStyleName("leftTabPanel"); //$NON-NLS-1$
     
     deckPanel.setStyleName("deckPanel"); //$NON-NLS-1$
     deckPanel.setWidth("100%"); //$NON-NLS-1$
     deckPanel.setHeight("100%"); //$NON-NLS-1$
 
     DockPanel deckPanelWrapper = new DeckPanelWrapper(deckPanel);
     deckPanelWrapper.setWidth("100%"); //$NON-NLS-1$
     deckPanelWrapper.setHeight("100%"); //$NON-NLS-1$
     
     DockPanel leftPanelWrapper = new LeftPanelWrapper(leftVerticalPanel);
     leftPanelWrapper.setHeight("100%"); //$NON-NLS-1$
     
     centerPanel.add(leftPanelWrapper, DockPanel.WEST);
     centerPanel.add(deckPanelWrapper, DockPanel.CENTER);
    centerPanel.setHeight("100%"); //$NON-NLS-1$
     
     centerPanel.setCellHeight(deckPanelWrapper, "100%"); //$NON-NLS-1$
     centerPanel.setCellWidth(deckPanelWrapper, "100%"); //$NON-NLS-1$
     
     //Main DockPanel
     setStyleName("main-panel"); //$NON-NLS-1$
     add(topPanel, DockPanel.NORTH);
    setCellWidth(topPanel, "100%"); //$NON-NLS-1$
     add(centerPanel, DockPanel.CENTER);
     setCellHeight(centerPanel, "100%"); //$NON-NLS-1$
     
     activateWidgetOnAdminDeck(getDefaultActiveAdminDeckWidget());
   }
   
   public void addWidgetToAdminDeck(String toggleButtonLabel, Widget widget) {
     ToggleButton toggleButton = new ToggleButton(toggleButtonLabel);
     toggleButton.setStylePrimaryName("leftToggleButtons"); //$NON-NLS-1$
     toggleButton.addClickListener(this);
     componentActivationToggleButtons.add(toggleButton);
     leftVerticalPanel.add(toggleButton);
    widget.setWidth("100%"); //$NON-NLS-1$
    widget.setHeight("100%"); //$NON-NLS-1$
     deckPanel.add(widget);
   }
   
   protected void initTopPanel() {
     SimplePanel logo = new SimplePanel();
     logo.setStyleName("logo"); //$NON-NLS-1$
    topPanel.setWidth("100%");  //$NON-NLS-1$
     topPanel.add(logo);
     topPanel.add(toolbar);
    topPanel.setCellWidth(toolbar, "100%"); //$NON-NLS-1$
   }
   
   public void onClick(Widget sender) {
     if (componentActivationToggleButtons.contains(sender)) {
       ToggleButton toggleButton = (ToggleButton)sender;
       if (!toggleButton.isDown()) {
         toggleButton.setDown(true);
       } else {
         for (int i = 0; i < componentActivationToggleButtons.size(); i++) {
           ToggleButton tmpToggleButton = (ToggleButton)componentActivationToggleButtons.get(i);
           tmpToggleButton.setDown(tmpToggleButton == sender);
         }
         
         int index = componentActivationToggleButtons.indexOf(sender);
         Widget component = deckPanel.getWidget(index);
         activateWidgetOnAdminDeck(component);
       }
     }
   }
   
   protected Widget getDefaultActiveAdminDeckWidget() {
     return homePanel;
   }
   
   protected void activateWidgetOnAdminDeck(Widget widget) {
     if (widget == adminTabPanel) {
       int selectedTab = adminTabPanel.getTabBar().getSelectedTab();
       if (selectedTab <= 0) {
         adminTabPanel.selectTab(AdministrationTabPanel.ADMIN_USERS_ROLES_TAB_INDEX);
       }
     }
     int componentIndex = deckPanel.getWidgetIndex(widget);
     if (componentIndex >= 0) {
       deckPanel.showWidget(componentIndex);
     }
   }
   
   protected void initializeAdminDeck() {
     addWidgetToAdminDeck(PentahoAdminConsole.MSGS.home(), homePanel);
     addWidgetToAdminDeck(PentahoAdminConsole.MSGS.administration(), adminTabPanel);
   }
   //TOP Toolbar
   private class ConsoleToolbar extends HorizontalPanel{
     
     private Label statusLabel;
     private Timer statusTimer = null;
     private SimplePanel serverIcon = new SimplePanel();
     private Image statusIcon = PacImageBundle.getBundle().statusWorkingIcon().createImage();
     
     public ConsoleToolbar(){
       super();
 
       setStyleName("toolbar"); //$NON-NLS-1$
       
       //Left end-cap
       SimplePanel leftCap = new SimplePanel();
      leftCap.setStyleName("toolbar_left"); //$NON-NLS-1$
       add(leftCap);
      this.setCellWidth(leftCap, "5px"); //$NON-NLS-1$
       
       //the body of the toolbar
       HorizontalPanel centerPanel = new HorizontalPanel();
       centerPanel.setStyleName("toolbar_center"); //$NON-NLS-1$
       add(centerPanel);
       
       //Right end-cap
       SimplePanel rightCap = new SimplePanel();
      rightCap.setStyleName("toolbar_right"); //$NON-NLS-1$
       add(rightCap);
      this.setCellWidth(rightCap, "6px"); //$NON-NLS-1$
       
       SimplePanel indicatorsPanel = new SimplePanel();
       indicatorsPanel.setStyleName("ToolBarIndicators"); //$NON-NLS-1$
       centerPanel.add(indicatorsPanel);
       
       SimplePanel indicatorsLeft = new SimplePanel();
       indicatorsLeft.setStyleName("indicators_left"); //$NON-NLS-1$
       indicatorsPanel.add(indicatorsLeft);
       
       HorizontalPanel indicatorsRight = new HorizontalPanel();
       indicatorsRight.setStyleName("indicators_right"); //$NON-NLS-1$
       indicatorsLeft.add(indicatorsRight);
      
       statusLabel = new Label(PentahoAdminConsole.MSGS.toolbarStatus());
       statusLabel.setStyleName("indicators_label"); //$NON-NLS-1$
 
       serverIcon.setStyleName( "biServerDeadIcon" ); //$NON-NLS-1$
       serverIcon.setTitle( PentahoAdminConsole.MSGS.biServerDead() );
       
       HorizontalPanel indicators = new HorizontalPanel();
       indicators.setStyleName("indicators"); //$NON-NLS-1$
       
       indicators.add(statusLabel);
       indicators.add(serverIcon);
       indicators.add(statusIcon);
       
       indicatorsRight.add(indicators);
       
       HorizontalPanel buttonsPanel = new HorizontalPanel();
       buttonsPanel.setStyleName("buttons"); //$NON-NLS-1$
       buttonsPanel.add(PacImageBundle.getBundle().refreshIcon().createImage());
       Image helpImage = PacImageBundle.getBundle().helpIcon().createImage();
       helpImage.addClickListener( new ClickListener() {
 
         public void onClick(Widget sender) {
           Window.open( "http://www.youtube.com/watch?v=9ibX3TejlZE", "Help", null ); //$NON-NLS-1$ //$NON-NLS-2$
         }
         
       });
       buttonsPanel.add( helpImage );
       centerPanel.add(buttonsPanel);
       centerPanel.setCellHorizontalAlignment(buttonsPanel, HorizontalPanel.ALIGN_RIGHT);
       centerPanel.setCellVerticalAlignment(buttonsPanel, HorizontalPanel.ALIGN_MIDDLE);
       
       statusTimer = new Timer() {
         public void run()
         {
           PacServiceFactory.getPacService().isBiServerAlive(
               new AsyncCallback() {
                 public void onSuccess( Object isAlive ) {
                   serverIcon.setStyleName( "biServerAliveIcon" ); //$NON-NLS-1$
                   serverIcon.setTitle( PentahoAdminConsole.MSGS.biServerAlive() );
                 }
                 public void onFailure(Throwable caught) {
                   serverIcon.setStyleName( "biServerDeadIcon" ); //$NON-NLS-1$
                   serverIcon.setTitle( PentahoAdminConsole.MSGS.biServerDead() );
                 }
               }
             );
         }
       };
       
       PacServiceFactory.getPacService().getBiServerStatusCheckPeriod(
         new AsyncCallback() {
           public void onSuccess( Object oCheckPeriod ) {
             int checkPeriod = ((Integer)oCheckPeriod ).intValue();
             if ( checkPeriod > 0 ) {
               statusTimer.scheduleRepeating( checkPeriod );
             }
           }
           public void onFailure(Throwable caught) {
             // otherwise we don't know what the status check period is, so don't schedule anything
           }
         }
       );
     }
   }
   
   private class CommonTasks extends SimplePanel{
     public CommonTasks(){
       super();
       VerticalPanel vertPanel = new VerticalPanel();
       
       SimplePanel headerPanel = new SimplePanel();
       headerPanel.setStyleName("CommonTasksHeader"); //$NON-NLS-1$
       
       Label header = new Label("Common Tasks");
       header.setStyleName("commonTasksHeaderText"); //$NON-NLS-1$
       headerPanel.add(header);
       vertPanel.add(headerPanel);
       
       VerticalPanel list = new VerticalPanel();
       list.add(new Hyperlink("Link 1 text","Link1")); //$NON-NLS-1$ //$NON-NLS-2$
       list.add(new Hyperlink("Link 2 text","Link2")); //$NON-NLS-1$ //$NON-NLS-2$
       list.add(new Hyperlink("Link 3 text","Link3")); //$NON-NLS-1$ //$NON-NLS-2$
       list.setStyleName("CommonTasksLinks"); //$NON-NLS-1$
       vertPanel.add(list);
       
       setStyleName("CommonTasks"); //$NON-NLS-1$
       this.add(vertPanel);
     }
   }
   
   private class DeckPanelWrapper extends DockPanel{
     public DeckPanelWrapper(DeckPanel deck){
       Grid grid = new Grid(3,2);
       grid.setWidth("100%"); //$NON-NLS-1$
       grid.setHeight("100%"); //$NON-NLS-1$
       grid.setCellPadding(0);
       grid.setCellSpacing(0);
       
       grid.getRowFormatter().setStyleName(0,"deckPanel-top-tr"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(0, 0, "deckPanel-n"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(0, 1, "deckPanel-ne"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(1, 0, "deckPanel-c"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(1, 1, "deckPanel-ce"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(2, 0, "deckPanel-s"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(2, 1, "deckPanel-se"); //$NON-NLS-1$
       
       grid.setWidget(1, 0, deck);
       add(grid, DockPanel.CENTER);
     }
   }
   
   private class LeftPanelWrapper extends DockPanel{
     public LeftPanelWrapper(VerticalPanel vertPanel){
       Grid grid = new Grid(3,1);
       grid.setWidth("100%"); //$NON-NLS-1$
       grid.setHeight("100%"); //$NON-NLS-1$
       grid.setCellPadding(0);
       grid.setCellSpacing(0);
       
       grid.getCellFormatter().setStyleName(0, 0, "deckPanel-nw"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(1, 0, "deckPanel-cw"); //$NON-NLS-1$
       grid.getCellFormatter().setStyleName(2, 0, "deckPanel-sw"); //$NON-NLS-1$
       
       grid.setWidget(1, 0, vertPanel);
       add(grid, DockPanel.CENTER);
     }
   }
   
   static public PacLocalizedMessages getLocalizedMessages() {
     return pacLocalizedMessages;
   }
   
   
 }
  
