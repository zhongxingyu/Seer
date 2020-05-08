 package scratch;
 
 import java.io.File;
 
 import de.tuberlin.dima.presslufthammer.query.Query;
 import de.tuberlin.dima.presslufthammer.query.parser.QueryParser;
 import de.tuberlin.dima.presslufthammer.transport.CLIClient;
 import de.tuberlin.dima.presslufthammer.transport.Coordinator;
 import de.tuberlin.dima.presslufthammer.transport.Leaf;
 import de.tuberlin.dima.presslufthammer.transport.messages.QueryMessage;
 
 /**
  * Test the query handling by creating a coordinator and some leafs in the same
  * process.
  * 
  * @author Aljoscha Krettek
  */
 public class QueryTest {
     private static final String HOST = "localhost";
     private static final int PORT = 44444;
     private static final String DATASOURCES = "src/main/example-data/DataSources.xml";
     private static final File LEAF_DATAFIR = new File("data-dir");
 
     public static void main(String[] args) throws Exception {
 
         Coordinator coord = new Coordinator(PORT, DATASOURCES);
         coord.start();
         Leaf leaf1 = new Leaf(HOST, PORT, LEAF_DATAFIR);
         Leaf leaf2 = new Leaf(HOST, PORT, LEAF_DATAFIR);
         Leaf leaf3 = new Leaf(HOST, PORT, LEAF_DATAFIR);
         leaf1.start();
         leaf2.start();
         leaf3.start();
 
         CLIClient client = new CLIClient(HOST, PORT);
 
         if (client.start()) {
             Query query = QueryParser
                    .parse("SELECT Document.DocId, Document.Name.Language.Code,Document.Name.Language.Country FROM DOCUMENT");
             QueryMessage queryMsg = new QueryMessage(-1, query);
             client.query(queryMsg);
         }
 
         Thread.sleep(2000);
 
         client.stop();
         leaf1.stop();
         leaf2.stop();
         leaf3.stop();
         coord.stop();
     }
 }
