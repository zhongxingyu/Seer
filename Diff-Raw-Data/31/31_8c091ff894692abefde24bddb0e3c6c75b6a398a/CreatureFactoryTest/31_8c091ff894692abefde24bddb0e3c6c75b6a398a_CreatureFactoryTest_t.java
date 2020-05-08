 package se.chalmers.kangaroo.model.creature;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.creatures.BlackAndWhiteCreature;
 import se.chalmers.kangaroo.model.creatures.BullCreature;
 import se.chalmers.kangaroo.model.creatures.CrabCreature;
 import se.chalmers.kangaroo.model.creatures.Creature;
 import se.chalmers.kangaroo.model.creatures.CreatureFactory;
 import se.chalmers.kangaroo.model.creatures.SmurfCreature;
 import se.chalmers.kangaroo.model.creatures.SumoCreature;
 import se.chalmers.kangaroo.model.creatures.TurtleCreature;
 import se.chalmers.kangaroo.model.utils.Position;
 
 
 public class CreatureFactoryTest {
 
 	
 	@Test
 	public void testCreateCreature() {
 		Creature a = CreatureFactory.createCreature(111, new Position(1,1));
 		Creature b = CreatureFactory.createCreature(112, new Position(1,1));
 		Creature c = CreatureFactory.createCreature(113, new Position(1,1));
 		Creature d = CreatureFactory.createCreature(114, new Position(1,1));
 		Creature e = CreatureFactory.createCreature(115, new Position(1,1));
 		Creature f = CreatureFactory.createCreature(116, new Position(1,1));
 		assertTrue(a instanceof CrabCreature && b instanceof TurtleCreature && c instanceof BlackAndWhiteCreature 
 				&& d instanceof BullCreature && e instanceof SumoCreature && f instanceof SmurfCreature );
 	}
 	
 	@Test
 	public void creatureTest(){
 		for(int i = 0; i < 200; i++){
 			if( Constants.CREATURE_IDS.contains(" "+i+" ")){
 				assertTrue(CreatureFactory.createCreature(i, new Position(10,10)) != null);
 			}else{
 				assertTrue(CreatureFactory.createCreature(i, new Position(10,10)) == null);
 			}
 		}
 	}
 
 }
