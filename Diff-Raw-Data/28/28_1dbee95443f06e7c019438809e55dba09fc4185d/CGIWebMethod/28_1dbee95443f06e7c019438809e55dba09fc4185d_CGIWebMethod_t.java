 package com.purplefrog.httpcliches;
 
 import java.lang.annotation.*;
 import java.lang.reflect.*;
 import java.util.*;
 import javax.jws.*;
 
 import org.apache.log4j.*;
 
 /**
  * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
  * $Author thoth $
  * $Date 6/26/12 $
  */
 public class CGIWebMethod
 {
     private static final Logger logger = Logger.getLogger(CGIWebMethod.class);
 
     /**
      * Scan the parameters to the {@link Method} m for {@link WebParam} annotations.
      * Then extract the relevant values from the cgiArgs and transform them into the correct type.
      * @return an array of Objects suitable for use as parameters for a call to {@link Method#invoke(Object, Object...)}.
      *
     * @see #transform(Class, Annotation[], CGIEnvironment, String)
      */
     public static Object[] transformCGIArgumentsToJavaParams(Method m, CGIEnvironment cgiEnv)
         throws CGISOAPTransformException
     {
         Class<?>[] pt = m.getParameterTypes();
         Annotation[][] pa = m.getParameterAnnotations();
 
         Object[] params = new Object[pt.length];
         for (int i=0; i<pt.length; i++) {
            String annotationDebugTag = m.getDeclaringClass().getName()
                +"."+m.getName()
                + "(param[" + i + "] a " + pt[i].getSimpleName() + ")";
            params[i] = transform(pt[i], pa[i], cgiEnv, annotationDebugTag);
         }
         return params;
     }
 
     /**
      * Scan annotations for a {@link WebParam} and use its name as the key to the cgiArgs map.
      * Transform the value (or values) from the map into an object of class parameterType.
      *
      *
      *
      * @param parameterType the class of the object we are supposed to return.
      * {@link Collection}s are not supported (since type erasure means we have no way of determining the correct element type).
      * @param annotations all annotations on the parameter.  There should be a WebParam in there, or we're going to throw an exception.
      * @return an object of class parameterType extracted from cgiArgs according to the WebParam in annotations
      * @throws CGISOAPTransformException
      */
    public static Object transform(Class<?> parameterType, Annotation[] annotations, CGIEnvironment cgiEnv, String annotationDebugTag)
         throws CGISOAPTransformException
     {
         Map<String, List<String>> cgiArgs = cgiEnv.args;
         WebParam a = grep(annotations, WebParam.class);
         if (a == null)
             throw new CGISOAPTransformException("parameter lacks WebParam annotation", null);
 
         if (parameterType.equals(Map.class) && a.name().length() <1) {
             return cgiArgs;
         }
 
         if (parameterType.equals(CGIEnvironment.class) && a.name().length() <1) {
             return cgiEnv;
         }
 
         if (a.name()==null || a.name().length()<1)
            throw new CGISOAPTransformException("WebParam annotation for " +annotationDebugTag+
                " has blank name", a.name());
 
         List<String> arg = cgiArgs.get(a.name());
 
         if (parameterType.isArray()) {
             return transformArray(arg, parameterType.getComponentType(), annotations);
         }
 
         // must be a scalar
 
         String arg_ = HTMLTools.firstOrNull(arg);
 
         if (parameterType.isAssignableFrom(int.class)) {
             if (arg_==null)
                 throw new CGISOAPTransformException("missing parameter "+a.name()+" can't be omitted", a.name());
             return Integer.parseInt(arg_);
 
         } else if (parameterType.isAssignableFrom(Integer.class)) {
             if (arg_==null)
                 return null;
             else if (0==arg_.length())
                 return null;
             else
                 return Integer.parseInt(arg_);
 
         } else if (parameterType.isAssignableFrom(double.class)) {
             if (arg_==null)
                 throw new CGISOAPTransformException("missing parameter "+a.name()+" can't be omitted", a.name());
             return Double.parseDouble(arg_);
 
         } else if (parameterType.isAssignableFrom(boolean.class)) {
             if (arg_==null)
                 throw new CGISOAPTransformException("missing parameter "+a.name()+" can't be omitted", a.name());
 
             String argl = arg_.toLowerCase();
             return "true".equals(argl) || "on".equals(argl) || "1".equals(argl);
 
         } else if (parameterType.isAssignableFrom(Boolean.class)) {
             if (arg_==null)
                 return null;
 
             String argl = arg_.toLowerCase();
             return "true".equals(argl) || "on".equals(argl) || "1".equals(argl);
 
         } else if (parameterType.isAssignableFrom(long.class)) {
             if (arg_==null)
                 throw new CGISOAPTransformException("missing parameter "+a.name()+" can't be omitted", a.name());
 
             return Long.parseLong(arg_);
 
         } else if (parameterType.isAssignableFrom(Long.class)) {
             if (arg_==null)
                 return null;
 
             return Long.parseLong(arg_);
 
         } else if (parameterType.isAssignableFrom(String.class)) {
             return arg_;
 
         } else {
             throw new CGISOAPTransformException("unsupported parameter type "+parameterType.getName(), null);
         }
     }
 
     public static Object transformArray(List<String> args, Class<?> parameterType, Annotation[] annotations)
         throws CGISOAPTransformException
     {
 
         if (parameterType.isAssignableFrom(int.class)) {
 
             int[] rval = new int[args.size()];
             int j=0;
             for (int i = 0; i < rval.length; i++) {
 
                 String s = args.get(i);
                 if (s!=null)
                     rval[j++] = Integer.parseInt(s);
             }
 
             if (j!=rval.length) {
                 rval = Arrays.copyOf(rval, j);
             }
             return rval;
 
         } else if (parameterType.isAssignableFrom(Integer.class)) {
 
             Integer[] rval = new Integer[args.size()];
             for (int i = 0; i < rval.length; i++) {
                 rval[i] = Integer.parseInt(args.get(i));
             }
             return rval;
 
         } else if (parameterType.isAssignableFrom(String.class)) {
 
             if (args==null)
                 return new String[0];
 
             return args.toArray(new String[args.size()]);
         } else {
             throw new CGISOAPTransformException("unsupported parameter type "+parameterType.getClass().getName()+"[]", null);
         }
     }
 
     private static <T extends Annotation>  T grep(Annotation[] annotations, Class<T> cls)
     {
         for (Annotation a : annotations) {
             if (cls.isAssignableFrom(a.getClass()))
                 return (T) a;
         }
         return null;
     }
 
     public static Method matchName(Class cls, String name)
     {
         Method[] ms = cls.getMethods();
         for (Method m : ms) {
             String otherName = webNameFor(m);
             if (name.equals(otherName)) {
                 return m;
             }
         }
         return null;
     }
 
     public static String webNameFor(Method m)
     {
         WebMethod a = m.getAnnotation(WebMethod.class);
         if (a==null)
             return null;
 
         if (a.operationName()==null || a.operationName().length()<1)
             return m.getName();
         else
             return a.operationName();
     }
 
     public static Object invokeCGI(Object thingus, String methodName, CGIEnvironment cgiEnv)
         throws InvocationTargetException, IllegalAccessException, CGISOAPTransformException, NoSuchMethodException
     {
         Class<?> cls = thingus.getClass();
         Method m = matchName(cls, methodName);
 
         if (m==null) {
             throw new NoSuchMethodException(cls.getName()+" has no method with @WebMethod matching '"+methodName+"'");
         }
 
         Object[] params = transformCGIArgumentsToJavaParams(m, cgiEnv);
 
         return m.invoke(thingus, params);
     }
 
     public static class CGISOAPTransformException
         extends Exception
     {
         public final String parameterName;
 
         public CGISOAPTransformException(String msg, String parameterName)
         {
             super(msg);
             this.parameterName = parameterName;
         }
     }
 }
