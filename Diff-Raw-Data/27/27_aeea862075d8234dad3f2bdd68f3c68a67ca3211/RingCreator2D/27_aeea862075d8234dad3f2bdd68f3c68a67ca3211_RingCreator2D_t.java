 package yang.graphics.defaults.geometrycreators;
 
 import yang.graphics.defaults.Default3DGraphics;
 import yang.math.objects.YangMatrix;
 
 public class RingCreator2D extends CylinderCreator {
 
 	public RingCreator2D(Default3DGraphics graphics) {
 		super(graphics);
 	}
 
 	private YangMatrix mTempMat = new YangMatrix();
 
 	@Override
 	public void putPositionsAndIndices(YangMatrix transform,float outerRadius,float innerRadius) {
 		mTempMat.set(transform);
		mTempMat.rotateZ(PI);
 		mTempMat.rotateX(PI*0.5f);
 		mTempMat.scale(1,0);
 
 		super.putPositionsAndIndices(mTempMat, outerRadius, innerRadius);
 	}
 
 }
