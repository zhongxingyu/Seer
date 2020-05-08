 package silentAbyss.item;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.Scanner;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemWritableBook;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.world.World;
 import silentAbyss.core.util.LogHelper;
 import silentAbyss.core.util.ResourceLocationHelper;
 import silentAbyss.lib.Reference;
 import silentAbyss.lib.Strings;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 public class ModBook extends ItemWritableBook {
     
     public static NBTTagCompound[] books = { tagForBook(0), tagForBook(1) };
 
     public ModBook(int id) {
 
         super(id - Reference.SHIFTED_ID_RANGE_CORRECTION);
         setHasSubtypes(true);
         setMaxDamage(0);
     }
     
     @Override
     public boolean getShareTag() {
         
         return false;
     }
     
     @Override
     public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
         
         // Create a temporary written book with the desired NBT.
         // We don't want every instance of mod book to have lots of NBT data in the world save.
         ItemStack bookWithNBT = new ItemStack(Item.writtenBook);
         bookWithNBT.stackTagCompound = books[stack.getItemDamage()];
         
         player.displayGUIBook(bookWithNBT);
         return stack;
     }
     
     public static boolean validBookTagPages(NBTTagCompound tags) {
         
         return true;
     }
     
     
     @SideOnly(Side.CLIENT)
     public void getSubItems(int id, CreativeTabs tabs, List list) {
         
         for (int i = 0; i < books.length; ++i) {
             list.add(new ItemStack(this, 1, i));
         }
     }
     
     @Override
     public String getUnlocalizedName(ItemStack stack) {
         
         StringBuilder s = new StringBuilder();
         s.append("item.");
         s.append(Strings.RESOURCE_PREFIX);
         s.append("book.");
         s.append(stack.getItemDamage());
         return s.toString();
     }
     
     @Override
     public boolean hasEffect(ItemStack stack) {
         
         return true;
     }
     
     @Override
     public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
         
         if (stack.getItemDamage() < books.length && books[stack.getItemDamage()].hasKey("author")) {
             list.add("by " + books[stack.getItemDamage()].getString("author"));
         }
         else {
             LogHelper.severe("ModBook with damage value " + stack.getItemDamage() + " is missing its NBT data!");
             list.add("(book data missing!)");
         }
     }
     
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister iconRegister) {
         
         itemIcon = iconRegister.registerIcon("book_written");
     }
     
     public static void addRecipes() {
         
         GameRegistry.addShapelessRecipe(new ItemStack(ModItems.book, 1, 0), Item.book, ModItems.sigilStone);
         for (int i = 0; i < AbyssGem.names.length; ++i) {
             GameRegistry.addShapelessRecipe(new ItemStack(ModItems.book, 1, 1), Item.book, new ItemStack(ModItems.abyssGem, 1, i));
         }
     }
     
     private static NBTTagCompound tagForBook(int meta) {
         
      // It would be better to read from a file, but MC is not cooperating >_<
         switch (meta) {
             case 0: {
                 NBTTagCompound tags = new NBTTagCompound();
                 tags.setString("title", "An Attempt to Explain Sigils");
                 tags.setString("author", "SilentChaos512");
                 NBTTagList pages = new NBTTagList();
                pages.appendTag(new NBTTagString("1", "\u00a7nAbyss Sigils\u00a7r\n\nSigils are powerful magical items. They can light enemies on fire, heal you, and teleport you home. Or any combination of those things and more."));
                pages.appendTag(new NBTTagString("2", "\u00a7nCrafting\u00a7r\n\nYou will need a Sigil Infuser and some Sigil Stones. Place non-empty Sigil Stones into the Sigil Infuser to craft Abyss Sigils. The order of the Sigil Stones determines the effects (see Grammar)."));
                pages.appendTag(new NBTTagString("3", "\u00a7nSigil Stones\u00a7r\n\nSigil Stones are the \"words\" that describe a Sigil. Up to four are allowed per Sigil. Multiple \"effect\" words are allowed on one Sigil. \"Modifiers\" are applied to the first effect that follows them."));
                pages.appendTag(new NBTTagString("4", "\u00a7nGrammar\u00a7r\n\nSigil Stones can be divided into two rough categories: Effects (nouns) and Modifiers (adjectives)\nEffects describe what happens when a Sigil is used."));
                 pages.appendTag(new NBTTagString("5", "Not all effects accept all modifiers. For example, \"Pink Teleport\" makes no sense, so the \"Pink\" word will be ignored. Nonsense modifiers have no ill effects, so feel free to color-code teleports if you need to."));
                pages.appendTag(new NBTTagString("6", "\u00a7nExamples\u00a7r\n\nRed Amplify Amplify Fireball: Shoots a red fireball that does 100% extra damage.\nSpeed Remedy Healing Teleport: Removes potion effects (such as poison), heals 3 hearts, teleports you home, and takes less time to use."));
                 tags.setTag("pages", pages);
                 return tags;
             }
             case 1: {
                 NBTTagCompound tags = new NBTTagCompound();
                 tags.setString("title", "Chaos and You");
                 tags.setString("author", "SilentChaos512");
                 NBTTagList pages = new NBTTagList();
                pages.appendTag(new NBTTagString("1", "\u00a7nChaos\u00a7r\n\nChaos is a mysterious force that governs unpredictable (and often dangerous) events (Chaos Events)."));
                pages.appendTag(new NBTTagString("2", "\u00a7nChaos Level\u00a7r\n\nThe chaos level is the sum of chaos energy in the world. It will slowly rise if below a certain level, and slowly fall if above that level. Also, using certain artifacts can alter the chaos level."));
                pages.appendTag(new NBTTagString("3", "\u00a7nChaos Factor\u00a7r\n\nThe chaos factor rises exponentially with the chaos level. The higher the chaos factor, the higher the chance of chaos events occuring."));
                pages.appendTag(new NBTTagString("4", "\u00a7nChaos Events\u00a7r\n\nSometimes chaos will cause strange events, called chaos events. These include meteor showers and strange creatures appearing. Walls and roofs are good defenses against many chaos events."));
                 tags.setTag("pages", pages);
                 return tags;
             }
             default: {
                 return null;
             }
         }
     }
 
 }
