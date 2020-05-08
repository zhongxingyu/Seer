 package pl.edu.agh.two.mud.server.world.fight.impl;
 
 import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import pl.edu.agh.two.mud.common.ICreature;
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.provider.CommandProvider;
 import pl.edu.agh.two.mud.common.message.MessageType;
 import pl.edu.agh.two.mud.common.world.model.Board;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.HitUICommand;
 import pl.edu.agh.two.mud.server.command.LogInUICommand;
 import pl.edu.agh.two.mud.server.command.LogOutUICommand;
 import pl.edu.agh.two.mud.server.command.RefreshUICommand;
 import pl.edu.agh.two.mud.server.command.RegisterUICommand;
 import pl.edu.agh.two.mud.server.command.RunUICommand;
 import pl.edu.agh.two.mud.server.command.SendAvailableCommandsCommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.TalkUICommand;
 import pl.edu.agh.two.mud.server.command.WhisperUICommand;
 import pl.edu.agh.two.mud.server.command.util.AvailableCommands;
 
 import com.google.common.collect.ImmutableList.Builder;
 
 public class DefaultFightTest {
 
 	private static final String PLAYER1_NAME = "player1";
 	private static final String PLAYER2_NAME = "player2";
 	private static final String CREATURE_NAME = "creature";
 
 	private static final String ATTACKING_USER_STRING = "Zaatkowales gracza %s";
 	private static final String ATTACKING_MONSTER_STRING = "Zaatkowales potwora %s";
 	private static final String ATTACKED_BY_USER_STRING = "Zostales zaatakowany przez gracza %s";
 	private static final String ATTACKED_BY_MONSTER_STRING = "Zostales zaatakowany przez potwora %s";
 
 	private IServiceRegistry serviceRegistry;
 	private Dispatcher dispatcher;
 	private Random random;
 	private Board board;
 	private static List<UICommand> fightOponnentTurn;
 	private static List<UICommand> fightYourTurn;
 	private static List<UICommand> gameCommands;
 	private static List<UICommand> deadPlayerCommands;
 
 	private DefaultFight fight;
 
 	@SuppressWarnings("unchecked")
 	@BeforeClass
 	public static void prepare() {
 		fightOponnentTurn = new Builder<UICommand>().add(mock(UICommand.class)).add(mock(UICommand.class)).build();
 		fightYourTurn = new Builder<UICommand>().add(mock(UICommand.class)).add(mock(UICommand.class))
 				.add(mock(UICommand.class)).build();
 		gameCommands = new Builder<UICommand>().add(mock(UICommand.class)).build();
 		deadPlayerCommands = new Builder<UICommand>().add(mock(UICommand.class)).add(mock(UICommand.class))
 				.add(mock(UICommand.class)).add(mock(UICommand.class)).build();
 
 		// AvailableCommands mocking
 		CommandProvider provider = mock(CommandProvider.class);
 
 		when(provider.getUICommands(RefreshUICommand.class, TalkUICommand.class, WhisperUICommand.class)).thenReturn(
 				fightOponnentTurn);
 		when(
 				provider.getUICommands(HitUICommand.class, RunUICommand.class, RefreshUICommand.class,
 						TalkUICommand.class, WhisperUICommand.class)).thenReturn(fightYourTurn);
 		when(
 				provider.getUICommandsWithout(RegisterUICommand.class, LogInUICommand.class, HitUICommand.class,
 						RunUICommand.class)).thenReturn(gameCommands);
 		when(
 				provider.getUICommands(RefreshUICommand.class, TalkUICommand.class, WhisperUICommand.class,
 						LogOutUICommand.class)).thenReturn(deadPlayerCommands);
 
 		AvailableCommands commands = AvailableCommands.getInstance();
 		commands.setCommandProvider(provider);
 	}
 
 	@Before
 	public void prepareTest() {
 		serviceRegistry = mock(IServiceRegistry.class);
 		dispatcher = mock(Dispatcher.class);
 		random = mock(Random.class);
 		board = mock(Board.class);
 
 		fight = new DefaultFight();
 		fight.setServiceRegistry(serviceRegistry);
 		fight.setDispatcher(dispatcher);
 		fight.setRandom(random);
 		fight.setBoard(board);
 
 	}
 
 	@Test
 	public void Player1Player2FirstBeginsFight() {
 		IPlayer player1 = preparePlayer(PLAYER1_NAME, 0, 0, 2);
 		IPlayer player2 = preparePlayer(PLAYER2_NAME, 0, 0, 1);
 		setInFight(player1, player2);
 
 		when(random.nextInt((player1.getAgililty() / 2) + 1)).thenReturn(1);
 		when(random.nextInt((player2.getAgililty() / 2) + 1)).thenReturn(0);
 
 		fight.startFight(player1, player2);
 
 		verifySendingMessage(player1, String.format(ATTACKING_USER_STRING, player2.getName()));
 		verifySendingMessage(player2, String.format(ATTACKED_BY_USER_STRING, player1.getName()));
 
 		switchCreatures(player2, player1);
 	}
 
 	@Test
 	public void Player1Player2SecondBeginsFight() {
 		IPlayer player1 = preparePlayer(PLAYER1_NAME, 0, 0, 1);
 		IPlayer player2 = preparePlayer(PLAYER2_NAME, 0, 0, 2);
 		setInFight(player1, player2);
 
 		when(random.nextInt((player1.getAgililty() / 2) + 1)).thenReturn(0);
 		when(random.nextInt((player2.getAgililty() / 2) + 1)).thenReturn(1);
 
 		fight.startFight(player1, player2);
 
 		verifySendingMessage(player1, String.format(ATTACKING_USER_STRING, player2.getName()));
 		verifySendingMessage(player2, String.format(ATTACKED_BY_USER_STRING, player1.getName()));
 
 		switchCreatures(player1, player2);
 	}
 
 	@Test
 	public void PlayerCreatureFirstBeginsFight() {
 		IPlayer player = preparePlayer(PLAYER1_NAME, 0, 0, 2);
 		ICreature creature = prepareCreature(CREATURE_NAME, 0, 0, 1);
 		setInFight(player, creature);
 
 		when(random.nextInt((player.getAgililty() / 2) + 1)).thenReturn(1);
 		when(random.nextInt((creature.getAgililty() / 2) + 1)).thenReturn(0);
 
 		fight.startFight(player, creature);
 
 		verifySendingMessage(player, String.format(ATTACKING_MONSTER_STRING, creature.getName()));
 		verifySendingMessage(creature, String.format(ATTACKED_BY_USER_STRING, player.getName()));
 
 		switchCreatures(creature, player);
 	}
 
 	@Test
 	public void PlayerCreatureSecondBeginsFight() {
 		IPlayer player = preparePlayer(PLAYER1_NAME, 1, 1, 1);
 		ICreature creature = prepareCreature(CREATURE_NAME, 1, 1, 2);
 		setInFight(player, creature);
 
 		when(random.nextInt((player.getAgililty() / 2) + 1)).thenReturn(0);
 		when(random.nextInt((creature.getAgililty() / 2) + 1)).thenReturn(1);
 
 		Service service = mock(Service.class);
 		when(serviceRegistry.getService(player)).thenReturn(service);
 
 		fight.startFight(player, creature);
 
 		verifySendingMessage(player, String.format(ATTACKING_MONSTER_STRING, creature.getName()));
 		verifySendingMessage(creature, String.format(ATTACKED_BY_USER_STRING, player.getName()));
 
 		switchCreatures(player, creature);
 		// TODO test hitting ?
 	}
 
 	@Test
 	public void CreaturePlayerFirstBeginsFight() {
 		IPlayer player = preparePlayer(PLAYER1_NAME, 1, 1, 1);
 		ICreature creature = prepareCreature(CREATURE_NAME, 1, 1, 2);
 		setInFight(player, creature);
 
 		when(random.nextInt((player.getAgililty() / 2) + 1)).thenReturn(0);
 		when(random.nextInt((creature.getAgililty() / 2) + 1)).thenReturn(1);
 
 		Service service = mock(Service.class);
 		when(serviceRegistry.getService(player)).thenReturn(service);
 		
 		fight.startFight(creature, player);
 
 		verifySendingMessage(player, String.format(ATTACKED_BY_MONSTER_STRING, creature.getName()));
 		verifySendingMessage(creature, String.format(ATTACKING_USER_STRING, player.getName()));
 
 		switchCreatures(player, creature);
 	}
 
 	@Test
 	public void CreaturePlayerSecondBeginsFight() {
 		IPlayer player = preparePlayer(PLAYER1_NAME, 1, 1, 2);
 		ICreature creature = prepareCreature(CREATURE_NAME, 1, 1, 1);
 		setInFight(player, creature);
 
 		when(random.nextInt((player.getAgililty() / 2) + 1)).thenReturn(1);
 		when(random.nextInt((creature.getAgililty() / 2) + 1)).thenReturn(0);
 
 		fight.startFight(creature, player);
 
 		verifySendingMessage(player, String.format(ATTACKED_BY_MONSTER_STRING, creature.getName()));
 		verifySendingMessage(creature, String.format(ATTACKING_USER_STRING, player.getName()));
 
 		switchCreatures(creature, player);
 		// TODO test hitting ?
 	}
 
 	private ICreature prepareCreature(String name, int strength, int power, int agility) {
 		ICreature creature = mock(ICreature.class);
 		setAttributes(creature, name, strength, power, agility);
 
 		return creature;
 	}
 
 	private IPlayer preparePlayer(String name, int strength, int power, int agility) {
 		IPlayer player = mock(IPlayer.class);
 		setAttributes(player, name, strength, power, agility);
 
 		return player;
 	}
 
 	private void setAttributes(ICreature creature, String name, int strength, int power, int agility) {
 		when(creature.getName()).thenReturn(name);
 		when(creature.getAgililty()).thenReturn(agility);
 		when(creature.getPower()).thenReturn(power);
 		when(creature.getStrength()).thenReturn(strength);
 	}
 
 	private void setInFight(ICreature creature1, ICreature creature2) {
 		when(creature1.getEnemy()).thenReturn(creature2);
 		when(creature2.getEnemy()).thenReturn(creature1);
 		when(creature1.isInFight()).thenReturn(true);
 		when(creature2.isInFight()).thenReturn(true);
 	}
 
 	private void switchCreatures(ICreature from, ICreature to) {
 		verifySendingMessage(from, "Tura przeciwnika");
 		verifySendingCommands(from, fightOponnentTurn);
 
 		verifySendingMessage(to, "Twoja tura");
 		verifySendingCommands(to, fightYourTurn);
 	}
 
 	private IPlayer castToPlayer(ICreature creature) {
 		if (creature instanceof IPlayer) {
 			return (IPlayer) creature;
 		}
 
 		return null;
 	}
 
 	// Verification methods
 	private void verifySendingMessage(ICreature creature, String message) {
 		IPlayer player = castToPlayer(creature);
 
 		if (player != null) {
 			verify(dispatcher).dispatch(new SendMessageToUserCommand(player, message, MessageType.INFO));
 		}
 	}
 
 	private void verifySendingCommands(ICreature creature, List<UICommand> commands) {
 		IPlayer player = castToPlayer(creature);
 
 		if (player != null) {
 			verify(dispatcher).dispatch(new SendAvailableCommandsCommand(player, commands));
 		}
 	}
 
 }
