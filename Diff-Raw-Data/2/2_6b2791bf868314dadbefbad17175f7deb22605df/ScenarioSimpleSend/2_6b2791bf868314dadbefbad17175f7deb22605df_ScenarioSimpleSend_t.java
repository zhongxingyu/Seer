 package ch.ethz.mlmq.client.testscenario;
 
 import java.io.IOException;
 
 import ch.ethz.mlmq.client.Client;
 import ch.ethz.mlmq.client.ClientConfiguration;
 import ch.ethz.mlmq.dto.QueueDto;
 
 public class ScenarioSimpleSend {
 
 	private Client client;
 	private ClientConfiguration config;
 
 	public ScenarioSimpleSend(Client client, ClientConfiguration config) {
 		this.client = client;
 		this.config = config;
 	}
 
 	public void run() throws IOException {
 
		client.register();

 		QueueDto queue = client.createQueue("QueueOf" + config.getName());
 
 		for (int i = 0; i < 100; i++) {
 			byte[] content = ("Some Random Text and message Nr " + i).getBytes();
 			client.sendMessage(queue.getId(), content, i % 10);
 		}
 	}
 }
