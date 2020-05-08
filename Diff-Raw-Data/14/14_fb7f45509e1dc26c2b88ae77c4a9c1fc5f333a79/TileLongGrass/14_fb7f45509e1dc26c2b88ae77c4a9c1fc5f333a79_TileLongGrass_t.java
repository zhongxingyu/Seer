 import java.util.Random;
 
 
 public class TileLongGrass extends Tile {

public static final int CHANCE_OF_FINDING_MONSTER = 2;

 	@Override
 	public void stepOn(EntityPlayer entityPlayer, int x, int y) {
 		System.out.println("YOU DARE STAND ON LONG GRASS!!!!" );
 		Random generator = new Random();

                if(generator.nextInt(CHANCE_OF_FINDING_MONSTER) == 0)
		MonsterData.monsters[generator.nextInt(MonsterData.monsters.length)].printMonster();
                else
                System.out.println("No monster");
 	}
 }
