 import java.util.Random;
 
 
 public class TileLongGrass extends Tile {
	
 
	
 	@Override
 	public void stepOn(EntityPlayer entityPlayer, int x, int y) {
 		System.out.println("YOU DARE STAND ON LONG GRASS!!!!" );
 		Random generator = new Random();
		 int identify = generator.nextInt(3);
		MonsterData.monsters[identify].printMonster();
 	}
 }
