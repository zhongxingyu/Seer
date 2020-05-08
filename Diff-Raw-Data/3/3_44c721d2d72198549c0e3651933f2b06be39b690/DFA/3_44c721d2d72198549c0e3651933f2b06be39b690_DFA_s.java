 package main;
 
 import java.util.*;
 
 
 public class DFA
 {
 	private DFAState start;
 	private List<DFAState> accept;
     private HashSet<DFAState> states;
 
     // This will call convertToDFA() using the passed in BigNFA
 	public DFA(BigNFA bnfa)
 	{
         NFA nfa = bnfa.getNFA();
         convertToDFA(nfa);
 	}
 
     private void convertToDFA(NFA nfa)
     {
         DFAState curr, temp;
         List<String> tokens = new ArrayList<String>();
         LinkedList<DFAState> queue = new LinkedList<DFAState>();
         findStart(nfa);
         queue.add(start);
 
         while (!queue.isEmpty())
         {
             curr = queue.removeFirst();
             // Pull out all keys
             for (State s : curr.getStates())
             {
                 for (String k : curr.getTransitions().keySet())
                 {
                     tokens.add(k);
                 }
             }
             // Iterate over all possible iterations and link states together
             for (String j : tokens)
             {
                 temp = goTo(curr, j);
                 queue.add(temp);
             }
         }
     }
 
     // Calculates the start state of the DFA
     private void findStart(NFA nfa)
     {
        DFAState dummy = new DFAState("", false, null, new HashMap<String, DFAState>());
         dummy.addState(nfa.getStart());
         start = closure(dummy);
     }
 
     // Calculates epsilon transition states
     private DFAState closure(DFAState s)
     {
         HashSet<State> ret = new HashSet<State>();
         HashSet<State> toAdd;
         boolean isAccept = false;
         String name = "";
         DFAState output;
 
         // Add all input states to output
         for (State i : s.getStates())
         {
             ret.add(i);
         }
 
         while (true)
         {
             toAdd = new HashSet<State>();
 
             // Iterate over all states currently in ret
             for (State r : ret)
             {
                 if (r.getTransitionTable().containsKey(""))
                 {
                     for (State t : r.getTransitionTable().get(""))
                     {
                         if (!ret.contains(t))
                         {
                             toAdd.add(t);
                         }
                     }
                 }
             }
             if (toAdd.isEmpty())
             {
                 break;
             }
             for (State a : toAdd)
             {
                 ret.add(a);
             }
         }
         // Determine if new state is accept
         for (State t : ret)
         {
             if (t.isAccept())
             {
                 isAccept = true;
                 name = t.getName();
                 break;
             }
         }
         // Create new DFAState
         output = new DFAState(name, isAccept, ret, new HashMap<String, DFAState>());
         states.add(output);
         if (output.isAccept())
         {
             accept.add(output);
         }
 
         return output;
     }
 
     // This calculates all of the transitions out of a state
     // for a given transition string
     private DFAState goTo(DFAState s, String token)
     {
         HashSet<State> ret = new HashSet<State>();
         DFAState dummy, output;
 
         for (State i : s.getStates())
         {
             if (i.getTransitionTable().containsKey(token))
             {
                 for (State t : i.getTransitionTable().get(token))
                 {
                     ret.add(t);
                 }
             }
         }
         // Create dummy DFAState to pass to closure()
         dummy = new DFAState("", false, ret, new HashMap<String, DFAState>());
         output = closure(dummy);
         s.getTransitions().put(token, output);
 
         return output;
     }
 
     public DFAState getStart()
     {
         return start;
     }
 
     public List<DFAState> getAccepts()
     {
         return accept;
     }
 
     public HashSet<DFAState> getStates()
     {
         return states;
     }
 }
