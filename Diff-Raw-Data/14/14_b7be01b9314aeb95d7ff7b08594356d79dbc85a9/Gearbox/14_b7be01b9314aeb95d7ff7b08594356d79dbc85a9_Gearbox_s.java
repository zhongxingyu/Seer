 package org.usfirst.frc1923.systems.attachments;
 
 import org.usfirst.frc1923.systems.System;
 
 /**
  * A simple gearbox as a <code>System</code> attachment.
  * 
  * @author Aaron Weiss
  * @version 2.0
  * @since 2/9/15
  */
 public class Gearbox {
 	private final System system;
 	private final double[] gears;
 	private int gearIndex = 0;
 	
 	/**
 	 * Creates a gearbox attached to a system with the desired set of gears.
 	 * 
 	 * @param gears
 	 * 				the desired set of gears
 	 * @param system
 	 * 				the system to attach to
 	 */
 	public Gearbox(double[] gears, System system) {
 		this.gears = gears;
 		this.system = system;
 	}
 	
 	/**
 	 * Creates a gearbox attached to a system with an incremental gear set.
 	 * 
 	 * @param start
 	 * 				the starting value for the gear set
 	 * @param end
 	 * 				the ending value for the gear set
 	 * @param increment
 	 * 				the value to increment each gear by
 	 * @param system
 	 * 				the system to attach to
 	 */
 	public Gearbox(double start, double end, double increment, System system) {
 		int k = 0;
		this.gears = new double[(int) ((start - end) / increment)];
 		for (double i = start; i < end; i += increment) {
 			this.gears[k++] = i;
 		}
 		this.system = system;
 	}
 	
 	/**
 	 * Gears up the system by one.
 	 */
 	public void gearUp() {
 		if (this.gearIndex + 1 <= this.gears.length)
 			this.system.setMaxOutput(this.gears[++gearIndex]);
 	}
 	
 	/**
 	 * Gears down the system by one.
 	 */
 	public void gearDown() {
 		if (this.gearIndex - 1 >= 0)
 			this.system.setMaxOutput(this.gears[--gearIndex]);
 	}
 	
 	/**
 	 * Gets the system's current gear index.
 	 * 
 	 * @return the system's current gear number
 	 */
 	public int getGear() {
 		return this.gearIndex;
 	}
 	
 	/**
 	 * Sets the system's current gear index.
 	 * 
 	 * @param gearIndex
 	 * 				the desired gear index
 	 */
 	public void setGear(int gearIndex) {
 		this.gearIndex = gearIndex;
 		this.system.setMaxOutput(this.gears[this.gearIndex]);
 	}
 	
 	/**
 	 * Gets the current speed of the shooter.
 	 * 
 	 * @return the shooter's current speed
 	 */
 	public double getSpeed() {
 		return this.getSpeed(this.gearIndex);
 	}
 	
 	/**
 	 * Gets the speed of the specified shooter index.
 	 * 
 	 * @param gearIndex
 	 * 				the desired gear index to check
 	 * @return the speed of the specified gear
 	 */
 	public double getSpeed(int gearIndex) {
 		return this.gears[gearIndex];
 	}
 }
