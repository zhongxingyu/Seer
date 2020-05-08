 /**
  *
  */
 package ca.eandb.jmist.framework.color.monochrome;
 
 import ca.eandb.jmist.framework.Function1;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.framework.Raster;
 import ca.eandb.jmist.framework.color.CIEXYZ;
 import ca.eandb.jmist.framework.color.Color;
 import ca.eandb.jmist.framework.color.ColorModel;
 import ca.eandb.jmist.framework.color.ColorUtil;
 import ca.eandb.jmist.framework.color.DoubleRaster;
 import ca.eandb.jmist.framework.color.RGB;
 import ca.eandb.jmist.framework.color.Spectrum;
 import ca.eandb.jmist.framework.color.WavelengthPacket;
 import ca.eandb.jmist.math.MathUtil;
 
 /**
  * @author Brad
  *
  */
 public final class MonochromeColorModel extends ColorModel {
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 7290649793402973937L;
 
 	private final double wavelength;
 
 	private final Sample black = new Sample(0);
 
 	private final Sample white = new Sample(1);
 
	private final WavelengthPacket lambda = new WavelengthPacket() {
 		public ColorModel getColorModel() {
 			return MonochromeColorModel.this;
		}
	};
 
 	/**
 	 * @author Brad
 	 *
 	 */
	private final class Sample implements Color, Spectrum {
 
 		/**
 		 * Serialization version ID.
 		 */
 		private static final long serialVersionUID = -5632987671182335721L;
 
 		private final double value;
 
 		public Sample(double value) {
 			this.value = value;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#toXYZ()
 		 */
 		public CIEXYZ toXYZ() {
 			return ColorUtil.convertSample2XYZ(wavelength, value);
 		}
 
 		/* s(non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#toRGB()
 		 */
 		public RGB toRGB() {
 			return ColorUtil.convertSample2RGB(wavelength, value);
 		}
 		
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#abs()
 		 */
 		public Color abs() {
 			return new Sample(Math.abs(value));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#divide(ca.eandb.jmist.framework.color.Color)
 		 */
 		public Color divide(Color other) {
 			return divide((Sample) other);
 		}
 
 		public Sample divide(Sample other) {
 			return new Sample(value / other.value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#divide(double)
 		 */
 		public Color divide(double c) {
 			return new Sample(value / c);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#exp()
 		 */
 		public Color exp() {
 			return new Sample(Math.exp(value));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#invert()
 		 */
 		public Color invert() {
 			return new Sample(1.0 / value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#luminance()
 		 */
 		public double luminance() {
 			return ColorUtil.convertSample2Luminance(wavelength, value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#minus(ca.eandb.jmist.framework.color.Color)
 		 */
 		public Color minus(Color other) {
 			return minus((Sample) other);
 		}
 
 		public Sample minus(Sample other) {
 			return new Sample(value - other.value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#negative()
 		 */
 		public Color negative() {
 			return new Sample(-value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#plus(ca.eandb.jmist.framework.color.Color)
 		 */
 		public Color plus(Color other) {
 			return plus((Sample) other);
 		}
 
 		public Sample plus(Sample other) {
 			return new Sample(value + other.value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#pow(ca.eandb.jmist.framework.color.Color)
 		 */
 		public Color pow(Color other) {
 			return pow((Sample) other);
 		}
 
 		public Sample pow(Sample other) {
 			return new Sample(Math.pow(value, other.value));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#pow(double)
 		 */
 		public Color pow(double e) {
 			return new Sample(Math.pow(value, e));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#sqrt()
 		 */
 		public Color sqrt() {
 			return new Sample(Math.sqrt(value));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#times(ca.eandb.jmist.framework.color.Color)
 		 */
 		public Color times(Color other) {
 			return times((Sample) other);
 		}
 
 		public Sample times(Sample other) {
 			return new Sample(value * other.value);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#times(double)
 		 */
 		public Color times(double c) {
 			return new Sample(value * c);
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#clamp(double, double)
 		 */
 		public Color clamp(double min, double max) {
 			return new Sample(MathUtil.clamp(value, min, max));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#clamp(double)
 		 */
 		public Color clamp(double max) {
 			return new Sample(Math.min(value, max));
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#disperse(int)
 		 */
 		public Color disperse(int channel) {
 			if (channel != 0) {
 				throw new IndexOutOfBoundsException();
 			}
 			return this;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#getColorModel()
 		 */
 		public ColorModel getColorModel() {
 			return MonochromeColorModel.this;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#getValue(int)
 		 */
 		public double getValue(int channel) {
 			if (channel != 0) {
 				throw new IndexOutOfBoundsException();
 			}
 			return value;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#toArray()
 		 */
 		public double[] toArray() {
 			return new double[]{ value };
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Color#getWavelengthPacket()
 		 */
 		public WavelengthPacket getWavelengthPacket() {
 			return lambda;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.color.Spectrum#sample(ca.eandb.jmist.framework.color.WavelengthPacket)
 		 */
 		public Color sample(WavelengthPacket lambda) {
 			return this;
 		}
 
 	}
 
 	/**
 	 * @param wavelength
 	 */
 	public MonochromeColorModel(double wavelength) {
 		this.wavelength = wavelength;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#fromRGB(double, double, double)
 	 */
 	@Override
 	public Spectrum fromRGB(double r, double g, double b) {
 		// TODO choose reflectance at wavelength from RGB triple.
 		return new Sample(g);
 	}
 
 	public Spectrum fromXYZ(double x, double y, double z) {
 		// TODO choose reflectance at wavelength from XYZ triple.
 		return new Sample(y);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#fromSpectrum(ca.eandb.jmist.framework.Function1)
 	 */
 	@Override
 	public Spectrum getContinuous(Function1 spectrum) {
 		return new Sample(spectrum.evaluate(wavelength));
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getBlack()
 	 */
 	@Override
 	public Spectrum getBlack() {
 		return black;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getGray(double)
 	 */
 	@Override
 	public Spectrum getGray(double value) {
 		return new Sample(value);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getUnit()
 	 */
 	@Override
 	public Spectrum getWhite() {
 		return white;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getNumBands()
 	 */
 	@Override
 	public int getNumChannels() {
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#fromChannels(double[], ca.eandb.jmist.framework.color.WavelengthPacket)
 	 */
 	public Color fromArray(double[] values, WavelengthPacket lambda) {
 		if (values.length < 1) {
 			throw new IllegalArgumentException("values.length < 1");
 		}
 		return new Sample(values[0]);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getBlack(ca.eandb.jmist.framework.color.WavelengthPacket)
 	 */
 	@Override
 	public Color getBlack(WavelengthPacket lambda) {
 		return black;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getGray(double, ca.eandb.jmist.framework.color.WavelengthPacket)
 	 */
 	@Override
 	public Color getGray(double value, WavelengthPacket lambda) {
 		return new Sample(value);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getWhite(ca.eandb.jmist.framework.color.WavelengthPacket)
 	 */
 	@Override
 	public Color getWhite(WavelengthPacket lambda) {
 		return white;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#sample(ca.eandb.jmist.framework.Random)
 	 */
 	@Override
 	public Color sample(Random random) {
 		return white;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#createRaster(int, int)
 	 */
 	@Override
 	public Raster createRaster(int width, int height) {
 		return new DoubleRaster(width, height, 1) {
 			private static final long serialVersionUID = -2403693898531527409L;
 			protected Color getPixel(double[] raster, int index) {
 				return new Sample(raster[index]);
 			}
 		};
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.color.ColorModel#getChannelName(int)
 	 */
 	public String getChannelName(int channel) {
 		if (channel != 0) {
 			throw new IllegalArgumentException("Invalid channel");
 		}
 		return String.format("%em", wavelength);
 	}
 
 }
