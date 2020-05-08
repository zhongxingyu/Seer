 package org.team4159.frc2013.controllers;
 
 import org.team4159.frc2013.IO;
 import org.team4159.frc2013.subsystems.Drive;
 import org.team4159.frc2013.subsystems.Elevator;
 import org.team4159.frc2013.subsystems.Shooter;
 import org.team4159.support.Controller;
 import org.team4159.support.DriverStationLCD;
 import org.team4159.support.ModeEnumerator;
 import com.sun.squawk.util.Arrays;
 import edu.wpi.first.wpilibj.DriverStation;
 
 class ElevatorTest
 {
 	private double[] samples = new double[8];
 	private double[] samplessorted = new double[samples.length];
 	private int samplei = 0;
 	
 	private double maxRate = 0;
 	
 	public void tick ()
 	{
 		samples[samplei] = IO.elevatorEncoder.getRate ();
 		samplei = (samplei + 1) % samples.length;
 		
 		System.arraycopy (samples, 0, samplessorted, 0, samples.length);
 		Arrays.sort (samplessorted);
 		
 		double medianRate = samplessorted[samples.length / 2];
 		maxRate = IO.joystick1.getRawButton (2) ?
 			0 : Math.max (maxRate, Math.abs (medianRate));
 		
 		double elevatorOutput = IO.joystick1.getZ ();
 		IO.elevatorMotor.set (IO.joystick1.getTrigger () ? elevatorOutput : 0);
 		
 		DriverStationLCD.setLine (0, "Motor: " + elevatorOutput);
 		DriverStationLCD.setLine (1, "Encoder: " + maxRate);
 	}
 }
 
 public class OperatorController extends Controller 
 {
 	public OperatorController ()
 	{
 		super (ModeEnumerator.OPERATOR);
 	}
 	
 	public void tick ()
 	{
 		Drive.instance.arcadeDrive (IO.joystick1);
 		
 		boolean shiftDown = IO.joystick1.getRawButton (2);
 		boolean shiftUp = IO.joystick1.getRawButton (3);
 		if (shiftUp ^ shiftDown)
 			Drive.instance.setGearboxPosition (shiftUp);
 		
 		/*
 		if (IO.joystick1.getRawButton (6))
 			Elevator.instance.moveTrayToInput (0);
 		if (IO.joystick1.getRawButton (7))
 			Elevator.instance.moveTrayToInput (1);
 		if (IO.joystick1.getRawButton (8))
 			Elevator.instance.moveTrayToInput (2);
 		*/
 		
 		boolean elevatorUp = IO.joystick1.getRawButton (6);
 		boolean elevatorDown = IO.joystick1.getRawButton (7);
 		if (elevatorUp ^ elevatorDown)
 			Elevator.instance.setMotorOutput (elevatorDown ? 0.5 : -0.5);
 		else
 			Elevator.instance.setMotorOutput (0);
 		
 		if (IO.joystick1.getRawButton (9))
 			Elevator.instance.moveTrayToOutput (0);
 		if (IO.joystick1.getRawButton (10))
 			Elevator.instance.moveTrayToOutput (1);
 		if (IO.joystick1.getRawButton (11))
 			Elevator.instance.moveTrayToOutput (2);
 		
 		if (IO.joystick1.getTrigger ())
 			Shooter.instance.extend ();
 		else
 			Shooter.instance.retract ();
 		
 		Shooter.instance.setMotorOutput ((IO.joystick1.getZ () + 1) / 2);
 		
 		boolean anglerDown = IO.joystick1.getRawButton (4);
 		boolean anglerUp = IO.joystick1.getRawButton (5);
 		if (anglerDown != anglerUp)
 		{
 			if (anglerUp)
 				Shooter.instance.raiseAngler ();
 			if (anglerDown)
 				Shooter.instance.lowerAngler ();
 		}
 		
 		DriverStationLCD.setLine (0, "ShtOut: " + IO.shooterMotor.get ());
 		DriverStationLCD.setLine (1, "ShtEncX: " + IO.shooterEncoder.getDistance ());
 		DriverStationLCD.setLine (2, "ShtEncV: " + IO.shooterEncoder.getRate ());
		DriverStationLCD.setLine (3, "ElevEnc: " + IO.elevatorEncoder.getDistance ());
 		
 		/*
 		if (ticks++ % 10 == 0)
 		{
 			System.out.println (
 				"raw: " + IO.elevatorEncoder.get () +
 				"\t" + 
 				"elev: " + IO.elevatorEncoder.getDistance ()
 			);
 		}
 		*/
 	}
 }
