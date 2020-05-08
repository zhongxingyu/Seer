 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 
 package de.ailis.jasdoc.types;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.ailis.jasdoc.util.JsTypeUtils;
 
 /**
  * Factory for JavaScript types.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public class TypeFactory
 {
     /** Cached types. */
     private final Map<String, AbstractType> cache =
         new HashMap<String, AbstractType>();
 
     /**
      * Performs an initial parsing of a type expression. This means top-level
      * curly braces are removed before starting the real type parsing. This
      * allows to support type expressions like "number" and "{number}".
      *
      * @param expression
      *            The type expression.
      * @return The concrete type.
      */
     public final AbstractType parseInitial(final String expression)
     {
         if (expression.startsWith("{") && expression.endsWith("}"))
             return valueOf(expression.substring(1, expression.length() - 1));
         else
             return valueOf(expression);
     }
 
     /**
      * Converts the specified type expression into a concrete type.
      *
      * @param expression
      *            The type expression.
      * @return The concrete type.
      */
     public final AbstractType valueOf(final String expression)
     {
         AbstractType type = this.cache.get(expression);
         if (type != null) return type;
         type = createType(expression);
         this.cache.put(expression, type);
         return type;
     }
 
     /**
      * Creates the type for the specified expression.
      *
      * @param expression
      *            The type expression.
      * @return The created type.
      */
     private AbstractType createType(final String expression)
     {
         final String[] parts = JsTypeUtils.split(expression, '|');
         if (parts.length > 1)
         {
             final List<AbstractType> types = new ArrayList<AbstractType>();
             for (final String part: parts)
                 types.add(valueOf(part));
             return new UnionType(types);
         }
 
         if ("*".equals(expression))
             return new AnyType();
         if ("string".equals(expression))
             return new StringType();
         if ("number".equals(expression))
             return new NumberType();
         if ("boolean".equals(expression))
             return new BooleanType();
 
         if (expression.startsWith("..."))
             return new VarArgType(valueOf(expression.substring(3)));
         if (expression.endsWith("="))
             return new OptionalType(valueOf(expression.substring(0,
                 expression.length() - 1)));
         if (expression.startsWith("!"))
             return new NonNullType(valueOf(expression.substring(1)));
         if (expression.startsWith("?"))
             return new NullableType(valueOf(expression.substring(1)));
         if (expression.startsWith("Array.<"))
             return new ArrayType(valueOf(expression.substring(
                 7, JsTypeUtils.findEnd(expression, 6))));
        if (expression.startsWith("Array<"))
            return new ArrayType(valueOf(expression.substring(
                6, JsTypeUtils.findEnd(expression, 5))));
         if (expression.startsWith("Object.<"))
         {
             final int end = JsTypeUtils.findEnd(expression, 7);
             final String[] args = JsTypeUtils.split(
                 expression.substring(8, end), ',');
             if (args.length == 2)
                 return new MapType(valueOf(args[0]), valueOf(args[1]));
         }
        if (expression.startsWith("Object<"))
        {
            final int end = JsTypeUtils.findEnd(expression, 6);
            final String[] args = JsTypeUtils.split(
                expression.substring(7, end), ',');
            if (args.length == 2)
                return new MapType(valueOf(args[0]), valueOf(args[1]));
        }
        if (expression.contains(".<")) {
            return new ClassType(expression.substring(0, expression.indexOf(".<")));
        }
        if (expression.contains("<")) {
            return new ClassType(expression.substring(0, expression.indexOf('<')));
        }
         return new ClassType(expression);
     }
 }
