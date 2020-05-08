 package uk.co.jezuk.mango;
 
 import java.lang.reflect.Method;
 
 /**
  * Object method adaptors.
  * @see Bind
  * @author Jez Higgins, jez@jezuk.co.uk
  * @version $Id$
  */
 public class Adapt
 {
   /**
    * Compose is a unary function adaptor.  If <code>f</code> and <code>g</code>
   * are <code>UnaryFunctions</code>, then <code>Compose</code> creates a new
    * function <code>h</code>, where <code>h(x)</code> is equal to <code>f(g(x))</code>.
    */
   static public UnaryFunction Compose(UnaryFunction f, UnaryFunction g)
   {
     final UnaryFunction ff = f;
     final UnaryFunction gg = g;
     return new UnaryFunction() {
 	public Object fn(Object x)
 	{
 	  return ff.fn(gg.fn(x));
 	} // fn
       };
   } // Compose
 
   /**
    * Compose is a function adaptor.  If <code>f</code> is a <code>BinaryFunction</code>
    * and <code>g1</code> and <code>g2</code> are <code>UnaryFunctions</code>, then Compose
    * returns a new <code>BinaryFunction</code> <code>h</code> such that <code>h(x, y)</code>
    * is <code>f(g1(x), g2(y))</code>
    */
   static public BinaryFunction Compose(BinaryFunction f, UnaryFunction g1, UnaryFunction g2)
   {
     final BinaryFunction ff = f;
     final UnaryFunction gg1 = g1;
     final UnaryFunction gg2 = g2;
     return new BinaryFunction() {
 	public Object fn(Object x, Object y)
 	{
 	  return ff.fn(gg1.fn(x), gg2.fn(y));
 	} // fn
       };
   } // Compose
 
   /**
    * Adapts member functions as <code>UnaryFunction</code> objects, allowing them
    * to be passed to algorithms.
    * <br>
    * e.g. to print all the elements in a list<br>
    * <code>Mango.forEach(list, Adapt.Method(System.out, "println"));</code><br>
    * is equivalent to <br>
    * <code>for(int i = 0; i < list.size(); ++i)</code><br>
    * <code>  System.out.println(list.get(i));</code>
    * <p>
    * If the named method is not found, or its signature is incorrect throws a
    * RuntimeException.  If multiple methods have the correct name, and take a single
    * parameter one of them will be called, but you can't determine which.
    */
   static public UnaryFunction Method(final Object obj, String methodName)
   {
     return wrapMethod(obj.getClass(), obj, methodName);
   } // Method 
 
   /**
    * Adapts static member functions as <code>UnaryFunction</code> objects, allowing them
    * to be passed to algorithms.
    * <p>
    * If the named method is not found, or its signature is incorrect throws a
    * RuntimeException.  If multiple methods have the correct name, and take a single
    * parameter one of them will be called, but you can't determine which.
    */
   static public UnaryFunction Method(final Class klass, String methodName)
   {
     return wrapMethod(klass, null, methodName);
   } // Method
 
   static private class UnaryMethodNamed implements Predicate
   {
       UnaryMethodNamed(String name, int argCount) 
       { 
 	name_ = name; 
 	argCount_ = argCount;
       } // UnaryMethodNamed
       public boolean test(Object obj)
       {
         java.lang.reflect.Method m = (java.lang.reflect.Method)obj;
 	return (m.getName().equals(name_) && (m.getParameterTypes().length == argCount_));
       } // test
       private String name_;
       private int argCount_;
   } // UnaryMethodNamed
 
   static private UnaryFunction wrapMethod(final Class klass, final Object obj, String methodName)
   {
     Method[] methods = klass.getMethods();
     final Method m = (Method)Mango.findIf(java.util.Arrays.asList(methods).iterator(), new UnaryMethodNamed(methodName, 1));
     if(m == null)
       throw new RuntimeException(new NoSuchMethodException());
 
     return new UnaryFunction() {
       private Object obj_;
       private Method method_;
       { obj_ = obj; method_ = m; }
       public Object fn(Object arg) 
       { 
         Object[] args = new Object[]{ arg };
         try {
 	  return method_.invoke(obj_, args);
         } // try
 	catch(Exception e) {
 	  throw new RuntimeException(e);
 	} // catch
       } // fn
     }; // UnaryFunction
   } // wrapMethod
 
   /**
    * Creates a <code>UnaryFunction</code> which will call a method on the
    * object passed as the argument to <code>UnaryFunction.fn</code> method.
    * <br>
    * e.g. to print all the elements in a list<br>
    * <code>interface Something { void persist(); }<br>
    * // fill list with Somethings
    * Mango.forEach(list, Bind.ArgumentMethod("persist"));</code><br>
    * is equivalent to <br>
    * <code>for(int i = 0; i < list.size(); ++i)<br>
    * {<br>
    * &nbsp;&nbsp;Something s = (Something)list.get(i);<br>
    * &nbsp;&nbsp;s.persist();<br>
    * }</code>
    * <p>
    * If the named method is not found, or its signature is incorrect throws a
    * RuntimeException.  
    * @see UnaryFunction
    */
   static public UnaryFunction ArgumentMethod(final String methodName)
   {
     return new UnaryFunction() {
       private String methodName_;
       private Class lastClass_;
       private Method method_;
       { methodName_ = methodName; }
       public Object fn(Object arg) 
       { 
 	if(!arg.getClass().equals(lastClass_))
 	{
 	  lastClass_ = arg.getClass();
 	  Method[] methods = lastClass_.getMethods();
 	  method_ = (Method)Mango.findIf(java.util.Arrays.asList(methods).iterator(), new UnaryMethodNamed(methodName, 0));
 	  if(method_ == null)
 	    throw new RuntimeException(new NoSuchMethodException());
 	} // if ...
 	  
 	try {
 	  return method_.invoke(arg, null);
 	} // try
 	catch(Exception e) {
 	  throw new RuntimeException(e);
 	} // catch
       } // fn
     }; // UnaryFunction
   } // ArgumentMethod
 
   //////////////////////////////////////////
   private Adapt() { }
 } // Adapt
