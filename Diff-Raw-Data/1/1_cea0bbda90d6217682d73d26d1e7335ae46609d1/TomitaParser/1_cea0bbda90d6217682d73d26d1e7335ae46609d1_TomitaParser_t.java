 package x.cfg.parsing.tomitaParser;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Stack;
 
 import java.util.LinkedList;
 import java.util.ArrayList;
 
 import x.cfg.Production;
 import x.cfg.Terminal;
 import x.cfg.EOITerminal;
 
 import x.cfg.parsing.parsingTable.ParsingTable;
 import x.cfg.parsing.parsingTable.parsingAction.*;
 
 public class TomitaParser
 {
     private static final boolean DEBUG=true;
 
     private final ParsingTable table;
 
     public TomitaParser(ParsingTable t)
     {
         table=t;
     }
 
     private void dprintln(String s)
     {
         if (DEBUG)
             System.out.println(this.getClass().getSimpleName()+": "+s);
     }
 
     // Input should be an ArrayList for performance reasons
     public List<ParsingAction> parse(ArrayList<Terminal> input)
     {
         Stack<Integer> stack=new Stack<Integer>();
         int pos=0;
         List<ParsingAction> result=new LinkedList<ParsingAction>();
 
         assert input.size() > 0 
             && input.get(input.size()-1) instanceof EOITerminal
             : "input should end with $";
 
         stack.push(table.getInitialState());
         
         end:
         while (true)
         {
             int s;
             Terminal t;
             Collection<ParsingAction> actions;
 
             assert stack.size() > 0;
             assert pos < input.size();
 
             s=stack.peek();
             t=input.get(pos);
             actions=table.actions(s,t);
 
             if (actions == null 
                  || actions.size() == 0)
             {
                 dprintln("error");
                 return null;
             }
 
             // TODO proof of concept: only do first action
             {
                 ParsingAction action=actions.iterator().next();
 
                 if (action instanceof ParsingActionShift)
                 {
                     ParsingActionShift shift=(ParsingActionShift)action;
 
                     stack.push(shift.getState());
                     pos++;
 
                     dprintln("shift "+shift.getState());
                 }
                 else if (action instanceof ParsingActionReduce)
                 {
                     ParsingActionReduce reduction=(ParsingActionReduce)action;
                     Production p=reduction.getProduction();
 
                     for (int i=0; i < p.bodyLength(); i++)
                         stack.pop();
                     
                     s=stack.peek();
 
                     stack.push(table.goTo(s,p.getHead()));
 
                     result.add(reduction);
 
                     dprintln("reduce "+p);
                 }
                 else if (action instanceof ParsingActionAccept)
                 {
                     dprintln("accept");
                     break end; // XXX ACCEPT
                 }
                 else
                     assert false;
             }
         }
 
         return result;
     }
 }
