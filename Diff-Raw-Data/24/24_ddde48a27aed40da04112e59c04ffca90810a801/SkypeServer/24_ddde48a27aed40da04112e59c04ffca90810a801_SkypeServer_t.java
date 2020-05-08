 package skype;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import sliceWars.RemoteInvite;
 import sliceWars.RemotePlay;
 import sliceWars.gui.GuiPlayer;
 import sliceWars.logic.Player;
 
 import com.skype.Application;
 import com.skype.ApplicationAdapter;
 import com.skype.Skype;
 import com.skype.SkypeException;
 import com.skype.Stream;
 import com.skype.StreamAdapter;
 import com.skype.connector.Connector;
 import com.thoughtworks.xstream.XStream;
 
 public class SkypeServer {
 
 	public static void awaitConnection() throws SkypeException {
 		Connector.getInstance().setApplicationName(SkypeMain.APPNAME);
         Skype.setDebug(true);
         Skype.setDaemon(false);
         Application application = Skype.addApplication(SkypeMain.APPNAME);
         
         final JFrame jFrame = new JFrame();
         jFrame.setLayout(new BorderLayout());
         jFrame.add(new JLabel("Awaiting connection"),BorderLayout.CENTER);
         JButton buttonCancel = new JButton("Cancel");
         buttonCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		jFrame.add(buttonCancel,BorderLayout.SOUTH);
         jFrame.pack();
         jFrame.setVisible(true);
         
         application.addApplicationListener(new ApplicationAdapter() {
             @Override
             public void connected(Stream stream) throws SkypeException {
                 System.out.println("connected:" + stream.getId());
                 final SkypePlayBroadcaster skypePlayBroadcaster = new SkypePlayBroadcaster();
                 skypePlayBroadcaster.setSkypePlayerStream(stream);
                 jFrame.dispose();
                 stream.addStreamListener(new StreamAdapter() {
                     @Override
                     public void textReceived(String receivedText) throws SkypeException {
                     	XStream xstream = new XStream();
                     	Object received = xstream.fromXML(receivedText);
                     	if(received instanceof RemoteInvite){
                     		RemoteInvite invite = (RemoteInvite) received;
                     		GuiPlayer guiPlayer = new GuiPlayer(new Player(2, 2), skypePlayBroadcaster, invite.get_randomSeed() , invite.get_numberOfPlayers(),invite.get_lines(),invite.get_columns(),invite.get_randomlyScenarioCells());
                     		skypePlayBroadcaster.setLocalPlayer(guiPlayer);
                     	}
                     	if(received instanceof RemotePlay){
                     		RemotePlay play = (RemotePlay) received;
                    		skypePlayBroadcaster.remotePlay(play);
                     	}
                     }
                 });
             }
             
             @Override
             public void disconnected(Stream stream) throws SkypeException {
             	SkypeMain.showDisconnectedMessageAndExit(stream);
             }
 
         });
 	}
 }
