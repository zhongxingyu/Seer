 package mod.industrialscience.modules;
 
 import java.util.Hashtable;
 import java.util.logging.Level;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import mod.industrialscience.ResearchManager;
 import mod.industrialscience.modules.research.*;
 import mod.industrialscience.modules.research.backend.model.*;
 
 public class ResearchModule extends ISAbstractModule {
 	public static Block researchdesk;
 	public static int researchdeskid;
 	
 	public static Item researchbook;
 	public static int researchbookid;
 	
 	public static Block copier;
 	public static int copierid;
 	
 	public static Item researchNote;
 	public static int researchNoteid;
 	public void load() {
 		logger.log(Level.INFO, "LOADING");
 		
 		//Research Desk
 		GameRegistry.registerBlock(researchdesk,researchdesk.getUnlocalizedName());
 		GameRegistry.registerTileEntity(ResearchDeskTile.class, "Research-Desk");
 		researchdesk.setCreativeTab(CreativeTab);
 		GameRegistry.addRecipe(new ShapedOreRecipe(researchdesk, new Object[]{"WWW","S S","S S",Character.valueOf('W'), "slabWood",Character.valueOf('S'),"stickWood"}));
 		
 		//Researchbook
 		researchbook.setCreativeTab(CreativeTab);
 		GameRegistry.addRecipe(new ShapelessOreRecipe(researchbook, new Object[]{Item.book,"dyeLime", "dyeLime"}));
 		
 		//Researchcopier
 		copier.setCreativeTab(CreativeTab);
 		GameRegistry.registerBlock(copier, copier.getUnlocalizedName());
 		GameRegistry.registerTileEntity(CopierTile.class, "Research-Copier");
 		
 		//Reseach Note
 		researchNote.setCreativeTab(CreativeTab);
 		
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
 		researchNote= new ResearchNote(ItemIDs.get("researchnote"));
 		researchNoteid=ItemIDs.get("researchnote");
 		
 	}
 	private void addresearches() {
 		try {
		//	ResearchManager.getInstance().registerResearch(new Researchfactory("Research", new DefaultLocker(new Object[]{"XXX","YXY","XXX",Character.valueOf('X'),Block.planks}, new ItemStack(copier)), new DefaultChecker()).getResearch("Copying", null, new Researchstep[]{new Researchstep(0, new ResearchObject(new ItemStack(Item.paper)), "I'm working on a way to duplicate infomation. Paper is very important for it."), new Researchstep(1, new ResearchObject(new ItemStack(Item.dyePowder, 1, 0)), "I also need some ink to write the infomation down, but I can't use my fingers...")}));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
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
 		neededItemIDs.put("researchnote", 8124);
 		return neededItemIDs;
 	}
 	@Override
 	public void postinit() {
 		logger.log(Level.INFO, "POST-INIT");
 		
 	}
 	@Override
 	public String getName() {
 		return "Research Module";
 	}
 	@Override
 	public ItemStack getIconitemstack() {
 		return new ItemStack(researchbook);
 	}
 
 }
