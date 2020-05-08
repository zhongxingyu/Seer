 package messages;
 
 public class Index {
 	
 	private int number;
 	
 	private int maxSize;
 	
 	public boolean checkIt()
 	{
 		return maxSize > number;
 	}
 	
 	public boolean checkIt(int move)
 	{
 		return maxSize > number + move;
 	}
 	
 	public void setSize(int size)
 	{
 		this.maxSize = size;
 	}
 	
 	public int getValue()
 	{
 		return this.number;
 	}
 	
 	public void move(int change)
 	{
 		number += change;
 	}
 	
 	Index()
 	{
 		number = 0;
 	}
 	
 }
