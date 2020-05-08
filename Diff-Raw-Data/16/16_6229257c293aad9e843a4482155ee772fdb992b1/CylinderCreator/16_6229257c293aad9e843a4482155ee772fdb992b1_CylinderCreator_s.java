 package yang.graphics.defaults.geometrycreators;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.defaults.Default3DGraphics;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.math.objects.YangMatrix;
 
 public class CylinderCreator extends GeometryCreator<Default3DGraphics> {
 
 	public int mSamples;
 	public boolean mClosed = true;
 
 	public CylinderCreator(Default3DGraphics graphics) {
 		super(graphics);
 		mSamples = 8;
 	}
 
 	public void putPositionsAndIndices(YangMatrix transform,float bottomRadius,float topRadius) {
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		if(transform==null)
 			transform = YangMatrix.IDENTITY;
 		float alpha = 0;
 		final float omega;
 			if(mClosed)
 				omega = 2*PI/mSamples;
 			else
 				omega = 2*PI/(mSamples-1);
 		final int startIndex = vertexBuffer.getCurrentIndexWriteCount();
 		final int startVertex = vertexBuffer.getCurrentVertexWriteCount();
 		for(int i=0;i<mSamples;i++) {
 			final float x = (float)Math.sin(alpha);
 			final float z = (float)Math.cos(alpha);
 			vertexBuffer.putTransformed3D(DefaultGraphics.ID_POSITIONS, x*bottomRadius, 0, z*bottomRadius, transform.mValues);
 			vertexBuffer.putTransformed3D(DefaultGraphics.ID_POSITIONS, x*topRadius, 1.0f, z*topRadius, transform.mValues);
 			if(i>0) {
 				final int i2 = startVertex+2*i;
 				//vertexBuffer.putRectIndices(2*i-2,(i2%mSamples),i2-1,(i2+1)%mSamples);
 				vertexBuffer.putRectIndices(i2-2,i2,i2-1,i2+1);
 			}
 			alpha += omega;
 		}
 		if(mClosed)
 			vertexBuffer.putRectIndices(startVertex+mSamples*2-2,startVertex,startVertex+mSamples*2-1,startVertex+1);
 //		if(calcNormals)
 //			mGraphics.fillNormals(0);
 	}
 
 	public void putTextureCoordinates() {
 		final float steps = mClosed?1f/mSamples:1f/(mSamples-1);
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		for(int i=0;i<mSamples;i++) {
 			final float x = i*steps;
 			vertexBuffer.putVec4(DefaultGraphics.ID_TEXTURES, 1-x,0,1-x,1);
 		}
 	}
 
 	public void putTextureCoordinates(TextureCoordinatesQuad texCoords) {
 		float steps = mClosed?1f/mSamples:1f/(mSamples-1);
		steps /= texCoords.getBiasedWidth();
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		final float x1 = texCoords.getBiasedLeft();
 		final float y1 = texCoords.getBiasedBottom();
 		final float y2 = texCoords.getBiasedTop();
 
 		for(int i=0;i<mSamples;i++) {
 			final float x = i*steps;
 			vertexBuffer.putVec4(DefaultGraphics.ID_TEXTURES, x1+x,y1,x1+x,y2);
 		}
 	}
 
 	public void putTextureCoordinates(float xShift) {
 		final float steps = mClosed?1f/mSamples:1f/(mSamples-1);
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		for(int i=0;i<mSamples;i++) {
 			float x = i*steps + xShift;
 //			while(x<0)
 //				x += 1;
 //			while(x>1)
 //				x -= 1;
 			vertexBuffer.putVec4(DefaultGraphics.ID_TEXTURES, 1-x,0,1-x,1);
 		}
 	}
 
 	public void drawCylinder(YangMatrix transform) {
 		putPositionsAndIndices(transform,1,1);
 	}
 
 	public void putColor(float[] color) {
 		mGraphics.mCurrentVertexBuffer.putArrayMultiple(DefaultGraphics.ID_COLORS, color, mSamples*2);
 	}
 
 	public void putSuppData(float[] suppData) {
 		mGraphics.mCurrentVertexBuffer.putArrayMultiple(DefaultGraphics.ID_SUPPDATA, suppData, mSamples*2);
 	}
 
 	public int getVertexCount() {
 		return mSamples*2;
 	}
 
 	public void putStartEndColors(float[] startColor,float[] endColor) {
 		final IndexedVertexBuffer vertexBuffer = mGraphics.getCurrentVertexBuffer();
 		for(int i=0;i<mSamples;i++) {
 			vertexBuffer.putArray(DefaultGraphics.ID_COLORS, startColor);
 			vertexBuffer.putArray(DefaultGraphics.ID_COLORS, endColor);
 		}
 	}
 
 }
