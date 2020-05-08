 /**
  * Java Modular Image Synthesis Toolkit (JMIST)
  * Copyright (C) 2008-2013 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.eandb.jmist.framework.tone;
 
 import ca.eandb.jmist.framework.color.CIEXYZ;
import ca.eandb.jmist.math.MathUtil;
 
 /**
  * @author Brad
  *
  */
 public final class ReinhardToneMapperFactory implements ToneMapperFactory {
 
 	/** Serialization version ID. */
 	private static final long serialVersionUID = -8014074363504189066L;
 
	private static final double DEFAULT_KEY = 0.18;

	private static final double DEFAULT_DELTA = MathUtil.EPSILON;

	private final double key;
 
 	private final double delta;
 
	public ReinhardToneMapperFactory(double key, double delta) {
		this.key = key;
 		this.delta = delta;
 	}
 
 	public ReinhardToneMapperFactory() {
		this(DEFAULT_KEY, DEFAULT_DELTA);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ToneMapperFactory#createToneMapper(java.lang.Iterable)
 	 */
 	public ToneMapper createToneMapper(Iterable<CIEXYZ> samples) {
 		double Yavg = 0.0;
 		double Ymax = 0.0;
 		int n = 0;
 		for (CIEXYZ sample : samples) {
 			if (sample != null) {
 				double Y = sample.Y();
 				if (Y > Ymax) {
 					Ymax = Y;
 				}
 				Yavg += Math.log(delta + Y);
 				n++;
 			}
 		}
 		Yavg /= (double) n;
 		Yavg = Math.exp(Yavg) - delta;
 
		return new ReinhardToneMapper(key / Yavg, Ymax);
 	}
 
 }
