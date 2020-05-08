 package com.punchline.NinjaSpacePirate.gameplay.entities.systems.spawn.locations;
 
 import com.punchline.NinjaSpacePirate.gameplay.entities.components.render.HudWarning.WarningType;
 import com.punchline.NinjaSpacePirate.gameplay.entities.systems.spawn.LocationTemplate;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.utils.Random;
 
 /**
  * Creates a pit obstacle.
  * @author Natman64
  *
  */
 public class PitLocation extends LocationTemplate {
 
 	private Random r = new Random();
 	
 	private int grateLoc;
 	
 	/**
 	 * Creates a pit location template.
 	 */
 	public PitLocation() {
 		super(null, 0, 1);
 	}
 
 	@Override
 	public String[] getRows() {
 		
 		grateLoc = r.nextInt(5);
 		String rowKey = "PitGrate" + grateLoc;
 		
 		String[] rows = new String[16];
 		rows[0] = rowKey;
 		rows[1] = rowKey;
 		rows[2] = rowKey;
 		rows[3] = rowKey;
 		rows[4] = rowKey;
 		rows[5] = rowKey;
 		rows[6] = "HallSegment";
 		rows[7] = "HallSegment";
 		rows[8] = "HallSegment";
 		rows[9] = "HallSegment";
 		rows[10] = "HallSegmentWallVents";
 		rows[11] = "HallSegment";
 		rows[12] = "HallSegment";
 		rows[13] = "HallSegment";
 		rows[14] = "HallSegment";
 		rows[15] = "HallSegment";
 		
 		return rows;
 		
 	}
 
 	@Override
 	public int getDifficulty() {
 		return 2;
 	}
 
 	@Override
 	public void onQueue(EntityWorld world) {
		world.createEntity("HudWarning", WarningType.GreenArrow, grateLoc + 1);
 	}
 	
 }
