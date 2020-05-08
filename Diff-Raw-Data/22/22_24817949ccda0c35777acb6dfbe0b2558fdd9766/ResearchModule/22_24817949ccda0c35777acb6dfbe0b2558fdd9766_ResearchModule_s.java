 package mod.industrialscience.modules;
 
 import java.util.Hashtable;
 import java.util.logging.Level;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import mod.industrialscience.ResearchManager;
 import mod.industrialscience.modules.research.*;
 
 public class ResearchModule extends ISAbstractModule {
 	public Block researchdesk;
 	public int researchdeskid;
 	public Item researchbook;
 	public int researchbookid;
 	public Block copier;
 	public int copierid;
 	public void load() {
 		logger.log(Level.INFO, "LOADING");
 		
 		//Research Desk
 		GameRegistry.registerBlock(researchdesk);
 		GameRegistry.registerTileEntity(ResearchDeskTile.class, "Research-Desk");
		LanguageRegistry.addName(researchdesk, "Research Table");
 		researchdesk.setCreativeTab(CreativeTab);
 		GameRegistry.addRecipe(new ShapedOreRecipe(researchdesk, new Object[]{"WWW","S S","S S",Character.valueOf('W'), "slabWood",Character.valueOf('S'),"stickWood"}));
 		//Researchbook
 		researchbook.setCreativeTab(CreativeTab);
 		GameRegistry.addRecipe(new ShapelessOreRecipe(researchbook, new Object[]{Item.book,"dyeLime", "dyeLime"}));
 		//Researchcopier
 		copier.setCreativeTab(CreativeTab);
 		GameRegistry.registerBlock(copier);
		LanguageRegistry.addName(copier, "Research Copier");
 		GameRegistry.registerTileEntity(CopierTile.class, "Research-Copier");
 		
 		addresearches();
 	}
 	@Override
 	public void init() {
 		initCreativeTab();
 		logger.log(Level.INFO, "INIT");
 		copier= new Copier(BlockIDs.get("researchcopier"));
 		copierid=BlockIDs.get("researchcopier");
 		researchdesk=new ResearchDesk(BlockIDs.get("researchdesk"));
 		researchdeskid=BlockIDs.get("researchdesk");
 		researchbook = new ResearchBook(ItemIDs.get("researchbook"));
 		researchbookid = ItemIDs.get("researchbook");
 		
 	}
 	private void addresearches() {
 		
 	}
 	@Override
 	public Hashtable<String, Integer> getNeededBlockIDs() {		
 		Hashtable<String, Integer> neededBlockIDs = new Hashtable<String, Integer>();
 		neededBlockIDs.put("researchdesk", 756);
 		neededBlockIDs.put("researchcopier", 757);
 		return neededBlockIDs;
 	}
 	public Hashtable<String, Integer> getNeededItemIDs() {
 		Hashtable<String, Integer> neededItemIDs = new Hashtable<String, Integer>();
 		neededItemIDs.put("researchbook", 8123);
 		return neededItemIDs;
 	}
 	@Override
 	public void postinit() {
 		logger.log(Level.INFO, "POST-INIT");
 		ResearchManager.getInstance().disable();
 		ResearchManager.getInstance().loadResearches();
 		
 	}
 	@Override
 	public String getName() {
 		return "Research-Module-IS";
 	}
 	@Override
 	public ItemStack getIconitemstack() {
 		return new ItemStack(researchbook);
 	}
 
 }
