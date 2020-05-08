 package ch.zhaw.arsphema.model.enemies;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import ch.zhaw.arsphema.util.EnemyTextures;
 import ch.zhaw.arsphema.util.Sizes;
 
 
 public class EnemyFactory
 {
 	private boolean dropEnemies = false;
 	private static EnemyFactory instance;
     private EnemyFactory(){/*Singleton*/}
     
     public static EnemyFactory getInstance()
     {
     	if(instance == null)
     		instance = new EnemyFactory();
     	return instance;
     }
     
 	public List<AbstractEnemy> dropEnemy(float delta, float elapsed) {
		
 		if(dropEnemies)
 		{
 			dropEnemies = false;
 			return createUfoGroup();
 		}
 		return Collections.emptyList();
 	}
 	
 	private List<AbstractEnemy> createUfoGroup()
 	{
 		List<AbstractEnemy> ufos = new ArrayList<AbstractEnemy>();
 		ufos.add(createUfo(0));
 		ufos.add(createUfo(2));
 		ufos.add(createUfo(4));
 		ufos.add(createUfo(6));
 		return ufos;
 	}
     
     private AbstractEnemy createUfo(float x)
     {
     	//TODO y
     	Ufo ufo = new Ufo(Sizes.DEFAULT_WORLD_WIDTH + x, Sizes.DEFAULT_WORLD_HEIGHT / 2, 
     			Sizes.UFO_WIDTH, Sizes.UFO_HEIGHT, EnemyTextures.UFO);
     	return ufo;
     }
 
     
 	public void setDropEnemies(boolean dropEnemies) {
 		this.dropEnemies = dropEnemies;
 	}
 
 }
