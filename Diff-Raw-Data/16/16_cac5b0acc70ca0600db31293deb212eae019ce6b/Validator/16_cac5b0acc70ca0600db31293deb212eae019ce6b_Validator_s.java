 package xhl.core.validator;
 
 import xhl.core.Environment;
 import xhl.core.Error;
 import xhl.core.elements.*;
 
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 
 public class Validator implements ElementVisitor<Type> {
     private final Environment<Type> table = new Environment<Type>();
     private final Map<Symbol, ElementValidator> elements = newHashMap();
     private final List<Error> errors = newArrayList();
 
     public void addElements(Schema schema) {
         for (ElementSchema element : schema) {
             this.elements.put(element.getSymbol(), element.getValidator());
             if (element.getParams().size() == 0)
                 table.put(element.getSymbol(), element.getType());
             else
                 table.put(element.getSymbol(), Type.Element);
         }
     }
 
     public List<Error> getErrors() {
         return errors;
     }
 
     public Type check(Expression expression) {
         return expression.accept(this);
     }
 
     @Override
     public Type visit(SNumber num) {
         return Type.Number;
     }
 
     @Override
     public Type visit(SBoolean bool) {
         return Type.Boolean;
     }
 
     @Override
     public Type visit(SString str) {
         return Type.String;
     }
 
     @Override
     public Type visit(SList lst) {
         for (Expression exp : lst) {
             check(exp);
         }
         return Type.List;
     }
 
     @Override
     public Type visit(SMap map) {
         for (Expression key : map.keySet()) {
             check(map.get(key));
         }
         return Type.Map;
     }
 
     @Override
     public Type visit(Symbol sym) {
         if (table.containsKey(sym)) {
             if (table.get(sym).equals(Type.Element)) {
                 ElementValidator elValidator = elements.get(sym);
                 ValidationResult result =
                         elValidator.check(this,
                                 new SList(sym.getPosition()));
                 errors.addAll(result.errors);
                 return result.type;
             } else
                 return table.get(sym);
         } else {
             errors.add(new Error(sym.getPosition(), String.format(
                     "Symbol '%s' is not defined", sym)));
             return Type.AnyType;
         }
     }
 
     @Override
     public Type visit(Combination cmb) {
         if (!(cmb.get(0) instanceof Symbol)) {
             errors.add(new Error(cmb.getPosition(),
                     "Combination head is not a symbol"));
             return Type.AnyType;
         }
         Symbol head = (Symbol) cmb.get(0);
         if (!elements.containsKey(head)) {
             errors.add(new Error(head.getPosition(), String.format(
                     "Symbol '%s' is not defined", head)));
             return Type.AnyType;
         }
         ElementValidator elValidator = elements.get(head);
         table.putAll(elValidator.definedSymbols(cmb.tail(), false));
         ValidationResult result = elValidator.check(this, cmb.tail());
         errors.addAll(result.errors);
         return result.type;
     }
 
     @Override
     public Type visit(Block blk) {
         collectBackwardDefunitions(blk);
         for (Expression exp : blk)
             check(exp);
         return Type.Block;
     }
 
    public static Map<Symbol, Type> backwardDefunitions(Block blk,
                                                         Schema schema) {
         // FIXME: Remove duplication!
         Map<Symbol, Type> table = newHashMap();
         for (Expression exp : blk) {
             try {
                 Combination cmb = (Combination) exp;
                 Symbol head = (Symbol) cmb.head();
                 SList tail = cmb.tail();
                 ElementSchema elemSchema = schema.get(head);
                 if (schema != null)
                     table.putAll(elemSchema.getValidator().definedSymbols
                             (tail, true));
             } catch (ClassCastException e) {
                 // Ignore cases, where types did not match expectations
             }
         }
         return table;
     }
 
     private void collectBackwardDefunitions(Block blk) {
         for (Expression exp : blk) {
             try {
                 Combination cmb = (Combination) exp;
                 Symbol head = (Symbol) cmb.head();
                 SList tail = cmb.tail();
                 ElementValidator elValidator = elements.get(head);
                 if (elValidator == null) {
                     errors.add(new Error(head.getPosition(), "Element '" + head
                             + "' not defined."));
                 } else
                     table.putAll(elValidator.definedSymbols(tail, true));
             } catch (ClassCastException e) {
                 // Ignore cases, where types did not match expectations
             }
         }
     }
 }
