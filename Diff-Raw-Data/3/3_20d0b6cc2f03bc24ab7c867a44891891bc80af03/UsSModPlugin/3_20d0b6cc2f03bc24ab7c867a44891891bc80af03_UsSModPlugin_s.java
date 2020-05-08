 package data.scripts;
 
 import com.fs.starfarer.api.BaseModPlugin;
 import com.fs.starfarer.api.Global;
 import com.fs.starfarer.api.campaign.SectorAPI;
import data.scripts.uss.UsSEveryFrame;
 import data.scripts.uss.UsSCoreCampaignPlugin;
 
 public class UsSModPlugin extends BaseModPlugin
 {
     private static void initUsS()
     {
         UsSEveryFrame script = new UsSEveryFrame();
         SectorAPI sector = Global.getSector();
         sector.addScript(script);
     }
 
     @Override
     public void onNewGame()
     {
     	Global.getSector().registerPlugin(new UsSCoreCampaignPlugin());
     	initUsS();
     }
 
     //@Override
     //public void onEnabled(boolean wasEnabledBefore)
     //{
     //    if (!wasEnabledBefore)
     //    {
     //    	initUsS();
     //    }
     //}
 }
