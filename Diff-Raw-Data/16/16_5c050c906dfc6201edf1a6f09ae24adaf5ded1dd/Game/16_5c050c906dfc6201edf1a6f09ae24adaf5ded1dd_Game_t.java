 package core;
 
 import javax.swing.ImageIcon;
 
 import core.tilefactory.AbstractTileSetFactory;
 
 
 
 public abstract class Game implements IGameStrategy
 {
 	protected static AbstractTileSetFactory editorTileset		= null;
 	protected static AbstractTileSetFactory playgroundTileset	= null;
 	protected static IGameStrategy game							= null;
 	protected IGameEditor editor								= null;
 	protected IGamePlayground playground						= null;
 	
 	public Game() 				{				}
 	public void execute() 		{				}
 	public ImageIcon getIcon() 	{ return null; 	}
 	public String getName() 	{ return null; 	}
 
 	public void setGame( IGameStrategy game  )
 	{
 		Game.game	= game; 
 	}
 	
 	public void setEditorTileset( AbstractTileSetFactory editorTileset )
 	{
 		Game.editorTileset = editorTileset;
 	}
 
 	public void setPGTileset( AbstractTileSetFactory playgroundTileset )
 	{
 		Game.playgroundTileset = playgroundTileset;
 	}
 
 	public void setPlayground( IGamePlayground playground )	
 	{ 
 		this.playground = playground;
 	}
 	
 	public void setEditor( IGameEditor editor )	
 	{ 
 		this.editor = editor;
 	}
 	
 	public void openEditor()
 	{
 		this.editor.showEditor();
 	}
	
	public void closeEditor() 
	{
		
	}
 
 	public void openPlayground()
 	{
		this.playground.openPlayground();
 	}
 	
	public void closePlayground() {}

 
 	public static IGameStrategy getGame() { return Game.game; }
 	
	public static AbstractTileSetFactory getEditorTileset() 	{ return Game.editorTileset; }
	public static AbstractTileSetFactory getPlaygroundTileset() { return Game.playgroundTileset; }
 }
