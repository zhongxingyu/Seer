 package minebot.net;
 
 import java.io.*;
 import java.net.*;
 
 import minebot.*;
 import minebot.world.*;
 
 
 public class Session {
 	
 	public Player player;
 	public boolean connected;
 	
 	public World world;
 	
 	public int EID;
 	
 	public String username;
 	public Socket socket;
 	public DataInputStream reader;
 	public PacketWriter writer;
 	
 	private String password;
 	private String sessionID;
 	
 	public Session(String username, String password) {
 		this.username = username;
 		this.password = password;
 		connected = false;
 	}
 	
 	// Login to minecraft.net
 	public void login() throws IOException {
 		String loginURL = String.format("https://login.minecraft.net/?user=%s&password=%s&version=9999", username, password);
 		
 		URL url = new URL(loginURL);
 		URLConnection conn = url.openConnection();
 		
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String line = rd.readLine();
 		rd.close();
 		System.out.println(line);
 		if (line.equals("Bad login")) {
 			System.exit(0);
 		}
 		sessionID = line.split(":")[3];
 	}
 	
 	// Connect to a server
 	public void connect(String host, int port) throws IOException {
 		socket = new Socket(host, port);
 		reader = new DataInputStream(socket.getInputStream());
 		writer = new PacketWriter(socket.getOutputStream());
 		
 		writer.writeHandshake(username);
 		readPacket();
 		
 		writer.writeLoginRequest(username);
 		readPacket();
 	}
 	
 	// Start listening to packets and running the Player instance
 	public void begin() throws IOException, InterruptedException {
 		if (!connected) {
 			System.out.println("You need to connect to a server first.");
 			System.exit(0);
 		}
 		
 		while(socket.isConnected()) {
 			while (reader.available() > 0) {
 				readPacket();
 			}
 			player.logic();
 			Thread.sleep(100);
 		}
 	}
 	
 	public String readString8() throws IOException {
 		return reader.readUTF();
 	}
 	
 	public String readString16() throws IOException {
 		return new String(readBytes(reader.readShort()*2), "UTF-16BE");
 	}
 	
 	public byte[] readBytes(int length) throws IOException {
 		byte[] bytes = new byte[length];
		int recv = 0;
		do {
 			recv = reader.read(bytes, recv, length-recv);
		} while (recv < length);
 		return bytes;
 	}
 	
 	public void readMetadata() throws IOException {
 		byte x = reader.readByte();
 		while (x != 127) {
 			switch (x >> 5) {
 			case 0: 
 				reader.readByte();
 				break;
 			case 1:
 			case 2:
 			case 3:
 			case 4:
 			case 5:
 			case 6:
 				System.out.println("Op 0x18 non case 0.");
 				// System.exit(0);
 			}
 			x = reader.readByte();
 		}
 	}
 	
 	private int lastOpcode = -1;
 	
 	public void readPacket() {
 		int opcode = -1;
 		try {
 			opcode = reader.readUnsignedByte();
 			switch (opcode) {
 			case PacketID.KeepAlive:
 				writer.writeKeepAlive();
 				break;
 				
 			case PacketID.LoginRequest: {
 				EID = reader.readInt();
 				String serverName = readString16();
 				reader.skipBytes(9); // not required
 				System.out.println("Login Response: EID=" + EID);
 				world = new World();
 				connected = true;
 				break;
 			}
 			case PacketID.Handshake: {
 				String hash = readString16();
 				String authURL = String.format("http://www.minecraft.net/game/joinserver.jsp?user=%s&sessionId=%s&serverId=%s", username, sessionID, hash);
 				URL url = new URL(authURL);
 				URLConnection conn = url.openConnection();
 				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 				String line = rd.readLine();
 				System.out.println("Attempting to join server:" + line + " | " + authURL);
 				rd.close();
 				if (!line.equals("OK")) {
 					throw new IOException("Failed to authenticate with server.");
 				}
 				break;
 			}
 			case PacketID.ChatMessage: {
 				String msg = readString16();
 				player.handleChat(msg);
 				break;
 			}
 			case PacketID.TimeUpdate: // TODO
 				reader.readLong();
 				break;
 				
 			case PacketID.EntityEqupment: // TODO
 				reader.skipBytes(10);
 				break;
 				
 			case PacketID.SpawnPosition:
 				player.spawnX = reader.readInt();
 				player.spawnY = reader.readInt();
 				player.spawnZ = reader.readInt();
 				break;
 				
 			case PacketID.UseEntity: // TODO
 				reader.skipBytes(9);
 				break;
 				
 			case PacketID.HealthUpdate: {
 				int hp = reader.readShort();
 				if (hp <= 0) {
 					writer.writeRespawn(PacketWriter.DIMENSION_WORLD);
 				}
 				break;
 			}
 			case PacketID.Respawn: // TODO?
 				reader.readByte();
 				player.respawn();
 				break;
 				
 			case PacketID.PositionAndLook:
 				player.x = reader.readDouble();
 				player.stance = reader.readDouble();
 				player.y = reader.readDouble();
 				player.z = reader.readDouble();
 				player.yaw = reader.readFloat();
 				player.pitch = reader.readFloat();
 				player.onGround = reader.readBoolean();
 				player.spawned = true;
 				break;
 				
 			case PacketID.UseBed: // TODO
 				reader.skipBytes(14);
 				break;
 				
 			case PacketID.Animation: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.EntityAction: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.SpawnNamedEntity: {
 				EID = reader.readInt();
 				String name = readString16();
 				int x = reader.readInt();
 				int y = reader.readInt();
 				int z = reader.readInt();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				int item = reader.readShort();
 				NamedEntity ent = new NamedEntity(EID, x,y,z, yaw,pitch, name, item);
 				world.entities.add(ent);
 				break;
 			}
 			case PacketID.SpawnPickup: {
 				EID = reader.readInt();
 				int itemID = reader.readShort();
 				int count = reader.readByte();
 				int data = reader.readShort();
 				int x = reader.readInt();
 				int y = reader.readInt();
 				int z = reader.readInt();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				int roll = reader.readByte();
 				Item item = new Item(itemID, count, data);
 				ItemEntity ent = new ItemEntity(EID, x,y,z, yaw,pitch,roll, item);
 				world.entities.add(ent);
 				break;
 			}
 			case PacketID.CollectItem: // TODO
 				reader.skipBytes(8);
 				break;
 				
 			case PacketID.SpawnObject: {
 				int EID = reader.readInt();
 				int type = reader.readByte();
 				int x = reader.readInt();
 				int y = reader.readInt();
 				int z = reader.readInt();
 				int flag = reader.readInt(); // TODO
 				if (flag > 0) {
 					reader.skipBytes(6);
 				}
 				ObjectEntity ent = new ObjectEntity(EID, x,y,z, 0,0, type);
 				world.entities.add(ent);
 				break;
 			}
 			case PacketID.SpawnMob: {
 				int EID = reader.readInt();
 				int type = reader.readByte();
 				int x = reader.readInt();
 				int y = reader.readInt();
 				int z = reader.readInt();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				readMetadata(); // TODO
 				byte[] meta = new byte[0];
 				MobEntity ent = new MobEntity(EID, x,y,z, yaw,pitch, type, meta);
 				world.entities.add(ent);
 				break;
 			}
 			case PacketID.Painting: { // TODO
 				int EID = reader.readInt();
 				readString16();
 				reader.skipBytes(16);
 				break;
 			}
 			case PacketID.StanceUpdate: // this packet is largely unused/unknown
 				reader.skipBytes(18);	// but we'll read it just in case
 				break;
 				
 			case PacketID.EntityVelocity: {
 				int EID = reader.readInt();
 				int vx = reader.readShort();
 				int vy = reader.readShort();
 				int vz = reader.readShort();
 				Entity ent = world.entities.get(EID);
 				if (ent != null) {
 					ent.velocity(vx, vy, vz);
 				}
 				break;
 			}
 			case PacketID.DestroyEntity: {
 				int EID = reader.readInt();
 				world.entities.remove(EID);
 				break;
 			}
 			case PacketID.Entity:
 				reader.readInt();
 				break;
 				
 			case PacketID.EntityRelMove: {
 				int EID = reader.readInt();
 				int dx = reader.readByte();
 				int dy = reader.readByte();
 				int dz = reader.readByte();
 				Entity ent = world.entities.get(EID);
 				if (ent != null) {
 					ent.move(dx, dy, dz);
 				}
 				break;
 			}
 			case PacketID.EntityLook: {
 				int EID = reader.readInt();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				Entity ent = world.entities.get(EID);
 				if (ent != null) {
 					ent.look(yaw, pitch);
 				}
 				break;
 			}
 			case PacketID.EntityRelMoveLook: {
 				int EID = reader.readInt();
 				int dx = reader.readByte();
 				int dy = reader.readByte();
 				int dz = reader.readByte();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				Entity ent = world.entities.get(EID);
 				if (ent != null) {
 					ent.move(dx, dy, dz);
 					ent.look(yaw, pitch);
 				}
 				break;
 			}
 			case PacketID.EntityTeleport: {
 				int EID = reader.readInt();
 				int x = reader.readInt();
 				int y = reader.readInt();
 				int z = reader.readInt();
 				int yaw = reader.readByte();
 				int pitch = reader.readByte();
 				Entity ent = world.entities.get(EID);
 				if (ent != null) {
 					ent.teleport(x, y, z);
 					ent.look(yaw, pitch);
 				}
 				break;
 			}
 			case PacketID.EntityStatus: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.AttachEntity: // TODO
 				reader.skipBytes(8);
 				break;
 				
 			case PacketID.EntityMetadata: { // TODO
 				int EID = reader.readInt();
 				readMetadata();
 				break;
 			}
 			case PacketID.ChunkAction: {
 				int cx = reader.readInt();
 				int cz = reader.readInt();
 				boolean create = reader.readBoolean();
 				if (create) {
 					world.createEmptyChunk(cx, cz);
 				} else {
 					world.deleteChunk(cx, cz);
 				}
 				break;
 			}
 			case PacketID.ChunkLoad: {
 				int x = reader.readInt();
 				int y = reader.readShort();
 				int z = reader.readInt();
 				int sx = reader.readByte();
 				int sy = reader.readByte();
 				int sz = reader.readByte();
 				int size = reader.readInt();
 				byte[] data = readBytes(size);
 				world.readChunkData(x, y, z, sx+1, sy+1, sz+1, data);
 				break;
 			}
 			case PacketID.MultiBlockChange: {
 				int cx = reader.readInt();
 				int cz = reader.readInt();
 				int len = reader.readShort();
 				byte[] coords = readBytes(len*2);
 				byte[] types = readBytes(len);
 				byte[] metadata = readBytes(len);
 				world.multiBlockChange(cx, cz, len, coords, types, metadata);
 				break;
 			}
 			case PacketID.BlockChange: {
 				int x = reader.readInt();
 				int y = reader.readByte();
 				int z = reader.readInt();
 				int type = reader.readByte();
 				int metadata = reader.readByte();
 				world.setBlock(x, y, z, type);
 				break;
 			}
 			case PacketID.BlockAction: // TODO
 				reader.skipBytes(12);
 				break;
 				
 			case PacketID.Explosion: { // TODO
 				reader.skipBytes(28);
 				int count = reader.readInt();
 				reader.skipBytes(3*count);
 				break;
 			}
 			case PacketID.SoundEffect: // TODO
 				reader.skipBytes(17);
 				break;
 				
 			case PacketID.NewState: // TODO
 				reader.readByte();
 				break;
 				
 			case PacketID.Thunderbolt: // TODO
 				reader.skipBytes(17);
 				break;
 				
 			case PacketID.OpenWindow: // TODO
 				reader.skipBytes(2);
 				readString8();
 				reader.readByte();
 				break;
 				
 			case PacketID.CloseWindow: // TODO
 				reader.readByte();
 				break;
 				
 			case PacketID.SetSlot: {
 				reader.readByte();
 				int slot = reader.readShort();
 				int itemID = reader.readShort();
 				if (itemID != -1 && slot != -1) {
 					int count = reader.readByte();
 					int uses = reader.readShort();
 					player.inventory.addItem(slot, itemID, count, uses);
 				} else if (slot != -1) {
 					player.inventory.deleteItem(slot);
 				} else if (itemID != -1) {
 					reader.readByte();
 					reader.readShort();
 				}
 				break;
 			}
 			case PacketID.WindowItem: {
 				reader.readByte();
 				int len = reader.readShort();
 				int count, uses;
 				for (int i = 0; i < len; i++) {
 					int type = reader.readShort();
 					if (type != -1) {
 						count = reader.readByte();
 						uses = reader.readShort();
 						player.inventory.addItem(i, type, count, uses);
 					}						
 				}
 				player.inventory.print();
 				break;
 			}
 			case PacketID.UpdateProgressBar: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.Transaction: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.UpdateSign: // TODO
 				reader.skipBytes(10);
 				System.out.println(readString16());
 				readString16();
 				readString16();
 				readString16();
 				break;
 				
 			case PacketID.MapData: { // TODO
 				reader.skipBytes(4);
 				int len = reader.readUnsignedByte();
 				reader.skipBytes(len);
 				break;
 			}
 			case PacketID.IncrementStatistic: // TODO
 				reader.skipBytes(5);
 				break;
 				
 			case PacketID.Kick: {
 				String reason = readString16();
 				System.out.println("Kicked: " + reason);
 				System.exit(0);
 				break;
 			}	
 			default:
 				System.out.println("Unknown opcode: "+ Integer.toHexString(opcode) + "|"+opcode);
 				System.out.println("Last opcode:"+Integer.toHexString(lastOpcode));
 				System.out.println("Quitting.");
 				System.exit(0);
 				break;
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			System.out.println("Recorded opcode: " + opcode);
 		}
 		lastOpcode = opcode;
 	}
 }
