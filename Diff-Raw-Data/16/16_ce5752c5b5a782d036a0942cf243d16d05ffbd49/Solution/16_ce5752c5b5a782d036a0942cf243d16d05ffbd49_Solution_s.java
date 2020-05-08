 //Solution.java
 //holds 1 solution
 
 public class Solution
 {
 	public ID id;
	public String text = "";
 	
 	public Solution()
 	{
 		id = new ID();
 	}
 	
 	public Solution(String text)
 	{
 		this();
 		this.text = text+"";
 	}
 }
