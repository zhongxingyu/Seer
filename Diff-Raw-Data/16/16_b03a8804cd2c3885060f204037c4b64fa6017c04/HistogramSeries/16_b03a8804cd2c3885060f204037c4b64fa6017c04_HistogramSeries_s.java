 package gui.figure.series;
 
 import java.awt.geom.Point2D;
 import java.util.Comparator;
 import java.util.List;
 
 import math.Histogram;
 
 /**
  * A type of XY series that wraps a Histogram
  * @author brendan
  *
  */
 public class HistogramSeries extends XYSeries {
 
 	Histogram histo;
 	
 	public HistogramSeries(String name, int bins, double min, double max) {
 		super(name);
 		histo = new Histogram(min, max, bins);
 		pointList = null;
 	}
 
 	
 	public HistogramSeries(String name, List<Point2D> points, int bins, double min, double max) {
 		super(name);
 		replace(points, bins, min, max);
		System.out.println("Initializing histogram with min: " + min + " and max: " + max);
 		pointList = null;
		System.out.println(histo.toString());
 	}
 	
 	public HistogramSeries(int bins, double min, double max) {
 		this("Density", bins, min, max);
 	}
 	
 	public void addValue(double x) {
 		int bin = histo.getBin(x);
 		histo.addValue(x);
 	}
 	
 	public void replace(List<Point2D> points, int bins, double min, double max) {
 		histo = new Histogram(min, max, bins);
 		for(int i=0; i<points.size(); i++) {
 			histo.addValue(points.get(i).getY());
 		}
 	}
 	
 	public void clear() {
 		if (histo != null)
 			histo.clear();
 	}
 
 	
 	public void addPointInOrder(Point2D newPoint) {
 		addValue(newPoint.getY());
 	}
 	
 	/**
 	 * The number of bins in the histogram
 	 * @return
 	 */
 	public int getBinCount() {
 		return histo.getBinCount();
 	}
 		
 	/**
 	 * Sorts the values in X order
 	 */
 	private void sortByX() {	}
 
 	/**
 	 * Return the x-value of the point with the given index
 	 * @param index
 	 * @return
 	 */
 	public double getX(int index) {
 		return histo.getMin() + index*histo.getBinWidth();
 	}
 	
 	/**
 	 * return y-yalue of point at given index
 	 * @param index
 	 * @return
 	 */
 	public double getY(int index) {
 		return histo.getCount(index);
 	}
 	
 	
 	public Point2D[] getLineForXVal(double xVal) {
 		int lower = getIndexForXVal(xVal);
 		
 		if (lower<0 || lower>=(histo.getBinCount()-1))
 			return null;
 		
 		int upper = lower+1;
 		Point2D[] line = new Point2D[2];
 		line[0] = new Point2D.Double(getX(lower), getY(lower));
 		line[1] = new Point2D.Double(getX(upper), getY(upper));
 		return line;
 	}
 
 	/**
 	 * Returns the index with the highest x found such that points.get(index).x < xVal. 
 	 */
 	public int getIndexForXVal(double xVal) {
 		return histo.getBin(xVal);
 	}
 	
 	/**
 	 * Returns the Point with the highest index found such that points.get(index).x < xVal. 
 	 * Since the x values are sorted, we can use a bisection search. Additionally, since we
 	 * usually expect x-values to be linearly increasing, we can make an educated guess about
 	 * what the right index is at the start
 	 */
 	public Point2D getClosePointForXVal(double xVal) {
 		int index = getIndexForXVal(xVal);
 		
 		if (index<0 || index>=histo.getBinCount())
 			return null;
 		else
 			return new Point2D.Double(getX(index), histo.getCount(index));
 	}
 	
 	
 	/**
 	 * Return the minimum x value in the list. Since the list is sorted, this is always the x-val of the first 
 	 * point in the list. 
 	 * @return
 	 */
 	public double getMinX() {
 		return histo.getMin();
 	}
 	
 	/**
 	 * Return the maximum x-value in the list. Since the list is sorted by x-value, this is always the x-val
 	 * of the last point in the list. 
 	 * 
 	 * @return
 	 */
 	public double getMaxX() {
 		return histo.getMax();
 	}
 	
 	public double getMinY() {
 		return 0;
 	}
 	
 	
 	public double getMaxY() {
 		return histo.getMaxCount();
 	}
 	
 	/**
 	 * Return the greatest x-val such that the y-val of all subsequent elements is zero. Useful for
 	 * some data sets which tend to generate long lists of 0-valued points (such as allele frequency spectra)
 	 * @return
 	 */
 	public int lastNonZero() {
 		return histo.getBinCount();
 	}
 	
 	/**
 	 * The number of points in the list
 	 */
 	public int size() {
 		return histo.getBinCount();
 	}
 
 	public int getCount() {
 		return histo.getCount();
 	}
 	
 	/**
 	 * Returns the Point at the given index in this list of points, or null if  i> this.size()
 	 * @param i
 	 * @return
 	 */
 	public Point2D get(int i) {
 		if (i>=histo.getBinCount())
 			return null;
 		else {
 			return new Point2D.Double(getX(i), getY(i));
 		}
 	}
 	
 	private Comparator<Point2D> getXComparator() {
 		return new XComparator();
 	}
 
 	class XComparator implements Comparator<Point2D> {
 
 		public int compare(Point2D a, Point2D b) {
 			return a.getX() > b.getX() ? 1 : -1;
 		}
 		
 	}
 
 	
 }
