 package core;
 
 import core.tilefactory.AbstractTileSetFactory;
 
 public class GameObjects
 {
	protected AbstractTileSetFactory tileset	= null;
 	protected IGameStrategy game				= null;
 	
 	public GameObjects()
 	{
		this.tileset	= Game.getEditorTileset();
 		this.game		= Game.getGame();
 	}
 }
