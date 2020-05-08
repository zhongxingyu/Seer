 /**
  * The class Frisbee contains the method simulate which uses Eulers method to
  * calculate the position and the velocity of a frisbee in two dimensions.
  * 
  * @author Vance Morrison
  * @version March 4, 2005
  */
 
 import java.lang.Math;
 import java.util.ArrayList;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 public class Calcs {
 
 	public ArrayList<Double> X, Y;
 	private int k;
 	private static double[][] points;
 	private static double x;
 	// The x position of the frisbee.
 	private static double y;
 	// The y position of the frisbee.
 	private static double vx;
 	// The x velocity of the frisbee.
 	private static double vy;
 	// The y velocity of the frisbee.
 	private static final double g = -9.81;
 	// The acceleration of gravity (m/s^2).
 	private static final double m = 0.175;
 	// The mass of a standard frisbee in kilograms.
 	private static final double RHO = 1.23;
 	// The density of air in kg/m^3.
	private static final double AREA = 0.061311;
 	// The area of a standard frisbee.
 	private static final double CL0 = 0.1;
 	// The lift coefficient at alpha = 0.
 	private static final double CLA = 1.4;
 	// The lift coefficient dependent on alpha.
 	private static final double CD0 = 0.08;
 	// The drag coefficent at alpha = 0.
 	private static final double CDA = 2.72;
 	// The drag coefficient dependent on alpha.
 	private static final double ALPHA0 = -4;
 
 	public Calcs() {
 		
 		// double (y0, vx0, vy0, angle, deltaT)
 		double velocity = 500;
 		double theta = 30;
 		
 		simulate(velocity, theta);
 		X = new ArrayList<Double>();
 		Y = new ArrayList<Double>();
 
 	}
 
 	public void simulate(double velocity, double theta) {
 		double y0 = .5;
 		double vx0 = velocity * Math.cos(theta*Math.PI/180);
 		double vy0 = velocity * Math.sin(theta*(Math.PI)/180);
 		double alpha = theta;
 		double deltaT = .01;
 		// Calculation of the lift coefficient using the relationship given
 		// by S. A. Hummel.
 		double cl = CL0 + CLA * alpha * Math.PI / 180;
 		// Calculation of the drag coefficient (for Prantls relationship)
 		// using the relationship given by S. A. Hummel.
 		double cd = CD0 + CDA * Math.pow((alpha - ALPHA0) * Math.PI / 180, 2);
 		// Initial position x = 0.
 		x = 0;
 		// Initial position y = y0.
 		y = y0;
 		// Initial x velocity vx = vx0.
 		vx = vx0;
 		// Initial y velocity vy = vy0.
 		vy = vy0;
 		try {
 			// A PrintWriter object to write the output to a spreadsheet.
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
 					"frisbee.csv")));
 
 			// A loop index to monitor the simulation steps.
 			k = 0;
 			points = new double[1000][2];
 
 			// A while loop that performs iterations until the y position
 			// reaches zero (i.e. the frisbee hits the ground).
 			while (y > 0) {
 				// The change in velocity in the y direction obtained setting
 				// the
 				// net force equal to the sum of the gravitational force and the
 				// lift force and solving for delta v.
 				double deltavy = (RHO * Math.pow(vx, 2) * AREA * cl / 2 / m + g)
 						* deltaT;
 				// The change in velocity in the x direction, obtained by
 				// solving the force equation for delta v. (The only force
 				// present is the drag force).
 				double deltavx = -RHO * Math.pow(vx, 2) * AREA * cd * deltaT;
 				// The new positions and velocities are calculated using
 				// simple introductory mechanics.
 				vx = vx + deltavx;
 				vy = vy + deltavy;
 				x = x + vx * deltaT;
 				y = y + vy * deltaT;
 				// Only the output from every tenth iteration will be sent
 				// to the spreadsheet so as to decrease the number of data
 				// points.
 
 				pw.print(x + "," + y + "," + vx + "," + vy + ","
 						+ Math.pow(Math.pow(vx, 2) + Math.pow(vy, 2), .5));
 				pw.println();
 				pw.flush();
 				X.add(x);
 				Y.add(y);
 
 				k++;
 			}
 			pw.close();
 		} catch (Exception e) {
 			System.out.println("Error, file frisbee.csv is in use.");
 		}
 	}
 }
