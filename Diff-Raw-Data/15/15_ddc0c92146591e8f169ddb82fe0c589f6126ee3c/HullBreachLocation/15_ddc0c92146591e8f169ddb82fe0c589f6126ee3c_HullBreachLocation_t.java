 package com.punchline.NinjaSpacePirate.gameplay.entities.systems.spawn.locations;
 
 import com.punchline.NinjaSpacePirate.gameplay.entities.components.render.HudWarning.WarningType;
 import com.punchline.NinjaSpacePirate.gameplay.entities.systems.spawn.LocationTemplate;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.utils.Random;
 
 /**
  * Template for a hull breach obstacle.
  * @author Natman64
  * @created Oct 12, 2013
  */
 public class HullBreachLocation extends LocationTemplate {
 
 	private static final int ROWS = 7;
 	private static final int PADDING_ROWS = 10;
 	
 	private Random r = new Random();
 	
 	private boolean leftSide;
 	
 	/**
 	 * Constructs a HullBreachLocation.
 	 */
 	public HullBreachLocation() {
 		super(null, 0, 1);
 	}
 
 	@Override
 	public String[] getRows() {
 		String prefix;
 		
 		String[] rows = new String[ROWS + PADDING_ROWS];
 		
 		leftSide = r.nextBoolean();
 		
 		if (leftSide) {
 			//left side
 			prefix = "Left";
 		} else {
 			//right side
 			prefix = "Right";
 		}
 		
 		for (int i = 0; i < ROWS; i++) {
 			rows[i] = prefix + "Breach" + i;
 		}
 		
 		for (int i = ROWS; i < ROWS + PADDING_ROWS; i++) {
 			rows[i] = "HallSegment";
 		}
 		
 		return rows;
 	}
 	
 	@Override
 	public int getDifficulty() {
 		return 3;
 	}
 
 	@Override
 	public void onQueue(EntityWorld world) {
 		int lane = 1;
		
 		if (!leftSide) {
 			lane = 5;
 		}
 		
 		world.createEntity("HudWarning", WarningType.WarningSign, lane);
 	}
 	
 	
 	
 }
