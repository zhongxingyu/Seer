 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.unfication;
 
 import java.util.*;
 import org.junit.BeforeClass;
 import uk.co.mtford.abduction.logic.*;
 import uk.co.mtford.abduction.logic.Variable;
 
 /**
  *
  * @author mtford
  */
 public class Unifier {
     
     private static Predicate Knows;
     private static Constant John;
     private static Constant Jane;
     private static Variable x;
      
     
     public boolean occurs(Variable x, IUnifiable t, Map<Variable,IUnifiable> sub) {
         
         Stack<IUnifiable> stack = new Stack<IUnifiable>();
         stack.push(t);
         
         while (!stack.empty()) {
             t = stack.pop();
            if (t instanceof Predicate) {
                Predicate p = (Predicate) t;
                 stack.addAll(Arrays.asList(p.getParameters()));
             }
             else if (t instanceof Variable) {
                 Variable y = (Variable) t;
                 if (x.equals(y)) {
                     return true;
                 }
                 if (sub.containsKey(y)) {
                     stack.push(sub.get(y));
                 }
             }
             else if (t instanceof Function ) {
                 Function p = (Function) t;
                 stack.addAll(Arrays.asList(p.getParameters()));
             }
 	}
 	
 	return false;
     }
     
     public Map<Variable,IUnifiable> unify(IUnifiable s, IUnifiable t) throws CouldNotUnifyException {
         
         Stack<IUnifiable[]> stack = new Stack<IUnifiable[]>();
         Map<Variable, IUnifiable> sub = new HashMap<Variable,IUnifiable>();
         
         IUnifiable[] pair = new IUnifiable[2];
         pair[0] = s;
         pair[1] = t;
         stack.push(pair);
         while(!stack.isEmpty()) {
             pair = stack.pop();
             s = pair[0];
             t = pair[1];
             while (s instanceof Variable && sub.containsKey((Variable)s)) s = sub.get((Variable)s);
             while (t instanceof Variable && sub.containsKey((Variable)t)) t = sub.get((Variable)t);
             if (!s.equals(t)) {
                 if (s instanceof Variable && t instanceof Variable) {
                     sub.put((Variable)s,(Variable)t);
                 }
                 else if (s instanceof Variable && t instanceof IUnifiable) {
                     if (!occurs((Variable)s,t,sub)) {
                         sub.put((Variable)s,t);
                     }
                     else {
                         throw new CouldNotUnifyException();
                     }
                 }
                 else if (s instanceof IUnifiable && t instanceof Variable) {
                     if (!occurs((Variable)t,s,sub)) {
                         sub.put((Variable)t,s);
                     }
                     else {
                         throw new CouldNotUnifyException();
                     }
                 }
                 else if (s instanceof Function && t instanceof Function) {
                     Term[] sTerm = ((Function)s).getParameters();
                     Term[] tTerm = ((Function)t).getParameters();
                     boolean sameName = ((Function)s).getName().equals(((Function)t).getName());
                     boolean sameParamSize = ((Function)s).getParameters().length==((Function)t).getParameters().length;
                     if (sameName&&sameParamSize) {
                         for (int i = 0; i<sTerm.length; i++) {
                             Term[] termPair = new Term[2];
                             termPair[0] = sTerm[i];
                             termPair[1] = tTerm[i];
                             stack.add(termPair);
                         }
                     }
                     else { // Different functions.
                         throw new CouldNotUnifyException();
                     }
                 }
                 else if (s instanceof Predicate && t instanceof Predicate) {
                     IUnifiable[] sTerm = ((Predicate)s).getParameters();
                     IUnifiable[] tTerm = ((Predicate)t).getParameters();
                     boolean sameName = ((Predicate)s).getName().equals(((Predicate)t).getName());
                     boolean sameParamSize = ((Predicate)s).getParameters().length==((Predicate)t).getParameters().length;
                     if (sameName&&sameParamSize) {  
                         for (int i = 0; i<sTerm.length; i++) {
                             IUnifiable[] termPair = new IUnifiable[2];
                             termPair[0] = sTerm[i];
                             termPair[1] = tTerm[i];
                             stack.add(termPair);
                         }
                     }
                     else { // Different functions.
                         throw new CouldNotUnifyException();
                     }
                 }
             }
         }
         
         return sub;
         
     }
  
 }
