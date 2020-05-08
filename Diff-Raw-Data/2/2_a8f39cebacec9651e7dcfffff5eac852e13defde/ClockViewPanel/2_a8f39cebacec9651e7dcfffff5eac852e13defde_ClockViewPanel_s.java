 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz Bï¿½rger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.ui.simplegui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.uniluebeck.imis.casi.simulation.engine.ISimulationClockListener;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 
 /**
  * This class extends JPanel and shows the simulation time in the simple GUI. It
  * allows also to scale the simulation time with a slider. In addition is
  * contains a control panel to pause and resume the simulation.
  * 
  * @author Moritz Bürger
  * 
  */
 @SuppressWarnings("serial")
 public class ClockViewPanel extends JPanel implements ISimulationClockListener,
 		ChangeListener {
 
 	private JLabel timeLabel;
 	private JSlider slider;
 
 	/**
 	 * The constructor adds the ClockViewPanel as itself as a listener of the
 	 * simulation clock.
 	 */
 	public ClockViewPanel() {
 
 		/** Add the clock panel as listener on the simulation clock */
 		SimulationClock.getInstance().addListener(this);
 
 		/** Set layout to FlowLayout */
 		this.setLayout(new GridLayout(1, 0));
 		
 		this.setBackground(ColorScheme.BACKGROUND_GUI);
 
 		/** Set the components */
 		this.setComponents();
 
 		/** Set preferred size */
 		this.setPreferredSize(new Dimension(0, 65));
 	}
 
 	/**
 	 * This method sets the components of the ClockViewPanel (JLabel showing the
 	 * date and time, JSlider scaling the simulation time).
 	 */
 	private void setComponents() {
 
 		/** JLabel showing the simulation time */
 		this.timeLabel = new JLabel();
 		this.timeLabel.setFont(new Font("sans", Font.BOLD, 16));
 		this.timeLabel
 				.setBorder(BorderFactory.createTitledBorder("Date/Time:"));
 		this.add(this.timeLabel);
 
 		/** JSlider to set the speed */
 		this.slider = new JSlider(JSlider.HORIZONTAL,
 				SimulationClock.MINIMUM_SCALE_FACTOR,
 				SimulationClock.MAXIMUM_SCALE_FACTOR,
				this.recalculateScaledValue(SimulationClock.DEFAULT_SCALE_FACTOR));
 
 		this.slider.setMajorTickSpacing(200);
 		this.slider.setMinorTickSpacing(100);
 //		this.slider.setSnapToTicks(true);
 		this.slider.setPaintTicks(true);
 		this.slider.addChangeListener(this);
 		this.slider.setBorder(BorderFactory.createTitledBorder("Time scaler:"));
 		this.slider.setBackground(ColorScheme.BACKGROUND_GUI);
 		this.slider.setForeground(Color.BLACK);
 		this.add(this.slider);
 
 		/** Add control panel */
 		ControlPanel controlPanel = new ControlPanel();
 		controlPanel.setBackground(ColorScheme.BACKGROUND_GUI);
 		this.add(controlPanel);
 	}
 
 	/**
 	 * This method gets a value between maximum and minimum and computes a
 	 * squared and scaled value between minimum and maximum.
 	 * 
 	 * @param value
 	 *            - value between maximum and minimum
 	 * @return squared value between minimum and maximum
 	 */
 	private int calculateScaledValue(int value) {
 
 		/** Save maximum and minimum in doubles */
 		double max = SimulationClock.MAXIMUM_SCALE_FACTOR;
 		double min = SimulationClock.MINIMUM_SCALE_FACTOR;
 
 		/** Compute values of the quadratic function */
 //		double a = (max - min) / (Math.pow(min, 2) - Math.pow(max, 2));
 //		double c = max - a * Math.pow(min, 2);
 		
 		double a = (max - min)/(1/min - 1/max);
 		double b = min - a/max;
 
 		/** Invert the value limits */
 //		double number = -value + max + min;
 
 		/** Square and scale the new value */
 //		value = (int) (a * Math.pow(value, 2) + c);
 		
 		value = (int) (a/value + b);
 
 		/** Check, if the new values are in range */
 		if ((int) value < min) {
 
 			/** Else return minimum */
 			return (int) min;
 		}
 		if ((int) value > max) {
 
 			/** Else return maximum */
 			return (int) max;
 		}
 
 		/** Return the new value */
 		return value;
 	}
 	
 	private int recalculateScaledValue(int value) {
 
 		/** Save maximum and minimum in doubles */
 		double max = SimulationClock.MAXIMUM_SCALE_FACTOR;
 		double min = SimulationClock.MINIMUM_SCALE_FACTOR;
 
 		/** Compute values of the quadratic function */
 		double a = (max - min)/(1/min - 1/max);
 		double b = min - a/max;
 
 		/** Invert the value limits */
 
 		value = (int) (a / value - b);
 
 		/** Check, if the new values are in range */
 		if ((int) value < min) {
 
 			/** Else return minimum */
 			return (int) min;
 		}
 		if ((int) value > max) {
 
 			/** Else return maximum */
 			return (int) max;
 		}
 
 		/** Return the new value */
 		return value;
 	}
 
 	@Override
 	public void timeChanged(final SimulationTime newTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				/** Calculate the new scale factor */
 				double newScaleFactor = ClockViewPanel.this
 						.calculateScaledValue(ClockViewPanel.this.slider
 								.getValue());
 
 				double scaleFactor = 1000 / newScaleFactor;
 				scaleFactor = Math.floor(scaleFactor * 100) / 100;
 
 				ClockViewPanel.this.timeLabel.setText(newTime
 						.getLocalizedDate()
 						+ " - "
 						+ newTime.getLocalizedTime()
 						+ " ("
 						+ scaleFactor
 						+ "x)");
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationPaused(final boolean pause) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationStopped() {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationStarted() {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent arg0) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				/** Calculate the new scale factor */
 				int newScaleFactor = ClockViewPanel.this.slider.getValue();
 				newScaleFactor = ClockViewPanel.this
 						.calculateScaledValue(newScaleFactor);
 
 				/** Only change if the value has changed */
 				if (SimulationClock.getInstance().getScaleFactor() != newScaleFactor) {
 
 					/** Set new value to SimulationClock */
 					SimulationClock.getInstance()
 							.setScaleFactor(newScaleFactor);
 
 				}
 
 				ClockViewPanel.this.timeChanged(SimulationClock.getInstance()
 						.getCurrentTime());
 
 			}
 		});
 	}
 
 }
