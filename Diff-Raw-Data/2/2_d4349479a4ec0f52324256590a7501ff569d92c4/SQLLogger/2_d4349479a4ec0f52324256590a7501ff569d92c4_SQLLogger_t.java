 package btwmod.itemlogger;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Container;
 import net.minecraft.src.DamageSource;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityItemFrame;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.ItemStack;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.ServerAPI;
 import btwmods.entity.EntityEvent;
 import btwmods.io.Settings;
 import btwmods.player.ContainerEvent;
 import btwmods.player.DropEvent;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.player.SlotEvent;
 import btwmods.server.ITickListener;
 import btwmods.server.TickEvent;
 import btwmods.world.BlockEvent;
 
 public class SQLLogger implements ILogger, ITickListener {
 
 	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH");
 	public static final SimpleDateFormat commentDateFormat = new SimpleDateFormat("EEE, dd MMM, yyyy HH:mm:ss Z");
 	public static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	public static final SimpleDateFormat sqlTimeFormat = new SimpleDateFormat("HH:mm:ss");
 	
 	private mod_ItemLogger mod = null;
 	
 	private String currentDate = null;
 	private String fileNamePattern = "itemlogger-%DATE%.sql";
 	private File outputDir = ModLoader.modDataDir;
 	
 	private File outputFile = null;
 	
 	private Map<String, String> lastPlayerPos = new HashMap<String, String>();
 	
 	@Override
 	public void init(mod_ItemLogger mod, Settings settings) {
 		this.mod = mod;
 		ServerAPI.addListener(this);
 		
 		if (settings.hasKey(SQLLogger.class.getSimpleName(), "outputDir")) {
 			File newDir = new File(settings.get(SQLLogger.class.getSimpleName(), "outputDir"));
 			if (newDir.isDirectory())
 				outputDir = newDir;
 			else
 				ModLoader.outputError(mod.getName() + "'s [" + SQLLogger.class.getSimpleName() + "] outputDir setting does not point to a directory.");
 		}
 		
 		if (settings.hasKey(SQLLogger.class.getSimpleName(), "dateFormat")) {
 			try {
 				dateFormat = new SimpleDateFormat(settings.get(SQLLogger.class.getSimpleName(), "dateFormat"));
 			}
 			catch (IllegalArgumentException e) {
 				ModLoader.outputError(e, mod.getName() + "'s [" + SQLLogger.class.getSimpleName() + "] dateFormat is invalid: " + e.getMessage());
 			}
 		}
 		
 		if (settings.hasKey(SQLLogger.class.getSimpleName(), "fileNamePattern")) {
 			String newPattern = settings.get(SQLLogger.class.getSimpleName(), "fileNamePattern");
 			if (newPattern.length() == 0) {
 				ModLoader.outputError(mod.getName() + "'s [" + SQLLogger.class.getSimpleName() + "] fileNamePattern cannot be an empty string.");
 			}
 			else {
 				fileNamePattern = newPattern;
 			}
 		}
 	}
 	
 	public void checkOutputFile() {
 		String date = dateFormat.format(new Date());
 		if (currentDate == null || !currentDate.equals(date)) {
 			currentDate = date;
 			outputFile = new File(outputDir, fileNamePattern.replaceAll("%DATE%", date));
 		}
 	}
 	
 	private static String escapeValue(String value) {
 		StringBuilder sb = new StringBuilder("'");
 		
 		for (int i = 0; i < value.length(); i++) {
 			char c = value.charAt(i);
 			if (c < 32) {
 				switch (c) {
 					case '\n':
 						sb.append("\\n");
 						break;
 					case '\r':
 						sb.append("\\r");
 						break;
 					case '\t':
 						sb.append("\\t");
 						break;
 				}
 			}
 			else {
 				switch (c) {
 					case '\t':
 						sb.append("\\t");
 						break;
 					case '\\':
 					case '\'':
 					case '"':
 						sb.append('\\').append(c);
 						break;
 					default:
 						sb.append(c);
 				}
 			}
 		}
 		
 		return sb.append("'").toString();
 	}
 	
 	private static String buildStatement(String table, String columns, Object[] values) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("/* ").append(commentDateFormat.format(new Date())).append(" */ INSERT INTO ");
 		
 		sb.append(table).append(" (").append(columns).append(") VALUES (");
 		
 		for (int i = 0; i < values.length; i++) {
 			if (i > 0) sb.append(", ");
 			
 			if (values[i] == null) {
 				sb.append("null");
 			}
 			else if (values[i] instanceof Number) {
 				sb.append(values[i].toString());
 			}
 			else if (values[i].getClass() == Boolean.class) {
 				sb.append(((Boolean)values[i]).booleanValue() ? '1' : '0');
 			}
 			else {
 				sb.append(escapeValue(values[i].toString()));
 			}
 		}
 		
 		return sb.append(");").toString();
 	}
 
 	@Override
 	public void containerOpened(ContainerEvent event, EntityPlayer player, Block block, int dimension, int x, int y, int z) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("containers",
 				"eventdate, eventtime, actiontype, username, container, dimension, x, y, z",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "open", player.username, event.getBlock().getBlockName(), dimension, x, y, z }));
 	}
 
 	@Override
 	public void containerClosed(ContainerEvent event, EntityPlayer player) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("containers",
 				"eventdate, eventtime, actiontype, username, container",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "close", player.username, event.getContainer().getClass().getSimpleName() }));
 	}
 
 	@Override
 	public void containerRemoved(ContainerEvent event, EntityPlayer player, Block block, int dimension, int x, int y, int z) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("containers",
 				"eventdate, eventtime, actiontype, username, container, dimension, x, y, z",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "remove", player.username, event.getBlock().getBlockName(), dimension, x, y, z }));
 	}
 
 	@Override
 	public void withdrew(SlotEvent event, EntityPlayer player, ItemStack withdrawn, int withdrawnQuantity, Container container, IInventory inventory, BlockInfo lastContainerOpened) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("slots",
 				"eventdate, eventtime, actiontype, username, block, dimension, x, y, z, item, quantity, container, inventory",
 				new Object[] {
 					sqlDateFormat.format(now), sqlTimeFormat.format(now),
 					"withdrew",
 					player.username,
 					(lastContainerOpened == null ? null : lastContainerOpened.block.getBlockName()),
 					(lastContainerOpened == null ? null : lastContainerOpened.dimension),
 					(lastContainerOpened == null ? null : lastContainerOpened.x),
 					(lastContainerOpened == null ? null : lastContainerOpened.y),
 					(lastContainerOpened == null ? null : lastContainerOpened.z),
 					mod.getUniqueItemStackName(withdrawn),
 					withdrawnQuantity,
 					container.getClass().getSimpleName(),
 					inventory.getInvName()
 				}
 		));
 	}
 
 	@Override
 	public void deposited(SlotEvent event, EntityPlayer player, ItemStack deposited, int depositedQuantity, Container container, IInventory inventory, BlockInfo lastContainerOpened) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("slots",
 				"eventdate, eventtime, actiontype, username, block, dimension, x, y, z, item, quantity, container, inventory",
 				new Object[] {
 					sqlDateFormat.format(now), sqlTimeFormat.format(now),
 					"deposited",
 					player.username,
 					(lastContainerOpened == null ? null : lastContainerOpened.block.getBlockName()),
 					(lastContainerOpened == null ? null : lastContainerOpened.dimension),
 					(lastContainerOpened == null ? null : lastContainerOpened.x),
 					(lastContainerOpened == null ? null : lastContainerOpened.y),
 					(lastContainerOpened == null ? null : lastContainerOpened.z),
 					mod.getUniqueItemStackName(deposited),
 					depositedQuantity,
 					container.getClass().getSimpleName(),
 					inventory.getInvName()
 				}
 		));
 	}
 
 	@Override
 	public void containerBroken(BlockEvent event, int dimension, int x, int y, int z, ItemStack[] contents) {
 		Date now = new Date();
 		String contentsList = mod.getItemStackList(contents);
 		
 		if (contentsList.length() != 0)
 			mod.queueWrite(outputFile, buildStatement("containerbroken",
 					"eventdate, eventtime, container, dimension, x, y, z, contents",
 					new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), event.getBlockId() > 0 ? event.getBlock().getBlockName() : "air", dimension, x, y, z, contentsList }));
 	}
 
 	@Override
 	public void playerEdit(PlayerBlockEvent event, EntityPlayer player, int direction, int dimension, int x, int y, int z, ItemStack itemStack) {
 		Date now = new Date();
 		int blockId = event.getWorld().getBlockId(x, y, z);
 		mod.queueWrite(outputFile, buildStatement("playeredits",
 				"eventdate, eventtime, actiontype, username, block, dimension, x, y, z, helditem",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "edit", player.username, blockId == 0 ? "air" : Block.blocksList[blockId].getBlockName(), dimension, x, y, z, mod.getFullItemStackName(itemStack) }));
 	}
 
 	@Override
 	public void playerRemove(PlayerBlockEvent event, EntityPlayer player, int dimension, int x, int y, int z) {
 		Date now = new Date();
 		int blockId = event.getWorld().getBlockId(x, y, z);
 		mod.queueWrite(outputFile, buildStatement("playeredits",
 				"eventdate, eventtime, actiontype, username, block, dimension, x, y, z, helditem",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "remove", player.username, blockId == 0 ? "Air" : Block.blocksList[blockId].getBlockName(), dimension, x, y, z, null }));
 	}
 
 	@Override
 	public void playerPosition(EntityPlayer player, int dimension, int x, int y, int z) {
 		Date now = new Date();
 		String lastPos = lastPlayerPos.get(player.username.toLowerCase());
 		
 		if (lastPos == null)
 			lastPlayerPos.put(player.username.toLowerCase(), dimension + "," + x + "," + y + "," + z);
 		
 		else if (lastPos.equals(dimension + "," + x + "," + y + "," + z))
 			return;
 		
 		else
 			lastPlayerPos.put(player.username.toLowerCase(), dimension + "," + x + "," + y + "," + z);
 		
 		mod.queueWrite(outputFile, buildStatement("playerposition",
 				"eventdate, eventtime, username, dimension, x, y, z",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), player.username, dimension, x, y, z }));
 	}
 
 	@Override
 	public void playerLogin(PlayerInstanceEvent event, EntityPlayer player, int dimension, int x, int y, int z, boolean isLogout) {
 		Date now = new Date();
 		lastPlayerPos.remove(player.username.toLowerCase());
 		mod.queueWrite(outputFile, buildStatement("playerinstance",
 				"eventdate, eventtime, actiontype, username, dimension, x, y, z",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), isLogout ? "logout" : "login", player.username, dimension, x, y, z }));
 	}
 
 	@Override
 	public void playerDeath(PlayerInstanceEvent event, EntityPlayer player, int dimension, int x, int y, int z, String deathMessage) {
 		Date now = new Date();
 		lastPlayerPos.remove(player.username.toLowerCase());
 		mod.queueWrite(outputFile, buildStatement("playerdeath",
 				"eventdate, eventtime, username, dimension, x, y, z, message",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), player.username, dimension, x, y, z, deathMessage }));
 	}
 
 	@Override
 	public void playerDropAll(DropEvent event, EntityPlayer player, int dimension, int x, int y, int z, InventoryPlayer inventory) {
 		Date now = new Date();
 		ItemStack[] fullInventory = new ItemStack[inventory.armorInventory.length + inventory.mainInventory.length];
 		System.arraycopy(inventory.armorInventory, 0, fullInventory, 0, inventory.armorInventory.length);
 		System.arraycopy(inventory.mainInventory, 0, fullInventory, inventory.armorInventory.length, inventory.mainInventory.length);
 		
 		mod.queueWrite(outputFile, buildStatement("playerdrop",
 				"eventdate, eventtime, actiontype, username, dimension, x, y, z, items",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "all", player.username, dimension, x, y, z, mod.getItemStackList(fullInventory) }));
 	}
 
 	@Override
 	public void playerDropItem(DropEvent event, EntityPlayer player, int dimension, int x, int y, int z, ItemStack itemStack) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("playerdrop",
 				"eventdate, eventtime, actiontype, username, dimension, x, y, z, items",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), event.getType() == DropEvent.TYPE.EJECT ? "eject" : "drop", player.username, dimension, x, y, z, mod.getFullItemStackName(itemStack) }));
 	}
 
 	@Override
 	public void playerPickupItem(DropEvent event, EntityPlayer player, int dimension, int x, int y, int z, ItemStack itemStack) {
 		Date now = new Date();
 		mod.queueWrite(outputFile, buildStatement("playerdrop",
 				"eventdate, eventtime, actiontype, username, dimension, x, y, z, items",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), "pickup", player.username, dimension, x, y, z, mod.getFullItemStackName(itemStack) }));
 	}
 
 	@Override
 	public void playerUseEntity(PlayerActionEvent event, EntityPlayer player, int dimension, int x, int y, int z, Entity entity, int entityX, int entityY, int entityZ, boolean isAttack) {
 		Date now = new Date();
 		StringBuilder extra = new StringBuilder();
 		
 		if (entity.isDead || (entity instanceof EntityLiving && ((EntityLiving)entity).getHealth() <= 0)) {
 			if (extra.length() > 0) extra.append(", ");
 			extra.append("died");
 		}
 		
 		if (entity instanceof EntityItemFrame) {
 			if (extra.length() > 0) extra.append(", ");
 			extra.append(mod.getFullItemStackName(((EntityItemFrame)entity).getDisplayedItem()));
 		}
 		
 		mod.queueWrite(outputFile, buildStatement("playeruseentity",
 				"eventdate, eventtime, actiontype, username, dimension, x, y, z, entity, entityX, entityY, entityZ, extra",
 				new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), isAttack ? "attack" : "use", player.username, dimension, x, y, z, entity.getEntityName(), entityX, entityY, entityZ, extra.toString() }));
 	}
 
 	@Override
 	public void playerUseItem(PlayerActionEvent event, EntityPlayer player, int dimension, int x, int y, int z, ItemStack itemStack) {
 		if (itemStack == null)
 			itemStack = event.getOriginalItemStack();
 		
 		if (itemStack != null) {
 			Date now = new Date();
 			mod.queueWrite(outputFile, buildStatement("playeruseitem",
 					"eventdate, eventtime, username, dimension, x, y, z, item",
 					new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), player.username, dimension, x, y, z, mod.getFullItemStackName(itemStack) }));
 		}
 	}
 
 	@Override
 	public IMod getMod() {
 		return mod;
 	}
 
 	@Override
 	public void onTick(TickEvent event) {
 		if (event.getType() == TickEvent.TYPE.START && event.getTickCounter() % 5 == 0) {
 			checkOutputFile();
 		}
 	}
 
 	@Override
 	public void entityAttacked(EntityEvent event, EntityLiving entity, int dimension, int x, int y, int z, DamageSource source) {
 		Date now = new Date();
 		StringBuilder info = new StringBuilder();
 		
 		info.append(source.getDamageType());
 		
 		if (source.getEntity() != null)
 			info.append(source.getEntity().getEntityName());
 		
 		if (entity.getHealth() <= 0)
 			info.append(" died");
 		
 		mod.queueWrite(outputFile, buildStatement("entityattacked",
			"eventdate, eventtime, entity, dimension, x, y, z, info",
 			new Object[] { sqlDateFormat.format(now), sqlTimeFormat.format(now), entity.getEntityName(), entity.dimension, x, y, z, info.toString() }));
 	}
 }
