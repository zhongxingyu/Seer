 package xhl.examples.statemachine;
 
 import java.util.List;
 import java.util.Map;
 
 import xhl.core.GenericModule;
 import xhl.core.elements.Block;
 import xhl.core.elements.Expression;
 import xhl.core.elements.Symbol;
 import xhl.core.validator.Type;
 import xhl.core.validator.Validator;
 
 /**
  * XHL module for configuring state machine
  *
  * @author Sergej Chodarev
  */
 public class StateMachineModule extends GenericModule {
     private Event[] resetEvents = null;
     private State startState = null;
     private State currentState = null;
 
     // DSL functions =========================================================
 
     @Function(name = "!script")
     public void script(@Symbolic Block exprs) {
         Map<Symbol, Type> defined =
                Validator.backwardDefinitions(exprs, getSchema());
         for (Symbol sym : defined.keySet())
             if (defined.get(sym).isNamed("State"))
                 evaluator.putSymbol(sym, new State(sym.getName()));
         evaluator.eval(exprs);
     }
 
     @Function(evaluateArgs = false)
     public void events(Block blk) throws Exception {
         for (Expression stmt : blk) {
             try {
                 Definition def = (Definition) evaluator.eval(stmt);
                 Event event = new Event(def.symbol.getName(), def.value);
                 evaluator.putSymbol(def.symbol, event);
             } catch (ClassCastException e) {
                 throw new Exception("Wrong type of argument.");
             }
         }
     }
 
     @Function
     public void resetEvents(List<Event> events) {
         resetEvents = events.toArray(new Event[0]);
     }
 
     @Function(evaluateArgs = false)
     public void commands(Block blk) throws Exception {
         for (Expression expr : blk) {
             try {
                 Definition def = (Definition) evaluator.eval(expr);
                 Command cmd = new Command(def.symbol.getName(), def.value);
                 evaluator.putSymbol(def.symbol, cmd);
             } catch (ClassCastException e) {
                 throw new Exception("Wrong type of argument.");
             }
         }
     }
 
     @Function(name=":")
     public Definition colon(@Symbolic Symbol name, String code) {
         return new Definition(name, code);
     }
 
     @Function(evaluateArgs = false)
     public void state(Symbol name, Block blk) throws Exception {
         currentState = (State) evaluator.getSymbol(name);
         evaluator.eval(blk);
         if (startState == null)
             startState = currentState;
         currentState = null;
     }
 
     @Function
     public void actions(List<Command> args) throws Exception {
         if (currentState == null)
             throw new Exception("Actions must by defined inside state.");
         for (Command cmd : args) {
             currentState.addAction(cmd);
         }
     }
 
     @Function(name = "->")
     public void transition(Event trigger, State target) {
         currentState.addTransition(trigger, target);
     }
 
     // End of DSL functions ==================================================
 
     public StateMachine getStateMachine() {
         StateMachine machine = new StateMachine(startState);
         machine.addResetEvents(resetEvents);
         return machine;
     }
 
     private static class Definition {
         public final Symbol symbol;
         public final String value;
 
         public Definition(Symbol name, String value) {
             this.symbol = name;
             this.value = value;
         }
     }
 }
