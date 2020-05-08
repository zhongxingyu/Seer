 package items;
 
 import com.golden.gamedev.Game;
 
 import characterSprites.CollectibleItemSprite;
 /**
  * @author Kathleen Oshima
  */
 public class DefenseAttribute extends AttributeDecorator {
 
 	public DefenseAttribute(Game game) {
 	    super(game);
 	    // TODO Auto-generated constructor stub
     }
 
 	private double defensePower;
 	private CollectibleItemSprite collectableItemSprite;
 
 
 	public void setDefensePower(double defensePower) {
 		this.defensePower = defensePower;
 	}
 	
 	@Override
     public double attackPower() {
 	    return collectableItemSprite.attackPower();
     }
 
 	@Override
     public double defensePower() {
 		return defensePower + collectableItemSprite.defensePower();
 
     }
 
 }
