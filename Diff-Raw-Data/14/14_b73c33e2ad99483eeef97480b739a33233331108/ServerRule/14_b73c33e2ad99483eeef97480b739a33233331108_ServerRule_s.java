 package cs13;
 
 import gumi.builders.UrlBuilder;
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 import java.util.concurrent.ExecutionException;
 
 public class ServerRule implements TestRule {
     private CodeStory2013 codeStory2013;
 
    private void startServer() throws ExecutionException, InterruptedException {
        codeStory2013 = new CodeStory2013(8087);
         codeStory2013.start();
     }
 
     private void stopServer() {
         codeStory2013.stop();
     }
 
     public UrlBuilder uriBuilder() {
         return UrlBuilder.fromUri(codeStory2013.uri());
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
