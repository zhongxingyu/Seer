 package testpackage.shared.ship;
 
 import testpackage.shared.ship.SoundHandler.Sound;
 
 public class TemplateImages {
 	private static final String soundspath = "/sounds";
 
 	public static final String translationspath = "/translations/";
 
 	public static final String levelspath = "/resources/levels/defaultlevels/";
 	public static final String imagesdir = "/resources/images/";
 	
 	public static final String weblevelspath = "template/" + levelspath;
 	public static final String webimagesdir = "template/" + imagesdir + "defaultskin/";
 
 	public static final String iconsdir = "/resources/icons/";
 	
 	public static String[] allSkins = {"defaultskin", "smallskin"};
 
 	public static String[] icons = { "fogofwar", "ship_bottom",
 		"ship_hit_bottom", "ship_hit_horizontal", "ship_hit_left",
 		"ship_hit", "ship_hit_right", "ship_hit_top",
 		"ship_hit_vertical", "ship_horizontal", "ship_left",
 		"ship_right", "ship_top", "ship_vertical", "water_hit",
 	"water" };
 
 	public static String getSoundPath(Sound sound) {
 		return soundspath + "/" + sound.toString().replace('_', '.');
 	}
 
 	public static String fieldToIcon(Character c) {
 		switch (c) {
 		case '#': return "fogofwar";
 		case 'B': return "ship_hit_bottom";
 		case 'b': return "ship_bottom";
 		case 'H': return "ship_hit_horizontal";
 		case 'h': return "ship_horizontal";
 		case 'L': return "ship_hit_left";
 		case 'l': return "ship_left";
 		case 'R': return "ship_hit_right";
 		case 'r': return "ship_right";
 		case 'T': return "ship_hit_top";
 		case 't': return "ship_top";
 		case 'V': return "ship_hit_vertical";
 		case 'v': return "ship_vertical";
 		case '*': return "water_hit";
 		case '-': return "water";
 		default: throw new RuntimeException("Don't know char: " + c);
 		}
 	}
 }
