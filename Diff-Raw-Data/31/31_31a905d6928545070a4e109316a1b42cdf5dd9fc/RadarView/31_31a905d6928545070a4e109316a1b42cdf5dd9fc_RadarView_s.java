 package me.kevinwells.darxen;
 
 import me.kevinwells.darxen.data.RadialDataPacket;
 import me.kevinwells.darxen.loaders.RenderLegend;
 import me.kevinwells.darxen.loaders.RenderLocation;
 import me.kevinwells.darxen.loaders.RenderRadar;
 import me.kevinwells.darxen.model.LegendRenderData;
 import me.kevinwells.darxen.model.LocationRenderData;
 import me.kevinwells.darxen.model.RadarData;
 import me.kevinwells.darxen.model.RadarRenderData;
 import me.kevinwells.darxen.model.RenderData;
 import me.kevinwells.darxen.model.RenderModel;
 import me.kevinwells.darxen.renderables.LegendRenderable;
 import me.kevinwells.darxen.renderables.LocationRenderable;
 import me.kevinwells.darxen.renderables.RadarRenderable;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.MotionEvent;
 
 public class RadarView extends GLSurfaceView implements GestureSurface {
 
 	private LoaderManager mLoaderManager;
 	
 	private RadarData mData;
 	
 	private GestureRecognizer mRecognizer = new GestureRecognizer(this);
 
 	private RadarRenderer mRenderer;
 	private RenderModel mModel;
 	
 	private float[] mTransform = new float[32];
 	private static final int OFFSET_CURRENT = 0;
 	private static final int OFFSET_TEMP = 16;
 	
 	private ViewpointListener mViewpointListener;
 	
 	private static final int TASK_RENDER_RADAR = 100;
 	private static final int TASK_RENDER_LEGEND = 101;
 	private static final int TASK_RENDER_LOCATION = 102;
 
 	public RadarView(Context context, LoaderManager loaderManager) {
 		super(context);
 		mLoaderManager = loaderManager;
 		
 		mModel = new RenderModel();
 		
 		Matrix.setIdentityM(mTransform, OFFSET_CURRENT);
 		scale(1.0f/230.0f);
 		updateTransform();
 		mModel.commit();
 		
 		mRenderer = new RadarRenderer(mModel);
 		
 		//setEGLContextClientVersion(2);
 		setRenderer(mRenderer);
 	}
 	
 	public RenderModel getModel() {
 		return mModel;
 	}
 	
 	private RenderData getData() {
 		return mModel.getWritable();
 	}
 
 	public void setDataFile(RadarData data) {
 		if (mData == data) {
 			return;
 		}
 		
 		if (mData == null) {
 			//set the initial transform
 			Matrix.setIdentityM(mTransform, OFFSET_CURRENT);
 			RadialDataPacket packet = (RadialDataPacket)data.getDataFile().description.symbologyBlock.packets[0];
 			scale(1.0f/packet.rangeBinCount);
 		}
 		
 		mData = data;
 		loadRadar();
 		loadLegend();
 	}
 	
 	public void setLocation(LatLon pos) {
 		updateLocation(pos);
 	}
 	
 	private void updateLocation(LatLon pos) {
 		if (getData().mCenter == null || pos == null) {
 			loadLocation(null);
 			
 		} else {
 			Point2D p = null;
 			p = pos.project(getData().mCenter, null);
 			loadLocation(p);
 		}
 	}
 
 	public void setViewpointListener(ViewpointListener listener) {
 		mViewpointListener = listener;
 	}
 	
 	@Override
 	public void scale(float factor) {
 		Matrix.setIdentityM(mTransform, OFFSET_TEMP);
 		Matrix.scaleM(mTransform, OFFSET_TEMP, factor, factor, 1.0f);
 		Matrix.multiplyMM(mTransform, OFFSET_CURRENT, mTransform, OFFSET_TEMP, mTransform, OFFSET_CURRENT);
 	}
 	
 	@Override
 	public void translate(float dx, float dy) {
 		Matrix.setIdentityM(mTransform, OFFSET_TEMP);
 		Matrix.translateM(mTransform, OFFSET_TEMP, dx, dy, 0.0f);
 		Matrix.multiplyMM(mTransform, OFFSET_CURRENT, mTransform, OFFSET_TEMP, mTransform, OFFSET_CURRENT);
 	}
 	
 	@Override
 	public void onTouchUpdate() {
 		updateTransform();
 		updateViewpoint();
 	}
 	
 	private void updateTransform() {
 		float[] transform = new float[16];
 		for (int i = 0; i < transform.length; i++) {
 			transform[i] = mTransform[OFFSET_CURRENT + i];
 		}
 		
 		mModel.getWritable().setTransform(transform);
 		mModel.commit();
 	}
 	
 	private void updateViewpoint() {
 		if (!Matrix.invertM(mTransform, OFFSET_TEMP, mTransform, OFFSET_CURRENT))
 			return;
 		
 		if (getData().mCenter == null)
 			return;
 
 		float vect[] = new float[4];
 		vect[3] = 1.0f;
 		Matrix.multiplyMV(vect, 0, mTransform, OFFSET_TEMP, vect, 0);
 
 		LatLon viewpoint = LatLon.unproject(new Point2D(vect[0], vect[1]), getData().mCenter, null);
 		
 		if (mViewpointListener != null) {
 			mViewpointListener.onViewpointChanged(viewpoint);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e) {
 		return mRecognizer.onTouchEvent(e);
 	}
 
 	private void loadRadar() {
 		Bundle args = RenderRadar.bundleArgs(mData);
 		mLoaderManager.initLoader(TASK_RENDER_RADAR, args, mTaskLoadRadarCallbacks);
 		
 		if (RenderRadar.getInstance(mLoaderManager, TASK_RENDER_RADAR).getData() != mData) {
 			mLoaderManager.restartLoader(TASK_RENDER_RADAR, args, mTaskLoadRadarCallbacks);
 		}
 	}
 	
     private LoaderManager.LoaderCallbacks<RadarRenderData> mTaskLoadRadarCallbacks =
     		new LoaderManager.LoaderCallbacks<RadarRenderData>() {
 		@Override
 		public Loader<RadarRenderData> onCreateLoader(int id, Bundle args) {
 			return RenderRadar.createInstance(getContext(), args);
 		}
 		@Override
 		public void onLoadFinished(Loader<RadarRenderData> loader, RadarRenderData renderData) {
 			RenderData data = getData();
 
 			if (data.mRadar == null) {
 				data.setRadar(new RadarRenderable(renderData));
 				mModel.commit();
 			} else {
 				data.mRadar.setData(renderData);
 			}
 		}
 		@Override
 		public void onLoaderReset(Loader<RadarRenderData> loader) {
 			getData().mRadar.setData(new RadarRenderData());
 		}
     };
 	
 	private void loadLegend() {
 		Bundle args = RenderLegend.bundleArgs(mData);
 		mLoaderManager.initLoader(TASK_RENDER_LEGEND, args, mTaskLoadLegendCallbacks);
 		
 		if (RenderLegend.getInstance(mLoaderManager, TASK_RENDER_LEGEND).getData() != mData) {
 			mLoaderManager.restartLoader(TASK_RENDER_LEGEND, args, mTaskLoadLegendCallbacks);	
 		}
 	}
 	
     private LoaderManager.LoaderCallbacks<LegendRenderData> mTaskLoadLegendCallbacks =
     		new LoaderManager.LoaderCallbacks<LegendRenderData>() {
 		@Override
 		public Loader<LegendRenderData> onCreateLoader(int id, Bundle args) {
 			return RenderLegend.createInstance(getContext(), args);
 		}
 		@Override
 		public void onLoadFinished(Loader<LegendRenderData> loader, LegendRenderData renderData) {
 			RenderData data = getData();
 			
 			if (data.mLegend == null) {
 				data.setLegend(new LegendRenderable(renderData));
 				mModel.commit();
 			} else {
 				data.mLegend.setData(renderData);	
 			}
 		}
 		@Override
 		public void onLoaderReset(Loader<LegendRenderData> loader) {
 			getData().mLegend.setData(new LegendRenderData());
 		}
     };
     
 	private void loadLocation(Point2D position) {
 		RenderData data = getData();
 		
 		if (data.mLocation == null) {
 			LocationRenderData renderData = new LocationRenderData();
 			renderData.setPosition(position);
 			data.setLocation(new LocationRenderable(renderData));
 			mModel.commit();
 			Bundle args = RenderLocation.bundleArgs(renderData);
 			mLoaderManager.initLoader(TASK_RENDER_LOCATION, args, mTaskLoadLocationCallbacks);
 			
 		} else {
 			if (position != null)
 				data.mLocation.getData().setPosition(position);
 		}
 	}
 	
     private LoaderManager.LoaderCallbacks<LocationRenderData> mTaskLoadLocationCallbacks =
     		new LoaderManager.LoaderCallbacks<LocationRenderData>() {
 		@Override
 		public Loader<LocationRenderData> onCreateLoader(int id, Bundle args) {
 			return RenderLocation.createInstance(getContext(), args);
 		}
 		@Override
 		public void onLoadFinished(Loader<LocationRenderData> loader, LocationRenderData renderData) {
 			getData().mLocation.setData(renderData);
 		}
 		@Override
 		public void onLoaderReset(Loader<LocationRenderData> loader) {
 			getData().mLocation.getData().setPosition(null);
 		}
     };
 
 }
