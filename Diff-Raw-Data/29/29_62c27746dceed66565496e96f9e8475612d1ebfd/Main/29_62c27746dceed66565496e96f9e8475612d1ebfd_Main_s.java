 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.*;
 import javax.imageio.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.util.*;
 import maslab.telemetry.channel.*;
 import orc.*;
 
 /**
  *
  * @author geza
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
 
 	public int ncd = 0;
 	public double nca = 0.0;
 
 	public static void convolve(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
 		int[] rgbf = new int[r1.getNumBands()];
 		int[] rgbft = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				for (int i = 0; i < rgbf.length; ++i)
 					rgbf[i] = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						for (int i = 0; i < rgbf.length; ++i)
 							rgbf[i] += rgbft[i]*m[my][mx];
 					}
 				}
 				for (int i = 0; i < rgbf.length; ++i)
 					rgbf[i] /= w;
 				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
 				r2.setPixel(x, y, rgbf);
 			}
 		}
 	}
 
 	public static void convolveBW(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
 		int[] rgbft = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int tot = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						for (int i = 0; i < rgbft.length; ++i)
 							tot += rgbft[i]*m[my][mx];
 					}
 				}
 				//for (int i = 0; i < rgbf.length; ++i)
 				//	rgbf[i] /= w;
 				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
 				//r2.setSample(x, y, 0, bound(tot/(w*3), 255, 0));
 				r2.setSample(x, y, 0, tot/(w*3));
 			}
 		}
 	}
 
 
 	public static void copyChannels(WritableRaster r1, WritableRaster r2, int ch, int nch) {
 		int[] buf1 = new int[r1.getNumBands()];
 		int[] buf2 = new int[nch];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				for (int i = 0; i < buf2.length; ++i) {
 					buf2[i] = buf1[ch+i];
 				}
 				for (int i = 0; i < buf2.length; ++i) {
 					r2.setSample(x, y, i, buf2[i]);
 				}
 				//r2.setPixel(x, y, buf2);
 			}
 		}
 	}
 
 	public static void mergeChannels(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		int[] buf1 = new int[r1.getNumBands()];
 		int[] buf2 = new int[r2.getNumBands()];
 		int[] buf3 = new int[r1.getNumBands()+r2.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r2.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				r2.getPixel(x, y, buf2);
 				for (int i = 0; i < buf1.length; ++i) {
 					buf3[i] = buf1[i];
 				} for (int i = 0; i < buf2.length; ++i) {
 					buf3[i+buf1.length] = buf2[i];
 				}
 				r3.setPixel(x, y, buf3);
 			}
 		}
 	}
 
 	public static void sqrtImage(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		int[] buf1 = new int[3];
 		int[] buf2 = new int[3];
 		int[] buf3 = new int[3];
 		for (int x = 0; x < r3.getWidth(); ++x) {
 			for (int y = 0; y < r3.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				r2.getPixel(x, y, buf2);
 				for (int i = 0; i < buf3.length; ++i)
 					buf3[i] = buf1[i]+buf2[i];
 				r3.setPixel(x, y, buf3);
 			}
 		}
 	}
 
 	public static int square(int x) {
 		return x*x;
 	}
 
 	public static void sqrtImageBW(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		for (int x = 0; x < r3.getWidth(); ++x) {
 			for (int y = 0; y < r3.getHeight(); ++y) {
 				r3.setSample(x, y, 0, (int)Math.sqrt(square(r1.getSample(x, y, 0))+square(r2.getSample(x, y, 0))));
 			}
 		}
 	}
 
 	public static int bound(int v, int max, int min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static double bound(double v, double max, double min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static void normImage(WritableRaster r1, WritableRaster r2) {
 		//int[] buf = new int[3];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				//r1.getPixel(x, y, buf);
 				//int tot = buf[0]+buf[1]+buf[2];
 				//buf[0] *= (400.0f/tot);
 				//buf[1] *= (400.0f/tot);
 				//buf[2] *= (400.0f/tot);
 				//r2.setPixel(x, y, buf);
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				//int tot = bound((int)Math.sqrt(r*r+g*g+b*b), 765, -765);
 				int tot = r+g+b;
 				if (tot == 0) tot = 1;
 				//int tot = r+g+b;
 				r2.setSample(x, y, 0, bound(r*255/tot, 255, -255));
 				r2.setSample(x, y, 1, bound(g*255/tot, 255, -255));
 				r2.setSample(x, y, 2, bound(b*255/tot, 255, -255));
 				r2.setSample(x, y, 3, bound(255-tot/3, 255, -255));
 			}
 		}
 	}
 
 	public static void mergeAlpha(WritableRaster r1, WritableRaster r2) {
 		//int[] buf = new int[3];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				//r1.getPixel(x, y, buf);
 				//int tot = buf[0]+buf[1]+buf[2];
 				//buf[0] *= (400.0f/tot);
 				//buf[1] *= (400.0f/tot);
 				//buf[2] *= (400.0f/tot);
 				//r2.setPixel(x, y, buf);
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				int tot = 3*(255-r1.getSample(x, y, 3));
 				r2.setSample(x, y, 0, r*tot/255);
 				r2.setSample(x, y, 1, g*tot/255);
 				r2.setSample(x, y, 2, b*tot/255);
 			}
 		}
 	}
 
 	public static void rmlastchannel(WritableRaster r1, WritableRaster r2) {
 		int[] buf = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				r1.getPixel(x, y, buf);
 				buf[buf.length-1] = 255;
 				r2.setPixel(x, y, buf);
 			}
 		}
 	}
 
 	public static void rgb2hsv(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				int max = Math.max(Math.max(r, g), b);
 				int min = Math.min(Math.min(r, g), b);
 				int h = 0;
 				int s = 0;
 				int delta = max-min;
 				if (delta == 0) delta = 1;
 				if (max != 0) {
 					s = 255*delta/max;
 					if (max == r) {
 						h = (g-b)*85/(2*delta);
 						if (h < 0)
 							h += 255;
 					} else if (max == g) {
 						h = 85 + (b-r)*85/(2*delta);
 					} else { // max == b
 						h = 170 + (r-g)*85/(2*delta);
 					}
 				}
 				r2.setSample(x, y, 0, h); // h
 				//r2.setSample(x, y, 1, h);
 				//r2.setSample(x, y, 2, h);
 				r2.setSample(x, y, 1, s); // s
 				r2.setSample(x, y, 2, max); // v
 			}
 		}
 	}
 
 	public static void colorProp(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				
 			}
 		}
 	}
 
 	public static void findRed(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				if (r > 130)
 					r2.setSample(x, y, 0, 255);
 			}
 		}
 	}
 
 	public static void findBlue(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int b = r1.getSample(x, y, 2);
 				if (b > 110)
 					r2.setSample(x, y, 2, 255);
 			}
 		}
 	}
 
 	public static void findGate(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				if (r > 90 && g > 70 && b < 50) {
 					r2.setSample(x, y, 0, 100);
 					r2.setSample(x, y, 1, 100);
 					r2.setSample(x, y, 2, 100);
 				}
 			}
 		}
 	}
 
 	public static boolean isRed(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
 			int r = r1.getSample(x, y, 0);
 			int g = r1.getSample(x, y, 1);
 			int b = r1.getSample(x, y, 2);
 			//if (b < 150 && r > 2*b && g > 2*b) return true;
 			if (r > 90 && 2*(g+b) < 3*r) return true;
 			//if (r > 110 && 3*(g+b) < 4*r) return true;
 		} return false;
 	}
 
 	public static void safeDraw(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight())
 			r1.setSample(x, y, 0, 255);
 	}
 
 	public static void rasterCircle(WritableRaster r1, int x0, int y0, int radius)
 	{
 		int f = 1 - radius;
 		int ddF_x = 1;
 		int ddF_y = -2 * radius;
 		int x = 0;
 		int y = radius;
 		safeDraw(r1,x0, y0 + radius);
 		safeDraw(r1, x0, y0 - radius);
 		safeDraw(r1, x0 + radius, y0);
 		safeDraw(r1, x0 - radius, y0);
 
 		while(x < y)
 		{
 			// ddF_x == 2 * x + 1;
 			// ddF_y == -2 * y;
 			// f == x*x + y*y - radius*radius + 2*x - y + 1;
 			if(f >= 0)
 			{
 				y--;
 				ddF_y += 2;
 				f += ddF_y;
 			}
 			x++;
 			ddF_x += 2;
 			f += ddF_x;
 			safeDraw(r1, x0 + x, y0 + y);
 			safeDraw(r1, x0 - x, y0 + y);
 			safeDraw(r1, x0 + x, y0 - y);
 			safeDraw(r1, x0 - x, y0 - y);
 			safeDraw(r1, x0 + y, y0 + x);
 			safeDraw(r1, x0 - y, y0 + x);
 			safeDraw(r1, x0 + y, y0 - x);
 			safeDraw(r1, x0 - y, y0 - x);
 		}
 	}
 
 	public static void filledCircle(WritableRaster r1, int x0, int y0, int r) {
 		int xe = Math.min(r1.getWidth(), x0+r+1);
 		int ye = Math.min(r1.getHeight(), y0+r+1);
 		for (int x = Math.max(0, x0-r); x < xe; ++x) {
 			int xq = (x0-x)*(x0-x);
 			for (int y = Math.max(0, y0-r); y < ye; ++y) {
 				if (xq+(y0-y)*(y0-y) <= r*r)
 					r1.setSample(x, y, 0, 255);
 			}
 		}
 	}
 
 	public static float median(float[] arr) {
 		if (arr.length == 0) return 0.0f;
 		if (arr.length % 2 == 0) {
 			return (arr[arr.length/2-1] + arr[arr.length/2])/2;
 		} else {
 			return arr[arr.length/2];
 		}
 	}
 
 	public static float median (float[] arr, int end) {
 		if (end == 0) return 0.0f;
 		if (end % 2 == 0) {
 			return (arr[end/2-1] + arr[end/2])/2;
 		} else {
 			return arr[end/2];
 		}
 	}
 
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
 
 	public static void printList(float[] c) {
 		if (c.length == 0) return;
 		System.out.print("[ ");
 		for (int x = 0; x < c.length-1; ++x) {
 			System.out.print(c[x]+", ");
 		}
 		System.out.println(c[c.length-1]+" ]");
 	}
 
 	public static void circleDetectTop(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int nstartx = startx;
 		while (isRed(r1,nstartx,starty)) ++nstartx;
 		nstartx = (startx+nstartx)/2;
 		int diam = 0;
 		while (isRed(r1,nstartx,starty+diam)) ++diam;
 		if (diam/2 == 0) return;
 		int[] uy = new int[diam];
 		int[] ly = new int[diam];
 		for (int y = starty; y < starty+diam; ++y) {
 			for (int x = nstartx; x < r1.getWidth(); ++x) {
 				if (!isRed(r1,x,y)) {
 					uy[y-starty] = x;
 					break;
 				}
 			} for (int x = nstartx-1; x >= 0; --x) {
 				if (!isRed(r1,x,y)) {
 					ly[y-starty] = x;
 					break;
 				}
 			}
 		}
 		float[] rvals = new float[diam/2-1];
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[x+1]-ly[x+1];
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float lr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(lr-rvals[x]);
 		Arrays.sort(rvals);
 		float ldev = median(rvals);
 		printList(rvals);
 		if (rvals.length < 2) ldev = Float.MAX_VALUE;
 		//System.out.println("lr is "+lr+" ldev is "+ldev);
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[diam-x-1]-ly[diam-x-1];
 			//System.out.println(c);
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float rr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(rr-rvals[x]);
 		Arrays.sort(rvals);
 		float rdev = median(rvals);
 		if (rvals.length < 2) rdev = Float.MAX_VALUE;
 		rvals = null;
 		System.out.println("rr is "+rr+" rdev is "+rdev);
 		if (ldev < 1.0f && ldev < rdev /*&& ldev < udev && ldev < bdev*/) {
 			filledCircle(r2,nstartx,(int)(starty+Math.ceil(lr)),(int)(Math.ceil(lr)));
 			r2.setSample(nstartx, (int)(Math.ceil(starty+lr)), 2, 255);
 		} else if (rdev < 1.0f && rdev < ldev /*&& rdev < udev && rdev < bdev*/) {
 			filledCircle(r2,nstartx,(int)(starty+diam-Math.ceil(rr)),(int)(Math.ceil(rr)));
 			r2.setSample(nstartx, (int)(Math.ceil(starty+diam-rr)), 2, 255);
 		} /*else if (udev < 2.0f && udev < ldev && udev < rdev && udev < bdev) {
 			filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			//filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			//r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		} else if  (bdev < 2.0f && bdev < ldev && bdev < rdev && bdev < udev) {
 			//filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			//r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		}*/ else {
 			System.out.println("circledetect failed");
 		}
 	}
 
 	public static void circleDetect(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int nstarty = starty;
 		while (isRed(r1,startx,nstarty)) ++nstarty;
 		nstarty = (starty+nstarty)/2;
 		int diam = 0;
 		while (isRed(r1,startx+diam,nstarty)) ++diam;
 		if (diam/2 == 0) return;
 		int[] uy = new int[diam];
 		int[] ly = new int[diam];
 		for (int x = startx; x < startx+diam; ++x) {
 			for (int y = nstarty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					uy[x-startx] = y;
 					break;
 				}
 			} for (int y = nstarty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					ly[x-startx] = y;
 					break;
 				}
 			}
 		}
 		//System.out.println("ly length "+ly.length+" uy length "+uy.length);
 		//int r = Math.min(uy.size()/2, uy.get(uy.size()/2)-ly.get(uy.size()/2));
 		//int r = (uy.get(uy.size()/2)-ly.get(uy.size()/2))/2;
 		//int r = uy.size()/2;
 		//int centery = 0;
 		//for (x = 0; x < uy.size(); ++x) {
 		//	centery += uy.get(x)+ly.get(x);
 		//}
 		//centery /= (2*uy.size());
 		//System.out.println(centery);
 		//int lmean = 0;
 		//int lvar = 0;
 		float[] rvals = new float[diam/2-1];
 		//rvals[0] = uy.size()/2.0f;
 		//rvals[uy.size()-1] = (uy.get(uy.size()/2)-ly.get(uy.size()/2))/2.0f;
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[x+1]-ly[x+1];
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float lr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(lr-rvals[x]);
 		Arrays.sort(rvals);
 		float ldev = median(rvals);
 		if (rvals.length < 6) ldev = Float.MAX_VALUE;
 		System.out.println("lr is "+lr+" ldev is "+ldev);
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[diam-x-1]-ly[diam-x-1];
 			//System.out.println(c);
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float rr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(rr-rvals[x]);
 		Arrays.sort(rvals);
 		float rdev = median(rvals);
 		if (rvals.length < 6) rdev = Float.MAX_VALUE;
 		rvals = null;
 		System.out.println("rr is "+rr+" rdev is "+rdev);
 		/*
 		int uc = 0;
 		int umaxv = 0;
 		for (int x = 0; x < diam; ++x) {
 			if (uy[x]-nstarty > umaxv) {
 				umaxv = uy[x]-nstarty;
 				uc = x;
 			}
 		}
 		{
 			int nuc = uc;
 			while (nuc < diam && uy[nuc]-nstarty == umaxv) ++nuc;
 			uc = (uc+nuc)/2;
 		}
 		rvals = new float[umaxv];
 		for (int y = umaxv+nstarty; y > nstarty; --y) {
 			int c = 0;
 			for (int x = uc; x < diam; ++x) {
 				if (uy[x] >= y) ++c;
 				else break;
 			} for (int x = uc-1; x >= 0; --x) {
 				if (uy[x] >= y) ++c;
 				else break;
 			}
 			int h = umaxv-y+nstarty+1;
 			rvals[umaxv-y+nstarty] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float ur = median(rvals);
 		for (int x = 0; x < umaxv; ++x)
 			rvals[x] = Math.abs(ur-rvals[x]);
 		Arrays.sort(rvals);
 		float udev = median(rvals);
 		if (rvals.length < 6) udev = Float.MAX_VALUE;
 		System.out.println("ur is "+ur+" udev is "+udev);
 		rvals = null;
 		int bc = 0;
 		int bmaxv = 0;
 		for (int x = 0; x < diam; ++x) {
 			if (nstarty-ly[x] > bmaxv) {
 				bmaxv = nstarty-ly[x];
 				bc = x;
 			}
 		}
 		{
 			int nbc = bc;
 			while (nbc < diam && nstarty-ly[nbc] == bmaxv) ++nbc;
 			bc = (bc+nbc)/2;
 		}
 		System.out.println("bmaxv is "+bmaxv+" bc is "+bc);
 		rvals = new float[bmaxv];
 		for (int y = nstarty-bmaxv; y < nstarty; ++y) {
 			int c = 0;
 			for (int x = bc; x < diam; ++x) {
 				if (ly[x] <= y) ++c;
 				else break;
 			} for (int x = bc-1; x >= 0; --x) {
 				if (ly[x] <= y) ++c;
 				else break;
 			}
 			int h = bmaxv+y-nstarty+1;
 			rvals[bmaxv+y-nstarty] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float br = median(rvals);
 		for (int x = 0; x < bmaxv; ++x)
 			rvals[x] = Math.abs(br-rvals[x]);
 		Arrays.sort(rvals);
 		float bdev = median(rvals);
 		if (rvals.length < 6) bdev = Float.MAX_VALUE;
 		System.out.println("br is "+br+" bdev is "+bdev);
 		*/
 		if (ldev < 0.5f && ldev < rdev /*&& ldev < udev && ldev < bdev*/ && lr > 3.0f) {
 			filledCircle(r2,(int)(startx+Math.ceil(lr)),nstarty,(int)(Math.ceil(lr)));
 			r2.setSample((int)(Math.ceil(startx+lr)), nstarty, 2, 255);
 		} else if (rdev < 0.5f && rdev < ldev /*&& rdev < udev && rdev < bdev*/ && rr > 3.0f) {
 			filledCircle(r2,(int)(startx+diam-Math.ceil(rr)),nstarty,(int)(Math.ceil(rr)));
 			r2.setSample((int)(Math.ceil(startx+diam-rr)), nstarty, 2, 255);
 		} /*else if (udev < 1.0f && udev < ldev && udev < rdev && udev < bdev && ur > 3.0f) {
 			filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 		} else if (bdev < 1.0f && bdev < ldev && bdev < rdev && bdev < udev && br > 3.0f) {
 			filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		}*/ else {
 			System.out.println("circledetect failed");
 		}
 	}
 
 	public static void circleDetectRightFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx+diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int h = 0;
 		for (int x = startx; x < startx+diam/2; ++x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					++h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx+diam/2; x < startx+diam; ++x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					--h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		printList(rvals);
 		Arrays.sort(rvals);
 		//printList(rvals);
 		float lrt = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevt = median(rvals);
 		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
 		System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		h = 0;
 		for (int x = startx; x < startx+diam/2; ++x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					++h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx+diam/2; x < startx+diam; ++x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					--h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		Arrays.sort(rvals);
 		float lrb = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevb = median(rvals);
 		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
 		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 0.05f*(float)diam && lrt > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 0.05f*(float)diam && lrb > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 
 	public static void circleDetectLeftFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx-diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int h = 0;
 		for (int x = startx; x > startx-diam/2; --x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					++h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx-diam/2; x > startx-diam; --x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					--h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		//printList(rvals);
 		Arrays.sort(rvals);
 		//printList(rvals);
 		float lrt = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevt = median(rvals);
 		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
 		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		h = 0;
 		for (int x = startx; x > startx-diam/2; --x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					++h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx-diam/2; x > startx-diam; --x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					--h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		Arrays.sort(rvals);
 		float lrb = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevb = median(rvals);
 		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
 		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 1.0f+0.025f*(float)diam && lrt > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 1.0f+0.025f*(float)diam && lrb > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 
 	public static void circleDetectRight(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx+diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int stoptop = 0;
 		int prevy = 0;
 		int prevprevy = 0;
 		end1:
 		for (int x = startx; x < startx+diam; ++x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					if (y >= prevy || y > prevprevy) {
 						prevprevy = prevy;
 						prevy = y;
 						++stoptop;
 						int c = 2*(y-starty);
 						rvals[x-startx] = (c*c+4.0f*stoptop*stoptop)/(8.0f*stoptop);
 						break;
 					} else {
 						break end1;
 					}
 				}
 			}
 		}
 		printList(rvals);
 		Arrays.sort(rvals, 0, stoptop);
 		//printList(rvals);
 		float lrt = median(rvals, stoptop);
 		for (int x = 0; x < stoptop; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals, 0, stoptop);
 		float ldevt = median(rvals, stoptop);
 		System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		prevy = prevprevy = r1.getHeight();
 		int stopbot = 0;
 		end2:
 		for (int x = startx; x < startx+diam; ++x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					if (y <= prevy || y < prevprevy) {
 						prevprevy = prevy;
 						prevy = y;
 						++stopbot;
 						int c = 2*(starty-y);
 						rvals[x-startx] = (c*c+4.0f*stopbot*stopbot)/(8.0f*stopbot);
 						break;
 					} else {
 						break end2;
 					}
 				}
 			}
 		}
 		Arrays.sort(rvals, 0, stopbot);
 		float lrb = median(rvals, stopbot);
 		for (int x = 0; x < stopbot; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals, 0, stopbot);
 		float ldevb = median(rvals, stopbot);
 		if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 1.0f && lrt > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 1.0f && lrb > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 
 	public static void circleDetectLeft(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx-diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int stoptop = 0;
 		int prevy = 0;
 		int prevprevy = 0;
 		end1:
 		for (int x = startx; x > startx-diam; --x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					if (y >= prevy || y > prevprevy) {
 						prevprevy = prevy;
 						prevy = y;
 						++stoptop;
 						int c = 2*(y-starty);
 						rvals[startx-x] = (c*c+4.0f*stoptop*stoptop)/(8.0f*stoptop);
 						break;
 					} else {
 						break end1;
 					}
 				}
 			}
 		}
 		//printList(rvals);
 		Arrays.sort(rvals, 0, stoptop);
 		//printList(rvals);
 		float lrt = median(rvals, stoptop);
 		for (int x = 0; x < stoptop; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals, 0, stoptop);
 		float ldevt = median(rvals, stoptop);
 		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		prevy = prevprevy = r1.getHeight();
 		int stopbot = 0;
 		end2:
 		for (int x = startx; x > startx-diam; --x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					if (y <= prevy || y < prevprevy) {
 						prevprevy = prevy;
 						prevy = y;
 						++stopbot;
 						int c = 2*(starty-y);
 						rvals[startx-x] = (c*c+4.0f*stopbot*stopbot)/(8.0f*stopbot);
 						break;
 					} else {
 						break end2;
 					}
 				}
 			}
 		}
 		Arrays.sort(rvals, 0, stopbot);
 		float lrb = median(rvals, stopbot);
 		for (int x = 0; x < stopbot; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals, 0, stopbot);
 		float ldevb = median(rvals, stopbot);
 		if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 1.0f && lrt > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 1.0f && lrb > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 
 	public static void circleFound(WritableRaster r1, int x, int y, int r) {
 		if (x >= 0 && x < r1.getWidth() && y >= 0 && y < r1.getHeight()) {
 			filledCircle(r1,x,y,r);
 			r1.setSample(x, y, 2, 255);
 			//System.out.println(r);
 			System.out.println("distance is "+(600/r)+"cm offcenter is "+(x-r1.getWidth()/2)+"px angle is ");
 		}
 	}
 
 	public static void seekStart2(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = 0;
 			int ey = 0;
 			while (ey < r2.getHeight()) {
 				if (isRed(r1,x,ey)) {
 					while (isRed(r1,x,ey) && ey < r2.getHeight()) ++ey;
 					y += ey;
 					y /= 2;
 					if (!isRed(r1,x-1,y)) {
 						if (!isRed(r2,x,y) && !isRed(r2,x+1,y) && !isRed(r2,x+2,y) && !isRed(r2,x+3,y))
 							circleDetectRightFull(r1,r2,x,y);
 					}
 					if (!isRed(r1,x+1,y)) {
 						if (!isRed(r2,x,y) && !isRed(r2,x-1,y) && !isRed(r2,x-2,y) && !isRed(r2,x-3,y))
 							circleDetectLeftFull(r1,r2,x,y);
 					}
 				}
 				y = ++ey;
 			}
 		}
 	}
 
 	public static void seekStart(WritableRaster r1, WritableRaster r2) {
 		//for (int x = r1.getWidth()-1; x >= 0; --x) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			//for (int y = r1.getHeight()-1; y >= 0; --y) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 		//for (int y = 0; y < r1.getHeight(); ++y) {
 		//	for (int x = 0; x < r1.getWidth(); ++x) {
 				if (isRed(r1,x,y) &&
 					!isRed(r2,x,y) && !isRed(r2,x-1,y) && !isRed(r2,x,y-1) &&
 					!isRed(r2,x-1,y-1) && !isRed(r2,x+1,y-1) && !isRed(r2,x+1,y) &&
 					!isRed(r2,x-1,y+1) && !isRed(r2,x,y+1) && !isRed(r2,x+1,y+1)) {
 					circleDetect(r1,r2,x,y);
 					//circleDetectTop(r1,r2,x,y);
 					System.out.println("circledetect at "+x+" , "+y);
 					//return;
 				}
 			}
 		}
 	}
 
 	public static void findEdge(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int state = 0;
 			int y = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking blue line
 				int v = r2.getSample(x, y, 2);
 				if (v == 255) {
 					if (state++ >= 2) break;
 				} else state = 0;
 			}
 			//System.out.println("ch1");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking end of blue
 				int v = r2.getSample(x, y, 2);
 				if (v != 255) {
 					if (state++ >= 2) break;
 				} else state = 0;
 			}
 			state = 0;
 			int[][] m = {{3,10,3},{0,0,0},{-3,-10,-3}};
 			for (; y < r1.getHeight(); ++y) { // seeking end-of-wall edge
 				int v = 0;
 				if (r1.getSample(x, y, 0)+r1.getSample(x, y, 1)+r1.getSample(x, y, 2) < 490) {
 					if (state++ >= 5) break;
 				} else state = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						//r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						//for (int i = 0; i < rgbf.length; ++i)
 							//rgbf[i] += rgbft[i]*m[my][mx];
 						//v += r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 0)*m[my][mx]+r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 1)*m[my][mx]+r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 2)*m[my][mx];
 						v += (r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 0)+r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 1)+r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 2))*m[my][mx];
 					}
 				}
 				v /= 32;
 				//System.out.println("value of v"+v);
 				if (v > 8) {
 					break;
 				} else {
 					r2.setSample(x, y, 1, 255);
 				}
 			}
 			/*
 			//System.out.println("ch2");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking start of white
 				int v = r2.getSample(x,y,0)+r2.getSample(x,y,1)+r2.getSample(x,y,2);
 				if (v > 230) {
 					if (state++ >= 2) break;
 				} else if (state != 0) state = 0;
 			}
 			//System.out.println("ch3");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking end of white
 				int v = r2.getSample(x,y,0)+r2.getSample(x,y,1)+r2.getSample(x,y,2);
 				if (v < 230) {
 					if (state++ >= 2) break;
 				} else if (state != 0) state = 0;
 				r1.setSample(x, y, 1, 255);
 			}
 			*/
 		}
 	}
 
 	public static double sum5(double[] a) {
 		return a[0]+a[1]+a[2]+a[3]+a[4];
 	}
 
 	public static double sum5l(double[] a) {
 		return a[5]+a[6]+a[7]+a[8]+a[9];
 	}
 
 	public static void shiftleft10(double[] a, double nd) {
 		a[0] = a[1];
 		a[1] = a[2];
 		a[2] = a[3];
 		a[3] = a[4];
 		a[4] = a[5];
 		a[5] = a[6];
 		a[6] = a[7];
 		a[7] = a[8];
 		a[8] = a[9];
 		a[9] = nd;
 	}
 
 	public static void normImage2(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				int tot = r+g+b;
 				//System.err.println("r "+r+" g "+g+" b "+b+" tot "+tot);
 				if (tot != 0) {
 					r2.setSample(x, y, 0, bound(r*255/tot, 255, -255));
 					r2.setSample(x, y, 1, bound(g*255/tot, 255, -255));
 					r2.setSample(x, y, 2, bound(b*255/tot, 255, -255));
 				} else {
 					r2.setSample(x, y, 0, 0);
 					r2.setSample(x, y, 1, 0);
 					r2.setSample(x, y, 2, 0);
 				}
 			}
 		}
 	}
 
 	public static void blankimg(WritableRaster r) {
 		for (int x = 0; x < r.getWidth(); ++x) {
 			for (int y = 0; y < r.getHeight(); ++y) {
 				r.setSample(x, y, 0, 0);
 				r.setSample(x, y, 1, 0);
 				r.setSample(x, y, 2, 0);
 			}
 		}
 	}
 
 	public static void shadeRed(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				if (isRed(r1,x,y)) {
 					r2.setSample(x, y, 0, 255);
 					r2.setSample(x, y, 1, 255);
 					r2.setSample(x, y, 2, 255);
 				} else {
 					r2.setSample(x, y, 0, 0);
 					r2.setSample(x, y, 1, 0);
 					r2.setSample(x, y, 2, 0);
 				}
 			}
 		}
 	}
 
 	public static void testimage(String imgloc) {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		//a.start();
 		//v.start();
 		File f = new File(imgloc);
 		if (!f.exists())
 		{
 			System.out.println("file "+imgloc+" does not exist");
 			return;
 		}
 		BufferedImage im = null;//new BufferedImage();
 		try {
 			//ImageIO.read
 			im = ImageIO.read(f);
 		} catch (IOException e) {
 			System.out.println("could not load image");
 			return;
 		}
 		v.origI = im;
 		v.allocImages();
 		v.setupImagePanels();
 		v.processImage();
 		java.lang.Thread.sleep(296000); // 296 seconds
 		v.bye();
 		//a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testcamera2() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		//a.start();
 		v.start();
 		java.lang.Thread.sleep(300000); // 300 seconds
 		v.bye();
 		//a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void navigate() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(3);
 		InfraR v = new InfraR();
 		v.setup(a, 0);
 		MouseController m = new MouseController();
 		m.setup(a, 1);
 		Bump b = new Bump();
 		b.setup(a, 2);
 		a.start();
 		b.start();
 		v.start();
 		//java.lang.Thread.sleep(10000); // 10 seconds
 		m.start();
 		java.lang.Thread.sleep(300000); // 300 seconds
 		//m.bye();
 		v.bye();
 		b.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void navodom() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(3);
 		InfraR v = new InfraR();
 		v.setup(a, 0);
 		MouseController m = new MouseController();
 		m.setup(a, 1);
 		Bump b = new Bump();
 		b.setup(a, 2);
 		Gyroscope g = new Gyroscope();
 		g.setup(a, 3);
 		Odometry o = new Odometry();
 		m.start();
 		g.start();
 		a.start();
 		b.start();
 		v.start();
 		//java.lang.Thread.sleep(10000); // 10 seconds
 		o.setup(m.m, g);
 		o.start();
 		java.lang.Thread.sleep(300000); // 300 seconds
 		//m.bye();
 		v.bye();
 		b.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void wallfollow2() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		InfraR v = new InfraR();
 		v.setup(a, 0);
 		a.start();
 		v.start();
 		java.lang.Thread.sleep(300000); // 300 seconds
 		v.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void testcamera() {
 		try {
 		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		System.out.println("1");
 		orc.camera.Camera c;
 		System.out.println("1a");
 		c = new orc.camera.Camera("/dev/video0");
 		//c = orc.camera.Camera.makeCamera();
 		System.out.println("2");
 		//orc.camera.Camera c = new orc.camera.Camera("/dev/video0");
 		BufferedImage im = c.createImage();
 		c.capture(im);
 		WritableRaster r = im.getRaster();
 		BufferedImage im2 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//BufferedImage im2 = orc.camera.ImageUtil.rgbToHsv(im);
 		WritableRaster r2 = im2.getRaster();
 		//rgb2hsv(r,r2);
 		normImage2(r,r2);
 		BufferedImage im3 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r3 = im3.getRaster();
 		seekStart2(r,r3);
 		BufferedImage im4 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r4 = im4.getRaster();
 		shadeRed(r,r4);
 		System.out.println("3");
 		System.out.println("4");
 		JFrame jf = new JFrame();
 		ImageIcon ic = new ImageIcon();
 		JLabel jl = new JLabel();
 		ic.setImage(im);
 		jl.setIcon(ic);
 		ImageIcon ic2 = new ImageIcon();
 		JLabel jl2 = new JLabel();
 		ic2.setImage(im2);
 		jl2.setIcon(ic2);
 		ImageIcon ic3 = new ImageIcon();
 		JLabel jl3 = new JLabel();
 		ic3.setImage(im3);
 		jl3.setIcon(ic3);
 		ImageIcon ic4 = new ImageIcon();
 		JLabel jl4 = new JLabel();
 		ic4.setImage(im4);
 		jl4.setIcon(ic4);
 		JPanel cp = new JPanel(new GridLayout(1,4));
 		cp.add(jl);
 		cp.add(jl2);
 		cp.add(jl3);
 		cp.add(jl4);
 		jf.setContentPane(cp);
 		jf.setSize(im.getWidth()*4, im.getHeight());
 		jf.setVisible(true);
 		while (true) {
 			c.capture(im);
 			ic.setImage(im);
 			jl.setIcon(ic);
 			jl.repaint();
 			normImage2(r,r2);
 			//rgb2hsv(r,r2);
 			//im2 = orc.camera.ImageUtil.rgbToHsv(im);
 			ic2.setImage(im2);
 			jl2.setIcon(ic2);
 			jl2.repaint();
 			seekStart2(r,r3);
 			ic3.setImage(im3);
 			jl3.setIcon(ic3);
 			jl3.repaint();
 			blankimg(r3);
 			shadeRed(r,r4);
 			ic4.setImage(im4);
 			jl4.setIcon(ic4);
 			jl4.repaint();
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testmousecontroller() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(3);
 		MouseController mc = new MouseController();
 		mc.setup(a, 2);
 		mc.start();
 		java.lang.Thread.sleep(296000); // 296 seconds
 		mc.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void explore() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(4);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		InfraR ir = new InfraR();
 		ir.setup(a, 1);
 		MouseController mc = new MouseController();
 		mc.setup(a, 2);
 		Bump b = new Bump();
 		b.setup(a,3);
 		v.start();
 		ir.start();
 		b.start();
 		mc.start();
 		//java.lang.Thread.sleep(1000);
 		a.start();
 		//java.lang.Thread.sleep(5000);
 		java.lang.Thread.sleep(296000); // 296 seconds
 		v.bye();
 		ir.bye();
 		mc.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testshoot() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		v.setState(5);
 		v.gatetimer = 501;
 		InfraR ir = new InfraR();
 		ir.setup(a, 1);
 		MouseController mc = new MouseController();
 		mc.setup(a, 2);
 		Bump b = new Bump();
 		b.setup(a,3);
 		v.start();
 		//ir.start();
 		//b.start();
 		//mc.start();
 		java.lang.Thread.sleep(1000);
 		a.start();
 		//java.lang.Thread.sleep(5000);
 		java.lang.Thread.sleep(296000); // 296 seconds
 		v.bye();
 		ir.bye();
 		mc.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void collect() {
 	try {
 		Arbiter a = new Arbiter();
 		a.setup(2);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		InfraR ir = new InfraR();
 		ir.setup(a, 1);
 		//v.mc = new MouseController();
 		//v.mc.setup(a, 1);
 		ir.start();
 		v.start();
 		a.start();
 		//v.mc.start();
 		java.lang.Thread.sleep(296000); // 296 seconds
 		ir.bye();
 		v.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void fetchball() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(2);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		//v.mc = new MouseController();
 		//v.mc.setup(a, 1);
 		a.start();
 		//v.mc.start();
 		v.start();
 		java.lang.Thread.sleep(296000); // 296 seconds
 		v.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void competition() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		Vision v = new Vision();
 		v.setup(a, 0);
 		v.testmode = false;
 		a.start();
 		v.start();
 		java.lang.Thread.sleep(296000); // 296 seconds
 		v.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void wallfollow() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		InfraR v = new InfraR();
 		v.setup(a, 0);
 		a.start();
 		v.start();
 		java.lang.Thread.sleep(50000); // 50 seconds
 		v.bye();
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 		if (args.length > 0) {
 			System.out.println(args[0]);
 			if (args[0].contentEquals("testmotor")) testmotor();
 			if (args[0].contentEquals("testroller")) testroller();
 			else if (args[0].contentEquals("testir")) testir3();
 			else if (args[0].contentEquals("testbump")) testbump();
 			else if (args[0].contentEquals("bump")) bump();
 			else if (args[0].contentEquals("testshoot")) testshoot();
 			else if (args[0].contentEquals("testchannel")) testchannel();
 			else if (args[0].contentEquals("testcirc")) testcirc(args[1]);
 			else if (args[0].contentEquals("testimage")) testimage(args[1]);
 			else if (args[0].contentEquals("testcamera")) testcamera2();
 			else if (args[0].contentEquals("collect")) collect();
 			else if (args[0].contentEquals("fetchball")) fetchball();
 			else if (args[0].contentEquals("competition")) competition();
 			else if (args[0].contentEquals("wallfollow")) wallfollow2();
 			else if (args[0].contentEquals("navigate")) navigate();
 			else if (args[0].contentEquals("navodom")) navodom();
 			else if (args[0].contentEquals("explore")) explore();
 			else if (args[0].contentEquals("testmousecontroller")) testmousecontroller();
 			else if (args[0].contentEquals("saveimages")) saveimages();
 			else if (args[0].contentEquals("testmouse")) testmouse3();
 			//else if (args[0].contentEquals("teststuck")) teststuck();
 			else if (args[0].contentEquals("testforward")) testforward();
 			else if (args[0].contentEquals("testspin")) testspin();
 			else if (args[0].contentEquals("testencoder")) testencoder();
 			else if (args[0].contentEquals("testgyro")) testgyro2();
 			else if (args[0].contentEquals("testodom")) testodom();
 			else if (args[0].contentEquals("testrot")) testrot();
 			else if (args[0].contentEquals("visodom")) visodom();
 			else if (args[0].contentEquals("gyrodrive")) gyrodrive();
 			else if (args[0].contentEquals("gyroturn")) gyroturn(Double.parseDouble(args[1]));
 			else if (args[0].contentEquals("testpid")) testpid(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
 			else System.out.println("unknown option");
 		} else {
 			System.out.println("need argument");
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void shiftleft(double[] a, double v) {
 		int i = 0;
 		for (; i < a.length-1; ++i) {
 			a[i] = a[i+1];
 		}
 		a[i] = v;
 	}
 
 	public static void shiftleft(int[] a, int v) {
 		int i = 0;
 		for (; i < a.length-1; ++i) {
 			a[i] = a[i+1];
 		}
 		a[i] = v;
 	}
 
 	public static void shiftleft6(double[] a, double v) {
 		a[0] = a[1];
 		a[1] = a[2];
 		a[2] = a[3];
 		a[3] = a[4];
 		a[4] = a[5];
 		a[5] = v;
 	}
 
 /*	public static double avgtop(double[] a) {
 		double total = 0.0;
 		for (int i = a.length/2; )
 	}*/
 
 	public static void testpid(double velocity, double kp, double kd, double ki) {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		a.kp = (float)kp;
 		a.kd = (float)kd;
 		a.ki= (float)ki;
 		//Vision v = new Vision();
 		//v.setup(a, 0);
 		//a.start();
 		//v.start();
 		a.leftMotorWeight[0] = 0.5f;
 		a.rightMotorWeight[0] = 0.5f;
 		a.leftMotorAction[0] = (float)velocity;
 		a.rightMotorAction[0] = (float)velocity;
 		a.start();
 		java.lang.Thread.sleep(50000); // 50 seconds
 		a.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testencoder() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor m0 = new Motor(o, 2, false);
 		QuadratureEncoder e0 = new QuadratureEncoder(o, 0, false);
 		while (true) {
 			m0.setPWM(0.3);
 			double v = e0.getVelocity();
 			if (v != 0.0) System.out.println(v);
 			//System.out.println("position in ticks is "+e0.getPosition()+" velocity in ticks per second is "+e0.getVelocity());
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static double averageArray(double[] a) {
 		double total = 0.0;
 		for (int i = 0; i < a.length; ++i) {
 			total += a[i];
 		}
 		return total / (double)a.length;
 	}
 
 	public static double averageArray(int[] a) {
 		double total = 0.0;
 		for (int i = 0; i < a.length; ++i) {
 			total += a[i];
 		}
 		return total / (double)a.length;
 	}
 
 	public static void visodom() {
 		try {
 			Arbiter a = new Arbiter();
 			a.setup(4);
 			Vision v = new Vision();
 			Mouse m = new Mouse();
 			Gyroscope g = new Gyroscope();
 			v.setup(a, 0);
 			g.setup(a, 0);
 			Odometry o = new Odometry();
 			o.setup(m, g);
 			v.odom = o;
 			g.start();
 			m.start();
 			o.start();
 			v.start();
 			java.lang.Thread.sleep(296000);
 			a.bye();
 			g.bye();
 			m.bye();
 			o.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testrot() {
 		try {
 			Arbiter a = new Arbiter();
 			a.setup(2);
 			Gyroscope g = new Gyroscope();
 			g.setup(a, 0);
 			Explorer e = new Explorer();
 			e.setup(a, g, 1);
			e.setState(4);
			e.targang = 180;
 			g.start();
 			java.lang.Thread.sleep(1000);
 			e.start();
 			a.start();
 			java.lang.Thread.sleep(296000);
 			a.bye();
 			e.bye();
 			g.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testodom() {
 		try {
 			Arbiter a = new Arbiter();
 			a.setup(3);
 			Mouse m = new Mouse();
 			Gyroscope g = new Gyroscope();
 			g.setup(a, 0);
 			Odometry o = new Odometry();
 			o.setup(m, g);
 			g.start();
 			m.start();
 			o.start();
 			java.lang.Thread.sleep(296000);
 			a.bye();
 			g.bye();
 			m.bye();
 			o.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testgyro2() {
 		try {
 			Arbiter a = new Arbiter();
 			a.setup(1);
 			Gyroscope g = new Gyroscope();
 			g.setup(a, 0);
 			g.start();
 			java.lang.Thread.sleep(296000);
 			a.bye();
 			g.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testgyro() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		//Gyro g = new Gyro(o, 0); // 0.005 V/(degree/sec)
 		WiiMotionPlus g = new WiiMotionPlus(o);
 		long[] angles = new long[3];
 		long[] baseang = new long[3];
 		int numsamples = 300;
 		while (numsamples > 0) {
 			int[] angvel = g.readAxes();
 			baseang[0] += angvel[0];
 			baseang[1] += angvel[1];
 			baseang[2] += angvel[2];
 			--numsamples;
 		}
 		baseang[0] /= 300;
 		baseang[1] /= 300;
 		baseang[2] /= 300;
 		long prevtime = System.nanoTime();
 		while (true) {
 			//double degrees = g.getTheta()/0.005;
 			//System.out.println(degrees);
 			//System.out.println(g.getTheta());
 			//printList(g.readAxes());
 			int[] angvel = g.readAxes();
 			angvel[0] -= baseang[0];
 			angvel[1] -= baseang[1];
 			angvel[2] -= baseang[2];
 			if (angvel[0] < 100 && angvel[0] > -100) angvel[0] = 0;
 			if (angvel[1] < 100 && angvel[1] > -100) angvel[1] = 0;
 			if (angvel[2] < 100 && angvel[2] > -100) angvel[2] = 0;
 			//printList(angvel);
 			long deltatime = System.nanoTime()-prevtime;
 			//System.out.println(deltatime);
 			angles[0] += (angvel[0]*deltatime)/100000;
 			angles[1] += (angvel[1]*deltatime)/100000;
 			angles[2] += (angvel[2]*deltatime)/100000;
 			//printList(angles);
 			System.out.println(angles[1]*360/80000000);
 			prevtime += deltatime;
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void gyrodrive() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Gyro g = new Gyro(o, 0, 0.005); // 0.005 V/(degree/sec)
 		g.reset();
 		Motor m0 = new Motor(o, 0, true);
 		Motor m1 = new Motor(o, 1, false);
 		double preverror = 0.0;
 		double kp = 0.1;
 		double kd = 0.0;
 		double[] greadings = new double[1];
 		while (true) {
 			shiftleft(greadings, g.getTheta()/0.005);
 			double error = averageArray(greadings);
 			System.out.println(error);
 			double basevel = 0.5;
 			double lspeed = (kp*error-kd*(error-preverror))+basevel;
 			double rspeed = -(kp*error-kd*(error-preverror))+basevel;
 			preverror = error;
 			m0.setPWM(bound(rspeed, 1.0, -1.0));
 			m1.setPWM(bound(lspeed, 1.0, -1.0));
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void gyroturn(double numdegrees) {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Gyro g = new Gyro(o, 0, 0.005); // 0.005 V/(degree/sec)
 		g.reset();
 		Motor m0 = new Motor(o, 0, true);
 		Motor m1 = new Motor(o, 1, false);
 		double preverror = numdegrees;
 		double kp = 0.1;
 		double kd = 0.0;
 		double[] greadings = new double[1];
 		while (true) {
 			shiftleft(greadings, g.getTheta()/0.005);
 			double error = numdegrees - averageArray(greadings);
 			System.out.println(numdegrees+error);
 			double lspeed = kp*error-kd*(error-preverror);
 			double rspeed = -(kp*error-kd*(error-preverror));
 			preverror = error;
 			m0.setPWM(bound(rspeed, 1.0, -1.0));
 			m1.setPWM(bound(lspeed, 1.0, -1.0));
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void getVelocity(byte[] xreadbuf, byte[] yreadbuf, long[] readtimes, double[] outv, int bufend) {
 		long ct = System.nanoTime();
 		int i = bufend;
 		//System.out.println("bufend is "+i+"timedelta is "+(ct-readtimes[i]));
 		int velx = 0;
 		int vely = 0;
 		int numread = 0;
 		while (ct-readtimes[i] < 10000000) { // get readings within the past 100 milliseconds
 			//System.out.println(ct-readtimes[i]);
 			velx += xreadbuf[i];
 			vely += yreadbuf[i];
 			if (--i < 0) {
 				i = 1023;
 			}
 			if (++numread > 200) break;
 		}
 		System.out.println("velx is "+velx+" vely is "+vely);
 	}
 
 	public static void testspin() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor rightMotor = new Motor(o, 0, true);
 		rightMotor.setWatchDog(1000000);
 		Motor leftMotor = new Motor(o, 1, false);
 		rightMotor.setWatchDog(1000000);
 		Motor rollerMotor = new Motor(o, 2, false);
 		rollerMotor.setPWM(0.0);
 		Mouse m = new Mouse();
 		m.start();
 		double basevel = 0.0;
 		final double desv = 0.0;
 		final double dess = 10.0;
 		final double kp = 0.0003;
 		final double kps = 0.0002;
 		final double kd = 0.0002;
 		final double kds = 0.00001;
 		double preverror = 0.0;
 		double preverrors = 0.0;
 		int[] prevx = new int[5];
 		int[] prevy = new int[5];
 		while (m.totalx < 1820) { // 300 cm
 			int vely = 0;
 			int velx = 0;
 			if (System.currentTimeMillis() - m.readtime < 500) {
 				velx = -m.output[1];
 				vely = m.output[2];
 			}
 			shiftleft(prevx, velx);
 			shiftleft(prevy, vely);
 			System.out.println(averageArray(prevx));
 			double error = desv - averageArray(prevy);
 			//if (error < 0) error = 0;
 			double errors = dess - averageArray(prevx);
 			//System.out.println(m.totaly);
 			//System.out.println(kp*error);
 			double rvel = bound(kp*error-kd*(error-preverror)-(kps*errors-kds*(errors-preverrors))+basevel, 1.0, -1.0);
 			double lvel = bound(kp*error-kd*(error-preverror)+(kps*errors-kds*(errors-preverrors))+basevel, 1.0, -1.0);
 			preverror = error;
 			preverrors = errors;
 			rightMotor.setPWM(rvel);
 			leftMotor.setPWM(lvel);
 			java.lang.Thread.sleep(100);
 		}
 		m.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testforward() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor rightMotor = new Motor(o, 0, true);
 		rightMotor.setWatchDog(1000000);
 		rightMotor.setPWM(0.0);
 		Motor leftMotor = new Motor(o, 1, false);
 		leftMotor.setPWM(0.0);
 		rightMotor.setWatchDog(1000000);
 		Motor rollerMotor = new Motor(o, 2, false);
 		rollerMotor.setWatchDog(1000000);
 		rollerMotor.setPWM(0.0);
 		Mouse m = new Mouse();
 		m.start();
 		double basevel = 0.8;
 		final double desv = 40.0;
 		final double dess = 0.0;
 		//final double kp = 0.007;
 		//final double kps = 0.03;
 		final double kp = 0.000;
 		final double kps = 0.01;
 		//final double kd = 0.003;
 		final double kd = 0.000;
 		final double kds = 0.005;
 		final double ki = 0.000;
 		final double kis = 0.00005;
 		double preverror = 0.0;
 		double preverrors = 0.0;
 		int[] prevx = new int[3];
 		int[] prevy = new int[3];
 		while (m.totaly < 300000) { // 300 cm
 			int vely = 0;
 			int velx = 0;
 			if (System.currentTimeMillis() - m.readtime < 500) {
 				velx = -m.output[2];
 				vely = m.output[1];
 			}
 			shiftleft(prevx, velx);
 			shiftleft(prevy, vely);
 			System.out.println(averageArray(prevx));
 			double error = desv - averageArray(prevy);
 			//if (error < 0) error = 0;
 			double errors = dess - averageArray(prevx);
 			//System.out.println(m.totaly);
 			//System.out.println(kp*error);
 			double rvel = bound(kp*error-kd*(error-preverror)-(kps*errors-kds*(errors-preverrors)+kis*m.totaly)+basevel, 1.0, -1.0);
 			double lvel = bound(kp*error-kd*(error-preverror)+(kps*errors-kds*(errors-preverrors)+kis*m.totaly)+basevel, 1.0, -1.0);
 			System.out.append("rvel is "+rvel+" lvel is "+lvel);
 			preverror = error;
 			preverrors = errors;
 			rightMotor.setPWM(rvel);
 			leftMotor.setPWM(lvel);
 			java.lang.Thread.sleep(100);
 		}
 		m.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	public static void test() {
 		try {
 		Mouse m = new Mouse();
 		//m.mapping = true;
 		m.start();
 		while (true) {
 			if (System.currentTimeMillis() - m.readtime < 100) {
 				System.out.println(m.output[1]+","+m.output[2]);
 			} else {
 				System.out.println("0,0");
 			}
 			java.lang.Thread.sleep(50);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}*/
 
 	public static void testmouse3() {
 		try {
 		Mouse m = new Mouse();
 		//m.mapping = true;
 		m.start();
 		while (true) {
 			if (System.currentTimeMillis() - m.readtime < 100) {
 				//System.out.println(m.output[1]+","+m.output[2]);
 			} else {
 				//System.out.println("0,0");
 			}
 			java.lang.Thread.sleep(50);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testmouse2() {
 		try {
 		Mouse m = new Mouse();
 		//java.lang.Thread.sleep(296000); // 296 seconds
 		double[] outv = new double[2];
 		int buflen = 1024;
 		byte[] xreadbuf = new byte[buflen];
 		byte[] yreadbuf = new byte[buflen];
 		long[] readtimes = new long[buflen];
 		m.xreadbuf = xreadbuf;
 		m.yreadbuf = yreadbuf;
 		m.readtimes = readtimes;
 		m.buflen = 1024;
 		m.start();
 		while (true) {
 			//synchronized(m) {
 			getVelocity(xreadbuf, yreadbuf, readtimes, outv, m.bufend);
 			//}
 			java.lang.Thread.sleep(100);
 		}
 		//m.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testmouse() {
 		try {
 			java.io.FileInputStream mouse = new java.io.FileInputStream("/dev/input/mice");
 			byte[] output = new byte[3];
 			long totalx = 0;
 			long totaly = 0;
 			while (true) {
 				//while (mouse.available() > 0) {
 					mouse.read(output);
 					//System.out.println("dx: "+output[1]+" dy: "+output[2]);
 					totalx += output[1];
 					totaly += output[2];
 					System.out.println("totalx: "+(totalx/333.0)+" totaly: "+(totaly/333.0));
 				//}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void bump() {
 		try {
 		Arbiter a = new Arbiter();
 		a.setup(1);
 		Bump b = new Bump();
 		b.setup(a, 0);
 		b.start();
 		a.start();
 		java.lang.Thread.sleep(296000);
 		a.bye();
 		b.bye();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testbump() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		DigitalInput a = new DigitalInput(o, 10, false, true);
 		// 5 cross left
 		// 7 side left
 		// 0 side right
 		// 2 cross right
 		while (true) {
 			System.out.println(a.getValue());
 		}
 		} catch (Exception e) {
 		e.printStackTrace();
 		}
 	}
 
 	public static void testir3() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		AnalogInput left = new AnalogInput(o, 7);
 		AnalogInput right = new AnalogInput(o, 0);
 		AnalogInput cleft = new AnalogInput(o, 5);
 		AnalogInput cright = new AnalogInput(o, 2);
 		// 5 cross left
 		// 7 side left
 		// 0 side right
 		// 2 cross right
 		while (true) {
 			System.out.println("left: "+62.5/left.getVoltage()+"right: "+62.5/right.getVoltage()+"cleft: "+62.5/cleft.getVoltage()+"cright: "+62.5/cright.getVoltage());
 		}
 		} catch (Exception e) {
 		e.printStackTrace();
 		}
 	}
 
 	public static void testir() {
 		try {
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		AnalogInput a = new AnalogInput(o, 7);
 		while (true) {
 			System.out.println(a.getVoltage());
 		}
 		} catch (Exception e) {
 		e.printStackTrace();
 		}
 	}
 
 	public static void testir2() {
 		try {
 		TextChannel tx = new TextChannel("team6");
 		java.lang.Thread.sleep(3000);
 		tx.publish("connected\n");
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor m0 = new Motor(o, 0, true);
 		Motor m1 = new Motor(o, 1, true);
 		AnalogInput a = new AnalogInput(o, 0);
 		double[] prevd = new double[10];
 		{
 			double d = 62.5/a.getVoltage();
 			for (int x = 0; x < prevd.length; ++x) {
 				prevd[x] = d;
 			}
 		}
 		while (true) { // Vd = 62.5, d in cm
 			//System.out.println(62.5/a.getVoltage());
 			double nd = 62.5/a.getVoltage();
 			//System.out.println(nd);
 			shiftleft10(prevd, nd);
 			
 			if (Math.abs(sum5(prevd)-sum5l(prevd)) > 10.0) {
 				// if varies greater than 10 cm, wall has changed
 				tx.publish("wall changed\n");
 				//System.out.println("wall changed\n");
 				//d = nd;
 				//java.lang.Thread.sleep(10000);
 				m0.setPWM(0.0);
 				m1.setPWM(0.0);
 				break;
 			} else {
 				tx.publish(Math.abs(sum5(prevd)-sum5l(prevd))+"old5 is "+sum5(prevd)+"new5 is "+sum5l(prevd));
 				//System.out.println(Math.abs(sum5(prevd)-sum5l(prevd))+"old5 is "+sum5(prevd)+"new5 is "+sum5l(prevd));
 				//tx.publish("delta is "+Math.abs(d-nd)+"\n");
 				//tx.publish(""+Math.abs(d-nd)+"\n");
 				//System.out.println(""+Math.abs(d-nd)+" nd is "+nd+"\n");
 				//d = nd;
 				m0.setPWM(0.7);
 				m1.setPWM(0.7);
 			}
 			 
 			//System.out.println(a.getVoltage());
 		}
 		//java.lang.Thread.sleep(10000);
 		} catch (Exception e) {
 
 		}
 	}
 
 	public static void saveimages() {
 		try {
 		orc.camera.Camera c;
 		c = new orc.camera.Camera("/dev/video0");
 		BufferedImage im = c.createImage();
 		int imgnum = 0;
 		File outfile = null;
 		while ((outfile = new File(imgnum+".png")).exists()) {
 			++imgnum;
 		}
 		while (true) {
 			c.capture(im);
 			javax.imageio.ImageIO.write(im, "png", outfile);
 			outfile = new File((++imgnum)+".png");
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testmotor() {
 		try {
 		/*
 		TextChannel tx = new TextChannel("team6");
 		java.lang.Thread.sleep(10000);
 		tx.publish("Hello World");
 		java.lang.Thread.sleep(10000);
 		*/
 		
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor m0 = new Motor(o, 0, true);
 		m0.setWatchDog(4000000);
 		m0.setPWM(0.7);
 		Motor m1 = new Motor(o, 1, false);
 		m1.setWatchDog(4000000);
 		m1.setPWM(0.7);
 		Motor m2 = new Motor(o, 2, false);
 		m2.setWatchDog(4000000);
 		m2.setPWM(0.7);
 		//while (true) {
 		//m.setPWM(1.0);
 		//}
 		java.lang.Thread.sleep(10000);
 		} catch (Exception e) {
 			
 		}
 	}
 
 	public static void testroller() {
 		try {
 		/*
 		TextChannel tx = new TextChannel("team6");
 		java.lang.Thread.sleep(10000);
 		tx.publish("Hello World");
 		java.lang.Thread.sleep(10000);
 		*/
 
 		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		Motor m2 = new Motor(o, 2, false);
 		m2.setWatchDog(1000000);
 		//m2.setPWM(0.5);
 		while (true) {
 		m2.setPWM(-1.0);
 		}
 		//java.lang.Thread.sleep(10000);
 		} catch (Exception e) {
 
 		}
 	}
 
 	public static void testchannel() {
 		try {
 		TextChannel tx = new TextChannel("team6");
 		java.lang.Thread.sleep(10000);
 		tx.publish("Hello World");
 		java.lang.Thread.sleep(10000);
 		} catch (Exception e) {
 
 		}
 	}
 
 	public static void setExtrema(final WritableRaster r1, final WritableRaster r2, final int x, final int y, final Extrema m) {
 		m.update(x, y);
 		r2.setSample(x, y, 0, 255);
 		if (isRed(r1,x+1,y) && r2.getSample(x+1, y, 0) != 255) {
 			setExtrema(r1,r2,x+1,y,m);
 		} if (isRed(r1,x-1,y) && r2.getSample(x-1, y, 0) != 255) {
 			setExtrema(r1,r2,x-1,y,m);
 		} if (isRed(r1,x,y+1) && r2.getSample(x, y+1, 0) != 255) {
 			setExtrema(r1,r2,x,y+1,m);
 		} if (isRed(r1,x,y-1) && r2.getSample(x, y-1, 0) != 255) {
 			setExtrema(r1,r2,x,y-1,m);
 		}
 	}
 
 	public static void drawline(final WritableRaster r, int x, int y, final int x2, final int y2) {
 		int w = x2 - x ;
 		int h = y2 - y ;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
 		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
 		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
 		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
 		int longest = Math.abs(w) ;
 		int shortest = Math.abs(h) ;
 		if (!(longest>shortest)) {
 			longest = Math.abs(h) ;
 			shortest = Math.abs(w) ;
 			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
 			dx2 = 0 ;
 		}
 		int numerator = longest >> 1 ;
 		for (int i=0;i<=longest;i++) {
 			r.setSample(x, y, 2, 255);
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 	}
 
 	public static void countLine(final WritableRaster r, int x, int y, final int x2, final int y2, final int[] matchvnon) {
 		int w = x2 - x ;
 		int h = y2 - y ;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
 		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
 		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
 		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
 		int longest = Math.abs(w) ;
 		int shortest = Math.abs(h) ;
 		if (!(longest>shortest)) {
 			longest = Math.abs(h) ;
 			shortest = Math.abs(w) ;
 			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
 			dx2 = 0 ;
 		}
 		int numerator = longest >> 1 ;
 		for (int i=0;i<=longest;i++) {
 			//r.setSample(x, y, 2, 255);
 			if (isRed(r,x,y)) {
 				++matchvnon[0];
 			} else {
 				++matchvnon[1];
 			}
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 	}
 
 	public static void findExtrema(final WritableRaster r1, final WritableRaster r2) {
 		Extrema m = new Extrema();
 		int[] matchvnon = new int[2];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getWidth(); ++y) {
 				if (isRed(r1,x,y) && r2.getSample(x, y, 0) != 255) {
 					m.initval(x, y);
 					r2.setSample(x, y, 2, 255);
 					setExtrema(r1, r2, x, y, m);
 					r2.setSample(m.lbx, m.lby, 1, 255);
 					r2.setSample(m.rbx, m.rby, 1, 255);
 					r2.setSample(m.ltx, m.lty, 1, 255);
 					r2.setSample(m.rtx, m.rty, 1, 255);
 					matchvnon[0] = matchvnon[1] = 0;
 					countLine(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4, matchvnon);
 					 drawline(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4);
 					//  drawline(r2, m.lbx, m.lby, m.rbx, m.rby);
 					countLine(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3, matchvnon);
 					 drawline(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3);
 					//  drawline(r2, m.ltx, m.lty, m.rbx, m.rby);
 					countLine(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3, matchvnon);
 					 drawline(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3);
 					//  drawline(r2, m.rtx, m.rty, m.lbx, m.lby);
 					if (matchvnon[0] > matchvnon[1]) {
 						System.out.println("ball"+matchvnon[0]+" vs "+matchvnon[1]);
 						int lbrb = (m.lbx-m.rbx)*(m.lbx-m.rbx)+(m.lby-m.rby)*(m.lby-m.rby); // bot-left to bot-right distance squared
 						int lbrt = (m.lbx-m.rtx)*(m.lbx-m.rtx)+(m.lby-m.rty)*(m.lby-m.rty); // bot-left to top-right distance squared
 						int ltrb = (m.ltx-m.rbx)*(m.ltx-m.rbx)+(m.lty-m.rby)*(m.lty-m.rby); // top-left to bot-right distance squared
 						if (3*lbrb < lbrt || 3*lbrb < ltrb) { // likely actually a gate
 							System.out.println("gate misdetected as ball");
 						}
 					} else {
 						System.out.println("gate"+matchvnon[0]+" vs "+matchvnon[1]);
 					}
 				}
 			}
 		}
 	}
 
 	public static void testcirc(String imgloc) {
 		//for (String x : args)
 		//	System.out.println(x);
 		//if (args.length < 1)
 		//{
 		//	System.out.println("not enough args");
 		//	return;
 		//}
 		File f = new File(imgloc);
 		if (!f.exists())
 		{
 			System.out.println("file "+imgloc+" does not exist");
 			return;
 		}
 		BufferedImage im = null;//new BufferedImage();
 		try {
 			//ImageIO.read
 			im = ImageIO.read(f);
 		} catch (IOException e) {
 			System.out.println("could not load image");
 			return;
 		}
 		WritableRaster r = im.getRaster();
 		BufferedImage im2 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r2 = im2.getRaster();
 		//float[][] m = { {01.f, 0.2f, 0.1f}, {0.2f, 0.4f, 0.2f}, {0.1f, 0.2f, 0.1f} };
 		int[][] m = {{1,2,1},{2,4,2},{1,2,1}};
 		//int[][] m = {{2,4,5,4,2},{4,9,12,9,4},{5,12,15,12,5},{4,9,12,9,4},{2,4,5,4,2}};
 		convolve(r, r2, m, 16);
 		BufferedImage im3 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r3 = im3.getRaster();
 		int[][] m3 = {{3,10,3},{0,0,0},{-3,-10,-3}};
 		convolve(r2, r3, m3, 32);
 		BufferedImage im4 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r4 = im4.getRaster();
 		int[][] m4 = {{3,0,-3},{10,0,-10},{3,0,-3}};
 		convolve(r2, r4, m4, 32);
 		BufferedImage im5 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r5 = im5.getRaster();
 		sqrtImageBW(r3, r4, r5);
 		BufferedImage im6 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		WritableRaster r6 = im6.getRaster();
 		normImage(r2, r6);
 		//BufferedImage im7 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		//WritableRaster r7 = im7.getRaster();
 		//convolve(r6, r7, m, 159);
 		BufferedImage im8 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r8 = im8.getRaster();
 		//copyChannels(r6, r8, 0, 3);
 		normImage2(r2,r8);
 		BufferedImage im9 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r9 = im9.getRaster();
 		copyChannels(r6, r9, 3, 1);
 		/*
 		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r10 = im10.getRaster();
 		convolve(r9, r10, m, 159);
 		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r11 = im11.getRaster();
 		convolve(r10, r11, m, 159);
 		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		WritableRaster r12 = im12.getRaster();
 		mergeChannels(r8, r11, r12);
 		BufferedImage im12n = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r12n = im12n.getRaster();
 		mergeAlpha(r12, r12n);
 		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r13 = im13.getRaster();
 		convolve(r12n, r13, m3, 16);
 		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r14 = im14.getRaster();
 		convolve(r12n, r14, m4, 16);
 		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r15 = im15.getRaster();
 		sqrtImageBW(r13, r14, r15);
 		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r16 = im16.getRaster();
 		rgb2hsv(r, r16);
 		*/
 		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r10 = im10.getRaster();
 		convolve(r8, r10, m3, 32);
 		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r11 = im11.getRaster();
 		convolve(r8, r11, m4, 32);
 		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r12 = im12.getRaster();
 		sqrtImageBW(r10, r11, r12);
 		BufferedImage im9b = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r9b = im9b.getRaster();
 		convolve(r9, r9b, m, 159);
 		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r13 = im13.getRaster();
 		convolve(r9b, r13, m3, 32);
 		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r14 = im14.getRaster();
 		convolve(r9b, r14, m4, 32);
 		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r15 = im15.getRaster();
 		sqrtImageBW(r13, r14, r15);
 		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r16 = im16.getRaster();
 		sqrtImageBW(r12, r15, r16);
 		BufferedImage im17 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r17 = im17.getRaster();
 		seekStart2(r8, r17);
 		//findRed(r8 , r17);
 		BufferedImage im18 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r18 = im18.getRaster();
 		findBlue(r8 , r17);
 		BufferedImage im19 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r19 = im19.getRaster();
 		findEdge(r2, r17);
 		BufferedImage im20 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r20 = im20.getRaster();
 		findExtrema(r2, r20);
 		//findGate(r8, r17);
 		
 		//convolve(r6, r8, m, 159);
 		//for (int x = 0; x < 50; ++x) {
 		//	for (int y = 0; y < 50; ++y)
 		//		im.getR
 		//		im.setRGB(x, y, 255*255*255);
 		//}
 		JFrame jf = new JFrame();
 		ImageIcon ic = new ImageIcon();
 		JLabel jl = new JLabel();
 		ic.setImage(im);
 		jl.setIcon(ic);
 		ImageIcon ic2 = new ImageIcon();
 		JLabel jl2 = new JLabel();
 		ic2.setImage(im2);
 		jl2.setIcon(ic2);
 		ImageIcon ic3 = new ImageIcon();
 		JLabel jl3 = new JLabel();
 		ic3.setImage(im5);
 		jl3.setIcon(ic3);
 		ImageIcon ic4 = new ImageIcon();
 		JLabel jl4 = new JLabel();
 		ic4.setImage(im6);
 		jl4.setIcon(ic4);
 		ImageIcon ic5 = new ImageIcon();
 		//JLabel jl5 = new JLabel();
 		//ic5.setImage(im7);
 		//jl5.setIcon(ic5);
 		ImageIcon ic6 = new ImageIcon();
 		JLabel jl6 = new JLabel();
 		ic6.setImage(im8);
 		jl6.setIcon(ic6);
 		ImageIcon ic7 = new ImageIcon();
 		JLabel jl7 = new JLabel();
 		ic7.setImage(im9);
 		jl7.setIcon(ic7);
 		ImageIcon ic8 = new ImageIcon();
 		JLabel jl8 = new JLabel();
 		ic8.setImage(im12);
 		jl8.setIcon(ic8);
 		ImageIcon ic9 = new ImageIcon();
 		JLabel jl9 = new JLabel();
 		ic9.setImage(im15);
 		jl9.setIcon(ic9);
 		ImageIcon ic10 = new ImageIcon();
 		JLabel jl10 = new JLabel();
 		ic10.setImage(im16);
 		jl10.setIcon(ic10);
 		ImageIcon ic11 = new ImageIcon();
 		JLabel jl11 = new JLabel();
 		ic11.setImage(im17);
 		jl11.setIcon(ic11);
 		ImageIcon ic12 = new ImageIcon();
 		JLabel jl12 = new JLabel();
 		ic12.setImage(im18);
 		jl12.setIcon(ic12);
 		ImageIcon ic13 = new ImageIcon();
 		JLabel jl13 = new JLabel();
 		ic13.setImage(im20);
 		jl13.setIcon(ic13);
 		JPanel cp = new JPanel(new GridLayout(3,4));
 		cp.add(jl);
 		cp.add(jl2);
 		cp.add(jl3);
 		cp.add(jl4);
 		//cp.add(jl5);
 		cp.add(jl6);
 		cp.add(jl7);
 		cp.add(jl8);
 		cp.add(jl9);
 		cp.add(jl10);
 		cp.add(jl11);
 		cp.add(jl12);
 		cp.add(jl13);
 		//jf.getContentPane().add(jl);
 		//jf.getContentPane().add(jl2);
 		//jf.setSize(im.getWidth()*2, im.getHeight());
 		//jf.setVisible(true);
 		jf.setContentPane(cp);
 		jf.setSize(im.getWidth()*4, im.getHeight()*3);
 		jf.setVisible(true);
 		//jf2.setSize(im2.getWidth(), im2.getHeight());
 		//jf2.setVisible(true);
 
 	//System.out.println(args);
         // TODO code application logic here
     }
 
 }
