 /**
  * 
  */
 package se.iroiro.md.graph.simple;
 
 import se.iroiro.md.graph.Coordinate;
 import se.iroiro.md.graph.LineEquation;
 import se.iroiro.md.graph.XYEdge;
 import se.iroiro.md.graph.XYNode;
 
 /**
  * @author j
  *
  * @param <NP>	the type of the node piggyback object
  * @param <EP>	the type of the edge piggyback object
  */
 public class SimpleXYEdge<NP,EP> extends AbstractXYGraphable<EP> implements XYEdge<NP,EP> {
 
 	private XYNode<NP,EP> from = null;
 	private XYNode<NP,EP> to = null;
 
 	private Coordinate topleft = null;
 	private Coordinate bottomright = null;
 	private LineEquation equation = null;
 	
 	private int headPort = -1;
 	private int tailPort = -1;
 	
 	/**
 	 * Class constructor. Empty.
 	 */
 	public SimpleXYEdge(){}
 	
 	/**
 	 * Class constructor with node arguments
 	 * @param from	from-node
 	 * @param to	to-node
 	 */
 	public SimpleXYEdge(XYNode<NP,EP> from, XYNode<NP,EP> to){
 		setFrom(from);
 		setTo(to);
 	}
 	 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getFrom()
 	 */
 	public XYNode<NP, EP> getFrom() {
 		return from;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getOpposite(se.iroiro.md.graph.XYNode)
 	 */
 	public XYNode<NP, EP> getOpposite(XYNode<NP, EP> opposite) {
 		return opposite == getFrom() ? getTo() : (opposite == getTo() ? getFrom() : null);
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getTo()
 	 */
 	public XYNode<NP, EP> getTo() {
 		return to;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#reverse()
 	 */
 	public void reverse() {
 		XYNode<NP,EP> tmp = from;
 		from = to;
 		to = tmp;
 		reset();
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#setFrom(se.iroiro.md.graph.XYNode)
 	 */
 	public void setFrom(XYNode<NP, EP> from) {
 		if(getFrom() != null && getFrom() != getTo()) getFrom().removeEdge(this);
 		this.from = from;
 		if(from != null) from.addEdge(this);
 		reset();
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#setTo(se.iroiro.md.graph.XYNode)
 	 */
 	public void setTo(XYNode<NP, EP> to) {
 		if(getTo() != null && getTo() != getFrom()) getTo().removeEdge(this);
 		this.to = to;
 		if(to != null) to.addEdge(this);
 		reset();
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#getAngle()
 	 */
 	public double getAngle() {
 		if(getFrom() != null && getTo() != null){
 			return getFrom().getAngleTo(getTo());
 		}else{
 			return Double.NaN;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#getPosition()
 	 */
 	public Coordinate getPosition() {
 		if(getFrom() != null && getTo() != null){
 			Coordinate tl = getTopLeft();
 			Coordinate br = getBottomRight();
 			return new SimpleCoordinate(tl.getX() + (br.getX()-tl.getX())/2,	// Get centre point of bounding rectangle
 										br.getY() + (tl.getY()-br.getY())/2);	// "
 		}else{
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#setPosition(se.iroiro.md.graph.Coordinate)
 	 */
 	public void setPosition(Coordinate position) {
 		throw new UnsupportedOperationException("This class has no position field. Use setPosition(double x, double y) instead.");
 	}
 	
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#setPosition(double, double)
 	 */
 	public void setPosition(double x, double y){
 		if(getFrom() != null && getTo() != null){
 			double dx = x - getPosition().getX();
 			double dy = y - getPosition().getY();
 			getFrom().getPosition().translate(dx, dy);
 			getTo().getPosition().translate(dx, dy);
 			reset();
 		}
 	}
 
 	/**
 	 * Resets cached fields.
 	 */
 	public void reset() {
 		topleft = null;
 		bottomright = null;
 		equation = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getTopLeft()
 	 */
 	public Coordinate getTopLeft() {
 		if(topleft == null){
 			topleft = new SimpleCoordinate(Math.min(getFrom().getPosition().getX(), getTo().getPosition().getX()),
 				Math.max(getFrom().getPosition().getY(), getTo().getPosition().getY()));
 		}
 		return topleft;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getBottomRight()
 	 */
 	public Coordinate getBottomRight() {
 		if(bottomright == null){
 			bottomright = new SimpleCoordinate(Math.max(getFrom().getPosition().getX(), getTo().getPosition().getX()),
 					Math.min(getFrom().getPosition().getY(), getTo().getPosition().getY()));
 		}
 		return bottomright;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#withinBounds(se.iroiro.md.graph.Coordinate)
 	 */
 	public boolean withinBounds(Coordinate c){
 		double x = c.getX();
 		double y = c.getY();
 		return (x >= getTopLeft().getX() && x <= getBottomRight().getX())
 		&& (y <= getTopLeft().getY() && y >= getBottomRight().getY());
 	}
 //	/* (non-Javadoc)
 //	 * @see se.iroiro.md.graph.XYEdge#withinBounds(se.iroiro.md.graph.Coordinate)
 //	 */
 //	public boolean withinBounds(Coordinate c){
 //		double x = c.getX();
 //		double y = c.getY();
 //		return (x >= getTopLeft().getX() && x <= getBottomRight().getX())
 //		&& (y <= getTopLeft().getY() && y >= getBottomRight().getY());
 //	}
 	
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getEquation()
 	 */
 	public LineEquation getEquation() {
 		if(equation == null){
 			if(getFrom() != null && getTo() != null){
 				equation = new SimpleLineEquation(getFrom().getPosition().getX(),
 											getFrom().getPosition().getY(),
 											getTo().getPosition().getX(),
 											getTo().getPosition().getY());
 			}
 		}
 		return equation;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#touches(se.iroiro.md.graph.XYEdge)
 	 */
 	public boolean touches(XYEdge<NP,EP> edge) {
		if(getEquation().sameAs(edge.getEquation())){
			return withinBounds(edge.getFrom().getPosition()) || withinBounds(edge.getTo().getPosition()) ||
			edge.withinBounds(getFrom().getPosition()) || edge.withinBounds(getTo().getPosition());
 		}else{
 			Coordinate is = getEquation().getIntersection(edge.getEquation());
 			return withinBounds(is) && edge.withinBounds(is);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYPositioned#rotate(double, se.iroiro.md.graph.Coordinate)
 	 */
 	public void rotate(double angle, Coordinate pivot) {
 		if(getFrom() != null && getTo() != null){
 			getFrom().getPosition().rotate(angle, pivot);
 			getTo().getPosition().rotate(angle, pivot);
 			reset();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString(){
 		return getFrom().toString() + " -> " + getTo().toString();	
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getLength()
 	 */
 	public double getLength() {
 		if(getFrom() != null && getTo() != null){
 			return getFrom().getPosition().distanceTo(getTo().getPosition());
 		}else{
 			return 0;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#unlink()
 	 */
 	public void unlink() {
 		setFrom(null);
 		setTo(null);
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#links(se.iroiro.md.graph.XYNode, se.iroiro.md.graph.XYNode)
 	 */
 	public boolean links(XYNode<NP, EP> n1, XYNode<NP, EP> n2) {
 		if(n1 != null && n2 != null){
 			return (getFrom() == n1 && getTo() == n2) ||
 				(getTo() == n1 && getFrom() == n2);
 		}else{
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getHeadPort()
 	 */
 	public int getHeadPort() {
 		return headPort;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#getTailPort()
 	 */
 	public int getTailPort() {
 		return tailPort;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#setHeadPort(int)
 	 */
 	public void setHeadPort(int hp) {
 		headPort = hp;
 	}
 
 	/* (non-Javadoc)
 	 * @see se.iroiro.md.graph.XYEdge#setTailPort(int)
 	 */
 	public void setTailPort(int tp) {
 		tailPort = tp;
 	}
 }
