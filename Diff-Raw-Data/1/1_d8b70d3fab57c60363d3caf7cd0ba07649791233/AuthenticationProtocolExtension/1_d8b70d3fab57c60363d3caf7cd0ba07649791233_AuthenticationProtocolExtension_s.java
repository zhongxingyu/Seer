 package de.uulm.presenter.protocol;
 
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 import de.uulm.presenter.auth.Authentication;
 import de.uulm.presenter.control.Main;
 import de.uulm.presenter.io.IODevice;
 import de.uulm.presenter.io.IORemote;
 import de.uulm.presenter.io.IORemoteImpl;
 import de.uulm.presenter.remote.RemoteDevice;
 import de.uulm.presenter.view.MessagePrompt;
 
 public class AuthenticationProtocolExtension extends RegisteredMessageHandler implements IODevice{
 
 	private ProtocolState state;
 	private int challenge=0;
 	private final MessagePrompt authPrompt;
 	
 	public AuthenticationProtocolExtension() throws IOException {
 		state = ProtocolState.UNAUTHORIZED;
 		authPrompt = MessagePrompt.getInstance();
 	}
 	
 	@Override
 	public void aMessage(Object o) {
 		//System.out.println("raw message: "+(String) o);
 		
 		if (state==ProtocolState.UNAUTHORIZED){
 //			if ((challenge+"").equals((String)o)){
 //				state=ProtocolState.AUTHORIZED;
 //				authPrompt.authSuccess();
 //				Main.control.stateServerConnected();
 //			}else{
 //				IORemoteImpl.getRemoteDevice().kickDevices();
 //				authPrompt.authFailed();
 //			}
 		}else{
 			for (IORemote r:remoteDevices){
 				r.aMessage(o);
 			}
 		}
 	}
 	
 
 	@Override
 	public void init() {
 		super.init();
 		//challenge=Authentication.generateChallenge();
 		//JOptionPane.showMessageDialog(null, "Type: "+challenge+" in your phone to confirm security authentication", "Presenter BT guard", JOptionPane.INFORMATION_MESSAGE);
 		//authPrompt.showAuth(challenge);
 		
 		int i = JOptionPane.showConfirmDialog(null, "<html><p>An external device is trying to connect to your phone.<br>Allow this connection? </p></html>", "Connection attempt", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
 		if (i == JOptionPane.YES_OPTION){
 			state=ProtocolState.AUTHORIZED;
 			RemoteDevice.authOK();
 		}else{
 			RemoteDevice.authReject();
 			IORemoteImpl.getRemoteDevice().kickDevices();
 		}
 	}
 	@Override
 	public synchronized void shutdown() {
 		//authPrompt.connectionLost();
 		if (state==ProtocolState.AUTHORIZED){
 			JOptionPane.showMessageDialog(null, "Connection lost", "Connection error", JOptionPane.ERROR_MESSAGE);
 		}
 		super.shutdown();
 	}
 }
 
 enum ProtocolState{
 	UNAUTHORIZED,AUTHORIZED
 }
