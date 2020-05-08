 package projectrts.model.abilities;
 
 import java.util.List;
 
 import projectrts.model.entities.AbstractEntity;
 import projectrts.model.entities.AbstractStructure;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.IPlayerControlledEntity;
 import projectrts.model.player.Player;
 import projectrts.model.utils.Position;
 /**
  * An ability for delivering a resource at a deposit structure
  * @author Jakob Svensson
  *
  */
 public class DeliverResourceAbility extends AbstractAbility implements IUsingMoveAbility {
 	
 	private IPlayerControlledEntity entity;
 	private AbstractStructure depositStructure;
 	private AbstractAbility moveAbility;
 	private double range = 1;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(DeliverResourceAbility.class.getSimpleName(), new DeliverResourceAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(IPlayerControlledEntity entity, MoveAbility moveAbility) {
 		this.entity=entity;
 		this.moveAbility = moveAbility;
 	}
 	
 	@Override
 	public String getName() {
 		return "DeliverResource";
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			
 			findDepositStructure();
 			
 			//if(Position.getDistance(entity.getPosition(),depositStructure.getPosition() )<1.5*depositStructure.getSize()){
 			if(inRange(depositStructure)){
 				//If in range of deposit structure
 				moveAbility.setFinished(true);
 				
 				Player player = (Player)entity.getOwner();
 				player.modifyResource(12);//TODO Jakob: As a consequence of removing P, 12 is now hardcoded. should be fixed in some way
 				setFinished(true);
 			}else{
 				// Not in range
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(depositStructure.getPosition());
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void useAbility(Position target) {
 		setActive(true);
 		setFinished(false);
 		
 	}
 	
 	private void findDepositStructure(){
 		List<IPlayerControlledEntity> entities = 
 				EntityManager.getInstance().getEntitiesOfPlayer(entity.getOwner());
 		
 		for(IPlayerControlledEntity e: entities){
 			if(e instanceof AbstractStructure){
 				AbstractStructure struct = (AbstractStructure)e;
 				if(struct.isDeposit()){
 					if(depositStructure == null) {
 						depositStructure = struct;
 					}else{
 						//System.out.println(e.getPosition());
 						if(Position.getDistance(entity.getPosition(), e.getPosition())
 							<Position.getDistance(entity.getPosition(), depositStructure.getPosition())){
 							//If e is closer to unit than saved depositStructure
 							depositStructure = struct;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public AbstractAbility createAbility(IPlayerControlledEntity entity, MoveAbility moveAbility) {
 		DeliverResourceAbility newAbility = new DeliverResourceAbility();
 		newAbility.initialize(entity, moveAbility);
 		return newAbility;
 		
 	}
 
 	private boolean inRange(AbstractEntity target)
 	{
		// TODO Plankton: !!!Problem here, because Headquarter has size 2, but takes up space for size 3...
		return (Position.getDistance(entity.getPosition(), target.getPosition()) < range  + target.getSize()*1.5);
		//return (Position.getDistance(entity.getPosition(), target.getPosition()) < range  + (target.getSize()/2)*1.5);
 	}
 }
