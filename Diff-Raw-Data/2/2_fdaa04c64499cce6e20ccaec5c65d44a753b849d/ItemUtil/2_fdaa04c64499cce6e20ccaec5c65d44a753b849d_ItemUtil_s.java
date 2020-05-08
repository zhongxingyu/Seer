 package syam.Honeychest.util;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 public class ItemUtil {
 	/**
 	 * アイテムの文字列(itemid:datavalue)を取得 例: 94:3
 	 * @param stack 変換する ItemStack オブジェクト
 	 * @return アイテムIDの文字列
 	 */
 	public static String getItemString(ItemStack stack) {
 		// データ値が存在すればidに続いて :datavalue を付けて返す
 		if (stack.getData() != null && stack.getData().getData() != 0){
 			return stack.getTypeId() + ":" + stack.getData().getData();
 		}
 		// データIDがnullまたは0の場合はIDをそのまま返す
 		return Integer.toString(stack.getTypeId());
 	}
 
 	/**
 	 * アイテム名からItemStackを返す
 	 * @param item アイテムIDとデータ値の文字列
 	 * @param amount アイテム個数
 	 * @return ItemStack
 	 */
 	public static ItemStack itemStringToStack(String item, Integer amount) {
 		// アイテムID文字列を配列に分ける
 		String[] itemArr = item.split(":");
		ItemStack stack = new ItemStack(Integer.parseInt(itemArr[0], amount));
 		// アイテム名にデータ値があれば付与
 		if (itemArr.length > 1){
 			stack.setData(new MaterialData(Integer.parseInt(itemArr[0]), Byte.parseByte(itemArr[1])));
 		}
 		// ItemStackを返す
 		return stack;
 	}
 
 	/**
 	 * データ値があれば付与したまま、アイテムの名前を返す
 	 * @param itemData アイテムデータ
 	 * 	 * @return 文字列 アイテム名(:データ値)
 	 */
 	public static String getItemStringName(String itemData) {
 		String[] itemArr = itemData.split(":");
 
 		// 文字列が数値でなければそのまま返す
 		if (!Util.isInteger(itemArr[0])) {
 			return itemData;
 		}
 		// データ値があればデータ値を付けて、無ければアイテム名だけを返す
 		if (itemArr.length > 1){
 			return Material.getMaterial(Integer.parseInt(itemArr[0])).name() + ":" + itemArr[1];
 		}else{
 			return Material.getMaterial(Integer.parseInt(itemArr[0])).name();
 		}
 	}
 }
