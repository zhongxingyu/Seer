 package items;
 
 import com.golden.gamedev.Game;
 
 import characterSprites.CollectibleItemSprite;
 /**
  * @author Kathleen Oshima
  */
 public class AttackAttribute extends AttributeDecorator {
 
 	public AttackAttribute(Game game) {
 	    super(game);
 	    // TODO Auto-generated constructor stub
     }
 
 	private double attackPower;
 	private CollectibleItemSprite collectableItemSprite;
 	
 	public void setAttackPower(double attackPower) {
 		this.attackPower = attackPower;
 	}
 	
 	@Override
     public double attackPower() {
 		return attackPower + collectableItemSprite.attackPower();
     }
 
 	@Override
     public double defensePower() {
 	    return collectableItemSprite.defensePower();
     }
 
    @Override
    public void specialSkill ()
    {
        // TODO Auto-generated method stub
        
    }

 }
