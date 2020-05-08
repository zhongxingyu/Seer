 package fearlesscode;
 
public class Wall extends Entity {
 
	private int points;

	public Wall(PlayField playField){
		Logger.call(this,"Wall.init()");
 		super(playField);		       
		this.requiredKeys=requiredKeys;
		Logger.ret(this,"Wall.init()");

 	}


 	public void meetPlayer(Player player){
    	Logger.call(this,"Wall.meetPlayer(player)");
    	Logger.ret(this,"Wall.meetPlayer(player)");
 	}

 }
