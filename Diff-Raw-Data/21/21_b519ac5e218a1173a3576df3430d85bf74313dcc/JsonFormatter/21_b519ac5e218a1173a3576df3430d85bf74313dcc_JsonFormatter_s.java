 package com.wesabe.xmlson;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.Map.Entry;
 
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 
 /**
  * A class which formats {@link XmlsonMember}s as JSON.
  * 
  * @author coda
  * @see <a href="http://json.org">JSON Documentation</a>
  */
 public class JsonFormatter implements XmlsonFormatter {
 	// TODO coda@wesabe.com -- Mar 14, 2009: reduce the number of branches in JsonFormatter
 	
 	private static final JsonFactory FACTORY = new JsonFactory();
 
 	@Override
 	public void format(XmlsonMember member, OutputStream output) throws IOException {
 		final JsonGenerator generator = FACTORY.createJsonGenerator(output, JsonEncoding.UTF8);
 		serialize(generator, member);
 		generator.close();
 	}
 	
 	@Override
 	public String format(XmlsonMember member) {
 		try {
 			final ByteArrayOutputStream output = new ByteArrayOutputStream();
 			format(member, output);
			return output.toString();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
  	}
 
 	private void serialize(JsonGenerator generator, XmlsonMember member) throws IOException {
 		if (member instanceof XmlsonArray) {
 			serialize(generator, (XmlsonArray) member);
 		} else if (member instanceof XmlsonObject) {
 			serialize(generator, (XmlsonObject) member);
 		}
 	}
 
 	private void serialize(JsonGenerator generator, final XmlsonObject object) throws IOException,
 			JsonGenerationException {
 		generator.writeStartObject();
 		for (Entry<XmlsonString, XmlsonElement> property : object.getProperties().entrySet()) {
 			final String key = property.getKey().getValue();
 			final XmlsonElement value = property.getValue();
 
 			if (value instanceof XmlsonMember) {
 				generator.writeFieldName(key);
 				serialize(generator, (XmlsonMember) value);
 			} else if (value instanceof XmlsonNumber) {
 				final Number number = ((XmlsonNumber) value).getValue();
 				if (number instanceof Integer) {
 					generator.writeNumberField(key, (Integer) number);
 				} else if (number instanceof Long) {
 					generator.writeNumberField(key, (Long) number);
 				} else if (number instanceof Double) {
 					generator.writeNumberField(key, (Double) number);
 				} else if (number instanceof Float) {
 					generator.writeNumberField(key, (Float) number);
 				} else if (number instanceof BigDecimal) {
 					generator.writeNumberField(key, (BigDecimal) number);
 				}
 			} else if (value instanceof XmlsonBoolean) {
 				generator.writeBooleanField(key, ((XmlsonBoolean) value).getValue());
 			} else if (value instanceof XmlsonNull) {
 				generator.writeNullField(key);
 			} else {
 				generator.writeStringField(key, ((XmlsonString) value).getValue());
 			}
 		}
 		generator.writeEndObject();
 	}
 
 	private void serialize(JsonGenerator generator, final XmlsonArray array) throws IOException,
 			JsonGenerationException {
 		generator.writeStartArray();
 		for (XmlsonMember member : array.getMembers()) {
 			serialize(generator, member);
 		}
 		generator.writeEndArray();
 	}
 }
