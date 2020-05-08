 package core;
 
 import core.tilefactory.AbstractTileSetFactory;
 
 public class GameObjects
 {
	protected AbstractTileSetFactory eTileset	= null;
	protected AbstractTileSetFactory gTileset	= null;
 	protected IGameStrategy game				= null;
 	
 	public GameObjects()
 	{
		this.eTileset	= Game.getEditorTileset();
		this.gTileset	= Game.getPlaygroundTileset();
 		this.game		= Game.getGame();
 	}
 }
