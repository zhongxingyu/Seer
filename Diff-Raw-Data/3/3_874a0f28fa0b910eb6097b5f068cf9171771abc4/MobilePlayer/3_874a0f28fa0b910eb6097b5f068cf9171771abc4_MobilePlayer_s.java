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
 
 import net.minecraft.server.v1_6_R3.Block;
 import net.minecraft.server.v1_6_R3.ContainerAnvil;
 import net.minecraft.server.v1_6_R3.ContainerEnchantTable;
 import net.minecraft.server.v1_6_R3.ContainerWorkbench;
 import net.minecraft.server.v1_6_R3.EntityHuman;
 import net.minecraft.server.v1_6_R3.EntityPlayer;
 import net.minecraft.server.v1_6_R3.IInventory;
 import net.minecraft.server.v1_6_R3.ItemStack;
 import net.minecraft.server.v1_6_R3.Packet100OpenWindow;
 import net.minecraft.server.v1_6_R3.TileEntityBrewingStand;
 import net.minecraft.server.v1_6_R3.TileEntityFurnace;
 
 import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 public class MobilePlayer {
 	private final MobilePlayerData data;
 	private final MobileConfiguration config;
 
 	private final Player player;
 	private final EntityPlayer handle;
 
 	private Inventory chest;
 	private EntityFurnace furnace;
 	private EntityBrewingStand brewingStand;
 
 	public MobilePlayer(MobileTools plugin, Player player) {
 		this.player = player;
 		this.handle = ((CraftPlayer) player).getHandle();
 		this.chest = Bukkit.createInventory(player, this.getChestSize());
 		this.furnace = new EntityFurnace(this.handle);
 		this.brewingStand = new EntityBrewingStand(this.handle);
 
 		this.config = plugin.getConfiguration();
 		this.data = plugin.getPlayerData(player.getName());
		this.data.load();
 	}
 
 	protected EntityFurnace getFurnace() {
 		return this.furnace;
 	}
 
 	protected EntityBrewingStand getBrewingStand() {
 		return this.brewingStand;
 	}
 
 	public void openEnderChest() {
 		this.player.openInventory(this.player.getEnderChest());
 	}
 
 	public void openWorkbench() {
 		WorkbenchContainer container = new WorkbenchContainer(this.handle);
 
 		int c = this.handle.nextContainerCounter();
 		this.handle.playerConnection
 				.sendPacket(new Packet100OpenWindow(c, 1, "Crafting", 9, true));
 		this.handle.activeContainer = container;
 		this.handle.activeContainer.windowId = c;
 		this.handle.activeContainer.addSlotListener(this.handle);
 	}
 
 	public void openEnchantingTable() {
 		EnchantTableContainer container = new EnchantTableContainer(this.config, this.handle);
 		
 		int c = this.handle.nextContainerCounter();
 		this.handle.playerConnection.sendPacket(new Packet100OpenWindow(c, 4, "Enchant", 9, true));
 		this.handle.activeContainer = container;
 		this.handle.activeContainer.windowId = c;
 		this.handle.activeContainer.addSlotListener(this.handle);
 	}
 
 	public void openAnvil() {
 		AnvilContainer container = new AnvilContainer(this.handle);
 
 		int c = this.handle.nextContainerCounter();
 		this.handle.playerConnection
 				.sendPacket(new Packet100OpenWindow(c, 8, "Repairing", 9, true));
 		this.handle.activeContainer = container;
 		this.handle.activeContainer.windowId = c;
 		this.handle.activeContainer.addSlotListener(this.handle);
 	}
 
 	public void updateChestSize() {
 		int newSize = this.getChestSize();
 		if (this.chest.getSize() == newSize) {
 			return;
 		}
 
 		org.bukkit.inventory.ItemStack[] items = this.chest.getContents();
 		this.chest = Bukkit.createInventory(this.player, newSize);
 
 		for (int i = 0; i < (items.length > this.chest.getSize() ? this.chest.getSize() :
 				items.length); i++) {
 			this.chest.setItem(i, items[i]);
 		}
 	}
 
 	public int getChestSize() {
 		int maxSize = 54;
 		int size = 9;
 		for (int i = 1; i <= (maxSize / 9); i++) {
 			if (this.player.hasPermission(new Permission("mobiletools.chestsize." + (i * 9),
 					PermissionDefault.OP))) {
 				size = i * 9;
 			}
 		}
 		return size;
 	}
 
 	public void openChest() {
 		this.player.openInventory(this.chest);
 	}
 
 	public void openFurnace() {
 		this.handle.openFurnace(this.furnace);
 	}
 
 	public void openBrewingStand() {
 		this.handle.openBrewingStand(this.brewingStand);
 	}
 
 	public void save() {
 		this.data.saveInventory("Chest", this.chest);
 		this.data.saveInventory("Furnace", this.furnace);
 		this.data.saveInventory("BrewingStand", this.brewingStand);
 
 		this.data.save();
 	}
 
 	public void load() {
 		this.data.load();
 
 		this.data.loadInventory("Chest", this.chest);
 		this.data.loadInventory("Furnace", this.furnace);
 		this.data.loadInventory("BrewingStand", this.brewingStand);
 	}
 
 	public void remove() {
 		this.save();
 	}
 
 	public class EnchantTableContainer extends ContainerEnchantTable {
 		private final MobileConfiguration config;
 
 		public EnchantTableContainer(MobileConfiguration config, EntityHuman entity) {
 			super(entity.inventory, entity.world, 0, 0, 0);
 			this.config = config;
 		}
 
 		@Override
 		public void a(IInventory iinventory) {
 			if (iinventory == this.enchantSlots) {
 				ItemStack itemstack = iinventory.getItem(0);
 
 				if (itemstack != null && itemstack.x()) {
 					this.costs[0] = this.config.getRandom("enchant.levels.line1").getRandom();
 					this.costs[1] = this.config.getRandom("enchant.levels.line2").getRandom();
 					this.costs[2] = this.config.getRandom("enchant.levels.line3").getRandom();
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
