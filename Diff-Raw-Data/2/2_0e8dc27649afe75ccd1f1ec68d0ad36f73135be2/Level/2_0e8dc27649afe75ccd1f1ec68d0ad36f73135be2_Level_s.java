 /*******************************************************************************
  * Copyright (c) 2012 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.world;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import net.mcforge.iomodel.Player;
 import net.mcforge.server.Server;
 import net.mcforge.server.Tick;
 import net.mcforge.util.properties.Properties;
 import net.mcforge.world.generator.*;
 
 public class Level implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7297498370800056856L;
 
 	private Thread physics;
 
 	private boolean run;
 
 	transient ArrayList<Tick> ticks = new ArrayList<Tick>();
 
 	private boolean autosave;
 	
 	private int physicsspeed;
 	
 	private transient Properties levelprop;
 
 	public Block[] blocks;
 	
 	private boolean unloading;
 
 	/**
 	 * The width of the level (max X)
 	 */
 	public short width;
 
 	/**
 	 * The height of the level (max Y)
 	 */
 	public short height;
 
 	/**
 	 * The depth of the level (max Z)
 	 */
 	public short depth;
 
 	/**
 	 * The X position (in blocks) where the player spawns.
 	 */
 	public int spawnx;
 
 	/**
 	 * The Y position (in blocks) where the player spawns.
 	 */
 	public int spawny;
 
 	/**
 	 * The Z position (in blocks) where the player spawns.
 	 */
 	public int spawnz;
 
 	/**
 	 * The name of the level
 	 */
 	public String name;
 	
 	/**
 	 * The MoTD for this level
 	 */
	public String motd;
 
 	/**
 	 * The constructor for {@link Level}
 	 * The constructor wont generate a flat world, you need to
 	 * call {@link #FlatGrass()}
 	 * @param width
 	 *             The width (X) of the level
 	 * @param height
 	 *             The height (Y) of the level
 	 * @param depth
 	 *             The depth (Z) of the level
 	 */
 	public Level(short width, short height, short depth) {
 		this();
 		this.width = width;
 		this.height = height;
 		this.depth = depth;
 		this.spawnx = width / 2;
 		this.spawny = 33;
 		this.spawnz = depth / 2;
 		blocks = new Block[width*height*depth];
 	}
 
 	/**
 	 * The constructor for the level
 	 * This constructor starts the physics ticks
 	 * This constructor wont generate a level, nor will it set a default
 	 * width, height, and depth. To set a width, height, and depth and
 	 * initialize the blocks, use must use {@link #Level(short, short, short)}
 	 */
 	public Level() {
 		this.ticks = new ArrayList<Tick>();
 	}
 
 	/**
 	 * Generate a flat world
 	 */
 	public void FlatGrass(Server s) {
 		FlatGrass g = new FlatGrass(s);
 		generateWorld(g);
 	}
 
 	/**
 	 * Generate a world
 	 * @param g
 	 *         The {@link Generator} object that will
 	 *         generate the world.
 	 */
 	public void generateWorld(Generator g) {
 		if (blocks == null)
 			blocks = new Block[width*height*depth];
 		g.generate(this);
 	}
 
 	public void startPhysics(Server server) {
 		physics = new Ticker(server, this);
 		run = true;
 		physics.start();
 	}
 
 	/**
 	 * Weather or not this level will autosave.
 	 * AutoSave will save the level every minute and save
 	 * when the level is unloaded.
 	 * @return
 	 *        True if the level will autosave, false if it will not.
 	 */
 	public boolean isAutoSaveEnabled() {
 		return autosave;
 	}
 
 	/**
 	 * Set weather the level will autosave or not.
 	 * AutoSave will save the level every minute and save
 	 * when the level is unloaded.
 	 * @param set
 	 *           True if the level will autosave, false if it will not.
 	 */
 	public void setAutoSave(boolean set) {
 		autosave = set;
 	}
 
 	/**
 	 * Set a block in this world.
 	 * If the block is a physicsblock, it will be added
 	 * to the physics tick.
 	 * This method wont send out a change to the clients.
 	 * To do this, use {@link Player#GlobalBlockChange(short, short, short, Block, Level, Server)} instead
 	 * @param b
 	 *         The block to add
 	 * @param index
 	 *             Where to add the block
 	 * @param server
 	 *              The server this blockchange is happening in
 	 */
 	public void setTile(Block b, int index, Server server, boolean physics) {
 		if (index < 0) index = 0;
 		if (index >= blocks.length) index = blocks.length - 1;
 		Block wasthere = blocks[index];
 		int[] pos = IntToPos(index);
 		if (b instanceof PhysicsBlock && physics) {
 			PhysicsBlock pb = ((PhysicsBlock)b).clone(server);
 			pb.setLevel(this);
 			pb.setServer(server);
 			pb.setPos(pos[0], pos[1], pos[2]);
 			blocks[index] = pb;
 			if (this.ticks == null)
 				this.ticks = new ArrayList<Tick>();
 			this.ticks.add(pb);
 		}
 		else
 			blocks[index] = b;
 		if(wasthere != null){
 			wasthere.onDelete(this, pos[0], pos[1], pos[2], server);
 		}
 		b.onPlace(this, pos[0], pos[1], pos[2], server);
 		if (!physics)
 			return;
 		try {
 			if (getTile(pos[0] + 1, pos[1], pos[2]) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0] + 1, pos[1], pos[2])))
 				ticks.add((PhysicsBlock)getTile(pos[0] + 1, pos[1], pos[2]));
 
 			if (getTile(pos[0] - 1, pos[1], pos[2]) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0] - 1, pos[1], pos[2])))
 				ticks.add((PhysicsBlock)getTile(pos[0] - 1, pos[1], pos[2]));
 
 			if (getTile(pos[0], pos[1] + 1, pos[2]) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0], pos[1] + 1, pos[2])))
 				ticks.add((PhysicsBlock)getTile(pos[0], pos[1] + 1, pos[2]));
 
 			if (getTile(pos[0], pos[1] - 1, pos[2]) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0], pos[1] - 1, pos[2])))
 				ticks.add((PhysicsBlock)getTile(pos[0], pos[1] - 1, pos[2]));
 
 			if (getTile(pos[0], pos[1], pos[2] + 1) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0], pos[1], pos[2] + 1)))
 				ticks.add((PhysicsBlock)getTile(pos[0], pos[1], pos[2] + 1));
 
 			if (getTile(pos[0], pos[1], pos[2] - 1) instanceof PhysicsBlock && !ticks.contains(getTile(pos[0], pos[1], pos[2] - 1)))
 				ticks.add((PhysicsBlock)getTile(pos[0], pos[1], pos[2] - 1));
 		} catch (Exception e) { }
 	}
 
 	public void checkPhysics(Server server) {
 		Thread t = new StartPhysics(server, this);
 		t.start();
 	}
 	
 	public void loadProperties() {
 		levelprop = new Properties();
 		try {
 			levelprop.load("levels/properties/" + name + ".properties");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		this.physicsspeed = 100;
 		if (!levelprop.hasValue("Physics speed"))
 			levelprop.addSetting("Physics speed", physicsspeed);
 		else
 			this.physicsspeed = levelprop.getInt("Physics speed");
 		
 		this.motd = "ignore";
 		if (!levelprop.hasValue("MOTD"))
 			levelprop.addSetting("MOTD", "ignore");
 		else
 			this.motd = levelprop.getValue("MOTD");	
 		
 		try {
 			levelprop.save("levels/properties/" + name + ".properties");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 	}
 	
 	public Properties getLevelProperties() {
 		return levelprop;
 	}
 
 	/**
 	 * Get a block in this level
 	 * @param index
 	 *            Which block to get
 	 * @return
 	 *        The block at that index
 	 */
 	public Block getTile(int index) {
 		if (index < 0) index = 0;
 		if (index >= blocks.length) index = blocks.length - 1;
 		if (blocks[index] == null)
 			return Block.getBlock((byte)0);
 		if (blocks[index].name.equals("NULL"))
 			System.out.println("" + blocks[index].getVisableBlock());
 		return blocks[index];
 	}
 
 	/**
 	 * Get a block at the X, Y, Z coordinates
 	 * @param x
 	 *        The X coordinate
 	 * @param y
 	 *        The Y coordinate
 	 * @param z
 	 *        The Z coordinate
 	 * @return
 	 *        The block at those coordinates
 	 */
 	public Block getTile(int x, int y, int z) {
 		return getTile(posToInt(x, y, z));
 	}
 
 	/**
 	 * Get how big the block array is
 	 * @return
 	 *       The size of the block array
 	 */
 	public int getLength() {
 		return blocks.length;
 	}
 
 	/**
 	 * Set a block in this world.
 	 * If the block is a physicsblock, it will be added
 	 * to the physics tick.
 	 * This method wont send out a change to the clients.
 	 * To do this, use {@link Player#GlobalBlockChange(short, short, short, Block, Level, Server)} instead
 	 * @param b
 	 *         The block to add
 	 * @param x
 	 *        The X coordinate
 	 * @param y
 	 *        The Y coordinate
 	 * @param z
 	 *        The Z coordinate
 	 * @param server
 	 *              The server this blockchange is happening in
 	 */
 	public void setTile(Block b, int x, int y, int z, Server server) {
 		setTile(b, posToInt(x, y, z), server, true);
 	}
 
 	/**
 	 * Convert coordinates to a number that will
 	 * correspond to where the coordinates are in the
 	 * block array
 	 * @param x
 	 *        The X coordinate
 	 * @param y
 	 *        The Y coordinate
 	 * @param z
 	 *        The Z coordinate
 	 * @return
 	 *        The number that will correspond to where the coordinates
 	 *        are in the block array
 	 */
 	public int posToInt(int x, int y, int z) {
 		if (x < 0) { return -1; }
 		if (x >= width) { return -1; }
 		if (y < 0) { return -1; }
 		if (y >= height) { return -1; }
 		if (z < 0) { return -1; }
 		if (z >= depth) { return -1; }
 		return x + z * width + y * width * depth;
 	}
 
 	private int[] IntToPos(int index) {
 		int[] toreturn = new int[3];
 		toreturn[1] = (index / width / height);
 		index -= toreturn[1]*width*height;
 		toreturn[2] = (index/width);
 		index -= toreturn[2]*width;
 		toreturn[0] = index;
 		return toreturn;
 	}
 
 	/**
 	 * Save the level
 	 * @throws IOException
 	 *                   An IOExceptoin is thrown if there is a problem writing to the file
 	 */
 	public void save() throws IOException {
 		if (!new File("levels").exists())
 			new File("levels").mkdir();
 		FileOutputStream fos = new FileOutputStream("levels/" + name + ".ggs");
 		GZIPOutputStream gos = new GZIPOutputStream(fos);
 		ObjectOutputStream out = new ObjectOutputStream(gos);
 		out.writeLong(serialVersionUID);
 		out.writeObject(this);
 		out.close();
 		gos.close();
 		fos.close();
 	}
 
 	/**
 	 * Unload this level.
 	 * All players who are in this level will be sent to the {@link Server#MainLevel}
 	 * @param server
 	 *             The server thats unloading the level
 	 * @throws IOException
 	 *                   An IOException will occur if there is a problem saving the level
 	 */
 	public void unload(Server server) throws IOException {
 		unload(server, autosave);
 	}
 	/**
 	 * Unload this level.
 	 * All players who are in this level will be sent to the {@link Server#MainLevel}
 	 * @param server
 	 *             The server thats unloading the level
 	 * @param save
 	 *           Weather the level should save before unloading
 	 * @throws IOException
 	 *                   An IOException will occur if there is a problem saving the level
 	 */
 	public void unload(Server server, boolean save) throws IOException {
 		if (save)
 			save();
 		run = false;
 		unloading = true;
 		server.Log("[" + name + "] Stopping physics..");
 		try {
 			physics.interrupt();
 			physics.join(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		server.Log("Unloading " + name);
 		for (Player p : server.players) {
 			if (p.getLevel() == this)
 				p.changeLevel(server.getLevelHandler().findLevel(server.MainLevel), false);
 		}
 		blocks = null;
 	}
 
 	/**
 	 * Load a level and return the level
 	 * @param filename
 	 *               The file to load and read
 	 * @return
 	 *        The level object
 	 * @throws IOException
 	 *                   An IOException is thrown if there is a problem reading the level
 	 * @throws ClassNotFoundException
 	 *                              This exception is thrown if a block that was saved with the level is not loaded or cant be found.
 	 */
 	public static Level Load(String filename, Server server) throws IOException, ClassNotFoundException {
 		Level l = null;
 		if (filename.endsWith(".lvl"))
 			l = convertLVL(filename);
 		else {
 			FileInputStream fis = new FileInputStream(filename);
 			GZIPInputStream gis = new GZIPInputStream(fis);
 			ObjectInputStream obj = new ObjectInputStream(gis);
 			long version = obj.readLong();
 			if (version == serialVersionUID){
 				l = (Level)obj.readObject();
 			}else{
 				obj.close();
 				throw new IOException("The level version does not match the current");
 			}
 			l.ticks = new ArrayList<Tick>();
 			l.physics = l.new Ticker(server, l);
 			l.name = new File(filename).getName().split("\\.")[0];
 			l.run = true;
 			l.loadProperties();
 			l.unloading = false;
 			l.checkPhysics(server);
 			l.physics.start();
 			obj.close();
 			gis.close();
 			fis.close();
 		}
 		return l;
 	}
 
 	/**
 	 * Convert a .lvl file to a .ggs file
 	 * @param file
 	 *           The file to load and convert
 	 * @return
 	 *        The converted level object
 	 * @throws IOException
 	 *                   An IOException is thrown if there is a problem reading the file
 	 */
 	public static Level convertLVL(String file) throws IOException {
 		String name = new File(file).getName().split("\\.")[0];
 		File f = new File(file);
 		FileInputStream in = new FileInputStream(f);
 		GZIPInputStream decompressor = new GZIPInputStream(in);
 		DataInputStream data = new DataInputStream(decompressor);
 		int magic = convert(data.readShort());
 
 		if (magic != 1874) {
 			System.out.println("INVALID .lvl FILE!");
 			in.close();
 			decompressor.close();
 			data.close();
 			return null;
 		}
 		//data.read(new byte[16]);
 		short width = convert(data.readShort());
 		short height = convert(data.readShort());
 		short depth = convert(data.readShort());
 		Level level = new Level(width, height, depth);
 		level.spawnx = convert(data.readShort());
 		level.spawnz = convert(data.readShort());
 		level.spawny = convert(data.readShort());
 		//Ignore these bytes
 		data.readUnsignedByte();
 		data.readUnsignedByte();
 
 		level.blocks = new Block[width * depth * height];
 		for (int i = 0; i < level.blocks.length; i++) {
 			level.blocks[i] = translateBlock(data.readByte());
 		}
 		data.close();
 		try {
 			f.delete();
 		} catch(SecurityException e) {
 			e.printStackTrace();
 		}
 		level.name = name;
 		level.save();
 		return level;
 	}
 
 	private static short convert(int convert) {
 		return (short) (((convert >> 8) & 0xff) + ((convert << 8) & 0xff00));
 	}
 
 	private static Block translateBlock(byte id) {
 		if (id == 8)
 			return Block.getBlock((byte)9);
 		if (id == 10)
 			return Block.getBlock((byte)11);
 		if (id >= 0 && id <= 49)
 			return Block.getBlock(id);
 		//TODO Convert more blocks
 		if (id == 106)
 			return Block.getBlock((byte)9);
 		if (id == 105)
 			return Block.getBlock("Air");
 		if (id == 111)
 			return Block.getBlock("Log");
 
 		return Block.getBlock("Air");
 	}
 
 	private class Ticker extends Thread implements Serializable {
 		private static final long serialVersionUID = 1609185967611447514L;
 		private transient Server server;
 		private transient Level level;
 		public Ticker(Server server, Level level) { this.level = level; this.server = server; }
 
 		@Override
 		public void run() {
 			Thread.currentThread().getId();
 			ArrayList<Tick> toremove = new ArrayList<Tick>();
 			while (run) {
 				if (ticks == null)
 					ticks = new ArrayList<Tick>();
 				for (Tick t : toremove) {
 					ticks.remove(t);
 				}
 				toremove.clear();
 				@SuppressWarnings("unchecked")
 				ArrayList<Tick> temp = (ArrayList<Tick>)ticks.clone();
 				for (int i = 0; i < temp.size(); i++) {
 					if (unloading)
 						break;
 					if (temp.get(i) instanceof PhysicsBlock) {
 						PhysicsBlock pb = (PhysicsBlock)temp.get(i);
 						if (pb.getLevel() == null)
 							pb.setLevel(level);
 						if (pb.getServer() == null)
 							pb.setServer(server);
 						if (pb.runInSeperateThread()) {
 							Thread t = new Ticker2(pb);
 							t.start();
 							continue;
 						}
 					}
 					Tick t = temp.get(i);
 					if (t != null && !unloading)
 						t.tick();
 				}
 				temp.clear();
 				try {
 					Thread.sleep(physicsspeed);
 				} catch (InterruptedException e) { }
 			}
 			server.Log("[" + name + "] Physics stopped.");
 		}
 	}
 
 	private class Ticker2 extends Thread implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		PhysicsBlock pb;
 		public Ticker2(PhysicsBlock pb) { this.pb = pb; }
 		@Override
 		public void run() {
 			if (unloading)
 				return;
 			pb.tick();
 		}
 	}
 
 	private class StartPhysics extends Thread implements Serializable {
 		private static final long serialVersionUID = 1L;
 		transient Server server;
 		transient Level l;
 		public StartPhysics(Server server, Level l) { this.l = l; this.server = server; }
 		@Override
 		public void run() {
 			for (int i = 0; i < blocks.length; i++) {
 				if (blocks[i] instanceof PhysicsBlock) {
 					PhysicsBlock b = (PhysicsBlock)blocks[i];
 					PhysicsBlock pb = ((PhysicsBlock)b).clone(server);
 					pb.setLevel(l);
 					pb.setServer(server);
 					pb.setPos(l.IntToPos(i));
 					blocks[i] = pb;
 					if (l.ticks == null)
 						l.ticks = new ArrayList<Tick>();
 					l.ticks.add(pb);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Changes the block at the specified coordinates to the specified block
 	 * without checking for any physics changes
 	 * If you change a block in a level, it won't be sent to clients
 	 * 
 	 * @param x - The x coordinate
 	 * @param y - The y coordinate
 	 * @param z - The z coordinate
 	 * @param block - The block to change to
 	 * @param server - The server
 	 */
 	public void skipChange(int x, int y, int z, Block block, Server server) {
 		if (x < 0 || y < 0 || z < 0) return;
 		if (x >= width || y >= depth || z >= height) return;
 
 		this.setTile(block, posToInt(x, y, z), server, false);
 	}
 }
