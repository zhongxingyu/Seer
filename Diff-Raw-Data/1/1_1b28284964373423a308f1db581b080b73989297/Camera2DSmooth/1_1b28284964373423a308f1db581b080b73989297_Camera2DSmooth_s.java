 package yang.graphics.util;
 
 import yang.graphics.camera.Camera2D;
 import yang.math.objects.Point3f;
 
 
 /**
  * @author Xider
  *
  */
 public class Camera2DSmooth extends Camera2D {
 
 	private static final float MAX_ZOOM = 30;
 	private static final float MIN_ZOOM = 1;
 	private static final float ZOOM_STEP = 1;
 	private Point3f mTarPos;
 
 	private float mTarZoom;
 	public float mAdaption;
 
 	public Camera2DSmooth(float startX, float startY, float zoom) {
 		mPosition.set(startX,startY);
 		mTarPos = new Point3f(mPosition);
 
 		this.mZoom = zoom;
 		this.setZoom(zoom);
 
 		mRotation = 0;
 		mAdaption = 0.1f;
 	}
 
 	public Camera2DSmooth() {
 		this(0,0,1);
 	}
 
 	public float getZoom() {
 		return mZoom;
 	}
 
 	public void setZoom(float zoom) {
 		this.mTarZoom = zoom;
 	}
 
 	@Override
 	public float getX(){
 		return mPosition.mX;
 	}
 
 	@Override
 	public float getY(){
 		return mPosition.mY;
 	}
 
 	public float getTargetX(){
 		return mTarPos.mX;
 	}
 
 	public float getTargetY(){
 		return mTarPos.mY;
 	}
 
 	public void setX(float x) {
 		mTarPos.mX = x;
 	}
 
 	public void setY(float y) {
 		mTarPos.mY = y;
 	}
 
 	public void addX(float deltaX) {
 		mTarPos.mX += deltaX;
 	}
 
 	public void addY(float deltaY) {
 		mTarPos.mY += deltaY;
 	}
 
 	public void setPos(float x, float y) {
 		mTarPos.set(x,y);
 	}
 
 	public void setPosInstant(float x, float y) {
 		mPosition.set(x,y);
 		mTarPos.set(x,y);
 	}
 
 	public void move(float deltaX, float deltaY) {
 		mTarPos.mX += deltaX;
 		mTarPos.mY += deltaY;
 	}
 
 	public void moveInstant(float deltaX, float deltaY) {
 		mTarPos.mX += deltaX;
 		mTarPos.mY += deltaY;
 
 		mPosition.mX = mTarPos.mX;
 		mPosition.mY = mTarPos.mY;
 		refreshViewTransform();
 	}
 
 	public void setRotation(float rotation) {
 		mRotation = rotation;
 	}
 
 	public float getRotation() {
 		return mRotation;
 	}
 
 	public void update(){
 		mPosition.lerp(mTarPos, mAdaption);
 		mZoom = (1-mAdaption) * mZoom + (mAdaption)*mTarZoom;
 		refreshViewTransform();
 	}
 
 	public void zoomOut(){
 		mTarZoom += ZOOM_STEP;
 		limitZoom();
 	}
 
 	public void zoomIn(){
 		mTarZoom -= ZOOM_STEP;
 	}
 
 	private void limitZoom(){
 		if(mTarZoom > MAX_ZOOM) mTarZoom = MAX_ZOOM;
 		if(mTarZoom < MIN_ZOOM) mTarZoom = MIN_ZOOM;
 	}
 
 	public void zoom(float factor) {
 		mTarZoom += factor;
 		limitZoom();
 	}
 
 	public boolean isAdjusting() {
 		if(mTarPos.getDistance(mPosition)>0.1f) return true;
 		return false;
 	}
 
 	@Override
 	public void set(float x, float y, float zoom, float rotation) {
 		setPos(x,y);
 		setZoom(zoom);
 		setRotation(rotation);
 	}
 
 	public void set(float x, float y, float zoom) {
 		set(x,y,zoom,0);
 	}
 
 	@Override
 	public void set(Camera2D prefaceCam) {
 		mTarPos.set(prefaceCam.getPositionReference());
 		mTarZoom = prefaceCam.mZoom;
 		mRotation = prefaceCam.mRotation;
 		mPreShiftEnabled = prefaceCam.mPreShiftEnabled;
 		mPreShiftX = prefaceCam.mPreShiftX;
 		mPreShiftY = prefaceCam.mPreShiftY;
 	}
 }
