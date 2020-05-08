 package org.andengine.extension.rubeloader.factory;
 
 import java.io.File;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.UncoloredSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.extension.rubeloader.ITextureProvider;
 import org.andengine.extension.rubeloader.def.ImageDef;
 import org.andengine.extension.rubeloader.json.AutocastMap;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.adt.color.ColorUtils;
 import org.andengine.util.exception.AndEngineRuntimeException;
 import org.andengine.util.math.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 
 import android.util.Log;
 
 /**
  * Simple implementation of IEntityFactory. It will try to create Sprite instance for
  * all the ImageDef provided.
  * 
  * @author nazgee
  *
  */
 public class EntityFactory implements IEntityFactory {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	private IEntity mSceneEntity;
 	private ITextureProvider mTextureProvider;
 	private VertexBufferObjectManager mVBOM;
 	public boolean isConfigured = false;
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	/**
 	 * @note when using this constructor you MUST call configure() before this factory is used!
 	 */
 	public EntityFactory() {
 		super();
 		configure(null, null, null);
 	}
 
 	/**
 	 * 
 	 * @param pSceneEntity Every created {@link IEntity} will be attached to this object
 	 * @param pTextureProvider Will be used to obtain {@link ITextureRegion} from a given {@link String} texture region name
 	 * @param pVBOM Every {@link IEntity} will be created using this {@link VertexBufferObjectManager}
 	 */
 	public EntityFactory(IEntity pSceneEntity, ITextureProvider pTextureProvider, VertexBufferObjectManager pVBOM) {
 		super();
 		configure(pSceneEntity, pTextureProvider, pVBOM);
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public void configure(IEntity pSceneEntity, ITextureProvider pTextureProvider, VertexBufferObjectManager pVBOM) {
 		this.mSceneEntity = pSceneEntity;
 		this.mTextureProvider = pTextureProvider;
 		this.mVBOM = pVBOM;
 		isConfigured = true;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	@Override
 	public IEntity produce(PhysicsWorld pWorld, ImageDef image, AutocastMap pAutocastMap) {
 		if (!isConfigured) {
 			throw new AndEngineRuntimeException("configure() was not called on this instance of " + getClass().getSimpleName());
 		}
 
 		final float p2m = getPixelToMeterRatio(image);
 		if (image.file == null) {
 			/*
 			 * This is strange, that I have to put this check here, but (apparently) it happens in some cases...
 			 */
 			Log.e(getClass().getSimpleName(), "No file name was given. This is either parsing or .json error. Substitute graphics will be created (simple red X with white border).");
 
 			Entity errorEntity = new Entity(image.center.x * p2m, image.center.y * p2m);
 
 			Rectangle bar1out = new Rectangle(0, 0, 110, 25, this.mVBOM);
 			Rectangle bar1in = new Rectangle(0, 0, 100, 20, this.mVBOM);
 			Rectangle bar2out = new Rectangle(0, 0, 110, 25, this.mVBOM);
 			Rectangle bar2in = new Rectangle(0, 0, 100, 20, this.mVBOM);
 
 			bar1out.setRotation(+45);
 			bar1in.setRotation(+45);
 			bar2out.setRotation(-45);
 			bar2in.setRotation(-45);
 
 			bar1out.setColor(Color.WHITE);
 			bar1in.setColor(Color.RED);
 			bar2out.setColor(Color.WHITE);
 			bar2in.setColor(Color.RED);
 
 			errorEntity.attachChild(bar1out);
 			errorEntity.attachChild(bar2out);
 			errorEntity.attachChild(bar1in);
 			errorEntity.attachChild(bar2in);
 			return errorEntity;
 		}
 		String wantedTextureRegion = new File(image.file).getName();
 		final ITextureRegion region = this.mTextureProvider.get(wantedTextureRegion);
 		if (region == null) {
 			throw new AndEngineRuntimeException("ITextureProvider returned null, when asked for " + wantedTextureRegion + " texture region");
 		}
 		final float scale = image.heightWorldUnits * p2m / region.getHeight();
 		final float w = region.getWidth() * scale * image.aspectScale;
 		final float h = region.getHeight() * scale;
 
 		final float x = image.center.x * p2m;
 		final float y = image.center.y * p2m;
 
 		return produceImpl(x, y, w, h, region, this.mVBOM, this.mSceneEntity, this.mTextureProvider, pWorld, image, pAutocastMap);
 	}
 
 
 	protected IEntity produceImpl(float x, float y, float w, float h, ITextureRegion region, VertexBufferObjectManager pVBOM, IEntity pSceneEntity, ITextureProvider pTextureProvider, PhysicsWorld pWorld, ImageDef pImageDef, AutocastMap pAutocastMap) {
 		float requestedRotationOffset;
 		float requestedRotation;
 
 		if (pImageDef.body != null) {
 			requestedRotation = 0; //physics connector will rotate appropriately
 			requestedRotationOffset = pImageDef.angle;
 		} else {
 			requestedRotation = pImageDef.angle;
 			requestedRotationOffset = 0;
 		}
 
 		
 		IEntity entity = createSprite(x, y, w, h, pImageDef.colorTint, region, pVBOM, Math.round(pImageDef.renderOrder), requestedRotation, requestedRotationOffset, pImageDef.flip);
 
 		if (entity != null) {
 			if (pImageDef.body != null) {
 				Vector2 dissplacement = Vector2Pool.obtain();
 
 				dissplacement.set(x, y);
 				dissplacement.rotate(MathUtils.radToDeg((float) (Math.PI - requestedRotationOffset)));
 				x = dissplacement.x;
 				y = dissplacement.y;
 
 				final float anchorCenterX = x / entity.getWidth() + 0.5f;
 				final float anchorCenterY = y / entity.getHeight() + 0.5f;
 
 				entity.setAnchorCenter(anchorCenterX, anchorCenterY);
 
 				PhysicsConnector connector = createPhysicsConnector(pWorld, pImageDef, entity);
 				entity.setUserData(connector);
 				pWorld.registerPhysicsConnector(connector);
 
 				Vector2Pool.recycle(dissplacement);
 			}
 
 			pSceneEntity.attachChild(entity);
 		}
 
 		return entity;
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
 	 * @param requestedRotation
 	 * @param requestedRotationOffset
 	 * @param pFlipped 
 	 * @return
 	 */
 	protected Sprite createSprite(final float pX, final float pY, final float pWidth, final float pHeight, final Color pColorTint, final ITextureRegion region, VertexBufferObjectManager pVBOM, final int pZindex, final float pRequestedRotation, final float pRequestedRotationOffset, boolean pFlipped) {
 		/**
 		 * We should get rid of this rotation mess, when setRotationOffset() will eventualy find it's way to
 		 * AndEngine main branch.
 		 */
 		Sprite sprite;
 		if (pColorTint.equals(Color.WHITE)) {
 			sprite = new UncoloredSprite(pX, pY, pWidth, pHeight, region, pVBOM) {
 				private final float mRotationOffset;
 				{
 					this.mRotationOffset = MathUtils.radToDeg(-pRequestedRotationOffset);
 				}
 	
 				@Override
 				protected void applyRotation(GLState pGLState) {
 					this.mRotation += this.mRotationOffset;
 					super.applyRotation(pGLState);
 					this.mRotation -= this.mRotationOffset;
 				}
 			};
 		} else {
 			sprite = new Sprite(pX, pY, pWidth, pHeight, region, pVBOM) {
 				private final float mRotationOffset;
 				{
 					this.mRotationOffset = MathUtils.radToDeg(-pRequestedRotationOffset);
 				}
 	
 				@Override
 				protected void applyRotation(GLState pGLState) {
 					this.mRotation += this.mRotationOffset;
 					super.applyRotation(pGLState);
 					this.mRotation -= this.mRotationOffset;
 				}
 			};
 		}
 		sprite.setColor(pColorTint);
 		//sprite.setRotationOffset(MathUtils.radToDeg(-pAngle));
 		sprite.setRotation(MathUtils.radToDeg(-pRequestedRotation));
 		sprite.setCullingEnabled(false);
 		sprite.setZIndex(pZindex);
 		sprite.setFlippedHorizontal(pFlipped);
 		return sprite;
 	}
 
 	protected PhysicsConnector createPhysicsConnector(PhysicsWorld pWorld, ImageDef pImageDef, IEntity pEntity) {
 		return new PhysicsConnector(pEntity, pImageDef.body);
 	}
 
 	protected float getPixelToMeterRatio(ImageDef pImage) {
 		return PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	}
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 }
