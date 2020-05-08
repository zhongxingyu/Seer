 package org.fedorahosted.beaker.model;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.fedorahosted.beaker.model.Result;
 import org.fedorahosted.beaker.remote_model.TaskResult;
 import org.junit.Test;
 
 public class ResultTest {
     
     
     @Test 
     public void parseResultXml() throws JAXBException, URISyntaxException {
         File file  = new File(getClass().getResource("result.xml").toURI());
         JAXBContext jaxbContext = JAXBContext.newInstance(Result.class);
         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         Result result = (Result) jaxbUnmarshaller.unmarshal(file);
         
         assertEquals("2138849", result.getId());
         assertEquals("/distribution/reservesys", result.getPath());
         assertEquals("0.00", result.getScore());
        assertEquals(TaskResult.PASS.toString(), result.getResult().toUpperCase());
     }
 
 }
