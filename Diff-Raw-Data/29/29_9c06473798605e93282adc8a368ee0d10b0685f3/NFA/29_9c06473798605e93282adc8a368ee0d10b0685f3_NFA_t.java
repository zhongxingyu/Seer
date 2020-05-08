 package sg.fa;
 
 /****
  * NFA
  * represents a nondeterministic finite automata
  * 
  * @author mlfong
  * @version 1.0
  */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 public class NFA
 {
     private State startState;
     private State acceptState;
 
     public NFA(State ss, State as)
     {
         this.startState = ss;
         this.acceptState = as;
     }
 
     public NFA(NFA other)
     {
         this.startState = new State(other.startState);
         Queue<State> openList = new LinkedList<State>();
         HashSet<State> visited = new HashSet<State>();
         openList.add(this.startState);
         State temp = null;
         while (!openList.isEmpty())
         {
             State curr = openList.poll();
             if (curr.isAcceptState())
             {
                 temp = curr;
                 break;
             }
             if (visited.contains(curr))
                 continue;
             visited.add(curr);
             for (Transition t : curr.getTransitions())
                 openList.add(t.getDestState());
         }
         this.acceptState = temp;
         // this.acceptState = new State(other.acceptState);
     }
 
     public NFA(State one)
     {
         State newState = new State("start" + one.getName(), false);
         newState.addTransition(new Transition(Transition.EPSILON, one));
         one.setAccept(false);
         State newAccept = new State("accept" + one.getName(), true);
         one.addTransition(new Transition(Transition.EPSILON, newAccept));
         this.startState = newState;
         this.acceptState = newAccept;
     }
 
     public void setAcceptToken(String token)
     {
         this.acceptState.setAcceptToken(token);
     }
 
     /*****
      * Subtracts the removeset from the superset and returns a new set
      * 
      * @param superset
      * @param removeset
      * @return HashSet<Character>
      */
     public static HashSet<Character> disjointSet(HashSet<Character> superset,
             HashSet<Character> removeset)
     {
         HashSet<Character> newSet = new HashSet<Character>();
         for (Character c : superset)
             if (!removeset.contains(c))
                 newSet.add(c);
         return newSet;
     }
 
     /*****
      * given a "1 layer NFA", so a NFA that represents a ranged NFA, returns set
      * of transitions
      * 
      * @param nfa
      * @return NFA
      */
     public static HashSet<Character> oneLayerTransitions(NFA nfa)
     {
         HashSet<Character> hs = new HashSet<Character>();
         Queue<State> openList = new LinkedList<State>();
         HashSet<State> visited = new HashSet<State>();
         openList.add(nfa.getStartState());
         while (!openList.isEmpty())
         {
             State curr = openList.poll();
             if (visited.contains(curr))
                 continue;
             visited.add(curr);
             for (Transition t : curr.getTransitions())
             {
                 if (t.getTransitionChar() != Transition.EPSILON)
                     hs.add(t.getTransitionChar());
                 openList.add(t.getDestState());
             }
         }
         return hs;
     }
 
     /****
      * makes a NFA for a range of characters
     * 
      * @param start
      * @param end
      * @return NFA
      */
     public static NFA makeRangedNFA(int start, int end)
     {
         return makeRangedNFA((char) (start), (char) (end));
     }

     /****
      * makes a NFA for a range of characters
     * 
      * @param start
      * @param end
      * @return NFA
      */
     public static NFA makeRangedNFA(char start, char end)
     {
         return makeRangedNFA(new Character(start), new Character(end));
     }
 
     /****
      * makes a NFA for a range of characters
     * 
      * @param hs
      * @return NFA
      */
     public static NFA makeRangedNFA(HashSet<Character> hs)
     {
         ArrayList<NFA> nfas = new ArrayList<NFA>();
         // make individual
         for (Character c : hs)
         {
             nfas.add(NFA.makeCharNFA(c));
         }
         // union them all
         Character start = 'S', end = 'E';
         int rand1 = (int) (Math.random() * 10000);
         int rand2 = (int) (Math.random() * 10000);
         State startMe = new State("range" + start + "-" + end + "s" + rand1,
                 false);
         State endMe = new State("range" + start + "-" + end + "f" + rand2, true);
         for (NFA nn : nfas)
         {
             startMe.addTransition(new Transition(Transition.EPSILON, nn
                     .getStartState()));
             nn.getAcceptState().setAccept(false);
             nn.getAcceptState().addTransition(
                     new Transition(Transition.EPSILON, endMe));
         }
         NFA all = new NFA(startMe, endMe);
         return all;
     }
 
     /***
      * makes a NFA for a range of characters
     * 
      * @param start
      * @param end
      * @return NFA
      */
     public static NFA makeRangedNFA(Character start, Character end)
     {
        if (!(start.charValue() >= 33 && start.charValue() <= 126))
        {
            System.err.println("Range [" + start + "-" + end
                    + "] beyond printable ASCII.");
            System.exit(0);
        }
        if (!(start.charValue() <= end.charValue()))
        {
            System.err.println("Range end (" + end + ") comes before start ("
                    + start + ")");
            System.exit(0);
        }
         State startMe = new State("range" + start + "-" + end + "s", false);
         State endMe = new State("range" + start + "-" + end + "f", true);
         for (char c = start.charValue(); c <= end.charValue(); c++)
         {
             startMe.addTransition(new Transition(c, endMe));
         }
         NFA all = new NFA(startMe, endMe);
         return all;
     }
 
     /*****
      * makes a NFA for one character
      * 
      * @param c
      * @return NFA
      */
     public static NFA makeCharNFA(char c)
     {
         return makeCharNFA(new Character(c));
     }
 
     /*****
      * makes a NFA for one character
      * 
      * @param c
      * @return NFA
      */
     public static NFA makeCharNFA(Character c)
     {
         State one = new State("" + c + (int) (Math.random() * 1000) + "p",
                 false);
         State two = new State("" + c + (int) (Math.random() * 1000) + "p", true);
         one.addTransition(new Transition(c, two));
         return new NFA(one, two);
     }
 
     public State getStartState()
     {
         return this.startState;
     }
 
     public State getAcceptState()
     {
         return this.acceptState;
     }
 
     public void setStartState(State ss)
     {
         this.startState = ss;
     }
 
     public void setAcceptState(State as)
     {
         this.acceptState = as;
     }
 
     /******
      * does a regex CONCAT on two NFAs
      * 
      * @param one
      * @param two
      * @return NFA
      */
     public static NFA concatenate(NFA one, NFA two)
     {
         State newStart = new State(one.getStartState().getName() + "+"
                 + two.getStartState().getName(), false);
         newStart.addTransition(new Transition(Transition.EPSILON, one
                 .getStartState()));
         one.getAcceptState().setAccept(false);
         one.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, two.getStartState()));
         State newAccept = new State(one.getAcceptState().getName()
                 + two.getAcceptState().getName(), true);
         two.getAcceptState().setAccept(false);
         two.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, newAccept));
         NFA newNFA = new NFA(newStart, newAccept);
         return newNFA;
     }
 
     /******
      * does a regex UNION on two NFAs
      * 
      * @param one
      * @param two
      * @return NFA
      */
     public static NFA union(NFA one, NFA two)
     {
         State newStart = new State(one.getStartState().getName() + "|"
                 + two.getStartState().getName(), false);
 
         newStart.addTransition(new Transition(Transition.EPSILON, one
                 .getStartState()));
         newStart.addTransition(new Transition(Transition.EPSILON, two
                 .getStartState()));
         one.getAcceptState().setAccept(false);
         two.getAcceptState().setAccept(false);
 
         State newAccept = new State(one.getAcceptState().getName()
                 + two.getAcceptState().getName(), true);
         one.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, newAccept));
         two.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, newAccept));
 
         NFA newNFA = new NFA(newStart, newAccept);
         return newNFA;
     }
 
     /*****
      * does a regex STAR on a NFA
      * 
      * @param one
      * @return NFA
      */
     public static NFA star(NFA one)
     {
         State newStart = new State(one.getStartState().getName() + "*", false);
         State newAccept = new State(one.getAcceptState().getName() + "*", true);
         newStart.addTransition(new Transition(Transition.EPSILON, one
                 .getStartState()));
         one.getStartState().addTransition(
                 new Transition(Transition.EPSILON, newAccept));
         one.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, newAccept));
         one.getAcceptState().addTransition(
                 new Transition(Transition.EPSILON, one.getStartState()));
         one.getAcceptState().setAccept(false);
         NFA newNFA = new NFA(newStart, newAccept);
         return newNFA;
     }
 
     /*****
      * prettyPrint does a state, transition -> state dump
      */
     public void prettyPrint()
     {
         HashSet<State> visited = new HashSet<State>();
         // Queue<State> openList = new LinkedList<State>();
         Stack<State> openList = new Stack<State>();
         openList.add(startState);
         while (!openList.isEmpty())
         {
             // State curr = openList.poll();
             State curr = openList.pop();
            if (visited.contains(curr))
                 continue;
             visited.add(curr);
             System.out.println(curr.getName());
             if (curr.getTransitions().size() == 0)
             {
                 System.out.println("\tACCEPT");
                 continue;
             }
             for (Transition t : curr.getTransitions())
             {
                 String transCharStr = "" + t.getTransitionChar();
                 if (t.getTransitionChar() == Transition.EPSILON)
                     transCharStr = "EPSILON";
                 System.out.println("\t" + transCharStr + " -> "
                         + t.getDestState().getName());
                 if (!visited.contains(t.getDestState()))
                     openList.add(t.getDestState());
             }
         }
 
     }
 
 }
