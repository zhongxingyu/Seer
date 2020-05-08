 package br.com.six2six.xstreamdsl.unmarshal;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 
 import org.junit.Test;
 
 import br.com.six2six.xstreamdsl.converter.OrderConverter;
 import br.com.six2six.xstreamdsl.model.Order;
 import br.com.six2six.xstreamdsl.model.Product;
 
 import com.thoughtworks.xstream.XStream;
 
 public class OrderUnmarshalTest {
 
 	@Test
 	public void toObject() {
 		final String content = "<br.com.six2six.xstreamdsl.model.Order>\n"
 							 + "  <id>1</id>\n"
 							 + "  <products>\n"
							 + "    <id>1</id>\n"
							 + "    <name>nicething</name>\n"
							 + "    <id>2</id>\n"
							 + "    <name>nicething</name>\n"
							 + "    <id>3</id>\n"
							 + "    <name>nicething</name>\n"
 							 + "  </products>\n"
 							 + "</br.com.six2six.xstreamdsl.model.Order>";
 		
 		Order order = (Order) xstream().fromXML(content);
 		
 		assertNotNull(order);
 		assertEquals(Long.valueOf(1), order.getId());
 		assertFalse(order.getProducts().isEmpty());
 		
 		Integer i = 1;
 		for (Product product : order.getProducts()) {
			assertEquals(i, product.getId());
 			assertEquals("nicething", product.getName());
 		}
 	}
 	
 	private XStream xstream() {
 		XStream xstream = new XStream() {
 			{setMode(NO_REFERENCES);}
 		};
 		xstream.registerConverter(new OrderConverter());
 
 		return xstream;
 	}
 }
