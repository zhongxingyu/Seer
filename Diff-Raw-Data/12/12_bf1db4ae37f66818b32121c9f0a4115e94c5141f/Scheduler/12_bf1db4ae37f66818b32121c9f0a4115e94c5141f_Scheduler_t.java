 package edu.nrao.dss.client;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.LoadEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.LoadListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TabPanelEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class Scheduler extends Viewport implements EntryPoint {
     public void onModuleLoad() {
         initLayout();
     }
 
     private void initLayout() {
         
         // project explorer tab
         tabPanel.add(addTab(pe, "Project Explorer", "Define and edit projects."));
         pe.setParent(this);
         
         tabPanel.add(addTab(ue, "User Explorer", "Define and edit users."));
         tabPanel.add(addTab(se, "Session Explorer", "Define and edit sessions."));
         tabPanel.add(addTab(we, "Window Explorer", "Define and edit windows."));
         tabPanel.add(addTab(wc, "Window Calendar", "Display Windows."));
         
        // schedule tab - we need to update the period explorer when it
         // comes into focus
         TabItem schTab = addTab(sch, "Schedule", "Manage the Schedule");
         schTab.addListener(Events.Select, new SelectionListener<TabPanelEvent>(){
         	@Override
         	public void componentSelected(TabPanelEvent tpe){
         		sch.scheduleExplorer.pe.loadData();
         	}
         });
         tabPanel.add(schTab);
         tabPanel.add(addTab(ta, "Time Accounting", "Manage Time Accounting"));
         tabPanel.add(addTab(pp, "Project Page", "Per Project"));
         tabPanel.add(addTab(rs, "Receiver Schedule", "Change the Receiver Schedule"));
         
         //  Register Observers
         pe.registerObservers((SessionColConfig) se.getPcodeConfig(), ta, pp);
         se.registerObservers((PeriodColConfig) sch.scheduleExplorer.pe.getSessionConfig(), we);
         ue.registerObservers(pp.getInvestigatorExplorer().getAddInvest());
 
         tabPanel.setAutoHeight(true);
         
         // Factor access
         factors = new Factors(sch.getFactorsDlg(), new FactorsTab(tabPanel));
 
         RootPanel rp = RootPanel.get();
         rp.add(new Image("http://www.gb.nrao.edu/~dss/images/banner.jpg"));
         rp.add(new Html("<br/><a href=\"http://www.gb.nrao.edu/~rmaddale/Weather/DSSOverview.html\" target=\"_blank\">Weather</a> | <a href=\"http://www.gb.nrao.edu/~rmaddale/Weather/CloudCoverage.html\" target=\"_blank\">Cloud Coverage</a> | <a href=\"mailto:helpdesk-dss@gb.nrao.edu\">Help Desk</a> | <a href=\"#\" onClick=\"window.open('/projects/ical', 'iCalendar', 'scrollbars=yes');return false;\">iCalendar</a>"));
         rp.add(tabPanel);
         
     }
     
     private TabItem addTab(ContentPanel container, String title, String toolTip) {
         TabItem item = new TabItem(title);
         item.setId(title);
         item.add(container);
         item.getHeader().setToolTip(toolTip);
         item.setLayout(new FitLayout());
         //item.setAutoHeight(true);
         return item;
     }
 
     // brings focus to the Project Page tab and selects given period.
     // this is called by any child widget (ex: Project Explorer) 
     public void showProject(String pcode) {
         TabItem ppTab =	tabPanel.findItem("Project Page", false);
     	tabPanel.setSelection(ppTab);
     	pp.setProject(pcode);
     }
     
     private final TabPanel         tabPanel = new TabPanel();
     private final ProjectExplorer  pe       = new ProjectExplorer();
     private final SessionExplorer  se       = new SessionExplorer();
     private final UserExplorer     ue       = new UserExplorer();
     private final WindowExplorer   we       = new WindowExplorer();
     private final WindowCalendar   wc       = new WindowCalendar();
     private final Schedule         sch      = new Schedule();
     private final TimeAccounting   ta       = new TimeAccounting();
     private Factors                factors;
     private final ProjectPage      pp       = new ProjectPage();
     private final ReceiverSchedule rs       = new ReceiverSchedule();
 }
