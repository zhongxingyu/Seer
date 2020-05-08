 package com.bookofbrilliantthings.mustache4j;
 
 import java.io.Writer;
 import java.lang.reflect.Field;
 
 public class FieldRenderer
     implements FragmentRenderer
 {
     private final Field field;
     private final PrimitiveType primitiveType;
 
     public FieldRenderer(final Field field)
     {
         this.field = field;
        final Class<?> theClass = field.getClass(); // TODO what if its type is given by a TypeVariable?
         primitiveType = PrimitiveType.getSwitchType(theClass);
     }
 
     @Override
     public void render(final Writer writer, final Object o)
         throws Exception
     {
         // we don't check o's type because we've specifically compiled the fragment list for o
         switch(primitiveType)
         {
         case BOOLEAN:
         {
             final boolean v = field.getBoolean(o);
             writer.write(Boolean.toString(v));
             break;
         }
 
         case BYTE:
         {
             final byte v = field.getByte(o);
             writer.write(Byte.toString(v));
             break;
         }
 
         case CHAR:
         {
             final char v = field.getChar(o);
             writer.write(v);
             break;
         }
 
         case DOUBLE:
         {
             final double v = field.getDouble(o);
             writer.write(Double.toString(v));
             break;
         }
 
         case FLOAT:
         {
             final float v = field.getFloat(o);
             writer.write(Float.toString(v));
             break;
         }
 
         case INT:
         {
             final int v = field.getInt(o);
             writer.write(Integer.toString(v));
             break;
         }
 
         case LONG:
         {
             final long v = field.getLong(o);
             writer.write(Long.toString(v));
             break;
         }
 
         case OBJECT:
         {
             final Object o2 = field.get(o);
             writer.write(o2.toString());
             break;
         }
 
         case SHORT:
         {
             final short v = field.getShort(o);
             writer.write(Short.toString(v));
             break;
         }
 
         case VOID:
             throw new IllegalStateException("a field cannot have type (void)");
         }
     }
 
 }
