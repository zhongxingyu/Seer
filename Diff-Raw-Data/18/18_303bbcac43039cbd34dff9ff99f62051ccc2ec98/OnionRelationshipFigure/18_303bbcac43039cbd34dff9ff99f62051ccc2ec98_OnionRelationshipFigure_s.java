 package edu.ysu.onionuml.ui.graphics.figures;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.Polyline;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 
 import edu.ysu.onionuml.core.RelationshipType;
 
 /**
  * Figure that represents a UML relationship as an image.
  */
 public class OnionRelationshipFigure extends Figure {
 
 	private static final Rectangle CANVAS = new Rectangle(0, 0, 48, 48);
 	private static final int BORDER_THICKNESS = 1;
	private static final float[] LINE_DASH = new float[]{8.0f, 8.0f};
 	
 	private Color mFillColor;
 	private PointList mPolygon;
 	private Polyline mLine;
 	private PointList mLinePoints;
 	private RelationshipType mRelationshipType;
 	
 	/**
 	 * Constructs a new relationship figure representing the specified relationship.
 	 * If relationship is null, renders as blank rectangle.
 	 */
 	public OnionRelationshipFigure(RelationshipType relationshipType){
 		
 		mRelationshipType = relationshipType;
 		setBorder(new LineBorder(ColorConstants.black, BORDER_THICKNESS));
 		setBounds(CANVAS);
 		
 		if(relationshipType != null){
 			makeShapes(relationshipType);
 		}
 	}
 	
 	@Override
 	public void primTranslate(int dx, int dy) {
 		super.primTranslate(dx, dy);
 		
		if(mRelationshipType != null && mPolygon != null){
 			mPolygon.translate(dx, dy);
 			mLinePoints.translate(dx, dy);
 			mLine.setPoints(mLinePoints);
 		}
 	}
 	
 	@Override
 	public void paint(Graphics graphics){
 		super.paint(graphics);
 		if(mRelationshipType == null){
 			return;
 		}
 		
 		mLine.paint(graphics);
 		
		graphics.setBackgroundColor(mFillColor);
		graphics.drawPolygon(mPolygon);
		graphics.fillPolygon(mPolygon);
 	}
 	
 	
 	/*
 	 * Constructs a polygon and line that represent the given relationship.
 	 */
 	private void makeShapes(RelationshipType relationshipType){
 		
 		List<Point2D.Float> points = new ArrayList<Point2D.Float>();
 		mFillColor = ColorConstants.black;
 		boolean isLineDashed = false;
 		
 		// construct points as percentages inside canvas
 		switch(relationshipType){
 			case AGGREGATION:
 				mFillColor = ColorConstants.white;
 			case COMPOSITION:
 				points.add(new Point2D.Float(0.5f, 0.05f));
 				points.add(new Point2D.Float(0.3f, 0.3f));
 				points.add(new Point2D.Float(0.5f, 0.55f));
 				points.add(new Point2D.Float(0.7f, 0.3f));
 				break;
 			case DEPENDENCY:
 				isLineDashed = true;
 			case DIRECTEDASSOCIATION:
 				points.add(new Point2D.Float(0.5f, 0.05f));
 				points.add(new Point2D.Float(0.2f, 0.3f));
 				points.add(new Point2D.Float(0.5f, 0.05f));
 				points.add(new Point2D.Float(0.8f, 0.3f));
 				break;
 			case REALIZATION:
 				isLineDashed = true;
 			case GENERALIZATION:
 				mFillColor = ColorConstants.white;
 				points.add(new Point2D.Float(0.5f, 0.05f));
 				points.add(new Point2D.Float(0.2f, 0.45f));
 				points.add(new Point2D.Float(0.8f, 0.45f));
 				break;
 			case ASSOCIATION:
 				// no head
 				break;
 			default:
 				throw new RuntimeException("Relationship type not supported.");
 		}
 		
 		// scale polygon to fit in the canvas
 		if(points.size() > 0){
 			mPolygon = new PointList();
 			for(int i=0; i < points.size(); ++i){
 				Point2D.Float p = points.get(i);
 				mPolygon.addPoint(new Point((int) (p.x * CANVAS.width()),
 						(int) (p.y * CANVAS.height())));
 			}
 		}
 		else{
 			mPolygon = null;
 		}
 		mLinePoints = new PointList();
 		mLinePoints.addPoint((int) (0.5f * CANVAS.width()), (int) (0.05f * CANVAS.height()));
 		mLinePoints.addPoint((int) (0.5f * CANVAS.width()), (int) (0.95f * CANVAS.height()));
 		mLine = new Polyline();
 		mLine.setPoints(mLinePoints);
 		if(isLineDashed){
 			mLine.setLineDash(LINE_DASH);
 			mLine.setLineStyle(SWT.LINE_DASH);
 		}
 	}
 }
