 package org.pathwayeditor.visualeditor.geometry;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
 import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
 import org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults;
 import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
 import org.pathwayeditor.figure.geometry.Dimension;
 import org.pathwayeditor.figure.geometry.Envelope;
 import org.pathwayeditor.figure.geometry.LineSegment;
 import org.pathwayeditor.figure.geometry.Point;
 
 public class LinkPointDefinition implements ILinkPointDefinition {
 	private final Logger logger = Logger.getLogger(this.getClass());
 	
	private static final double LINE_HIT_TOLERENCE = 5.0;
 	private static final int SRC_TERM_DIM = 2;
 	private static final int SRC_IDX = 0;
 	private static final double DEFAULT_LINE_WIDTH = 1.0;
 	private final List<Point> pointList;
 	private RGB lineColour = RGB.BLACK;
 	private LineStyle lineStyle = LineStyle.SOLID;
 	private double lineWidth = DEFAULT_LINE_WIDTH;
 	private final IGraphicalLinkTerminusDefinition srcTermDefn;
 	private final IGraphicalLinkTerminusDefinition tgtTermDefn;
 	private Point srcLineStartPosn;
 	private Point tgtLineEndPosn;
 	
 	public LinkPointDefinition(ILinkAttribute link){
 		IBendPointContainer bpContainer = link.getBendPointContainer();
 		this.pointList = new ArrayList<Point>(bpContainer.numBendPoints()+SRC_TERM_DIM);
 		this.pointList.add(link.getSourceTerminus().getLocation());
 		Iterator<Point> iter = bpContainer.bendPointIterator();
 		while(iter.hasNext()){
 			Point bp = iter.next();
 			this.pointList.add(bp);
 		}
 		this.pointList.add(link.getTargetTerminus().getLocation());
 		this.lineColour = link.getLineColour();
 		this.lineStyle = link.getLineStyle();
 		this.lineWidth = link.getLineWidth();
 		this.srcTermDefn = new GraphicalLinkTerminusDefinition(link.getSourceTerminus());
 		this.tgtTermDefn = new GraphicalLinkTerminusDefinition(link.getTargetTerminus());
 		this.srcLineStartPosn = calcFirstDrawnPoint(this.getSourceLineSegment(), this.srcTermDefn.getEndSize(), this.srcTermDefn.getGap());
 		this.tgtLineEndPosn = calcFirstDrawnPoint(this.getTargetLineSegment(), this.tgtTermDefn.getEndSize(), this.tgtTermDefn.getGap());
 	}
 	
 	public LinkPointDefinition(ILinkObjectType linkObjectType, Point srcPosn, Point tgtPosn){
 		ILinkAttributeDefaults link = linkObjectType.getDefaultAttributes();
 		this.pointList = new ArrayList<Point>(SRC_TERM_DIM);
 		this.pointList.add(srcPosn);
 		this.pointList.add(tgtPosn);
 		this.lineColour = link.getLineColour();
 		this.lineStyle = link.getLineStyle();
 		this.lineWidth = link.getLineWidth();
 		this.srcTermDefn = new GraphicalLinkTerminusDefinition(linkObjectType.getSourceTerminusDefinition());
 		this.tgtTermDefn = new GraphicalLinkTerminusDefinition(linkObjectType.getTargetTerminusDefinition());
 		this.srcLineStartPosn = calcFirstDrawnPoint(this.getSourceLineSegment(), this.srcTermDefn.getEndSize(), this.srcTermDefn.getGap());
 		this.tgtLineEndPosn = calcFirstDrawnPoint(this.getTargetLineSegment(), this.tgtTermDefn.getEndSize(), this.tgtTermDefn.getGap());
 	}
 	
 	public LinkPointDefinition(Point srcAnchor, Point tgtAnchor) {
 		this.pointList = new ArrayList<Point>(SRC_TERM_DIM);
 		this.srcTermDefn = new GraphicalLinkTerminusDefinition();
 		this.tgtTermDefn = new GraphicalLinkTerminusDefinition();
 		this.pointList.add(srcAnchor);
 		this.pointList.add(tgtAnchor);
 		this.srcLineStartPosn = calcFirstDrawnPoint(this.getSourceLineSegment(), this.srcTermDefn.getEndSize(), this.srcTermDefn.getGap());
 		this.tgtLineEndPosn = calcFirstDrawnPoint(this.getTargetLineSegment(), this.tgtTermDefn.getEndSize(), this.tgtTermDefn.getGap());
 	}
 	
 	public LinkPointDefinition(LinkPointDefinition other, Point translation){
 		this.pointList = new ArrayList<Point>(SRC_TERM_DIM);
 		for(Point originalPoint : other.pointList){
 			pointList.add(originalPoint.translate(translation));
 		}
 		lineColour = other.lineColour;
 		lineStyle = other.lineStyle;
 		lineWidth = other.lineWidth;
 		srcTermDefn = new GraphicalLinkTerminusDefinition(other.srcTermDefn);
 		tgtTermDefn = new GraphicalLinkTerminusDefinition(other.tgtTermDefn);
 		this.srcLineStartPosn = other.srcLineStartPosn;
 		this.tgtLineEndPosn = other.tgtLineEndPosn;
 	}
 
 	public LinkPointDefinition(LinkPointDefinition other){
 		this.pointList = new ArrayList<Point>(other.pointList);
 		lineColour = other.lineColour;
 		lineStyle = other.lineStyle;
 		lineWidth = other.lineWidth;
 		srcTermDefn = new GraphicalLinkTerminusDefinition(other.srcTermDefn);
 		tgtTermDefn = new GraphicalLinkTerminusDefinition(other.tgtTermDefn);
 		this.srcLineStartPosn = other.srcLineStartPosn;
 		this.tgtLineEndPosn = other.tgtLineEndPosn;
 	}
 
 	@Override
 	public Envelope getBounds(){
 		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
 		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
 		final double halfLineHeight = this.lineWidth + LINE_HIT_TOLERENCE;
 		Iterator<Point> pointIter = this.pointList.iterator();
 		while(pointIter.hasNext()){
 			Point p = pointIter.next();
			minX = Math.min(minX, p.getX()-halfLineHeight);
			maxX = Math.max(maxX, p.getX()+halfLineHeight);
			minY = Math.min(minY, p.getY()-halfLineHeight);
			maxY = Math.max(maxY, p.getY()+halfLineHeight);
 		}
		return new Envelope(minX, minY, maxX-minX, maxY-minY);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setSrcAnchorPosition(org.pathwayeditor.figure.geometry.Point)
 	 */
 	@Override
 	public void setSrcAnchorPosition(Point newPosn){
 		this.pointList.set(SRC_IDX, newPosn);
 		updateLineStart();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setTgtAnchorPosition(org.pathwayeditor.figure.geometry.Point)
 	 */
 	@Override
 	public void setTgtAnchorPosition(Point newPosn){
 		this.pointList.set(this.pointList.size()-1, newPosn);
 		updateLineStart();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#setBendPointPosition(int, org.pathwayeditor.figure.geometry.Point)
 	 */
 	@Override
 	public void setBendPointPosition(int bpIdx, Point newPosn){
 		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
 		
 		// adjust for fact the src anchor is 1st element
 		this.pointList.set(bpIdx+1, newPosn);
 		updateLineStart();
 	}
 	
 	private void updateLineStart(){
 		this.srcLineStartPosn = calcFirstDrawnPoint(this.getSourceLineSegment(), this.srcTermDefn.getEndSize(), this.srcTermDefn.getGap());
 		this.tgtLineEndPosn = calcFirstDrawnPoint(this.getTargetLineSegment(), this.tgtTermDefn.getEndSize(), this.tgtTermDefn.getGap());
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getSrcEndPoint()
 	 */
 	@Override
 	public Point getSrcEndPoint(){
 		return this.pointList.get(SRC_IDX);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getTgtEndPoint()
 	 */
 	@Override
 	public Point getTgtEndPoint(){
 		return this.pointList.get(this.pointList.size()-1);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getBendPointPosition(int)
 	 */
 	@Override
 	public Point getBendPointPosition(int bpIdx){
 		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
 		
 		// adjust for fact the src anchor is 1st element
 		return this.pointList.get(bpIdx+1);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getSourceLineSegment()
 	 */
 	@Override
 	public LineSegment getSourceLineSegment(){
 		return new LineSegment(this.pointList.get(SRC_IDX), this.pointList.get(SRC_IDX+1));
 	}
 
 	private static Point calcFirstDrawnPoint(LineSegment original, Dimension decoratorSize, double gapLen){
 		double totalOffset = gapLen + decoratorSize.getWidth();
 		double length = original.getLength();
 		double x = (original.getXDisplacement() * totalOffset)/length;
 		double y = (original.getYDisplacement() * totalOffset)/length;
 		return original.getOrigin().translate(x, y);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getTargetLineSegment()
 	 */
 	@Override
 	public LineSegment getTargetLineSegment(){
 		int lastIdx = this.pointList.size()-1;
 		return new LineSegment(this.pointList.get(lastIdx), this.pointList.get(lastIdx-1));
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#pointIterator()
 	 */
 	@Override
 	public Iterator<Point> pointIterator(){
 		return this.pointList.iterator();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#lineSegIterator()
 	 */
 	@Override
 	public Iterator<LineSegment> lineSegIterator(){
 		List<LineSegment> retVal = new LinkedList<LineSegment>();
 		Point firstP = this.getSrcEndPoint();
 		Point lastP = null;
 		for(int i = 1; i < this.pointList.size(); i++){
 			lastP = this.pointList.get(i);
 			retVal.add(new LineSegment(firstP, lastP));
 			firstP = lastP;
 		} 
 		return retVal.iterator();
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#drawnLineSegIterator()
 	 */
 	@Override
 	public Iterator<LineSegment> drawnLineSegIterator(){
 		List<LineSegment> retVal = new LinkedList<LineSegment>();
 		Point firstP = this.srcLineStartPosn;
 		Point lastP = null;
 		for(int i = 1; i < this.pointList.size() -1; i++){
 			lastP = this.pointList.get(i);
 			retVal.add(new LineSegment(firstP, lastP));
 			firstP = lastP;
 		}
 		lastP = this.tgtLineEndPosn;
 		retVal.add(new LineSegment(firstP, lastP));
 		return retVal.iterator();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#addNewBendPoint(int, org.pathwayeditor.figure.geometry.Point)
 	 */
 	@Override
 	public void addNewBendPoint(int bpIdx, Point bpPosn) {
 		if(bpIdx > this.numBendPoints()) throw new IllegalArgumentException("Bendpoint index is outseide permitted range: " + bpIdx);
 		this.pointList.add(bpIdx+1, bpPosn);
 		updateLineStart();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#addNewBendPoint(org.pathwayeditor.figure.geometry.Point)
 	 */
 	@Override
 	public void addNewBendPoint(Point bpPosn) {
 		this.pointList.add(this.numBendPoints()+1, bpPosn);
 		updateLineStart();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#numPoints()
 	 */
 	@Override
 	public int numPoints(){
 		return this.pointList.size();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#numBendPoints()
 	 */
 	@Override
 	public int numBendPoints(){
 		return this.pointList.size()-SRC_TERM_DIM;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#removeBendPoint(int)
 	 */
 	@Override
 	public void removeBendPoint(int bpIdx) {
 		if(bpIdx >= this.numBendPoints()) throw new IllegalArgumentException("No bendpoint exists with this index: " + bpIdx);
 		
 		this.pointList.remove(bpIdx+1);
 		updateLineStart();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.pathwayeditor.graphicsengine.ILinkPointDefinition#getLinkDirection()
 	 */
 	@Override
 	public LineSegment getLinkDirection() {
 		return new LineSegment(this.getSrcEndPoint(), this.getTgtEndPoint());
 	}
 
 	@Override
 	public boolean containsPoint(Point p){//, double lineWidthTolerence) {
 		boolean retVal = false;
 //		final double halfLineHeight = this.lineWidth + LINE_HIT_TOLERENCE;
 		Iterator<LineSegment> lineSegIter = this.lineSegIterator();
 		while(lineSegIter.hasNext() && !retVal){
 			LineSegment seg = lineSegIter.next();
			retVal = seg.intersectsWithCircle(p, LINE_HIT_TOLERENCE);
 			if(logger.isTraceEnabled() && retVal){
 				logger.trace("Segment contains point: p=" + p + ", seg" + seg);
 			}
 		}
 		return retVal;
 	}
 
 	@Override
 	public RGB getLineColour() {
 		return this.lineColour ;
 	}
 
 	@Override
 	public LineStyle getLineStyle() {
 		return this.lineStyle ;
 	}
 
 	@Override
 	public double getLineWidth() {
 		return this.lineWidth ;
 	}
 
 	@Override
 	public IGraphicalLinkTerminusDefinition getSourceTerminusDefinition() {
 		return this.srcTermDefn ;
 	}
 
 	@Override
 	public IGraphicalLinkTerminusDefinition getTargetTerminusDefinition() {
 		return this.tgtTermDefn;
 	}
 
 	@Override
 	public void translate(Point translation) {
 		for(int i = 0; i < this.pointList.size(); i++){
 			Point translatedPoint = this.pointList.get(i).translate(translation);
 			this.pointList.set(i, translatedPoint);
 		}
 		updateLineStart();
 	}
 
 	@Override
 	public void setLineColour(RGB lineColour) {
 		this.lineColour = lineColour;
 	}
 
 	@Override
 	public void setLineStyle(LineStyle lineStyle) {
 		this.lineStyle = lineStyle;
 	}
 
 	@Override
 	public void setLineWidth(double lineWidth) {
 		this.lineWidth = lineWidth;
 	}
 
 	@Override
 	public ILinkPointDefinition getCopy() {
 		return new LinkPointDefinition(this);
 	}
 }
