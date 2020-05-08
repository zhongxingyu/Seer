 /**
  * 
  */
 package rky.portfolio.gambles;
 
 /**
  * @author stoked
  *
  */
 public final class Return
 {
 	public static final Return   high = new Return();
 	public static final Return medium = new Return();
 	public static final Return    low = new Return();
 
	private Return() { }
 
 	@Override
 	public String toString()
 	{
 		if( this == high   )     return "Return[HI]";
 		if( this == medium )     return "Return[MED]"; 
 		if( this == low    )     return "Return[LOW]";
 		throw new RuntimeException( "unexpected instance of Return" );
 	}
 
 	public Character getAliesChar()
 	{
 		if( this == high   )     return 'h';
 		if( this == medium )     return 'm';
 		if( this == low    )     return 'l';
 		throw new RuntimeException( "unexpected instance of Return" );
 	}
 }
