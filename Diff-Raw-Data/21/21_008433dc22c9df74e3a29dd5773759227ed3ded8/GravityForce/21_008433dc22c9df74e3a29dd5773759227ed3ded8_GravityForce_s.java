 package feynstein.forces;
 
 import feynstein.geometry.*;
 import feynstein.shapes.*;
 
 import java.util.ArrayList;
 
 public class GravityForce extends Force<GravityForce> {
 	private double gx;
 	private double gy;
 	private double gz;
 
     public GravityForce() {
 		super(0);
 		objectType = "Gravity";
 		gx = 0;
 		gy = 0;
 		gz = 0;
     }
  
     public GravityForce set_gx(double gx) {
 		this.gx = gx;
 		return this;
     }
 	
 	public GravityForce set_gy(double gy) {
 		this.gy = gy;
 		return this;
     }
 	
 	public GravityForce set_gz(double gz) {
 		this.gz = gz;
 		return this;
     }
 
 	public double[] getLocalForce(double [] globalPositions,
 										   double [] globalVelocities,
 										   double [] globalMasses) {
 		int n = globalPositions.length;
 		if(localForce == null)
 			localForce = new double[n];
 		
		for(int i = 0; i < n; i++){
 			localForce[3*i] = globalMasses[3*i]*gx;
 			localForce[3*i+1] = globalMasses[3*i]*gy;
 			localForce[3*i+2] = globalMasses[3*i]*gz;
 		}
 		
 		return localForce;
 	}
 }
