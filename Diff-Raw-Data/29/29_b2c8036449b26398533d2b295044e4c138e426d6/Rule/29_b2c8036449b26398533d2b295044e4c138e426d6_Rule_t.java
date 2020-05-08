 package student;
 
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import student.ParserImpl.HistObj;
 
 /**
  * A representation of a critter rule.
  */
 public class Rule extends Node<Update> {
 
     private Condition condition;
     private Action action = null;
 
     public Rule(Condition c, List<Update> u, Action a) {
         super(u);
         condition = c;
         action = a;
     }
 
     public Rule(Condition c, List<Update> u) {
         this(c, u, null);
     }
     
     protected Rule() {
         super(new LinkedList<Update>());
     }
     
     public static Rule parse(LinkedList<HistObj> hist) throws SyntaxError {
         Rule r = new Rule();
         HistObj self = hist.pop();
         //Rule => Condition --> Command ;
         r.condition =  Condition.parse(hist);
         hist.pop().expect("-->");
         while(hist.peek().rule.startsWith("Command") && hist.pop().production.length > 0) {
             if(hist.peek().rule.equals("Update"))
                 r.children.add(Update.parse(hist));
             else if(hist.peek().rule.equals("Action")) {
                 r.action = Action.parse(hist);
                 break;
             }
         }
         hist.pop().expect(";");
         return r;
     }
 
     @Override
     public int size() {
         return super.size() + 2; //2 extra for the condition and action
     }
 
     @Override
     public StringBuffer toString(StringBuffer sb) {
         condition.toString(sb);
         sb.append(" --> ");
         for(Update u : children) 
             u.toString(sb).append('\n');
         if (action != null) 
             sb = action.toString(sb);
         else
             sb.deleteCharAt(sb.length()-1);
         sb.append(';');
         return sb;
     }
 
     @Override
     public boolean hasChildren() {
         return true;
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         Rule r = (Rule) super.clone();
         r.condition = (Condition) condition.clone();
         if(action != null)
             r.action = (Action) action.clone();
         return r;
     }
    @Override
   protected LinkedList<Node<?>> buildList(LinkedList<Node<?>> list) {
        list.add(this);
        Iterator it = children.iterator();
        Node child;
        list = condition.buildList(list);
        while(it.hasNext())
        {
            child = (Node)it.next();
            child.buildList(list);
        }
        if(action!=null)
            list = action.buildList(list);
        return list;
    }
 }
