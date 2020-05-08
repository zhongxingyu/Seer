 package com.feefighers.model;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.SingleValueConverter;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.converters.basic.BooleanConverter;
 import com.thoughtworks.xstream.converters.basic.DateConverter;
 import com.thoughtworks.xstream.converters.basic.IntConverter;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
 
 public class XmlMarshaller {
 
 	private static final XmlMarshaller instance = new XmlMarshaller();
 	
 	private static XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_DOLLAR_", "_"))); 
 	
 	public static class SingleValueConverterTypeWrapper implements Converter {
 		private final SingleValueConverter converter;
 		private final String type;
 		
 		public SingleValueConverterTypeWrapper(SingleValueConverter converter, String type) {
 			this.converter = converter;
 			this.type = type;
 		}
 		
 		@Override
 		public boolean canConvert(Class classType) {
 			return converter.canConvert(classType);
 		}
 
 		@Override
 		public void marshal(Object obj, HierarchicalStreamWriter writer,
 				MarshallingContext context) {
 			writer.addAttribute("type", type);
 			writer.setValue(converter.toString(obj));
 		}
 
 		@Override
 		public Object unmarshal(HierarchicalStreamReader reader,
 				UnmarshallingContext context) {			
 			return converter.fromString(reader.getValue());
 		}
 	}
 	
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";
	private static final String[] DATE_FORMATS = { };
 	static {		
		
		xstream.registerConverter(new SingleValueConverterTypeWrapper(new DateConverter(DEFAULT_DATE_FORMAT, DATE_FORMATS), "datetime"));
 		xstream.registerConverter(new SingleValueConverterTypeWrapper(new BooleanConverter(), "boolean"));
 		xstream.registerConverter(new SingleValueConverterTypeWrapper(new IntConverter(), "integer"));
 	}	
 		
 	public static void registerModelClass(Class cl) {
 		xstream.processAnnotations(cl);
 	}
 	
 	public static String toXml(Object obj) {
 		return xstream.toXML(obj);
 	}
 	
 	public static Object fromXml(String xml) {
 		return xstream.fromXML(xml);
 	}
 }
