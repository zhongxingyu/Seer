 package uk.ac.cam.cl.dtg.teaching.controllers;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.jboss.resteasy.client.ClientRequestFactory;
 
 import uk.ac.cam.cl.dtg.teaching.api.ExampleApi;
 import uk.ac.cam.cl.dtg.teaching.models.Fruit;
 
 public class ExampleController implements ExampleApi {
 
 	@Override
 	public Fruit load(String type) throws Exception {
 		if (type.equals("chocolate")) {
 			throw new Exception("Not a fruit");
 		}
 		return new Fruit(type);
 	}
 
 	@Override
 	public List<Fruit> loadList() {
 		return Arrays.asList(new Fruit[] { new Fruit("apple"), new Fruit("orange"), new Fruit("pear") } );
 	}
 
 	@Override
 	public Map<String, ?> exception() throws Exception {
 		throw new Exception("Test");
 	}
 
 	@Override
 	public Fruit proxy(String type) throws Exception {
 		ClientRequestFactory c = new ClientRequestFactory(UriBuilder.fromUri(
				"http://localhost:8080/api-template/api")
 				.build());
 		Fruit apple = c.createProxy(ExampleApi.class).load(type);
 		return apple;
 	}
 
 }
