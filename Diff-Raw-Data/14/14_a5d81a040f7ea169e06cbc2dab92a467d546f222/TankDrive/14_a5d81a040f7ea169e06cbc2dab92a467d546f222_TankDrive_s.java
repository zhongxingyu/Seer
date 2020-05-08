 package org.badgerbots.lib;
 
 import edu.wpi.first.wpilibj.*;
 
 public class TankDrive
 {
 
 	boolean _square;
 	Jaguar _leftJag;
 	Jaguar _rightJag;
 	Joystick _leftJoy;
 	Joystick _rightJoy;
 
 	public TankDrive(Jaguar leftJag, Jaguar rightJag, Joystick leftJoy, Joystick rightJoy)
 	{
 		_square = false;
 		_leftJag = leftJag;
 		_rightJag = rightJag;
 		_leftJoy = leftJoy;
 		_rightJoy = rightJoy;
 	}
 
 	public TankDrive(int leftChan, int rightChan, Joystick leftJoy, Joystick rightJoy)
 	{
 		_leftJag = new Jaguar(leftChan);
 		_rightJag = new Jaguar(rightChan);
 		_leftJoy = leftJoy;
 		_rightJoy = rightJoy;
 	}
 
 	public TankDrive(Jaguar l, Jaguar r)
 	{
 		_leftJag = l;
 		_rightJag = r;
 	}
 
 	public TankDrive(int leftChan, int rightChan, int leftPort, int rightPort)
 	{
 		_leftJag = new Jaguar(leftChan);
 		_rightJag = new Jaguar(rightChan);
 		_leftJoy = new Joystick(leftPort);
 		_rightJoy = new Joystick(rightPort);
 	}
 	
 //Why are there many separate TankDrives instead of just one with smart inputs?
 	
 	public void singleDrive()
 	{
 		if (_leftJoy.getRawButton(3))  //Trigger button, should probably create boolean _triggerButton
 			setLeft(_leftJoy.getY()/5);
 		else
 			setLeft(_leftJoy.getY());
 		
 		if (_leftJoy.getRawButton(3))
 			setRight(_leftJoy.getY()/-5);
 		else
 			setRight(-(_leftJoy.getY()));
 	}
 
 	public void drive()
 	{
		if (_leftJoy.getRawButton(3))
			setLeft(_leftJoy.getY() / 5);
 		else
			setLeft(_leftJoy.getY());
 		
		if (_rightJoy.getRawButton(3))
 			setRight(-_rightJoy.getY() / 5);
 		else
 			setRight(-_rightJoy.getY());
 	}
 
 	public void arcadeDrive()
 	{
 		double move = _rightJoy.getX();
 		double rotate = _rightJoy.getY();
 		double rightMotorSpeed, leftMotorSpeed;
 
 		if (move > 0.0)
 		{
 			if (rotate > 0.0)
 			{
 				leftMotorSpeed = move - rotate;
 				rightMotorSpeed = Math.max(move, rotate);
 			}
 			else
 			{
 				leftMotorSpeed = Math.max(move, -rotate);
 				rightMotorSpeed = move + rotate;
 			}
 		}
 		else
 		{
 			if (rotate > 0.0)
 			{
 				leftMotorSpeed = -Math.max(-move, rotate);
 				rightMotorSpeed = move + rotate;
 			}
 			else
 			{
 				leftMotorSpeed = move - rotate;
 				rightMotorSpeed = -Math.max(-move, -rotate);
 			}
 
 		}
 		if (_rightJoy.getRawButton(3))
 		{
 			leftMotorSpeed /= 3;
 			rightMotorSpeed /= 3;
 		}
 		set(-leftMotorSpeed, -rightMotorSpeed);
 	}
 
 	public void set(double left, double right)
 	{
 		setLeft(left);
 		setRight(right);
 	}
 
 	public void set(double val)
 	{
 		set(val, val);
 	}
 
 	public double getSpeed()
 	{
 		return (_leftJag.get()+_rightJag.get())/2;
 	}
 	
 	public double getLeft()
 	{
 		return _leftJag.get();
 	}
 	
 	public double getRight()
 	{
 		return _rightJag.get();
 	}
 
 	public boolean moving()
 	{
 		return !(_leftJag.get() == 0 && _rightJag.get() == 0);
 	}
 
 	public void setLeft(double speed)
 	{
 		if (_square)
 		{
 			if (speed >= 0)  speed =  (speed * speed);
 			else            speed = -(speed * speed);
 		}
 		_leftJag.set(-speed);
 	}
 
 	public void setRight(double speed)
 	{
 		if (_square)
 		{
 			if (speed >= 0) speed =  (speed * speed);
 			else            speed = -(speed * speed);
 		}
 		_rightJag.set(speed);
 	}
 
 	public void setSquare(boolean square)
 	{
 		_square = square;
 	}
 
 }
