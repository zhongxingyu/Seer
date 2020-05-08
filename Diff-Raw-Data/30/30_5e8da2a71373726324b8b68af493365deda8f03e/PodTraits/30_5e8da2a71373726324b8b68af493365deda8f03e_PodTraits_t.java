 package MoFoliage2.Blocks;
 
 import MoFoliage2.Refrences.Names;
 
 public class PodTraits
 {
     public static String[] POD_TRAIT_ARRAY = {Names.BLINDNESS_POD_BLOCK_NAME, Names.DARK_POD_BLOCK_NAME, Names.FIRE_POD_BLOCK_NAME, Names.HASTE_POD_BLOCK_NAME, Names.JUMP_POD_BLOCK_NAME, Names.LAVA_POD_BLOCK_NAME, Names.LOVE_POD_BLOCK_NAME, Names.SPEED_POD_BLOCK_NAME, Names.STRENGTH_POD_BLOCK_NAME, Names.WATER_POD_BLOCK_NAME, Names.FAILED_POD_BLOCK_NAME};
     
     public static String[] BLINDNESS_POD_TRAITS = { "Dark", "Damp", "Cold"};
     public static String[] DARK_POD_TRAITS = { "Dark", "Arid", "Hot"};
     public static String[] FIRE_POD_TRAITS = { "Light", "Arid", "Warm"};
     public static String[] HASTE_POD_TRAITS = { "Dark", "Normal", "Cold"};
     public static String[] JUMP_POD_TRAITS = { "Light", "Damp", "Warm"};
     public static String[] LAVA_POD_TRAITS = { "Light", "Arid", "Hot"};
     public static String[] LOVE_POD_TRAITS = { "Light", "Damp", "Hot"};
     public static String[] SPEED_POD_TRAITS = { "Light", "Normal", "Cold"};
     public static String[] STRENGTH_POD_TRAITS = { "Dark", "Normal", "Cold"};
     public static String[] WATER_POD_TRAITS = { "Light", "Damp", "Cold"};
     
     
     public static String GetClosestPodTrait(String trait1, String trait2, String trait3)
     {
     	System.out.println(trait1);
     	System.out.println(trait2);
     	System.out.println(trait3);
     	
         if(trait1.equalsIgnoreCase(BLINDNESS_POD_TRAITS[0]) && trait2.equalsIgnoreCase(BLINDNESS_POD_TRAITS[1]) && trait3.equalsIgnoreCase(BLINDNESS_POD_TRAITS[2]))
         {
              return Names.BLINDNESS_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(DARK_POD_TRAITS[0]) && trait2.equalsIgnoreCase(DARK_POD_TRAITS[1]) && trait3.equalsIgnoreCase(DARK_POD_TRAITS[2]))
         {
              return Names.DARK_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(FIRE_POD_TRAITS[0]) && trait2.equalsIgnoreCase(FIRE_POD_TRAITS[1]) && trait3.equalsIgnoreCase(FIRE_POD_TRAITS[2]))
         {
              return Names.FIRE_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(HASTE_POD_TRAITS[0]) && trait2.equalsIgnoreCase(HASTE_POD_TRAITS[1]) && trait3.equalsIgnoreCase(HASTE_POD_TRAITS[2]))
         {
              return Names.HASTE_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(JUMP_POD_TRAITS[0]) && trait2.equalsIgnoreCase(JUMP_POD_TRAITS[1]) && trait3.equalsIgnoreCase(JUMP_POD_TRAITS[2]))
         {
              return Names.JUMP_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(LAVA_POD_TRAITS[0]) && trait2.equalsIgnoreCase(LAVA_POD_TRAITS[1]) && trait3.equalsIgnoreCase(LAVA_POD_TRAITS[2]))
         {
              return Names.LAVA_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(LOVE_POD_TRAITS[0]) && trait2.equalsIgnoreCase(LOVE_POD_TRAITS[1]) && trait3.equalsIgnoreCase(LOVE_POD_TRAITS[2]))
         {
              return Names.LOVE_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(SPEED_POD_TRAITS[0]) && trait2.equalsIgnoreCase(SPEED_POD_TRAITS[1]) && trait3.equalsIgnoreCase(SPEED_POD_TRAITS[2]))
         {
              return Names.SPEED_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(STRENGTH_POD_TRAITS[0]) && trait2.equalsIgnoreCase(STRENGTH_POD_TRAITS[1]) && trait3.equalsIgnoreCase(STRENGTH_POD_TRAITS[2]))
         {
              return Names.STRENGTH_POD_BLOCK_NAME;
         }
         if(trait1.equalsIgnoreCase(WATER_POD_TRAITS[0]) && trait2.equalsIgnoreCase(WATER_POD_TRAITS[1]) && trait3.equalsIgnoreCase(WATER_POD_TRAITS[2]))
         {
              return Names.WATER_POD_BLOCK_NAME;
         }
         
         return Names.FAILED_POD_BLOCK_NAME;
     }
 }
