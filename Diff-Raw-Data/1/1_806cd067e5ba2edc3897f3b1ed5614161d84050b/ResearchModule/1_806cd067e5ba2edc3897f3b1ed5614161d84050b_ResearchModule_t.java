 package mod.industrialscience.modules;
 
 import java.util.Hashtable;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import mod.industrialscience.ResearchManager;
 import mod.industrialscience.modules.research.*;
 
 public class ResearchModule extends ISAbstractModule {
 	public Block researchdesk;
 	public int researchdeskid;
 	public Item researchbook;
 	public int researchbookid;
 	public void load() {
 		GameRegistry.registerBlock(researchdesk);
		GameRegistry.registerTileEntity(ResearchDeskTile.class, "Research-Desk");
 		LanguageRegistry.addName(researchdesk, "Research Table");
 
 	}
 	@Override
 	public void init() {
 		researchdesk=new ResearchDesk(BlockIDs.get("researchdesk"));
 		researchdeskid=BlockIDs.get("researchdesk");
 		researchbook = new ResearchBook(ItemIDs.get("researchbook"));
 		researchbookid = ItemIDs.get("researchbook");
 		
 	}
 	@Override
 	public Hashtable<String, Integer> getNeededBlockIDs() {		
 		Hashtable<String, Integer> blub = new Hashtable<String, Integer>();
 		blub.put("researchdesk", 756);
 		return blub;
 	}
 	public Hashtable<String, Integer> getNeededItemIDs() {
 		Hashtable<String, Integer> blub = new Hashtable<String, Integer>();
 		blub.put("researchbook", 8123);
 		return blub;
 	}
 	@Override
 	public void postinit() {
 		ResearchManager.getInstance().disable();
 		ResearchManager.getInstance().loadResearches();
 		
 	}
 	@Override
 	public String getName() {
 		return "Research-Module-IS";
 	}
 
 }
