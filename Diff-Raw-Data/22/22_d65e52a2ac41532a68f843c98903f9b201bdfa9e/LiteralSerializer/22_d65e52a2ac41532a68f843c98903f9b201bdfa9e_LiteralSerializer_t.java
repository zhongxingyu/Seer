 package it.polimi.deib.streams.oracle.io.serializer;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.SerializerProvider;
 import org.codehaus.jackson.map.ser.std.SerializerBase;
 import org.openrdf.model.Literal;
 
 public class LiteralSerializer extends SerializerBase<Literal> {
 	
 	public LiteralSerializer() {
 		super(Literal.class, true);
 	}
 	
 	@Override
 	public void serialize(Literal literal, JsonGenerator jGen, SerializerProvider sPro)
 			throws IOException, JsonProcessingException {
 		jGen.writeStartObject();
    	jGen.writeStringField("type", "literal");
	    if(literal.getDatatype()!=null)
 		    jGen.writeStringField("datatype", literal.getDatatype().toString());
 	    if(literal.getLanguage()!=null){
 	    	jGen.writeStringField("xml:lang", literal.getLanguage());
 	    }
 	    jGen.writeStringField("value", literal.toString());
 	    jGen.writeEndObject();	}
 }
