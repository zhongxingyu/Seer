 package com.bluesky.visualprogramming.core.serialization;
 
 import java.io.Reader;
 import java.io.Writer;
 
 import com.bluesky.visualprogramming.core.Field;
 import com.bluesky.visualprogramming.core.Link;
 import com.bluesky.visualprogramming.core.Procedure;
 import com.bluesky.visualprogramming.core._Object;
 import com.bluesky.visualprogramming.core.value.BooleanValue;
 import com.bluesky.visualprogramming.core.value.FloatValue;
 import com.bluesky.visualprogramming.core.value.IntegerValue;
 import com.bluesky.visualprogramming.core.value.StringValue;
 import com.bluesky.visualprogramming.core.value.TimeValue;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 
 public class XmlSerializer implements ObjectSerializer {
 	XStream xstream;
 	boolean gui;
 
 	public XmlSerializer(boolean gui) {
 		createXstream();
 		initXstream(gui);
 
 	}
 
 	protected void createXstream() {
 		xstream = new XStream();
 	}
 
 	protected void initXstream(boolean gui) {
 
 		xstream.setMode(XStream.ID_REFERENCES);
 
 		xstream.alias("object", _Object.class);
 		xstream.alias("field", Field.class);
 		xstream.omitField(_Object.class, "fieldNameMap");
 		xstream.omitField(_Object.class, "childrenObjectMap");
 		// owner can be restored after deserialze.
 		//xstream.omitField(_Object.class, "owner");
 
 		xstream.alias("boolean", BooleanValue.class);
 		xstream.alias("integer", IntegerValue.class);
 		xstream.alias("float", FloatValue.class);
 		xstream.alias("string", StringValue.class);
 		xstream.alias("link", Link.class);
 		xstream.alias("procedure", Procedure.class);
 
 		if (!gui)
 			omitGuiInfo();
 	}
 
 	protected void omitGuiInfo() {
 
 		Class cls = _Object.class;
 
 		xstream.omitField(cls, "area");
 		xstream.omitField(cls, "scaleRate");
 		xstream.omitField(cls, "border");
 		xstream.omitField(cls, "borderColor");
 
 		xstream.omitField(Field.class, "selectedStatus");
 	}
 
 	@Override
 	public void serialize(_Object obj, Writer writer) {
 		xstream.toXML(obj, writer);
 
 	}
 
 	@Override
 	public _Object deserialize(Reader reader) {
 
 		return (_Object) xstream.fromXML(reader);
 	}
 
 }
