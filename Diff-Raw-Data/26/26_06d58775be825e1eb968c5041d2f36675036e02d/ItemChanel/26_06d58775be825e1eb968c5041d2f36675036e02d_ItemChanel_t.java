 package ru.dark32.chat.chanels;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import ru.dark32.chat.Main;
 import ru.dark32.chat.Util;
 import ru.dark32.chat.ValueStorage;
 import ru.dark32.chat.ichanels.IItemChanel;
 
 /**
  * @author Andrew
  * 
  */
 public class ItemChanel extends BaseChanel implements IItemChanel {
 
	final private int		itemAmount;
	final private int		itemId;
 	final private Material	itemMaterial;
	final private int		itemSubId;
	final private boolean	requestPprefix;
 
 	public ItemChanel(String name ){
 		super(name);
 		final String path_item_id = "Chat." + name + ".item.id";
 		final String path_item_subId = "Chat." + name + ".item.subid";
 		final String path_item_amount = "Chat." + name + ".item.amount";
 		final String path_item_material = "Chat." + name + ".item.material";
 		final String path_requestPrefix = "Chat." + name + ".requestPrefix";
 		this.itemId = Main.chatConfig.getInt(path_item_id, 0);
 		this.itemSubId = Main.chatConfig.getInt(path_item_subId, 0);
 		this.itemAmount = Main.chatConfig.getInt(path_item_amount, 1);
 		Material ma = Material.getMaterial(Main.chatConfig.getString(path_item_material));
 		this.itemMaterial = ma != null ? ma : Material.AIR;
 		this.requestPprefix = Main.chatConfig.getBoolean(path_requestPrefix, true);
 	}
 
 	@Override
 	public boolean canSend(final Player sender, final String message ) {
		if (!Main.getPermissionsHandler()
				.hasPermission(sender, Main.BASE_PERM + "." + this.getInnerName() + ".no_item")) {
 			// если вещь в руках совпала
 			if (((IItemChanel) this).equalItem(sender.getItemInHand())) {
 				// теряем 1 вещь
 				((IItemChanel) this).loseItem(sender);
 			} else { // иначе
 				// глаголим, что вещи нет
 				sender.sendMessage(ValueStorage.nei);
 				// event.setCancelled(true);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@SuppressWarnings("deprecation" )
 	@Override
 	public boolean equalItem(final ItemStack item ) {
 		if (item == null) {
 			return false;
 		}
 		Util.DEBUG("debug inhand " + item.getTypeId() + ":" + item.getDurability() + " - " + item.getType());
 		Util.DEBUG("debug need " + itemId + ":" + itemSubId + " - " + itemMaterial);
 		final boolean isItem = item.getDurability() == this.itemSubId
 				&& item.getAmount() >= this.itemAmount
 				&& ((ValueStorage.experemental && item.getType() == this.itemMaterial) || item.getTypeId() == this.itemId);
 		return isItem;
 	}
 
 	@Override
 	final public int getItemAmount() {
 		return itemAmount;
 	}
 
 	@Override
 	@Deprecated
 	final public int getItemId() {
 		return this.itemId;
 	}
 
 	@Override
 	final public Material getItemMaterial() {
 		return itemMaterial;
 	}
 
 	@Override
 	final public int getItemSubId() {
 		return this.itemSubId;
 	}
 
 	@Override
 	final public boolean isRequestPprefix() {
 		return requestPprefix;
 	}
 
 	@SuppressWarnings("deprecation" )
 	@Override
 	final public void loseItem(final Player player ) {
 		final ItemStack inHand = player.getItemInHand();
 		if (this.itemAmount == 0) {
 			return;
 		}
 		final int amoutHand = inHand.getAmount() - this.itemAmount;
 		ItemStack inHandrem = null;
 		if (ValueStorage.experemental) {
 			inHandrem = new ItemStack(inHand.getType(), amoutHand);
 		} else {
 			inHandrem = new ItemStack(inHand.getTypeId(), amoutHand);
 		}
 		if (amoutHand <= 0) {
 			inHandrem = null;
 		}
 		player.setItemInHand(inHandrem);
 		return;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + ", id =>" + this.itemId + ", subid=>" + this.itemSubId + ", material =>"
 				+ this.itemMaterial.name() + ", count =>" + this.getItemAmount();
 	}
 }
