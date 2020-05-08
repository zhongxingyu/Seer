 package languish.util;
 
 import java.util.List;
 
 import languish.lambda.NativeFunction;
 import languish.lambda.Operation;
 import languish.lambda.Operations;
 import languish.lambda.Primitive;
 import languish.lambda.Term;
 import languish.primitives.AbstractPrimitive;
 import languish.primitives.LBoolean;
 import languish.primitives.LCharacter;
 import languish.primitives.LInteger;
 import languish.primitives.LSymbol;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public class Lambda {
 
   public static final Term TRUE = abs(abs(ref(2)));
 
   public static final Term FALSE = abs(abs(ref(1)));
 
   public static Term abs(Term exp) {
     return new Term(Operations.ABS, exp, Term.NULL);
   }
 
   public static Term app(Term func, Term arg) {
     return new Term(Operations.APP, func, arg);
   }
 
   public static Term primitive(Primitive obj) {
     return new Term(Operations.PRIMITIVE, obj, Term.NULL);
   }
 
   public static Term ref(int i) {
     return new Term(Operations.REF, i, Term.NULL);
   }
 
   public static Term nativeApply(final NativeFunction func, Term arg) {
     Primitive funcPrimitive = new AbstractPrimitive() {
       public Object getJavaObject() {
         return func;
       }
     };
 
     return new Term(Operations.NATIVE_APPLY, primitive(funcPrimitive), arg);
   }
 
   public static Term cons(Term obj1, Term obj2) {
     return abs(app(app(ref(1), obj1), obj2));
   }
 
   public static Term car(Term obj) {
     return app(obj, TRUE);
   }
 
   public static Term cdr(Term obj) {
     return app(obj, FALSE);
   }
 
   public static Term branch(Term condition, Term thenTerm, Term elseTerm) {
     return app(app(condition, thenTerm), elseTerm);
   }
 
   private Lambda() {
   }
 
   public static Term convertJavaObjectToTerm(JavaWrapper obj) {
     if (obj.isList()) {
       List<JavaWrapper> list = obj.asList();
 
       Term result = Term.NULL;
       for (int i = list.size() - 1; i >= 0; i--) {
         result = Lambda.cons(convertJavaObjectToTerm(list.get(i)), result);
       }
 
       return result;
     } else if (obj.isBoolean()) {
       return Lambda.primitive(LBoolean.of(obj.asBoolean()));
     } else if (obj.isCharacter()) {
       return Lambda.primitive(LCharacter.of(obj.asCharacter()));
     } else if (obj.isInteger()) {
       return Lambda.primitive(LInteger.of(obj.asInteger()));
     } else if (obj.isString()) {
       return Lambda.primitive(LSymbol.of(obj.asString()));
     } else {
       throw new IllegalArgumentException("Invalid object: " + obj);
     }
   }
 
   public static JavaWrapper convertTermToJavaObject(Term term) {
     term = term.reduceCompletely();
 
     Operation op = term.getOperation();
     if (op == Operations.NOOP) {
       return JavaWrapper.of(ImmutableList.of());
     }
 
     if (op == Operations.PRIMITIVE) {
       return JavaWrapper.of(((Primitive) term.getFirst()).getJavaObject());
     }
 
     if (op == Operations.ABS) {
       JavaWrapper car = convertTermToJavaObject(Lambda.car(term));
       List<JavaWrapper> cdr =
           convertTermToJavaObject(Lambda.cdr(term)).asList();
 
       List<Object> result = Lists.newLinkedList();
 
       result.add(car);
       result.addAll(cdr);
 
       return JavaWrapper.of(ImmutableList.copyOf(result));
     }
 
     throw new IllegalArgumentException("term is not in a convertible state: "
         + term);
   }
 
 }
