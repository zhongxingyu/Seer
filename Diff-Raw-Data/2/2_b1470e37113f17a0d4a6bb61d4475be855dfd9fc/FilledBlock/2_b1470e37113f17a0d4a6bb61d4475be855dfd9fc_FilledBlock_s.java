 package fearlesscode;
 
 import fearlesscode.util.*;
 
import java.util.logging.Logger;

 public class FilledBlock extends Block
 {
 	public FilledBlock()
 	{
 		super();
 		Logger.call("FilledBlock", "init()");
 		Logger.ret("FilledBlock", "init()");
 	}
 	public void checkBorders()
 	{
 		Logger.call(this,"checkBorders()");
 		if(Logger.ask("Blokk széléhez ért?"))
 		{
 			if(Logger.ask("Lehetséges az átjutás?"))
 			{
 			player.player.enterBlock(neighbour);
 			neighbour.setPlayer(player.player,pos);
 			}
 			else if(Logger.ask("Kiesik?"))
 			{
 				//????????vissza kéne hogy hivjon a PlayField-re de nincs rá ref??????
 			}
 		}
 		else if(Logger.ask("Kilépett egy blokkból?"))
 		{
 		player.player.leaveBlock(this);
 		}
 		Logger.ret(this,"checkBorders()");
 	}
 	public void processCollisions()
 	{
 		Logger.call(this,"processCollisions()");
 		if(Logger.ask("Volt ütközés?"))
 		{
 			e.entity.meetPlayer(player.player);
 		}
 		Logger.ret(this,"processCollisions()");
 	}
 }
