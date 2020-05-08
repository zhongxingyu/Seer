 package org.vnguyen.joreman;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.ContextResolver;
 
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.testng.Assert;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class Deserialization {
 
 	@BeforeTest
 	public void setup () throws ClassNotFoundException {
 		Class.forName("org.vnguyen.joreman.ForemanClientFactory");
 	}
 	@Test
 	public void loadTemplate() throws Exception {
 		Host host = HostFormBuilder.newTemplate("/templates/simple.host.json");
 		Assert.assertNotNull(host.computeAttrs.interfaces_attributes);
 	}
 	
 	@Test
 	public void toJson() throws Exception {
		Host host = HostFormBuilder.newTemplate("foos").withHostGroup(new SomeHostGroup("4"));
 		
 		ContextResolver<ObjectMapper> ctx = ResteasyProviderFactory.getInstance().getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
 		String json = ctx.getContext(null).writerWithDefaultPrettyPrinter().writeValueAsString(host);
 		System.out.println((json));
 	}
 	
 	
	private static class SomeHostGroup extends AbstractGroup {

		public SomeHostGroup(String groupId) {
			super(groupId);
			addParam("pi", "3.14");
		}
		
	}
 
 }
