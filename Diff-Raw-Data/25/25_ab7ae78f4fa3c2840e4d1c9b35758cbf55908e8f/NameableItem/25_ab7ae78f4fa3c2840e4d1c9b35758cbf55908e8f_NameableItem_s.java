 package to.joe.strangeweapons;
 
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
 
import org.bukkit.craftbukkit.inventory.CraftItemStack;
 
 public class NameableItem {
 
     private ItemStack s;
 
     public NameableItem(CraftItemStack itemStack) {
         s = itemStack.getHandle();
     }
 
     public boolean isStrange() {
         if (s.tag == null)
             return false;
         return s.tag.hasKey("Strange");
     }
 
     public void makeStrange() {
         if (s.tag == null) {
             s.tag = new NBTTagCompound();
         }
         s.tag.setBoolean("Strange", true);
         if (!s.tag.hasKey("display")) {
             s.tag.setCompound("display", new NBTTagCompound());
         }
     }
 
     public boolean hasName() {
         return s.tag.getCompound("display").hasKey("Name");
     }
 
     public void setName(String name) {
         s.tag.getCompound("display").setString("Name", name);
     }
 
     public String getName() {
         return s.tag.getCompound("display").getString("Name");
     }
 
     public boolean hasKills() {
         return s.tag.hasKey("PlayerKills");
     }
 
     public int getKills() {
         return s.tag.getInt("PlayerKills");
     }
 
     public void setKills(int kills) {
         s.tag.setInt("PlayerKills", kills);
     }
 
     public String[] getLore() {
         NBTTagList rawLore = s.tag.getCompound("display").getList("Lore");
         String[] lore = new String[rawLore.size()];
         for (int x = 0; x < rawLore.size(); x++) {
             lore[x] = ((NBTTagString) rawLore.get(x)).data;
         }
         return lore;
     }
 
     public void setLore(String[] lore) {
         NBTTagList list = new NBTTagList();
         for (int x = 0; x < lore.length; x++) {
             NBTTagString st = new NBTTagString(lore[x]);
             st.data = lore[x];
             list.add(st);
         }
         s.tag.getCompound("display").set("Lore", list);
     }
 
 }
