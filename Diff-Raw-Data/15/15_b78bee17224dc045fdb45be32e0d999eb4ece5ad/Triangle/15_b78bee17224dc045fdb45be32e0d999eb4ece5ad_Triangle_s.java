 package org.utils.render;
 
 import java.util.ArrayList;
 
 import org.utils.MISApplet;
 
 /**
  * Triangle Class, used to render on 2d.
  * @author kerry
  *
  */
 public class Triangle {
 	/**
 	 * This point saves: x, y pixel and color
 	 */
 	int point[][] = new int[3][5];
 	/**
 	 * Save the index for the 3 vertices.
 	 */
 	double zindex[] = new double[3];
 	int area;
 	private int yt, yb, xlt, xrt, xlb, xrb;
 
 	private int[] colorlt = new int[3];
 	private int[] colorrt = new int[3];
 	private int[] colorlb = new int[3];
 	private int[] colorrb = new int[3];
 
 	private double zlt, zrt, zlb, zrb;
 
 	/**
 	 * Create a Triangle. Also need to update the area and top and bot info.
 	 * @param point1
 	 * @param point2
 	 * @param point3
 	 * @param zIndex1
 	 * @param zIndex2
 	 * @param zIndex3
 	 */
 	Triangle(int point1[], int point2[], int point3[], double zIndex1,
 			double zIndex2, double zIndex3) {
 		System.arraycopy(point1, 0, point[0], 0, 5);
 		System.arraycopy(point2, 0, point[1], 0, 5);
 		System.arraycopy(point3, 0, point[2], 0, 5);
 		zindex[0] = zIndex1;
 		zindex[1] = zIndex2;
 		zindex[2] = zIndex3;
 
 		int temp[] = new int[5];
 		double tempd = 0;
 		if (point[0][1] > point[1][1]) {
 			System.arraycopy(point[0], 0, temp, 0, 5);
 			System.arraycopy(point[1], 0, point[0], 0, 5);
 			System.arraycopy(temp, 0, point[1], 0, 5);
 			tempd = zindex[0];
 			zindex[0] = zindex[1];
 			zindex[1] = tempd;
 		}
 		if (point[1][1] > point[2][1]) {
 			System.arraycopy(point[1], 0, temp, 0, 5);
 			System.arraycopy(point[2], 0, point[1], 0, 5);
 			System.arraycopy(temp, 0, point[2], 0, 5);
 			tempd = zindex[1];
 			zindex[1] = zindex[2];
 			zindex[2] = tempd;
 		}
 		if (point[0][1] > point[1][1]) {
 			System.arraycopy(point[0], 0, temp, 0, 5);
 			System.arraycopy(point[1], 0, point[0], 0, 5);
 			System.arraycopy(temp, 0, point[1], 0, 5);
 			tempd = zindex[0];
 			zindex[0] = zindex[1];
 			zindex[1] = tempd;
 		}
 		
 		this.updatePoints();
 		this.makeAntiClock();
 		this.area = this.area();
 	}
 
 	/**
 	 * Split a triangle into 2.
 	 * @param src
 	 * @return
 	 */
 	static public ArrayList<Triangle> splitTriangle(Triangle src) {
 		ArrayList<Triangle> result = new ArrayList<Triangle>();
 		if (src.point[1][1] == src.point[0][1]
 				|| src.point[1][1] == src.point[2][1])
 			result.add(src);
 		else {
 			Triangle temp;
 			int newPoint[] = new int[5];
 			double newZIndex;
 			double k = (src.point[0][0] - src.point[2][0])
 					/ ((double) src.point[0][1] - src.point[2][1]);
 			newPoint[1] = src.point[1][1];
 			newPoint[0] = (int) ((src.point[1][1] - src.point[2][1]) * k + src.point[2][0]);
 			newPoint[2] = (int) ((src.point[1][1] - src.point[2][1])
 					* (src.point[0][2] - src.point[2][2])
					/ ((double) src.point[0][1] - src.point[2][1]) + src.point[2][2]);
 			newPoint[3] = (int) ((src.point[1][1] - src.point[2][1])
 					* (src.point[0][3] - src.point[2][3])
					/ ((double) src.point[0][1] - src.point[2][1]) + src.point[2][3]);
 			newPoint[4] = (int) ((src.point[1][1] - src.point[2][1])
 					* (src.point[0][4] - src.point[2][4])
					/ ((double) src.point[0][1] - src.point[2][1]) + src.point[2][4]);
			newZIndex = (int) ((src.zindex[1] - src.zindex[2])
 					* (src.zindex[0] - src.zindex[2])
					/ ((double) src.point[0][1] - src.point[2][1]) + src.zindex[2]);
 
 			temp = new Triangle(src.point[0], src.point[1], newPoint,
 					src.zindex[0], src.zindex[1], newZIndex);
 			result.add(temp);
 			temp = new Triangle(src.point[2], src.point[1], newPoint,
 					src.zindex[2], src.zindex[1], newZIndex);
 			result.add(temp);
 		}
 		return result;
 	}
 
 	/**
 	 * Figure out the area.
 	 * @return
 	 */
 	private int area() {
 		double area = 0;
 		area = (point[0][0] - point[1][0]) * (point[0][1] + point[1][1])
 				/ ((double) 2);
 		area += (point[0][0] - point[2][0]) * (point[0][1] + point[2][1])
 				/ ((double) 2);
 		area += (point[2][0] - point[1][0]) * (point[2][1] + point[1][1])
 				/ ((double) 2);
 
 		return (int) area;
 	}
 
 	/**
 	 * update the top and bot infor.
 	 */
 	private void updatePoints() {
 		if (point[0][1] < point[1][1]) {
 			this.yt = point[0][1];
 			this.yb = point[1][1];
 			this.xrt = point[0][0];
 			System.arraycopy(point[0], 2, this.colorrt, 0, 3);
 			this.zrt = this.zindex[0];
 			this.xlt = point[0][0];
 			System.arraycopy(point[0], 2, this.colorlt, 0, 3);
 			this.zlt = this.zindex[0];
 			if (point[1][0] > point[2][0]) {
 				this.xrb = point[1][0];
 				this.xlb = point[2][0];
 				System.arraycopy(point[1], 2, this.colorrb, 0, 3);
 				System.arraycopy(point[2], 2, this.colorlb, 0, 3);
 				this.zrb = this.zindex[1];
 				this.zlb = this.zindex[2];
 			} else {
 				this.xlb = point[1][0];
 				this.xrb = point[2][0];
 				System.arraycopy(point[1], 2, this.colorlb, 0, 3);
 				System.arraycopy(point[2], 2, this.colorrb, 0, 3);
 				this.zlb = this.zindex[1];
 				this.zrb = this.zindex[2];
 			}
 		} else {
 			this.yb = point[2][1];
 			this.yt = point[1][1];
 			this.xrb = point[2][0];
 			System.arraycopy(point[2], 2, this.colorrb, 0, 3);
 			this.zrb = this.zindex[2];
 			this.xlb = point[2][0];
 			System.arraycopy(point[2], 2, this.colorlb, 0, 3);
 			this.zlb = this.zindex[2];
 
 			if (point[1][0] > point[0][0]) {
 				this.xrt = point[1][0];
 				this.xlt = point[0][0];
 				System.arraycopy(point[1], 2, this.colorrt, 0, 3);
 				System.arraycopy(point[0], 2, this.colorlt, 0, 3);
 				this.zrt = this.zindex[1];
 				this.zlt = this.zindex[0];
 			} else {
 				this.xrt = point[0][0];
 				this.xlt = point[1][0];
 				System.arraycopy(point[1], 2, this.colorlt, 0, 3);
 				System.arraycopy(point[0], 2, this.colorrt, 0, 3);
 				this.zlt = this.zindex[1];
 				this.zrt = this.zindex[0];
 			}
 		}
 	}
 
 	private int getXL(int y) {
 		double t = (y - yt) / ((double) yb - yt);
 		return (int) (xlt + t * (xlb - xlt));
 	}
 
 	private int getXR(int y) {
 		double t = (y - yt) / ((double) yb - yt);
 		return (int) (xrt + t * (xrb - xrt));
 	}
 
 	private int getRColor(int y, int color) {
 		double t = (y - yt) / ((double) yb - yt);
 		return (int) (colorrt[color] + t * (colorrb[color] - colorrt[color]));
 	}
 
 	private int getLColor(int y, int color) {
 		double t = (y - yt) / ((double) yb - yt);
 		return (int) (colorlt[color] + t * (colorlb[color] - colorlt[color]));
 	}
 
 	private double getZR(int y) {
 		double t = (y - yt) / ((double) yb - yt);
 		return zrt + t * (zrb - zrt);
 	}
 
 	private double getZL(int y) {
 		double t = (y - yt) / ((double) yb - yt);
 		return zlt + t * (zlb - zlt);
 	}
 
 	public boolean isInTriangle(int x, int y) {
 		if (y > yb || y < yt)
 			return false;
 		int rx = this.getXR(y);
 		int lx = this.getXL(y);
 		if (x >= lx && x <= rx) {
 			return true;
 		} else
 			return false;
 	}
 
 	/**
 	 * Render the triangle into the pix array in zBuffer.
 	 * @param pix
 	 * @param color
 	 * @param W
 	 * @param zBuffer
 	 */
 	public void renderTriangle(int pix[], int color, int W, double zBuffer[]) {
 		int[] rColor = new int[3];
 		int[] lColor = new int[3];
 		double zl, zr, z;
 		for (int y = yt; y <= yb; y++) {
 			int rx = this.getXR(y);
 			int lx = this.getXL(y);
 			zl = this.getZL(y);
 			zr = this.getZR(y);
 			rColor[0] = this.getRColor(y, 0);
 			rColor[1] = this.getRColor(y, 1);
 			rColor[2] = this.getRColor(y, 2);
 			lColor[0] = this.getLColor(y, 0);
 			lColor[1] = this.getLColor(y, 1);
 			lColor[2] = this.getLColor(y, 2);
 			for (int x = lx; x <= rx; x++) {
 				z = zl + (x - lx) * (zr - zl) / (rx - lx);
 				if (z > zBuffer[y * W + x]){
 					zBuffer[y*W+x] = z;
 
 					pix[y * W + x] = MISApplet.pack(lColor[0] + (x - lx)
 							* (rColor[0] - lColor[0]) / (rx - lx), lColor[1]
 							+ (x - lx) * (rColor[1] - lColor[1]) / (rx - lx),
 							lColor[2] + (x - lx) * (rColor[2] - lColor[2])
 									/ (rx - lx));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * make the vertices into anti-clock order.
 	 */
 	private void makeAntiClock(){
 		double tempd;
 		int[] temp = new int[5];
 		if(point[0][1] == point[1][1] && point[0][0] < point[1][0]){
 			System.arraycopy(point[0], 0, temp, 0, 5);
 			System.arraycopy(point[1], 0, point[0], 0, 5);
 			System.arraycopy(temp, 0, point[1], 0, 5);
 			tempd = zindex[0];
 			zindex[0] = zindex[1];
 			zindex[1] = tempd;
 		}
 		else if(point[1][1] == point[2][1] && point[1][0] > point[2][0]){
 			System.arraycopy(point[1], 0, temp, 0, 5);
 			System.arraycopy(point[2], 0, point[1], 0, 5);
 			System.arraycopy(temp, 0, point[2], 0, 5);
 			tempd = zindex[1];
 			zindex[1] = zindex[2];
 			zindex[2] = tempd;
 		}
 	}
 
 	public String toString() {
 		return "[" + this.point[0][0] + ", " + this.point[0][1] + "]" + "["
 				+ this.point[1][0] + ", " + this.point[1][1] + "]" + "["
 				+ this.point[2][0] + ", " + this.point[2][1] + "]";
 	}
 }
