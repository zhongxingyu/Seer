 package com.zephyrr.werewolf.enums;
 
 import com.zephyrr.werewolf.Werewolf;
 import org.bukkit.Location;
 
 /**
  *
  * @author Phoenix
  */
 public enum WarpPoint {
     HOUSE1(-258, 65, 305.5, "House #1"),
     HOUSE2(-258.5, 65, 294.5, "House #2"),
     HOUSE3(-275, 65, 300, "House #3"),
     HOUSE4(-275, 65, 292, "House #4"),
     HOUSE5(-275, 65, 278, "House #5"),
     HOUSE6(-275, 66, 317, "House #6"),
     HOUSE7(-262, 66, 318, "House #7"),
     HOUSE8(-275, 66, 331, "House #8"),
     HOUSE9(-249, 65, 379, "House #9"),
     HOUSE10(-245.5, 65, 361, "House #10"),
     HOUSE11(-267, 65, 365.5, "House #11"),
     HOUSE12(-252.5, 65, 361, "House #12"),
     HOUSE13(-252, 66, 349.5, "House #13"),
     HOUSE14(-268.5, 66, 351.5, "House #14"),
     HOUSE15(-232.5, 65, 370.5, "House #15"),
     HOUSE16(-232.5, 65, 379.5, "House #16"),
     EXECUTIONER1(-240.5, 65, 423.5, "Executioner #1"),
     EXECUTIONER2(-240.5, 65, 435.5, "Executioner #2"),
     EXECUTIONER3(-242.5, 65, 423.5, "Executioner #3"),
     EXECUTIONER4(-242.5, 65, 435.5, "Executioner #4"),
     EXECUTIONER5(-244.5, 65, 423.5, "Executioner #5"),
     EXECUTIONER6(-244.5, 65, 435.5, "Executioner #6"),
     EXECUTIONER7(-246.5, 65, 423.5, "Executioner #7"),
     EXECUTIONER8(-246.5, 65, 435.5, "Executioner #8"),
     EXECUTIONER9(-248.5, 65, 423.5, "Executioner #9"),
     EXECUTIONER10(-248.5, 65, 435.5, "Executioner #10"),
     EXECUTIONER11(-250.5, 65, 423.5, "Executioner #11"),
     EXECUTIONER12(-250.5, 65, 435.5, "Executioner #12"),
     EXECUTIONER13(-252.5, 65, 423.5, "Executioner #13"),
     EXECUTIONER14(-252.5, 65, 435.5, "Executioner #14"),
     EXECUTIONER15(-254.5, 65, 425.5, "Executioner #15"),
     EXECUTIONER16(-254.5, 65, 435.5, "Executioner #16"),
    EXECUTION_STAND(-246, 65, 423.5, "Execution Stand"),
     TOWNSQUARE(-224.4, 65, 318.5, "Town Square"),
     OBSERVATORY(-251.5, 87.6, 332.5, "Observation Cube");
 
     private Location dest;
     private String name;
     WarpPoint(double x, double y, double z, String name) {
         this.name = name;
         dest = new Location(Werewolf.getWolfWorld(), x, y, z);
     }
     public Location getLocation() {
         return dest;
     }
     public String getName() {
         return name;
     }
 }
