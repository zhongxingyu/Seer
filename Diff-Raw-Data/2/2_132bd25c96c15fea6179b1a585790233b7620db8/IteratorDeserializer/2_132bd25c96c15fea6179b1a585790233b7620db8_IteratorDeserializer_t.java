 package com.caucho.hessian.io;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Vector;
 
 /**
  * Deserializing a JDK 1.2 Iterator.
  */
 public class IteratorDeserializer extends AbstractListDeserializer
 {
     private static IteratorDeserializer _deserializer;
 
     public static IteratorDeserializer create()
     {
         if (_deserializer == null)
         {
             _deserializer = new IteratorDeserializer();
         }
 
         return _deserializer;
     }
 
     public Object readList(AbstractHessianInput in, int length) throws IOException
     {
         Collection list = new Vector();
 
         while (!in.isEnd())
         {
             list.add(in.readObject());
         }
 
         in.readEnd();
 
         return list.iterator();
     }
 }
