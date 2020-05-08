 package cern.accsoft.steering.jmad.util.xml.converters;
 
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 /**
  * This is a converter to specially format a string for references
  */
 public class NameRefConverter implements Converter {
 
 	@Override
 	public void marshal(Object object, HierarchicalStreamWriter writer,
 			MarshallingContext ctx) {
 		String valueString = (String) object;
 		writer.addAttribute("ref-name", valueString);
 	}
 
 	@Override
 	public Object unmarshal(HierarchicalStreamReader reader,
 			UnmarshallingContext ctx) {
 		return reader.getAttribute("ref-name");
 	}
 
	@SuppressWarnings("unchecked")
 	@Override
	public boolean canConvert(Class clazz) {
 		return clazz.equals(String.class);
 	}
 }
