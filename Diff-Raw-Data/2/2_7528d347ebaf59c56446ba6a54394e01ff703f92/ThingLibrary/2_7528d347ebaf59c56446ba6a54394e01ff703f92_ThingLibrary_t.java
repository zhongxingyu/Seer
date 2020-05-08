 package ui.isometric.builder.things;
 
 import game.GameThing;
 import game.GameWorld;
 
 import java.awt.image.BufferedImage;
 import java.util.*;
 
 import ui.isometric.IsoRendererLibrary;
 import ui.isometric.IsoSquare;
 import util.Direction;
 
 /**
  * A class to manage all the ThingCreators
  * 
  * @author melby
  *
  */
 public class ThingLibrary {
 	/**
 	 * A class that generates new GroundThings
 	 * @author melby
 	 *
 	 */
 	public static class GroundCreator implements ThingCreator {
 		private String renderer;
 		
 		/**
 		 * Create a ground tile with a given renderer
 		 * @param rendererName
 		 */
 		public GroundCreator(String rendererName) {
 			renderer = rendererName;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w) {
 			return new game.things.GroundTile(w, renderer);
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer, Direction.NORTH);
 		}
 		
 		@Override
 		public String rendererName() {
 			return renderer;
 		}
 	}
 	
 	/**
 	 * A class that generates new Players
 	 * @author melby
 	 *
 	 */
 	public static class PlayerCreator implements ThingCreator {
 		private String renderer;
 		
 		/**
 		 * Create a player with a given renderer
 		 * @param rendererName
 		 */
 		public PlayerCreator(String rendererName) {
 			renderer = rendererName;
 		}
 		
 		@Override
 		public GameThing createThing(GameWorld w) {
			game.things.Player player = new game.things.Player(w, renderer);
 			IsoRendererLibrary.setLevelInArguments(player.userArguments(), IsoSquare.PLAYER);
 			return player;
 		}
 
 		@Override
 		public BufferedImage previewImage() {
 			return IsoRendererLibrary.imageForRendererName(renderer, Direction.NORTH);
 		}
 		
 		@Override
 		public String rendererName() {
 			return renderer;
 		}
 	}
 	
 	private static List<ThingCreator> creators = null;
 	private static List<ThingCreator> unmodifiable = null;
 	
 	/**
 	 * Initialize all the internal ThingCreators
 	 */
 	private static void setupCreators() {
 		synchronized(ThingLibrary.class) {
 			if(creators == null) {
 				creators = new ArrayList<ThingCreator>();
 				unmodifiable = Collections.unmodifiableList(creators);
 				
 				creators.add(new GroundCreator("ground_grey_1"));
 				creators.add(new GroundCreator("ground_grey_2"));
 				creators.add(new GroundCreator("ground_grey_water_corner"));
 				creators.add(new GroundCreator("ground_grey_water_two_sides"));
 				creators.add(new GroundCreator("ground_grey_water_one_side"));
 				creators.add(new GroundCreator("water_1"));
 				
 				creators.add(new GroundCreator("ground_grey_road_corner_1"));
 				creators.add(new GroundCreator("ground_grey_road_end_1"));
 				creators.add(new GroundCreator("ground_grey_road_straight_1"));
 				creators.add(new GroundCreator("ground_grey_road_t_1"));
 				creators.add(new GroundCreator("ground_grey_road_x_1"));
 				
 				creators.add(new GroundCreator("ground_grey_tile_1_corner_1"));
 				creators.add(new GroundCreator("ground_grey_tile_1_one_side_1"));
 				creators.add(new GroundCreator("ground_grey_tile_1_two_sides_1"));
 				creators.add(new GroundCreator("ground_tile_1"));
 				
 				creators.add(new PlayerCreator("character_cordi_empty"));
 			}
 		}
 	}
 	
 	/**
 	 * Get all the ThingCreators
 	 * @return - an immutable list of ThingCreators
 	 */
 	public static List<ThingCreator> creators() {
 		synchronized(ThingLibrary.class) {
 			if(creators == null) {
 				setupCreators();
 			}
 		}
 		
 		return unmodifiable;
 	}
 }
