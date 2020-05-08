 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author maslab-9
  */
 
 import orc.*;
 
 public class Gyroscope extends java.lang.Thread {
 
	public static final int gyrot = 6913242;
 	public int anglei = 0;
 	public double angle = 0;
 	public Orc o = null;
 	public long[] angles = null;
 	public int[] baseang = null;
 	long prevtime = 0;
 	boolean running = true;
 
 	public static void printList(int[] c) {
 		if (c.length == 0) return;
 		System.out.print("[ ");
 		for (int x = 0; x < c.length-1; ++x) {
 			System.out.print(c[x]+", ");
 		}
 		System.out.println(c[c.length-1]+" ]");
 	}
 
 	public static void printList(long[] c) {
 		if (c.length == 0) return;
 		System.out.print("[ ");
 		for (int x = 0; x < c.length-1; ++x) {
 			System.out.print(c[x]+", ");
 		}
 		System.out.println(c[c.length-1]+" ]");
 	}
 
 	public static int median3(int a, int b, int c) {
 		if (a < b) {
 			if (c < b) { // a,c < b
 				if (a < c) { // a < c < b
 					return c;
 				} else { // c < a < b
 					return a;
 				}
 			} else { // a < b < c
 				return b;
 			}
 		} else { // b < a
 			if ( c < a) { // b,c < a
 				if (b < c) { // b < c < a
 					return c;
 				} else { // c < b < a
 					return b;
 				}
 			} else { // b < a < c
 				return a;
 			}
 		}
 	}
 
 	public static void shiftleft3(int[] a, int v) {
 		a[0] = a[1];
 		a[1] = a[2];
 		a[2] = v;
 	}
 
 	public void run() {
 		try {
 		WiiMotionPlus g = new WiiMotionPlus(o);
 		int numsamples = 300;
 		angles = new long[3];
 		baseang = new int[3];
 		int[] mdf0 = new int[3];
 		int[] mdf1 = new int[3];
 		int[] mdf2 = new int[3];
 		{
 			int[] angvel = g.readAxes();
 			mdf0[2] = angvel[0];
 			mdf1[2] = angvel[1];
 			mdf2[2] = angvel[2];
 			angvel = g.readAxes();
 			mdf0[1] = angvel[0];
 			mdf1[1] = angvel[1];
 			mdf2[1] = angvel[2];
 		}
 		{
 		long ba0 = 0;
 		long ba1 = 0;
 		long ba2 = 0;
 		while (numsamples > 0) {
 			int[] angvel = g.readAxes();
 			shiftleft3(mdf0, angvel[0]);
 			shiftleft3(mdf1, angvel[1]);
 			shiftleft3(mdf2, angvel[2]);
 			ba0 += median3(mdf0[0], mdf0[1], mdf0[2]);
 			ba1 += median3(mdf1[0], mdf1[1], mdf1[2]);
 			ba2 += median3(mdf2[0], mdf2[1], mdf2[2]);
 			--numsamples;
 		}
 		baseang[0] = (int)(ba0/300);
 		baseang[1] = (int)(ba1/300);
 		baseang[2] = (int)(ba2/300);
 		}
 		prevtime = System.currentTimeMillis();
 		while (running) {
 			//double degrees = g.getTheta()/0.005;
 			//System.out.println(degrees);
 			//System.out.println(g.getTheta());
 			//printList(g.readAxes());
 			int[] angvel = g.readAxes();
 			shiftleft3(mdf0, angvel[0]);
 			shiftleft3(mdf1, angvel[1]);
 			shiftleft3(mdf2, angvel[2]);
 			int v0 = median3(mdf0[0], mdf0[1], mdf0[2])-baseang[0];
 			int v1 = median3(mdf1[0], mdf1[1], mdf1[2])-baseang[1];
 			int v2 = median3(mdf2[0], mdf2[1], mdf2[2])-baseang[2];
 			if (v0 < 100 && v0 > -100) v0 = 0;
 			if (v1 < 100 && v1 > -100) v1 = 0;
 			if (v2 < 100 && v2 > -100) v2 = 0;
 			//printList(angvel);
 			long deltatime = System.currentTimeMillis()-prevtime;
 			//System.out.println(deltatime);
 			angles[0] += (v0*deltatime);
 			if ((angles[0]) < 0) angles[0] += gyrot;
 			if ((angles[0]) > gyrot) angles[0] -= gyrot;
 			angles[1] += (v1*deltatime);
 			if ((angles[1]) < 0) angles[1] += gyrot;
 			if ((angles[1]) > gyrot) angles[1] -= gyrot;
 			angles[2] += (v2*deltatime);
 			if ((angles[2]) < 0) angles[2] += gyrot;
 			if ((angles[2]) > gyrot) angles[2] -= gyrot;
 			//printList(angles);
 			//System.out.println(angles[1]);
 			anglei = (int)((angles[0]*360/gyrot));
 			System.out.println(anglei);
 			angle = ((angles[0]*2.0*Math.PI/gyrot));
 			//System.out.println(angle);
 			//System.out.println(angles[1]*360/80000000);
 			prevtime += deltatime;
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setup(Arbiter a, int ActionWeightIndex) {
 		o = a.o;
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
