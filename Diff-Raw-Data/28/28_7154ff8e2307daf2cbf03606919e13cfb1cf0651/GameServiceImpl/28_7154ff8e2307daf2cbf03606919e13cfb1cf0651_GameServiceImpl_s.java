 package com.whiuk.philip.game.server.game;
 
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.whiuk.philip.game.server.MessageHandlerService;
 import com.whiuk.philip.game.server.auth.Account;
 import com.whiuk.philip.game.server.auth.AuthService;
 import com.whiuk.philip.game.server.watchdog.WatchdogService;
 import com.whiuk.philip.game.shared.Messages.ClientMessage.GameData.ActionInformation;
 import com.whiuk.philip.game.shared.Messages.ClientMessage.GameData.CombatInformation;
 import com.whiuk.philip.game.shared.Messages.ClientMessage.GameData.MovementInformation;
 import com.whiuk.philip.game.shared.Messages.ServerMessage;
 import com.whiuk.philip.game.shared.Messages.ClientMessage.GameData;
 
 /**
  * Game Service implementation.
  * @author Philip Whitehouse
  *
  */
 @Service
 public class GameServiceImpl implements GameService {
 	/**
 	 * Authentication service.
 	 */
 	@Autowired
 	private AuthService authService;
 	/**
 	 * Message handler.
 	 */
 	@Autowired
 	private MessageHandlerService messageHandlerService;
 	/**
 	 * Character -> Account mapping.
 	 */
 	private Map<Character, Account> accounts;
 	/**
 	 * Account -> Character mapping.
 	 */
 	private Map<Account, Character> characters;
 
 	/**
 	 * Watchdog service.
 	 */
 	private WatchdogService watchdogService;
 
 	/**
 	 * Game world.
 	 */
 	private GameWorld gameWorld;
 
 	/**
 	 * Initialize the service.
 	 */
 	@PostConstruct
 	public final void init() {
 		watchdogService.monitor(gameWorld);
 	}
 	
 	@Override
 	public final void processMessage(final Account account, final GameData data) {
 		if (data.getType() == GameData.Type.CHARACTER_SELECTION) {
 			characterSelection(account, data);
 		} else if (data.getType() == GameData.Type.EXIT) {
 			if (accounts.get(account) != null) {
 				//TODO: Logout
 			}
 		} else if (characters.get(account) != null) {
 			update(account, data);
 			
 		} else {
 			//TODO: Log action attempted when no character selected
 		}
     }
 
 	/**
 	 * Handle client game update.
 	 * @param account
 	 * @param data
 	 */
 	private void update(final Account account,
 			final GameData data) {
 		switch(data.getType()) {
 			case MOVEMENT: move(characters.get(account),
 					data.getMovementInformation()); break;
 			case ACTION: action(characters.get(account),
 					data.getActionInformation()); break;
 			case COMBAT: combat(characters.get(account),
 					data.getCombatInformation()); break;
 			default:
 				messageHandlerService
 					.handleUnknownMessageType(authService
 					.getConnection(account));
 		}
 	}
 
 	/**
 	 * Handle character selection messages.
 	 * @param account
 	 * @param data
 	 */
 	private void characterSelection(final Account account,
 			final GameData data) {
 		if (characters.get(account) != null) {
 			ServerMessage message = ServerMessage
 				.newBuilder()
 				.setType(ServerMessage.Type.GAME)
 				.setGameData(ServerMessage
 				.GameData.newBuilder()
 				.setError(ServerMessage.GameData
 				.Error.CHARACTER_ALREADY_SELECTED))
 				.build();
 			messageHandlerService.processOutboundMessage(message);
 		} else {
 			//TODO: Load character
 		}
 	}
 
 	/**
 	 * Handles character combat messages.
 	 * @param character
 	 * @param combatInformation
 	 */
 	private void combat(final Character character,
 			final CombatInformation combatInformation) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Handles action messages.
 	 * @param character
 	 * @param actionInformation
 	 */
 	private void action(final Character character,
 			final ActionInformation actionInformation) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Handles character movement messages.
 	 * @param character
 	 * @param movementInformation
 	 */
 	private void move(final Character character,
 			final MovementInformation movementInformation) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
