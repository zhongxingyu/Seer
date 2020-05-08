 package com.age;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.age.graphics.Camera;
 import com.age.graphics.Drawable;
 import com.age.graphics.effects.TextureLoader;
 import com.age.logic.entity.Entity;
 import com.age.world.World;
 
 public class Age {
 	public final static String name = "Abereth game engine";
 	public final static String version = "0.0.1";
 	
 	public static int EmptyTexture = -1;
 	
 	public static ArrayList<Drawable> clickedList =  new ArrayList<Drawable>();
 	
 	/**
	 * Would be best to <b>NOT</b> touch this array </br>
      * @see Age#cameraMain instead
 	 */
 	public static ArrayList<Camera> cameraList = new ArrayList<Camera>();
     /**
      * Main camera that is responsible for rendering, fluid camera movement, player targeting.
      */
 	public static Camera cameraMain = new Camera(0, 0, Screen.getWidth(), Screen.getHeight());
 	static ArrayList<View> viewList = new ArrayList<View>();
 	public static void addView(View v){
 		for(View view: viewList){
 			if(v.getName().toLowerCase().equals(view.getName().toLowerCase())){
 				
 			}
 		}
 	}
 	public static ArrayList<Drawable> drawList = new ArrayList<Drawable>();
     public static ArrayList<Entity> entityList = new ArrayList<Entity>();
 
     public static Random randomID = new Random();
 
     /**
      * Do not call this method to add an object after
      * calling toEngine as it is already called and will only duplicate
      * the object.
      *
      * @see Age#remove(long)
      * @param d Drawable object
      * @return Drawable object added to the Engine
      */
     public static Drawable add(Drawable d){
         drawList.add(d);
 		return d;
 	}
 
     /**
      *
      * REMOVES DRAWABLE OBJECTS FROM ID'S
      * HOW EVER THIS IS A LONG WAY TO DELETE THEM AND
      * CAN BE INCREDIBLY SLOW IF REMOVING
      * SIGNIFICANT AMOUNTS OF OBJECTS!
      *
      * I FIXED IT.
      *
      * ALSO I HAVE NO IDEA WHY I'M YELLING.
      * @param id
      * @return if removed, returns true, else, false
      */
 	public static boolean remove(long id){
 		for(int i = 0; i < drawList.size(); i++){
             if(id == drawList.get(i).getID()){
                 drawList.remove(i);
                 return true;
             }
         }
         return false;
 	}
 
     public static boolean remove(Drawable d){
         for(int i = 0; i < drawList.size(); i++){
             if(d == drawList.get(i)){
                 drawList.remove(i);
                 return true;
             }
         }
         return false;
     }
 
 	public static float ratioX(){
 		return Screen.getWidth() / Screen.originalDimensionX;
 	}
 	public static float ratioY(){
 		return Screen.getHeight() / Screen.originalDimensionY;
 	}
 	
 	
 	/*
 	 * Resources
 	 */
 	
 	/*
 	 * Items
 	 */
 	public static char letters[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
 		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
         'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
 		'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
 		'v', 'w', 'x', 'y', 'z' };
 
 	public static char numbers[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
 
 	public static ArrayList<Integer> letterTexID = new ArrayList<Integer>();
 
 	public static ArrayList<Integer> numberTexID = new ArrayList<Integer>();
 
 	public static void loadTextures() {
 		for (int i = 0; i < letters.length / 2; i++) {
 			letterTexID.add(TextureLoader.createTexture("res/font/sprite1.png",
 					i * 8, 0, 8, 8));
 		}
 	    for (int i = 0; i < letters.length / 2; i++) {
 			letterTexID.add(TextureLoader.createTexture("res/font/sprite1.png",
 					i * 8, 8, 8, 8));
 		}
     	for (int i = 0; i < numbers.length; i++) {
 			numberTexID.add(TextureLoader.createTexture("res/font/sprite1.png",
 					i * 8, 16, 8, 8));
 		}
 	}
 				
 	public static void init(){//booiiiiii
 		EmptyTexture = TextureLoader.createTexture("res/none.png");
 		cameraList.add(cameraMain);
 		loadTextures();
 	}
 	
 	
 	public static void onUpdate(){
 		for(Drawable d : drawList){
 			d.render();
 		}
 		if(clickedList.size() != 0){
 			clickedList.get(clickedList.size() - 1).onClick();
 			clickedList.clear();
 		}
         //Check if world is set up
         if(World.activeWorld != null)
             World.activeWorld.onUpdate();
     }
 }
