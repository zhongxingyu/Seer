 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URL;
 
 public class FightServer implements HttpHandler {
 	final Scorer scorer;
 	HttpServer server;
 
 	FightServer(Scorer scorer) {
 		this.scorer = scorer;
 	}
 
 	public static void main(String[] args) throws Exception {
 		URL planningUrl = URI.create("http://planning.code-story.net/planning.json").toURL();
		URL votesUrl = URI.create("http://planning.code-story.net/planning.json").toURL();
 
 		new FightServer(new Scorer(new TalkIds(planningUrl), new Scores(votesUrl))).start(8080);
 	}
 
 	@Override
 	public void handle(HttpExchange exchange) throws IOException {
 		String body = Files.toString(new File("index.html"), Charsets.UTF_8);
 
 		byte[] response = body.getBytes();
 		exchange.sendResponseHeaders(200, response.length);
 		exchange.getResponseHeaders().add("content-type", "text/html");
 		exchange.getResponseBody().write(response);
 		exchange.close();
 	}
 
 	public void start(int port) throws IOException {
 		server = HttpServer.create(new InetSocketAddress(port), 0);
 		server.createContext("/", this);
 		server.start();
 	}
 
 	public void stop() {
 		server.stop(0);
 	}
 }
