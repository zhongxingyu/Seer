 package com.gravypod.alladmin.user;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ContainerChest;
 import net.minecraft.inventory.ContainerEnchantment;
 import net.minecraft.inventory.ContainerWorkbench;
 import net.minecraft.inventory.InventoryEnderChest;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet100OpenWindow;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.world.ChunkCoordIntPair;
 
 import com.gravypod.alladmin.AllAdmin;
 import com.gravypod.alladmin.IUser;
 import com.gravypod.alladmin.files.SerializedLocation;
 import com.gravypod.alladmin.files.SerializedUser;
 import com.gravypod.alladmin.files.UserFiles;
 import com.gravypod.alladmin.minecraft.FakeCrafting;
 import com.gravypod.alladmin.minecraft.FakeEnchantment;
 import com.gravypod.alladmin.permissions.Group;
 import com.gravypod.alladmin.permissions.PermissionManager;
 import com.gravypod.alladmin.permissions.PermissionManager.CommandPermissions;
 
 public class AllAdminUser implements IUser {
 
 	private final EntityPlayerMP sender;
 	private final String name;
 	private Group group;
 	private boolean isInvisible;
 	private final HashMap<String, SerializedLocation> homes = new HashMap<String, SerializedLocation>();
 	private boolean muted;
 	private boolean godemode;
 	
 	public AllAdminUser(EntityPlayerMP sender) {
 		this.sender = sender;
 		this.name = getHandle().getCommandSenderName();
 		SerializedUser serializedUser = UserFiles.loadUser(name);
 		this.group = PermissionManager.getGroup(serializedUser.rank);
 		this.muted = serializedUser.isMuted;
 		this.isInvisible = serializedUser.isInvisible;
 		this.godemode = serializedUser.godmode;
 		if (serializedUser.homes != null) {
 			homes.putAll(serializedUser.homes);
 		}
 	}
 
 	@Override
 	public boolean hasPermission(String permission) {
 		if (!PermissionManager.groupExists(group.getName())) {
 			setRank(PermissionManager.getDefaultRank());
 		}
 		return group.hasPermission(permission) || MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(getUsername());
 
 	}
 
 	@Override
 	public void setRank(String rank) {
 		group = PermissionManager.changeRank(name, rank);
 	}
 
 	@Override
 	public Group getRank() {
 
 		return this.group;
 
 	}
 
 	@Override
 	public void heal() {
 		setHealth(Integer.MAX_VALUE);
 	}
 
 	@Override
 	public void setHealth(int h) {
 		getHandle().setHealth(h);
 	}
 
 	@Override
 	public void send(String message) {
		getHandle().sendChatToPlayer(
				ChatMessageComponent.createFromText(message));
 	}
 
 	@Override
 	public void translate(String key, Object... args) {
 		send(String.format(AllAdmin.getString(key), args));
 	}
 
 	@Override
 	public void logout() {
 		try {
			System.out.println("Logging out");
 			SerializedUser user = new SerializedUser();
 			user.rank = getRank().getName();
 			user.homes = getHomes();
 			user.isMuted = isMute();
 			user.isInvisible = isInvisible();
 			user.godmode = this.godemode;
 			user.name = name;
 			UserFiles.unloadUser(name, user);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void teleport(double x, double y, double z) {
 		teleport(x, y, z, getHandle().cameraPitch, getHandle().cameraYaw);
 	}
 
 	@Override
 	public void teleport(double x, double y, double z, float pitch, float yaw) {
 		getHandle().cameraPitch = pitch;
 		getHandle().cameraYaw = yaw;
 		getHandle().setPositionAndUpdate(x, y, z);
 	}
 
 	@Override
 	public void setHome(String name) {
 		SerializedLocation location = getLocation();
 		homes.put(name, location);
 	}
 
 	@Override
 	public void setHome() {
 		setHome("default");
 	}
 
 	@Override
 	public void sendHome(String name) {
 		SerializedLocation location = homes.get(name);
 
 		if (location == null) {
 			return;
 		}
 
 		if (location.dim != getDimension()) {
 			changeDimension(location.dim);
 		}
 
 		teleport(location.x, location.y, location.z, location.pitch, location.yaw);
 
 	}
 
 	@Override
 	public void sendHome() {
 		sendHome("default");
 	}
 
 	@Override
 	public HashMap<String, SerializedLocation> getHomes() {
 		return homes == null ? new HashMap<String, SerializedLocation>()
 				: homes;
 	}
 
 	@Override
 	public void feed() {
 		feed(20);
 	}
 
 	@Override
 	public void feed(int food) {
 		getHandle().getFoodStats().addStats(food, food);
 	}
 	@Override
 	public void openEnderChest() {
 		InventoryEnderChest chest = this.getHandle().getInventoryEnderChest();
 		getHandle().displayGUIChest(chest);
 	}
 
 	@Override
 	public void openWorkbench() {
         openContainer(new FakeCrafting(getHandle().inventory, getHandle().worldObj), 1, 9, "Crafting");
 	}
 
 	@Override
 	public void toggleMute() {
 		this.muted = !muted;
 	}
 
 	@Override
 	public boolean isMute() {
 		return muted;
 	}
 
 	@Override
 	public void openEnchantment() {
 		openContainer(new FakeEnchantment(getHandle().inventory, getHandle().worldObj), 4, 9, "");
 	}
 
 	@Override
 	public ICommandSender getICommandSender() {
 		return getHandle();
 	}
 
 	@Override
 	public boolean isInvisible() {
 		return isInvisible;
 	}
 
 	@Override
 	public void setInvisible(boolean on) {
 		this.isInvisible = on;
 	}
 
 	@Override
 	public String getUsername() {
 		return getHandle().username;
 	}
 
 	@Override
 	public void changeDimension(int dim) {
 		getHandle().travelToDimension(dim);
 	}
 
 	@Override
 	public int getDimension() {
 		return getHandle().worldObj.provider.dimensionId;
 	}
 
 	@Override
 	public boolean hasPermission(CommandPermissions perm) {
 		return hasPermission(perm.getPermission());
 	}
 
 	@Override
 	public void toggleFlight() {
 		boolean currentToggle = getHandle().capabilities.allowFlying;
 		getHandle().capabilities.allowFlying = !currentToggle;
 		
 		if (!currentToggle && getHandle().capabilities.isFlying) {
 			getHandle().capabilities.isFlying = false;
 		}
 		
 		getHandle().sendPlayerAbilities();
 	}
 
 	@Override
 	public EntityPlayerMP getHandle() {
 		return sender;
 	}
 
 	@Override
 	public boolean isOnline() {
 		return true;
 	}
 
 	@Override
 	public boolean isFlying() {
 		return getHandle().capabilities.allowFlying;
 	}
 	
 	private void openContainer(Container container, int indescriptNumberOne, int indescriptNumberTwo, String randomIndescriptString) {
 		getHandle().incrementWindowID();
 		getHandle().playerNetServerHandler.sendPacketToPlayer(new Packet100OpenWindow(getHandle().currentWindowId, indescriptNumberOne, randomIndescriptString, indescriptNumberTwo, false));
 		getHandle().openContainer = container;
 		getHandle().openContainer.windowId = getHandle().currentWindowId;
 		getHandle().openContainer.addCraftingToCrafters(getHandle());
 	}
 
 	@Override
 	public SerializedLocation getLocation() {
 		SerializedLocation location = new SerializedLocation();
 		ChunkCoordinates coords = getHandle().getPlayerCoordinates();
 		location.x = coords.posX;
 		location.y = coords.posY;
 		location.z = coords.posZ;
 		location.pitch = getHandle().cameraPitch;
 		location.yaw = getHandle().cameraYaw;
 		location.dim = getDimension();
 		return location;
 	}
 
 	@Override
 	public void teleport(SerializedLocation location) {
 		teleport(location.x, location.y, location.z, location.pitch, location.yaw);
 	}
 
 	@Override
 	public void teleport(ChunkCoordinates location) {
 		teleport(location.posX, location.posY, location.posZ);
 	}
 
 	@Override
 	public void showInventory(IUser user) {
 		getHandle().displayGUIChest(user.getHandle().inventory);
 	}
 
 	@Override
 	public void clearInventory() {
 		InventoryPlayer inv = getHandle().inventory;
 		for (int i = 0; i < inv.mainInventory.length; i++) {
 			inv.mainInventory[i] = null;
 		}
 		inv.onInventoryChanged();
 		
 	}
 
 	@Override
 	public boolean hasGodMode() {
 		return this.godemode;
 	}
 
 	@Override
 	public boolean canBreak(int blockID) {
 		return hasPermission(CommandPermissions.BLOCK_ALL) || hasPermission(CommandPermissions.BLOCK.getPermissionFor(blockID));
 	}
 
 	@Override
 	public void repairItemInHand() {
 		
 		ItemStack stack = getHandle().inventory.getCurrentItem();
 		
 		if (stack == null) {
 			return;
 		}
 		
 		stack.setItemDamage(0);
 		
 	}
 
 	@Override
 	public void toggleGodMode() {
 		godemode = !godemode;
 	}
 
 	@Override
 	public boolean isJailed() {
 		return false;
 	}
 
 	@Override
 	public void sendJail() {
 	}
 
 	@Override
 	public void setFire() {
 		setFire(60);
 	}
 	
 	@Override
 	public void setFire(int seconds) {
 		getHandle().setFire(seconds);
 	}
 }
