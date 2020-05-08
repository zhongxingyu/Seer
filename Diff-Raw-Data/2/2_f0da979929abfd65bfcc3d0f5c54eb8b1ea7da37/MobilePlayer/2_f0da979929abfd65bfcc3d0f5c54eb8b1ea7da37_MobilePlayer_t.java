 /**
  * 
  * This software is part of the MobileTools
  * 
  * MobileTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or 
  * any later version.
  * 
  * MobileTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MobileTools. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package me.cybermaxke.mobiletools;
 
 import java.io.File;
 
 import me.cybermaxke.mobiletools.tagutils.TagCompound;
 import me.cybermaxke.mobiletools.tagutils.TagUtils;
 
 import net.minecraft.server.v1_6_R2.Block;
 import net.minecraft.server.v1_6_R2.ContainerAnvil;
 import net.minecraft.server.v1_6_R2.ContainerEnchantTable;
 import net.minecraft.server.v1_6_R2.ContainerWorkbench;
 import net.minecraft.server.v1_6_R2.EntityHuman;
 import net.minecraft.server.v1_6_R2.EntityPlayer;
 import net.minecraft.server.v1_6_R2.IInventory;
 import net.minecraft.server.v1_6_R2.ItemStack;
 import net.minecraft.server.v1_6_R2.Packet100OpenWindow;
 import net.minecraft.server.v1_6_R2.TileEntityBrewingStand;
 import net.minecraft.server.v1_6_R2.TileEntityFurnace;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 public class MobilePlayer {
 	private Player player;
 	private EntityPlayer ep;
 	private Inventory chest;
 	private EntityFurnace furnace;
 	private EntityBrewingStand brewingStand;
 	private File dataFile;
 
 	public MobilePlayer(Player player) {
 		this.player = player;
 		this.ep = ((CraftPlayer) player).getHandle();
 		this.chest = Bukkit.createInventory(player, this.getChestSize());
 		this.furnace = new EntityFurnace(this.ep);
 		this.brewingStand = new EntityBrewingStand(this.ep);
 		File f = new File(MobileTools.getInstance().getDataFolder() + File.separator + "PlayerData");
 		if (!f.exists()) {
 			f.mkdirs();
 		}
 		this.dataFile = new File(f, player.getName() + ".data");
 		if (!this.dataFile.exists()) {
 			try {
 				this.dataFile.createNewFile();
 			} catch (Exception e) {}
 		} else {
 			TagCompound t = (TagCompound) TagUtils.load(this.dataFile);
 			if (t.hasKey("Chest")) {
 				this.chest = TagUtils.getInventoryFromTag(this.chest, t.getCompound("Chest"));
 			}
 			if (t.hasKey("Furnace")) {
 				this.furnace = TagUtils.getInventoryFromTag(this.furnace, t.getCompound("Furnace"));
 			}
 			if (t.hasKey("BrewingStand")) {
 				this.brewingStand = TagUtils.getInventoryFromTag(this.brewingStand, t.getCompound("BrewingStand"));
 			}
 		}
 	}
 
 	protected EntityFurnace getFurnace() {
 		return this.furnace;	
 	}
 
 	protected EntityBrewingStand getBrewingStand() {
 		return this.brewingStand;	
 	}
 
 	public void openWorkbench() {
 		WorkbenchContainer container = new WorkbenchContainer(this.ep);
 
 		int c = this.ep.nextContainerCounter();
 		this.ep.playerConnection.sendPacket(new Packet100OpenWindow(c, 1, "Crafting", 9, true));
 		this.ep.activeContainer = container;
 		this.ep.activeContainer.windowId = c;
 		this.ep.activeContainer.addSlotListener(this.ep);
 	}
 
 	public void openEnchantingTable() {
 		EnchantTableContainer container = new EnchantTableContainer(this.ep);
 		
 		int c = this.ep.nextContainerCounter();
 		this.ep.playerConnection.sendPacket(new Packet100OpenWindow(c, 4, "Enchant", 9, true));
 		this.ep.activeContainer = container;
 		this.ep.activeContainer.windowId = c;
 		this.ep.activeContainer.addSlotListener(this.ep);
 	}
 
 	public void openAnvil() {
 		AnvilContainer container = new AnvilContainer(this.ep);
 
 		int c = this.ep.nextContainerCounter();
 		this.ep.playerConnection.sendPacket(new Packet100OpenWindow(c, 8, "Repairing", 9, true));
 		this.ep.activeContainer = container;
 		this.ep.activeContainer.windowId = c;
 		this.ep.activeContainer.addSlotListener(this.ep);
 	}
 
 	public void updateChestSize() {
 		int newSize = this.getChestSize();
 		if (this.chest.getSize() == newSize) {
 			return;
 		}
 
 		org.bukkit.inventory.ItemStack[] items = this.chest.getContents();
 		this.chest = Bukkit.createInventory(this.player, newSize);
 
 		for (int i = 0; i < (items.length > this.chest.getSize() ? this.chest.getSize() : items.length); i++) {
 			this.chest.setItem(i, items[i]);
 		}
 	}
 
 	public int getChestSize() {
 		int maxSize = 54;
 		int size = 9;
		for (int i = 1; i < (1 + maxSize / 9); i++) {
 			if (this.player.hasPermission(new Permission("mobiletools.chestsize." + (i * 9), PermissionDefault.OP))) {
 				size = i * 9;
 			}
 		}
 		return size;
 	}
 
 	public void openChest() {
 		this.player.openInventory(this.chest);
 	}
 
 	public void openFurnace() {
 		this.ep.openFurnace(this.furnace);
 	}
 
 	public void openBrewingStand() {
 		this.ep.openBrewingStand(this.brewingStand);
 	}
 
 	public void save() {
 		TagCompound t = new TagCompound();
 		t.setCompound("Chest", TagUtils.getInventoryAsTag(this.chest));
 		t.setCompound("Furnace", TagUtils.getInventoryAsTag(this.furnace));
 		t.setCompound("BrewingStand", TagUtils.getInventoryAsTag(this.brewingStand));
 		TagUtils.save(this.dataFile, t);
 	}
 
 	public void remove() {
 		this.save();
 	}
 
 	public class EnchantTableContainer extends ContainerEnchantTable {
 
 		public EnchantTableContainer(EntityHuman entity) {
 			super(entity.inventory, entity.world, 0, 0, 0);
 		}
 
 		@Override
 		public void a(IInventory iinventory) {
 			if (iinventory == this.enchantSlots) {
 				ItemStack itemstack = iinventory.getItem(0);
 
 				if (itemstack != null && itemstack.x()) {
 					this.costs[0] = 8;
 					this.costs[1] = 21;
 					this.costs[2] = 30;
 				} else {
 					this.costs[0] = 0;
 					this.costs[1] = 0;
 					this.costs[2] = 0;
 				}
 			}
 		}
 
 		@Override
 		public boolean a(EntityHuman entityhuman) {
 			return true;
 		}
 	}
 
 	public class WorkbenchContainer extends ContainerWorkbench {
 
 		public WorkbenchContainer(EntityHuman entity) {
 			super(entity.inventory, entity.world, 0, 0, 0);
 		}
 
 		@Override
 		public boolean a(EntityHuman entityhuman) {
 			return true;
 		}
 	}
 
 	public class AnvilContainer extends ContainerAnvil {
 
 		public AnvilContainer(EntityHuman entity) {
 			super(entity.inventory, entity.world, 0, 0, 0, entity);
 		}
 
 		@Override
 		public boolean a(EntityHuman entityhuman) {
 			return true;
 		}
 	}
 
 	public class EntityBrewingStand extends TileEntityBrewingStand {
 
 		public EntityBrewingStand(EntityHuman entity) {
 			this.b(entity.world);
 		}
 
 		@Override
 		public boolean a(EntityHuman entityhuman) {
 			return true;
 		}
 
 		@Override
 		public int p() {
 		    return -1;
 		}
 
 		@Override
 		public void update() {
 
 		}
 
 		@Override
 		public Block q() {
 		    return Block.BREWING_STAND;
 		}
 	}
 
 	public class EntityFurnace extends TileEntityFurnace {
 
 		public EntityFurnace(EntityHuman entity) {
 			this.b(entity.world);
 		}
 
 		@Override
 		public boolean a(EntityHuman entityhuman) {
 			return true;
 		}
 
 		@Override
 		public int p() {
 		    return -1;
 		}
 
 		@Override
 		public void update() {
 
 		}
 
 		@Override
 		public Block q() {
 		    return Block.FURNACE;
 		}
 	}
 }
