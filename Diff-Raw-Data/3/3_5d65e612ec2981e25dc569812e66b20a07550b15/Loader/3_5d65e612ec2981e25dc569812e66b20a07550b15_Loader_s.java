 package org.andengine.extension.rubeloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import net.minidev.json.parser.ParseException;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.UncoloredSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.extension.rubeloader.def.ImageDef;
 import org.andengine.extension.rubeloader.def.RubeDef;
 import org.andengine.extension.rubeloader.parser.RubeParser;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.StreamUtils;
 import org.andengine.util.adt.io.in.ResourceInputStreamOpener;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.math.MathUtils;
 
 import android.content.res.Resources;
 
 import com.badlogic.gdx.physics.box2d.Body;
 
 /**
  * Simple example of a customr R.U.B.E. loader
  * @author Michal Stawinski (nazgee)
  */
 public class Loader {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private RubeParser mRubeParser;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	public Loader() {
 		this.mRubeParser = new RubeParser();
 	}
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	protected void handleImages(VertexBufferObjectManager pVBOM, IEntity pSceneEntity, ITextureProvider pTextureProvider, RubeDef pRubeDef) {
 		Vector<ImageDef> imgs = pRubeDef.primitives.images;
 		for (ImageDef image : imgs) {
 			final float p2m = getPixelToMeterRatio(image);
 			final ITextureRegion region = pTextureProvider.get(new File(image.file).getName());
 			final float scale = image.scale * p2m / region.getHeight();
 			final float w = region.getWidth() * scale;
 			final float h = region.getHeight() * scale;
 			final float x = image.center.x * p2m;
 			final float y = image.center.y * p2m;
 
 			handleImage(x, y, w, h, region, pVBOM, pSceneEntity, pTextureProvider, pRubeDef, image);
 		}
 	}
 
 	protected void handleImage(float x, float y, float w, float h, ITextureRegion region, VertexBufferObjectManager pVBOM, IEntity pSceneEntity, ITextureProvider pTextureProvider, RubeDef pRubeDef, ImageDef pImageDef) {
 		IEntity entity = populateEntity(x, y, w, h, region, pVBOM, (int)pImageDef.renderOrder, pImageDef.angle);
 
 		if (entity != null) {
 			if (pImageDef.body != null) {
 
 				PhysicsConnector connector = populatePhysicsConnector(pRubeDef, pImageDef.body, entity);
 				pImageDef.body.setUserData(connector);
 				entity.setUserData(connector);
 				pRubeDef.world.registerPhysicsConnector(connector);
 			}
 
 			pSceneEntity.attachChild(entity);
 		}
 	}
 
 	/**
 	 * 
 	 * @param pX
 	 * @param pY
 	 * @param pWidth
 	 * @param pHeight
 	 * @param region
 	 * @param pVBOM
 	 * @param pZindex
 	 * @param pAngle
 	 * @return
 	 */
 	protected Sprite populateEntity(final float pX, final float pY, final float pWidth, final float pHeight, final ITextureRegion region,
 			VertexBufferObjectManager pVBOM, final int pZindex, final float pAngle) {
 		Sprite sprite = new UncoloredSprite(pX, pY, pWidth, pHeight, region, pVBOM);
 		sprite.setRotationOffset(MathUtils.radToDeg(-pAngle));
		sprite.setAnchorCenter(-pX / pWidth + 0.5f, -pY / pHeight + 0.5f);
 		sprite.setCullingEnabled(true);
 		sprite.setZIndex(pZindex);
 		return sprite;
 	}
 
 	protected PhysicsConnector populatePhysicsConnector(RubeDef pRube, Body pBody, IEntity pEntity) {
 		return new PhysicsConnector(pEntity, pBody);
 	}
 
 	protected float getPixelToMeterRatio(ImageDef pImage) {
 		return PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	}
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	public RubeDef loadMoreToExistingWorld(final Resources pResources, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, int resId, final PhysicsWorld pWorld) {
 		long startTime = System.currentTimeMillis();
 
 		RubeDef rube;
 		try {
 			rube = mRubeParser.continueParse(pWorld, readResource(resId, pResources));
 		} catch (ParseException e) {
 			throw new RuntimeException("RUBE json parsing failed! ", e);
 		}
 		handleImages(pVBOM, pSceneEntity, pTextureProvider, rube);
 
 		long elapseTime = System.currentTimeMillis() - startTime;
 		Debug.w("RubeLoaderExtension LOAD_TIME=" + elapseTime/1000.f);
 
 		return rube;
 	}
 
 	public RubeDef load(final Resources pResources, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, int resId) {
 		long startTime = System.currentTimeMillis();
 
 		RubeDef rube;
 		try {
 			rube = mRubeParser.parse(readResource(resId, pResources));
 		} catch (ParseException e) {
 			throw new RuntimeException("RUBE json parsing failed! ", e);
 		}
 		handleImages(pVBOM, pSceneEntity, pTextureProvider, rube);
 
 		long elapseTime = System.currentTimeMillis() - startTime;
 		Debug.w("RubeLoaderExtension LOAD_TIME=" + elapseTime/1000.f);
 
 		return rube;
 	}
 
 	public static String readResource(int resId, Resources pResources) {
 		try {
 			return StreamUtils.readFully(new ResourceInputStreamOpener(pResources, resId).open());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return null;
 		}
 	}
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	public static interface ITextureProvider {
 		public ITextureRegion get(final String pFileName);
 	}
 }
