 package Generator.Character;
 
 import java.util.ArrayList;
 
 public class CharacterClass 
 {	
 	boolean[] valid;
 	ArrayList<Character> real;
 	String name;
 	
 	public CharacterClass()
 	{
		this("");
 	}
 	
 	public CharacterClass(String name)
 	{
 		valid = new boolean[256];
 		real = new ArrayList<Character>();
 		this.name = name;
 	}
 
 	public boolean isMatched(char c) 
 	{
 		return valid[(int)c];
 	}
 	
 	public void accept(char c)
 	{
 		valid[(int)c] = true;
 	}
 	
 	public void acceptAll()
 	{
 		for(int a = 0; a < valid.length; a++)
 		{
 			valid[a] = true;
 		}
 	}
 	
 	public void acceptBoundary(char start, char end)
 	{
 		for(int a = (int)start; a <= (int)end; a++)
 		{
 			valid[(char)a] = true;
 		}
 	}
 
 	public CharacterClass union(CharacterClass c) 
 	{
 		CharacterClass temp = new CharacterClass(name);
 		for(int a = 0; a < valid.length; a++)
 		{
 			if(c.valid[a] || valid[a])
 			{
 				temp.valid[a] = true;
 			}
 		}
 		return temp;
 	}
 	
 	public CharacterClass intersect(CharacterClass c)
 	{
 		CharacterClass temp = new CharacterClass(name);
 		for(int a = 0; a < valid.length; a++)
 		{
 			if(c.valid[a] && valid[a])
 			{
 				temp.valid[a] = true;
 			}
 		}
 		return temp;
 	}
 	
 	public CharacterClass exclude(CharacterClass c)
 	{
 		CharacterClass temp = new CharacterClass(name);
 		for(int a = 0; a < valid.length; a++)
 		{
 			if(valid[a] && !c.valid[a])
 			{
 				temp.valid[a] = true;
 			}
 		}
 		return temp;
 	}
 }
