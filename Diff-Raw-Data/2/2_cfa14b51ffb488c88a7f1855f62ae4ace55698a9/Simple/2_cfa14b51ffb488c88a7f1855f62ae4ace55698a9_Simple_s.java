 package service;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.xml.bind.JAXBException;
 
 import models.A;
 import models.B;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
 import com.sun.jersey.api.json.JSONMarshaller;
 
 @Path("/simple")
 public class Simple {
 	
 	A a;
 	public Simple(){
 		a = new A();
 		List<B> elBs = new ArrayList<B>();
 		B b = new B();
 		b.setDoubl(0.3);
 		elBs.add(b);
 		a.setElements(elBs);
 		
 		a.setText("Message to the people");
 	}
 
 	@GET @Produces("application/json")
 	public A emptyGet(){
 		return a;
 	}
 	
 	@GET @Path("/all") @Produces("application/json")
 	public Collection<B> getBs(){
 		return a.getElements();
 	}
 	
 	@GET @Path("/string")
 	public String getJsoned() throws Exception{
 		// NOTE: this generates slighty different JSON ..
         A myInstance = a;
         StringWriter writer = new StringWriter();
         JSONConfiguration config = JSONConfiguration.natural().build();
        Class[] types = {A.class, B.class};
         JSONJAXBContext context = new JSONJAXBContext(config, types);
         JSONMarshaller marshaller = context.createJSONMarshaller();
         marshaller.marshallToJSON(myInstance, writer);
         String json = writer.toString();
         System.out.println(json);
         return json;
 	}
 }
