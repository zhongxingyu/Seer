 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.AnimatedImage;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.CheckPointView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.ConveyorBeltView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.GearsView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.LaserView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.DockView;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Laser;
 import se.chalmers.dryleafsoftware.androidrally.sharred.MapBuilder;
 
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
 
 /**
  * This view class displays everything on the game area.
  * 
  * @author
  *
  */
 public class BoardView extends Stage {
 
 	private List<RobotView> robots = new ArrayList<RobotView>();
 	private List<AnimatedImage> animated = new ArrayList<AnimatedImage>();
 	private Vector2[] docks = new Vector2[8];
 	private final Group container; 
 	private final Table scrollContainer;
 	private final ScrollPane pane;
 	
 	private boolean[][][] collisionMatrix;
 			
 	/**
 	 * Creates a new instance of BoardView.
 	 */
 	public BoardView() {
 		super();
 		// Create a group of all the objects on the map.
 		container = new Group();
 		container.setPosition(0, 0);
 		container.setSize(480, 640);		
 		
 		// Create a inner container for the the ScrollPane.
 		scrollContainer = new Table();
 		scrollContainer.setFillParent(true);
 		scrollContainer.setLayoutEnabled(false);
 		scrollContainer.add(container);
 		scrollContainer.debug();
 		
 		// Create the ScrollPane.
         ScrollPaneStyle scrollStyle = new ScrollPaneStyle();
         pane = new ScrollPane(scrollContainer, scrollStyle);
         pane.setForceOverscroll(true, true);
         
         // Create a container to hold the ScrollPane.
         Table scrollParent = new Table();
         scrollParent.add(pane);
         scrollParent.debug();
         scrollParent.setPosition(0, 320);
         scrollParent.setSize(480, 480);
         addActor(scrollParent);
         
         // Create a default camera.
         OrthographicCamera boardCamera = new OrthographicCamera(480, 800);
 		boardCamera.position.set(240, 400, 0f);
 		boardCamera.update();
 		setCamera(boardCamera);
         
         setZoom(1f, 0, 0);	        
         pane.addListener(new ActorGestureListener() {
         	@Override
         	public void zoom(InputEvent event, float initialDistance, float distance) {
         		if(distance - initialDistance > 0) {
         			setZoom(getZoom() + 0.02f, event.getStageX(), event.getStageY());
         		}else{
         			setZoom(getZoom() - 0.02f, event.getStageX(), event.getStageY());
         		}
         	}
         	@Override
         	public void tap(InputEvent event, float x, float y, int count, int button) {
         		if(count == 2) {
         			if(getZoom() == 2){
         				setZoom(1f, event.getStageX(), event.getStageY());
         			} else {
         				setZoom(2f, event.getStageX(), event.getStageY());
         			}
         		}
         	}
         });
 	}
 	
 	/**
 	 * Gives the current zoom level.
 	 * @return The current zoom level.
 	 */
 	public float getZoom() {
 		return container.getScaleX();
 	}
 	
 	/**
 	 * Sets the zoom level.
 	 * @param zoom The new zoom level.
 	 */
 	public void setZoom(float zoom, float x, float y) {
 		if(zoom < 1) {
 			zoom = 1;
 		}else if(zoom > 2) {
 			zoom = 2f;
 		}
 		container.setScale(zoom);
 		container.setSize(480 * zoom, 640 * zoom);	
 		scrollContainer.setSize(container.getWidth(), container.getHeight());	
 		pane.layout();
 	}
 	
 	/**
 	 * Builds the board using the specified texture and map data.
 	 * @param texture The texture to use.
 	 * @param map An array of strings describing the map's layout.
 	 * NOTE: The bottom four rows of the map array will always be created as the dock area.
 	 */
 	public void createBoard(final Texture texture, String map) {
 		collisionMatrix = new boolean[12][16][2];
 		final Texture conveyerTexture = new Texture(Gdx.files.internal("textures/special/conveyor.png"));
 		conveyerTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		conveyerTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.Repeat);
 
 		// To be added on top of all the other objects
 		final List<Image> overlay = new ArrayList<Image>();
 
 		new MapBuilder(map) {
 			@Override
 			public void buildFactoryFloor(int x, int y) {
 				Image floor = new Image(new TextureRegion(texture, 0, 0, 64, 64));
 				floor.setSize(40, 40);
 				floor.setPosition(40 * x, 640 - 40 * (y+1));
 				container.addActor(floor);
 			}
 			@Override
 			public void buildDockFloor(int x, int y) {
 				Image floor = new Image(new TextureRegion(texture, 64, 0, 64, 64));
 				floor.setSize(40, 40);
 				floor.setPosition(40 * x, 640 - 40 * (y+1));
 				container.addActor(floor);
 			}
 			@Override
 			public void buildHole(int x, int y) {
 				container.addActor(setCommonValues(
 						new Image(new TextureRegion(texture, 128, 0, 64, 64)), x, y));
 			}
 			@Override
 			public void buildGear(int x, int y, boolean cw) {
 				animated.add((AnimatedImage)setCommonValues(
 						new GearsView(new TextureRegion(texture, 320, 0, 64, 64), cw), x, y));
 			}
 			@Override
 			public void buildConveyerBelt(int x, int y, int speed, int dir) {
 				animated.add((AnimatedImage)setCommonValues(
 						new ConveyorBeltView(new TextureRegion(
 								conveyerTexture, 64 * (speed - 1), 0, 64, 64), 
 								dir * 90, speed), x, y));
 			}
 			@Override
 			public void buildCheckPoint(int x, int y, int nbr) {
 				container.addActor(setCommonValues(
 						new CheckPointView(new TextureRegion(texture, 192, 0, 64, 64), nbr), x, y));
 			}
 			@Override
 			public void buildRepair(int x, int y) {
 				container.addActor(setCommonValues(
 						new Image(new TextureRegion(texture, 256, 0, 64, 64)), x, y));
 						
 			}
 			@Override
 			public void buildStartDock(int x, int y, int nbr) {
 				container.addActor(setCommonValues(
 						new DockView(new TextureRegion(
 								texture, 0, 128, 64, 64), nbr), x, y));
 				docks[nbr - 1] = new Vector2(40 * x, 640 - 40 * (y+1));
 			}
 			@Override
 			public void buildWall(int x, int y, int dir) {
 				overlay.add(setCommonOverlayValues(
 						new Image(new TextureRegion(texture, 384, 0, 64, 64)), x, y, dir));
 				if(dir == MapBuilder.DIR_NORTH) {
 					collisionMatrix[x][y][0] = true;
 				}else if(dir == MapBuilder.DIR_SOUTH) {
 					collisionMatrix[x][y+1][0] = true;
 				}else if(dir == MapBuilder.DIR_WEST) {
 					collisionMatrix[x][y][1] = true;
 				}else if(dir == MapBuilder.DIR_EAST) {
 					collisionMatrix[x+1][y][1] = true;
 				}
 			}
 			@Override
 			public void buildLaser(int x, int y, int dir) {
 				overlay.add(setCommonOverlayValues(
 						new Image(new TextureRegion(texture, 448, 0, 64, 64)), x, y, dir));
 				animated.add((AnimatedImage)setCommonValues(
 						new LaserView(new TextureRegion(texture, 0, 192, 64, 64), (dir + 2)%4), x, y));
 			}
 			private Image setCommonOverlayValues(Image overlayImage, int x, int y, int dir) {
 				overlayImage.setSize(40, 40);
 				overlayImage.setPosition(40 * x, 640 - 40 * (y+1) + 20);
 				overlayImage.setOrigin(overlayImage.getWidth()/2 , 
 						overlayImage.getHeight()/2 - 20);
 				overlayImage.rotate(-(dir) * 90);
 				return overlayImage;	
 			}
 			private Image setCommonValues(Image i, int x, int y) {
 				i.setSize(40, 40);
 				i.setPosition(40 * x, 640 - 40 * (y+1));
 				i.setOrigin(20, 20);
 				return i;
 			}
 		};
 				
 		for(AnimatedImage i : animated) {
 			container.addActor(i);
 		}
 		
 		//Add walls (added last to be on top of everything else)
 		for(Image i : overlay) {
 			container.addActor(i);
 		}
 	}
 
 	/**
 	 * Starts the animations of the items connected with the specified phase.
 	 * @param subPhase The phase to animate.
 	 */
 	public void setAnimate(int subPhase) {
 		if(subPhase == GameAction.PHASE_LASER) {
 			for(RobotView robot : robots) {
 				animated.add(robot.getLaser());
 				container.addActor(robot.getLaser());
 			}
 		}
 		for(AnimatedImage a : animated) {
 			a.enable(subPhase);
 			if(subPhase == GameAction.PHASE_LASER && a instanceof LaserView) {
 				((LaserView)a).setCollisionMatrix(collisionMatrix);
 			}
 		}
 	}
 
 	/**
 	 * Stops all animations.
 	 */
 	public void stopAnimations() {
 		for(AnimatedImage a : animated) {
 			a.disable();
 		}
 	}
 	
 	/**
 	 * Gives an array of the eight docks' positions stored as vectors where (0,0) is the top
 	 * left corner and (40, 40) is the tile to the right and down.
 	 * @return An array of the eight docks.
 	 */
 	public Vector2[] getDocksPositions() {
 		return this.docks;
 	}
 	
 	/**
 	 * Adds the specified player to the board.
 	 * @param player The player to add.
 	 */
 	public void addRobot(RobotView player) {
 		robots.add(player);
 		container.addActor(player);
 	}
 	
 	/**
 	 * Gives a list of all the robots on the board.
 	 * @return A list of all the robots on the board.
 	 */
 	public List<RobotView> getRobots() {
 		return this.robots;
 	}
 	
 	/**
 	 * Gives the robot with the specified robot ID-number.
 	 * @param robotID The ID-number to look for.
 	 * @return The robot with the specified ID.
 	 */
 	public RobotView getRobot(int robotID) {
 		for(RobotView p : robots) {
 			if(p.getRobotID() == robotID) {
 				return p;
 			}
 		}
 		return null;
 	}
 }
