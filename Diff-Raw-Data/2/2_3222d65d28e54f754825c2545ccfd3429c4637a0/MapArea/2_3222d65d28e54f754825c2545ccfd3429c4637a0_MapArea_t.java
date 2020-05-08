 package lightbeam.editor;
 
 import java.awt.Color;
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
 	private static final long serialVersionUID = 2L;
 	private AbstractTileSetFactory tileset		= null;
 	private JPanel panel						= null;
 	private JScrollPane scroll					= new JScrollPane();
 	
 	private RepaintManager m					= null;
 	private TileArray map						= null;
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
 		
 		scroll.setViewportView( this.panel );
 		this.panel.setDoubleBuffered( true );
 		this.initScrollpane();
 		
 		this.m	= RepaintManager.currentManager( this.panel );
 		
 		this.panel.addMouseListener(new MouseAdapter(){public void mouseClicked( MouseEvent e ) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
 			
 			String focusedType	= MapArea.this.setFocused( row, col );
 			String selectedType	= MapArea.this.tileset.getSelected().type();
 
 			if( "beam" == focusedType && selectedType != "field" )
 			{
 				Tile beamsource 	= MapArea.this.map.tile( row, col ).beamsource();
 
 				MapArea.this.highlightBeams( beamsource.row(), beamsource.col() );
 			} else
 			{
 				MapArea.this.updateTile( row, col );
 			}
 		}});
 		
 		this.panel.addMouseMotionListener(new MouseMotionAdapter(){public void mouseDragged(MouseEvent e) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
 			
 			MapArea.this.setFocused( row, col );
 			MapArea.this.updateTile( row, col );
 		}});
 		
 		this.panel.addMouseMotionListener(new MouseMotionAdapter(){public void mouseMoved( MouseEvent e ) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
 			
 			String focusedType	= MapArea.this.setFocused( row, col );
 
 			if( focusedType == "beam" || focusedType == "beamsource" )
 			{
 				Tile beamsource	= MapArea.this.map.tile( row, col );
 				
 				if( focusedType == "beam" )
 				{
 					beamsource 	= beamsource.beamsource();
 				}
 				
 				MapArea.this.highlightBeams( beamsource.row(), beamsource.col() );
 				MapArea.this.highlightBeamsource( beamsource );
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
 			this.removeBeamsources( row, Tile.HORIZONTAL );
 			this.map.delRow();
 			this.updateBeamsources();
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
 			this.removeBeamsources( col, Tile.VERTICAL );
 			this.map.delCol();
 			this.updateBeamsources();
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
 				
 				if( tile.focused() == true )
 				{
 					if( tile.type() == "beam" )
 					{
 						g.setColor( new Color( 255, 0, 0, 135 ) );
 					} else if( tile.type() == "beamsource" )
 					{
 						g.setColor( new Color( 3, 115, 210, 135 ) );
 					}
 					
 					g.fillRect( ( col * 32 ) + 2, ( row * 32 ) + 2, 28, 28 );
 				}
 				
 				// Ich denke max 999 Beams/Beamsource sollten reichen!
 				// Keine Lust auf Relative Grenermittlung der FontSizes sowie Padding,
 				// Margin, etc. in Relation zum parentTile !!!
 				if( strength > 0 && tile.type() == "beamsource" )
 				{
 					g.setColor( new Color( 0, 0, 0, 255 ) );
 					
 					if( strength < 100 )
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 						
 						if( strength < 10 )
 						{
 							g.drawString( strength+"", ( col * 32 ) + 11, ( row * 32 ) + 24 );		
 						} else
 						{
 							g.drawString( strength+"", ( col * 32 ) + 4, ( row * 32 ) + 24 );		
 						}
 					} else
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 12 ) );
 						g.drawString( strength+"", ( col * 32 ) + 5, ( row * 32 ) + 21 );
 					}
 				} else if( strength == 0 && tile.type() == "beamsource" )
 				{
 					g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 					g.setColor( Color.RED );
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
 	
 	public void setMap( TileArray map )			
 	{ 
 		this.map = map;
 		
 		int rows = map.rows();
 		int cols = map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				String type			= this.map.tile(row, col).type();
 				ITileState newTile	= null;
 
 				if( type.equals( "field" ) )
 				{
 					try 					{ newTile = new TileField(); 		}
 					catch (IOException e) 	{ e.printStackTrace();				}
 				} else if( type.equals( "beam" ) )
 				{
 					try 					{ newTile = new TileBeam(); 		}
 					catch (IOException e) 	{ e.printStackTrace();				}
 				} else if( type.equals( "beamsource" ) )
 				{
 					try 					{ newTile = new TileBeamsource();	}
 					catch (IOException e) 	{ e.printStackTrace();				}
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
				this.map.setTile( row, col ).withState( this.tileset.tile( 1 ) );
 			}
 		}
 	}
 	
 	public void resetMap( int rows, int cols )
 	{
 		this.initMap( rows, cols );
 
 		this.scroll.repaint();
 	}
 	
 	private void drawTile( int row, int col )
 	{
 		int[] area			= getTileArea( row, col );
 		
 		int acol			= area[0];
 		int arow			= area[1];
 		ITileState selected	= this.tileset.getSelected();
 		String type			= this.map.tile( row, col ).type();
 		String selType		= selected.type(); 
 
 		if( type != "beam" || selType == "field" )
 		{
 			this.map.setTile( row, col ).withState( selected );
 			
 			if( acol > -1 && arow > -1 )
 			{
 				this.m.addDirtyRegion( this.panel , acol, arow, 32, 32 );
 			}
 		}
 	}
 	
 	private void updateTile( int row, int col )
 	{
 		if( isInArea( row, col ) == true )
 		{
 			Tile trigger	= this.map.tile( row, col );
 			Tile beamsource	= getParent( row, col );
 			
 			if( beamsource == null || trigger.type() == "beamsource" )
 			{
 				if( this.tileset.getSelected().type() == "field" )
 				{
 					removeBeams( trigger );
 				}
 				
 				drawTile( row, col );					
 				
 				if( isBeamsource( row, col ) == true )
 				{
 					setBeams( row, col );
 					highlightBeams( row, col );
 					highlightBeamsource( trigger );
 					
 					m.addDirtyRegion( this.scroll, row * 32, col * 32, 32, 32 );
 				}
 			} else if( this.tileset.getSelected().type() == "field" )
 			{
 				int bRow	= beamsource.row();
 				
 				if( row == bRow )
 				{
 					removeBeams( beamsource, trigger, Tile.HORIZONTAL, col );
 				} else
 				{
 					removeBeams( beamsource, trigger, Tile.VERTICAL, row );
 				}
 			}
 			
 			this.scroll.repaint();
 		}
 	}
 	
 	private void removeBeams( Tile source, Tile trigger, int axis, int posMouse )
 	{
 		ArrayList<Tile> bTiles		= this.map.filter( "beam", source ).depends( source );
 		ArrayList<Tile> foundTiles	= this.map.find( axis, bTiles );
 		
 		int remFrom, posSrc;
 		
 		if( axis == Tile.HORIZONTAL )
 		{
 			posSrc	= source.col();
 			remFrom = ( trigger.col() < posSrc )? Tile.LEFT : Tile.RIGHT;
 		} else
 		{
 			posSrc	= source.row();
 			remFrom	= ( trigger.row() < posSrc )? Tile.TOP: Tile.BOTTOM;
 		}
 		
 		ArrayList<Tile> depTiles	= this.map.removeFrom( posSrc, remFrom, foundTiles );
 		
 		int fLen					= depTiles.size(); 
 
 		for( int cntTile = 0; cntTile < fLen; cntTile++ )
 		{
 			Tile tile 	= depTiles.get( cntTile );
 			int row		= tile.row();
 			int col		= tile.col();
 			
 			try 
 			{
 				this.map.setTile( row, col ).withState( new TileField() );
 				
 				m.addDirtyRegion( this.scroll, col * 32, row * 32, 32, 32 );
 			} catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		int row			= trigger.row();
 		int col			= trigger.col();
 		int strength	= this.map.filter( "beam", source ).depends( source ).size();
 		
 		source.strength( strength );
 		
 		m.addDirtyRegion( this.scroll, col * 32, row * 32, 32, 32 );
 		
 		this.scroll.repaint(); 
 	}
 	
 	private void removeBeams( Tile beamsource )
 	{
 		ArrayList<Tile> bTiles		= this.map.filter( "beam", beamsource ).depends( beamsource );
 		int bLen					= bTiles.size(); 
 
 		for( int cntTile = 0; cntTile < bLen; cntTile++ )
 		{
 			Tile tile 	= bTiles.get( cntTile );
 			int row		= tile.row();
 			int col		= tile.col();
 			
 			try 
 			{
 				this.map.setTile( row, col ).withState( new TileField() );
 				
 				m.addDirtyRegion( this.scroll, col * 32, row * 32, 32, 32 );
 			} catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private Tile getParent( int row, int col )
 	{
 		Tile tile	= this.map.tile( row, col );
 		String type	= tile.type();
 		
 		if( type == "beam" || type == "beamsource" )
 		{
 			if( type == "beam" ) 	{ return tile.beamsource(); }
 			else					{ return tile;				}
 		} else
 		{
 			return null;
 		}
 	}
 	
 	private void highlightBeamsource( Tile beamsource )
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		int bRow	= beamsource.row();
 		int bCol	= beamsource.col();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= this.map.tile( row, col );
 			
 				if( tile.type() == "beamsource" ) 
 				{ 
 					tile.focus( false );
 					m.addDirtyRegion( this.scroll, row * 32, col * 32, 32, 32 );
 				}
 			}
 		}
 		
 		m.addDirtyRegion( this.scroll, bRow * 32, bCol * 32, 32, 32 );
 		
 		this.map.tile( bRow, bCol ).focus( true );
 	}
 	
 	private void highlightBeams( int row, int col )
 	{
 		Tile beamsource			= this.map.tile( row, col );
 		
 		ArrayList<Tile> beams	= this.map.filter( "beam", beamsource ).depends( beamsource );
 		int lenBeams			= beams.size();
 		
 		int rows			= this.map.rows();
 		int cols			= this.map.cols();
 
 		for( int cntRow = 0; cntRow < rows; cntRow++ )
 		{
 			for( int cntCol = 0; cntCol < cols; cntCol++ )
 			{
 				this.map.tile( cntRow, cntCol ).focus( false );
 			}
 		}
 
 		for( int beam = 0; beam < lenBeams; beam++ )
 		{
 			Tile bBeam	= beams.get( beam );
 			int bRow	= bBeam.row();
 			int bCol	= bBeam.col();
 			
 			this.map.tile( bRow, bCol ).focus( true );
 		}
 
 		this.scroll.repaint();
 	}
 	
 	private void setBeams( int row, int col )
 	{
 //		this.oldMap			= this.map;
 		
 		Tile beamsource		= this.map.tile( row, col );
 		int beamstrength	= 0;
 
 		int rows			= this.map.rows();
 		int cols			= this.map.cols();
 		
 		int startRow		= 0;
 		int startCol		= 0;
 
 		int endRow			= rows;
 		int endCol			= cols;
 
 		for( int cntCol = 0; cntCol < cols; cntCol++ )
 		{
 			Tile tile	= this.map.tile( row, cntCol );
 
 			if( cntCol < col )
 			{
 				if( ( tile.type() == "beamsource" || tile.type() == "beam" ) ) {
 					startCol	= tile.col() + 1;
 				}
 			} else if( cntCol > col )
 			{
 				if( ( tile.type() == "beamsource" || tile.type() == "beam" ) &&
 					cntCol < endCol
 				) {
 					endCol		= tile.col();
 				}
 			}
 		}
 
 		for( int cntRow = 0; cntRow < rows; cntRow++ )
 		{
 			Tile tile	= this.map.tile( cntRow, col );
 			
 			if( cntRow < row )
 			{
 				if( ( tile.type() == "beamsource" || tile.type() == "beam" ) )
 				{
 					startRow	= tile.row() + 1;
 				} 
 			} else if( cntRow > row )
 			{
 				if( ( tile.type() == "beamsource" || tile.type() == "beam" ) &&
 					cntRow < endRow
 				) {
 					endRow	= tile.row();
 				}
 			}
 		}
 
 		for( int cntRow = startRow; cntRow < endRow; cntRow++ )
 		{
 			if( cntRow != row )
 			{
 				try 
 				{
 					this.map.setTile( cntRow, col ).withState( new TileBeam(), beamsource );
 					this.map.tile( cntRow, col ).setDirection( Tile.VERTICAL );
 				}
 				catch (IOException e) { e.printStackTrace(); }
 			}
 		}
 
 		if( startCol < endCol )
 		{
 			for( int cntCol = startCol; cntCol < endCol; cntCol++ )
 			{
 				if( cntCol != col )
 				{
 					try 
 					{ 
 						this.map.setTile( row, cntCol ).withState( new TileBeam(), beamsource );
 						this.map.tile( row, cntCol ).setDirection( Tile.HORIZONTAL );
 					} catch (IOException e) 
 					{ 
 						e.printStackTrace(); 
 					}
 				}
 			}
 		}
 		
 		beamstrength	= this.map.filter( "beam", beamsource ).depends( beamsource ).size();
 		
 		if( beamstrength > 0 )
 		{
 			this.map.tile( row, col ).strength( beamstrength );
 		}
 	}
 	
 	private boolean isInArea( int row, int col )
 	{
 		int maxRow	= MapArea.this.map.rows();
 		int maxCol	= MapArea.this.map.cols();
 		
 		return ( row < maxRow && col < maxCol && row > -1 && col > -1 )? true : false;
 	}
 	
 	private int[] getTileArea( int row, int col )
 	{
 		int[] ret	= new int[2];
 		
 		ret[0]		= -1;
 		ret[1]		= -1;
 		
 		int maxy	= this.map.rows();
 		int maxx	= this.map.cols();
 		
 		if( col > -1 && col < maxx && 
 			row > -1 && row < maxy
 		) {
 			Rectangle r		= this.scroll.getViewport().getViewRect();
 
 			ret[0]			= this.scroll.getLocation().x + this.panel.getInsets().left - r.x + ( col * 32 );
 			ret[1]			= this.scroll.getLocation().y + this.panel.getInsets().top - r.y + ( row * 32 );
 
 			return ret; 
 		} else
 		{
 			return ret;
 		}
 	}
 	
 	private boolean isBeamsource( int row, int col )
 	{
 		Tile seltile	= this.map.tile( row, col );
 
 		if( seltile.type() == "beamsource" )
 		{
 			return true;
 		} else
 		{
 			return false;
 		}
 	}
 	
 	private String setFocused( int row, int col )
 	{
 		if( this.isInArea( row, col ) )
 		{
 			return this.map.tile( row, col ).type();
 		} else
 		{
 			return null;
 		}
 	}
 	
 	private void removeBeamsources( int pos, int axis )
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 				
 		if( axis == Tile.HORIZONTAL )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile src	= this.map.tile( pos, col );
 				
 				if( src.type() == "beamsource" )
 				{
 					removeBeams( src );
 					try 
 					{
 						this.map.setTile( pos, col ).withState( new TileField() );
 					} catch( IOException e ) 
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		} else if( axis == Tile.VERTICAL )
 		{
 			for( int row = 0; row < rows; row++ )
 			{
 				Tile src	= this.map.tile( row, pos );
 				
 				if( src.type() == "beamsource" )
 				{
 					removeBeams( src );
 					try 
 					{
 						this.map.setTile( row, pos ).withState( new TileField() );
 					} catch( IOException e ) 
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	private void updateBeamsources()
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile source	= this.map.tile( row, col );
 				
 				if( source.type() == "beamsource" )
 				{
 					ArrayList<Tile> beams	= this.map.filter( "beam", source ).depends( source );
 					
 					source.strength( beams.size() );
 				}
 			}
 		}
 	}
 }
