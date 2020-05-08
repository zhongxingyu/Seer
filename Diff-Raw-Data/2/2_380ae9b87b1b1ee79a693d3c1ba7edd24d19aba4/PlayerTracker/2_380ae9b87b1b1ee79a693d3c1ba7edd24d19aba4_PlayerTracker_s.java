 package de.minestar.cok.hook;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.util.ChunkCoordinates;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.game.Team;
 import de.minestar.cok.helper.ChatSendHelper;
 import de.minestar.cok.network.CoKGamePacketServer;
 import de.minestar.cok.network.PacketHandler;
 import de.minestar.cok.profession.Profession;
 
 public class PlayerTracker implements IPlayerTracker {
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
         if (side == Side.SERVER) {
         	CoKGamePacketServer.sendGameStateToPlayer((Player) player);
         	if(CoKGame.gameRunning && CoKGame.getTeamOfPlayer(player.username) == null){
         		CoKGame.setPlayerSpectator((EntityPlayerMP) player);
         		String[] usernames = {player.username};
         		CoKGamePacketServer.sendPacketToAllPlayers(PacketHandler.SPECTATOR_ADD, usernames);
         	}
         	Team team = CoKGame.getTeamOfPlayer(player.username);
         	if(team != null){
         		team.playerReturned(player.username);
         		ChunkCoordinates spawnCoordinates = team.getSpawnCoordinates();
         		if(spawnCoordinates != null){
     				player.setSpawnChunk(spawnCoordinates, true);
     			}
         		Profession profession = CoKGame.playerProfessions.get(player.username);
        		if(profession != null){
         			profession.giveKit(player, team);
         		}
         	}
         }
 
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
         if (side == Side.SERVER) {
         	if(!CoKGame.gameRunning){
         		return;
         	}
         	Team team = CoKGame.getTeamOfPlayer(player.username);
         	if(team != null){
         		String captain = team.getCaptain();
         		Profession profession = CoKGame.playerProfessions.get(player.username);
         		team.playerGone(player.username);
         		if(captain.equalsIgnoreCase(player.username)){
         			ChatSendHelper.broadCastError(captain + " ,the king of team " + team.getName() + " fled!");
         			ChatSendHelper.broadCastError("Long life king " + team.getCaptain() + "!");
         		}
         		if(profession != null){
         			for(Item item: profession.givenItems){
         				player.inventory.clearInventory(item.itemID, -1);
         			}
         		}
         	} else{
         		CoKGame.removeSpectator(player);
         		String[] usernames = {player.username};
         		CoKGamePacketServer.sendPacketToAllPlayers(PacketHandler.SPECTATOR_REMOVE, usernames);
         	}
         }
 
 	}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
         if (side == Side.SERVER) {
 			if(!CoKGame.gameRunning){
 				return;
 			}
 			
 			Team team = CoKGame.getTeamOfPlayer(player.username);
 			if(team != null){
 				team.playerReturned(player.username);
 			}
 			Profession profession = CoKGame.playerProfessions.get(player.username);
 			if(team != null && profession != null){
 				profession.giveKit(player, team);
 			} else{
         		CoKGame.setPlayerSpectator((EntityPlayerMP) player);
         		String[] usernames = {player.username};
         		CoKGamePacketServer.sendPacketToAllPlayers(PacketHandler.SPECTATOR_ADD, usernames);
         	}
         }
 	}
 
 }
