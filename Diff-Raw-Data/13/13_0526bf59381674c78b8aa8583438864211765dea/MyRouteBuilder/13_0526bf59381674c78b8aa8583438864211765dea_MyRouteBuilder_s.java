 package de.gzockoll.prototype.camel.encashment;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 
 import de.gzockoll.prototype.camel.encashment.entity.EncashmentType;
 import de.gzockoll.prototype.camel.encashment.service.EncashmentService;
 
 @SuppressWarnings("javadoc")
 public final class MyRouteBuilder extends RouteBuilder {
 	@Override
 	public void configure() {
 		errorHandler(deadLetterChannel("seda:error").maximumRedeliveries(5)
 				.retryAttemptedLogLevel(LoggingLevel.WARN).redeliveryDelay(250)
 				.backOffMultiplier(2));
 
		from("quartz://myGroup/myTimerName/*/*/*/*/*/$").bean(
 				EncashmentService.class, "startProcessing()");
 
 		from("direct:input").to("seda:inkasso1");
 
 		from("seda:inkasso1")
 				.multicast()
 				.to("seda:filemanager")
 				.choice()
 				.when(property("TYPE").isEqualTo(EncashmentType.ORDER.name()))
 				.to("seda:inkasso1_order")
 				.when(property("TYPE").isEqualTo(EncashmentType.CREDIT.name()))
 				.to("seda:inkasso1_credit")
 				.when(property("TYPE").isEqualTo(EncashmentType.PAYMENT.name()))
 				.to("seda:inkasso1_payment").otherwise().to("seda:error").end();
 
 		from("seda:inkasso1_order").marshal().json()
 				.to("ftp://ftpuser@zockoll.dyndns.org/out?password=tux88.")
 				.bean(EncashmentService.class, "onSuccess()");
 
 		from("seda:inkasso1_credit").marshal().json()
 				.setHeader("subject", constant("Credit received"))
 				.setHeader("to", constant("gzockoll@gmail.com"))
 				.to("seda:gmail");
 
 		from("seda:inkasso1_payment").marshal().json()
 				.setHeader("subject", constant("PAYMENT made"))
 				.setHeader("to", constant("gzockoll@gmail.com"))
 				.to("seda:gmail");
 
 		from("seda:gmail").to(
 				"smtps://gztest999@smtp.gmail.com?password=fifi9999");
 
 		from("seda:filemanager")
 				.marshal()
 				.xstream("UTF-8")
 				.to("log:de.gzockoll.prototype.camel?showAll=true&multiline=true")
 				.to("file:data/outbox");
 
 		from("seda:error")
 				.marshal()
 				.xstream("UTF-8")
 				.setHeader(
 						Exchange.FILE_NAME,
 						simple("${file:name.noext}-${header:breadcrumbId}-${date:now:yyyyMMddHHmmssSSS}.xml"))
 				.to("log:de.gzockoll.prototype.camel?showAll=true&multiline=true")
				.to("file:data/error");
 	}
 }
