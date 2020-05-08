 package cz.ogion.ultraitems;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.material.MaterialData;
 
 public class Ingredient {
 	Integer itemid;
 	Integer itemdata;
 	Material material;
 	Logger log = Logger.getLogger("Minecraft");
 	public Ingredient(String ingredient) throws Exception {
 		String[] item = null;
 		try {
			item = ingredient.split("[:,;-]");
 			itemid = Integer.decode(item[0]);
 			itemdata = Integer.decode(item[1]);
 		} catch (Exception e) {
 			itemid = Integer.decode(ingredient);
 			itemdata = 0;
 		}
 		try {
 			material = new MaterialData(itemid).getItemType();
 		} catch (Exception e) {
 			throw new Exception("Id " + itemid + " of ingredient doesn't exist", new Throwable("wrongitem"));
 		}
 	}
 	public Material getMaterial() throws Exception {
 		if (material == null) {
 			throw new Exception("Id " + itemid + " of ingredient doesn't exist", new Throwable("wrongitem"));
 		}
 		return material;
 	}
 	public Short getData() {
 		return itemdata.shortValue();
 	}
 	public Byte getDataByte() {
 		return itemdata.byteValue();
 	}
 }
