 package org.percepta.mgrankvi.floorplanner.gwt.client;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.GeometryUtil;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Rectangle;
 import org.percepta.mgrankvi.floorplanner.gwt.client.room.CRoom;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 
 public class InfoButton extends VisualItem {
 
 	private static final int SIDE_LENGTH = 15;
 	private static final int LINE_LENGTH = SIDE_LENGTH - 6;
 	private static final int OFFSET_X = SIDE_LENGTH * 2;
 	private static final int OFFSET_Y = 10;
 
 	private final Rectangle rect = new Rectangle(SIDE_LENGTH, SIDE_LENGTH);
 	private Point offset = new Point(0, 0);
 	private final CRoom room;
 
 	public InfoButton(final CRoom room) {
 		this.room = room;
 		offset = new Point(GeometryUtil.maxX(room.getPoints()) + room.getPositionX() - OFFSET_X, GeometryUtil.minY(room.getPoints()) + room.getPositionY()
 				+ OFFSET_Y);
 	}
 
 	public CRoom getRoom() {
 		return room;
 	}
 
 	@Override
 	public void paint(final Context2d context) {
 		offset = new Point(GeometryUtil.maxX(room.getPoints()) + room.getPositionX() - OFFSET_X, GeometryUtil.minY(room.getPoints()) + room.getPositionY()
 				+ OFFSET_Y);
		while (!room.pointInObject(offset.getX(), offset.getY())) {
			offset.move(-SIDE_LENGTH, 0);
		}
 
 		context.setStrokeStyle("gray");
 		context.beginPath();
 
 		context.strokeRect(offset.getX(), offset.getY(), rect.getWidth(), rect.getHeight());
 		final int x = offset.getX() + 3;
 		int y = offset.getY() + 3;
 		while (y < offset.getY() + SIDE_LENGTH) {
 			context.moveTo(x, y);
 			context.lineTo(x + LINE_LENGTH, y);
 			y += 3;
 		}
 		context.closePath();
 		context.stroke();
 	}
 
 	@Override
 	public boolean pointInObject(final int x, final int y) {
 		return x > offset.getX() && x < (offset.getX() + rect.getWidth()) && y > rect.getY() && y < (offset.getY() + rect.getHeight());
 	}
 
 	@Override
 	public void paint(final Context2d context, final Point offset) {
 		paint(context);
 	}
 
 }
