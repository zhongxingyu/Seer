 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 
 
 public class SkillsListener extends PluginListener {
 	static final Logger log = Logger.getLogger("Minecraft");
 	
 	public SkillProperties Props;
 	public PropertiesFile propertiesFile;
 	public PropertiesFile playersFile;
 	public Hashtable<Player, SkillsPlayer> playersList = new Hashtable<Player, SkillsPlayer>();
 	public Timer timer;
 	
 
 	
 	
 	public SkillsListener() {
 		
 		this.Props = new SkillProperties();
 		
 	    this.timer = new Timer();
 	    this.timer.scheduleAtFixedRate(new SkillsTimer(this), 0L, Props.savetimer);
 	}
 	
 	public void onLogin(Player player) {
 		if(this.playersList.containsKey(player)){
 			return;
 		}
 	    try {
 	    	this.playersFile = new PropertiesFile("Skills.txt");
 			this.playersFile.load();
 			if(this.playersFile.containsKey(player.getName())){
 				String skills = this.playersFile.getString(player.getName());
 				this.playersList.put(player, new SkillsPlayer(this, player, skills));
 			}
 			else
 			{
 				this.playersList.put(player, new SkillsPlayer(this, player));
 			} 
 		} catch (IOException ioe) {}
 	}
 	
 	public boolean onBlockBreak(Player player, Block block) {
 		SkillsPlayer sp = this.playersList.get(player);
 		
 		
 		int skill = Props.GetDestroySkill(block);
 		if(skill < 0)
 			return false;
 		
		int tool = Props.GetToolLevel(player.getItemInHand());
 		int durability = Props.GetDurability(block);
 
 		player.sendMessage("Skill -" + Integer.toString(sp.getLevel(skill)) + "Tool -" + Integer.toString(tool) + "Dura +" + Integer.toString(durability));
 		
 		if(durability - sp.getLevel(skill) - tool > Props.tobroke){
 			player.sendMessage("This block is too hard for you!");
 		}
 		else
 		{
 			if(sp.getLevel(skill) + tool >= durability){
 				sp.giveExp(skill, durability);
 				return false;
 			}
 			else
 			{
 				durability -= sp.getLevel(skill);
 				durability -= tool;
 				sp.giveExp(skill, sp.getLevel(skill));
 				etc.getServer().setBlockData(block.getX(), block.getY(), block.getZ(), durability);
 				return true;
 			}
 		}
 		return true;
 	}
 	
 
 	public boolean onBlockPlace(Player player, Block block, Block blockClicked, Item itemInHand) {
 		SkillsPlayer sp = this.playersList.get(player);
 		
 		int skill = Props.GetCreateSkill(block);
 		if(skill < 0)
 			return false;
 		
 		etc.getServer().setBlockData(block.getX(), block.getY(), block.getZ(), sp.getLevel(skill));
 		sp.giveExp(skill, 1);
 		return false;
     }
 
     
     public boolean onCommand(Player player, String[] split)
     {
         if ((split[0].equalsIgnoreCase("/exp")) && (player.canUseCommand("/exp")))
         {
         	SkillsPlayer sp = this.playersList.get(player);
             player.sendMessage(player.getName()+" are:");
             for(int i = 0; i < Props.Skills.length; i++){
             	int level = sp.getLevel(i);
             	player.sendMessage(Props.GetRangFromLevel(level)+" "+Props.Skills[i]+"; level - "+level+"; exp - "+sp.getExp(i)+";");
             }
             return true;
         }
         else if ((split[0].equalsIgnoreCase("/giveexp")) && (player.canUseCommand("/giveexp")))
         {
         	try
         	{
         		SkillsPlayer sp = this.playersList.get(etc.getServer().getPlayer(split[1]));
         		int skill = -1;
         		int amount = Integer.parseInt(split[3]); 
 
         		for(int i = 0; i < Props.Skills.length; i++){
         			if(Props.Skills[i].equalsIgnoreCase(split[2])){
         				skill = i;
         				break;
         			}
         		}
         		if(amount != 0 && skill >= 0 && sp != null){
         			sp.giveExp(skill, amount);
         			player.sendMessage("Done!");
         		}
         		else
         		{
         			player.sendMessage("Syntax: <playername> <skillname> <amount>");
         		}
         	}
         	catch (Exception e)
         	{
         		player.sendMessage("Syntax: <playername> <skillname> <amount>");
         	}
         	return true;
         } 
     return false;
     }
 }
