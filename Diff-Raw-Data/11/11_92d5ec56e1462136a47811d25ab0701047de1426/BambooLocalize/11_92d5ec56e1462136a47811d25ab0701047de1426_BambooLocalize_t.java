 package ruby.bamboo;
 
 import static ruby.bamboo.BambooInit.*;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import ruby.bamboo.entity.EnumSlideDoor;
 import ruby.bamboo.item.EnumFood;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.resources.Language;
 import net.minecraft.client.resources.ResourceManager;
 import net.minecraft.client.resources.ResourceManagerReloadListener;
 import net.minecraft.client.resources.SimpleReloadableResourceManager;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 @SideOnly(Side.CLIENT)
 public class BambooLocalize implements ResourceManagerReloadListener {
     private String lang;
     private Properties prop;
     public static BambooLocalize instance = new BambooLocalize();
     static {
         instance = new BambooLocalize();
     }
 
     public static void init() {
         for (Field field : Minecraft.getMinecraft().getResourceManager().getClass().getDeclaredFields()) {
             try {
                 if (field.getType() == List.class) {
                     field.setAccessible(true);
                     ((List) field.get(Minecraft.getMinecraft().getResourceManager())).add(0, instance);
                     instance.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
                 }
             } catch (Exception e) {
                 FMLLog.log(Level.WARNING, "BambooMod localize exception");
                 ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(instance);
                 return;
             }
         }
     }
 
     public void load(String lang) {
         try {
             this.lang = lang;
             prop = new Properties();
             InputStream stream = this.getClass().getResourceAsStream("/ruby/bamboo/lang/" + lang + ".properties");
 
            if (stream == null) {
                 System.out.printf("[mod_bamboo]lang %s not found \n", lang);
                stream = this.getClass().getResourceAsStream("/ruby/bamboo/lang/en_US.properties");
             }
            prop.load(new InputStreamReader(stream, "UTF-8"));
         } catch (Exception e) {
             e.printStackTrace();
            return;
         }
         // 桜の花どうしようかなー、名前が似たようなの?
         addName(sakuraleavsBID);
         addName(itemSackIID);
         int i;
         // サブタイプ無しは一括
         for (Field field : BambooInit.class.getDeclaredFields()) {
             if (field.getName().matches(".*BID") || field.getName().matches(".*IID")) {
                 try {
                     i = (Integer) field.get(null);
                     if (Item.itemsList[i] != null && !Item.itemsList[i].getHasSubtypes()) {
                         addName(i);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         // サブタイプありは個別
         addName(new ItemStack(bambooBlockBID, 1, 0));
         addName(new ItemStack(firecrackerIID, 0, 0));
         addName(new ItemStack(firecrackerIID, 0, 1));
         addName(new ItemStack(firecrackerIID, 0, 2));
 
         for (i = 0; i < EnumFood.values().length; i++) {
             addName(new ItemStack(foodsIID, 1, i));
         }
 
         addName(new ItemStack(bambooSpearIID, 1, 0));
         addName(new ItemStack(bambooSpearIID, 1, 1));
 
         for (i = 0; i < EnumSlideDoor.values().length; i++) {
             addName(new ItemStack(slideDoorsIID, 1, i));
         }
 
         addName(new ItemStack(dSquareBID, 1, 0));
         addName(new ItemStack(dSquareBID, 1, 4));
         addName(new ItemStack(dSquareBID, 1, 8));
         addName(new ItemStack(dSquareBID, 1, 10));
         addName(new ItemStack(dSquareBID, 1, 12));
         addName(new ItemStack(dHalfSquareBID, 1, 0));
         addName(new ItemStack(dHalfSquareBID, 1, 4));
         addName(new ItemStack(dHalfSquareBID, 1, 8));
         addName(new ItemStack(dHalfSquareBID, 1, 10));
         addName(new ItemStack(dHalfSquareBID, 1, 12));
         addName(new ItemStack(bamboopaneBID, 1, 0));
         addName(new ItemStack(bamboopaneBID, 1, 1));
         addName(new ItemStack(bamboopaneBID, 1, 2));
         addName(new ItemStack(bamboopaneBID, 1, 3));
         addName(new ItemStack(bamboopaneBID, 1, 4));
         addName(new ItemStack(bamboopaneBID, 1, 5));
         for (i = 0; i < 18; i++) {
             addName(new ItemStack(shavedIceIID, 1, i));
         }
 
         for (i = 0; i < 10; i++) {
             addName(new ItemStack(shavedIceIID, 1, i));
         }
         for (i = 0; i < 10; i++) {
             addName((new ItemStack(snowBallIID, 1, i)));
         }
         addName(new ItemStack(windmillIID, 1, 0));
         addName(new ItemStack(windmillIID, 1, 1));
         addName(new ItemStack(decoBID, 1, 0));
         addName(new ItemStack(decoBID, 1, 1));
         addName(new ItemStack(decoBID, 1, 2));
         addName(new ItemStack(decoBID, 1, 3));
         addName(new ItemStack(decoBID, 1, 4));
         addName(new ItemStack(decoBID, 1, 5));
         addName(new ItemStack(halfDecoBID, 1, 0));
         addName(new ItemStack(halfDecoBID, 1, 1));
         addName(new ItemStack(halfDecoBID, 1, 2));
         addName(new ItemStack(halfDecoBID, 1, 3));
         addName(new ItemStack(halfDecoBID, 1, 4));
         addName(new ItemStack(halfDecoBID, 1, 5));
         addName(new ItemStack(twoDirDecoBID, 1, 0));
         addName(new ItemStack(twoDirDecoBID, 1, 2));
         addName(new ItemStack(halfTwoDirDecoBID, 1, 0));
         addName(new ItemStack(halfTwoDirDecoBID, 1, 2));
         addName(new ItemStack(decoCarpetBID, 1, 0));
         addName(new ItemStack(decoCarpetBID, 1, 2));
         addName(new ItemStack(decoCarpetBID, 1, 4));
         addName(new ItemStack(decoCarpetBID, 1, 6));
         addName(new ItemStack(decoCarpetBID, 1, 8));
     }
 
     private void addName(int itemId) {
         try {
             LanguageRegistry.instance().addNameForObject(Item.itemsList[itemId], lang, prop.getProperty(Item.itemsList[itemId].getUnlocalizedName()));
         } catch (NullPointerException e) {
             FMLLog.getLogger().fine(Item.itemsList[itemId].getUnlocalizedName() + " translation mistake");
         }
     }
 
     private void addName(ItemStack is) {
         try {
             LanguageRegistry.instance().addNameForObject(is, lang, prop.getProperty(is.getItem().getUnlocalizedName() + "." + is.getItemDamage()));
         } catch (NullPointerException e) {
             FMLLog.getLogger().fine(is.getItem().getUnlocalizedName() + "." + is.getItemDamage() + " translation mistake");
         }
     }
 
     @Override
     public void onResourceManagerReload(ResourceManager resourcemanager) {
         Language lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
         load(lang.getLanguageCode());
     }
 }
