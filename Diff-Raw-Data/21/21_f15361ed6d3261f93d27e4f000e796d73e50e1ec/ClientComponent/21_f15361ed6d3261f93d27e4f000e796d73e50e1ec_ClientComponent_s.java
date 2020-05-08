 package M1.overview;
 
import java.util.ArrayList;

 import M2.Component;
 import M2.ComponentPort;
 import M2.exceptions.ComponentServiceException;
 
 public class ClientComponent extends Component {
 
	public ClientComponent() throws ComponentServiceException {
		super("Client", 0);
 		
 		SendRequestPort sendRequestPort = new SendRequestPort(this);
 		this.addProvPort(sendRequestPort);
 
 		ReceiveRequestPort receiveRequestPort = new ReceiveRequestPort(this);
 		this.addReqPort(receiveRequestPort);
 		
		this.addProvService(new SendMessageService(this, new ComponentPort[]{sendRequestPort,receiveRequestPort}));
 	}
 
 }
