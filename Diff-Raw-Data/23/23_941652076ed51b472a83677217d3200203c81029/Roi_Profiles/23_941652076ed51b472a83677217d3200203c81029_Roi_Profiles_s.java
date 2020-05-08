 package huisken.micha;
 
 import ij.IJ;
 import ij.ImagePlus;

 import ij.gui.PolygonRoi;
 import ij.gui.Roi;

 import ij.measure.ResultsTable;

 import ij.plugin.filter.PlugInFilter;

 import ij.process.ImageProcessor;
 
 import java.awt.Polygon;
 


 public class Roi_Profiles implements PlugInFilter {
 	protected ImagePlus image;
 
 	public int setup(String arg, ImagePlus image) {
 		this.image = image;
 		return DOES_8G | DOES_16 | DOES_32;
 	}
 
 	public void run(ImageProcessor ip) {
 		Roi roi = image.getRoi();
 		if(!(roi instanceof PolygonRoi)) {
 			IJ.error("Expected PolygonRoi.");
 			return;
 		}
 
 		PolygonRoi r = (PolygonRoi)roi;
 
 		Polygon poly = r.getPolygon();
 		int n = poly.npoints;
 
 		int[] xs = poly.xpoints;
 		int[] ys = poly.ypoints;
 		ResultsTable rt = new ResultsTable();
 
 		// Fill table
 		for(int z = 0; z < image.getStackSize(); z++) {
 			ip = image.getStack().getProcessor(z + 1);
 
 			int xl = xs[0];
 			int yl = ys[0];
 			rt.incrementCounter();
 			int c = 0;
 			for(int i = 1; i <= n; i++) {
 				LineIterator li = new LineIterator(xl, yl, xs[i % n], ys[i % n]);
 				while(li.next() != null) {
 					int x = (int)Math.round(li.x);
 					int y = (int)Math.round(li.y);
 					int v = ip.get(x, y);
 					rt.addValue(c++, v);
 				}
 				xl = xs[i % n];
 				yl = ys[i % n];
 			}
 		}
 
 		// create column headings
 		int xl = xs[0];
 		int yl = ys[0];
 		int c = 0;
 		for(int i = 1; i <= n; i++) {
 			LineIterator li = new LineIterator(xl, yl, xs[i % n], ys[i % n]);
 			while(li.next() != null) {
 				int x = (int)Math.round(li.x);
 				int y = (int)Math.round(li.y);
 				rt.setHeading(c++, "(" + x + "; " + y + ")");
 			}
 			xl = xs[i % n];
 			yl = ys[i % n];
 		}
 		rt.show("Results");
 	}
 
 
 	private static final class LineIterator {
 
		int x1, y1;
 		int x2, y2;
 		int dx, dy;
 		boolean finished;
 		double x, y, dx_dt, dy_dt;
 
		public LineIterator() {}

 		public LineIterator(int x1, int y1, int x2, int y2) {
 			init(x1, y1, x2, y2);
 		}
 
 		public void init(int x1, int y1, int x2, int y2) {
			this.x1 = x1; this.x2 = x2;
			this.y1 = y1; this.y2 = y2;
 			this.x = x1;
 			this.y = y1;
 
 			dx = x2 - x1;
 			dy = y2 - y1;
 
 			int dt = Math.abs(dx) > Math.abs(dy) ? dx : dy;
 			dt = Math.abs(dt);
 			if(dt == 0)
 				dt = 1;
 
 			dx_dt = (double)dx/dt;
 			dy_dt = (double)dy/dt;
 
 			dx = Math.abs(dx);
 			dy = Math.abs(dy);
 
 			x -= dx_dt;
 			y -= dy_dt;
 
 			finished = false;
 		}
 
 		public LineIterator next() {
 			x += dx_dt;
 			y += dy_dt;
 			finished = Math.round(x) == x2 && Math.round(y) == y2;
 			if(finished)
 				return null;
 			return this;
 		}
 	}
 }
