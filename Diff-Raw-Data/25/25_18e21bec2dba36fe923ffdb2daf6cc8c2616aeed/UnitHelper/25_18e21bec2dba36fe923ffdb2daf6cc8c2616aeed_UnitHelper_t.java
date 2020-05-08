 package pruebas.Renders.helpers;
 
 import java.util.Hashtable;
 
 import pruebas.Renders.UnitRender;
 import pruebas.Util.SuperAnimation;
 import pruebas.Util.UnitAnimPrefReader;
 import pruebas.Util.UnitAnimPrefReader.UnitPrefReaderData;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class UnitHelper {
 	public static final float WIDTH = 135;
 	public static final float HEIGHT = 150;
 
 	public static final int HP_BAR_HEIGHT = 10;
 	public static final int HP_BAR_WIDTH = 50;
 	public static final int HP_BAR_Y = 130;
 	public static final int HP_BAR_X = 10;
 
 	public static Hashtable<String, UnitRender> renderMap;
 	public static Texture enemyHPBar;
 	public static Texture allyHPBar;
 	
 	public static SuperAnimation shieldAnimation;
 
 	public static void init() {
 		renderMap = new Hashtable<String, UnitRender>();
 		shieldAnimation = loadShieldAnim();
 	}
 
 	public static UnitRender getUnitRender(String unitName) {
 		if (renderMap.contains(unitName)) {
 			return renderMap.get(unitName).clone();
 		}
 
 		UnitRender render = loadRender(unitName);
 		render.setAnimation(UnitRender.ANIM.idle);
 		renderMap.put(unitName, render);
 		return render;
 	}
 
 	private static UnitRender loadRender(String unitName) {
 		UnitRender render = new UnitRender();
 		render.idleAnim = getUnitSuperAnimation(unitName, "idle");
 		render.idleAnim.randomCurrentFrame();
 
 		// render.idleAnim = getUnitSuperAnimation(unitName, "attack");
 		render.walkAnim = getUnitSuperAnimation(unitName, "run");
		render.shieldAnim = shieldAnimation.clone();
 
 		return render;
 	}
 
 	public static SuperAnimation getUnitSuperAnimation(String unitName,
 			String action) {
 		String base_file_name = String.format("data/Units/%s/%s", unitName,
 				action);
 
 		UnitPrefReaderData data = UnitAnimPrefReader.load(base_file_name
 				+ ".pref");
 
 		Texture sheet = new Texture(Gdx.files.internal(base_file_name + ".png"));
 		TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()
 				/ data.cols, sheet.getHeight() / data.rows);
 		TextureRegion[] frames = new TextureRegion[data.cols * data.rows];
 
 		int index = 0;
 		for (int i = 0; i < data.rows; i++) {
 			for (int j = 0; j < data.cols; j++) {
 				frames[index++] = tmp[i][j];
 			}
 		}
 
 		SuperAnimation anim = new SuperAnimation(data.total_time,
 				data.image_times, frames);
 		anim.handle_x = data.handle_x;
 		anim.handle_y = data.handle_x;
 		return anim;
 	}
 
 	private static SuperAnimation loadShieldAnim(){
 		String base_file_name = "data/Units/defensive_shield";
 		UnitPrefReaderData data = UnitAnimPrefReader.load(base_file_name + ".pref");
 		Texture sheet = new Texture(Gdx.files.internal(base_file_name + ".png"));
 		TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()
 				/ data.cols, sheet.getHeight() / data.rows);
 		TextureRegion[] frames = new TextureRegion[data.cols * data.rows];
 
 		int index = 0;
 		for (int i = 0; i < data.rows; i++) {
 			for (int j = 0; j < data.cols; j++) {
 				frames[index++] = tmp[i][j];
 			}
 		}
 		
 		SuperAnimation shieldAnimation = new SuperAnimation(data.total_time, data.image_times, frames);
 		shieldAnimation.handle_x = data.handle_x;
 		shieldAnimation.handle_y = data.handle_x;
 		
 		return shieldAnimation;
 	}
 	
 	public static Texture getEnemyHPBar() {
 		if (enemyHPBar == null) {
 			enemyHPBar = createBar(1, 0, 0, 1);
 		}
 		return enemyHPBar;
 	}
 
 	public static Texture getAllyHPBar() {
 		if (allyHPBar == null) {
 			allyHPBar = createBar(0, 1, 0, 1);
 		}
 		return allyHPBar;
 	}
 
 	private static Texture createBar(int r, int g, int b, int a) {
 		Pixmap pixmap = new Pixmap(UnitHelper.HP_BAR_WIDTH,
 				UnitHelper.HP_BAR_HEIGHT, Format.RGBA4444); // or RGBA8888
 		pixmap.setColor(r, g, b, a);
 		pixmap.fill();
 		Texture text = new Texture(pixmap); // must be manually disposed
 		pixmap.dispose();
 		return text;
 	}
 }
