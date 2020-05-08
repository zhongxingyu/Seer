 package mx.x10.divinescripts.scalegrabber.utils;
 
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.map.TilePath;
 
 public interface Strategy {
 	public static final int SCALE_ID = 243;
 	public static final int BANKER_ID = 14924;
 	public static final int TUNNEL_ID = 66991;
 	public static final int SHORTCUT_ID = 9293;	
 	public static final int[] ids = {SCALE_ID, BANKER_ID, TUNNEL_ID, SHORTCUT_ID};
 	
 	public static final int LODESTONE_PARENT_ID = 1092;
 	public static final int TAVERLY_TELE_ID = 50;
 	// Tiles & such
 	
	public static final Tile SAFE_SPOT = new Tile(2892, 9786, 0);
 	public static final Tile DOWN_STAIRS = new Tile(2886, 9795, 0);
 	
 	public static final TilePath PATH_TO_BANK = new TilePath(new Tile[] {
 			new Tile(2878, 3442, 0), new Tile(2884, 3433, 0),
 			new Tile(2881, 3424, 0), new Tile(2876, 3416, 0) });
 
 	public static final TilePath PATH_TO_TUNNEL = new TilePath(new Tile[] {
 			new Tile(2875, 3417, 0), new Tile(2886, 3415, 0),
 			new Tile(2897, 3415, 0), new Tile(2896, 3405, 0),
 			new Tile(2888, 3398, 0) });
 
 	public static final Area DRAG_AREA = new Area(new Tile(2892, 9812, 0), new Tile(2923, 9812, 0), new Tile(2923, 9784, 0), new Tile(2892, 9784, 0));
 
 	public boolean isValid();
 	
 	public void execute();
 	
 	public String getState();
 }
