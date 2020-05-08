 package org.rs2server.rs2.model.skills;
 
 import org.rs2server.rs2.action.impl.ProductionAction;
 import org.rs2server.rs2.model.*;
 import org.rs2server.rs2.util.Misc;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Physiologus
  * Date: 4/7/12
  * Time: 7:01 PM
  */
 public class PickPocketing extends ProductionAction {
     /**
      * The Npc.
      */
     private NPC npcs;
 
     /**
      * The player
      */
     private Player player;
 
     /**
      * The npc pickPocket.
      */
     private PickPocket pickPocket;
 
     public PickPocketing(Player player, NPC npc, PickPocket pickPocket) {
         super(player);
         this.player = player;
         this.npcs = npc;
         this.pickPocket = pickPocket;
     }
 
     @Override
     public AnimationPolicy getAnimationPolicy() {
         return AnimationPolicy.RESET_ALL;
     }
 
     @Override
     public CancelPolicy getCancelPolicy() {
         return CancelPolicy.ALWAYS;
     }
 
     @Override
     public StackPolicy getStackPolicy() {
         return StackPolicy.NEVER;
     }
 
     /**
      * Represents pickpocketing npcs.
      *
      * @author Fire cape
      */
     public static enum PickPocket {
 
         MAN(1, 132, new Item[]{new Item(995, 20 + Misc.random(12))}, new int[]{1, 3});
 
         /**
          * A map of object ids to stalls.
          */
         private static Map<Integer, PickPocket> pickpocket = new HashMap<Integer, PickPocket>();
 
         /**
          * Gets an npc by an npc id.
          *
          * @param npc The npc id.
          * @return The npc, or <code>null</code> if the npc is not a choosen to be pickpocketed.
          */
         public static PickPocket forId(int npc) {
             return pickpocket.get(npc);
         }
 
         /**
          * Populates the tree map.
          */
         static {
             for (PickPocket pickpockets : PickPocket.values()) {
                 for (int npc : pickpockets.npcId) {
                     pickpocket.put(npc, pickpockets);
                 }
             }
         }
 
         /**
          * The required level.
          */
         private int level;
 
         /**
          * The experiance gained.
          */
         private double experience;
 
         /**
          * The item id.
          */
         private Item[] item;
 
         /**
          * The npc id.
          */
         private int[] npcId;
 
         /**
          * Pickpocketing handling enum.
          *
          * @param level      The level.
          * @param experience The experience.
          * @param item       The item recieved.
          * @param npcId      The npc id.
          */
         private PickPocket(int level, int experience, Item[] item, int[] npcId) {
             this.level = level;
             this.experience = experience;
             this.item = item;
             this.npcId = npcId;
         }
 
         /**
          * Gets the required level.
          *
          * @return The required level.
          */
         public int getRequiredLevel() {
             return level;
         }
 
         /**
          * Gets the experience.
          *
          * @return The experience.
          */
         public double getExperience() {
             return experience;
         }
 
         /**
          * Gets the items.
          *
          * @return The item id.
          */
         public Item[] getItems() {
             return item;
         }
 
         /**
          * Gets the npc id.
          *
          * @return The npc id.
          */
         public int[] getNpcId() {
             return npcId;
         }
     }
 
     @Override
     public int getCycleCount() {
         int skill = getMob().getSkills().getLevel(getSkill());
         int level = pickPocket.getRequiredLevel();
         int modifier = pickPocket.getRequiredLevel();
         double cycleCount = 1;
         cycleCount = Math.ceil((level * 50 - skill * 10) / modifier * 0.0625 * 4);
         if (cycleCount < 1) {
             cycleCount = 1;
         }
        return (int) cycleCount;
     }
 
 
     @Override
     public int getProductionCount() {
        return pickPocket.getItems()[0].getCount();
     }
 
     @Override
     public Item[] getRewards() {
        return new Item[]{pickPocket.getItems()[Misc.random(pickPocket.getItems().length)]};
     }
 
     @Override
     public Item[] getConsumedItems() {
         return new Item[0];
     }
 
     @Override
     public int getSkill() {
         return Skills.THIEVING;
     }
 
     @Override
     public int getRequiredLevel() {
         return pickPocket.getRequiredLevel();
     }
 
     @Override
     public double getExperience() {
         return pickPocket.getExperience();
     }
 
     @Override
     public String getLevelTooLowMessage() {
         return "You need a level of " + pickPocket.getRequiredLevel() + " to pickpocket the " + NPCDefinition.forId(pickPocket.getNpcId()[pickPocket.getNpcId().length]).getName() + ".";
     }
 
     @Override
     public String getSuccessfulProductionMessage() {
         return "You successfully steal from the "+npcs.getDefinedName()+".";
     }
 
     @Override
     public Animation getAnimation() {
         return Animation.create(881);
     }
 
     @Override
     public Graphic getGraphic() {
         return null;
     }
 
     @Override
     public boolean canProduce() {
         if (player.getSkills().getLevel(Skills.THIEVING) >= pickPocket.getRequiredLevel()) {
             return true;
         } else {
             return false;
         }
     }
 
 }
