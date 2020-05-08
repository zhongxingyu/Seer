 /*
 * This file is part of LinkJVM.
 *
 * Java Framework for the KIPR Link
 * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package libkovan;
 
 import libkovan.jni.LinkJVM;
 
 /**
  * This class is provides a motor instance with all functions to control the motor.
  * There is one additional method to turn off all motor.
  * 
  * @author Markus Klein
  *
  */
 
 public class Motor {
 	private int motorPort;
 	
 	/**
 	 * Generates a new motor instance on the specified motor port.
 	 * 
 	 * @param motorPort the port of the motor port
 	 */
 	public Motor(int motorPort){
 		this.motorPort = motorPort;
 	}
 	
 	/**
 	 * Turns the motor off.
 	 */
 	public void off(){
 		LinkJVM.off(motorPort);
 	}
 	
 	/**
 	 * This methods waits until the motor completes any executes or position control moves.
 	 */
 	public void blockMotorDone(){
 		LinkJVM.bmd(motorPort);
 	}
 	
 	/**
 	 * Turns the motor speed to full backward.
 	 */
 	public void bk(){
 		LinkJVM.bk(motorPort);
 	}
 	
 	/**
 	 * Resets the motor position counter to 0.
 	 */
 	public void clearPosistionCounter(){
 		LinkJVM.clear_motor_position_counter(motorPort);
 	}
 	
 	/**
 	 * Turns the motor speed to full forward.
 	 */
 	public void fd(){
 		LinkJVM.fd(motorPort);
 	}
 	
 	/**
 	 * Prevents the current motor movement.
 	 */
 	public void freeze(){
 		LinkJVM.freeze(motorPort);
 	}
 	
 	/**
 	 * 
 	 * @return 
 	 */
 	public int getDone(){
 		return LinkJVM.get_motor_done(motorPort);
 	}
 	
 	/**
 	 * Returns the current position counter.
 	 * 
 	 * @return the current motor position counter
 	 */
 	public int getCounterPosition(){
 		return LinkJVM.get_motor_position_counter(motorPort);
 	}
 	
 	/**
 	 * Moves the motor at velocity with the specified ticks per seconds.
 	 * 
 	 * @param vel velocity in ticks per second (range from -1000 to 1000)
 	 */
 	public void moveAtVelecity(int vel){
 		LinkJVM.mav(motorPort, vel);
 	}
 	
 	/**
 	 * Moves the motor from its current position to current position + pos.
 	 * 
 	 * @param speed speed in ticks (range from -1000 to 1000)
 	 * @param pos difference of the current and the final position
 	 */
 	public void moveRelativePosition(int speed, int pos){
 		LinkJVM.move_relative_position(motorPort, speed, pos);
 	}
 	
 	/**
 	 * Moves the motor to the specified position at the specified.
 	 * If motorPosition > specifiedPosition, the motor does not move.
 	 * 
 	 * @param speed motor speed (range from 0 to 1000)
 	 * @param pos motor position until the motor stops
 	 */
 	public void moveToPosition(int speed, int pos){
 		LinkJVM.move_to_position(motorPort, speed, pos);
 	}
 	
 	/**
 	 * Adjusts the weights of the PID control. If the motor is jerky, the p and d values should be reduced. 
 	 * If a motor lags far behind they should be increased.
 	 * 
 	 * @param p numerator for p coefficient
 	 * @param i numerator for i coefficient
 	 * @param d numerator for d coefficient
 	 * @param pd p respective denominator
 	 * @param id i respective denominator
 	 * @param dd d respective denominator
 	 */
 	public void setPidGains(short p, short i, short d, short pd, short id, short dd){
 		LinkJVM.set_pid_gains(motorPort, p, i, d, pd, id, dd);
 	}
 	
 	/**
 	 * Runs the motor at duty cycle dutycycle.
 	 * 
 	 * @param dc speed from -100 to 100
 	 * @return
 	 */
 	public int setPwm(int dc){
 		return LinkJVM.setpwm(motorPort, dc);
 	}
 	
 	/**
 	 * Sets the port of the motor.
 	 * 
 	 * @param port port number
 	 */
 	public void setPort(int port){
 		motorPort = port;
 	}
 	
 	/**
 	 * Returns the port of this motor instance.
 	 * 
 	 * @return motor port
 	 */
 	public int getPort(){
 		return motorPort;
 	}
 	
 	/**
 	 * Turns on motor m at scaled PWM duty cycle percentage. 
 	 * This method is the equivalent to the libkovan function "motor".
 	 * 
 	 * @param vel velocity from 100(full forward) to -100(full backward)
 	 */
 	public void drive(int vel){
		LinkJVM.motor(motorPort, vel);
 	}
 	
 	/**
 	 * Turns all motors off.
 	 */
 	public static void allOff(){
 		LinkJVM.ao();
 	}
 }
