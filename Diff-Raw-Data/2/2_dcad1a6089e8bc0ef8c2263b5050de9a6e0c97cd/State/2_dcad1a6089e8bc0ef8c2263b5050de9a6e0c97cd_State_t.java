 package main;
 
 import java.util.*;
 
 public class State
 {
 	private boolean accept;
 	private HashMap<String, List<State>> transitionTable;
 	
 	public State(boolean accept, HashMap<String, List<State>> table)
 	{
 		this.accept = accept;
 		this.transitionTable = table;
 	}
 	
 	public boolean isAccept()
 	{
 		return accept;
 	}
 	
	public HashMap<String, List<State>> getTransitionTable()
 	{
 		return transitionTable;
 	}
 
     public void setIsAccept(boolean accept)
     {
         this.accept = accept;
     }
     
     public void addTransition(String trans, State toGo)
     {
     	transitionTable.put(trans, transitionTable.get(trans).add(toGo));
     }
 }
