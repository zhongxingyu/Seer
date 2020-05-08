 package net.unit8.sastruts.easyapi.converter;
 
 import org.seasar.framework.util.StringUtil;
 
 import net.unit8.sastruts.easyapi.dto.EasyApiMessageDto;
 import net.unit8.sastruts.easyapi.dto.HeaderDto;
 import net.unit8.sastruts.easyapi.dto.RequestDto;
 import net.unit8.sastruts.easyapi.dto.ResponseDto;
 
 import com.thoughtworks.xstream.XStreamException;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 public class RequestConverter implements Converter {
 	public ThreadLocal<Class<?>> bodyDtoClass = new ThreadLocal<Class<?>>();
 
 	@SuppressWarnings("rawtypes")
 	public boolean canConvert(Class clazz) {
 		return EasyApiMessageDto.class.isAssignableFrom(clazz);
 	}
 
 	public void marshal(Object source, HierarchicalStreamWriter writer,
 			MarshallingContext context) {
 		EasyApiMessageDto dto = (EasyApiMessageDto) source;
		writer.startNode("head");
 		context.convertAnother(dto.header);
		writer.endNode();
 		writer.startNode("body");
 		context.convertAnother(dto.body);
 		writer.endNode();
 	}
 
 	public Object unmarshal(HierarchicalStreamReader reader,
 			UnmarshallingContext context) {
 		EasyApiMessageDto dto = null;
 		String nodeName = reader.getNodeName();
 		if (StringUtil.equals(nodeName, "request")) {
 			dto = new RequestDto();
 		} else if (StringUtil.equals(nodeName, "response")) {
 			dto = new ResponseDto();
 		} else {
 			throw new XStreamException("unknown node name");
 		}
 		while (reader.hasMoreChildren()) {
 			reader.moveDown();
 			if (StringUtil.equals(reader.getNodeName(), "header")) {
 				dto.header = (HeaderDto)context.convertAnother(dto, HeaderDto.class);
 			} else if (StringUtil.equals(reader.getNodeName(), "body")) {
 				dto.body = context.convertAnother(dto, bodyDtoClass.get());
 			}
 			reader.moveUp();
 		}
 		return dto;
 	}
 }
