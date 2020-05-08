 package com.odistagon.glone;
 
 import java.nio.FloatBuffer;
 import java.util.Calendar;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.RectF;
 
 public class GlStripe
 {
 	private FloatBuffer	m_fbVtxStrp;
 	private FloatBuffer	m_fbTexStrp; 
 	private FloatBuffer	m_fbTexStr0; 
 	private FloatBuffer	m_fbVtxNums;
 	private FloatBuffer	m_fbTexNums; 
 	private FloatBuffer	m_fbVtxMons;
 	private FloatBuffer	m_fbTexMons; 
 	private FloatBuffer	m_fbVtxAbcs;
 	private FloatBuffer	m_fbTexAbcs; 
 	private FloatBuffer	m_fbVtxPros;
 	private FloatBuffer	m_fbTexPros; 
 	private FloatBuffer	m_fbVtxSigs;
 	private FloatBuffer	m_fbTexSigs; 
 //	private FloatBuffer	m_buffColor;
 
 	private static final float	CFTEXCX = 1024f;	// texture width in px
 	private static final float	CFTEXCY = 1024f;	// texture height in px
 	// hour stripe
 	public static final RectF	CRECTF_VTXHUR = new RectF(0.0f, 0.0f, 0.5f, 0.2f);
 	public static final float	CF_VTXHUR_Z = 1.2f;
 	private static final RectF	CRECTF_TEXHUR = new RectF(0.0f / CFTEXCX, 0.0f, 96f / CFTEXCX, 40f / CFTEXCY);
 	private static final RectF	CRECTF_TEXHR0 = new RectF(96f * 1f / CFTEXCX, 0.0f, (96f * 1f + 96f) / CFTEXCX, 40f / CFTEXCY);
 	// numbers
 	public static final RectF	CRECTF_VTXNUM = new RectF(0.0f, 0.0f, 0.2f, 0.15f);
 	public static final float	CF_VTXNUM_Z = 1.2f;
 	private static final RectF	CRECTF_TEXNUM = new RectF(96f * 2f / CFTEXCX, 0.0f, (96f * 2f + 96f) / CFTEXCX, 96f / CFTEXCY);
 	// name of months
 	public static final RectF	CRECTF_VTXMON = new RectF(0.0f, 0.0f, 0.3f, 0.15f);
 	public static final float	CF_VTXMON_Z = 1.2f;
 	private static final RectF	CRECTF_TEXMON = new RectF(96f * 3f / CFTEXCX, 0.0f, (96f * 3f + 96f) / CFTEXCX, 48f / CFTEXCY);
 	// alphabets
 	public static final RectF	CRECTF_VTXABC = new RectF(0.0f, 0.0f, 0.1f, 0.1f);
 	public static final float	CF_VTXABC_Z = 1.2f;
 	private static final RectF	CRECTF_TEXABC = new RectF(96f * 4f / CFTEXCX, 0.0f, (96f * 4f + 32f) / CFTEXCX, 32f / CFTEXCY);
 	// alphabets (proportional)
 	public static final RectF	CRECTF_VTXPRO = new RectF(0.0f, 0.0f, 0.12f, 0.12f);
 	public static final float	CF_VTXPRO_Z = 1.2f;
 	private static final RectF	CRECTF_TEXPRO = new RectF((96f * 4f + 64f) / CFTEXCX, 0.0f, (96f * 4f + 64f + 32f) / CFTEXCX, 32f / CFTEXCY);
 	private static final float	CAF_PROPCY[] = {
 		21f, 21f, 21f, 21f, 20f, 10f, 21f,		// a-g
 		20f, 6f, 11f, 17f, 5f, 28f, 20f,		// h-n
 		21f, 21f, 20f, 10f, 19f, 12f, 21f,		// o-u
 		24f, 28f, 20f, 21f, 20f, 20f,			// v-z, _
 	};
 	// signs
 	public static final RectF	CRECTF_VTXSIG = new RectF(0.0f, 0.0f, 0.06f, 0.15f);
 	public static final float	CF_VTXSIG_Z = 1.2f;
 	private static final RectF	CRECTF_TEXSIG = new RectF((96f * 4f + 32f) / CFTEXCX, 0f / CFTEXCY, (96f * 4f + 32f + 32f) / CFTEXCX, 96f / CFTEXCY);
 
 	public GlStripe() {
 	}
 
 	public static float getVtxHeightOfOneHour() {
 		return	CRECTF_VTXHUR.bottom;	// TODO take scaling into account
 	}
 
 	/** call this in Renderer#onSurfaceCreated()
 	 */
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		// Stripe
 		m_fbVtxStrp = makeVertBuffs(24, CRECTF_VTXHUR, true, CF_VTXHUR_Z, null);
 		m_fbTexStrp = makeTexBuffs(24, CRECTF_TEXHUR, true, null);
 		m_fbTexStr0 = makeTexBuffs(24, CRECTF_TEXHR0, true, null);
 		// Numbers
 		m_fbVtxNums = makeVertBuffs(10, CRECTF_VTXNUM, false, CF_VTXNUM_Z, null);
 		m_fbTexNums = makeTexBuffs(10, CRECTF_TEXNUM, false, null);
 		// Name of months
 		m_fbVtxMons = makeVertBuffs(12, CRECTF_VTXMON, false, CF_VTXMON_Z, null);
 		m_fbTexMons = makeTexBuffs(12, CRECTF_TEXMON, false, null);
 		// Alphabets
 		m_fbVtxAbcs = makeVertBuffs(26 + 6, CRECTF_VTXABC, false, CF_VTXABC_Z, null);
 		m_fbTexAbcs = makeTexBuffs(26 + 6, CRECTF_TEXABC, false, null);
 		// Alphabets (proportional)
 		m_fbVtxPros = makeVertBuffs(26 + 1, CRECTF_VTXPRO, false, CF_VTXABC_Z, CAF_PROPCY);
 		m_fbTexPros = makeTexBuffs(26 + 1, CRECTF_TEXPRO, false, CAF_PROPCY);
 		// Signs
 		m_fbVtxSigs = makeVertBuffs(3, CRECTF_VTXSIG, false, CF_VTXSIG_Z, null);
 		m_fbTexSigs = makeTexBuffs(3, CRECTF_TEXSIG, false, null);
 	}
 
 	/** Makes coords for vertically arranged tile textures
 	 * @param nelems
 	 * @param fvert stores vert LT, RT, LB, RB
 	 * @param ftex stores texture left, top, width of a element, height of a element
 	 * @return
 	 */
 	private static FloatBuffer makeVertBuffs(int nelems, RectF fvert, boolean bstacktiledvtx, float fzarg, float[] afprophs) {
 		int			nidx = 0;
 		float[]		aftemp = new float[nelems * 4 * 3];	// nelems * 4(rectangle) * 3(xyz)
 		for(int i = 0; i < nelems; i++) {
 			float	ftop = fvert.top;
 			float	fbottom = fvert.bottom * (afprophs == null ? 1f : (afprophs[i] / CFTEXCY) / CRECTF_TEXPRO.bottom);
 			if(bstacktiledvtx) {	// make vertically tiled vertices?
 				ftop = (fvert.top + fbottom * (float)i);
 				fbottom = (ftop + fbottom);
 			}
 			// coords: TL TR BL BR
 			aftemp[nidx++] = fvert.left;	aftemp[nidx++] = ftop;		aftemp[nidx++] = fzarg;
 			aftemp[nidx++] = fvert.right;	aftemp[nidx++] = ftop;		aftemp[nidx++] = fzarg;
 			aftemp[nidx++] = fvert.left;	aftemp[nidx++] = fbottom;	aftemp[nidx++] = fzarg;
 			aftemp[nidx++] = fvert.right;	aftemp[nidx++] = fbottom;	aftemp[nidx++] = fzarg;
 		}
 //		for(int i = 0; i < 24; i++) {
 //			Log.d("dump: ", ": [" + i + "](" + aftemp[i * 3 + 0] + ", " + aftemp[i * 3 + 1] + ", " + aftemp[i * 3 + 2]);
 //		}
 		return	GloneUtils.makeFloatBuffer(aftemp);
 	}
 
 	private static FloatBuffer makeTexBuffs(int nelems, RectF ftex, boolean bstacktiledvtx, float[] afprophs) {
 		int			nidx = 0;
 		float[]		aftemp = new float[nelems * 4 * 2];	// nelems * 4 (rectangle) + (x, y)
 		float		ftop = (bstacktiledvtx ? ((float)(nelems - 1)) * ftex.bottom + ftex.top : ftex.top);	// TODO if(bstacktiledvtx && afprophs != null)
 		for(int i = 0; i < nelems; i++) {
 			float	fheight;
 			if(bstacktiledvtx) {
 //				ftop = (((float)(nelems - i - 1)) * ftex.bottom) + ftex.top;
 				fheight = (afprophs == null ? ftex.bottom : afprophs[nelems - i - 1] / CFTEXCY);
 			} else {
 //				ftop = (((float)(i + 0)) * ftex.bottom) + ftex.top;
 				fheight = (afprophs == null ? ftex.bottom : afprophs[i] / CFTEXCY);
 			}
 			aftemp[nidx++] = ftex.left;		aftemp[nidx++] = ftop + fheight;
 			aftemp[nidx++] = ftex.right;	aftemp[nidx++] = ftop + fheight;
 			aftemp[nidx++] = ftex.left;		aftemp[nidx++] = ftop;
 			aftemp[nidx++] = ftex.right;	aftemp[nidx++] = ftop;
 			//
 			ftop += fheight * (bstacktiledvtx ? -1 : +1);
 		}
 		return	GloneUtils.makeFloatBuffer(aftemp);
 	}
 
 	/**
 	 * @param gtz
 	 * @param ltime
 	 * @param fscrhgt logical height of screen
 	 */
 	public void drawStripe(GL10 gl, GloneTz gtz, long ltime, float fscrhgt, boolean bfirst) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_fbVtxStrp);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 //		gl.glColorPointer(4, GL10.GL_FLOAT, 0, m_buffColor);
 //		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 		gl.glTexCoordPointer(2 ,GL10.GL_FLOAT, 0, (bfirst ? m_fbTexStr0 : m_fbTexStrp));
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 
 		// draw
 		float	fhrsfrommn = gtz.getHoursFromMidNight(ltime);				// hour number is useless because sometimes 1 a.m. comes twice
 		gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour()
 				* (fhrsfrommn * -1f), 0.0f);								// apply hour, minute offset
 		// start from a day before
		float	fdstoffset = gtz.getDSTOffsetInTheDay(gtz.getTimeNextDay(ltime, -1));
 		float	fhrs = (24f + fdstoffset);
 		gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour() * (fhrs * -1f), 0.0f);
 		// loop 3 days
 		for(int i = 0; i < 3; i++) {
 			drawAStripe(gl, gtz, fdstoffset);
 			gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour() * 24f, 0.0f);
 			// go to the next day
			fdstoffset = gtz.getDSTOffsetInTheDay(gtz.getTimeNextDay(ltime, i));
 		}
 
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 	private static void drawAStripe(GL10 gl, GloneTz gtz, float fdstoffset) {
 		int	n0 = 0;
 		// draw 0, 1
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4 * 0, 4 * 2);
 		// draw from 2
 		if(fdstoffset > 0) {
 			gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour() * 1f, 0.0f);
 			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4 * 1, 4 * 1);	// duplicate 1
 			gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour() * -1f, 0.0f);
 		} else
 		if(fdstoffset < 0) {
 			// don't cover 1 and restart with 3.
 			n0 = 1;
 		}
 		gl.glTranslatef(0.0f, GlStripe.getVtxHeightOfOneHour() * (fdstoffset), 0.0f);
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4 * (2 + n0), 4 * (24 - 2 - n0));
 	}
 
 	/**
 	 * @param narg
 	 * @param nmindigits minimum digits number (0 fill)
 	 */
 	public void drawNumberString(GL10 gl, int narg, int nmindigits) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_fbVtxNums);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glTexCoordPointer(2 ,GL10.GL_FLOAT, 0, m_fbTexNums);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		int		nd = 0;
 		while(true) {
 			int	n0 = narg % 10;
 			gl.glNormal3f(0, 0, 1.0f);
 			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, n0 * 4, 4);
 			gl.glTranslatef(CRECTF_VTXNUM.right * -1f, 0.0f, 0.0f);
 			nd++;
 			if(narg < 10)
 				break;
 			narg /= 10;
 		}
 		for(int i = nd; i < nmindigits; i++) {
 			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0 * 4, 4);
 			gl.glTranslatef(CRECTF_VTXNUM.right * -1f, 0.0f, 0.0f);
 		}
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 	public void drawMonth(GL10 gl, int narg) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_fbVtxMons);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glTexCoordPointer(2 ,GL10.GL_FLOAT, 0, m_fbTexMons);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		gl.glNormal3f(0, 0, 1.0f);
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, narg * 4, 4);
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 	public void drawSign(GL10 gl, int narg) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_fbVtxSigs);
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glTexCoordPointer(2 ,GL10.GL_FLOAT, 0, m_fbTexSigs);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		gl.glNormal3f(0, 0, 1.0f);
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, narg * 4, 4);
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 	/**
 	 * @param gl
 	 * @param sarg
 	 * @param nmax maximum number of characters drawing
 	 * @param bprop be proportional
 	 */
 	public void drawAbcString(GL10 gl, String sarg, int nmax, boolean bprop) {
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, (bprop ? m_fbVtxPros : m_fbVtxAbcs));
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glTexCoordPointer(2 ,GL10.GL_FLOAT, 0, (bprop ? m_fbTexPros : m_fbTexAbcs));
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		for(int i = 0; i < sarg.length(); i++) {
 			char	c0 = sarg.charAt(i);
 			if(c0 >= 'A' && c0 <= 'Z')
 				c0 = (char)('a' + (c0 - 'A'));	// de-capitalize
 			else if(c0 < 'a' || c0 > 'z')
 				c0 = 'z' + 1;
 			gl.glTranslatef(0.0f, (bprop ? CRECTF_VTXPRO.bottom * ((CAF_PROPCY[c0 - 'a'] / CFTEXCY) / CRECTF_TEXPRO.bottom * -1f) : CRECTF_VTXABC.bottom * -1f), 0.0f);
 			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, (c0 - 'a') * 4, 4);
 			if(i > nmax)	// maximum number of chars
 				break;
 		}
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 }
