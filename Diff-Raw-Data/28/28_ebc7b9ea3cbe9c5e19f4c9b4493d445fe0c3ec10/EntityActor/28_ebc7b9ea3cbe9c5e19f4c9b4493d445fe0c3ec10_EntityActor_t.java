 package com.insofar.actor;
 
 import java.util.ArrayList;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.ItemInWorldManager;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.Packet;
 import net.minecraft.server.Packet18ArmAnimation;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet33RelEntityMoveLook;
 import net.minecraft.server.Packet34EntityTeleport;
 import net.minecraft.server.Packet35EntityHeadRotation;
 import net.minecraft.server.Packet3Chat;
 import net.minecraft.server.Packet53BlockChange;
 import net.minecraft.server.Packet5EntityEquipment;
 import net.minecraft.server.ServerConfigurationManager;
 import net.minecraft.server.World;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 public class EntityActor extends EntityPlayer {
 
 	private Boolean isPlayback = false;
 	private int playbackTick = 0;
 	private Recording recording;
 	private MinecraftServer mcServer;
 	private Boolean loop = false;
 	private Boolean allPlayersView = false;
 	private Player owner = null;
 
 	private int translateX = 0, translateY = 0, translateZ = 0;
 	private long translateTime;
 
 	ServerConfigurationManager scm = ((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager;
 
 	/**
 	 * Constructor
 	 * @param minecraftserver
 	 * @param world
 	 * @param s
 	 * @param iteminworldmanager
 	 */
 	public EntityActor(MinecraftServer minecraftserver, World world, String s,
 			ItemInWorldManager iteminworldmanager) {
 		super(minecraftserver, world, s, iteminworldmanager);
 	}
 
 	public void tick()
 	{
 		if (isPlayback && recording != null)
 		{
 			doPlayback();
 		}
 	}
 
 	private void doPlayback()
 	{
 		// System.out.print(new StringBuilder("Playback entity: ").append(entityId));
 		if (recording.eof())
 		{
 			if (loop)
 			{
 				recording.rewind();
 			}
 			else
 			{
 				//System.out.print(new StringBuilder(" EOF"));
 				isPlayback = false;
 				return;
 			}
 		}
 
 		ArrayList<Packet>packets = recording.getNextPlaybackPackets();
 
 		if (packets != null && packets.size() > 0)
 		{
 			//System.out.println(new StringBuilder(" #packets=").append(packets.size()).append(": ").toString());
 			sendPackets(packets);
 		}
 	}
 
 	/**
 	 * Spawn the EntityActor in the world.
 	 */
 	public void spawn()
 	{
 		// Send spawn packet to the viewer
 		Packet20NamedEntitySpawn np = new Packet20NamedEntitySpawn(this);
 		np.a = id;
 		sendPacket(np);
 
 		// Send jumpstart packets to viewer
 		sendPackets(recording.getJumpstartPackets());
 	}
 
 	/**
 	 * Spawn the EntityActor in the world for a particular viewer.
 	 */
 	public void spawn(Player player)
 	{
 		// Send spawn packet to the viewer
 		Packet20NamedEntitySpawn np = new Packet20NamedEntitySpawn(this);
 		np.a = id;
 		sendPacket(np, player);
 
 		// Send jumpstart packets to viewer
 		sendPackets(recording.getJumpstartPackets(), player);
 	}
 
 
 	/**
 	 * Rewind this actor - sending all rewind packets to viewers and a teleport to the jumpstart
 	 */
 	public void rewind()
 	{
 		// Send rewind packets
 		for (Packet p : recording.rewindPackets)
 		{
 			if (p instanceof Packet53BlockChange)
 			{
 				// Set the block in the server's world so it is in sync with the client
 				world.setRawTypeIdAndData(
 						((Packet53BlockChange) p).a,
 						((Packet53BlockChange) p).b,
 						((Packet53BlockChange) p).c,
 						((Packet53BlockChange) p).material,
 						((Packet53BlockChange) p).data);
 
 				sendPacket(p);
 			}
 		}
 
 		// Rewind the recording and send the jumpstart packets
 		recording.rewind();
 		sendPackets(recording.getJumpstartPackets());
 	}
 
 	/**
 	 * Send the given packets to all viewers
 	 * @param packets
 	 */
 	public void sendPackets(ArrayList<Packet> packets)
 	{
 		sendPackets(packets, null);
 	}
 	
 	/**
 	 * Send packets to one or all players. If player == null send to all players.
 	 * @param packets
 	 * @param player
 	 */
 	public void sendPackets(ArrayList<Packet> packets, Player player)
 	{
 		for (int i = 0; i < packets.size(); i++)
 		{
 			// For cloning packets
 			Packet newp = null;
 			Packet p = packets.get(i);
 
 			// System.out.print("Packet: "+p.a());
 			//System.out.println(new StringBuilder("   ").append(p.b()).toString());
 
 			if (p instanceof Packet33RelEntityMoveLook)
 			{
 				// System.out.println(new StringBuilder("   Packet33"));
 				// Set the entity for this actor on this packet
 				((Packet33RelEntityMoveLook)p).a = id;
 			}
 			else if (p instanceof Packet34EntityTeleport)
 			{
 				newp = new Packet34EntityTeleport(
 						id,
 						((Packet34EntityTeleport) p).b+translateX,
 						((Packet34EntityTeleport) p).c+translateY,
 						((Packet34EntityTeleport) p).d+translateZ,
 						((Packet34EntityTeleport) p).e,
 						((Packet34EntityTeleport) p).f);
 
 				setPosition(
 						(((Packet34EntityTeleport)newp).b / 32),
 						(((Packet34EntityTeleport)newp).c / 32),
 						(((Packet34EntityTeleport)newp).d / 32));
 
 				//System.out.println(" Packet34: (" + tp.b + "," + tp.c + "," + tp.d + ")" + " yaw: "+ tp.e + " pitch: " + tp.f);
 				//System.out.println("    yaw: "+((Packet34EntityTeleport)newp).e);
 			}
 			else if (p instanceof Packet35EntityHeadRotation)
 			{
 				newp = new Packet35EntityHeadRotation(id, ((Packet35EntityHeadRotation)p).b);
 			}
 			else if (p instanceof Packet5EntityEquipment)
 			{
 				((Packet5EntityEquipment)p).a = id;
 			}
 			else if (p instanceof Packet18ArmAnimation)
 			{
 				((Packet18ArmAnimation)p).a = id;
 			}
 			else if (p instanceof Packet3Chat)
 			{
 				if (((Packet3Chat)p).message.indexOf(ChatColor.WHITE+"<") != 0)
 				{
 					((Packet3Chat)p).message = ChatColor.WHITE+"<" +
 					ChatColor.RED + name + ChatColor.WHITE +
 					"> "+((Packet3Chat)p).message;
 				}
 			}
 			else if (p instanceof Packet53BlockChange)
 			{
 				// Set the block in the server's world so it is in sync with the client
 				world.setRawTypeIdAndData(((Packet53BlockChange) p).a,
 						((Packet53BlockChange) p).b,
 						((Packet53BlockChange) p).c,
 						((Packet53BlockChange) p).material,
 						((Packet53BlockChange) p).data);
 			}
 
 			if (newp != null)
 			{
 				sendPacket(newp,player);
 			}
 			else
 			{
 				sendPacket(p,player);
 			}
 		}
 	}
 	
 	/**
 	 * Send a packet to all players 
 	 * @param p
 	 */
 	public void sendPacket(Packet p)
 	{
 		sendPacket(p,null);
 	}
 
 	/**
	 * Send a packet to one (if player) or all (if player == null) players 
 	 * @param p
 	 */
 	public void sendPacket(Packet p, Player player)
 	{
 		for (int i = 0; i < scm.players.size(); ++i) {
 			EntityPlayer p2 = (EntityPlayer) scm.players.get(i);
 			if (player==null || p2.name.equals(player.getName())) 
 			{
 				if (isViewableFrom(new Vector(p2.locX,p2.locY,p2.locZ)) ||
 						p.getClass() == Packet20NamedEntitySpawn.class)
 				{
 					p2.netServerHandler.sendPacket(p);
 					if (player != null)
 						return;
 				}
 			}
 		}	
 
 		return;
 	}
 
 	/**
 	 * True if vector v is within the configured viewableRadius
 	 * @param v
 	 * @return
 	 */
 	public boolean isViewableFrom(Vector v)
 	{
 		Vector v1 = new Vector(this.locX, this.locY, this.locZ);
 
 		double distance = v.distance(v1);
 
 		if (distance > ActorPlugin.getInstance().getRootConfig().viewableRadius)
 		{
 			return false;
 		}
 
 		return true;
 	}
 
 	public MinecraftServer getServer()
 	{
 		return mcServer;
 	}
 
 	public String getActorName()
 	{
 		return name;
 	}
 
 	public void setActorName(String name)
 	{
 		this.name = name;
 	}
 
 	public Boolean getIsPlayback()
 	{
 		return isPlayback;
 	}
 
 	public void setIsPlayback(Boolean isPlayback)
 	{
 		this.isPlayback = isPlayback;
 	}
 
 	public int getPlaybackTick()
 	{
 		return playbackTick;
 	}
 
 	public void setPlaybackTick(int playbackTick)
 	{
 		this.playbackTick = playbackTick;
 	}
 
 	public Recording getRecording()
 	{
 		return recording;
 	}
 
 	public void setRecording(Recording recording)
 	{
 		this.recording = recording;
 	}
 
 	public Boolean getLoop()
 	{
 		return loop;
 	}
 
 	public void setLoop(Boolean loop)
 	{
 		this.loop = loop;
 	}
 
 	public Boolean getAllPlayersView()
 	{
 		return allPlayersView;
 	}
 
 	public void setAllPlayersView(Boolean allPlayersView)
 	{
 		this.allPlayersView = allPlayersView;
 	}
 
 	public int getTranslateX()
 	{
 		return translateX;
 	}
 
 	public void setTranslateX(int translateX)
 	{
 		this.translateX = translateX;
 	}
 
 	public int getTranslateY()
 	{
 		return translateY;
 	}
 
 	public void setTranslateY(int translateY)
 	{
 		this.translateY = translateY;
 	}
 
 	public int getTranslateZ()
 	{
 		return translateZ;
 	}
 
 	public void setTranslateZ(int translateZ)
 	{
 		this.translateZ = translateZ;
 	}
 
 	public long getTranslateTime()
 	{
 		return translateTime;
 	}
 
 	public void setTranslateTime(long translateTime)
 	{
 		this.translateTime = translateTime;
 	}
 
 	public Player getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Player owner) {
 		this.owner = owner;
 	}
 }
