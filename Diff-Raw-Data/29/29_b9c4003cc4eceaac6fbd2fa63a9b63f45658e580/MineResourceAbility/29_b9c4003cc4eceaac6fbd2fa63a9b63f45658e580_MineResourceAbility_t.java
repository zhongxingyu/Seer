 package projectrts.model.core.abilities;
 
 import projectrts.model.core.P;
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.PlayerControlledEntity;
 import projectrts.model.core.entities.Resource;
 import projectrts.model.core.utils.ModelUtils;
 /**
  * An ability for moving to a resource and mine from it
  * @author Jakob Svensson
  *
  */
 public class MineResourceAbility extends AbstractAbility{
 	private Resource targetResource;
 	private PlayerControlledEntity unit;
 	private AbstractAbility moveAbility;
 	private int resourceCarriedAmount;
 	private final float recoveryTime = 0.3f;
 	private float miningCooldown = 0;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(MineResourceAbility.class.getSimpleName(), new MineResourceAbility());
 	}
 	
 	private MineResourceAbility() {
 		super();
 	}
 	
 	@Override
 	public String getName() {
 		return "FetchResource";
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			
			if(ModelUtils.INSTANCE.getDistance(unit.getPosition(),targetResource.getPosition() )<1.5*targetResource.getSize()){
 				//If in range of resource
 				//Check cooldown and mine resource or reduce cooldown as appropriate.
 				if (miningCooldown <= 0) { 
 					resourceCarriedAmount += targetResource.mine();
 					miningCooldown = recoveryTime;
 				} else {
 					miningCooldown -= tpf; 
 				}
 				
 				if(resourceCarriedAmount >= P.INSTANCE.getWorkerCarryAmount()) {
 					setFinished(true);
 					System.out.println("mined");
 				}
 				
 			}else{
 				// Not in range
 				
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(unit, targetResource.getPosition());
 				}
 				
 				moveAbility.update(tpf);
 				if(moveAbility.isFinished()){
 					moveAbility.setActive(false);
 					moveAbility.setFinished(false);
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void useAbility(PlayerControlledEntity entity, Position target) {
 		this.unit = entity;
 		this.targetResource = (Resource) ModelUtils.INSTANCE.getNonPlayerControlledEntity(target);
 		resourceCarriedAmount = 0;
 		setActive(true);
 		setFinished(false);
 	}
 	
 	@Override
 	public AbstractAbility createAbility() {
 		MineResourceAbility newAbility = new MineResourceAbility();
 		newAbility.moveAbility = AbilityFactory.INSTANCE.createAbility(MoveAbility.class.getSimpleName());
 		return newAbility;
 	}
 
 }
