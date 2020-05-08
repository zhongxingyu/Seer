 /*
  * FiniteStateAutomata.java
  * Joe Smith
  * Algorithms - Spring 2009
  * Erik Linstead
  * Chapman University
  * Released under GPLv2 or later.
  */
 
 import java.util.*;
 
 public class FiniteStateAutomata{
 	// The second line of the file
 	public String[] Alphabet;
 	// 3rd
 	public String startState;
 	// 4th
 	public String acceptState;
 
 	//5th to the end
	public ArrayList<Transition> transitions = new ArrayList<Transition>();
 
 	public FiniteStateAutomata()
 	{
 	}
 }
 
 class Transition 
 {
 	String Target;
 	String Input;
 	String Home;
 
 	public void setTarget(String Target)
 	{
 		this.Target = Target;
 	}
 
 	public String getTarget()
 	{
 		return this.Target;
 	}
 
 	public void setInput(String Input)
 	{
 		this.Input = Input;
 	}
 
 	public String getInput()
 	{
 		return this.Input;
 	}
 
 	public void setHome(String Home)
 	{
 		this.Home = Home;
 	}
 
 	public String getHome()
 	{
 		return this.Home;
 	}
 }
