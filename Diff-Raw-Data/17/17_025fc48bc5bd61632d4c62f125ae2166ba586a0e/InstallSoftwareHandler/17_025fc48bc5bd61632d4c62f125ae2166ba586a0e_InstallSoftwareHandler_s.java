 package sfs2x.projectsasha.game.handlers;
 
 import sfs2x.projectsasha.game.utils.RoomHelper;
 import sfs2x.projectsasha.game.GameConsts;
 import sfs2x.projectsasha.game.entities.GameWorld;
 import sfs2x.projectsasha.game.entities.Player;
 import sfs2x.projectsasha.game.entities.gateways.Gateway;
 
 import com.smartfoxserver.v2.entities.User;
 import com.smartfoxserver.v2.entities.data.ISFSObject;
 import com.smartfoxserver.v2.entities.data.SFSObject;
 import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
 
 public class InstallSoftwareHandler extends BaseClientRequestHandler{
 	public void handleClientRequest(User sender, ISFSObject params){
 		
 		String sw = params.getUtfString("software");
 		String gw = params.getUtfString("gateway");
 		Player p = RoomHelper.getPlayer(this, sender.getName());
 		GameWorld world = RoomHelper.getWorld(this);
 		Gateway currentGw = world.gateways.get(gw);
 		if (currentGw.getOwner().getUserName() != p.getName())
 		{
 			sendError("NOTOWNER", sender);
 			return;
 		}
 		String swName = nameToConst(sw.substring(0,sw.length()-1));
 		int swVersion = Integer.parseInt(sw.substring(sw.length()-1));
 		if(!p.hasItems(swName, swVersion))
 		{
 			sendError("ITEMNOTOWNED", sender);
 			return;
 		}
 		if(currentGw.hasSoftware(swName) && !currentGw.getInstalledSoftware(swName).isCumulative())
 		{
 			sendError("NOTCUMULATIVE", sender);
 			return;
 		}
 		
		if(currentGw.hasSoftware(swName))
 		{
 			currentGw.upgradeSoftware(swName, p);
 			trace("has sw, i'm upgrading it");
 		}
 		else
 		{
			currentGw.installSoftware(swName, p);
 			trace("has sw, i'm installing it");
 		}
 		
 		ISFSObject reback = SFSObject.newInstance();
 		reback.putBool("success", true);
 		send("install", reback, sender);	
 	}
 	private String nameToConst(String name)
 	{
 		trace(name);
 		String ret = "";
 		if(name.equals("brute"))
 			ret = GameConsts.BRUTEFORCER;
 		if(name.equals("fire"))
 			ret = GameConsts.FIREWALL;
 		if(name.equals("ids"))
 			ret = GameConsts.IDS;
 		if(name.equals("log"))
 			ret = GameConsts.LOGCLEANER;
 		if(name.equals("antivirus"))
 			ret = GameConsts.ANTIVIRUS;
 		if(name.equals("virus"))
 			ret = GameConsts.VIRUS;
 		if(name.equals("deepthroat"))
 			ret = GameConsts.DEEPTHROAT;
 		if(name.equals("sniffer"))
 			ret = GameConsts.SNIFFER;
 		if(name.equals("dict"))
 			ret = GameConsts.DICTIONARY;
 		if(name.equals("proxy"))
 			ret = GameConsts.PROXY;
 		return ret;
 	}
 	private void sendError(String errorType, User sender)
 	{
 		ISFSObject reback = SFSObject.newInstance();
 		reback.putBool("success", false);
 		
 		if(errorType == "NOTOWNER")
 		{
 			reback.putUtfString("error", "NOTOWNER");
 		}
 		if(errorType == "ITEMNOTOWNED")
 		{
 			reback.putUtfString("error", "ITEMNOTOWNED");
 		}
 		if(errorType == "NOTCUMULATIVE")
 		{
 			reback.putUtfString("error", "NOTCUMULATIVE");
 		}
 		send("error", reback, sender);	
 	}
 }
