 /**
  * BookEditor - Package: syam.BookEditor.Command
  * Created: 2012/09/08 15:30:53
  */
 package syam.BookEditor.Command;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import syam.BookEditor.BookEditor;
 import syam.BookEditor.Book.Book;
 import syam.BookEditor.Book.BookActions;
 import syam.BookEditor.Enum.Permission;
 import syam.BookEditor.Util.Actions;
 
 /**
  * CopyCommand (CopyCommand.java)
  * @author syam
  */
 public class CopyCommand extends BaseCommand{
 	public CopyCommand(){
 		bePlayer = true;
 		name = "copy";
 		argLength = 0;
 		usage = "<- copy your book";
 	}
 
 	@Override
 	public void execute() {
 		ItemStack is = player.getItemInHand();
 
 		// Check inHand item
 		if (is.getType() == Material.BOOK_AND_QUILL){
 			Actions.message(null, player, "&cコピーするためには本に署名する必要があります！");
 			return;
 		}
 		else if (is.getType() != Material.WRITTEN_BOOK){
 			Actions.message(null, player, "&c持っているアイテムが署名済みの本ではありません！");
 			return;
 		}
 
 		// Check Author
 		Book book = new Book(is);
 
 		if (!player.getName().equalsIgnoreCase(book.getAuthor()) && !Permission.COPY_OTHER.hasPerm(player)){
 			Actions.message(null, player, "&cそれはあなたが書いた本ではありません！");
 			return;
 		}
 
 		// Check empty slot
 		PlayerInventory inv = player.getInventory();
 
 		if (inv.firstEmpty() < 0){
 			Actions.message(null, player, "&cインベントリがいっぱいです！");
 			return;
 		}
 
 		// Pay cost
 		boolean paid = false;
 		int cost = 100; // 100 Coins
 
		if (!Permission.COPY_FREE.hasPerm(sender)){
 			paid = Actions.takeMoney(player.getName(), cost);
 			if (!paid){
 				Actions.message(null, player, "&cお金が足りません！ " + cost + " Coin 必要です！");
 				return;
 			}
 		}
 
 		// Copy
 		inv.addItem(is.clone());
 
 		String msg = "&aタイトル'&6" + book.getTitle() + "&a'の本をコピーしました！";
 		if (paid) msg = msg + " &c(-" + cost + " Coins)";
 		Actions.message(null, player, msg);
 
 		return;
 	}
 
 	@Override
 	public boolean permission() {
 		return Permission.COPY.hasPerm(sender);
 	}
 }
