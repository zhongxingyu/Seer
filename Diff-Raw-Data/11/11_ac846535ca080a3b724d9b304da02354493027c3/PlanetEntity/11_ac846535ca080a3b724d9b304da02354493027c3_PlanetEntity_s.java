 package org.phonybone.sprites;
 import java.lang.Math.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.util.ArrayList;
 
 /*
   Todo:
   fix up keyboard UI
   - allow start/stop
   - allow keyboard quit
   add more planets
   add options to show traces
   fix ArrayList warnings?
   
  */
 
 public class PlanetEntity extends Entity {
     private Game game;
     private String name;
     private double mass;
     private final double G=6.67428e-2;	// m^3 * kg^-1 * sec^-2
     private double fx,fy;		// force acting on planet
     private Color color;
 
     public PlanetEntity(Game game,String name, int x,int y, double dx, double dy, double mass, Color color) {
 	super(x,y);
 		
 	this.game = game;
 	this.name=name;
 	this.dx=dx;
 	this.dy=dy;
 	this.mass=mass;
 	this.color=color;
     }
 
     public void draw(Graphics g) {
 	int radius=(int) Math.min(Math.max(Math.pow(this.mass,1.0/3.0),4.0),20.0);
 	g.setColor(this.color);
 	g.fillArc((int)this.x, (int)this.y, radius, radius, 0, 360);
 
 	// draw the force vector:
 	if (this.name=="Earth") {
 	    //	    g.setColor(Color.green);
 	    //	    g.drawLine((int)this.x,(int)this.y, (int)(this.x+this.fx*50), (int)(this.y+this.fy*50));
 	}
     }
 	
     // return the gravitation force vector between two planets (as a double[2])
     public double[] gForce2d(PlanetEntity p2) {
 	double f=-G*this.mass*p2.mass/distance2d(p2);
 	double[] f2d= new double[2];
 	double theta=this.angle2d(p2);
 	f2d[0]=f*java.lang.Math.cos(theta);
 	f2d[1]=f*java.lang.Math.sin(theta);        
         return f2d;
     }
 	
     // return the square of the distance between two planets
     public double distance2d(PlanetEntity p2) {
 	double ddx=this.x-p2.x;
 	double ddy=this.y-p2.y;
 	return ((ddx*ddx)+(ddy*ddy));
     }
 
     // return the angle (in radians) between the two planet's location:
     public double angle2d(PlanetEntity p2){
 	double x=(this.x-p2.x);
 	double y=(this.y-p2.y);
 	if (x==0) return y>0? Math.PI/2 : Math.PI*3/2;
 
 	double theta=Math.atan(y/x);
 
 	if (x>0) {
 	    if (y>0) return theta;	 // quad 1
 	    else return Math.PI*2+theta; // quad 4
 	} else {	       
 	    return  Math.PI+theta; // quads 2&3
 	}
     }
 	
     public String toString() {
 	return String.format("%s: mass=%g x=%.0f y=%.0f dx=%.2f dy=%.2f fx=%.2g fy=%.2g",
 			     this.name, this.mass, this.x, this.y, this.dx, this.dy, this.fx, this.fy);
     }
 
     public static void sumForces2d(ArrayList planets) {
 	int n_planets=planets.size();
 	int i,j;
 	// Clear all forces
 	for (i=0; i<n_planets; i++){
 	    PlanetEntity p=(PlanetEntity)planets.get(i);
 	    p.fx=0; p.fy=0;
 	}
 	for (i=0; i<n_planets; i++){
 	    for (j=0; j<i; j++){
 		PlanetEntity pi=(PlanetEntity)planets.get(i);
 		PlanetEntity pj=(PlanetEntity)planets.get(j);
 		double[] f2d=pi.gForce2d(pj);
 		pi.fx+=f2d[0];
 		pi.fy+=f2d[1];
 		pj.fx-=f2d[0];
 		pj.fy-=f2d[1];
 	    }
 	}
     }
 
     public void move(long delta) {
 	double ax=this.fx/this.mass;
 	double ay=this.fy/this.mass;
 	this.dx+=ax;
 	this.dy+=ay;
 	this.x+=this.dx*delta/1000;
 	this.y+=this.dy*delta/1000;
     }
 
     public boolean collidesWith(Entity other) {
 	return false;
     }
 
     public void collidedWith(Entity other) {
 	// Yeah yeah whatever
     }
 
     public static ArrayList init_planets(Game game) {
 	ArrayList planets=new ArrayList();
 	planets.add(new PlanetEntity(game,"Sun",  400,300, 0, 0, 10000, Color.yellow));
 	planets.add(new PlanetEntity(game,"Earth",500,300 ,0,15,    10, Color.blue));
 	planets.add(new PlanetEntity(game,"Moon" ,500,304 ,0,15,     2, Color.gray));
 	return planets;
     }
 
     // Return the velocity vector that corresponds to a stable orbit (gravity cancels centripital force)
    public double[] stable_orbit(PlantetEntity p2) {
	double r=sqrt(this.distance2d(p2));
	double vm=sqrt(G*p2.mass/r);
 	double theta=this.angle2d(p2);
	double[2] v=[vm*cos(theta),vm*sin(theta)];
 	return v;
     }
 
     public int getWidth() {
 	return 1;
     }
     public int getHeight() {
 	return 1;
     }
 }
 
