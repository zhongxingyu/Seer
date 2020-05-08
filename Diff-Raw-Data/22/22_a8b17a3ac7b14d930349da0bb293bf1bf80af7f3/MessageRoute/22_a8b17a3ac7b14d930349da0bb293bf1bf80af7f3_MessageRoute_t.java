 /**
  * 
  */
 package de.inovex.jax2013.showcase.cxf.producer;
 
import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 
 import de.inovex.jax2013.showcase.defaults.ShowcaseDefaults;
import de.inovex.jax2013.showcase.wsendpoint.Message;
 
 /**
  * @author anierbeck
  *
  */
 public class MessageRoute extends RouteBuilder {
 
 	private String inputPath;
 
 	@Override
 	public void configure() throws Exception {
 		from("file://"+inputPath+"?moveFailed=.failed")
 			.convertBodyTo(String.class)
 			.log(LoggingLevel.WARN, ShowcaseDefaults.MESSAGE_LOGGER, "Retrieved incoming text ${body}")
			.process(new Processor() {
				
				@Override
				public void process(Exchange exchange) throws Exception {
					Message message = new Message();
					String body = exchange.getIn().getBody(String.class);
					message.setMessage(body);
					exchange.getIn().setBody(message, Message.class);
				}
			})
 			.to("cxf:bean:messageService");
 	}
 
 	public void setInputPath(String inputPath) {
 		this.inputPath = inputPath;
 	}
 
 }
