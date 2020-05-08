 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package granita.Parser.LeftValues;
 
 import granita.Parser.Expressions.Expression;
 import granita.Parser.Expressions.LitInt;
 import granita.Semantic.SymbolTable.ArrayVariable;
 import granita.Semantic.SymbolTable.SymbolTableTree;
 import granita.Semantic.SymbolTable.SymbolTableValue;
 import granita.Semantic.SymbolTable.Variable;
 import granita.Semantic.Types.IntType;
 import granita.Semantic.Types.Type;
 import granitainterpreter.ErrorHandler;
 import granitainterpreter.GranitaException;
 import granitainterpreter.Interpreter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Néstor A. Bermúdez <nestor.bermudez@unitec.edu>
  */
 public class ArrayLeftValue extends LeftValue {
 
     String id;
     Expression index;
     int calculatedIndex = -1;
 
     public ArrayLeftValue(int scopeId, int line) {
         super(scopeId, line);
     }
 
     public ArrayLeftValue(String id, Expression index, int scopeId, int line) {
         super(scopeId, line);
         this.id = id;
         this.index = index;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public Expression getIndex() {
         return index;
     }
 
     public void setIndex(Expression index) {
         this.index = index;
     }
 
     public int getScopeId() {
         return scopeId;
     }
 
     public void setScopeId(int scopeId) {
         this.scopeId = scopeId;
     }
 
 
     @Override
     public Type validateSemantics() throws GranitaException {
         SymbolTableValue value = SymbolTableTree.getInstance().lookupFromCurrent(id);
         if (value == null) {
             return ErrorHandler.handle("undefined variable '" + id + "' in line "
                     + this.getLine());
         } else {
             if (!(value instanceof ArrayVariable)) {
                 return ErrorHandler.handle("variable '" + id + "' is not an array: line "
                         + this.getLine());
             } else {
                 ArrayVariable array = (ArrayVariable) value;
                 Type it = this.index.validateSemantics();
 
                 if (!it.equivalent(new IntType())) {
                     return ErrorHandler.handle("array index must be int: line "
                             + this.getLine());
                 }
                 array.getType().setValue(it.getValue());
                 return array.getType();
             }
         }
     }
 
     @Override
     public String toString() {
         return id + "[" + index.toString() + "]";
     }
 
     @Override
     public void initializeVariable() {
         
     }
 
     @Override
     public Object evaluate() throws GranitaException {
        ArrayVariable value = (ArrayVariable) Interpreter.getInstance().getVariable(id);
        calculatedIndex = (Integer) index.evaluate();
         if (calculatedIndex >= value.getSize().getValue()) {
             ErrorHandler.handleFatalError("index out of bound: line " + this.getLine());
         }
         Type[] items = value.getItems();
         Type item = items[calculatedIndex];
         return item.getValue();
     }
 
     @Override
     public Type getLocation() {
         try {
             Variable var = Interpreter.getInstance().getVariable(id);
             ArrayVariable arrVar = (ArrayVariable) var;
            calculatedIndex = (Integer) index.evaluate();
             return arrVar.getItems()[calculatedIndex];
         } catch (GranitaException ex) {
             return null;
         }
     }
 }
