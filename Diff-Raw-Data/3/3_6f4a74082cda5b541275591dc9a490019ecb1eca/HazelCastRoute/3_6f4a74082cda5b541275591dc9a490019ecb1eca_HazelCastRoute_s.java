 /**
  * 
  */
 package de.inovex.jax2013.showcase.hazelcast.consumer;
 
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 
 import de.inovex.jax2013.showcase.defaults.ShowcaseDefaults;
 
 
 /**
  * @author anierbeck
  * 
  */
 public class HazelCastRoute extends RouteBuilder {
 
 	private String bucketName;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.camel.builder.RouteBuilder#configure()
 	 */
 	@Override
 	public void configure() throws Exception {
 		
 		from(ShowcaseDefaults.HAZELCAST_QUEUE)
 				.id(ShowcaseDefaults.HAZELCAST_CONSUMER_ROUTE_ID)
 				.log(LoggingLevel.WARN, ShowcaseDefaults.MESSAGE_LOGGER,"Retrieved message: ${body}")
 				.setHeader("filenName").constant("MessageFile.txt")
 				.to("aws-s3://"+bucketName+"?amazonS3Client=#client");
 	}
 	
 	public void setBucketName(String bucketName) {
 		this.bucketName = bucketName;
 	}
 
 }
