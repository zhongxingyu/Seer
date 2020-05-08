 /*
     SignLift Bukkit plugin for Minecraft
     Copyright (C) 2011 Shannon Wynter (http://fremnet.net/)
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.net.fremnet.bukkit.SignLift;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 public class LiftSign {
 	static private SignLift plugin;
 	
 	public enum Direction { UP, DOWN, NONE };
 	
 	private Block block;
 	private Sign sign;
 	private String label;
 	private Direction direction;
 	private String owner = "";
 	private Boolean isPrivate = false;
 	
 	static public void init (SignLift parent) {
 		plugin = parent;
 	}
 
 	private File liftDir() {
 		return liftDir(this.sign.getBlock());
 	}
 		
 	static private File liftDir (Block block) {
 		return new File(plugin.getDataFolder() + File.separator + block.getWorld().getName() + File.separator + block.getX() + File.separator + block.getZ() + File.separator + block.getY());
 	}
 
 	private String readOwner() {
 		File owner = new File(this.liftDir() + File.separator + "owner");
 		if (!owner.exists()) {
 			return null;
 		}
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new InputStreamReader(new FileInputStream(owner)));
 			String inputLine = br.readLine();
 			if (inputLine == null) {
 				return null;
 			}
 			br.close();
 			return inputLine;
 		}
 		catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	static public void writeOwner(Block block, String playerName) {
 		File dir = liftDir(block);
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		File owner = new File(dir + File.separator + "owner");
 		BufferedWriter bw;
 		try {
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(owner)));
 			bw.write(playerName);
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public LiftSign(Block block) throws NotASignLiftException {
 		this(block.getState());
 	}
 	
 	public LiftSign(BlockState state) throws NotASignLiftException {
 		if (!(state instanceof Sign))
 			throw new NotASignLiftException();
 		
 		this.block = state.getBlock();
 		this.sign = (Sign)state;
 		
 		String line = this.sign.getLine(1).toString();
 		
 		if (line.length() < 3)
 			throw new NotASignLiftException();
 		
 		if (line.startsWith(plugin.getNormalOpen()) && line.endsWith(plugin.getNormalClose())) {
 			this.isPrivate = false;
 		}
 		else if (line.startsWith(plugin.getPrivateOpen()) && line.endsWith(plugin.getPrivateClose())) {
 			String loaded = this.readOwner();
 			if (loaded != null) {
 				this.isPrivate = true;
 				this.owner = loaded;
 			}
 		}
 		else {
 			throw new NotASignLiftException();
 		}
 		
 		// Remove the prefix and suffix
 		line = line.substring(1, line.length() - 1);
 
 		if (line.equalsIgnoreCase(plugin.getLiftString())) {
 			this.direction = Direction.NONE;
 			this.sign.setLine(1, (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen()) + plugin.getLiftString() + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
 		}
 		else if (line.equalsIgnoreCase(plugin.getLiftUpString())) {
 			this.direction = Direction.UP;
 			this.sign.setLine(1, (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen()) + plugin.getLiftUpString() + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
 		}
 		else if (line.equalsIgnoreCase(plugin.getLiftDownString())) {
 			this.direction = Direction.DOWN;
 			this.sign.setLine(1, (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen()) + plugin.getLiftDownString() + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
 		}
 		else {
 			throw new NotASignLiftException();
 		}
 		
 		this.label = this.sign.getLine(0);
 	}
 	
 	public Direction getDirection() {
 		return direction;
 	}
 	
 	public String getLabel() {
 		return label;
 	}
 	
 	public String getOwner() {
 		return owner.length() == 0 ? null : owner;
 	}
 	
 	public Boolean checkOwner(Player player) {
 		if (!isPrivate && player.hasPermission("signlift.use.normal"))
 			return true;
 
 		String playerName = player.getName();
 
 		if (owner.equalsIgnoreCase(playerName))
 			return player.hasPermission("signlift.use.private.own");
 
 		return player.hasPermission("signlift.use.private.other");
 	}
 
 	public Boolean checkBreak(Player player) {
 		String playerName = player.getName();
 
 		if (!isPrivate && player.hasPermission("signlift.create.normal"))
 			return true;
 		
 		if (owner.equalsIgnoreCase(playerName))
 			return player.hasPermission("signlift.create.private.own");
 
 		return player.hasPermission("signlift.create.private.other");
 	}
 	
 	public void remove() {
 		if (!this.isPrivate) return;
 		File dir = this.liftDir();
 		File[] contents = dir.listFiles();
 		for (int i = 0; i < contents.length; i++) {
 			contents[i].delete();
 		}
 		dir.delete();
 		// Clean higher dirs
 		dir = dir.getParentFile();
 		contents =  dir.listFiles();
 		int sanity = 3;
 		while (contents.length == 0) {
 			dir.delete();
 			dir = dir.getParentFile();
 			contents =  dir.listFiles();
 			if (sanity-- == 0) {
 				return;
 			}
 		}
 	}
 	
 	public Block getBlock() {
 		return block;
 	}
 	
 	public Sign getSign() {
 		return sign;
 	}
 	
 	public Boolean activate(Player player) {
 		if (this.direction == Direction.NONE) {
 			return false;
 		}
 
 		LiftSign target = this.findSign();
 
 		if (target == null) return false;
 
 		if (!(this.checkOwner(player) && target.checkOwner(player))) {
 			player.sendMessage(plugin.getDeniedLift());
 			return false;
 		}
 
 		Location loc = player.getLocation();
 		
 		String destination = target.getLabel();
 		String message;
 		if (destination.equals("")) {
 			message = this.direction == Direction.UP ? plugin.getDefaultGoingUpString() : plugin.getDefaultGoingDownString();
 		}
 		else {
 			message = String.format(this.direction == Direction.UP ? plugin.getGoingUpStringFormat() : plugin.getGoingDownStringFormat(), destination);
 		}
 
 		Block block0 = target.getTargetBlock(loc, 0);
 		Boolean safe = false;
		if (block0.getY() < block0.getWorld().getMaxHeight()) {
 			Block block1 = target.getTargetBlock(loc, 1);
 			loc.setY(block0.getY());
 			safe = this.safeBlock(block0) && this.safeBlock(block1);
 		}
 
 		if (block0.getY() > 0 && !safe) {
 			Block block1 = target.getTargetBlock(loc, -1);
 			loc.setY(block0.getY()-1);
 			safe = this.safeBlock(block0) && this.safeBlock(block1);
 		}
 
 		if (safe) {
 			player.teleport(loc);
 			player.sendMessage(message);
 			return true;
 		}
 
 		return false;
 	}
 	
 	private Block getTargetBlock(Location loc, int offset) {
 		Boolean sanity = plugin.getSanityCheck();
 		int x = sanity ? (int)Math.round(loc.getX()) : loc.getBlockX(); 
 		int y = this.sign.getY() + offset;
 		int z = sanity ? (int)Math.round(loc.getZ()) : loc.getBlockZ();
 		
 		return this.sign.getWorld().getBlockAt(x, y, z);
 	}
 	
 	private Boolean safeBlock(Block block) {
 		switch (block.getType()) {
 		case AIR:
 		case BROWN_MUSHROOM:
 		case CROPS:
 		case DEAD_BUSH:
 		case DIODE_BLOCK_OFF:
 		case DIODE_BLOCK_ON:
 		case GLOWING_REDSTONE_ORE:
 		case LADDER:
 		case LEVER:
 		case LONG_GRASS:
 		case RED_MUSHROOM:
 		case RED_ROSE:
 		case REDSTONE_ORE:
 		case SIGN:
 		case SIGN_POST:
 		case STATIONARY_WATER:
 		case STONE_BUTTON:
 		case STONE_PLATE:
 		case SUGAR_CANE_BLOCK:
 		case TORCH:
 		case WALL_SIGN:
 		case WATER:
 		case WOOD_PLATE:
 		case YELLOW_FLOWER:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	private LiftSign findSign() {
 		World world = this.sign.getWorld();
 		int x = this.sign.getX(), y = this.sign.getY(), z = this.sign.getZ();
 		int d;
 		switch (this.direction) {
 			case UP :
 				d = 1;
 				break;
 			case DOWN:
 				d = -1;
 				break;
 			default:
 				return null;
 		}
 
 		for (int h = world.getMaxHeight(), y1 = y + d; y1 < h && y1 > 0; y1 += d) {
 			Block block = world.getBlockAt(x, y1, z);
 			BlockState blockState = block.getState();
 			if (blockState instanceof Sign) {
 				String line = ((Sign)blockState).getLine(1);
 				if (line.length() > 2) {
 					line = line.substring(1, line.length()-1);
 					if (line.equalsIgnoreCase(plugin.getLiftString()) || line.equalsIgnoreCase(plugin.getLiftUpString()) || line.equalsIgnoreCase(plugin.getLiftDownString())) {
 						try {
 							return new LiftSign(blockState);
 						}
 						catch (NotASignLiftException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 }
