 package edu.agh.tunev.ui.opengl;
 
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import javax.media.opengl.GL2;
 
 public final class Common {
 
 	// -- bryły
 
	public static void drawCuboid(GL2 gl, double lx, double ly, double lz) {
		drawCuboid(gl, lx, (int) Math.round(Math.ceil(lx)), ly,
				(int) Math.round(Math.ceil(ly)), lz,
				(int) Math.round(Math.ceil(lz)));
	}

	private static void drawCuboid(GL2 gl, double lx, int nx, double ly,
			int ny, double lz, int nz) {
 		double x, x2, y, y2, z, z2;
 		final double dx = lx / nx;
 		final double dy = ly / ny;
 		final double dz = lz / nz;
 
 		// front & back
 		x = 0;
 		y = 0;
 		for (int i = 0; i < nx; i++) {
 			x2 = x + dx;
			y = 0;
 			for (int j = 0; j < ny; j++) {
 				y2 = y + dy;
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(0, 0, 1);
 				gl.glVertex3d(x, y2, lz);
 				gl.glVertex3d(x, y, lz);
 				gl.glVertex3d(x2, y, lz);
 				gl.glVertex3d(x2, y2, lz);
 				gl.glEnd();
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(0, 0, -1);
 				gl.glVertex3d(x2, y2, 0);
 				gl.glVertex3d(x2, y, 0);
 				gl.glVertex3d(x, y, 0);
 				gl.glVertex3d(x, y2, 0);
 				gl.glEnd();
 				y = y2;
 			}
 			x = x2;
 		}
 
 		// top & bottom
 		x = 0;
 		y = 0;
 		z = 0;
 		for (int i = 0; i < nx; i++) {
 			x2 = x + dx;
			z = 0;
 			for (int j = 0; j < nz; j++) {
 				z2 = z + dz;
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(0, -1, 0);
 				gl.glVertex3d(x, 0, z2);
 				gl.glVertex3d(x, 0, z);
 				gl.glVertex3d(x2, 0, z);
 				gl.glVertex3d(x2, 0, z2);
 				gl.glEnd();
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(0, 1, 0);
 				gl.glVertex3d(x, ly, z);
 				gl.glVertex3d(x, ly, z2);
 				gl.glVertex3d(x2, ly, z2);
 				gl.glVertex3d(x2, ly, z);
 				gl.glEnd();
 				z = z2;
 			}
 			x = x2;
 		}
 
 		// left & right
 		y = 0;
 		z = 0;
 		for (int i = 0; i < ny; i++) {
 			y2 = y + dy;
			z = 0;
 			for (int j = 0; j < nz; j++) {
 				z2 = z + dz;
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(-1, 0, 0);
 				gl.glVertex3d(0, y2, z);
 				gl.glVertex3d(0, y, z);
 				gl.glVertex3d(0, y, z2);
 				gl.glVertex3d(0, y2, z2);
 				gl.glEnd();
 				gl.glBegin(GL2.GL_QUADS);
 				gl.glNormal3d(1, 0, 0);
 				gl.glVertex3d(lx, y2, z2);
 				gl.glVertex3d(lx, y, z2);
 				gl.glVertex3d(lx, y, z);
 				gl.glVertex3d(lx, y2, z);
 				gl.glEnd();
 				z = z2;
 			}
 			y = y2;
 		}
 	}
 
 	// -- temp -> color
 
 	public static class Color {
 		final double r, g, b;
 
 		public Color(double r, double g, double b) {
 			this.r = r;
 			this.g = g;
 			this.b = b;
 		}
 
 		public String toString() {
 			return "(" + r + ", " + g + ", " + b + ")";
 		}
 	}
 
 	public static Color temp2Color(double temp) {
 		final Entry<Double, Double> prevEntry = heatMapScale.floorEntry(temp);
 		final Entry<Double, Double> nextEntry = heatMapScale.ceilingEntry(temp);
 
 		if (prevEntry == null && nextEntry == null)
 			return null; // should not happen
 		else if (prevEntry == null)
 			return jetMap(nextEntry.getValue());
 		else if (nextEntry == null)
 			return jetMap(prevEntry.getValue());
 		else if (prevEntry == nextEntry)
 			return jetMap(prevEntry.getValue());
 
 		final double prevI = prevEntry.getValue();
 		final double nextI = nextEntry.getValue();
 
 		final double prevT = prevEntry.getKey();
 		final double nextT = nextEntry.getKey();
 
 		final double ratio = (temp - prevT) / (nextT - prevT);
 
 		return jetMap(prevI + ratio * (nextI - prevI));
 	}
 
 	// temp -> jetMap.x
 	private final static NavigableMap<Double, Double> heatMapScale = new ConcurrentSkipListMap<Double, Double>();
 
 	static {
 		// maps temp [*C] -> jetMap.index [0;1)
 		heatMapScale.put(23.0, 0.0);
 		heatMapScale.put(100.0, 1.0);
 	}
 
 	private static Color jetMap(double x) {
 		double r, g, b;
 		if (x < -1.0 / 8.0)
 			b = 0.0;
 		else if (x < 1.0 / 8.0)
 			b = (x - -1.0 / 8.0) * 4.0;
 		else if (x < 3.0 / 8.0)
 			b = 1.0;
 		else if (x < 5.0 / 8.0)
 			b = (x - 5.0 / 8.0) * -4.0;
 		else
 			b = 0.0;
 
 		if (x < 1.0 / 8.0)
 			g = 0.0;
 		else if (x < 3.0 / 8.0)
 			g = (x - 1.0 / 8.0) * 4.0;
 		else if (x < 5.0 / 8.0)
 			g = 1.0;
 		else if (x < 7.0 / 8.0)
 			g = (x - 7.0 / 8.0) * -4.0;
 		else
 			g = 0.0;
 
 		if (x < 3.0 / 8.0)
 			r = 0.0;
 		else if (x < 5.0 / 8.0)
 			r = (x - 3.0 / 8.0) * 4.0;
 		else if (x < 7.0 / 8.0)
 			r = 1.0;
 		else if (x < 9.0 / 8.0)
 			r = (x - 9.0 / 8.0) * -4.0;
 		else
 			r = 0.0;
 
 		return new Color(r, g, b);
 	}
 
 }
