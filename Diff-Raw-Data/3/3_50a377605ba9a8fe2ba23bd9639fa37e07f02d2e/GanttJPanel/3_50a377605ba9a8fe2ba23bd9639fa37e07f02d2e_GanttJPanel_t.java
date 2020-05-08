 package org.promasi.client_swing.gui.desktop.application.Scheduler;
 
 import java.awt.BorderLayout;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 
 import org.joda.time.DateTime;
 import org.promasi.client_swing.gui.GuiException;
 import org.promasi.game.IGame;
 import org.promasi.game.company.CompanyMemento;
 import org.promasi.game.company.DepartmentMemento;
 import org.promasi.game.company.EmployeeMemento;
 import org.promasi.game.company.EmployeeTaskMemento;
 import org.promasi.game.company.ICompanyListener;
 import org.promasi.game.company.IDepartmentListener;
 import org.promasi.game.project.ProjectMemento;
 import org.promasi.game.project.ProjectTaskMemento;
 
 import com.jidesoft.gantt.DateGanttChartPane;
 import com.jidesoft.gantt.DefaultGanttEntry;
 import com.jidesoft.gantt.DefaultGanttEntryRelation;
 import com.jidesoft.gantt.DefaultGanttModel;
 import com.jidesoft.gantt.GanttChartPane;
 import com.jidesoft.gantt.GanttEntryRelation;
 import com.jidesoft.grid.TableUtils;
 import com.jidesoft.range.TimeRange;
 import com.jidesoft.scale.DateScaleModel;
 import com.jidesoft.scale.ResizePeriodsPopupMenuCustomizer;
 import com.jidesoft.scale.VisiblePeriodsPopupMenuCustomizer;
 import com.jidesoft.swing.CornerScroller;
 
 public class GanttJPanel extends JPanel  implements ICompanyListener, IDepartmentListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * 
 	 */
 	private Lock _lockObject;
 	
 	/**
 	 * 
 	 */
 	private DateTime _projectAssignDate;
 	
 	/**
 	 * 
 	 */
 	private GanttChartPane< Date, DefaultGanttEntry<Date> > _ganttPane;
 	
 	/**
 	 * 
 	 */
 	public static final int CONST_DURATION_MULTIPLIER = 10;
 	
 	/**
 	 * @throws GuiException 
 	 * 
 	 */
 	public GanttJPanel( IGame game ) throws GuiException{
 		if( game == null ){
 			throw new GuiException("Wrong argument game");
 		}
 
 		_ganttPane= new DateGanttChartPane<>(new DefaultGanttModel<Date, DefaultGanttEntry<Date>>());
 		_ganttPane.getGanttChart().setShowGrid(false);
 		_lockObject = new ReentrantLock();
 		setBorder(BorderFactory.createTitledBorder("Scheduler"));
 		setLayout(new BorderLayout());
 		
         JScrollPane chartScroll = new JScrollPane(_ganttPane);
         chartScroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new CornerScroller(chartScroll));
         
 		add(chartScroll, BorderLayout.CENTER);
 		game.addCompanyListener(this);
 		game.addDepartmentListener(this);
 		
 		_ganttPane.getGanttChart().setEditable(false);
 		_ganttPane.getTreeTable().setEnabled(false);
 		_ganttPane.getGanttChart().getScaleArea().addPopupMenuCustomizer(new VisiblePeriodsPopupMenuCustomizer<Date>());
 		_ganttPane.getGanttChart().getScaleArea().addPopupMenuCustomizer(new ResizePeriodsPopupMenuCustomizer<Date>(_ganttPane.getGanttChart()));
 		_ganttPane.getGanttChart().setEditable(false);
 		TableUtils.autoResizeAllColumns(_ganttPane.getTreeTable());
 	}
 	
 	/**
 	 * 
 	 * @param scheduledTasks
 	 * @param assignedProject
 	 */
 	private void drawGanttDiagramm( Map<String, EmployeeTaskMemento> scheduledTasks, ProjectMemento assignedProject, DateTime dateTime ){
 		try{
 			_lockObject.lock();
 			
 			DefaultGanttModel<Date, DefaultGanttEntry<Date>> model = new DefaultGanttModel<Date, DefaultGanttEntry<Date>>();
 		    
 	        DateScaleModel scaleModel = new DateScaleModel( );
 
 		    model.setScaleModel(scaleModel);
 
 		    Calendar projectStartDate = Calendar.getInstance(Locale.getDefault());
 		    projectStartDate.setTime(_projectAssignDate.toDate());
 		    
 		    Calendar projectEndDate = Calendar.getInstance(Locale.getDefault());
 		    projectEndDate.setTime(_projectAssignDate.plusHours(assignedProject.getProjectDuration()/CONST_DURATION_MULTIPLIER).toDate());
 		    
             model.setRange(new TimeRange(projectStartDate, projectEndDate));

 			if(_projectAssignDate != null ){
 				Map<String,  DefaultGanttEntry<Date>> ganttTasks = new TreeMap<String,  DefaultGanttEntry<Date>>();
 				for (Map.Entry<String, EmployeeTaskMemento> entry : scheduledTasks.entrySet() ){
 					EmployeeTaskMemento employeeTask = entry.getValue();
 					Date startDate = _projectAssignDate.plusHours( employeeTask.getFirstStep() ).toDate();
 					Calendar startTime = Calendar.getInstance(Locale.getDefault());
 					startTime.setTime(startDate);
 					
 					Calendar endTime = Calendar.getInstance(Locale.getDefault());
 					endTime.setTime(_projectAssignDate.plusHours( employeeTask.getLastStep()).toDate());
 	
 					DefaultGanttEntry<Date> newTask = new DefaultGanttEntry<Date>(employeeTask.getTaskName(), Date.class, new TimeRange(startTime, endTime), 0);
 					ganttTasks.put(employeeTask.getTaskName(), newTask);
 					
 					if( assignedProject.getProjectTasks() != null && assignedProject.getProjectTasks().containsKey(employeeTask.getProjectTaskName() ) ){
 						ProjectTaskMemento prjTask = assignedProject.getProjectTasks().get(employeeTask.getProjectTaskName());
 						newTask.setCompletion(prjTask.getProgress()/100.0);
 					}
 
 					model.addGanttEntry(newTask);
 				}
 				
 				
 				for (Map.Entry<String, EmployeeTaskMemento> entry : scheduledTasks.entrySet() ){
 					EmployeeTaskMemento employeeTask = entry.getValue();
 					if(  employeeTask.getDependencies() != null ){
 						for( String taskName : employeeTask.getDependencies() ){
 							if( ganttTasks.containsKey(taskName) && ganttTasks.containsKey(employeeTask.getTaskName() ) ){
 								model.getGanttEntryRelationModel().addEntryRelation(new DefaultGanttEntryRelation<DefaultGanttEntry<Date>>(ganttTasks.get(taskName), ganttTasks.get(employeeTask.getTaskName()),  GanttEntryRelation.ENTRY_RELATION_FINISH_TO_START));
 							}
 						}
 					}
 				}
 				
 				_ganttPane.setGanttModel(model);
 			}
 		}finally{
 			_lockObject.unlock();
 		}
 	}
 
 	@Override
 	public void employeeDischarged(String director, DepartmentMemento department) {
 	}
 
 	@Override
 	public void employeeHired(String director, DepartmentMemento department) {
 	}
 
 	@Override
 	public void tasksAssigned(String director, DepartmentMemento department) {
 	}
 
 	@Override
 	public void tasksAssignFailed(String director, DepartmentMemento department) {
 	}
 
 	@Override
 	public void departmentAssigned(String director, DepartmentMemento department) {
 	}
 	
 	/**
 	 * 
 	 * @param owner
 	 * @param company
 	 * @param project
 	 * @param dateTime
 	 */
 	@Override
 	public void projectAssigned(String owner, CompanyMemento company,final ProjectMemento project, final DateTime dateTime) {
 		try{
 			_lockObject.lock();
 			_projectAssignDate = dateTime;
 		}finally{
 			_lockObject.unlock();
 		}
 	}
 
 
 	@Override
 	public void projectFinished(String owner, CompanyMemento company,
 			ProjectMemento project, DateTime dateTime) {
 	}
 
 	@Override
 	public void companyIsInsolvent(String owner, CompanyMemento company,
 			ProjectMemento assignedProject, DateTime dateTime) {
 	}
 
 	@Override
 	public void onExecuteWorkingStep(String owner, final CompanyMemento company, final ProjectMemento assignedProject, final DateTime dateTime) {
 		SwingUtilities.invokeLater( new Runnable() {
 			
 			@Override
 			public void run() {
 				try{
 					_lockObject.lock();
 
 					final Map<String, EmployeeTaskMemento> projectTasks = new TreeMap<String, EmployeeTaskMemento>();
 					if( company != null && company.getITDepartment() != null && company.getITDepartment().getEmployees() != null ){
 						Map<String, EmployeeMemento> employees = company.getITDepartment().getEmployees();
 						for (Map.Entry<String, EmployeeMemento> entry : employees.entrySet()){
 							Map<String, EmployeeTaskMemento> tasks = entry.getValue().getTasks();
 							for (Map.Entry<String, EmployeeTaskMemento> taskEntry : tasks.entrySet() ){
 								if( !projectTasks.containsKey(taskEntry.getValue().getTaskName())){
 									projectTasks.put(taskEntry.getValue().getTaskName(), taskEntry.getValue() );
 								}
 							}
 						}
 					}
 
 					drawGanttDiagramm(projectTasks, assignedProject, dateTime);
 					
 				}finally{
 					_lockObject.unlock();
 				}
 				
 			}
 		});
 	}
 
 	@Override
 	public void companyAssigned(String owner, CompanyMemento company) {
 	}
 
 }
