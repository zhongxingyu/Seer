 package fieldbot;
 
 import battlecode.common.*;
 
 public class Weights
 {
     public static final double DROPOFF      = 0.4;
 
     public static final double ENEMY_HQ     = 20.0;
     public static final double ALLY_HQ      = -1.0;
 
     public static final double GL_ENEMY_SD  =  2.0;
     public static final double GL_ALLY_SD   = -0.01;
 
     public static final double LC_ENEMY_SD  =   1.0;
     public static final double LC_ALLY_SD   =   0.7;
     public static final double LC_MUL       = 100.0;
 
     public static final double EXPLORE_MINE = -0.3;
     public static final double BATTLE_MINE  = -0.5;
     public static final double CAPTURE      =  1.0;
     public static final double HEAL         = 10.0;
     public static final double SHIELD       = 10.0;
 
     public static final double GROUP_ATTACK = 2.0;
     public static final double GROUP_UP     = 2.0;
 
     public static final double ARTILLERY    = 0.8;
     public static final double PATH         = 0.6;
     public static final double TO_HQ        = 1 - PATH;
 
    public static final double MIN_ENERGON  = 30;
 
 /*
     public static final double MEDBAY       = 0.0;
     public static final double SHIELDS      = 0.0;
     public static final double ARTILLERY    = 0.3;
     public static final double GENERATOR    = 0.3;
     public static final double SUPPLIER     = 0.4;
 
     public static final double MEDBAY_SUM    = MEDBAY;
     public static final double SHIELDS_SUM   = SHIELDS   + MEDBAY_SUM;
     public static final double ARTILLERY_SUM = ARTILLERY + SHIELDS_SUM;
     public static final double GENERATOR_SUM = GENERATOR + ARTILLERY_SUM;
     public static final double SUPPLIER_SUM  = SUPPLIER  + GENERATOR_SUM;
 */
 
 }
