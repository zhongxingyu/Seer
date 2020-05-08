 import java.awt.Color;
 
 import acm.graphics.GCompound;
 import acm.graphics.GOval;
 
 @SuppressWarnings("serial")
 public class PCircle extends GCompound implements Drawable, Selectable, MadeWith2Points {
 	
 		public static final double POINT_DIAMETER = 10;
 		
 		private GOval gOval_;
 		private final String label_;
 		private FancyLabel fancyLabel_;
 		private PPoint c1_;
 		private PPoint p2_;
 		private boolean selected_;
 		private double labelAngle = -Math.PI/6;
 				
 		/**
 		 * Create a new circle from a center, a point on the circle, and a label.
 		 * @param c1 the center of the circle
 		 * @param p2 a point on the circle
 		 * @param label a label for the circle
 		 */
 		public PCircle(final PPoint c1, final PPoint p2, final String label) {			
 			gOval_ = new GOval(c1.getX(), c1.getY(), p2.getX(), p2.getY());
 			this.add(gOval_);
 			c1_ = c1;
 			p2_ = p2;
 
 			gOval_.setLocation(c1_.getX() - getRadius(), c1_.getY() - getRadius());
 			gOval_.setSize(2*getRadius(), 2*getRadius());
 			
 			label_ = label;
 			fancyLabel_ = new FancyLabel(label_);
 			this.add(fancyLabel_);
 			this.update();
 			
 			setSelected(false);
 		}
 		
 		/**
 		 * Find the radius.
 		 * @return this circle's radius
 		 */
 		public double getRadius() {
 			return PPoint.distance(c1_, p2_);
 		}
 		
 		/**
 		 * Redetermine the graphical properties of this circle based on its center and radius.
 		 */
 		public void update() {
 			gOval_.setLocation(c1_.getX() - getRadius(), c1_.getY() - getRadius());
 			gOval_.setSize(2*getRadius(), 2*getRadius());
 			double radius = gOval_.getWidth() / 2.0;
 			double labelX = c1_.getX() + Math.cos(labelAngle) * radius;
 			double labelY = c1_.getY() - Math.sin(labelAngle) * radius;
 			fancyLabel_.setLocation(labelX,labelY);
 		}
 		
 		public void setSelected(boolean selected) {
 			selected_ = selected;
			setColor(selected ? Color.MAGENTA : Color.BLACK);
 		}
 		
 		public boolean isSelected() {
 			return selected_;
 		}
 		
 		public PPoint get1stPoint() {
 			return c1_;
 		}
 		
 		public PPoint getCenter() {
 			return get1stPoint();
 		}
 		
 		public PPoint get2ndPoint() {
 			return p2_;
 		}
 		
 		public void set1stPoint(PPoint p) {
 			c1_ = p;
 		}
 		
 		public void set2ndPoint(PPoint p) {
 			p2_ = p;
 		}
 				
 		public String getLabel() {
 			return label_;
 		}
 
 		/**
 		 * Determine whether this is the same as another object. A circle is not the same as anything
 		 * which is not a circle, and two circles are the same if they have the same label and
 		 * center.
 		 */
 		@Override
 		public boolean equals(Object o) {
 			
 			if (o instanceof PCircle) {
 				return ((PCircle) o).getLabel().equals(label_);
 			}
 			else {
 				return false;
 			}
 		}
 		
 		@Override
 		public String toString() {
 			return String.format("%s with center (%f.1, %f.1) including point (%f1., %f.1)",
 					getLabel(),
 					c1_.getX(), c1_.getY(),
 					p2_.getX(), p2_.getY());
 		}
 		
 		/**
 		 * Get the distance from the outside of this circle to the given point.
 		 */
 		public double distanceTo(double x, double y) {
 			double cx = c1_.getX();
 			double cy = c1_.getY();
 			
 			double distToCenter = Math.sqrt((x-cx)*(x-cx) + (y-cy)*(y-cy));
 			
 			return Math.abs(distToCenter - getRadius());
 		}
 		
 		
 	}
 
