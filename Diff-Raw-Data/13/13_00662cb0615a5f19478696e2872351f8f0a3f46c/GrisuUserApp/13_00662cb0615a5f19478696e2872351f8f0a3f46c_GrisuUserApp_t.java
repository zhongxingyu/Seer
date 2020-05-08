 package grisu.web;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import listeners.CustomValueChangeListener;
 import grisu.backend.model.job.JobStat;
 import grisu.settings.ServerPropertiesManager;
 //import grisu.settings.ServerPropertiesManager;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import vo.Users;
 
 import com.github.wolfie.refresher.Refresher;
 import com.github.wolfie.refresher.Refresher.RefreshListener;
 import com.vaadin.Application;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.VerticalSplitPanel;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 
 
 public class GrisuUserApp extends Application {
 	
 	private static Logger log = LoggerFactory.getLogger(Thread.currentThread().getClass());
 	
 	@Override
 	public void init() {
 
 		log.debug("Entered init()");
 		
 		Window mainWindow = new Window("Grisu");
 
 		final Table t2 = new Table("Job Table");
 
 		final UserTable userTab = new UserTable();
 
 		VerticalLayout layout= new VerticalLayout();
 
 //start
 		final Panel userTabPan = new Panel();
 		userTabPan.setCaption("Users");
 		
 		Button btnUserRefresh = new Button();
 		btnUserRefresh.setCaption("Refresh");
 		btnUserRefresh.addListener(new Button.ClickListener() {
 			
 			public void buttonClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 					Thread refresherThread = new Thread(){
 					public void run(){
 						log.debug("refresh starts ");
 						System.out.println("refresh start");
 						userTab.refresh();
 
 						log.debug("refresh ends ");
 						System.out.println("refresh end");
 					}
 				};
 				refresherThread.start();
 			}				
 		});
 		
 		userTabPan.addComponent(btnUserRefresh);
 		userTabPan.addComponent(userTab);
 //end		
 		
 		HorizontalSplitPanel horiSplitPanel = new HorizontalSplitPanel();
 		layout.addComponent(horiSplitPanel);
 		mainWindow.setContent(layout);
 //		horiSplitPanel.addComponent(userTab);
 		horiSplitPanel.addComponent(userTabPan);
 		userTab.setHeight("100%");
 		horiSplitPanel.setSizeFull();
 		horiSplitPanel.setHeight("100%");
 		layout.getComponent(0).setSizeFull();
 		layout.setExpandRatio(horiSplitPanel, 1.0f);
 
 		VerticalSplitPanel vertiSplitPanel = new VerticalSplitPanel();
 
 		final JobTable jobTab = new JobTable();
 		//vertiSplitPanel.addComponent(jobTab);
 		final Panel jobTabPan = new Panel();
 		jobTabPan.setScrollable(true);
 		
 		Button btnJobRefresh = new Button();
 		btnJobRefresh.setCaption("Refresh");
 		btnJobRefresh.addListener(new Button.ClickListener() {
 			
 			public void buttonClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 				
 				Users selectedUser = (Users) userTab.getSelectedUser();
 				if(selectedUser!=null){
 					jobTab.populate(selectedUser.getDn(), userTab);//, selectedUser.getActiveJobCount(), selectedUser.getRunningJobCount());
 					jobTabPan.setCaption("Jobs for "+selectedUser.getUserName());
 				}
 			}
 		});
 		
 		jobTabPan.addComponent(btnJobRefresh);
 		jobTabPan.setSizeFull();
 		jobTabPan.addComponent(jobTab);
 		jobTabPan.setCaption("Jobs for selected user");
 		//jobTabPan.getContent().setSizeFull();
 		vertiSplitPanel.addComponent(jobTabPan);
		vertiSplitPanel.setSplitPosition(60);
 		
 		horiSplitPanel.addComponent(vertiSplitPanel);
 		horiSplitPanel.setHeight("100%");
 		horiSplitPanel.getFirstComponent().setSizeFull();
 		
 		horiSplitPanel.setSplitPosition(40);
 
 		userTab.addListener(new ValueChangeListener() {
 			public void valueChange(ValueChangeEvent event) {
 				// TODO Auto-generated method stub
 				Users selectedUser = (Users) userTab.getSelectedUser();
 				if(selectedUser!=null){
 					jobTab.populate(selectedUser.getDn(), userTab);//, selectedUser.getActiveJobCount(), selectedUser.getRunningJobCount());
 					jobTabPan.setCaption("Jobs for "+selectedUser.getUserName());
 				}
 			}
 		});
 
 		final JobDetailsComponent jobDets = new JobDetailsComponent();
 
 		Panel jobDetPan = new Panel();
 		jobDetPan.setSizeFull();
 		jobDetPan.setContent(jobDets);
 		jobDetPan.setCaption("Job Details");
 		vertiSplitPanel.addComponent(jobDetPan);
 		
 		jobTab.addListener(new CustomValueChangeListener()  {
 			public void valueChange(ValueChangeEvent event) {
 				// TODO Auto-generated method stub
 				JobStat selectedJob = (JobStat) jobTab.getSelectedJob();
 				jobDets.populate(selectedJob);
 			}
 
 			public void fireValueChange() {
 				JobStat selectedJob = (JobStat) jobTab.getSelectedJob();
 				jobDets.populate(selectedJob);
 			}
 		});
 		
 		vertiSplitPanel.getSecondComponent().setSizeFull();
 		mainWindow.getContent().setSizeFull();
 		setMainWindow(mainWindow);
 
 		userTab.populate();
 		Map vars = new HashMap();
 		vars.put("state",true);
 		btnUserRefresh.changeVariables(this, vars);
 		
 		final Refresher refresher = new Refresher();
 		//refresher.setEnabled(false);
 		refresher.setRefreshInterval(ServerPropertiesManager.getDatabaseRefresh());
 		
 		refresher.setHeight("1%");
 		
 		refresher.addListener(new RefreshListener() {
 			
 			public void refresh(Refresher source) {
 				// TODO Auto-generated method stub
 				Thread refresherThread = new Thread(){
 					
 					public void run(){
 						refresher.setEnabled(false);
 						log.debug("refresh starts ");
 						userTab.refresh();
 
 						log.debug("refresh ends ");
 						refresher.setEnabled(true);
 					}
 				};
 				refresherThread.start();
 			}
 		});
 		
 		
 		mainWindow.addComponent(refresher);
 		refresher.setEnabled(false);
 		
 		log.debug("Exiting init()");
 	}
 
 }
