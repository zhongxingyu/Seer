 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import javax.swing.JOptionPane;
 
 import acm.graphics.GCanvas;
 import acm.graphics.GObject;
 
 
 @SuppressWarnings("serial")
 public class SketchCanvas extends GCanvas {
 
 	    //Constants
 		public final byte SELECT_MODE = 0;
 		public final byte ADDING_POINT_MODE = 1;
 		public final byte ADDING_FIRST_POINT_MODE = 2;
 		public final byte ADDING_MIDPOINT_MODE = 3;
 		public final byte ADDING_LINE_MODE = 4;
 		public final byte ADDING_CIRCLE_MODE = 5;
 		public final byte ADDING_SECOND_POINT_MODE = 6;
 		public final byte INTERSECTION_MODE = 7;
 		
 		public final byte SEGMENT = 0;
 		public final byte LINE = 1;
 		public final byte CIRCLE = 2;
 		public final byte RAY = 3;
 		
 		//Instance Variables
 		private SketchPanel sketchPanel_;
 		private final MainWindow mainWindow_;
 		private byte mode_;
 		private byte elementBeingAdded_;
 		
 		private final PPoints points_;
 		private final Drawables segments_;
 		private final Drawables lines_;
 		private final Drawables rays_;
 		private final Drawables circles_;
 		
 		private final Drawables drawables_;
 		private final Drawables selectedDrawables_;
 		
 		private final LabelMaker labelMaker_;
 		
 		private PPoint workingPoint_;
 		private MadeWith2Points selected2PointObject_;		
 		
 		public void setViewingRectangle(ViewingRectangle viewingRectangle) {
 			PLine.viewingRectangle = viewingRectangle;
 			PRay.viewingRectangle = viewingRectangle;
 		}
 		
 		//Constructors
 		public SketchCanvas(MainWindow mainWindow, int width, int height) {
 			
 			//Initialize instance variables
 			mainWindow_ = mainWindow;
 			labelMaker_ = new LabelMaker();
 			drawables_ = new Drawables();
 			selectedDrawables_ = new Drawables();
 			points_ = new PPoints();
 			segments_ = new Drawables();
 			lines_ = new Drawables();
 			circles_ = new Drawables();
 			rays_ = new Drawables();
 			
 			//Initialize viewing rectangle for line and ray classes
 			setViewingRectangle(new ViewingRectangle(0,width,0,height));
 			
 			//Setup canvas mouseListener
 			this.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {}
 				@Override
 				public void mouseEntered(MouseEvent e) {}
 				@Override
 				public void mouseExited(MouseEvent e) {}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 					
 					switch (mode_) {
 					case ADDING_POINT_MODE :
 						//deselect everything, add a point at the mouse location,
 						//select this new  point and change the mode to SELECT_MODE
 						
 						deselectEverythingInCanvas();
 						PPoint p = addPoint(e);
 						select(p, true);
 						setMode(SELECT_MODE);
 						break;
 						
 					case ADDING_FIRST_POINT_MODE :
 						
 						//begin construction of a segment
 						deselectEverythingInCanvas();
 						
 						//if point clicked in MouseEvent e is close to an existing point,
 						//use this existing point as the first point of the element;
 						//otherwise, add a point where the user clicked
 						PPoint pointClicked = getNearestPointExcept(e, null);
 						PPoint firstPoint;
 						if (pointClicked == null)
 							firstPoint = addPoint(e);
 						else
 							firstPoint = pointClicked;
 						
 						//initially, add the second point at the position clicked in 
 						//MouseEvent e (this element involved should be updated by the 
 						//MouseMotionListener when the mouse is moved)
 						PPoint secondPoint = addPointNoLabel(e);
 						
 						//select (visually) the endpoint of the segment
 						select(secondPoint, true);
 						
 						//add the appropriate object (depending on the elementBeingAdded_)
 						//and then make this the "working" made-with-2-points object
 						selected2PointObject_ = addWith2PointsHalfBaked(firstPoint, secondPoint);
 						
 						//make the endpoint be the "working" point
 						workingPoint_ = secondPoint;
 						
 						break;
 						
 					case ADDING_SECOND_POINT_MODE :
 						PPoint p1 = selected2PointObject_.get1stPoint();
 						PPoint p2 = selected2PointObject_.get2ndPoint();
 						
 						// If we have snapped to a point, find it
 						PPoint closePoint = getNearestPointExcept(e, selected2PointObject_.get2ndPoint());
 
 						// Remove the dummy object and its dummy second point
 						remove((GObject) selected2PointObject_);
 						remove(p2);
 						
 						// If we didn't snap to a point
 						if (closePoint == null) {
 							// Make a new point where the mouse is and recreate a (non-dummy) object
 							addWith2Points(p1, addPoint(e));
 						}
 						else {
 							// Recreate a (non-dummy) object snapped to the nearby point
 							addWith2Points(p1, closePoint);
 						}
 						
 						//the 2-point object has been added, so switch back to select mode
 						mode_ = SELECT_MODE;
 						break;
 						
 					case SELECT_MODE :
 						selectObjectAt(e);
 						break;
 					}
 					
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					if (mode_ == ADDING_FIRST_POINT_MODE)
 						mode_ = ADDING_SECOND_POINT_MODE;
 				}
 			});
 			
 			//Setup canvas mouseMotionListener
 			this.addMouseMotionListener(new MouseMotionListener() {
 
 				@Override
 				public void mouseDragged(MouseEvent e) {
 					if (mode_ == SELECT_MODE && workingPoint_ != null) {
 						double x = e.getX();
 						double y = e.getY();
 						
 						double dx = x - workingPoint_.getX();
 						double dy = y - workingPoint_.getY();
 						workingPoint_.move(dx, dy);
 						drawables_.update();
 					}
 				}
 
 				@Override
 				public void mouseMoved(MouseEvent e) {
 					
 					double x = e.getX();
 					double y = e.getY();
 					
 					switch (mode_) {
 					case ADDING_SECOND_POINT_MODE :
 						//update position of segment endpoint to agree with mouse location
 						//or to "snap" to an existing near point.  
 						
 						if (workingPoint_ != null) {
 							PPoint p = getNearestPointExcept(e, selected2PointObject_.get2ndPoint());
 							if (p != null) {
 								x = p.getX();
 								y = p.getY();
 							}
 							double dx = x - workingPoint_.getX();
 							double dy = y - workingPoint_.getY();
 							workingPoint_.move(dx, dy);
 							
 							//for safety, we update all of the drawable objects
 							drawables_.update();
 						}
 						break;
 					case INTERSECTION_MODE :
 						drawables_.update();
 						break;
 					}
 				}
 				
 			});
 			
 		}
 		
 		//Getters and Setters
 		public SketchPanel getSketchPanel() {
 			return sketchPanel_;
 		}
 		
 		public void setSketchPanel(SketchPanel sketchPanel) {
 			sketchPanel_ = sketchPanel;
 		}
 		
 		public int getMode() {
 			return mode_;
 		}
 		
 		public void setMode(byte mode) {
 			mode_ = mode;
 		}
 		
 		public Drawables getSelectedDrawables() {
 			return selectedDrawables_;
 		}
 		
 		public void setElementBeingAdded(byte elementBeingAdded) {
 			elementBeingAdded_ = elementBeingAdded;
 		}
 		
 		//Instance Methods
 		
 		@Override
 		public void add(GObject obj) {
 			super.add(obj);
 			if (obj instanceof Drawable) {
 				drawables_.add((Drawable) obj);
 
 				if (obj instanceof PPoint) 
 					points_.add((PPoint) obj); 	
 				else if (obj instanceof PSegment)
 					segments_.add((PSegment) obj);
 				else if (obj instanceof PLine)
 					lines_.add((PLine) obj);
 				else if (obj instanceof PRay)
 					rays_.add((PRay) obj);
 				else if (obj instanceof PCircle)
 					circles_.add((PCircle) obj);
 			}
 		}
 		
 		@Override
 		public void remove(GObject obj) {
 			if (obj instanceof Drawable) {
 				select((Drawable) obj, false);
 				drawables_.remove(obj);
 				
 				if (obj instanceof PPoint)
 					points_.remove(obj);
 				else if (obj instanceof PSegment)
 					segments_.remove(obj);
 				else if (obj instanceof PLine)
 					lines_.remove(obj);
 				else if (obj instanceof PRay)
 					rays_.remove(obj);
 				else if (obj instanceof PCircle)
 					circles_.remove(obj);
 			}
 			super.remove(obj);
 		}
 		
 		/**
 		 * Select or deselect the provided Drawable, and update the SketchPanel's display.
 		 * @param d
 		 */
 		public void select(Drawable d, boolean selected) {
 			d.setSelected(selected);
 			if (selected)
 				selectedDrawables_.add(d);
 			else
 				selectedDrawables_.remove(d);
 			updateSelectedDisplay();
 		}
 		
 		/**
 		 * Produce a newline-delimited list of all selected objects.
 		 * @return
 		 */
 		public String getSelectedTypes() {
 			
 			StringBuilder typeStr = new StringBuilder();
 			for (int i=0; i < selectedDrawables_.size(); i++) {
 				Drawable d = selectedDrawables_.get(i);	
 				if (d instanceof PCircle)
 					typeStr.append("circle ");
 				else if (d instanceof PLine)
 					typeStr.append("line ");
 				else if (d instanceof PPoint)
 					typeStr.append("point ");
 				else if (d instanceof PRay)
 					typeStr.append("ray ");
 				else if (d instanceof PSegment)
 					typeStr.append("segment ");
 				
 				typeStr.append(d.getLabel());
 				typeStr.append("\n");
 			}
 			return typeStr.toString();
 		}
 		
 		
 		/**
 		 * Update the SketchPanel's display of selected objects.
 		 */
 		public void updateSelectedDisplay() {
 			sketchPanel_.setSelectedTypesText(getSelectedTypes());
 		}
 		
 		public Drawable getDrawableAt(double x, double y) {
 			//TODO: This still has some issues.  It will return a drawable point within epsilon
 			//of (x,y) if it can, or simply the last drawable in drawables_ that is within
 			//epsilon of (x,y).  Problem: what if there is more than one?  the top most point is
 			//simply the last one added, and there may be no visual cue for some drawables
 			//that this is the case (i.e., segments, lines, circles, etc...) Note: points are 
 			//actually added in this way, so the last one added will appear "above" others points.
 			
 			Drawable aCloseDrawable = null;
 			PPoint aClosePoint = null;
 			
 			for (int i=0; i < drawables_.size(); i++) {
 				Drawable d = drawables_.get(i);
 				double distance = d.distanceTo(x,y);
 				if (distance < Selectable.EPSILON) {
 					aCloseDrawable = d;
 					if (d instanceof PPoint) {
 						aClosePoint = (PPoint) d;
 					}
 				}
 			}
 			
 			//give points priority when selecting something at x,y (they are always "on top")
 			if (aClosePoint != null) {
 				return aClosePoint;
 			}
 			else {
 				return aCloseDrawable;
 			}
 		}
 		
 		public Drawable getPPointAt(double x, double y) {			
 			Drawable closestPoint = null;
 			double minDistance = Selectable.EPSILON;
 			
 			for (Drawable d : points_) {
 				double distance = d.distanceTo(x,y);
 				if (distance < minDistance) {
 					minDistance = distance;
 					closestPoint = d;
 				}
 			}
 			
 			return closestPoint;
 		}
 		
 		private void addStatement(String s, Object... args) {
 			Drawables parents = new Drawables();
 			for (int i=0; i<args.length; i++) {
 				if (args[i] instanceof Drawable) {
 					parents.add((Drawable) args[i]);
 					args[i] = ((Drawable) args[i]).expression();
 				}
 			}
			parents = new Drawables(ListUtils.removeDuplicates(parents));
 			
 			Statement result = new Statement(Expression.parse(String.format(s, args)), null, parents);
 			mainWindow_.addStatement(result);
 		}
 		
 		/**
 		 * Put a point on the canvas.
 		 * @param e where to put the point
 		 * @return the added point
 		 */
 		public PPoint addPoint(MouseEvent e) {
 			//create a new point, add it to the points list, 
 			//and make it visible on the canvas
 			
 			double x = e.getX();
 			double y = e.getY();
 			
 			PPoint point = new PPoint(x, y, labelMaker_.nextLabel(LabelMaker.POINT));
 			add(point); 
 			return point;
 		}
 		/**
 		 * Put an unlabeled point on the canvas.
 		 * @param e where to put the point
 		 * @return the added point
 		 */
 		public PPoint addPointNoLabel(MouseEvent e) {
 			double x = e.getX();
 			double y = e.getY();
 			
 			PPoint point = new PPoint(x, y, "");
 			add(point);
 			return point;
 		}
 		
 		/**
 		 * Put a construction on the canvas.
 		 * @param p1 the first reference point for the construction:
 		 * * One endpoint of a segment
 		 * * A point on a line
 		 * * The center of a circle
 		 * * The endpoint of a ray
 		 * @param p2 the second reference point for the construction:
 		 * * The other endpoint of a segment
 		 * * Another point on a line
 		 * * A point on a circle
 		 * * A point on a ray
 		 * @return the constructed object
 		 */
 		public MadeWith2Points addWith2Points(PPoint p1, PPoint p2) {
 			MadeWith2Points thing;
 			
 			switch(elementBeingAdded_) {
 			case SEGMENT :	thing = new PSegment(p1, p2, p1.getLabel() + "-" + p2.getLabel());
 							break;
 			case LINE :		thing = new PLine(p1, p2, labelMaker_.nextLabel(LabelMaker.LINE));
 							addStatement("on %s %s", p1, thing);
 							addStatement("on %s %s", p2, thing);
 							break;
 							
 			case RAY :		thing = new PRay(p1, p2, labelMaker_.nextLabel(LabelMaker.RAY));
 							addStatement("endpoint %s %s", p1, thing);
 							addStatement("on %s %s", p2, thing);
 							break;
 							
 			case CIRCLE :	thing = new PCircle(p1, p2, labelMaker_.nextLabel(LabelMaker.CIRCLE));
 							addStatement("center %s %s", p1, thing);
 							addStatement("on %s %s", p2, thing);
 							break;
 							
 			default :		throw new IllegalArgumentException();
 			}
 			add((GObject) thing);
 			return thing;
 		}
 
 		/**
 		 * Put a construction on the canvas, without giving it a label or adding a statement.
 		 * @param p1 the first reference point for the construction:
 		 * * One endpoint of a segment
 		 * * A point on a line
 		 * * The center of a circle
 		 * * The endpoint of a ray
 		 * @param p2 the second reference point for the construction:
 		 * * The other endpoint of a segment
 		 * * Another point on a line
 		 * * A point on a circle
 		 * * A point on a ray
 		 * @return the constructed object
 		 */
 		public MadeWith2Points addWith2PointsHalfBaked(PPoint p1, PPoint p2) {
 			MadeWith2Points thing;
 			
 			switch(elementBeingAdded_) {
 			case SEGMENT :	thing = new PSegment(p1, p2, "");
 							break;
 							
 			case LINE :		thing = new PLine(p1, p2, "");
 							break;
 							
 			case RAY :		thing = new PRay(p1, p2, "");
 							break;
 							
 			case CIRCLE :	thing = new PCircle(p1, p2, "");
 							break;
 							
 			default :		throw new IllegalArgumentException();
 			}
 			add((GObject) thing);
 			return thing;
 		}
 		
 		/**
 		 * Put the intersection(s) of two constructions on the canvas.
 		 */
 		public void addIntersection() {			
 			if (selectedDrawables_.size() == 2) {
 				Drawable parent1 = selectedDrawables_.get(0);
 				Drawable parent2 = selectedDrawables_.get(1);
 				Drawables parents = new Drawables();
 				PPoint intersection = null;
 				PPoint intersection2 = null;
 				
 				if ((parent1 instanceof PLine) && (parent2 instanceof PLine)) {
 					parents.addInOrder(parent1, parent2);
 					intersection = new PPoint(PPoint.INTERSECTION_OF_LINES, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if (((parent1 instanceof PLine) && (parent2 instanceof PRay)) || ((parent1 instanceof PRay) && (parent2 instanceof PLine))) {
 					if (parent1 instanceof PLine)
 						parents.addInOrder(parent1, parent2);
 					else
 						parents.addInOrder(parent2, parent1);
 					
 					intersection = new PPoint(PPoint.INTERSECTON_OF_RAY_AND_LINE, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if (((parent1 instanceof PLine) && (parent2 instanceof PSegment)) || ((parent1 instanceof PSegment) && (parent2 instanceof PLine))) {
 					if (parent1 instanceof PLine)
 						parents.addInOrder(parent1, parent2);
 					else 
 						parents.addInOrder(parent2, parent1);
 					
 					intersection = new PPoint(PPoint.INTERSECTION_OF_SEGMENT_AND_LINE, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if (((parent1 instanceof PRay) && (parent2 instanceof PSegment)) || ((parent1 instanceof PSegment) && (parent2 instanceof PRay))) {
 					if (parent1 instanceof PRay)
 						parents.addInOrder(parent1, parent2);
 					else 
 						parents.addInOrder(parent2, parent1);
 					
 					intersection = new PPoint(PPoint.INTERSECTION_OF_SEGMENT_AND_RAY, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if ((parent1 instanceof PRay) && (parent2 instanceof PRay)) {
 					parents.addInOrder(parent1, parent2);
 			
 					intersection = new PPoint(PPoint.INTERSECTON_OF_RAYS, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if ((parent1 instanceof PSegment) && (parent2 instanceof PSegment)) {
 					parents.addInOrder(parent1, parent2);
 					
 					intersection = new PPoint(PPoint.INTERSECTON_OF_SEGMENTS, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if (((parent1 instanceof PCircle) && (parent2 instanceof PLine)) ||
 						 ((parent1 instanceof PLine) && (parent2 instanceof PCircle))) {
 					
 					if (parent1 instanceof PCircle) 
 						parents.addInOrder(parent1, parent2);
 					else 
 						parents.addInOrder(parent2, parent1);
 					
 					//now find one of the intersections and add it
 					intersection = new PPoint(PPoint.LEFT_INTERSECTION_OF_CIRCLE_AND_LINE, parents,
 			                                  labelMaker_.nextLabel(LabelMaker.POINT));
 					
 					//now find the other intersection and add it
 					intersection2 = new PPoint(PPoint.RIGHT_INTERSECTION_OF_CIRCLE_AND_LINE, parents,
 			                                   labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				
 				else if (((parent1 instanceof PCircle) && (parent2 instanceof PRay)) ||
 						 ((parent1 instanceof PRay) && (parent2 instanceof PCircle))) {
 					
 					if (parent1 instanceof PCircle) 
 						parents.addInOrder(parent1, parent2);
 					else 
 						parents.addInOrder(parent2, parent1);
 					
 					//now find one of the intersections and add it
 					intersection = new PPoint(PPoint.LEFT_INTERSECTION_OF_RAY_AND_CIRCLE, parents,
 			                                  labelMaker_.nextLabel(LabelMaker.POINT));
 					
 					//now find the other intersection and add it
 					intersection2 = new PPoint(PPoint.RIGHT_INTERSECTION_OF_RAY_AND_CIRCLE, parents,
 			                                   labelMaker_.nextLabel(LabelMaker.POINT));
 				} 
 				
 				else if (((parent1 instanceof PCircle) && (parent2 instanceof PSegment)) ||
 						 ((parent1 instanceof PSegment) && (parent2 instanceof PCircle))) {
 					
 					if (parent1 instanceof PCircle) 
 						parents.addInOrder(parent1, parent2);
 					else 
 						parents.addInOrder(parent2, parent1);
 					
 					//now find one of the intersections and add it
 					intersection = new PPoint(PPoint.LEFT_INTERSECTION_OF_SEGMENT_AND_CIRCLE, parents,
 			                                  labelMaker_.nextLabel(LabelMaker.POINT));
 					
 					//now find the other intersection and add it
 					intersection2 = new PPoint(PPoint.RIGHT_INTERSECTION_OF_SEGMENT_AND_CIRCLE, parents,
 			                                   labelMaker_.nextLabel(LabelMaker.POINT));
 				} 
 				
 				else if ((parent1 instanceof PCircle) && (parent2 instanceof PCircle)) {
 					parents.addInOrder(parent1, parent2);
 					
 					//first find one of the intersections of the two circles (the left one) and add it
 					intersection = new PPoint(PPoint.LEFT_INTERSECTION_OF_CIRCLES, parents,
 							                  labelMaker_.nextLabel(LabelMaker.POINT));
 					
 					//now find the other intersection of the two circles (the right one) and add it
 					intersection2 = new PPoint(PPoint.RIGHT_INTERSECTION_OF_CIRCLES, parents,
 			                                   labelMaker_.nextLabel(LabelMaker.POINT));
 				}
 				else {
 					JOptionPane.showMessageDialog(null,
 							"Unable to find intersection of the selected items.",
 							"I don't know what to do...",
 							JOptionPane.ERROR_MESSAGE);
 				}
 				
 				if (intersection != null) {
 					add(intersection);
 					addStatement("on %s %s", intersection, parent1);
 					addStatement("on %s %s", intersection, parent2);
 					addStatement("intersect %s %s %s", intersection, parent1, parent2);
 				}
 				if (intersection2 != null) {
 					add(intersection2);
 					addStatement("on %s %s", intersection2, parent1);
 					addStatement("on %s %s", intersection2, parent2);
 					addStatement("intersect %s %s %s", intersection2, parent1, parent2);
 				}
 				
 				deselectEverythingInCanvas();
 				mode_ = SELECT_MODE;
 				if (intersection != null) {
 					select(intersection, true);
 				}
 				if (intersection2 != null) {
 					select(intersection2, true);
 				}
 			}
 			else {
 				JOptionPane.showMessageDialog(null,
 						"The wrong number of items were selected.",
 						"I don't know what to do...",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		/**
 		 * Construct the midpoint of two selected points and put it on the canvas.
 		 */
 		public void addMidpoint() {
 			
 			PPoints selectedPoints = points_.getSelected();
 			
 			//make sure only 2 points were selected
 			//if so, create the midpoint, add it to the points list, and make it visible
 			//on the canvas
 			
 			//TODO: also check to make sure we haven't previously constructed this midpoint
 			
 			if (selectedPoints.size() == 2) {
 				
 				PPoint midpoint = new PPoint(PPoint.MIDPOINT, selectedPoints, labelMaker_.nextLabel(LabelMaker.POINT));
 				
 				add(midpoint);
 				addStatement("midpoint %s %s %s", midpoint, selectedPoints.get(0), selectedPoints.get(1));
 				
 				deselectEverythingInCanvas();				
 				select(midpoint, true);
 			}
 			
 			else {
 				JOptionPane.showMessageDialog(null,
 						"Select 2 points to construct a midpoint.\n" +
 						"There are " + selectedPoints.size() + " points selected.",
 						"Wrong number of points",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		/**
 		 * Deselect each selected point or construction on the canvas.
 		 */
 		public void deselectEverythingInCanvas() {
 			//loop through all of the points, and deselect them
 			for (Drawable d : drawables_)
 				select(d, false);
 			
 			selectedDrawables_.clear();			
 		}
 		
 		/**
 		 * Find the nearest point to the cursor, not including a specified exception.
 		 * @param e where to look for points
 		 * @param exceptionPoint a point to not include in the search. If this is null, no points will be excluded.
 		 * @return the closest point, not including the exceptionPoint if it exists, or null if no point is within EPSILON
 		 */
 		public PPoint getNearestPointExcept(MouseEvent e, PPoint exceptionPoint) {			
 			//get coordinates and key states out of mouse event e
 			double x = e.getX();
 			double y = e.getY();
 			
 			//provided there are points to examine...
 			if (points_.size() > 0) {
 				
 				//set default closest point and minDistance to correspond to the first point of points_
 				PPoint closestPoint = points_.get(0);
 				double minDistance = points_.get(0).getDistanceTo(x, y);
 				
 				//cycle through the rest of the points, updating the closest point and minDistance appropriately
 				//if a closer point is found (except the exceptionPoint)
 				for (int i=0; i<points_.size(); i++) {
 					double distance = points_.get(i).getDistanceTo(x, y);
 					if ((distance < minDistance) && (!points_.get(i).equals(exceptionPoint))) {
 						minDistance = distance;
 						closestPoint = points_.get(i);
 					}
 				}
 				
 				//if the closest point is not within EPSILON units of the coordinates of MouseEvent e, 
 				//we will want to return null
 				if (closestPoint.getDistanceTo(x, y) > Selectable.EPSILON) {
 					closestPoint = null;
 				}
 				return closestPoint;
 			}
 			else { //there were no points to examine in the first place, so return null
 				return null;
 			}
 		}
 		
 		public void selectObjectAt(MouseEvent e) {
 			//get point clicked, if it exists, and make this point the selectedPoint_
 			//deselect all other elements unless shift button is held down
 			 
 			//get coordinates and key states out of mouse event e
 			double x = e.getX();
 			double y = e.getY();
 			boolean isCtrlDown = e.isControlDown();
 			
 			if (!isCtrlDown) {
 				deselectEverythingInCanvas();
 				workingPoint_ = null;
 			}
 			
 			Drawable d = getDrawableAt(x, y);
 			
 			if (d != null) {
 				System.out.println("object selected: " + d);
 				if (d instanceof PPoint) {
 					workingPoint_ = (PPoint) d;    
 				}				
 				select(d, !(isCtrlDown && d.isSelected()));				
 			}
 			
 			
 		}
 	
 }
 
