 package com.secondhand.view.opengl;
 
 import static org.anddev.andengine.util.constants.Constants.VERTEX_INDEX_X;
 import static org.anddev.andengine.util.constants.Constants.VERTEX_INDEX_Y;
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.collision.RectangularShapeCollisionChecker;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.shape.RectangularShape;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.util.GLHelper;
 import org.anddev.andengine.util.MathUtils;
 
 public class RandomRepeatingBackground extends RectangularShape {
 
 	private final int tileColumns;
 	private final int tileRows;
 
 	private final int tileWidth;
 	private final int tileHeight;
 
 	private final TextureRegion[][] textureRegions;
 
 	private final float[] mCullingVertices = new float[2 * 4];
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public RandomRepeatingBackground(
 			final List<TextureRegion> baseTextureRegions, final float width,
 			final float height) {
 		super(0, 0, 0, 0, null);
 
 		final TextureRegion first = baseTextureRegions.get(0);
 		for (int i = 1; i < baseTextureRegions.size(); ++i) {
 			final TextureRegion other = baseTextureRegions.get(i);
 			if (other.getWidth() != first.getWidth()
 					|| other.getHeight() != first.getHeight()) {
 				throw new IllegalArgumentException(
 						"all base texture regions must be of the same size!");
 			}
 		}
 
 		this.tileColumns = (int) Math.ceil(width / (float) first.getWidth());
 		this.tileRows = (int) Math.ceil(height / (float) first.getHeight());
 
 		this.tileWidth = first.getWidth();
 		this.tileHeight = first.getHeight();
 
 		// place them out randomly
 		this.textureRegions = new TextureRegion[this.tileRows][this.tileColumns];
 
 		for (int row = 0; row < this.tileRows; ++row) {
 			for (int col = 0; col < this.tileRows; ++col) {
 				this.textureRegions[row][col] = first;
 			}
 		}
 
 		super.mWidth = width;
 		super.mBaseWidth = width;
 
 		super.mHeight = height;
 		super.mBaseHeight = height;
 
 		this.mRotationCenterX = width * 0.5f;
 		this.mRotationCenterY = height * 0.5f;
 
 		this.mScaleCenterX = this.mRotationCenterX;
 		this.mScaleCenterY = this.mRotationCenterY;
 
 	}
 
 	@Override
 	@Deprecated
 	public void setRotation(final float pRotation) {
 
 	}
 
 	@Override
 	protected void onUpdateVertexBuffer() {
 		/* Nothing. */
 	}
 
 	@Override
 	protected void onInitDraw(final GL10 pGL) {
 		super.onInitDraw(pGL);
 
 		GLHelper.enableTextures(pGL);
 		GLHelper.enableTexCoordArray(pGL);
 	}
 
 	// TODO: should we use some kind of shared vertex buffer to speed things up?
 	// Refer to TMXLayer for inspiration.
/*
 	@Override
 	protected void onApplyVertices(final GL10 pGL) {
 		if (GLHelper.EXTENSIONS_VERTEXBUFFEROBJECTS) {
 			// final GL11 gl11 = (GL11)pGL;
 
			//this.mTMXTiledMap.getSharedVertexBuffer().selectOnHardware(gl11);
			//GLHelper.vertexZeroPointer(gl11);
 		} else {
		GLHelper.vertexPointer(pGL,
		//	 this.mTMXTiledMap.getSharedVertexBuffer().getFloatBuffer());
 		}
 	}
*/
 	@Override
 	protected void drawVertices(final GL10 pGL, final Camera pCamera) {
 
 		final float scaledTileWidth = tileWidth * this.mScaleX;
 		final float scaledTileHeight = tileHeight * this.mScaleY;
 
 		final float[] cullingVertices = this.mCullingVertices;
 		RectangularShapeCollisionChecker.fillVertices(this, cullingVertices);
 
 		final float layerMinX = cullingVertices[VERTEX_INDEX_X];
 		final float layerMinY = cullingVertices[VERTEX_INDEX_Y];
 
 		final float cameraMinX = pCamera.getMinX();
 		final float cameraMinY = pCamera.getMinY();
 		final float cameraWidth = pCamera.getWidth();
 		final float cameraHeight = pCamera.getHeight();
 
 		/* Determine the area that is visible in the camera. */
 		final float firstColumnRaw = (cameraMinX - layerMinX) / scaledTileWidth;
 		final int firstColumn = MathUtils.bringToBounds(0, tileColumns - 1,
 				(int) Math.floor(firstColumnRaw));
 		final int lastColumn = MathUtils
 				.bringToBounds(
 						0,
 						tileColumns - 1,
 						(int) Math.ceil(firstColumnRaw + cameraWidth
 								/ scaledTileWidth));
 
 		final float firstRowRaw = (cameraMinY - layerMinY) / scaledTileHeight;
 		final int firstRow = MathUtils.bringToBounds(0, tileRows - 1,
 				(int) Math.floor(firstRowRaw));
 		final int lastRow = MathUtils
 				.bringToBounds(
 						0,
 						tileRows - 1,
 						(int) Math.floor(firstRowRaw + cameraHeight
 								/ scaledTileHeight));
 
 		final int visibleTilesTotalWidth = (lastColumn - firstColumn + 1)
 				* tileWidth;
 
 		pGL.glTranslatef(firstColumn * tileWidth, firstRow * tileHeight, 0);
 
 		for (int row = firstRow; row <= lastRow; row++) {
 			final TextureRegion[] tileRow = textureRegions[row];
 
 			for (int column = firstColumn; column <= lastColumn; column++) {
 				final TextureRegion textureRegion = tileRow[column];
 				if (textureRegion != null) {
 					textureRegion.onApply(pGL);
 
 					pGL.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
 				}
 				pGL.glTranslatef(tileWidth, 0, 0);
 			}
 			/*
 			 * Translate one row downwards and the back left to the first
 			 * column. Just like the 'Carriage Return' + 'New Line' (\r\n) on a
 			 * typewriter.
 			 */
 			pGL.glTranslatef(-visibleTilesTotalWidth, tileHeight, 0);
 		}
 		pGL.glLoadIdentity();
 	}
 
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 		/* Nothing. */
 	}
 
 }
