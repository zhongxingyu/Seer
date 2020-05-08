 package org.blockout.ui;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
import org.blockout.common.TileCoordinate;
 import org.blockout.engine.ISpriteManager;
 import org.blockout.engine.SpriteType;
 import org.blockout.world.IWorld;
 import org.blockout.world.LocalGameState;
 import org.blockout.world.Tile;
 import org.blockout.world.entity.Actor;
 import org.blockout.world.entity.Crate;
 import org.blockout.world.event.AttackEvent;
 import org.blockout.world.event.CrateOpenedEvent;
 import org.blockout.world.event.IEvent;
 import org.blockout.world.state.IStateMachine;
 import org.bushe.swing.event.EventTopicSubscriber;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.InputListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.util.pathfinding.Path;
 import org.newdawn.slick.util.pathfinding.Path.Step;
 import org.newdawn.slick.util.pathfinding.PathFinder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.Button;
 import de.lessvoid.nifty.controls.ButtonClickedEvent;
 import de.lessvoid.nifty.elements.Element;
 
 /**
  * This class is responsible for converting user interactions to {@link IEvent}s
  * for the underlying {@link IStateMachine}.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 @Named
 public class InputHandler implements InputListener {
 
 	private static final Logger	logger;
 	static {
 		logger = LoggerFactory.getLogger( InputHandler.class );
 	}
 	protected Camera			camera;
 	protected PathFinder		pathFinder;
 	protected LocalGameState	gameState;
 	protected PlayerMoveHandler	playerController;
 	protected IStateMachine		stateMachine;
 	protected IWorld			world;
 	protected GameContainer		container;
 	protected ISpriteManager	spriteManager;
 	protected Nifty				nifty;
 
 	private Element				exitPopup;
 
 	@Inject
 	public InputHandler(final Camera camera, final PathFinder pathFinder, final LocalGameState gameState,
 			final PlayerMoveHandler playerController, final IStateMachine stateMachine, final IWorld world,
 			final ISpriteManager spriteManager) {
 		this.camera = camera;
 		this.pathFinder = pathFinder;
 		this.gameState = gameState;
 		this.playerController = playerController;
 		this.stateMachine = stateMachine;
 		this.world = world;
 		this.spriteManager = spriteManager;
 	}
 
 	public void setGameContainer( final GameContainer container ) {
 		this.container = container;
 	}
 
 	public void setNifty( final Nifty nifty ) {
 		this.nifty = nifty;
 	}
 
 	@Override
 	public void keyReleased( final int arg0, final char arg1 ) {
 		if ( arg0 == Input.KEY_I ) {
 			Element inventory = nifty.getCurrentScreen().findElementByName( "inventory_layer" );
 			inventory.setVisible( !inventory.isVisible() );
 		}
 
 		if ( arg0 == Input.KEY_ESCAPE ) {
 			if ( exitPopup == null ) {
 				exitPopup = nifty.createPopup( "popupMenu" );
 				Button btnReturnToGame = exitPopup.findNiftyControl( "btnReturnToGame", Button.class );
 				nifty.subscribe( nifty.getCurrentScreen(), btnReturnToGame.getId(), ButtonClickedEvent.class,
 						new EventTopicSubscriber<ButtonClickedEvent>() {
 
 							@Override
 							public void onEvent( final String arg0, final ButtonClickedEvent arg1 ) {
 								System.out.println( "Closing popup..." );
 								nifty.closePopup( exitPopup.getId() );
 								exitPopup = null;
 							}
 						} );
 				nifty.showPopup( nifty.getCurrentScreen(), exitPopup.getId(), null );
 			}
 		}
 	}
 
 	private static enum MouseCursor {
 		Move, Attack
 	}
 
 	private MouseCursor	cursor;
 
 	@Override
 	public void mouseMoved( final int oldx, final int oldy, final int newx, final int newy ) {
 		try {
 			camera.lock();
 			Tile tile = world.getTile( camera.worldToTile( camera.screenToWorldX( newx ) ),
 					camera.worldToTile( camera.screenToWorldY( newy ) ) );
 			if ( cursor != MouseCursor.Attack && tile != null && tile.getEntityOnTile() instanceof Actor
 					&& tile.getEntityOnTile() != gameState.getPlayer() ) {
 				container.setMouseCursor( spriteManager.getSprite( SpriteType.sword, true ), 31, 0 );
 				cursor = MouseCursor.Attack;
 			} else if ( cursor != MouseCursor.Move && tile != null && !(tile.getEntityOnTile() instanceof Actor) ) {
 				container.setMouseCursor( spriteManager.getSprite( SpriteType.arrow, true ), 31, 0 );
 				cursor = MouseCursor.Move;
 			}
 		} catch ( SlickException e ) {
 			logger.error( "Couldn't set mouse cursor.", e );
 		} finally {
 			camera.unlock();
 		}
 	}
 
 	private boolean areNeighbours( final int x1, final int y1, final int x2, final int y2 ) {
 		if ( Math.abs( x1 - x2 ) > 1 ) {
 			return false;
 		}
 		if ( Math.abs( y1 - y2 ) > 1 ) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void mouseReleased( final int button, final int x, final int y ) {
 		try {
 			camera.lock();
 			int tileX = camera.worldToTile( camera.screenToWorldX( x ) );
 			int tileY = camera.worldToTile( camera.screenToWorldY( y ) );
 			int centerX = camera.worldToTile( camera.getCenterX() );
 			int centerY = camera.worldToTile( camera.getCenterY() );
 
 			// Handle Attacks
 			Tile tile = world.getTile( tileX, tileY );
 			if ( tile != null && tile.getEntityOnTile() instanceof Actor ) {
 				stateMachine.pushEvent( new AttackEvent( gameState.getPlayer(), (Actor) tile.getEntityOnTile() ) );
 				return;
 			}
 
 			// Handle Crates
 			if ( tile != null && tile.getEntityOnTile() instanceof Crate
 					&& areNeighbours( tileX, tileY, centerX, centerY ) ) {
				TileCoordinate coordinate = world.findTile( tile.getEntityOnTile() );
				stateMachine.pushEvent( new CrateOpenedEvent( gameState.getPlayer(), (Crate) tile.getEntityOnTile(),
						coordinate ) );
 				return;
 			}
 
 			// Handle Movements
 			int fromX = centerX - camera.getStartTileX();
 			int fromY = centerY - camera.getStartTileY();
 			int toX = tileX - camera.getStartTileX();
 			int toY = tileY - camera.getStartTileY();
 			Path path = pathFinder.findPath( gameState.getPlayer(), fromX, fromY, toX, toY );
 			Path worldPath = new Path();
 			if ( path != null ) {
 				for ( int i = 0; i < path.getLength(); i++ ) {
 					Step step = path.getStep( i );
 					worldPath.appendStep( step.getX() + camera.getStartTileX(), step.getY() + camera.getStartTileY() );
 				}
 			}
 			playerController.setPath( stateMachine, worldPath );
 		} finally {
 			camera.unlock();
 		}
 	}
 
 	@Override
 	public void mouseWheelMoved( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseClicked( final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4 ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed( final int paramInt1, final int paramInt2, final int paramInt3 ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseDragged( final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4 ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setInput( final Input paramInput ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isAcceptingInput() {
 		return true;
 	}
 
 	@Override
 	public void inputEnded() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void inputStarted() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyPressed( final int paramInt, final char paramChar ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerLeftPressed( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerLeftReleased( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerRightPressed( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerRightReleased( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerUpPressed( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerUpReleased( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerDownPressed( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerDownReleased( final int paramInt ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerButtonPressed( final int paramInt1, final int paramInt2 ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void controllerButtonReleased( final int paramInt1, final int paramInt2 ) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
