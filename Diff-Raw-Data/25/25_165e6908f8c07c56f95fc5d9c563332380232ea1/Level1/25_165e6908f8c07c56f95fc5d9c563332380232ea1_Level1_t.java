 package screenCore;
 
import java.util.ArrayList;
 import java.util.List;
 
 import behavior.PortalBehavior;
 import behavior.SpawnBehaviour;
 
 import components.BehaviourComp;
 import components.RenderingComp;
 import components.TransformationComp;
 
 import gameMap.SceneNode;
 import gameMap.TexturedSceneNode;
 import content.ContentManager;
 import entityFramework.IEntity;
 import entityFramework.IEntityArchetype;
 import utils.StringHelper;
 import utils.time.TimeSpan;
 
 public class Level1 extends Level{
 	private static final String triggetSpawnArchetypeName = "Spawn.archetype";
 	private static final String portalArchetypeName = "Portal.archetype";
 	
 	
 	public Level1(boolean popup, TimeSpan transOnTime, TimeSpan transOffTime,
 			String level) {
 		super(popup, transOnTime, transOffTime, level);
 	}
 	
 	@Override
 	public void initialize(ContentManager contentManager) {
 		super.initialize(contentManager);
 		
 		
 		SpawnTextureNodes();
 		SpawnOther();
 		
 		
 	}
 
 	private void SpawnOther() {
		List<SceneNode> nodes = new ArrayList<>(this.scene.getNodes());
 		this.SpawnTriggers(nodes);
 		this.SpawnPortals(nodes);
 		this.SpawnArchetypes(nodes);
 	}
 
 	private void SpawnArchetypes(List<SceneNode> nodes) {
		for (int i = nodes.size() - 1; i >= 0; i--) {
 			SceneNode node = nodes.get(i);
			if(node.getNodeID().contains("Spawn") && !node.getNodeID().contains("Player")) {
 				String[] data = StringHelper.split(node.getNodeID(), '_');
 				String archetypeName = data[1];
 				IEntityArchetype archetype = this.ScreenManager.getContentManager().loadArchetype(archetypeName);
 				this.World.getEntityManager().createEntity(archetype);
 			}
 		}
 	}
 
 	private void SpawnPortals(List<SceneNode> nodes) {
 		IEntityArchetype archetype = this.ScreenManager.getContentManager().loadArchetype(portalArchetypeName);
		for (int i = nodes.size() - 1; i >= 0; i--) {
 			SceneNode node = nodes.get(i);
 			if(node.getNodeID().contains("Portal")) {
 				IEntity portal = this.World.getEntityManager().createEntity(archetype);
 				
 				String otherPortalID;
 				if(node.getNodeID().contains("X")) {
 					otherPortalID = "PortalY" + node.getNodeID().charAt(node.getNodeID().length() - 1);
 				} else {
 					otherPortalID = "PortalX" + node.getNodeID().charAt(node.getNodeID().length() - 1);
 				}				
 				
 				PortalBehavior behavior = new PortalBehavior(node.getNodeID(), otherPortalID);
 				portal.addComponent(new BehaviourComp(behavior));
 				portal.refresh();
 				nodes.remove(i);
 			}
 		}
 	}
 
 	private void SpawnTriggers(List<SceneNode> nodes) {
 		IEntityArchetype triggerSpawnArchetype = this.ScreenManager.getContentManager().loadArchetype(triggetSpawnArchetypeName);
		for (int i = nodes.size() - 1; i >= 0; i--) {
 			SceneNode node = nodes.get(i);
 			if(node.getNodeID().contains("Trigger")) {
 				String[] data = StringHelper.split(node.getNodeID(), '_');
 				
 				String archetypeName = data[1];
 				int count = Integer.parseInt(data[2]);
 				
 				SpawnBehaviour behaviour = new SpawnBehaviour(archetypeName, count);
 				IEntity entity = this.World.getEntityManager().createEntity(triggerSpawnArchetype);
 				entity.addComponent(new BehaviourComp(behaviour));
 				entity.refresh();
 				nodes.remove(i);
 			}
 		}
 	}
 
 	
 	
 	private void SpawnTextureNodes() {
 		for (TexturedSceneNode node : this.scene.getTexturedNodes()) {
 			IEntity entity = this.World.getEntityManager().createEmptyEntity();
 			TransformationComp transComp = new TransformationComp();
 			transComp.setPosition(node.getPosition());
 			transComp.setOrigin(node.getSrcRectangle().getCenter());
 			RenderingComp renderComp = new RenderingComp();
 			renderComp.setTexture(node.getTexture());
 			renderComp.setSource(node.getSrcRectangle());
 
 			entity.addComponent(transComp);
 			entity.addComponent(renderComp);
 			entity.refresh();
 		}
 	}
 }
