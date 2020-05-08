 package xhl.examples.entity;
 
 import java.util.Map;
 
 import xhl.core.Producer;
 import xhl.core.EvaluationException;
 import xhl.core.GenericModule;
 import xhl.core.elements.Block;
 import xhl.core.elements.Expression;
 import xhl.core.elements.Symbol;
 import xhl.core.validator.Validator;
 import xhl.examples.entity.Type.T;
 import static xhl.examples.entity.Type.*;
 
 public class EntityModule extends GenericModule {
 
     private Module module;
     private Entity currentEntity;
 
     public Module getModule() {
         return module;
     }
 
     @Function(evaluateArgs = false)
     public void module(Symbol name, Block body) {
         module = new Module(name.getName());
 
         // Initialize all defined entities
         Map<Symbol, xhl.core.validator.Type> defined =
                Validator.backwardDefunitions(body, getSchema());
         for (Symbol sym : defined.keySet()) {
             if (defined.get(sym).isNamed("Entity")) {
                 Entity entity = new Entity(sym.getName());
                 module.add(entity);
                 evaluator.putSymbol(sym, entity);
             }
         }
 
         evaluator.eval(body);
     }
 
     @Function(evaluateArgs = false)
     public Entity entity(Symbol name, Block attrs) {
         currentEntity = (Entity) evaluator.getSymbol(name);
         evaluator.eval(attrs);
         return currentEntity;
     }
 
     @Function(name = ":")
     public void attribute(@Symbolic Symbol name, @Symbolic Symbol typeName) {
         Type type;
         if (typeName.isNamed("int"))
             type = simple(T.INT);
         else if (typeName.isNamed("string"))
             type = simple(T.STRING);
         else if (typeName.isNamed("boolean"))
             type = simple(T.BOOLEAN);
         else {
             Entity ref = module.get(typeName.getName());
             type = reference(ref);
         }
         Attribute attr = new Attribute(name.getName(), type);
         currentEntity.add(attr);
         evaluator.putSymbol(name, attr);
     }
 
     @Function
     public void validate(@Symbolic Block block) {
         for (Expression expr : block) {
             try {
                 // TODO: Nomal values are not converted to builders in this case
                 @SuppressWarnings("unchecked")
                 Producer<Boolean> rule = (Producer<Boolean>) evaluator.eval(expr);
                 currentEntity.addValidation(rule);
             } catch (ClassCastException e) {
                 throw new EvaluationException(
                         "Validate block can only contain boolean expressions.");
             }
         }
     }
 
     @Function
     public Producer<Double> length(Attribute attr) {
         return new LengthProducer(attr);
     }
 
     public static class LengthProducer implements Producer<Double> {
         public final Attribute attr;
 
         public LengthProducer(Attribute attr) {
             this.attr = attr;
         }
 
         @Override
         public Double toValue() {
             throw new UnsupportedOperationException(
                     "LengthProducer can not be evaluated.");
         }
 
         @Override
         public String toCode() {
             return attr.getName() + ".size()";
         }
     }
 }
