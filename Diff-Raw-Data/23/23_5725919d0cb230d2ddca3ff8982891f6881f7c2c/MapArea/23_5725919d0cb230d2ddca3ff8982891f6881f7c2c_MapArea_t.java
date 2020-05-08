 package lightbeam.editor;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.RepaintManager;
 
 import core.tilefactory.AbstractTileSetFactory;
 import core.tilestate.ITileState;
 import core.tilestate.Tile;
 import core.tilestate.TileArray;
 
 import lightbeam.tiles.TileBeam;
 import lightbeam.tiles.TileBeamsource;
 import lightbeam.tiles.TileField;
 
 public class MapArea
 {
 	private AbstractTileSetFactory tileset		= null;
 	private JPanel panel						= null;
 	private JScrollPane scroll					= new JScrollPane();
 		
 	final static Cursor CURSOR_HAND				= new Cursor( Cursor.HAND_CURSOR );
 	final static Cursor CURSOR_DEFAULT			= new Cursor( Cursor.DEFAULT_CURSOR );
 	
 	private RepaintManager m					= null;
 	private TileArray map						= null;
 	private Tile focusedSource					= null;
 	private Tile manipSource					= null;
 //	private TileArray oldMap					= null;
 	
 	private String mapName						= "Testkarte";
 	
 	public MapArea( AbstractTileSetFactory tileset, int rows, int cols )
 	{
 		this.tileset	= tileset;
 
 		this.tileset.setSelected( this.tileset.tile( 1 ) );
 
 		this.panel		= new JPanel() 
 		{
 			private static final long serialVersionUID = -6409045276617050561L;
 
 			public void paintComponent( Graphics g )
 			{
 				MapArea.this.paintComponent( g );
 			}
 		};
 		
 		this.panel.setLayout( null );
 		
 		// MapArray init:
 		this.initMap( rows, cols );
 		
 		this.scroll.setViewportView( this.panel );
 		this.panel.setDoubleBuffered( true );
 		this.initScrollpane();
 		
 		this.m	= RepaintManager.currentManager( this.panel );
 	
 		this.panel.addMouseListener(new MouseAdapter(){public void mouseClicked( MouseEvent e ) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
 
 			if( MapArea.this.isInArea( row, col ) )
 			{
				if( MapArea.this.isField( row, col ) && MapArea.this.manipSource == null )
 				{
 					MapArea.this.drawBeamsource( row, col );
				} else if( MapArea.this.isBeamsource( row, col ) && MapArea.this.manipSource == null )
 				{
 					MapArea.this.editBeamsource( row, col );
 				} else if( MapArea.this.isField( row, col ) && MapArea.this.manipSource != null )
 				{
 					MapArea.this.saveBeamsource();
 					MapArea.this.leavePreviewMode();
 					MapArea.this.manipSource = null;
				} else if( 
					MapArea.this.isBeamsource( row, col ) && MapArea.this.manipSource != null &&
					( MapArea.this.manipSource.row() != row || MapArea.this.manipSource.col() != col )
				) {
					MapArea.this.leavePreviewMode();
					MapArea.this.manipSource = null;	
					MapArea.this.editBeamsource( row, col );
 				}
 				
 				MapArea.this.scroll.repaint();
 //				if( MapArea.this.isBeamsource( row, col ) == true )
 //				{
 //					Tile fSource	= MapArea.this.focusedSource;
 //					
 //					if( MapArea.this.manipSource != null && !fSource.equals( MapArea.this.manipSource ) )
 //					{
 //						MapArea.this.manipSource	= null;
 //						MapArea.this.doHilightPossibleBeams( row, col );
 //					} else if( 
 //						MapArea.this.manipSource != null &&
 //						fSource.row() == MapArea.this.manipSource.row() &&
 //						fSource.col() == MapArea.this.manipSource.col()
 //					) {
 //						MapArea.this.assignBeamsToSource();
 //					}
 //					
 //					Tile source 	= MapArea.this.map.tile( row, col );
 //					Tile mSource	= MapArea.this.manipSource;
 //					
 //					if( mSource == null ) 	
 //					{ 
 //						MapArea.this.manipSource 	= MapArea.this.map.tile( row, col );
 //						MapArea.this.manipSource.color( Tile.CYELLOW );
 //					} else
 //					{
 //						if( mSource.row() == fSource.row() && mSource.col() == fSource.col() )
 //						{
 //							MapArea.this.manipSource	= null;
 //							MapArea.this.doHilightPossibleBeams( row, col );
 //						}
 //						
 //						fSource	= MapArea.this.focusedSource;
 //						
 //						if( fSource.row() == source.row() && fSource.col() == source.col() &&
 //							( fSource.row() != mSource.row() || fSource.col() != mSource.col() )
 //						) {
 //							MapArea.this.manipSource	= fSource;
 //							MapArea.this.manipSource.color( Tile.CYELLOW );
 //						}
 //					}
 //					
 //					MapArea.this.scroll.repaint();
 //				} else if( MapArea.this.isField( row, col ) && MapArea.this.tileset.getSelected().type() != "beamsource" )
 //				{
 //					if( MapArea.this.manipSource != null && 
 //						MapArea.this.map.tile( row, col ).type() != "field" &&
 //						MapArea.this.map.tile( row, col ).type() == "beam" && 
 //						MapArea.this.map.tile( row, col ).hidden() == false
 //					) { 
 //						MapArea.this.assignBeamsToSource(); 
 //					}
 //					
 //					MapArea.this.map.mode( TileArray.MODE_READY );	
 //					MapArea.this.focusedSource 	= null;
 //					MapArea.this.manipSource	= null;
 //					
 //					MapArea.this.scroll.repaint();
 //				} else if( MapArea.this.tileset.getSelected().type() == "beamsource" )
 //				{
 //					MapArea.this.drawBeamsource( row, col );
 //					MapArea.this.manipSource	= MapArea.this.map.tile( row, col );
 //					MapArea.this.scroll.repaint();
 //				}
 			}
 		}});
 		
 		this.panel.addMouseMotionListener(new MouseMotionAdapter(){public void mouseMoved( MouseEvent e ) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
 			
 			if( MapArea.this.isInArea( row, col ) )
 			{
 				if( MapArea.this.map.mode() == TileArray.MODE_PREVIEW )
 				{
 					if( MapArea.this.isBeamsource( row, col ) &&
 						( MapArea.this.manipSource.row() != row || MapArea.this.manipSource.col() != col )
 					) {
 						MapArea.this.focusBeamsource( row, col );
 						MapArea.this.hilightPossibleBeams( MapArea.this.focusedSource );
 					} else if(
 						!MapArea.this.isBeamsource( row, col ) &&
 						MapArea.this.focusedSource != null && MapArea.this.manipSource != null &&
 						( MapArea.this.focusedSource.row() != MapArea.this.manipSource.row() || MapArea.this.focusedSource.col() != MapArea.this.manipSource.col() )
 					) {
 						MapArea.this.dehilightPossibleBeams( MapArea.this.focusedSource );
 						MapArea.this.defocusBeamsource();
 					}
 				}
 				MapArea.this.scroll.repaint();
 //				if( MapArea.this.manipSource == null || MapArea.this.isBeamsource( row, col ) )
 //				{
 //					MapArea.this.doHilightPossibleBeams( row, col );
 //				} 
 //				
 //				if( MapArea.this.manipSource != null ) 
 //				{
 //					MapArea.this.doHilightPossibleBeams( row, col );
 //					MapArea.this.prepaintBeams( row, col );
 //				}
 			}
 		}});
 	}
 	
 	public JScrollPane getScrollPane() { return this.scroll; }
 	
 	public void addRow()
 	{
 		this.map.addRow( this.tileset.getSelected() );
 		this.scroll.repaint();
 	}
 	
 	public boolean delRow()
 	{
 		int row	= this.map.rows() - 1;
 		
 		if( row > 0 )
 		{
 			this.map.delRow();
 			this.scroll.repaint();
 			
 			return true;
 		} else
 		{
 			return false;
 		}
 	}
 	
 	public void addCol()
 	{
 		this.map.addCol( this.tileset.getSelected() );
 		this.scroll.repaint();
 	}
 	
 	public boolean delCol()
 	{
 		int col	= this.map.cols() - 1;
 		
 		if( col > 1 )
 		{
 			this.map.delCol();
 			this.scroll.repaint();
 			
 			return true;
 		} else
 		{
 			return false;
 		}
 	}
 	
 	private void paintComponent( Graphics g )
 	{
 		Rectangle r	= g.getClipBounds();
 
 		int startRow	= ( r.y / 32 );
 		int startCol	= ( r.x / 32 ); 
 
 		int endRow		= this.map.rows();
 		int endCol		= this.map.cols();
 		
 		for( int row = startRow; row < endRow; row++ )
 		{
 			for( int col = startCol; col < endCol; col++ )
 			{
 				Tile tile				= this.map.tile( row, col );
 
 				BufferedImage imgTile	= tile.image();
 				int strength			= tile.strength();
 				
 				g.drawImage( imgTile, col * 32, row * 32, this.panel );
 				g.setColor( tile.color() );
 				g.fillRect( ( col * 32 ), ( row * 32 ), 32, 32 );
 				
 				// Ich denke max 999 Beams/Beamsource sollten reichen!
 				// Keine Lust auf Relative Grenermittlung der FontSizes sowie Padding,
 				// Margin, etc. in Relation zum parentTile !!!
 				if( strength > 0 && tile.type() == "beamsource" )
 				{
 					int bStrength	= strength;
 					
 					if( this.map.mode() == TileArray.MODE_PREVIEW )
 					{
 						bStrength	= tile.strength() - tile.consumption();
 					} else
 					{
 						bStrength	= tile.strength();
 					}
 					
 					g.setColor( new Color( 0, 0, 0, 255 ) );
 					
 					if( bStrength < 100 )
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 						
 						if( bStrength < 10 )
 						{
 							g.drawString( bStrength+"", ( col * 32 ) + 11, ( row * 32 ) + 24 );		
 						} else
 						{
 							g.drawString( bStrength+"", ( col * 32 ) + 4, ( row * 32 ) + 24 );
 						}
 					} else
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 12 ) );
 						g.drawString( bStrength+"", ( col * 32 ) + 5, ( row * 32 ) + 21 );
 					}
 				} else if( strength == 0 && tile.type() == "beamsource" )
 				{
 					g.setColor( new Color( 0, 0, 0, 255 ) );
 					g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 //					g.setColor( Color.RED );
 					g.drawString( "!", ( col * 32 ) + 13, ( row * 32 ) + 24 );
 					g.setColor( new Color( 149, 47, 49, 100 ) );
 					g.fillRect( ( col * 32 ) + 2, ( row * 32 ) + 2, 28, 28 );
 				}
 			}
 		}
 	}
 	
 	public void initScrollpane()
 	{
 		//Hier wird nun eine Feste gre des JPanel gesetzt.
 		this.panel.setPreferredSize( new Dimension( this.map.rows() * 32, this.map.cols() * 32 ) );
 		this.scroll.setViewportView( this.panel );
 	}
 	
 	public TileArray getMap() 	{ return this.map; 		} 
 	public String getMapName()	{ return this.mapName; 	}
 	
 	public void setMap( TileArray map, Boolean savegame )			
 	{ 
 		this.map 			= map;
 		
 		ITileState newTile	= null;
 		int rows			= this.map.rows();
 		int cols			= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				String type			= this.map.tile( row, col ).type();
 				
 				if( type.equals( "field" ) )
 				{
 					try 						{ newTile = new TileField(); 		}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 				} else if( type.equals( "beam" ) )
 				{
 					try 						{ newTile = new TileBeam(); 		}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 					
 					if( savegame == false  )	
 					{ 
 						newTile.hidden( true );
 						newTile.image( this.tileset.tile( 1 ).image() );
 					} else
 					{
 						if( type.equals( "beam" ) &&
 							this.map.tile( row, col ).hidden() == true  
 						) {
 							this.map.tile( row, col ).image( this.tileset.tile( 1 ).image() );
 						}
 					}
 				} else if( type.equals( "beamsource" ) )
 				{
 					try 						{ newTile = new TileBeamsource(); 	}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 				}
 				
 				this.map.tile( row, col ).setTileState( newTile );
 			}
 		}
 	}
 	
 	public void setMapName( String mapName )	{ this.mapName = mapName; 	}
 	public void reload()						{ this.scroll.repaint();	}
 	
 	public void initMap( int rows, int cols )
 	{
 		this.map		= new TileArray( rows, cols );
 
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ ) 
 			{
 				this.map.setTile( row, col ).withState( this.tileset.getSelected() );
 			}
 		}
 	}
 	
 	/* Prft, ob sich die Maus im Zeichnen-Bereich der Scrollpane befindet.
 	 * Falls ja, wird geprft, ob das fokusierte Tile manipulierbar ist
 	 * und setzt den Maus-Cursor auf "Hand", falls manipulierbar.
 	 * Manipulierbare Tiles sind: beamsource 
 	 * 
 	 * @param row Zeile, auf dem sich der Mauszeiger befindet
 	 * @param col Spalte, auf dem sich der Mauszeiger befindet
 	 * 
 	 *  @return boolean
 	 */
 	private boolean isInArea( int row, int col )
 	{
 		int maxRow	= MapArea.this.map.rows();
 		int maxCol	= MapArea.this.map.cols();
 		
 		// Prfen, ob sich die Maus im Zeichnen-Bereich befindet: 
 		if( row < maxRow && col < maxCol && row > -1 && col > -1 )
 		{
 			if( this.isBeamsource( row, col ) ) { this.scroll.setCursor( CURSOR_HAND ); 	}
 			else								{ this.scroll.setCursor( CURSOR_DEFAULT ); 	}
 			
 			return true;
 		} else
 		{
 			this.scroll.setCursor( CURSOR_DEFAULT );
 			
 			return false;
 		}
 	}
 	
 	/*
 	 * Prft, ob es sich bei dem Tile um ein Beamsource handelt.
 	 * 
 	 * @param row Zeile, welche es zu prfen gilt
 	 * @param col Spalte, welche es zu prfen gilt
 	 * 
 	 * @return boolean
 	 */
 	private boolean isBeamsource( int row, int col )
 	{
 		return ( this.map.tile( row, col ).type() == "beamsource" )? true : false; 
 	}
 
 	/*
 	 * Prft, ob es sich bei dem Tile um ein Field handelt.
 	 * 
 	 * @param row Zeile, welche es zu prfen gilt
 	 * @param col Spalte, welche es zu prfen gilt
 	 * 
 	 * @return boolean
 	 */
 	private boolean isField( int row, int col )
 	{
 		return ( this.map.tile( row, col ).type() == "field" )? true : false;
 	}
 
 	/*
 	 * 1) Bereitet das Hervorheben mglicher Beams vor (Possible-Beam-Hilighting)
 	 * 2) Fhrt das o.g. Hervorheben aus.
 	 * 
 	 * @param row Zeile, welche es hervorzuheben gilt 
 	 * @param col Spalte, welche es hervorzuheben gilt 
 	 */
 	private void doHilightPossibleBeams( int row, int col )
 	{
 		this.dehilightPossibleBeams( this.map.tile( row, col ) );
 		
 		if( this.isBeamsource( row, col ) == true )
 		{
 			this.map.mode( TileArray.MODE_PREVIEW );
 			this.focusedSource	= this.map.tile( row, col );
 			this.hilightPossibleBeams( this.map.tile( row, col ) );
 		}
 		
 		this.scroll.repaint();
 	}
 
 	
 	/*
 	 * Hebt mgliche Beams blau hervor (Possible-Beam-Hilighting)
 	 * 
 	 * @param beamsource der hervorzuhebenden mglichen Beams
 	 */
 	private void hilightPossibleBeams( Tile source )
 	{
 		// Zeile der Beamsource:
 		int row			= source.row();
 		// Spalte der Beamsource:
 		int col			= source.col();
 		// Erster mglicher Beam von links: 
 		int toLeft		= this.getLeftPossibleBeams( source );
 		// Erster mglicher Beam von oben:
 		int toTop		= this.getTopPossibleBeams( source );
 		// Erster mglicher Beam von rechts:
 		int toRight		= this.getRightPossibleBeams( source );
 		// Erster mglicher Beam von unten:
 		int toBottom	= this.getBottomPossibleBeams( source );
 
 		// Mgliche Beams fr die paintComponent-Methode vorbereiten:
 		for( int cntCol = col; cntCol > toLeft; cntCol-- )
 		{
 			preparePossibleBeam( this.map.tile( row, cntCol ) );
 		}
 		
 		for( int cntRow = row; cntRow > toTop; cntRow-- )
 		{
 			preparePossibleBeam( this.map.tile( cntRow, col ) );
 		}
 		
 		for( int cntCol = col; cntCol < toRight; cntCol++ )
 		{
 			preparePossibleBeam( this.map.tile( row, cntCol ) );
 		}
 		
 		for( int cntRow = row; cntRow < toBottom; cntRow++ )
 		{
 			preparePossibleBeam( this.map.tile( cntRow, col ) );
 		}
 		
 		// Beamsource grn hervorheben:
 //		int fRow	= this.focusedSource.row();
 //		int fCol	= this.focusedSource.col();
 //		
 //		this.map.tile( fRow, fCol ).color( Tile.CGREEN );
 	}
 	
 	/*
 	 * Sofern ein Beamsource per Maus fokussiert wurde, so werden
 	 * alle hervorgehobenen mglichen Beams wieder auf ihren
 	 * Ready-Zustand gesetzt.
 	 */
 	private void dehilightPossibleBeams( Tile source )
 	{
 		int sRow	= source.row();
 		int sCol	= source.col();
 		
 		int mRow	= -1;
 		int mCol	= -1;
 
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		
 		if( this.manipSource != null )
 		{
 			mRow	= this.manipSource.row();
 			mCol	= this.manipSource.col();
 		}
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			if( row != mRow )
 			{
 				this.map.tile( row, sCol ).color( Tile.CTRANSPARENT );
 			}
 		}
 		
 		for( int col = 0; col < cols; col++ )
 		{
 			if( col != mCol )
 			{
 				this.map.tile( sRow, col ).color( Tile.CTRANSPARENT );
 			}
 		}
 	}
 	
 	private int getLeftPossibleBeams( Tile beamsource )
 	{
 		// Source-Strke:
 		int strength	= this.map.cols();
 		// Letzter Beam des Sources von links:
 		int left		= beamsource.col();
 		// Erstes vorkommendes Hindernis:
 		int min_crossed	= -1;
 		// Minumum der Source:
 		int min_source	= -1;
 		// Zeile des Sources:
 		int row			= beamsource.row();
 		// Spalte des Sources:
 		int col			= beamsource.col();
 		// Source-Verbrauch Gesamt:
 		int used		= this.map.filter( "beam", beamsource ).depends( beamsource ).size();
 
 		// 1) Col-Position des ersten Hindernisses
 		// 2) Anzahl der eigenen Beams des Sources von links
 		for( int cntCol = col - 1; cntCol > -1; cntCol-- )
 		{
 			Tile cross	= this.map.tile( row, cntCol );
 			
 			// Col-Position des ersten Hindernisses:
 			if( min_crossed == -1 &&
 				( cross.type() == "beamsource" || 
 				  ( cross.type() == "beam" && cross.hidden() == false &&
 				    cross.parent().row() != row &&
 				    cross.parent().col() != col
 				  )
 				)
 			) {
 				min_crossed = cntCol;
 			}
 			
 			// Anzahl der eigenen Beams des Sources von links:
 			if( cross.type() == "beam" && cross.hidden() == false &&
 				cross.parent() != null && 
 				cross.parent().row() == row &&
 				cross.parent().col() == col
 			) {
 				left = cntCol;
 			}
 		}
 		
 		min_source	= left + used - strength - 1;
 
 		return ( min_source > min_crossed )? min_source : min_crossed;
 	}
 	
 	private int getTopPossibleBeams( Tile beamsource )
 	{
 		// Source-Strke:
 		int strength	= this.map.rows();
 		// Letzter Beam des Sources von oben:
 		int top			= beamsource.row();
 		// Erstes vorkommendes Hindernis:
 		int min_crossed	= -1;
 		// Minumum der Source:
 		int min_source	= -1;
 		// Zeile des Sources:
 		int row			= beamsource.row();
 		// Spalte des Sources:
 		int col			= beamsource.col();
 		// Source-Verbrauch Gesamt:
 		int used		= this.map.filter( "beam", beamsource ).depends( beamsource ).size();
 
 		// 1) Row-Position des ersten Hindernisses
 		// 2) Anzahl der eigenen Beams des Sources von oben
 		for( int cntRow = row - 1; cntRow > -1; cntRow-- )
 		{
 			Tile cross	= this.map.tile( cntRow, col );
 			
 			// Col-Position des ersten Hindernisses:
 			if( min_crossed == -1 &&
 				( cross.type() == "beamsource" || 
 				  ( cross.type() == "beam" && cross.hidden() == false &&
 				    cross.parent().row() != row &&
 				    cross.parent().col() != col
 				  )
 				)
 			) {
 				min_crossed = cntRow;
 			}
 			
 			// Anzahl der eigenen Beams des Sources von links:
 			if( cross.type() == "beam" && cross.hidden() == false &&
 				cross.parent() != null && 
 				cross.parent().row() == row &&
 				cross.parent().col() == col
 			) {
 				top = cntRow;
 			}
 		}
 		
 		min_source	= top + used - strength - 1;
 
 		return ( min_source > min_crossed )? min_source : min_crossed;
 	}
 	
 	private int getRightPossibleBeams( Tile beamsource )
 	{
 		// Kartenbreite:
 		int cols		= this.map.cols();
 		// Source-Strke:
 		int strength	= cols;
 		// Letzter Beam des Sources von rechts:
 		int right		= beamsource.col();
 		// Erstes vorkommendes Hindernis:
 		int max_crossed	= cols;
 		// Minumum der Source:
 		int max_source	= cols;
 		// Zeile des Sources:
 		int row			= beamsource.row();
 		// Spalte des Sources:
 		int col			= beamsource.col();
 		// Source-Verbrauch Gesamt:
 		int used		= this.map.filter( "beam", beamsource ).depends( beamsource ).size();
 
 		// 1) Row-Position des ersten Hindernisses
 		// 2) Anzahl der eigenen Beams des Sources von oben
 		for( int cntCol = col + 1; cntCol < cols; cntCol++ )
 		{
 			Tile cross	= this.map.tile( row, cntCol );
 			
 			// Col-Position des ersten Hindernisses:
 			if( max_crossed == cols &&
 				( cross.type() == "beamsource" || 
 				  ( cross.type() == "beam" && cross.hidden() == false &&
 				    cross.parent().row() != row &&
 				    cross.parent().col() != col
 				  )
 				)
 			) {
 				max_crossed = cntCol;
 			}
 			
 			// Anzahl der eigenen Beams des Sources von links:
 			if( cross.type() == "beam" && cross.hidden() == false &&
 				cross.parent() != null && 
 				cross.parent().row() == row &&
 				cross.parent().col() == col
 			) {
 				right = cntCol;
 			}
 		}
 		
 		max_source	= right + strength - used + 1;
 
 		return ( max_source < max_crossed )? max_source : max_crossed;
 	}
 	
 	private int getBottomPossibleBeams( Tile beamsource )
 	{
 		// Kartenhhe:
 		int rows		= this.map.rows();
 		// Source-Strke:
 		int strength	= this.map.rows();
 		// Letzter Beam des Sources von unten:
 		int bottom		= beamsource.row();
 		// Erstes vorkommendes Hindernis:
 		int max_crossed	= rows;
 		// Minumum der Source:
 		int max_source	= rows;
 		// Zeile des Sources:
 		int row			= beamsource.row();
 		// Spalte des Sources:
 		int col			= beamsource.col();
 		// Source-Verbrauch Gesamt:
 		int used		= this.map.filter( "beam", beamsource ).depends( beamsource ).size();
 
 		// 1) Row-Position des ersten Hindernisses
 		// 2) Anzahl der eigenen Beams des Sources von oben
 		for( int cntRow = row + 1; cntRow < rows; cntRow++ )
 		{
 			Tile cross	= this.map.tile( cntRow, col );
 			
 			// Col-Position des ersten Hindernisses:
 			if( max_crossed == rows &&
 				( cross.type() == "beamsource" || 
 				  ( cross.type() == "beam" && cross.hidden() == false &&
 				    cross.parent().row() != row &&
 				    cross.parent().col() != col
 			      )
 				)
 			) {
 				max_crossed = cntRow;
 			}
 			
 			// Anzahl der eigenen Beams des Sources von links:
 			if( cross.type() == "beam" && cross.hidden() == false &&
 				cross.parent() != null && 
 				cross.parent().row() == row &&
 				cross.parent().col() == col
 			) {
 				bottom = cntRow;
 			}
 		}
 
 		max_source	= bottom + strength - used + 1;
 
 		return ( max_source < max_crossed )? max_source : max_crossed;
 	}
 	
 	private void preparePossibleBeam( Tile pBeam )
 	{
 		if( pBeam.type() == "field" )
 		{
 			pBeam.color( Tile.CBLUE );
 			this.m.addDirtyRegion( this.scroll, pBeam.row() * 32, pBeam.col() * 32, 32, 32 );
 		} else if( pBeam.type() == "beam" )
 		{
 			pBeam.color( Tile.CBLUE );
 			this.m.addDirtyRegion( this.scroll, pBeam.row() * 32, pBeam.col() * 32, 32, 32 );
 		}
 	}
 	
 	private void prepaintBeams( int mouseRow, int mouseCol )
 	{
 		int mRow		= this.manipSource.row();
 		int mCol		= this.manipSource.col();
 		
 		int toLeft		= this.getLeftPossibleBeams( this.manipSource );
 		int toTop		= this.getTopPossibleBeams( this.manipSource );
 		int toRight		= this.getRightPossibleBeams( this.manipSource );
 		int toBottom	= this.getBottomPossibleBeams( this.manipSource );
 		
 		this.clearPrepaintedBeams();
 
 		if( mouseRow == mRow && mouseCol > toLeft && mouseCol < toRight )
 		{
 			if( mouseCol < mCol )
 			{
 				this.scroll.setCursor( MapArea.CURSOR_HAND );
 				this.clearHorizontalBeams( TileArray.TILE_FROM_LEFT );
 				this.paintHorizontalBeams( mouseCol, mCol, mRow );
 			} else if( mouseCol > mCol )
 			{
 				this.scroll.setCursor( MapArea.CURSOR_HAND );
 				this.clearHorizontalBeams( TileArray.TILE_FROM_RIGHT );
 				this.paintHorizontalBeams( mCol + 1, mouseCol + 1, mRow );
 			}
 		} else if( mouseCol == mCol && mouseRow > toTop && mouseRow < toBottom )
 		{
 			if( mouseRow < mRow )
 			{
 				this.scroll.setCursor( MapArea.CURSOR_HAND );
 				this.clearVerticalBeams( TileArray.TILE_FROM_TOP );
 				this.paintVerticalBeams( mouseRow, mRow, mCol );
 			} else if( mouseRow > mRow )
 			{
 				this.scroll.setCursor( MapArea.CURSOR_HAND );
 				this.clearVerticalBeams( TileArray.TILE_FROM_BOTTOM );
 				this.paintVerticalBeams( mRow + 1, mouseRow + 1, mCol );
 			}
 		}
 		
 		int consumption	= this.map.filter( "beam", this.manipSource ).depends( this.manipSource ).size();
 
 		if( consumption == this.manipSource.strength() )	{ this.manipSource.color( Tile.CBLUE ); 	}
 		else												{ this.manipSource.color( Tile.CYELLOW );	}
 		
 		this.manipSource.consumption( consumption );		
 	}
 
 	private void clearPrepaintedBeams()
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= this.map.tile( row, col );
 				
 				if( tile.parent() != null && tile.parent().equals( this.manipSource ) )
 				{
 					this.map.setTile( row, col ).withState( this.tileset.tile( 1 ) );
 				}
 			}
 		}
 		
 		this.scroll.repaint();
 	}
 	
 	private void clearHorizontalBeams( int posFrom )
 	{
 		if( this.manipSource != null )
 		{
 			int mCol	= this.manipSource.col();
 			int inRow	= this.manipSource.row();
 
 			if( posFrom == TileArray.TILE_FROM_LEFT )
 			{
 				for( int col = 0; col < mCol; col++ )
 				{
 					Tile tile	= this.map.tile( inRow, col );
 					
 					if( tile.type() == "beam" &&
 						tile.hidden() == false &&
 						tile.parent() != null && 
 						tile.parent().row() == this.manipSource.row() &&
 						tile.parent().col()	== this.manipSource.col()
 					) {
 						this.map.setTile( inRow, col ).withState( this.tileset.tile( 1 ) );
 					}
 				}
 			} else if( posFrom == TileArray.TILE_FROM_RIGHT )
 			{
 				int cols	= this.map.cols();
 				
 				for( int col = mCol + 1; col < cols; col++ )
 				{
 					Tile tile	= this.map.tile( inRow, col );
 					
 					if( tile.type() == "beam" &&
 						tile.hidden() == false &&
 						tile.parent() != null && 
 						tile.parent().row() == this.manipSource.row() &&
 						tile.parent().col()	== this.manipSource.col()
 					) {
 						this.map.setTile( inRow, col ).withState( this.tileset.tile( 1 ) );
 					}
 				}
 			}
 		}
 	}
 
 	private void clearVerticalBeams( int posFrom )
 	{
 		if( this.manipSource != null )
 		{
 			int mRow	= this.manipSource.row();
 			int inCol	= this.manipSource.col();
 
 			if( posFrom == TileArray.TILE_FROM_TOP )
 			{
 				for( int row = 0; row < mRow; row++ )
 				{
 					Tile tile	= this.map.tile( row, inCol );
 					
 					if( tile.type() == "beam" &&
 						tile.hidden() == false &&
 						tile.parent() != null && 
 						tile.parent().row() == this.manipSource.row() &&
 						tile.parent().col()	== this.manipSource.col()
 					) {
 						this.map.setTile( row, inCol ).withState( this.tileset.tile( 1 ) );
 					}
 				}
 			} else if( posFrom == TileArray.TILE_FROM_BOTTOM )
 			{
 				int rows	= this.map.rows();
 				
 				for( int row = mRow + 1; row < rows; row++ )
 				{
 					Tile tile	= this.map.tile( row, inCol );
 					
 					if( tile.type() == "beam" &&
 						tile.hidden() == false &&
 						tile.parent() != null && 
 						tile.parent().row() == this.manipSource.row() &&
 						tile.parent().col()	== this.manipSource.col()
 					) {
 						this.map.setTile( row, inCol ).withState( this.tileset.tile( 1 ) );
 					}
 				}
 			}
 		}
 	}
 	
 	private void paintHorizontalBeams( int fromCol, int toCol, int inRow )
 	{
 		for( int col = fromCol; col < toCol; col++ )
 		{
 			this.map.setTile( inRow, col ).withState( this.tileset.tile( 2 ) );
 			this.map.tile( inRow, col ).parent( this.manipSource );
 			this.map.tile( inRow, col ).color( Tile.CBLUE );
 		}
 	}
 	
 	private void paintVerticalBeams( int fromRow, int toRow, int inCol )
 	{
 		for( int row = fromRow; row < toRow; row++ )
 		{
 			this.map.setTile( row, inCol ).withState( this.tileset.tile( 2 ) );
 			this.map.tile( row, inCol ).parent( this.manipSource );
 			this.map.tile( row, inCol ).color( Tile.CBLUE );
 		}
 	}
 	
 	private void assignBeamsToSource()
 	{
 		ArrayList<Tile> diffs	= this.map.diff();
 		int lenDiff				= diffs.size();
 		
 		for( int cntDiff = 0; cntDiff < lenDiff; cntDiff++ )
 		{
 			Tile diff	= diffs.get( cntDiff );
 			
 			if( diff.type() != "beamsource" )
 			{
 				diff.color( Tile.CTRANSPARENT );
 				this.map.pushReady( diff, diff.row(), diff.col() );
 			}
 		}
 	}
 	
 	private void drawBeamsource( int row, int col )
 	{
 		this.map.setTile( row, col ).withState( this.tileset.tile( 0 ) );
 		
 		this.editBeamsource( row, col );
 		
 	}
 	
 	private void editBeamsource( int row, int col )
 	{
 		this.manipSource	= this.map.tile( row, col );
 		int mRow			= this.manipSource.row();
 		int mCol			= this.manipSource.col();
 		
 		this.enterPreviewMode();
 		this.map.tile( mRow, mCol ).color( Tile.CYELLOW );
 		this.hilightPossibleBeams( this.manipSource );
 	}
 	
 	private void saveBeamsource()
 	{
 		this.dehilightPossibleBeams( this.manipSource );
 		this.map.tile( this.manipSource.row(), this.manipSource.col() ).color( Tile.CTRANSPARENT );
 	}
 	
 	private void focusBeamsource( int row, int col )
 	{
 		this.focusedSource = MapArea.this.map.tile( row, col );
 		this.map.tile( row, col ).color( Tile.CGREEN );
 	}
 	
 	private void defocusBeamsource()
 	{
 		this.map.tile( this.focusedSource.row(), this.focusedSource.col() ).color( Tile.CTRANSPARENT );
 		this.focusedSource	= null;
 	}
 	
 	private void enterPreviewMode()	{ this.map.mode( TileArray.MODE_PREVIEW ); 	}
 	private void leavePreviewMode()	{ this.map.mode( TileArray.MODE_READY );	}
 }
