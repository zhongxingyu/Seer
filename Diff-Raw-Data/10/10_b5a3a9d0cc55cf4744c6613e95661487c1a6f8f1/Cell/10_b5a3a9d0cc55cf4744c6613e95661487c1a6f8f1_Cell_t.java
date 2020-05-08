 package eu.nazgee.prank.solar;
 
 import org.andengine.entity.modifier.ColorModifier;
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.color.Color;
 
 import eu.nazgee.game.utils.helpers.Positioner;
 
 public class Cell extends Rectangle {
 
 	private final Sprite mCellSprite;
	private boolean mIsActive = true;
 	private IEntityModifier mActiveMod;
 
 	public Cell(float pX, float pY, float pWidth, float pHeight,
 			Sprite pCellSprite,
 			VertexBufferObjectManager pVertexBufferObjectManager) {
 		super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
 		mCellSprite = pCellSprite;
 
 		attachChild(getCellSprite());
 		Positioner.setCentered(getCellSprite(), getWidth()/2, getHeight()/2);
 	}
 
 	public Sprite getCellSprite() {
 		return mCellSprite;
 	}
 
 	public boolean isActive() {
 		return mIsActive;
 	}
 
 	public void setIsActive(boolean mIsActive) {
 		if (isActive() != mIsActive) {
 			this.mIsActive = mIsActive;
 			forceRefresh();
 		}
 	}
 
 	public void forceRefresh() {
 		unregisterEntityModifier(mActiveMod);
 		if (isActive()) {
 			mActiveMod = new ColorModifier(0.8f, getColor(), Color.WHITE);
 			registerEntityModifier(mActiveMod);
 		} else {
 			mActiveMod = new ColorModifier(0.8f, getColor(), Color.BLACK);
 			registerEntityModifier(mActiveMod);
 		}
 	}
 }
