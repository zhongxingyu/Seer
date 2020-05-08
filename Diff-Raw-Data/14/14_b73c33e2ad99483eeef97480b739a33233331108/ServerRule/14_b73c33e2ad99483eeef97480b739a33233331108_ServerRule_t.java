 package cs13;
 
 import gumi.builders.UrlBuilder;
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
import java.io.IOException;
import java.net.ServerSocket;
 import java.util.concurrent.ExecutionException;
 
 public class ServerRule implements TestRule {
     private CodeStory2013 codeStory2013;
 
    private void startServer() throws ExecutionException, InterruptedException, IOException {
        codeStory2013 = new CodeStory2013(findAvailablePort());
         codeStory2013.start();
     }
 
     private void stopServer() {
         codeStory2013.stop();
     }
 
     public UrlBuilder uriBuilder() {
         return UrlBuilder.fromUri(codeStory2013.uri());
     }
 
    private int findAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

 
     public Statement apply(final Statement base, Description description) {
         return new Statement() {
             @Override
             public void evaluate() throws Throwable {
                 startServer();
                 try {
                     base.evaluate();
                 } finally {
                     stopServer();
                 }
             }
         };
     }
 }
