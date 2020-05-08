 package ch.bfh.bti7301.w2013.battleship.gui;
 
 import static ch.bfh.bti7301.w2013.battleship.gui.BoardView.SIZE;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javafx.event.EventHandler;
 import javafx.geometry.Point2D;
 import javafx.scene.control.Button;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.HBox;
 import ch.bfh.bti7301.w2013.battleship.game.Board.Coordinates;
 import ch.bfh.bti7301.w2013.battleship.game.Board.Direction;
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.GameRule;
 import ch.bfh.bti7301.w2013.battleship.game.Ship;
 
 public class ShipStack extends HBox {
 	private final GameRule rule;
 
 	private double initX;
 	private double initY;
 	private Point2D dragAnchor;
 
 	public ShipStack(final Game game, final GameRule rule, final BoardView pbv,
 			final Button ready) {
 		super(-16);
 		this.rule = rule;
 		// FIXME: this is just for layout debugging
 		setStyle("-fx-background-color: #ffc;");
 		setMaxHeight(SIZE);
 		for (Ship s : getAvailableShips()) {
 			final ShipView sv = new ShipView(s);
 			getChildren().add(sv);
 
 			sv.setOnMousePressed(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent me) {
 					initX = sv.getTranslateX();
 					initY = sv.getTranslateY();
 					dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
 				}
 			});
 			sv.setOnMouseDragged(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent me) {
 					double dragX = me.getSceneX() - dragAnchor.getX();
 					double dragY = me.getSceneY() - dragAnchor.getY();
 					// calculate new position of the circle
 					double newXPosition = initX + dragX;
 					double newYPosition = initY + dragY;
 					sv.setTranslateX(newXPosition);
 					sv.setTranslateY(newYPosition);
 				}
 			});
 			sv.setOnMouseReleased(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent me) {
 					double shipStartX = me.getSceneX() - me.getX()
 							- pbv.getLayoutX();
 					double shipStartY = me.getSceneY() - me.getY()
 							- pbv.getLayoutY();
 					if (pbv.contains(shipStartX, shipStartY)) {
 						// if on board, snap & add to it
 						Coordinates c = pbv.getCoordinates(shipStartX,
 								shipStartY);
 						Ship ship = buildShip(sv.getShipType(), c,
 								Direction.SOUTH);
 						try {
							game.getLocalPlayer().getBoard().placeShip(ship);
 						} catch (RuntimeException e) {
 							// snap back
 							// Maybe coloring the ship and leaving it there
 							// would be better?
 							sv.setTranslateX(initX);
 							sv.setTranslateY(initY);
 							return;
 						}
 						getChildren().remove(sv);
 						if (getChildren().isEmpty()) {
 							ready.setVisible(true);
 							setVisible(false);
 						}
 						pbv.addShip(ship);
 					} else {
 						// snap back
 						sv.setTranslateX(initX);
 						sv.setTranslateY(initY);
 					}
 				}
 			});
 		}
 	}
 
 	private List<Ship> getAvailableShips() {
 		List<Ship> availableShips = new LinkedList<>();
 		Coordinates dc = new Coordinates(0, 0);
 		Direction dd = Direction.SOUTH;
 
 		for (Entry<Class<? extends Ship>, Integer> e : rule.getShipList()
 				.entrySet()) {
 			Ship ship = buildShip(e.getKey(), dc, dd);
 			for (int i = 0; i < e.getValue(); i++)
 				availableShips.add(ship);
 		}
 		return availableShips;
 	}
 
 	private Ship buildShip(Class<? extends Ship> type, Coordinates c,
 			Direction d) {
 		try {
 			return type.getConstructor(Coordinates.class, Direction.class)
 					.newInstance(c, d);
 		} catch (Exception e) {
 			throw new RuntimeException(
 					"Error while creating ships through reflection", e);
 		}
 	}
 }
