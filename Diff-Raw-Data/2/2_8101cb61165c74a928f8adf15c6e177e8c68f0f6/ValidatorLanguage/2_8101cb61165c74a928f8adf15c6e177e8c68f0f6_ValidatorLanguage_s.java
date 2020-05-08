 package xhl.core.validator;
 
 import java.util.List;
 
 import xhl.core.GenericModule;
 import xhl.core.Language;
 import xhl.core.Module;
 import xhl.core.elements.Block;
 import xhl.core.elements.Symbol;
 import xhl.core.validator.ElementSchema.DefSpec;
 import xhl.core.validator.ElementSchema.ParamSpec;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Lists.newArrayList;
 
 public class ValidatorLanguage extends GenericModule implements Language {
 
     private ElementSchema currentElement;
     private final Schema schema = new Schema();
 
     @Function(evaluateArgs = false)
     public void element(Symbol name, Block blk) {
         currentElement = new ElementSchema(name);
         schema.put(currentElement);
         evaluator.eval(blk);
     }
 
     @Function
     public void params(List<ParamSpec> args) {
         currentElement.setParams(args);
     }
 
     @Function(evaluateArgs = false)
     public ParamSpec val(Symbol type) {
         return ParamSpec.val(new Type(type));
     }
 
     @Function(evaluateArgs = false)
     public ParamSpec sym(Symbol type) {
         return ParamSpec.sym(new Type(type));
     }
 
     @Function
     public ParamSpec variadic(ParamSpec param) {
         return ParamSpec.variadic(param);
     }
     @Function
     public ParamSpec block(ParamSpec param) {
         return ParamSpec.block(param);
     }
 
     @Function(evaluateArgs = false)
     public void type(Symbol type) {
         currentElement.setType(new Type(type));
     }
 
     @Function
     public void defines(double arg, @Symbolic Symbol type) {
         checkArgument(arg % 1 == 0);
         DefSpec def = new DefSpec((int) arg, new Type(type));
         currentElement.addDefine(def);
     }
 
     @Function
     public void defines_forward(double arg, @Symbolic Symbol type) {
         checkArgument(arg % 1 == 0);
         DefSpec def = new DefSpec((int) arg, new Type(type), true);
         currentElement.addDefine(def);
     }
 
     @Override
     public Module[] getModules() {
         return new Module[] { this };
     }
 
     @Override
     public Schema getSchema() {
         Schema langSchema = new Schema();
         ElementSchema element = new ElementSchema(new Symbol("element"));
         element.setParams(newArrayList(ParamSpec.sym(new Type("Symbol")),
                ParamSpec.block(ParamSpec.sym(new Type("Block")))));
         element.setType(new Type("Symbol"));
         langSchema.put(element);
         ElementSchema params = new ElementSchema(new Symbol("params"));
         params.setParams(newArrayList(ParamSpec.val(Type.List)));
         params.setType(new Type("Parameters"));
         langSchema.put(params);
         ElementSchema val = new ElementSchema(new Symbol("val"));
         val.setParams(newArrayList(ParamSpec.sym(Type.Symbol)));
         val.setType(new Type("Parameter"));
         langSchema.put(val);
         ElementSchema sym = new ElementSchema(new Symbol("sym"));
         sym.setParams(newArrayList(ParamSpec.sym(Type.Symbol)));
         sym.setType(new Type("Parameter"));
         langSchema.put(sym);
         ElementSchema variadic = new ElementSchema(new Symbol("variadic"));
         variadic.setParams(newArrayList(ParamSpec.val(new Type("Parameter"))));
         variadic.setType(new Type("Parameter"));
         langSchema.put(variadic);
         ElementSchema block = new ElementSchema(new Symbol("block"));
         block.setParams(newArrayList(ParamSpec.val(new Type("Parameter"))));
         block.setType(new Type("Parameter"));
         langSchema.put(block);
         ElementSchema type = new ElementSchema(new Symbol("type"));
         type.setParams(newArrayList(ParamSpec.sym(new Type("Symbol"))));
         type.setType(new Type("Type"));
         langSchema.put(type);
         ElementSchema defines = new ElementSchema(new Symbol("defines"));
         defines.setParams(newArrayList(ParamSpec.val(new Type("Number")), ParamSpec.sym(new Type("Symbol"))));
         defines.setType(new Type("Define"));
         langSchema.put(defines);
         ElementSchema defines_forward = new ElementSchema(new Symbol("defines_forward"));
         defines_forward.setParams(newArrayList(ParamSpec.val(new Type("Number")), ParamSpec.sym(new Type("Symbol"))));
         defines_forward.setType(new Type("Define"));
         langSchema.put(defines_forward);
         return langSchema;
     }
 
     public Schema getReadedSchema() {
         return schema;
     }
 }
