 package serverNetworkSystems;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import misc.AnimationFromCharacterHelper;
 import misc.PlayerCharacteristics;
 
 import utils.input.Keyboard;
 import utils.input.Mouse;
 import behavior.PlayerScript;
 
 import entityFramework.IEntity;
 import entityFramework.IEntityArchetype;
 import entityFramework.IEntityWorld;
 import entitySystems.CameraControlSystem;
 import com.esotericsoftware.kryonet.Connection;
 
 import common.Network;
 import common.messages.NewPlayerMessage;
 import components.AnimationComp;
 import components.BehaviourComp;
 import components.InputComp;
 import components.ShadowComp;
 import components.TransformationComp;
 import content.ContentManager;
 
 public class ServerPlayerCreationNetworkSystem extends ServerNetworkSystem{
 
 	private final ContentManager contentManager;
 	private final Map<Integer, NewPlayerMessage> playerMessages;	
 	private final PlayerCharacteristics playerProperties;
 
 
 	public ServerPlayerCreationNetworkSystem(IEntityWorld world, Network server, ContentManager manager, PlayerCharacteristics pcars) {
 		super(world, server);
 		this.contentManager = manager;
 		this.playerProperties = pcars;
 		this.playerMessages = new HashMap<>();
 	}
 
 	@Override
 	public void initialize() {		
 		NewPlayerMessage message = new NewPlayerMessage();
 		message.senderID = 0;
 		message.messageID = 0;
 		message.playerCharacteristics = this.playerProperties;
 
 		this.playerMessages.put(0, message);
 		this.createNewPlayer(message);		
 	}
 
 
 	protected void createNewPlayer(NewPlayerMessage message) {	
 		System.out.println("Player Created " + message.senderID);
 
 		IEntityArchetype archetype = contentManager.loadArchetype("Player.archetype");
 		final IEntity entity = this.getEntityManager().createEntity(archetype);
 		entity.addComponent(new BehaviourComp(new PlayerScript()));
 		entity.addComponent(new ShadowComp());
 		entity.getComponent(TransformationComp.class).setPosition(1000,1000);
 		AnimationComp animComp = entity.getComponent(AnimationComp.class);
		animComp.setAnimationPlayer(AnimationFromCharacterHelper.animationPlayerFromCharacter(message.playerCharacteristics, contentManager));
 
 		entity.getComponent(InputComp.class).setKeyboard(new Keyboard());
 		entity.getComponent(InputComp.class).setMouse(new Mouse());
 		entity.setLabel("Player" + message.senderID);
 
 		if(message.senderID == 0)
 			entity.addToGroup(CameraControlSystem.GROUP_NAME);
 
 		entity.refresh();
 		message.messageID = entity.getUniqueID();	
 		this.getServer().sendToAllTCP(message);
 	}
 
 	@Override
 	protected void received(Connection connection, Object obj){
 		if(obj instanceof NewPlayerMessage) {
 			NewPlayerMessage message = (NewPlayerMessage)obj;	
 			sendExistingPlayers(connection);			
 
 			this.playerMessages.put(connection.getID(), message);
 			this.createNewPlayer(message);
 		}
 	}
 
 
 	@Override
 	protected void disconnected(Connection connection) {
 		NewPlayerMessage message = this.playerMessages.get(connection.getID());	
 		if(message != null){
 			removeDisconnectedPlayer(message);
 			this.playerMessages.remove(connection.getID());
 		}
 	}
 
 	private void removeDisconnectedPlayer(NewPlayerMessage message) {
 		IEntity entity = this.getEntityManager().getEntity(message.messageID);
 		if(entity != null) {
 			entity.kill();
 		}
 	}
 
 	private void sendExistingPlayers(Connection connection) {
 		for (NewPlayerMessage message : this.playerMessages.values()) {
 			connection.sendTCP(message);
 		}
 	}
 
 	@Override
 	public void process() {	}
 
 }
