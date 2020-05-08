 package Blatt3;
 
public class Parallelogram extends Shape{
 
 protected Point p1;
 protected double winkel;
 protected double laenge1;
 private double laenge2;
 
 public Parallelogram(){
 	this.p1 = new Point(0,0);
 	this.winkel = 90;
 	this.laenge1 = 1;
 	this.laenge2 = 1;
 }
 
 public Parallelogram(Point p1,double laenge1,double laenge2,double winkel){
 	this.p1 = p1;
 	this.winkel = winkel;
 	this.laenge1 = laenge1;
 	this.laenge2 = laenge2;
 }
 
 @Override
 public double calculateArea() {
 	return (laenge1*laenge2*Math.sin(Math.PI*winkel/180));
 }
 @Override
 public double calculatePerimeter() {
 	return (2*laenge1+2*laenge2);
 }
 @Override
 public void shift(double x, double y) {
   p1.shift(x,y);
 	}
 @Override
 public boolean equals(Object o){
 	if(o instanceof Parallelogram){
 		Parallelogram t2 = (Parallelogram) o;
 		return p1.equals(t2.p1);
 	}
 	return false;
 	
 }
 
 }
