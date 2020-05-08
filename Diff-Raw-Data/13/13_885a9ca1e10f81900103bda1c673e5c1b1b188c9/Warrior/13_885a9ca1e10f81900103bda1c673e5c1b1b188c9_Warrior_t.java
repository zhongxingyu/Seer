 package projectrts.model.entities.units;
 
 import projectrts.model.entities.AbstractUnit;
 import projectrts.model.entities.EntityFactory;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.abilities.AbilityFactory;
 import projectrts.model.entities.abilities.AttackAbility;
 import projectrts.model.entities.abilities.MoveAbility;
 import projectrts.model.player.Player;
 import projectrts.model.utils.Position;
 
 /**
  *  An example of a concrete unit and it's methods.
  * @author Markus Ekstrm
  *
  */
 public class Warrior extends AbstractUnit{
 	private static final float size = 1f;
 	private static final float speed = 4f;
 	private static final float sightRange = 5f;
 	private static final int damage = 50;
 	private static final int maxHealth = 100;
 	
 	
 	static {
 		EntityFactory.INSTANCE.registerPCE(Warrior.class.getSimpleName(), new Warrior());
 	}
 	
 	protected void initialize(Player owner, Position spawnPos) {
 		super.initialize(owner, spawnPos);
		MoveAbility moveAbility = (MoveAbility) AbilityFactory.INSTANCE.createAbility(MoveAbility.class.getSimpleName(),this);
 		this.abilities.add(moveAbility);
 		this.abilities.add(AbilityFactory.INSTANCE.createMAbility(AttackAbility.class.getSimpleName(), this, moveAbility));
 		setName(Warrior.class.getSimpleName());
 		setSightRange(sightRange);
 		this.setMaxHealth(maxHealth);
 		this.setCurrentHealth(this.getMaxHealth());
 		this.setSize(size);
 		this.setSpeed(speed);
 		this.setDamage(damage);
 	}
 
 	@Override
 	public PlayerControlledEntity createPCE(Player owner, Position pos) {
 		Warrior newWarrior = new Warrior();
 		newWarrior.initialize(owner, pos);
 		return newWarrior;
 	}
 
 }
