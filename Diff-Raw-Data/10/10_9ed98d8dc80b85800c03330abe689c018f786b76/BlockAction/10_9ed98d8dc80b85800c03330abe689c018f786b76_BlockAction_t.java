 package net.timstans.actionblocks.blocks;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.bergerkiller.bukkit.common.config.ConfigurationNode;
 import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
 import com.bergerkiller.bukkit.common.utils.ParseUtil;
 import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
 import com.bergerkiller.bukkit.tc.controller.MinecartMember;
 
 public abstract class BlockAction {
 	private static Map<Material, BlockAction> blocks = new HashMap<Material, BlockAction>();
 	private boolean invertPow = false;
 	private Material material = null;
 	private final Set<MinecartGroup> activeGroups = new HashSet<MinecartGroup>();
 	private final Set<MinecartMember<?>> activeMembers = new HashSet<MinecartMember<?>>();
 
 	/**
 	 * Fired when a player completes the placement of an Action Block
 	 * 
 	 * @param player that built
 	 * @param block that was built
 	 * @return True if building was successful, False if not
 	 * @throws NoPermissionException - when Player has no permission for placing it
 	 */
 	public abstract boolean onBuild(Player player, Block block) throws NoPermissionException;
 
 	/**
 	 * Gets the Default Material assigned to this Action block (can be overrideen in configuration)
 	 * 
 	 * @return Default Block Material
 	 */
 	public abstract Material getDefaultMaterial();
 
 	/**
 	 * Gets the name of this type of Action Block
 	 * 
 	 * @return Action Block Name
 	 */
 	public abstract String getName();
 
 	public void load(ConfigurationNode node) {
 		this.invertPow = node.get("powerInverted", this.invertPow);
 	}
 
 	public boolean isPowerInverted() {
 		return this.invertPow;
 	}
 
 	public Material getMaterial() {
 		return this.material;
 	}
 
	protected void setMaterial(Material material) {
		this.material = material;
	}

 	public boolean isPowered(Block block) {
 		return block.isBlockIndirectlyPowered() != this.isPowerInverted();
 	}
 
 	public final void onBlockLeave(MinecartMember<?> member, Block block) {
 		if (activeMembers.remove(member)) {
 			this.onMemberLeave(member, block);
 			MinecartGroup group = member.getGroup();
 			if (group.tail() == member && activeGroups.remove(group)) {
 				this.onGroupLeave(member.getGroup(), block);
 				this.cleanUp();
 			}
 		}
 	}
 
 	public final void onBlockEnter(MinecartMember<?> member, Block block) {
 		if (activeMembers.add(member)) {
 			this.onMemberEnter(member, block);
 			MinecartGroup group = member.getGroup();
 			if (activeGroups.add(group)) {
 				this.onGroupEnter(group, block);
 			}
 		}
 	}
 
 	public final  void cleanUp() {
 		Iterator<MinecartMember<?>> members = activeMembers.iterator();
 		while (members.hasNext()) {
 			if (members.next().getEntity().isDead()) {
 				members.remove();
 			}
 		}
 		Iterator<MinecartGroup> groups = activeGroups.iterator();
 		while (groups.hasNext()) {
 			if (!groups.next().isValid()) {
 				groups.remove();
 			}
 		}
 	}
 
 	/**
 	 * Fired when a Minecart enters this Action Block
 	 * 
 	 * @param member that entered
 	 * @param block of this Action Block that was entered
 	 */
 	public void onMemberEnter(MinecartMember<?> member, Block block) {}
 
 	/**
 	 * Fired when a Minecart leaves this Action Block
 	 * 
 	 * @param member that left
 	 * @param block of this Action Block that was left
 	 */
 	public void onMemberLeave(MinecartMember<?> member, Block block) {}
 
 	/**
 	 * Fired when a Minecart Group enters this Action Block
 	 * 
 	 * @param group that entered
 	 * @param block of this Action Block that was entered
 	 */
 	public void onGroupEnter(MinecartGroup group, Block block) {}
 
 	/**
 	 * Fired when a Minecart Group leaves this Action Block
 	 * 
 	 * @param group that left
 	 * @param block of this Action Block that was left
 	 */
 	public void onGroupLeave(MinecartGroup group, Block block) {}
 
 	/**
 	 * Fired when the redstone power state of a Block changes
 	 * 
 	 * @param block that changed redstone state
 	 * @param powered state to which was changed
 	 */
 	public void onRedstoneChange(Block block, boolean powered) {}
 
 	/**
 	 * Obtains he BlockAction registered to the Material specified
 	 * 
 	 * @param material to get the Block Action for
 	 * @return Block Action registered for the Material, or null if none is registered
 	 */
 	public static BlockAction get(Material material) {
 		return blocks.get(material);
 	}
 
 	/**
 	 * Obtains he BlockAction registered to the Material of a Block specified
 	 * 
 	 * @param block to get the Block Action for
 	 * @return Block Action registered for the Material, or null if none is registered
 	 */
 	public static BlockAction get(Block block) {
 		return get(block.getType());
 	}
 
 	/**
 	 * Registers a single Action Block
 	 * 
 	 * @param material of the Action Block
 	 * @param actionBlock to register
 	 * @return the input actionBlock
 	 */
 	public static <T extends BlockAction> T register(Material material, T actionBlock) {
		BlockAction block = (BlockAction) actionBlock;
		blocks.put(material, block);
		block.setMaterial(material);
 		return actionBlock;
 	}
 
 	/**
 	 * Registers a single Action Block
 	 * 
 	 * @param materialName of the Action Block
 	 * @param actionBlock to register
 	 * @return the input actionBlock
 	 */
 	public static <T extends BlockAction> T register(String materialName, T actionBlock) {
 		Material mat = actionBlock.getDefaultMaterial();
 		if (materialName != null && materialName.length() > 0) {
 			mat = ParseUtil.parseMaterial(materialName, mat);
 		}
 		return register(mat, actionBlock);
 	}
 
 	/**
 	 * Registers a single Action Block
 	 * 
 	 * @param config to read the Block configuration from
 	 * @param actionBlock to register
 	 * @return the input actionBlock
 	 */
 	public static <T extends BlockAction> T register(ConfigurationNode config, T actionBlock) {
 		config = config.getNode(actionBlock.getName());
 		register(config.get("material", actionBlock.getDefaultMaterial().toString()), actionBlock).load(config);
 		return actionBlock;
 	}
 
 	/**
 	 * Initializes all Action Blocks by loading the configuration
 	 * 
 	 * @param materials configuration
 	 */
 	public static void init(ConfigurationNode materials) {
 		register(materials, new BlockActionStation());
 		register(materials, new BlockActionEjector());
 		register(materials, new BlockActionElevator());
 	}
 
 	/**
 	 * De-initializes all registered Action Blocks
 	 */
 	public static void deinit() {
 		blocks.clear();
 	}
 }
