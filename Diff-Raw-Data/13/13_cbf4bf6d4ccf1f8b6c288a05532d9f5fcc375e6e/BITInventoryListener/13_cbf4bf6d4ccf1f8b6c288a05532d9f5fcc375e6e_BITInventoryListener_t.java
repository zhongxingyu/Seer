 package dk.gabriel333.BukkitInventoryTools.Inventory;
 
 import org.bukkit.Material;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.craftbukkit.inventory.CraftInventory;
 import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
 import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import org.getspout.spoutapi.event.inventory.InventoryListener;
 import org.getspout.spoutapi.event.inventory.InventoryOpenEvent;
import org.getspout.spoutapi.SpoutManager;

import org.getspout.spoutapi.material.Item;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dk.gabriel333.BukkitInventoryTools.BIT;
 import dk.gabriel333.BukkitInventoryTools.Book.BITBook;
 import dk.gabriel333.Library.BITConfig;
 import dk.gabriel333.Library.BITMessages;
 import dk.gabriel333.Library.BITPermissions;
 
 public class BITInventoryListener extends InventoryListener {
 
 	public BIT plugin;
 
 	public BITInventoryListener(BIT plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onInventoryOpen(InventoryOpenEvent event) {
 		SpoutPlayer sPlayer = (SpoutPlayer) event.getPlayer();
 		if (sPlayer.isSpoutCraftEnabled()) {
 			if (BITConfig.SORT_DISPLAYSORTARCHIEVEMENT
 					&& BITPermissions.hasPerm(sPlayer, "sortinventory.use",
 							BITPermissions.QUIET)) {
 				BITMessages.sendNotification(sPlayer, "Sort:"
 						+ BITConfig.LIBRARY_SORTKEY);
 			}
 		}
 		// CHEST_INVENTORY / BOOKSHELF_INVENTORY
		// Inventory inv = event.getInventory();  Created AbstractErrorMethod HERE because of this line.
		
		String name = "Bookshelf"; //New Line
		Inventory inv = SpoutManager.getInventoryBuilder() //NewLine
				.construct(BITConfig.BOOKSHELF_SIZE, name);
		setBookNamesAndCleanup(sPlayer, inv); //Abstract Error Method
 
 		if (!inv.getName().equals(sPlayer.getInventory().getName())) {
 			inv = sPlayer.getInventory();
 			setBookNamesAndCleanup(sPlayer, inv);
 		}
 	}
 
 	private void setBookNamesAndCleanup(SpoutPlayer sPlayer, Inventory inv) {
 		short bookId = 0;
 		if (inv.contains(Material.BOOK)) {
 			BITBook bitBook = new BITBook();
 			for (int i = 0; i < inv.getSize(); i++) {
 				if (inv.getItem(i).getType() == Material.BOOK) {
 					bookId = inv.getItem(i).getDurability();
 					if (bookId > 1000) {
 						if (BITBook.isWritten(sPlayer, bookId)) {
 							bitBook = BITBook.loadBook(sPlayer, bookId);
 							//Item item = (Item) inv.getItem(i);
 							//item.setName(bitBook.getTitle()+" written by "+bitBook.getAuthor());
 							
 							BITBook.setBookName(bookId, bitBook.getTitle(),
 									bitBook.getAuthor());
 						} else {
 							BITMessages
 									.showInfo("Wiping unknown BITBook in slot "
 											+ i + " (Id:" + bookId
 											+ ") in inventory:" + inv.getName());
 							ItemStack is = inv.getItem(i);
 							is.setDurability((short) 0);
 						}
 
 					}
 				}
 			}
 		}
 	}
 
 	public void onInventoryClose(InventoryCloseEvent event) {
 		SpoutPlayer sPlayer = (SpoutPlayer) event.getPlayer();
 		if (event.getInventory().getName().equals("Bookshelf")) {
 			int id = sPlayer.getEntityId();
 			BITInventory bitInventory = BITInventory.openedInventories.get(id);
 			BITInventory.saveBitInventory(sPlayer, bitInventory);
 			BITInventory.openedInventories.remove(id);
 		}
 	}
 
 	public void onInventoryClick(InventoryClickEvent event) {
 
 		SpoutPlayer sPlayer = (SpoutPlayer) event.getPlayer();
 		ItemStack itemClicked = event.getItem();
 		ItemStack itemPlaced = event.getCursor();
 		if (itemClicked != null) {
 			if (itemClicked.getType().equals(Material.BOOK)
 					&& itemClicked.getDurability() > 1000) {
 				short bookId = itemClicked.getDurability();
 				if (BITBook.isWritten(sPlayer, bookId)) {
 					BITBook bitBook = BITBook.loadBook(sPlayer, bookId);
 					//Item item = (Item) itemClicked;
 					//item.setName(bitBook.getTitle() + " written by "
 					//		+ bitBook.getAuthor());
 					 BITBook.setBookName(bookId, bitBook.getTitle(),
 					 bitBook.getAuthor());
 					int slotNo = event.getSlot();
 					if (bitBook.getMasterCopy()
 							&& bitBook.getMasterCopyId() == 0) {
 						// ItemStack copyOfMaster = itemClicked;
 						ItemStack masterBook = itemClicked.clone();
 						Inventory inventory = event.getInventory();
 						inventory.setItem(slotNo, masterBook);
 
 						short nextBookId = BITBook.getNextBookId();
 						// sPlayer.sendMessage("nextBookId:" + nextBookId);
 						BITBook newBook = BITBook.loadBook(sPlayer, bookId);
 						newBook.setBookId(nextBookId);
 						newBook.setTitle(bitBook.getTitle()
 								+ " (Syncronized copy)");
 						newBook.setBookId(nextBookId);
 						newBook.setMasterCopyId(bitBook.getBookId());
 						BITBook.bitBooks.put(nextBookId, newBook);
 						BITBook.saveBook(sPlayer, nextBookId);
 						itemClicked.setDurability(nextBookId);
 						sPlayer.sendMessage("BIT:This Book is a copy of a master, and will be updated autotmatically.");
 					} else if (bitBook.getCopyTheBookWhenMoved()) {
 						ItemStack book = itemClicked;
 						event.getInventory().setItem(slotNo, book);
 						short nextBookId = BITBook.getNextBookId();
 						BITBook newBook = bitBook;
 						newBook.setBookId(nextBookId);
 						itemClicked.setDurability(nextBookId);
 						BITBook.bitBooks.put(nextBookId, bitBook);
 						BITBook.saveBook(sPlayer, nextBookId);
 						sPlayer.sendMessage("BIT:This Book is being copied when moved");
 					} else if (!(bitBook.getCanBeMovedFromInventory()
 							&& (sPlayer.getName().equalsIgnoreCase(bitBook
 									.getAuthor())) || bitBook.getCoAuthors()
 							.contains(sPlayer.getName()))) {
 						event.setCancelled(true);
 					}
 				} else {
 					sPlayer.sendMessage("The book is invalid (bookId:" + bookId
 							+ ")");
 				}
 			}
 		} else if (itemPlaced != null) {
 			if (itemPlaced.getType().equals(Material.BOOK)
 					&& itemPlaced.getDurability() > 1000) {
 				short bookId = itemPlaced.getDurability();
 				if (BITBook.isWritten(sPlayer, bookId)) {
 
 				}
 			}
 		}
 	}
 
 	public void onInventoryCraft(InventoryCraftEvent event) {
 	}
 
 	public void onCustumEvent(Event event) {
 	}
 
 }
