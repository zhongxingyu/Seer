 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.csstudio.swt.xygraph.linearscale;
 
 import java.math.BigDecimal;
 import java.util.LinkedList;
 
 /**
  * Tick factory produces the different axis ticks. When specifying a format and
  * given the screen size parameters and range it will return a list of Ticks
  */
 
 public class TickFactory {
 	public enum TickFormatting {
 		/**
 		 * Plain mode no rounding no chopping maximum 6 figures before the 
 		 * fraction point and four after
 		 */
 		plainMode,
 		/**
 		 * Rounded or chopped to the nearest decimal
 		 */
 		roundAndChopMode,
 		/**
 		 * Use Exponent 
 		 */
 		useExponent,
 		/**
 		 * Use SI units (k,M,G,etc.)
 		 */
 		useSIunits,
 		/**
 		 * Use external scale provider
 		 */
 		useCustom;
 	}
 
 	private TickFormatting formatOfTicks;
 	private final static BigDecimal EPSILON = new BigDecimal("1.0E-20");
 	private static final int DIGITS_UPPER_LIMIT = 6; // limit for number of digits to display left of decimal point
 	private static final int DIGITS_LOWER_LIMIT = -6; // limit for number of zeros to display right of decimal point
 	
 	private double graphMin;
 	private double graphMax;
 	private String tickFormat;
 	private IScaleProvider scale;
 	
 	/**
 	 * @param format
 	 */
 	public TickFactory(IScaleProvider scale) {
 		this(TickFormatting.useCustom, scale);
 	}
 
 	/**
 	 * @param format
 	 */
 	public TickFactory(TickFormatting format, IScaleProvider scale) {
 	   formatOfTicks = format;
 	   this.scale = scale;
 	}
 
 	private String getTickString(double value) {
 		
 		if (scale!=null) value = scale.getLabel(value);
 		
 		String returnString = "";
 		switch (formatOfTicks) {
 		case plainMode:
 			returnString = String.format(tickFormat, value);
 			break;
 		case useExponent:
 			returnString = String.format(tickFormat, value);
 			break;
 		case roundAndChopMode:
 			returnString = String.format("%d", Math.round(value));
 			break;
 		case useSIunits:
 			double absValue = Math.abs(value);
 			if (absValue == 0.0) {
 				returnString = String.format("%6.2f", value);
 			} else if (absValue <= 1E-15) {
 				returnString = String.format("%6.2ff", value * 1E15);
 			} else if (absValue <= 1E-12) {
 				returnString = String.format("%6.2fp", value * 1E12);
 			} else if (absValue <= 1E-9) {
 				returnString = String.format("%6.2fn", value * 1E9);
 			} else if (absValue <= 1E-6) {
 				returnString = String.format("%6.2fµ", value * 1E6);
 			} else if (absValue <= 1E-3) {
 				returnString = String.format("%6.2fm", value * 1E3);
 			} else if (absValue < 1E3) {
 				returnString = String.format("%6.2f", value);
 			} else if (absValue < 1E6) {
 				returnString = String.format("%6.2fk", value * 1E-3);
 			} else if (absValue < 1E9) {
 				returnString = String.format("%6.2fM", value * 1E-6);
 			} else if (absValue < 1E12) {
 				returnString = String.format("%6.2fG", value * 1E-9);
 			} else if (absValue < 1E15) {
 				returnString = String.format("%6.2fT", value * 1E-12);
 			} else if (absValue < 1E18)
 				returnString = String.format("%6.2fP", value * 1E-15);
 			break;
 		case useCustom:
 			returnString = scale.format(value);
 			break;
 		}
 		return returnString;
 	}
 
 	/**
 	 * @param x
 	 * @return floor of log 10
 	 */
 	private int log10(BigDecimal x) {
 		int c = x.compareTo(BigDecimal.ONE); 
 		int e = 0;
 		while (c < 0) {
 			e--;
 			x = x.scaleByPowerOfTen(1);
 			c = x.compareTo(BigDecimal.ONE);
 		}
 	
 		c = x.compareTo(BigDecimal.TEN);
 		while (c >= 0) {
 			e++;
 			x = x.scaleByPowerOfTen(-1);
 			c = x.compareTo(BigDecimal.TEN);
 		}
 	
 		return e;
 	}
 
 	private BigDecimal nicenum(BigDecimal x, boolean round) {
 		int expv; /* exponent of x */
 		double f; /* fractional part of x */
 		double nf; /* nice, rounded number */
 		BigDecimal bf;
 
 		expv = log10(x);
 		bf = x.scaleByPowerOfTen(-expv);
 		f = bf.doubleValue();
 		// f = x/Math.pow(10., expv); /* between 1 and 10 */
 		if (round)
 			if (f < 1.5)
 				nf = 1.;
 			else if (f < 3.)
 				nf = 2.;
 			else if (f < 7.)
 				nf = 5.;
 			else
 				nf = 10.;
 		else if (f <= 1.)
 			nf = 1.;
 		else if (f <= 2.)
 			nf = 2.;
 		else if (f <= 5.)
 			nf = 5.;
 		else
 			nf = 10.;
 		return BigDecimal.valueOf(nf).scaleByPowerOfTen(expv);
 	}
 
 
 	/**
 	 * Round numerator down to multiples of denominators 
 	 * @param n numerator
 	 * @param d denominator
 	 * @return
 	 */
 	protected static double roundDown(BigDecimal n, BigDecimal d) {
 		final int ns = n.signum();
 		if (ns == 0)
 			return 0;
 		final int ds = d.signum();
 		if (ds == 0)
 			throw new IllegalArgumentException("Zero denominator is not allowed");
 
 		n = n.abs();
 		d = d.abs();
 		final BigDecimal[] x = n.divideAndRemainder(d);
 		final int xs = x[1].signum();
 		if (xs == 0) {
 			return ns != ds ? -x[0].multiply(d).doubleValue() : x[0].multiply(d).doubleValue();
 		} else if (xs < 0) {
 			throw new IllegalStateException("Cannot happen!");
 		}
 
 		if (ns != ds)
 			return x[0].signum() == 0 ? -d.doubleValue() : -x[0].add(BigDecimal.ONE).multiply(d).doubleValue();
 
 		return x[0].multiply(d).doubleValue();
 	}
 
 	/**
 	 * Round numerator up to multiples of denominators 
 	 * @param n numerator
 	 * @param d denominator
 	 * @return
 	 */
 	protected static double roundUp(BigDecimal n, BigDecimal d) {
 		final int ns = n.signum();
 		if (ns == 0)
 			return 0;
 		final int ds = d.signum();
 		if (ds == 0)
 			throw new IllegalArgumentException("Zero denominator is not allowed");
 
 		n = n.abs();
 		d = d.abs();
 		final BigDecimal[] x = n.divideAndRemainder(d);
 		final int xs = x[1].signum();
 		if (xs == 0) {
 			return ns != ds ? -x[0].multiply(d).doubleValue() : x[0].multiply(d).doubleValue();
 		} else if (xs < 0) {
 			throw new IllegalStateException("Cannot happen!");
 		}
 
 		if (ns != ds)
 			return x[0].signum() == 0 ? 0 : -x[0].multiply(d).doubleValue();
 
 		return x[0].add(BigDecimal.ONE).multiply(d).doubleValue();
 	}
 
 	private void createFormatString(final int precision, final boolean b) {
 		switch (formatOfTicks) {
 		case plainMode:
 			tickFormat = b ? String.format("%%.%de", precision) : String.format("%%.%df", precision);
 			break;
 		case useExponent:
 			tickFormat = String.format("%%.%de", precision);
 			break;
 		default:
 			tickFormat = null;
 			break;
 		}
 	}
 
 	/*
 	 *  TODO include zero tick if range straddles origin
 	 *  Split into lower and upper ranges and use larger range?
 	 *  How do we reconcile with maxTicks?
 	 *  Or check if zero is included first?
 	 */
 
 	private double determineNumTicks(int size, double min, double max, int maxTicks, boolean allowMinMaxOver) {
 		BigDecimal bMin = BigDecimal.valueOf(min);
 		BigDecimal bMax = BigDecimal.valueOf(max);
 		BigDecimal bRange = bMax.subtract(bMin);
 		final boolean isReverse;
 		if (bRange.signum() < 0) {
 			BigDecimal bt = bMin;
 			bMin = bMax;
 			bMax = bt;
 			bRange = bRange.negate();
 			isReverse = true;
 		} else {
 			isReverse = false;
 		}
 	
 		// tick points too dense to do anything
 		if (bRange.compareTo(EPSILON) < 0) {
 			return 0;
 		}
 	
 		bRange = nicenum(bRange, false);
 		BigDecimal bUnit;
 		long n;
 		do { // ensure number of ticks is less or equal to number requested
 			bUnit = nicenum(BigDecimal.valueOf(bRange.doubleValue() / (maxTicks - 1)), true);
 			n = bRange.divideToIntegralValue(bUnit).longValue();
 		} while (n > maxTicks-- && maxTicks > 1);
 
 		double tickUnit = isReverse ? -bUnit.doubleValue() : bUnit.doubleValue();
 		if (allowMinMaxOver) {
 			graphMin = roundDown(bMin, bUnit);
 			graphMax = roundUp(bMax, bUnit);
 		} else {
 			graphMin = min;
 			graphMax = max;
 		}
 		if (isReverse) {
 			double t = graphMin;
 			graphMin = graphMax;
 			graphMax = t;
 		}
 
 		int d = (int) Math.floor(Math.log10(Math.abs(tickUnit))); // number of digits required
 		if (d <= DIGITS_LOWER_LIMIT || d >= DIGITS_UPPER_LIMIT) {
 			int p = (int) Math.max(Math.floor(Math.log10(Math.abs(min))),
					Math.floor(Math.log10(Math.abs(max))));
 			createFormatString(Math.max(p - d, 0), true);
 		} else {
 			createFormatString(Math.max(-d, 0), false);
 		}
 		return tickUnit;
 	}
 
 	/**
 	 * @param displaySize 
 	 * @param min
 	 * @param max
 	 * @param maxTicks
 	 * @param allowMinMaxOver allow min/maximum overwrite
 	 * @param tight if true then remove ticks outside range 
 	 * @return a list of the ticks for the axis
 	 */
 	public LinkedList<Tick> generateTicks(int displaySize, double min, double max, int maxTicks,
 										  boolean allowMinMaxOver, final boolean tight)
 	{
 		LinkedList<Tick> ticks = new LinkedList<Tick>();
 		double tickUnit = determineNumTicks(displaySize, min, max, maxTicks, allowMinMaxOver);
 		double p = graphMin;
 		if (tickUnit > 0) {
 			final double pmax = graphMax + 0.5 * tickUnit;
 			int i = 0;
 			while (p < pmax) {
 				if (!tight || (p >= min && p <= max))
 					if (allowMinMaxOver || p <= max) {
 						Tick newTick = new Tick();
 						newTick.setValue(p);
 						newTick.setText(getTickString(p));
 						ticks.add(newTick);
 					}
 				double newTickValue = graphMin + (++i)*tickUnit;
 				if (p == newTickValue)
 					break;
 				else if (p < graphMax && newTickValue > graphMax)
 					p = graphMax; // if slightly exceed maximum (zero corner case)
 				else
 					p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = tight ? min : ticks.get(0).getValue();
 				double hi = tight ? max : ticks.get(imax - 1).getValue();
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition((t.getValue() - lo) / range);
 				}
 			}
 		} else if (tickUnit < 0) {
 			final double pmin = graphMax + 0.5 * tickUnit;
 			while (p > pmin) {
 				if (!tight || (p >= max && p <= min))
 					if (allowMinMaxOver || p <= max) {
 						Tick newTick = new Tick();
 						newTick.setValue(p);
 						newTick.setText(getTickString(p));
 						ticks.add(newTick);
 					}
 				double newTickValue = p + tickUnit;
 				if (p == newTickValue)
 					break;
 				else if (p < graphMax && newTickValue > graphMax)
 					p = graphMax; // if slightly exceed maximum (zero corner case)
 				else
 					p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = tight ? max : ticks.get(0).getValue();
 				double hi = tight ? min : ticks.get(imax - 1).getValue();
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition(1 - (t.getValue() - lo) / range);
 				}
 			}
 		}
 		return ticks;
 	}
 
 
 	private double determineNumLogTicks(int size, double min, double max, int maxTicks, boolean allowMinMaxOver) {
 		final boolean isReverse = min > max;
 		final int loDecade; // lowest decade (or power of ten)
 		final int hiDecade;
 		if (isReverse) {
 			loDecade = (int) Math.floor(Math.log10(max));
 			hiDecade = (int) Math.ceil(Math.log10(min));
 		} else {
 			loDecade = (int) Math.floor(Math.log10(min));
 			hiDecade = (int) Math.ceil(Math.log10(max));
 		}
 	
 		int decades = hiDecade - loDecade;
 
 		int unit = 0;
 		int n;
 		do {
 			n = decades/++unit;
 		} while (n > maxTicks);	
 
 		double tickUnit = isReverse ? Math.pow(10, -unit) : Math.pow(10, unit);
 		if (allowMinMaxOver) {
 			graphMin = Math.pow(10, loDecade);
 			graphMax = Math.pow(10, hiDecade);
 		} else {
 			graphMin = min;
 			graphMax = max;
 		}
 		if (isReverse) {
 			double t = graphMin;
 			graphMin = graphMax;
 			graphMax = t;
 		}
 	
 		createFormatString((int) Math.max(-Math.floor(loDecade), 0), false);
 		return tickUnit;
 	}
 
 	/**
 	 * @param displaySize 
 	 * @param min
 	 * @param max
 	 * @param maxTicks
 	 * @param allowMinMaxOver allow min/maximum overwrite
 	 * @param tight if true then remove ticks outside range 
 	 * @return a list of the ticks for the axis
 	 */
 	public LinkedList<Tick> generateLogTicks(int displaySize, double min, double max, int maxTicks,
 										  boolean allowMinMaxOver, final boolean tight)
 	{
 		LinkedList<Tick> ticks = new LinkedList<Tick>();
 		double tickUnit = determineNumLogTicks(displaySize, min, max, maxTicks, allowMinMaxOver);
 		double p = graphMin;
 		if (tickUnit > 1) {
 			final double pmax = graphMax * Math.sqrt(tickUnit);
 			while (p < pmax) {
 				if (!tight || (p >= min && p <= max))
 				if (allowMinMaxOver || p <= max) {
 					Tick newTick = new Tick();
 					newTick.setValue(p);
 					newTick.setText(getTickString(p));
 					ticks.add(newTick);
 				}
 				double newTickValue = p * tickUnit;
 				if (p == newTickValue)
 					break;
 				p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = Math.log(tight ? min : ticks.get(0).getValue());
 				double hi = Math.log(tight ? max : ticks.get(imax - 1).getValue());
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition((Math.log(t.getValue()) - lo) / range);
 				}
 			}
 		} else {
 			final double pmin = graphMax * Math.sqrt(tickUnit);
 			while (p > pmin) {
 				if (!tight || (p >= max && p <= min))
 				if (allowMinMaxOver || p <= max) {
 					Tick newTick = new Tick();
 					newTick.setValue(p);
 					newTick.setText(getTickString(p));
 				}
 				double newTickValue = p * tickUnit;
 				if (p == newTickValue)
 					break;
 				p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = Math.log(tight ? max : ticks.get(0).getValue());
 				double hi = Math.log(tight ? min : ticks.get(imax - 1).getValue());
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition(1 - (Math.log(t.getValue()) - lo) / range);
 				}
 			}
 		}
 		return ticks;
 	}
 }
