 package ru.terra.game.client.gui.jmonkey;
 
 import ru.terra.game.client.entity.Entity;
 import ru.terra.game.client.entity.MapObject;
 import ru.terra.game.client.entity.Player;
 import ru.terra.game.client.game.GameView;
 
 public class JMonkeyGameView extends GameView
 {
 	private JMEGameViewImpl jmeGameViewImpl;
 
	private static JMonkeyGameView instance = new JMonkeyGameView();

	private JMonkeyGameView()
 	{
 		jmeGameViewImpl = new JMEGameViewImpl();
 		jmeGameViewImpl.start();
 	}
 
	public static GameView getInstance()
	{
		return instance;
	}

	@Override
	public void init()
	{
	}

 	@Override
 	public void loadPlayer()
 	{
 		jmeGameViewImpl.loadPlayer();
 	}
 
 	@Override
 	public void addMapObject(MapObject entity)
 	{
 		jmeGameViewImpl.addMapObject(entity);
 	}
 
 	@Override
 	public void enemyLoggedIn(Player enemy)
 	{
 		jmeGameViewImpl.enemyLoggedIn(enemy);
 	}
 
 	@Override
 	public void updateEntityPosition(Entity entity)
 	{
 		jmeGameViewImpl.updateEntityPosition(entity);
 	}
 
 	@Override
 	public void entityVectorMove(Entity entity, float x, float y, float z, float h)
 	{
 		jmeGameViewImpl.entityVectorMove(entity.getGuid(), x, y, z, h);
 	}
 }
