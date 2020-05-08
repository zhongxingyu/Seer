 package com.gw.eip.router.resequencer;
 
 import com.gw.eip.router.aggregator.*;
 import org.apache.camel.*;
 import org.apache.camel.builder.*;
 import org.apache.camel.impl.*;
 import org.junit.*;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 import static com.jayway.awaitility.Awaitility.*;
 import static com.jayway.awaitility.Duration.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  * User: grzesiek
  * Date: 20.04.13
  * Time: 20:55
  */
 public class ResequencerTest {
 
 	private final CamelContext context = new DefaultCamelContext();
 	private final AggregationConsumer aggregationConsumer = new AggregationConsumer();
 
 	@Before
 	public void setUpContext() throws Exception {
 		context.addRoutes(new RouteBuilder() {
 			@Override
 			public void configure() throws Exception {
 				from("direct:input").split().tokenize(",").parallelProcessing().to("direct:split");
				from("direct:split").resequence(header(Exchange.SPLIT_INDEX)).to("direct:resequence");
				from("direct:resequence").aggregate(header(Exchange.CORRELATION_ID), new AppendAggregationStrategy()).completionSize(3).bean(aggregationConsumer, "consume");
 			}
 		});
 
 		context.start();
 	}
 
 	@Test
 	public void shouldResequenceSplittedMessagesBeforeAggregation() throws Exception {
 		final ProducerTemplate producerTemplate = context.createProducerTemplate();
 		final String message = "a,b,c";
 
 		producerTemplate.sendBody("direct:input", message);
 
 		waitAtMost(FIVE_SECONDS).until(aggregatedMessage(), is(equalTo("a, b, c")));
 	}
 
 	private Callable<String> aggregatedMessage() {
 		return new Callable<String>() {
 			@Override
 			public String call() throws Exception {
 				final List<String> messages = aggregationConsumer.getMessages();
 				return messages.isEmpty() ? null : messages.get(0);
 			}
 		};
 	}
 
 }
