 package controller.view;
 
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import model.Activity;
 import model.Developer;
 import model.Project;
 import model.TimeEntry;
 import persistency.Database;
 import utils.ActionUtils;
 import utils.Dialog;
 import utils.TimeService;
 import view.ViewContainer;
 import view.state.AbstractViewState;
 import view.state.CalendarViewState;
 import controller.ControllerCollection;
 import controller.action.ChangeViewAction;
 import factory.ViewControllerFactory;
 
 public class CalendarViewController extends AbstractViewController {
 
 	private CalendarViewState viewState;
 	private Developer developer;
 	private TimeService timeService;
 	private Calendar currentStartDate;
 	private boolean isSelf;
 	private List<Developer> developers;
 	
 	public CalendarViewController(Database database, ViewContainer viewContainer, ControllerCollection controllers, int developerId) {
 		super(database, viewContainer, controllers);
 		this.developer = this.database.developer().read(developerId);
 		this.isSelf = developerId == this.controllers.getLoginController().getUser().getId();
 	}
 
 	@Override
 	public AbstractViewState getViewState() {
 		return viewState;
 	}
 
 	@Override
 	public void initialize() {
 		this.timeService = new TimeService();
 		this.viewState = new CalendarViewState();
 		
 		this.developers = this.database.developer().readAll();
 		
 		this.currentStartDate = this.timeService.getCurrentDateTime();
 		this.currentStartDate.add(Calendar.DAY_OF_YEAR, -2);
 		
 		this.viewState.setDeveloperName((this.isSelf ? "(You) " : "") + this.developer.getName());
 		this.viewState.setProjects(this.database.project().readAll());
 		this.viewState.setDateString(this.timeService.convertCalendarToInputString(this.currentStartDate));
 		this.viewState.setActivityTypes();
 		this.viewState.setFixedEnabled(false);
 		
 		this.initListeners();	
 		this.updateStartDate();
 	}
 
 	private void initListeners() {
 
 		ActionUtils.addListener(this.viewState.getRegisterButton(), this, "addRegisterTimeEntry");
 		ActionUtils.addListener(this.viewState.getReserveButton(), this, "addReserveTimeEntry");
 		
 		this.viewState.getBackButton().addActionListener(new ChangeViewAction(this.viewContainer, ViewControllerFactory.CreateMenuViewController()));
 		
 		this.viewState.getFixedToggleButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				boolean selected = viewState.getFixedState();
 				viewState.setFixedEnabled(selected);
 			}
 		});
 		
 		this.viewState.getProjectComboBox().addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent event) {
 				if (event.getStateChange() == ItemEvent.SELECTED)
 					updateActivities();
 			}
 		});
 		
 		this.viewState.getNextButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				currentStartDate.add(Calendar.DAY_OF_YEAR, 1);
 				updateStartDate();
 			}
 		});
 		
 		this.viewState.getPrevButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				currentStartDate.add(Calendar.DAY_OF_YEAR, -1);
 				updateStartDate();
 			}
 		});
 		
 		this.viewState.getPrevDeveloperButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (developers.size() <= 1) 
 					return;
 				
 				int prevDevId = 0;
 				for (int i = 0; i < developers.size(); i++) {
 					Developer dev = developers.get(i);
 					if (dev.getId() == developer.getId())
 						prevDevId = developers.get(i == 0 ? developers.size() - 1 : i - 1).getId();
 				}
 				
 				viewContainer.setViewState(ViewControllerFactory.CreateCalendarViewController(prevDevId));
 			}
 		});
 		
 		this.viewState.getNextDeveloperButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (developers.size() <= 1) 
 					return;
 				
 				int nextDevId = 0;
 				for (int i = 0; i < developers.size(); i++) {
 					Developer dev = developers.get(i);
 					if (dev.getId() == developer.getId())
 						nextDevId = developers.get(i == developers.size() - 1 ? 0 : i + 1).getId();
 				}
 				
 				viewContainer.setViewState(ViewControllerFactory.CreateCalendarViewController(nextDevId));
 			}
 		});
 	}
 	
 	public void addRegisterTimeEntry() {
 		String startString = viewState.getDateString().trim() + " " + viewState.getStartTimeString().trim();
 		String endString = viewState.getDateString().trim() + " " + viewState.getEndTimeString().trim();
 		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
         df.setLenient(false);
         Calendar startDate = Calendar.getInstance();
         Calendar endDate = Calendar.getInstance();
 
         Activity act = this.viewState.getSelectedActivity();
         if (act == null) {
         	Dialog.message("You must select project and activity");
         	return;
         }
         
         try {
             startDate.setTime(df.parse(startString));
             endDate.setTime(df.parse(endString));
             
             if ((startDate.get(Calendar.MINUTE) % 30) + endDate.get(Calendar.MINUTE) % 30 != 0) {
             	Dialog.message("Please use 30 minute time resolution");
             	return;
             }
             
             int actId = 0;
             if (this.viewState.getFixedState()) {
             	Activity fixedAct = this.database.activity().createFixedActivity(this.viewState.getSelectedActivityType(), "", 0, 0);
             	actId = fixedAct.getId();
             } 
             else {
             	actId = act.getId();
             }	
             TimeEntry entry = database.registerTime().create(startDate.getTimeInMillis(), 
         			endDate.getTimeInMillis(), 
     				this.developer.getId(), 
     				actId);
 			if (entry == null)
         		Dialog.message("Specified date and time is overlapping existing time registrations");
             
         }
         catch (ParseException e) {
         	Dialog.message("Specified date is invalid");
         }
 
 		this.updateStartDate();
 	}
 	
 	public void addReserveTimeEntry() {
 		String startString = viewState.getDateString().trim() + " " + viewState.getStartTimeString().trim();
 		String endString = viewState.getDateString().trim() + " " + viewState.getEndTimeString().trim();
 		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
         df.setLenient(false);
         Calendar startDate = Calendar.getInstance();
         Calendar endDate = Calendar.getInstance();
         
         Activity act = this.viewState.getSelectedActivity();
        if (act == null && !this.viewState.getFixedState()) {
         	Dialog.message("You must select project and activity");
         	return;
         }
         
         try {
             startDate.setTime(df.parse(startString));
             endDate.setTime(df.parse(endString));
             
             if ((startDate.get(Calendar.MINUTE) % 30) + endDate.get(Calendar.MINUTE) % 30 != 0) {
             	Dialog.message("Please use 30 minute time resolution");
             	return;
             }
             
     		TimeEntry entry = database.reserveTime().create(startDate.getTimeInMillis(), 
     				endDate.getTimeInMillis(), 
     				this.developer.getId(), 
     				act.getId());
     		
     		if (entry == null)
     			Dialog.message("Specified date and time is overlapping existing time reservations");
         } 
         catch (ParseException e) {
         	Dialog.message("Specified date is invalid");
         }
 
 		this.updateStartDate();
 	}
 	
 	private void updateActivities() {
 		Project selectedProject = this.viewState.getSelectedProject();
 		this.viewState.setActivities(this.database.activity().readAllWhereEquals("project_id", selectedProject.getId()));
 	}
 	
 	private void updateStartDate() {
 		List<TimeEntry> regTimeEntries = this.database.registerTime().readByDeveloperId(this.developer.getId());
 		List<TimeEntry> resTimeEntries = this.database.reserveTime().readByDeveloperId(this.developer.getId());
 		
 		this.viewState.clearTimeEntries();
 
 		DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
 		DateFormat labelFormat = new SimpleDateFormat("dd-MM");
 		Calendar iterDate = (Calendar) this.currentStartDate.clone();
 		for (int j = 0; j < 7; j++) {
 			// set date label
 			this.viewState.getDayLabels()[j].setText(labelFormat.format(iterDate.getTime()));
 			// Find registered times for the day
 			for (TimeEntry entry : regTimeEntries) {
 				String startDateFormat = df.format(entry.getStartDate().getTime());
 				String currentDateFormat = df.format(iterDate.getTime());
 
 				if (startDateFormat.equals(currentDateFormat)) {
 					this.viewState.addTimeEntry(entry, j, Color.ORANGE);
 				}
 			}
 			
 			// Find registered times for the day
 			for (TimeEntry entry : resTimeEntries) {
 				String startDateFormat = df.format(entry.getStartDate().getTime());
 				String currentDateFormat = df.format(iterDate.getTime());
 
 				if (startDateFormat.equals(currentDateFormat)) {
 					this.viewState.addTimeEntry(entry, j, Color.MAGENTA);
 				}
 			}
 			iterDate.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		
 		for (final TimeEntry te : this.viewState.getTimeEntryPanels().keySet()) {
 			final JPanel panel = this.viewState.getTimeEntryPanels().get(te);
 
 			panel.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (panel.getBackground().equals(Color.MAGENTA))
 						database.reserveTime().delete(te.getId());
 					else
 						database.registerTime().delete(te.getId());
 					
 					updateStartDate();
 				}
 			});
 		}
 	}
 }
