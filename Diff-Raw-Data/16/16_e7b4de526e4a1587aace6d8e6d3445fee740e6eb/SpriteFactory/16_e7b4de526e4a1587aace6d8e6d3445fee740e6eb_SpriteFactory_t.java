 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import ultraextreme.model.entity.IEntity;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.ObjectName;
 
 /**
  * Class in charge of creating new GameObjectSprites
  * 
  * @author Johan Gronvall
  * 
  */
 public final class SpriteFactory {
 
 	public static void initialize(SimpleBaseGameActivity activity) {
 		instance = new SpriteFactory(activity);
 	}
 
 	private Map<ObjectName, ITextureRegion> textureMap = new HashMap<ObjectName, ITextureRegion>();
 
 	private Map<ObjectName, Vector2d> offsetMap = new HashMap<ObjectName, Vector2d>();
 
 	/**
 	 * The items' textures.
 	 */
 	private Map<ObjectName, ITextureRegion> itemTextures = new HashMap<ObjectName, ITextureRegion>();
 
 	/**
 	 * The item bar's texture.
 	 */
 	private ITextureRegion itemBarTexture;
 
 	/**
 	 * The item bar's marker texture.
 	 */
 	private ITextureRegion itemBarMarkerTexture;
 
 	/**
 	 * A map containing the main menu's textures, which are its background and
 	 * buttons.
 	 */
 	private Map<String, ITextureRegion> mainMenuTextures;
 
 	private TiledTextureRegion textInputBackground;
 
 	/**
 	 * Reference to a sprite factory, making this a singleton.
 	 */
 	private static SpriteFactory instance;
 
 	/**
 	 * Creates a spriteFactory OBS: should be called during a loadResources
 	 * because this constructor might get heavy
 	 */
 	private SpriteFactory(final SimpleBaseGameActivity activity) {
 		Dimension screenDimension = new Dimension(activity.getResources()
 				.getDisplayMetrics().widthPixels, activity.getResources()
 				.getDisplayMetrics().heightPixels);
 		GameObjectSprite.setScreenDimension(screenDimension);
 		textureMap = new HashMap<ObjectName, ITextureRegion>();
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		final TextureManager textureManager = activity.getTextureManager();
 		BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(
				textureManager, 502, 119,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		// TODO make textureAtlas as small as possible (also play tetris with
 		// the textures to make it smaller)
 
 		// init enemies, bullets and the player
 		final TextureRegion playerShip = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "ship_blue_42px.png",
 						40, 0);
 		putProperties(ObjectName.PLAYERSHIP, playerShip, new Vector2d(16.5, 13));
 
 		final TextureRegion playerBullet = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "laserGreen.png", 0,
 						34);
 		putProperties(ObjectName.BASIC_BULLET, playerBullet, new Vector2d(4.5,
 				16.5));
 
//		final TextureRegion enemyBullet = BitmapTextureAtlasTextureRegionFactory
//				.createFromAsset(textureAtlas, activity, "laserRed.png", 0, 34);
//		putProperties(ObjectName.BASIC_BULLET, playerBullet, new Vector2d(4.5,
//				16.5));
 
 		final TextureRegion basicEnemy = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(textureAtlas, activity, "evil_ship_1.png", 165,
						0);
 		putProperties(ObjectName.BASIC_ENEMYSHIP, basicEnemy, new Vector2d(27,
 				40));
 
 		final TextureRegion hitAndRunEnemy = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "evil_ship_2.png",
 						221, 0);
 
 		putProperties(ObjectName.HITANDRUN_ENEMYSHIP, hitAndRunEnemy,
 				new Vector2d(27, 40));
 
 		final TextureRegion parabolaEnemy = BitmapTextureAtlasTextureRegionFactory
 
 		.createFromAsset(textureAtlas, activity, "evil_ship_3.png", 275, 0);
 
 		putProperties(ObjectName.PARABOLA_ENEMYSHIP, parabolaEnemy,
 				new Vector2d(56, 59));
 
 		// init pickupables
 		final TextureRegion basicWeapon = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "cannon.png", 73, 31);
 		putProperties(ObjectName.BASIC_WEAPON, basicWeapon,
 				new Vector2d(15, 15));
 		itemTextures.put(ObjectName.BASIC_WEAPON, basicWeapon);
 
 		final TextureRegion spinningWeapon = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "spin.png", 9, 31);
 		putProperties(ObjectName.SPINNING_WEAPON, spinningWeapon, new Vector2d(
 				15, 15));
 		itemTextures.put(ObjectName.SPINNING_WEAPON, spinningWeapon);
 
 		final TextureRegion spreadWeapon = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "spread.png", 73, 0);
 		putProperties(ObjectName.SPREAD_WEAPON, spreadWeapon, new Vector2d(15,
 				15));
 		itemTextures.put(ObjectName.SPREAD_WEAPON, spinningWeapon);
 
 		// Init the item bar texture
 		itemBarTexture = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "itembar.png", 0, 68);
 
 		// Init the item bar marker
 		itemBarMarkerTexture = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(textureAtlas, activity, "itembar_marker.png",
 						104, 0);
 
 		// Init the textinput background
 		textInputBackground = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(textureAtlas, activity, "button_1.png",
 						331, 0, 1, 1);
 
 		// What is this for?(I think it needs to be called to init the atlas, we
 		// will never know.. gramlich 2012)
 		textureManager.loadTexture(textureAtlas);
 
 		// Init main menu atlas and texture map
 		mainMenuTextures = new HashMap<String, ITextureRegion>();
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 		BitmapTextureAtlas textureAtlasMainMenu = new BitmapTextureAtlas(
 				textureManager, 800, 1730, TextureOptions.DEFAULT);
 
 		// Init the main menu background
 		mainMenuTextures.put("background",
 				BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 						textureAtlasMainMenu, activity, "main_menu_bg.jpg", 0,
 						0));
 
 		// Init main menu's start button
 		mainMenuTextures.put("startButton",
 				BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 						textureAtlasMainMenu, activity,
 						"main_menu_start_button.png", 0, 1281));
 
 		// Init main menu's high scores button
 		mainMenuTextures.put("highScoresButton",
 				BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 						textureAtlasMainMenu, activity,
 						"main_menu_high_scores_button.png", 0, 1431));
 
 		// Init main menu's exit button
 		mainMenuTextures.put("exitButton",
 				BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 						textureAtlasMainMenu, activity,
 						"main_menu_exit_button.png", 0, 1581));
 		textureManager.loadTexture(textureAtlasMainMenu);
 	}
 
 	/**
 	 * 
 	 * @return The texture of the item bar's marker.
 	 */
 	public static ITextureRegion getItemBarMarkerTexture() {
 		return instance.itemBarMarkerTexture;
 	}
 
 	/**
 	 * 
 	 * @return The texture of the item bar.
 	 */
 	public static ITextureRegion getItemBarTexture() {
 		return instance.itemBarTexture;
 	}
 
 	/**
 	 * 
 	 * @param item
 	 *            The item you want an image of.
 	 * @return An texture of an item that you want to show in the item bar.
 	 */
 	public static ITextureRegion getItemTexture(ObjectName item) {
 		ITextureRegion output = instance.itemTextures.get(item);
 		if (output == null) {
 			throw new IllegalArgumentException(
 					"No texture is associated with that kind of object");
 		}
 		return output;
 	}
 
 	/**
 	 * 
 	 * @return The texture of the main menu scene's background.
 	 */
 	public static ITextureRegion getMainMenuBackgroundTexture() {
 		return instance.mainMenuTextures.get("background");
 	}
 
 	/**
 	 * 
 	 * @return The texture of the main menu scene's exit button.
 	 */
 	public static ITextureRegion getMainMenuExitButtonTexture() {
 		return instance.mainMenuTextures.get("exitButton");
 	}
 
 	/**
 	 * 
 	 * @return The texture of the main menu scene's high scores button.
 	 */
 	public static ITextureRegion getMainMenuHighScoresButtonTexture() {
 		return instance.mainMenuTextures.get("highScoresButton");
 	}
 
 	/**
 	 * 
 	 * @return The texture of the main menu scene's start button.
 	 */
 	public static ITextureRegion getMainMenuStartButtonTexture() {
 		return instance.mainMenuTextures.get("startButton");
 	}
 
 	/**
 	 * Creates and returns a sprite of the specified type
 	 * 
 	 * @param entity
 	 *            The entity which this sprite is to follow
 	 * @param vbom
 	 *            the VertexBufferOBjectManager
 	 * @param objectName
 	 *            what kind of sprite (picture) is desired
 	 * @return a new GameOBjectSprite
 	 */
 	public static GameObjectSprite getNewSprite(final IEntity entity,
 			final VertexBufferObjectManager vbom) {
 		ObjectName objName = entity.getObjectName();
 		ITextureRegion texture = instance.textureMap.get(objName);
 		Vector2d offset = instance.offsetMap.get(objName);
 		if (texture == null) {
 			throw new IllegalArgumentException(
 					"No texture is associated with that kind of object");
 		}
 		if (offset == null) {
 			offset = new Vector2d();
 		}
 
 		return new GameObjectSprite(entity, vbom, texture, offset);
 	}
 
 	/**
 	 * @return the textInputBackground
 	 */
 	public static TiledTextureRegion getTextInputBackground() {
 		return instance.textInputBackground;
 	}
 
 	/**
 	 * Puts the properties into textureMap and offsetMap. Also multiplies the
 	 * offset with the sprite scaling factor.
 	 * 
 	 * @param objectName
 	 *            The key in the maps.
 	 * @param texture
 	 *            The texture that goes into textureMap.
 	 * @param textureOffset
 	 *            The offset that goes into offsetMap, multiplied with the
 	 *            sprite scaling factor.
 	 */
 	private void putProperties(ObjectName objectName, TextureRegion texture,
 			Vector2d textureOffset) {
 		textureOffset.scale(ultraextreme.util.Constants.SPRITE_SCALE_FACTOR);
 		textureMap.put(objectName, texture);
 		offsetMap.put(objectName, textureOffset);
 	}
 }
