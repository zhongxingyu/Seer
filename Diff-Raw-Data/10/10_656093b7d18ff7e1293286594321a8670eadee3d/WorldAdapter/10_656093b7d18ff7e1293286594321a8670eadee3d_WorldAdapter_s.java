 package dungeon.load.adapters;
 
 import dungeon.models.Level;
 import dungeon.models.Player;
 import dungeon.models.World;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import java.util.ArrayList;
 import java.util.List;
 
 @XmlRootElement(name = "world")
 public class WorldAdapter extends XmlAdapter<WorldAdapter, World> {
   @XmlElement(name = "level")
   @XmlJavaTypeAdapter(LevelAdapter.class)
   public List<Level> levels = new ArrayList<>();
 
   @XmlJavaTypeAdapter(PlayerAdapter.class)
  public Player player;
 
   @Override
   public World unmarshal (WorldAdapter worldAdapter) throws Exception {
    return new World(worldAdapter.levels, worldAdapter.player);
   }
 
   @Override
   public WorldAdapter marshal (World world) {
     WorldAdapter adapter = new WorldAdapter();
     adapter.levels = world.getLevels();
    adapter.player = world.getPlayer();
 
     return adapter;
   }
 }
