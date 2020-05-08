 package balle.strategy.curve;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import balle.world.Coord;
 import balle.world.Scaler;
 
 public class Spline implements Curve {
 
 	private final Curve[] splines;
 	
 	public Spline(Curve[] splines) {
 		this.splines = splines;
 	}
 
 	protected double div() {
 		return 1.0 / ((double) splines.length);
 	}
 
 	@Override
 	public Coord pos(double t) {
 		if (t >= 1)
 			return splines[splines.length - 1].pos(t);
 		else
 			return splines[(int) Math.floor(t / div())]
 					.pos((t % div()) / div());
 	}
 
 	@Override
 	public Coord vel(double t) {
 		return splines[(int) Math.floor(t / div())].vel((t % div()) / div());
 	}
 
 	@Override
 	public Coord acc(double t) {
 		return splines[(int) Math.floor(t / div())].acc((t % div()) / div());
 	}
 
 	@Override
 	public Coord cor(double t) {
 		double f, f1, f11, g, g1, g11;
 		f = pos(t).getX();
 		f1 = vel(t).getX();
 		f11 = acc(t).getX();
 		g = pos(t).getY();
 		g1 = vel(t).getY();
 		g11 = acc(t).getY();
 		return new Coord(
 				f
 						- ((((f1 * f1) + (g1 * g1)) * g1) / ((f1 * g11) - (f11 * g1))),
 				g
 						+ ((((f1 * f1) + (g1 * g1)) * f1) / ((f1 * g11) - (f11 * g1))));
 	}
 
 	@Override
 	public double rad(double t) {
 		return cor(t).dist(pos(t));
 	}
 
 	@Override
 	public void draw(Graphics g, Scaler s) {
 		for (Curve spline : splines)
 			spline.draw(g, s);
 	}
 
 	@Override
 	public Coord closestPoint(Coord c) {
 		Coord closest = null;
 		for (Curve each : splines) {
 			Coord testing = each.closestPoint(c);
 			if (closest == null || c.dist(closest) > c.dist(testing))
 				closest = testing;
 		}
 		return closest;
 	}
 
 	@Override
 	public double length() {
 		double sum = 0;
 		for (Curve each : splines) {
 			sum += each.length();
 		}
 		return sum;
 	}
 
 	public Spline getSubSpline(int start, int finish) {
 		if (finish < 0)
 			finish += splines.length;
 
 		ArrayList<Curve> curves = new ArrayList<Curve>();
 		for (int i = start; i < finish; i++)
 			curves.add(splines[i]);
 
		Curve[] out = new Curve[curves.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = curves.get(i);

		return new Spline(out);
 
 	}
 
     @Override
     public void reduceVisibility() {
         // TODO: implement!!!
     }
 
 }
