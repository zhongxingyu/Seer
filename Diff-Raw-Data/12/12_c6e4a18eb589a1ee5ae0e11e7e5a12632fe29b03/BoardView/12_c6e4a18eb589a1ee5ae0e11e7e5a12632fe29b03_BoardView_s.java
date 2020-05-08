 package ch.bfh.bti7301.w2013.battleship.gui;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javafx.application.Platform;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Parent;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.shape.Shape;
 import ch.bfh.bti7301.w2013.battleship.game.Board;
 import ch.bfh.bti7301.w2013.battleship.game.BoardListener;
 import ch.bfh.bti7301.w2013.battleship.game.Coordinates;
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.Ship;
 import ch.bfh.bti7301.w2013.battleship.game.Missile.MissileState;
 
 public class BoardView extends Parent {
 	public static final int SIZE = 40;
 
 	private Map<Coordinates, MissileView> missileViews = new HashMap<>();
 	private List<ShipView> shipViews = new LinkedList<>();
 	private Group ships = new Group();
 	private Group missiles = new Group();
 
 	private boolean blocked = false;
 
 	public BoardView(Game game, BoardType type) {
 		final Board board = type.getBoard(game);
 
 		final int rows, columns;
 		rows = columns = board.getBoardSize();
 
 		getChildren().add(getWater(rows, columns));
 		for (int i = 0; i <= rows; i++) {
 			getChildren().add(getLine(i * SIZE, 0, i * SIZE, SIZE * columns));
 		}
 		for (int i = 0; i <= columns; i++) {
 			getChildren().add(getLine(0, i * SIZE, SIZE * rows, i * SIZE));
 		}
 
 		getChildren().add(ships);
 		getChildren().add(missiles);
 
 		for (Ship ship : board.getPlacedShips()) {
 			addShip(ship);
 		}
 
 		for (Missile missile : board.getPlacedMissiles()) {
 			drawMissile(missile);
 		}
 
 		// This is for the opponent's board. This has to move somewhere else
 		// later, I think
 		switch (type) {
 		case OPPONENT:
 			setOnMouseClicked(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent e) {
 					if (blocked)
 						return;
 					else
 						blocked = true;
 
 					Missile m = new Missile(getCoordinates(e.getX(), e.getY()));
 					drawMissile(m);
 					try {
 						board.placeMissile(m);
 					} catch (RuntimeException r) {
 						missileViews.get(m.getCoordinates()).setVisible(false);
 					}
 				}
 			});
 			break;
 		case LOCAL:
 			setOnMousePressed(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent e) {
 					final ShipView sv = getShipView(e);
 					if (sv == null)
 						return;
 
 					final double dx = e.getSceneX() - sv.getLayoutX();
 					final double dy = e.getSceneY() - sv.getLayoutY();
 
 					sv.setOnMouseDragged(new EventHandler<MouseEvent>() {
 						@Override
 						public void handle(MouseEvent me) {
 							sv.setLayoutX(me.getSceneX() - dx);
 							sv.setLayoutY(me.getSceneY() - dy);
 						}
 					});
 					sv.setOnMouseReleased(new EventHandler<MouseEvent>() {
 						@Override
 						public void handle(MouseEvent me) {
 							Coordinates c = getCoordinates(sv.getLayoutX()
 									- getLayoutX(), sv.getLayoutY()
 									- getLayoutY());
 
 							Ship s = sv.getShip();
 							try {
 								if (c.equals(s.getStartCoordinates())) {
 									// this one was just a click
 									if (me.getButton() == MouseButton.PRIMARY)
 										board.getBoardSetup().moveShip(s,
 												s.getStartCoordinates(),
 												s.getDirection().rotateCCW());
 									else
 										board.getBoardSetup().moveShip(s,
 												s.getStartCoordinates(),
 												s.getDirection().rotateCW());
 								} else {
 									// this was an actual move
 									board.getBoardSetup().moveShip(s, c,
 											s.getDirection());
 								}
 							} catch (RuntimeException ignore) {
 								// The ship will be reset
 								ignore.printStackTrace();
 							}
 							sv.update();
 							sv.relocate(getX(s.getStartCoordinates()),
 									getY(s.getStartCoordinates()));
 							sv.setOnMouseDragged(null);
 							sv.setOnMouseReleased(null);
 						}
 					});
 				}
 			});
 			break;
 		}
 
 		board.addBoardListener(new BoardListener() {
 			@Override
 			public void stateChanged(final Missile m) {
 				Platform.runLater(new Runnable() {
 					@Override
 					public void run() {
 						// javaFX operations should go here
 						MissileView mv = missileViews.get(m.getCoordinates());
 						if (mv != null)
 							mv.update(m);
 						else
 							drawMissile(m);
 
 						if (m.getSunkShip() != null) {
 							addShip(m.getSunkShip());
 						}
 
 						if (m.getMissileState() != MissileState.FIRED)
 							blocked = false;
 					}
 				});
 			}
 		});
 	}
 
 	private ShipView getShipView(MouseEvent e) {
 		for (ShipView v : shipViews) {
 			if (v.getBoundsInParent().contains(e.getX(), e.getY())) {
 				return v;
 			}
 		}
 		return null;
 	}
 
 	public void addShip(Ship ship) {
 		ShipView sv = new ShipView(ship);
 		moveShip(sv, ship);
 		ships.getChildren().add(sv);
 		shipViews.add(sv);
 	}
 
 	private void moveShip(ShipView sv, Ship ship) {
 		sv.relocate(getX(ship.getStartCoordinates()),
 				getY(ship.getStartCoordinates()));
 	}
 
 	public Coordinates getCoordinates(double x, double y) {
 		return new Coordinates((int) (x / SIZE) + 1, (int) (y / SIZE) + 1);
 	}
 
 	private void drawMissile(Missile missile) {
 		MissileView mv = new MissileView(missile);
 		mv.relocate(getX(missile.getCoordinates()),
 				getY(missile.getCoordinates()));
 		missiles.getChildren().add(mv);
 		missileViews.put(missile.getCoordinates(), mv);
 	}
 
 	private double getX(Coordinates c) {
 		return SIZE * (c.x - 1);
 	}
 
 	private double getY(Coordinates c) {
 		return SIZE * (c.y - 1);
 	}
 
 	private Shape getWater(int rows, int columns) {
 		Rectangle water = new Rectangle(columns * SIZE, rows * SIZE);
 		water.setFill(Color.LIGHTBLUE);
 		return water;
 	}
 
 	private Line getLine(double x1, double y1, double x2, double y2) {
 		Line line = new Line(x1, y1, x2, y2);
 		line.setStrokeWidth(0.1);
 		return line;
 	}
 
 	public static enum BoardType {
 		LOCAL, OPPONENT;
 
 		private Board getBoard(Game game) {
 			switch (this) {
 			case LOCAL:
 				return game.getLocalPlayer().getBoard();
 			case OPPONENT:
 				return game.getOpponent().getBoard();
 			}
 			throw new RuntimeException("This mustn't happen ;)");
 		}
 	}
 }
