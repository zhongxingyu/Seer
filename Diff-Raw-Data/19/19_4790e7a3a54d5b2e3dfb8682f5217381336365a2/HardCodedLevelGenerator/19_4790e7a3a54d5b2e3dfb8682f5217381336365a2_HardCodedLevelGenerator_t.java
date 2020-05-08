 /**
  * @author Michael Zhou (Dominator008), Siyang Chen
  */
 package game;
 
 import java.awt.Point;
 import java.util.Map;
 import java.util.HashMap;
 import leveleditor.VoogaUtilities;
 import com.golden.gamedev.object.Sprite;
 
 
 public class HardCodedLevelGenerator
 {
     public static void main (String[] args)
     {
         String herosrc = "resources/W-Gundam.png";
         String badguysrc = "resources/Mario1.png";
         Sprite hero = new Sprite(VoogaUtilities.getImageFromString(herosrc));
         Sprite badguy =
             new Sprite(VoogaUtilities.getImageFromString(badguysrc));
        String background = "resources/mario2s.gif";
         Map<Point, SpriteWrapper> spritemap =
             new HashMap<Point, SpriteWrapper>();
        spritemap.put(new Point(0, 0), new SpriteWrapper(hero, "W-Gundam", herosrc));
        spritemap.put(new Point(0, 60), new SpriteWrapper(badguy, "Mario", badguysrc));
        spritemap.put(new Point(20, 200), new SpriteWrapper(badguy, "Mario", badguysrc));
        spritemap.put(new Point(373, 238), new SpriteWrapper(badguy, "Mario", badguysrc));
         LevelState lv1 = new LevelState(background, spritemap);
         lv1.save("saves/level1.lvl");
     }
 }
