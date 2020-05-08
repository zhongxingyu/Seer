 package raisa.ui.tool;
 
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D.Float;
 
 import raisa.ui.VisualizerFrame;
 import raisa.util.Vector2D;
 
 public class DrawTool extends BasicTool {
 	public DrawTool(VisualizerFrame frame) {
 		super(frame);
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent mouseEvent, Float mouseFrom, Float mouseTo) {
 		if ((mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
 			if (mouseEvent.isControlDown()) {
 			} else {
 				getVisualizerFrame().pushUserEditUndoLevel();
 				drawLine(mouseEvent, mouseFrom, mouseTo);
 			}
 		} else {
 			super.mouseDragged(mouseEvent, mouseFrom, mouseTo);
 		}
 	}
 
 	private void drawLine(MouseEvent mouseEvent, Float mouseFrom, Float mouseTo) {
 		int length = (int) mouseTo.distance(mouseFrom);
 		float dx = (mouseTo.x - mouseFrom.x) / length;
 		float dy = (mouseTo.y - mouseFrom.y) / length;
 		Float position = new Float();
 		for (int i = 0; i < length; ++i) {
 			position.x = mouseFrom.x + i * dx;
 			position.y = mouseFrom.y + i * dy;
 			drawPoint(position, !mouseEvent.isShiftDown());
 		}		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent mouseEvent, Float mouse) {
 		if ((mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent mouseEvent, Float mouseFrom, Float mouseTo) {
 		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
 			if (mouseEvent.isControlDown()) {
 				getVisualizerFrame().pushUserEditUndoLevel();
 				drawLine(mouseEvent, mouseFrom, mouseTo);
 			} else {
 				getVisualizerFrame().pushUserEditUndoLevel();
 				drawPoint(mouseTo, !mouseEvent.isShiftDown());
 			}
 		}
 	}
 
 	private void drawPoint(Float mouse, boolean isBlocked) {
 		Vector2D worldPosition = getVisualizerFrame().toWorld(new Vector2D(mouse));
 		getVisualizerFrame().setUserPosition(worldPosition, isBlocked);
 	}
 }
