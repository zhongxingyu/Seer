 /**
  * 
  */
 package yucatan.communication.presentation;
 
 import java.util.HashMap;
 
 import org.junit.Test;
 
 /**
  * @author Samuel
  * 
  */
 public class TestTemplateProcessor {
 
 	/**
 	 * Test method for {@link yucatan.communication.presentation.TemplateProcessor#doRender(java.lang.Object, java.lang.String)}.
 	 */
 	@Test
 	public void testDoRender() {
		String template = "<html><body><h1>${@odd}</h1><ul>${@count}</ul></body>";
 		TemplateProcessor.doRender(new Object(), template);
 
 		HashMap<String, String> hs = new HashMap<String, String>();
 		hs.put("myKey", "myValue");
 		
		template = "<html><body><h1>${@data(myKey)}</h1><ul>${@list(myKey)}</ul>${@count}</body>";
 		TemplateProcessor.doRender(hs, template);
 
 		// template = "${:data:}</h1><ul>${:list:}</ul></body>";
 		// TemplateProcessor.doRender(new Object(), template);
 
 	}
 }

