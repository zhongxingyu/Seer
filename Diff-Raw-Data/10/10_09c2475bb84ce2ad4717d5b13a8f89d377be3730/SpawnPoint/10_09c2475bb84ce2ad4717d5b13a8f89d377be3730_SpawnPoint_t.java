 package fearlesscode;
 
 public class SpawnPoint extends Entity
 {
 	public SpawnPoint(PlayField playField)
 	{
		//Logger.call(this,"SpawnPoint.init()");
 		super(playField);
		//Logger.ret(this,"SpawnPoint.init()");
 	}
 	public void meetPlayer(Player player)
 	{
    	Logger.call(this,"meetPlayer(player)");
    	Logger.ret(this,"meetPlayer(player)");
 	}
 }
