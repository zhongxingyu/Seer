 package player;
 
 import session.*;
 
 public class VirtualChannel
 {
 	// flags
 	public static final boolean DEBUG_TORTURE_CPU = false;
 	public static final boolean NO_FILTERS = false;
 	public static final float FILTER_NAN_THRESHOLD = 0.99f;
 	
 	// playback info
 	private SessionSample csmp = null;
 	
 	private SessionInstrument cins = null;
 	
 	private VirtualChannel prevSlave = null;
 	private VirtualChannel nextSlave = null;
 	private Player player = null;
 	
 	private PlayerChannel chn = null; // purely to determine ownership
 	
 	private boolean mixing = false;
 	private boolean active = false;
 	
 	private int offs = 0;
 	private int suboffs = 0;
 	private boolean looping = false;
 	private boolean pingpong = false;
 	private boolean reverse = false;
 	
 	private SessionInstrument.Envelope.Handle env_vol = null;
 	private SessionInstrument.Envelope.Handle env_pan = null;
 	private SessionInstrument.Envelope.Handle env_per = null;
 	
 	private int curlplen = 0;
 	private int curlpbeg = 0;
 	
 	private float vol_import = 1.0f;
 	private int vol_env = 64;
 	private int vol_fadeout = 1024;
 	private float vol_calc = 1.0f;
 	
 	private int pan_import = 32;
 	private int pan_env = 32;
 	
 	private int per_import = 0;
 	private int per_env = 32;
 	
 	private int filt_import = 127;
 	private int filt_env = 64;
 	private int filt_res = 0;
 	
 	private float filt_a = 1.0f;
 	private float filt_b = 0.0f;
 	private float filt_c = 0.0f;
 	private boolean filt_needs_calc = false;
 	
 	private float filt_k1l = 0.0f;
 	private float filt_k2l = 0.0f;
 	private float filt_k1r = 0.0f;
 	private float filt_k2r = 0.0f;
 	
 	private boolean note_off = false;
 	private boolean note_fade = false;
 	private boolean foreground = false;
 	
 	public VirtualChannel(Player player)
 	{
 		this.player = player;
 		
 		reset();
 	}
 	
 	public void reset()
 	{
 		csmp = null;
 		cins = null;
 		prevSlave = nextSlave = null;
 		
 		chn = null;
 		
 		mixing = false;
 		active = false;
 		
 		offs = 0;
 		suboffs = 0;
 		looping = false;
 		pingpong = false;
 		reverse = false;
 		
 		env_vol = null;
 		env_pan = null;
 		env_per = null;
 		
 		curlplen = 0;
 		curlpbeg = 0;
 		
 		vol_import = 1.0f;
 		vol_env = 64;
 		vol_fadeout = 1024;
 		vol_calc = 1.0f;
 		
 		pan_import = 32;
 		pan_env = 32;
 		
 		per_import = 0;
 		per_env = 32;
 		
 		filt_import = 127;
 		filt_env = 64;
 		filt_res = 0;
		filt_needs_calc = true;
 		
 		filt_k1l = 0.0f;
 		filt_k2l = 0.0f;
 		filt_k1r = 0.0f;
 		filt_k2r = 0.0f;
 		
 		note_off = false;
 		note_fade = false;
 		foreground = false;
 	}
 	
 	public void enslave(PlayerChannel chn, VirtualChannel other)
 	{
 		if(nextSlave != null)
 			nextSlave.prevSlave = other;
 		
 		if(other != null)
 			other.foreground = false;
 		
 		foreground = true;
 		
 		nextSlave = other;
 		
 		this.chn = chn;
 	}
 	
 	public void detach()
 	{
 		if(prevSlave != null)
 			prevSlave.nextSlave = nextSlave;
 		if(nextSlave != null)
 			nextSlave.prevSlave = prevSlave;
 		
 		nextSlave = prevSlave = null;
 		chn = null;
 		
 		foreground = false;
 		reset();
 	}
 	
 	public void mix(float[][] buf, int offs, int len)
 	{
 		if(!active)
 			return;
 		
 		doMix(buf, offs, len);
 	}
 	
 	public void updateEnvelopes()
 	{
 		if(!active)
 			return;
 		
 		if(env_vol != null)
 			vol_env = env_vol.read();
 		if(env_pan != null)
 			pan_env = env_pan.read() + 32;
 		if(env_per != null)
 		{
 			int pval = env_per.read() + 32;
 			
 			if(env_per.hasFilter())
 			{
 				filt_env = pval + 32;
 				filt_needs_calc = true;
 			} else
 				per_env = pval;
 		}
 		
 		if(note_fade && cins != null)
 		{
 			vol_fadeout -= cins.getFadeout();
 			
 			if(vol_fadeout < 0)
 				vol_fadeout = 0;
 		}
 		
 		if((!note_fade) && env_vol != null && env_vol.fadeCheck(note_off))
 		{
 			//System.out.printf("NOTEFADE volcheck\n");
 			note_fade = true;
 		}
 	}
 	
 	private void calculateVol()
 	{
 		vol_calc = vol_import*((float)(vol_env*vol_fadeout))/((float)(1<<(6+10)));
 	}
 	
 	public float getCalculatedVol()
 	{
 		return vol_calc;
 	}
 	
 	private void calcFilter()
 	{
 		if(!filt_needs_calc)
 			return;
 		
 		filt_needs_calc = false;
 		
 		// calculate filter coefficients
 		filt_a = 1.0f;
 		filt_b = 0.0f;
 		filt_c = 0.0f;
 		if(filt_import != 127 || filt_res != 0 || filt_env != 64)
 		{
 			// Word of Jeff:
 			// d = 2*(damping factor)*(sampling rate)/(natural frequency) + 2*(damping factor)-1. 
 			// e = (sampling rate/natural frequency)^2 
 			//
 			// a = 1/(1+d+e)
 			// b = (d+2e)/(1+d+e)
 			// c = -e/(1+d+e)
 			//
 			// according to the slightly more accurate Word of Jeff (some source code snippets),
 			// there's a weird thing you multiply by. i don't know why.
 			// with that said, those formulae are correct.
 			
 			float r = (float)Math.pow(2.0,(filt_import*filt_env)/(24.0*64.0));
 			r = ((float)player.getFreq()*0.0012166620101443976f)/r;
 			float p = (float)Math.pow(10.0f,((-filt_res*24.0)/(128.0f*20.0f)));
 			
 			float d = 2.0f*p*(r+1.0f)-1.0f;
 			float e = r*r;
 			
 			filt_a = 1.0f/(1.0f+d+e);
 			filt_b = (d+2.0f*e)*filt_a;
 			filt_c = -e*filt_a;
 			
 			// XXX: attempt to de-NaN the code
 			if(filt_b < -FILTER_NAN_THRESHOLD)
 				filt_b = -FILTER_NAN_THRESHOLD;
 			
 			//System.out.printf("%f %f [%f, %f, %f / %f, %f]\n", filt_k1l, filt_k1r, fa, fb, fc, d, e);
 		}
 	}
 	
 	private void doMix(float[][] buf, int boffs, int blen)
 	{
 		//System.out.printf("%.3f %d %s\n", vol_import, per_import, csmp);
 		if(csmp == null)
 			return;
 		
 		// calculate main volume
 		calculateVol();
 		float base_vol = getCalculatedVol();
 		
 		// calculate main speed
 		int base_spd_c = player.calcSlide2(per_import, (per_env-32));
 		float base_spd_x = (float)base_spd_c / (float)player.getFreq();
 		
 		// FIXME: this really should handle this case more gracefully!
 		if(base_spd_x > (csmp.getC5Speed()<<5))
 			base_spd_x = (csmp.getC5Speed()<<5);
 		
 		int base_spd = (int)(base_spd_x * (float)(1<<13));
 		
 		
 		//if(reverse)
 		//	base_spd = -base_spd;
 		
 		// calculate panning
 		float vol1 = base_vol;
 		float vol2 = base_vol;
 		
 		if(player.hasStereo())
 		{
 			int base_pan_c = (pan_import == 100 ? 32 : pan_import) - 32;
 			base_pan_c = base_pan_c + (((32-Math.abs(base_pan_c))*(pan_env-32))>>5);
 			
 			if(base_pan_c < 0)
 				vol2 *= (32+base_pan_c)/32.0f;
 			else
 				vol1 *= (32-base_pan_c)/32.0f;
 			
 			if(pan_import == 100)
 				vol2 = -vol2;
 		}
 		
 		// calculate filter
 		calcFilter();
 		
 		// load some things
 		
 		float[][] data = note_off ? csmp.getDataLoop() : csmp.getDataSustain();
 		float[] dl = data[0];
 		float[] dr = data[1];
 		int length = data[0].length;
 		//System.out.printf("mix %d [%.3f, %.3f]\n", length, vol1, vol2);
 		int xcurlplen = (pingpong ? curlplen*2-1 : curlplen);
 		int lpend = curlpbeg + xcurlplen;
 		int end = boffs + blen;
 		
 		float[] bufl = buf[0];
 		float[] bufr = buf[1];
 		
 		if(curlplen <= 0)
 			looping = false;
 		
 		
 		// TODO: work out the appropriate threshold for de-NaN'ing
 		// would also be wise to fiddle with fa,fb,fc
 		// instead of cluttering up the mix function with if statements
 		/*
 		if(outl >= 1.0f)
 			outl = 1.0f;
 		if(outr >= 1.0f)
 			outr = 1.0f;
 		if(outl <= -1.0f)
 			outl = -1.0f;
 		if(outr <= -1.0f)
 			outr = -1.0f;
 		*/
 		
 		float fa = filt_a;
 		float fb = filt_b;
 		float fc = filt_c;
 		
 		try
 		{
 			if(base_vol == 0.0f)
 			{
 				// vol0 optimisation
 				suboffs += base_spd * blen;
 				offs += suboffs>>13;
 				suboffs &= (1<<13)-1;
 			} else if(NO_FILTERS || ((!DEBUG_TORTURE_CPU) && fa == 1.0f && fb == 0.0f && fc == 0.0f)) {
 				// stereo sample mix
 				for(int i = boffs; i < end; i++)
 				{
 					bufl[i] += dl[offs] * vol1;
 					bufr[i] += dr[offs] * vol2;
 					
 					suboffs += base_spd;
 					offs += suboffs>>13;
 					suboffs &= (1<<13)-1;
 				}	
 			} else if((csmp.getFlags() & SessionSample.SFLG_STEREO) == 0) {
 				// mono sample filter mix
 				for(int i = boffs; i < end; i++)
 				{
 					float outl = dl[offs] * filt_a + filt_k1l * fb + filt_k2l * fc;
 					
 					bufl[i] += outl * vol1;
 					bufr[i] += outl * vol2;
 					
 					filt_k2l = filt_k1l;
 					filt_k1l = outl;
 					
 					suboffs += base_spd;
 					offs += suboffs>>13;
 					suboffs &= (1<<13)-1;
 				}
 				filt_k2r = filt_k2l;
 				filt_k1r = filt_k1l;
 			} else {
 				// stereo sample filter mix
 				vol1 *= filt_a;
 				vol2 *= filt_a;
 				
 				for(int i = boffs; i < end; i++)
 				{
 					float outl = dl[offs] * vol1 + filt_k1l * fb + filt_k2l * fc;
 					float outr = dr[offs] * vol2 + filt_k1r * fb + filt_k2r * fc;
 					
 					bufl[i] += outl;
 					bufr[i] += outr;
 					
 					filt_k2l = filt_k1l;
 					filt_k1l = outl;
 					filt_k2r = filt_k1r;
 					filt_k1r = outr;
 					
 					/*
 					bufl[i] += dl[offs] * vol1;
 					bufr[i] += dr[offs] * vol2;
 					*/
 					
 					suboffs += base_spd;
 					offs += suboffs>>13;
 					suboffs &= (1<<13)-1;
 				}
 			}
 		} catch(ArrayIndexOutOfBoundsException ex) {
 			System.err.printf("ARRAYINDEXOUTOFBOUNDSEXCEPTION - TERMINATING CHANNEL!\n");
 			active = false;
 			return;
 		}
 		
 		// COMPENSATE FOR UNROLLED LOOP
 		if(looping)
 		{
 			if(offs >= lpend)
 			{
 				offs -= curlpbeg;
 				offs %= xcurlplen;
 				offs += curlpbeg;
 			}
 		} else {
 			if(offs >= length)
 				active = false;
 		}
 	}
 	
 	public void retrig(int uoffs)
 	{
 		if(csmp == null)
 			return;
 		
 		offs = uoffs;
 		suboffs = 0;
 		reverse = false;
 		note_off = false;
 		note_fade = false;
 		active = mixing = true;
 		vol_fadeout = 1024;
 		doLoop();
 		doSustainLoop();
 	}
 	
 	public void noteOff()
 	{
 		note_off = true;
 		
 		doLoop();
 		
 		if(env_vol != null)
 			env_vol.noteOff();
 		if(env_pan != null)
 			env_pan.noteOff();
 		if(env_per != null)
 			env_per.noteOff();
 	}
 	
 	public void noteCut()
 	{
 		active = mixing = false;
 		
 		if(env_vol != null)
 			env_vol.stop();
 		if(env_pan != null)
 			env_pan.stop();
 		if(env_per != null)
 			env_per.stop();
 	}
 	
 	public void noteFade()
 	{
 		note_fade = true;
 	}
 	
 	private void doLoop()
 	{
 		looping = pingpong = false;
 		
 		if(csmp != null)
 		{
 			curlpbeg = csmp.getLpBeg();
 			curlplen = csmp.getLpLen();
 			
 			if((csmp.getFlags() & SessionSample.SFLG_LOOP) != 0)
 				looping = true;
 			if((csmp.getFlags() & SessionSample.SFLG_BIDI) != 0)
 				pingpong = true;
 		}
 	}
 	
 	private void doSustainLoop()
 	{
 		if(csmp == null)
 			return;
 		
 		if((csmp.getFlags() & SessionSample.SFLG_SUSLOOP) != 0)
 		{
 			looping = true;
 			curlpbeg = csmp.getSusBeg();
 			curlplen = csmp.getSusLen();
 			pingpong = false;
 			if((csmp.getFlags() & SessionSample.SFLG_SUSBIDI) != 0)
 				pingpong = true;
 		}
 	}
 	
 	public boolean imYoursRight(PlayerChannel chn)
 	{
 		// if it breaks again, use this line instead
 		//return true;
 		return this.chn == chn;
 	}
 	
 	// getters
 	
 	public boolean isForeground()
 	{
 		return foreground;
 	}
 	
 	public boolean isNoteOn()
 	{
 		return !note_off;
 	}
 	
 	public boolean isActive()
 	{
 		return active;
 	}
 	
 	// setters
 	
 	public void importVol(float val)
 	{
 		vol_import = val;
 	}
 	
 	public void importPan(int val)
 	{
 		pan_import = val;
 	}
 	
 	public void importPer(int val)
 	{
 		per_import = val;
 	}
 	
 	public void importFilt(int cut, int res)
 	{
 		if(cut != filt_import || res != filt_res)
 			filt_needs_calc = true;
 		filt_import = cut;
 		filt_res = res;
 	}
 	
 	public void changeInstrument(SessionInstrument cins)
 	{
 		this.cins = cins;
 		
 		if(cins == null)
 			return;
 		
 		env_vol = cins.getVolEnvHandle();
 		env_pan = cins.getPanEnvHandle();
 		env_per = cins.getPerEnvHandle();
 	}
 	
 	public void changeSample(SessionSample csmp)
 	{
 		this.csmp = csmp;
 	}
 }
