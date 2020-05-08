 package enemy;
 
 import gameCharacter.Attackable;
 import gameCharacter.GameCharacter;
 
 import java.util.HashMap;
 
 import state.State;
 
 import com.golden.gamedev.Game;
 
 import ai.GreedyPathFindingAI;
 import ai.SimpleAttackAI;
 import app.RPGame;
 import attacks.ShootingAttack;
 
 /**
  * GameCharacter decorated with attacks and actions.
  * Can use pluggable AI algorithms to behave automatically.
  * @author jameshong
  *
  */
 public class TestEnemy extends AbstractEnemy implements Attackable{
 
 	public TestEnemy(RPGame game, GameCharacter character, String configURL) {
 		super(character);
 		this.configURL = configURL;
 		this.game = game;
 		initResources();
 		moneyValue = 10;
 	}
 
 	@Override
 	protected void initAttacks() {
 		attacks.put("shooting",new ShootingAttack(game,this,"shooting"));
 	}
 
 	@Override
 	protected void initActions(String json) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public static class TestEnemyFactory extends EnemyFactory{
 	
 		@Override
 		public boolean isThisType(String enemyName) {
 			return "TestEnemy".equals(enemyName);
 		}
 
 		@Override
 		public AbstractEnemy constructEnemy(RPGame game, GameCharacter gameChar, String configURL) {
 			return new TestEnemy(game, gameChar, configURL);
 		}
 		
 	}
 
 	@Override
 	public void uponDeath() {
		getCharacter().getGame().getPlayer().getCharacter().getInventory().get("money").add(moneyValue);
 	}
 
 }
