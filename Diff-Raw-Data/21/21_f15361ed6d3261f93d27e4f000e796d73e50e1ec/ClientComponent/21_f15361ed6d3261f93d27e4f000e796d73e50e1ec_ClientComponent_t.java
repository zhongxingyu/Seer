 package M1.overview;
 
 import M2.Component;
 import M2.ComponentPort;
import M2.Configuration;
 import M2.exceptions.ComponentServiceException;
 
 public class ClientComponent extends Component {
 
	public ClientComponent(Configuration parent) throws ComponentServiceException {
		super("Client", 0, parent);
 		
 		SendRequestPort sendRequestPort = new SendRequestPort(this);
 		this.addProvPort(sendRequestPort);
 
 		ReceiveRequestPort receiveRequestPort = new ReceiveRequestPort(this);
 		this.addReqPort(receiveRequestPort);
 		
		this.addProvService(new SendMessageService(this, new ComponentPort[]{}, new ComponentPort[]{sendRequestPort}));
 	}
 
 }
