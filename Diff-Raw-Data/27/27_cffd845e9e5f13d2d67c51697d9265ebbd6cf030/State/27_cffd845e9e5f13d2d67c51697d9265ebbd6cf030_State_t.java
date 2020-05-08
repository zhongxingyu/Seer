 package mi.parser;
 
 import mi.common.CharHashMap;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * User: goldolphin
  * Time: 2014-02-09 20:25
  */
 public class State {
     public final int id;
     private int acceptedNontermId = Nonterm.ANY;
     private CharHashMap<Transition.TermTransition> termTransitions = new CharHashMap<>();
     private HashMap<Integer, Transition.NontermTransition> nontermTransitions = new HashMap<>();
 
     private State(int id) {
         this.id = id;
     }
 
     public void setAcceptedNonterm(int nontermId) {
         if (hasAcceptedNonterm()) {
             throw new ParseException("The state already accepts nonterm: " + acceptedNontermId);
         }
         acceptedNontermId = nontermId;
     }
     
     public boolean hasAcceptedNonterm() {
         return acceptedNontermId != Nonterm.ANY;
     }
 
     public int getAcceptedNontermId() {
         return acceptedNontermId;
     }
 
    public boolean canConsume() {
        return termTransitions.size() > 0;
    }

     public State getTermTransition(char c, int headNontermId) {
         Transition.TermTransition transition = termTransitions.get(c);
         if (transition != null && transition.containsHeadNonterm(headNontermId)) {
             return transition.target;
         }
         return null;
     }
 
     public State addTermTransition(char t, int headNontermId, StateGenerator generator) {
         Transition.TermTransition transition = termTransitions.get(t);
         if (transition == null) {
             transition = new Transition.TermTransition(t, generator.generate());
             termTransitions.put(t, transition);
         }
         transition.addHeadNonterm(headNontermId);
         return transition.target;
     }
 
     public State addTermTransition(String t, int headNontermId, StateGenerator generator) {
         int len = t.length();
         State current = this;
         for (int i = 0; i < len; i ++) {
             current = current.addTermTransition(t.charAt(i), headNontermId, generator);
         }
         return current;
     }
 
     public State addNontermTransition(int nontermId, int headNontermId, StateGenerator generator) {
         Transition.NontermTransition transition = nontermTransitions.get(nontermId);
         if (transition == null) {
             transition = new Transition.NontermTransition(nontermId, generator.generate());
             nontermTransitions.put(nontermId, transition);
         }
         transition.addHeadNonterm(headNontermId);
         return transition.target;
     }
 
     CharHashMap<Transition.TermTransition> getTermTransitions() {
         return termTransitions;
     }
 
     Collection<Transition.NontermTransition> getNontermTransitions() {
         return nontermTransitions.values();
     }
 
     @Override
     public String toString() {
         StringBuilder buffer = new StringBuilder();
         buffer.append("{").append(id).append(", ").append(acceptedNontermId).append("\n");
         for (Transition t: getNontermTransitions()) {
             buffer.append(t).append("\n");
         }
         for (CharHashMap.Entry<Transition.TermTransition> e: getTermTransitions()) {
             buffer.append(e.getValue()).append("\n");
         }
         buffer.append("}");
         return buffer.toString();
     }
 
     public static class StateGenerator {
         private ArrayList<State> list = new ArrayList<>();
 
         public State generate() {
             State s = new State(list.size());
             list.add(s);
             return s;
         }
 
         public ArrayList<State> getAll() {
             return list;
         }
     }
 }
