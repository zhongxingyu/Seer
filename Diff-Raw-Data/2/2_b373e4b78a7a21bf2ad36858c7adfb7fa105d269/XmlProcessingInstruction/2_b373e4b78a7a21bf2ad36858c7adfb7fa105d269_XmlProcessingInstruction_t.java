 package bingo.lang.xml;
 
 public class XmlProcessingInstruction extends XmlNode {
 
 	private final String target;
 	private final String data;
 
 	public XmlProcessingInstruction(String target, String data) {
 		this.target = target;
 		this.data = data;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	@Override
 	public XmlNodeType nodeType() {
 		return XmlNodeType.PROCESSING_INSTRUCTION;
 	}
 
 	@Override
 	public String toXml(XmlFormat format) {
 		String indent = getIndent(format);
		return indent + "<?" + target + " " + data + "?>";
 	}
 }
