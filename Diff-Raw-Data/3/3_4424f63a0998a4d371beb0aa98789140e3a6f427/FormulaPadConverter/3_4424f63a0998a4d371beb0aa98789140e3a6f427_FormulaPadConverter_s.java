 package org.eclipse.iee.sample.formula.storage;
 
 import org.eclipse.iee.sample.formula.pad.FormulaPad;
 
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 public class FormulaPadConverter implements Converter  {
 	
 	@Override
     public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
         return type.equals(FormulaPad.class);
     }
 	
 	@Override
 	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
 		FormulaPad pad = (FormulaPad) source;
		
		writer.addAttribute("type", pad.getType());
 		writer.addAttribute("translating_expression", pad.getTranslatingExpression());
 		writer.addAttribute("original_expression", pad.getOriginalExpression());
 	}
 
 	@Override
 	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
 		FormulaPad pad = new FormulaPad();
 		
 		pad.setTranslatingExression(reader.getAttribute("translating_expression"));
 		pad.setOriginalExression(reader.getAttribute("original_expression"));
 		return pad;
 	}
 }
