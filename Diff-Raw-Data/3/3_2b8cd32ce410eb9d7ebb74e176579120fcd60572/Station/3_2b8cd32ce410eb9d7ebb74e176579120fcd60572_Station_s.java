 package ia;
 
 /**
  * 
  * @author Anderson Queiroz, Fernando Zucatelli, João Coutinho, Tiago Queiroz
  *
  */
 public class Station 
 {
 	/**
 	 * Class to represent stations
 	 * to go from UFABC to USP
 	 */
 	protected String name;
 	protected Colour c;
 	protected Station father;
 
 	public Station(String s)
 	{
 	    name = s;
 	}
 	public String getName() 
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
		c = Colour.WHITE;
 	}
 
 	public Colour getColour()
 	{
 		return c;
 	}
 
 	public void setColour(Colour c)
 	{
 		this.c = c;
 	}
 
     public Station getFather()
     {
         return father;
     }
 
     public void setFather(Station father)
     {
         this.father = father;
     }
     
     public String toString()
     {
         return name;        
     }
 }
