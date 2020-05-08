 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.iterations;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import net.miginfocom.swing.MigLayout;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ViewMode;
 
 /**
  */
 public class IterationCalendarPanel extends JScrollPane {
 	
 	private ViewMode viewMode;
 	
 	private IterationCalendar calendarView;
 	
 	private JButton nextYear;
 	private JButton prevYear;
 	private JButton nextMonth;
 	private JButton prevMonth;
 	private JButton today;
 	
 	/**
 	 * Constructor for IterationCalendarPanel.
 	 * @param parent IterationPanel
 	 * @param vm ViewMode
 	 * @param displayIteration Iteration
 	 */
 	public IterationCalendarPanel(IterationPanel parent, ViewMode vm, Iteration displayIteration)
 	{
 		this.viewMode = vm;
 		JPanel contentPanel = new JPanel();
 		contentPanel.setLayout(new MigLayout());
 		
 		JPanel buttonPanel = new JPanel();
 		
 		nextYear = new JButton(">>");
 		nextYear.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		nextMonth = new JButton(">");
 		nextMonth.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		today = new JButton ("Today");
 		today.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		prevMonth = new JButton("<");
 		prevMonth.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		prevYear = new JButton("<<");
 		prevYear.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		setupButtonListeners();
 		
 		buttonPanel.add(prevYear);
 		buttonPanel.add(prevMonth);
 		buttonPanel.add(today);
 		buttonPanel.add(nextMonth);
 		buttonPanel.add(nextYear);
 		
 		contentPanel.add(buttonPanel, "alignx center, dock north");
 		 		
 		JPanel calendarPanel = new JPanel(new BorderLayout());
 		calendarView = new IterationCalendar(parent, viewMode, displayIteration);
 		calendarPanel.add(calendarView, BorderLayout.CENTER);
 				
 		calendarPanel.add(calendarView, BorderLayout.CENTER);		
 		contentPanel.add(calendarPanel, "alignx center, push, span");
 		
 		JPanel keyPanel = new JPanel(new MigLayout("height 200:200:200","", ""));
 		keyPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		JLabel keyLabel = new JLabel("Key:");
 				
 		JPanel otherIterations = new JPanel ();
 		otherIterations.add(new JLabel("Other Iterations"));
 		otherIterations.setBackground(Color.RED);
 		 		
 		JPanel endStartDatePanel = new JPanel();
 		endStartDatePanel.add(new JLabel("Start/End Days"));
 		endStartDatePanel.setBackground(Color.MAGENTA);
 				
 		JPanel thisIteration = new JPanel();
 		thisIteration.add(new JLabel("This Iteration"));
 		thisIteration.setBackground(Color.GREEN);
 				
 		keyPanel.add(keyLabel, "alignx center, push, span, wrap");
 		keyPanel.add(thisIteration, "alignx left, push, span, wrap");
 		keyPanel.add(otherIterations, "alignx left, push, span, wrap");
 		keyPanel.add(endStartDatePanel, "alignx left, push, span, wrap");
 		JPanel keyWrapper = new JPanel();
 		keyWrapper.add(keyPanel);
		contentPanel.add(keyWrapper, "east");
 
 		this.setViewportView(contentPanel);	
 	}
 	
 	/**
 	 * Adds action listeners to the year control buttons for the calendar view.
 	 */
 	private void setupButtonListeners()
 	{
 		nextYear.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nextYear();
 			}	
 		});
 		
 		prevYear.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				previousYear();
 			}
 		});
 		
 		today.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				today();
 			}
 		});
 		
 		prevMonth.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				previousMonth();
 			}
 		});
 		
 		nextMonth.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nextMonth();
 			}
 		});
 		
 	}
 	
 	/**
 	 * Switches the calendar to the previous year.
 	 */
 	private void previousYear()
 	{
 		Calendar cal = calendarView.getCalendar();
 		cal.add(Calendar.YEAR, -1);
 		calendarView.setFirstDisplayedDay(cal.getTime());
 	}
 	
 	/**
 	 * Switches the calendar to the current date
 	 */
 	private void today()
 	{
 		Calendar cal = Calendar.getInstance();
 		calendarView.setFirstDisplayedDay(cal.getTime());
 	}
 	
 	/**
 	 * Switches the calendar to the next month.
 	 */
 	private void nextMonth()
 	{
 		Calendar cal = calendarView.getCalendar();
 		cal.add(Calendar.MONTH, 1);
 		calendarView.setFirstDisplayedDay(cal.getTime());
 	}
 	
 	/**
 	 * Switches the calendar to the previous month.
 	 */
 	private void previousMonth()
 	{
 		Calendar cal = calendarView.getCalendar();
 		cal.add(Calendar.MONTH, -1);
 		calendarView.setFirstDisplayedDay(cal.getTime());
 	}
 	
 	/**
 	 * Switches the calendar to the next year.
 	 */
 	private void nextYear()
 	{
 		Calendar cal = calendarView.getCalendar();
 		cal.add(Calendar.YEAR, +1);
 		calendarView.setFirstDisplayedDay(cal.getTime());
 	}
 	
 	/**
 	 * Returns the iteration calendar
 	
 	 * @return the iteration calendar */
 	public IterationCalendar getIterationCalendar()
 	{
 		return this.calendarView;
 	}
 }
