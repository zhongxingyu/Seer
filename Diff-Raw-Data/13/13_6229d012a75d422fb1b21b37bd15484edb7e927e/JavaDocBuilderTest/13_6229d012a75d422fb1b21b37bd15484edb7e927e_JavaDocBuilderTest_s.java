 /**
  *
  */
 package org.arachna.netweaver.javadoc;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.dom.DOMSource;
 
 import org.arachna.netweaver.dc.types.Compartment;
 import org.arachna.netweaver.dc.types.CompartmentState;
 import org.arachna.netweaver.dc.types.DevelopmentComponent;
 import org.arachna.netweaver.dc.types.DevelopmentComponentType;
 import org.arachna.netweaver.dc.types.DevelopmentConfiguration;
import org.arachna.netweaver.javadoc.JavaDocBuilder;
 import org.junit.Before;
 import org.junit.Test;
import org.w3c.dom.Node;
 
 /**
  * @author g526521
  *
  */
 public final class JavaDocBuilderTest {
     /**
      * instance under test.
      */
     private JavaDocBuilder builder;
 
     /**
      * test data container.
      */
     private DevelopmentConfiguration config;
 
     @Before
     public void setUp() {
         this.builder = new JavaDocBuilder("");
         this.config = new DevelopmentConfiguration("DI1_Example_D");
         Compartment compartment =
            new Compartment("example.com_EXAMPLE_SC1_1", CompartmentState.Source, "example.com", "", "EXAMPLE_SC1");
         compartment.add(new DevelopmentComponent("example.com", "DC1", DevelopmentComponentType.Java));
         compartment.add(new DevelopmentComponent("example.com", "DC2", DevelopmentComponentType.Java));
 
         this.config.add(compartment);
 
         compartment =
            new Compartment("example.com_EXAMPLE_SC2_1", CompartmentState.Source, "example.com", "", "EXAMPLE_SC2");
         compartment.add(new DevelopmentComponent("example.com", "DC3", DevelopmentComponentType.Java));
         compartment.add(new DevelopmentComponent("example.com", "DC4", DevelopmentComponentType.Java));
 
         this.config.add(compartment);
     }
 
     /**
      * Test method for
      * {@link org.arachna.netweaver.javadoc.JavaDocBuilder#createIndexDom(org.arachna.netweaver.dc.types.DevelopmentConfiguration)}
      * .
      *
      * @throws InterruptedException
      * @throws ParserConfigurationException
      */
     @Test
     public void testCreateIndexDom() throws IOException, ParserConfigurationException, InterruptedException {
         DOMSource source = this.builder.createIndexDom(this.config);
 
         // FIXME: test generated DOM
     }
 
     /**
      * Test method for
      * {@link org.arachna.netweaver.javadoc.JavaDocBuilder#createIndexDom(org.arachna.netweaver.dc.types.DevelopmentConfiguration)}
      * .
      *
      * @throws InterruptedException
      * @throws ParserConfigurationException
      */
     @Test
     public void testCreateIndexHtml() throws IOException, ParserConfigurationException, InterruptedException {
         String result = this.builder.createIndexHtml(this.config);
 
         // FIXME: test generated HTML
     }
 }
