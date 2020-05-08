 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.Bead;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.data.DataBead;
 
 /**
  * A simple implementation of a multi-channel biquad filter. It calculates
  * coefficients based on three parameters (frequency, Q, and gain - the latter
  * only relevant for EQ and shelving filters), each of which may be specified by
  * a static float or by the output of a UGen.
  * <p>
  * Filter parameters may be set with individual setter functions (
  * {@link #setFreq(float) setFreq}, {@link #setQ(float) setQ}, and
  * {@link #setGain(float) setGain}), or by passing a DataBead with the
  * appropriate properties to {@link #setParams(DataBead) setParams}. (Messaging
  * the filter with a DataBead is equivalent to calling setParams.) Setter
  * methods return the instance, so they may be strung together:
  * <p>
  * <code>filt.setFreq(200).setQ(30).setGain(.4);</code>
  * <p>
  * BiquadFilterMulti can be used with pre-programmed algorithms that calculate
  * coefficients for various filter types. (See {@link #setType(int)} for a list
  * of available types.)
  * <p>
  * BiquadFilterMulti can also implement a user-defined filter algorithm by
  * calling {@link #setCustomType(CustomCoeffCalculator)}.
  * 
  * @author Benito Crawford
  * @version 0.9.5
  */
 public class BiquadFilterMulti extends UGen {
 
 	/**
 	 * Indicates a low-pass filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 */
 	public final static int LP = 0;
 
 	/**
 	 * Indicates a high-pass filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 */
 	public final static int HP = 1;
 
 	/**
 	 * Indicates a band-pass filter with constant skirt gain; coefficients are
 	 * calculated from equations given in "Cookbook formulae for audio EQ biquad
 	 * filter coefficients" by Robert Bristow-Johnson.
 	 */
 	public final static int BP_SKIRT = 2;
 
 	/**
 	 * Indicates a band-pass filter with constant peak gain; coefficients are
 	 * calculated from equations given in "Cookbook formulae for audio EQ biquad
 	 * filter coefficients" by Robert Bristow-Johnson.
 	 */
 	public final static int BP_PEAK = 3;
 
 	/**
 	 * Indicates a notch (band-reject) filter; coefficients are calculated from
 	 * equations given in
 	 * "Cookbook formulae for audio EQ biquad filter coefficients" by Robert
 	 * Bristow-Johnson.
 	 */
 	public final static int NOTCH = 4;
 
 	/**
 	 * Indicates an all-pass filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 */
 	public final static int AP = 5;
 
 	/**
 	 * Indicates a peaking-EQ filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 * 
 	 * <em>untested!</em>
 	 */
 	public final static int PEAKING_EQ = 6;
 
 	/**
 	 * Indicates a low-shelf filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 * 
 	 * <em>untested!</em>
 	 */
 	public final static int LOW_SHELF = 7;
 
 	/**
 	 * Indicates a high-shelf filter; coefficients are calculated from equations
 	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
 	 * Robert Bristow-Johnson.
 	 * 
 	 * <em>untested!</em>
 	 */
 	public final static int HIGH_SHELF = 8;
 
 	/**
 	 * Indicates a user-defined filter; see
 	 * {@link #setCustomType(CustomCoeffCalculator) setCustomType}. This
 	 * constant is not recognized by {@link #setType(int) setType}.
 	 */
 	public final static int CUSTOM_FILTER = 100;
 
 	protected float a0 = 1;
 	protected float a1 = 0;
 	protected float a2 = 0;
 	protected float b0 = 0;
 	protected float b1 = 0;
 	protected float b2 = 0;
 
 	private int channels = 2;
 	protected float freq = 100, q = 1, gain = 0;
 	private int type = -1;
 	protected float samplingfreq, two_pi_over_sf;
 
 	private float[] bo1m, bo2m, bi1m, bi2m;
 
 	protected ValCalculator vc;
 	protected UGen freqUGen, qUGen, gainUGen;
 	protected boolean isFreqStatic, isQStatic, isGainStatic, areAllStatic;
 
 	/**
 	 * Constructor for a multi-channel low-pass biquad filter UGen with the
 	 * specified number of channels.
 	 * 
 	 * @param context
 	 *            The audio context.
 	 * @param channels
 	 *            The number of channels.
 	 */
 	public BiquadFilterMulti(AudioContext context, int channels) {
 		this(context, channels, LP);
 	}
 
 	/**
 	 * Constructor for a multi-channel biquad filter UGen of specified type with
 	 * the specified number of channels. See {@link #setType(int) setType} for a
 	 * list of supported filter types.
 	 * 
 	 * @param context
 	 *            The AudioContext.
 	 * @param channels
 	 *            The number of channels.
 	 * @param itype
 	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
 	 *            {@link #BP_SKIRT}, etc.
 	 */
 	public BiquadFilterMulti(AudioContext context, int channels, int itype) {
 		super(context, channels, channels);
 		this.channels = super.getOuts();
 		bi1m = new float[this.channels];
 		bi2m = new float[this.channels];
 		bo1m = new float[this.channels];
 		bo2m = new float[this.channels];
 		samplingfreq = context.getSampleRate();
 		two_pi_over_sf = (float) (Math.PI * 2 / samplingfreq);
 		setType(itype);
 		setFreq(freq).setQ(q).setGain(gain);
 	}
 
 	/**
 	 * Constructor for a multi-channel biquad filter UGen with the specified
 	 * number of channels and parameters specified by a DataBead.
 	 * 
 	 * @param context
 	 *            The audio context.
 	 * @param channels
 	 *            The number of channels.
 	 * @param params
 	 *            A DataBead specifying parameter values; see
 	 *            {@link #setParams(DataBead)}.
 	 */
 	public BiquadFilterMulti(AudioContext context, int channels, DataBead params) {
 		this(context, channels, LP);
 		setParams(params);
 	}
 
 	/**
 	 * Constructor for a multi-channel biquad filter UGen of specified type,
 	 * with the specified number of channels, and with parameters specified by a
 	 * DataBead.
 	 * 
 	 * @param context
 	 *            The audio context.
 	 * @param channels
 	 *            The number of channels.
 	 * @param itype
 	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
 	 *            {@link #BP_SKIRT}, etc.
 	 * @param params
 	 *            A DataBead specifying parameter values; see
 	 *            {@link #setParams(DataBead)}.
 	 */
 	public BiquadFilterMulti(AudioContext context, int channels, int itype,
 			DataBead params) {
 		this(context, channels, itype);
 		setParams(params);
 	}
 
 	private void checkStaticStatus() {
 		if (isFreqStatic && isQStatic && isGainStatic) {
 			areAllStatic = true;
 			vc.calcVals();
 		} else {
 			areAllStatic = false;
 		}
 	}
 
 	@Override
 	public void calculateBuffer() {
 
 		if (areAllStatic) {
 
 			for (int i = 0; i < channels; i++) {
 				float[] bi = bufIn[i];
 				float[] bo = bufOut[i];
 
 				// first two samples
 				bo[0] = (b0 * bi[0] + b1 * bi1m[i] + b2 * bi2m[i] - a1
 						* bo1m[i] - a2 * bo2m[i])
 						/ a0;
 				bo[1] = (b0 * bi[1] + b1 * bi[0] + b2 * bi1m[i] - a1 * bo[0] - a2
 						* bo1m[i])
 						/ a0;
 
 				// main loop
 				for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
 
 					bo[currsamp] = (b0 * bi[currsamp] + b1 * bi[currsamp - 1]
 							+ b2 * bi[currsamp - 2] - a1 * bo[currsamp - 1] - a2
 							* bo[currsamp - 2])
 							/ a0;
 				}
 
 				// get 2 samples of "memory" between sample vectors
 				bi2m[i] = bi[bufferSize - 2];
 				bi1m[i] = bi[bufferSize - 1];
 				bo2m[i] = bo[bufferSize - 2];
 
 				// and check to make sure filter didn't blow up
 				if (Float.isNaN(bo1m[i] = bo[bufferSize - 1]))
 					reset();
 
 			}
 
 		} else {
 
 			freqUGen.update();
 			qUGen.update();
 			gainUGen.update();
 
 			// first two samples
 			freq = freqUGen.getValue(0, 0);
 			q = qUGen.getValue(0, 0);
 			gain = gainUGen.getValue(0, 0);
 			vc.calcVals();
 
 			for (int i = 0; i < channels; i++) {
 				bufOut[i][0] = (b0 * bufIn[i][0] + b1 * bi1m[i] + b2 * bi2m[i]
 						- a1 * bo1m[i] - a2 * bo2m[i])
 						/ a0;
 			}
 
 			freq = freqUGen.getValue(0, 1);
 			q = qUGen.getValue(0, 1);
 			gain = gainUGen.getValue(0, 1);
 			vc.calcVals();
 			for (int i = 0; i < channels; i++) {
 				bufOut[i][1] = (b0 * bufIn[i][1] + b1 * bufIn[i][0] + b2
 						* bi1m[i] - a1 * bufOut[i][0] - a2 * bo1m[i])
 						/ a0;
 			}
 
 			// main loop
 			for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
 				freq = freqUGen.getValue(0, currsamp);
 				q = qUGen.getValue(0, currsamp);
 				gain = gainUGen.getValue(0, currsamp);
 				vc.calcVals();
 
 				for (int i = 0; i < channels; i++) {
 					bufOut[i][currsamp] = (b0 * bufIn[i][currsamp] + b1
 							* bufIn[i][currsamp - 1] + b2
 							* bufIn[i][currsamp - 2] - a1
 							* bufOut[i][currsamp - 1] - a2
 							* bufOut[i][currsamp - 2])
 							/ a0;
 				}
 
 			}
 
 			for (int i = 0; i < channels; i++) {
 				// get 2 samples of "memory" between sample vectors
 				bi2m[i] = bufIn[i][bufferSize - 2];
 				bi1m[i] = bufIn[i][bufferSize - 1];
 				bo2m[i] = bufOut[i][bufferSize - 2];
 
 				// and check to make sure filter didn't blow up
 				if (Float.isNaN(bo1m[i] = bufOut[i][bufferSize - 1]))
 					reset();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Resets the filter in case it "explodes".
 	 */
 	public void reset() {
 		for (int i = 0; i < channels; i++) {
 			bi1m[i] = 0;
 			bi2m[i] = 0;
 			bo1m[i] = 0;
 			bo2m[i] = 0;
 		}
 	}
 
 	protected class ValCalculator {
 		public void calcVals() {
 		};
 	}
 
 	private class LPValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			float cosw = (float) Math.cos(w);
 			float a = (float) Math.sin(w) / q * .5f;
 			b1 = 1 - cosw;
 			b2 = b0 = b1 * .5f;
 			a0 = 1 + a;
 			a1 = -2 * cosw;
 			a2 = 1 - a;
 		}
 	}
 
 	private class HPValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			float cosw = (float) Math.cos(w);
 			float a = (float) Math.sin(w) / q * .5f;
 			b1 = -1 - cosw;
 			b2 = b0 = b1 * -.5f;
 			a0 = 1 + a;
 			a1 = -2 * cosw;
 			a2 = 1 - a;
 		}
 	}
 
 	private class BPSkirtValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			float sinw = (float) Math.sin(w);
 			float a = sinw / q * .5f;
 			b1 = 0;
 			b2 = 0 - (b0 = sinw * .5f);
 			a0 = 1 + a;
 			a1 = -2 * (float) Math.cos(w);
 			a2 = 1 - a;
 		}
 	}
 
 	private class BPPeakValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			// float a = (float) Math.sin(w) / q * .5f;
 			b1 = 0;
 			b2 = 0 - (b0 = (float) Math.sin(w) / q * .5f);
 			a0 = 1 + b0;
 			a1 = -2 * (float) Math.cos(w);
 			a2 = 1 - b0;
 		}
 	}
 
 	private class NotchValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			float a = (float) Math.sin(w) / q * .5f;
 			b2 = b0 = 1;
 			a1 = b1 = -2 * (float) Math.cos(w);
 			a0 = 1 + a;
 			a2 = 1 - a;
 		}
 	}
 
 	private class APValCalculator extends ValCalculator {
 		public void calcVals() {
 			float w = two_pi_over_sf * freq;
 			float a = (float) (Math.sin(w) / q * .5);
 			a2 = b0 = 1 - a;
 			a1 = b1 = (float) (-2 * Math.cos(w));
 			a0 = b2 = 1 + a;
 		}
 	}
 
 	private class PeakingEQValCalculator extends ValCalculator {
 		public void calcVals() {
 			float A = (float) Math.pow(10, gain * .025);
 			float w = two_pi_over_sf * freq;
 			// float cosw = (float) Math.cos(w);
 			float a = (float) (Math.sin(w) / q * .5);
 			b2 = 2 - (b0 = 1 + a * A);
 			a1 = b1 = -2 * (float) Math.cos(w);
 			a2 = 2 - (a0 = 1 + a / A);
 			/*
 			 * peakingEQ: H(s) = (s^2 + s*(A/Q) + 1) / (s^2 + s/(A*Q) + 1)
 			 * 
 			 * b0 = 1 + alpha*A b1 = -2*cos(w0) b2 = 1 - alpha*A a0 = 1 +
 			 * alpha/A a1 = -2*cos(w0) a2 = 1 - alpha/A
 			 */
 		}
 	}
 
 	private class LowShelfValCalculator extends ValCalculator {
 		public void calcVals() {
 			float A = (float) Math.pow(10, gain * .025);
 			float w = two_pi_over_sf * freq;
 			float cosw = (float) Math.cos(w);
 			float a = (float) (Math.sin(w) / q * .5);
 			float b = 2 * a * (float) Math.sqrt(A);
 			float c = (A - 1) * cosw;
 			b0 = A * (A + 1 - c + b);
 			b1 = 2 * A * ((A - 1) - (A + 1) * cosw);
 			b2 = A * (A + 1 - c - b);
 			a0 = A + 1 + c + b;
 			a1 = -2 * ((A - 1) + (A + 1) * cosw);
 			a2 = A + 1 + c - b;
 			/*
 			 * lowShelf: H(s) = A * (s^2 + (sqrt(A)/Q)*s + A)/(A*s^2 +
 			 * (sqrt(A)/Q)*s + 1)
 			 * 
 			 * b0 = A*( (A+1) - (A-1)*cos(w0) + 2*sqrt(A)*alpha ) b1 = 2*A*(
 			 * (A-1) - (A+1)*cos(w0) ) b2 = A*( (A+1) - (A-1)*cos(w0) -
 			 * 2*sqrt(A)*alpha ) a0 = (A+1) + (A-1)*cos(w0) + 2*sqrt(A)*alpha a1
 			 * = -2*( (A-1) + (A+1)*cos(w0) ) a2 = (A+1) + (A-1)*cos(w0) -
 			 * 2*sqrt(A)*alpha
 			 */
 		}
 	}
 
 	private class HighShelfValCalculator extends ValCalculator {
 		public void calcVals() {
 			float A = (float) Math.pow(10, gain * .025);
 			float w = two_pi_over_sf * freq;
 			float cosw = (float) Math.cos(w);
 			float a = (float) (Math.sin(w) / q * .5);
 			float b = 2 * a * (float) Math.sqrt(A);
 			float c = (A - 1) * cosw;
 
 			b0 = A * (A + 1 - c + b);
 			b1 = -2 * A * ((A - 1) - (A + 1) * cosw);
 			b2 = A * (A + 1 - c - b);
 			a0 = A + 1 + c + b;
 			a1 = 2 * (A - 1 + (A + 1) * cosw);
 			a2 = A + 1 + c - b;
 			/*
 			 * highShelf: H(s) = A * (A*s^2 + (sqrt(A)/Q)*s + 1)/(s^2 +
 			 * (sqrt(A)/Q)*s + A)
 			 * 
 			 * b0 = A*( (A+1) + (A-1)*cos(w0) + 2*sqrt(A)*alpha ) b1 =
 			 * -2*A*((A-1) + (A+1)*cos(w0) ) b2 = A*( (A+1) + (A-1)*cos(w0) -
 			 * 2*sqrt(A)*alpha ) a0 = (A+1) - (A-1)*cos(w0) + 2*sqrt(A)*alpha a1
 			 * = 2*( (A-1) - (A+1)*cos(w0) ) a2 = (A+1) - (A-1)*cos(w0) -
 			 * 2*sqrt(A)*alpha
 			 */
 		}
 	}
 
 	/**
 	 * The coeffiecent calculator that interfaces with a
 	 * {@link CustomCoeffCalculator} to allow user-defined filter algorithms.
 	 * 
 	 * @author benito
 	 * @version .9
 	 */
 	private class CustomValCalculator extends ValCalculator {
 		CustomCoeffCalculator ccc;
 
 		CustomValCalculator(CustomCoeffCalculator iccc) {
 			ccc = iccc;
 		}
 
 		public void calcVals() {
 			ccc.calcCoeffs(freq, q, gain);
 			a0 = ccc.a0;
 			a1 = ccc.a1;
 			a2 = ccc.a2;
 			b0 = ccc.b0;
 			b1 = ccc.b1;
 			b2 = ccc.b2;
 		}
 	}
 
 	/**
 	 * Sets the filter parameters with a DataBead.
 	 * <p>
 	 * Use the following properties to specify filter parameters:
 	 * </p>
 	 * <ul>
 	 * <li>"filterType": (int) The filter type.</li>
 	 * <li>"frequency": (float or UGen) The filter frequency.</li>
 	 * <li>"q": (float or UGen) The filter Q-value.</li>
 	 * <li>"gain": (float or UGen) The filter gain.</li>
 	 * </ul>
 	 * 
 	 * @param paramBead
 	 *            The DataBead specifying parameters.
	 * @return This filter instance.
 	 */
 	public BiquadFilterMulti setParams(DataBead paramBead) {
 		if (paramBead != null) {
 			Object o;
 
 			o = paramBead.get("filterType");
 			if (o instanceof Number) {
 				setType(((Number) o).intValue());
 			}
 
 			if ((o = paramBead.get("frequency")) != null) {
 				if (o instanceof UGen) {
 					setFreq((UGen) o);
 				} else {
 					setFreq(paramBead.getFloat("frequency", freq));
 				}
 			}
 
 			if ((o = paramBead.get("q")) != null) {
 				if (o instanceof UGen) {
 					setQ((UGen) o);
 				} else {
 					setQ(paramBead.getFloat("q", q));
 				}
 			}
 
 			if ((o = paramBead.get("gain")) != null) {
 				if (o instanceof UGen) {
 					setGain((UGen) o);
 				} else {
 					setGain(paramBead.getFloat("gain", gain));
 				}
 			}
 		}
 		return this;
 	}
 
 	public void messageReceived(Bead message) {
 		if (message instanceof DataBead) {
 			setParams((DataBead) message);
 		}
 	}
 
 	/**
 	 * Gets a DataBead with the filter's parameters (whether float or UGen),
 	 * stored in the keys "frequency", "q", "gain", and "filterType".
 	 * 
 	 * @return The DataBead with stored parameters.
 	 */
 	public DataBead getParams() {
 		DataBead db = new DataBead();
 
 		if (isFreqStatic) {
 			db.put("frequency", freq);
 		} else {
 			db.put("frequency", freqUGen);
 		}
 
 		if (isQStatic) {
 			db.put("q", q);
 		} else {
 			db.put("q", qUGen);
 		}
 
 		if (isGainStatic) {
 			db.put("gain", gain);
 		} else {
 			db.put("gain", gainUGen);
 		}
 
 		db.put("filterType", type);
 
 		return db;
 	}
 
 	/**
 	 * Gets a DataBead with properties "frequency", "q", and "gain" set to their
 	 * current float values and "type" set appropriately.
 	 * 
 	 * @return The DataBead with static float parameter values.
 	 */
 	public DataBead getStaticParams() {
 		DataBead db = new DataBead();
 		db.put("frequency", freq);
 		db.put("q", q);
 		db.put("gain", gain);
 		db.put("type", type);
 		return db;
 	}
 
 	/**
 	 * Sets the type of filter. To set a custom type, use
 	 * {@link #setCustomType(CustomCoeffCalculator) setCustomType}. The
 	 * following types are recognized:
 	 * <ul>
 	 * <li>{@link #LP} - Low-pass filter.</li>
 	 * <li>{@link #HP} - High-pass filter.</li>
 	 * <li>{@link #BP_SKIRT} - Band-pass filter with constant skirt gain.</li>
 	 * <li>{@link #BP_PEAK} - Band-pass filter with constant peak gain.</li>
 	 * <li>{@link #NOTCH} - Notch (band-reject) filter.</li>
 	 * <li>{@link #AP} - All-pass filter.</li>
 	 * <li>{@link #PEAKING_EQ} - Peaking-EQ filter.</li>
 	 * <li>{@link #LOW_SHELF} - Low-shelf filter.</li>
 	 * <li>{@link #HIGH_SHELF} - High-shelf filter.</li>
 	 * </ul>
 	 * 
 	 * @param ntype
 	 *            The type of filter.
 	 */
 	public BiquadFilterMulti setType(int ntype) {
 		if (ntype != type || vc == null) {
 			int t = type;
 			type = ntype;
 			switch (type) {
 			case LP:
 				vc = new LPValCalculator();
 				break;
 			case HP:
 				vc = new HPValCalculator();
 				break;
 			case BP_SKIRT:
 				vc = new BPSkirtValCalculator();
 				break;
 			case BP_PEAK:
 				vc = new BPPeakValCalculator();
 				break;
 			case NOTCH:
 				vc = new NotchValCalculator();
 				break;
 			case AP:
 				vc = new APValCalculator();
 				break;
 			case PEAKING_EQ:
 				vc = new PeakingEQValCalculator();
 				break;
 			case LOW_SHELF:
 				vc = new LowShelfValCalculator();
 				break;
 			case HIGH_SHELF:
 				vc = new HighShelfValCalculator();
 				break;
 			default:
 				type = t;
 				break;
 			}
 			vc.calcVals();
 		}
 		return this;
 	}
 
 	/**
 	 * Gets the type of the filter.
 	 * 
 	 * @return The filter type.
 	 * @see #setType(int)
 	 */
 	public int getType() {
 		return type;
 	}
 
 	/**
 	 * Gets the current filter frequency.
 	 * 
 	 * @return The filter frequency.
 	 */
 	public float getFreq() {
 		return freq;
 	}
 
 	/**
 	 * Sets the filter frequency to a float value. This will remove the
 	 * frequency UGen, if there is one.
 	 * 
 	 * @param nfreq
 	 *            The frequency.
 	 */
 	public BiquadFilterMulti setFreq(float nfreq) {
 		freq = nfreq;
 		if (isFreqStatic) {
 			freqUGen.setValue(nfreq);
 		} else {
 			freqUGen = new Static(context, nfreq);
 			isFreqStatic = true;
 			checkStaticStatus();
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Sets a UGen to determine the filter frequency.
 	 * 
 	 * @param nfreq
 	 *            The frequency UGen.
 	 */
 	public BiquadFilterMulti setFreq(UGen nfreq) {
 		if (nfreq == null) {
 			setFreq(freq);
 		} else {
 			freqUGen = nfreq;
 			freqUGen.update();
 			freq = freqUGen.getValue();
 			isFreqStatic = false;
 			areAllStatic = false;
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Gets the frequency UGen, if there is one.
 	 * 
 	 * @return The frequency UGen.
 	 */
 	public UGen getFreqUGen() {
 		if (isFreqStatic == true) {
 			return null;
 		} else {
 			return freqUGen;
 		}
 	}
 
 	/**
 	 * Sets the filter Q-value to a float. This will remove the Q UGen if there
 	 * is one.
 	 * 
 	 * @param nqval
 	 *            The Q-value.
 	 */
 	public BiquadFilterMulti setQ(float nqval) {
 		q = nqval;
 		if (isQStatic) {
 			qUGen.setValue(nqval);
 		} else {
 			qUGen = new Static(context, nqval);
 			isQStatic = true;
 			checkStaticStatus();
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Sets a UGen to determine the filter Q-value.
 	 * 
 	 * @param nqval
 	 *            The Q-value UGen.
 	 * @return This BiquadFilter instance.
 	 */
 	public BiquadFilterMulti setQ(UGen nqval) {
 		if (nqval == null) {
 			setQ(q);
 		} else {
 			qUGen = nqval;
 			qUGen.update();
 			q = freqUGen.getValue();
 			isQStatic = false;
 			areAllStatic = false;
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Gets the current Q-value for the filter.
 	 * 
 	 * @return The current Q-value.
 	 */
 	public float getQ() {
 		return q;
 	}
 
 	/**
 	 * Gets the Q UGen, if there is one.
 	 * 
 	 * @return The Q UGen.
 	 */
 	public UGen getQUGen() {
 		if (isQStatic) {
 			return null;
 		} else {
 			return qUGen;
 		}
 	}
 
 	/**
 	 * Sets the filter gain to a float. This will remove the gain UGen if there
 	 * is one. (Only relevant for {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and
 	 * {@link #HIGH_SHELF} types.)
 	 * 
 	 * @param ngain
 	 *            The gain in decibels (0 means no gain).
 	 */
 	public BiquadFilterMulti setGain(float ngain) {
 		gain = ngain;
 		if (isGainStatic) {
 			gainUGen.setValue(ngain);
 		} else {
 			gainUGen = new Static(context, ngain);
 			isGainStatic = true;
 			checkStaticStatus();
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Sets a UGen to determine the filter Q-value. (Only relevant for
 	 * {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and {@link #HIGH_SHELF} types.)
 	 * 
 	 * @param ngain
 	 *            The gain UGen, specifying the gain in decibels.
 	 */
 	public BiquadFilterMulti setGain(UGen ngain) {
 		if (ngain == null) {
 			setGain(gain);
 		} else {
 			gainUGen = ngain;
 			gainUGen.update();
 			gain = freqUGen.getValue();
 			isGainStatic = false;
 			areAllStatic = false;
 		}
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * Gets the current gain in decibels for the filter. (Only relevant for
 	 * {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and {@link #HIGH_SHELF} types.)
 	 * 
 	 * @return The current gain.
 	 */
 	public float getGain() {
 		return gain;
 	}
 
 	/**
 	 * Gets the gain UGen, if there is one.
 	 * 
 	 * @return The gain UGen.
 	 */
 	public UGen getGainUGen() {
 		if (isGainStatic) {
 			return null;
 		} else {
 			return gainUGen;
 		}
 	}
 
 	/**
 	 * Sets a user-defined coefficient calculation algorithm. The algorithm is
 	 * defined in a user-defined class that extends
 	 * {@link CustomCoeffCalculator}.
 	 * 
 	 * @param cc
 	 *            The custom coefficient calculator.
 	 */
 
 	public BiquadFilterMulti setCustomType(CustomCoeffCalculator cc) {
 		vc = new CustomValCalculator(cc);
 		vc.calcVals();
 		return this;
 	}
 
 	/**
 	 * CustomCoeffCalculator provides a mechanism to define custom filter
 	 * coefficients for a biquad filter based on frequency and Q. Users can
 	 * create their own coefficient calculator classes by extending this class
 	 * and passing it to a BiquadFilterMulti instance with
 	 * {@link BiquadFilterMulti#setCustomType(CustomCoeffCalculator)
 	 * setCustomType}.
 	 * 
 	 * <p>
 	 * An instance of such a custom class should override
 	 * {@link #calcCoeffs(float, float, float)} to define the coefficient
 	 * calculation algorithm. The floats a0, a1, a2, b0, b1, and b2 should be
 	 * set according to the input parameters freq, q, and gain, as well as the
 	 * useful class variables {@link #sampFreq} and {@link #two_pi_over_sf}.
 	 * </p>
 	 * 
 	 * @author Benito Crawford
 	 * @version .9.1
 	 */
 	public class CustomCoeffCalculator {
 		public float a0 = 1;
 		public float a1 = 0;
 		public float a2 = 0;
 		public float b0 = 0;
 		public float b1 = 0;
 		public float b2 = 0;
 		/**
 		 * The sampling frequency.
 		 */
 		protected float sampFreq;
 		/**
 		 * Two * pi / sampling frequency.
 		 */
 		protected float two_pi_over_sf;
 
 		/**
 		 * Constructor for a given sampling frequency.
 		 * 
 		 * @param sf
 		 *            The sampling frequency, in Hertz.
 		 */
 		CustomCoeffCalculator(float sf) {
 			setSamplingFrequency(sf);
 		}
 
 		/**
 		 * Constructor with default sampling frequency of 44100.
 		 */
 		CustomCoeffCalculator() {
 			setSamplingFrequency(44100);
 		}
 
 		/**
 		 * Sets the sampling frequency.
 		 * 
 		 * @param sf
 		 *            The sampling frequency in Hertz.
 		 */
 		public void setSamplingFrequency(float sf) {
 			sampFreq = sf;
 			two_pi_over_sf = (float) (Math.PI * 2 / sf);
 		}
 
 		/**
 		 * Override this function with code that sets a0, a1, etc.&nbsp;in terms
 		 * of frequency, Q, and sampling frequency.
 		 * 
 		 * @param freq
 		 *            The frequency of the filter in Hertz.
 		 * @param q
 		 *            The Q-value of the filter.
 		 * @param gain
 		 *            The gain of the filter.
 		 */
 		public void calcCoeffs(float freq, float q, float gain) {
 			// override with coefficient calculations
 		}
 	}
 
 }
