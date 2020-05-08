 package skype;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
 import sliceWars.remote.Broadcaster;
 import sliceWars.remote.LocalGame;
 import sliceWars.remote.messages.AllPlayersInvitesResult;
 import sliceWars.remote.messages.AnswerToTheInvitation;
 import sliceWars.remote.messages.Invitation;
 import sliceWars.remote.messages.RemotePlay;
 
 import com.skype.ApplicationListener;
 import com.skype.SkypeException;
 import com.skype.Stream;
 import com.skype.StreamAdapter;
 import com.thoughtworks.xstream.XStream;
 
 public class SkypeBridge implements ApplicationListener {
 
 	private LocalGame localGame;
 	private boolean server = false;
 	private List<String> _invitedNames;
 	private int randomSeed;
 	private int lines = 6;
 	private int columns = 6;
 	private int randomScenariosCellsCount = 12;
 	
 	public SkypeBridge(final String id) {
 		localGame = new LocalGame(new Broadcaster(), id );
 		Random random = new Random(Calendar.getInstance().getTimeInMillis());
 		randomSeed = random.nextInt();
 	}
 
     @Override
     public void connected(Stream stream) throws SkypeException {
    	System.out.println("Connected to "+stream.getId());
     	localGame.addRemotePlayer(new SkypePlayer(stream));
     	if(isClient()){
     		SkypeServerGame game = new SkypeServerGame(stream);
     		localGame.setServerGame(game);
     	}
     	if(isServer()){
    		int invitedIndexOnIntedList = _invitedNames.indexOf(stream.getId());
			int idStartingOnOne = invitedIndexOnIntedList+1;
    		int firstPlayerIsServer = idStartingOnOne + 1;
 			localGame.setGameSettings(_invitedNames.size()+1,randomSeed,randomScenariosCellsCount,lines,columns);
			Invitation invitation = new Invitation(randomSeed, _invitedNames, firstPlayerIsServer, lines, columns, randomScenariosCellsCount);
 			SkypeApp.sendMessageThroughStream(invitation, stream);
     	}
     	
         stream.addStreamListener(new StreamAdapter() {
             @Override
             public void textReceived(String receivedText) throws SkypeException {
             	XStream xstream = new XStream();
             	Object received = xstream.fromXML(receivedText);
             	
             	if(received instanceof Invitation){
             		localGame.invite((Invitation) received);
             	}
             	if(received instanceof AllPlayersInvitesResult){
             		localGame.allPlayersInvitesResult((AllPlayersInvitesResult) received);
             	}
             	
             	if(received instanceof AnswerToTheInvitation){
             		localGame.answerToTheInvitation((AnswerToTheInvitation) received);
             	}
             	
             	if(received instanceof RemotePlay){
             		localGame.play((RemotePlay) received);
             	}
             }
         });
     }
 
 	private boolean isClient() {
 		return !server;
 	}
 	
 	private boolean isServer() {
 		return server;
 	}
     
     @Override
     public void disconnected(Stream stream) throws SkypeException {
     	SkypeApp.showDisconnectedMessageAndExit(stream);
     }
 
 	public void setServer() {
 		server = true;
 	}
 
 	public void setInvited(List<String> invitedNames) {
 		_invitedNames = invitedNames;
 	}
 
 }
