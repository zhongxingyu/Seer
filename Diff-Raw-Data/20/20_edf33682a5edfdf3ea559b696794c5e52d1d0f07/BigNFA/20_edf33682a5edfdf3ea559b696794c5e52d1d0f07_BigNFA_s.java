 package main;
 
 import java.util.*;
 
 public class BigNFA
 {
 	private NFA nfa;
 
 	public BigNFA(HashSet<NFA> NFATable)
 	{
 		nfa = conversion(NFATable);
 	}
 
     // Converts a set of NFA's into one giant NFA
     // Adds epsilon transitions from a new start state to each NFA
     // Adds epsilon transitions from all accept states to one new accept state
	private NFA conversion(HashSet<NFA> NFATAble)
 	{
 	    NFA fin=new NFA("The Big One");
 	    
	    State start=new State(false,new HashMap<String,State>());
 	    State accept=new State(true,null);
 	    for(NFA nfa : NFATable)
 	    {
		start.addTransition("Epsilon",nfa.getStart());
		for(State st : nfa.getAccept())
		{
		    st.addTransition("Epsilon",accept);
		    st.setAccept(false);
	    	}
 	    }
	     fin=fin.setStart(start);
	     fin=fin.setAccept(accept);
 
 	     return fin;
 	}
 
 	public NFA getNFA()
 	{
 	    return nfa;
 	}
 
 	public void setNFA(NFA nfa)
 	{
 	    this.nfa=nfa;
 	}
 }
