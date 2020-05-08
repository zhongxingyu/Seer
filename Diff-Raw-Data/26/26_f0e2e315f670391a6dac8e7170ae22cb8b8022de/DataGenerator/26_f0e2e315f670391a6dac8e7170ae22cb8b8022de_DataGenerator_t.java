 package com.totoro.incardisplay.simulations;
 
 import java.util.*;

/**
 * Random data generator.
 */

 public class DataGenerator {
 
 	private Random rng;
 
 	private TimeSlice[] data;
 
 	public DataGenerator()	{
 		rng = new Random();
 	}
 
 	public TimeSlice getTimeSlice(int time)	{
 		if(time < 0)	{
 			throw new IllegalArgumentException("Time cannot be negative.");
 		}
 		else if(time >= data.length)	{
 			throw new IllegalArgumentException("Time is out-of-bounds.");
 		}
 		return data[time];
 	}
 	
	public TimeSlice[] getAllData()	{
		return data;
	}
	
	// TODO(Nick): Add stopping feature.
	// TODO(Nick): Latitude/longitude
	// TODO(Nick): Fuel usage
	// TODO(Nick): MPG
	// TODO(Nick): Eco-score
	// TODO(Nick): Trip-length
	// TODO(Nick): Tell KX what units are
	// TODO(Nick): Make output JSON
 	public void generateData(final int NUM_LINES) {
 		currentSpeed = 0;
 		acceleration = 0;
 		airConditioning = 0;
 		steering = 0;
 		odometer = 0;
 		brakingPressure = 0;
 		pedalForce = 0;
 		altitude = 0;
 		double goalSpeed = 0;
 		int numSpeedTurnsLeft = 0;
		data = new TimeSlice[NUM_LINES];
 		for(int line = 0; line < NUM_LINES; line++, numSpeedTurnsLeft--)	{
 			if(numSpeedTurnsLeft == 0)	{
				goalSpeed = 80 * rng.nextDouble();
 				numSpeedTurnsLeft = 50+rng.nextInt(100);
 			}
 			double accelInc = getNextAccel(goalSpeed, acceleration, currentSpeed);
 			acceleration += accelInc;
 			if(currentSpeed + acceleration < 0)	{
 				currentSpeed = 0;
 				acceleration = 0;
 				brakingPressure = 0;
 				pedalForce = 0;
 			}
 			else	{
 				currentSpeed += acceleration;
 				currentSpeed = Math.max(0, currentSpeed);
 				brakingPressure = Math.max(0, -accelInc);
 				pedalForce = Math.max(0, accelInc);
 			}
 			airConditioning += getNextAC(airConditioning);
 			steering += getNextSteering(steering);
 			odometer += currentSpeed / 3600;
			altitude = 20 * rng.nextGaussian();
 			data[line] = generateDatum();
 		}
 	}
 
 	private double currentSpeed;
 	private double acceleration;
 	private double airConditioning;
 	private double steering;
 	private double odometer;
 	private double brakingPressure;
 	private double pedalForce;
 	private double altitude;
 	
 	private TimeSlice generateDatum()	{
 		List<Datum> list = new ArrayList<Datum>();
 		list.add(new Datum("currentSpeed", currentSpeed));
 		list.add(new Datum("acceleration", acceleration));
 		list.add(new Datum("airConditioning", airConditioning));
 		list.add(new Datum("steering", steering));
 		list.add(new Datum("odometer", odometer));
 		list.add(new Datum("brakingPressure", brakingPressure));
 		list.add(new Datum("pedalForce", pedalForce));
 		list.add(new Datum("altitude", altitude));
 		return new TimeSlice(list);
 	}
 	
 	private static final double STEERING_SCALE = 0.01;
 
 	private double getNextSteering(double steering) {
 		return 10 * rng.nextGaussian() - STEERING_SCALE * steering;
 	}
 
 	private static final double AC_SCALING = 0.01;
 
 	private double getNextAC(double airConditioning)	{
 		return Math.max(-airConditioning, rng.nextGaussian() - AC_SCALING * airConditioning);
 	}
 
 	private static final double ACCEL_SCALING_FACTOR = 0.005;
 
 	private static final double ACCEL_CORRECTION = 0.1;
 
 	private double getNextAccel(double goalSpeed, double acceleration, double currentSpeed) {
 		double randomChange = ACCEL_SCALING_FACTOR * Math.abs(rng.nextGaussian()) * (goalSpeed - currentSpeed);
 		acceleration -= ACCEL_CORRECTION * Math.abs(rng.nextGaussian()) * acceleration;
 		return randomChange;
 	}
 
 }
