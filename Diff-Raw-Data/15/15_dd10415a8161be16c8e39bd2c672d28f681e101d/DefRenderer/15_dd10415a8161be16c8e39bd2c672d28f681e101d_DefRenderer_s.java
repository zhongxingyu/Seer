 package com.odistagon.glone;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLSurfaceView.Renderer;
 import android.opengl.GLU;
 import android.opengl.GLUtils;
 import android.util.Log;
 
 public class DefRenderer implements Renderer
 {
 	private int			m_nWidth;
 	private int			m_nHeight;
 	private float		m_frmgn;						// right margin in opengl unit
 
 	private int[]		m_nTextures = new int[1];
 	private int			m_nTextureId = 0;
 
 	private TestCube	m_testcube = null;//new TestCube();
 	private long		m_lLastRendered;
 	private float		m_fStripeScaleH;
 	private GlOneDoc	m_doc;
 	private Gl2DString	m_glstr;
 	private GlStripe	m_glstripe;
 	private boolean		m_bNeedPersSet = true;			// perspective set.
 	private float		m_fHorzShift = 0f;
 	private long		m_lHorzReld = 0L;				// time when horizontal shift touch released
 	private float		m_fFovySrc;						// zoom src
 	private float		m_fFovyDst = 45f;				// FoVY (zoom dst)
 	private long		m_lTimeZoomStart;				// time when zoom started
 	private static final long	CL_ZOOMPERD = 1000L;
 
 	private int			m_nframes;						// fps counter
 	private int			m_nframesprev;					// fps of previous second
 	private long		m_lfpsprev;						// the last time fps counted
 	private int			m_nClockTz;						// which clock kind to be shown?
 
 //	public static float			CF_PERS_FOVY = 45f;
 	public static final float	CF_PERS_NEAR = 2.0f;	// distance from eye point to near plane
 	public static final float	CF_PERS_FAR_ = 6.0f;	// distance from eye point to far plane
 	public static final float	CF_LOOK_EYZ = 4.0f;		// eye point
 
 	private static final long	CL_FRMPRDAC = 16L;		// constant frame period (when active)
 	private static final long	CL_FRMPRDST = 250L;		// constant frame period (when stalled)
 	public static final long	CL_HORZRELD = 800L;		// time to released horizontal shift go back
 	private static final float	CF_RIGHMRGN = 0.1f;		// right margin where stripes not being drawn
 
 	public DefRenderer(GlOneDoc doc) {
 		m_lLastRendered = System.currentTimeMillis();
 		m_fStripeScaleH = 1.0f;
 		m_doc = doc;
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl0, EGLConfig arg1) {
 		gl0.glEnable(GL10.GL_DEPTH_TEST);
 		gl0.glDepthFunc(GL10.GL_LEQUAL);
 
 		m_glstr = new Gl2DString();
 		m_glstr.onSurfaceCreated(gl0, arg1);
 
 		m_glstripe = new GlStripe();
 		m_glstripe.onSurfaceCreated(gl0, arg1);
 
 		makeOrgBuffs();
 
 		// generate texture buffer
 		if (m_nTextureId != 0){
 			gl0.glDeleteTextures(1, m_nTextures, 0);
 		}
 		gl0.glGenTextures(1, m_nTextures, 0);
 		m_nTextureId = m_nTextures[0];
 		gl0.glBindTexture(GL10.GL_TEXTURE_2D, m_nTextureId);
 		// NOTE if image is read from another dpi resource, that will be resized automatically. 
 		Bitmap	bm0 = BitmapFactory.decodeResource(GloneApp.getContext().getResources(), R.drawable.timestr_tex);
 		Log.d(getClass().getName(), "texture size: (u, v each must be x^2) (" + bm0.getWidth() + ", " + bm0.getHeight() + ")");
 		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm0, 0);
 		gl0.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
 		gl0.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
 		bm0.recycle();
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl0, int width, int height) {
 		m_nWidth = width;
 		m_nHeight = height;
 		gl0.glViewport(0, 0, width, height);
 		gl0.glClearColor(0.85f, 0.85f, 0.85f, 1.0f);	// set background color (RGBA)
 
 		m_bNeedPersSet = true;
 	}
 
 	@Override
 	public void onDrawFrame(GL10 gl0) {
 		float	fscrh = calcClipHeight(GlStripe.CF_VTXHUR_Z);	// screen width in opengl unit
 		float	fscrw = calcClipWidth(GlStripe.CF_VTXHUR_Z);	// screen height in opengl unit
 		m_frmgn = (fscrw * CF_RIGHMRGN);
 		if(m_bNeedPersSet) {
 			gl0.glMatrixMode(GL10.GL_PROJECTION);
 			gl0.glLoadIdentity();
 			GLU.gluPerspective(gl0, getCurrentFovy(), (float)m_nWidth / (float)m_nHeight, CF_PERS_NEAR, CF_PERS_FAR_);
 			GLU.gluLookAt(gl0, 0, 0, CF_LOOK_EYZ, 0, 0, 0, 0, 1.0f, 0);
 			m_bNeedPersSet = false;
 		}
 
 		gl0.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 //		gl0.glOrthof(-1, 1, -1 / ratio, 1 / ratio, 0.01f, 100.0f);
 //		gl0.glViewport(0, 0, (int) _width, (int) _height);
 		gl0.glMatrixMode(GL10.GL_MODELVIEW);
 		gl0.glLoadIdentity();
 
 		// make constant fps
 		waitConstant();
 
 		gl0.glEnable(GL10.GL_BLEND);
 		gl0.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
 //		gl0.glShadeModel(GL10.GL_SMOOTH);	// GL_FLAT
 
 		// Lighting
 //		final float[] afLightAmbient = { 1.0f, 1.0f, 1.0f, 0.6f };
 //		final float[] afLightDiffuse = { 1.0f, 1.0f, 0.3f, 0.6f };
 //		final float[] afLightPosition = { 0.0f, 0.0f, 3.0f, 0.6f };
 //		gl0.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, afLightAmbient, 0);
 //		gl0.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, afLightDiffuse, 0);
 //		gl0.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, afLightPosition, 0);
 		gl0.glEnable(GL10.GL_LIGHTING);
 		gl0.glEnable(GL10.GL_LIGHT0);
 //		final float[] matAmbient = { 0.3f, 0.3f, 0.3f, 0.6f };
 //		final float[] matDiffuse = { 0.6f, 0.6f, 0.6f, 0.6f };
 //		gl0.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
 //		gl0.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);
 //		gl0.glEnable(GL10.GL_COLOR_MATERIAL);
 //		gl0.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
 
 		// draw objects
 		ArrayList<GloneTz>	altz = m_doc.getTzList();
 		GloneTz				gtz1 = null;
 		int[]				andt = null;
 		if(altz.size() > 0) {
 			gtz1 = altz.get(0);
 			andt = gtz1.getTimeNumbers(m_doc.getTime());
 		}
 
 		// calc rotation
 		if(m_testcube != null) {
 			gl0.glPushMatrix();
 			gl0.glScalef(0.3f, 0.3f, 0.3f);
 			gl0.glRotatef((((float)andt[4]) / 24f) * 360f, 1f, 0f, 0f);
 			gl0.glTranslatef(-2.0f, -1.5f, -0.5f);
 			// cube
 			m_testcube.draw(gl0);
 			gl0.glPopMatrix();
 		}
 
 		gl0.glDisable(GL10.GL_LIGHTING);
 		gl0.glDisable(GL10.GL_LIGHT0);
 		gl0.glEnable(GL10.GL_BLEND);
 
 		gl0.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
 		gl0.glEnable(GL10.GL_TEXTURE_2D);
 		gl0.glBindTexture(GL10.GL_TEXTURE_2D, m_nTextureId);
 
 		gl0.glPushMatrix();
 		gl0.glScalef(1.0f, m_fStripeScaleH, 1.0f);
 		// stripes
 		Iterator<GloneTz>	it0 = altz.iterator();
 		float				fm0 = calcStripesShiftWidth(fscrw);
 		final int			ncharstz = 8;
 		float				frabc = (fscrh / 2f) / (GlStripe.CRECTF_VTXABC.bottom * (float)ncharstz);
 		float				fhorz = 0f;
 		if(m_lHorzReld == 0) {
 			// dragging
 			fhorz = m_fHorzShift;
 		} else
 		if(m_lHorzReld + CL_HORZRELD > System.currentTimeMillis()) {
 			// released and moving
 			float	f1 = ((float)(CL_HORZRELD - (System.currentTimeMillis() - m_lHorzReld)));
 			fhorz = (m_fHorzShift * (f1 / CL_HORZRELD));
 		} else {
 			// released -> reset
 			m_fHorzShift = 0f;
 			m_lHorzReld = 0L;
 		}
 		gl0.glTranslatef(fscrw / 2f - (m_frmgn + GlStripe.CRECTF_VTXHUR.right)
 				+ fhorz, 0.0f, 0.0f);	// draw from right toward left edge 
 		int		i = 0;
 		while(it0.hasNext()) {
 			GloneTz	gtz0 = it0.next();
 			gl0.glPushMatrix();
 			m_glstripe.drawStripe(gl0, gtz0, m_doc.getTime(), fscrh, (i++ == 0));
 			gl0.glPopMatrix();
 			// timezone names
 			String	s0 = gtz0.getTimeZoneId();
 			gl0.glPushMatrix();
 			gl0.glTranslatef(GlStripe.CRECTF_VTXHUR.right - (frabc / (float)ncharstz / 2f),
					fscrh / 2 - frabc / (float)ncharstz, 0f);
 			gl0.glScalef(frabc, frabc, 1f);
 			m_glstripe.drawAbcString(gl0, s0, ncharstz, true);
 			gl0.glPopMatrix();
 			gl0.glTranslatef(fm0 * -1f, 0.0f, 0.0f);	// -> left
 		}
 		gl0.glPopMatrix();
 
 		gl0.glDisable(GL10.GL_TEXTURE_2D);
 
 		gl0.glLoadIdentity();
 		// org
 		gl0.glPushMatrix();
 		drawOrg(gl0, fscrw, fscrh);
 		gl0.glPopMatrix();
 
 		// date string
 		// day month year
 		if(m_nClockTz != GloneUtils.NC_PREF_CLOCKTZ_NONE) {
 			if(andt == null || m_nClockTz == GloneUtils.NC_PREF_CLOCKTZ_SYST) {
 				andt = m_doc.getSystemTz().getTimeNumbers(m_doc.getTime());
 			}
 
 			gl0.glEnable(GL10.GL_TEXTURE_2D);
 
 			float	flocmgn = 0.2f;
 			float	fscale0 = fscrw / ((GlStripe.CRECTF_VTXNUM.right * (2 + 4)) + GlStripe.CRECTF_VTXMON.right + flocmgn * 2f);
 			gl0.glPushMatrix();
 			gl0.glScalef(fscale0, fscale0, 1.0f);
 			gl0.glTranslatef((GlStripe.CRECTF_VTXNUM.right + GlStripe.CRECTF_VTXMON.right) * -1f, -0.20f, 0f);
 			m_glstripe.drawNumberString(gl0, andt[2], 2);	// day
 			gl0.glTranslatef(GlStripe.CRECTF_VTXNUM.right * 2f + flocmgn, 0f, 0f);
 			m_glstripe.drawMonth(gl0, andt[1] - 1);		// month name
 			gl0.glTranslatef(GlStripe.CRECTF_VTXMON.right + flocmgn + GlStripe.CRECTF_VTXNUM.right * 2f, 0f, 0f);
 			m_glstripe.drawNumberString(gl0, andt[0], 4);	// year
 			gl0.glPopMatrix();
 			// hour + min
 			gl0.glLoadIdentity();
 			fscale0 = fscrw / (GlStripe.CRECTF_VTXNUM.right * 4f + GlStripe.CRECTF_VTXSIG.right * (2 + 1));
 			gl0.glScalef(fscale0, fscale0, 1.0f);
 			gl0.glTranslatef(GlStripe.CRECTF_VTXNUM.right, GlStripe.CRECTF_VTXNUM.bottom * -1f + -0.1f, 0f);
 			m_glstripe.drawNumberString(gl0, andt[5], 2);	// min.
 			gl0.glTranslatef((GlStripe.CRECTF_VTXNUM.right - GlStripe.CRECTF_VTXSIG.right) * 1f, 0f, 0f);
 			m_glstripe.drawSign(gl0, 0);					// :
 			gl0.glTranslatef(GlStripe.CRECTF_VTXNUM.right * -1f, 0f, 0f);
 			m_glstripe.drawNumberString(gl0, andt[4], 2);	// hour
 
 			gl0.glDisable(GL10.GL_TEXTURE_2D);
 		}
 
 		if(GloneApp.getDoc().isDebug()) {
 			// fps
 			countFramesPerSecond();
 			gl0.glLoadIdentity();
 			gl0.glEnable(GL10.GL_TEXTURE_2D);
 			float	fscale0 = fscrw / (GlStripe.CRECTF_VTXNUM.right * 16f);
 			gl0.glScalef(fscale0, fscale0, 1.0f);
 			gl0.glTranslatef(GlStripe.CRECTF_VTXNUM.right * -7f,
 					GlStripe.CRECTF_VTXNUM.bottom * -1f, 0f);
 //					fscrh / 2f - GlStripe.CRECTF_VTXNUM.bottom * +1f, 0f);
 			m_glstripe.drawNumberString(gl0, m_nframesprev, 2);	// fps
 			gl0.glDisable(GL10.GL_TEXTURE_2D);
 
 //			m_glstr.setColor(0xFF0000FF);
 //			// date time string of each tz
 //			gl0.glLoadIdentity();
 //			gl0.glScalef(0.7f, 0.7f, 1.0f);
 //			gl0.glTranslatef(0.4f, 0.1f, 1.2f);
 //			altz = m_doc.getTzList();
 //			it0 = altz.iterator();
 //			while(it0.hasNext()) {
 //				GloneTz	tz0 = it0.next();
 //				String	s0 = Float.toString(tz0.getDSTOffsetInTheDay(GloneApp.getDoc().getTime()));
 //				m_glstr.setTextString(gl0, s0 + "*" + tz0.getDebugString(m_doc.getTime()));
 //				m_glstr.draw(gl0);
 //				gl0.glTranslatef(-0.1f, 0.2f, 0.0f);
 //			}
 		}
 
 		//
 		m_lLastRendered = System.currentTimeMillis();
 
 		gl0.glDisable(GL10.GL_BLEND);
 	}
 
 	private float calcStripesShiftWidth(float fScreenWidth) {
 		float	fm0 = (fScreenWidth - m_frmgn) / GloneApp.getDoc().getTzList().size();
 		if(fm0 > GlStripe.CRECTF_VTXHUR.right) {
 			fm0 = GlStripe.CRECTF_VTXHUR.right;
 		}
 		return	fm0;
 	}
 
 	/**
 	 * @param frelative relative angle that will be added
 	 */
 	public void zoomIn(float frelative) {
 		m_fFovySrc = getCurrentFovy();
 		m_fFovyDst += frelative;
 		if(m_fFovyDst < 20f)
 			m_fFovyDst = 20f;
 		else if(m_fFovyDst > 120f)
 			m_fFovyDst = 120f;
 		m_lTimeZoomStart = System.currentTimeMillis();
 		m_bNeedPersSet = true;
 	}
 
 	public void setClockTz(int nClockTz) {
 		m_nClockTz = nClockTz;
 	}
 
 	private float getCurrentFovy() {
 		if(m_lTimeZoomStart == 0L)
 			return	m_fFovyDst;
 
 		float	fret = m_fFovyDst;
 		long	lnow = System.currentTimeMillis();
 		if(CL_ZOOMPERD > lnow - m_lTimeZoomStart) {
 			float	frate = (float)(lnow - m_lTimeZoomStart) / (float)CL_ZOOMPERD;
 //			Log.d("XXXX", m_fFovySrc + "->" + m_fFovyDst + " * " + frate + " =(" + lnow + "-" + m_lTimeZoomStart + ")/" + (float)CL_ZOOMPERD);
 			fret = m_fFovySrc + (m_fFovyDst - m_fFovySrc) * frate;
 		} else {
 			m_lTimeZoomStart = 0L;
 		}
 		m_bNeedPersSet = true;
 		return	fret;
 	}
 
 	public void addHorizontalShift(float farg) {
 		m_fHorzShift += farg;
 		m_lHorzReld = 0L;
 
 		// change order when distance go over a threshold
 		float		fm0 = calcStripesShiftWidth(calcClipWidth(GlStripe.CF_VTXHUR_Z));
 //		Log.d("XXXX", "add (" + m_fHorzShift + ", " + f0);
 		if(m_fHorzShift > fm0) {
 			m_fHorzShift -= fm0;
 			m_doc.shiftTzOrder(false);
 		} else
 		if(m_fHorzShift < -fm0) {
 			m_fHorzShift += fm0;
 			m_doc.shiftTzOrder(true);
 		}
 	}
 
 	public void releaseHorizontal() {
 		m_lHorzReld = System.currentTimeMillis();
 	}
 
 	/**
 	 * @return view width in pixels
 	 */
 	public int getWidth() {
 		return	m_nWidth;
 	}
 
 	/**
 	 * @return view height in pixels
 	 */
 	public int getHeight() {
 		return	m_nHeight;
 	}
 
 	public float calcClipWidth(float fz) {
 		return	calcClipHeight(fz) * (float)m_nWidth / (float)m_nHeight;
 	}
 
 	/** Calculate and return the height in logical unit, how tall things are drew in view.
 	 */
 	public float calcClipHeight(float fz) {
 		float	fradian = (float)Math.PI * ((getCurrentFovy() / 2f) / 180f);
 		float	fret = (float)Math.tan(fradian) * (CF_LOOK_EYZ - fz);
 		return	fret * 2;
 	}
 
 	private void countFramesPerSecond() {
 		long	l0 = System.currentTimeMillis();
 		if(m_lfpsprev < (l0 - 1000L)) {
 			m_nframesprev = m_nframes;
 			m_nframes = 0;
 			m_lfpsprev = l0;
 		} else {
 			m_nframes++;
 		}
 	}
 
 	/** Wait constant period to make stable fps. When app is idle, 
 	 * stall fps and conserve battery consumptions.
 	 */
 	private void waitConstant() {
 		long	lwait = CL_FRMPRDAC;
 		// drawing is active?
 		if(m_fHorzShift == 0 && m_lTimeZoomStart == 0
 				&& GloneApp.getDoc().isOnAnimation() == false)
 			lwait = CL_FRMPRDST;		// stalled
 
 		// make constant fps
 		// http://stackoverflow.com/questions/4772693/
 		long	ldt = System.currentTimeMillis() - m_lLastRendered;
 		try {
 			if(ldt < lwait)
 				Thread.sleep(lwait - ldt);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private FloatBuffer	m_buffOrgVerts = null;
 	private FloatBuffer	m_buffOrgColrs = null;
 
 	private void makeOrgBuffs() {
 		// vertices
 		float[]	aftemp = new float[] {
 				-0.9f,  0.0f, GlStripe.CF_VTXHUR_Z,	// LT
 				+0.9f,  0.0f, GlStripe.CF_VTXHUR_Z,	// RT
 				-0.9f, -0.9f, GlStripe.CF_VTXHUR_Z,	// LB
 				+0.9f, -0.9f, GlStripe.CF_VTXHUR_Z,	// RB
 		};
 		m_buffOrgVerts = GloneUtils.makeFloatBuffer(aftemp);
 		// colors RGBA
 		aftemp = new float[] {
 				+0.0f, 0.0f, 0.0f, 0.6f,
 				+0.0f, 0.0f, 0.0f, 0.6f,
 				+0.5f, 0.2f, 0.2f, 0.1f,
 				+0.5f, 0.2f, 0.2f, 0.1f,
 		};
 		m_buffOrgColrs = GloneUtils.makeFloatBuffer(aftemp);
 	}
 
 	/** 
 	 * @param gl
 	 */
 	private void drawOrg(GL10 gl, float fScreenWidth, float fScreenHeight) {
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_buffOrgVerts);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glColorPointer(4, GL10.GL_FLOAT, 0, m_buffOrgColrs);
 		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 		gl.glScalef(fScreenWidth, fScreenHeight, 1f);
 		gl.glNormal3f(0, 0, 1.0f);
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
 	}
 }
