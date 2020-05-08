 package sliceWars.remote;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import org.apache.commons.lang.StringUtils;
 
 import sliceWars.gui.GuiPlayer;
 import sliceWars.logic.Player;
 import sliceWars.remote.messages.AllPlayersInvitesResult;
 import sliceWars.remote.messages.AnswerToTheInvitation;
 import sliceWars.remote.messages.Invitation;
 import sliceWars.remote.messages.RemotePlay;
 import sliceWars.remote.messages.GameMessageListener;
 
 public class LocalGame implements Game{
 	
 	private final Broadcaster _broadcaster;
 	private Game serverGame;
 	private String _id;
 	private Map<String, Boolean> answersToInvites = new LinkedHashMap<>();
 	private int _playerCount;
 	private int _player;
 	private int _randomSeed;
 	private int _randomlyScenarioCells;
 	private int _lines;
 	private int _columns;
 
 	public LocalGame(final Broadcaster broadcaster,final String id) {
 		_broadcaster = broadcaster;
 		this._id = id;
 	}
 	
 	public void play(RemotePlay play) {
 		_broadcaster.receivesRemotePlay(play);
 	}
 
 	public void invite(Invitation invitation) {
 		if(serverGame == null){
 			throw new IllegalStateException("Someone invited player for a game while he was inviting others....");
 		}
 			
 		String otherPlayersNames = StringUtils.join(invitation.getPlayerNames(),", ");
 		int result = JOptionPane.showConfirmDialog(null,"Gostaria de jogar com " + otherPlayersNames + " ?", "Convite de jogo Slice wars", JOptionPane.YES_NO_OPTION);
 		if(result == JOptionPane.NO_OPTION){
 			boolean accepted = false;
 			serverGame.answerToTheInvitation(new AnswerToTheInvitation(_id,accepted));
 			return;
 		}
 		_playerCount = invitation.getPlayersCount();
 		_player = invitation.getPlayerNumber();
 		_randomSeed = invitation.getRandomSeed();
 		_randomlyScenarioCells = invitation.getRandomlyScenarioCells();
 		_lines = invitation.getLines();
 		_columns = invitation.getColumns();
 	}
 
 	public void addRemotePlayer(final GameMessageListener player) {
 		_broadcaster.addRemotePlayer(player);
 	}
 	
 
	public void setGameSettings(final int playerCount, final int randomSeed, final int randomlyScenarioCells, final int lines, final int columns){
 		_playerCount = playerCount;
 		_randomSeed = randomSeed;
 		_randomlyScenarioCells = randomlyScenarioCells;
 		_lines = lines;
 		_columns = columns;
 		_player = 1;
 	}
 	
	public void setServerGame(final Game game){	
		this.serverGame = game;
	}
	
 	@Override
 	public void answerToTheInvitation(AnswerToTheInvitation answerToTheInvitation) {
 		boolean accepted = answerToTheInvitation.isAccepted();
 		answersToInvites.put(answerToTheInvitation.getContactName(), accepted);
 		if(!accepted){
 			_broadcaster.allPlayersInviteResult(new AllPlayersInvitesResult(false));
 			return;
 		}
 		if(answersToInvites.size() == _broadcaster.getPlayerCount()){
 			_broadcaster.allPlayersInviteResult(new AllPlayersInvitesResult(true));
 			return;
 		}
 	}
 
 	public void allPlayersInvitesResult(AllPlayersInvitesResult allPlayersInvitesResult) {
 		if(!allPlayersInvitesResult.isAllPlayersHaveAccepted()){
 			JOptionPane.showInternalMessageDialog(null, "Alguem no aceitou o jogo, saindo...");
 			System.exit(0);
 		}
 			
 		GuiPlayer guiPlayer = new GuiPlayer(new Player(_player, _playerCount), _broadcaster, _randomSeed , _playerCount,_lines,_columns,_randomlyScenarioCells);
 		_broadcaster.setLocalPlayer(guiPlayer);
 	}
 
 }
