 package de.minestar.cok.network;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.network.packet.Packet250CustomPayload;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.game.Team;
 import de.minestar.cok.references.Reference;
 
 public class CoKGamePacketServer {
 
 	
 	public static void sendPacketToAllPlayers(byte code, String[] data){
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(0);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try {
 			outputStream.writeByte(code);
 			for(int i = 0; i < data.length; i++){
 				outputStream.writeUTF(data[i]);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//build packet
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = Reference.CHANNEL_NAME;
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		//send packet
 		PacketDispatcher.sendPacketToAllPlayers(packet);
 	}
 	
 	/**
 	 * secifically used to send gameRunning
 	 * @param code
 	 * @param data
 	 */
 	public static void sendPacketToAllPlayers(byte code, boolean b){
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(0);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try {
 			outputStream.writeByte(code);
 			outputStream.writeBoolean(b);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//build packet
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = Reference.CHANNEL_NAME;
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		//send packet
 		PacketDispatcher.sendPacketToAllPlayers(packet);
 	}
 	
 	/**
 	 * send the gamestate to one specific Player
 	 */
 	public static void sendGameStateToPlayer(Player player){
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(0);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try {
 			outputStream.writeByte(PacketHandler.GAME_STATE);
 			//write gameState
 			outputStream.writeBoolean(CoKGame.gameRunning);
 			//write teams
 			outputStream.writeInt(CoKGame.teams.size());
 			for(Team team: CoKGame.teams.values()){
 				outputStream.writeUTF(team.getName());
 				outputStream.writeChar(team.getColor());
 				int teamSize = team.getAllPlayers().size();
 				//write players of team
 				outputStream.writeInt(teamSize);
 				for(String playerName: team.getAllPlayers()){
 					outputStream.writeUTF(playerName);
 				}
				
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		
 		//build packet
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = Reference.CHANNEL_NAME;
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		//send packet
 		PacketDispatcher.sendPacketToPlayer(packet, player);
 	}
 }
