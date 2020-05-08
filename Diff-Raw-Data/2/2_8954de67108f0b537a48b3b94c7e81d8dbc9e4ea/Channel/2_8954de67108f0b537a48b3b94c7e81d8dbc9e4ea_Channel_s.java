 package me.botsko.darmok.channels;
 
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 import me.botsko.darmok.Darmok;
 
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 
 public class Channel {
 
 	private final String name;
 	private final String command;
 	private final String color;
 	private final String format;
 	private final int range;
 	private final String context;
 	
 	private boolean isDefault = false;
 	private boolean isMuted = false;
 	
 	
 	/**
 	 * 
 	 * @param command
 	 */
 	public Channel( String name, String command, String color, String format, int range, String context ){
 		this.name = name;
 		this.command = command;
 		this.color = color;
 		this.format = format;
 		this.range = range;
 		this.context = context;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getName(){
 		return name;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCommand(){
 		return command;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getColor(){
 		return color;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getFormat(){
 		return format;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getRange(){
 		return range;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getContext(){
 		return context;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isDefault(){
 		return isDefault;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public void setDefault( boolean isDefault ){
 		this.isDefault = isDefault;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public void setMuted( boolean isMuted ){
 		this.isMuted = isMuted;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isMuted(){
 		return isMuted;
 	}
 	
 	
 	/**
 	 * 
 	 * @param msg
 	 * @return
 	 */
 	public String formatMessage( Player player, String msg ){
 		
 		String prefix = Darmok.getVaultChat().getPlayerPrefix(player);
 		String suffix = Darmok.getVaultChat().getPlayerSuffix(player);
 
 		Hashtable<String,String> headVal = new Hashtable<String,String>();
 		headVal.put("color", color );
 		headVal.put("prefix", prefix );
 		headVal.put("suffix", suffix );
 		headVal.put("command", command );
 		headVal.put("msg", msg );
 		headVal.put("player", player.getDisplayName() );
 		return getString( format, headVal );
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param key
 	 * @param replacer
 	 * @return
 	 */
 	protected String getString( String msg, Hashtable<String,String> replacer ){
 		if( msg != null && !replacer.isEmpty() ){
 			for (Entry<String,String> entry : replacer.entrySet()){
 			    msg = msg.replace("%("+entry.getKey()+")", entry.getValue());
 			}
 		}
 		return colorize( msg );
 	}
 	
 	
 	/**
 	 * Converts colors place-holders.
 	 * @param text
 	 * @return
 	 */
 	public String colorize(String text){
         return ChatColor.translateAlternateColorCodes('&', text);
     }
 	
 	
 	/**
 	 * 
 	 * @param text
 	 * @return
 	 */
 	public String stripColor( String text ){
		return ChatColor.stripColor( text.replaceAll("(&([a-f0-9A-K]))", "") );
 	}
 	
 	
 	/**
 	 * 
 	 */
 	public Channel clone() throws CloneNotSupportedException {
 		return (Channel) super.clone();
 	}
 }
