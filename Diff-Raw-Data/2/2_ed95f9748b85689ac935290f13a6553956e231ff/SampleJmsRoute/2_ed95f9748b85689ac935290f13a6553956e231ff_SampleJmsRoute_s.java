 package de.ctrlaltdel.cci.sample;
 
 import org.apache.camel.builder.RouteBuilder;
 
 /**
  * SampleJmsRoute
  * @author ds
  */
 public class SampleJmsRoute extends RouteBuilder {
 
 	@Override
 	public void configure() throws Exception {
 		from("jms:data")
        .log("received: ${in.body}")
 		.bean(SampleJmsProducer.class, "echo")
 		;
 	}
 }
