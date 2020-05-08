 
 package org.ebayopensource.turmeric.example.v1.services.echoservice.impl;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.example.v1.services.EchoRequest;
 import org.ebayopensource.turmeric.example.v1.services.EchoResponse;
 import org.ebayopensource.turmeric.example.v1.services.GetVersionRequest;
 import org.ebayopensource.turmeric.example.v1.services.GetVersionResponse;
import org.ebayopensource.turmeric.example.v1.services.echoservice.impl.ExampleEchoServiceV1;
 
 public class ExampleEchoServiceV1Impl
     implements ExampleEchoServiceV1
 {
 
 
     public GetVersionResponse getVersion(GetVersionRequest param0) {
         return null;
     }
 
     public EchoResponse echo(EchoRequest param0) {
     	EchoResponse resp = new EchoResponse();
     	resp.setAck(AckValue.SUCCESS);
     	resp.setOutput(param0.getEchoText());
     	return resp;
     }
 
 }
