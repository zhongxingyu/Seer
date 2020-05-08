 package com.bookofbrilliantthings.mustache4j;
 
 import java.io.Writer;
 import java.lang.reflect.Field;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.util.Iterator;
 import java.util.List;
 
 public class ListRenderer
     implements FragmentRenderer
 {
     private final ObjectRenderer objectRenderer;
     private final Field field;
 
     public ListRenderer(final List<FragmentRenderer> fragmentList, final Field field)
         throws MustacheParserException
     {
         if (field.getType() != List.class)
             throw new IllegalArgumentException("field must be a List<T>");
 
         // if we passed the above, we know this is the correct generic type
         final ParameterizedType fieldType = (ParameterizedType)field.getGenericType();
         final Type typeArguments[] = fieldType.getActualTypeArguments();
 
         // for a List<>, we know there can be only one
         final Type typeArgument = typeArguments[0];
 
         // if it's a GenericArrayType, we don't support that (don't know how to render the array)
         if (typeArgument instanceof GenericArrayType)
             throw new MustacheParserException(null, "don't know how to render a list of arrays"); // TODO
 
         // if it's a WildcardType (<?>), we don't support that (don't know how to render the target)
         if (typeArgument instanceof WildcardType)
             throw new MustacheParserException(null, "don't know how to render an unknown type"); // TODO
 
         // if it's a TypeVariable, then we can find out the type from the declaring class
         if (typeArgument instanceof TypeVariable)
         {
             throw new RuntimeException("unimplemented");
         }
 
         // if it's a ParameterizedType, dive in again; as long as it's an object, and we can resolve it, we can do it
         if (typeArgument instanceof ParameterizedType)
         {
             throw new RuntimeException("unimplemented");
         }
 
         // if it's a class, we've got the target we need for our object renderer
         // we have to check this last in case it's a parameterized class, at least until we make
         // the FieldRenderer capable of handling TypeVariables
         if (typeArgument instanceof Class<?>)
         {
             // create an object renderer that can operate on the list elements
             objectRenderer = new ObjectRenderer(fragmentList, (Class<?>)typeArgument);
             this.field = field;
         }
 
         throw new RuntimeException("shouldn't get here; missing case!");
     }
 
     @Override
     public void render(final Writer writer, final Object o)
         throws Exception
     {
         final List<?> list = (List<?>)field.get(o);
         if (list == null)
             return;
 
         final Iterator<?> iterator = list.iterator();
         while(iterator.hasNext())
         {
             objectRenderer.render(writer, iterator.next());
         }
     }
 
     private static class MyFactory
         implements RendererFactory
     {
         private final List<FragmentRenderer> fragmentList;
         private final Field field;
 
         MyFactory(List<FragmentRenderer> fragmentList, Field field)
         {
             this.fragmentList = fragmentList;
             this.field = field;
         }
 
         @Override
         public FragmentRenderer createRenderer()
         {
            throw new RuntimeException("unimplemented");
            //return new ListRenderer(fragmentList, field);
         }
     }
 
     public static RendererFactory createFactory(List<FragmentRenderer> fragmentList, Field field)
     {
         return new MyFactory(fragmentList, field);
     }
 }
