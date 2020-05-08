 package de.flower.common.util.xstream;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
 import com.thoughtworks.xstream.core.JVM;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 /**
  * @author flowerrrr
  */
 public class ClassEmittingReflectionConverter extends ReflectionConverter {
 
    private HierarchicalStreamWriter _writer;

     public ClassEmittingReflectionConverter(XStream xstream) {
         super(xstream.getMapper(), new JVM().bestReflectionProvider());
     }
 
     @Override
     public void marshal(Object original, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        this._writer = writer;

         writer.addAttribute("type", original.getClass().getName());

         super.marshal(original, writer, context);
        this._writer = null;
     }
 }
