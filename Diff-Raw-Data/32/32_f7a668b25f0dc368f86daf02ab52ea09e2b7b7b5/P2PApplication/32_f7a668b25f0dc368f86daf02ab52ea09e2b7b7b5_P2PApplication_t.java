 package luposdate.operators;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Set;
 
 import lupos.datastructures.items.Variable;
 import lupos.datastructures.queryresult.QueryResult;
 import lupos.endpoint.server.format.Formatter;
 import lupos.endpoint.server.format.XMLFormatter;
 import lupos.engine.operators.application.Application;
 
 /**
  * Diese Klasse wird mit zusammen mit dem Triple Pattern verschickt und dann bei
 * dem Empfänger von dem ResultOperator aufgerufen (call Methode). Wir befinden
 * uns nach der Zeichnung an der Position (3), also direkt bevor der Empfänger
 * sein Ergebnis an den Sender zurückliefern will.
  * 
 * Teil Anfragegraph -------- (1) ------------------> (2) ----------- |Sender|
 * |Empfänger| -------- (4) <------------------ (3) ----------- Anfrage Result
  * 
  * Dazu kann man dem Result Operator (@see
  * lupos.engine.operators.singleinput.Result) sogenannte Applikationen zur
 * Verfügung stellen, die bei einem call Methodenaufruf alle abgearbeitet werden
  * und bei denen wiederrum call aufgerufen wird. So kann man verschiedene
 * Funktionalitäten in einem Result Operator zur Verfügung stellen. Eine
 * Applikation soll dann das Rücksenden durchführen
  * 
  * In der Call Methode ist das Ergebnis der Teilanfrage dann drin und soll an
 * den Ursprünglichen Sender zurück geschickt werden. Dazu muss der
 * ursprüngliche Sender beim versenden in dieser Klasse irgendwie seine Quell IP
 * festlegen, damit der Empfänger an diese dann zurückschicken kann.
  * 
  */
 public class P2PApplication implements Application {
 
 	public void call(QueryResult res) {
 		/*
 		 * Anfrageresultat (QueryResult res) kommt an (wir befinden uns auf der
		 * Empfangsseite und wollen jetzt wieder zurück schicken). Nun muss das
 		 * queryresult an den sender verschickt werden Zum Serialisieren des
 		 * QueryResult kann im Packet Endpoint.server/client.format/formatreader
 		 * die Klassen verwendet werden: Serialisierung:
 		 * lupos.endpoint.server.format; Deserialisierung:
 		 * lupos.endpoint.client.formatreader;
 		 */
 
 		// Schritt 3: Ruecksenden des QueryResults
 
 		Formatter formatter = new XMLFormatter();
 
 		// hier kommt die Implementierung fur das P2P Netz hin
 		OutputStream os = null;
 
 
 		// TODO: schlecht, weil hier das Iteratorkonzept verletzt wird. es muss
 		// gewartet werden, bis das komplette query result erzeugt wurde
 		Set<Variable> variables = res.getVariableSet();
 
 		try {
 			formatter.writeResult(os, variables, res);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void start(Type type) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void stop() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void deleteResult(QueryResult res) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void deleteResult() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
