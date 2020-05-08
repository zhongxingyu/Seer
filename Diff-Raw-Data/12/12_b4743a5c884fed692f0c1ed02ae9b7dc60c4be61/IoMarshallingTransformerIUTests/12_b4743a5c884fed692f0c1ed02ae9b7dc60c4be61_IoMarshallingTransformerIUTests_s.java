 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *			http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.c24.io.spring.integration.transformer;
 
 import biz.c24.io.examples.models.basic.InputDocumentRootElement;
 import biz.c24.io.spring.core.C24Model;
import biz.c24.io.spring.sink.JsonSinkFactory;
 import biz.c24.io.spring.sink.OutputType;
 import biz.c24.io.spring.sink.TextualSinkFactory;
 import biz.c24.io.spring.sink.XmlSinkFactory;
 import org.junit.Test;
 import org.springframework.integration.Message;
 import org.springframework.integration.support.MessageBuilder;
 
 import static biz.c24.io.spring.integration.test.TestUtils.*;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 public class IoMarshallingTransformerIUTests {
 
 	C24Model model = new C24Model(InputDocumentRootElement.getInstance());
 
 	@Test
 	public void canMarshalXmlToBytearray() throws Exception {
 
 		C24MarshallingTransformer ioMarshallingTransformer = new C24MarshallingTransformer();
 		ioMarshallingTransformer.setOutputType(OutputType.BYTE_ARRAY);
 		XmlSinkFactory xmlSinkFactory = new XmlSinkFactory();
 		ioMarshallingTransformer.setSinkFactory(xmlSinkFactory);
 
 		Message<?> message = MessageBuilder.withPayload(loadObject()).build();
 
 		Message<?> outputMessage = ioMarshallingTransformer.transform(message);
 
 		assertThat(outputMessage.getPayload(), notNullValue());
 		assertThat(outputMessage.getPayload(), is(byte[].class));
 
 		String xml = new String((byte[]) outputMessage.getPayload(), "UTF-8");
 
 		// TODO: XML equivalence match
 
 	}
 
 	@Test
 	public void canMarshalXmlToString() throws Exception {
 
 		C24MarshallingTransformer ioMarshallingTransformer = new C24MarshallingTransformer();
 		ioMarshallingTransformer.setOutputType(OutputType.STRING);
 		XmlSinkFactory xmlSinkFactory = new XmlSinkFactory();
 		ioMarshallingTransformer.setSinkFactory(xmlSinkFactory);
 
 		Message message = MessageBuilder.withPayload(loadObject()).build();
 
 		Message<?> outputMessage = ioMarshallingTransformer.transform(message);
 
 		assertThat(outputMessage.getPayload(), notNullValue());
 		assertThat(outputMessage.getPayload(), is(String.class));
 
 		// TODO: XML equivalence match
 
 	}
 
 	@Test
 	public void canMarshalTextToString() throws Exception {
 
 		C24MarshallingTransformer ioMarshallingTransformer = new C24MarshallingTransformer();
 		ioMarshallingTransformer.setOutputType(OutputType.STRING);
 		ioMarshallingTransformer.setSinkFactory(new TextualSinkFactory());
 
 		Message message = MessageBuilder.withPayload(loadObject()).build();
 
 		Message<?> outputMessage = ioMarshallingTransformer.transform(message);
 
 		assertThat((String) outputMessage.getPayload(), is(loadCsvString()));
 
 	}
 
 	@Test
 	public void canMarshalTextToByteArray() throws Exception {
 
 		C24MarshallingTransformer ioMarshallingTransformer = new C24MarshallingTransformer();
 		ioMarshallingTransformer.setOutputType(OutputType.BYTE_ARRAY);
 		ioMarshallingTransformer.setSinkFactory(new TextualSinkFactory());
 
 		Message message = MessageBuilder.withPayload(loadObject()).build();
 
 		Message<?> outputMessage = ioMarshallingTransformer.transform(message);
 
 		assertThat((byte[]) outputMessage.getPayload(), is(loadCsvBytes()));
 
 	}
 
     @Test
 	public void canMarshalTextToJsonString() throws Exception {
 
 		C24MarshallingTransformer ioMarshallingTransformer = new C24MarshallingTransformer();
 		ioMarshallingTransformer.setOutputType(OutputType.STRING);
		ioMarshallingTransformer.setSinkFactory(new JsonSinkFactory());
 
 		Message message = MessageBuilder.withPayload(loadObject()).build();
 		Message<?> outputMessage = ioMarshallingTransformer.transform(message);
 
 		assertThat((String) outputMessage.getPayload(), is(loadJsonString()));
 	}
 
 }
