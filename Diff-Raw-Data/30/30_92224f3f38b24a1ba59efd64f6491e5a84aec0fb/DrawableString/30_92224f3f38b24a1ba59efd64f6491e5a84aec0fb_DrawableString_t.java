 package yang.graphics.font;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.model.FloatColor;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.math.objects.matrix.YangMatrix;
 
 public class DrawableString extends FixedString {
 
 	public static StringProperties DEFAULT_PROPERTIES;
 	protected static YangMatrix interMatrix;
 	protected static YangMatrix interMatrix2;
 
 	public static int MAX_DYNAMIC_CHARS = 10000;
 	public static int CHAR_MACRO = 1024;
 	public static int CHAR_WORD_SPLITTER = '~';
 	protected static float LINEBREAK_FLOAT = Float.MIN_VALUE;
 	public static float DEFAULT_LINE_WIDTH = Float.MAX_VALUE;
 	public static float DEFAULT_SIZE = 1;
 
 	public static final float ANCHOR_LEFT = 0;
 	public static final float ANCHOR_CENTER = 0.5f;
 	public static final float ANCHOR_RIGHT = 1;
 	public static final float ANCHOR_TOP = 0;
 	public static final float ANCHOR_MIDDLE = 0.5f;
 	public static final float ANCHOR_BOTTOM = 1;
 	public float mSize;
 	protected boolean mPropertiesCloned = false;
 
 	public static float DEFAULT_HORIZONTAL_ANCHOR = ANCHOR_LEFT;
 	public static float DEFAULT_VERTICAL_ANCHOR = ANCHOR_TOP;
 
 	protected static float[] staticOffsets = new float[MAX_DYNAMIC_CHARS];
 	protected static float[] staticPositions = new float[MAX_DYNAMIC_CHARS*4*3];
 
 	//public NonConcurrentList<Pair<FloatColor,Integer>> mColors;
 
 	//Properties
 	public float mVerticalAnchor;
 	public float mHorizontalAnchor;
 	public StringProperties mProperties;
 	public float mMaxLineWidth = Float.MAX_VALUE;
 	public float mShiftZ = 0;
 
 	//String attributes
 	public int mRecentCharCount = 0;
 	public int mRecentLineCount;
 	public float mRecentStringWidth = 0;
 	public float mRecentStringHeight = 0;
 
 	//String data
 	protected int[] mInterChars;
 	public TextureCoordinatesQuad[] mTexCoords;
 	protected float[] mConstantPositions;
 	public int[] mLetters;
 
 	//public FloatColor[] mLetterColors;
 	/**
 	 * Swallow, read only!
 	 */
 	public FloatColor[] mWorkingColors;
 
 	public DrawableString() {
 		super();
 		setDefaults();
 		//mLetterColors = null;
 		mWorkingColors = null;
 		if(interMatrix==null) {
 			interMatrix = new YangMatrix();
 			interMatrix2 = new YangMatrix();
 		}
 	}
 
 	public DrawableString(String string) {
 		this();
 		allocString(string);
 	}
 
 	public DrawableString(int capacity) {
 		this();
 		alloc(capacity);
 	}
 
 	public DrawableString(String text, boolean formated) {
 		this();
 		if(formated) {
 			allocFormatString(text);
 		} else {
 			allocString(text);
 		}
 	}
 
 	@Override
 	public DrawableString alloc(int capacity) {
 		super.alloc(capacity);
 		mTexCoords = new TextureCoordinatesQuad[capacity];
 		mLetters = new int[capacity];
 		return this;
 	}
 
 	public DrawableString setSize(float size) {
 		mSize = size;
 		return this;
 	}
 
 	public DrawableString setFont(BitmapFont font) {
 		cloneProperties(false);
 		mProperties.mFont = font;
 		return this;
 	}
 
 	public DrawableString setKerningEnabled(boolean enabled) {
 		cloneProperties(false);
 		mProperties.mKerningEnabled = enabled;
 		return this;
 	}
 
 	public DrawableString setProperties(StringProperties settings) {
 		mProperties = settings;
 		return this;
 	}
 
 	protected void setDefaults() {
 		setProperties(DEFAULT_PROPERTIES);
 		mVerticalAnchor = DEFAULT_VERTICAL_ANCHOR;
 		mHorizontalAnchor = DEFAULT_HORIZONTAL_ANCHOR;
 		mMaxLineWidth = DEFAULT_LINE_WIDTH;
 		mSize = DEFAULT_SIZE;
 	}
 
 	public DrawableString setMaxLineWidth(float lineWidth) {
 		mMaxLineWidth = lineWidth;
 		return this;
 	}
 
 	public void createStringPositions(float[] positionTarget,float[] offsetsTarget) {
 		//TODO handle invalid characters
 		int c = 0;
 		int o = 0;
 		int clId = 0;
 		final BitmapFont font = mProperties.mFont;
 		final boolean kerningEnabled = mProperties.mKerningEnabled;
 		final float lineShift = 0;
 		int lstSpaceO = -1;
 		int lstSpaceI = -1;
 		float spaceCharX = -1;
 		FloatColor curColor = mProperties.getColor(0);
 		mRecentStringWidth = 0;
 		mRecentCharCount = 0;
 		mRecentLineCount = 1;
 		mRecentStringHeight = 0;
 		final float lineWidth = mMaxLineWidth/mSize-0.2f;
 		boolean wordSplit = false;
 		final float spacing = font.mSpacing+mProperties.mAdditionalSpacing;
 		int lstVal = -1;
 		int lstSpaceVal = -1;
 		int curLineCharCount = 0;
 		float spaceWidth;
 		if(kerningEnabled)
 			spaceWidth = font.mSpaceWidth;
 		else
 			spaceWidth = font.mConstantCharDistance;
 		spaceWidth += mProperties.mAdditionalSpacing;
 
 		float charX = 0;
 		float charY = -mProperties.mLineHeight;
 		int i;
 		final int count = mLength+1;
 		for(i=0;i<count;i++) {
 			int val;
 			if(i==mLength)
 				val = CHAR_LINEBREAK;
 			else
 				val = mChars[i];
 			if(val>0) {
 
 				if(val == CHAR_MACRO) {
 					i++;
 					curColor = mProperties.mStyle.mPalette[mChars[i]];
 				}else{
 					if(val == CHAR_SPACE) {
 						//Space
 						if(lstVal>0 && (!mProperties.mIgnoreSpaceAtLineStart || curLineCharCount>0)) {
 							lstSpaceI = i;
 							lstSpaceO = o;
 							lstSpaceVal = lstVal;
 
 							wordSplit = false;
 							if(kerningEnabled) {
 								spaceCharX = charX+font.mKerningMaxX[lstVal];
 								charX = spaceCharX+font.mSpaceWidth;
 							}else{
								spaceCharX = charX + spaceWidth + spacing;
								charX += spaceWidth*2;
 							}
 						}
 						lstVal = -1;
 					}else if(val == CHAR_WORD_SPLITTER){
 						lstSpaceI = i;
 						lstSpaceO = o;
 						lstSpaceVal = lstVal;
 						spaceCharX = charX;
 						wordSplit = true;
 						if(kerningEnabled) {
 							spaceCharX = charX+font.mKerningMaxX[lstVal];
 						}else{
 							spaceCharX = charX + spacing;
 						}
 					}else if(val!=CHAR_LINEBREAK) {
 						if(val == CHAR_TAB) {
 							//Tab
 							if(kerningEnabled) {
 								charX += spaceWidth*mProperties.mTabs;
 							}else{
 								final int tabs = (curLineCharCount/mProperties.mTabs+1)*mProperties.mTabs-curLineCharCount;
 								charX += spaceWidth*tabs;
 							}
 							lstVal = -1;
 						}else if(val>=0) {
 							//Character
 							if(lstVal>0) {
 								if(kerningEnabled) {
 									//Kerning
 									float maxKernShift = 0;
 									final float[] kernBoxes1 = font.mKerningValues[lstVal];
 									final float[] kernBoxes2 = font.mKerningValues[val];
 									for(int k=0;k<font.mKernBoxes;k++) {
 										final float shift = kernBoxes1[k*2+1]-kernBoxes2[k*2];
 										if(shift>maxKernShift) {
 											maxKernShift = shift;
 										}
 									}
 									charX += maxKernShift+spacing;
 								}else
 									charX += font.mConstantCharDistance;
 								charX += mProperties.mAdditionalSpacing;
 							}else{
 								//first char of line
 								if(curLineCharCount==0) {
 									if(kerningEnabled) {
 										charX -= -font.mKerningMinX[val];
 									}
 								}
 							}
 
 							int uVal = val;
 							TextureCoordinatesQuad coords = font.mCoordinates[val];
 							float w = font.mWidths[val];
 							float uX;
 							if(kerningEnabled)
 								uX = charX;
 							else
 								uX = charX+font.mConstantCharDistance*0.5f-w*0.5f;
 
 							if(lineWidth<Float.MAX_VALUE && uX+font.mKerningMaxX[val]>lineWidth && lstSpaceI>=0) {
 								//Auto line break
 								final int charCount = o-lstSpaceO;
 								val = CHAR_LINEBREAK;
 								i = lstSpaceI;
 								o = lstSpaceO;
 								clId -= charCount;
 								c -= charCount*(mProperties.mPosDim)*4;
 								charX = spaceCharX;
 								lstVal = lstSpaceVal;
 								mRecentCharCount -= charCount;
 								if(wordSplit) {
 									uVal = '-';
									if(kerningEnabled)
										uX = charX;
									else
										uX = charX+font.mConstantCharDistance;
 									coords = font.mCoordinates[uVal];
 									w = font.mWidths[uVal];
 								}else
 									uVal = CHAR_LINEBREAK;
 							}
 							if(uVal!=CHAR_LINEBREAK) {
 								//Add char
 								final float h = font.mHeights[uVal];
 								mLetters[mRecentCharCount] = uVal;
 								mTexCoords[mRecentCharCount] = coords;
 								final float uY = charY;
 
 								if(positionTarget!=null) {
 									positionTarget[c++] = uX;
 									positionTarget[c++] = uY;
 									if(mProperties.mHasZComponent)
 										positionTarget[c++] = mShiftZ;
 									positionTarget[c++] = uX + w;
 									positionTarget[c++] = uY;
 									if(mProperties.mHasZComponent)
 										positionTarget[c++] = mShiftZ;
 									positionTarget[c++] = uX;
 									positionTarget[c++] = uY + h;
 									if(mProperties.mHasZComponent)
 										positionTarget[c++] = mShiftZ;
 									positionTarget[c++] = uX + w;
 									positionTarget[c++] = uY + h;
 									if(mProperties.mHasZComponent)
 										positionTarget[c++] = mShiftZ;
 								}
 								if(offsetsTarget!=null) {
 									offsetsTarget[o] = uX;
 								}
 								o++;
 								if(mWorkingColors!=null) {
 									//mWorkingColors[clId++] = mLetterColors[i];
 									mWorkingColors[clId++] = curColor;
 								}
 
 								mRecentCharCount++;
 								curLineCharCount++;
 								lstVal = uVal;
 							}
 						}
 					}
 				}
 			}
 
 			if(val == CHAR_LINEBREAK) {
 				//Line break
 				if(lstVal>0) {
 					if(kerningEnabled) {
 						charX += font.mKerningMaxX[lstVal]+spacing;
 					}else{
 						charX += font.mConstantCharDistance+spacing;
 					}
 				}
 				if(charX>mRecentStringWidth)
 					mRecentStringWidth = charX;
 				lstVal = -1;
 				lstSpaceI = -1;
 				lstSpaceO = -1;
 				charY -= mProperties.mLineHeight;
 				curLineCharCount = 0;
 				mRecentLineCount++;
 				mRecentStringHeight += mProperties.mLineHeight;
 				if(offsetsTarget!=null) {
 					if(mHorizontalAnchor>0) {
 						int k=o-1;
 						final float shift = -(charX+lineShift)*mHorizontalAnchor;
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
 		final int len = mCapacity*(mProperties.mPosDim)*4;
 		if(mConstantPositions==null || mConstantPositions.length<len)
 			mConstantPositions = new float[len];
 		createStringPositions(mConstantPositions,null);
 		applyAnchors(mHorizontalAnchor,mVerticalAnchor,mConstantPositions);
 		return this;
 	}
 
 	protected void applyAnchors(float horizontal,float vertical,float[] positionTarget) {
 		final float shiftX = -horizontal*mRecentStringWidth;
 		final float shiftY = vertical*mRecentStringHeight;
 		int c = 0;
 		for(int i=0;i<mRecentCharCount*4;i++) {
 			positionTarget[c++] += shiftX;
 			positionTarget[c++] += shiftY;
 			if(mProperties.mHasZComponent)
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
 
 	public DrawableString setCentered() {
 		return setAnchors(ANCHOR_CENTER,ANCHOR_MIDDLE);
 	}
 
 	protected void putColors() {
 		if(mWorkingColors==null) {
 			for(int i=0;i<mRecentCharCount;i++) {
 				mProperties.mGraphics.putColorRect(mProperties.mGraphics.mCurColor);
 				mProperties.mGraphics.putSuppDataRect(mProperties.mGraphics.mCurSuppData);
 			}
 		}else{
 			for(int i=0;i<mRecentCharCount;i++) {
 				mProperties.mGraphics.putColorRect(mWorkingColors[i].mValues);
 				mProperties.mGraphics.putSuppDataRect(mProperties.mGraphics.mCurSuppData);
 			}
 		}
 	}
 
 	protected void putVertexProperties() {
 		final IndexedVertexBuffer vertexBuffer = mProperties.mGraphics.mCurrentVertexBuffer;
 		short offset = (short)vertexBuffer.getCurrentVertexWriteCount();
 		for(int i=0;i<mRecentCharCount;i++) {
 			vertexBuffer.beginQuad(false,offset);
 			vertexBuffer.putArray(DefaultGraphics.ID_TEXTURES, mTexCoords[i].mAppliedCoordinates);
 			offset += 4;
 		}
 
 		if(mProperties.mAutoPutColors)
 			putColors();
 	}
 
 	public void draw(YangMatrix transform) {
 		float[] positions;
 		YangMatrix resultTransf;
 		if(mConstantPositions==null){
 			createStringPositions(staticPositions,null);
 			positions = staticPositions;
 			interMatrix.loadIdentity();
 			interMatrix.translate(-mRecentStringWidth*mHorizontalAnchor, mRecentStringHeight*mVerticalAnchor);
 			interMatrix.multiplyLeft(transform);
 			resultTransf = interMatrix;
 		}else{
 			positions = mConstantPositions;
 			resultTransf = transform;
 		}
 
 		final IndexedVertexBuffer vertexBuffer = mProperties.mGraphics.mCurrentVertexBuffer;
 
 		mProperties.mGraphics.mTranslator.bindTexture(mProperties.mFont.mTexture);
 
 		putVertexProperties();
 
 		if(mProperties.mGraphics.mPositionDimension==2)
 			vertexBuffer.putTransformedArray2D(DefaultGraphics.ID_POSITIONS,positions,mRecentCharCount*4,resultTransf.mValues);
 		else
 			vertexBuffer.putTransformedArray3D(DefaultGraphics.ID_POSITIONS,positions,mRecentCharCount*4,resultTransf.mValues);
 	}
 
 	public void draw(float x,float y,float lineHeight) {
 		interMatrix2.loadIdentity();
 		interMatrix2.translate(x, y);
 		interMatrix2.scale(mProperties.mLineHeight*lineHeight*mSize);
 		draw(interMatrix2);
 	}
 
 	public void draw(float x,float y,float lineHeight,float rotation) {
 		interMatrix2.loadIdentity();
 		interMatrix2.translate(x, y);
 		interMatrix2.scale(lineHeight*mSize);
 		interMatrix2.rotateZ(rotation);
 		draw(interMatrix2);
 	}
 
 	public void draw() {
 		interMatrix2.loadIdentity();
 		draw(interMatrix2);
 	}
 
 	public StringProperties cloneProperties(boolean forceClone) {
 		if(forceClone || !mPropertiesCloned) {
 			mProperties = mProperties.clone();
 			mPropertiesCloned = true;
 		}
 		return mProperties;
 	}
 
 	public StringProperties cloneProperties() {
 		return cloneProperties(true);
 	}
 
 	@Override
 	protected void startFormatStringParse() {
 
 	}
 
 	public void allocLetterColors() {
 		if(mWorkingColors==null)
 			mWorkingColors = new FloatColor[mCapacity];
 	}
 
 	@Override
 	protected int handleMacro(String macro, int pos,int lstMacro) {
 		mChars[pos] = CHAR_MACRO;
 		if(macro.equals("\\"))
 			mChars[pos+1] = 0;
 		else{
 			final Integer clIndex = mProperties.mStyle.mColorHash.get(macro);
 			if(clIndex==null)
 				return -1;
 			else
 				mChars[pos+1] = clIndex;
 		}
 		if(mWorkingColors==null)
 			allocLetterColors();
 		return pos+2;
 	}
 
 	@Override
 	protected void endFormatStringParse(int pos,int lstMacro) {
 
 	}
 
 	public DrawableString setLeftTopJustified() {
 		return setAnchors(ANCHOR_LEFT,ANCHOR_TOP);
 	}
 
 	public DrawableString setRightTopJustified() {
 		return setAnchors(ANCHOR_RIGHT,ANCHOR_TOP);
 	}
 
 	public DrawableString setLeftBottomJustified() {
 		return setAnchors(ANCHOR_LEFT,ANCHOR_BOTTOM);
 	}
 
 }
