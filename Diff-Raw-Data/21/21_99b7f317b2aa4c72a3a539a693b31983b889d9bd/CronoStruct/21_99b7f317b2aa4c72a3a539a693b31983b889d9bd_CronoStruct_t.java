 package crono.type;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import crono.InterpreterException;
 import crono.Visitor;
 
 import crono.inf.InfDatabase;
 
 /**
  * A structure, similar to C structures.
  * CronoStruct inherits from Function so that they can be accessed as if their
  * name was a function, i.e.:
  *   (mystruct field value)
  * However their type inherits from the base CronoType. This is because they
  * are not 'really' a function, nor are they an atomic type.
  */
 public class CronoStruct extends Function {
     public static final TypeId TYPEID = new TypeId(":struct",
                                                    CronoStruct.class,
                                                    CronoType.TYPEID);
     private static final String _inv_field_name =
         "Invalid field name: (%s %s) does not exist";
     private static final String _field_type_mismatch =
         "Field %s expects %s, got %s";
     private static final TypeId[] _args = {Symbol.TYPEID, CronoType.TYPEID};
     
     public static class Field {
         public final Symbol sym;
         public final TypeId type;
         private CronoType data;
         
         public Field(Symbol sym, TypeId type, CronoType data) {
             this.sym = sym;
             this.type = type;
             this.data = data;
         }
         public CronoType put(CronoType data) {
             if(!(type.isType(data))) {
                 throw new InterpreterException(_field_type_mismatch, sym,
                                                type, data.typeId());
             }
             this.data = data;
             return data;
         }
         public CronoType get() {
             return data;
         }
        
        public Field copy() {
            return new Field(sym, type, data.copy());
        }
     }
     
     private static final String _malformed_field =
         "%s: malformed field default %s; expected (:symbol <:type> <:any>)";
     public static Map<String, Field> BuildFieldMap(String name, Cons list) {
         Map<String, Field> fields = new HashMap<String, Field>();
         Iterator<CronoType> iter;
         String str;
         Symbol sym;
         CronoType element, data;
         TypeId accept;
         boolean el2isType;
         for(CronoType ct: list) {
             if(!(ct instanceof Cons)) {
                 throw new InterpreterException(_malformed_field, name, ct);
             }
             
             accept = CronoType.TYPEID;
             data = Nil.NIL;
             iter = ((Cons)ct).iterator();
             str = null;
             sym = null;
             el2isType = false;
             if(iter.hasNext()) {
                 element = iter.next();
                 if(!(element instanceof Symbol)) {
                     throw new InterpreterException(_malformed_field, name, ct);
                 }
                 sym = (Symbol)element;
                 str = sym.toString();
             }
             if(iter.hasNext()) {
                 element = iter.next();
                 if(element instanceof CronoTypeId) {
                     accept = ((CronoTypeId)element).type;
                     el2isType = true;
                 }else {
                     data = element;
                 }
             }
             if(iter.hasNext()) {
                 element = iter.next();
                 if(element instanceof CronoTypeId) {
                     if(el2isType) {
                         throw new InterpreterException(_malformed_field, name,
                                                        ct);
                     }
                     accept = ((CronoTypeId)element).type;
                 }else if(!el2isType) {
                     throw new InterpreterException(_malformed_field, name, ct);
                 }else {
                     data = element;
                 }
             }
             if(iter.hasNext()) {
                 throw new InterpreterException(_malformed_field, name, ct);
             }
             fields.put(str, new CronoStruct.Field(sym, accept, data));
         }
         return fields;
     }
     
     public final String name;
     public final CronoStruct parent;
     private Map<String, Field> fields;
     private TypeId type;
     
     public CronoStruct(String name, Map<String, Field> fields) {
         this(name, fields, null);
     }
     public CronoStruct(String name, Map<String, Field> fields, CronoStruct p)
     {
         super(_args, CronoType.TYPEID, 1, true, Function.EvalType.NONE);
         this.name = name;
         this.parent = p;
         this.fields = new HashMap<String, Field>();
         TypeId id = TYPEID;
         if(p != null) {
             fields.putAll(parent.fields);
             id = p.type;
         }
         this.fields.putAll(fields);
         this.type = new TypeId(":struct-"+name, CronoStruct.class, id);
     }
     protected CronoStruct(CronoStruct cs) {
         super(_args, CronoType.TYPEID, 1, true, Function.EvalType.NONE);
         this.name = cs.name;
         this.parent = cs.parent;
        
         this.fields = new HashMap<String, Field>();
        for(Map.Entry<String,Field> item: cs.fields.entrySet()){
            fields.put(item.getKey(), item.getValue().copy());
         }
         this.type = cs.type;
     }
     
     public CronoType run(Visitor v, CronoType[] args) {
         /* CronoStruct doesn't contain an AST, so we can ignore the visitor */
         Field field;
         switch(args.length) {
         case 1:
             /* Being used as a get */
             field = fields.get(args[0].toString());
             if(field == null) {
                 throw new InterpreterException(_inv_field_name, name,
                                                args[0].toString());
             }
             return field.get();
         case 2:
             /* Being used as a set */
             field = fields.get(args[0].toString());
             if(field == null) {
                 throw new InterpreterException(_inv_field_name, name,
                                                args[0].toString());
             }
             InfDatabase.clearEntailments();
             
             return field.put(args[1].accept(v));
         default:
             throw new InterpreterException("Too many arguments to struct");
         }
     }
     
     public CronoStruct copy() {
         return new CronoStruct(this);
     }
     
     public TypeId typeId() {
         return type;
     }
     
     public String toString() {
         return name;
     }
     
     public boolean equals(Object o) {
         return true;
     }
 }
