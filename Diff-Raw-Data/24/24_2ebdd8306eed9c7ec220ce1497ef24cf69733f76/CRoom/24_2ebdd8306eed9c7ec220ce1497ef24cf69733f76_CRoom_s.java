 package org.percepta.mgrankvi.floorplanner.gwt.client.room;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.VisualItem;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.GeometryUtil;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Line;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.item.CLabel;
 import org.percepta.mgrankvi.floorplanner.gwt.client.item.door.CDoor;
 import org.percepta.mgrankvi.floorplanner.gwt.client.item.door.DoorState;
 import org.percepta.mgrankvi.floorplanner.gwt.client.paint.ItemUtils;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.dom.client.Document;
 import com.vaadin.client.VConsole;
 
 public class CRoom extends VisualItem {
 
 	private boolean selected = false;
 	private final List<VisualItem> roomItems = new LinkedList<VisualItem>();
 	private CLabel roomLabel;
 
 	public CRoom() {
 		// dummy element
 		setElement(Document.get().createDivElement());
 	}
 
 	public CRoom(final String id, final List<Point> points, final Point position) {
 		this.points.addAll(points);
 		this.position = position;
 		this.id = id;
 	}
 
 	@Override
 	public void setName(final String name) {
 		super.setName(name);
 		if (!name.isEmpty()) {
 			roomLabel = new CLabel(name, getCenter());
 		}
 	}
 
 	public void setPoints(final List<Point> points) {
 		this.points.addAll(points);
 	}
 
 	public void addRoomItem(final VisualItem item) {
 		roomItems.add(item);
 	}
 
 	public void removeRoomItem(final VisualItem item) {
 		roomItems.remove(item);
 	}
 
 	public void setId(final String id) {
 		this.id = id;
 	}
 
 	@Override
 	public void setPosition(final Point position) {
 		this.position = position;
 		VConsole.log("Start position: " + position.toString());
 	}
 
 	@Override
 	public void movePosition(final int x, final int y) {
 		super.movePosition(x, y);
 	}
 
 	public void scale(final double scale) {
 		for (final Point p : points) {
 			p.setX((int) Math.floor(p.getX() * scale));
 			p.setY((int) Math.floor(p.getY() * scale));
 		}
 		position.setX((int) Math.floor(position.getX() * scale));
 		position.setY((int) Math.floor(position.getY() * scale));
 
 		for (final VisualItem item : roomItems) {
 			final Point position = item.getPosition();
 			position.setX((int) Math.floor(position.getX() * scale));
 			position.setY((int) Math.floor(position.getY() * scale));
 			item.setPosition(position);
 			if (item instanceof CDoor) {
 				((CDoor) item).setSize((int) Math.floor(((CDoor) item).getSize() * scale));
 			}
 		}
 		if (roomLabel != null) {
			roomLabel.getPosition().setX((int) Math.floor(roomLabel.getPosition().getX() * scale));
			roomLabel.getPosition().setY((int) Math.floor(roomLabel.getPosition().getY() * scale));
 		}
		VConsole.log(position.toString());
 	}
 
 	public void setSelection(final boolean selected) {
 		this.selected = selected;
 	}
 
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void paintLabel(final Context2d context) {
 		if (roomLabel != null) {
 			roomLabel.paint(context, position);
 		}
 	}
 
 	@Override
 	public void paint(final Context2d context) {
 		paint(context, position);
 	}
 
 	@Override
 	public void paint(final Context2d context, final Point offset) {
 		if (selected) {
 			ItemUtils.paintPointToPoint(context, points, offset, CssColor.make("GREEN"));
 			ItemUtils.paintPointSelections(context, points, offset, CssColor.make("PURPLE"));
 			context.setStrokeStyle(CssColor.make("GREEN"));
 		} else {
 			ItemUtils.paintPointToPoint(context, points, offset, CssColor.make("BLACK"));
 		}
 		for (final VisualItem item : roomItems) {
 			item.paint(context, offset);
 		}
 	}
 
 	@Override
 	public boolean pointInObject(final int x, final int y) {
 		if (x < (position.getX() + minX()) || x > (position.getX() + maxX()) || y < (position.getY() + minY()) || y > (position.getY() + maxY())) {
 			return false;
 		}
 
 		final Point target = new Point(x, y);
 		// First point clearly outside shape.
 		final Line targetLine = new Line(new Point(position.getX() + minX() - 50, position.getY() + minY() - 50), target);
 		int intercepts = 0;
 		for (int i = 0; i + 2 <= points.size(); i++) {
 			final Line line = new Line(combine(position, points.get(i)), combine(position, points.get(i + 1)));
 			if ((line.start.getX() > x && line.end.getX() > x) || (line.start.getY() > y && line.end.getY() > y)) {
 				continue;
 			}
 			if (lineSegmentsIntersect(line, targetLine)) {
 				intercepts++;
 			}
 		}
 		final Line line = new Line(combine(position, points.getFirst()), combine(position, points.getLast()));
 		if (lineSegmentsIntersect(line, targetLine)) {
 			intercepts++;
 		}
 
 		return intercepts % 2 == 1;
 	}
 
 	public Point selectedPoint(final int x, final int y) {
 		for (final Point point : points) {
 			final int pointXMin = position.getX() + point.getX() - 4;
 			final int pointYMin = position.getY() + point.getY() - 4;
 			if (x > pointXMin && x < pointXMin + 8 && y > pointYMin && y < pointYMin + 8) {
 				return point;
 			}
 		}
 		return null;
 	}
 
 	private Point combine(final Point p1, final Point p2) {
 		return GeometryUtil.combine(p1, p2);
 	}
 
 	public void addDoor(final DoorState doorState) {
 		final CDoor door = new CDoor(doorState.getSize(), doorState.getOpeningDirection());
 		door.setPosition(doorState.getPosition());
 		addRoomItem(door);
 	}
 }
