 package me.tehbeard.BeardAch.achievement;
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import com.google.gson.annotations.Expose;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 
 /**
  * Represents an achievement.
  * @author James
  *
  */
 public class Achievement {
     
     public static enum Display{
         BROADCAST,
         PERSON,
         NONE
     }
 
     @Expose
 	private String slug;
     @Expose
 	private String name;
     @Expose
 	private String descrip;
 	private transient int id = 0;
 	private static int nextId = 1;
 	@Expose
 	private HashSet<ITrigger> triggers = new HashSet<ITrigger>();
 	@Expose
 	private HashSet<IReward> rewards = new HashSet<IReward>();
 	@Expose
 	Display broadcast;
 	@Expose
 	private boolean hidden;
 	
 	private static final Permission exemptAll = new Permission("ach.exempt.*",PermissionDefault.FALSE);
 	private Permission exempt;
 	
 	public boolean isHidden(){
 	    return hidden;
 	}
 	
 	public Achievement(){
 	    nextId ++;
 	}
 	
 	public void postLoad(){
 	    this.exempt = new Permission("ach.exempt." + slug,PermissionDefault.FALSE);
 	}
 	
 	@Deprecated
 	public Achievement(String slug,String name,String descrip,Display broadcast,boolean hidden) {
 		this.slug = slug;
 		this.name = name;
 		this.descrip = descrip;
 		this.broadcast = broadcast;
 		this.id = nextId;
 		this.hidden = hidden;
 		this.exempt = null;//new Permission("ach.exempt." + slug,PermissionDefault.FALSE);
 		//Bukkit.getPluginManager().removePermission(this.exempt);
 		//Bukkit.getPluginManager().addPermission(this.exempt);
 		
 
 	}
 
 	public static void resetId(){
 		nextId = 1;
 	}
 	public int getId() {
 		return id;
 	}
 
 	public String getSlug() {
 		return slug;
 	}
 
 	public String getName(){
 		return name;
 	}
 
 	public String getDescrip(){
 		return descrip;
 	}
 
 	public void addTrigger(ITrigger trigger){
 		if(trigger==null){return;}
 		triggers.add(trigger);
 	}
 
 	public void addReward(IReward reward){
 		if(reward==null){return;}
 		rewards.add(reward);
 	}
 
 	public boolean checkAchievement(Player player){
 		if(player==null){
 			return false;
 		}
 
 		if(BeardAch.self.getAchievementManager().playerHas(player.getName(), slug)){
 		    return false;
 		}
 		
 		for(ITrigger trigger:triggers){
 			if(!trigger.checkAchievement(player)){
 				return false;
 			}
 		}
 		
 		unlock(player);
 		
 
 		return true;
 	}
 
 	public void unlock(Player player){
 	    if(!player.hasPermission(exemptAll) && !player.hasPermission(exempt)){
             for(IReward reward:rewards){
                 reward.giveReward(player);
             }
         }
 	    else
 	    {
 	        BeardAch.self.getLogger().info(player.getName() + " Exempt from achievement rewards.");
 	    }
 
         
         switch (broadcast){
         case BROADCAST:{
             Bukkit.broadcastMessage(BeardAch.colorise(
                     BeardAch.self.getConfig().getString("ach.msg.broadcast", 
                             (ChatColor.AQUA + "<PLAYER> " + ChatColor.WHITE + "Unlocked: " + ChatColor.GOLD + "<ACH>")).replace("<ACH>", name ).replace("<PLAYER>",player.getName()
                                     )
                     )
                     );
 
         }break;
           
         case PERSON:{
             player.sendMessage(BeardAch.colorise(BeardAch.self.getConfig().getString("ach.msg.person", "Achievement Get! " + ChatColor.GOLD + "<ACH>").replace("<ACH>", name).replace("<PLAYER>",player.getName())));
             player.sendMessage(ChatColor.BLUE + descrip);
         }break;
             
         }
         
         BeardAch.self.getAchievementManager().makeAchievementLink(player.getName(),slug);
         BeardAch.self.getAchievementManager().removeCheck(this, player.getName());
 	}
 	
 	
 	public HashSet<ITrigger> getTrigs(){
 		return triggers;
 	}
 
 	public HashSet<IReward> getRewards(){
         return rewards;
     }
 
 
 }
