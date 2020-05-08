 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.frontend.items.MotionState;
 import edu.mines.csci598.recycler.frontend.items.Recyclable;
 import edu.mines.csci598.recycler.frontend.items.RecyclableType;
 import edu.mines.csci598.recycler.frontend.motion.ConveyorBelt;
 import edu.mines.csci598.recycler.frontend.motion.Movable;
 import org.apache.log4j.Logger;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Marshall
  * Date: 11/14/12
  * Time: 4:42 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RecycleBins {
 
     private static final Logger logger = Logger.getLogger(RecycleBins.class);
     private List<RecycleBin> recycleBins = new LinkedList<RecycleBin>();
 
 
     public RecycleBins(Side s) {
         if(s.equals(Side.LEFT)) {
             setUpLeftBins();
        } else {
             setUpRightBins();
         }
     }
 
     /**
      * Returns the bin that the recyclable is currently falling into. If it is not falling into any then it is going
      * into the trash.
      *
      * Moved from GameLogic check collision function
      * Need here for AI
      *
      * @param m
      * @return
      */
     public RecycleBin findBinForFallingRecyclable(Movable m) {
         if (!(m instanceof Recyclable)) {
             throw new IllegalStateException("Something is falling into a bin that shouldn't be (not a recyclable)!");
         }
 
         int yCoord = m.getSprite().getY();
 
         // finds the bin that the trash has gone into using the y coordinates since it can only fall to the right or
         // left of the conveyor we only need to check which way it's going and the y coordinates
         for (RecycleBin bin : recycleBins) {
             if ((m.getMotionState() == MotionState.FALL_LEFT && bin.getSide() == RecycleBin.ConveyorSide.LEFT) ||
                     (m.getMotionState() == MotionState.FALL_RIGHT && bin.getSide() == RecycleBin.ConveyorSide.RIGHT)) {
 
                 if (yCoord >= bin.getMinY() && yCoord <= bin.getMaxY()) {
                     return bin;
                 }
             }
         }
         return getLast();
     }
     
     /*
      * Find bin type
      */
     public RecycleBin findCorrectBin(Recyclable r) {
     	RecyclableType recycleType = r.getType();
         for(RecycleBin recycleBin: recycleBins) {
             if(recycleBin.getType() == recycleType) {
                 return recycleBin;
             }
         }
         
         throw new IllegalStateException("No bin found for recyclable " + r);
     }
     
     /**
      *  sets up the location of each of the bins with trash last since it is the last accessed bin
      */
     private void setUpLeftBins() {
         RecycleBin bin1 = new RecycleBin(
                 RecycleBin.ConveyorSide.LEFT,  PLASTIC_MIN_Y,
                 PLASTIC_MAX_Y, RecyclableType.PLASTIC,
                 PLASTIC_IMAGE_LEFT, SoundEffectEnum.PLASTIC_OR_PAPER_HITS_BIN);
         RecycleBin bin2 = new RecycleBin(
                 RecycleBin.ConveyorSide.LEFT, PAPER_MIN_Y,
                 PAPER_MAX_Y, RecyclableType.PAPER,
                 PAPER_IMAGE_LEFT, SoundEffectEnum.PLASTIC_OR_PAPER_HITS_BIN);
         RecycleBin bin3 = new RecycleBin(
                 RecycleBin.ConveyorSide.RIGHT, HAZARD_MIN_Y,
                 HAZARD_MAX_Y, RecyclableType.HAZARD,
                 HAZARD_IMAGE_LEFT, SoundEffectEnum.NUCLEAR_BIN);
         RecycleBin bin4 = new RecycleBin(
                 RecycleBin.ConveyorSide.RIGHT, GLASS_MIN_Y,
                 GLASS_MAX_Y, RecyclableType.GLASS,
                 GLASS_IMAGE_LEFT,SoundEffectEnum.GLASS_HITS_BIN);
 
         recycleBins.add(bin1);
         recycleBins.add(bin2);
         recycleBins.add(bin3);
         recycleBins.add(bin4);
         recycleBins.add(RecycleBin.TRASH_BIN);
     }
 
     /**
      *  sets up the location of each of the bins with trash last since it is the last accessed bin
      */
     private void setUpRightBins() {
         RecycleBin bin1 = new RecycleBin(
                 RecycleBin.ConveyorSide.RIGHT,  PLASTIC_MIN_Y,
                 PLASTIC_MAX_Y, RecyclableType.PLASTIC,
                 PLASTIC_IMAGE_RIGHT,SoundEffectEnum.PLASTIC_OR_PAPER_HITS_BIN);
         RecycleBin bin2 = new RecycleBin(
                 RecycleBin.ConveyorSide.RIGHT, PAPER_MIN_Y,
                 PAPER_MAX_Y, RecyclableType.PAPER,
                 PAPER_IMAGE_RIGHT,SoundEffectEnum.PLASTIC_OR_PAPER_HITS_BIN);
         RecycleBin bin3 = new RecycleBin(
                 RecycleBin.ConveyorSide.LEFT, HAZARD_MIN_Y,
                 HAZARD_MAX_Y, RecyclableType.HAZARD,
                 HAZARD_IMAGE_RIGHT,SoundEffectEnum.NUCLEAR_BIN);
         RecycleBin bin4 = new RecycleBin(
                 RecycleBin.ConveyorSide.LEFT, GLASS_MIN_Y,
                 GLASS_MAX_Y, RecyclableType.GLASS,
                 GLASS_IMAGE_RIGHT,SoundEffectEnum.GLASS_HITS_BIN);
 
         recycleBins.add(bin1);
         recycleBins.add(bin2);
         recycleBins.add(bin3);
         recycleBins.add(bin4);
         recycleBins.add(RecycleBin.TRASH_BIN);
     }
     
     public RecycleBin getLast() {
         return recycleBins.get(recycleBins.size() - 1);
     }
 
     public List<RecycleBin> getRecycleBins() {
         return recycleBins;
     }
 
     //Bin constants
     public static final int PLASTIC_MIN_Y = 80;
     public static final int PLASTIC_MAX_Y = 400;
     public static final String PLASTIC_IMAGE_LEFT = "src/main/resources/SpriteImages/Bins/left_bin_plastic_empty.png";
     public static final String PLASTIC_IMAGE_RIGHT = "src/main/resources/SpriteImages/Bins/right_bin_plastic_empty.png";
 
     public static final int PAPER_MIN_Y = 401;
     public static final int PAPER_MAX_Y = ConveyorBelt.SPRITE_BECOMES_TOUCHABLE;
     public static final String PAPER_IMAGE_LEFT = "src/main/resources/SpriteImages/Bins/left_bin_paper_empty.png";
     public static final String PAPER_IMAGE_RIGHT = "src/main/resources/SpriteImages/Bins/right_bin_paper_empty.png";
 
     public static final int HAZARD_MIN_Y = 173;
     public static final int HAZARD_MAX_Y = 547;
     public static final String HAZARD_IMAGE_LEFT = "src/main/resources/SpriteImages/Bins/left_bin_hazard_empty.png";
     public static final String HAZARD_IMAGE_RIGHT = "src/main/resources/SpriteImages/Bins/right_bin_hazard_empty.png";
 
     public static final int GLASS_MIN_Y = 548;
     public static final int GLASS_MAX_Y = ConveyorBelt.SPRITE_BECOMES_TOUCHABLE;
     public static final String GLASS_IMAGE_LEFT = "src/main/resources/SpriteImages/Bins/left_bin_glass_empty.png";
     public static final String GLASS_IMAGE_RIGHT = "src/main/resources/SpriteImages/Bins/right_bin_glass_empty.png";
 
 }
