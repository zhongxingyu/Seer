 package lightbeam.solution.strategies;
 
 import java.util.ArrayList;
 import lightbeam.solution.ILogicStrategy;
 import core.tilestate.Tile;
 import core.tilestate.TileArray;
 
 public class ConclusiveReachable implements ILogicStrategy
 {
 	private TileArray map						= null;
 	private boolean result						= false;
 	private ArrayList<int[][]> fieldToSource	= null;
 	private int[][] fields						= null;
 	
 	public ConclusiveReachable() { this.result = true; }
 
 	public void execute() 
 	{
 		this.result					= false;
 		// Alle Beamsources in der map holen:
 		ArrayList<Tile> beamsources	= this.map.getTilesInArea( "beamsource" );
 		
 		// Wenn vorhanden:
 		if( beamsources != null )
 		{
 			int sSources			= beamsources.size();
 			
 			this.fieldToSource		= new ArrayList<int[][]>();
 			this.fields				= new int[this.map.rows()][this.map.cols()];
 			
 			// Alle Beamsources durchlaufen:
 			for( int i = 0; i < sSources; i++ )
 			{
 				Tile source		= beamsources.get(i);
 				int strenght	= source.strength();
 				int sRow		= source.row();
 				int sCol		= source.col();
 				
 				// Wenn Beamsource-Strke > 0:
 				if( strenght > 0 )
 				{
 					int pos[][]		= new int[4][2];
 					
 					// Alle mglichen Feld-Positionen (nur Spalten) von links der Source
 					// speichern:
 					pos[0][0]		= sRow;
 					pos[0][1]		= this.getTillLastLeftField( source );
 					
 					// Alle mglichen Feld-Positionen (nur Zeilen) oberhalb der Source
 					// speichern:
 					pos[1][0]		= this.getTillLastTopField( source );
 					pos[1][1]		= sCol;
 					
 					// Alle mglichen Feld-Positionen (nur Spalten) rechts der Source
 					// speichern:
 					pos[2][0]		= sRow;
 					pos[2][1]		= this.getTillLastRightField( source );
 					
 					// Alle mglichen Feld-Positionen (nur Zeilen) unterhalb der Source
 					// speichern:
 					pos[3][0]		= this.getTillLastBottomField( source );
 					pos[3][1]		= sCol;
 					
 					// Mgliche eindeutige Felder der Beamsource zuordnen:
 					this.fieldToSource.add( i, pos );
//if( pos[0][1] == 10 )
//{
//	System.out.println("left" + " -> " + sRow + "|" + sCol );
//} 
//if( pos[1][0] == 10 )
//{
//	System.out.println("top" + " -> " + sRow + "|" + sCol );
//} 
//if( pos[2][1] == 10 )
//{
//	System.out.println("right" + " -> " + sRow + "|" + sCol );
//} 
//if( pos[3][0] == 10 )
//{
//	System.out.println("bottom" + " -> " + sRow + "|" + sCol );
//} 

 					// Hufigkeit der Feldbenutzung bzgl. der mglichen Beamsources hochzhlen, falls
 					// letzmgliche Felder noch nicht zugeordnet:
 					if( pos[0][1] > -1 && this.isField( sRow, pos[0][1] ) ) { this.fields[sRow][pos[0][1]]++; }
 					if( pos[1][0] > -1 && this.isField( pos[1][0], sCol ) ) { this.fields[pos[1][0]][sCol]++; }
 					if( pos[2][1] > -1 && this.isField( sRow, pos[2][1] ) ) { this.fields[sRow][pos[2][1]]++; }
 					if( pos[3][0] > -1 && this.isField( pos[3][0], sCol ) ) { this.fields[pos[3][0]][sCol]++; }
 				} else
 				{
 					this.fieldToSource.add( i, null );
 				}
 			}
 			
 			// Alle Beamsources durchlaufen:
 			for( int i = 0; i < sSources; i++ )
 			{
 				// Beamsource holen:
 				Tile source			= beamsources.get(i);
 				
 				if( source.strength() > 0 )
 				{
 					// Alle letztmglichen Felder einer Beamsource holen:
 					int[][] fieldsPos	= this.fieldToSource.get( i );
 					
 					if( fieldsPos != null )
 					{
 						// Prfen, ob letztmgliches Feld von links der Beamsource eindeutig
 						// zuordenbar ist. Dabei ist:
 						// [0][0] = row,
 						// [0][1] = col
 						if( fieldsPos[0][0] > -1 && fieldsPos[0][1] > -1 
 							&& this.fields[fieldsPos[0][0]][fieldsPos[0][1]] == 1 
 						) {
 							this.assignFieldsToSource( fieldsPos[0][0], fieldsPos[0][1], source );
 		
 							if( this.result == false ) { this.result = true; }
 						}
 						
 						// Prfen, ob letztmgliches Feld oberhalb der Beamsource eindeutig
 						// zuordenbar ist. Dabei ist:
 						// [1][0] = row,
 						// [1][1] = col
 						if( fieldsPos[1][0] > -1 && fieldsPos[1][1] > -1 && 
 							this.fields[fieldsPos[1][0]][fieldsPos[1][1]] == 1 
 						) {
 							this.assignFieldsToSource( fieldsPos[1][0], fieldsPos[1][1], source );
 							
 							if( this.result == false ) { this.result = true; }
 						}
 						
 						// Prfen, ob letztmgliches Feld rechts von der Beamsource eindeutig
 						// zuordenbar ist. Dabei ist:
 						// [2][0] = row,
 						// [2][1] = col
 						if( fieldsPos[2][0] > -1 && fieldsPos[2][1] > -1 &&
 							this.fields[fieldsPos[2][0]][fieldsPos[2][1]] == 1 
 						) {
 							this.assignFieldsToSource( fieldsPos[2][0], fieldsPos[2][1], source );
 							
 							if( this.result == false ) { this.result = true; }
 						}
 						
 						// Prfen, ob letztmgliches Feld unterhalb der Beamsource eindeutig
 						// zuordenbar ist. Dabei ist:
 						// [3][0] = row,
 						// [3][1] = col
 						if( fieldsPos[3][0] > -1 && fieldsPos[3][1] > -1 &&
 							this.fields[fieldsPos[3][0]][fieldsPos[3][1]] == 1 
 						) {
 							this.assignFieldsToSource( fieldsPos[3][0], fieldsPos[3][1], source );
 							
 							if( this.result == false ) { this.result = true; }
 						}
 					}
 				}
 			}
 		} 
 	}
 	
 	public void setMap( TileArray map )	{ this.map = map;		}	
 	public TileArray getMap()			{ return this.map; 		}
 	public boolean getResult() 			{ return this.result;	}
 
 	// Letztmgliches "Feld" links von der Beamsource zurckgeben:
 	private int getTillLastLeftField( Tile source )
 	{
 		int strength					= source.strength();
 		int sCol						= source.col();
 		int leftLastPos					= -1;
 		
 		// Prfen, ob links von Beamsource weiteres Beamsource liegt:
 		Tile leftBlock	= this.map.getLeftBlocking( source, "beamsource" );
 		
 		// Wenn nein, dann:
 		if( leftBlock == null )
 		{
 			leftLastPos	= ( sCol - strength > -1 )? sCol - strength : 0; 	
 		} 
 		// Ansonsten:
 		else 
 		{
 			leftLastPos	= ( sCol - strength > leftBlock.col() )? ( sCol - strength ) : leftBlock.col() + 1;
 		}
 		
 		// Wenn gefunden, dann Tile-Col-Positions speichern:
 		if( leftLastPos > -1 && leftLastPos != source.col() ) 
 		{
 			return leftLastPos;
 		}
 		
 		// "Nicht gefunden" zurckgeben:
 		return -1;
 	}
 	
 	// Letztmgliches "Feld" oben von der Beamsource zurckgeben:
 	private int getTillLastTopField( Tile source )
 	{
 		int strength					= source.strength();
 		int sRow						= source.row();
 		int topLastPos					= -1;
 		
 		// Prfen, ob links von Beamsource weiteres Beamsource liegt:
 		Tile topBlock	= this.map.getTopBlocking( source, "beamsource" );
 		
 		// Wenn nein, dann:
 		if( topBlock == null )
 		{
 			topLastPos	= ( sRow - strength > -1 )? sRow - strength : 0; 	
 		} 
 		// Ansonsten:
 		else 
 		{
			topLastPos	= ( sRow - strength > topBlock.row() )? ( sRow - strength ) : topBlock.row() + 1;
 		}
 		
 		// Wenn gefunden, dann Tile-Row-Positions speichern:
 		if( topLastPos > -1 && topLastPos != source.row() ) 
 		{ 
 			return topLastPos;
 		}
 		
		
		
 		// "Nicht gefunden" zurckgeben:
 		return -1;
 	}
 	
 	// Letztmgliches "Feld" rechts von der Beamsource zurckgeben:
 	private int getTillLastRightField( Tile source )
 	{
 		int strength					= source.strength();
 		int sCol						= source.col();
 		int cols						= this.map.cols();
 		int rightLastPos				= -1;
 		
 		// Prfen, ob links von Beamsource weiteres Beamsource liegt:
 		Tile rightBlock		= this.map.getRightBlocking( source, "beamsource" );
 
 		// Wenn nein, dann:
 		if( rightBlock == null )
 		{
 			rightLastPos	= ( sCol + strength < cols )? sCol + strength : cols - 1;
 		} 
 		// Ansonsten:
 		else 
 		{
 			rightLastPos	= ( sCol + strength < rightBlock.col() )? ( sCol + strength ) : rightBlock.col() - 1;
 		}
 		
 		// Wenn gefunden, dann Tile-Col-Posiiton zurckgeben:
 		if( rightLastPos > -1 && rightLastPos != source.col() ) 
 		{
 			return rightLastPos;
 		}
 		
 		// "Nicht gefunden" zurckgeben:
 		return -1;
 	}
 	
 	// Letztmgliches "Feld" unten von der Beamsource zurckgeben:
 	private int getTillLastBottomField( Tile source )
 	{
 		int strength					= source.strength();
 		int sRow						= source.row();
 		int rows						= this.map.rows();
 		int bottomLastPos				= -1;
 		
 		// Prfen, ob links von Beamsource weiteres Beamsource liegt:
 		Tile bottomBlock	= this.map.getBottomBlocking( source, "beamsource" );
 		
 		// Wenn nein, dann:
 		if( bottomBlock == null )
 		{
 			bottomLastPos	= ( sRow + strength < rows )? sRow + strength : rows - 1;
 		} 
 		// Ansonsten:
 		else 
 		{
 			bottomLastPos	= ( sRow + strength < bottomBlock.row() )? ( sRow + strength ) : bottomBlock.row() - 1;
 		}
 		
 		// Wenn gefunden, dann Tile-Row-Position speichern:
 		if( bottomLastPos > -1 && bottomLastPos != source.row() ) 
 		{ 
 			return bottomLastPos;
 		}
 		
 		// "Nicht gefunden" zurckgeben:
 		return -1;
 	}
 	
 	private void assignFieldsToSource( int fRow, int fCol, Tile source )
 	{
 		int sRow	= source.row();
 		int sCol	= source.col();
 		
 		// Prfen, ob Felder auf horizontaler Ebene zugeordnet werden sollen:
 		if( sRow == fRow )
 		{
 			// Prfen, ob Felder links von der Beamsource zugeordnet werden sollen:
 			if( fCol < sCol )
 			{
 				for( int col = fCol; col < sCol; col++ )
 				{
 					this.map.tile( sRow, col ).parent( source );
 					this.map.tile( sRow, col).type( "beam" );
 				}
 				
 				// Beamstrength fr den nchsten Durchlauf vermindern:
 				this.map.tile( sRow, sCol ).strength( this.map.tile( sRow, sCol ).strength() - ( sCol - fCol ) );
 			}
 			// Nein, Felder sollen rechts von der Beamsource zugeordnet werden:
 			else
 			{
 				for( int col = fCol; col > sCol; col-- )
 				{
 					this.map.tile( sRow, col ).parent( source );
 					this.map.tile( sRow, col).type( "beam" );
 				}
 				
 				// Beamstrength fr den nchsten Durchlauf vermindern:				
 				this.map.tile( sRow, sCol ).strength( this.map.tile( sRow, sCol ).strength() - ( fCol - sCol ) );
 			}
 		}
 		// Nein, vertikale Zuordnung:
 		else
 		{
 			// Prfen, ob Felder oberhalb von der Beamsource zugeordnet werden sollen:
 			if( fRow < sRow )
 			{
 				for( int row = fRow; row < sRow; row++ )
 				{
 					this.map.tile( row, sCol ).parent( source );
 					this.map.tile( row, sCol).type( "beam" );
 				}
 				
 				// Beamstrength fr den nchsten Durchlauf vermindern:				
 				this.map.tile( sRow, sCol ).strength( this.map.tile( sRow, sCol ).strength() - ( sRow - fRow ) );
 			}
 			// Nein, Felder sollen unterhalb von der Beamsource zugeordnet werden:
 			else
 			{
 				for( int row = fRow; row > sRow; row-- )
 				{
 					this.map.tile( row, sCol ).parent( source );
 					this.map.tile( row, sCol).type( "beam" );
 				}
 				
 				// Beamstrength fr den nchsten Durchlauf vermindern:				
 				this.map.tile( sRow, sCol ).strength( this.map.tile( sRow, sCol ).strength() - ( fRow  - sRow ) );
 			}
 		}
 	}
 	
 	private boolean isField( int row, int col )
 	{
 		return ( this.map.tile( row, col ).type().equals( "field" ) )? true : false;
 	}
 }
