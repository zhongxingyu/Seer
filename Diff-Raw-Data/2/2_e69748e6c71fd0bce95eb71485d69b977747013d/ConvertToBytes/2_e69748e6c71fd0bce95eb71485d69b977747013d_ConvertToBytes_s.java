 package org.vpac.grisu.client.model.template.postprocessor;
 
 import org.vpac.grisu.client.model.template.JsdlTemplate;
 import org.w3c.dom.Element;
 
 public class ConvertToBytes extends ElementPostprocessor {
 
 	public ConvertToBytes(JsdlTemplate template, Element element) {
 		super(template, element);
 	}
 
 	@Override
 	public void process(String fqan) throws PostProcessException {
 
 		Integer mb;
 		try {
 			mb = Integer.parseInt(element.getTextContent());
 		} catch (Exception e) {
 			throw new PostProcessException(
 					"Could not process specified memory.", e);
 		}
 
		Long bytes = new Long(mb * 1024);
 
 		element.setTextContent(bytes.toString());
 
 	}
 
 	@Override
 	public boolean processBeforeJobCreation() {
 		return true;
 	}
 
 }
