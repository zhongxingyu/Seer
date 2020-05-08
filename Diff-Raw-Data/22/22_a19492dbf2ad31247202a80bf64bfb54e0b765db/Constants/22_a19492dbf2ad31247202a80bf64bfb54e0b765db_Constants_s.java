 /*
  * Copyright (c) 2012. Redex Scripting - Unauthorized use prohibited by author.
  */
 
 package RedexAlter;
 
import org.omg.CORBA.PUBLIC_MEMBER;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Tile;
 
 /**
  * Created by IntelliJ IDEA.
  * User: 0xe9
  * Date: 6/23/12
  * Time: 4:46 PM
  */
 public class Constants {
 
     public static final int[] GILDED_ALTER = {13197, 13199, 13198, 13188, 13191};
     public static final int[] PORTAL = {15531, 13405};
    
    public static final Area YANILLE_PORTAL = new Area(new Tile[] {
             new Tile(2539, 3101, 0), new Tile(2539, 3094, 0),
             new Tile(2545, 3091, 0), new Tile(2546, 3099, 0)
     });
 
    public static final Area YANNILE_PORTAL = new Area(new Tile[] {
             new Tile(2607, 3097, 0), new Tile(2608, 3088, 0),
             new Tile(2616, 3088, 0), new Tile(2616, 3098, 0)
     });
 }
