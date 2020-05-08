 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 5, 2012
  * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.gui.plan;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.text.DateFormatter;
 import javax.swing.text.DefaultFormatterFactory;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.Courses;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.gui.plan.buttons.ForwardBtn;
 import edu.dhbw.sos.gui.plan.buttons.LiveBtn;
 import edu.dhbw.sos.gui.plan.buttons.PlayBtn;
 import edu.dhbw.sos.gui.plan.buttons.RewindBtn;
 import edu.dhbw.sos.gui.plan.data.MovableTimeBlocks;
 import edu.dhbw.sos.helper.Messages;
 import edu.dhbw.sos.observers.ICurrentCourseObserver;
 import edu.dhbw.sos.observers.ISpeedObserver;
 import edu.dhbw.sos.observers.ITimeBlocksLengthObserver;
 import edu.dhbw.sos.observers.Observers;
 import edu.dhbw.sos.simulation.SimController;
 
 
 /**
  * The PlanPanel displays information about the lecture.
  * This is especially the timeblocks and whole time
  * 
  * It also contains some controls
  * 
  * @author Nicolai Ommer <nicolai.ommer@gmail.com>
  * 
  */
 public class PlanPanel extends JPanel implements ComponentListener, ISpeedObserver, ICurrentCourseObserver,
 		ITimeBlocksLengthObserver {
 	private static final Logger	logger				= Logger.getLogger(PlanPanel.class);
 	private static final long		serialVersionUID	= -1665784555881941508L;
 	private static final int		LABEL_WIDTH			= 80;
 	// paintArea is the part of the Panel, where some drawings have to be done
 	private PPaintArea				paintArea;
 	// label where speed of playback is shown
 	private JLabel						lblSpeed;
 	// reference to the timeblocks to display
 	// private TimeBlocks timeBlocks;
 	
 	private JFormattedTextField	txtFrom;
 	private JTextField				txtTo;
 	
 	private Courses					courses;
 	
 	
 	/**
 	 * Initialize the PlanPanel with GUIData
 	 * 
 	 * @param data general GUIData object with needed information for GUI
 	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 	 */
 	public PlanPanel(SimController simController, Courses courses) {
 		this.courses = courses;
 		Course course = courses.getCurrentCourse();
 		Observers.subscribeTimeGUI(simController);
 		// get data
 		// timeBlocks = new TimeBlocks(course.getLecture().getTimeBlocks());
 		
 		// init this Panel
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 		this.setLayout(new BorderLayout());
 		this.addComponentListener(this);
 		
 		MouseListener ml = new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 			
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if (e.getSource() instanceof JLabel) {
 					JLabel label = (JLabel) e.getSource();
 					if (label.getText().equals(" " + Messages.getString("BlockType.BREAK"))) {
 						paintArea.addNewTimeBlock(BlockType.pause);
 					} else if (label.getText().equals(" " + Messages.getString("BlockType.EXERCISE"))) {
 						paintArea.addNewTimeBlock(BlockType.exercise);
 					} else if (label.getText().equals(" " + Messages.getString("BlockType.GROUP"))) {
 						paintArea.addNewTimeBlock(BlockType.group);
 					} else if (label.getText().equals(" " + Messages.getString("BlockType.THEORY"))) {
 						paintArea.addNewTimeBlock(BlockType.theory);
 					} else {
 						assert false : "Label has another text then expected.";
 					}
 				}
 			}
 			
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 			
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 			
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 		};
 
 		// create panel
 		JPanel lPanel = new JPanel();
 		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.Y_AXIS));
 		
 		// create labels
 		JLabel lBreak = new JLabel(" " + Messages.getString("BlockType.BREAK"));
 		JLabel lExercise = new JLabel(" " + Messages.getString("BlockType.EXERCISE"));
 		JLabel lGroup = new JLabel(" " + Messages.getString("BlockType.GROUP"));
 		JLabel lTheory = new JLabel(" " + Messages.getString("BlockType.THEORY"));
 		
 		lBreak.setPreferredSize(new Dimension(LABEL_WIDTH, MovableTimeBlocks.BLOCK_HEIGHT));
 		lBreak.setVerticalTextPosition(JLabel.BOTTOM);
 		lBreak.addMouseListener(ml);
 		lExercise.setPreferredSize(new Dimension(LABEL_WIDTH, MovableTimeBlocks.BLOCK_HEIGHT));
 		lExercise.setVerticalTextPosition(JLabel.BOTTOM);
 		lExercise.addMouseListener(ml);
 		lGroup.setPreferredSize(new Dimension(LABEL_WIDTH, MovableTimeBlocks.BLOCK_HEIGHT));
 		lGroup.setVerticalTextPosition(JLabel.BOTTOM);
 		lGroup.addMouseListener(ml);
 		lTheory.setPreferredSize(new Dimension(LABEL_WIDTH, MovableTimeBlocks.BLOCK_HEIGHT));
 		lTheory.setVerticalTextPosition(JLabel.BOTTOM);
 		lTheory.addMouseListener(ml);
 		
 		lPanel.add(Box.createVerticalStrut(5));
 		lPanel.add(lTheory);
 		lPanel.add(lGroup);
 		lPanel.add(lExercise);
 		lPanel.add(lBreak);
 		lPanel.add(Box.createVerticalGlue());
 		this.add(lPanel, BorderLayout.WEST);
 		
 		// create sidePanel
 		JPanel sidePanel = new JPanel();
 		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
 		sidePanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black));
 		this.add(sidePanel, BorderLayout.EAST);
 		
 		// control panel (play, pause, etc.)
 		JPanel controlPanel = new JPanel();
 		PlayBtn btnPlay = new PlayBtn();
 		LiveBtn btnLive = new LiveBtn();
 		btnPlay.addActionListener(simController);
 		btnLive.addActionListener(simController);
 		Observers.subscribeSimulation(btnPlay);
 		Observers.subscribeEditMode(btnPlay);
 		Observers.subscribeEditMode(btnLive);
 		
 		// init paintArea
 		updateCurrentCourse(course);
 		
 		controlPanel.add(btnPlay);
 		controlPanel.add(btnLive);
 		sidePanel.add(controlPanel);
 		
 		// spacer (BoxLayout will be told to put any left space here
 		sidePanel.add(Box.createVerticalGlue());
 		
 		// speed controls
 		lblSpeed = new JLabel("1x");
 		ForwardBtn btnPlus = new ForwardBtn();
 		RewindBtn btnMinus = new RewindBtn();
 		JPanel speedPanel = new JPanel();
 		speedPanel.add(btnMinus);
 		speedPanel.add(lblSpeed);
 		speedPanel.add(btnPlus);
 		sidePanel.add(speedPanel);
 		btnPlus.addActionListener(simController);
 		btnMinus.addActionListener(simController);
 		
 		
 		// time
 		Observers.subscribeTimeBlocksLength(this);
 
 		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 		String start = timeFormat.format(course.getLecture().getStart());
 		Date endTime = new Date();
 		endTime.setTime(course.getLecture().getStart().getTime() + course.getLecture().getLength() * 60 * 1000);
 		String end = timeFormat.format(endTime);
 		
 		JLabel lblFrom = new JLabel(Messages.getString("Lecture.FROM"), SwingConstants.LEFT);
 		JLabel lblTo = new JLabel(Messages.getString("Lecture.TO"), SwingConstants.LEFT);
 		
 		
 		txtFrom = new JFormattedTextField(new DefaultFormatterFactory(new DateFormatter(timeFormat)));
 		
 		txtFrom.setText(start);
 		txtFrom.setColumns(5);
 		txtTo = new JTextField(end, 5);
 		txtTo.setEditable(false);
 		txtTo.setToolTipText(Messages.getString("Lecture.TOINFO"));
 		
 		
 		txtFrom.addKeyListener(new EnterKeyListener());
 		
 		txtFrom.setInputVerifier(new InputVerifier() {
 			@Override
 			public boolean verify(JComponent input) {
 				if ((input instanceof JFormattedTextField)) {
 					JFormattedTextField in = (JFormattedTextField) input;
 					
 					Pattern timeP = Pattern.compile("([0-1][0-9]|2[0-3]):[0-5][0-9]");
 					Matcher timeM = timeP.matcher(((JFormattedTextField) input).getText());
 					// checks if the colon is at the right point
 					// if (in.isEditValid() && (((in.getText(1, 1).equals(":") && in.getText().length() == 4)))
 					// || (in.getText(2, 1).equals(":") && in.getText().length() == 5)) {
 					if (timeM.matches()) {
 						in.setFocusLostBehavior(JFormattedTextField.COMMIT);
 						in.getText();
 						return true;
 					}
 					in.setFocusLostBehavior(JFormattedTextField.REVERT);
 					return false;
 				} else
 					return false;
 			}
 			
 			
 			@Override
 			public boolean shouldYieldFocus(JComponent input) {
 				if (!verify(input)) {
 					logger.debug("Time value is not correct");
 					input.setToolTipText(Messages.getString("Lecture.STARTFAIL"));
 					input.setForeground(Color.WHITE);
 					input.setBackground(Color.RED);
 					return false;
 				} else {
 					logger.debug("Time value is correct");
 					input.setToolTipText("");
 					input.setForeground(Color.BLACK);
 					input.setBackground(Color.WHITE);
 					updateStart((String) ((JFormattedTextField) input).getText());
 					updateEnd();
 					return true;
 				}
 			}
 			
 			
 		});
 		
 		lblFrom.setAlignmentX(Component.LEFT_ALIGNMENT);
 		lblTo.setAlignmentX(Component.LEFT_ALIGNMENT);
 		txtFrom.setAlignmentX(Component.LEFT_ALIGNMENT);
 		txtTo.setAlignmentX(Component.LEFT_ALIGNMENT);
 		txtFrom.setMaximumSize(new Dimension(40, 0));
 		txtTo.setMaximumSize(new Dimension(40, 0));
 		JPanel timePanel = new JPanel();
 		timePanel.add(lblFrom);
 		timePanel.add(txtFrom);
 		timePanel.add(lblTo);
 		timePanel.add(txtTo);
 		sidePanel.add(timePanel);
 		
 		
 	}
 	
 	
 	@Override
 	public void componentResized(ComponentEvent e) {
 		// paintArea.initMovableBlocks();
 		paintArea.repaint();
 	}
 	
 	
 	@Override
 	public void componentMoved(ComponentEvent e) {
 	}
 	
 	
 	@Override
 	public void componentShown(ComponentEvent e) {
 	}
 	
 	
 	@Override
 	public void componentHidden(ComponentEvent e) {
 	}
 	
 	
 	@Override
 	public void speedChanged(int speed) {
 		lblSpeed.setText(speed + "x");
 	}
 	
 	private class EnterKeyListener implements KeyListener {
 		@Override
 		public void keyPressed(KeyEvent e) {
 			
 			int key = e.getKeyCode();
 			
 			if (key == KeyEvent.VK_ENTER) {
 				if (e.getSource() instanceof JFormattedTextField)
 					((JFormattedTextField) e.getSource()).getInputVerifier().shouldYieldFocus((JComponent) e.getSource());
 			}
 		}
 		
 		
 		@Override
 		public void keyTyped(KeyEvent e) {
 		}
 		
 		
 		@Override
 		public void keyReleased(KeyEvent e) {
 		}
 	}
 	
 	
 	@Override
 	public void updateCurrentCourse(Course course) {
 		if (paintArea != null) {
 			this.remove(paintArea);
 			Observers.unsubscribeStatistics(paintArea);
 			Observers.unsubscribeSimUntil(paintArea);
 		}
 		paintArea = new PPaintArea(course);
 		this.add(paintArea, BorderLayout.CENTER);
 		Observers.subscribeStatistics(paintArea);
 		Observers.subscribeSimUntil(paintArea);
 
 		paintArea.repaint();
 		this.validate();
 	}
 	
 	
 	/**
 	 * Updates the start time of the current course lectures.
 	 * Only used by InputVerifer of Start TextField in GUI.
 	 * 
 	 * @param time
 	 * @author andres
 	 */
 	private void updateStart(String time) {
 		Date startDate;
 		try {
 			SimpleDateFormat timeFormat = (new SimpleDateFormat("HH:mm"));
 			startDate = timeFormat.parse(time);
 			String oldDate = timeFormat.format(courses.getCurrentCourse().getLecture().getStart());
 			courses.getCurrentCourse().getLecture().setStart(startDate);
 			paintArea.repaint();
 			logger.debug("Changed Start of Lecture from " + oldDate + " to " + time);
 		} catch (ParseException err) {
 			// Normally this exception shouldn't be thrown, because the input is already verified by the calling method
 			logger.warn("Could not parse time " + time);
 		}
 	}
 	
 	
 	/**
 	 * Updates the displayed end time. This value is only displayed and not saved.
 	 * Called after editing start time or when changing the lecture length.
 	 * 
 	 * @author andres
 	 */
 	private void updateEnd() {
 		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 		Date endTime = new Date();
 		endTime.setTime(courses.getCurrentCourse().getLecture().getStart().getTime()
 				+ courses.getCurrentCourse().getLecture().getLength() * 60 * 1000);
 		String end = timeFormat.format(endTime);
 		txtTo.setText(end);
 	}
 	
 	
 	@Override
 	public void lengthChanged() {
 		updateEnd();
 	}
 }
