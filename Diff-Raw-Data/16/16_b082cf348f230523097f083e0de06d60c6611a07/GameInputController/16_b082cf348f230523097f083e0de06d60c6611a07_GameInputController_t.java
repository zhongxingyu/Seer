 package nl.sest.gamejam.controller;
 
 import nl.sest.gamejam.model.impl.Blockade;
 import nl.sest.gamejam.model.impl.Model;
 import nl.sest.gamejam.model.player.PlayerAttractor;
 import nl.sest.gamejam.model.player.PlayerRepulsor;
 import org.newdawn.slick.geom.Vector2f;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Controller which handles user input.
  *
  * @author Tim
  * @since 1/26/13 8:19 AM
  */
 public class GameInputController {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(GameInputController.class);
 
 	private Model model;
 
 	private Vector2f dragStart;
 
 	public GameInputController(Model model) {
 		this.model = model;
 		dragStart = null;
 	}
 
 	public void handleLeftClick(float x, float y) {
 		LOGGER.debug("Left click at: [{}, {}]", x, y);
 
 		Vector2f converted = Utils.screenToWorld(model, x, y);
 		PlayerAttractor playerAttractor = new PlayerAttractor(converted.x, converted.y);
 		model.addPlayerAttractor(playerAttractor);
 	}
 
 	public void handleRightClick(float x, float y) {
 		LOGGER.debug("Right click at [{}, {}]", x, y);
 
 		Vector2f converted = Utils.screenToWorld(model, x, y);
 		PlayerRepulsor playerRepulsor = new PlayerRepulsor(converted.x, converted.y);
 		model.addPlayerRepulsor(playerRepulsor);
 	}
 
 	public void handleMouseDrag(float startX, float startY, float endX, float endY) {
 		LOGGER.debug("Mouse dragged");
 		if (dragStart == null) {
 			dragStart = new Vector2f(startX, startY);
 		}
 
 	}
 
 	public void handleMouseUp(float x, float y) {
 		LOGGER.debug("Mouse Released");
 		if (dragStart != null) {
 			handleDragAndDrop(dragStart.x, dragStart.y, x, y);
 			dragStart = null;
 		}
 	}
 
 	public void handleDragAndDrop(float startX, float startY, float endX, float endY) {
 
 		float[] logvalues = {startX, startY, endX, endY};
 		LOGGER.debug("{}", logvalues);
 		Vector2f convertedStart = Utils.screenToWorld(model, startX, startY);
 		Vector2f convertedEnd = Utils.screenToWorld(model, endX, endY);
 
 		Vector2f addingVector = convertedEnd.copy().sub(convertedStart).normalise().scale(2 * Blockade.RADIUS);
 		Vector2f nextVectorPoint = convertedStart.copy();
 
 		Blockade previous = null;
 		int i = 0;
 		while (convertedEnd.copy().sub(convertedStart).lengthSquared() > nextVectorPoint.copy().sub(convertedStart).lengthSquared()
 				&& nextVectorPoint.copy().sub(convertedStart).lengthSquared() < 576) {
 
			Blockade blockade = new Blockade(nextVectorPoint.x, nextVectorPoint.y, i != 0, previous);
 			model.addBlockade(blockade);
 			previous = blockade;
 			nextVectorPoint.add(addingVector);
 			i++;
 		}
		model.addBlockade(new Blockade(nextVectorPoint.x, nextVectorPoint.y, false, previous));
 	}
 }
