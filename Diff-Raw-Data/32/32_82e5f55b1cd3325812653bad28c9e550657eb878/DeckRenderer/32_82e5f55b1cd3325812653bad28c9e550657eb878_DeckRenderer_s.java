 package de.xftl.game.states.combat;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 import de.xftl.game.framework.GameObject;
 import de.xftl.game.framework.XftlGameRenderer;
 import de.xftl.spec.model.Point;
 import de.xftl.spec.model.ships.Deck;
 import de.xftl.spec.model.ships.Room;
 import de.xftl.spec.model.ships.Tile;
 import de.xftl.spec.model.ships.TileUnit;
 
 public class DeckRenderer extends GameObject {
 
 	private Deck _deck;
 	private Texture _floorTexture;
 	private ArrayList<Sprite> _roomTiles;
 	private ArrayList<Sprite> _wallTiles;
 	private float _x;
 	private float _y;
 	
 	private final int WALLTHICKNESS = 3;
 	
 	public DeckRenderer(XftlGameRenderer game, Deck deck) {
 		super(game);
 		
 		_deck = deck;
 		_floorTexture = getResources().getTexture("res/tex/floor.png");
 		
 		_roomTiles = new ArrayList<Sprite>();
 		_wallTiles = new ArrayList<Sprite>();
 		
 		float tileSize = getGame().TileSize;
 		
 		for(Room room : deck.getRooms()) {
			Point<TileUnit> upperLeftCorner = room.getLeftUpperCornerPos();
			float baseX = upperLeftCorner.getX().getValue() * tileSize;
			float baseY = upperLeftCorner.getY().getValue() * tileSize;
 			
 			for(Tile tile : room.getTiles()) {
 				
 				Sprite sprite = new Sprite(_floorTexture);
 				Point<TileUnit> pos = tile.getLeftUpperCornerPos();
				sprite.setPosition(baseX + pos.getX().getValue() * tileSize, baseY + pos.getY().getValue() * tileSize);
 				_roomTiles.add(sprite);
 				
				int wallOffset = WALLTHICKNESS / 2;
				int wallLength = (int) (tileSize + WALLTHICKNESS - wallOffset);
 				
 				if (tile.getNorthNeighbor() == null) {
					addWallSprite(sprite.getX()-wallOffset, sprite.getY(), 0, wallOffset, wallLength, WALLTHICKNESS);
 				}
 				if (tile.getSouthNeighbor() == null) {
					addWallSprite(sprite.getX()-wallOffset, sprite.getY()+tileSize-wallOffset, 0, wallOffset, wallLength, WALLTHICKNESS);
 				}
 				if (tile.getEastNeighbor() == null) {
					addWallSprite(sprite.getX()+tileSize-wallOffset, sprite.getY()-wallOffset, wallOffset, 0, WALLTHICKNESS, wallLength);
 				}
 				if (tile.getWestNeighbor() == null) {
					addWallSprite(sprite.getX(), sprite.getY()-wallOffset, wallOffset, 0, WALLTHICKNESS, wallLength);
 				}
 			}
 		}
 	}
	
	private void addWallSprite(float x, float y, float offsetX, float offsetY, float width, float height) {
 		Sprite wall = new Sprite(getGame().getBlankTexture());
 		wall.setSize(width, height);
		wall.setPosition(x-offsetX, y-offsetY);
 		wall.setColor(0.0f, 0.0f, 0.0f, 1.0f);
 		_wallTiles.add(wall);
 	}
 	
 	public void setPosition(float x, float y) {
 		float differenceX = x - _x;
 		float differenceY = y - _y;
 		_x = x;
 		_y = y;
 		for(Sprite sprite : _roomTiles) {
 			sprite.setPosition(sprite.getX() + differenceX, sprite.getY() + differenceY);
 		}	
 		for(Sprite sprite : _wallTiles) {
 			sprite.setPosition(sprite.getX() + differenceX, sprite.getY() + differenceY);
 		}		
 	}
 	
 	public void draw() {
 		for(Sprite sprite : _roomTiles) {
 			sprite.draw(getSpriteBatch());
 		}
 		for(Sprite sprite : _wallTiles) {
 			sprite.draw(getSpriteBatch());
 		}
 	}
 
 }
