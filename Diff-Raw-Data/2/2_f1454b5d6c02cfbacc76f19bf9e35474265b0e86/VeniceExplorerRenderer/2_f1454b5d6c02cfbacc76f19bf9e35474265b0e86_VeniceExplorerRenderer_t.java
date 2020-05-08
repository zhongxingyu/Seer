 package com.example.veniceexplorer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 //import android.opengl.GLES20;
 import android.os.Environment;
 import rajawali.renderer.RajawaliRenderer;
 import rajawali.util.RajLog;
 import android.content.Context;
 import rajawali.BaseObject3D;
 import rajawali.lights.PointLight;
 import rajawali.parser.ObjParser;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import rajawali.math.Number3D;
 import rajawali.materials.*;
 import java.util.ArrayList;
 import android.util.Log;
 import android.view.Surface;
 import java.io.IOException;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.AudioManager;
 import android.graphics.SurfaceTexture;
 import rajawali.bounds.*;
 
 public class VeniceExplorerRenderer extends RajawaliRenderer implements
 		OnPreparedListener, OnBufferingUpdateListener, OnCompletionListener,
 		OnErrorListener
 {
 
 	private MediaPlayer				mMediaPlayer;
 	private SurfaceTexture			mTexture;
 	private PointLight				mLight;
 	private ObjParser				mParser;
 	private TextureManager			mTextureManager;
 	private ArrayList<ProjectLevel>	ps;
 	private ArrayList<String>		textureNames;
 	private ArrayList<TextureInfo>	textureInfos;
 	private boolean					izLoaded	= true; // to tell on draw
 														// frame if to load
 														// scene
 	private boolean					doLoad		= true; // no project loaded
 														// ever
 	private TextureInfo				vt;
 	VideoMaterial					vmaterial;
 	private float					fov;
 	private int						curProj		= 0;
 	private SimpleMaterial			sMm;
 	float							camH		= 1.4f;
 
 	public VeniceExplorerRenderer(Context context, float f)
 	{
 		super(context);
 		RajLog.enableDebug(false);
 		textureNames = new ArrayList<String>();
 		textureInfos = new ArrayList<TextureInfo>();
 		fov = f;
 		setFrameRate(30);
 	}
 
 	protected void initScene()
 	{
 		Log.d("main", "scene init");
 		mTextureManager = new TextureManager();
 		// setupVideoTexture();
 		sMm = new SimpleMaterial();
 		mLight = new PointLight();
 		mLight.setColor(1.0f, 1.0f, 1.0f);
 		mLight.setPower(5f);
 		mLight.setAttenuation(500, 1, .09f, .032f);
 		mLight.setPosition(0f, 1.6f, 0f);
 		mCamera.setPosition(0f, camH, 0f);
 		mCamera.setFarPlane(50f);
 		mCamera.setNearPlane(0.1f);
		//mCamera.setFieldOfView(fov);
 	}
 
 	public void setupVideoTexture()
 	{
 
 		vmaterial = new VideoMaterial();
 		vt = mTextureManager.addVideoTexture();
 		int textureid = vt.getTextureId();
 
 		mTexture = new SurfaceTexture(textureid);
 		mMediaPlayer = new MediaPlayer();
 		mMediaPlayer.setOnPreparedListener(this);
 		mMediaPlayer.setOnBufferingUpdateListener(this);
 		mMediaPlayer.setOnCompletionListener(this);
 		mMediaPlayer.setOnErrorListener(this);
 		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		mMediaPlayer.setSurface(new Surface(mTexture));
 		mMediaPlayer.setLooping(true);
 	}
 
 	public void setObjs(ArrayList<ProjectLevel> p)
 	{
 		this.ps = p;
 	}
 
 	public void LoadObjects(ProjectLevel p)
 	{
 		for (int i = 0; i < p.getModels().size(); i++)
 		{
 			mParser = new ObjParser(this, p.getModels().get(i).getModel());
 			mParser.parse();
 			BaseObject3D obj = mParser.getParsedObject();
 			obj.setDepthMaskEnabled(true);
 			obj.setDepthTestEnabled(true);
 			obj.setBlendingEnabled(true);
 			obj.setBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 			if (p.getModels().get(i).isDoubleSided())
 			{
 				obj.setDoubleSided(true);
 			}
 			if (p.getModels().get(i).isVideo())
 			{
 				// Log.d("isvideo", "yeees");
 				// obj.setMaterial(vmaterial);
 				// obj.addTexture(vt);
 			}
 			else
 			{
 				obj.setMaterial(new SimpleMaterial());
 				String tn = p.getModels().get(i).getTexture();
 				if (!textureNames.contains(tn))
 				{
 					textureNames.add(tn);// store texture names for unique
 											// textures
 					int idx = textureNames.indexOf(tn);// get index
 
 					Bitmap mBM = BitmapFactory.decodeFile(Environment
 							.getExternalStorageDirectory() + "/" + tn);
 					TextureInfo ti = mTextureManager.addTexture(mBM);
 					textureInfos.add(idx, ti);// store texture info with same
 												// index as texture name
 					obj.addTexture(ti);
 				}
 				else
 				{
 					int idx = textureNames.indexOf(tn);
 					obj.addTexture(textureInfos.get(idx));
 				}
 			}
 			addChild(obj);
 			BoundingBox bb = obj.getGeometry().getBoundingBox();
 			Number3D mi = bb.getMin();
 			Number3D mx = bb.getMax();
 			Number3D cnt = new Number3D((mi.x + mx.x) / 2, (mi.y + mx.y) / 2,
 					(mi.z + mx.z) / 2);
 			p.getModels().get(i).setCenter(cnt);
 			p.getModels().get(i).setObj(obj);
 		}
 		Log.d("objloader", "objects in scene:" + getNumChildren());
 	}
 
 	public void showProject(int k)
 	{
 		if (doLoad == true || curProj != k)
 		{
 			curProj = k;
 			izLoaded = false;
 			doLoad = false;
 		}
 		/*
 		 * mMediaPlayer.stop(); hideModels(); ProjectLevel p = ps.get(k); for
 		 * (int i = 0; i < p.getModels().size(); i++) {
 		 * p.getModels().get(i).obj.setVisible(true); if
 		 * (p.getModels().get(i).isVideo()) { try {
 		 * 
 		 * mMediaPlayer.setDataSource(Environment .getExternalStorageDirectory()
 		 * + "/" + p.getModels().get(i).getTexture());
 		 * mMediaPlayer.prepareAsync(); Log.d("video", "loading"); } catch
 		 * (IOException e) { Log.d("video", "not loaded"); } } }
 		 */
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config)
 	{
 		super.onSurfaceCreated(gl, config);
 	}
 
 	@Override
 	public void onDrawFrame(GL10 glUnused)
 	{
 		// mTexture.updateTexImage();
 		super.onDrawFrame(glUnused);
 		if (izLoaded == false)
 		{
 			clearScene();
 			loadScene();
 			izLoaded = true;
 		}
 	}
 
 	protected void clearScene()
 	{
 		Log.d("main", "clear scene");
 		clearChildren();
 		mTextureManager.reset();
 		textureNames.clear();
 		textureInfos.clear();
 	}
 
 	protected void loadScene()
 	{
 		LoadObjects(ps.get(curProj));
 	}
 
 	public void setCamLA(float ax, float ay, float az)
 	{
 		float cx = mCamera.getX();
 		float cy = mCamera.getY();
 		float cz = mCamera.getZ();
 		Log.d("pos"," "+cx+"|"+cy+"|"+cz);
 		mCamera.setLookAt(cx - ax, cy + ay, cz - az);
 	}
 	public void setCamPos(float ax, float ay, float az)
 	{
 		mCamera.setPosition(ax, camH, az);
 	}
 	public void onBufferingUpdate(MediaPlayer arg0, int arg1)
 	{
 	}
 
 	public void onPrepared(MediaPlayer mediaplayer)
 	{
 		mMediaPlayer.start();
 	}
 
 	public void onCompletion(MediaPlayer arg0)
 	{
 	}
 
 	public boolean onError(MediaPlayer mp, int what, int extra)
 	{
 		return false;
 	}
 
 	public void onSurfaceDestroyed()
 	{
 		mMediaPlayer.release();
 		super.onSurfaceDestroyed();
 	}
 }
