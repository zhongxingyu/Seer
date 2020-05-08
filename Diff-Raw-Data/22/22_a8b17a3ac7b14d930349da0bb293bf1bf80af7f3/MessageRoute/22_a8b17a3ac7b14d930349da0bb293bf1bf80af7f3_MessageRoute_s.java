 /**
  * 
  */
 package de.inovex.jax2013.showcase.cxf.producer;
 
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 
 import de.inovex.jax2013.showcase.defaults.ShowcaseDefaults;
 
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
 			.to("cxf:bean:messageService");
 	}
 
 	public void setInputPath(String inputPath) {
 		this.inputPath = inputPath;
 	}
 
 }
