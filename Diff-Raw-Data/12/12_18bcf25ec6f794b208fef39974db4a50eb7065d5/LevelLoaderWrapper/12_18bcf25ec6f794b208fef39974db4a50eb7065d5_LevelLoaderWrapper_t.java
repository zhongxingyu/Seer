 package edu.ua.cs.pbvs;
 
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.level.LevelLoader;
 import org.anddev.andengine.level.util.constants.LevelConstants;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.util.SAXUtils;
 import org.xml.sax.Attributes;
 
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class LevelLoaderWrapper extends LevelLoader {
 	
 	
 	/*
 	 * Here are the xml attributes.
 	 */
 	private static final String TAG_ENTITY = "entity";
 	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
 	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
 	private static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width";
 	private static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height";
 	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
 	
 	/*
 	 * Here are the XML object types
 	 */
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_METALBOX = "metalbox";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_4X1BLOCK = "4x1block";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_1X4BLOCK = "1x4block";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SCIENTIST = "scientist";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_NINJA = "ninja";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_STOCKBROKER = "stockbroker";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RACER = "racer";
 	LevelLoaderWrapper(final pbvs game, final Scene scene )
 	{
 		this.setAssetBasePath("level/");
 		this.registerEntityLoader(LevelConstants.TAG_LEVEL, 
 			new IEntityLoader() {
 				public void onLoadEntity(final String pEntityName, final Attributes pAttributes) {
 					final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
 					final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
 					//Toast.makeText(pbvs.this, "Loaded level with width=" + width + " and height=" + height + ".", Toast.LENGTH_LONG).show();
 					
					game.ground = new Rectangle(-3200, height+64, width+3200, 3200);
 					game.roof   = new Rectangle(-3200, -3200, width+3200, 3200);
 					game.left   = new Rectangle(-3200, -3200, 3200, height+6400);
 					game.right  = new Rectangle(width, -3200, 3200, 6400);
 		            game.ground.setColor(.20f, .20f, .20f);
 		            game.roof.setColor  (.20f, .20f, .20f);
 		            game.roof.setAlpha(0);
 		            game.left.setColor  (.20f, .20f, .20f);
 		            game.right.setColor (.20f, .20f, .20f);
 		            
 					scene.getLastChild().attachChild(game.ground);
 					scene.getLastChild().attachChild(game.roof);
 					scene.getLastChild().attachChild(game.left);
 					scene.getLastChild().attachChild(game.right);
 		            
 				}
 			}
 		);
 
 		this.registerEntityLoader(TAG_ENTITY, 
 			new IEntityLoader() {
 				public void onLoadEntity(final String pEntityName, final Attributes pAttributes) {
 					final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
 					final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
 					final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_WIDTH);
 					final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_HEIGHT);
 					final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
 	
 					TextureRegion temp = null;
 					TiledTextureRegion temp_tiled = null;
 					boolean dynamic = false;
 					boolean npc = false;
 					if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_METALBOX)) 
 					{
 						dynamic = true;
 						temp = game.metalBoxTextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_4X1BLOCK)) 
 					{
 						temp = game.block4X1TextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_1X4BLOCK)) 
 					{
 						temp = game.block1X4TextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SCIENTIST)) 
 					{
 						npc = true;
 						temp_tiled = game.scientistTextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_NINJA)) 
 					{
 						npc = true;
 						temp_tiled = game.ninjaTextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_STOCKBROKER)) 
 					{
 						npc = true;
 						temp_tiled = game.stockBrokerTextureRegion;
 					}
 					else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RACER)) 
 					{
 						npc = true;
 						temp_tiled = game.racerTextureRegion;
 					}
 					else
 					{
 						return;
 					}
 					if (!dynamic)
 					{
 						Sprite a = new Sprite (x, y+64, width, height, temp);
 						//TODO figure out how to make to make a static yet physics collidable object.
 						final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0.5f);
 			            PhysicsFactory.createBoxBody(game.mPhysicsWorld, a, BodyType.StaticBody, wallFixtureDef);
 						scene.getLastChild().attachChild(a);
 					}
 					else if ( npc )
 					{
 						//Enemy a = new Enemy (x, y+64, temp_tiled, game.mPhysicsWorld, game);
 						
 					}
 					else
 					{
 						PhysicsSprite a = new PhysicsSprite(x, y+64, width, height, temp, game.mPhysicsWorld);
 						scene.getLastChild().attachChild(a);
 					}
 				}
 			}
 		);
 	}
 	
 
 }
