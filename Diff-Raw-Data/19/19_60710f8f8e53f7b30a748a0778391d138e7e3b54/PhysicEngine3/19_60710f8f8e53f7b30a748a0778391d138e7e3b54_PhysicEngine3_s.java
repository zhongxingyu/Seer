 package pe_testenvironment;
 
 public class PhysicEngine3 {
 	
 	/*
	 * vars is the presentation of location and speed of every particle in the matter of 
	 * 0	1	2	3		4	...
	 * Ux	Uy	Vx	Vy		U1x	...
	 * 
	 * It is used for computating inside the PhysicEngine Class
	 * The method modifyObjects() applies the computed locations and speeds to the pm
	 */
	protected double[] vars;
	
	/*
 	 * The Particle Mngt to be worked with
 	 */
 	private Particle[] pm;
 	private PMgnt tmp_pmgnt;
 	private Particle[] tmp_pm;
 	
 	
 	/*
 	 * Physical Constants
 	 */
 	
 	private double SPRINGCONSTANT = 6;
 	private double MASS = 0.5;
 	private double LEN = 100;
 	private double GRAVITY = 0;
 	private double DAMPING = 0.1;
 	
 	/*
	 * vars is the presentation of location and speed of every particle in the matter of 
	 * 0	1	2	3		4	...
	 * Ux	Uy	Vx	Vy		U1x	...
	 * 
 	 * It is used for computating inside the PhysicEngine Class
 	 * The method modifyObjects() applies the computed locations and speeds to the pm
 	 */
 	public PhysicEngine3(Particle[] pm) {
 		this.pm = pm;
 	}
 	/*
 	 * Runge Kutta Algorithm, computes the most accurate forecast of position for 4 iterations
 	 */
 	public void step(double stepSize) {
 		tmp_pmgnt = new PMgnt(10,600,600,10);
 		tmp_pmgnt.createParticle();
 		tmp_pm = tmp_pmgnt.particlesystem;
 		int i;
 		int N = pm.length;
 		PMgnt pm0 = new PMgnt(N,600,600,10);
 		PMgnt pm1 = new PMgnt(N,600,600,10);
 		PMgnt pm2 = new PMgnt(N,600,600,10);
 		PMgnt pm3 = new PMgnt(N,600,600,10);
 		PMgnt pm4 = new PMgnt(N,600,600,10);
 		
 		pm0.createParticle();
 		pm1.createParticle();
 		pm2.createParticle();
 		pm3.createParticle();
 		pm4.createParticle();
 		
 		Particle[] inp = pm0.particlesystem;
 		Particle[] k1 = pm1.particlesystem;
 		Particle[] k2 = pm2.particlesystem;
 		Particle[] k3 = pm3.particlesystem;
 		Particle[] k4 = pm4.particlesystem;
 
 		evaluate(pm, k1); // evaluate at time t
 		for (i = 0; i < N; i++){
 			inp[i].setLocation(pm[i].getLocation(0) + k1[i].getLocation(0) * stepSize / 2, pm[i].getLocation(1) + k1[i].getLocation(1) * stepSize / 2);
 			inp[i].setSpeed(pm[i].getSpeed(0) + k1[i].getSpeed(0) * stepSize / 2, pm[i].getSpeed(1) + k1[i].getSpeed(1) * stepSize / 2);
 		}
 		evaluate(inp, k2); // evaluate at time t+stepSize/2
 		for (i = 0; i < N; i++){
 			inp[i].setLocation(pm[i].getLocation(0) + k2[i].getLocation(0) * stepSize / 2, pm[i].getLocation(1) + k2[i].getLocation(1) * stepSize / 2);
 			inp[i].setSpeed(pm[i].getSpeed(0) + k2[i].getSpeed(0) * stepSize / 2, pm[i].getSpeed(1) + k2[i].getSpeed(1) * stepSize / 2);
 		}
 		evaluate(inp, k3); // evaluate at time t+stepSize/2
 		for (i = 0; i < N; i++){
 			inp[i].setLocation(pm[i].getLocation(0) + k3[i].getLocation(0) * stepSize, pm[i].getLocation(1) + k3[i].getLocation(1) * stepSize);
 			inp[i].setSpeed(pm[i].getSpeed(0) + k3[i].getSpeed(0) * stepSize, pm[i].getSpeed(1) + k3[i].getSpeed(1) * stepSize);
 		}
 		evaluate(inp, k4); // evaluate at time t+stepSize
 		// modify the variables
 		for (i = 0; i < N; i++){
 			 if (!pm[i].isLocked()){
 			tmp_pm[i].setLocation(
 					pm[i].getLocation(0) + //(new)x_loc = (old)x_loc + (k1 + 2* k2 + 2*k3 + k4)*stepSize / 6
 					(k1[i].getLocation(0) + 
 							2 * k2[i].getLocation(0) + 
 							2 * k3[i].getLocation(0) + 
 					k4[i].getLocation(0)) 
 					* stepSize / 6,
 					pm[i].getLocation(1) + //(new)y_loc = (old)y_loc + (k1 + 2* k2 + 2*k3 + k4)*stepSize / 6
 					(k1[i].getLocation(1) + 
 							2 * k2[i].getLocation(1) + 
 							2 * k3[i].getLocation(1) + 
 					k4[i].getLocation(1)) 
 					* stepSize / 6);
 			
 			tmp_pm[i].setSpeed(
 					pm[i].getSpeed(0) + //(new)x_speed = (old)x_speed + (k1 + 2* k2 + 2*k3 + k4)*stepSize / 6
 					(k1[i].getSpeed(0) + 
 							2 * k2[i].getSpeed(0) + 
 							2 * k3[i].getSpeed(0) + 
 					k4[i].getSpeed(0)) 
 					* stepSize / 6,
 					pm[i].getSpeed(1) + //(new)y_speed = (old)y_speed + (k1 + 2* k2 + 2*k3 + k4)*stepSize / 6
 					(k1[i].getSpeed(1) + 
 							2 * k2[i].getSpeed(1) + 
 							2 * k3[i].getSpeed(1) + 
 					k4[i].getSpeed(1)) 
 					* stepSize / 6);
 			 }
 		}
 
 	}
 
 	public void evaluate(Particle[] inp, Particle[] change) {
 		for (int i = 0; i < inp.length; i++) {
 				change[i].setLocation(inp[i].getSpeed(0), inp[i].getSpeed(1));// derivative of position U is velocity V
 				double x=0;
 				double y=0;
 					for (int t = 0; t < inp.length; t++) {
 						if ((i != t)) {
 							double xx = inp[t].getLocation(0) - inp[i].getLocation(0);  // x distance between objects
 				            double yy = inp[t].getLocation(1) - inp[i].getLocation(1);  // y distance betw objects
 				            double len = Math.sqrt(xx*xx + yy*yy);  // total distance between objects
 							double f = (SPRINGCONSTANT / MASS) * (len - LEN) / len;// Springforce
 							x += f * xx;
 							y += -GRAVITY + f * yy;
 						}	
 				}
 					if (DAMPING != 0){
 						x -= (DAMPING/MASS)*inp[i].getSpeed(0);
 						y -= (DAMPING/MASS)*inp[i].getSpeed(1);
 					}
 				change[i].setSpeed(x,y);
 			}
 		}
 
 	public void modifyObjects() {
		double w = pm[0].OUTER_RAD / 2;
 		for (int i = 0; i < pm.length; i++) {
 			pm[i].setLocation((tmp_pm[i].getLocation(0)), (tmp_pm[i].getLocation(1) ));
 			pm[i].setSpeed((tmp_pm[i].getSpeed(0)), (tmp_pm[i].getSpeed(1)));
 		}
 	}
 	/*
 	 * Sets Particles in a stable round position
 	 */
 	public void stop(){
 		//TODO Fix central point
 		double r = 1; // radius
 	    for (int i=0; i<pm.length; i++)
 	    {
 	      double rnd = 1+ 0.1*Math.random();
 	      vars[0 + i*4] = r*Math.cos(rnd*i*2*Math.PI/pm.length);
 	      vars[1 + i*4] = r*Math.sin(rnd*i*2*Math.PI/pm.length);
 	    }
 	    
 	}
 	public double getSPRINGCONSTANT() {
 		return SPRINGCONSTANT;
 	}
 	public void setSPRINGCONSTANT(double sPRINGCONSTANT) {
 		SPRINGCONSTANT = sPRINGCONSTANT;
 	}
 	public double getMASS() {
 		return MASS;
 	}
 	public void setMASS(double mASS) {
 		MASS = mASS;
 	}
 	public double getLEN() {
 		return LEN;
 	}
 	public void setLEN(double lEN) {
 		LEN = lEN;
 	}
 	public double getGRAVITY() {
 		return GRAVITY;
 	}
 	public void setGRAVITY(double gRAVITY) {
 		GRAVITY = gRAVITY;
 	}
 	public double getDAMPING() {
 		return DAMPING;
 	}
 	public void setDAMPING(double dAMPING) {
 		DAMPING = dAMPING;
 	}
 
 }
