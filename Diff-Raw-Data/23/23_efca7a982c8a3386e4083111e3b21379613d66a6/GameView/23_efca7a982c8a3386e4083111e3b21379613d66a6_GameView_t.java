 package ru.terra.game.client.game;
 
 import ru.terra.game.client.entity.Entity;
 import ru.terra.game.client.entity.MapObject;
 import ru.terra.game.client.entity.Player;
 import ru.terra.game.client.gui.jmonkey.JMonkeyGameView;
 
 public abstract class GameView
 {
 	public abstract void init();
 
 	public abstract void loadPlayer();
 
 	public abstract void addMapObject(MapObject entity);
 
 	public static GameView getView()
 	{
		return JMonkeyGameView.getInstance();
 	}
 
 	public abstract void enemyLoggedIn(Player enemy);
 
 	public abstract void updateEntityPosition(Entity entity);
 
 	public abstract void entityVectorMove(Entity entity, float x, float y, float z, float h);
 }
