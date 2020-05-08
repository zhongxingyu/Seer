 package yang.graphics.font;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.math.objects.matrix.YangMatrix;
 
 public class DrawableString extends FixedString {
 	
 	public static int CHAR_LINEBREAK = '\n';
 	public static int CHAR_TAB = '\t';
 	public static int CHAR_SPACE = ' ';
 	public static int CHAR_WORD_SPLITTER = '';
 	protected static float LINEBREAK_FLOAT = Float.MIN_VALUE;
 	public static float DEFAULT_LINE_WIDTH = Float.MAX_VALUE;
 	
 	public static final float ANCHOR_LEFT = 0;
 	public static final float ANCHOR_CENTER = 0.5f;
 	public static final float ANCHOR_RIGHT = 1;
 	public static final float ANCHOR_TOP = 0;
 	public static final float ANCHOR_MIDDLE = 0.5f;
 	public static final float ANCHOR_BOTTOM = 1;
 	
 	public static DefaultGraphics<?> DEFAULT_GRAPHICS;
 	public static BitmapFont DEFAULT_FONT;
 	public static float DEFAULT_HORIZONTAL_ANCHOR = ANCHOR_LEFT;
 	public static float DEFAULT_VERTICAL_ANCHOR = ANCHOR_TOP;
 	
 	protected static float[] staticOffsets = new float[2048];
 	protected static float[] staticPositions = new float[2048];
 	
 	//References
 	protected DefaultGraphics<?> mGraphics;
 	
 	//Properties
 	//TODO create class
 	public float mLineHeight = 1;
 	public boolean mKerningEnabled = true;
 	public BitmapFont mFont;
 	public boolean mHasZComponent;
 	public float mAdditionalSpacing = 0;
 	public int mTabs = 4;
 	public boolean mIgnoreSpaceAtLineStart = true;
 	public float mVerticalAnchor;
 	public float mHorizontalAnchor;
 	public float mMaxLineWidth = Float.MAX_VALUE;
 	
 	//String properties
 	public int mRecentCharCount = 0;
 	public int mRecentLineCount;
 	public float mRecentStringWidth = 0;
 	public float mRecentStringHeight = 0;
 
 	//String data
 	protected int[] mInterChars;
 	public TextureCoordinatesQuad[] mTexCoords;
 	protected float[] mConstantPositions;
 	public int[] mLetters;
 	
 	public DrawableString() {
 		super();
 		setDefaults();
 	}
 	
 	public DrawableString(String string) {
 		this();
 		allocString(string);
 	}
 	
 	public DrawableString(int capacity) {
 		this();
 		allocString(capacity);
 	}
 	
 	public DrawableString allocString(int capacity) {
 		super.allocString(capacity);
 		mTexCoords = new TextureCoordinatesQuad[capacity];
 		mLetters = new int[capacity];
 		return this;
 	}
 	
 	protected void setDefaults() {
 		setGraphics(DEFAULT_GRAPHICS);
 		setFont(DEFAULT_FONT);
 		mVerticalAnchor = DEFAULT_VERTICAL_ANCHOR;
 		mHorizontalAnchor = DEFAULT_HORIZONTAL_ANCHOR;
 		mMaxLineWidth = DEFAULT_LINE_WIDTH;
 	}
 	
 	public DrawableString setGraphics(DefaultGraphics<?> graphics) {
 		mGraphics = graphics;
 		if(mGraphics!=null)
 			mHasZComponent = mGraphics.mPositionDimension==3;
 		return this;
 	}
 	
 	public DrawableString setMaxLineWidth(float lineWidth) {
 		mMaxLineWidth = lineWidth;
 		return this;
 	}
 	
 	public DrawableString setFont(BitmapFont font) {
 		mFont = font;
 		return this;
 	}
 	
 	public DrawableString setHasZComponent(boolean hasZComponent) {
 		mHasZComponent = hasZComponent;
 		return this;
 	}
 	
 	public void createStringPositions(float[] positionTarget,float[] offsetsTarget) {
 		int c=0;
 		int o=0;
 		float lineShift = 0;
 		int lstSpace = -1;
 		float spaceCharX = -1;
 		mRecentStringWidth = 0;
 		mRecentCharCount = 0;
 		mRecentLineCount = 1;
 		mRecentStringHeight = 0;
 		boolean wordSplit = false;
 		float spacing = mFont.mSpacing+mAdditionalSpacing;
 		int lstVal = -1;
 		int lstSpaceVal = -1;
 		int curLineCharCount = 0;
 		float spaceWidth;
 		if(mKerningEnabled)
 			spaceWidth = mFont.mSpaceWidth;
 		else
 			spaceWidth = mFont.mConstantCharDistance;
 		spaceWidth += mAdditionalSpacing;
 		
 		float charX = 0;
 		float charY = -mLineHeight;
 		int i;
 		int count = mLength+1;
 		for(i=0;i<count;i++) {
 			int val;
 			if(i==mLength)
 				val = CHAR_LINEBREAK;
 			else
 				val = mChars[i];
 			if(val>0) {
 				
 				if(val == CHAR_SPACE) {
 					//Space
 					if(lstVal>0 && (!mIgnoreSpaceAtLineStart || curLineCharCount>0)) {
 						lstSpace = i;
 						lstSpaceVal = lstVal;
 						
 						wordSplit = false;
 						if(mKerningEnabled) {
 							spaceCharX = charX+mFont.mKerningMaxX[lstVal];
 							charX = spaceCharX+mFont.mSpaceWidth;
 						}else{
 							spaceCharX = charX + spacing;
 							charX += spacing*2;
 						}
 					}
 					lstVal = -1;
 				}else if(val == CHAR_WORD_SPLITTER){
 					lstSpace = i;
 					lstSpaceVal = lstVal;
 					spaceCharX = charX;
 					wordSplit = true;
 					if(mKerningEnabled) {
 						spaceCharX = charX+mFont.mKerningMaxX[lstVal];
 					}else{
 						spaceCharX = charX + spacing;
 					}
 				}else if(val!=CHAR_LINEBREAK) {
 					if(val == CHAR_TAB) {
 						//Tab
						if(mKerningEnabled) {
							charX += spaceWidth*mTabs;
						}else{
							int tabs = (curLineCharCount/mTabs+1)*mTabs-curLineCharCount;
							charX += spaceWidth*tabs;
						}
 						lstVal = -1;
 					}else if(val>=0) {
 						//Character
 						if(lstVal>0) {
 							if(mKerningEnabled) {
 								//Kerning
 								float maxKernShift = 0;
 								float[] kernBoxes1 = mFont.mKerningValues[lstVal];
 								float[] kernBoxes2 = mFont.mKerningValues[val];
 								for(int k=0;k<mFont.mKernBoxes;k++) {
 									float shift = kernBoxes1[k*2+1]-kernBoxes2[k*2];
 									if(shift>maxKernShift) {
 										maxKernShift = shift;
 									}
 								}
 								charX += maxKernShift+spacing;
 							}else
 								charX += mFont.mConstantCharDistance;
 							charX += mAdditionalSpacing;
 						}else{
 							//first char of line
 							if(curLineCharCount==0) {
 								if(mKerningEnabled) {
									charX -= -mFont.mKerningMinX[val];
 								}
 							}
 						}
 						
 						int uVal = val;
 						TextureCoordinatesQuad coords = mFont.mCoordinates[val];
 						float w = mFont.mWidths[val];
 						float uX;
 						if(mKerningEnabled)
 							uX = charX;
 						else
 							uX = charX+mFont.mConstantCharDistance*0.5f-w*0.5f;
 						
 						if(mMaxLineWidth<Float.MAX_VALUE && uX+mFont.mKerningMaxX[val]>mMaxLineWidth-0.2f && lstSpace>=0) {
 							//Auto line break
 							int charCount = i-lstSpace-1;
 							val = CHAR_LINEBREAK;
 							i = lstSpace;
 							o -= charCount;
 							c -= charCount*(mHasZComponent?3:2)*4;
 							charX = spaceCharX;
 							lstVal = lstSpaceVal;
 							mRecentCharCount -= charCount;
 							if(wordSplit) {
 								uVal = '-';
 								uX = charX;
 								coords = mFont.mCoordinates[uVal];
 								w = mFont.mWidths[uVal];
 							}else
 								uVal = CHAR_LINEBREAK;
 						}
 						if(uVal!=CHAR_LINEBREAK) {
 							//Add char
 							float h = mFont.mHeights[uVal];
 							mLetters[mRecentCharCount] = uVal;
 							mTexCoords[mRecentCharCount] = coords;
 							
 							if(positionTarget!=null) {
 								positionTarget[c++] = uX;
 								positionTarget[c++] = charY;
 								if(mHasZComponent)
 									positionTarget[c++] = 0;
 								positionTarget[c++] = uX + w;
 								positionTarget[c++] = charY;
 								if(mHasZComponent)
 									positionTarget[c++] = 0;
 								positionTarget[c++] = uX;
 								positionTarget[c++] = charY + h;
 								if(mHasZComponent)
 									positionTarget[c++] = 0;
 								positionTarget[c++] = uX + w;
 								positionTarget[c++] = charY + h;
 								if(mHasZComponent)
 									positionTarget[c++] = 0;
 							}
 							if(offsetsTarget!=null) {
 								offsetsTarget[o++] = uX;
 							}
 							mRecentCharCount++;
 							curLineCharCount++;
 							lstVal = uVal;
 						}
 						
 					}
 				}
 			}
 				
 			if(val == CHAR_LINEBREAK) {
 				//Line break
 				if(lstVal>0) {
 					if(mKerningEnabled) {
 						charX += mFont.mKerningMaxX[lstVal]+spacing;
 					}else{
 						charX += mFont.mConstantCharDistance+spacing;
 					}
 				}
 				if(charX>mRecentStringWidth)
 					mRecentStringWidth = charX;
 				lstVal = -1;
 				lstSpace = -1;
 				charY -= mLineHeight;
 				curLineCharCount = 0;
 				mRecentLineCount++;
 				mRecentStringHeight+=mLineHeight;
 				if(offsetsTarget!=null) {
 					if(mHorizontalAnchor>0) {
 						int k=o-1;
 						float shift = -(charX+lineShift)*mHorizontalAnchor;
 						while(k>=0 && offsetsTarget[k]!=LINEBREAK_FLOAT) {
 							offsetsTarget[k] += shift;
 							k--;
 						}
 					}
 					offsetsTarget[o++] =LINEBREAK_FLOAT;
 				}
				charX = 0;
 			}
 			
 		}
 		
 	}
 
 	public DrawableString setConstant() {
 		if(mConstantPositions==null)
 			mConstantPositions = new float[mCapacity*(mHasZComponent?3:2)*4];
 		createStringPositions(mConstantPositions,null);
 		applyAnchors(mHorizontalAnchor,mVerticalAnchor,mConstantPositions);
 		return this;
 	}
 	
 	protected void applyAnchors(float horizontal,float vertical,float[] positionTarget) {
 		float shiftX = -horizontal*mRecentStringWidth;
 		float shiftY = vertical*mRecentStringHeight;
 		int c = 0;
 		for(int i=0;i<mRecentCharCount*4;i++) {
 			positionTarget[c++] += shiftX;
 			positionTarget[c++] += shiftY;
 			if(mHasZComponent)
 				c++;
 		}
 	}
 
 	
 	public float calcNormalizedStringWidth() {
 		if(mConstantPositions==null)
 			createStringPositions(null,null);
 		return mRecentStringWidth;
 	}
 	
 	public DrawableString setHorizontalAnchor(float anchor) {
 		mHorizontalAnchor = anchor;
 		return this;
 	}
 	
 	public DrawableString setVerticalAnchor(float anchor) {
 		mVerticalAnchor = anchor;
 		return this;
 	}
 	
 	public DrawableString setAnchors(float horizontalAnchor,float verticalAnchor) {
 		mHorizontalAnchor = horizontalAnchor;
 		mVerticalAnchor = verticalAnchor;
 		return this;
 	}
 	
 	protected void putColors() {
 		for(int i=0;i<mRecentCharCount;i++) {
 			mGraphics.putColorRect(mGraphics.mCurColor);
 			mGraphics.putSuppDataRect(mGraphics.mCurSuppData);
 		}
 	}
 
 	protected void putVertexProperties() {
 		IndexedVertexBuffer vertexBuffer = mGraphics.mCurrentVertexBuffer;
 		short offset = (short)vertexBuffer.getCurrentVertexWriteCount();
 		for(int i=0;i<mRecentCharCount;i++) {
 			vertexBuffer.beginQuad(false,offset);
 			vertexBuffer.putArray(DefaultGraphics.ID_TEXTURES, mTexCoords[i].mAppliedCoordinates);
 			offset += 4;
 		}
 		
 		putColors();
 	}
 	
 	public void draw(YangMatrix transform) {
 		float[] positions;
 		YangMatrix resultTransf;
 		if(mConstantPositions==null){
 			createStringPositions(staticPositions,null);
 			positions = staticPositions;
 			mGraphics.mInterTransf2.loadIdentity();
 			mGraphics.mInterTransf2.translate(-mRecentStringWidth*mHorizontalAnchor, mRecentStringHeight*mVerticalAnchor);
 			mGraphics.mInterTransf2.multiplyLeft(transform);
 			resultTransf = mGraphics.mInterTransf2;
 		}else{
 			positions = mConstantPositions;
 			resultTransf = transform;
 		}
 		
 		IndexedVertexBuffer vertexBuffer = mGraphics.mCurrentVertexBuffer;
 		
 		mGraphics.mTranslator.bindTexture(mFont.mTexture);
 		
 		putVertexProperties();
 
 		if(mGraphics.mPositionDimension==2)
 			vertexBuffer.putTransformedArray2D(DefaultGraphics.ID_POSITIONS,positions,mRecentCharCount*4,resultTransf.mMatrix);
 		else
 			vertexBuffer.putTransformedArray3D(DefaultGraphics.ID_POSITIONS,positions,mRecentCharCount*4,resultTransf.mMatrix);
 	}
 	
 	public void draw(float x,float y,float lineHeight) {
 		mGraphics.mInterTransf1.loadIdentity();
 		mGraphics.mInterTransf1.translate(x, y);
 		mGraphics.mInterTransf1.scale(lineHeight);
 		draw(mGraphics.mInterTransf1);
 	}
 	
 	public void draw(float x,float y,float lineHeight,float rotation) {
 		mGraphics.mInterTransf1.loadIdentity();
 		mGraphics.mInterTransf1.translate(x, y);
 		mGraphics.mInterTransf1.scale(lineHeight);
 		mGraphics.mInterTransf1.rotateZ(rotation);
 		draw(mGraphics.mInterTransf1);
 	}
 	
 }
