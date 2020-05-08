 /**
  * 
  */
 package se.iroiro.md.hangeul;
 
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import se.iroiro.md.graph.Coordinate;
 import se.iroiro.md.graph.Graph;
 import se.iroiro.md.graph.XYEdge;
 import se.iroiro.md.graph.XYNode;
 import se.iroiro.md.graph.simple.AbstractXYPositioned;
 import se.iroiro.md.graph.simple.SimpleCoordinate;
 import se.iroiro.md.graph.simple.SimpleGraph;
 import se.iroiro.md.graph.simple.SimpleXYEdge;
 import se.iroiro.md.graph.simple.SimpleXYNode;
 
 /**
  * This class represents a line.
  * @author j
  *
  */
 public class Line extends AbstractXYPositioned implements Cloneable {
 
 	public enum LineType {
 		HORIZONTAL { public String toString(){ return "ー"; }},
 		VERTICAL { public String toString(){ return "|"; }},
 		DIAGONAL_LEFT { public String toString(){ return "／"; }},
 		DIAGONAL_RIGHT { public String toString(){ return "＼"; }},
 		CIRCLE { public String toString(){ return "O"; }},
 		OTHER_CLOSED_POLYGON { public String toString(){ return "口"; }},
 		LEFT_BOX { public String toString(){ return "匚"; }},
 		RIGHT_BOX { public String toString(){ return "コ"; }},
 		UPPER_BOX { public String toString(){ return "冂"; }},
 		LOWER_BOX { public String toString(){ return "凵"; }},
 		TOPLEFT_CORNER { public String toString(){ return "厂"; }},
 		TOPRIGHT_CORNER { public String toString(){ return "フ"; }},
 		BOTTOMLEFT_CORNER { public String toString(){ return "レ"; }},
 		BOTTOMRIGHT_CORNER { public String toString(){ return "」"; }},
 		Z_SHAPE { public String toString(){ return "Z"; }},
 		UNKNOWN { public String toString(){ return "?"; }}
 	}
 
 	/**
 	 * The minimum ratio of the distance between first/last node and the actual line length, in percent.
 	 */
 	private static final double MINIMUM_STRAIGHTNESS_PERCENTAGE = 85;
 	
 	/**
 	 * Maximum variance in polygon radius to be classified as a circle.
 	 */
 	private static final double MAXIMUM_CIRCLE_RADIUS_VARIANCE = 20;
 	
 //	private static final int CORNER_SEGMENTS = 5;
 //	private static final int CORNER_QUEUE_SIZE = 10;
 //	private static final double CORNER_MAX_ANGLE = Math.toRadians(135);
 	
 	
 	/**
 	 * The line type
 	 */
 	private LineType lineType = null;
 	
 	private Graph<Object,Line> graph = null;
 	private Coordinate position = null;
 	private LineGroup group = null;
 //	private List<Corner> corners = null;
 	
 	/**
 	 * Returns the line group this line belongs to.
 	 * @return	the line group this line belongs to
 	 */
 	public LineGroup getGroup(){
 		return group;
 	}
 	
 	/**
 	 * Sets the line group that this line is part of.
 	 * @param group	the line group
 	 */
 	public void setGroup(LineGroup group){
 		this.group = group;
 	}
 	
 	/**
 	 * Returns the average angle of the edges in this line in radians.
 	 * @return	the average angle of the edges in this line
 	 */
 	public double getAngle() {
 		double theta;
 		double s_sin = 0.0;
 		double s_cos = 0.0;
 		for(XYEdge<Object,Line> e : getGraph().getEdges()){
 			theta = e.getAngle();
 			s_sin += Math.sin(theta);
 			s_cos += Math.cos(theta);
 		}
 		theta = Math.atan2(s_sin, s_cos);
 		while(theta < 0) theta += Math.PI * 2;
 		if(theta == 0.0) theta = 0.0;	// prevent negative zero
 		return theta;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#getPosition()
 	 */
 	public Coordinate getPosition() {
 		if(position == null) {
 			Coordinate tl = getTopLeft();
 			Coordinate br = getBottomRight();
 			if(tl == null || br == null) return null;
 			return new SimpleCoordinate((tl.getX() + br.getX())/2,	// Get centre point of bounding rectangle
 										(br.getY() + tl.getY())/2);	// "
 		}
 		return position;
 	}
 
 	/**
 	 * Returns the coordinate of the top left point of the rectangle bounding this line.
 	 * @return	the coordinate of the top left point of the bounding rectangle
 	 */
 	public Coordinate getTopLeft() {
 		return getGraph().getTopLeft();
 	}
 
 	/**
 	 * Returns the coordinate of the bottom right point of the rectangle bounding this line.
 	 * @return	the coordinate of the bottom right point of the bounding rectangle
 	 */
 	public Coordinate getBottomRight() {
 		return getGraph().getBottomRight();
 	}
 	
 	/**
 	 * Resets cached fields.
 	 */
 	private void reset() {
 		position = null;
 		lineType = null;
 		getGraph().clearCache();
 //		corners = null;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#rotate(double, se.iroiro.md.graph.Coordinate)
 	 */
 	public void rotate(double angle, Coordinate pivot) {
 		for(XYNode<Object,Line> n : getGraph().getNodes()){
 			n.rotate(angle,pivot);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#setPosition(double, double)
 	 */
 	public void setPosition(double x, double y) {
 		double dx = x - getPosition().getX();
 		double dy = y - getPosition().getY();
 		for(XYNode<Object,Line> n : getGraph().getNodes()){
 			n.getPosition().translate(dx,dy);	// move all nodes
 		}
 		reset();
 	}
 	
 	/**
 	 * Adds the specified node to the end of the line.
 	 * @param n	the node to add
 	 */
 	public void addNode(XYNode<Object,Line> n){
 		getGraph().addNode(n);
 		reset();
 	}
 
 	/**
 	 * Adds the specified node to the beginning of the line.
 	 * @param n	the node to add
 	 */
 	public void addNodeFirst(XYNode<Object,Line> n){
 		getGraph().addNodeFirst(n);
 		reset();
 	}
 
 	/**
 	 * Adds the specified edge to the end of the line.
 	 * @param e	the edge to add
 	 */
 	public void addEdge(XYEdge<Object,Line> e){
 		getGraph().addEdge(e);
 		reset();
 	}
 	
 	/**
 	 * Adds the edges in the specified list to the end of the line.
 	 * @param e	the list of edges to add
 	 */
 	public void addEdges(List<XYEdge<Object,Line>> e){
 		getGraph().addEdges(e);
 		reset();
 	}
 
 	/**
 	 * Returns the graph of the line. This never returns null.
 	 * @return	the graph of the line
 	 */
 	public Graph<Object,Line> getGraph() {
 		if(graph == null) graph = new SimpleGraph<Object,Line>();
 		return graph;
 	}
 
 	/**
 	 * Adds the specified edge to the beginning of the line.
 	 * @param e	the edge to add
 	 */
 	public void addEdgeFirst(XYEdge<Object, Line> e) {
 		getGraph().addEdgeFirst(e);
 		reset();
 	}
 	
 	/**
 	 * TODO check result
 	 * Reverses all the edges in the line. Node locations are not affected.
 	 * The order of the list of nodes and list of edges in the graph is also reversed.
 	 */
 	public void reverse(){
 		Collections.reverse(getGraph().getEdges());
 		Collections.reverse(getGraph().getNodes());
 		for(XYEdge<Object,Line> e : getGraph().getEdges()){
 			e.reverse();
 		}
 		reset();
 	}
 
 	/**
 	 * Clones the line.
 	 * Simple returns a new line with a cloned graph, and all caches reset.
 	 * If the old graph is null, the new graph will also be null.
 	 */
 	@SuppressWarnings("unchecked")
 	public Object clone() {
 		try {
 			Line l = (Line) super.clone();
 			if(graph != null){
 				l.graph = (Graph<Object,Line>) graph.clone();
 				for(XYEdge<Object,Line> e : l.graph.getEdges()){
 					e.setPiggybackObject(this);
 				}
 			}
 			l.reset();
 			return l;
 		} catch (CloneNotSupportedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Returns <code>true</code> if this line is determined to be a circle.
 	 * @return	<code>true</code> if this line is a circle
 	 */
 	private boolean isCircle() {
 		boolean circle = false;
 		if(isClosedPolygon()){	// the radius variance of a square-fitted shape with a side of 100 units.
 			circle = getCircularity() <= MAXIMUM_CIRCLE_RADIUS_VARIANCE;	// is compared to a threshold
 		}
 		return circle;
 	}
 	
 	/**
 	 * TODO make private
 	 * Returns <code>true</code> if this line represents a closed polygon, i.e. the first and last node are neighbours.
 	 * @return	<code>true</code> if this line represents a closed polygon
 	 */
 	public boolean isClosedPolygon() {
 		return getFrom() != null && getFrom().isNeighbour(getTo());
 	}
 	
 	/**
 	 * Returns a measurement of the circularity of this line.
 	 * This is done by first normalising the line to make it circle-like,
 	 * and then measure and return radius variance.
 	 * @return	the radius variance after line normalisation
 	 * @see Line#normaliseToCircle()
 	 */
 	private double getCircularity() {
 		double circularity = -1;
 		if(isClosedPolygon()){
 			Line copy = (Line) clone();
 			copy.normaliseToCircle();
 			circularity = copy.getRadiusVariance();
 		}
 		reset();
 		return circularity;
 	}
 	
 	private double getRadiusVariance() {
 		double mean = getRadiusMean();
 		double variance = 0;
 		double delta;
 		for(XYNode<Object,Line> n : getGraph().getNodes()){
 			delta = getPosition().distanceTo(n.getPosition()) - mean;
 			variance += delta * delta;
 		}
 		variance /= getGraph().getNodes().size();
 		return variance;
 	}
 
 	private double getRadiusMean() {
 		double mean = 0;
 		for(XYNode<Object,Line> n : getGraph().getNodes()){
 			mean += getPosition().distanceTo(n.getPosition());
 		}
 		mean /= getGraph().getNodes().size();
 		return mean;
 	}
 
 	/**
 	 * Iterates through all nodes, rotates and squeezes to make this line as similar
 	 * to a circle as possible without altering the shape itself (apart from squeezing).
 	 * TODO Write more here
 	 */
 	private void normaliseToCircle() {
 		double angle;
 		int iterations = 5; // 5 seems to work well so we choose 5.
 		XYEdge<Object,Object> shortest;
 		for(int i = 0; i < iterations; i++){
 			shortest = getShortestRadius();
 			angle = Math.PI/2 - shortest.getAngle();
 			rotate(angle);
 			equaliseAxes();
 		}
 		squeeze(100);	// make both X and Y sides of the shape 100 units long (to be able to compare shapes of different size)
 	}
 	
 	/**
 	 * Scans the line for the shortest radius line and returns it as an edge.
 	 * The returned edge is not connected to any node in the line itself.
 	 * @return	an edge representing the shortest radius in the line
 	 */
 	private XYEdge<Object,Object> getShortestRadius() {
 		XYEdge<Object,Object> shortest = null;	// init return object
 		if(getGraph().getNodes().size() > 0){	// if there are nodes in graph
 			shortest = new SimpleXYEdge<Object,Object>(new SimpleXYNode<Object,Object>(), new SimpleXYNode<Object,Object>());	// make new edge
 			Coordinate centre = getPosition();
 			shortest.getFrom().setPosition(centre.getX(),centre.getY());	// set its from-position to centre of line
 			Coordinate outer = getGraph().getNodes().get(0).getPosition();	// set its to-position to that of the first node
 			shortest.getTo().setPosition(outer.getX(),outer.getY());			// "
 			for(XYNode<Object,Line> n : getGraph().getNodes()){	// loop through all nodes
 				if(Double.compare(getPosition().distanceTo(n.getPosition()), shortest.getLength()) < 0){	// if distance from center to node is shorter than edge length
 					shortest.getTo().getPosition().setXY(n.getPosition().getX(), n.getPosition().getY());	// set new to-node
 				}
 			}
 		}
 		return shortest;
 	}
 
 	/**
 	 * Returns the width of the rectangle bounding this line.
 	 * @return	the width
 	 */
 	public double getWidth() {
 		return getGraph().getWidth();
 	}
 
 	/**
 	 * Returns the height of the rectangle bounding this line.
 	 * @return	the height
 	 */
 	public double getHeight() {
 		return getGraph().getHeight();
 	}
 	
 	/**
 	 * Sets both axes (<i>x</i> and <i>y</i>) to the same length.
 	 * The length of the longer axis is set to the length of the shorter axis.
 	 * After calling this method, the rectangle bounding the line is a wide as it is high.
 	 */
 	private void equaliseAxes(){
 		double w = getWidth();
 		double h = getHeight();
 		double side = w < h ? w : h;
 		squeeze(side);
 	}
 	
 	/**
 	 * Squeezes the line so that the sides of its bounding box are all <code>side</code> units long.
 	 * @param side	the length of a side
 	 */
 	private void squeeze(double side){
 		double w = getWidth();
 		double h = getHeight();
 		if(w == 0 || h == 0) return;
 		double ox = getPosition().getX();
 		double oy = getPosition().getY();
 		double mx = side / w;
 		double my = side / h;
 		for(XYNode<Object,Line> n : getGraph().getNodes()){
 			n.getPosition().setX((n.getPosition().getX()-ox) * mx + ox);
 			n.getPosition().setY((n.getPosition().getY()-oy) * my + oy);
 		}
 		reset();
 	}
 	
 	/**
 	 * Returns the first node in this line.
 	 * @return	the first node
 	 */
 	public XYNode<Object,Line> getFrom() {
 		return getGraph().getNodes().get(0);
 	}
 	
 	/**
 	 * Returns the last node in this line.
 	 * @return	the last node
 	 */
 	public XYNode<Object,Line> getTo() {
 		List<XYNode<Object,Line>> nodes = getGraph().getNodes();
 		return nodes.get(nodes.size()-1);
 	}
 	
 	/**
 	 * TODO Check the result of this method.
 	 * Returns a fraction of the line as a new <code>Line</code> object,
 	 * but with references to (a fraction of) the same edges and nodes.
 	 * If a separate copy is needed, simply clone the returned line.
 	 * Segment 1 is the first segment, and segment <code>segments</code> is the last segment.
 	 * Example: <code>from = 2</code>, <code>to = 3</code> and <code>segments = 4</code>
 	 * would give the second and third fourth of the line.
 	 * If the range is invalid or the resulting fraction contains no edges or nodes,
 	 * an empty <code>Line</code> will be returned.
 	 * @param from	the first segment to include
 	 * @param to	the last segment to include
 	 * @param segments	the total number of segments to divide the line into
 	 * @return	a fraction of the line
 	 */
 	public Line getFraction(int from, int to, int segments){
 		Line l = new Line();
 		if(to > segments || to < from || from < 0) return l;	// if the range is wrong, return the empty Line object
 		int fromIndex = (int) (((from-1) * 1.0 / segments) * getGraph().getEdges().size());	// check this
 		int toIndex = (int) ((to * 1.0 / segments) * getGraph().getEdges().size());			// "
 		if(fromIndex >= 0 && toIndex <= getGraph().getEdges().size()){
 			l.addEdges(getGraph().getEdges().subList(fromIndex, toIndex));
 			
 			// START [copied from SimpleGraph.java]
 			Iterator<XYEdge<Object,Line>> it = l.getGraph().getEdges().iterator();
 			Graph<Object,Line> linegraph = l.getGraph();
 			XYEdge<Object,Line> e = null;	// add the nodes as well
 			while(it.hasNext()){			//TODO Check result.
 				e = it.next();
 				linegraph.addNode(e.getFrom());
 			}
 			if(e != null && !linegraph.getNodes().contains(e.getTo())){
 				linegraph.addNode(e.getTo());
 			}
 			// END
 			
 //			for(XYEdge<Object,Line> e : l.getGraph().getEdges()){
 //				l.getGraph().addNode(e.getFrom());
 //			}
 //			List<XYEdge<Object,Line>> edges = l.getGraph().getEdges();
 //			List<XYNode<Object,Line>> nodes = l.getGraph().getNodes();
 //			if(!nodes.isEmpty() && !nodes.contains(edges.get(edges.size()-1).getTo())){
 //				l.getGraph().addNode(edges.get(edges.size()-1).getTo());
 //			}
 		}
 		return l;
 	}
 	
 	/**
 	 * Returns the fraction of the line close to the specified end point.
 	 * If the specified point is not part of the line, the method returns <code>null</code>.
 	 * @param end	end point
 	 * @param fraction	the fraction to extract
 	 * @param segments	the total number of segments to divide the line into
 	 * @return	a fraction of the line
 	 */
 	public Line getFractionCloseTo(XYNode<Object,Line> end, int fraction, int segments){
 		if(end == getFrom()){
 			return getFraction(fraction,segments);
 		}
 		if (end == getTo()){
 			return getFraction(segments,segments);
 		}
 		return null;
 	}
 	
 	/**
 	 * Equal to <code>getFraction(fraction,fraction,segments)</code>.
 	 * @param fraction	the fraction to extract
 	 * @param segments	the total number of segments to divide the line into
 	 * @return	a fraction of the line
 	 * @see	Line#getFraction(int, int, int)
 	 */
 	public Line getFraction(int fraction, int segments){
 		return getFraction(fraction,fraction,segments);
 	}
 	
 	/**
 	 * Returns the line type for the line.
 	 * @return	the line type
 	 * @see LineType
 	 */
 	public LineType getType(){
 		if(lineType != null) return lineType;
 		if(isCircle()){
 			lineType = LineType.CIRCLE;
 		}else if(isClosedPolygon()){
 			lineType = LineType.OTHER_CLOSED_POLYGON;
 		}else if(isHorizontal()){
 			lineType = LineType.HORIZONTAL;
 		}else if(isVertical()){
 			lineType = LineType.VERTICAL;
 		}else if(isLeftDiagonal()){
 			lineType = LineType.DIAGONAL_LEFT;
 		}else if(isRightDiagonal()){
 			lineType = LineType.DIAGONAL_RIGHT;
 		}else if(isZShape()){
 			lineType = LineType.Z_SHAPE;
 		}else if(isHorizontalHalfBox() && isLeftBalanced()){
 			lineType = LineType.LEFT_BOX;
 		}else if(isHorizontalHalfBox() && isRightBalanced()){
 			lineType = LineType.RIGHT_BOX;
 		}else if(isVerticalHalfBox() && isTopBalanced()){
 			lineType = LineType.UPPER_BOX;
 		}else if(isVerticalHalfBox() && isBottomBalanced()){
 			lineType = LineType.LOWER_BOX;
 		}else if(hasTopLeftCorner()){
 			lineType = LineType.TOPLEFT_CORNER;
 		}else if(hasTopRightCorner()){
 			lineType = LineType.TOPRIGHT_CORNER;
 		}else if(hasBottomLeftCorner()){
 			lineType = LineType.BOTTOMLEFT_CORNER;
 		}else if(hasBottomRightCorner()){
 			lineType = LineType.BOTTOMRIGHT_CORNER;
 		}else{
 			lineType = LineType.UNKNOWN;
 		}
 		return lineType;
 	}
 	
 	/**
 	 * Directly sets the line type - the line will maintain the set line type as long as its graph is not modified.
 	 * This can be used to set a line type to a line without a graph.
 	 * @param lt	the line type of the line
 	 */
 	public void setLineType(LineType lt){
 		lineType = lt;
 	}
 	
 	/**
 	 * TODO Write more here.
 	 * Returns <code>true</code> if the line has a top left corner.
 	 * @return	<code>true</code> if the line has a top left corner
 	 */
 	private boolean hasTopLeftCorner(){
 		Line fraction;
 		if(getFrom().getPosition().getY() > getTo().getPosition().getY()){	// if from-node is above to-node
 			if(getFrom().getPosition().getX() < getPosition().getX()) return false;	// return false if from-node is left of line centre
 			fraction = getFraction(1,5);	// otherwise get first part of line
 		}else{	// if from-node is below to-node
 			if(getTo().getPosition().getX() < getPosition().getX()) return false;	// return false if to-node is left of line centre
 			fraction = getFraction(5,5);	// otherwise get last part of line
 		}
 		return fraction.isHorizontal();	// return true if the part is horizontal, false otherwise
 	}
 
 	/**
 	 * TODO Write more here.
 	 * Returns <code>true</code> if the line has a top right corner.
 	 * @return	<code>true</code> if the line has a top right corner
 	 */
 	private boolean hasTopRightCorner(){
 		Line fraction;
 		if(getFrom().getPosition().getY() > getTo().getPosition().getY()){	// if from-node is above to-node
 			if(getFrom().getPosition().getX() > getPosition().getX()) return false;	// return false if from-node is right of line centre
 			fraction = getFraction(1,5);	// otherwise get first part of line
 		}else{	// if from-node is below to-node
 			if(getTo().getPosition().getX() > getPosition().getX()) return false;	// return false if to-node is right of line centre
 			fraction = getFraction(5,5);	// otherwise get last part of line
 		}
 		return fraction.isHorizontal();	// return true if the part is horizontal, false otherwise
 	}
 
 	/**
 	 * TODO Write more here.
 	 * Returns <code>true</code> if the line has a bottom left corner.
 	 * @return	<code>true</code> if the line has a bottom left corner
 	 */
 	private boolean hasBottomLeftCorner(){
 		Line fraction;
 		if(getFrom().getPosition().getY() > getTo().getPosition().getY()){	// if from-node is above to-node
 			if(getFrom().getPosition().getX() > getPosition().getX()) return false;	// return false if from-node is right of line centre
 			fraction = getFraction(5,5);	// otherwise get last part of line
 		}else{	// if from-node is below to-node
 			if(getTo().getPosition().getX() > getPosition().getX()) return false;	// return false if to-node is right of line centre
 			fraction = getFraction(1,5);	// otherwise get first part of line
 		}
 		return fraction.isHorizontal();	// return true if the part is horizontal, false otherwise
 	}
 
 	/**
 	 * TODO Write more here.
 	 * Returns <code>true</code> if the line has a bottom right corner.
 	 * @return	<code>true</code> if the line has a bottom right corner
 	 */
 	private boolean hasBottomRightCorner(){
 		Line fraction;
 		if(getFrom().getPosition().getY() > getTo().getPosition().getY()){	// if from-node is above to-node
 			if(getFrom().getPosition().getX() < getPosition().getX()) return false;	// return false if from-node is left of line centre
 			fraction = getFraction(5,5);	// otherwise get last part of line
 		}else{	// if from-node is below to-node
 			if(getTo().getPosition().getX() < getPosition().getX()) return false;	// return false if to-node is left of line centre
 			fraction = getFraction(1,5);	// otherwise get first part of line
 		}
 		return fraction.isHorizontal();	// return true if the part is horizontal, false otherwise
 	}
 
 	/**
 	 * TODO Write javadoc.
 	 * @return
 	 */
 	private boolean isHorizontalHalfBox(){
 		Line first = getFraction(1,5);
 		Line last = getFraction(5,5);
 		return first.hasAngle(last.getAngle() + Math.PI, Math.PI / 8) &&
 				first.hasAngleOrOpposite(0, Math.PI / 8);
 	}
 	
 	/**
 	 * TODO Write javadoc.
 	 * @return
 	 */
 	private boolean isVerticalHalfBox(){
 		Line first = getFraction(1,5);
 		Line last = getFraction(5,5);
 		return first.hasAngle(last.getAngle() + Math.PI, Math.PI / 8) &&
 				first.hasAngleOrOpposite(Math.PI / 2, Math.PI / 8);
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is mainly to the left of its own base line.
 	 * @return	<code>true</code> if the line is mainly to the left of its own base line
 	 */
 	private boolean isLeftBalanced(){
 		return getHorizontalBalance() < 0;
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is mainly to the right of its own base line.
 	 * @return	<code>true</code> if the line is mainly to the right of its own base line
 	 */
 	private boolean isRightBalanced(){
 		return getHorizontalBalance() > 0;
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is mainly above its own base line.
 	 * @return	<code>true</code> if the line is mainly above its own base line
 	 */
 	private boolean isTopBalanced(){
 		return getVerticalBalance() > 0;
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is mainly below its own base line.
 	 * @return	<code>true</code> if the line is mainly below its own base line
 	 */
 	private boolean isBottomBalanced(){
 		return getVerticalBalance() < 0;
 	}
 
 	/**
 	 * TODO Write proper javadoc.
 	 * Returns a new edge between two new nodes positioned at <code>getFrom()</code> and <code>getTo()</code>.
 	 * @return
 	 */
 	public XYEdge<Object,Line> getBaseLine(){
 		XYEdge<Object,Line> base = new SimpleXYEdge<Object,Line>(new SimpleXYNode<Object,Line>(), new SimpleXYNode<Object,Line>());
 		base.getFrom().setPosition(getFrom().getPosition());
 		base.getTo().setPosition(getTo().getPosition());
 		return base;
 	}
 	
 	/**
 	 * TODO Check the result of this method.
 	 * TODO Write proper javadoc.
 	 * Returns -1 if negative balance, 1 if positive balance. 0 if equal.
 	 * @return	horizontal balance
 	 */
 	public double getHorizontalBalance(){
 		double balance = 0;
 		for(XYEdge<Object,Line> e : getGraph().getEdges()){
 			double base_x = getBaseLine().getEquation().getX(e.getTo().getPosition().getY());
 			if(!Double.isNaN(base_x)){
 				balance += e.getTo().getPosition().getX() - base_x;
 			}
 		}
 		return balance;
 	}
 	
 	/**
 	 * TODO Check the result of this method.
 	 * TODO Write javadoc.
 	 * @return
 	 */
 	public double getVerticalBalance(){
 		double balance = 0;
 		for(XYEdge<Object,Line> e : getGraph().getEdges()){
 			double base_y = getBaseLine().getEquation().getY(e.getTo().getPosition().getX());
 			if(!Double.isNaN(base_y)){
 				balance += e.getTo().getPosition().getY() - base_y;
 			}
 		}
 		return balance;
 	}
 	
 	/**
 	 * Returns <code>true</code> if this line is determined to be a shape similar to "Z" or "S".
	 * This method checks if the line crosses itself exactly once, and is equal to:<br />
	 * <code>countIntersections() == 1</code>
 	 * @return	<code>true</code> if this line is determined to be a shape similar to "Z" or "S"
 	 */
 	private boolean isZShape(){
		return countIntersections() == 1;
 	}
 	
 	/**
 	 * TODO Check the result of this method.
 	 * Returns how many times the a straight line between the first and last
 	 * nodes of this line crosses the line itself.
 	 * For a line in the shape of an "L", this would return zero.
 	 * For a line in the shape of the an "S" or a "Z", this would return 1.
 	 * This method checks for intersection only in the middle two fourths. See illustration.<br />
 	 * 
 	 * <code>|-----|xxxxx|xxxxx|-----|</code>
 	 * 
 	 * @return	the number of times a straight line between the first and last nodes of this line crosses the line itself
 	 */
 	public int countIntersections(){
 		int result = 0;
 		XYEdge<Object,Line> base = getBaseLine();
 		if(!isClosedPolygon() && getFrom() != null && getTo() != null){
 			Line middleSection = getFraction(2,3,4);
 			List<XYEdge<Object,Line>> middleEdges = middleSection.getGraph().getEdges();
			boolean touchesPrevious = false;
 			for(XYEdge<Object,Line> e : middleEdges){
				if(base.touches(e) && !touchesPrevious){
					touchesPrevious = true;
					result++;
				}else{
					touchesPrevious = false;
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * TODO Check the result of this method.
 	 * Checks if this line has an average angle of<code>angle</code> radians.
 	 * This angle may deviate +- <code>allowed_deviation</code> radians.
 	 * However: <code>return angle-allowed_deviation <= a && a < angle+allowed_deviation</code>.
 	 * Observe operands. Includes lowest but not highest, to prevent possible overlapping.
 	 * 
 	 * @param angle	angle in radians
 	 * @param allowed_deviation	allowed deviation in radians
 	 * @return	<code>true</code> if the angle of the line is in the specified range 
 	 */
 	public boolean hasAngle(double angle, double allowed_deviation){
 		double a = getAngle();
 		double lower_bound = angle - allowed_deviation;
 		double upper_bound = angle + allowed_deviation;
 		while(upper_bound < lower_bound) upper_bound += Math.PI * 2;
 		while(a < lower_bound) a += Math.PI * 2;
 		while(a > upper_bound) a -= Math.PI * 2;
 		return lower_bound <= a && a < upper_bound;
 	}
 
 	/**
 	 * TODO Check the result of this method
 	 * Returns <code>true</code> if this line has an average angle of <code>angle</code> radians,
 	 * or <code>angle + PI</code> (opposite direction) radians. The angle of the line may deviate
 	 * <code>allowed_deviation</code> radians in either direction.
 	 * @param angle	the angle to check
 	 * @param allowed_deviation	the allowed deviation in radians
 	 * @return	<code>true</code> if the line has an average angle of <code>angle</code>, or <code>angle + PI</code>, plus/minus <code>allowed_deviation</code> radians.
 	 */
 	public boolean hasAngleOrOpposite(double angle, double allowed_deviation){
 		return hasAngle(angle, allowed_deviation) || hasAngle(angle + Math.PI, allowed_deviation);
 	}
 	
 	/**
 	 * Returns the length of this line.
 	 * @return	the length of this line
 	 */
 	public double getLength() {
 		double length = 0;
 		for(XYEdge<Object,Line> e : getGraph().getEdges()){
 			length += e.getLength();
 		}
 		return length;
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is determined to be straight.
 	 * This is calculated through comparing the ratio of the distance between
 	 * the first and last node in the line, and the actual length of the line itself -
 	 * and comparing it to a threshold value.
 	 * The threshold value is defined in {@link Line#MINIMUM_STRAIGHTNESS_PERCENTAGE}.
 	 * @return	<code>true</code> if the line is straight
 	 */
 	private boolean isStraight() {
 		if(getGraph().getNodes().size() == 0) return false;
 		return (getFrom().getPosition().distanceTo(getTo().getPosition())) * 100 / getLength() > MINIMUM_STRAIGHTNESS_PERCENTAGE;
 	}
 	
 	/**
 	 * Returns <code>true</code> if the line is determined to be horizontal.
 	 * @return	<code>true</code> if the line is horizontal
 	 */
 	private boolean isHorizontal() {
 		return isStraight() && hasAngleOrOpposite(0, Math.PI / 8);
 	}
 
 	/**
 	 * Returns <code>true</code> if the line is determined to be vertical.
 	 * @return	<code>true</code> if the line is vertical
 	 */
 	private boolean isVertical() {
 		return isStraight() && hasAngleOrOpposite(Math.PI / 2, Math.PI / 8);
 	}
 
 	/**
 	 * Returns <code>true</code> if the line is determined to be diagonal, top-right to bottom-left.
 	 * @return	<code>true</code> if the line is diagonal, top-right to bottom-left
 	 * @see Line#isRightDiagonal()
 	 */
 	private boolean isLeftDiagonal() {
 		return isStraight() && hasAngleOrOpposite(Math.PI / 4, Math.PI / 8);
 	}
 
 	/**
 	 * Returns <code>true</code> if the line is determined to be diagonal, top-left to bottom-right.
 	 * @return	<code>true</code> if the line is diagonal, top-left to bottom-right
 	 * @see Line#isLeftDiagonal()
 	 */
 	private boolean isRightDiagonal() {
 		return isStraight() && hasAngleOrOpposite(3 * Math.PI / 4, Math.PI / 8);
 	}
 
 	/**
 	 * Returns the type of the line object as a string.
 	 */
 	public String toString(){
 		return getType().toString();
 	}
 
 	/**
 	 * Removes references to this line from both of its end nodes.
 	 * Use this method when discarding a line from the graph altogether.
 	 */
 	public void kill() {
 		Iterator<XYEdge<Object,Line>> it = getFrom().getEdges().iterator();
 		while(it.hasNext()){
 			XYEdge<Object,Line> e = it.next();
 			if(e.getPiggybackObject() == this){
 				it.remove();
 			}
 		}
 		it = getTo().getEdges().iterator();
 		while(it.hasNext()){
 			XYEdge<Object,Line> e = it.next();
 			if(e.getPiggybackObject() == this){
 				it.remove();
 			}
 		}
 	}
 	
 //	/**
 //	 * Returns a list of corners found in the line.
 //	 * @return	a list of corners
 //	 */
 //	public List<Corner> getCorners(){
 //		if(corners != null) return corners;
 //		corners = new ArrayList<Corner>();
 //		Line queue = new Line();
 //		Line cornerLine = null;
 ////		int queueSize = (getGraph().getEdges().size() * CORNER_SIZE_PERCENTAGE) / 100;	// set size of queue to CSP % of the line size
 //		int queueSize = CORNER_QUEUE_SIZE;	// set size of queue to CSP % of the line size
 //		for(XYEdge<Object,Line> e : getFraction(2,8,10).getGraph().getEdges()){	// loop through all edges in the line
 //			if(queue.getGraph().getEdges().size() == queueSize){	// if the queue is full
 //				Line head = queue.getFraction(1, CORNER_SEGMENTS);
 //				Line tail = queue.getFraction(CORNER_SEGMENTS, CORNER_SEGMENTS);
 //				double headAngle = head.getAngle();	// get the angle of the first queue fraction
 //				double tailAngle = tail.getAngle() + Math.PI;	// get the angle of the last queue fraction and reverse it
 //				while(tailAngle < headAngle) tailAngle += Math.PI * 2;	// turn tail angle so that it can be compared to head angle
 //				double angle = tailAngle - headAngle;	// get angle
 //				if(angle > Math.PI) angle = Math.PI * 2 - angle;	// convert to inner angle
 ////				Corner cc = new Corner(queue.getFrom().getPosition(),queue.getMiddleNode().getPosition(),queue.getTo().getPosition());
 ////				if(cc.getAngle() > Math.PI) cc.flip();
 ////				double angle = cc.getAngle();
 //				if(angle <= CORNER_MAX_ANGLE){	// if the angle is lower than than threshold (corner is sharp)
 //					if(cornerLine == null) cornerLine = new Line();	// if there is no corner line, create it
 //					cornerLine.addNode(queue.getMiddleNode());
 //				}else{	// if the angle is not greater than threshold
 //					if(cornerLine != null && cornerLine.getGraph().getNodes().size() > 2){	// if there is a corner line
 //						Coordinate left = cornerLine.getFrom().getPosition();
 //						Coordinate centre = cornerLine.getMiddleNode().getPosition();
 //						Coordinate right = cornerLine.getTo().getPosition();
 //						Corner c = new Corner(left,centre,right);
 //						if(c.getAngle() > Math.PI) c.flip();
 //						corners.add(c);	// make new corner from corner line (above line) and add it to the list of corners
 //						cornerLine = null;	// reset the corner line
 //					}
 //				}
 //			}
 //			queue.addEdge(e);	// add edge to the queue
 //			if(queue.getGraph().getNodes().size() == 0) queue.addNode(e.getFrom());
 //			queue.addNode(e.getTo());
 //			while(queue.getGraph().getEdges().size() > queueSize){
 //				queue.removeFirstEdge();	// remove the first edge in the queue if the queue overfull
 //			}
 //		}
 //		return corners;
 //	}
 	
 //	/**
 //	 * Returns the middlemost node of the line or </code>null</code> if there are no nodes in the line.
 //	 * @return	the middlemost node of the line or <code>null</code>
 //	 */
 //	public XYNode<Object,Line> getMiddleNode(){
 //		List<XYNode<Object,Line>> nodes = getGraph().getNodes();
 //		if(nodes.size() == 0) return null;
 //		return nodes.get(nodes.size() / 2);
 //	}
 //
 //	/**
 //	 * Returns the middlemost edge of the line or </code>null</code> if there are no edges in the line.
 //	 * @return	the middlemost edge of the line or <code>null</code>
 //	 */
 //	public XYEdge<Object,Line> getMiddleEdge(){
 //		List<XYEdge<Object,Line>> edges = getGraph().getEdges();
 //		if(edges.size() == 0) return null;
 //		return edges.get(edges.size() / 2);
 //	}
 //	
 //	/**
 //	 * Returns the first edge in the line. If the line contais no edges, <code>null</code> is returned.
 //	 * @return	the first edge in the line or <code>null</code>
 //	 */
 //	public XYEdge<Object,Line> getFirstEdge(){
 //		if(getGraph().getEdges().size() == 0) return null;
 //		return getGraph().getEdges().get(0);
 //
 //	}
 //	/**
 //	 * Returns the last edge in the line. If the line contais no edges, <code>null</code> is returned.
 //	 * @return	the last edge in the line or <code>null</code>
 //	 */
 //	public XYEdge<Object,Line> getLastEdge(){
 //		List<XYEdge<Object,Line>> edges = getGraph().getEdges();
 //		if(edges.size() == 0) return null;
 //		return edges.get(edges.size()-1);
 //	}
 //	
 //	/**
 //	 * Removes the first edge from the line, and its associated from-node.
 //	 * If there is only one edge in the graph, the edge along with both its nodes will be removed.
 //	 */
 //	public void removeFirstEdge(){
 //		List<XYEdge<Object,Line>> edges = getGraph().getEdges();
 //		List<XYNode<Object,Line>> nodes = getGraph().getNodes();
 //		if(edges.size() <= 1){
 //			edges.clear();
 //			nodes.clear();
 //		}else{
 //			XYEdge<Object,Line> e = edges.get(0);
 //			edges.remove(e);
 //			nodes.remove(e.getFrom());
 //		}
 //	}
 //
 //	/**
 //	 * Removes the last edge from the line, and its associated to-node.
 //	 * If there is only one edge in the graph, the edge along with both its nodes will be removed.
 //	 */
 //	public void removeLastEdge(){
 //		List<XYEdge<Object,Line>> edges = getGraph().getEdges();
 //		List<XYNode<Object,Line>> nodes = getGraph().getNodes();
 //		if(edges.size() <= 1){
 //			edges.clear();
 //			nodes.clear();
 //		}else{
 //			XYEdge<Object,Line> e = edges.get(edges.size()-1);
 //			edges.remove(e);
 //			nodes.remove(e.getTo());
 //		}
 //	}
 	
 }
