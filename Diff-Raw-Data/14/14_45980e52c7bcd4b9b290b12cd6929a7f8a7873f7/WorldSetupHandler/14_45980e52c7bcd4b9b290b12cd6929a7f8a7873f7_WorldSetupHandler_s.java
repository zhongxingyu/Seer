 package sfs2x.projectsasha.game.handlers;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import sfs2x.projectsasha.game.GameConsts;
 import sfs2x.projectsasha.game.utils.RoomHelper;
 import sfs2x.projectsasha.game.entities.GameWorld;
 import sfs2x.projectsasha.game.entities.Player;
 import sfs2x.projectsasha.game.entities.gateways.Gateway;
 import com.smartfoxserver.v2.entities.User;
 import com.smartfoxserver.v2.entities.data.ISFSObject;
 import com.smartfoxserver.v2.entities.data.SFSObject;
 import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
 
 public class WorldSetupHandler extends BaseClientRequestHandler
 {
 	public void handleClientRequest(User sender, ISFSObject params)
 	{
 		
 		GameWorld world = RoomHelper.getWorld(this);
 		
 		List<User> userList = RoomHelper.getCurrentRoom(this).getUserList();
 		int i = 0;
 		for(User u : userList)
 		{
 			
 			Player p = new Player(u);
 			RoomHelper.putPlayer(this, p);
 			Gateway startingSpot = world.gateways.get(GameConsts.STARTING_SPOTS[i]);
 			startingSpot.setOwner(p);	
 			
 			Set<String> set = world.gateways.keySet();
 			Iterator<String> itr = set.iterator();
 			while (itr.hasNext())
 			{
 				String key = itr.next();
 				Gateway currentGW = world.gateways.get(key);
 				currentGW.setCostsSoFar(p.getUserName(), 0);
 				currentGW.setParentForPath(p.getUserName(), null);
 			}
 			
 			/**FIXME: only for demo**/
			p.addMoney(100);
 			/*p.addInventory(SoftwareFactory.makeSoftware(GameConsts.BRUTEFORCER));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.BRUTEFORCER, 2));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.BRUTEFORCER, 3));
 			
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.PROXY));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.PROXY, 2));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.PROXY, 3));
 			
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.FIREWALL));
 			
 			startingSpot.installSoftware(GameConsts.BRUTEFORCER, p);
 			startingSpot.installSoftware(GameConsts.PROXY, p);
 			startingSpot.installSoftware(GameConsts.FIREWALL, p);
 			//trace("installing "+ GameConsts.BRUTEFORCER + " on "+startingSpot.getName());
 			
 			startingSpot.upgradeSoftware(GameConsts.BRUTEFORCER, p); //upgrade to lvl 2
 			startingSpot.upgradeSoftware(GameConsts.BRUTEFORCER, p); //upgrade to lvl 3
 			startingSpot.upgradeSoftware(GameConsts.PROXY, p);		 //upgrade to lvl 2
 			startingSpot.upgradeSoftware(GameConsts.PROXY, p);		 //upgrade to lvl 3
 			
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.PROXY));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.BRUTEFORCER));
 			p.addInventory(SoftwareFactory.makeSoftware(GameConsts.FIREWALL));*/
 			
 			/************************/
 			i++;
 		}
 		
 		
 		String str, JSONString;
 		Gateway currentGW = null;
 		
 		Set<String> set = world.gateways.keySet();
 		Iterator<String> itr = set.iterator();
 		
 		//JSON composition
 		JSONString  = "{";
 		while (itr.hasNext())
 		{
 			str = itr.next();
 			currentGW = world.gateways.get(str);
 			String GWOwner = currentGW.getOwner()!=null?currentGW.getOwner().getName():"Neutral";
 			JSONString += "\""+str+"\":{"+
 						"\"STATE\":\""+str+"\","+
 						"\"X\":"+currentGW.getX()+","+
 						"\"Y\":"+currentGW.getY()+","+
 						"\"REGION\":\""+RoomHelper.getGatewayRegion(this,currentGW).getRegionName()+"\","+
 						"\"NAME\":\""+currentGW.getName()+"\","+
 						"\"OWNER\":\""+GWOwner+"\","+
 						"\"ATK\":"+currentGW.getAttackLevel()+","+
 						"\"DEF\":"+currentGW.getDefenceLevel()+","+
 						"\"TYPE\":\""+currentGW.getClass().getSimpleName()+"\","+
 						"\"SW\": ["+
 						"\""+currentGW.getInstalledSoftware(0)+"\","+
 						"\""+currentGW.getInstalledSoftware(1)+"\","+
 						"\""+currentGW.getInstalledSoftware(2)+"\""+
 						"]"+
 						"},";
 			
 		}
 		JSONString = JSONString.substring(0, JSONString.length()-1);
 		JSONString+="}";
 				
 		SFSObject reback = SFSObject.newFromJsonData(JSONString);
 		trace("Sending world setup info");
 		send("getWorldSetup", reback, sender);
 	}
 }
