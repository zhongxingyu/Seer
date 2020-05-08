 package org.uncertweb.ps.handler.soap;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.notNullValue;
 
 import java.io.IOException;
 
 import org.jdom.Document;
import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.uncertweb.ps.test.RunningService;
import org.uncertweb.xml.Namespaces;
 public class DocumentBuilderTest {
 	
 	@Rule
 	public RunningService service = new RunningService();
 
 	private DocumentBuilder builder;
 	
 	@Before
 	public void before() {
 		builder = new DocumentBuilder();
 	}
 	
 	@Test
 	public void buildValidReturnsDocument() throws JDOMException, IOException {
 		Document document = builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/sum-soaprequest.xml"));
 		assertThat(document, notNullValue());
 	}
 	
 	@Test
 	public void buildInvalidReturnsDocument() throws JDOMException, IOException {
 		Document document = builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/sum-invalid-soaprequest.xml"));
 		assertThat(document, notNullValue());
 	}
 	
 	@Test
 	public void buildValidIsValid() throws JDOMException, IOException {
 		builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/sum-soaprequest.xml"));
 		ValidationResult result = builder.getValidationResult();
 		assertThat(result.isValid(), equalTo(true));
 	}
 	
 	@Test
 	public void buildInvalidIsInvalid() throws JDOMException, IOException {
 		builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/sum-invalid-soaprequest.xml"));
 		ValidationResult result = builder.getValidationResult();
 		assertThat(result.isValid(), equalTo(false));
 	}
 	
	@Test
	public void buildStripsWhitespace() throws JDOMException, IOException {
		Document document = builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/bufferpolygon-request.xml"));
		Element polygon = document.getRootElement().getChild("Polygon", Namespaces.PS);
		assertThat(polygon, notNullValue());
		assertThat(polygon.getContentSize(), equalTo(1));
	}
	
 }
