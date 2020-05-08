 package de.xftl.game.states.combat;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 import de.xftl.game.framework.XftlGameRenderer;
 import de.xftl.game.framework.ui.RenderedGameObject;
 import de.xftl.spec.model.Direction;
 import de.xftl.spec.model.Point;
 import de.xftl.spec.model.ships.Deck;
 import de.xftl.spec.model.ships.Door;
 import de.xftl.spec.model.ships.Lift;
 import de.xftl.spec.model.ships.Room;
 import de.xftl.spec.model.ships.Tile;
 import de.xftl.spec.model.ships.TileOrRoomConnector;
 import de.xftl.spec.model.ships.TileUnit;
 
 public class DeckRenderer extends RenderedGameObject {
 
 	private ArrayList<DoorRenderer> _doors;
 	private ArrayList<LiftRenderer> _lifts;
 	private float _x;
 	private float _y;
 	private float _sizeX;
 	private float _sizeY;
 
 	public DeckRenderer(XftlGameRenderer game, Deck deck) {
 		super(game);
 		
 		_doors = new ArrayList<DoorRenderer>();
 		_lifts = new ArrayList<LiftRenderer>();
 		
 		float tileSize = getGame().TileSize;
 		
 		_sizeX = 0;
 		_sizeY = 0;
 		
 		for(Room room : deck.getRooms()) {
 			
 			for(Tile tile : room.getTiles()) {
 				
 				TileRenderer tileRenderer = new TileRenderer(game, tile);
				addChild(tileRenderer);
 				
 				handleNeighbour(tile.getNorthNeighbor(), tileRenderer.getX(), tileRenderer.getY(), Direction.NORTH);
 				handleNeighbour(tile.getSouthNeighbor(), tileRenderer.getX(), tileRenderer.getY(), Direction.SOUTH);
 				handleNeighbour(tile.getEastNeighbor(), tileRenderer.getX(), tileRenderer.getY(), Direction.EAST);
 				handleNeighbour(tile.getWestNeighbor(), tileRenderer.getX(), tileRenderer.getY(), Direction.WEST);
 				
 				Point<TileUnit> tilePosition = tile.getLeftUpperCornerPos();
 				_sizeX = Math.max(_sizeX, tilePosition.getX().getValue() * getGame().TileSize + getGame().TileSize);
 				_sizeY = Math.max(_sizeY, tilePosition.getY().getValue() * getGame().TileSize + getGame().TileSize);
 			}
 			
 			addChild(new RoomOverlay(game, room));
 		}
 	}
 	
 	public float getSizeX() {
 		return _sizeX;
 	}
 	
 	public float getSizeY() {
 		return _sizeY;
 	}
 	
 	private boolean doorExists(Door door) {
 		for(DoorRenderer doorRenderer : _doors) {
 			if (doorRenderer.getDoor() == door) return true;
 		}
 		
 		return false;
 	}
 	
 	public void handleNeighbour(TileOrRoomConnector tile, float x, float y, Direction direction) {
 		
 		if (tile == null) {
 			addChild(new WallRenderer(getGame(), x, y, direction));
 		}
		else if (tile instanceof Lift) {
			if (!liftExists((Lift)tile)) {
				LiftRenderer liftRenderer = new LiftRenderer(getGame(), (Lift)tile,x,y, direction);
				_lifts.add(liftRenderer);
				addChild(liftRenderer);
			}
		}
 		else if (tile instanceof Door) {
 			if (!doorExists((Door)tile)) {
 				DoorRenderer doorRenderer = new DoorRenderer(getGame(), (Door)tile, x, y, direction);
 				addChild(doorRenderer);
 				_doors.add(doorRenderer);
 			}
 		}
 	}
 	
 	private boolean liftExists(Lift lift) {
 		for(LiftRenderer doorRenderer : _lifts) {
 			if (doorRenderer.getLift() == lift) return true;
 		}
 	
 		return false;
 	}
 			
 	@Override
 	public float getX() {
 		return _x;
 	}
 
 	@Override
 	public float getY() {
 		return _y;
 	}
 
 }
