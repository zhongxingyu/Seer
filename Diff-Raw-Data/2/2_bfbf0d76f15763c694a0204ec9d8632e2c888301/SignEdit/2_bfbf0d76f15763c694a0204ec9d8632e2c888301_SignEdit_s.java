 package de.philworld.bukkit.magicsigns.signedit;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import de.philworld.bukkit.magicsigns.MagicSigns;
 import de.philworld.bukkit.magicsigns.permissions.PermissionException;
 
 public class SignEdit {
 
 	/**
 	 * This Map saves the location of the sign that contains the new content as
 	 * the key. The target sign's location is the value.
 	 *
 	 * <p>
 	 * TempSign => TargetSign
 	 */
 	private Map<Location, Location> editSigns = new HashMap<Location, Location>();
 
 	private Map<String, EditMode> editMode = new HashMap<String, EditMode>();
 
 	public final SignEditListener listener = new SignEditListener(this);
 	final MagicSigns plugin;
 	public final SignEditCommandExecutor cmdExecutor = new SignEditCommandExecutor(
 			this);
 
 	public SignEdit(MagicSigns plugin) {
 		this.plugin = plugin;
 		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
 	}
 
 	/**
 	 * Returns if this sign is just temporary to edit another sign.
 	 *
 	 * @param sign
 	 *            The block to check.
 	 * @return True if this sign is temporary, else false
 	 */
 	public boolean isTempSign(Block sign) {
 		return editSigns.containsKey(sign.getLocation());
 	}
 
 	/**
 	 * Returns if this sign is currently edited.
 	 *
 	 * @param sign
 	 *            The block to check.
 	 * @return True if this sign is currently edited, else false
 	 */
 	public boolean isEdited(Block sign) {
 		return editSigns.containsValue(sign.getLocation());
 	}
 
 	/**
 	 * Get the block that is edited by the given sign.
 	 *
 	 * @param sign
 	 *            The block to get the edited block of.
 	 * @return The edited block if the given sign is a temporary sign, else
 	 *         null.
 	 */
 	public Block getTargetBlock(Block sign) {
 		Location edited = editSigns.get(sign.getLocation());
 		if (edited != null)
 			return edited.getBlock();
 		else
 			return null;
 	}
 
 	/**
 	 * Get the temporary edit sign for this block.
 	 *
 	 * @param sign
 	 *            The block to get the temporary edit block for.
 	 * @return The temporary sign if the sign is edited, else null.
 	 */
 	public Block getTempEditBlock(Block sign) {
 		for (Entry<Location, Location> entry : editSigns.entrySet()) {
 			if (sign.equals(entry.getValue())) {
 				return entry.getKey().getBlock();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the {@link EditMode} for this player.
 	 *
 	 * @param p
 	 *            The player
 	 * @param mode
 	 *            The {@link EditMode}
 	 * @throws PermissionException
 	 */
 	public void setEditMode(Player p, EditMode mode) throws PermissionException {
		if (mode == DEFAULT_EDIT_MODE && editMode.containsKey(p.getName())) {
 			editMode.remove(p.getName());
 			return;
 		}
 		if (mode.hasPermission(p)) {
 			editMode.put(p.getName(), mode);
 		} else {
 			throw new PermissionException();
 		}
 	}
 
 	/**
 	 * Get the {@link EditMode} for this player.
 	 *
 	 * @param p
 	 *            The player.
 	 * @return The {@link EditMode} for this player.
 	 */
 	public EditMode getEditMode(Player p) {
 		if (editMode.containsKey(p.getName())) {
 			return editMode.get(p.getName());
 		} else {
 			if(EditMode.AUTO.hasPermission(p)) {
 				return EditMode.AUTO;
 			} else if(EditMode.MASK_MAGIC_SIGNS.hasPermission(p)) {
 				return EditMode.MASK_MAGIC_SIGNS;
 			} else if(EditMode.MODIFY.hasPermission(p)) {
 				return EditMode.MODIFY;
 			} else {
 				return EditMode.NONE;
 			}
 		}
 	}
 
 	/**
 	 * Registers a new temporary edit sign.
 	 *
 	 * @param editSign
 	 *            The temporary edit sign to edit the target.
 	 * @param target
 	 *            The targetSign that is edited by the editSign.
 	 */
 	void registerEditSign(Location editSign, Location target) {
 		editSigns.put(editSign, target);
 	}
 
 	/**
 	 * Unregisters a temporary edit sign.
 	 *
 	 * @param loc
 	 *            The location of the edit sign.
 	 */
 	void unregisterEditSign(Location loc) {
 		editSigns.remove(loc);
 	}
 
 }
