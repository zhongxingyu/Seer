 package en.trudan.ConstructionSites.Data;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 public class MaterialStorage implements Serializable {
 
 	private static final long serialVersionUID = -2568572539544142880L;
 	private static HashMap<Blockdata, Integer> ms = null;
 
 	public MaterialStorage() {
 		ms = new HashMap<Blockdata, Integer>();
 	}
 
 	public int getAmought(int blockID) {
 		if (ms.containsKey(new Blockdata(blockID))) {
 			return ms.get(new Blockdata(blockID));
 		}
 		return 0;
 	}
 
 	public Boolean hasMaterial(int blockID, int amount) {
 		if (ms.containsKey(new Blockdata(blockID))) {
 			if (ms.get(new Blockdata(blockID)) >= amount) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Boolean hasMaterial(Material material, int amount) {
 		if (ms.containsKey(material.getId())) {
 			if (ms.get(material.getId()) >= amount) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addMaterial(int blockID, int amount) {
 		if (ms.containsKey(new Blockdata(blockID))) {
 			ms.put(new Blockdata(blockID), ms.get(blockID) + amount);
 		}
 	}
 
 	public void addMaterial(Material material, int amount) {
 		if (ms.containsKey(new Blockdata(material.getId()))) {
 			ms.put(new Blockdata(material.getId()), ms.get(material.getId())
 					+ amount);
 		}
 	}
 
 	public boolean useMaterial(int blockID, int amount) {
 		if (ms.containsKey(new Blockdata(blockID))) {
 			if (ms.get(blockID) > amount) {
 				ms.put(new Blockdata(blockID), ms.get(blockID) - amount);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean useMaterial(Material material, int amount) {
 		if (ms.containsKey(new Blockdata(material.getId()))) {
 			if (ms.get(material.getId()) > amount) {
 				ms.put(new Blockdata(material.getId()),
 						ms.get(material.getId()) - amount);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getAmought(int blockID, byte data) {
 		if (ms.containsKey(new Blockdata(blockID, data))) {
 			return ms.get(new Blockdata(blockID, data));
 		}
 		return 0;
 	}
 
 	public Boolean hasMaterial(int blockID, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(blockID, data))) {
 			if (ms.get(new Blockdata(blockID, data)) >= amount) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Boolean hasMaterial(Material material, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(material.getId()))) {
 			if (ms.get(new Blockdata(material.getId(), data)) >= amount) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addMaterial(int blockID, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(blockID, data))) {
 			ms.put(new Blockdata(blockID, data),
 					ms.get(new Blockdata(blockID, data)) + amount);
 		}
 	}
 
 	public void addMaterial(Material material, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(material.getId(), data))) {
 			ms.put(new Blockdata(material.getId(), data),
 					ms.get(new Blockdata(material.getId(), data)) + amount);
 		}
 	}
 
 	public boolean useMaterial(int blockID, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(blockID, data))) {
 			if (ms.get(new Blockdata(blockID, data)) > amount) {
 				ms.put(new Blockdata(blockID, data),
 						ms.get(new Blockdata(blockID, data)) - amount);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean useMaterial(Material material, int amount, byte data) {
 		if (ms.containsKey(new Blockdata(material.getId()))) {
 			if (ms.get(new Blockdata(material.getId(), data)) > amount) {
 				ms.put(new Blockdata(material.getId(), data),
 						ms.get(new Blockdata(material.getId(), data)) - amount);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addMaterial(ItemStack is){
 		if (ms.containsKey(new Blockdata(is.getTypeId(), is.getData().getData()))) {
 			ms.put(new Blockdata(is.getTypeId(), is.getData().getData()),
 					ms.get(new Blockdata(is.getTypeId(), is.getData().getData())) + is.getAmount());
 		}
 	}
 
 	public ItemStack[] getChest() {
 
 		int i = 0;
 		Set<Blockdata> items = ms.keySet();
 		List<ItemStack> chest = new ArrayList<ItemStack>();
 		for (Blockdata blockdata : items) {
 			int amount = ms.get(blockdata);
 			while (amount != 0) {
 				if (i < 54) {
 					if (amount >= 64) {
 						amount -= 64;
 						ItemStack is = new ItemStack(blockdata.getBlockvalue(),
								64, blockdata.getData(), blockdata.getData());
 						chest.add(is);
 						i++;
 					} else if (amount >= 1 && amount <= 64) {
 						ItemStack is = new ItemStack(blockdata.getBlockvalue(),
								amount, blockdata.getData(), blockdata.getData());
 						chest.add(is);
 						amount = 0;
 						i++;
 					}
 				}
 			}
 		}
 		return (ItemStack[]) chest.toArray();
 	}
 
 }
