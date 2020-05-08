 package com.isocraft.thesis;
 
 import java.net.URL;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.item.crafting.ThesisOreDictRecipeShaped;
 import com.isocraft.item.crafting.ThesisRecipeShaped;
 import com.isocraft.item.crafting.XmlRecipieReference;
 import com.isocraft.lib.Reference;
 import com.isocraft.lib.Strings;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 
 /**
  * ISOCraft
  * 
  * Class for managing the Thesis System
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class ThesisSystem {
     
     public static final List<Thesis> ThesisList = new ArrayList<Thesis>();                                     //List of all of the Theses activated in the mod instance
     public static final Collection<String> ThesisOrderedList = new TreeSet<String>(Collator.getInstance());    // Alphabetically ordered list of thesis names
     public static final List<Thesis> PreloadThesisList  = new ArrayList<Thesis>();                             //List of all of the Theses that are designed to be preloaded
     
     public static final List<TickerData> ThesisCountdown = new ArrayList<TickerData>();                         //List of all of the NBTCompoundTags and usernames from players online on the server which need updating until then can potently use 
     
     /*XML map*/
     private static final HashMap<String, Element> ThesisXMLMap = new HashMap<String, Element>();                //Stores XML elements
     
     /*Crafting stuff*/
     public static final List<Integer> WhitelistPlayerDataSync = new ArrayList<Integer>();                       // If block id is present when a player right clicks on a block with the id it will sync entity data ready for crafting
     public static final HashMap<String, List<Boolean>> PlayerNBTDataUpdate = new HashMap<String, List<Boolean>>();// Stores updated player thesis unlock states client and server side, used for checking crafting is allowed
     public static final HashMap<Integer, String> CraftingChannels = new HashMap<Integer, String>();             //Stores reference channels
     public static final HashMap<String, XmlRecipieReference> xmlCraftingReference = new HashMap<String, XmlRecipieReference>(); 
     
     private static final ThesisSystem INSTANCE = new ThesisSystem();
     public static ThesisSystem instance() {
         return INSTANCE;
     }
     /*Thesis Management Methods*/
         public static void addThesis(Thesis obj){
             obj.init();
             ThesisList.add(obj);
             ThesisOrderedList.add(obj.getName());
             if(obj.isPreLoad()){
                 PreloadThesisList.add(obj);
             }
         }
 
     /*Player NBT Methods*/
         public static void createPlayerSetup(NBTTagCompound obj){
             for(int i = 0; i < ThesisList.size(); ++i){
                 obj.setCompoundTag(ThesisList.get(i).getName(), new NBTTagCompound());
                 
                 NBTTagCompound tag = obj.getCompoundTag(ThesisList.get(i).getName());
                 tag.setBoolean(Strings.UNLOCKED, ThesisList.get(i).isPreLoad());
                 tag.setInteger(Strings.COUNTDOWN_STATE, 0);
                 tag.setInteger(Strings.COUNTDOWN_TIME, ThesisList.get(i).getLimiterTimer());
                 tag.setTag(Strings.THEOREM, new NBTTagList());
                 
 
                 /* NBTTagCompound n = new NBTTagCompound();
                 n.setString("KEY", "TEST");
                 tag.getTagList(Strings.THEOREM).appendTag(n); -- adds a theorum to a thesis nbt*/
             }
         }
         
         public static void appendPlayerSetup(NBTTagCompound obj) {
             for(int i = 0; i < ThesisList.size(); ++i){
                 if(!obj.hasKey(ThesisList.get(i).getName())){
                     LogHelper.log(Level.INFO, " New Thesis '" + ThesisList.get(i).getName() + "' Found, Appending to NBT data for Player");
                     obj.setCompoundTag(ThesisList.get(i).getName(), new NBTTagCompound());
                     
                     NBTTagCompound tag = obj.getCompoundTag(ThesisList.get(i).getName());
                     tag.setBoolean(Strings.UNLOCKED, ThesisList.get(i).isPreLoad());
                     tag.setInteger(Strings.COUNTDOWN_STATE, 0);
                     tag.setInteger(Strings.COUNTDOWN_TIME, ThesisList.get(i).getLimiterTimer());  
                     tag.setTag(Strings.THEOREM, new NBTTagList());
                 }
             }
 
         }
         
         public static void addConectedPlayersCountdownsToTicker( String username, NBTTagCompound obj){
             for(int i = 0; i < ThesisList.size(); ++i){
                 NBTTagCompound tag = obj.getCompoundTag(ThesisList.get(i).getName());
                 if(tag.getInteger(Strings.COUNTDOWN_STATE) == 1){
                     ThesisCountdown.add(new TickerData(username, tag));
                 }
             }
         }
         
         public static void unlockCheck(NBTTagCompound obj){
             int theorems = 0;
             for(int i = 0; i < ThesisList.size(); ++i){
                 if(ThesisList.get(i).getName().equals(obj.getName())){
                     theorems = ThesisList.get(i).getNumberOfTheorems();
                 }
             }
             if(obj.getInteger(Strings.COUNTDOWN_STATE) == 2 && obj.getTagList(Strings.THEOREM).tagCount() == theorems){
                 obj.setBoolean(Strings.UNLOCKED, true);
                 LogHelper.log(Level.INFO, " Unlock");
             }
         }
         
         /*returns a list of all of the players unlock values for each thesis*/
         public static List<Boolean> getPlayerValues(EntityPlayer player){
             List<Boolean> ret = new ArrayList<Boolean>();
             NBTTagCompound nbt = player.getEntityData().getCompoundTag(Strings.PlayerPersisted).getCompoundTag(Reference.MOD_ID).getCompoundTag(Strings.THESIS);
 
             for (int i = 0; i < ThesisList.size(); ++i) {
                 ret.add(nbt.getCompoundTag(ThesisList.get(i).getName()).getBoolean(Strings.UNLOCKED));
             }
             return ret;
         }
         
     /*Crafting*/
         
         @SuppressWarnings({ "rawtypes", "unchecked" })
         public static void addShapedRecipe(ItemStack par1ItemStack,  String ThesisName, String xmlTag, boolean oredict, Object ... par2ArrayOfObj){
             String s = "";
             int i = 0;
             int j = 0;
             int k = 0;
 
             if (par2ArrayOfObj[i] instanceof String[]){
                 String[] astring = (String[])((String[])par2ArrayOfObj[i++]);
 
                 for (int l = 0; l < astring.length; ++l){
                     String s1 = astring[l];
                     ++k;
                     j = s1.length();
                     s = s + s1;
                 }
             }
             else{
                 while (par2ArrayOfObj[i] instanceof String){
                     String s2 = (String)par2ArrayOfObj[i++];
                     ++k;
                     j = s2.length();
                     s = s + s2;
                 }
             }
 
             HashMap hashmap;
 
             for (hashmap = new HashMap(); i < par2ArrayOfObj.length; i += 2){
                 Character character = (Character)par2ArrayOfObj[i];
                 Object itemstack1 = null;
 
                 if (par2ArrayOfObj[i + 1] instanceof Item){
                     itemstack1 = new ItemStack((Item)par2ArrayOfObj[i + 1]);
                 }
                 else if (par2ArrayOfObj[i + 1] instanceof Block){
                     itemstack1 = new ItemStack((Block)par2ArrayOfObj[i + 1], 1, 32767);
                 }
                 else if (par2ArrayOfObj[i + 1] instanceof ItemStack){
                     itemstack1 = (ItemStack)par2ArrayOfObj[i + 1];
                 }
                 else if (par2ArrayOfObj[i + 1] instanceof String){
                     itemstack1 = (String)par2ArrayOfObj[i + 1];
                 }
 
                 hashmap.put(character, itemstack1);
             }
 
             Object[] aitemstack = new Object[j * k];
 
             for (int i1 = 0; i1 < j * k; ++i1){
                 char c0 = s.charAt(i1);
 
                 if (hashmap.containsKey(Character.valueOf(c0))){
                     aitemstack[i1] = hashmap.get(Character.valueOf(c0));
                 }
                 else{
                     aitemstack[i1] = null;
                 }
             }
 
            if(oredict){ 
                 ThesisOreDictRecipeShaped recipe = new ThesisOreDictRecipeShaped(par1ItemStack, ThesisName, par2ArrayOfObj);
                 GameRegistry.addRecipe(recipe);
            } else {
                ItemStack[] bitemstack = new ItemStack[j * k];
                boolean NoErr = true;
                for(int o = 0; o < aitemstack.length; ++o){
                   if(aitemstack[o] instanceof ItemStack){
                       bitemstack[o] = (ItemStack) aitemstack[o];
                   } else {
                       NoErr = false;
                       LogHelper.log(Level.INFO, " Error in Recipe, will not be added. Make sure that this isn't an Ore Dictionary Recipe, if it is use the Ore Dict loader not the standard loader");
                   }
                }
                if(NoErr){
                    ThesisRecipeShaped recipe = new ThesisRecipeShaped(j, k, bitemstack, par1ItemStack, ThesisName);
                    GameRegistry.addRecipe(recipe);
                }
             }
             addRecipeTag(xmlTag, j, k, aitemstack, par1ItemStack);
         }
         
         public static void addRecipeTag(String xmlTag, int width, int height, Object[] aitemstack, ItemStack result){
             xmlCraftingReference.put(xmlTag, new XmlRecipieReference(width, height, aitemstack, result));
         }
         
         public static void addTextToXMLRecipie(String recipie, String text){
             XmlRecipieReference obj = xmlCraftingReference.get(recipie);
             obj.setText(text);
             xmlCraftingReference.put(recipie, obj);
         }
         
     /*Item Methods*/ 
         public static void addTheoremToItem(ItemStack iStack, Thesis thesis, Theorem theorem){
             if (!iStack.stackTagCompound.hasKey(Strings.THESIS)) {
                 iStack.stackTagCompound.setCompoundTag(Strings.THESIS, new NBTTagCompound());
             }
             if(!iStack.stackTagCompound.getCompoundTag(Strings.THESIS).hasKey(thesis.getName())){
                 iStack.stackTagCompound.getCompoundTag(Strings.THESIS).setTag(thesis.getName(), new NBTTagList());
             }
             if (iStack.stackTagCompound.hasKey(Strings.UsedDriveSpace)) {
                 iStack.stackTagCompound.setInteger(Strings.UsedDriveSpace, iStack.stackTagCompound.getInteger(Strings.UsedDriveSpace) + theorem.getSize());
             }
             NBTTagCompound n = new NBTTagCompound();
             n.setString(Strings.KEY, theorem.getName());    
             iStack.stackTagCompound.getCompoundTag(Strings.THESIS).getTagList(thesis.getName()).appendTag(n);
         }
         
     /*XML*/
         /*Loads the xml files which control the user output data for discoveries, used when mod loads*/
         public void loadFromXML(String xml) {
             try {
                 URL url = this.getClass().getResource(xml);
                 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                 Document doc = dBuilder.parse(url.openStream());
                 doc.getDocumentElement().normalize();
                 NodeList nList = doc.getElementsByTagName(Strings.THESIS);
     
                 for (int i = 0; i < nList.getLength(); i++) {
                     Element nNode = (Element) nList.item(i);
                     ThesisXMLMap.put(nNode.getAttribute(Strings.X_ID).toString(), nNode);
                 }
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         public static Element getNode(String name) {
             return ThesisXMLMap.get(name);
     
         }
         
         public static String getThesisName(String thesis) {
             Element nNode = ThesisSystem.getNode(thesis);
             return nNode.getElementsByTagName(Strings.X_NAME).item(0).getTextContent();
         }
     
         public static String getTheoremName(String thesis, String theorem) {
             Element nNode = ThesisSystem.getNode(thesis);
             NodeList nList = nNode.getElementsByTagName(Strings.THEOREM);
             
             String ret = "Err";
             for (int i = 0; i < nList.getLength(); i++) {
                 Element nnNode = (Element) nList.item(i);
                 if(nnNode.getAttribute(Strings.X_ID).toString().equals(theorem)){
                    ret = nnNode.getElementsByTagName(Strings.X_NAME).item(0).getTextContent();
                    return ret;
                 }
             }
             return ret;
         }
     
         public static NodeList getTheoremPageNodeList(String thesis, String theorem) {
             Element nNode = ThesisSystem.getNode(thesis);
             NodeList nList = nNode.getElementsByTagName(Strings.THEOREM);
             
             NodeList ret = null;
             for (int i = 0; i < nList.getLength(); i++) {
                 Element nnNode = (Element) nList.item(i);
                 if(nnNode.getAttribute(Strings.X_ID).toString().equals(theorem)){
                    ret = nnNode.getElementsByTagName(Strings.X_PAGE);
                    return ret;
                 }
             }
             return ret;
         }
 }
