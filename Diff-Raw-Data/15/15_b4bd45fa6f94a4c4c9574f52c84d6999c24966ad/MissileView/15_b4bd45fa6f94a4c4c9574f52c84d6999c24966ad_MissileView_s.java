 package ch.bfh.bti7301.w2013.battleship.gui;
 
 import javafx.scene.Parent;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Ellipse;
 import javafx.scene.shape.Shape;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 
 public class MissileView extends Parent {
 	private Shape shape;
 
 	public MissileView(Missile missile) {
 		shape = new Ellipse(BoardView.SIZE / 2, BoardView.SIZE / 2);
 		getChildren().add(shape);
 	}
 
 	public void update(Missile missile) {
 		switch (missile.getMissileState()) {
 		case HIT:
 			shape.setFill(Color.RED);
 			break;
 		case SUNK:
 		case GAME_WON:
 			shape.setFill(Color.DARKRED);
 			break;
 		case MISS:
 			shape.setFill(Color.BLUE);
 			break;
 		case FIRED:
 			shape.setFill(new Color(0, 0, 1, 0.1));
 			break;
 		}
 	}
 }
