 package Goldsack.DiscoSheep;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /*import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;*/
 
 public class DiscoPermission {
 	private DiscoSheep plugin;
 	//Permission plugin settings
 	//protected PermissionHandler permit = null;
 	protected boolean usePermit = false;
 
 	//To be used in permission. Etc - 'discosheep.*'
 	//To be used in permission. Etc - 'discosheep.mob.*'
 	//To be used in permission. Etc - 'discosheep.party.*'
 	private final String DISCOSHEEP = "discosheep"; 
 	public final String MOB 		= "mob";
 	public final String PARTY 		= "party";
 	private final char DOT 			= '.';
 	//Commands
 	public final String DEBUG 		= "debug";
 	public final String HELP 		= "help";
 	public final String COLOR 		= "color";
 	public final String ONE			= "one";
 	public final String MANY 		= "many";
 	public final String SHEEP	 	= "sheep";
 	public final String CREEPER 	= "creeper";
 	public final String GHAST	 	= "ghast";
 	public final String STOP  		= "stop";
 	public final String RELOAD		= "reload";
 	
 	public DiscoPermission(DiscoSheep discoSheep) {
 		plugin = discoSheep;
 		enablePermissons();
 	}
 	/**
 	 * Checks if we have Permissions installed, if not we set usePermit to false and let ops.txt handle it.
 	 */
 	public void enablePermissons(){
 		/*Plugin pTemp = plugin.getServer().getPluginManager().getPlugin("Permissions");
 		
 		if(pTemp == null){
 			System.out.println("[DiscoSheep] Permissions plugin not detected. Will use ops.txt");
 			usePermit = false;
 			return;
 		}
 		else{
 			plugin.getServer().getPluginManager().enablePlugin(pTemp);
 			permit = ((Permissions)pTemp).getHandler();
 			usePermit = true;
 			System.out.println("[DiscoSheep] Permissions plugin detected!");
 		}*/
 	}
 	/**
 	 * User permissions or ops.txt to decide if user can call command
 	 * 
 	 * send message is set to false when you don't want to send a message to sender on failed permissions
 	 * @param sender
 	 * @param command
 	 * @return
 	 */
 	
 	public boolean isPermittet(CommandSender sender, String command, boolean sendMessage){
 		if(!(sender instanceof Player)){
 			//Console wants to run command, always allowed
 			return true;
 		}
 		if(usePermit){
 			Player p = (Player)sender;
 			
 			String[][] pList = {
 					{HELP, 		DISCOSHEEP+DOT+HELP},
 					{DEBUG, 	DISCOSHEEP+DOT+DEBUG},
 					{STOP, 		DISCOSHEEP+DOT+STOP},
 					{RELOAD, 	DISCOSHEEP+DOT+RELOAD},
 					{ONE, 		DISCOSHEEP+DOT+PARTY+DOT+ONE},
 					{MANY, 		DISCOSHEEP+DOT+PARTY+DOT+MANY},
 					{COLOR, 	DISCOSHEEP+DOT+COLOR},
 					{SHEEP, 	DISCOSHEEP+DOT+MOB+DOT+SHEEP},
 					{CREEPER, 	DISCOSHEEP+DOT+MOB+DOT+CREEPER},
 					{GHAST, 	DISCOSHEEP+DOT+MOB+DOT+GHAST},
 			};
 			
 			for (int i = 0; i < pList.length; i++) {
 				int result = pCheck(p, command, pList[i][0], pList[i][1]);
 				if(result == 1){return true;}
 				if(result == 0){
 					if(sendMessage){
 						sender.sendMessage("[DiscoSheep] You are not registered in permissions to use \"" + pList[i][1] + "\"");
 					}
 					return false;
 				}
 				//if result == -1 then this was not the command we were looking for.
 			}
 			
 			sender.sendMessage("[DiscoShep] Permissions do not know what to do with \"" + command + "\"." +
 					"\nPlease tell the developer (me) about this and tell his lazy ass he forgot to add it");
 			return false;
 		}
 		
 		//Else, if we do not use permission, use ops.txt
 		else{
 			if(sender.isOp()){
 				return true;
 			}
 			else{
 				if(sendMessage){
 					sender.sendMessage("You are not OP and can't use [DiscoSheep] plugin");
 				}
 				return false;
 			}
 			
 		}
 	}
 
 	private int pCheck(Player p, String command, String small, String full){
 		
 		//Example, small is color
 		//If command equals color
 		if(command.equalsIgnoreCase(small)){
 			//Then we ask if player can use full where full equals "dicosheep.color"
 			if(p.hasPermission(full)){
 				return 1;
 			}
 			else{
                 if(p.isOp())
                     return 1;
                 else
 				    return 0;
 			}
 		}
 		return -1; //Not matching case
 	}
 	
 
 
 }
