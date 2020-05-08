 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Jul 7, 2012
  * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.gui.plan;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.gui.Diagram;
 import edu.dhbw.sos.gui.plan.data.MovableTimeBlocks;
 import edu.dhbw.sos.gui.plan.data.TimeMarkerBlock;
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.observers.ISimUntilObserver;
 import edu.dhbw.sos.observers.IStatisticsObserver;
 import edu.dhbw.sos.observers.ITimeBlocksLengthObserver;
 import edu.dhbw.sos.observers.Observers;
 
 
 /**
  * The PaintArea of the PlanPanel draws the timeblocks, timemarker and diagram.
  * 
  * @author Nicolai Ommer <nicolai.ommer@gmail.com>
  * 
  */
 public class PPaintArea extends JPanel implements IStatisticsObserver, ISimUntilObserver, ITimeBlocksLengthObserver {
 	private static final long	serialVersionUID	= -3407230660397557204L;
 	
 	// time marker
 	private TimeMarkerBlock		timeMarkerBlock;
 	// time diagram within planPanel
 	private Diagram				timeDiagram;
 	// flag for reacting occordingly, when simulating
 	private boolean				simulateUntil		= false;
 	// reference to current course
 	private Course					course;
 	// swing component of timeblock area
 	private MovableTimeBlocks	movableTimeBlocks;
 	
 
 	/**
 	 * Create a paintarea for the planPanel.
 	 * 
 	 * @param course initial course
 	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 	 */
 	public PPaintArea(Course course) {
 		this.setLayout(new BorderLayout());
 		this.course = course;
 		timeMarkerBlock = new TimeMarkerBlock(course.getLecture().getLength());
 		Observers.subscribeTime(timeMarkerBlock);
 		Observers.subscribeTimeBlocksLength(timeMarkerBlock);
 		movableTimeBlocks = new MovableTimeBlocks(course.getLecture().getTimeBlocks());
 
 		TimeMarkerBlockPanel timeMarkerBlockPanel = new TimeMarkerBlockPanel();
 		this.add(timeMarkerBlockPanel, BorderLayout.SOUTH);
 		this.addMouseMotionListener(timeMarkerBlockPanel);
 
 		movableTimeBlocks.setPreferredSize(new Dimension(movableTimeBlocks.getPreferredSize().width,
 				this.getHeight() - 200));
 		this.add(movableTimeBlocks, BorderLayout.CENTER);
 		
 		timeDiagram = new Diagram(new LinkedList<Float>());
 		timeDiagram.setLocation(new Point(5, 10));
 		timeDiagram.setRescaleY(false);
 		timeDiagram.setMaxY(100);
 		
 		// subscribe to changes of the length of all time blocks (length of lecture), this is used for redrawing the time
 		// markers
 		Observers.subscribeTimeBlocksLength(this);
 	}
 	
 	
 	/**
 	 * Add a new timeblock to timeblock area
 	 * 
 	 * @param blocktype
 	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 	 */
 	public void addNewTimeBlock(BlockType blocktype) {
 		movableTimeBlocks.addNewTimeBlock(blocktype);
 	}
 
 	
 	/**
 	 * Will be called by JPanel.
 	 * Will do all the drawing.
 	 * Is called frequently, e.g. by repaint or if JPanel resizes, etc.
 	 */
 	@Override
 	public void paint(Graphics g) {
 		// initialize
 		Graphics2D ga = (Graphics2D) g;
 		ga.setColor(getBackground());
 		ga.clearRect(0, 0, this.getWidth(), this.getHeight());
 		ga.fillRect(0, 0, this.getWidth(), this.getHeight());
 
 		if (simulateUntil) {
 			URL iconUrl = getClass().getResource("/res/icons/sos_logo.png");
 			if (iconUrl != null) {
 				Image image = Toolkit.getDefaultToolkit().getImage(iconUrl);
 				ga.drawImage(image, this.getWidth() / 2 - 60, 5, this);
 			}
 		} else {
 			// will draw the movableTimeBlocks (because its a swing component and part of this panel)
 			super.paint(g);
 			
 			// draw diagram
 			ga.setColor(Color.black);
 			timeDiagram.draw(ga);
 		}
 		
 		// draw Timeline
 		ga.setPaint(Color.blue);
 		ga.drawLine(0, 140, this.getWidth() - 0, 140);
 
 		// Timemarkers
 		int mi = 60;
 		int totalLength = course.getLecture().getTimeBlocks().getTotalLength();
 
 		if (totalLength < 90)
 			mi = 15;
 		else if (totalLength < 180)
 			mi = 30;
 		else if (totalLength < 360)
 			mi = 60;
 		else if (totalLength < 720)
 			mi = 120;
 		else if (totalLength < 1440)
 			mi = 240;
 		else
 			mi = 360;
 		
 		double timemarkers = movableTimeBlocks.getScaleRatio() * mi;
 		if (timemarkers > 0.0) {
 			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 			Calendar timeCal = Calendar.getInstance();
 			timeCal.setTime(course.getLecture().getStart());
 			for (int i = 0; i < this.getWidth(); i += (int) timemarkers) {
 				String time = timeFormat.format(timeCal.getTime());
 				ga.drawLine(i, 135, i, 145);
 				ga.drawString(time, i + 2, 139);
 				timeCal.add(Calendar.MINUTE, mi);
 			}
 		}
 		
 		// TimeMarkerBlock
 		timeMarkerBlock.draw(ga, movableTimeBlocks.getScaleRatio());
 	}
 
 
 	@Override
 	public void updateSimUntil(boolean state) {
 		// this can enable a loading image, when simulation tooks too long.
 		// however, this is not really usefull, because 1. simulation usually takes not long
 		// and 2. it does not look better
 		// simulateUntil = state;
 	}
 
 
 	@Override
 	public void updateStatistics() {
 		timeDiagram.setHeight(this.getHeight() - 20);
 		timeDiagram.setWidth((int) (timeMarkerBlock.getTime() * movableTimeBlocks.getScaleRatio() + timeMarkerBlock
 				.getWidth() / 2));
 		LinkedList<Float> newData = new LinkedList<Float>();
 		
 		for (Entry<Integer, CalcVector> stat : course.getHistStatAvgStudentStates().entrySet()) {
 			newData.add(stat.getValue().getValueAt(0));
 		}
 		timeDiagram.setData(newData);
 		
 		this.repaint();
 	}
 	
 	
 	/**
 	 * Helper function for calling repaint from within a subclass
 	 * 
 	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 	 */
 	protected void myRepaint() {
 		this.repaint();
 	}
 
 	
 	/**
 	 * This class represents the area below the blocks,
 	 * where the block of the timemarker and the time axis
 	 * is located.
 	 * It is used to provide a area, to drag and move the timeMarker
 	 * 
 	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 	 * 
 	 */
 	private class TimeMarkerBlockPanel extends JPanel implements MouseMotionListener {
 		private static final long	serialVersionUID	= -6662154284384951511L;
 
 
 		public TimeMarkerBlockPanel() {
 			this.setPreferredSize(new Dimension(this.getWidth(), 15));
 		}
 		
 		
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if (e.getX() >= 0 && e.getX() < this.getWidth()) {
 				int time = (int) (e.getX() / movableTimeBlocks.getScaleRatio());
 				Observers.notifyTimeGUI(time * 60000); // Call the SimController to set the new time.
 			}
 		}
 		
 		
 		@Override
 		public void mouseMoved(MouseEvent e) {
 		}
 		
 	}
 	
 
 	@Override
 	public void lengthChanged(int newLengthMin) {
 		myRepaint();
 	}
 }
