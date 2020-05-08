 package game;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import loader.data.json.CampaignData;
 import loader.data.json.LevelData;
 import loader.parser.ParserException;
 import main.Launcher;
 import other.CacheTool;
 
 public class GameCampaign {
   private final CampaignData     campaign;
   private LevelData              currentLevel;
   private final Iterator<String> iterator;
 
   public GameCampaign(final CampaignData data) throws ParserException, IOException {
     campaign = data;
 
     iterator = campaign.levels.iterator();
   }
 
   public void nextLevel() throws ParserException, IOException {
    if (iterator.hasNext()) {
      currentLevel = CacheTool.getLevel(Launcher.cache, iterator.next());
    }
   }
 
   public LevelData getCurrentLevel() {
     return currentLevel;
   }
 
   public boolean hasMoreLevels() {
     return iterator.hasNext();
   }
 }
