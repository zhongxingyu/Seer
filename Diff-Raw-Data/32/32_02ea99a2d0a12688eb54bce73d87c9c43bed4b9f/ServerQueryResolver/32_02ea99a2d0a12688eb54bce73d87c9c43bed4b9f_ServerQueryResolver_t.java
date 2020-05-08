 package nl.ru.cs.phasar.mediator.documentsource;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import java.util.concurrent.Executors;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import nl.naiaden.twistinator.client.TwistClientHandler;
 import nl.naiaden.twistinator.client.TwistClientPipelineFactory;
 import nl.naiaden.twistinator.indexer.document.Keyword;
 import nl.naiaden.twistinator.indexer.document.Relator;
 import nl.naiaden.twistinator.indexer.document.Triple;
 import nl.naiaden.twistinator.objects.SearchQuery;
 import nl.ru.cs.phasar.mediator.userquery.Metadata;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 /**
  * @author louis & bartvz
  */
 public class ServerQueryResolver implements DocumentSource {
 
     Logger log = Logger.getLogger(ServerQueryCache.class);
 
     static {
 	final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
 	if (!rootLogger.getAllAppenders().hasMoreElements()) {
 	    rootLogger.setLevel(Level.DEBUG);
 	    rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} [%-11t] %x %-5p %c{1} - %m%n")));
 	}
     }
 
     @Override
     public List<Result> getDocuments(Metadata metadata, List<nl.ru.cs.phasar.mediator.documentsource.Triple> triples) {
 
 	List<Result> resultList = new ArrayList<Result>();
 
 	//Searching without the triple(s) to find is imposible
 	if (triples.isEmpty()) {
 	    return resultList;
 	}
 
 	// Parse options.
 	final String host = "localhost";
 	final int port = 8123;
 
 
 	// Configure the client.
 	ClientBootstrap bootstrap = new ClientBootstrap(
 		new NioClientSocketChannelFactory(
 		Executors.newCachedThreadPool(),
 		Executors.newCachedThreadPool()));
 
 
 	// Set up the pipeline factory.
 	bootstrap.setPipelineFactory(new TwistClientPipelineFactory());
 
 	// Start the connection attempt.
 	ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
 	Channel channel = connectFuture.awaitUninterruptibly().getChannel();
 
 	TwistClientHandler handler = (TwistClientHandler) channel.getPipeline().getLast();
 
 	// Create the query
 	for (nl.ru.cs.phasar.mediator.documentsource.Triple t : triples) {
 	    handler.addQuery(new SearchQuery(new Triple(new Keyword(t.getGroundHead()), new Relator(t.getRelator()), new Keyword(t.getGroundTail()))));
 	}
 
 	String serverResult = handler.getMessage().toString();
 
 	serverResult = serverResult.replaceFirst("SearchResult: ", "");
 
 	String[] split = serverResult.split("\\[+[^\\s]+\\]");
	Matcher relatorMatcher = Pattern.compile(",[A-Z]+[^,]*,").matcher("");
	Matcher matcher = Pattern.compile("\\<+[^\\s]+\\>").matcher("");
	String[] words;
	String relator;
 
 	for (int s = 0; s < split.length; s++) {
 
 	    String sentence = split[s].replaceAll("\\<+[^\\s]+\\>", "");
 	    Result result = new Result(sentence);
 
	    matcher.reset(split[s]);

 	    while (matcher.find()) {
 		String protoTriple = matcher.group().replaceAll("<|>", "");

		words = protoTriple.split(",[A-Z]+[^,]*,");
		relatorMatcher.reset(protoTriple);

		if ((relatorMatcher.find()) && (words.length == 2)) {
		    relator = relatorMatcher.group();
		    relator = relator.replace(",", "");
		    relator.trim();

		    nl.ru.cs.phasar.mediator.documentsource.Triple triple = new nl.ru.cs.phasar.mediator.documentsource.Triple(words[0], relator, words[1], words[1]);

		    result.addTriple(triple);
		} else {
		    relator = null;
		}
 	    }
 	    resultList.add(result);
 	}
 
 	handler.done = true;
 
 	bootstrap.releaseExternalResources();
 
 	return resultList;
     }
 }
