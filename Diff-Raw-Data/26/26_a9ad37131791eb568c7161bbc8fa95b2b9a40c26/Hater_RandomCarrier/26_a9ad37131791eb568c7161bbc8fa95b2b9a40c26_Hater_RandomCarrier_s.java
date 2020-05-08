 
public final class Hater_Carrier extends Enemy {
 
	public Hater_Carrier(EnemyPlacementGrid Grid) {
 		super(Grid);
		type = "com.game.enemies.Hater_Carrier";
 		maxWeapons = 0;
 		maxPassives = 2;
 		imageFileName = "src/sprites/enemy_08.png";
 		setImageObject();
 	}
 
 }
