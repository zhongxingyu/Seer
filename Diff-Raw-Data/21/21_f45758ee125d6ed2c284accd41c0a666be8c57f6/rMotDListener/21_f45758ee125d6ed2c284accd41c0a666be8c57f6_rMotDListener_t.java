 
 public class rMotDListener extends PluginListener {
 	/**
 	 * 
 	 */
 	private final rMotD rMotD;
 
 	/**
 	 * @param rMotD
 	 */
 	rMotDListener(rMotD rMotD) {
 		this.rMotD = rMotD;
 	}
 
 	public void onLogin(Player triggerMessage){
 		this.rMotD.triggerMessagesWithOption(triggerMessage, "onlogin");
 		return;
 	}
 	
 	public void onDisconnect(Player triggerMessage){
 		this.rMotD.triggerMessagesWithOption(triggerMessage, "ondisconnect");
 		return;
 	}
 	
 	public boolean onHealthChange(Player triggerMessage, int oldValue, int newValue){
 		if (newValue <= 0) {
 			this.rMotD.triggerMessagesWithOption(triggerMessage, "ondeath");
 		}
 		return false;
 	}
 	
 	public void onBan(Player mod, Player triggerMessage, java.lang.String reason) {
 		String [] replaceThese = {"<<ban-reason>>", "<<ban-setter>>", "<<ban-recipient>>"     };
 		String [] withThese =    {reason          , mod.getName()   , triggerMessage.getName()};
 		this.rMotD.triggerMessagesWithOption(triggerMessage, "onban", replaceThese, withThese);
 	}
 	
 	public boolean onCommand(Player player, String[] split){
 		if (!player.canUseCommand(split[0]))
             return false;
 		
 		this.rMotD.triggerMessagesWithOption(player, "oncommand:" + split[0]);
		this.rMotD.triggerMessagesWithOption(player, "oncommand|" + split[0]);
         
         if (split[0].equalsIgnoreCase("/grouptell")){
         	Group iShouldExist;
         	if ((iShouldExist = etc.getDataSource().getGroup(split[1])) != null) {
 	        	String tag =  "<" + player.getColor() + player.getName() + Colors.White + " to " + iShouldExist.Prefix.charAt(0) + iShouldExist.Name + Colors.White + "> ";
 	        	String message = tag + etc.combineSplit(2, split, " ");
 	        	String [] functionParam = {split[1], player.getName()};
 	        	this.rMotD.sendToGroups(functionParam, message,player);
         	} else {
         		player.sendMessage(Colors.Red + "Invalid group name!");
         	}
         	return true;
         }    
         else if (split[0].equalsIgnoreCase("/rmotd")) {
 			this.rMotD.triggerMessagesWithOption(player, "onrmotd");
 			return true;
 		}
 		
 		return false; 
 	}
 	
 	public boolean onConsoleCommand(String[] split) {
 		if (split[0].equalsIgnoreCase("grouptell")) {
 			Group iShouldExist;
         	if ((iShouldExist = etc.getDataSource().getGroup(split[1])) != null) {
 	        	String tag =  "<dServer " + Colors.White + "to " + iShouldExist.Prefix.charAt(0) + iShouldExist.Name + Colors.White + "> ";
 	        	String message = tag + etc.combineSplit(2, split, " ");
 	        	this.rMotD.sendToGroup(split[1], message);
 	        	this.rMotD.log.info("[rMotd to " + iShouldExist.Name + "] " + etc.combineSplit(2, split, " "));
         	} else {
         		this.rMotD.log.info("[rMotD] Invalid group name!");
         	}
         	return true;
 		}
 		return false;
 	}
 }
