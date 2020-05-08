 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 18, 2012
  * Author(s): NicolaiO
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.gui.plan;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.course.lecture.TimeBlocks;
 import edu.dhbw.sos.course.statistics.IStatisticsObserver;
 import edu.dhbw.sos.gui.Diagram;
 import edu.dhbw.sos.gui.plan.MovableBlock.Areas;
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.simulation.ITimeObserver;
 import edu.dhbw.sos.simulation.SimController;
 
 
 /**
  * PaintArea for the PlanPanel.
  * This will draw the timeBlocks, etc.
  * 
  * @author NicolaiO
  * 
  */
 public class PPaintArea extends JPanel implements MouseListener, MouseMotionListener, IStatisticsObserver {
 	private static final long				serialVersionUID	= 5194596384018441495L;
 	private static final Logger			logger				= Logger.getLogger(PPaintArea.class);
 	
 	// list of all movable blocks
 	private MovableBlocks					movableBlocks		= new MovableBlocks();
 	// this block is set, when a block is moved by dragging with the mouse.
 	// it is set to the reference to the moving block
 	// it should be null, if no block is moved
 	private MovableBlock						moveBlock			= null;
 	private MovableBlock						rightBlock			= null;
 	private MovableBlock						leftBlock			= null;
 	private int									index					= -1;
 	private int									savedWidthLeft		= -1;
 	private int									savedWidthRight	= -1;
 	private TimeBlocks						tbs;
 	private Course								course;
 	
 	private TimeMarkerBlock					tmb;
 	
 	double										scaleRatio			= 1;
 	int											start;
 	
 	private Diagram							attDia;
 	
 	private Mode								mode					= null;
 	private Areas								area					= null;
 	
 	private LinkedList<ITimeObserver>	timeObservers		= new LinkedList<ITimeObserver>();
 	
 	
 	/**
 	 * Initialize PaintArea
 	 * 
 	 * @author NicolaiO
 	 */
 	public PPaintArea(SimController simController, Course course) {
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		tmb = new TimeMarkerBlock(0);
 		simController.subscribeTime(tmb);
 		init(course);
 	}
 	
 	
 	public void init(Course course) {
 		this.tbs = course.getLecture().getTimeBlocks();
 		this.course = course;
 		this.initMovableBlocks();
 		tmb.setLength(tbs.getTotalLength());
 		
 		attDia = new Diagram(new LinkedList<Float>());
 		attDia.setLocation(new Point(5, 10));
 		attDia.setRescaleY(false);
 		attDia.setMaxY(100);
 		
 		start = 0;
 	}
 	
 	
 	/**
 	 * TODO andres, add comment!
 	 * 
 	 * @param simController
 	 * @author andres
 	 */
 	public void subscribeTime(ITimeObserver to) {
 		timeObservers.add(to);
 	}
 	
 	
 	public void notifyTimeObservers() {
 		int timeInMilSec = tmb.getTime() * 60000;
 		for (ITimeObserver to : timeObservers) {
 			to.timeChanged(timeInMilSec);
 		}
 	}
 	
 	
 	/**
 	 * Create movableBlocks from timeBlocks
 	 * * set location
 	 * * set size
 	 * * set color
 	 * 
 	 * @param tbs TimeBlocks from which to initialize
 	 * @author NicolaiO
 	 */
 	public void initMovableBlocks() {
 		start = 0;
 		movableBlocks = new MovableBlocks();
 		scaleRatio = movableBlocks.init(tbs, start, this.getWidth());
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
 		ga.clearRect(0, 0, this.getWidth(), this.getHeight());
 		
 		// draw block
 		for (MovableBlock mb : movableBlocks) {
 			mb.draw(ga);
 		}
 		// if (moveBlock != null) {
 		// moveBlock.draw(ga);
 		// }
 		
 		
 		// draw Timeline
 		ga.setPaint(Color.blue);
 		ga.drawLine(start, 140, this.getWidth() - start, 140);
 		
 		// Timemarkers for each 60 min
 		double mi = 60.0;
 		double timemarkers = scaleRatio * mi;
 		// logger.debug(timemarkers + "");
 		if (timemarkers > 0.0) {
 			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 			Calendar timeCal = Calendar.getInstance();
 			timeCal.setTime(course.getLecture().getStart());
 			for (int i = start; i < this.getWidth(); i += (int) timemarkers) {
 				String time = timeFormat.format(timeCal.getTime());
 				ga.drawLine(i, 135, i, 145);
 				ga.drawString(time, i + 2, 139);
 				timeCal.add(Calendar.HOUR, 1);
 			}
 		}
 		
 		// TimeMarkerBlock
 		tmb.draw(ga);
 		
 		// draw diagram
 		// updateDiagram();
 		ga.setColor(Color.black);
 		attDia.draw(ga);
 	}
 	
 	
 	private void updateDiagram() {
 		attDia.setHeight(this.getHeight() - 20);
 		attDia.setWidth(tmb.getLength());
 		LinkedList<Float> newData = new LinkedList<Float>();
 		
 		for (Entry<Integer, CalcVector> stat : course.getHistStatState().entrySet()) {
 			newData.add(stat.getValue().getValueAt(0));
 		}
 		attDia.setData(newData);
 
 		this.repaint();
 	}
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		
 	}
 	
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		logger.debug("Lecture Planning: Drag&Drop started");
 		if (tmb.contains(e.getPoint())) {
 			mode = Mode.Time;
 			logger.debug("TimeMarkerBlock moving");
 			return;
 		}
 		
 		// Check if mouse clicked on a block and wants to drag
 		for (MovableBlock mb : movableBlocks) {
 			switch (mb.containsArea(e.getPoint())) {
 				case InArea:
 					mode = Mode.Move;
 					logger.debug("Block moving");
 					break;
 				case BorderLeft:
 				case BorderRight:
 					mode = Mode.Resize;
 					area = mb.containsArea(e.getPoint());
 					logger.debug("Block resizing");
 					break;
 				case NotInArea:
 				default:
 					continue;
 			}
 			
 			Point relML = new Point(e.getPoint().x - mb.x, e.getPoint().y - mb.y);
 			mb.setRelMouseLocation(relML);
 			moveBlock = mb;
 			// Any block must exist only one time in the list
 			index = movableBlocks.indexOf(moveBlock);
 			// moveBlock.printMbTb(index, "M");
 			// reset left and right blocks
 			if (index > 0) {
 				leftBlock = movableBlocks.get(index - 1);
 				savedWidthLeft = (int) leftBlock.getWidth();
 			}
 			if (index + 1 < movableBlocks.size()) {
 				rightBlock = movableBlocks.get(index + 1);
 				savedWidthRight = (int) rightBlock.getWidth();
 			}
 			// this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			break;
 		}
 	}
 	
 	private enum Mode {
 		Move,
 		Resize,
 		Time
 	}
 	
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		
 		for (MovableBlock mb : movableBlocks) {
 			mb.getTimeBlock().setLen((int) (mb.getWidth() / scaleRatio));
 			switch ((int) mb.getY()) {
 				case 10:
 					mb.getTimeBlock().setType(BlockType.pause);
 					break;
 				case 40:
 					mb.getTimeBlock().setType(BlockType.exercise);
 					break;
 				case 70:
 					mb.getTimeBlock().setType(BlockType.group);
 					break;
 				case 100:
 					mb.getTimeBlock().setType(BlockType.theory);
 					break;
 			}
 		}
 		
 		if (moveBlock != null) {
 			moveBlock = leftBlock = rightBlock = null;
 			index = savedWidthLeft = savedWidthRight = -1;
 			// tbs.clear();
 			// for (MovableBlock mb : movableBlocks) {
 			// tbs.add(mb.getTimeBlock());
 			// // mb.setRelMouseLocation(new Point(0, 0));
 			// }
 			// // this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			// area = null;
 			//
 			// initMovableBlocks();
 			// this.repaint();
 		}
 		if (mode == Mode.Time)
 			notifyTimeObservers();
 		mode = null;
 		return;
 	}
 	
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 	
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 	
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if (mode != null) {
 
 			switch (mode) {
 				case Move:
 					synchronized (this) {
 						dAndDMove(e.getPoint());
 					}
 					break;
 				case Resize:
 					// dAndDResize(e.getPoint());
 					break;
 				case Time:
 					dAndDTime(e.getPoint());
 					break;
 			}
 		}
 		this.repaint();
 	}
 	
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		MovableBlock movB = null;
 		for (MovableBlock mb : movableBlocks) {
 			if (mb.contains(e.getPoint())) {
 				movB = mb;
 				break;
 			}
 		}
 		if (movB != null) {
 			switch (movB.containsArea(e.getPoint())) {
 				case InArea:
 					this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 					break;
 				case BorderLeft:
 					this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
 					break;
 				case BorderRight:
 					this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
 					break;
 				case NotInArea:
 					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 					break;
 			}
 		} else
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	}
 	
 	
 	/**
 	 * Drag and Drop for TimeMarkerBlock
 	 * @param point
 	 * @author andres
 	 */
 	private void dAndDTime(Point e) {
 		int mmt_X = (int) Math.floor(e.getX() + tmb.getRelMouseLocation().getX() - tmb.getX());
 		// calculate new position of timeMarkerBloc
 		double x_mb = tmb.getLocation().getX();
 		double paWidth = this.getWidth();
 		if (x_mb < 0 && mmt_X < 0) {
 			return;
 		} else if ((x_mb + tmb.getWidth()) >= paWidth && mmt_X >= 0) {
 			return;
 		}
 		
 		double x = e.getX();
 		if (x < 0) {
 			x = 0.0;
 		} else if (x > paWidth) {
 			x = paWidth;
 		}
 		tmb.timeChanged((int) x + 5);
 		tmb.printTmb();
 	}
 
 
 	private void dAndDMove(Point e) {
 		// while mouse is pressed and moving, this will move the button
 		if (moveBlock != null) {
 			double paWidth = this.getWidth();
 
 			// Calculate the movement in x and the position of y. Negative Value means to
 			// the left and positive to the right.
 			double mmt_X = e.getX() - moveBlock.getRelMouseLocation().getX() - moveBlock.getX();
 			int pos_Y = (int) Math.floor(e.getY());
 			
 			/********************************/
 			// Vertical Movement
 			calcMBY(pos_Y);
 			
 			/********************************/
 			// Horizontal Movement
 			// 3 possiblilites: leftmost block (leftBlock is null), middle block, and rightmostblock (rightBlock is null)
 			if (!calcMBX(e, mmt_X))
 				return;
 			
 			/********************************/
 			// not the first block
 			if (leftBlock != null) {
 				leftBlock.addWidth(mmt_X);
 				leftBlock.printMbTb(index - 1, "L");
 				// Checks if the width of right Block is lower or equal then 0
 				if (leftBlock.getWidth() + mmt_X <= 0) {
 					int newIndex = 0;
 					
 					if (rightBlock != null) {
 						rightBlock.setWidth(savedWidthRight);
 						rightBlock.printMbTb(index + 1, "R");
 					}
 					leftBlock.setWidth(savedWidthLeft);
 					leftBlock.printMbTb(index - 1, "L");
 					
 					leftBlock.setLocation(moveBlock.getLocation().getX() + moveBlock.getWidth(), leftBlock.getY());
 					
 					// Swap the moving Block and its left neighbour
 					newIndex = movableBlocks.swap(index, index - 1);
 					rightBlock = movableBlocks.get(newIndex + 1);
 					if (newIndex > 0)
 						leftBlock = movableBlocks.get(newIndex - 1);
 					else
 						leftBlock = null;
 					
 					savedWidthRight = (int) rightBlock.getWidth();
 					if (newIndex + 2 < movableBlocks.size()) {
 						movableBlocks.get(newIndex + 2).setLocation(rightBlock.getX() + rightBlock.getWidth(),
 								movableBlocks.get(newIndex + 2).getY());
 					}
 					if (leftBlock != null) {
 						savedWidthLeft = (int) leftBlock.getWidth();
 					} else
 						savedWidthLeft = -1;
 					index = newIndex;
 					
 				} else {
 
 				}
 			}
 			
 			/********************************/
 			// Calculate width of right block and new position
 			// not the last block
 			if (rightBlock != null) {
 				
 				double x_P1 = rightBlock.getLocation().getX() + mmt_X;
 				if (x_P1 <= 0) {
 					x_P1 = 0;
 					mmt_X = 0;
 				} else if (x_P1 >= paWidth)
 					x_P1 = paWidth - rightBlock.getWidth();
 				rightBlock.setLocation(x_P1, rightBlock.getY());
 				
 				rightBlock.addWidth(-mmt_X);
 				rightBlock.printMbTb(index + 1, "R");
 
 				// Checks if the width of right Block is lower or equal then 0
 				if (rightBlock.getWidth() - mmt_X <= 0) {
 					int newIndex = 0;
 					
 					if (leftBlock != null) {
 						leftBlock.setWidth(savedWidthLeft);
 						leftBlock.printMbTb(index - 1, "L");
 					}
 					
 					rightBlock.setWidth(savedWidthRight);
 					rightBlock.printMbTb(index + 1, "R");
 					
 					
 					rightBlock.setLocation(moveBlock.getLocation().getX() + moveBlock.getWidth(), rightBlock.getY());
 					
 					// Swap the moving Block and its right neighbour
 					newIndex = movableBlocks.swap(index, index + 1);
 
 					leftBlock = movableBlocks.get(newIndex - 1);
 					if (newIndex + 1 < movableBlocks.size())
 						rightBlock = movableBlocks.get(newIndex + 1);
 					else
 						rightBlock = null;
 					
 					
 					savedWidthLeft = (int) leftBlock.getWidth();
 					
 					leftBlock.setLocation(movableBlocks.get(newIndex).getX() - leftBlock.getWidth(), leftBlock.getY());
 
 					if (newIndex + 1 < movableBlocks.size()) {
 						savedWidthRight = (int) rightBlock.getWidth();
 					} else
 						savedWidthRight = -1;
 					index = newIndex;
 				} else {
 					
 				}
 			}
 			
 			/********************************/
 			// Checks wether the width of left and right Blocks are lower or equal then 0
 			// if (index > 0 && leftBlock.width <= 0) {
 			// if (leftBlock != null && leftBlock.width <= 0) {
 			// int newIndex = 0;
 			//
 			// // if (index + 1 < movableBlocks.size()) {
 			// if (rightBlock != null) {
 			// rightBlock.setWidth(widthRight, scaleRatio);
 			// rightBlock.printMbTb(index + 1, "R");
 			// }
 			// leftBlock.setWidth(widthLeft, scaleRatio);
 			// leftBlock.printMbTb(index - 1, "L");
 			//
 			// leftBlock.setX(moveBlock.getLocation().getX() + moveBlock.width);
 			//
 			// // Swap the moving BLock and its left neighbour
 			// movableBlocks.swap(index, index - 1);
 			// newIndex = index - 1;
 			// // }
 			//
 			// widthRight = movableBlocks.get(newIndex + 1).width;
 			// if (newIndex + 2 < movableBlocks.size()) {
 			// movableBlocks.get(newIndex + 2).setX(
 			// movableBlocks.get(newIndex + 1).getX() + movableBlocks.get(newIndex + 1).width);
 			// }
 			// if (newIndex > 0) {
 			// widthLeft = movableBlocks.get(newIndex - 1).width;
 			// } else
 			// widthLeft = -1;
 			// index = newIndex;
 			//
 			/********************************/
 			// } else if (rightBlock != null && rightBlock.width <= 0) {
 			// else if (index + 1 < movableBlocks.size() && movableBlocks.get(index + 1).width <= 0) {
 			// int newIndex = 0;
 			//
 			// if (index > 0) {
 			// leftBlock.setWidth(widthLeft, scaleRatio);
 			// leftBlock.printMbTb(index - 1, "L");
 			// }
 			//
 			// // if (index + 1 < movableBlocks.size()) {
 			// rightBlock.setWidth(widthRight, scaleRatio);
 			// rightBlock.printMbTb(index + 1, "R");
 			//
 			//
 			// rightBlock.setX(moveBlock.getLocation().getX() + moveBlock.width);
 			//
 			// // Swap the moving BLock and its right neighbour
 			// movableBlocks.swap(index, index + 1);
 			// newIndex = index + 1;
 			// // }
 			//
 			// widthLeft = movableBlocks.get(newIndex - 1).width;
 			//
 			// movableBlocks.get(newIndex - 1).setX(
 			// movableBlocks.get(newIndex).getX() - movableBlocks.get(newIndex - 1).width);
 			//
 			// if (newIndex + 1 < movableBlocks.size()) {
 			// widthRight = movableBlocks.get(newIndex + 1).width;
 			// } else
 			// widthRight = -1;
 			// index = newIndex;
 			// }
 			
 			logger.debug("Dragged finished: index now:" + index);
 		}
 	}
 	
 	
 	private void dAndDResize(Point e) {
 		if (moveBlock != null) {
 			// 4 Possibilites: left area with and without neighbour; right area with and without neighbour
 			// MoveMenT X
 			// Calculate the movement in x. Negative Value means to
 			// the left and positive to the right.
 			int mmt_X = (int) Math.floor(e.getX() + moveBlock.getRelMouseLocation().getX() - moveBlock.getX());
 
 			// Blovalues, x,y,width,length
 			double moveBlock_X = 0.0;
 			double moveBlock_Y = 0.0;
 			double moveBlock_W = 0.0;
 			double moveBlock_L = 0.0;
 			if (index > 0) {
 				
 			}
 
 
 			// // left without neighbour
 			// if (area == Areas.BorderLeft && index == 0) {
 			//
 			// if (moveBlock.width + mmt_X < 20) {
 			// // do nothing anymore, because the block doesn't have to disappear
 			// // moveBlock.width = 20;
 			// // mmt_X = 0;
 			// // moveBlock.setLocation(moveBlock.getX(), moveBlock.getY());
 			// } else {
 			// moveBlock.addWidth(-mmt_X, scaleRatio);
 			// // moveBlock.setLocation(moveBlock.getX() + mmt_X, moveBlock.getY());
 			//
 			// }
 			// moveBlock.printMbTb(index, "LAM");
 			// }
 			// // left area with neighbour
 			// else if (area == Areas.BorderLeft && index > 0) {
 			// if (moveBlock.width + mmt_X < 20) {
 			// // do nothing anymore, because the block doesn't have to disappear
 			// } else {
 			// moveBlock.addWidth(mmt_X, scaleRatio);
 			// moveBlock.setLocation(moveBlock.getX() + mmt_X, moveBlock.getY());
 			// if (index - 1 < movableBlocks.size()) {
 			// leftBlock.addWidth(mmt_X, scaleRatio);
 			// leftBlock.setLocation(movableBlocks.get(index - 1).getX() - mmt_X, leftBlock.getY());
 			// }
 			// }
 			// moveBlock.printMbTb(index, "LAM");
 			// leftBlock.printMbTb(index - 1, "LAL");
 			// }
 			// // right area without neighbour
 			// else if (area == Areas.BorderRight && index + 1 == movableBlocks.size()) {
 			// if (moveBlock.getWidth() + mmt_X < 20) {
 			// // do nothing anymore, because the block doesn't have to disappear
 			// } else {
 			// moveBlock.addWidth(mmt_X, scaleRatio);
 			// }
 			// moveBlock.printMbTb(index, "RAM");
 			// }
 			// // right area with neighbour (index +1)
 			// else if (area == Areas.BorderRight && index + 1 < movableBlocks.size()) {
 			// if (moveBlock.width + mmt_X < 20) {
 			// // do nothing anymore, because the block doesn't have to disappear
 			// } else {
 			// moveBlock.addWidth(-mmt_X, scaleRatio);
 			// if (index + 1 < movableBlocks.size()) {
 			// rightBlock.addWidth(-mmt_X, moveBlock_L);
 			// rightBlock.setLocation(movableBlocks.get(index + 1).getX() - mmt_X, rightBlock.getY());
 			// }
 			// }
 			//
 			// moveBlock.printMbTb(index, "LAM");
 			// rightBlock.printMbTb(index + 1, "RAL");
 			// }
 		}
 		
 		
 		// if (moveBlock != null) {
 		// // Calculate the movement in x. Negative Value means to
 		// // the left and positive to the right.
 		// int mmt_X = (int) Math.floor(e.getX() + moveBlock.getRelMouseLocation().getX() - moveBlock.getX());
 		//
 		// if (area == Areas.BorderLeft) {
 		// if (index > 0) {
 		// // if (moveBlock.width > 20 && movableBlocks.get(index - 1).width > 20) {
 		// movableBlocks.get(index - 1).width += mmt_X;
 		// moveBlock.width -= mmt_X;
 		// // }
 		// if (movableBlocks.get(index - 1).width <= 20) {
 		// moveBlock.width -= 20 - movableBlocks.get(index - 1).width;
 		// movableBlocks.get(index - 1).width = 20;
 		// } else if (moveBlock.width <= 20) {
 		// movableBlocks.get(index - 1).width -= 20 - moveBlock.width;
 		// moveBlock.width = 20;
 		// }
 		// movableBlocks.get(index - 1).getTimeBlock()
 		// .setLen((int) (movableBlocks.get(index - 1).width / scaleRatio));
 		// movableBlocks.get(index - 1).printMbTb(index - 1, "L");
 		//
 		//
 		// moveBlock.getTimeBlock().setLen((int) (moveBlock.width / scaleRatio));
 		// moveBlock.printMbTb(index, "M");
 		// // TODO setLocation of moveBlock
 		// moveBlock.setLocation(moveBlock.getX() - mmt_X, moveBlock.getY());
 		// }
 		// }
 		// if (area == Areas.BorderRight) {
 		//
 		// if (index + 1 < movableBlocks.size()) {
 		// moveBlock.width += mmt_X;
 		// if (moveBlock.width < 20) {
 		// mmt_X -= moveBlock.width;
 		// moveBlock.width = 20;
 		// return;
 		// }
 		// moveBlock.getTimeBlock().setLen((int) (moveBlock.width / scaleRatio));
 		//
 		// movableBlocks.get(index + 1).width -= mmt_X;
 		// if (movableBlocks.get(index + 1).width < 20)
 		// movableBlocks.get(index + 1).width = 20;
 		// movableBlocks.get(index + 1).getTimeBlock()
 		// .setLen((int) (movableBlocks.get(index + 1).width / scaleRatio));
 		// movableBlocks.get(index + 1).printMbTb(index - 1, "R");
 		// // TODO setLocation of Block index+1
 		// movableBlocks.get(index + 1).setLocation(movableBlocks.get(index + 1).getX() + mmt_X,
 		// movableBlocks.get(index + 1).getY());
 		// }
 		// }
 		// }
 		
 		logger.trace("Resize of Blocks finished");
 	}
 	
 	
 	private boolean calcMBX(Point p, double moveX) {
 		// calculate new position of moveBlock
 		int x_mb = moveBlock.x;
 		int paWidth = this.getWidth();
 		if (x_mb <= 0 && moveX < 0) {
 			// e.getPoint().setLocation(0, e.getPoint().getY());
 			return false;
 		} else if ((x_mb + moveBlock.getWidth()) >= paWidth && moveX >= 0) {
 			// e.getPoint().setLocation(this.getWidth(), e.getPoint().getY());
 			return false;
 		}
 		
 		int x = p.x;
 		if (x < 0) {
 			// e.getPoint().setLocation(0, 0);
 			x = 0;
 		} else if (x > paWidth) {
 			// e.getPoint().setLocation(paWidth, 0);
 			x = paWidth;
 		}
		// if (leftBlock == null) {
		// if (rightBlock.getLocation().x > moveBlock.getX()) {
		// moveBlock.setWidth(rightBlock.getLocation().x);
		// }
		// } else if (rightBlock == null) {
		// moveBlock.setWidth((moveBlock.getWidth() - moveX));
 		// moveBlock.setLocation(new Point(x - moveBlock.getRelMouseLocation().x, moveBlock.y));
 		// } else {
 		moveBlock.setLocation(new Point(x - moveBlock.getRelMouseLocation().x, moveBlock.y));
 		// }
 		// moveBlock.printMbTb(index, "M");
 		return true;
 	}
 	
 	
 	private void calcMBY(int pos_Y) {
 		// FIXME other blocks are displayed randomly at other positions, after update() they are displayed correctly
 		// FIXME dynamic
 		if (pos_Y >= 1 && pos_Y < 40) {
 			pos_Y = 10;
 			// moveBlock.getTimeBlock().setType(BlockType.pause);
 			moveBlock.setColor(BlockType.pause.getColor());
 		} else if (pos_Y >= 41 && pos_Y < 70) {
 			pos_Y = 40;
 			// moveBlock.getTimeBlock().setType(BlockType.exercise);
 			moveBlock.setColor(BlockType.exercise.getColor());
 		} else if (pos_Y >= 71 && pos_Y < 100) {
 			pos_Y = 70;
 			// moveBlock.getTimeBlock().setType(BlockType.group);
 			moveBlock.setColor(BlockType.group.getColor());
 		} else if (pos_Y >= 101 && pos_Y < 130) {
 			pos_Y = 100;
 			// moveBlock.getTimeBlock().setType(BlockType.theory);
 			moveBlock.setColor(BlockType.theory.getColor());
 		}
 		// If Block is dragged out of the area it will not move
 		else if (pos_Y >= 130) {
 			// pos_Y = (int) moveBlock.getY();
 			pos_Y = 100;
 		} else if (pos_Y <= 0) {
 			pos_Y = 10;
 		}
 		moveBlock.setLocation(moveBlock.getX(), pos_Y);
 	}
 
 
 	@Override
 	public void updateStatistics() {
 		updateDiagram();
 		this.validate();
 	}
 
 }
