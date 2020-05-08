 package yang.graphics.translator;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import yang.graphics.model.FloatColor;
 import yang.graphics.textures.TextureData;
 import yang.graphics.textures.TextureProperties;
 import yang.graphics.textures.enums.TextureFilter;
 
 public class Texture extends AbstractTexture {
 
 	public static final int STATUS_UNINITIALIZED = 0;
 	public static final int STATUS_GENERATED = 1;
 	public static final int STATUS_FINISHED = 2;
 	public static final int STATUS_FREE = 3;
 
 	public GraphicsTranslator mGraphics;
 	public String mName;
 	public int mId;
 	public TextureProperties mProperties;
 	public int mStatus = STATUS_UNINITIALIZED;
 
 	protected Texture(GraphicsTranslator graphics) {
 		mGraphics = graphics;
 		mIsAlphaMap = false;
 	}
 
 	protected Texture(GraphicsTranslator graphics,TextureProperties properties) {
 		this(graphics);
 		mProperties = properties;
 	}
 
 	@Override
 	public void update(TextureData data) {
 		if(mIsAlphaMap)
 			mProperties.mChannels = 4;
 		else
 			mProperties.mChannels = data.mChannels;
 		super.update(data);
 	}
 
 //	public Texture(GraphicsTranslator graphics, ByteBuffer source, int width, int height, TextureProperties properties) {
 //		this(graphics);
 //		initCompletely(source, width, height, properties);
 //	}
 
 	public Texture generate() {
 		mId = mGraphics.genTexture();
 		mStatus = STATUS_GENERATED;
 		return this;
 	}
 
 	public void initCompletely(ByteBuffer source, int width, int height, TextureProperties properties) {
 		mWidth = width;
 		mHeight = height;
 		if(properties==null)
 			properties = new TextureProperties();
 		mProperties = properties;
 		generate();
 		if(source!=null) {
 			source.order(ByteOrder.nativeOrder());
 			source.rewind();
 		}
 		mGraphics.setTextureData(mId, mWidth,mHeight, source,mProperties);
 		if(source!=null)
 			finish();
 	}
 
 	public void initCompletely(TextureData source, TextureProperties properties) {
 		initCompletely(source.mData,source.mWidth,source.mHeight,properties);
 	}
 
 	public int getId() {
 		return mId;
 	}
 
 	public void setId(int id) {
 		this.mId = id;
 	}
 
 	public void free() {
 		mStatus = STATUS_FREE;
 		mGraphics.deleteTexture(mId);
 	}
 
 	@Override
 	public void update(ByteBuffer source,int width,int height) {
 		if(source!=null)
 			source.rewind();
 		mGraphics.setTextureData(mId, width,height, source, mProperties);
 		mWidth = width;
 		mHeight = height;
 		if(source!=null)
 			finish();
 		else
 			mStatus = STATUS_GENERATED;
 	}
 
 	public void resize(int width, int height) {
 		update(null,width,height);
 	}
 
 	/**
 	 * No mipmaps are generated
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 * @param data
 	 */
 	@Override
 	public void updateRect(int x, int y, int width, int height, ByteBuffer data) {
 		assert mGraphics.preCheck("Update rect");
 		if(x<0 || y<0 || x+width>mWidth || y+height>mHeight)
 			throw new RuntimeException("Texture rect values out of bounds: Rect x,y,w,h="+x+","+y+","+width+","+height+"; Texture w,h="+mWidth+","+mHeight);
 		mGraphics.setTextureRectData(this.mId, 0, x,y, width,height, mProperties.mChannels, data);
 		assert mGraphics.checkErrorInst("Update rect");
 	}
 
 	public void fillWithColor(FloatColor color) {
 		this.update(TextureData.createSingleColorBuffer(mWidth,mHeight, mProperties, color));
 	}
 
 	public void finish() {
 		if(mProperties.mFilter == TextureFilter.LINEAR_MIP_LINEAR || mProperties.mFilter == TextureFilter.NEAREST_MIP_LINEAR) {
 			mGraphics.bindTexture(this);
 			mGraphics.generateMipMap();
 			mGraphics.bindTextureNoFlush(mGraphics.mNoTexture,0);
 		}
 		if(mStatus<STATUS_FINISHED)
 			mStatus = STATUS_FINISHED;
 	}
 
 	@Override
 	public void finalize() {
 		assert (mStatus>=STATUS_GENERATED && mStatus<STATUS_FREE):"Texture garbage collected, but still in video memory";
 	}
 
 	@Override
 	public int getChannels() {
 		return mProperties.mChannels;
 	}
 
 	@Override
 	public boolean isFinished() {
 		return mStatus>=STATUS_FINISHED;
 	}
 
 	@Override
 	public boolean isFreed() {
 		return mStatus==STATUS_FREE;
 	}
 
 	public String statusToString() {
 		switch(mStatus) {
 		case STATUS_UNINITIALIZED: return "Uninitialized";
 		case STATUS_GENERATED: return "Generated";
 		case STATUS_FINISHED: return "Finished";
 		case STATUS_FREE: return "Free";
 		default: return "<undefined>";
 		}
 	}
 
 	@Override
 	public String toString() {
		return mName+": STATUS="+statusToString()+", PROPS=("+(mProperties==null?"null":mProperties.toString())+"), ALPHAMAP="+mIsAlphaMap;
 	}
 
 }
