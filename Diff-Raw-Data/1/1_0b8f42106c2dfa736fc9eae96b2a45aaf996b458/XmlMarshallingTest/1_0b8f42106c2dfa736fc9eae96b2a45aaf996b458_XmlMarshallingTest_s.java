 package at.ac.tuwien.infosys.aic.model;
 
 import at.ac.tuwien.infosys.aic.store.DataStore;
 import java.util.List;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.xml.bind.Unmarshaller;
 import org.junit.Before;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.JAXBContext;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 public class XmlMarshallingTest {
 
     private Marshaller m;
     private Unmarshaller u;
     private Address a1 = new Address();
     private Address a2 = new Address();
 
     @Before
     public void setUp() throws Exception {
 
 
 //      Address
         a1.setId("a8888070b-96f3-47ac-9fe9-dfe2dadc00cb");
         a1.setCity("Wien");
         a1.setDoor(1);
         a1.setHouse(23);
         a1.setIsShipping(true);
         a1.setIsBilling(true);
         a1.setIsOther(true);
         a1.setStreet("Mollardgasse");
         a1.setZipCode("1060");
         a1.setId("a9999070b-96f3-47ac-9fe9-dfe2dadc00cb");
         a2.setCity("Wien");
         a2.setHouse(6);
         a2.setIsShipping(false);
         a2.setIsBilling(false);
         a2.setIsOther(false);
         a2.setStreet("Mühlgasse");
         a2.setZipCode("1040");
 
 
 
 
     }
 
     @Test
     public void marshallunmarshalAddress() throws Exception {
         JAXBContext context = JAXBContext.newInstance(Address.class);
         m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         u = context.createUnmarshaller();
 
         StringWriter stringWriter = new StringWriter();
         m.marshal(a1, stringWriter);
         String result = stringWriter.toString();
         System.out.println(result);
         assertThat(result, containsString("<address id="));
         assertThat(result, containsString("a9999070b-96f3-47ac-9fe9-dfe2dadc00cb"));
         assertThat(result, containsString("<street>Mollardgasse</street>"));
         assertThat(result, containsString("<city>Wien</city>"));
         assertThat(result, containsString("<house>23</house>"));
         assertThat(result, containsString("<door>1</door>"));
         assertThat(result, containsString("<zipCode>1060</zipCode>"));
         assertThat(result, containsString("<isBilling>true</isBilling>"));
         assertThat(result, containsString("<isOther>true</isOther>"));
 
         Address uAddress = (Address) u.unmarshal(new StringReader(result));
         assertThat(uAddress, equalTo(a1));
         assertThat(uAddress.getId(), is("a9999070b-96f3-47ac-9fe9-dfe2dadc00cb"));
         assertThat(uAddress.getCity(), is("Wien"));
         assertThat(uAddress.getStreet(), is("Mollardgasse"));
         assertThat(uAddress.getZipCode(), is("1060"));
         assertThat(uAddress.getDoor(), is(1));
         assertThat(uAddress.getHouse(), is(23));
         assertTrue(uAddress.isIsBilling());
         assertTrue(uAddress.isIsOther());
     }
 
     @Test
     public void marshallunmarshalOrder() throws Exception {
         JAXBContext context = JAXBContext.newInstance(Order.class);
         m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         u = context.createUnmarshaller();
 
         Order o1 = DataStore.getInstance().getOrder("o7777070b-96f3-47ac-9fe9-dfe2dadc00cb");
 
 
         StringWriter stringWriter = new StringWriter();
         m.marshal(o1, stringWriter);
         String result = stringWriter.toString();
         System.out.println(result);
 
         String s = String.valueOf(o1.getOrderDate().getTime());
 
         assertThat(result, containsString("o7777070b-96f3-47ac-9fe9-dfe2dadc00cb"));
         assertThat(result, containsString(s));
         assertThat(result, containsString("item"));
         assertThat(result, containsString("quantity"));
         assertThat(result, containsString("21"));
         assertThat(result, containsString("product"));
         assertThat(result, containsString("a777070b-96f3-47ac-9fe9-dfe2dadc00cb"));
         Order uOrder = (Order) u.unmarshal(new StringReader(result));
         assertThat(uOrder, equalTo(o1));
         assertThat(uOrder.getId(), is("o7777070b-96f3-47ac-9fe9-dfe2dadc00cb"));
         assertThat(uOrder.getOrderDate().getTime(), is(new Long(s)));
     }
 
     @Test
     public void testUnknownProductFault() throws Exception {
         JAXBContext context = JAXBContext.newInstance(Item.class);
         m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         u = context.createUnmarshaller();
 
         Item i = new Item();
         i.setQuantity(4);
         //Product p2 = DataStore.getInstance().getProduct("a777070b-96f3-47ac-9fe9-dfe2dadc00cb");
         Product p2 = new Product();
         p2.setId("wrongID");
         i.setProduct(p2);
 
         StringWriter stringWriter = new StringWriter();
         m.marshal(i, stringWriter);
         String result = stringWriter.toString();
         System.out.println(result);
 
         Item ui = (Item) u.unmarshal(new StringReader(result));
         System.out.println(ui.getProduct());
         assertThat(ui.getProduct().getId(), equalTo(i.getProduct().getId()));
 
     }
 
     @Test
     public void marshallunmarshalProduct() throws Exception {
         JAXBContext context = JAXBContext.newInstance(Product.class);
         m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         u = context.createUnmarshaller();
 
         Product p = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
 
         StringWriter stringWriter = new StringWriter();
         m.marshal(p, stringWriter);
         String result = stringWriter.toString();
         System.out.println(result);
         assertThat(result, containsString("product id="));
         assertThat(result, containsString("aec0737d-e783-4c16-9b26-66040caf4aff"));
         assertThat(result, containsString("name>War and Peace"));
         assertThat(result, containsString("singleUnitPrice>0<"));
         Product up = (Product) u.unmarshal(new StringReader(result));
         assertThat(up, equalTo(p));
         assertThat(up.getId(), equalTo(p.getId()));
         assertThat(up.getName(), equalTo(p.getName()));
         assertThat(up.getSingleUnitPrice(), equalTo(p.getSingleUnitPrice()));
 
 
     }
 
     @Test
     public void marshallunmarshalItem() throws Exception {
         JAXBContext context = JAXBContext.newInstance(Item.class);
         m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         u = context.createUnmarshaller();
 
         Item i = new Item();
         i.setQuantity(4);
         Product p2 = DataStore.getInstance().getProduct("aec0737d-e783-4c16-9b26-66040caf4aff");
         i.setProduct(p2);
 
         StringWriter stringWriter = new StringWriter();
         m.marshal(i, stringWriter);
         String result = stringWriter.toString();
         System.out.println(result);
         assertThat(result, containsString("item"));
         assertThat(result, containsString("quantity>4"));
 //      assertThat(result, containsString("product>aec0737d-e783-4c16-9b26-66040caf4aff"));
         Item ui = (Item) u.unmarshal(new StringReader(result));
         assertThat(ui, equalTo(i));
         assertThat(ui.getQuantity(), equalTo(4));
         assertThat(ui.getProduct(), equalTo(i.getProduct()));
 
 
 
     }
 }
