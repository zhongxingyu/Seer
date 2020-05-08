 package core;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.ImageIcon;
 
 
 import core.gui.GUIScreener;
 import core.tilefactory.AbstractTileSetFactory;
 
 
 public class Core implements Observer
 {
 	// Instanzvariable fr den GUIScreener bereitstellen:
 	private static GUIScreener screener				= null;
 	
 	// ArrayList der im Konstruktor bergebenen Spiele ( =Packagename des jew. Spiels )
 	// bereitstellen:
 	private ArrayList<String> pkgGames				= new ArrayList<String>();
 	
 	// ArrayList fr die per Class-Reflection erzeugten Spiel-Instanzen
 	// bereitstellen:
 	private ArrayList<IGameStrategy> games						= new ArrayList<IGameStrategy>();
 	
 	// ArrayList der Spiele-Icons fr den Screener bereitstellen:
 	private ArrayList<ImageIcon> gamesIcons			= new ArrayList<ImageIcon>();
 	
 	// ArrayList der Spiele-Namen fr den Screener bereitstellen:
 	private ArrayList<String> gamesNames			= new ArrayList<String>();
 	
 	protected IGameEditor gameEditor				= null;			
 	
 	// Konstruktor:
 	public Core( String backgroundImage ) 
 	{ 
 		// Instanz des GUIScreeners holen und speichern:
 		Core.screener 	= GUIScreener.getInstance();
 		
 		// Eigene Instanz and GUIScreener bergeben 
 		// ( zur spteren Steuerung der GUIScreener-Events in der GameBuilder (MVC) ):
 		Core.screener.setFactory( this );
 		
 		// GUIScreener initialisieren:
 		Core.screener.init( backgroundImage );
 	}
 	
 	public static void showScreener()
 	{
 		Core.screener.setVisible( true );
 		Core.screener.requestFocus();
 		Core.screener.pack();
 	}
 	
 	// Ein Spiele-Package hinzufgen ( wird in der Klasse BWVGame aufgerufen ):
 	public void addGame( String pkgGame ) { this.pkgGames.add( pkgGame ); }
 	
 	// Laden der Spiele-Packages per Class-Reflection:
 	public void load()
 	{
 		// Anzahl der bergebenen Spiele ermitteln:
 		int gLen			= this.pkgGames.size();
 		
 		// Anzahl der Spiele an GUIScreeer zur Forschrittsanzeige bergeben:
		Core.screener.setMaximum( gLen * 23 );
 
 		for( int gCnt = 0; gCnt < gLen; gCnt++ )
 		{
 			try 
 			{
 				// String der Klassen generieren ( Schema: Package.Class ):
 				String gamename							= this.pkgGames.get( gCnt ).toLowerCase();
 				String game								= gamename + "." + this.pkgGames.get( gCnt );
 				String playground						= gamename + ".playground.Playground";
 				String playgroundTileset				= gamename + ".playground.Tileset";
 				String editor							= gamename + ".editor.Editor";
 				String editorTileset					= gamename + ".editor.Tileset";
 				
 				// Klassen-Objekte holen:
 				Class clGame							= (Class<IGameStrategy>) Class.forName( game );
 				Core.screener.setProgress();
 
 				Class clPlayground						= (Class<IGamePlayground>) Class.forName( playground );
 				Core.screener.setProgress();
 		
 				@SuppressWarnings("unused")
 				Class clPlaygroundTileset 				= (Class<AbstractTileSetFactory>) Class.forName( playgroundTileset );
 				Core.screener.setProgress();
 				
 				Class clEditor							= (Class<IGameEditor>) Class.forName( editor );
 				Core.screener.setProgress();
 		
 				Class clEditorTileset 					= (Class<AbstractTileSetFactory>) Class.forName( editorTileset );
 				Core.screener.setProgress();
 				
 				// Konstruktor der Klassenobjekte holen:
 				java.lang.reflect.Constructor coGame			= clGame.getConstructor();
 				Core.screener.setProgress();
 
 				java.lang.reflect.Constructor coPlayground		= clPlayground.getConstructor();
 				Core.screener.setProgress();
 				
 				java.lang.reflect.Constructor coPGTileset		= clPlaygroundTileset.getConstructor();
 				Core.screener.setProgress();
 				
 				java.lang.reflect.Constructor coEditor			= clEditor.getConstructor();
 				Core.screener.setProgress();
 				
 				java.lang.reflect.Constructor coEditorTileset	= clEditorTileset.getConstructor();
 				Core.screener.setProgress();
 				
 				// Objektinstanzen erzeugen:
 				AbstractTileSetFactory instETileset		= (AbstractTileSetFactory)coEditorTileset.newInstance();
 				Core.screener.setProgress();
 				
 				AbstractTileSetFactory instPGTileset	= (AbstractTileSetFactory)coPGTileset.newInstance();
 				Core.screener.setProgress();
 				
 				IGameStrategy instGame					= (IGameStrategy)coGame.newInstance();
 				Core.screener.setProgress();
 				
 				instGame.setGame( instGame );
 				Core.screener.setProgress();
 
 				instGame.setPGTileset( instPGTileset );		
 				Core.screener.setProgress();
 
 				instGame.setEditorTileset( instETileset );		
 				Core.screener.setProgress();
 				
 				IGamePlayground instPlayground			= (IGamePlayground)coPlayground.newInstance();
 				Core.screener.setProgress();
 				
 				IGameEditor instEditor					= (IGameEditor)coEditor.newInstance();
 				Core.screener.setProgress();
 
 				instGame.setPlayground( instPlayground );
 				Core.screener.setProgress();
 				
 				instGame.setEditor( instEditor );
 				Core.screener.setProgress();
 				
 				// Hinzufgen des instanziierten Spiels/Editors: 
 				this.games.add( instGame );
 				Core.screener.setProgress();
 				
 				// Hinzufgen des Spiel-Icons:
 				this.gamesIcons.add( instGame.getIcon() );
 				Core.screener.setProgress();
 				
 				// Hinzufgen des Spiele-Namens:
 				this.gamesNames.add( instGame.getName() );
 				Core.screener.setProgress();
 			} catch( Exception e ) 
 			{
 				// TODO: Fehler als Inscreen-Nachricht ausgeben:
 				e.printStackTrace();
 			}
 		}
 		
 		// Den GUIScreener anweisen, alle vorhandenen Spiele zur Auswahl zur 
 		// Verfgung zu stellen:
 		Core.screener.showGames( this.gamesIcons, this.gamesNames );
 	}
 	
 	// Spiel aufrufen:
 	public void executeGame( Integer i )
 	{
 		// Screener verstecken: 
 		Core.screener.setVisible( false );
 		
 		IGameStrategy game	= this.games.get( i );
 		
 		// Spielinstanz aus ArryList holen und ausfhren ( realisiert als Strategy-Pattern ):
 		game.execute();
 	}
 	
 	// Bei schliessen des GameScreeners (nicht zu verwechseln mit dem GUIScreener)
 	// wird die Methode "update" aufgerufen ( Observer-Pattern ):
 	public void update( Observable arg0, Object arg1 ) 
 	{
 		// GUIScreener wieder anzeigen:
 		Core.screener.setVisible( true );
 		Core.screener.repaintScreenPanel();
 	}
 }
