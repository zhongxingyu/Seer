 /**
  * 
  */
 package org.promasi.client_swing.gui.desktop.application.Scheduler;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.swingx.JXDatePicker;
 import org.joda.time.DateTime;
 import org.promasi.client_swing.gui.GuiException;
 import org.promasi.game.IGame;
 import org.promasi.game.company.CompanyMemento;
 import org.promasi.game.company.ICompanyListener;
 import org.promasi.game.project.ProjectMemento;
 
 /**
  * @author alekstheod
  *
  */
 public class DurationJPanel extends JPanel implements ICompanyListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * 
 	 */
 	private DateTime _projectAssignDate;
 	
 	/**
 	 * 
 	 */
 	private Lock _lockObject;
 	
 	/**
 	 * 
 	 */
 	private JSpinner _durationSpinner;
 	
 	
 	/**
 	 * 
 	 */
 	private JXDatePicker _startDatePicket;
 	
 	/**
 	 * 
 	 * @param game
 	 * @throws GuiException
 	 */
 	public DurationJPanel( IGame game )throws GuiException
 	{
 		if( game == null ){
 			throw new GuiException("Wrong argument game == null");
 		}
 	
 		_projectAssignDate = new DateTime();
 		_lockObject = new ReentrantLock();
 		setLayout(new BorderLayout());
 		
 		JPanel startDatePanel = new JPanel();
 		startDatePanel.setLayout(new BorderLayout());
 		_startDatePicket = new JXDatePicker();
 		_startDatePicket.setDate(new Date());
 		startDatePanel.add(_startDatePicket, BorderLayout.WEST);
 		startDatePanel.setBorder(BorderFactory.createTitledBorder("Start at"));
 		add(startDatePanel, BorderLayout.WEST);
 		setBorder(BorderFactory.createTitledBorder("Time scheduler"));
 		
 		JPanel endDatePanel = new JPanel();
 		endDatePanel.setLayout(new BorderLayout());
 		_durationSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
 		_durationSpinner.setPreferredSize(new Dimension(100,20));
 		endDatePanel.add(_durationSpinner, BorderLayout.EAST);
 		endDatePanel.setBorder(BorderFactory.createTitledBorder("Duration"));
 		add(endDatePanel, BorderLayout.EAST);
 		
 		game.addCompanyListener(this);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getFirstStep(){
 		Calendar startDate = Calendar.getInstance();
 		startDate.setTime(_startDatePicket.getDate());
 		
 		Calendar assignDate = Calendar.getInstance();
 		assignDate.setTime(_projectAssignDate.toDate());
 		
		long differenceFromStart = startDate.getTimeInMillis() - assignDate.getTimeInMillis();
		
		return (int)differenceFromStart/(60 * 60 * 1000);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getLastStep(){
 		return getFirstStep() + (Integer)_durationSpinner.getValue()*24;
 	}
 	
 	@Override
 	public void projectAssigned(String owner, CompanyMemento company,final ProjectMemento project, final DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try{
 					_lockObject.lock();
 					_projectAssignDate = dateTime;
 					_startDatePicket.setDate(dateTime.toDate());
 				}finally{
 					_lockObject.unlock();
 				}
 			}
 		});
 		
 	}
 
 	@Override
 	public void projectFinished(String owner, CompanyMemento company,ProjectMemento project, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void companyIsInsolvent(String owner, CompanyMemento company,ProjectMemento assignedProject, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onExecuteWorkingStep(String owner, CompanyMemento company, ProjectMemento assignedProject,final DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try{
 					_lockObject.lock();
 					if( dateTime.toDate().after(_startDatePicket.getDate()) ){
 						_startDatePicket.setDate(dateTime.plusDays(2).toDate());
 					}
 				}finally{
 					_lockObject.unlock();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void companyAssigned(String owner, CompanyMemento company) {
 		// TODO Auto-generated method stub
 		
 	}
 }
