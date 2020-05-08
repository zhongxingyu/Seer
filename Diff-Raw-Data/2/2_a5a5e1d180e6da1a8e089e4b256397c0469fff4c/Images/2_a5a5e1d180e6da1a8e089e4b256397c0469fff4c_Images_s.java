 package net.fourbytes.shadow;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.fourbytes.shadow.mod.AMod;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.ArrayMap;
 import com.badlogic.gdx.utils.ObjectMap;
 
 public class Images {
 	private final static ObjectMap<String, Image> images = new ObjectMap<String, Image>();
 	private final static ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
 	
 	public static void addImage(String savename, Image i) {
 		images.put(savename, i);
 	}
 	
 	public static Image getImage(String savename, boolean newInstance) {
 		if (newInstance) {
 			return new Image(textures.get(savename));
 		}
 		return images.get(savename);
 	}
 	
 	public static void addTexture(String savename, Texture t) {
 		textures.put(savename, t);
 	}
 	
 	public static Texture getTexture(String savename) {
 		return textures.get(savename);
 	}
 	
 	/**
 	 * Loads minimum of / only needed resources required for loading screen.
 	 */
 	public static void loadBasic() {
 		//ETC / UI
 		addImage("white", "data/white.png");
 		
 		addImage("logo", "data/logo.png", TextureFilter.Linear, TextureFilter.Linear); //Linear because of artifacts
 		
 		addImage("bg_blue", "data/bg_blue.png"/*, TextureFilter.Linear, TextureFilter.Linear*/);
 		
 	}
 	
 	public static void loadImages() {
 		//ETC / UI 
 		addImage("bg_polar", "data/bg_polar.png");
 		addImage("bg", "data/bg.png");
 		
 		addImage("cloud", "data/cloud.png");
 		addImage("cloud2", "data/cloud2.png");
 		addImage("cloud3", "data/cloud3.png");
 		
 		addImage("moon", "data/moon.png");
 		addImage("void", "data/void.png");
 		
 		addImage("treefull", "data/levels/tiles/treefull.png");
 		
 		//BLOCKS
 		addImage("block_test", "data/levels/tiles/test.png");
 		addImage("block_dirt", "data/levels/tiles/dirt.png");
 		addImage("block_grass", "data/levels/tiles/grass.png");
 		addImage("block_wood", "data/levels/tiles/wood.png");
 		addImage("block_brick", "data/levels/tiles/brick.png");
 		addImage("block_brick_grey", "data/levels/tiles/brick_grey.png");
 		addImage("block_glass", "data/levels/tiles/glass.png");
 		addImage("block_lab1", "data/levels/tiles/lab1.png");
 		addImage("block_lab2", "data/levels/tiles/lab2.png");
 		addImage("block_sign_bg", "data/levels/tiles/sign_bg.png");
 		addImage("block_finish", "data/levels/tiles/finish.png");
 		addImage("block_ladder", "data/levels/tiles/ladder.png");
 		addImage("block_point", "data/levels/tiles/point.png");
 		addImage("block_push", "data/levels/tiles/pushblock.png");
 		addImage("block_spring", "data/levels/tiles/spring.png");
 		addImage("block_vast_dirt", "data/levels/tiles/vast_dirt.png");
 		addImage("block_vast_grass", "data/levels/tiles/vast_grass.png");
 		addImage("block_stone", "data/levels/tiles/stone.png");
 		addImage("block_ore_bronze", "data/levels/tiles/ore_bronze.png");
 		addImage("block_ore_coal", "data/levels/tiles/ore_coal.png");
 		addImage("block_ore_gold", "data/levels/tiles/ore_gold.png");
 		addImage("block_ore_oreo", "data/levels/tiles/ore_oreo.png");
 		addImage("block_ore_silver", "data/levels/tiles/ore_silver.png");
 		addImage("block_ore_uranium", "data/levels/tiles/ore_uranium.png");
 		addImage("block_water", "data/levels/tiles/water.png");
 		addImage("block_water_top", "data/levels/tiles/water_top.png");
 		addImage("block_lava", "data/levels/tiles/lava.png");
 		addImage("block_lava_top", "data/levels/tiles/lava_top.png");
 		addImage("block_obsidian", "data/levels/tiles/obsidian.png");
 		addImage("block_wire", "data/levels/tiles/wire.png");
 		addImage("block_button", "data/levels/tiles/button.png");
 		addImage("block_dissolve", "data/levels/tiles/dissolve.png");
 		addImage("block_clock", "data/levels/tiles/clock.png");
 		addImage("block_invisible", "data/levels/tiles/invisible.png");
 		
 		//ENTITIES
 		addImage("player", "data/player.png");
 	}
 	
 	public static void addImage(String savename, String loadname) {
		addImage(savename, loadname, TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
 	}
 	
 	public static void addImage(String savename, String loadname, TextureFilter minFilter, TextureFilter magFilter) {
 		try {
 			Texture t = new Texture(Gdx.files.internal(loadname), true);
 			t.setFilter(minFilter, magFilter);
 			addTexture(savename, t);
 			Image i = new Image(t);
 			addImage(savename, i);
 		} catch (Throwable t) {
 			System.err.println("Loading failed: SN: "+savename+"; LN: "+loadname);
 			t.printStackTrace();
 		}
 	}
 
 	public static void addImageByMod(AMod mod, String savename, String loadname) {
 		addImageByMod(mod, savename, loadname, TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
 	}
 	
 	public static void addImageByMod(AMod mod, String savename, String loadname, TextureFilter minFilter, TextureFilter magFilter) {
 		try {
 			URL url = mod.getClass().getResource("/assets/"+loadname);
 			InputStream in = new BufferedInputStream(url.openStream());
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			byte[] buf = new byte[1024];
 			int n = 0;
 			while (-1 != (n = in.read(buf))) {
 				out.write(buf, 0, n);
 			}
 			out.close();
 			in.close();
 			byte[] response = out.toByteArray();
 			Pixmap pixmap = new Pixmap(response, 0, response.length);
 			Texture t = new Texture(pixmap);
 			t.setFilter(minFilter, magFilter);
 			addTexture(savename, t);
 			Image i = new Image(t);
 			addImage(savename, i);
 		} catch (Throwable t) {
 			System.err.println("Loading failed (from mod): SN: "+savename);
 			t.printStackTrace();
 		}
 	}
 }
