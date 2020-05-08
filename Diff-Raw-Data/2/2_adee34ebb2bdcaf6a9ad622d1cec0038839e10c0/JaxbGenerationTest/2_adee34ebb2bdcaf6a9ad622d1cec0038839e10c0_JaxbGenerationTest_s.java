 package org.codehaus.xfire.generator;
 
 import org.codehaus.xfire.gen.Wsdl11Generator;
 
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JDefinedClass;
 
 public class JaxbGenerationTest
     extends GenerationTestSupport
 {    
     public void testEchoWithFaults() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/echoFault.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("fault.echo.xfire.OtherEchoFault_Exception");
         assertNotNull(echo);
         echo = model._getClass("fault.echo.xfire.EchoFault_Exception");
         assertNotNull(echo);
     }
     
     public void testWWCars() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/wwcarsXMLInterface.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.wwcars");
         generator.setBinding("jaxb");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.wwcars.wwcarsXMLInterfaceSoap");
         assertNotNull(echo);
         
        assertEquals(getTestFilePath("src/wsdl/"), generator.getBaseURI());
     }
     
     public void testTwoPortsDifferentBindings() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/globalweather-twoporttypes.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.globalweather.twopts");
         generator.setBinding("jaxb");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.globalweather.twopts.GlobalWeatherSoap");
         assertNotNull(echo);
     }
     
     public void testFault() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/auth.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.auth");
         generator.setBinding("jaxb");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.auth.AuthServicePortType");
         assertNotNull(echo);
     }
     
     public void testOneWay() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/oneway.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.oneway");
         generator.setBinding("jaxb");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.oneway.SendMessagePortType");
         assertNotNull(echo);
     }
     
     public void testGlobalWeather() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/globalweather.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.globalweather");
         generator.setBinding("jaxb");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.globalweather.GlobalWeatherSoap");
         assertNotNull(echo);
     }
 
     public void testEchoWrappedServiceIntf() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/echoWrapped.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setDestinationPackage("jsr181.jaxb.echo.wrapped");
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("jsr181.jaxb.echo.wrapped.EchoPortType");
         assertNotNull(echo);
         
         /*JMethod method = echo.getMethod("echo", new JType[] { model._ref(String.class) });
         assertNotNull(method);
         assertEquals( model.ref(String.class), method.type() );
         
         assertNotNull(model._getClass("jsr181.jaxb.echo.wrapped.EchoClient"));
         assertNotNull(model._getClass("jsr181.jaxb.echo.wrapped.EchoImpl"));*/
     }
     
 
     public void testEchoNoDestPkg() throws Exception
     {
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/echoWrapped.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("wrapped.echo.EchoPortType");
         assertNotNull(echo);
     }
 }
