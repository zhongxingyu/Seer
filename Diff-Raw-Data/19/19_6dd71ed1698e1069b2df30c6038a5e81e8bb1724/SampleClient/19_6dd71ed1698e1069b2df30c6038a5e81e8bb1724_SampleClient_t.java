 package ru.forketyfork.mtomsoap.client;
 
 import org.apache.cxf.frontend.ClientProxy;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 import ru.forketyfork.mtomsoap.schema.Sample;
 import ru.forketyfork.mtomsoap.schema.SampleRequest;
 import ru.forketyfork.mtomsoap.schema.SampleResponse;
 import ru.forketyfork.mtomsoap.schema.SampleService;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.soap.SOAPBinding;
 
 /**
  * A sample service client that uses Apache CXF.
  *
  *
  * @author Serge Petunin
  * @created 22.06.13 2:46
  */
 public class SampleClient {
 
     public static void main(String... args) {
 
         if (args.length != 1) {
             System.out.println("Filename argument expected");
             return;
         }
 
         // Creating a CXF-generated service
         Sample sampleClient = new SampleService().getSampleSoap12();
 
         // Setting infinite HTTP timeouts
         HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
         httpClientPolicy.setConnectionTimeout(0);
         httpClientPolicy.setReceiveTimeout(0);
         HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(sampleClient).getConduit();
         httpConduit.setClient(httpClientPolicy);
 
         // Enabling MTOM for the SOAP binding provider
         BindingProvider bindingProvider = (BindingProvider) sampleClient;
         SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
         binding.setMTOMEnabled(true);
 
         // Creating request object
         SampleRequest request = new SampleRequest();
         request.setText("Hello");
         request.setFile(new DataHandler(new FileDataSource(args[0])));
 
         // Sending request
         SampleResponse response = sampleClient.sample(request);
 
         System.out.println(String.format("Server responded: \"%s\"", response.getText()));
 
     }
 }
