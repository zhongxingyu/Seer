 /** Shoes Class from W01
  * @version 05.01.2013
  * @author Wenjie Haung
 */
 
 package edu.ucsb.cs56.S13.lab04.ProtossJane;
 
 
 public class Shoes  {
 	private String Color = "Yellow";
 	private int Size = 35;
 
 	public Shoes ()	{ };
 
 	public Shoes (String Color, int Size) 
 	{
 		this.Color = Color;
 		this.Size = Size;
 	}
 
 	public String getColor ()
 	{
		return Color; 
 	}
 
 	public int getSize ()
 	{
		return Size;
 	}
 
 	public void setColor (String Color)
 	{
		this.Color = Color;
 	}
 
 	public void setSize (int Size)
 	{
		this.Size = Size;
 	}
 
 	public String toString ()
 	{
 		return "Color: "+this.Color+" Size: "+this.Size;
 	}
 
 	public boolean equals (Object o)
 	{
 		if(! (o instanceof Shoes) )
 			return false;
 		Shoes other = (Shoes) o;
 		return (other.getColor().equals(this.getColor())&&other.getSize()==this.getSize());
 	}
 
 	public static void main (String [] args)
 	{
 		Shoes myShoes =  new Shoes("Black",37);
 		System.out.println(myShoes);
 
 	}
 
 }
