 package uk.co.jacekk.bukkit.simpleirc;
 
 import java.util.Set;
 
 import org.bukkit.Server;
 import org.bukkit.command.RemoteConsoleCommandSender;
 import org.bukkit.permissions.PermissibleBase;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 
 import uk.co.jacekk.bukkit.baseplugin.v8.BaseObject;
 
 public class IRCCommandSender extends BaseObject<SimpleIRC> implements RemoteConsoleCommandSender {
 	
 	private PermissibleBase perm;
 	private SimpleIRCBot bot;
 	
 	private String name;
 	private String messageTarget;
 	
 	public IRCCommandSender(SimpleIRC plugin, SimpleIRCBot bot){
 		super(plugin);
 		
 		this.perm = new PermissibleBase(this);
 		this.bot = bot;
 		
		this.name = null;
 		this.messageTarget = null;
 	}
 	
 	/**
 	 * Sets the target that messages will be sent to.
 	 * 
 	 * @param target The target
 	 */
 	public void setMessageTarget(String target){
 		this.messageTarget = target;
 	}
 	
 	@Override
 	public String getName(){
		return (this.name == null) ? "SimpleIRC" : this.name;
 	}
 	
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	@Override
 	public Server getServer(){
 		return plugin.server;
 	}
 	
 	@Override
 	public void sendMessage(String message){
 		if (this.messageTarget == null){
			for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
				this.bot.sendNotice(channel, ChatColorHelper.convertMCtoIRC(message));
			}
		}else{
			this.bot.sendNotice(this.messageTarget, ChatColorHelper.convertMCtoIRC(message));
 		}
 	}
 	
 	@Override
 	public void sendMessage(String[] messages){
 		for (String message : messages){
 			this.sendMessage(message);
 		}
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin){
 		return this.perm.addAttachment(plugin);
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, int ticks){
 		return this.perm.addAttachment(plugin, ticks);
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value){
 		return this.perm.addAttachment(plugin, name, value);
 	}
 	
 	@Override
 	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks){
 		return this.perm.addAttachment(plugin, name, value, ticks);
 	}
 	
 	@Override
 	public Set<PermissionAttachmentInfo> getEffectivePermissions(){
 		return this.perm.getEffectivePermissions();
 	}
 	
 	@Override
 	public boolean hasPermission(String name){
 		return this.perm.hasPermission(name);
 	}
 	
 	@Override
 	public boolean hasPermission(Permission perm){
 		return this.perm.hasPermission(perm);
 	}
 	
 	@Override
 	public boolean isPermissionSet(String name){
 		return this.perm.isPermissionSet(name);
 	}
 	
 	@Override
 	public boolean isPermissionSet(Permission perm){
 		return this.perm.isPermissionSet(perm);
 	}
 	
 	@Override
 	public void recalculatePermissions(){
 		this.perm.recalculatePermissions();
 	}
 	
 	@Override
 	public void removeAttachment(PermissionAttachment attachment){
 		this.perm.removeAttachment(attachment);
 	}
 	
 	@Override
 	public boolean isOp(){
 		return true;
 	}
 	
 	@Override
 	public void setOp(boolean op){
 		throw new UnsupportedOperationException("Cannot change operator status of a remote command sender.");
 	}
 	
 }
