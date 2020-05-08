 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatablePolygonShape;
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatableRectangle;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.swt.graphics.Color;
 
 public class RectangularHandle extends SelectionHandle {
 
 	/**
 	 * @param xAxis
 	 * @param yAxis
 	 * @param colour
 	 * @param parent
 	 * @param side
 	 * @param params (first corner's x and y, also centre of rotation)
 	 */
 	public RectangularHandle(ICoordinateSystem coords, Color colour, Figure parent, int side, double... params) {
 		super(coords, colour, parent, side, params);
 	}
 
 	@Override
 	public Shape createHandleShape(Figure parent, int side, double[] params) {
 		double angle;
 		if (parent instanceof RotatablePolygonShape) {
 			RotatablePolygonShape pg = (RotatablePolygonShape) parent;
 			angle = pg.getAngleDegrees();
 		} else {
 			angle = 0;
 		}
 		location = new PrecisionPoint(params[0], params[1]);
		return new RotatableRectangle(location.x(), location.y(), side, side, angle);
 	}
 }
