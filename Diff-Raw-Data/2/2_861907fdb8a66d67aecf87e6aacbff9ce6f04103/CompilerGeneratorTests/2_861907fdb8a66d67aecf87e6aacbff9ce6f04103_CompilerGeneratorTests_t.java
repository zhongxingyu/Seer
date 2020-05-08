 package de.hszg.atocc.vcc.compilerGenerator.internal;
 
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import java.io.IOException;
 //import java.nio.charset.Charset;
 //import java.nio.file.Files;
 //import java.nio.file.Paths;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public class CompilerGeneratorTests extends AbstractTestHelper {
 
 //	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
 
 	@Test
 	public void testJFlexInputGeneration() throws IOException,
 			XmlUtilsException {
 //		final byte[] bytes = Files.readAllBytes(Paths.get(""));
 //		final String expected = new String(bytes, UTF8_CHARSET);
 
 		final Document mlCompilerDocument = getXmlService().documentFromFile(
 				"ML_vcc.xml");
 
 		final Document resultDocument = getWebUtils().post(
				"http://localhost:8081/vcc/jflex/generateInput", mlCompilerDocument);
 
 		Assert.assertEquals(WebServiceResultStatus.SUCCESS, getXmlService()
 				.getResultStatus(resultDocument));
 
 		//getXmlService().getContent(resultDocument);
 	}
 
 }
