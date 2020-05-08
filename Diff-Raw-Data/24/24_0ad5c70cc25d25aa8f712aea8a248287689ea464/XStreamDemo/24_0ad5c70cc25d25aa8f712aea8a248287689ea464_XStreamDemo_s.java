 package com.cedarsoft.sample.xstream;
 
 import com.cedarsoft.AssertUtils;
 import com.cedarsoft.sample.Car;
 import com.cedarsoft.sample.Extra;
 import com.cedarsoft.sample.Model;
 import com.cedarsoft.sample.Money;
 import com.thoughtworks.xstream.XStream;
 import org.junit.*;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 
 /**
  *
  */
 public class XStreamDemo {
 
   //START SNIPPET: createSampleCar
   public Car createSampleCar() {
     Model model = new Model( "Ford" );
     Extra extra0 = new Extra( "Whoo effect", new Money( 99.98 ) );
     Extra extra1 = new Extra( "Better Whoo effect", new Money( 199.00 ) );
     List<Extra> extras = Arrays.asList( extra0, extra1 );
     return new Car( model, new Money( 19000.00 ), extras );
   }
   //END SNIPPET: createSampleCar
 
   @Test
   public void testIt() throws IOException, SAXException {
     XStream xStream = new XStream();
     //We define some aliases to get a nicer xml output without fqns
     xStream.alias( "car", Car.class );
     xStream.alias( "extra", Extra.class );
     xStream.alias( "money", Money.class );
 
     String xml = xStream.toXML( createSampleCar() );
     AssertUtils.assertXMLEquals( getClass().getResource( "car.xml" ), xml );
   }
 
   @Test
   public void testDeserialize() {
     XStream xStream = new XStream();
     //We define some aliases to get a nicer xml output without fqns
     xStream.alias( "car", com.cedarsoft.sample.fixed.Car.class );
     xStream.alias( "extra", com.cedarsoft.sample.fixed.Extra.class );
     xStream.alias( "money", com.cedarsoft.sample.fixed.Money.class );
 
    com.cedarsoft.sample.fixed.Car deserialized = ( com.cedarsoft.sample.fixed.Car ) xStream.fromXML( getClass().getResourceAsStream( "car.xml" ) );
    assertEquals( deserialized.getBasePrice(), createSampleCar().getBasePrice() );
   }
 }
