 package projectrts.model.entities.abilities;
 
 import projectrts.model.entities.AbstractAbility;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.ITargetAbility;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.utils.ModelUtils;
 import projectrts.model.utils.Position;
 
 /**
  * An ability for attacking
  * @author Filip Brynfors
  *
  */
 public class AttackAbility extends AbstractAbility implements IMovableAbility, ITargetAbility {
 	private PlayerControlledEntity entity;
 	private PlayerControlledEntity target;
 	
 	private MoveAbility moveAbility;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(AttackAbility.class.getSimpleName(), new AttackAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		this.entity = entity;
 		this.moveAbility = moveAbility;
 		this.setCooldown(0.5f);
 	}
 	
 	@Override
 	public String getName() {
 		return "Attack";
 	}
 	
 	@Override
 	public void useAbility(Position pos){
 		target = EntityManager.getInstance().getPCEAtPosition(pos);
 		setActive(true);
 		setFinished(false);
 		
 	}
 
 	@Override
 	public void update(float tpf) {
 		updateCooldown(tpf);		
 		
 		
 		if(isActive() && !isFinished()){
 			//attacker.getRange();
			// TODO Plankton: !!!Fix this...
			if(Position.getDistance(entity.getPosition(), target.getPosition())>1.5){
 				//Out of range
 				
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(target.getPosition());
 				}
 				else
 				{
 					moveAbility.updateTarget(target.getPosition());
 				}
 				
 				if(moveAbility.isFinished()){
 					moveAbility.setActive(false);
 					moveAbility.setFinished(true);
 				}
 				
 				
 			} else {
 				//In range
 				if(getRemainingCooldown()<=0){
 					
 					target.dealDamageTo(entity.getDamage());
 					this.setAbilityUsed();
 			
 					if(target.getCurrentHealth() == 0) {
 						this.setFinished(true);
 					}
 					
 				}
 			}
 		}
 	}
 
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		AttackAbility newAbility = new AttackAbility();
 		newAbility.initialize(entity, moveAbility);
 		return newAbility;
 	}
 
 }
