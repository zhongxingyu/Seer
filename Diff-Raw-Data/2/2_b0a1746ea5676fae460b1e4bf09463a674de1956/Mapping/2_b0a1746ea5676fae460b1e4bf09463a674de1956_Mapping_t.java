 package mapthatset.sim;
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class Mapping 
 {
 	private ArrayList< Integer > alMapping;
 	private ArrayList< ArrayList< Integer > > alQueryHistory = new ArrayList< ArrayList< Integer > >();
 	private ArrayList< Integer > alGuess;
 	private int intScore;
 	
 	public Mapping( ArrayList< Integer > alMapping )
 	{
 		this.alMapping = alMapping;
 	}
 	
 	public ArrayList< Integer > query( ArrayList< Integer > alQueryIndices )
 	{
 		// update the history
 		alQueryHistory.add( alQueryIndices );
 		ArrayList< Integer > alTemp = new ArrayList< Integer >();
 		for ( int intQueryIndex : alQueryIndices )
 		{
 			alTemp.add( alMapping.get( intQueryIndex - 1 ) );			// translate mapping index into data structure index
 		}
 		Collections.sort( alTemp );
 		ArrayList < Integer > alReturn = new ArrayList< Integer >();
 		alReturn.add( alTemp.get( 0 ) );
 		for ( int intTemp : alTemp )
 		{
 			if ( intTemp != alReturn.get( alReturn.size() - 1 ) )
 			{
 				alReturn.add( intTemp );
 			}
 		}
 //		System.out.println( "The query result is: " + alReturn );
 		return alReturn;
 	}
 	
 	public boolean guess( ArrayList<Integer> alActionContent )
 	{
 		this.alGuess = alActionContent;
 		
 		if ( alActionContent.size() != alMapping.size() )
 		{
 			System.out.println( "Wrong guess size!" );
 			return false;
 		}
 		
 		for ( int i = 0; i < alActionContent.size(); i ++ )
 		{
			if ( !alActionContent.get(i).equals(alMapping.get(i)) )
 			{
 //				System.out.println( "Your guess is wrong. The mapping is: " + alMapping );
 				return false;
 			}
 		}
 //		System.out.println( "Correct. The mapping is: " + alMapping );
 		return true;
 	}
 	
 	public int getMappingLength()
 	{
 		return alMapping.size();
 	}
 	
 	public void setScore( int intScore )
 	{
 		this.intScore = intScore;
 	}
 	
 	public int getScore()
 	{
 		return intScore;
 	}
 	
 	public ArrayList< Integer > getMapping()
 	{
 		return alMapping;
 	}
 }
