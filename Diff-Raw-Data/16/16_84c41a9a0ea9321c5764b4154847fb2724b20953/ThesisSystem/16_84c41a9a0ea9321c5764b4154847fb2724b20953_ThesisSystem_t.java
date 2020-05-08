 package com.isocraft.thesis;
 
 import java.io.IOException;
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
 
 import com.isocraft.core.handlers.ISOCraftThrowHandler;
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.item.crafting.ThesisOreDictRecipeShaped;
 import com.isocraft.item.crafting.ThesisOreDictRecipeShapeless;
 import com.isocraft.item.crafting.ThesisRecipeShaped;
 import com.isocraft.item.crafting.ThesisRecipeShapeless;
 import com.isocraft.item.crafting.XmlRecipeReference;
 import com.isocraft.lib.ItemInfo;
 import com.isocraft.lib.Reference;
 import com.isocraft.lib.Strings;
 import com.isocraft.network.PacketTypeHandler;
 import com.isocraft.network.packets.PacketSendClientPing;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 /**
  * ISOCraft
  * 
  * Class for managing the Thesis System
  * 
  * @author Turnermator13
  */
 
 public class ThesisSystem {
 
 	/** List of all of the Theses activated in the mod instance */
 	public static final List<Thesis> ThesisList = new ArrayList<Thesis>();
 
 	/** Alphabetically ordered list of thesis names */
 	public static final Collection<String> ThesisOrderedList = new TreeSet<String>(Collator.getInstance());
 
 	/** List of all of the Theses that are designed to be preloaded */
 	public static final List<Thesis> PreloadThesisList = new ArrayList<Thesis>();
 
 	/* XML map */
 	/** Stores XML elements */
 	private static final HashMap<String, Element> ThesisXMLMap = new HashMap<String, Element>();
 
 	/* Crafting stuff */
 	/**
 	 * If block id is present when a player right clicks on a block with the id it will sync entity data ready for crafting
 	 */
 	public static final List<Integer> WhitelistPlayerDataSync = new ArrayList<Integer>();
 
 	/**
 	 * Stores updated player thesis unlock states client and server side, used for checking crafting is allowed
 	 */
 	public static final HashMap<String, PlayerStateData> PlayerNBTDataUpdate = new HashMap<String, PlayerStateData>();
 
 	/**
 	 * Stores reference channels
 	 */
 	public static final HashMap<Integer, String> CraftingChannels = new HashMap<Integer, String>();
 
 	/**
 	 * Stores the tags which are referred to in xml files for each recipe added, all isocraft recipies are added a standard
 	 */
 	public static final HashMap<String, XmlRecipeReference> xmlCraftingReference = new HashMap<String, XmlRecipeReference>();
 
 	// ###########################################################################################################
 	private static final ThesisSystem INSTANCE = new ThesisSystem();
 
 	public static ThesisSystem instance() {
 		return INSTANCE;
 	}
 
 	/* Thesis Management Methods */
 	// ##########################################################################################################
 
 	/**
 	 * Registers Thesis for use with ISOCraft with test for duplicates, WILL throw an exception if a duplicate is found
 	 * 
 	 * @param obj
 	 *            thesis
 	 */
 	public static void addThesis(Thesis obj) {
 		obj.init();
 
 		boolean flag = false;
 		for (int i = 0; i < ThesisList.size(); ++i) {
 			String tmp = ThesisList.get(i).getReference();
 			if (tmp.equals(obj.getReference())) {
 				flag = true;
 			}
 		}
 
 		if (!flag) {
 			ThesisList.add(obj);
 			ThesisOrderedList.add(obj.getReference());
 			if (obj.isPreLoad()) {
 				PreloadThesisList.add(obj);
 			}
 		}
 		else {
 			ISOCraftThrowHandler.duplicateThesisThrow(obj.getReference());
 		}
 	}
 
 	/**
 	 * Fetches a thesis from its reference
 	 * 
 	 * @param ref
 	 *            Reference
 	 * @return Thesis with passed reference
 	 */
 	public static Thesis getThesisFromReference(String ref) {
 		Thesis ret = null;
 
 		for (int i = 0; i < ThesisList.size(); ++i) {
 			if (ThesisList.get(i).getReference().equals(ref)) {
 				ret = ThesisList.get(i);
 			}
 		}
 
 		return ret;
 	}
 
 	/* Player NBT Methods */
 	// ##########################################################################################################
 
 	/**
 	 * Sets up Player NBT Data, only called when player first logs in
 	 * 
 	 * @param obj
 	 *            player nbt location
 	 */
 	public static void createPlayerSetup(NBTTagCompound obj) {
 		for (int i = 0; i < ThesisList.size(); ++i) {
 			obj.setCompoundTag(ThesisList.get(i).getReference(), new NBTTagCompound());
 
 			NBTTagCompound tag = obj.getCompoundTag(ThesisList.get(i).getReference());
 			tag.setTag(Strings.Theorem_name, new NBTTagList());
 
 			/*
 			 * NBTTagCompound n = new NBTTagCompound(); n.setString("KEY", "TEST"); tag.getTagList(Strings.THEOREM).appendTag(n); -- adds a theorum to a thesis nbt
 			 */
 		}
 	}
 
 	/**
 	 * Append new thesis to player nbt, making updates forward compatible
 	 * 
 	 * @param obj
 	 *            Player nbt location
 	 */
 	public static void appendPlayerSetup(NBTTagCompound obj) {
 		for (int i = 0; i < ThesisList.size(); ++i) {
 			if (!obj.hasKey(ThesisList.get(i).getReference())) {
 				LogHelper.log(Level.INFO, " New Thesis '" + ThesisList.get(i).getReference() + "' Found, Appending to NBT data for Player");
 				obj.setCompoundTag(ThesisList.get(i).getReference(), new NBTTagCompound());
 
 				NBTTagCompound tag = obj.getCompoundTag(ThesisList.get(i).getReference());
 				tag.setTag(Strings.Theorem_name, new NBTTagList());
 			}
 		}
 
 	}
 
 	/**
 	 * Checks a players nbt for a theorem, if not present adds it, used for theorem unlocks
 	 * 
 	 * @param player
 	 *            Player
 	 */
 	public static void unlockCheck(EntityPlayer player, String thesis, String theorem) {
 		boolean exists = false;
 		NBTTagList l = player.getEntityData().getCompoundTag(Strings.NBT_PlayerPersisted).getCompoundTag(Reference.MOD_ID).getCompoundTag(Strings.Thesis_name).getCompoundTag(thesis).getTagList(Strings.Theorem_name);
 		for (int j = 0; j < l.tagCount(); ++j) {
 			if (((NBTTagCompound) l.tagAt(j)).getString(Strings.NBT_Key).equals(theorem)) {
 				exists = true;
 				break;
 			}
 		}
 
 		if (!exists) {
 			NBTTagCompound n = new NBTTagCompound();
 			n.setString(Strings.NBT_Key, theorem);
 			l.appendTag(n);
 		}
 	}
 
 	/**
 	 * returns a list of all of the players unlock values for each thesis
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public static PlayerStateData getPlayerValues(EntityPlayer player) {
 		PlayerStateData ret = new PlayerStateData(player.username);
 		NBTTagCompound nbt = player.getEntityData().getCompoundTag(Strings.NBT_PlayerPersisted).getCompoundTag(Reference.MOD_ID).getCompoundTag(Strings.Thesis_name);
 
 		for (int i = 0; i < ThesisList.size(); ++i) {
 			NBTTagList l = nbt.getCompoundTag(ThesisList.get(i).getReference()).getTagList(Strings.Theorem_name);
 
 			for (int j = 0; j < l.tagCount(); ++j) {
 				ret.addKey(ThesisList.get(i).getReference(), ((NBTTagCompound) l.tagAt(j)).getString(Strings.NBT_Key));
 			}
 
 			if (ret.PlayerStateData.get(ThesisList.get(i).getReference()) == null) {
 				List<String> tmp = new ArrayList<String>();
 				tmp.add("");
 				ret.PlayerStateData.put(ThesisList.get(i).getReference(), tmp);
 			}
 
 		}
 		return ret;
 	}
 
 	/* Crafting */
 	// ##########################################################################################################
 
 	/**
 	 * Adds a shaped recipe to the register and registers it with the thesis that must be unlocked for it to activate. Once this thesis is found the recipe is unlocked automatically. Supports ore
 	 * dictionary
 	 * 
 	 * @param par1ItemStack
 	 *            Item to Craft
 	 * @param ThesisName
 	 *            Name of thesis that must be unlocked for it the be active
 	 * @param xmlTag
 	 *            Reference Tag
 	 * @param oredict
 	 *            Is recipe an ore dict one?
 	 * @param par2ArrayOfObj
 	 *            Shape of recipe (same as standard minecraft)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static void addShapedRecipe(ItemStack par1ItemStack, String ThesisRef, String TheoremRef, String xmlTag, boolean oredict, Object... par2ArrayOfObj) {
 		String s = "";
 		int i = 0;
 		int j = 0;
 		int k = 0;
 
 		if (par2ArrayOfObj[i] instanceof String[]) {
 			String[] astring = (String[]) par2ArrayOfObj[i++];
 
 			for (String s1 : astring) {
 				++k;
 				j = s1.length();
 				s = s + s1;
 			}
 		}
 		else {
 			while (par2ArrayOfObj[i] instanceof String) {
 				String s2 = (String) par2ArrayOfObj[i++];
 				++k;
 				j = s2.length();
 				s = s + s2;
 			}
 		}
 
 		HashMap hashmap;
 
 		for (hashmap = new HashMap(); i < par2ArrayOfObj.length; i += 2) {
 			Character character = (Character) par2ArrayOfObj[i];
 			Object itemstack1 = null;
 
 			if (par2ArrayOfObj[i + 1] instanceof Item) {
 				itemstack1 = new ItemStack((Item) par2ArrayOfObj[i + 1]);
 			}
 			else if (par2ArrayOfObj[i + 1] instanceof Block) {
 				itemstack1 = new ItemStack((Block) par2ArrayOfObj[i + 1], 1, 32767);
 			}
 			else if (par2ArrayOfObj[i + 1] instanceof ItemStack) {
 				itemstack1 = par2ArrayOfObj[i + 1];
 			}
 			else if (par2ArrayOfObj[i + 1] instanceof String) {
 				itemstack1 = par2ArrayOfObj[i + 1];
 			}
 
 			hashmap.put(character, itemstack1);
 		}
 
 		Object[] aitemstack = new Object[j * k];
 
 		for (int i1 = 0; i1 < j * k; ++i1) {
 			char c0 = s.charAt(i1);
 
 			if (hashmap.containsKey(Character.valueOf(c0))) {
 				aitemstack[i1] = hashmap.get(Character.valueOf(c0));
 			}
 			else {
 				aitemstack[i1] = null;
 			}
 		}
 
 		if (oredict) {
 			ThesisOreDictRecipeShaped recipe = new ThesisOreDictRecipeShaped(par1ItemStack, ThesisRef, TheoremRef, par2ArrayOfObj);
 			GameRegistry.addRecipe(recipe);
 		}
 		else {
 			ItemStack[] bitemstack = new ItemStack[j * k];
 			boolean NoErr = true;
 			for (int o = 0; o < aitemstack.length; ++o) {
 				if (aitemstack[o] instanceof ItemStack) {
 					bitemstack[o] = (ItemStack) aitemstack[o];
 				}
 				else {
 					NoErr = false;
 					LogHelper.log(Level.INFO, " Error in Recipe, will not be added. Make sure that this isn't an Ore Dictionary Recipe, if it is use the Ore Dict loader not the standard loader");
 				}
 			}
 			if (NoErr) {
 				ThesisRecipeShaped recipe = new ThesisRecipeShaped(j, k, bitemstack, par1ItemStack, ThesisRef, TheoremRef);
 				GameRegistry.addRecipe(recipe);
 			}
 		}
 		addRecipeTag(xmlTag, j, k, aitemstack, par1ItemStack);
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static void addShapelessRecipe(ItemStack par1ItemStack, String ThesisRef, String TheoremRef, String xmlTag, boolean oredict, Object... par2ArrayOfObj) {
 		ArrayList arraylist = new ArrayList();
 		Object[] aobject = par2ArrayOfObj;
 		int i = par2ArrayOfObj.length;
 
 		for (int j = 0; j < i; ++j) {
 			Object object1 = aobject[j];
 
 			if (object1 instanceof ItemStack) {
 				arraylist.add(((ItemStack) object1).copy());
 			}
 			else if (object1 instanceof Item) {
 				arraylist.add(new ItemStack((Item) object1));
 			}
 			else if (object1 instanceof Block) {
 				arraylist.add(new ItemStack((Block) object1));
 			}
 			else if (object1 instanceof String) {
 				arraylist.add(object1);
 			}
 			else {
 				throw new RuntimeException("Invalid shapeless recipe!");
 			}
 		}
 
 		if (oredict) {
 			ThesisOreDictRecipeShapeless recipe = new ThesisOreDictRecipeShapeless(par1ItemStack, ThesisRef, TheoremRef, par2ArrayOfObj);
 			GameRegistry.addRecipe(recipe);
 		}
 		else {
 			boolean NoErr = true;
 			for (int o = 0; o < arraylist.size(); ++o) {
 				if (!(arraylist.get(i) instanceof ItemStack)) {
 					NoErr = false;
 					LogHelper.log(Level.INFO, " Error in Recipe, will not be added. Make sure that this isn't an Ore Dictionary Recipe, if it is use the Ore Dict loader not the standard loader");
 				}
 			}
 			if (NoErr) {
 				ThesisRecipeShapeless recipe = new ThesisRecipeShapeless(par1ItemStack, arraylist, ThesisRef, TheoremRef);
 				GameRegistry.addRecipe(recipe);
 			}
 		}
 
 		int j = arraylist.size();
 		if (j > 3) {
 			j = 3;
 		}
 		int k = (int) Math.ceil((double) arraylist.size() / 3);
 
 		addRecipeTag(xmlTag, j, k, aobject, par1ItemStack);
 	}
 
 	public static void addRecipeTag(String xmlTag, int width, int height, Object[] aitemstack, ItemStack result) {
 		xmlCraftingReference.put(xmlTag, new XmlRecipeReference(width, height, aitemstack, result));
 	}
 
 	
 	/* Item Methods */
 	// ##########################################################################################################
 
 	/**
 	 * Copys a theorem to an itemstack, taking into account its drive size when applicable, also checks for duplicate theorems and will not copy if one is detected
 	 * 
 	 * @param baseStack
 	 *            itemstack to add theorem to
 	 * @param thesis
 	 *            thesis that the theorem to be added is in
 	 * @param theorem
 	 *            theorem to be added
 	 * @return itemstack with theorem added to its nbt
 	 */
 	public static ItemStack addTheoremToItem(ItemStack baseStack, Thesis thesis, Theorem theorem) {
 		ItemStack iStack = baseStack.copy();
 
 		if (iStack.stackTagCompound == null) {
 			iStack.stackTagCompound = new NBTTagCompound();
 		}
 
 		if (!iStack.stackTagCompound.hasKey(Strings.Thesis_name)) {
 			iStack.stackTagCompound.setCompoundTag(Strings.Thesis_name, new NBTTagCompound());
 		}
 
 		if (!iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).hasKey(thesis.getReference())) {
 			iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).setTag(thesis.getReference(), new NBTTagList());
 		}
 
 		NBTTagCompound n = new NBTTagCompound();
 		n.setString(Strings.NBT_Key, theorem.getReference());
 
 		for (int i = 0; i < iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).tagCount(); ++i) {
 			if (iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).tagAt(i).equals(n)) {
 				return baseStack;
 			}
 		}
 
 		iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).appendTag(n);
 
 		if (iStack.stackTagCompound.hasKey(Strings.UsedDriveSpace)) {
 			if (iStack.stackTagCompound.getInteger(Strings.UsedDriveSpace) + theorem.getSize() <= iStack.stackTagCompound.getInteger(Strings.DriveSize)) {
 				iStack.stackTagCompound.setInteger(Strings.UsedDriveSpace, iStack.stackTagCompound.getInteger(Strings.UsedDriveSpace) + theorem.getSize());
 			}
 			else {
 				return baseStack;
 			}
 		}
 		return iStack;
 	}
 
 	/**
 	 * Deletes a theorem to an itemstack, taking into account its drive size when applicable
 	 * 
 	 * @param baseStack
 	 *            itemstack to delete theorem from
 	 * @param thesis
 	 *            thesis that the theorem to be deleted is in
 	 * @param theorem
 	 *            theorem to be deleted
 	 * @return itemstack with theorem deleted from its nbt
 	 */
 	public static ItemStack deleteTheoremFromItem(ItemStack baseStack, Thesis thesis, Theorem theorem) {
 		ItemStack iStack = baseStack.copy();
 
 		if (iStack.stackTagCompound == null || !iStack.stackTagCompound.hasKey(Strings.Thesis_name) || !iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).hasKey(thesis.getReference())) {
 			return baseStack;
 		}
 		else {
 			boolean flag = false;
 
 			for (int i = 0; i < iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).tagCount(); ++i) {
 				NBTTagCompound comp = (NBTTagCompound) iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).tagAt(i);
 				if (comp.getString(Strings.NBT_Key).equals(theorem.getReference())) {
 					iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).removeTag(i);
 					flag = true;
 					break;
 				}
 			}
 
 			if (!flag) {
 				LogHelper.log(Level.SEVERE, " Error - No Matching tag");
 				return baseStack;
 			}
 
 			if (iStack.stackTagCompound.hasKey(Strings.UsedDriveSpace)) {
 				if (iStack.stackTagCompound.getInteger(Strings.UsedDriveSpace) - theorem.getSize() >= 0) {
 					iStack.stackTagCompound.setInteger(Strings.UsedDriveSpace, iStack.stackTagCompound.getInteger(Strings.UsedDriveSpace) - theorem.getSize());
 				}
 				else {
 					LogHelper.log(Level.SEVERE, " Error - Negative Drive Space");
 					return baseStack;
 				}
 			}
 
 			if (iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).getTagList(thesis.getReference()).tagCount() == 0) {
 				iStack.stackTagCompound.getCompoundTag(Strings.Thesis_name).removeTag(thesis.getReference());
 			}
 
 			if (iStack.itemID == ItemInfo.PDA_id + Reference.idDisplacement) {
 				iStack.stackTagCompound.setInteger("scroll1", 0);
 				iStack.stackTagCompound.setInteger("scroll2", 0);
 				iStack.stackTagCompound.setInteger("scroll3", 0);
 				iStack.stackTagCompound.setInteger("stage", 0);
 				iStack.stackTagCompound.setInteger("tab", 1);
 				iStack.stackTagCompound.setString("thesisSelected", "*");
 				iStack.stackTagCompound.removeTag("lastSelection1");
 				iStack.stackTagCompound.removeTag("lastSelection2");
 			}
 
 			return iStack;
 		}
 	}
 
 	/* XML */
 	// ##########################################################################################################
 
 	/**
 	 * Loads the XML files containing the information from each thesis/theorem ect which is used for example in gui's (see pda gui for example)
 	 * 
 	 * @param xml
 	 *            file location (/assets/yourmodid/ect....)
 	 */
 	public void loadFromXML(String xml) {
 		try {
 			URL url = this.getClass().getResource(xml);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(url.openStream());
 			doc.getDocumentElement().normalize();
 			NodeList nList = doc.getElementsByTagName(Strings.Thesis_name);
 
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
 
 	/**
 	 * Fetches a thesis full name from its relevant xml file
 	 * 
 	 * @param thesis
 	 *            reference of thesis
 	 * @return name of thesis shown to player
 	 * @throws IOException
 	 */
 	public static String getThesisName(String thesis) throws IOException {
 		Element nNode = ThesisSystem.getNode(thesis);
 		return nNode.getElementsByTagName(Strings.X_NAME).item(0).getTextContent();
 	}
 
 	/**
 	 * Fetches a theorems full name from its relevant xml file
 	 * 
 	 * @param thesis
 	 *            reference of thesis
 	 * @param theorem
 	 *            reference of theorem
 	 * @return actual name of theorem shown to player
 	 * @throws IOException
 	 */
 	public static String getTheoremName(String thesis, String theorem) throws IOException {
 		Element nNode = ThesisSystem.getNode(thesis);
 		NodeList nList = nNode.getElementsByTagName(Strings.Theorem_name);
 
 		String ret = "Err";
 		for (int i = 0; i < nList.getLength(); i++) {
 			Element nnNode = (Element) nList.item(i);
 			if (nnNode.getAttribute(Strings.X_ID).toString().equals(theorem)) {
 				ret = nnNode.getElementsByTagName(Strings.X_NAME).item(0).getTextContent();
 				return ret;
 			}
 		}
 		return ret;
 	}
 
 	public static NodeList getTheoremPageNodeList(String thesis, String theorem) {
 		Element nNode = ThesisSystem.getNode(thesis);
 		NodeList nList = nNode.getElementsByTagName(Strings.Theorem_name);
 
 		NodeList ret = null;
 		for (int i = 0; i < nList.getLength(); i++) {
 			Element nnNode = (Element) nList.item(i);
 			if (nnNode.getAttribute(Strings.X_ID).toString().equals(theorem)) {
 				ret = nnNode.getElementsByTagName(Strings.X_PAGE);
 				return ret;
 			}
 		}
 		return ret;
 	}
 	
 	
 	// Idea Methods
 	// ##########################################################################################################
 
 	public static void sendResearchPointsToClient(int rPoints, Player player){
 		PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketSendClientPing(0, rPoints)), (Player) player);
 		LogHelper.log(Level.INFO, "rPoints:" + rPoints);
 	}
 	
 	public static void incrementResearchPoints(EntityPlayer player){
		int rPoints = player.getEntityData().getCompoundTag(Strings.NBT_PlayerPersisted).getCompoundTag(Reference.MOD_ID).getInteger(Strings.NBT_ResearchPoints);
 		if (rPoints < 99){
 			++rPoints;
			player.getEntityData().getCompoundTag(Strings.NBT_PlayerPersisted).getCompoundTag(Reference.MOD_ID).setInteger(Strings.NBT_ResearchPoints, rPoints);
 			sendResearchPointsToClient(rPoints, (Player) player);
 		}
 	}
 	
	public static void decrementResearchPoints(EntityPlayer player, int noPoints){
		int rPoints = player.getEntityData().getCompoundTag(Reference.MOD_ID).getCompoundTag(Strings.NBT_PlayerPersisted).getInteger(Strings.NBT_ResearchPoints);
		if ((rPoints - noPoints) >= 0){
			rPoints = rPoints - noPoints;
			player.getEntityData().getCompoundTag(Strings.NBT_PlayerPersisted).getCompoundTag(Reference.MOD_ID).setInteger(Strings.NBT_ResearchPoints, rPoints);
 			sendResearchPointsToClient(rPoints, (Player) player);
 		}
 	}
 }
