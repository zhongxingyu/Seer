 package vooga.fighter.model;
 
 import java.util.ResourceBundle;
 
 /**
  * Stores the constants relevant to the model.
  * 
  * @author James Wei
  *
  */
 public class ModelConstants {
     
     // resource bundle setup
     private static final String RESOURCE_PATH = "vooga.fighter.config.objects";
     private static final String RESOURCE_DEFAULT_VALUES_PATH="vooga.fighter.config.defaultvalues";
     private static ResourceBundle myDefaults;  
     private static ResourceBundle myResources;  
 
     static {
         myResources = ResourceBundle.getBundle(RESOURCE_PATH);
         myDefaults= ResourceBundle.getBundle(RESOURCE_DEFAULT_VALUES_PATH);
     }
     
     // strings used to identify attack object properties
     public static final String ATTACK_PROPERTY_DAMAGE = myResources.getString("Damage");
     public static final String ATTACK_PROPERTY_SPEED = myResources.getString("Movespeed");
     public static final String ATTACK_PROPERTY_DIRECTION = myResources.getString("Direction");
     public static final String ATTACK_PROPERTY_DURATION = myResources.getString("Duration");
     
     // attack object constants
     public static final int ATTACK_COUNTER_ZERO = 0;
     
     // strings used to identify effect properties
     public static final String EFFECT_PROPERTY_DURATION = myResources.getString("Duration");
     
     // effect properties
     public static final String EFFECT_PROPERTY_BURN_DAMAGE = myResources.getString("Damage");
     
     //directions
     public static final int RIGHT=0; 
     public static final int DOWN=90;
     public static final int LEFT=180; 
     public static final int UP=270; 
     
     //Mouse click image tag
     public static final String MOUSE_CLICK_IMAGE_TAG = "mouseclick";
     
     //Mouse image tag
    public static final String MOUSE_IMAGE_TAG = "mouse";
    
     public static final String ATTACKLOADER_PATH_TAG = "AttackPath";
     public static final String CHARACTERLOADER_PATH_TAG = "CharacterPath";
     public static final String ENVIRONMENTLOADER_PATH_TAG= "EnvironmentPath";
     public static final String MAPLOADER_PATH_TAG = "MapPath";
     public static final String MENUGRIDLOADER_PATH_TAG = "MenuGridPath";
     public static final String MENULOADER_PATH_TAG = "MenuPath";
     public static final String MOUSECLICKLOADERPATH_TAG = "MouseClickPath";
     public static final String MOUSELOADER_PATH_TAG = "MousePath";
     
     // 4 tick delay
     public static final int FOUR_TICKS = 4;
     
     
 }
