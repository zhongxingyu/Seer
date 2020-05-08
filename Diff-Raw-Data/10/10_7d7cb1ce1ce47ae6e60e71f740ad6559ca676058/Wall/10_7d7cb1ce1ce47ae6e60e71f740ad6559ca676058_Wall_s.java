 package fearlesscode;
 
 import fearlesscode.util.*;
 
 public class Wall extends Entity
 {
 	public Wall(PlayField playField)
 	{
 		//Logger.call(this,"Wall.init()");
 		super(playField);		       
 		//Logger.ret(this,"Wall.init()");
 	}
 	public void meetPlayer(Player player){
    	Logger.call(this,"meetPlayer(player)");
    	Logger.ret(this,"meetPlayer(player)");
 	}
 }
