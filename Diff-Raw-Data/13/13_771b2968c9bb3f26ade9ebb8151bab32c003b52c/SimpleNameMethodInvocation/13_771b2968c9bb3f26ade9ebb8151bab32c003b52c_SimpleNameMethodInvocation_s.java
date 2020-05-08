 package chameleon.support.member.simplename;
 
 import java.util.List;
 
 import org.rejuse.logic.ternary.Ternary;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.expression.InvocationTarget;
 import chameleon.core.expression.NonConstructorInvocation;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.method.Method;
 import chameleon.core.method.MethodHeader;
 import chameleon.core.relation.WeakPartialOrder;
 import chameleon.core.type.Type;
 import chameleon.support.member.MoreSpecificTypesOrder;
 
 public abstract class SimpleNameMethodInvocation<I extends SimpleNameMethodInvocation, D extends Method> extends NonConstructorInvocation<I,D> {
 
   public SimpleNameMethodInvocation(InvocationTarget target, String name) {
     super(target);
     setName(name);
   }
   
   /********
    * NAME *
    ********/
 
   private String _methodName;
 
   public String getName() {
     return _methodName;
   }
 
   public void setName(String method) {
     _methodName = method;
   }
 
 //  public D getMethod() throws LookupException {
 //	    D result = lexicalContext().lookUp(selector());
 //	    if(result == null) {
 //	      throw new LookupException();
 //	    }
 //	    return result;
 //  }
 
 
   public abstract class SimpleNameMethodSelector extends DeclarationSelector<D> {
     
     public D filter(Declaration declaration) throws LookupException {
       D result = null;
       if(selectedClass().isInstance(declaration)) {
         D decl = (D)declaration;
         List<Type> actuals = getActualParameterTypes();
         List<Type> formals = ((MethodHeader)decl.header()).getParameterTypes();
         if((decl.is(language().CONSTRUCTOR) != Ternary.TRUE) &&
         	 new MoreSpecificTypesOrder().contains(actuals,formals) && 
            ((SimpleNameMethodSignature)decl.signature()).getName().equals(getName())) {
           result = decl;
         }
       }
       return result;
     }
 
     @Override
     public WeakPartialOrder<D> order() {
       return new WeakPartialOrder<D>() {
         @Override
         public boolean contains(D first, D second)
             throws LookupException {
           return new MoreSpecificTypesOrder().contains(((MethodHeader) first.header()).getParameterTypes(), ((MethodHeader) second.header()).getParameterTypes());
         }
       };
     }
   }
 }
