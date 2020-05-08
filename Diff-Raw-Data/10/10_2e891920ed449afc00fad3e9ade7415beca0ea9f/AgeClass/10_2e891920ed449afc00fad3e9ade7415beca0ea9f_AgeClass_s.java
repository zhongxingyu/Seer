 package dhbw.LWBS.CA5_KB1.model;
 
 /**
  * Contains all types of <code>AgeClasses</code> that can occur in the file that is read in.
  */
 public enum AgeClass
 {
 	B18(1, "<18"),
 	F19T24(2, "19-24"),
 	F25T35(3, "25-35"),
 	F36T49(4, "36-49"),
 	F50T65(5, "50-65"),
 	O65(6, ">65"),
 	NONE(0, ""),
 	ALL(100, "*");
 
 	//ENUM CODE
 	public static String NAME = "AgeClass";
 	private int id;
 	private String token;
 
 	/**
 	 * Enum constructor
 	 * @param id
 	 * @param token
 	 */
 	AgeClass(int id, String token)
 	{
 		this.id = id;
 		this.token = token;
 	}
 
 	public int getId()
 	{
 		return id;
 	}
 
 	/**
 	 * Returns the <code>AgeClass</code> constant that matches with the given id.
 	 * @param id that is searched for
 	 * @return <code>AgeClass</code> constant, <code>null</code> if id is not contained
 	 */
 	public static AgeClass fromInteger(Integer id)
 	{
 		if (id != null)
 		{
 			for (AgeClass a : AgeClass.values())
 			{
 				if (id == a.id)
 				{
 					return a;
 				}
 			}
 		}
 		System.err.println("Unknown AgeClass Id: " + id);
 		return null;
 	}
 	
 	/**
 	 * Returns the <code>AgeClass</code> constant that matches with the given token.
 	 * @param token that is searched for
 	 * @return <code>AgeClass</code> constant, <code>null</code> if token is not contained
 	 */
 	public static AgeClass fromString(String token)
 	{
 		if (token != null)
 		{
 			try {
 				int intValue = Integer.parseInt(token);
 				
 				token = getClassFromValue(intValue);
 			}
 			catch (NumberFormatException nfe) { /* do nothing, this will happen (in most cases) */ }
 			
 			for (AgeClass a : AgeClass.values())
 			{
 				if (token.equalsIgnoreCase(a.token))
 				{
 					return a;
 				}
 			}
 		}
 		System.err.println("Unknown AgeClass token: " + token);
 		return null;
 	}
 	
		/**
		 * Returns the correct <code>AgeClass<code> token for a single given value (e.g. class 19-24 for the value 21) 
		 * @param intValue an <code>int</code> that should be classified
		 * @return the token representing the correct <code>AgeClass</code> for the given intValue
		 */
 	private static String getClassFromValue(int intValue)
 	{
 		if (intValue <= 18)
 			return "<18";
 		
 		if ( (intValue > 18) && (intValue <= 24) )
 			return "19-24";
 		
 		if ( (intValue > 24) && (intValue <= 35) )
 			return "25-35";
 		
 		if ( (intValue > 35) && (intValue <= 49) )
 			return "36-49";
 		
 		if ( (intValue >= 50) && (intValue <= 65) )
 			return "50-65";
 		
 		if (intValue > 65)
 			return ">65";
 		
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		if (id != 0)
 			return token;
 		else
 			return "_";
 	}
 }
