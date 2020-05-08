 package de.minestar.cok.hook;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingSpawnEvent;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.game.Team;
 import de.minestar.cok.helper.ChatSendHelper;
 
 public class PlayerListener {
 
 	@ForgeSubscribe
 	public void onPlayerDeath(PlayerDropsEvent event){
 		if(!CoKGame.gameRunning){
 			return;
 		}
 		String name = event.entityPlayer.username;
 		if(name == null){
 			return;
 		}
 		Team team = CoKGame.getTeamOfPlayer(name);
 		if(team == null){
 			return;
 		}
 		//Set a new captain
 		if(team.getCaptain().equals(name)){
 			team.setRandomCaptain();
 			ChatSendHelper.broadCastError("THE RULER OF THE KINGDOM " + team.getName() + " " + name + " HAS DIED!");
 			if(team.getCaptain().equals("")){
 				ChatSendHelper.broadCastError("THERE IS NO NEW RULER TO ANOUNCE... THIS IS BAD!");
 			} else{
 				ChatSendHelper.broadCastError("LONG LIFE KING " + team.getCaptain() + "!");
 			}
 		}
 		//change profession
 		CoKGame.playerProfessions.remove(name);
		CoKGame.playerProfessions.put(name, CoKGame.professions.get(CoKGame.rand.nextInt(CoKGame.professions.size()-1)));
 	}
 	
 	
 	//TODO this doesn't work as expected... need to fix it somehow (wrong event)
 	@ForgeSubscribe
 	public void onPlayerSpawn(LivingSpawnEvent event){
 		if(!CoKGame.gameRunning){
 			return;
 		}
 		if(event.entity instanceof EntityPlayer){
 			EntityPlayer player = (EntityPlayer) event.entity;
 			Team team = CoKGame.getTeamOfPlayer(player.username);
 			if(team != null){
 				CoKGame.playerProfessions.get(player.username).giveKit(player, team);
 			}
 		}
 	}
 	
 }
