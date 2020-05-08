 package lightbeam.solution;
 
 import java.util.ArrayList;
 
 import core.GameObjects;
 import core.tilestate.Tile;
 import core.tilestate.TileArray;
 import lightbeam.solution.strategies.ConclusiveReachable;
 import lightbeam.solution.strategies.ForcedToBeam;
 
 public class LogicClient extends GameObjects
 {
 	private TileArray map			= null;
 	private LogicContext lContext	= null;
 	
 	// Aktulle Auswahl der Lsungs-Strategie
 	private int logicStrategy		= 1;
 	private int amountOfStrategies	= 2;
 	
 	private boolean[] result	= new boolean[this.amountOfStrategies];	
 	private boolean done		= false;
 	
 	public LogicClient() {}
 	
 	public void check( TileArray map, ILogicResponse callee )
 	{
 		this.done			= false;
 		this.map			= map.createClone();
 		this.logicStrategy	= 1;
		this.reset();
 		this.simulateMode( this.map );		
 		this.lContext		= new LogicContext( this.map );
 		
 		while( this.done == false )
 		{
 			this.recalcStrength();
 			this.setStrategy();
 			this.lContext.executeLogic();
 			this.map	= this.lContext.getMap();
 			this.markTiles();
 			callee.logicResponse( this.map );
 
 			if( this.lContext.getResult() == false )
 			{
 				this.result[this.logicStrategy-1] = false;
 				
 				if( this.endOfStrategies() == true )	{ this.done = true;		}
 				else									{ this.logicStrategy++;	}
 			} else
 			{
				this.reset();
 			}
 		}
 	}
 	
 	public boolean[] getResult() 	{ return this.result;	}
 	public TileArray getMap() 		{ return this.map; 		}
 	
 	private void simulateMode( TileArray map )
 	{
 		int rows	= map.rows();
 		int cols	= map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= map.tile( row, col );
 				
 				if( tile.type().equals( "beam" ) ) 			{ tile.type( "field" );		}
 				if( !tile.type().equals( "beamsource" ) )	{ tile.color( Tile.CRED );	}
 			}
 		}
 	}
 	
 	private void markTiles()
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= this.map.tile( row, col );
 				
 				if( tile.type().equals( "beam" ) && !tile.color().equals( Tile.CGREEN ) )
 				{
 					this.map.tile( row, col ).color( Tile.CGREEN );
 				}
 			}
 		}
 	}
 	
	private void reset()
 	{
 		for( int i = 0; i < this.amountOfStrategies; i++ ) { this.result[i]	= true; }
		this.logicStrategy = 1;
 	}
 	
 	private boolean endOfStrategies()
 	{
 		int len			= this.result.length;
 		
 		for( int i = 0; i < len; i++ )
 		{
 			if( this.result[i] == true ) { return false; }
 		}
 		
 		return true;
 	}
 	
 	private void setStrategy()
 	{
 		switch( this.logicStrategy )
 		{
 			case 1:
 				this.lContext.setLogic( new ConclusiveReachable() );
 				break;
 			case 2:
 				this.lContext.setLogic( new ForcedToBeam() );
 				break;
 			default:
 				this.lContext.setLogic( new ConclusiveReachable() );
 				this.logicStrategy	= 1;
 				break;
 		}
 	}
 	
 	private void recalcStrength()
 	{
 		ArrayList<Tile> beamsources	= this.map.getTilesInArea( "beamsource" );
 		int bSize					= beamsources.size();
 		
 		for( int i = 0; i < bSize; i++ )
 		{
 //			ArrayList<Tile> beams	= this.
 		}
 		
 	}
 }
