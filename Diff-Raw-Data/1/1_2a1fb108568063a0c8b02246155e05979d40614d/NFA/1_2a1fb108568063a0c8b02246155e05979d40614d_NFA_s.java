 package main;
 
 import java.util.HashMap;
 import java.util.Set;
 
 public class NFA
 {
 	private State start;
 	private String name;
 
 	public NFA(String name, String def)
 	{
         this.name = name;
 
         // Make accept state
         // TODO: this is for simple char classes only
         State accept = new State(true, new HashMap<String, State>());
         
         // Create transition table
         HashMap<String, State> transition = new HashMap<String, State>();
         transition.put(def, accept);
         
         // Create start state
         if (!def.equals(""))
         {
             start = new State(false, transition);
         }
         else
         {
             start = new State(true, transition);
         }
 	}
 
     public String getName()
     {
         return name;
     }
 
     public State getStart()
     {
         return start;
     }
     
     public String toString()
     {
     	String toReturn = name;
     	HashMap<String, State> nextStates = start.getTransitionTable();
     	Set<String> nextKeys = nextStates.keySet();
     	for(String key : nextKeys)
     	{
     		toReturn += (" -- " + key + " --> ");
     		if(nextStates.get(key).isAccept())
     		{
     			toReturn += "ACCEPT";
     		}
     		else
     		{
     			toReturn += "NODE";
     		}
     	}
     	return toReturn;
     }
 }
